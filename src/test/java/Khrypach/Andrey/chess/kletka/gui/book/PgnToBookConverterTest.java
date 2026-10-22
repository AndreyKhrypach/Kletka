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
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.move.Move;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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

            // Делаем ход e4 (белые)
            Move e4 = PgnToBookConverter.sanToMove(board, "e4");
            assertThat(e4).isNotNull();
            board.doMove(e4);

            // Делаем ход e5 (черные)
            Move e5 = PgnToBookConverter.sanToMove(board, "e5");
            assertThat(e5).isNotNull();
            board.doMove(e5);

            // Проверяем Nf3 (белые)
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
            // from=E2(12), to=E4(28). Код = 12 << 6 | 28 = 796
            assertThat(code).isEqualTo((short) ((0) | (12 << 6) | 28));
        }

        @Test
        void promotionQueen() {
            short code = PgnToBookConverter.encodeUciToPolyglot("e7e8q");
            // promo=4, from=E7(52), to=E8(60). Код = 4 << 12 | 52 << 6 | 60
            assertThat(code).isEqualTo((short) ((4 << 12) | (52 << 6) | 60));
        }

        @Test
        void invalidUci() {
            assertThat(PgnToBookConverter.encodeUciToPolyglot("xx")).isZero();
            assertThat(PgnToBookConverter.encodeUciToPolyglot(null)).isZero();
        }
    }

    // ========== 4. ТЕСТЫ ЛОГИКИ (processGame, buildEntries) ==========

    @Nested
    @DisplayName("processGame")
    class ProcessGame {
        @Test
        void whiteWinStatistics() {
            Map<Long, Map<String, int[]>> stats = new HashMap<>();
            List<String> sans = List.of("e4", "e5");
            PgnToBookConverter.processGame(sans, Outcome.WHITE_WIN, PgnToBookConverter.BuildOptions.defaults(), stats);

            // Проверяем начальную позицию (ход белых e4)
            Board board = new Board();
            long startKey = ZobristHasher.calculateWithoutCache(board);
            int[] e4Stats = stats.get(startKey).get("e2e4");
            assertThat(e4Stats).containsExactly(1, 0, 0); // white win

            // Проверяем позицию после e4 (ход черных e5)
            board.doMove(PgnToBookConverter.sanToMove(board, "e4"));
            long afterE4Key = ZobristHasher.calculateWithoutCache(board);
            int[] e5Stats = stats.get(afterE4Key).get("e7e5");
            assertThat(e5Stats).containsExactly(0, 1, 0); // black loss
        }

        @Test
        void drawStatistics() {
            Map<Long, Map<String, int[]>> stats = new HashMap<>();
            List<String> sans = List.of("d4", "d5");
            PgnToBookConverter.processGame(sans, Outcome.DRAW, PgnToBookConverter.BuildOptions.defaults(), stats);

            Board board = new Board();
            long startKey = ZobristHasher.calculateWithoutCache(board);
            int[] d4Stats = stats.get(startKey).get("d2d4");
            assertThat(d4Stats).containsExactly(0, 0, 1); // draw
        }

        @Test
        void unknownOutcomeSkipsGame() {
            Map<Long, Map<String, int[]>> stats = new HashMap<>();
            List<String> sans = List.of("e4", "e5");
            PgnToBookConverter.processGame(sans, Outcome.UNKNOWN, PgnToBookConverter.BuildOptions.defaults(), stats);

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
            stats.computeIfAbsent(key, k -> new HashMap<>()).put("e2e4", new int[]{3, 1, 2}); // 3W, 1L, 2D

            List<PolyglotEntry> entries = PgnToBookConverter.buildEntries(stats, PgnToBookConverter.BuildOptions.defaults());

            assertThat(entries).hasSize(1);
            PolyglotEntry entry = entries.get(0);
            assertThat(entry.key()).isEqualTo(key);
            assertThat(entry.weight()).isEqualTo(3 * 2 + 2); // 8
            assertThat(entry.games()).isEqualTo(6);
        }

        @Test
        void respectsMinGames() {
            Map<Long, Map<String, int[]>> stats = new HashMap<>();
            long key = ZobristHasher.calculateWithoutCache(new Board());
            stats.computeIfAbsent(key, k -> new HashMap<>()).put("e2e4", new int[]{1, 0, 0}); // только 1 партия

            PgnToBookConverter.BuildOptions options = new PgnToBookConverter.BuildOptions(40, 2); // minGames = 2
            List<PolyglotEntry> entries = PgnToBookConverter.buildEntries(stats, options);

            assertThat(entries).isEmpty();
        }

        @Test
        void weightNeverZero() {
            Map<Long, Map<String, int[]>> stats = new HashMap<>();
            long key = ZobristHasher.calculateWithoutCache(new Board());
            stats.computeIfAbsent(key, k -> new HashMap<>()).put("e2e4", new int[]{0, 5, 0}); // только поражения

            List<PolyglotEntry> entries = PgnToBookConverter.buildEntries(stats, PgnToBookConverter.BuildOptions.defaults());

            assertThat(entries).hasSize(1);
            assertThat(entries.get(0).weight()).isEqualTo(1); // сбросилось на 1
        }
    }
}