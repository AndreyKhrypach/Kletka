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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Variation - Вариант в дереве партии")
class VariationTest {

    private Variation variation;
    private MoveNode move1;
    private MoveNode move2;
    private MoveNode move3;

    private final LanguageManager lang = LanguageManager.getInstance();

    @BeforeEach
    void setUp() {
        // Создаем вариант с именем по умолчанию
        variation = new Variation();

        // Создаем тестовые ходы
        Move m1 = new Move(Square.E2, Square.E4, Piece.WHITE_PAWN);
        Move m2 = new Move(Square.E7, Square.E5, Piece.BLACK_PAWN);
        Move m3 = new Move(Square.G1, Square.F3, Piece.WHITE_KNIGHT);

        move1 = new MoveNode(m1, Piece.WHITE_PAWN, false, null);
        move2 = new MoveNode(m2, Piece.BLACK_PAWN, false, null);
        move3 = new MoveNode(m3, Piece.WHITE_KNIGHT, false, null);

        // Устанавливаем FEN для генерации SAN
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        move1.setSavedFenBefore(fen);
        move2.setSavedFenBefore(fen);
        move3.setSavedFenBefore(fen);
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ СОЗДАНИЯ VARIATION
    // ============================================================

    @Nested
    @DisplayName("Создание Variation")
    class CreationTests {

        @Test
        @DisplayName("Должен создавать Variation с именем по умолчанию")
        void shouldCreateVariationWithDefaultName() {
            // then
            assertThat(variation).isNotNull();
            assertThat(variation.getName()).isEqualTo(lang.get(LanguageKeys.VARIATION_DEFAULT_NAME));
            assertThat(variation.getId()).isGreaterThanOrEqualTo(0);
            assertThat(variation.getUuid()).isNotNull();
            assertThat(variation.isMainLine()).isFalse();
            assertThat(variation.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Должен создавать Variation с указанным именем")
        void shouldCreateVariationWithCustomName() {
            // given
            String name = "Главная линия";

            // when
            Variation customVariation = new Variation(name);

            // then
            assertThat(customVariation.getName()).isEqualTo(name);
            assertThat(customVariation.getId()).isGreaterThanOrEqualTo(0);
            assertThat(customVariation.getUuid()).isNotNull();
        }

        @Test
        @DisplayName("Должен иметь уникальные ID для каждого варианта")
        void shouldHaveUniqueIds() {
            // given
            Variation var1 = new Variation();
            Variation var2 = new Variation();

            // then
            assertThat(var1.getId()).isNotEqualTo(var2.getId());
        }

        @Test
        @DisplayName("Должен иметь уникальные UUID для каждого варианта")
        void shouldHaveUniqueUuids() {
            // given
            Variation var1 = new Variation();
            Variation var2 = new Variation();

            // then
            assertThat(var1.getUuid()).isNotEqualTo(var2.getUuid());
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ addMove()
    // ============================================================

    @Nested
    @DisplayName("addMove() - Добавление ходов")
    class AddMoveTests {

        @Test
        @DisplayName("Должен добавлять первый ход в пустой вариант")
        void shouldAddFirstMoveToEmptyVariation() {
            // when
            variation.addMove(move1);

            // then
            assertThat(variation.isEmpty()).isFalse();
            assertThat(variation.getFirstNode()).isEqualTo(move1);
            assertThat(variation.getMoveCount()).isEqualTo(1);
            assertThat(variation.getMoves()).containsExactly(move1);
        }

        @Test
        @DisplayName("Должен добавлять несколько ходов последовательно")
        void shouldAddMultipleMovesSequentially() {
            // when
            variation.addMove(move1);
            variation.addMove(move2);
            variation.addMove(move3);

            // then
            assertThat(variation.getMoveCount()).isEqualTo(3);
            assertThat(variation.getMoves())
                    .containsExactly(move1, move2, move3);

            // Проверяем связи
            assertThat(move1.getNext()).isEqualTo(move2);
            assertThat(move2.getNext()).isEqualTo(move3);
            assertThat(move3.getNext()).isNull();

            assertThat(move1.getParent()).isNull();
            assertThat(move2.getParent()).isEqualTo(move1);
            assertThat(move3.getParent()).isEqualTo(move2);
        }

        @Test
        @DisplayName("Должен игнорировать null при добавлении")
        void shouldIgnoreNullWhenAdding() {
            // when
            variation.addMove(null);

            // then
            assertThat(variation.isEmpty()).isTrue();
            assertThat(variation.getMoveCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Должен обновлять displayName после добавления ходов")
        void shouldUpdateDisplayNameAfterAddingMoves() {
            // when
            variation.addMove(move1);
            variation.addMove(move2);

            // then
            assertThat(variation.getDisplayName().get())
                    .contains(lang.get(LanguageKeys.VARIATION_DEFAULT_NAME))
                    .contains("e4")
                    .contains("e5");
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ getMoves()
    // ============================================================

    @Nested
    @DisplayName("getMoves() - Получение списка ходов")
    class GetMovesTests {

        @Test
        @DisplayName("Должен возвращать пустой список для пустого варианта")
        void shouldReturnEmptyListForEmptyVariation() {
            // when
            List<ParentNode> moves = variation.getMoves();

            // then
            assertThat(moves).isEmpty();
        }

        @Test
        @DisplayName("Должен возвращать список ходов для непустого варианта")
        void shouldReturnMovesForNonEmptyVariation() {
            // given
            variation.addMove(move1);
            variation.addMove(move2);

            // when
            List<ParentNode> moves = variation.getMoves();

            // then
            assertThat(moves).hasSize(2);
            assertThat(moves).containsExactly(move1, move2);
        }

        @Test
        @DisplayName("Должен возвращать список ходов в правильном порядке")
        void shouldReturnMovesInCorrectOrder() {
            // given
            variation.addMove(move1);
            variation.addMove(move2);
            variation.addMove(move3);

            // when
            List<ParentNode> moves = variation.getMoves();

            // then
            assertThat(moves)
                    .containsExactly(move1, move2, move3);
        }
    }

    // ============================================================
    // 4. ТЕСТЫ ДЛЯ getLastNode()
    // ============================================================

    @Nested
    @DisplayName("getLastNode() - Получение последнего узла")
    class GetLastNodeTests {

        @Test
        @DisplayName("Должен возвращать null для пустого варианта")
        void shouldReturnNullForEmptyVariation() {
            // when
            ParentNode last = variation.getLastNode();

            // then
            assertThat(last).isNull();
        }

        @Test
        @DisplayName("Должен возвращать единственный узел для варианта с одним ходом")
        void shouldReturnOnlyNodeForSingleMove() {
            // given
            variation.addMove(move1);

            // when
            ParentNode last = variation.getLastNode();

            // then
            assertThat(last).isEqualTo(move1);
        }

        @Test
        @DisplayName("Должен возвращать последний узел для варианта с несколькими ходами")
        void shouldReturnLastNodeForMultipleMoves() {
            // given
            variation.addMove(move1);
            variation.addMove(move2);
            variation.addMove(move3);

            // when
            ParentNode last = variation.getLastNode();

            // then
            assertThat(last).isEqualTo(move3);
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ getMoveCount()
    // ============================================================

    @Nested
    @DisplayName("getMoveCount() - Получение количества ходов")
    class GetMoveCountTests {

        @Test
        @DisplayName("Должен возвращать 0 для пустого варианта")
        void shouldReturnZeroForEmptyVariation() {
            // when
            int count = variation.getMoveCount();

            // then
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("Должен возвращать правильное количество ходов")
        void shouldReturnCorrectMoveCount() {
            // given
            variation.addMove(move1);
            variation.addMove(move2);
            variation.addMove(move3);

            // when
            int count = variation.getMoveCount();

            // then
            assertThat(count).isEqualTo(3);
        }
    }

    // ============================================================
    // 6. ТЕСТЫ ДЛЯ isEmpty()
    // ============================================================

    @Nested
    @DisplayName("isEmpty() - Проверка на пустоту")
    class IsEmptyTests {

        @Test
        @DisplayName("Должен возвращать true для пустого варианта")
        void shouldReturnTrueForEmptyVariation() {
            // then
            assertThat(variation.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Должен возвращать false для непустого варианта")
        void shouldReturnFalseForNonEmptyVariation() {
            // given
            variation.addMove(move1);

            // then
            assertThat(variation.isEmpty()).isFalse();
        }
    }

    // ============================================================
    // 7. ТЕСТЫ ДЛЯ clearMoves()
    // ============================================================

    @Nested
    @DisplayName("clearMoves() - Очистка ходов")
    class ClearMovesTests {

        @Test
        @DisplayName("Должен очищать все ходы")
        void shouldClearAllMoves() {
            // given
            variation.addMove(move1);
            variation.addMove(move2);
            assertThat(variation.getMoveCount()).isEqualTo(2);

            // when
            variation.clearMoves();

            // then
            assertThat(variation.isEmpty()).isTrue();
            assertThat(variation.getMoveCount()).isEqualTo(0);
            assertThat(variation.getFirstNode()).isNull();
        }

        @Test
        @DisplayName("Должен обновлять displayName после очистки")
        void shouldUpdateDisplayNameAfterClear() {
            // given
            variation.addMove(move1);
            variation.addMove(move2);
            String before = variation.getDisplayName().get();

            // when
            variation.clearMoves();

            // then
            assertThat(variation.getDisplayName().get())
                    .isNotEqualTo(before)
                    .isEqualTo(lang.get(LanguageKeys.VARIATION_DEFAULT_NAME));
        }
    }

    // ============================================================
    // 8. ТЕСТЫ ДЛЯ setName()
    // ============================================================

    @Nested
    @DisplayName("setName() - Установка имени")
    class SetNameTests {

        @Test
        @DisplayName("Должен устанавливать новое имя")
        void shouldSetNewName() {
            // given
            String newName = lang.get(LanguageKeys.MAIN_LINE);

            // when
            variation.setName(newName);

            // then
            assertThat(variation.getName()).isEqualTo(newName);
        }

        @Test
        @DisplayName("Должен обновлять displayName после смены имени")
        void shouldUpdateDisplayNameAfterNameChange() {
            // given
            variation.addMove(move1);
            String oldDisplayName = variation.getDisplayName().get();

            // when
            variation.setName("Новое имя");

            // then
            assertThat(variation.getDisplayName().get())
                    .isNotEqualTo(oldDisplayName)
                    .startsWith("Новое имя");
        }
    }

    // ============================================================
    // 9. ТЕСТЫ ДЛЯ getFirstNode()
    // ============================================================

    @Nested
    @DisplayName("getFirstNode() - Получение первого узла")
    class GetFirstNodeTests {

        @Test
        @DisplayName("Должен возвращать null для пустого варианта")
        void shouldReturnNullForEmptyVariation() {
            // when
            ParentNode first = variation.getFirstNode();

            // then
            assertThat(first).isNull();
        }

        @Test
        @DisplayName("Должен возвращать первый узел для непустого варианта")
        void shouldReturnFirstNodeForNonEmptyVariation() {
            // given
            variation.addMove(move1);
            variation.addMove(move2);

            // when
            ParentNode first = variation.getFirstNode();

            // then
            assertThat(first).isEqualTo(move1);
        }

        @Test
        @DisplayName("Должен возвращать первый реальный ход для главной линии с корнем")
        void shouldReturnFirstRealMoveForMainLineWithRoot() {
            // given
            RootNode root = new RootNode();
            variation.setFirstNode(root);
            variation.setMainLine(true);
            variation.setParentNodeRef(root);

            root.setNext(move1);
            move1.setNext(move2);

            // when
            ParentNode first = variation.getFirstNode();

            // then
            assertThat(first).isEqualTo(move1);
        }
    }

    // ============================================================
    // 10. ТЕСТЫ ДЛЯ toString()
    // ============================================================

    @Nested
    @DisplayName("toString() - Строковое представление")
    class ToStringTests {

        @Test
        @DisplayName("Должен возвращать строковое представление с именем и ID")
        void shouldReturnToStringWithNameAndId() {
            // when
            String toString = variation.toString();

            // then
            assertThat(toString).contains(variation.getName());
            assertThat(toString).contains("id=" + variation.getId());
            assertThat(toString).contains(variation.getUuid().substring(0, 8));
        }

        @Test
        @DisplayName("Должен иметь правильный формат")
        void shouldHaveCorrectFormat() {
            // when
            String toString = variation.toString();

            // then
            assertThat(toString).matches(
                    lang.get(LanguageKeys.VARIATION_DEFAULT_NAME) + " \\[id=\\d+, uuid=[a-f0-9]{8}]"
            );
        }
    }

    // ============================================================
    // 11. ТЕСТЫ ДЛЯ getDebugInfo()
    // ============================================================

    @Nested
    @DisplayName("getDebugInfo() - Отладочная информация")
    class DebugInfoTests {

        @Test
        @DisplayName("Должен возвращать отладочную информацию с именем, UUID, ID и флагом mainLine")
        void shouldReturnDebugInfo() {
            // given
            variation.setMainLine(true);

            // when
            String debug = variation.getDebugInfo();

            // then
            assertThat(debug).contains(variation.getName());
            assertThat(debug).contains(variation.getUuid().substring(0, 8));
            assertThat(debug).contains("id=" + variation.getId());
            assertThat(debug).contains("mainLine=true");
        }

        @Test
        @DisplayName("Должен показывать false для mainLine если не главная линия")
        void shouldShowFalseForNonMainLine() {
            // when
            String debug = variation.getDebugInfo();

            // then
            assertThat(debug).contains("mainLine=false");
        }
    }

    // ============================================================
    // 12. ТЕСТЫ ДЛЯ equals() и hashCode()
    // ============================================================

    @Nested
    @DisplayName("equals() и hashCode()")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Должен считать равными варианты с одинаковым UUID")
        void shouldBeEqualWithSameUuid() {
            // given
            Variation var1 = new Variation();
            Variation var2 = new Variation();

            // Принудительно устанавливаем одинаковый UUID для теста
            var2.setUuid(var1.getUuid());

            // then
            assertThat(var1).isEqualTo(var2);
            assertThat(var1.hashCode()).isEqualTo(var2.hashCode());
        }

        @Test
        @DisplayName("Должен считать разными варианты с разным UUID")
        void shouldNotBeEqualWithDifferentUuid() {
            // given
            Variation var1 = new Variation();
            Variation var2 = new Variation();

            // then
            assertThat(var1).isNotEqualTo(var2);
            assertThat(var1.hashCode()).isNotEqualTo(var2.hashCode());
        }

        @Test
        @DisplayName("Должен возвращать false при сравнении с null")
        void shouldReturnFalseWhenComparedWithNull() {
            // then
            assertThat(variation).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Должен возвращать true при сравнении с самим собой")
        void shouldReturnTrueWhenComparedWithItself() {
            // then
            assertThat(variation).isEqualTo(variation);
        }
    }

    // ============================================================
    // 13. ТЕСТЫ ДЛЯ СЕТТЕРОВ И ГЕТТЕРОВ
    // ============================================================

    @Nested
    @DisplayName("Сеттеры и Геттеры")
    class SettersAndGettersTests {

        @Test
        @DisplayName("Должен устанавливать и получать firstNode")
        void shouldSetAndGetFirstNode() {
            // when
            variation.setFirstNode(move1);

            // then
            assertThat(variation.getFirstNode()).isEqualTo(move1);
        }

        @Test
        @DisplayName("Должен устанавливать и получать mainLine")
        void shouldSetAndGetMainLine() {
            // when
            variation.setMainLine(true);

            // then
            assertThat(variation.isMainLine()).isTrue();
        }

        @Test
        @DisplayName("Должен устанавливать и получать parentVariation")
        void shouldSetAndGetParentVariation() {
            // given
            Variation parent = new Variation();

            // when
            variation.setParentVariation(parent);

            // then
            assertThat(variation.getParentVariation()).isEqualTo(parent);
        }

        @Test
        @DisplayName("Должен устанавливать и получать parentNodeRef")
        void shouldSetAndGetParentNodeRef() {
            // given
            RootNode root = new RootNode();

            // when
            variation.setParentNodeRef(root);

            // then
            assertThat(variation.getParentNodeRef()).isEqualTo(root);
        }

        @Test
        @DisplayName("Должен устанавливать и получать rootVariation")
        void shouldSetAndGetRootVariation() {
            // given
            Variation root = new Variation();

            // when
            variation.setRootVariation(root);

            // then
            assertThat(variation.getRootVariation()).isEqualTo(root);
        }

        @Test
        @DisplayName("Должен устанавливать и получать parentFen")
        void shouldSetAndGetParentFen() {
            // given
            String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

            // when
            variation.setParentFen(fen);

            // then
            assertThat(variation.getParentFen()).isEqualTo(fen);
        }

        @Test
        @DisplayName("Должен устанавливать и получать parentPly")
        void shouldSetAndGetParentPly() {
            // when
            variation.setParentPly(5);

            // then
            assertThat(variation.getParentPly()).isEqualTo(5);
        }

        @Test
        @DisplayName("Должен получать displayName")
        void shouldGetDisplayName() {
            // then
            assertThat(variation.getDisplayName()).isNotNull();
            assertThat(variation.getDisplayName().get()).isEqualTo(lang.get(LanguageKeys.VARIATION_DEFAULT_NAME));
        }

        @Test
        @DisplayName("displayName должен обновляться при добавлении ходов")
        void displayNameShouldUpdateWhenAddingMoves() {
            // when
            variation.addMove(move1);
            variation.addMove(move2);
            variation.addMove(move3);

            // then
            String displayName = variation.getDisplayName().get();
            assertThat(displayName).contains("e4", "e5", "Nf3");
            assertThat(displayName).doesNotContain("..."); // 3 хода, без троеточия
        }

        @Test
        @DisplayName("displayName должен показывать троеточие при 4+ ходах")
        void displayNameShouldShowEllipsisForManyMoves() {
            // given
            Move m4 = new Move(Square.D2, Square.D4, Piece.WHITE_PAWN);
            MoveNode move4 = new MoveNode(m4, Piece.WHITE_PAWN, false, null);
            move4.setSavedFenBefore("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

            // when
            variation.addMove(move1);
            variation.addMove(move2);
            variation.addMove(move3);
            variation.addMove(move4);

            // then
            String displayName = variation.getDisplayName().get();
            assertThat(displayName).contains("...");
        }
    }
}