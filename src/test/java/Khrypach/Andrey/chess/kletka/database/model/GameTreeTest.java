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

package Khrypach.Andrey.chess.kletka.database.model;

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.model.MoveNode;
import Khrypach.Andrey.chess.kletka.gui.model.RootNode;
import Khrypach.Andrey.chess.kletka.gui.model.Variation;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GameTree - Дерево вариантов партии")
class GameTreeTest {

    private GameTree gameTree;

    @BeforeEach
    void setUp() {
        gameTree = new GameTree();
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ КОНСТРУКТОРОВ
    // ============================================================

    @Nested
    @DisplayName("Конструкторы")
    class ConstructorTests {

        @Test
        @DisplayName("Конструктор по умолчанию должен создавать пустое дерево")
        void defaultConstructorShouldCreateEmptyTree() {
            // given
            GameTree tree = new GameTree();

            // then
            assertThat(tree).isNotNull();
            assertThat(tree.getRootNode()).isNotNull();
            assertThat(tree.getRootVariation()).isNotNull();
            assertThat(tree.getMainLine()).isNotNull();
            assertThat(tree.isEmpty()).isTrue();
            assertThat(tree.getInitialFen()).isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            assertThat(tree.isStartWithBlack()).isFalse();
            assertThat(tree.getResult()).isEqualTo("*");
            assertThat(tree.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("Конструктор с параметрами должен создавать дерево с указанными значениями")
        void parameterConstructorShouldCreateTreeWithValues() {
            // given
            RootNode root = new RootNode();
            Variation mainLine = new Variation("Main");
            Variation rootVariation = new Variation("Root");

            // when
            GameTree tree = new GameTree(root, mainLine, rootVariation);

            // then
            assertThat(tree.getRootNode()).isEqualTo(root);
            assertThat(tree.getMainLine()).isEqualTo(mainLine);
            assertThat(tree.getRootVariation()).isEqualTo(rootVariation);
            assertThat(tree.isEmpty()).isTrue();
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ isEmpty()
    // ============================================================

    @Nested
    @DisplayName("isEmpty() - Проверка пустоты")
    class IsEmptyTests {

        @Test
        @DisplayName("Должен возвращать true для пустого дерева")
        void shouldReturnTrueForEmptyTree() {
            // then
            assertThat(gameTree.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Должен возвращать false для непустого дерева")
        void shouldReturnFalseForNonEmptyTree() {
            // given
            MoveNode moveNode = createMoveNode("e4");
            gameTree.getMainLine().addMove(moveNode);

            // then
            assertThat(gameTree.isEmpty()).isFalse();
        }

        @Test
        @DisplayName("Должен возвращать true если mainLine null")
        void shouldReturnTrueIfMainLineNull() {
            // given
            gameTree.setMainLine(null);

            // then
            assertThat(gameTree.isEmpty()).isTrue();
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ setInitialBoard() И getInitialBoard()
    // ============================================================

    @Nested
    @DisplayName("setInitialBoard() и getInitialBoard()")
    class InitialBoardTests {

        @Test
        @DisplayName("Должен устанавливать и получать начальную доску")
        void shouldSetAndGetInitialBoard() {
            // given
            Board board = new Board();
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1");

            // when
            gameTree.setInitialBoard(board);

            // then
            Board result = gameTree.getInitialBoard();
            assertThat(result).isNotNull();
            assertThat(result.getFen()).isEqualTo(board.getFen());
        }

        @Test
        @DisplayName("Должен возвращать копию доски, а не оригинал")
        void shouldReturnCopyOfBoard() {
            // given
            Board board = new Board();
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            gameTree.setInitialBoard(board);

            // when
            Board result = gameTree.getInitialBoard();

            // then
            assertThat(result).isNotSameAs(board);
            assertThat(result.getFen()).isEqualTo(board.getFen());
        }

        @Test
        @DisplayName("Должен возвращать новую доску при null")
        void shouldReturnNewBoardWhenNull() {
            // given
            gameTree.setInitialBoard(null);

            // when
            Board result = gameTree.getInitialBoard();

            // then
            assertThat(result).isNotNull();
            assertThat(result.getFen()).isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        }
    }

    // ============================================================
    // 4. ТЕСТЫ ДЛЯ ГЕТТЕРОВ И СЕТТЕРОВ
    // ============================================================

    @Nested
    @DisplayName("Геттеры и сеттеры")
    class GettersAndSettersTests {

        @Test
        @DisplayName("Должен устанавливать и получать gameId")
        void shouldSetAndGetGameId() {
            // given
            String gameId = "test-123";

            // when
            gameTree.setGameId(gameId);

            // then
            assertThat(gameTree.getGameId()).isEqualTo(gameId);
        }

        @Test
        @DisplayName("Должен устанавливать и получать initialFen")
        void shouldSetAndGetInitialFen() {
            // given
            String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

            // when
            gameTree.setInitialFen(fen);

            // then
            assertThat(gameTree.getInitialFen()).isEqualTo(fen);
        }

        @Test
        @DisplayName("Должен устанавливать и получать startWithBlack")
        void shouldSetAndGetStartWithBlack() {
            // when
            gameTree.setStartWithBlack(true);

            // then
            assertThat(gameTree.isStartWithBlack()).isTrue();
        }

        @Test
        @DisplayName("Должен устанавливать и получать result")
        void shouldSetAndGetResult() {
            // given
            String result = "1-0";

            // when
            gameTree.setResult(result);

            // then
            assertThat(gameTree.getResult()).isEqualTo(result);
        }

        @Test
        @DisplayName("Должен устанавливать и получать deleted")
        void shouldSetAndGetDeleted() {
            // when
            gameTree.setDeleted(true);

            // then
            assertThat(gameTree.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("Должен устанавливать и получать rootNode")
        void shouldSetAndGetRootNode() {
            // given
            RootNode root = new RootNode();

            // when
            gameTree.setRootNode(root);

            // then
            assertThat(gameTree.getRootNode()).isEqualTo(root);
        }

        @Test
        @DisplayName("Должен устанавливать и получать mainLine")
        void shouldSetAndGetMainLine() {
            // given
            Variation mainLine = new Variation("New Main");

            // when
            gameTree.setMainLine(mainLine);

            // then
            assertThat(gameTree.getMainLine()).isEqualTo(mainLine);
        }

        @Test
        @DisplayName("Должен устанавливать и получать rootVariation")
        void shouldSetAndGetRootVariation() {
            // given
            Variation rootVariation = new Variation("New Root");

            // when
            gameTree.setRootVariation(rootVariation);

            // then
            assertThat(gameTree.getRootVariation()).isEqualTo(rootVariation);
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ ЯЗЫКОВЫХ КЛЮЧЕЙ
    // ============================================================

    @Nested
    @DisplayName("Языковые ключи")
    class LanguageKeysTests {

        @Test
        @DisplayName("Должен иметь ROOT имя из LanguageKeys")
        void shouldHaveRootNameFromLanguageKeys() {
            // given
            GameTree tree = new GameTree();

            // then
            assertThat(tree.getRootVariation().getName())
                    .isEqualTo(tree.getLanguageManager().get(LanguageKeys.ROOT));
        }

        @Test
        @DisplayName("Должен иметь MAIN_LINE имя из LanguageKeys")
        void shouldHaveMainLineNameFromLanguageKeys() {
            // given
            GameTree tree = new GameTree();

            // then
            assertThat(tree.getMainLine().getName())
                    .isEqualTo(tree.getLanguageManager().get(LanguageKeys.MAIN_LINE));
        }
    }

    // ============================================================
    // 6. ИНТЕГРАЦИОННЫЕ ТЕСТЫ
    // ============================================================

    @Nested
    @DisplayName("Интеграционные тесты")
    class IntegrationTests {

        @Test
        @DisplayName("Должен создавать дерево и добавлять ходы")
        void shouldCreateTreeAndAddMoves() {
            // given
            GameTree tree = new GameTree();
            Variation mainLine = tree.getMainLine();

            // when
            MoveNode move1 = createMoveNode("e4");
            MoveNode move2 = createMoveNode("e5");

            mainLine.addMove(move1);
            mainLine.addMove(move2);

            // then
            assertThat(tree.isEmpty()).isFalse();
            assertThat(mainLine.getMoveCount()).isEqualTo(2);
            assertThat(mainLine.getMoves()).containsExactly(move1, move2);
        }

        @Test
        @DisplayName("Должен создавать дерево с начальной позицией")
        void shouldCreateTreeWithInitialPosition() {
            // given
            Board board = new Board();
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1");

            // when
            gameTree.setInitialBoard(board);

            // then
            Board result = gameTree.getInitialBoard();
            assertThat(result.getFen()).isEqualTo(board.getFen());
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
            case "Nf3" -> {
                move = new Move(Square.G1, Square.F3, Piece.WHITE_KNIGHT);
                piece = Piece.WHITE_KNIGHT;
            }
            case "Nc6" -> {
                move = new Move(Square.B8, Square.C6, Piece.BLACK_KNIGHT);
                piece = Piece.BLACK_KNIGHT;
            }
            default -> move = new Move(Square.E2, Square.E4, Piece.WHITE_PAWN);
        }

        MoveNode node = new MoveNode(move, piece, false, null);
        node.setSavedFenBefore("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        return node;
    }
}