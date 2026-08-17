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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("MoveAnnotation - Аннотации шахматных ходов")
class MoveAnnotationTest {

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ getSymbol()
    // ============================================================

    @Nested
    @DisplayName("getSymbol() - Получение символа аннотации")
    class GetSymbolTests {

        @Test
        @DisplayName("Должен возвращать правильный символ для GOOD_MOVE")
        void shouldReturnCorrectSymbolForGoodMove() {
            assertThat(MoveAnnotation.GOOD_MOVE.getSymbol()).isEqualTo("!");
        }

        @Test
        @DisplayName("Должен возвращать правильный символ для BLUNDER")
        void shouldReturnCorrectSymbolForBlunder() {
            assertThat(MoveAnnotation.BLUNDER.getSymbol()).isEqualTo("??");
        }

        @Test
        @DisplayName("Должен возвращать правильный символ для CHECK")
        void shouldReturnCorrectSymbolForCheck() {
            assertThat(MoveAnnotation.CHECK.getSymbol()).isEqualTo("+");
        }

        @Test
        @DisplayName("Должен возвращать правильный символ для MATE")
        void shouldReturnCorrectSymbolForMate() {
            assertThat(MoveAnnotation.MATE.getSymbol()).isEqualTo("#");
        }

        @Test
        @DisplayName("Должен возвращать правильный символ для DOUBLE_CHECK")
        void shouldReturnCorrectSymbolForDoubleCheck() {
            assertThat(MoveAnnotation.DOUBLE_CHECK.getSymbol()).isEqualTo("++");
        }

        @Test
        @DisplayName("Должен возвращать правильный символ для CLEAR_ADVANTAGE_WHITE")
        void shouldReturnCorrectSymbolForClearAdvantageWhite() {
            assertThat(MoveAnnotation.CLEAR_ADVANTAGE_WHITE.getSymbol()).isEqualTo(" ± ");
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ getDisplayWithSpace()
    // ============================================================

    @Nested
    @DisplayName("getDisplayWithSpace() - Получение отображения с пробелом")
    class GetDisplayWithSpaceTests {

        @Test
        @DisplayName("Должен возвращать отображение с пробелом для GOOD_MOVE")
        void shouldReturnDisplayWithSpaceForGoodMove() {
            assertThat(MoveAnnotation.GOOD_MOVE.getDisplayWithSpace()).isEqualTo("! ");
        }

        @Test
        @DisplayName("Должен возвращать отображение с пробелом для BLUNDER")
        void shouldReturnDisplayWithSpaceForBlunder() {
            assertThat(MoveAnnotation.BLUNDER.getDisplayWithSpace()).isEqualTo("?? ");
        }

        @Test
        @DisplayName("Должен возвращать отображение без пробела для CHECK")
        void shouldReturnDisplayWithoutSpaceForCheck() {
            assertThat(MoveAnnotation.CHECK.getDisplayWithSpace()).isEqualTo("+");
        }

        @Test
        @DisplayName("Должен возвращать отображение без пробела для MATE")
        void shouldReturnDisplayWithoutSpaceForMate() {
            assertThat(MoveAnnotation.MATE.getDisplayWithSpace()).isEqualTo("# ");
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ getDescription()
    // ============================================================

    @Nested
    @DisplayName("getDescription() - Получение описания аннотации")
    class GetDescriptionTests {

        @Test
        @DisplayName("Должен возвращать описание для GOOD_MOVE")
        void shouldReturnDescriptionForGoodMove() {
            String description = MoveAnnotation.GOOD_MOVE.getDescription();
            assertThat(description).isNotNull();
            assertThat(description).isNotEmpty();
        }

        @Test
        @DisplayName("Должен возвращать описание для BLUNDER")
        void shouldReturnDescriptionForBlunder() {
            String description = MoveAnnotation.BLUNDER.getDescription();
            assertThat(description).isNotNull();
            assertThat(description).isNotEmpty();
        }

        @Test
        @DisplayName("Должен возвращать описание для CHECK")
        void shouldReturnDescriptionForCheck() {
            String description = MoveAnnotation.CHECK.getDescription();
            assertThat(description).isNotNull();
            assertThat(description).isNotEmpty();
        }
    }

    // ============================================================
    // 4. ТЕСТЫ ДЛЯ fromSymbol()
    // ============================================================

    @Nested
    @DisplayName("fromSymbol() - Получение аннотации по символу")
    class FromSymbolTests {

        @Test
        @DisplayName("Должен возвращать GOOD_MOVE для символа '!'")
        void shouldReturnGoodMoveForExclamation() {
            MoveAnnotation result = MoveAnnotation.fromSymbol("!");
            assertThat(result).isEqualTo(MoveAnnotation.GOOD_MOVE);
        }

        @Test
        @DisplayName("Должен возвращать BLUNDER для символа '??'")
        void shouldReturnBlunderForDoubleQuestion() {
            MoveAnnotation result = MoveAnnotation.fromSymbol("??");
            assertThat(result).isEqualTo(MoveAnnotation.BLUNDER);
        }

        @Test
        @DisplayName("Должен возвращать CHECK для символа '+'")
        void shouldReturnCheckForPlus() {
            MoveAnnotation result = MoveAnnotation.fromSymbol("+");
            assertThat(result).isEqualTo(MoveAnnotation.CHECK);
        }

        @Test
        @DisplayName("Должен возвращать MATE для символа '#'")
        void shouldReturnMateForHash() {
            MoveAnnotation result = MoveAnnotation.fromSymbol("#");
            assertThat(result).isEqualTo(MoveAnnotation.MATE);
        }

        @Test
        @DisplayName("Должен возвращать DOUBLE_CHECK для символа '++'")
        void shouldReturnDoubleCheckForDoublePlus() {
            MoveAnnotation result = MoveAnnotation.fromSymbol("++");
            assertThat(result).isEqualTo(MoveAnnotation.DOUBLE_CHECK);
        }

        @Test
        @DisplayName("Должен возвращать INTERESTING_MOVE для символа '!?'")
        void shouldReturnInterestingMoveForExclamationQuestion() {
            MoveAnnotation result = MoveAnnotation.fromSymbol("!?");
            assertThat(result).isEqualTo(MoveAnnotation.INTERESTING_MOVE);
        }

        @Test
        @DisplayName("Должен возвращать DUBIOUS_MOVE для символа '?!'")
        void shouldReturnDubiousMoveForQuestionExclamation() {
            MoveAnnotation result = MoveAnnotation.fromSymbol("?!");
            assertThat(result).isEqualTo(MoveAnnotation.DUBIOUS_MOVE);
        }

        @Test
        @DisplayName("Должен возвращать null для неизвестного символа")
        void shouldReturnNullForUnknownSymbol() {
            MoveAnnotation result = MoveAnnotation.fromSymbol("unknown");
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Должен возвращать CLEAR_ADVANTAGE_WHITE для символа ' ± '")
        void shouldReturnClearAdvantageWhiteForSymbolWithSpaces() {
            MoveAnnotation result = MoveAnnotation.fromSymbol(" ± ");
            assertThat(result).isEqualTo(MoveAnnotation.CLEAR_ADVANTAGE_WHITE);
        }

        @Test
        @DisplayName("Должен возвращать CLEAR_ADVANTAGE_WHITE для символа '±' без пробелов")
        void shouldReturnClearAdvantageWhiteForSymbolWithoutSpaces() {
            MoveAnnotation result = MoveAnnotation.fromSymbol("±");
            assertThat(result).isEqualTo(MoveAnnotation.CLEAR_ADVANTAGE_WHITE);
        }

        @Test
        @DisplayName("Должен возвращать null для null")
        void shouldReturnNullForNull() {
            MoveAnnotation.fromSymbol(null);
            assertThat((MoveAnnotation) null).isNull();
        }

        @Test
        @DisplayName("Должен возвращать null для пустой строки")
        void shouldReturnNullForEmptyString() {
            MoveAnnotation result = MoveAnnotation.fromSymbol("");
            assertThat(result).isNull();
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ VALUES()
    // ============================================================

    @Nested
    @DisplayName("values() - Получение всех значений")
    class ValuesTests {

        @Test
        @DisplayName("Должен возвращать все значения аннотаций")
        void shouldReturnAllAnnotationValues() {
            MoveAnnotation[] values = MoveAnnotation.values();
            assertThat(values).isNotEmpty();
            assertThat(values).contains(
                    MoveAnnotation.GOOD_MOVE,
                    MoveAnnotation.BLUNDER,
                    MoveAnnotation.CHECK,
                    MoveAnnotation.MATE
            );
        }

        @Test
        @DisplayName("Должен содержать все ожидаемые аннотации")
        void shouldContainAllExpectedAnnotations() {
            MoveAnnotation[] values = MoveAnnotation.values();

            // Проверяем наличие всех основных аннотаций
            assertThat(values).contains(
                    MoveAnnotation.BRILLIANT_MOVE,
                    MoveAnnotation.GOOD_MOVE,
                    MoveAnnotation.INTERESTING_MOVE,
                    MoveAnnotation.DUBIOUS_MOVE,
                    MoveAnnotation.BAD_MOVE,
                    MoveAnnotation.BLUNDER,
                    MoveAnnotation.CHECK,
                    MoveAnnotation.DOUBLE_CHECK,
                    MoveAnnotation.MATE
            );
        }
    }

    // ============================================================
    // 6. ТЕСТЫ ДЛЯ valueOf()
    // ============================================================

    @Nested
    @DisplayName("valueOf() - Получение аннотации по имени")
    class ValueOfTests {

        @Test
        @DisplayName("Должен возвращать GOOD_MOVE по имени 'GOOD_MOVE'")
        void shouldReturnGoodMoveByName() {
            MoveAnnotation result = MoveAnnotation.valueOf("GOOD_MOVE");
            assertThat(result).isEqualTo(MoveAnnotation.GOOD_MOVE);
        }

        @Test
        @DisplayName("Должен возвращать CHECK по имени 'CHECK'")
        void shouldReturnCheckByName() {
            MoveAnnotation result = MoveAnnotation.valueOf("CHECK");
            assertThat(result).isEqualTo(MoveAnnotation.CHECK);
        }

        @Test
        @DisplayName("Должен выбрасывать исключение для несуществующего имени")
        void shouldThrowExceptionForInvalidName() {
            assertThatThrownBy(() -> MoveAnnotation.valueOf("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ============================================================
    // 7. ТЕСТЫ ДЛЯ name() И ordinal()
    // ============================================================

    @Nested
    @DisplayName("name() и ordinal() - Методы Enum")
    class NameAndOrdinalTests {

        @Test
        @DisplayName("name() должен возвращать имя константы")
        void shouldReturnName() {
            assertThat(MoveAnnotation.GOOD_MOVE.name()).isEqualTo("GOOD_MOVE");
            assertThat(MoveAnnotation.CHECK.name()).isEqualTo("CHECK");
        }

        @Test
        @DisplayName("ordinal() должен отражать порядок объявления")
        void shouldReturnOrdinal() {
            // Проверяем, что CHECK имеет больший ordinal, чем GOOD_MOVE
            assertThat(MoveAnnotation.CHECK.ordinal())
                    .isGreaterThan(MoveAnnotation.GOOD_MOVE.ordinal());

            // Проверяем, что MATE имеет больший ordinal, чем CHECK
            assertThat(MoveAnnotation.MATE.ordinal())
                    .isGreaterThan(MoveAnnotation.CHECK.ordinal());
        }
    }

    // ============================================================
    // 8. ТЕСТЫ ДЛЯ toString() И equals()
    // ============================================================

    @Nested
    @DisplayName("toString() и equals() - Строковое представление и сравнение")
    class ToStringAndEqualsTests {

        @Test
        @DisplayName("toString() должен возвращать имя константы")
        void toStringShouldReturnName() {
            assertThat(MoveAnnotation.GOOD_MOVE.toString()).isEqualTo("GOOD_MOVE");
            assertThat(MoveAnnotation.CHECK.toString()).isEqualTo("CHECK");
        }

        @Test
        @DisplayName("Должен быть равным самому себе")
        void shouldBeEqualToItself() {
            MoveAnnotation annotation = MoveAnnotation.GOOD_MOVE;
            assertThat(annotation).isEqualTo(annotation);
        }

        @Test
        @DisplayName("Должен быть равным другому объекту с тем же значением")
        void shouldBeEqualToSameValue() {
            MoveAnnotation ann1 = MoveAnnotation.GOOD_MOVE;
            MoveAnnotation ann2 = MoveAnnotation.GOOD_MOVE;
            assertThat(ann1).isEqualTo(ann2);
        }

        @Test
        @DisplayName("Должен иметь одинаковый hashCode для одинаковых значений")
        void shouldHaveSameHashCodeForSameValues() {
            MoveAnnotation ann1 = MoveAnnotation.GOOD_MOVE;
            MoveAnnotation ann2 = MoveAnnotation.GOOD_MOVE;
            assertThat(ann1.hashCode()).isEqualTo(ann2.hashCode());
        }
    }
}