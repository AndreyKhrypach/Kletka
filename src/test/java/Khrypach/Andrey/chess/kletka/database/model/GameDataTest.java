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
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GameData - Данные партии (record)")
class GameDataTest {

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ КОНСТРУКТОРА И НОРМАЛИЗАЦИИ
    // ============================================================

    @Nested
    @DisplayName("Конструктор и нормализация полей")
    class ConstructorAndNormalizationTests {

        @Test
        @DisplayName("Должен нормализовать null и пустые строки")
        void shouldNormalizeNullAndEmptyStrings() {
            // given
            GameData gameData = givenData();

            // then
            assertThat(gameData.whitePlayer()).isEqualTo(LanguageManager.getInstance().get(LanguageKeys.DEFAULT_PLAYER_NAME));
            assertThat(gameData.blackPlayer()).isEqualTo(LanguageManager.getInstance().get(LanguageKeys.DEFAULT_PLAYER_NAME));
            assertThat(gameData.result()).isEqualTo("*");
            assertThat(gameData.whiteElo()).isEqualTo("?");
            assertThat(gameData.blackElo()).isEqualTo("?");
            assertThat(gameData.event()).isEqualTo("?");
            assertThat(gameData.site()).isEqualTo("?");
            assertThat(gameData.round()).isEqualTo("?");
            assertThat(gameData.subround()).isEqualTo("?");
            assertThat(gameData.eco()).isEqualTo("?");
            assertThat(gameData.opening()).isEqualTo("?");
            assertThat(gameData.variation()).isEqualTo("?");
            assertThat(gameData.annotator()).isEqualTo("?");
            assertThat(gameData.whiteTeam()).isEqualTo("?");
            assertThat(gameData.blackTeam()).isEqualTo("?");
            assertThat(gameData.source()).isEqualTo("?");
            assertThat(gameData.whiteFideId()).isEqualTo("?");
            assertThat(gameData.blackFideId()).isEqualTo("?");
            assertThat(gameData.timeControl()).isEqualTo("?");
            assertThat(gameData.plyCount()).isEqualTo("?");
            assertThat(gameData.pgn()).isEmpty();
            assertThat(gameData.fen()).isEmpty();
            assertThat(gameData.positionType()).isEqualTo("game");
            assertThat(gameData.date()).isNotNull();
        }

        private static GameData givenData() {
            String black = "";
            String result = "   ";
            String empty = "";

            return new GameData(
                    null, black, result,
                    null, null,
                    empty, empty, empty, empty, null,
                    empty, empty, empty,
                    empty, empty, empty, empty,
                    empty, empty, empty, empty,
                    empty,
                    null, false, null, false
            );
        }

        @Test
        @DisplayName("Должен устанавливать текущую дату при null")
        void shouldSetCurrentDateWhenNull() {
            // when
            GameData gameData = new GameData(
                    "White", "Black", "*",
                    "?", "?",
                    "Event", "Site", "1", "1", null,
                    "?", "?", "?",
                    "?", "?", "?", "?",
                    "?", "?", "?", "?",
                    "",
                    "", false, "game", false
            );

            // then
            assertThat(gameData.date()).isNotNull();
            assertThat(gameData.date()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("Должен сохранять корректные значения без нормализации")
        void shouldKeepValidValuesWithoutNormalization() {
            // given
            String white = "Carlsen";
            String black = "Nepomniachtchi";
            String result = "1-0";
            LocalDate date = LocalDate.of(2021, 12, 1);

            // when
            GameData gameData = new GameData(
                    white, black, result,
                    "2800", "2750",
                    "World Championship", "Dubai", "1", "1.1", date,
                    "C67", "Ruy Lopez", "Berlin Defense",
                    "Annotator", "Team White", "Team Black", "Source",
                    "123456", "654321", "40/120", "40",
                    "1. e4 e5",
                    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                    true,
                    "position",
                    false
            );

            // then
            assertThat(gameData.whitePlayer()).isEqualTo(white);
            assertThat(gameData.blackPlayer()).isEqualTo(black);
            assertThat(gameData.result()).isEqualTo(result);
            assertThat(gameData.whiteElo()).isEqualTo("2800");
            assertThat(gameData.blackElo()).isEqualTo("2750");
            assertThat(gameData.event()).isEqualTo("World Championship");
            assertThat(gameData.site()).isEqualTo("Dubai");
            assertThat(gameData.round()).isEqualTo("1");
            assertThat(gameData.subround()).isEqualTo("1.1");
            assertThat(gameData.date()).isEqualTo(date);
            assertThat(gameData.eco()).isEqualTo("C67");
            assertThat(gameData.opening()).isEqualTo("Ruy Lopez");
            assertThat(gameData.variation()).isEqualTo("Berlin Defense");
            assertThat(gameData.annotator()).isEqualTo("Annotator");
            assertThat(gameData.whiteTeam()).isEqualTo("Team White");
            assertThat(gameData.blackTeam()).isEqualTo("Team Black");
            assertThat(gameData.source()).isEqualTo("Source");
            assertThat(gameData.whiteFideId()).isEqualTo("123456");
            assertThat(gameData.blackFideId()).isEqualTo("654321");
            assertThat(gameData.timeControl()).isEqualTo("40/120");
            assertThat(gameData.plyCount()).isEqualTo("40");
            assertThat(gameData.pgn()).isEqualTo("1. e4 e5");
            assertThat(gameData.fen()).isEqualTo("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
            assertThat(gameData.isSetUp()).isTrue();
            assertThat(gameData.positionType()).isEqualTo("position");
            assertThat(gameData.deleted()).isFalse();
        }

        @Test
        @DisplayName("Должен создавать GameData через конструктор обратной совместимости")
        void shouldCreateGameDataViaCompatibilityConstructor() {
            // given
            String white = "Carlsen";
            String black = "Nepomniachtchi";
            String whiteElo = "2800";
            String blackElo = "2750";
            String event = "World Championship";
            String site = "Dubai";
            String round = "1";
            String pgn = "1. e4 e5 2. Nf3 Nc6";

            // when
            GameData gameData = new GameData(white, black, whiteElo, blackElo, event, site, round, pgn);

            // then
            assertThat(gameData.whitePlayer()).isEqualTo(white);
            assertThat(gameData.blackPlayer()).isEqualTo(black);
            assertThat(gameData.whiteElo()).isEqualTo(whiteElo);
            assertThat(gameData.blackElo()).isEqualTo(blackElo);
            assertThat(gameData.event()).isEqualTo(event);
            assertThat(gameData.site()).isEqualTo(site);
            assertThat(gameData.round()).isEqualTo(round);
            assertThat(gameData.pgn()).isEqualTo(pgn);
            assertThat(gameData.result()).isEqualTo("*");
            assertThat(gameData.date()).isEqualTo(LocalDate.now());
            assertThat(gameData.eco()).isEqualTo("?");
            assertThat(gameData.opening()).isEqualTo("?");
            assertThat(gameData.positionType()).isEqualTo("game");
            assertThat(gameData.isSetUp()).isFalse();
            assertThat(gameData.deleted()).isFalse();
        }

        @Test
        @DisplayName("Должен нормализовать whitePlayer и blackPlayer отдельно")
        void shouldNormalizeWhiteAndBlackPlayersSeparately() {
            // given
            String white = "";
            String black = null;

            // when
            GameData gameData = new GameData(
                    white, black, "*",
                    "?", "?",
                    "Event", "Site", "1", "1", LocalDate.now(),
                    "?", "?", "?",
                    "?", "?", "?", "?",
                    "?", "?", "?", "?",
                    "",
                    "", false, "game", false
            );

            // then
            String defaultName = LanguageManager.getInstance().get(LanguageKeys.DEFAULT_PLAYER_NAME);
            assertThat(gameData.whitePlayer()).isEqualTo(defaultName);
            assertThat(gameData.blackPlayer()).isEqualTo(defaultName);
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ ОПРЕДЕЛЕНИЯ ТИПА
    // ============================================================

    @Nested
    @DisplayName("Определение типа контента")
    class ContentTypeTests {

        @Test
        @DisplayName("isPosition() должен возвращать false для игры")
        void shouldReturnFalseForGame() {
            // given
            GameData gameData = createTestGameData("game");

            // then
            assertThat(gameData.isPosition()).isFalse();
        }

        @Test
        @DisplayName("isPosition() должен возвращать true для позиции")
        void shouldReturnTrueForPosition() {
            // given
            GameData gameData = createTestGameData("position");

            // then
            assertThat(gameData.isPosition()).isTrue();
        }

        @Test
        @DisplayName("isPosition() должен возвращать true для этюда")
        void shouldReturnTrueForStudy() {
            // given
            GameData gameData = createTestGameData("study");

            // then
            assertThat(gameData.isPosition()).isTrue();
        }

        @Test
        @DisplayName("isPosition() должен возвращать true для задачи")
        void shouldReturnTrueForProblem() {
            // given
            GameData gameData = createTestGameData("problem");

            // then
            assertThat(gameData.isPosition()).isTrue();
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ getTypeDisplay()
    // ============================================================

    @Nested
    @DisplayName("getTypeDisplay() - Отображение типа")
    class TypeDisplayTests {

        @Test
        @DisplayName("Должен возвращать правильное отображение для игры")
        void shouldReturnCorrectDisplayForGame() {
            // given
            GameData gameData = createTestGameData("game");

            // when
            String display = gameData.getTypeDisplay();

            // then
            assertThat(display).contains("♟");
            assertThat(display).contains(LanguageManager.getInstance().get(LanguageKeys.GAME_TYPE_GAME));
        }

        @Test
        @DisplayName("Должен возвращать правильное отображение для позиции")
        void shouldReturnCorrectDisplayForPosition() {
            // given
            GameData gameData = createTestGameData("position");

            // when
            String display = gameData.getTypeDisplay();

            // then
            assertThat(display).contains("◇");
            assertThat(display).contains(LanguageManager.getInstance().get(LanguageKeys.GAME_TYPE_POSITION));
        }

        @Test
        @DisplayName("Должен возвращать правильное отображение для этюда")
        void shouldReturnCorrectDisplayForStudy() {
            // given
            GameData gameData = createTestGameData("study");

            // when
            String display = gameData.getTypeDisplay();

            // then
            assertThat(display).contains("📖");
            assertThat(display).contains(LanguageManager.getInstance().get(LanguageKeys.GAME_TYPE_STUDY));
        }

        @Test
        @DisplayName("Должен возвращать правильное отображение для задачи")
        void shouldReturnCorrectDisplayForProblem() {
            // given
            GameData gameData = createTestGameData("problem");

            // when
            String display = gameData.getTypeDisplay();

            // then
            assertThat(display).contains("🧩");
            assertThat(display).contains(LanguageManager.getInstance().get(LanguageKeys.GAME_TYPE_PROBLEM));
        }

        @Test
        @DisplayName("Должен возвращать игру для неизвестного типа")
        void shouldReturnGameForUnknownType() {
            // given
            GameData gameData = createTestGameData("unknown");

            // when
            String display = gameData.getTypeDisplay();

            // then
            assertThat(display).contains("♟");
            assertThat(display).contains(LanguageManager.getInstance().get(LanguageKeys.GAME_TYPE_GAME));
        }
    }

    // ============================================================
    // 4. ТЕСТЫ ДЛЯ ГЕТТЕРОВ (проверка, что record работает)
    // ============================================================

    @Nested
    @DisplayName("Геттеры - Получение полей")
    class GettersTests {

        @Test
        @DisplayName("Должен возвращать все поля корректно")
        void shouldReturnAllFieldsCorrectly() {
            // given
            String white = "Carlsen";
            String black = "Nepomniachtchi";
            String result = "1-0";
            String eco = "C67";
            String opening = "Ruy Lopez";
            String pgn = "1. e4 e5";
            LocalDate date = LocalDate.of(2021, 12, 1);

            GameData gameData = new GameData(
                    white, black, result,
                    "2800", "2750",
                    "Event", "Site", "1", "1.1", date,
                    eco, opening, "Berlin",
                    "Annotator", "Team W", "Team B", "Source",
                    "123", "321", "40/120", "40",
                    pgn,
                    "fen", true, "game", false
            );

            // then
            assertThat(gameData.whitePlayer()).isEqualTo(white);
            assertThat(gameData.blackPlayer()).isEqualTo(black);
            assertThat(gameData.result()).isEqualTo(result);
            assertThat(gameData.eco()).isEqualTo(eco);
            assertThat(gameData.opening()).isEqualTo(opening);
            assertThat(gameData.pgn()).isEqualTo(pgn);
            assertThat(gameData.date()).isEqualTo(date);
            assertThat(gameData.isSetUp()).isTrue();
            assertThat(gameData.deleted()).isFalse();
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ equals() И hashCode()
    // ============================================================

    @Nested
    @DisplayName("equals() и hashCode()")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Должен считать равными объекты с одинаковыми полями")
        void shouldBeEqualWithSameFields() {
            // given
            GameData gameData1 = createFullGameData();
            GameData gameData2 = createFullGameData();

            // then
            assertThat(gameData1).isEqualTo(gameData2);
            assertThat(gameData1.hashCode()).isEqualTo(gameData2.hashCode());
        }

        @Test
        @DisplayName("Должен считать разными объекты с разными полями")
        void shouldNotBeEqualWithDifferentFields() {
            // given
            GameData gameData1 = createFullGameData();
            GameData gameData2 = createFullGameDataWithDifferentWhite();

            // then
            assertThat(gameData1).isNotEqualTo(gameData2);
            assertThat(gameData1.hashCode()).isNotEqualTo(gameData2.hashCode());
        }

        @Test
        @DisplayName("Должен возвращать false при сравнении с null")
        void shouldReturnFalseWhenComparedWithNull() {
            // given
            GameData gameData = createFullGameData();

            // then
            assertThat(gameData).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Должен возвращать true при сравнении с самим собой")
        void shouldReturnTrueWhenComparedWithItself() {
            // given
            GameData gameData = createFullGameData();

            // then
            assertThat(gameData).isEqualTo(gameData);
        }
    }

    // ============================================================
    // 6. ТЕСТЫ ДЛЯ toString()
    // ============================================================

    @Nested
    @DisplayName("toString() - Строковое представление")
    class ToStringTests {

        @Test
        @DisplayName("Должен возвращать строковое представление record")
        void shouldReturnRecordToString() {
            // given
            GameData gameData = createFullGameData();

            // when
            String toString = gameData.toString();

            // then
            assertThat(toString).contains("GameData");
            assertThat(toString).contains("Carlsen");
            assertThat(toString).contains("Nepomniachtchi"); // Теперь будет правильное имя
            assertThat(toString).contains("1-0");
            assertThat(toString).contains("C67");
        }
    }

    // ============================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ============================================================

    private GameData createTestGameData(String positionType) {
        return new GameData(
                "White", "Black", "*",
                "?", "?",
                "Event", "Site", "1", "1", LocalDate.now(),
                "?", "?", "?",
                "?", "?", "?", "?",
                "?", "?", "?", "?",
                "",
                "", false, positionType, false
        );
    }

    private GameData createFullGameData() {
        return new GameData(
                "Carlsen",
                "Nepomniachtchi",
                "1-0",
                "2800",
                "2750",
                "World Championship",
                "Dubai",
                "1",
                "1.1",
                LocalDate.of(2021, 12, 1),
                "C67",
                "Ruy Lopez",
                "Berlin Defense",
                "Annotator",
                "Team White",
                "Team Black",
                "Source",
                "123456",
                "654321",
                "40/120",
                "40",
                "1. e4 e5 2. Nf3 Nc6",
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                true,
                "game",
                false
        );
    }

    private GameData createFullGameDataWithDifferentWhite() {
        return new GameData(
                "Karpov",
                "Nepomniachtchi",
                "1-0",
                "2800",
                "2750",
                "World Championship",
                "Dubai",
                "1",
                "1.1",
                LocalDate.of(2021, 12, 1),
                "C67",
                "Ruy Lopez",
                "Berlin Defense",
                "Annotator",
                "Team White",
                "Team Black",
                "Source",
                "123456",
                "654321",
                "40/120",
                "40",
                "1. e4 e5 2. Nf3 Nc6",
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                true,
                "game",
                false
        );
    }
}