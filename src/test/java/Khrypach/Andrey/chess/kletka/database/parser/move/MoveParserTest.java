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

package Khrypach.Andrey.chess.kletka.database.parser.move;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MoveParser - Парсинг ходов")
class MoveParserTest {

    private MoveParser moveParser;

    @BeforeEach
    void setUp() {
        moveParser = new MoveParser();
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ charToPiece()
    // ============================================================

    @Nested
    @DisplayName("charToPiece() - Преобразование символа в фигуру")
    class CharToPieceTests {

        @Test
        @DisplayName("Должен преобразовывать 'R' в белую ладью для белых")
        void shouldConvertRToWhiteRook() {
            // when
            Piece piece = moveParser.charToPiece('R', Side.WHITE);

            // then
            assertThat(piece).isEqualTo(Piece.WHITE_ROOK);
        }

        @Test
        @DisplayName("Должен преобразовывать 'r' в белую ладью для белых (регистронезависимо)")
        void shouldConvertLowercaseRToWhiteRook() {
            // when
            Piece piece = moveParser.charToPiece('r', Side.WHITE);

            // then
            assertThat(piece).isEqualTo(Piece.WHITE_ROOK);
        }

        @Test
        @DisplayName("Должен преобразовывать 'R' в черную ладью для черных")
        void shouldConvertRToBlackRook() {
            // when
            Piece piece = moveParser.charToPiece('R', Side.BLACK);

            // then
            assertThat(piece).isEqualTo(Piece.BLACK_ROOK);
        }

        @Test
        @DisplayName("Должен преобразовывать 'N' в белого коня для белых")
        void shouldConvertNToWhiteKnight() {
            // when
            Piece piece = moveParser.charToPiece('N', Side.WHITE);

            // then
            assertThat(piece).isEqualTo(Piece.WHITE_KNIGHT);
        }

        @Test
        @DisplayName("Должен преобразовывать 'B' в белого слона для белых")
        void shouldConvertBToWhiteBishop() {
            // when
            Piece piece = moveParser.charToPiece('B', Side.WHITE);

            // then
            assertThat(piece).isEqualTo(Piece.WHITE_BISHOP);
        }

        @Test
        @DisplayName("Должен преобразовывать 'Q' в белого ферзя для белых")
        void shouldConvertQToWhiteQueen() {
            // when
            Piece piece = moveParser.charToPiece('Q', Side.WHITE);

            // then
            assertThat(piece).isEqualTo(Piece.WHITE_QUEEN);
        }

        @Test
        @DisplayName("Должен преобразовывать неизвестный символ в ферзя")
        void shouldConvertUnknownToQueen() {
            // when
            Piece piece = moveParser.charToPiece('X', Side.WHITE);

            // then
            assertThat(piece).isEqualTo(Piece.WHITE_QUEEN);
        }

        @Test
        @DisplayName("Должен преобразовывать 'N' в черного коня для черных")
        void shouldConvertNToBlackKnight() {
            // when
            Piece piece = moveParser.charToPiece('N', Side.BLACK);

            // then
            assertThat(piece).isEqualTo(Piece.BLACK_KNIGHT);
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ getPieceChar()
    // ============================================================

    @Nested
    @DisplayName("getPieceChar() - Преобразование фигуры в символ")
    class GetPieceCharTests {

        @Test
        @DisplayName("Должен возвращать 'K' для белого короля")
        void shouldReturnKForWhiteKing() {
            // when
            char c = moveParser.getPieceChar(Piece.WHITE_KING);

            // then
            assertThat(c).isEqualTo('K');
        }

        @Test
        @DisplayName("Должен возвращать 'K' для черного короля")
        void shouldReturnKForBlackKing() {
            // when
            char c = moveParser.getPieceChar(Piece.BLACK_KING);

            // then
            assertThat(c).isEqualTo('K');
        }

        @Test
        @DisplayName("Должен возвращать 'Q' для белого ферзя")
        void shouldReturnQForWhiteQueen() {
            // when
            char c = moveParser.getPieceChar(Piece.WHITE_QUEEN);

            // then
            assertThat(c).isEqualTo('Q');
        }

        @Test
        @DisplayName("Должен возвращать 'Q' для черного ферзя")
        void shouldReturnQForBlackQueen() {
            // when
            char c = moveParser.getPieceChar(Piece.BLACK_QUEEN);

            // then
            assertThat(c).isEqualTo('Q');
        }

        @Test
        @DisplayName("Должен возвращать 'R' для белой ладьи")
        void shouldReturnRForWhiteRook() {
            // when
            char c = moveParser.getPieceChar(Piece.WHITE_ROOK);

            // then
            assertThat(c).isEqualTo('R');
        }

        @Test
        @DisplayName("Должен возвращать 'B' для белого слона")
        void shouldReturnBForWhiteBishop() {
            // when
            char c = moveParser.getPieceChar(Piece.WHITE_BISHOP);

            // then
            assertThat(c).isEqualTo('B');
        }

        @Test
        @DisplayName("Должен возвращать 'N' для белого коня")
        void shouldReturnNForWhiteKnight() {
            // when
            char c = moveParser.getPieceChar(Piece.WHITE_KNIGHT);

            // then
            assertThat(c).isEqualTo('N');
        }

        @Test
        @DisplayName("Должен возвращать пробел для NONE")
        void shouldReturnSpaceForNone() {
            // when
            char c = moveParser.getPieceChar(Piece.NONE);

            // then
            assertThat(c).isEqualTo(' ');
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ convertSanToMove()
    // ============================================================

    @Nested
    @DisplayName("convertSanToMove() - Конвертация SAN в Move")
    class ConvertSanToMoveTests {

        @Test
        @DisplayName("Должен конвертировать пешечный ход e4")
        void shouldConvertPawnMoveE4() {
            // given
            Board board = new Board();
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

            // when
            Move move = moveParser.convertSanToMove("e4", board);

            // then
            assertThat(move).isNotNull();
            assertThat(move.getFrom()).isEqualTo(Square.E2);
            assertThat(move.getTo()).isEqualTo(Square.E4);
        }

        @Test
        @DisplayName("Должен конвертировать пешечный ход d5")
        void shouldConvertPawnMoveD5() {
            // given
            Board board = new Board();
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

            // when
            Move move = moveParser.convertSanToMove("d4", board);

            // then
            assertThat(move).isNotNull();
            assertThat(move.getFrom()).isEqualTo(Square.D2);
            assertThat(move.getTo()).isEqualTo(Square.D4);
        }

        @Test
        @DisplayName("Должен конвертировать ход коня Nf3")
        void shouldConvertKnightMoveNf3() {
            // given
            Board board = new Board();
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

            // when
            Move move = moveParser.convertSanToMove("Nf3", board);

            // then
            assertThat(move).isNotNull();
            assertThat(move.getFrom()).isEqualTo(Square.G1);
            assertThat(move.getTo()).isEqualTo(Square.F3);
        }

        @Test
        @DisplayName("Должен конвертировать короткую рокировку O-O")
        void shouldConvertKingsideCastling() {
            // given
            Board board = new Board();
            board.loadFromFen("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1");

            // when
            Move move = moveParser.convertSanToMove("O-O", board);

            // then
            assertThat(move).isNotNull();
            assertThat(move.getFrom()).isEqualTo(Square.E1);
            assertThat(move.getTo()).isEqualTo(Square.G1);
        }

        @Test
        @DisplayName("Должен конвертировать длинную рокировку O-O-O")
        void shouldConvertQueensideCastling() {
            // given
            Board board = new Board();
            board.loadFromFen("r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1");

            // when
            Move move = moveParser.convertSanToMove("O-O-O", board);

            // then
            assertThat(move).isNotNull();
            assertThat(move.getFrom()).isEqualTo(Square.E1);
            assertThat(move.getTo()).isEqualTo(Square.C1);
        }

        @Test
        @DisplayName("Должен конвертировать взятие пешкой exd5")
        void shouldConvertPawnCaptureExd5() {
            // given
            Board board = new Board();
            board.loadFromFen("rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 1");

            // when
            Move move = moveParser.convertSanToMove("exd5", board);

            // then
            assertThat(move).isNotNull();
            assertThat(move.getFrom()).isEqualTo(Square.E4);
            assertThat(move.getTo()).isEqualTo(Square.D5);
        }

        @Test
        @DisplayName("Должен возвращать null для нелегального хода")
        void shouldReturnNullForIllegalMove() {
            // given
            Board board = new Board();
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

            // when
            Move move = moveParser.convertSanToMove("e5", board);

            // then
            assertThat(move).isNull();
        }

        @Test
        @DisplayName("Должен возвращать null для пустого SAN")
        void shouldReturnNullForEmptySan() {
            // given
            Board board = new Board();

            // when
            Move move = moveParser.convertSanToMove("", board);

            // then
            assertThat(move).isNull();
        }

        @Test
        @DisplayName("Должен возвращать null для null SAN")
        void shouldReturnNullForNullSan() {
            // given
            Board board = new Board();

            // when
            Move move = moveParser.convertSanToMove(null, board);

            // then
            assertThat(move).isNull();
        }
    }

    // ============================================================
    // 4. ТЕСТЫ ДЛЯ findMoveBySan()
    // ============================================================

    @Nested
    @DisplayName("findMoveBySan() - Поиск хода по SAN")
    class FindMoveBySanTests {

        @Test
        @DisplayName("Должен находить пешечный ход e4")
        void shouldFindPawnMoveE4() {
            // given
            Board board = new Board();
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

            // when
            Move move = moveParser.findMoveBySan("e4", board);

            // then
            assertThat(move).isNotNull();
            assertThat(move.getFrom()).isEqualTo(Square.E2);
            assertThat(move.getTo()).isEqualTo(Square.E4);
        }

        @Test
        @DisplayName("Должен находить ход коня Nf3")
        void shouldFindKnightMoveNf3() {
            // given
            Board board = new Board();
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

            // when
            Move move = moveParser.findMoveBySan("Nf3", board);

            // then
            assertThat(move).isNotNull();
            assertThat(move.getFrom()).isEqualTo(Square.G1);
            assertThat(move.getTo()).isEqualTo(Square.F3);
        }

        @Test
        @DisplayName("Должен находить ход с превращением e8=Q")
        void shouldFindPromotionToQueen() {
            // given
            Board board = new Board();
            board.loadFromFen("2k5/4P3/8/8/8/8/8/4K3 w - - 0 1");

            // when
            Move move = moveParser.findMoveBySan("e8=Q", board);

            // then
            assertThat(move).isNotNull();
            assertThat(move.getFrom()).isEqualTo(Square.E7);
            assertThat(move.getTo()).isEqualTo(Square.E8);
            assertThat(move.getPromotion()).isEqualTo(Piece.WHITE_QUEEN);
        }

        @Test
        @DisplayName("Должен находить ход с превращением в коня e8=N")
        void shouldFindPromotionToKnight() {
            // given
            Board board = new Board();
            board.loadFromFen("2k5/4P3/8/8/8/8/8/4K3 w - - 0 1");

            // when
            Move move = moveParser.findMoveBySan("e8=N", board);

            // then
            assertThat(move).isNotNull();
            assertThat(move.getFrom()).isEqualTo(Square.E7);
            assertThat(move.getTo()).isEqualTo(Square.E8);
            assertThat(move.getPromotion()).isEqualTo(Piece.WHITE_KNIGHT);
        }

        @Test
        @DisplayName("Должен находить взятие с превращением dxc8=Q")
        void shouldFindPromotionWithCapture() {
            // given
            Board board = new Board();
            board.loadFromFen("2n2k2/3P4/8/8/8/8/8/4K3 w - - 0 1");

            // when
            Move move = moveParser.findMoveBySan("dxc8=Q", board);

            // then
            assertThat(move).isNotNull();
            assertThat(move.getFrom()).isEqualTo(Square.D7);
            assertThat(move.getTo()).isEqualTo(Square.C8);
            assertThat(move.getPromotion()).isEqualTo(Piece.WHITE_QUEEN);
        }

        @Test
        @DisplayName("Должен разрешать неоднозначность для коней (Nbd4)")
        void shouldResolveKnightDisambiguation() {
            // given
            Board board = new Board();
            board.loadFromFen("rnbqkbnr/pppppppp/8/1N6/8/1N6/PPPPPPPP/R1BQKB1R w KQkq - 0 1");

            // when
            Move move = moveParser.findMoveBySan("N5d4", board);

            // then
            assertThat(move).isNotNull();
            assertThat(move.getFrom()).isEqualTo(Square.B5);
            assertThat(move.getTo()).isEqualTo(Square.D4);
        }

        @Test
        @DisplayName("Должен возвращать null если ход не найден")
        void shouldReturnNullIfMoveNotFound() {
            // given
            Board board = new Board();
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

            // when
            Move move = moveParser.findMoveBySan("e5", board);

            // then
            assertThat(move).isNull();
        }

        @Test
        @DisplayName("Должен находить взятие Nxe5")
        void shouldFindKnightCapture() {
            // given
            Board board = new Board();
            board.loadFromFen("rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 1");

            // when
            Move move = moveParser.findMoveBySan("Nxe5", board);

            // then
            assertThat(move).isNotNull();
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ ИНТЕГРАЦИИ convertSanToMove И findMoveBySan
    // ============================================================

    @Nested
    @DisplayName("Интеграция convertSanToMove и findMoveBySan")
    class IntegrationTests {

        @Test
        @DisplayName("Должен корректно конвертировать и находить ход")
        void shouldConvertAndFindMove() {
            // given
            Board board = new Board();
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

            // when
            Move converted = moveParser.convertSanToMove("e4", board);
            Move found = moveParser.findMoveBySan("e4", board);

            // then
            assertThat(converted).isNotNull();
            assertThat(found).isNotNull();
            assertThat(converted.getFrom()).isEqualTo(found.getFrom());
            assertThat(converted.getTo()).isEqualTo(found.getTo());
        }

        @Test
        @DisplayName("Должен корректно конвертировать и находить ход с превращением")
        void shouldConvertAndFindPromotion() {
            // given
            Board board = new Board();
            board.loadFromFen("2k5/4P3/8/8/8/8/8/4K3 w - - 0 1");

            // when
            Move converted = moveParser.convertSanToMove("e8=Q", board);
            Move found = moveParser.findMoveBySan("e8=Q", board);

            // then
            assertThat(converted).isNotNull();
            assertThat(found).isNotNull();
            assertThat(converted.getFrom()).isEqualTo(found.getFrom());
            assertThat(converted.getTo()).isEqualTo(found.getTo());
            assertThat(converted.getPromotion()).isEqualTo(found.getPromotion());
        }
    }
}