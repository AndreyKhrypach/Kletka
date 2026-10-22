package Khrypach.Andrey.chess.kletka.gui.book;

import Khrypach.Andrey.chess.kletka.gui.book.enums.Outcome;
import Khrypach.Andrey.chess.kletka.pgn.index.PgnIndexingFacade;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.move.Move;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PgnToBookConverterTest {

    // ========== 1. ТЕСТЫ ПАРСИНГА PGN ==========

    @Nested
    @DisplayName("parseOutcome")
    class ParseOutcome {
        @Test
        void whiteWin() {
            assertThat(PgnToBookConverter.parseOutcome("[Result \"1-0\"]\n1. e4 1-0"))
                    .isEqualTo(Outcome.WHITE_WIN);
        }

        @Test
        void blackWin() {
            assertThat(PgnToBookConverter.parseOutcome("[Result \"0-1\"]\n1. e4 0-1"))
                    .isEqualTo(Outcome.BLACK_WIN);
        }

        @Test
        void draw() {
            assertThat(PgnToBookConverter.parseOutcome("[Result \"1/2-1/2\"]\n1. e4 1/2-1/2"))
                    .isEqualTo(Outcome.DRAW);
        }

        @Test
        void unknown() {
            assertThat(PgnToBookConverter.parseOutcome("[Result \"*\"]\n1. e4 *"))
                    .isEqualTo(Outcome.UNKNOWN);
        }

        @Test
        void noResultTag() {
            assertThat(PgnToBookConverter.parseOutcome("[White \"A\"]\n[Black \"B\"]"))
                    .isEqualTo(Outcome.UNKNOWN);
        }
    }

    @Nested
    @DisplayName("extractTag и extractBody")
    class ExtractHelpers {
        @Test
        void extractTag() {
            String pgn = "[White \"Kasparov\"]\n[Black \"Karpov\"]";
            assertThat(PgnToBookConverter.extractTag(pgn, "White")).isEqualTo("Kasparov");
            assertThat(PgnToBookConverter.extractTag(pgn, "Black")).isEqualTo("Karpov");
        }

        @Test
        void extractTagMissing() {
            assertThat(PgnToBookConverter.extractTag("[White \"A\"]", "Missing")).isNull();
        }

        @Test
        void extractBody() {
            String pgn = "[White \"A\"]\n[Black \"B\"]\n\n1. e4 e5 1-0";
            assertThat(PgnToBookConverter.extractBody(pgn)).contains("1. e4 e5 1-0");
        }

        @Test
        void extractBodyWithoutHeaders() {
            String pgn = "1. e4 e5 1-0";
            assertThat(PgnToBookConverter.extractBody(pgn)).isEqualTo(pgn);
        }
    }

    @Nested
    @DisplayName("extractMainLineSans")
    class ExtractMainLine {
        @Test
        void simpleGame() {
            String body = "1. e4 e5 2. Nf3 Nc6 1-0";
            assertThat(PgnToBookConverter.extractMainLineSans(body))
                    .containsExactly("e4", "e5", "Nf3", "Nc6");
        }

        @Test
        void removesComments() {
            String body = "1. e4 {King's pawn} e5 {symmetrical} 2. Nf3 Nc6 1-0";
            assertThat(PgnToBookConverter.extractMainLineSans(body))
                    .containsExactly("e4", "e5", "Nf3", "Nc6");
        }

        @Test
        void removesVariations() {
            String body = "1. e4 (1. d4 d5) 1... e5 2. Nf3 Nc6 1-0";
            assertThat(PgnToBookConverter.extractMainLineSans(body))
                    .containsExactly("e4", "e5", "Nf3", "Nc6");
        }

        @Test
        void removesNags() {
            String body = "1. e4 $1 e5 $2 2. Nf3 $14 Nc6 1-0";
            assertThat(PgnToBookConverter.extractMainLineSans(body))
                    .containsExactly("e4", "e5", "Nf3", "Nc6");
        }

        @Test
        void removesMoveNumbersAndResults() {
            String body = "1. e4 e5 2. Nf3 Nc6 3. Bb5 *";
            assertThat(PgnToBookConverter.extractMainLineSans(body))
                    .containsExactly("e4", "e5", "Nf3", "Nc6", "Bb5");
        }

        @Test
        void emptyAndNull() {
            assertThat(PgnToBookConverter.extractMainLineSans("")).isEmpty();
            assertThat(PgnToBookConverter.extractMainLineSans(null)).isEmpty();
        }
    }

    // ========== 2. ТЕСТЫ SAN → MOVE ==========

    @Nested
    @DisplayName("sanToMove")
    class SanToMove {
        @Test
        void simplePawnMove() {
            Board board = new Board();
            Move move = PgnToBookConverter.sanToMove(board, "e4");

            assertThat(move).isNotNull();
            assertThat(move.getFrom().toString()).isEqualTo("E2");
            assertThat(move.getTo().toString()).isEqualTo("E4");
        }

        @Test
        void knightMove() {
            Board board = new Board();
            applyMoves(board, "e4", "e5");

            Move move = PgnToBookConverter.sanToMove(board, "Nf3");

            assertThat(move).isNotNull();
            assertThat(move.getFrom().toString()).isEqualTo("G1");
            assertThat(move.getTo().toString()).isEqualTo("F3");
        }

        @Test
        void kingsideCastling() {
            Board board = new Board();
            applyMoves(board, "e4", "e5", "Nf3", "Nc6", "Bc4", "Bc5");

            Move move = PgnToBookConverter.sanToMove(board, "O-O");

            assertThat(move).isNotNull();
            assertThat(move.getFrom().toString()).isEqualTo("E1");
            assertThat(move.getTo().toString()).isEqualTo("G1");
        }

        @Test
        void promotion() {
            Board board = new Board();
            board.loadFromFen("8/P7/8/8/8/8/8/K6k w - - 0 1");

            Move move = PgnToBookConverter.sanToMove(board, "a8=Q");

            assertThat(move).isNotNull();
            assertThat(move.getPromotion()).isEqualTo(Piece.WHITE_QUEEN);
        }

        @Test
        void capture() {
            Board board = new Board();
            applyMoves(board, "e4", "d5");

            Move move = PgnToBookConverter.sanToMove(board, "exd5");

            assertThat(move).isNotNull();
            assertThat(move.getFrom().toString()).isEqualTo("E4");
            assertThat(move.getTo().toString()).isEqualTo("D5");
        }

        @Test
        void invalidSan() {
            Board board = new Board();
            assertThat(PgnToBookConverter.sanToMove(board, "Xyz9")).isNull();
            assertThat(PgnToBookConverter.sanToMove(board, "")).isNull();
            assertThat(PgnToBookConverter.sanToMove(board, null)).isNull();
        }

        private void applyMoves(Board board, String... sans) {
            for (String san : sans) {
                Move m = PgnToBookConverter.sanToMove(board, san);
                assertThat(m).as("Move " + san).isNotNull();
                board.doMove(m);
            }
        }
    }

    // ========== 3. ТЕСТЫ КОДИРОВАНИЯ ==========

    @Nested
    @DisplayName("encodeUciToPolyglot")
    class EncodeUci {
        @Test
        void normalMove() {
            short code = PgnToBookConverter.encodeUciToPolyglot("e2e4");
            assertThat(code).isEqualTo((short) ((0) | (12 << 6) | 28));
        }

        @Test
        void promotionQueen() {
            short code = PgnToBookConverter.encodeUciToPolyglot("e7e8q");
            assertThat(code).isEqualTo((short) ((4 << 12) | (52 << 6) | 60));
        }

        @Test
        void invalidUci() {
            assertThat(PgnToBookConverter.encodeUciToPolyglot("xx")).isZero();
            assertThat(PgnToBookConverter.encodeUciToPolyglot(null)).isZero();
        }
    }

    // ========== 4. ТЕСТЫ ЛОГИКИ ==========

    @Nested
    @DisplayName("processGame")
    class ProcessGame {
        @Test
        void whiteWinStatistics() {
            Map<Long, Map<String, int[]>> stats = new HashMap<>();
            List<String> sans = List.of("e4", "e5");
            PgnToBookConverter.processGame(sans, Outcome.WHITE_WIN, PgnToBookConverter.BuildOptions.defaults(), stats);

            Board board = new Board();
            long startKey = ZobristHasher.calculateWithoutCache(board);
            int[] e4Stats = stats.get(startKey).get("e2e4");
            assertThat(e4Stats).containsExactly(1, 0, 0);

            board.doMove(PgnToBookConverter.sanToMove(board, "e4"));
            long afterE4Key = ZobristHasher.calculateWithoutCache(board);
            int[] e5Stats = stats.get(afterE4Key).get("e7e5");
            assertThat(e5Stats).containsExactly(0, 1, 0);
        }

        @Test
        void drawStatistics() {
            Map<Long, Map<String, int[]>> stats = new HashMap<>();
            PgnToBookConverter.processGame(List.of("d4", "d5"), Outcome.DRAW, PgnToBookConverter.BuildOptions.defaults(), stats);

            Board board = new Board();
            long startKey = ZobristHasher.calculateWithoutCache(board);
            int[] d4Stats = stats.get(startKey).get("d2d4");
            assertThat(d4Stats).containsExactly(0, 0, 1);
        }

        @Test
        void unknownOutcomeSkipsGame() {
            Map<Long, Map<String, int[]>> stats = new HashMap<>();
            PgnToBookConverter.processGame(List.of("e4", "e5"), Outcome.UNKNOWN, PgnToBookConverter.BuildOptions.defaults(), stats);

            assertThat(stats).isEmpty();
        }
    }

    @Nested
    @DisplayName("buildEntries")
    class BuildEntries {
        @Test
        void buildsCorrectWeight() {
            Map<Long, Map<String, int[]>> stats = new HashMap<>();
            long key = ZobristHasher.calculateWithoutCache(new Board());
            stats.computeIfAbsent(key, k -> new HashMap<>()).put("e2e4", new int[]{3, 1, 2});

            List<PolyglotEntry> entries = PgnToBookConverter.buildEntries(stats, PgnToBookConverter.BuildOptions.defaults());

            assertThat(entries).hasSize(1);
            PolyglotEntry entry = entries.get(0);
            assertThat(entry.key()).isEqualTo(key);
            assertThat(entry.weight()).isEqualTo(8);
            assertThat(entry.games()).isEqualTo(6);
        }

        @Test
        void respectsMinGames() {
            Map<Long, Map<String, int[]>> stats = new HashMap<>();
            long key = ZobristHasher.calculateWithoutCache(new Board());
            stats.computeIfAbsent(key, k -> new HashMap<>()).put("e2e4", new int[]{1, 0, 0});

            PgnToBookConverter.BuildOptions options = new PgnToBookConverter.BuildOptions(40, 2);
            List<PolyglotEntry> entries = PgnToBookConverter.buildEntries(stats, options);

            assertThat(entries).isEmpty();
        }

        @Test
        void weightNeverZero() {
            Map<Long, Map<String, int[]>> stats = new HashMap<>();
            long key = ZobristHasher.calculateWithoutCache(new Board());
            stats.computeIfAbsent(key, k -> new HashMap<>()).put("e2e4", new int[]{0, 5, 0});

            List<PolyglotEntry> entries = PgnToBookConverter.buildEntries(stats, PgnToBookConverter.BuildOptions.defaults());

            assertThat(entries).hasSize(1);
            assertThat(entries.get(0).weight()).isEqualTo(1);
        }
    }

    // ========== 5. ИНТЕГРАЦИОННЫЕ ТЕСТЫ ==========

    @Nested
    @DisplayName("buildStats (интеграция)")
    class BuildStatsIntegration {

        private static final String TWO_GAMES_PGN = """
                [Event "Test"]
                [Site "?"]
                [Date "2026.01.01"]
                [White "A"]
                [Black "B"]
                [Result "1-0"]
                
                1. e4 e5 2. Nf3 Nc6 1-0
                
                [Event "Test"]
                [Site "?"]
                [Date "2026.01.01"]
                [White "C"]
                [Black "D"]
                [Result "0-1"]
                
                1. e4 c5 2. Nf3 d6 0-1
                """;

        @Test
        @DisplayName("Собирает статистику из двух партий")
        void collectsStatsFromTwoGames(@TempDir Path tempDir) throws IOException {
            // 1. Готовим PGN
            Path pgnPath = tempDir.resolve("test.pgn");
            Files.writeString(pgnPath, TWO_GAMES_PGN);

            // 2. Создаём .klt (через полный цикл индексации)
            new PgnIndexingFacade().indexFile(pgnPath, null);

            // 3. Вызываем buildStats
            PgnToBookConverter.BuildOptions options = PgnToBookConverter.BuildOptions.defaults();
            StatsResult result = PgnToBookConverter.buildStats(pgnPath, options, null);

            // 4. Проверяем
            assertThat(result.processed()).isEqualTo(2);
            assertThat(result.skipped()).isEqualTo(0);
            assertThat(result.entries()).isNotEmpty();

            // Проверяем вес e4 (1 победа белых, 1 победа чёрных)
            long startKey = ZobristHasher.calculateWithoutCache(new Board());
            PolyglotEntry e4 = result.entries().stream()
                    .filter(e -> e.key() == startKey && e.getUciMove().equals("e2e4"))
                    .findFirst().orElse(null);

            assertThat(e4).isNotNull();
            // 1-0 (wins для белых) + 0-1 (losses для белых) → weight = 1*2 + 0 = 2
            assertThat(e4.weight()).isEqualTo(2);
        }

        @Test
        @DisplayName("progressCb вызывается с правильными статусами")
        void progressCallbackIsCalled(@TempDir Path tempDir) throws IOException {
            Path pgnPath = tempDir.resolve("test.pgn");
            Files.writeString(pgnPath, TWO_GAMES_PGN);
            new PgnIndexingFacade().indexFile(pgnPath, null);

            List<Double> progresses = new ArrayList<>();
            List<String> statuses = new ArrayList<>();

            PgnToBookConverter.buildStats(pgnPath, PgnToBookConverter.BuildOptions.defaults(),
                    (progress, status) -> {
                        progresses.add(progress);
                        statuses.add(status);
                    });

            // Должно быть: 0.90 (building), 1.0 (processing complete)
            assertThat(progresses).isNotEmpty();
            assertThat(progresses).contains(0.90);
            assertThat(progresses).contains(1.0);
        }

        @Test
        @DisplayName("PGN не найден → IOException")
        void missingPgnThrows() {
            assertThatThrownBy(() -> PgnToBookConverter.buildStats(
                    Path.of("nonexistent.pgn"),
                    PgnToBookConverter.BuildOptions.defaults(),
                    null))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("not found");
        }
    }

    @Nested
    @DisplayName("convert (полный цикл)")
    class ConvertIntegration {

        private static final String SIMPLE_GAME_PGN = """
                [Event "Test"]
                [Site "?"]
                [Date "2026.01.01"]
                [White "A"]
                [Black "B"]
                [Result "1-0"]
                
                1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 1-0
                """;

        @Test
        @DisplayName("Создаёт .bin, который читается обратно")
        void createsReadableBin(@TempDir Path tempDir) throws IOException {
            // 1. Готовим PGN + индекс
            Path pgnPath = tempDir.resolve("test.pgn");
            Files.writeString(pgnPath, SIMPLE_GAME_PGN);
            new PgnIndexingFacade().indexFile(pgnPath, null);

            Path binPath = tempDir.resolve("test.bin");

            // 2. Конвертируем
            PgnToBookConverter.BuildResult result = PgnToBookConverter.convert(
                    pgnPath, binPath,
                    PgnToBookConverter.BuildOptions.defaults(),
                    null);

            // 3. Проверяем результат
            assertThat(result.gamesProcessed()).isEqualTo(1);
            assertThat(result.entriesWritten()).isGreaterThan(0);
            assertThat(Files.exists(binPath)).isTrue();

            // 4. Читаем .bin обратно
            try (PolyglotBookParser parser = new PolyglotBookParser()) {
                parser.open(binPath);

                assertThat(parser.getTotalEntries()).isEqualTo(result.entriesWritten());

                // Проверяем стартовую позицию
                long startKey = ZobristHasher.calculateWithoutCache(new Board());
                List<PolyglotEntry> rootMoves = parser.findAllEntries(startKey);

                assertThat(rootMoves).isNotEmpty();
                assertThat(rootMoves.stream().anyMatch(e -> e.getUciMove().equals("e2e4"))).isTrue();
            }
        }

        @Test
        @DisplayName("progressCb вызывается с правильной последовательностью")
        void convertCallsProgress(@TempDir Path tempDir) throws IOException {
            Path pgnPath = tempDir.resolve("test.pgn");
            Files.writeString(pgnPath, SIMPLE_GAME_PGN);
            new PgnIndexingFacade().indexFile(pgnPath, null);

            Path binPath = tempDir.resolve("test.bin");

            AtomicInteger callCount = new AtomicInteger();
            List<Double> progresses = new ArrayList<>();

            PgnToBookConverter.convert(pgnPath, binPath,
                    PgnToBookConverter.BuildOptions.defaults(),
                    (progress, status) -> {
                        callCount.incrementAndGet();
                        progresses.add(progress);
                    });

            assertThat(callCount.get()).isGreaterThanOrEqualTo(3); // stats-start, write, complete
            assertThat(progresses).last().isEqualTo(1.0);
        }
    }
}