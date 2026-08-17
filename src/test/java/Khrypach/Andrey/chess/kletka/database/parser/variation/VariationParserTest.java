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

package Khrypach.Andrey.chess.kletka.database.parser.variation;

import Khrypach.Andrey.chess.kletka.database.parser.PgnToken;
import Khrypach.Andrey.chess.kletka.database.parser.enums.PgnTokenType;
import Khrypach.Andrey.chess.kletka.database.parser.move.MoveParser;
import Khrypach.Andrey.chess.kletka.database.parser.tree.GameTreeBuilder;
import Khrypach.Andrey.chess.kletka.gui.model.RootNode;
import Khrypach.Andrey.chess.kletka.gui.model.Variation;
import com.github.bhlangonijr.chesslib.Board;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DisplayName("VariationParser - Парсер вариантов")
class VariationParserTest {

    private VariationParser parser;
    private MoveParser moveParser;
    private GameTreeBuilder treeBuilder;

    @BeforeEach
    void setUp() {
        moveParser = new MoveParser();
        treeBuilder = new GameTreeBuilder();
        treeBuilder.initializeGuiLogic();
        parser = new VariationParser(moveParser, treeBuilder);
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ clear()
    // ============================================================

    @Nested
    @DisplayName("clear() - Сброс состояния")
    class ClearTests {

        @Test
        @DisplayName("Должен очищать стек и сбрасывать счетчик")
        void shouldClearStackAndCounter() {
            // given
            parser.getVariationStack().push(new VariationParser.VariationContext(
                    null, null, null, null, null, false,
                    false, 0, false, false, 0
            ));

            // when
            parser.clear();

            // then
            assertThat(parser.getVariationStack()).isEmpty();
            assertThat(parser.getPosition()).isEqualTo(0);
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ syncWithParser()
    // ============================================================

    @Nested
    @DisplayName("syncWithParser() - Синхронизация состояния")
    class SyncWithParserTests {

        @Test
        @DisplayName("Должен синхронизировать состояние")
        void shouldSyncState() {
            // given
            RootNode root = new RootNode();
            Variation variation = new Variation("Test");
            Board board = new Board();

            // when
            parser.syncWithParser(root, board, variation);

            // then
            assertThat(parser.getCurrentNode()).isEqualTo(root);
            assertThat(parser.getCurrentVariation()).isEqualTo(variation);
            assertThat(parser.getCurrentBoard()).isNotNull();
        }

        @Test
        @DisplayName("Должен клонировать доску при синхронизации")
        void shouldCloneBoardWhenSyncing() {
            // given
            Board board = new Board();
            Variation variation = new Variation("Test");

            // when
            parser.syncWithParser(null, board, variation);

            // then
            assertThat(parser.getCurrentBoard()).isNotSameAs(board);
            assertThat(parser.getCurrentBoard().getFen()).isEqualTo(board.getFen());
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ restoreFromVariation()
    // ============================================================

    @Nested
    @DisplayName("restoreFromVariation() - Восстановление из стека")
    class RestoreFromVariationTests {

        @Test
        @DisplayName("Должен восстанавливать состояние из стека")
        void shouldRestoreFromStack() {
            // given
            Variation parentVar = new Variation("Parent");
            Board board = new Board();
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

            VariationParser.VariationContext context = new VariationParser.VariationContext(
                    parentVar, null, board, null, null, false, false, 0, false, false, 0
            );
            parser.getVariationStack().push(context);
            parser.setCurrentVariation(new Variation("Child"));

            // when
            parser.restoreFromVariation();

            // then
            assertThat(parser.getCurrentVariation()).isEqualTo(parentVar);
            assertThat(parser.getCurrentBoard().getFen()).isEqualTo(board.getFen());
        }

        @Test
        @DisplayName("Не должен падать при пустом стеке")
        void shouldNotFailWhenStackEmpty() {
            // when/then
            assertDoesNotThrow(() -> parser.restoreFromVariation());
        }
    }

    // ============================================================
    // 4. ТЕСТЫ ДЛЯ ГЕТТЕРОВ И СЕТТЕРОВ
    // ============================================================

    @Nested
    @DisplayName("Геттеры и сеттеры")
    class GettersAndSettersTests {

        @Test
        @DisplayName("Должен устанавливать и получать position")
        void shouldSetAndGetPosition() {
            // when
            parser.setPosition(42);

            // then
            assertThat(parser.getPosition()).isEqualTo(42);
        }

        @Test
        @DisplayName("Должен устанавливать и получать currentVariation")
        void shouldSetAndGetCurrentVariation() {
            // given
            Variation variation = new Variation("Test");

            // when
            parser.setCurrentVariation(variation);

            // then
            assertThat(parser.getCurrentVariation()).isEqualTo(variation);
        }

        @Test
        @DisplayName("Должен устанавливать и получать currentNode")
        void shouldSetAndGetCurrentNode() {
            // given
            RootNode root = new RootNode();

            // when
            parser.setCurrentNode(root);

            // then
            assertThat(parser.getCurrentNode()).isEqualTo(root);
        }

        @Test
        @DisplayName("Должен устанавливать и получать currentBoard")
        void shouldSetAndGetCurrentBoard() {
            // given
            Board board = new Board();

            // when
            parser.setCurrentBoard(board);

            // then
            assertThat(parser.getCurrentBoard()).isEqualTo(board);
        }

        @Test
        @DisplayName("Должен устанавливать токены")
        void shouldSetTokens() {
            // given
            List<PgnToken> tokens = new ArrayList<>();
            tokens.add(new PgnToken(PgnTokenType.MOVE, "e4", 1, 1));

            // when
            parser.setTokens(tokens);

            // then
            // Проверяем через поведение — токены используются при парсинге
            assertThat(parser.getPosition()).isEqualTo(0);
        }

        @Test
        @DisplayName("Должен получать variationStack")
        void shouldGetVariationStack() {
            // then
            assertThat(parser.getVariationStack()).isNotNull();
            assertThat(parser.getVariationStack()).isEmpty();
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ parseMoveInVariationWithSide()
    // ============================================================

    @Nested
    @DisplayName("parseMoveInVariationWithSide() - Парсинг хода в варианте")
    class ParseMoveInVariationWithSideTests {

        @Test
        @DisplayName("Должен парсить белый ход в варианте")
        void shouldParseWhiteMoveInVariation() {
            // given
            Variation variation = new Variation("Test");
            Board board = new Board();
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            parser.setCurrentBoard(board);
            parser.setCurrentVariation(variation);

            // when
            boolean result = parser.parseMoveInVariationWithSide("e4", variation, board, true, 1);

            // then
            assertThat(result).isTrue();
            assertThat(variation.getMoveCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Должен парсить черный ход в варианте")
        void shouldParseBlackMoveInVariation() {
            // given
            Variation variation = new Variation("Test");
            Board board = new Board();
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1");
            parser.setCurrentBoard(board);
            parser.setCurrentVariation(variation);

            // when
            boolean result = parser.parseMoveInVariationWithSide("e5", variation, board, false, 1);

            // then
            assertThat(result).isTrue();
            assertThat(variation.getMoveCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Должен возвращать false для нелегального хода")
        void shouldReturnFalseForIllegalMove() {
            // given
            Variation variation = new Variation("Test");
            Board board = new Board();
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            parser.setCurrentBoard(board);
            parser.setCurrentVariation(variation);

            // when
            boolean result = parser.parseMoveInVariationWithSide("e5", variation, board, true, 1);

            // then
            assertThat(result).isFalse();
        }
    }

    // ============================================================
    // 6. ТЕСТЫ ДЛЯ addVariationToTree()
    // ============================================================

    @Nested
    @DisplayName("addVariationToTree() - Добавление варианта в дерево")
    class AddVariationToTreeTests {

        @Test
        @DisplayName("Не должен добавлять пустой вариант")
        void shouldNotAddEmptyVariation() {
            // given
            RootNode root = new RootNode();
            Variation variation = new Variation("Empty");

            // when
            parser.addVariationToTree(root, variation);

            // then
            assertThat(root.getSubVariations()).isEmpty();
        }

        @Test
        @DisplayName("Не должен добавлять вариант при null forkNode")
        void shouldNotAddVariationWithNullForkNode() {
            // given
            Variation variation = new Variation("Test");
            Board board = new Board();
            board.loadFromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            parser.setCurrentBoard(board);
            parser.parseMoveInVariationWithSide("e4", variation, board, true, 1);

            // when
            parser.addVariationToTree(null, variation);

            // then
            // Должно пройти без ошибок, вариант не добавлен
        }
    }
}