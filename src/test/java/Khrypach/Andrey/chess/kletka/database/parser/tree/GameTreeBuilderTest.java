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

package Khrypach.Andrey.chess.kletka.database.parser.tree;

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
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

@DisplayName("GameTreeBuilder - Построитель дерева игры")
class GameTreeBuilderTest {

    private GameTreeBuilder builder;
    private final LanguageManager lang = LanguageManager.getInstance();

    @BeforeEach
    void setUp() {
        builder = new GameTreeBuilder();
        builder.initializeGuiLogic();
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ initializeGuiLogic()
    // ============================================================

    @Nested
    @DisplayName("initializeGuiLogic() - Инициализация GUI логики")
    class InitializeGuiLogicTests {

        @Test
        @DisplayName("Должен инициализировать все компоненты")
        void shouldInitializeAllComponents() {
            // given
            GameTreeBuilder newBuilder = new GameTreeBuilder();

            // when
            newBuilder.initializeGuiLogic();

            // then
            assertThat(newBuilder.getRootVariation()).isNotNull();
            assertThat(newBuilder.getMainLine()).isNotNull();
            assertThat(newBuilder.getCurrentVariation()).isNotNull();
            assertThat(newBuilder.getNamingService()).isNotNull();
            assertThat(newBuilder.getPathBuilder()).isNotNull();
            assertThat(newBuilder.getBoardReconstructor()).isNotNull();
            assertThat(newBuilder.getVariationManager()).isNotNull();
            assertThat(newBuilder.getCurrentBoard()).isNotNull();
            assertThat(newBuilder.getInitialBoard()).isNotNull();
        }

        @Test
        @DisplayName("Должен создавать корневой узел")
        void shouldCreateRootNode() {
            // given
            GameTreeBuilder newBuilder = new GameTreeBuilder();

            // when
            newBuilder.initializeGuiLogic();

            // then
            RootNode root = (RootNode) newBuilder.getRootVariation().getFirstNode();
            assertThat(root).isNotNull();
            assertThat(root.isRoot()).isTrue();
        }

        @Test
        @DisplayName("Должен создавать главную линию")
        void shouldCreateMainLine() {
            // given
            GameTreeBuilder newBuilder = new GameTreeBuilder();

            // when
            newBuilder.initializeGuiLogic();

            // then
            Variation mainLine = newBuilder.getMainLine();
            assertThat(mainLine).isNotNull();
            assertThat(mainLine.isMainLine()).isTrue();
            assertThat(mainLine.getName()).isEqualTo(lang.get(LanguageKeys.MAIN_LINE));
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ createRootNode()
    // ============================================================

    @Nested
    @DisplayName("createRootNode() - Создание корневого узла")
    class CreateRootNodeTests {

        @Test
        @DisplayName("Должен создавать корневой узел")
        void shouldCreateRootNode() {
            // when
            RootNode root = builder.createRootNode();

            // then
            assertThat(root).isNotNull();
            assertThat(root.isRoot()).isTrue();
            assertThat(root.getSan()).isEmpty();
            assertThat(root.getUciMove()).isEmpty();
            assertThat(root.getAbsolutePly()).isEqualTo(-1);
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ createMainLine()
    // ============================================================

    @Nested
    @DisplayName("createMainLine() - Создание главной линии")
    class CreateMainLineTests {

        @Test
        @DisplayName("Должен создавать главную линию")
        void shouldCreateMainLine() {
            // when
            Variation mainLine = builder.createMainLine();

            // then
            assertThat(mainLine).isNotNull();
            assertThat(mainLine.isMainLine()).isTrue();
            assertThat(mainLine.getName()).isEqualTo(lang.get(LanguageKeys.MAIN_LINE));
            assertThat(mainLine.isEmpty()).isTrue();
        }
    }

    // ============================================================
    // 4. ТЕСТЫ ДЛЯ addMoveToTree()
    // ============================================================

    @Nested
    @DisplayName("addMoveToTree() - Добавление хода в дерево")
    class AddMoveToTreeTests {

        @Test
        @DisplayName("Должен добавлять первый ход в главную линию")
        void shouldAddFirstMoveToMainLine() {
            // given
            MoveNode moveNode = createMoveNode("e4");

            // when
            builder.addMoveToTree(moveNode);

            // then
            Variation mainLine = builder.getMainLine();
            assertThat(mainLine.getMoveCount()).isEqualTo(1);
            assertThat(mainLine.getFirstNode()).isEqualTo(moveNode);
            assertThat(builder.getCurrentNode()).isEqualTo(moveNode);
            assertThat(builder.getMoveCounter()).isEqualTo(1);
        }

        @Test
        @DisplayName("Должен добавлять второй ход в главную линию")
        void shouldAddSecondMoveToMainLine() {
            // given
            MoveNode move1 = createMoveNode("e4");
            MoveNode move2 = createMoveNode("e5");

            // when
            builder.addMoveToTree(move1);
            builder.addMoveToTree(move2);

            // then
            Variation mainLine = builder.getMainLine();
            assertThat(mainLine.getMoveCount()).isEqualTo(2);
            assertThat(mainLine.getMoves()).containsExactly(move1, move2);
            assertThat(builder.getCurrentNode()).isEqualTo(move2);
            assertThat(builder.getMoveCounter()).isEqualTo(2);
        }

        @Test
        @DisplayName("Должен добавлять ход в вариант")
        void shouldAddMoveToVariation() {
            // given
            Variation variation = new Variation("Test");
            builder.setCurrentVariation(variation);
            builder.setCurrentNode(null);

            MoveNode moveNode = createMoveNode("d4");

            // when
            builder.addMoveToTree(moveNode);

            // then
            assertThat(variation.getMoveCount()).isEqualTo(1);
            assertThat(variation.getFirstNode()).isEqualTo(moveNode);
            assertThat(builder.getCurrentNode()).isEqualTo(moveNode);
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ setAbsolutePlyForNode()
    // ============================================================

    @Nested
    @DisplayName("setAbsolutePlyForNode() - Установка absolutePly")
    class SetAbsolutePlyForNodeTests {

        @Test
        @DisplayName("Должен устанавливать absolutePly для белого хода")
        void shouldSetAbsolutePlyForWhiteMove() {
            // given
            Board board = new Board();
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            builder.setCurrentBoard(board);

            MoveNode moveNode = createMoveNode("e4");

            // when
            builder.setAbsolutePlyForNode(moveNode);

            // then
            assertThat(moveNode.getAbsolutePly()).isEqualTo(1);
        }

        @Test
        @DisplayName("Должен устанавливать absolutePly для черного хода")
        void shouldSetAbsolutePlyForBlackMove() {
            // given
            Board board = new Board();
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1");
            builder.setCurrentBoard(board);

            MoveNode moveNode = createMoveNode("e5");

            // when
            builder.setAbsolutePlyForNode(moveNode);

            // then
            assertThat(moveNode.getAbsolutePly()).isEqualTo(2);
        }

        @Test
        @DisplayName("Не должен падать при null узле")
        void shouldNotFailForNullNode() {
            // when/then
            builder.setAbsolutePlyForNode(null);
            // Никаких исключений
        }

        @Test
        @DisplayName("Не должен падать при null доске")
        void shouldNotFailForNullBoard() {
            // given
            builder.setCurrentBoard(null);
            MoveNode moveNode = createMoveNode("e4");

            // when/then
            builder.setAbsolutePlyForNode(moveNode);
            // Никаких исключений
        }
    }

    // ============================================================
    // 6. ТЕСТЫ ДЛЯ reset()
    // ============================================================

    @Nested
    @DisplayName("reset() - Сброс состояния")
    class ResetTests {

        @Test
        @DisplayName("Должен сбрасывать все состояние")
        void shouldResetAllState() {
            // given
            builder.addMoveToTree(createMoveNode("e4"));
            assertThat(builder.getMoveCounter()).isEqualTo(1);

            // when
            builder.reset();

            // then
            assertThat(builder.getRootVariation()).isNull();
            assertThat(builder.getMainLine()).isNull();
            assertThat(builder.getCurrentVariation()).isNull();
            assertThat(builder.getCurrentNode()).isNull();
            assertThat(builder.getCurrentBoard()).isNull();
            assertThat(builder.getInitialBoard()).isNull();
            assertThat(builder.getMoveCounter()).isEqualTo(0);
        }
    }

    // ============================================================
    // 7. ТЕСТЫ ДЛЯ ГЕТТЕРОВ И СЕТТЕРОВ
    // ============================================================

    @Nested
    @DisplayName("Геттеры и сеттеры")
    class GettersAndSettersTests {

        @Test
        @DisplayName("Должен получать rootVariation")
        void shouldGetRootVariation() {
            assertThat(builder.getRootVariation()).isNotNull();
        }

        @Test
        @DisplayName("Должен получать mainLine")
        void shouldGetMainLine() {
            assertThat(builder.getMainLine()).isNotNull();
        }

        @Test
        @DisplayName("Должен устанавливать и получать currentVariation")
        void shouldSetAndGetCurrentVariation() {
            // given
            Variation variation = new Variation("Test");

            // when
            builder.setCurrentVariation(variation);

            // then
            assertThat(builder.getCurrentVariation()).isEqualTo(variation);
        }

        @Test
        @DisplayName("Должен устанавливать и получать currentNode")
        void shouldSetAndGetCurrentNode() {
            // given
            RootNode root = new RootNode();

            // when
            builder.setCurrentNode(root);

            // then
            assertThat(builder.getCurrentNode()).isEqualTo(root);
        }

        @Test
        @DisplayName("Должен устанавливать и получать currentBoard")
        void shouldSetAndGetCurrentBoard() {
            // given
            Board board = new Board();

            // when
            builder.setCurrentBoard(board);

            // then
            assertThat(builder.getCurrentBoard()).isEqualTo(board);
        }

        @Test
        @DisplayName("Должен устанавливать и получать initialBoard")
        void shouldSetAndGetInitialBoard() {
            // given
            Board board = new Board();

            // when
            builder.setInitialBoard(board);

            // then
            assertThat(builder.getInitialBoard()).isEqualTo(board);
        }

        @Test
        @DisplayName("Должен получать moveCounter")
        void shouldGetMoveCounter() {
            assertThat(builder.getMoveCounter()).isEqualTo(0);
        }

        @Test
        @DisplayName("Должен получать namingService")
        void shouldGetNamingService() {
            assertThat(builder.getNamingService()).isNotNull();
        }

        @Test
        @DisplayName("Должен получать pathBuilder")
        void shouldGetPathBuilder() {
            assertThat(builder.getPathBuilder()).isNotNull();
        }

        @Test
        @DisplayName("Должен получать boardReconstructor")
        void shouldGetBoardReconstructor() {
            assertThat(builder.getBoardReconstructor()).isNotNull();
        }

        @Test
        @DisplayName("Должен получать variationManager")
        void shouldGetVariationManager() {
            assertThat(builder.getVariationManager()).isNotNull();
        }
    }

    // ============================================================
    // 8. ТЕСТЫ ДЛЯ ИНТЕГРАЦИИ
    // ============================================================

    @Nested
    @DisplayName("Интеграционные тесты")
    class IntegrationTests {

        @Test
        @DisplayName("Должен строить простое дерево с несколькими ходами")
        void shouldBuildSimpleTree() {
            // given
            MoveNode move1 = createMoveNode("e4");
            MoveNode move2 = createMoveNode("e5");
            MoveNode move3 = createMoveNode("Nf3");

            // when
            builder.addMoveToTree(move1);
            builder.addMoveToTree(move2);
            builder.addMoveToTree(move3);

            // then
            Variation mainLine = builder.getMainLine();
            assertThat(mainLine.getMoveCount()).isEqualTo(3);
            assertThat(mainLine.getMoves()).containsExactly(move1, move2, move3);
            assertThat(builder.getMoveCounter()).isEqualTo(3);
        }

        @Test
        @DisplayName("Должен отслеживать текущий узел при добавлении ходов")
        void shouldTrackCurrentNode() {
            // given
            MoveNode move1 = createMoveNode("e4");
            MoveNode move2 = createMoveNode("e5");

            // when
            builder.addMoveToTree(move1);
            builder.addMoveToTree(move2);

            // then
            assertThat(builder.getCurrentNode()).isEqualTo(move2);
            assertThat(builder.getCurrentNode().getParent()).isEqualTo(move1);
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
            case "d4" -> move = new Move(Square.D2, Square.D4, Piece.WHITE_PAWN);
            default -> move = new Move(Square.E2, Square.E4, Piece.WHITE_PAWN);
        }

        MoveNode node = new MoveNode(move, piece, false, null);
        node.setSavedFenBefore("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        return node;
    }
}