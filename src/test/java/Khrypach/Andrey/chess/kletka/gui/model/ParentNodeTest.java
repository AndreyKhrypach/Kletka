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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ParentNode и MoveNode - Базовые узлы дерева вариантов")
class ParentNodeTest {

    // ============================================================
    // 1. ВСПОМОГАТЕЛЬНЫЙ КЛАСС ДЛЯ ТЕСТИРОВАНИЯ ParentNode
    // ============================================================

    /**
     * Конкретная реализация ParentNode для тестирования
     */
    private static class TestNode extends ParentNode {
        private final String san;
        private final String uci;

        TestNode(String san, String uci) {
            this.san = san;
            this.uci = uci;
        }

        @Override
        public boolean isRoot() {
            return false;
        }

        @Override
        public String getSan() {
            return san;
        }

        @Override
        public String getUciMove() {
            return uci;
        }

        @Override
        public String toString() {
            return "TestNode{" + san + "}";
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ ParentNode
    // ============================================================

    @Nested
    @DisplayName("ParentNode - Базовый узел")
    class ParentNodeBaseTests {

        private TestNode node;

        @BeforeEach
        void setUp() {
            node = new TestNode("e4", "e2e4");
        }

        @Test
        @DisplayName("Должен создавать узел с уникальным UUID")
        void shouldCreateNodeWithUniqueUuid() {
            // given
            TestNode node1 = new TestNode("e4", "e2e4");
            TestNode node2 = new TestNode("d4", "d2d4");

            // then
            assertThat(node1.getNodeUuid()).isNotNull();
            assertThat(node2.getNodeUuid()).isNotNull();
            assertThat(node1.getNodeUuid()).isNotEqualTo(node2.getNodeUuid());
        }

        @Test
        @DisplayName("Должен иметь absolutePly = -1 по умолчанию")
        void shouldHaveDefaultAbsolutePlyMinusOne() {
            // then
            assertThat(node.getAbsolutePly()).isEqualTo(-1);
        }

        @Test
        @DisplayName("Должен уметь устанавливать absolutePly")
        void shouldSetAbsolutePly() {
            // when
            node.setAbsolutePly(5);

            // then
            assertThat(node.getAbsolutePly()).isEqualTo(5);
        }

        @Test
        @DisplayName("Должен иметь пустой список subVariations по умолчанию")
        void shouldHaveEmptySubVariationsByDefault() {
            // then
            assertThat(node.getSubVariations()).isEmpty();
        }

        @Test
        @DisplayName("Должен уметь добавлять subVariations")
        void shouldAddSubVariations() {
            // given
            Variation variation = new Variation();

            // when
            node.getSubVariations().add(variation);

            // then
            assertThat(node.getSubVariations()).hasSize(1);
            assertThat(node.getSubVariations()).contains(variation);
        }

        @Test
        @DisplayName("Должен возвращать SAN через getSan()")
        void shouldReturnSan() {
            // then
            assertThat(node.getSan()).isEqualTo("e4");
        }

        @Test
        @DisplayName("Должен возвращать UCI через getUciMove()")
        void shouldReturnUci() {
            // then
            assertThat(node.getUciMove()).isEqualTo("e2e4");
        }

        @Test
        @DisplayName("Должен иметь isRoot() = false для TestNode")
        void shouldNotBeRoot() {
            // then
            assertThat(node.isRoot()).isFalse();
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ АННОТАЦИЙ
    // ============================================================

    @Nested
    @DisplayName("ParentNode - Аннотации")
    class AnnotationTests {

        private TestNode node;

        @BeforeEach
        void setUp() {
            node = new TestNode("e4", "e2e4");
        }

        @Test
        @DisplayName("Должен добавлять аннотацию при отсутствии других")
        void shouldAddAnnotationWhenNoneExist() {
            // when
            node.addAnnotation(MoveAnnotation.GOOD_MOVE);

            // then
            assertThat(node.getAnnotation()).isEqualTo(MoveAnnotation.GOOD_MOVE);
            assertThat(node.getAdditionalAnnotations()).isEmpty();
        }

        @Test
        @DisplayName("Должен добавлять дополнительную аннотацию при наличии основной")
        void shouldAddAdditionalAnnotationWhenMainExists() {
            // given
            node.addAnnotation(MoveAnnotation.GOOD_MOVE);

            // when
            node.addAnnotation(MoveAnnotation.WITH_INITIATIVE);

            // then
            assertThat(node.getAnnotation()).isEqualTo(MoveAnnotation.GOOD_MOVE);
            assertThat(node.getAdditionalAnnotations()).contains(MoveAnnotation.WITH_INITIATIVE);
        }

        @Test
        @DisplayName("Должен не добавлять дубликаты аннотаций")
        void shouldNotAddDuplicateAnnotations() {
            // given
            node.addAnnotation(MoveAnnotation.GOOD_MOVE);

            // when
            node.addAnnotation(MoveAnnotation.GOOD_MOVE);

            // then
            assertThat(node.getAnnotation()).isEqualTo(MoveAnnotation.GOOD_MOVE);
            assertThat(node.getAdditionalAnnotations()).isEmpty();
        }

        @Test
        @DisplayName("Должен игнорировать null при добавлении аннотации")
        void shouldIgnoreNullAnnotation() {
            // when
            node.addAnnotation(null);

            // then
            assertThat(node.getAnnotation()).isNull();
            assertThat(node.getAdditionalAnnotations()).isEmpty();
        }

        @Test
        @DisplayName("Должен корректно обрабатывать несколько дополнительных аннотаций")
        void shouldHandleMultipleAdditionalAnnotations() {
            // given
            node.addAnnotation(MoveAnnotation.GOOD_MOVE);
            node.addAnnotation(MoveAnnotation.WITH_INITIATIVE);
            node.addAnnotation(MoveAnnotation.WITH_IDEA);

            // then
            assertThat(node.getAnnotation()).isEqualTo(MoveAnnotation.GOOD_MOVE);
            assertThat(node.getAdditionalAnnotations())
                    .containsExactlyInAnyOrder(
                            MoveAnnotation.WITH_INITIATIVE,
                            MoveAnnotation.WITH_IDEA
                    );
        }
    }

    // ============================================================
    // 4. ТЕСТЫ ДЛЯ FEN
    // ============================================================

    @Nested
    @DisplayName("ParentNode - FEN для восстановления")
    class FenTests {

        private TestNode node;

        @BeforeEach
        void setUp() {
            node = new TestNode("e4", "e2e4");
        }

        @Test
        @DisplayName("Должен уметь сохранять FEN до хода")
        void shouldSaveFenBefore() {
            // given
            String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

            // when
            node.setSavedFenBefore(fen);

            // then
            assertThat(node.getSavedFenBefore()).isEqualTo(fen);
        }

        @Test
        @DisplayName("Должен уметь сохранять FEN после хода")
        void shouldSaveFenAfter() {
            // given
            String fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1";

            // when
            node.setSavedFenAfter(fen);

            // then
            assertThat(node.getSavedFenAfter()).isEqualTo(fen);
        }

        @Test
        @DisplayName("Должен иметь null FEN по умолчанию")
        void shouldHaveNullFenByDefault() {
            // then
            assertThat(node.getSavedFenBefore()).isNull();
            assertThat(node.getSavedFenAfter()).isNull();
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ КОММЕНТАРИЕВ
    // ============================================================

    @Nested
    @DisplayName("ParentNode - Комментарии")
    class CommentTests {

        private TestNode node;

        @BeforeEach
        void setUp() {
            node = new TestNode("e4", "e2e4");
        }

        @Test
        @DisplayName("Должен уметь устанавливать комментарий")
        void shouldSetComment() {
            // given
            String comment = "Отличный ход!";

            // when
            node.setComment(comment);

            // then
            assertThat(node.getComment()).isEqualTo(comment);
        }

        @Test
        @DisplayName("Должен иметь null комментарий по умолчанию")
        void shouldHaveNullCommentByDefault() {
            // then
            assertThat(node.getComment()).isNull();
        }

        @Test
        @DisplayName("Должен уметь устанавливать флаг NAG комментария")
        void shouldSetNagCommentFlag() {
            // when
            node.setHasNagComment(true);

            // then
            assertThat(node.isHasNagComment()).isTrue();
        }

        @Test
        @DisplayName("Должен иметь false NAG флаг по умолчанию")
        void shouldHaveFalseNagCommentByDefault() {
            // then
            assertThat(node.isHasNagComment()).isFalse();
        }
    }

    // ============================================================
    // 6. ТЕСТЫ ДЛЯ СВЯЗЕЙ
    // ============================================================

    @Nested
    @DisplayName("ParentNode - Связи между узлами")
    class NodeLinksTests {

        private TestNode node1;
        private TestNode node2;

        @BeforeEach
        void setUp() {
            node1 = new TestNode("e4", "e2e4");
            node2 = new TestNode("e5", "e7e5");
        }

        @Test
        @DisplayName("Должен уметь устанавливать parent")
        void shouldSetParent() {
            // when
            node2.setParent(node1);

            // then
            assertThat(node2.getParent()).isEqualTo(node1);
        }

        @Test
        @DisplayName("Должен уметь устанавливать next")
        void shouldSetNext() {
            // when
            node1.setNext(node2);

            // then
            assertThat(node1.getNext()).isEqualTo(node2);
        }

        @Test
        @DisplayName("Должен уметь устанавливать forkNode")
        void shouldSetForkNode() {
            // when
            node2.setForkNode(node1);

            // then
            assertThat(node2.getForkNode()).isEqualTo(node1);
        }

        @Test
        @DisplayName("Должен уметь устанавливать owningVariation")
        void shouldSetOwningVariation() {
            // given
            Variation variation = new Variation();

            // when
            node1.setOwningVariation(variation);

            // then
            assertThat(node1.getOwningVariation()).isEqualTo(variation);
        }

        @Test
        @DisplayName("Должен иметь null связи по умолчанию")
        void shouldHaveNullLinksByDefault() {
            // then
            assertThat(node1.getParent()).isNull();
            assertThat(node1.getNext()).isNull();
            assertThat(node1.getForkNode()).isNull();
            assertThat(node1.getOwningVariation()).isNull();
        }
    }

    // ============================================================
    // 7. ТЕСТЫ ДЛЯ РАБОТЫ С ВАРИАНТАМИ
    // ============================================================

    @Nested
    @DisplayName("ParentNode - Работа с вариантами")
    class VariationMethodsTests {

        private TestNode node;

        @BeforeEach
        void setUp() {
            node = new TestNode("e4", "e2e4");
        }

        @Test
        @DisplayName("getMainLineVariation() должен возвращать null если subVariations пуст")
        void shouldReturnNullForEmptySubVariations() {
            // then
            assertThat(node.getMainLineVariation()).isNull();
        }

        @Test
        @DisplayName("getMainLineVariation() должен находить вариант с isMainLine=true")
        void shouldFindMainLineVariation() {
            // given
            Variation mainLine = new Variation();
            mainLine.setMainLine(true);
            Variation subLine = new Variation();
            subLine.setMainLine(false);

            node.getSubVariations().add(mainLine);
            node.getSubVariations().add(subLine);

            // when
            Variation result = node.getMainLineVariation();

            // then
            assertThat(result).isEqualTo(mainLine);
        }

        @Test
        @DisplayName("getMainLineByName() должен возвращать null если subVariations пуст")
        void shouldReturnNullForEmptySubVariationsByName() {
            // then
            assertThat(node.getMainLineByName()).isNull();
        }

        @Test
        @DisplayName("getMainLineByName() должен находить вариант с именем '~'")
        void shouldFindVariationByNameTilde() {
            // given
            Variation mainLine = new Variation();
            mainLine.setName("~");
            Variation subLine = new Variation();
            subLine.setName("Other");

            node.getSubVariations().add(mainLine);
            node.getSubVariations().add(subLine);

            // when
            Variation result = node.getMainLineByName();

            // then
            assertThat(result).isEqualTo(mainLine);
        }
    }
}