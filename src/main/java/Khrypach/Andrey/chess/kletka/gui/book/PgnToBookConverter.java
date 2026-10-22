/*
 * Copyright (c) 2025 Andrey Khrypach
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

import Khrypach.Andrey.chess.kletka.gui.book.enums.Outcome;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.pgn.index.PgnFileEditor;
import Khrypach.Andrey.chess.kletka.pgn.index.PgnIndexManager;
import Khrypach.Andrey.chess.kletka.pgn.index.binary.LazyPgnIndex;
import Khrypach.Andrey.chess.kletka.pgn.index.binary.LightGameEntry;
import Khrypach.Andrey.chess.kletka.pgn.index.model.GameIndexEntry;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Конвертер PGN-файла (через .klt-индекс) в Polyglot-книгу.
 * <p>
 * Алгоритм (как в оригинальном polyglot make-book):
 * <ol>
 *   <li>Берём {@link LazyPgnIndex} — все активные партии в памяти.</li>
 *   <li>Для каждой партии читаем сырой PGN через {@link PgnFileEditor#readGame(GameIndexEntry)}.</li>
 *   <li>Извлекаем ТОЛЬКО main line (без вариантов/комментариев/NAG).</li>
 *   <li>Воспроизводим ходы на {@link Board}, накапливая статистику
 *       {@code key -> uci -> [wins, losses, draws]}.</li>
 *   <li>В конце: {@code weight = 2*wins + draws}, пишем через
 *       {@link PolyglotBookWriter#createBook(Path, List)}.</li>
 * </ol>
 * <p>
 * Партии с {@code [SetUp "1"]} (нестандартная начальная позиция)
 * ПРОПУСКАЮТСЯ — иначе книга будет искажена.
 */
public final class PgnToBookConverter {

    private static final Logger log = LoggerFactory.getLogger(PgnToBookConverter.class);
    private static final LanguageManager lang = LanguageManager.getInstance();

    private PgnToBookConverter() {
    }

    // ========== ОПЦИИ ==========

    /**
     * Параметры построения книги.
     *
     * @param maxPly   максимальная глубина в полуходах (40 — как в polyglot)
     * @param minGames минимум партий, чтобы ход попал в книгу
     */
    public record BuildOptions(int maxPly, int minGames) {
        public static BuildOptions defaults() {
            return new BuildOptions(40, 1);
        }
    }

    /**
     * Результат построения.
     */
    public record BuildResult(
            int gamesProcessed,
            int gamesSkipped,
            int positionsCollected,
            int entriesWritten,
            long elapsedMs
    ) {
    }

    // ========== ТОЧКА ВХОДА ==========

    /**
     * Строит книгу из PGN-файла. Требует существующий .klt-индекс.
     *
     * @param pgnPath    путь к PGN
     * @param outputBook путь к .bin (будет перезаписан)
     * @param options    параметры
     * @param progressCb колбэк прогресса (может быть null), получает 0.0..1.0
     * @return статистика
     */
    public static BuildResult convert(Path pgnPath,
                                      Path outputBook,
                                      BuildOptions options,
                                      BiConsumer<Double, String> progressCb) throws IOException {

        // 1. Обработка партий + сборка статистики
        StatsResult statsResult = buildStats(pgnPath, options, progressCb);

        // 2. Запись файла
        if (progressCb != null) {
            progressCb.accept(0.95, lang.get(LanguageKeys.BOOK_CREATE_STATUS_WRITING));
        }

        List<PolyglotEntry> entries = statsResult.entries();
        if (!entries.isEmpty()) {
            PolyglotBookWriter.createBook(outputBook, entries);
        }

        if (progressCb != null) {
            progressCb.accept(1.0, lang.get(LanguageKeys.BOOK_CREATE_STATUS_COMPLETE));
        }

        return new BuildResult(statsResult.processed(), statsResult.skipped(),
                statsResult.positions(), entries.size(), statsResult.elapsedMs());
    }

    /**
     * Проходит по всем партиям в PGN-файле, собирает статистику ходов
     * и возвращает готовый список {@link PolyglotEntry} для записи в .bin.
     * <p>
     * НЕ пишет файл — это делает {@code MainController} отдельно,
     * чтобы показать пользователю статус "Запись файла...".
     */
    public static StatsResult buildStats(Path pgnPath,
                                         BuildOptions options,
                                         BiConsumer<Double, String> progressCb) throws IOException {
        long start = System.currentTimeMillis();

        if (pgnPath == null || !java.nio.file.Files.exists(pgnPath)) {
            throw new IOException("PGN file not found: " + pgnPath);
        }

        log.info("[BOOK BUILD] Start stats collection: pgn={}, opts={}", pgnPath, options);

        // 1. Загружаем ленивый индекс (все лёгкие записи в памяти)
        PgnIndexManager indexManager = new PgnIndexManager();
        LazyPgnIndex lazyIndex = indexManager.loadLazyIndex(pgnPath);
        List<LightGameEntry> games = lazyIndex.getActiveLightEntries();

        int total = games.size();
        log.info("[BOOK BUILD] Active games: {}", total);

        if (total == 0) {
            log.warn("[BOOK BUILD] No active games, nothing to do");
            return new StatsResult(0, 0, 0, List.of(), System.currentTimeMillis() - start);
        }

        // 2. Статистика: key -> (uci -> [wins, losses, draws])
        Map<Long, Map<String, int[]>> stats = new HashMap<>();

        int processed = 0;
        int skipped = 0;

        // Диагностика скипов
        int skipNoBody = 0;
        int skipNoMoves = 0;
        int skipSetUp = 0;
        int skipBadResult = 0;
        int skipIllegalSan = 0;
        int skipException = 0;

        // 3. Один RandomAccessFile на весь цикл
        try (PgnFileEditor editor = new PgnFileEditor(pgnPath, lazyIndex).batch()) {
            for (LightGameEntry light : games) {
                processed++;

                try {
                    GameIndexEntry fullEntry = lazyIndex.getFullEntry(light.id());
                    if (fullEntry == null || fullEntry.isDeleted()) {
                        skipped++;
                        skipException++;
                        continue;
                    }

                    String rawPgn = editor.readGame(fullEntry);
                    if (rawPgn == null || rawPgn.isEmpty()) {
                        skipped++;
                        skipNoBody++;
                        continue;
                    }

                    if (rawPgn.contains("[SetUp \"1\"]")) {
                        log.debug("[BOOK BUILD] Skip game {} (SetUp/FEN)", light.id());
                        skipped++;
                        skipSetUp++;
                        continue;
                    }

                    Outcome winner = parseOutcome(rawPgn);
                    String resultTag = extractTag(rawPgn, "Result");
                    if (winner == null && !"1/2-1/2".equals(resultTag)) {
                        skipped++;
                        skipBadResult++;
                        continue;
                    }

                    String body = extractBody(rawPgn);
                    List<String> sans = extractMainLineSans(body);

                    if (sans.isEmpty()) {
                        log.debug("[BOOK BUILD] Skip game {} (no moves in body)", light.id());
                        skipped++;
                        skipNoMoves++;
                        continue;
                    }

                    processGame(sans, winner, options, stats);

                } catch (Exception e) {
                    log.debug("[BOOK BUILD] Failed to process game {}: {}", light.id(), e.getMessage());
                    skipped++;
                    skipException++;
                }

                // ========== ПРОГРЕСС ОБРАБОТКИ ПАРТИЙ ==========
                if (progressCb != null && processed % 1000 == 0) {
                    progressCb.accept((double) processed / total,
                            String.format(lang.get(LanguageKeys.BOOK_CREATE_BUILDING_PROGRESS),
                                    processed, total, (int) ((double) processed / total * 100)));
                }
            }
        }

        log.info("[BOOK BUILD] Skip reasons: noBody={}, noMoves={}, setUp={}, badResult={}, illegalSan={}, exceptions={}",
                skipNoBody, skipNoMoves, skipSetUp, skipBadResult, skipIllegalSan, skipException);

        // ========== ПАРТИИ ОБРАБОТАНЫ ==========
        if (progressCb != null) {
            progressCb.accept(1.0, lang.get(LanguageKeys.BOOK_CREATE_STATUS_PROCESSING_COMPLETE));
        }

        // ========== СБОРКА СТАТИСТИКИ ==========
        if (progressCb != null) {
            progressCb.accept(0.90, lang.get(LanguageKeys.BOOK_CREATE_STATUS_BUILDING));
        }
        List<PolyglotEntry> entries = buildEntries(stats, options);

        long elapsed = System.currentTimeMillis() - start;
        log.info("[BOOK BUILD] Stats collected: processed={}, skipped={}, positions={}, entries={}, elapsed={}ms",
                processed, skipped, stats.size(), entries.size(), elapsed);

        return new StatsResult(processed, skipped, stats.size(), entries, elapsed);
    }

    // ========== ОБРАБОТКА ОДНОЙ ПАРТИИ ==========

    static void processGame(List<String> sans,
                            Outcome outcome,
                            BuildOptions options,
                            Map<Long, Map<String, int[]>> stats) {
        // Если исход неизвестен — статистику не собираем
        if (outcome == Outcome.UNKNOWN) return;

        Board board = new Board();
        int ply = 0;

        for (String san : sans) {
            if (ply >= options.maxPly()) break;

            Move move = sanToMove(board, san);
            if (move == null) return;

            long key = ZobristHasher.calculateWithoutCache(board);
            String uci = moveToUci(move);

            Side mover = board.getSideToMove();

            // Теперь мы уверены, что исход известен, и можем создавать запись
            int[] counters = stats
                    .computeIfAbsent(key, k -> new HashMap<>())
                    .computeIfAbsent(uci, u -> new int[3]); // [wins, losses, draws]

            switch (outcome) {
                case WHITE_WIN -> {
                    if (mover == Side.WHITE) counters[0]++;
                    else counters[1]++;
                }
                case BLACK_WIN -> {
                    if (mover == Side.BLACK) counters[0]++;
                    else counters[1]++;
                }
                case DRAW -> counters[2]++;
            }

            board.doMove(move);
            ply++;
        }
    }

    // ========== СБОРКА POLYGLOT-ЗАПИСЕЙ ==========

    static List<PolyglotEntry> buildEntries(Map<Long, Map<String, int[]>> stats,
                                                    BuildOptions options) {

        // ========== ДИАГНОСТИКА СТАРТОВОЙ ПОЗИЦИИ ==========
        long startKey = ZobristHasher.calculateWithoutCache(new Board());
        Map<String, int[]> startStats = stats.get(startKey);
        if (startStats != null) {
            log.info("[BOOK BUILD] === Start position stats ({} moves) ===", startStats.size());
            startStats.entrySet().stream()
                    .sorted((a, b) -> Integer.compare(b.getValue()[0] * 2 + b.getValue()[2],
                            a.getValue()[0] * 2 + a.getValue()[2]))
                    .limit(10)
                    .forEach(e -> {
                        int[] c = e.getValue();
                        log.info("[BOOK BUILD]   {}: wins={}, losses={}, draws={} → weight={}",
                                e.getKey(), c[0], c[1], c[2], c[0] * 2 + c[2]);
                    });
        } else {
            log.warn("[BOOK BUILD] No stats for start position!");
        }

        List<PolyglotEntry> result = new ArrayList<>();

        for (Map.Entry<Long, Map<String, int[]>> posEntry : stats.entrySet()) {
            long key = posEntry.getKey();

            for (Map.Entry<String, int[]> moveEntry : posEntry.getValue().entrySet()) {
                int[] c = moveEntry.getValue();
                int wins = c[0];
                int losses = c[1];
                int draws = c[2];
                int totalGames = wins + losses + draws;

                if (totalGames < options.minGames()) continue;

                int weight = wins * 2 + draws;
                if (weight <= 0) {
                    // Ход только проигрывал — оставляем с минимальным весом 1,
                    // чтобы книга не теряла линии (как делает polyglot с -min-score 0)
                    weight = 1;
                }
                if (weight > 65535) weight = 65535;

                short moveCode = encodeUciToPolyglot(moveEntry.getKey());
                if (moveCode == 0) continue;

                result.add(new PolyglotEntry(key, moveCode, weight, 0, totalGames));
            }
        }

        result.sort((a, b) -> Long.compareUnsigned(a.key(), b.key()));
        return result;
    }

    // ========== ПАРСИНГ PGN ==========

    /**
     * Определяет победителя по тегу {@code [Result "..."]}.
     */
    static Outcome parseOutcome(String rawPgn) {
        String result = extractTag(rawPgn, "Result");
        if (result == null) return Outcome.UNKNOWN;
        return switch (result.trim()) {
            case "1-0" -> Outcome.WHITE_WIN;
            case "0-1" -> Outcome.BLACK_WIN;
            case "1/2-1/2" -> Outcome.DRAW;
            default -> Outcome.UNKNOWN;
        };
    }

    /**
     * Извлекает значение тега {@code [Name "value"]}.
     */
    static String extractTag(String pgn, String tagName) {
        String prefix = "[" + tagName + " \"";
        int start = pgn.indexOf(prefix);
        if (start < 0) return null;
        start += prefix.length();
        int end = pgn.indexOf('"', start);
        if (end < 0) return null;
        return pgn.substring(start, end);
    }

    /**
     * Отрезает заголовки: возвращает текст после последнего {@code ]} + разделитель.
     */
    static String extractBody(String rawPgn) {
        int lastBracket = rawPgn.lastIndexOf(']');
        if (lastBracket < 0) return rawPgn;
        return rawPgn.substring(lastBracket + 1);
    }

    /**
     * Извлекает main line SAN-ходы из тела PGN.
     * <p>
     * Удаляет:
     * <ul>
     *   <li>{@code {...}} — комментарии (с учётом многострочности)</li>
     *   <li>{@code ;...} — комментарии до конца строки</li>
     *   <li>{@code (...)} — варианты (с учётом вложенности)</li>
     *   <li>{@code $N} — NAG</li>
     *   <li>номера ходов {@code 1.}, {@code 1...}</li>
     *   <li>результаты {@code 1-0}, {@code 0-1}, {@code 1/2-1/2}, {@code *}</li>
     * </ul>
     */
    static List<String> extractMainLineSans(String body) {
        if (body == null || body.isEmpty()) return List.of();

        String s = body;

        // 1. Удаляем {...} (комментарии)
        s = removeBraced(s);

        // 2. Удаляем ;... до конца строки
        s = s.replaceAll(";[^\\n]*", " ");

        // 3. Удаляем (...) с учётом вложенности
        s = removeParens(s);

        // 4. Удаляем NAG $N
        s = s.replaceAll("\\$\\d+", " ");

        // 5. Удаляем номера ходов: "1." "1..." "12." и т.п.
        s = s.replaceAll("\\d+\\.+", " ");

        // 6. Удаляем результаты
        s = s.replaceAll("1-0|0-1|1/2-1/2|\\*", " ");

        // 7. Разбиваем по пробелам
        List<String> result = new ArrayList<>();
        for (String token : s.split("\\s+")) {
            if (token.isEmpty()) continue;
            // Оставляем только «похожие на SAN» токены (буквы, цифры, =, +, #, -, O)
            if (token.matches("[KQRBNO0-9a-hx=+#\\-]+")) {
                result.add(token);
            }
        }
        return result;
    }

    private static String removeBraced(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        int depth = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{') {
                depth++;
                continue;
            }
            if (c == '}') {
                if (depth > 0) depth--;
                continue;
            }
            if (depth == 0) sb.append(c);
        }
        return sb.toString();
    }

    private static String removeParens(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        int depth = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') {
                depth++;
                continue;
            }
            if (c == ')') {
                if (depth > 0) depth--;
                continue;
            }
            if (depth == 0) sb.append(c);
        }
        return sb.toString();
    }

    // ========== SAN → MOVE ==========

    /**
     * Лёгкий SAN-парсер: находит {@link Move} среди легальных ходов доски,
     * соответствующий SAN-токену.
     * <p>
     * Поддерживает: {@code e4}, {@code Nf3}, {@code Nbd2}, {@code N1d2},
     * {@code exd5}, {@code O-O}, {@code O-O-O}, {@code e8=Q}, {@code Qxe7+}, {@code Qxe7#}.
     */
    static Move sanToMove(Board board, String sanRaw) {
        if (sanRaw == null || sanRaw.isEmpty()) return null;

        // Убираем суффиксы шаха/мата
        String san = sanRaw.replace("+", "").replace("#", "").replace("++", "");
        if (san.isEmpty()) return null;

        boolean isCastleK = san.equals("O-O") || san.equals("0-0");
        boolean isCastleQ = san.equals("O-O-O") || san.equals("0-0-0");

        // Рокировка
        if (isCastleK || isCastleQ) {
            Square kingFrom = board.getSideToMove() == Side.WHITE ? Square.E1 : Square.E8;
            Square kingTo = isCastleK
                    ? (board.getSideToMove() == Side.WHITE ? Square.G1 : Square.G8)
                    : (board.getSideToMove() == Side.WHITE ? Square.C1 : Square.C8);
            return findLegal(board, kingFrom, kingTo, Piece.NONE);
        }

        // Разбираем SAN
        Piece promo = Piece.NONE;
        int eq = san.indexOf('=');
        if (eq >= 0 && eq + 1 < san.length()) {
            promo = promoCharToPiece(san.charAt(eq + 1), board.getSideToMove());
            san = san.substring(0, eq);
        }

        // Фигура (K/Q/R/B/N), иначе пешка
        Piece movingPieceType = Piece.NONE;
        if (!san.isEmpty()) {
            char first = san.charAt(0);
            Piece p = pieceCharToType(first, board.getSideToMove());
            if (p != Piece.NONE) {
                movingPieceType = p;
                san = san.substring(1);
            } else if (Character.isLowerCase(first)) {
                movingPieceType = board.getSideToMove() == Side.WHITE ? Piece.WHITE_PAWN : Piece.BLACK_PAWN;
            }
        }

        san = san.replace("x", "");

        // Последние 2 символа — целевая клетка
        if (san.length() < 2) return null;
        String toStr = san.substring(san.length() - 2);
        Square to = parseSquare(toStr);
        if (to == null || to == Square.NONE) return null;

        // Остаток — уточнение (file/rank)
        String disamb = san.substring(0, san.length() - 2);

        List<Move> candidates = new ArrayList<>();
        for (Move m : board.legalMoves()) {
            if (m.getTo() != to) continue;
            if (m.getPromotion() != promo && !(m.getPromotion() == null && promo == Piece.NONE)) {
                // В chesslib promotion может быть null/NONE
                if (!isPromoMatch(m.getPromotion(), promo)) continue;
            }

            Piece p = board.getPiece(m.getFrom());
            if (!isSamePieceType(p, movingPieceType)) continue;

            // Уточнение по file/rank
            if (!disamb.isEmpty()) {
                char d0 = disamb.charAt(0);
                boolean matched = false;
                if (disamb.length() == 1) {
                    if (Character.isLetter(d0)) {
                        matched = m.getFrom().toString().toLowerCase().charAt(0) == d0;
                    } else if (Character.isDigit(d0)) {
                        matched = m.getFrom().toString().charAt(1) == d0;
                    }
                } else if (disamb.length() == 2) {
                    String fromStr = m.getFrom().toString().toLowerCase();
                    matched = fromStr.equals(disamb.toLowerCase());
                }
                if (!matched) continue;
            }

            candidates.add(m);
        }

        if (candidates.size() == 1) {
            return candidates.get(0);
        }

        if (candidates.size() > 1) {
            // Неоднозначность не разрешена — берём первый (редкий случай в корректном PGN)
            log.trace("[BOOK BUILD] Ambiguous SAN '{}', taking first of {} candidates", sanRaw, candidates.size());
            return candidates.get(0);
        }

        return null;
    }

    private static Move findLegal(Board board, Square from, Square to, Piece promo) {
        for (Move m : board.legalMoves()) {
            if (m.getFrom() == from && m.getTo() == to) {
                if (isPromoMatch(m.getPromotion(), promo)) return m;
            }
        }
        return null;
    }

    private static boolean isPromoMatch(Piece a, Piece b) {
        Piece na = (a == null) ? Piece.NONE : a;
        Piece nb = (b == null) ? Piece.NONE : b;
        return na == nb;
    }

    private static boolean isSamePieceType(Piece actual, Piece expected) {
        if (expected == Piece.NONE) return false;
        // Сравниваем только тип (пешка/конь/...), без цвета
        return switch (expected) {
            case WHITE_PAWN, BLACK_PAWN -> actual == Piece.WHITE_PAWN || actual == Piece.BLACK_PAWN;
            case WHITE_KNIGHT, BLACK_KNIGHT -> actual == Piece.WHITE_KNIGHT || actual == Piece.BLACK_KNIGHT;
            case WHITE_BISHOP, BLACK_BISHOP -> actual == Piece.WHITE_BISHOP || actual == Piece.BLACK_BISHOP;
            case WHITE_ROOK, BLACK_ROOK -> actual == Piece.WHITE_ROOK || actual == Piece.BLACK_ROOK;
            case WHITE_QUEEN, BLACK_QUEEN -> actual == Piece.WHITE_QUEEN || actual == Piece.BLACK_QUEEN;
            case WHITE_KING, BLACK_KING -> actual == Piece.WHITE_KING || actual == Piece.BLACK_KING;
            default -> false;
        };
    }

    private static Piece pieceCharToType(char c, Side side) {
        return switch (Character.toUpperCase(c)) {
            case 'K' -> side == Side.WHITE ? Piece.WHITE_KING : Piece.BLACK_KING;
            case 'Q' -> side == Side.WHITE ? Piece.WHITE_QUEEN : Piece.BLACK_QUEEN;
            case 'R' -> side == Side.WHITE ? Piece.WHITE_ROOK : Piece.BLACK_ROOK;
            case 'B' -> side == Side.WHITE ? Piece.WHITE_BISHOP : Piece.BLACK_BISHOP;
            case 'N' -> side == Side.WHITE ? Piece.WHITE_KNIGHT : Piece.BLACK_KNIGHT;
            default -> Piece.NONE;
        };
    }

    private static Piece promoCharToPiece(char c, Side side) {
        return switch (Character.toUpperCase(c)) {
            case 'Q' -> side == Side.WHITE ? Piece.WHITE_QUEEN : Piece.BLACK_QUEEN;
            case 'R' -> side == Side.WHITE ? Piece.WHITE_ROOK : Piece.BLACK_ROOK;
            case 'B' -> side == Side.WHITE ? Piece.WHITE_BISHOP : Piece.BLACK_BISHOP;
            case 'N' -> side == Side.WHITE ? Piece.WHITE_KNIGHT : Piece.BLACK_KNIGHT;
            default -> Piece.NONE;
        };
    }

    private static Square parseSquare(String s) {
        if (s == null || s.length() != 2) return Square.NONE;
        char file = s.charAt(0);
        char rank = s.charAt(1);
        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') return Square.NONE;
        return Square.valueOf(s.toUpperCase());
    }

    // ========== MOVE → UCI / POLYGLOT ==========

    private static String moveToUci(Move move) {
        String uci = move.getFrom().toString().toLowerCase()
                + move.getTo().toString().toLowerCase();
        Piece promo = move.getPromotion();
        if (promo != null && promo != Piece.NONE) {
            uci += promoChar(promo);
        }
        return uci;
    }

    private static char promoChar(Piece p) {
        return switch (p) {
            case WHITE_QUEEN, BLACK_QUEEN -> 'q';
            case WHITE_ROOK, BLACK_ROOK -> 'r';
            case WHITE_BISHOP, BLACK_BISHOP -> 'b';
            case WHITE_KNIGHT, BLACK_KNIGHT -> 'n';
            default -> ' ';
        };
    }

    /**
     * Кодирует UCI-строку в 16-битный Polyglot-код.
     */
    static short encodeUciToPolyglot(String uci) {
        if (uci == null || uci.length() < 4) return 0;
        try {
            Square from = Square.valueOf(uci.substring(0, 2).toUpperCase());
            Square to = Square.valueOf(uci.substring(2, 4).toUpperCase());
            int promoCode = 0;
            if (uci.length() > 4) {
                promoCode = switch (uci.charAt(4)) {
                    case 'n' -> 1;
                    case 'b' -> 2;
                    case 'r' -> 3;
                    case 'q' -> 4;
                    default -> 0;
                };
            }
            return (short) ((promoCode << 12) | (from.ordinal() << 6) | to.ordinal());
        } catch (Exception e) {
            return 0;
        }
    }
}