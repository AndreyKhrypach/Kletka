/*
 *
 *  * Copyright (c) 2025 Andrey Khrypach
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
import Khrypach.Andrey.chess.kletka.gui.languages.RuLanguage;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import org.junit.jupiter.api.*;


import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("VariationNamingService - Сервис именования вариантов")
class VariationNamingServiceTest {

    private VariationNamingService namingService;
    private LanguageManager languageManager;

    @BeforeEach
    void setUp() {
        LanguageManager.getInstance().setLanguage("en");
        namingService = new VariationNamingService();
        languageManager = LanguageManager.getInstance();
    }

    @AfterEach
    void tearDown() {
        LanguageManager.getInstance().setLanguage("en");
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
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ updateAllVariationNames()
    // ============================================================

    @Nested
    @DisplayName("renameAllIterative() - Полное переименование")
    class RenameAllIterativeTests {

        @Test
        @DisplayName("Должен переименовывать корневые варианты")
        void shouldRenameRootVariations() {
            // given
            RootNode root = new RootNode();
            Variation rootVariation = new Variation(languageManager.get(LanguageKeys.ROOT));
            rootVariation.setFirstNode(root);

            Variation var1 = new Variation("");
            var1.setFirstNode(createMoveNode("e4"));
            var1.setMainLine(false);
            root.getSubVariations().add(var1);

            Variation var2 = new Variation("");
            var2.setFirstNode(createMoveNode("d4"));
            var2.setMainLine(false);
            root.getSubVariations().add(var2);

            // when
            namingService.renameAllIterative(root, rootVariation, false);

            // then
            assertThat(var1.getName()).isEqualTo(namingService.getUppercaseLetter(1) + ")");
            assertThat(var2.getName()).isEqualTo(namingService.getUppercaseLetter(2) + ")");
        }

        @Test
        @DisplayName("Должен сохранять имя главной линии ~")
        void shouldPreserveMainLineName() {
            // given
            RootNode root = new RootNode();
            Variation rootVariation = new Variation(languageManager.get(LanguageKeys.ROOT));
            rootVariation.setFirstNode(root);

            Variation mainLine = new Variation(languageManager.get(LanguageKeys.MAIN_LINE));
            mainLine.setFirstNode(createMoveNode("e4"));
            mainLine.setMainLine(true);
            root.getSubVariations().add(mainLine);

            Variation var1 = new Variation("");
            var1.setFirstNode(createMoveNode("d4"));
            var1.setMainLine(false);
            root.getSubVariations().add(var1);

            // when
            namingService.renameAllIterative(root, rootVariation, false);

            // then
            assertThat(mainLine.getName()).isEqualTo(languageManager.get(LanguageKeys.MAIN_LINE));
            assertThat(var1.getName()).isEqualTo(namingService.getUppercaseLetter(1) + ")");
        }

        @Test
        @DisplayName("Должен переименовывать вложенные варианты")
        void shouldRenameNestedVariations() {
            // given
            RootNode root = new RootNode();
            Variation rootVariation = new Variation(languageManager.get(LanguageKeys.ROOT));
            rootVariation.setFirstNode(root);

            Variation var1 = new Variation("");
            MoveNode node1 = createMoveNode("e4");
            var1.setFirstNode(node1);
            var1.setMainLine(false);
            root.getSubVariations().add(var1);

            Variation subVar1 = new Variation("");
            subVar1.setFirstNode(createMoveNode("e5"));
            subVar1.setMainLine(false);
            node1.getSubVariations().add(subVar1);

            Variation subVar2 = new Variation("");
            subVar2.setFirstNode(createMoveNode("d5"));
            subVar2.setMainLine(false);
            node1.getSubVariations().add(subVar2);

            // when
            namingService.renameAllIterative(root, rootVariation, false);

            // then
            assertThat(var1.getName()).isEqualTo(namingService.getUppercaseLetter(1) + ")");
            assertThat(subVar1.getName()).isEqualTo(namingService.getUppercaseLetter(1) + "1)");
            assertThat(subVar2.getName()).isEqualTo(namingService.getUppercaseLetter(1) + "2)");
        }

        @Test
        @DisplayName("Должен обрабатывать null rootNode безопасно")
        void shouldHandleNullRootNode() {
            // when/then - не должно быть исключений
            namingService.renameAllIterative(null, new Variation(""), false);
        }

        @Test
        @DisplayName("Должен обрабатывать null rootVariation безопасно")
        void shouldHandleNullRootVariation() {
            // when/then - не должно быть исключений
            namingService.renameAllIterative(new RootNode(), null, false);
        }

        @Test
        @DisplayName("force=false должен сохранять кастомные имена")
        void shouldPreserveCustomNamesWhenNotForced() {
            // given
            RootNode root = new RootNode();
            Variation rootVariation = new Variation(languageManager.get(LanguageKeys.ROOT));
            rootVariation.setFirstNode(root);

            Variation var1 = new Variation("Custom Name");
            var1.setFirstNode(createMoveNode("e4"));
            var1.setMainLine(false);
            var1.setNameGenerated(true);   // ? имя уже сгенерировано (кастомное)
            root.getSubVariations().add(var1);

            // when
            namingService.renameAllIterative(root, rootVariation, false);

            // then
            assertThat(var1.getName()).isEqualTo("Custom Name");
        }

        @Test
        @DisplayName("force=true должен перезаписывать кастомные имена")
        void shouldOverwriteCustomNamesWhenForced() {
            // given
            RootNode root = new RootNode();
            Variation rootVariation = new Variation(languageManager.get(LanguageKeys.ROOT));
            rootVariation.setFirstNode(root);

            Variation var1 = new Variation("Custom Name");
            var1.setFirstNode(createMoveNode("e4"));
            var1.setMainLine(false);
            var1.setNameGenerated(true);
            root.getSubVariations().add(var1);

            // when
            namingService.renameAllIterative(root, rootVariation, true);

            // then
            assertThat(var1.getName()).isEqualTo(namingService.getUppercaseLetter(1) + ")");
        }
    }

    // ============================================================
    // 6. ТЕСТЫ ДЛЯ updateNamesRecursive() - ВЛОЖЕННЫЕ С ПРОДОЛЖЕНИЯМИ
    // ============================================================

    @Nested
    @DisplayName("renameFromNodeIterative() - Локальное переименование")
    class RenameFromNodeIterativeTests {

        @Test
        @DisplayName("Должен переименовывать поддерево от узла")
        void shouldRenameSubtreeFromNode() {
            // given
            ParentNode forkNode = new RootNode();
            Variation parentVariation = new Variation(languageManager.get(LanguageKeys.MAIN_LINE));
            parentVariation.setMainLine(true);

            Variation var1 = new Variation("");
            var1.setFirstNode(createMoveNode("e4"));
            var1.setMainLine(false);
            forkNode.getSubVariations().add(var1);

            Variation var2 = new Variation("");
            var2.setFirstNode(createMoveNode("d4"));
            var2.setMainLine(false);
            forkNode.getSubVariations().add(var2);

            // when
            namingService.renameFromNodeIterative(forkNode, parentVariation);

            // then
            assertThat(var1.getName()).isEqualTo("a)");   // parent = mainLine ? строчные
            assertThat(var2.getName()).isEqualTo("b)");
        }

        @Test
        @DisplayName("Должен сохранять кастомные имена")
        void shouldPreserveCustomNames() {
            // given
            ParentNode forkNode = new RootNode();
            Variation parentVariation = new Variation("A)");   // ? родитель — обычный вариант

            Variation var1 = new Variation("Custom");
            var1.setFirstNode(createMoveNode("e4"));
            var1.setMainLine(false);
            var1.setNameGenerated(true);
            forkNode.getSubVariations().add(var1);

            // when
            namingService.renameFromNodeIterative(forkNode, parentVariation);

            // then
            assertThat(var1.getName()).isEqualTo("Custom");
        }

        @Test
        @DisplayName("Должен обрабатывать null startNode безопасно")
        void shouldHandleNullStartNode() {
            // when/then - не должно быть исключений
            namingService.renameFromNodeIterative(null, new Variation(""));
        }

        @Test
        @DisplayName("Должен обрабатывать null parentVariation безопасно")
        void shouldHandleNullParentVariation() {
            // when/then - не должно быть исключений
            namingService.renameFromNodeIterative(new RootNode(), null);
        }

        @Test
        @DisplayName("Должен обрабатывать дубликаты (visited)")
        void shouldHandleDuplicateNodes() {
            // given
            RootNode root = new RootNode();
            Variation parentVariation = new Variation(languageManager.get(LanguageKeys.MAIN_LINE));
            parentVariation.setMainLine(true);

            MoveNode node = createMoveNode("e4");

            Variation var1 = new Variation("");
            var1.setFirstNode(node);
            var1.setMainLine(false);
            root.getSubVariations().add(var1);

            // Тот же node как next от root (дубликат)
            root.setNext(node);

            // when - не должно зациклиться (visited)
            namingService.renameFromNodeIterative(root, parentVariation);

            // then - var1 получил имя, не зависли
            assertThat(var1.getName()).isEqualTo("a)");
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