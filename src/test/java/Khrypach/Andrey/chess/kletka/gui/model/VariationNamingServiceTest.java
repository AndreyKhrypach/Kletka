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
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("VariationNamingService - Сервис именования вариантов")
class VariationNamingServiceTest {

    private VariationNamingService namingService;
    private LanguageManager languageManager;

    @BeforeEach
    void setUp() {
        namingService = new VariationNamingService();
        languageManager = LanguageManager.getInstance();
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ generateUniqueName() - КОРНЕВЫЕ ВАРИАНТЫ
    // ============================================================

    @Nested
    @DisplayName("generateUniqueName() - Корневые варианты")
    class GenerateUniqueNameRootTests {

        @Test
        @DisplayName("Должен генерировать A) для первого корневого варианта")
        void shouldGenerateAforFirstRootVariation() {
            // given
            int siblingIndex = 1;

            // when
            String name = namingService.generateUniqueName(null, siblingIndex);

            // then
            assertThat(name).isEqualTo("A)");
        }

        @Test
        @DisplayName("Должен генерировать B) для второго корневого варианта")
        void shouldGenerateBforSecondRootVariation() {
            // given
            int siblingIndex = 2;

            // when
            String name = namingService.generateUniqueName(null, siblingIndex);

            // then
            assertThat(name).isEqualTo("B)");
        }

        @Test
        @DisplayName("Должен генерировать C) для третьего корневого варианта")
        void shouldGenerateCforThirdRootVariation() {
            // given
            int siblingIndex = 3;

            // when
            String name = namingService.generateUniqueName(null, siblingIndex);

            // then
            assertThat(name).isEqualTo("C)");
        }

        @Test
        @DisplayName("Должен генерировать Z) для 26-го корневого варианта")
        void shouldGenerateZfor26thRootVariation() {
            // given
            int siblingIndex = 26;

            // when
            String name = namingService.generateUniqueName(null, siblingIndex);

            // then
            assertThat(name).isEqualTo("Z)");
        }

        @Test
        @DisplayName("Должен генерировать A) для 27-го корневого варианта (циклически)")
        void shouldGenerateAfor27thRootVariation() {
            // given
            int siblingIndex = 27;

            // when
            String name = namingService.generateUniqueName(null, siblingIndex);

            // then
            assertThat(name).isEqualTo("A)");
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ generateUniqueName() - ГЛАВНАЯ ЛИНИЯ
    // ============================================================

    @Nested
    @DisplayName("generateUniqueName() - Главная линия")
    class GenerateUniqueNameMainLineTests {

        @Test
        @DisplayName("Должен генерировать a) для варианта от главной линии")
        void shouldGenerateAforMainLineParent() {
            // given
            Variation parent = new Variation("~");
            int siblingIndex = 1;

            // when
            String name = namingService.generateUniqueName(parent, siblingIndex);

            // then
            assertThat(name).isEqualTo("a)");
        }

        @Test
        @DisplayName("Должен генерировать b) для второго варианта от главной линии")
        void shouldGenerateBforSecondMainLineParent() {
            // given
            Variation parent = new Variation("~");
            int siblingIndex = 2;

            // when
            String name = namingService.generateUniqueName(parent, siblingIndex);

            // then
            assertThat(name).isEqualTo("b)");
        }

        @Test
        @DisplayName("Должен генерировать a) для варианта от 'Главная линия'")
        void shouldGenerateAforMainLineParentByName() {
            // given
            Variation parent = new Variation(languageManager.get(LanguageKeys.MAIN_LINE));
            int siblingIndex = 1;

            // when
            String name = namingService.generateUniqueName(parent, siblingIndex);

            // then
            assertThat(name).isEqualTo("a)");
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ generateUniqueName() - ВЛОЖЕННЫЕ ВАРИАНТЫ
    // ============================================================

    @Nested
    @DisplayName("generateUniqueName() - Вложенные варианты")
    class GenerateUniqueNameNestedTests {

        @Test
        @DisplayName("Должен генерировать A1) для варианта внутри A)")
        void shouldGenerateA1ForVariationInsideA() {
            // given
            Variation parent = new Variation("A)");
            int siblingIndex = 1;

            // when
            String name = namingService.generateUniqueName(parent, siblingIndex);

            // then
            assertThat(name).isEqualTo("A1)");
        }

        @Test
        @DisplayName("Должен генерировать A2) для второго варианта внутри A)")
        void shouldGenerateA2ForSecondVariationInsideA() {
            // given
            Variation parent = new Variation("A)");
            int siblingIndex = 2;

            // when
            String name = namingService.generateUniqueName(parent, siblingIndex);

            // then
            assertThat(name).isEqualTo("A2)");
        }

        @Test
        @DisplayName("Должен генерировать A1a) для варианта внутри A1)")
        void shouldGenerateA1aForVariationInsideA1() {
            // given
            Variation parent = new Variation("A1)");
            int siblingIndex = 1;

            // when
            String name = namingService.generateUniqueName(parent, siblingIndex);

            // then
            assertThat(name).isEqualTo("A1a)");
        }

        @Test
        @DisplayName("Должен генерировать A1b) для второго варианта внутри A1)")
        void shouldGenerateA1bForSecondVariationInsideA1() {
            // given
            Variation parent = new Variation("A1)");
            int siblingIndex = 2;

            // when
            String name = namingService.generateUniqueName(parent, siblingIndex);

            // then
            assertThat(name).isEqualTo("A1b)");
        }

        @Test
        @DisplayName("Должен генерировать A1a1) для варианта внутри A1a)")
        void shouldGenerateA1a1ForVariationInsideA1a() {
            // given
            Variation parent = new Variation("A1a)");
            int siblingIndex = 1;

            // when
            String name = namingService.generateUniqueName(parent, siblingIndex);

            // then
            assertThat(name).isEqualTo("A1a1)");
        }
    }

    // ============================================================
    // 4. ТЕСТЫ ДЛЯ generateUniqueName() - РУССКИЙ ЯЗЫК
    // ============================================================

    @Nested
    @DisplayName("generateUniqueName() - Русский язык")
    class GenerateUniqueNameRussianTests {

        @Test
        @DisplayName("Должен генерировать А) для первого корневого варианта на русском")
        void shouldGenerateRussianAforFirstRoot() {
            // given
            languageManager.setLanguage("ru");
            int siblingIndex = 1;

            // when
            String name = namingService.generateUniqueName(null, siblingIndex);

            // then
            assertThat(name).isEqualTo("А)");
            languageManager.setLanguage("en");
        }

        @Test
        @DisplayName("Должен генерировать а) для варианта от главной линии на русском")
        void shouldGenerateRussianAforMainLine() {
            // given
            languageManager.setLanguage("ru");
            Variation parent = new Variation("~");
            int siblingIndex = 1;

            // when
            String name = namingService.generateUniqueName(parent, siblingIndex);

            // then
            assertThat(name).isEqualTo("а)");
            languageManager.setLanguage("en");
        }

        @Test
        @DisplayName("Должен генерировать А1) для варианта внутри А) на русском")
        void shouldGenerateRussianA1ForVariationInsideA() {
            // given
            languageManager.setLanguage("ru");
            Variation parent = new Variation("А)");
            int siblingIndex = 1;

            // when
            String name = namingService.generateUniqueName(parent, siblingIndex);

            // then
            assertThat(name).isEqualTo("А1)");
            languageManager.setLanguage("en");
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ updateAllVariationNames()
    // ============================================================

    @Nested
    @DisplayName("updateAllVariationNames() - Обновление всех имен")
    class UpdateAllVariationNamesTests {

        @Test
        @DisplayName("Должен обновлять имена корневых вариантов")
        void shouldUpdateRootVariationNames() {
            // given
            RootNode root = new RootNode();
            Variation rootVariation = new Variation(languageManager.get(LanguageKeys.ROOT));
            rootVariation.setFirstNode(root);

            Variation var1 = new Variation("");
            MoveNode node1 = createMoveNode("e4");
            var1.setFirstNode(node1);
            var1.setMainLine(false);
            root.getSubVariations().add(var1);

            Variation var2 = new Variation("");
            MoveNode node2 = createMoveNode("d4");
            var2.setFirstNode(node2);
            var2.setMainLine(false);
            root.getSubVariations().add(var2);

            // when
            namingService.updateAllVariationNames(rootVariation);

            // then
            assertThat(var1.getName()).isEqualTo((namingService.getUppercaseLetter(1) + ")"));
            assertThat(var2.getName()).isEqualTo((namingService.getUppercaseLetter(2) + ")"));
        }

        @Test
        @DisplayName("Должен обновлять имена с главной линией")
        void shouldUpdateNamesWithMainLine() {
            // given
            RootNode root = new RootNode();
            Variation rootVariation = new Variation(languageManager.get(LanguageKeys.ROOT));
            rootVariation.setFirstNode(root);

            Variation mainLine = new Variation(languageManager.get(LanguageKeys.MAIN_LINE));
            MoveNode mainNode = createMoveNode("e4");
            mainLine.setFirstNode(mainNode);
            mainLine.setMainLine(true);
            root.getSubVariations().add(mainLine);

            Variation var1 = new Variation("");
            MoveNode node1 = createMoveNode("d4");
            var1.setFirstNode(node1);
            var1.setMainLine(false);
            root.getSubVariations().add(var1);

            Variation var2 = new Variation("");
            MoveNode node2 = createMoveNode("c4");
            var2.setFirstNode(node2);
            var2.setMainLine(false);
            root.getSubVariations().add(var2);

            // when
            namingService.updateAllVariationNames(rootVariation);

            // then
            assertThat(mainLine.getName()).isEqualTo("~");
            assertThat(var1.getName()).isEqualTo(namingService.getUppercaseLetter(1) + ")");
            assertThat(var2.getName()).isEqualTo(namingService.getUppercaseLetter(2) + ")");
        }

        @Test
        @DisplayName("Должен обновлять имена вложенных вариантов")
        void shouldUpdateNestedVariationNames() {
            // given
            RootNode root = new RootNode();
            Variation rootVariation = new Variation(languageManager.get(LanguageKeys.ROOT));
            rootVariation.setFirstNode(root);

            Variation var1 = new Variation("");
            MoveNode node1 = createMoveNode("e4");
            var1.setFirstNode(node1);
            var1.setMainLine(false);
            var1.setNameGenerated(true);
            root.getSubVariations().add(var1);

            // Вложенный вариант
            Variation subVar1 = new Variation("");
            MoveNode subNode1 = createMoveNode("e5");
            subVar1.setFirstNode(subNode1);
            subVar1.setMainLine(false);
            subVar1.setNameGenerated(true);
            node1.getSubVariations().add(subVar1);

            // Второй вложенный вариант
            Variation subVar2 = new Variation("");
            MoveNode subNode2 = createMoveNode("d5");
            subVar2.setFirstNode(subNode2);
            subVar2.setMainLine(false);
            subVar2.setNameGenerated(true);
            node1.getSubVariations().add(subVar2);

            // when
            namingService.updateAllVariationNames(rootVariation);

            // then
            assertThat(var1.getName()).isEqualTo(namingService.getUppercaseLetter(1) + ")");
            // Вложенные варианты должны получить имена на основе A)
            assertThat(subVar1.getName()).isEqualTo(namingService.getUppercaseLetter(1) + "1)");
            assertThat(subVar2.getName()).isEqualTo(namingService.getUppercaseLetter(1) + "2)");
        }

        @Test
        @DisplayName("Должен корректно обрабатывать null rootVariation")
        void shouldHandleNullRootVariation() {
            // when/then - не должно быть исключений
            namingService.updateAllVariationNames(null);
        }

        @Test
        @DisplayName("Должен корректно обрабатывать rootVariation без firstNode")
        void shouldHandleRootVariationWithoutFirstNode() {
            // given
            Variation rootVariation = new Variation(languageManager.get(LanguageKeys.ROOT));
            rootVariation.setFirstNode(null);

            // when/then - не должно быть исключений
            namingService.updateAllVariationNames(rootVariation);
        }
    }

    // ============================================================
    // 6. ТЕСТЫ ДЛЯ updateNamesRecursive() - ВЛОЖЕННЫЕ С ПРОДОЛЖЕНИЯМИ
    // ============================================================

    @Nested
    @DisplayName("updateNamesRecursive() - Вложенные варианты с продолжениями")
    class UpdateNamesRecursiveContinuationTests {

        @Test
        @DisplayName("Должен правильно именовать варианты с продолжением")
        void shouldCorrectlyNameVariationsWithContinuation() {
            // given
            RootNode root = new RootNode();
            Variation rootVariation = new Variation(languageManager.get(LanguageKeys.ROOT));
            rootVariation.setFirstNode(root);

            Variation var1 = new Variation("");
            MoveNode node1 = createMoveNode("e4");
            var1.setFirstNode(node1);
            var1.setMainLine(false);
            root.getSubVariations().add(var1);

            MoveNode node2 = createMoveNode("e5");
            node1.setNext(node2);
            node2.setParent(node1);

            Variation continuation = new Variation("");
            MoveNode contNode = createMoveNode("e6");
            continuation.setFirstNode(contNode);
            continuation.setMainLine(false);
            node1.getSubVariations().add(continuation);

            Variation newVar = new Variation("");
            MoveNode newVarNode = createMoveNode("d5");
            newVar.setFirstNode(newVarNode);
            newVar.setMainLine(false);
            node1.getSubVariations().add(newVar);

            // when
            namingService.updateAllVariationNames(rootVariation);

            // then
            assertThat(var1.getName()).isEqualTo((namingService.getUppercaseLetter(1) + ")"));
            // Проверяем, что продолжение и новый вариант получили имена
            assertThat(continuation.getName()).isNotEqualTo("");
            assertThat(newVar.getName()).isNotEqualTo("");
            // Имена должны быть разными
            assertThat(continuation.getName()).isNotEqualTo(newVar.getName());
        }
    }

    // ============================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ============================================================

    private MoveNode createMoveNode(String san) {
        Move move;
        Piece piece = Piece.WHITE_PAWN;

        switch (san) {
            case "d4" -> move = new Move(Square.D2, Square.D4, Piece.WHITE_PAWN);
            case "c4" -> move = new Move(Square.C2, Square.C4, Piece.WHITE_PAWN);
            case "e5" -> {
                move = new Move(Square.E7, Square.E5, Piece.BLACK_PAWN);
                piece = Piece.BLACK_PAWN;
            }
            case "d5" -> {
                move = new Move(Square.D7, Square.D5, Piece.BLACK_PAWN);
                piece = Piece.BLACK_PAWN;
            }
            case "e6" -> {
                move = new Move(Square.E7, Square.E6, Piece.BLACK_PAWN);
                piece = Piece.BLACK_PAWN;
            }
            default -> move = new Move(Square.E2, Square.E4, Piece.WHITE_PAWN);
        }

        MoveNode node = new MoveNode(move, piece, false, null);
        node.setSavedFenBefore("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        return node;
    }
}