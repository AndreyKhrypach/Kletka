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

package Khrypach.Andrey.chess.kletka.database.formatter;

import Khrypach.Andrey.chess.kletka.database.model.GameData;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PgnFormatter - Форматирование PGN")
class PgnFormatterTest {

    private PgnFormatter formatter;
    private final LanguageManager lang = LanguageManager.getInstance();

    @BeforeEach
    void setUp() {
        formatter = new PgnFormatter();
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ format()
    // ============================================================

    @Nested
    @DisplayName("format() - Форматирование GameData в PGN")
    class FormatTests {

        @Test
        @DisplayName("Должен возвращать пустую строку для null")
        void shouldReturnEmptyForNull() {
            // when
            String result = formatter.format(null);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Должен возвращать существующий PGN если он есть")
        void shouldReturnExistingPgn() {
            // given
            String existingPgn = """
                    [Event "Kletka Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    """;
            GameData game = createGameDataWithPgn(existingPgn);

            // when
            String result = formatter.format(game);

            // then
            assertThat(result).isEqualTo(existingPgn);
        }

        @Test
        @DisplayName("Должен создавать PGN из GameData без PGN тела")
        void shouldCreatePgnFromGameData() {
            // given
            GameData game = createGameData();

            // when
            String result = formatter.format(game);

            // then
            assertThat(result).contains("[Event \"Kletka Game\"]");
            assertThat(result).contains("[White \"Carlsen\"]");
            assertThat(result).contains("[Black \"Nepomniachtchi\"]");
            assertThat(result).contains("[Result \"1-0\"]");
            assertThat(result).contains("[Date \"2024.01.01\"]");
            assertThat(result).contains("[ECO \"C67\"]");
            assertThat(result).contains("[Opening \"Ruy Lopez\"]");
            assertThat(result).contains("[WhiteElo \"2800\"]");
            assertThat(result).contains("[BlackElo \"2750\"]");
        }

        @Test
        @DisplayName("Должен добавлять только непустые заголовки")
        void shouldAddOnlyNonEmptyHeaders() {
            // given
            GameData game = createMinimalGameData();

            // when
            String result = formatter.format(game);

            // then
            assertThat(result).contains("[Event \"Minimal Game\"]");
            assertThat(result).contains("[White " + "\"" + lang.get(LanguageKeys.DEFAULT_PLAYER_NAME) + " 1\"]");
            assertThat(result).contains("[Black " + "\"" + lang.get(LanguageKeys.DEFAULT_PLAYER_NAME) + " 2\"]");
            assertThat(result).contains("[Result \"1-0\"]");
            // Этих заголовков не должно быть (пустые или "?")
            assertThat(result).doesNotContain("[ECO");
            assertThat(result).doesNotContain("[Opening");
            assertThat(result).doesNotContain("[Variation");
        }

        @Test
        @DisplayName("Должен корректно форматировать дату")
        void shouldFormatDateCorrectly() {
            // given
            GameData game = createGameDataWithDate(LocalDate.of(2024, 12, 25));

            // when
            String result = formatter.format(game);

            // then
            assertThat(result).contains("[Date \"2024.12.25\"]");
        }

        @Test
        @DisplayName("Должен использовать значения по умолчанию для отсутствующих полей")
        void shouldUseDefaultValuesForMissingFields() {
            // given
            GameData game = createGameDataWithMissingFields();

            // when
            String result = formatter.format(game);

            // then
            // Проверяем, что используются значения по умолчанию
            assertThat(result).contains("[Event \"?\"]");
            assertThat(result).contains("[Site \"?\"]");
            assertThat(result).contains("[Round \"?\"]");
            // Игроки получают значение "Игрок" из LanguageManager
            assertThat(result).contains("[White " + "\"" + lang.get(LanguageKeys.DEFAULT_PLAYER_NAME) + "\"]");
            assertThat(result).contains("[Black " + "\"" + lang.get(LanguageKeys.DEFAULT_PLAYER_NAME) + "\"]");
        }

        @Test
        @DisplayName("Должен добавлять SetUp если isSetUp true")
        void shouldAddSetUpIfTrue() {
            // given
            GameData game = createGameDataWithSetUp(true, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

            // when
            String result = formatter.format(game);

            // then
            assertThat(result).contains("[SetUp \"1\"]");
            assertThat(result).contains("[FEN \"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1\"]");
        }

        @Test
        @DisplayName("Должен добавлять SetUp как 0 если isSetUp false")
        void shouldAddSetUpAsZeroIfFalse() {
            // given
            GameData game = createGameDataWithSetUp(false, "");

            // when
            String result = formatter.format(game);

            // then
            assertThat(result).contains("[SetUp \"0\"]");
        }

        @Test
        @DisplayName("Должен добавлять PositionType если не 'game'")
        void shouldAddPositionTypeIfNotGame() {
            // given
            GameData game = createGameDataWithPositionType(lang.get(LanguageKeys.GAME_TYPE_POSITION));

            // when
            String result = formatter.format(game);

            // then
            assertThat(result).contains("[PositionType " + "\"" + lang.get(LanguageKeys.GAME_TYPE_POSITION) + "\"" + "]");
        }

        @Test
        @DisplayName("Не должен добавлять PositionType если 'game'")
        void shouldNotAddPositionTypeIfGame() {
            // given
            GameData game = createGameDataWithPositionType(lang.get(LanguageKeys.GAME_TYPE_GAME));

            // when
            String result = formatter.format(game);

            // then
            assertThat(result).doesNotContain("[PositionType");
        }
    }

    // ============================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ============================================================

    private GameData createGameData() {
        return new GameData(
                "Carlsen",
                "Nepomniachtchi",
                "1-0",
                "2800",
                "2750",
                "Kletka Game",
                "Dubai",
                "1",
                "1.1",
                LocalDate.of(2024, 1, 1),
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
                "",
                "",
                false,
                "game",
                false
        );
    }

    private GameData createGameDataWithPgn(String pgn) {
        return new GameData(
                "Player 1",
                "Player 2",
                "1-0",
                "?",
                "?",
                "Kletka Game",
                "?",
                "?",
                "?",
                LocalDate.now(),
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                pgn,
                "",
                false,
                "game",
                false
        );
    }

    private GameData createMinimalGameData() {
        return new GameData(
                lang.get(LanguageKeys.DEFAULT_PLAYER_NAME) + " 1",
                lang.get(LanguageKeys.DEFAULT_PLAYER_NAME) + " 2",
                "1-0",
                "?",
                "?",
                "Minimal Game",
                "?",
                "?",
                "?",
                LocalDate.now(),
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "",
                "",
                false,
                "game",
                false
        );
    }

    private GameData createGameDataWithDate(LocalDate date) {
        return new GameData(
                lang.get(LanguageKeys.DEFAULT_PLAYER_NAME) + " 1",
                lang.get(LanguageKeys.DEFAULT_PLAYER_NAME) + " 2",
                "1-0",
                "?",
                "?",
                "Event",
                "Site",
                "Round",
                "?",
                date,
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "",
                "",
                false,
                "game",
                false
        );
    }

    private GameData createGameDataWithMissingFields() {
        return new GameData(
                null,
                null,
                "*",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "",
                "",
                false,
                "game",
                false
        );
    }

    private GameData createGameDataWithSetUp(boolean isSetUp, String fen) {
        return new GameData(
                lang.get(LanguageKeys.DEFAULT_PLAYER_NAME) + " 1",
                lang.get(LanguageKeys.DEFAULT_PLAYER_NAME) + " 2",
                "1-0",
                "?",
                "?",
                "Position",
                "?",
                "?",
                "?",
                LocalDate.now(),
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "",
                fen,
                isSetUp,
                "position",
                false
        );
    }

    private GameData createGameDataWithPositionType(String positionType) {
        return new GameData(
                lang.get(LanguageKeys.DEFAULT_PLAYER_NAME) + " 1",
                lang.get(LanguageKeys.DEFAULT_PLAYER_NAME) + " 2",
                "*",
                "?",
                "?",
                "Position",
                "?",
                "?",
                "?",
                LocalDate.now(),
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "",
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                true,
                positionType,
                false
        );
    }
}