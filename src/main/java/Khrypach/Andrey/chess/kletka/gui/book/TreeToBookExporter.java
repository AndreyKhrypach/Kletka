/*
 * Copyright (c) 2025-2026 Andrey Khrypach
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package Khrypach.Andrey.chess.kletka.gui.book;

import Khrypach.Andrey.chess.kletka.gui.model.MoveNode;
import Khrypach.Andrey.chess.kletka.gui.model.ParentNode;
import Khrypach.Andrey.chess.kletka.gui.model.RootNode;
import Khrypach.Andrey.chess.kletka.gui.model.Variation;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.move.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Конвертер дерева вариантов в Polyglot-книгу.
 * <p>
 * Обходит дерево от корня, для каждого хода восстанавливает позицию,
 * вычисляет Zobrist-ключ, кодирует ход и собирает {@link PolyglotEntry}.
 * <p>
 * Экспорт — это <b>личный репертуар</b> пользователя, а не статистическая
 * книга из партий. Все ходы получают одинаковый вес.
 * <p>
 * Дедупликация по (key, move) — требование формата Polyglot.
 */
public final class TreeToBookExporter {

    private static final Logger log = LoggerFactory.getLogger(TreeToBookExporter.class);

    /**
     * Вес по умолчанию для экспортируемых ходов.
     * Не влияет на воспроизведение, используется только при выборе хода.
     */
    private static final int DEFAULT_WEIGHT = 100;

    private TreeToBookExporter() {
        // утилитарный класс
    }

    /**
     * Результат экспорта — для логирования и UI.
     */
    public record ExportResult(int entriesWritten, int duplicatesSkipped) {
        public boolean isEmpty() {
            return entriesWritten == 0;
        }
    }

    /**
     * Экспортирует дерево в Polyglot-книгу.
     *
     * @param rootNode      корневой узел дерева
     * @param initialBoard  начальная доска (может быть null — тогда стандартная)
     * @param startWithBlack если true и initialBoard == null — начинаем с чёрных
     * @param bookPath      путь к .bin файлу (будет создан или перезаписан)
     * @return статистика экспорта
     * @throws IOException при ошибке записи файла
     */
    public static ExportResult exportToBook(RootNode rootNode,
                                            Board initialBoard,
                                            boolean startWithBlack,
                                            Path bookPath) throws IOException {
        if (rootNode == null) {
            throw new IllegalArgumentException("rootNode is null");
        }
        if (bookPath == null) {
            throw new IllegalArgumentException("bookPath is null");
        }

        log.info("[EXPORT] Starting export to {}", bookPath);

        // 1. Готовим начальную доску
        Board startBoard = resolveStartBoard(initialBoard, startWithBlack);

        // 2. Собираем записи
        List<PolyglotEntry> entries = new ArrayList<>();
        Set<KeyMove> seenKeys = new HashSet<>();
        int[] counters = new int[2]; // [0] = written, [1] = duplicates

        collectEntries(rootNode, startBoard, entries, seenKeys, counters);

        log.info("[EXPORT] Collected {} entries ({} duplicates skipped)",
                counters[0], counters[1]);

        if (entries.isEmpty()) {
            log.warn("[EXPORT] No entries to export");
            return new ExportResult(0, counters[1]);
        }

        // 3. Сортируем и пишем
        entries.sort((a, b) -> Long.compareUnsigned(a.key(), b.key()));
        PolyglotBookWriter.createBook(bookPath, entries);

        log.info("[EXPORT] Book written: {}", bookPath);
        return new ExportResult(counters[0], counters[1]);
    }

    /**
     * Рекурсивный обход дерева с накоплением записей.
     */
    private static void collectEntries(ParentNode node,
                                       Board boardBefore,
                                       List<PolyglotEntry> entries,
                                       Set<KeyMove> seen,
                                       int[] counters) {
        if (node == null) return;

        if (node instanceof MoveNode moveNode) {
            // Используем savedFenBefore — надёжнее, чем реконструкция
            long key;
            String fenBefore = moveNode.getSavedFenBefore();
            if (fenBefore != null && !fenBefore.isEmpty()) {
                Board board = new Board();
                board.loadFromFen(fenBefore);
                key = ZobristHasher.calculate(board);
            } else {
                key = ZobristHasher.calculate(boardBefore);
            }

            short moveCode = encodePolyglotMove(moveNode);

            KeyMove km = new KeyMove(key, moveCode);
            if (seen.add(km)) {
                PolyglotEntry entry = new PolyglotEntry(key, moveCode, (short) DEFAULT_WEIGHT, 0, 0);
                entries.add(entry);
                counters[0]++;
            } else {
                counters[1]++;
            }
        }

        // Обход next
        Board boardAfter = boardBefore;
        if (node instanceof MoveNode moveNode) {
            boardAfter = applyMove(boardBefore, moveNode);
        }

        ParentNode next = node.getNext();
        if (next != null && !next.isRoot()) {
            collectEntries(next, boardAfter, entries, seen, counters);
        }

        // ========== ОБХОД subVariations С ФИЛЬТРОМ ДУБЛИКАТОВ ГЛАВНОЙ ЛИНИИ ==========
        if (node.getSubVariations() != null) {
            // Собираем UCI главной линии, чтобы пропустить её копии в subVariations
            String nextUci = (next != null && !next.isRoot()) ? next.getUciMove() : null;

            for (Variation sub : node.getSubVariations()) {
                if (sub == null || sub.isEmpty()) continue;
                ParentNode subFirst = sub.getFirstNode();
                if (subFirst == null || subFirst.isRoot()) continue;

                // ⚠️ ГЛАВНОЕ: пропускаем subVariation, чей первый ход совпадает с next
                // Это "копия главной линии" — её уже обошли через next
                if (nextUci != null && nextUci.equals(subFirst.getUciMove())) {
                    log.debug("[EXPORT] Skip duplicate of main line in subVariations: {} (node={})",
                            subFirst.getSan(),
                            node.isRoot() ? "ROOT" : node.getSan());
                    continue;
                }

                // Дополнительная защита: subFirst.getParent() должен быть == node
                ParentNode parent = subFirst.getParent();
                if (parent != node) {
                    log.warn("[EXPORT] Skip sub '{}': parent mismatch (expected={}, actual={})",
                            sub.getName(),
                            node.getSan(),
                            parent != null ? parent.getSan() : "null");
                    continue;
                }

                collectEntries(subFirst, boardAfter, entries, seen, counters);
            }
        }
    }

    /**
     * Применяет ход к доске, возвращает новую доску.
     */
    private static Board applyMove(Board boardBefore, MoveNode moveNode) {
        Board board = boardBefore.clone();
        try {
            board.doMove(moveNode.getMove());
        } catch (Exception e) {
            log.warn("[EXPORT] Failed to apply move {}: {}",
                    moveNode.getSan(), e.getMessage());
        }
        return board;
    }

    /**
     * Определяет начальную доску экспорта.
     */
    private static Board resolveStartBoard(Board initialBoard, boolean startWithBlack) {
        if (initialBoard != null) {
            return initialBoard.clone();
        }
        Board board = new Board();
        if (startWithBlack) {
            board.setSideToMove(com.github.bhlangonijr.chesslib.Side.BLACK);
        }
        return board;
    }

    /**
     * Кодирует ход в 16-битный формат Polyglot.
     * <pre>
     *   bits 0-5   : to   (0-63)
     *   bits 6-11  : from (0-63)
     *   bits 12-15 : promotion (0=none, 1=N, 2=B, 3=R, 4=Q)
     * </pre>
     */
    private static short encodePolyglotMove(MoveNode moveNode) {
        Move move = moveNode.getMove();
        int from = move.getFrom().ordinal();
        int to = move.getTo().ordinal();
        int promo = getPolyglotPromotionCode(moveNode.getPromotionPiece());
        return (short) ((promo << 12) | (from << 6) | to);
    }

    /**
     * Возвращает код превращения для Polyglot.
     */
    private static int getPolyglotPromotionCode(Piece piece) {
        if (piece == null || piece == Piece.NONE) return 0;
        return switch (piece) {
            case WHITE_KNIGHT, BLACK_KNIGHT -> 1;
            case WHITE_BISHOP, BLACK_BISHOP -> 2;
            case WHITE_ROOK, BLACK_ROOK -> 3;
            case WHITE_QUEEN, BLACK_QUEEN -> 4;
            default -> 0;
        };
    }

    private record KeyMove(long key, short move) {}
}