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

import Khrypach.Andrey.chess.kletka.database.model.GameTree;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.model.MoveNode;
import Khrypach.Andrey.chess.kletka.gui.model.RootNode;
import Khrypach.Andrey.chess.kletka.gui.model.Variation;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.*;

import static Khrypach.Andrey.chess.kletka.gui.book.PolyglotConstants.ENTRY_SIZE;

/**
 * Парсер для Polyglot книг (.bin)
 * Использует MappedByteBuffer (как mmap в Python-chess)
 * БЫСТРАЯ ЗАГРУЗКА — без создания миллионов объектов!
 */
public class PolyglotBookParser {

    private static final Logger log = LoggerFactory.getLogger(PolyglotBookParser.class);
    private final LanguageManager languageManager = LanguageManager.getInstance();

    // MappedByteBuffer для прямого доступа к файлу (как mmap в Python)
    private MappedByteBuffer buffer;
    private int entryCount;

    @Getter
    private int totalEntries = 0;

    /**
     * Парсит .bin файл и возвращает GameTree
     */
    public GameTree parse(Path bookPath) throws IOException {
        log.debug("Parsing Polyglot book: {}", bookPath);

        // 1. Отображаем файл в память (как mmap)
        loadMappedFile(bookPath);
        log.debug("Mapped {} entries", entryCount);

        totalEntries = entryCount;

        // 2. Строим дерево
        GameTree gameTree = buildTreeIterative();

        gameTree.setResult("*");
        gameTree.setStartWithBlack(false);

        log.debug("Book parsing complete. Main line moves: {}",
                gameTree.getMainLine().getMoveCount());

        return gameTree;
    }

    /**
     * Отображает файл в память через MappedByteBuffer (аналог mmap в Python)
     */
    private void loadMappedFile(Path bookPath) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(bookPath.toFile(), "r");
             FileChannel channel = file.getChannel()) {

            long fileSize = channel.size();
            this.entryCount = (int) (fileSize / ENTRY_SIZE);
            log.debug("File size: {} bytes, expected entries: {}", fileSize, entryCount);

            this.buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
            this.buffer.order(ByteOrder.BIG_ENDIAN);
        }
    }

    /**
     * Получает ключ по индексу (без создания объекта)
     */
    private long getKeyAt(int index) {
        int position = index * ENTRY_SIZE;
        return buffer.getLong(position);
    }

    /**
     * Получает запись по индексу (создаёт объект только при необходимости)
     */
    private PolyglotEntry getEntryAt(int index) {
        int position = index * ENTRY_SIZE;
        long key = buffer.getLong(position);
        short move = buffer.getShort(position + 8);
        short weight = buffer.getShort(position + 10);
        int learn = buffer.getInt(position + 12);
        int games = 0;
        return new PolyglotEntry(key, move, weight, learn, games);
    }

    /**
     * Бинарный поиск первой записи с ключом >= key
     * Аналог bisect_left в Python
     */
    private int bisectKeyLeft(long key) {
        int lo = 0;
        int hi = entryCount;

        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            long midKey = getKeyAt(mid);
            // Беззнаковое сравнение!
            if (Long.compareUnsigned(midKey, key) < 0) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return lo;
    }

    /**
     * Находит все записи для позиции (по ключу)
     * Аналог find_all в Python-chess
     */
    public List<PolyglotEntry> findAllEntries(long key) {
        int index = bisectKeyLeft(key);

        List<PolyglotEntry> result = new ArrayList<>();
        while (index < entryCount) {
            long currentKey = getKeyAt(index);
            if (currentKey != key) {
                log.debug("  Stopping at index {}: key=0x{} (expected 0x{})",
                        index, Long.toHexString(currentKey), Long.toHexString(key));
                break;
            }
            result.add(getEntryAt(index));
            index++;
        }

        return result;
    }

    /**
     * Проверяет, легален ли ход на доске
     */
    private boolean isLegalMove(PolyglotEntry entry, Board board) {
        Square from = entry.getFromSquare();
        Square to = entry.getToSquare();
        Piece promotion = entry.getPromotionPiece();

        List<Move> legalMoves = board.legalMoves();
        for (Move move : legalMoves) {
            if (move.getFrom() == from && move.getTo() == to) {
                Piece legalPromotion = move.getPromotion();
                if ((legalPromotion == null || legalPromotion == Piece.NONE) &&
                        (promotion == null || promotion == Piece.NONE)) {
                    return true;
                }
                if (legalPromotion == promotion) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Создает Move из PolyglotEntry
     */
    public Move createChesslibMove(PolyglotEntry entry) {
        Square from = entry.getFromSquare();
        Square to = entry.getToSquare();
        Piece promotion = entry.getPromotionPiece();
        Piece promotionPiece = (promotion != null) ? promotion : Piece.NONE;
        return new Move(from, to, promotionPiece);
    }

    /**
     * Определяет фигуру для клетки в начальной позиции
     */
    private Piece getPieceForSquareInInitial(Square square) {
        Board board = new Board();
        return board.getPiece(square);
    }

    /**
     * Генерирует имя для варианта
     */
    public String generateVariationName(MoveNode node, int id) {
        String san = node.getSan();
        if (san == null || san.isEmpty()) {
            return "V" + id;
        }
        san = san.replace("+", "").replace("#", "");
        return san + " (" + id + ")";
    }

    /**
     * ИТЕРАТИВНОЕ построение дерева - БЕЗ РЕКУРСИИ!
     */
    private GameTree buildTreeIterative() {
        // Создаем корневой узел
        RootNode rootNode = new RootNode();
        rootNode.setAbsolutePly(-1);

        // Корневой вариант
        Variation rootVariation = new Variation(languageManager.get(LanguageKeys.ROOT));
        rootVariation.setFirstNode(rootNode);
        rootVariation.setMainLine(false);

        // Начальная доска
        Board startBoard = new Board();
        long startKey = ZobristHasher.calculate(startBoard);

        // Находим ВСЕ записи с ключом начальной позиции
        List<PolyglotEntry> startEntries = findAllEntries(startKey);
        log.debug("Found {} entries with start key", startEntries.size());

        // Фильтруем по весу и легальности
        List<PolyglotEntry> filteredEntries = new ArrayList<>();
        for (PolyglotEntry entry : startEntries) {
            if (entry.weight() >= 1 && isLegalMove(entry, startBoard)) {
                filteredEntries.add(entry);
            }
        }

        // Главная линия
        Variation mainLine = null;
        Stack<BuildContext> stack = new Stack<>();

        // ========== УВЕЛИЧЕННЫЕ ОГРАНИЧЕНИЯ ==========
        int maxDepth = 60;              // 60 полуходов = 30 полных ходов
        int maxMovesPerPosition = 20;   // Максимум вариантов на позицию
        int maxTotalNodes = 500000;     // 500,000 узлов - достаточно для большой книги

        int nodeCount = 0;
        int variationId = 0;

        // Создаем варианты для каждого первого хода
        for (int i = 0; i < Math.min(filteredEntries.size(), maxMovesPerPosition); i++) {
            PolyglotEntry entry = filteredEntries.get(i);

            // Создаем узел для хода
            MoveNode moveNode = createMoveNode(entry, startBoard, 1);
            if (moveNode == null) continue;

            nodeCount++;
            if (nodeCount > maxTotalNodes) {
                log.debug("Reached max total nodes limit ({})", maxTotalNodes);
                break;
            }

            // Создаем вариант
            Variation variation = new Variation(generateVariationName(moveNode, variationId++));
            variation.addMove(moveNode);
            variation.setMainLine(i == 0);
            variation.setParentVariation(rootVariation);
            variation.setParentNodeRef(rootNode);

            moveNode.setParent(rootNode);
            moveNode.setOwningVariation(variation);

            rootNode.getSubVariations().add(variation);

            if (i == 0) {
                mainLine = variation;
            }

            // Применяем ход на доске
            Board newBoard = startBoard.clone();
            try {
                Move move = createChesslibMove(entry);
                newBoard.doMove(move);
            } catch (Exception e) {
                log.trace("BuildTreeIterative Failed to apply move: {}", e.getMessage());
                continue;
            }

            // Добавляем в стек для продолжения
            stack.push(new BuildContext(
                    variation,
                    newBoard,
                    moveNode,
                    1,
                    entry.key(),
                    new HashSet<>()
            ));
        }

        // Если нет главной линии - создаем пустую
        if (mainLine == null) {
            mainLine = new Variation(languageManager.get(LanguageKeys.MAIN_LINE));
            mainLine.setMainLine(true);
            mainLine.setParentVariation(rootVariation);
            mainLine.setParentNodeRef(rootNode);
        }

        // ИТЕРАТИВНЫЙ ОБХОД
        int iterations = 0;
        int maxIterations = 5000; // Увеличиваем для глубоких книг

        while (!stack.isEmpty() && iterations < maxIterations) {
            iterations++;
            BuildContext ctx = stack.pop();

            if (ctx.depth >= maxDepth) {
                continue;
            }

            String fen = ctx.board.getFen();
            if (ctx.visited.contains(fen)) {
                continue;
            }
            ctx.visited.add(fen);

            long currentKey = ZobristHasher.calculate(ctx.board);
            List<PolyglotEntry> entries = findAllEntries(currentKey);
            if (entries.isEmpty()) continue;

            int movesToTake = Math.min(entries.size(), maxMovesPerPosition);
            boolean isFirst = true;

            for (int i = 0; i < movesToTake; i++) {
                PolyglotEntry entry = entries.get(i);

                if (entry.weight() < 1) continue;
                if (!isLegalMove(entry, ctx.board)) continue;

                int ply = ctx.depth + 1;
                MoveNode moveNode = createMoveNode(entry, ctx.board, ply);
                if (moveNode == null) continue;

                nodeCount++;
                if (nodeCount > maxTotalNodes) {
                    log.warn("Reached max total nodes limit ({}). Stopping.", maxTotalNodes);
                    stack.clear();
                    break;
                }

                if (isFirst) {
                    ctx.lastNode.setNext(moveNode);
                    moveNode.setParent(ctx.lastNode);
                    moveNode.setOwningVariation(ctx.variation);
                    isFirst = false;

                    Board nextBoard = ctx.board.clone();
                    try {
                        nextBoard.doMove(createChesslibMove(entry));
                        stack.push(new BuildContext(
                                ctx.variation,
                                nextBoard,
                                moveNode,
                                ply,
                                entry.key(),
                                new HashSet<>(ctx.visited)
                        ));
                    } catch (Exception e) {
                        log.trace("Failed to apply move: {}", e.getMessage());
                    }
                } else {
                    Variation subVar = new Variation(generateVariationName(moveNode, variationId++));
                    subVar.addMove(moveNode);
                    subVar.setMainLine(false);
                    subVar.setParentVariation(ctx.variation);
                    subVar.setParentNodeRef(ctx.lastNode);

                    moveNode.setParent(ctx.lastNode);
                    moveNode.setOwningVariation(subVar);
                    moveNode.setForkNode(ctx.lastNode);

                    ctx.lastNode.getSubVariations().add(subVar);

                    Board nextBoard = ctx.board.clone();
                    try {
                        nextBoard.doMove(createChesslibMove(entry));
                        stack.push(new BuildContext(
                                subVar,
                                nextBoard,
                                moveNode,
                                ply,
                                entry.key(),
                                new HashSet<>(ctx.visited)
                        ));
                    } catch (Exception e) {
                        log.trace("Failed to apply move in nextBoard: {}", e.getMessage());
                    }
                }
            }
        }

        if (iterations >= maxIterations) {
            log.warn("Reached max iterations limit ({}). Tree may be incomplete.", maxIterations);
        }

        log.info("Built tree with {} nodes, {} variations, depth: {}",
                nodeCount, variationId, maxDepth / 2);

        GameTree gameTree = new GameTree(rootNode, mainLine, rootVariation);
        gameTree.setInitialBoard(startBoard);
        gameTree.setStartWithBlack(false);
        return gameTree;
    }

    /**
     * Создает MoveNode из записи Polyglot
     */
    public MoveNode createMoveNode(PolyglotEntry entry, Board board, int ply) {
        Square from = entry.getFromSquare();
        Square to = entry.getToSquare();
        Piece promotion = entry.getPromotionPiece();

        Piece promotionPiece = (promotion != null) ? promotion : Piece.NONE;
        Move move = new Move(from, to, promotionPiece);

        Piece piece = board.getPiece(from);
        if (piece == Piece.NONE) {
            piece = getPieceForSquareInInitial(from);
        }

        boolean isCapture = board.getPiece(to) != Piece.NONE;

        MoveNode node = new MoveNode(move, piece, isCapture, promotion);
        node.setAbsolutePly(ply);

        String stats = String.format("weight:%d,games:%d,rating:%d",
                entry.weight(), entry.games(), entry.getRating());
        node.setComment(stats);

        return node;
    }

    /**
         * Контекст для построения дерева
         */
        private record BuildContext(Variation variation, Board board, MoveNode lastNode, int depth, long key,
                                    Set<String> visited) {
    }
}