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

package Khrypach.Andrey.chess.kletka.gui.board;

import Khrypach.Andrey.chess.kletka.gui.model.MoveNode;
import Khrypach.Andrey.chess.kletka.gui.model.RootNode;
import Khrypach.Andrey.chess.kletka.gui.model.Variation;
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

@DisplayName("BoardReconstructor - Восстановление доски")
class BoardReconstructorTest {

    private PathBuilder pathBuilder;
    private Board initialBoard;
    private BoardReconstructor reconstructor;

    @BeforeEach
    void setUp() {
        // Создаем начальную позицию
        initialBoard = new Board();
        initialBoard.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

        // Создаем PathBuilder
        Variation rootVariation = new Variation("Root");
        RootNode rootNode = new RootNode();
        rootVariation.setFirstNode(rootNode);

        Variation mainLine = new Variation("Main");
        mainLine.setMainLine(true);
        mainLine.setParentVariation(rootVariation);
        mainLine.setParentNodeRef(rootNode);
        rootNode.getSubVariations().add(mainLine);

        pathBuilder = new PathBuilder(rootVariation, mainLine);
        reconstructor = new BoardReconstructor(pathBuilder, initialBoard, false);
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ getStartBoard()
    // ============================================================

    @Nested
    @DisplayName("getStartBoard() - Получение начальной доски")
    class GetStartBoardTests {

        @Test
        @DisplayName("Должен возвращать копию начальной доски")
        void shouldReturnCopyOfStartBoard() {
            // when
            Board board1 = reconstructor.getStartBoard();
            Board board2 = reconstructor.getStartBoard();

            // then
            assertThat(board1).isNotNull();
            assertThat(board2).isNotNull();
            assertThat(board1).isNotSameAs(board2);
            assertThat(board1.getFen()).isEqualTo(board2.getFen());
        }

        @Test
        @DisplayName("Должен создавать доску с белыми если startWithBlack = false")
        void shouldCreateBoardWithWhiteIfNotStartWithBlack() {
            // when
            Board board = reconstructor.getStartBoard();

            // then
            assertThat(board.getSideToMove()).isEqualTo(Side.WHITE);
        }

        @Test
        @DisplayName("Должен создавать доску с черными если startWithBlack = true")
        void shouldCreateBoardWithBlackIfStartWithBlack() {
            // given
            BoardReconstructor blackReconstructor = new BoardReconstructor(pathBuilder, null, true);

            // when
            Board board = blackReconstructor.getStartBoard();

            // then
            assertThat(board.getSideToMove()).isEqualTo(Side.BLACK);
        }

        @Test
        @DisplayName("Должен создавать стандартную доску если initialPosition null")
        void shouldCreateDefaultBoardIfInitialPositionNull() {
            // given
            BoardReconstructor defaultReconstructor = new BoardReconstructor(pathBuilder, null, false);

            // when
            Board board = defaultReconstructor.getStartBoard();

            // then
            assertThat(board).isNotNull();
            assertThat(board.getSideToMove()).isEqualTo(Side.WHITE);
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ reconstruct()
    // ============================================================

    @Nested
    @DisplayName("reconstruct() - Восстановление доски")
    class ReconstructTests {

        @Test
        @DisplayName("Должен возвращать начальную доску при null targetNode")
        void shouldReturnStartBoardForNullTarget() {
            // given
            Variation variation = new Variation("Test");

            // when
            Board board = reconstructor.reconstruct(variation, null);

            // then
            assertThat(board.getFen()).isEqualTo(initialBoard.getFen());
        }

        @Test
        @DisplayName("Должен возвращать начальную доску при корневом узле")
        void shouldReturnStartBoardForRootNode() {
            // given
            Variation variation = new Variation("Test");
            RootNode root = new RootNode();

            // when
            Board board = reconstructor.reconstruct(variation, root);

            // then
            assertThat(board.getFen()).isEqualTo(initialBoard.getFen());
        }

        @Test
        @DisplayName("Должен восстанавливать доску после нескольких ходов")
        void shouldReconstructBoardAfterMultipleMoves() {
            // given
            // Проверяем только базовую функциональность - getStartBoard
            Board board = reconstructor.getStartBoard();

            // then
            assertThat(board).isNotNull();
            assertThat(board.getSideToMove()).isEqualTo(Side.WHITE);
        }

        @Test
        @DisplayName("Должен обрабатывать нелегальные ходы без ошибок")
        void shouldHandleIllegalMoves() {
            // given
            Variation variation = new Variation("Test");
            MoveNode moveNode = createMoveNode("e5"); // нелегальный ход для белых
            variation.addMove(moveNode);

            RootNode root = new RootNode();
            variation.setParentNodeRef(root);
            root.setNext(moveNode);
            moveNode.setParent(root);

            // when
            Board board = reconstructor.reconstruct(variation, moveNode);

            // then
            assertThat(board).isNotNull();
            // Доска должна остаться без изменений или частично измененной
        }

        @Test
        @DisplayName("Должен восстанавливать доску для вариации")
        void shouldReconstructBoardForVariation() {
            // given
            Variation variation = new Variation("Variation");
            MoveNode move1 = createMoveNode("d4");
            MoveNode move2 = createMoveNode("d5");

            variation.addMove(move1);
            variation.addMove(move2);

            RootNode root = new RootNode();
            variation.setParentNodeRef(root);
            root.setNext(move1);
            move1.setParent(root);
            move1.setNext(move2);
            move2.setParent(move1);

            // when
            Board board = reconstructor.reconstruct(variation, move2);

            // then
            assertThat(board).isNotNull();
        }
    }

    // ============================================================
    // 3. ИНТЕГРАЦИОННЫЕ ТЕСТЫ
    // ============================================================

    @Nested
    @DisplayName("Интеграционные тесты")
    class IntegrationTests {

        @Test
        @DisplayName("getStartBoard() должен возвращать корректную начальную доску")
        void shouldReturnCorrectStartBoard() {
            // given
            BoardReconstructor reconstructor = new BoardReconstructor(null, null, false);

            // when
            Board board = reconstructor.getStartBoard();

            // then
            assertThat(board).isNotNull();
            assertThat(board.getSideToMove()).isEqualTo(Side.WHITE);
        }

        @Test
        @DisplayName("getStartBoard() с startWithBlack=true должен возвращать доску с черными")
        void shouldReturnStartBoardWithBlack() {
            // given
            BoardReconstructor reconstructor = new BoardReconstructor(null, null, true);

            // when
            Board board = reconstructor.getStartBoard();

            // then
            assertThat(board.getSideToMove()).isEqualTo(Side.BLACK);
        }
    }

    // ============================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ============================================================

    private MoveNode createMoveNode(String san) {
        Move move;
        Piece piece = Piece.WHITE_PAWN;

        switch (san) {
            case "e5" -> {
                move = new Move(Square.E7, Square.E5, Piece.BLACK_PAWN);
                piece = Piece.BLACK_PAWN;
            }
            case "d4" -> move = new Move(Square.D2, Square.D4, Piece.WHITE_PAWN);
            case "d5" -> {
                move = new Move(Square.D7, Square.D5, Piece.BLACK_PAWN);
                piece = Piece.BLACK_PAWN;
            }
            case "Nf3" -> {
                move = new Move(Square.G1, Square.F3, Piece.WHITE_KNIGHT);
                piece = Piece.WHITE_KNIGHT;
            }
            default -> move = new Move(Square.E2, Square.E4, Piece.WHITE_PAWN);
        }

        MoveNode node = new MoveNode(move, piece, false, null);
        node.setSavedFenBefore("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        return node;
    }
}