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

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RootNode - Корневой узел дерева вариантов")
class RootNodeTest {

    private RootNode rootNode;
    LanguageManager lang = LanguageManager.getInstance();
    @BeforeEach
    void setUp() {
        rootNode = new RootNode();
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ СОЗДАНИЯ ROOT NODE
    // ============================================================

    @Nested
    @DisplayName("Создание RootNode")
    class CreationTests {

        @Test
        @DisplayName("Должен создавать RootNode с корректными параметрами")
        void shouldCreateRootNodeWithCorrectParams() {
            // then
            assertThat(rootNode).isNotNull();
            assertThat(rootNode.getNodeUuid()).isNotNull();
            assertThat(rootNode.getAbsolutePly()).isEqualTo(-1);
        }

        @Test
        @DisplayName("Должен создавать RootNode с уникальным UUID")
        void shouldCreateRootNodeWithUniqueUuid() {
            // given
            RootNode anotherRoot = new RootNode();

            // then
            assertThat(rootNode.getNodeUuid()).isNotEqualTo(anotherRoot.getNodeUuid());
        }

        @Test
        @DisplayName("Должен иметь absolutePly = -1")
        void shouldHaveAbsolutePlyMinusOne() {
            // then
            assertThat(rootNode.getAbsolutePly()).isEqualTo(-1);
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ isRoot()
    // ============================================================

    @Nested
    @DisplayName("isRoot() - Проверка корневого узла")
    class IsRootTests {

        @Test
        @DisplayName("Должен возвращать true для RootNode")
        void shouldReturnTrueForRootNode() {
            // when
            boolean result = rootNode.isRoot();

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Должен всегда возвращать true для любого RootNode")
        void shouldAlwaysReturnTrue() {
            // given
            RootNode anotherRoot = new RootNode();

            // then
            assertThat(rootNode.isRoot()).isTrue();
            assertThat(anotherRoot.isRoot()).isTrue();
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ getSan()
    // ============================================================

    @Nested
    @DisplayName("getSan() - Получение SAN")
    class SanTests {

        @Test
        @DisplayName("Должен возвращать пустую строку для RootNode")
        void shouldReturnEmptyString() {
            // when
            String san = rootNode.getSan();

            // then
            assertThat(san).isEmpty();
        }

        @Test
        @DisplayName("Должен всегда возвращать пустую строку")
        void shouldAlwaysReturnEmptyString() {
            // given
            RootNode anotherRoot = new RootNode();

            // then
            assertThat(rootNode.getSan()).isEmpty();
            assertThat(anotherRoot.getSan()).isEmpty();
        }
    }

    // ============================================================
    // 4. ТЕСТЫ ДЛЯ getUciMove()
    // ============================================================

    @Nested
    @DisplayName("getUciMove() - Получение UCI")
    class UciMoveTests {

        @Test
        @DisplayName("Должен возвращать пустую строку для RootNode")
        void shouldReturnEmptyString() {
            // when
            String uci = rootNode.getUciMove();

            // then
            assertThat(uci).isEmpty();
        }

        @Test
        @DisplayName("Должен всегда возвращать пустую строку")
        void shouldAlwaysReturnEmptyString() {
            // given
            RootNode anotherRoot = new RootNode();

            // then
            assertThat(rootNode.getUciMove()).isEmpty();
            assertThat(anotherRoot.getUciMove()).isEmpty();
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ toString()
    // ============================================================

    @Nested
    @DisplayName("toString() - Строковое представление")
    class ToStringTests {

        @Test
        @DisplayName("Должен возвращать строковое представление с UUID")
        void shouldReturnToStringWithUuid() {
            // when
            String toString = rootNode.toString();

            // then
            assertThat(toString).startsWith("RootNode [uuid=");
            assertThat(toString).contains(rootNode.getNodeUuid().substring(0, 8));
            assertThat(toString).endsWith("]");
        }

        @Test
        @DisplayName("Должен содержать короткий UUID (8 символов)")
        void shouldContainShortUuid() {
            // when
            String toString = rootNode.toString();
            String uuid = rootNode.getNodeUuid().substring(0, 8);

            // then
            assertThat(toString).contains(uuid);
            assertThat(uuid).hasSize(8);
        }

        @Test
        @DisplayName("Должен иметь формат RootNode [uuid=...]")
        void shouldHaveCorrectFormat() {
            // when
            String toString = rootNode.toString();

            // then
            assertThat(toString).matches("RootNode \\[uuid=[a-f0-9]{8}]");
        }
    }

    // ============================================================
    // 6. ТЕСТЫ ДЛЯ СЕТТЕРОВ (унаследованных от ParentNode)
    // ============================================================

    @Nested
    @DisplayName("Сеттеры - Установка полей (унаследованные от ParentNode)")
    class SettersTests {

        @Test
        @DisplayName("Должен устанавливать absolutePly")
        void shouldSetAbsolutePly() {
            // when
            rootNode.setAbsolutePly(10);

            // then
            assertThat(rootNode.getAbsolutePly()).isEqualTo(10);
        }

        @Test
        @DisplayName("Должен устанавливать savedFenBefore")
        void shouldSetSavedFenBefore() {
            // given
            String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

            // when
            rootNode.setSavedFenBefore(fen);

            // then
            assertThat(rootNode.getSavedFenBefore()).isEqualTo(fen);
        }

        @Test
        @DisplayName("Должен устанавливать savedFenAfter")
        void shouldSetSavedFenAfter() {
            // given
            String fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1";

            // when
            rootNode.setSavedFenAfter(fen);

            // then
            assertThat(rootNode.getSavedFenAfter()).isEqualTo(fen);
        }

        @Test
        @DisplayName("Должен устанавливать parent")
        void shouldSetParent() {
            // given
            RootNode parent = new RootNode();

            // when
            rootNode.setParent(parent);

            // then
            assertThat(rootNode.getParent()).isEqualTo(parent);
        }

        @Test
        @DisplayName("Должен устанавливать next")
        void shouldSetNext() {
            // given
            RootNode next = new RootNode();

            // when
            rootNode.setNext(next);

            // then
            assertThat(rootNode.getNext()).isEqualTo(next);
        }

        @Test
        @DisplayName("Должен устанавливать forkNode")
        void shouldSetForkNode() {
            // given
            RootNode fork = new RootNode();

            // when
            rootNode.setForkNode(fork);

            // then
            assertThat(rootNode.getForkNode()).isEqualTo(fork);
        }

        @Test
        @DisplayName("Должен устанавливать owningVariation")
        void shouldSetOwningVariation() {
            // given
            Variation variation = new Variation();

            // when
            rootNode.setOwningVariation(variation);

            // then
            assertThat(rootNode.getOwningVariation()).isEqualTo(variation);
        }

        @Test
        @DisplayName("Должен устанавливать comment")
        void shouldSetComment() {
            // given
            String comment = "Начало партии";

            // when
            rootNode.setComment(comment);

            // then
            assertThat(rootNode.getComment()).isEqualTo(comment);
        }

        @Test
        @DisplayName("Должен устанавливать annotation")
        void shouldSetAnnotation() {
            // when
            rootNode.setAnnotation(MoveAnnotation.GOOD_MOVE);

            // then
            assertThat(rootNode.getAnnotation()).isEqualTo(MoveAnnotation.GOOD_MOVE);
        }

        @Test
        @DisplayName("Должен устанавливать hasNagComment")
        void shouldSetHasNagComment() {
            // when
            rootNode.setHasNagComment(true);

            // then
            assertThat(rootNode.isHasNagComment()).isTrue();
        }

        @Test
        @DisplayName("Должен устанавливать subVariations")
        void shouldSetSubVariations() {
            // given
            Variation variation = new Variation();
            java.util.List<Variation> variations = java.util.List.of(variation);

            // when
            rootNode.setSubVariations(variations);

            // then
            assertThat(rootNode.getSubVariations()).containsExactly(variation);
        }

        @Test
        @DisplayName("Должен устанавливать nodeUuid")
        void shouldSetNodeUuid() {
            // given
            String newUuid = "12345678-1234-1234-1234-123456789012";

            // when
            rootNode.setNodeUuid(newUuid);

            // then
            assertThat(rootNode.getNodeUuid()).isEqualTo(newUuid);
        }
    }

    // ============================================================
    // 7. ТЕСТЫ ДЛЯ addAnnotation() (унаследованный от ParentNode)
    // ============================================================

    @Nested
    @DisplayName("addAnnotation() - Добавление аннотаций (унаследованный от ParentNode)")
    class AddAnnotationTests {

        @Test
        @DisplayName("Должен добавлять аннотацию")
        void shouldAddAnnotation() {
            // when
            rootNode.addAnnotation(MoveAnnotation.GOOD_MOVE);

            // then
            assertThat(rootNode.getAnnotation()).isEqualTo(MoveAnnotation.GOOD_MOVE);
        }

        @Test
        @DisplayName("Должен добавлять несколько аннотаций")
        void shouldAddMultipleAnnotations() {
            // when
            rootNode.addAnnotation(MoveAnnotation.GOOD_MOVE);
            rootNode.addAnnotation(MoveAnnotation.WITH_INITIATIVE);

            // then
            assertThat(rootNode.getAnnotation()).isEqualTo(MoveAnnotation.GOOD_MOVE);
            assertThat(rootNode.getAdditionalAnnotations()).contains(MoveAnnotation.WITH_INITIATIVE);
        }

        @Test
        @DisplayName("Должен игнорировать null")
        void shouldIgnoreNull() {
            // when
            rootNode.addAnnotation(null);

            // then
            assertThat(rootNode.getAnnotation()).isNull();
            assertThat(rootNode.getAdditionalAnnotations()).isEmpty();
        }

        @Test
        @DisplayName("Должен не добавлять дубликаты")
        void shouldNotAddDuplicates() {
            // when
            rootNode.addAnnotation(MoveAnnotation.GOOD_MOVE);
            rootNode.addAnnotation(MoveAnnotation.GOOD_MOVE);

            // then
            assertThat(rootNode.getAnnotation()).isEqualTo(MoveAnnotation.GOOD_MOVE);
            assertThat(rootNode.getAdditionalAnnotations()).isEmpty();
        }
    }

    // ============================================================
    // 8. ТЕСТЫ ДЛЯ ГЕТТЕРОВ (унаследованных от ParentNode)
    // ============================================================

    @Nested
    @DisplayName("Геттеры - Получение полей (унаследованные от ParentNode)")
    class GettersTests {

        @Test
        @DisplayName("Должен возвращать nodeUuid")
        void shouldReturnNodeUuid() {
            // then
            assertThat(rootNode.getNodeUuid()).isNotNull();
            assertThat(rootNode.getNodeUuid()).hasSize(36); // UUID формат
        }

        @Test
        @DisplayName("Должен возвращать absolutePly = -1 по умолчанию")
        void shouldReturnAbsolutePlyMinusOne() {
            // then
            assertThat(rootNode.getAbsolutePly()).isEqualTo(-1);
        }

        @Test
        @DisplayName("Должен возвращать null savedFenBefore по умолчанию")
        void shouldReturnNullSavedFenBefore() {
            // then
            assertThat(rootNode.getSavedFenBefore()).isNull();
        }

        @Test
        @DisplayName("Должен возвращать null savedFenAfter по умолчанию")
        void shouldReturnNullSavedFenAfter() {
            // then
            assertThat(rootNode.getSavedFenAfter()).isNull();
        }

        @Test
        @DisplayName("Должен возвращать null parent по умолчанию")
        void shouldReturnNullParent() {
            // then
            assertThat(rootNode.getParent()).isNull();
        }

        @Test
        @DisplayName("Должен возвращать null next по умолчанию")
        void shouldReturnNullNext() {
            // then
            assertThat(rootNode.getNext()).isNull();
        }

        @Test
        @DisplayName("Должен возвращать null forkNode по умолчанию")
        void shouldReturnNullForkNode() {
            // then
            assertThat(rootNode.getForkNode()).isNull();
        }

        @Test
        @DisplayName("Должен возвращать null owningVariation по умолчанию")
        void shouldReturnNullOwningVariation() {
            // then
            assertThat(rootNode.getOwningVariation()).isNull();
        }

        @Test
        @DisplayName("Должен возвращать пустой список subVariations по умолчанию")
        void shouldReturnEmptySubVariations() {
            // then
            assertThat(rootNode.getSubVariations()).isEmpty();
        }

        @Test
        @DisplayName("Должен возвращать null annotation по умолчанию")
        void shouldReturnNullAnnotation() {
            // then
            assertThat(rootNode.getAnnotation()).isNull();
        }

        @Test
        @DisplayName("Должен возвращать пустой Set additionalAnnotations по умолчанию")
        void shouldReturnEmptyAdditionalAnnotations() {
            // then
            assertThat(rootNode.getAdditionalAnnotations()).isEmpty();
        }

        @Test
        @DisplayName("Должен возвращать null comment по умолчанию")
        void shouldReturnNullComment() {
            // then
            assertThat(rootNode.getComment()).isNull();
        }

        @Test
        @DisplayName("Должен возвращать false hasNagComment по умолчанию")
        void shouldReturnFalseHasNagComment() {
            // then
            assertThat(rootNode.isHasNagComment()).isFalse();
        }
    }

    // ============================================================
    // 9. ТЕСТЫ ДЛЯ РАБОТЫ С ВАРИАНТАМИ (унаследованные от ParentNode)
    // ============================================================

    @Nested
    @DisplayName("Работа с вариантами (унаследованные от ParentNode)")
    class VariationMethodsTests {

        @Test
        @DisplayName("getMainLineVariation() должен возвращать null если subVariations пуст")
        void shouldReturnNullForEmptySubVariations() {
            // then
            assertThat(rootNode.getMainLineVariation()).isNull();
        }

        @Test
        @DisplayName("getMainLineVariation() должен находить главную линию")
        void shouldFindMainLineVariation() {
            // given
            Variation mainLine = new Variation();
            mainLine.setMainLine(true);
            Variation subLine = new Variation();
            subLine.setMainLine(false);

            rootNode.getSubVariations().add(mainLine);
            rootNode.getSubVariations().add(subLine);

            // when
            Variation result = rootNode.getMainLineVariation();

            // then
            assertThat(result).isEqualTo(mainLine);
        }

        @Test
        @DisplayName("getMainLineByName() должен возвращать null если subVariations пуст")
        void shouldReturnNullForEmptySubVariationsByName() {
            // then
            assertThat(rootNode.getMainLineByName()).isNull();
        }

        @Test
        @DisplayName("getMainLineByName() должен находить вариант с именем '~'")
        void shouldFindVariationByName() {
            // given
            Variation mainLine = new Variation();
            mainLine.setName("~");
            Variation subLine = new Variation();
            subLine.setName("Other");

            rootNode.getSubVariations().add(mainLine);
            rootNode.getSubVariations().add(subLine);

            // when
            Variation result = rootNode.getMainLineByName();

            // then
            assertThat(result).isEqualTo(mainLine);
        }

        @Test
        @DisplayName("getMainLineByName() должен находить вариант с именем MAIN_LINE")
        void shouldFindVariationByMainLineName() {
            // given
            Variation mainLine = new Variation();
            mainLine.setName(lang.get(LanguageKeys.MAIN_LINE));
            Variation subLine = new Variation();
            subLine.setName("Other");

            rootNode.getSubVariations().add(mainLine);
            rootNode.getSubVariations().add(subLine);

            // when
            Variation result = rootNode.getMainLineByName();

            // then
            assertThat(result).isEqualTo(mainLine);
        }
    }
}