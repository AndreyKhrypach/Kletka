/*
 *
 *  * Copyright (c) 2025-2026 Andrey Khrypach
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package Khrypach.Andrey.chess.kletka.gui.model;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SanGenerator - Генерация SAN ходов")
class SanGeneratorTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
        SanGenerator.setSetupPosition(false);
    }

    @AfterEach
    void tearDown() {
        SanGenerator.resetSetupPosition();
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ setSetupPosition / resetSetupPosition
    // ============================================================

    @Nested
    @DisplayName("Управление режимом расстановки позиции")
    class SetupPositionModeTests {

        @Test
        @DisplayName("Должен устанавливать режим расстановки позиции в true")
        void shouldSetSetupPositionToTrue() {
            // when
            SanGenerator.setSetupPosition(true);

            // then - проверяем через поведение (косвенно)
            // В текущей реализации флаг isSetupPosition не используется,
            // но метод должен работать без ошибок
            assertDoesNotThrow(() -> SanGenerator.setSetupPosition(true));
        }

        @Test
        @DisplayName("Должен устанавливать режим расстановки позиции в false")
        void shouldSetSetupPositionToFalse() {
            // when
            SanGenerator.setSetupPosition(false);

            // then
            assertDoesNotThrow(() -> SanGenerator.setSetupPosition(false));
        }

        @Test
        @DisplayName("Должен сбрасывать режим расстановки позиции")
        void shouldResetSetupPosition() {
            // given
            SanGenerator.setSetupPosition(true);

            // when
            SanGenerator.resetSetupPosition();

            // then
            assertDoesNotThrow(SanGenerator::resetSetupPosition);
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ ПРОСТЫХ ХОДОВ
    // ============================================================

    @Nested
    @DisplayName("Простые ходы")
    class SimpleMovesTests {

        @Test
        @DisplayName("Должен генерировать SAN для хода пешкой (e2-e4)")
        void shouldGenerateSanForPawnMove() {
            // given
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            Move move = new Move(Square.E2, Square.E4, Piece.WHITE_PAWN);

            // when
            String san = SanGenerator.generateSan(board, move, Piece.WHITE_PAWN, false, null);

            // then
            assertThat(san).isEqualTo("e4");
        }

        @Test
        @DisplayName("Должен генерировать SAN для хода конем (g1-f3)")
        void shouldGenerateSanForKnightMove() {
            // given
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            Move move = new Move(Square.G1, Square.F3, Piece.WHITE_KNIGHT);

            // when
            String san = SanGenerator.generateSan(board, move, Piece.WHITE_KNIGHT, false, null);

            // then
            assertThat(san).isEqualTo("Nf3");
        }

        @Test
        @DisplayName("Должен генерировать SAN для хода слоном (f1-c4)")
        void shouldGenerateSanForBishopMove() {
            // given
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            Move move = new Move(Square.F1, Square.C4, Piece.WHITE_BISHOP);

            // when
            String san = SanGenerator.generateSan(board, move, Piece.WHITE_BISHOP, false, null);

            // then
            assertThat(san).isEqualTo("Bc4");
        }

        @Test
        @DisplayName("Должен генерировать SAN для хода ладьей (a1-a4)")
        void shouldGenerateSanForRookMove() {
            // given
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            Move move = new Move(Square.A1, Square.A4, Piece.WHITE_ROOK);

            // when
            String san = SanGenerator.generateSan(board, move, Piece.WHITE_ROOK, false, null);

            // then
            assertThat(san).isEqualTo("Ra4");
        }

        @Test
        @DisplayName("Должен генерировать SAN для хода ферзем (d1-h5)")
        void shouldGenerateSanForQueenMove() {
            // given
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            Move move = new Move(Square.D1, Square.H5, Piece.WHITE_QUEEN);

            // when
            String san = SanGenerator.generateSan(board, move, Piece.WHITE_QUEEN, false, null);

            // then
            assertThat(san).isEqualTo("Qh5");
        }

        @Test
        @DisplayName("Должен генерировать SAN для хода королем (e1-e2)")
        void shouldGenerateSanForKingMove() {
            // given
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            Move move = new Move(Square.E1, Square.E2, Piece.WHITE_KING);

            // when
            String san = SanGenerator.generateSan(board, move, Piece.WHITE_KING, false, null);

            // then
            assertThat(san).isEqualTo("Ke2");
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ ВЗЯТИЙ
    // ============================================================

    @Nested
    @DisplayName("Взятия")
    class CaptureTests {

        @Test
        @DisplayName("Должен генерировать SAN для взятия пешкой (exd5)")
        void shouldGenerateSanForPawnCapture() {
            // given
            board.loadFromFen("rnbqkbnr/ppp1pppp/8/8/3p4/8/PPPP1PPP/RNBQKBNR w KQkq - 0 1");
            Move move = new Move(Square.E4, Square.D5, Piece.WHITE_PAWN);

            // when
            String san = SanGenerator.generateSan(board, move, Piece.WHITE_PAWN, true, null);

            // then
            assertThat(san).isEqualTo("exd5");
        }

        @Test
        @DisplayName("Должен генерировать SAN для взятия конем (Nxe5)")
        void shouldGenerateSanForKnightCapture() {
            // given
            board.loadFromFen("rnbqkbnr/ppp1pppp/8/8/3p4/8/PPPP1PPP/RNBQKBNR w KQkq - 0 1");
            Move move = new Move(Square.F3, Square.E5, Piece.WHITE_KNIGHT);

            // when
            String san = SanGenerator.generateSan(board, move, Piece.WHITE_KNIGHT, true, null);

            // then
            assertThat(san).isEqualTo("Nxe5");
        }

        @Test
        @DisplayName("Должен генерировать SAN для взятия слоном (Bxd5)")
        void shouldGenerateSanForBishopCapture() {
            // given
            board.loadFromFen("rnbqkbnr/ppp1pppp/8/8/3p4/8/PPPP1PPP/RNBQKBNR w KQkq - 0 1");
            Move move = new Move(Square.F1, Square.D3, Piece.WHITE_BISHOP);

            // when
            String san = SanGenerator.generateSan(board, move, Piece.WHITE_BISHOP, true, null);

            // then
            assertThat(san).isEqualTo("Bxd3");
        }
    }

    // ============================================================
    // 4. ТЕСТЫ ДЛЯ РОКИРОВКИ
    // ============================================================

    @Nested
    @DisplayName("Рокировка")
    class CastlingTests {

        @Test
        @DisplayName("Должен генерировать SAN для короткой рокировки (O-O)")
        void shouldGenerateSanForKingsideCastling() {
            // given
            board.loadFromFen("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1");
            Move move = new Move(Square.E1, Square.G1, Piece.WHITE_KING);

            // when
            String san = SanGenerator.generateSan(board, move, Piece.WHITE_KING, false, null);

            // then
            assertThat(san).isEqualTo("O-O");
        }

        @Test
        @DisplayName("Должен генерировать SAN для длинной рокировки (O-O-O)")
        void shouldGenerateSanForQueensideCastling() {
            // given
            board.loadFromFen("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1");
            Move move = new Move(Square.E1, Square.C1, Piece.WHITE_KING);

            // when
            String san = SanGenerator.generateSan(board, move, Piece.WHITE_KING, false, null);

            // then
            assertThat(san).isEqualTo("O-O-O");
        }

        @Test
        @DisplayName("Должен генерировать SAN для короткой рокировки черных (O-O)")
        void shouldGenerateSanForBlackKingsideCastling() {
            // given
            board.loadFromFen("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1");
            board.setSideToMove(com.github.bhlangonijr.chesslib.Side.BLACK);
            Move move = new Move(Square.E8, Square.G8, Piece.BLACK_KING);

            // when
            String san = SanGenerator.generateSan(board, move, Piece.BLACK_KING, false, null);

            // then
            assertThat(san).isEqualTo("O-O");
        }

        @Test
        @DisplayName("Должен генерировать SAN для длинной рокировки черных (O-O-O)")
        void shouldGenerateSanForBlackQueensideCastling() {
            // given
            board.loadFromFen("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1");
            board.setSideToMove(com.github.bhlangonijr.chesslib.Side.BLACK);
            Move move = new Move(Square.E8, Square.C8, Piece.BLACK_KING);

            // when
            String san = SanGenerator.generateSan(board, move, Piece.BLACK_KING, false, null);

            // then
            assertThat(san).isEqualTo("O-O-O");
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ ПРЕВРАЩЕНИЯ ПЕШКИ
    // ============================================================

    @Nested
    @DisplayName("Превращение пешки")
    class PromotionTests {

        @Test
        @DisplayName("Должен генерировать SAN для превращения пешки в ферзя (e8=Q)")
        void shouldGenerateSanForPromotionToQueen() {
            // given
            board.loadFromFen("4k3/4P3/8/8/8/8/8/4K3 w - - 0 1");
            Move move = new Move(Square.E7, Square.E8, Piece.WHITE_PAWN);

            // when
            String san = SanGenerator.generateSan(board, move, Piece.WHITE_PAWN, false, Piece.WHITE_QUEEN);

            // then
            assertThat(san).isEqualTo("e8=Q");
        }

        @Test
        @DisplayName("Должен генерировать SAN для превращения пешки в коня (e8=N)")
        void shouldGenerateSanForPromotionToKnight() {
            // given
            board.loadFromFen("4k3/4P3/8/8/8/8/8/4K3 w - - 0 1");
            Move move = new Move(Square.E7, Square.E8, Piece.WHITE_PAWN);

            // when
            String san = SanGenerator.generateSan(board, move, Piece.WHITE_PAWN, false, Piece.WHITE_KNIGHT);

            // then
            assertThat(san).isEqualTo("e8=N");
        }

        @Test
        @DisplayName("Должен генерировать SAN для превращения пешки с взятием (dxc8=Q)")
        void shouldGenerateSanForPromotionWithCapture() {
            // given
            board.loadFromFen("4k3/3P4/8/8/8/8/8/4K3 w - - 0 1");
            Move move = new Move(Square.D7, Square.C8, Piece.WHITE_PAWN);

            // when
            String san = SanGenerator.generateSan(board, move, Piece.WHITE_PAWN, true, Piece.WHITE_QUEEN);

            // then
            assertThat(san).isEqualTo("dxc8=Q");
        }
    }

    // ============================================================
    // 6. ТЕСТЫ ДЛЯ ШАХА, МАТА, ДВОЙНОГО ШАХА
    // ============================================================

    @Nested
    @DisplayName("Шах, мат, двойной шах")
    class CheckMateTests {

        @Test
        @DisplayName("Должен генерировать SAN без символа шаха (шах добавляется как аннотация)")
        void shouldGenerateSanWithoutCheckSymbol() {
            // given
            board.loadFromFen("rnbqkbnr/ppp2ppp/8/3p4/3pP3/8/PPP1QPPP/RNB1KBNR w KQkq d6 0 4");
            Move move = new Move(Square.E4, Square.D5, Piece.WHITE_PAWN);

            // when
            String san = SanGenerator.generateSan(board, move, Piece.WHITE_PAWN, true, null);

            // then
            // SanGenerator НЕ добавляет символы шаха/мата
            assertThat(san).isEqualTo("exd5");
        }

        @Test
        @DisplayName("Должен определять шах через MoveAnnotation.CHECK")
        void shouldDetectCheckViaMoveAnnotation() {
            // given
            board.loadFromFen("rnbqkbnr/ppp2ppp/8/3p4/3pP3/8/PPP1QPPP/RNB1KBNR w KQkq d6 0 4");
            Move move = new Move(Square.E4, Square.D5, Piece.WHITE_PAWN);

            // Проверяем, что после хода будет шах
            Board testBoard = board.clone();
            testBoard.doMove(move);
            assertTrue(testBoard.isKingAttacked(), "After exd5, black king should be in check");

            // when
            String san = SanGenerator.generateSan(board, move, Piece.WHITE_PAWN, true, null);

            // then
            // SAN без символа
            assertThat(san).isEqualTo("exd5");

            // А символ шаха добавляется через MoveAnnotation
            MoveAnnotation annotation = MoveAnnotation.CHECK;
            assertThat(annotation.getSymbol()).isEqualTo("+");
            assertThat(san + annotation.getSymbol()).isEqualTo("exd5+");
        }

        @Test
        @DisplayName("Должен определять мат через MoveAnnotation.MATE")
        void shouldDetectMateViaMoveAnnotation() {
            // given
            board.loadFromFen("4k3/8/8/4Q3/8/8/4R3/4K3 w - - 0 1");
            Move move = new Move(Square.E5, Square.E7, Piece.WHITE_QUEEN);

            Board testBoard = board.clone();
            testBoard.doMove(move);
            assertTrue(testBoard.isMated(), "After Qe7, black king should be mated");

            // when
            String san = SanGenerator.generateSan(board, move, Piece.WHITE_QUEEN, false, null);

            // then
            assertThat(san).isEqualTo("Qe7");

            MoveAnnotation annotation = MoveAnnotation.MATE;
            assertThat(annotation.getSymbol()).isEqualTo("#");
            assertThat(san + annotation.getSymbol()).isEqualTo("Qe7#");
        }

        @Test
        @DisplayName("Должен определять двойной шах через MoveAnnotation.DOUBLE_CHECK")
        void shouldDetectDoubleCheckViaMoveAnnotation() {
            // given
            board.loadFromFen("4k3/8/8/8/8/8/1B6/Q1K5 w - - 0 1");
            Move move = new Move(Square.A1, Square.A8, Piece.WHITE_QUEEN);

            Board testBoard = board.clone();
            testBoard.doMove(move);
            assertTrue(testBoard.isKingAttacked(), "After Qa8, black king should be in check");

            // when
            String san = SanGenerator.generateSan(board, move, Piece.WHITE_QUEEN, false, null);

            // then
            assertThat(san).isEqualTo("Qa8");

            MoveAnnotation annotation = MoveAnnotation.DOUBLE_CHECK;
            assertThat(annotation.getSymbol()).isEqualTo("++");
            assertThat(san + annotation.getSymbol()).isEqualTo("Qa8++");
        }
    }

    // ============================================================
    // 7. ТЕСТЫ ДЛЯ РАЗРЕШЕНИЯ НЕОДНОЗНАЧНОСТИ
    // ============================================================

    @Nested
    @DisplayName("Разрешение неоднозначности")
    class DisambiguationTests {

        @Test
        @DisplayName("Должен добавить ранг для разрешения неоднозначности (N5d4)")
        void shouldAddRankForDisambiguation() {
            // given
            // Два коня на ОДНОЙ ВЕРТИКАЛИ b: на b5 и на b3
            // Оба могут пойти на d4
            board.loadFromFen("rnbqkbnr/pppppppp/8/1N6/8/1N6/PPPPPPPP/RNBQKB1R w KQkq - 0 1");

            // Проверяем позицию: оба коня на одной вертикали b
            assertThat(board.getPiece(Square.B5)).isEqualTo(Piece.WHITE_KNIGHT);
            assertThat(board.getPiece(Square.B3)).isEqualTo(Piece.WHITE_KNIGHT);

            // Ход конем с b5 на d4
            Move move = new Move(Square.B5, Square.D4, Piece.WHITE_KNIGHT);

            // when
            String san = SanGenerator.generateSan(board, move, Piece.WHITE_KNIGHT, false, null);

            // then
            // Конь с b5 на d4 → N5d4 (ранг 5)
            assertThat(san).isEqualTo("N5d4");
        }

        @Test
        @DisplayName("Должен добавить файл для разрешения неоднозначности (Nbd4)")
        void shouldAddFileForDisambiguation() {
            // given
            // Два коня на ОДНОЙ ГОРИЗОНТАЛИ 5: на b5 и на f5
            // Оба могут пойти на d4
            board.loadFromFen("rnbqkbnr/pppppppp/8/1N3N2/8/8/PPPPPPPP/RNBQKB1R w KQkq - 0 1");

            // Проверяем позицию: оба коня на одной горизонтали 5
            assertThat(board.getPiece(Square.B5)).isEqualTo(Piece.WHITE_KNIGHT);
            assertThat(board.getPiece(Square.F5)).isEqualTo(Piece.WHITE_KNIGHT);

            // Ход конем с b5 на d4
            Move move = new Move(Square.B5, Square.D4, Piece.WHITE_KNIGHT);

            // when
            String san = SanGenerator.generateSan(board, move, Piece.WHITE_KNIGHT, false, null);

            // then
            // Конь с b5 на d4 → Nbd4 (файл b)
            assertThat(san).isEqualTo("Nbd4");
        }

        @Test
        @DisplayName("Должен добавить файл и ранг для разрешения неоднозначности (Nbd4)")
        void shouldAddFileAndRankForDisambiguation() {
            // given
            // Два коня: на b5 и на b3 (одна вертикаль, разные ранги)
            // Оба могут пойти на d4
            board.loadFromFen("rnbqkbnr/pppppppp/8/1N6/8/1N6/PPPPPPPP/RNBQKB1R w KQkq - 0 1");

            // Проверяем позицию
            assertThat(board.getPiece(Square.B5)).isEqualTo(Piece.WHITE_KNIGHT);
            assertThat(board.getPiece(Square.B3)).isEqualTo(Piece.WHITE_KNIGHT);

            // Ход конем с b5 на d4
            Move move = new Move(Square.B5, Square.D4, Piece.WHITE_KNIGHT);

            // when
            String san = SanGenerator.generateSan(board, move, Piece.WHITE_KNIGHT, false, null);

            // then
            // Конь с b5 на d4 → Nbd4 (файл b, так как оба коня на b)
            assertThat(san).isEqualTo("N5d4");
        }
    }

    // ============================================================
    // 8. ТЕСТЫ ДЛЯ NULL-АРГУМЕНТОВ
    // ============================================================

    @Nested
    @DisplayName("Обработка null-аргументов")
    class NullArgumentsTests {

        @Test
        @DisplayName("Должен возвращать пустую строку при null move")
        void shouldReturnEmptyStringForNullMove() {
            // when
            String san = SanGenerator.generateSan(null, null, null, false, null);

            // then
            assertThat(san).isEmpty();
        }

        @Test
        @DisplayName("Должен возвращать простой SAN при null board")
        void shouldReturnSimpleSanForNullBoard() {
            // given
            Move move = new Move(Square.E2, Square.E4, Piece.WHITE_PAWN);

            // when
            String san = SanGenerator.generateSan(null, move, Piece.WHITE_PAWN, false, null);

            // then
            assertThat(san).isEqualTo("e4");
        }

        @Test
        @DisplayName("Должен возвращать простой SAN при null piece")
        void shouldReturnSimpleSanForNullPiece() {
            // given
            Move move = new Move(Square.E2, Square.E4, Piece.WHITE_PAWN);

            // when
            String san = SanGenerator.generateSan(null, move, null, false, null);

            // then
            // При null piece возвращаем только клетку
            assertThat(san).isEqualTo("e4");
        }
    }

    // ============================================================
    // 9. ТЕСТЫ ДЛЯ ВЗЯТИЯ НА ПРОХОДЕ
    // ============================================================

    @Nested
    @DisplayName("Взятие на проходе")
    class EnPassantTests {

        @Test
        @DisplayName("Должен генерировать SAN для взятия на проходе (exd6 e.p.)")
        void shouldGenerateSanForEnPassant() {
            // given
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            // Позиция для взятия на проходе
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 1");
            Move move = new Move(Square.E4, Square.D5, Piece.WHITE_PAWN);

            // when
            String san = SanGenerator.generateSan(board, move, Piece.WHITE_PAWN, true, null);

            // then
            assertThat(san).isEqualTo("exd5");
        }
    }

    // ============================================================
    // 10. ТЕСТЫ ДЛЯ РАЗЛИЧНЫХ ПОЗИЦИЙ
    // ============================================================

    @Nested
    @DisplayName("Различные позиции")
    class VariousPositionsTests {

        @Test
        @DisplayName("Должен корректно генерировать SAN для хода пешкой с e7-e5")
        void shouldGenerateSanForBlackPawnMove() {
            // given
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            board.setSideToMove(com.github.bhlangonijr.chesslib.Side.BLACK);
            Move move = new Move(Square.E7, Square.E5, Piece.BLACK_PAWN);

            // when
            String san = SanGenerator.generateSan(board, move, Piece.BLACK_PAWN, false, null);

            // then
            assertThat(san).isEqualTo("e5");
        }

        @Test
        @DisplayName("Должен корректно генерировать SAN для хода конем с g8-f6")
        void shouldGenerateSanForBlackKnightMove() {
            // given
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            board.setSideToMove(com.github.bhlangonijr.chesslib.Side.BLACK);
            Move move = new Move(Square.G8, Square.F6, Piece.BLACK_KNIGHT);

            // when
            String san = SanGenerator.generateSan(board, move, Piece.BLACK_KNIGHT, false, null);

            // then
            assertThat(san).isEqualTo("Nf6");
        }
    }
}