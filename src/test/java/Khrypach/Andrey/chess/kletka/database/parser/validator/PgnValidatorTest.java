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

package Khrypach.Andrey.chess.kletka.database.parser.validator;

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PgnValidator - Валидатор PGN")
class PgnValidatorTest {

    private PgnValidator validator;
    private final LanguageManager lang =  LanguageManager.getInstance();


    @BeforeEach
    void setUp() {
        validator = new PgnValidator();
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ isPositionPgn()
    // ============================================================

    @Nested
    @DisplayName("isPositionPgn() - Определение позиции")
    class IsPositionPgnTests {

        @Test
        @DisplayName("Должен возвращать true для PGN с SetUp и FEN")
        void shouldReturnTrueForPgnWithSetUpAndFen() {
            // given
            String pgn = """
                    [Event "Position"]
                    [SetUp "1"]
                    [FEN "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"]
                    """;

            // when
            boolean result = validator.isPositionPgn(pgn);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Должен возвращать true для PGN только с FEN")
        void shouldReturnTrueForPgnWithFenOnly() {
            // given
            String pgn = """
                    [Event "Position"]
                    [FEN "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"]
                    """;

            // when
            boolean result = validator.isPositionPgn(pgn);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Должен возвращать false для обычной партии")
        void shouldReturnFalseForRegularGame() {
            // given
            String pgn = """
                    [Event "Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "*"]
                    
                    1. e4 e5 *
                    """;

            // when
            boolean result = validator.isPositionPgn(pgn);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Должен возвращать false для null")
        void shouldReturnFalseForNull() {
            // when
            boolean result = validator.isPositionPgn(null);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Должен возвращать false для пустой строки")
        void shouldReturnFalseForEmptyString() {
            // when
            boolean result = validator.isPositionPgn("");

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Должен возвращать true для PGN с SetUp без FEN")
        void shouldReturnTrueForPgnWithSetUpOnly() {
            // given
            String pgn = """
                    [Event "Position"]
                    [SetUp "1"]
                    """;

            // when
            boolean result = validator.isPositionPgn(pgn);

            // then
            assertThat(result).isTrue(); // содержит [SetUp "1"]
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ detectContentType()
    // ============================================================

    @Nested
    @DisplayName("detectContentType() - Определение типа контента")
    class DetectContentTypeTests {

        @Test
        @DisplayName("Должен определять обычную игру")
        void shouldDetectGame() {
            // given
            String pgn = """
                    [Event "Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "*"]
                    
                    1. e4 e5 *
                    """;

            // when
            String type = validator.detectContentType(pgn);

            // then
            assertThat(type).isEqualTo(lang.get(LanguageKeys.GAME_TYPE_GAME));
        }

        @Test
        @DisplayName("Должен определять позицию")
        void shouldDetectPosition() {
            // given
            String pgn = """
                    [Event "Position"]
                    [SetUp "1"]
                    [FEN "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"]
                    """;

            // when
            String type = validator.detectContentType(pgn);

            // then
            assertThat(type).isEqualTo(lang.get(LanguageKeys.GAME_TYPE_POSITION));
        }

        @Test
        @DisplayName("Должен определять задачу (мат в N ходов) по EventType")
        void shouldDetectProblemByEventType() {
            // given
            String pgn = """
                    [Event "Problem"]
                    [SetUp "1"]
                    [FEN "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"]
                    [EventType "tourn"]
                    """;

            // when
            String type = validator.detectContentType(pgn);

            // then
            assertThat(type).isEqualTo(lang.get(LanguageKeys.GAME_TYPE_PROBLEM));
        }

        @Test
        @DisplayName("Должен определять задачу по ключевому слову 'мат' в White")
        void shouldDetectProblemByMateKeywordInWhite() {
            // given
            String mateWord = lang.get(LanguageKeys.PGN_KEYWORD_MATE);
            String pgn = """
                    [Event "Position"]
                    [SetUp "1"]
                    [FEN "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"]
                    [White "%s в 2 хода"]
                    """.formatted(mateWord);

            // when
            String type = validator.detectContentType(pgn);

            // then
            assertThat(type).isEqualTo(lang.get(LanguageKeys.GAME_TYPE_PROBLEM));
        }

        @Test
        @DisplayName("Должен определять задачу по ключевому слову 'мат' в Black")
        void shouldDetectProblemByMateKeywordInBlack() {
            // given
            String mateWord = lang.get(LanguageKeys.PGN_KEYWORD_MATE);

            String pgn = """
            [Event "Position"]
            [SetUp "1"]
            [FEN "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"]
            [Black "%s в 2 хода"]
            """.formatted(mateWord);

            // when
            String type = validator.detectContentType(pgn);

            // then
            assertThat(type).isEqualTo(lang.get(LanguageKeys.GAME_TYPE_PROBLEM));
        }

        @Test
        @DisplayName("Должен определять этюд по EventType")
        void shouldDetectStudyByEventType() {
            // given
            String pgn = """
                    [Event "Study"]
                    [SetUp "1"]
                    [FEN "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"]
                    [EventType "study"]
                    """;

            // when
            String type = validator.detectContentType(pgn);

            // then
            assertThat(type).isEqualTo(lang.get(LanguageKeys.GAME_TYPE_STUDY));
        }

        @Test
        @DisplayName("Должен определять этюд по EventType 'eth'")
        void shouldDetectStudyByEthEventType() {
            // given
            String pgn = """
                    [Event "Study"]
                    [SetUp "1"]
                    [FEN "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"]
                    [EventType "eth"]
                    """;

            // when
            String type = validator.detectContentType(pgn);

            // then
            assertThat(type).isEqualTo(lang.get(LanguageKeys.GAME_TYPE_STUDY));
        }

        @Test
        @DisplayName("Должен определять этюд по ключевому слову в White")
        void shouldDetectStudyByKeywordInWhite() {
            // given
            String etud =  lang.get(LanguageKeys.GAME_TYPE_STUDY);
            String pgn = """
                    [Event "Position"]
                    [SetUp "1"]
                    [FEN "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"]
                    [White "%s"]
                    """.formatted(etud);

            // when
            String type = validator.detectContentType(pgn);

            // then
            assertThat(type).isEqualTo(lang.get(LanguageKeys.GAME_TYPE_STUDY));
        }

        @Test
        @DisplayName("Должен определять этюд по ключевому слову в Black")
        void shouldDetectStudyByKeywordInBlack() {
            // given
            String etud = lang.get(LanguageKeys.GAME_TYPE_STUDY);

            String pgn = """
                    [Event "Position"]
                    [SetUp "1"]
                    [FEN "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"]
                    [Black "%s"]
                    """.formatted(etud);

            // when
            String type = validator.detectContentType(pgn);

            // then
            assertThat(type).isEqualTo(lang.get(LanguageKeys.GAME_TYPE_STUDY));
        }

        @Test
        @DisplayName("Должен возвращать 'game' для null")
        void shouldReturnGameForNull() {
            // when
            String type = validator.detectContentType(null);

            // then
            assertThat(type).isEqualTo(lang.get(LanguageKeys.GAME_TYPE_GAME));
        }

        @Test
        @DisplayName("Должен возвращать 'game' для пустой строки")
        void shouldReturnGameForEmptyString() {
            // when
            String type = validator.detectContentType("");

            // then
            assertThat(type).isEqualTo(lang.get(LanguageKeys.GAME_TYPE_GAME));
        }

        @Test
        @DisplayName("Должен определять позицию без SetUp но с FEN")
        void shouldDetectPositionWithFenOnly() {
            // given
            String pgn = """
                    [Event "Position"]
                    [FEN "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"]
                    """;

            // when
            String type = validator.detectContentType(pgn);

            // then
            assertThat(type).isEqualTo(lang.get(LanguageKeys.GAME_TYPE_POSITION));
        }

        @Test
        @DisplayName("Должен определять позицию с SetUp без FEN")
        void shouldDetectPositionWithSetUpOnly() {
            // given
            String pgn = """
                    [Event "Position"]
                    [SetUp "1"]
                    """;

            // when
            String type = validator.detectContentType(pgn);

            // then
            assertThat(type).isEqualTo(lang.get(LanguageKeys.GAME_TYPE_POSITION));
        }

        @Test
        @DisplayName("Должен определять позицию без SetUp и FEN как игру")
        void shouldDetectGameWithoutSetUpAndFen() {
            // given
            String pgn = """
                    [Event "Game"]
                    [White "Player"]
                    [Black "Player"]
                    """;

            // when
            String type = validator.detectContentType(pgn);

            // then
            assertThat(type).isEqualTo(lang.get(LanguageKeys.GAME_TYPE_GAME));
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ ИНТЕГРАЦИИ isPositionPgn И detectContentType
    // ============================================================

    @Nested
    @DisplayName("Интеграция isPositionPgn и detectContentType")
    class IntegrationTests {

        @Test
        @DisplayName("Должен корректно определять позицию обоими методами")
        void shouldDetectPositionByBothMethods() {
            // given
            String pgn = """
                    [Event "Position"]
                    [SetUp "1"]
                    [FEN "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"]
                    """;

            // when
            boolean isPosition = validator.isPositionPgn(pgn);
            String type = validator.detectContentType(pgn);

            // then
            assertThat(isPosition).isTrue();
            assertThat(type).isEqualTo(lang.get(LanguageKeys.GAME_TYPE_POSITION));
        }

        @Test
        @DisplayName("Должен корректно определять игру обоими методами")
        void shouldDetectGameByBothMethods() {
            // given
            String pgn = """
                    [Event "Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "*"]
                    
                    1. e4 e5 *
                    """;

            // when
            boolean isPosition = validator.isPositionPgn(pgn);
            String type = validator.detectContentType(pgn);

            // then
            assertThat(isPosition).isFalse();
            assertThat(type).isEqualTo(lang.get(LanguageKeys.GAME_TYPE_GAME));
        }

        @Test
        @DisplayName("Должен корректно определять задачу обоими методами")
        void shouldDetectProblemByBothMethods() {
            // given
            String pgn = """
                    [Event "Problem"]
                    [SetUp "1"]
                    [FEN "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"]
                    [EventType "tourn"]
                    """;

            // when
            boolean isPosition = validator.isPositionPgn(pgn);
            String type = validator.detectContentType(pgn);

            // then
            assertThat(isPosition).isTrue();
            assertThat(type).isEqualTo(lang.get(LanguageKeys.GAME_TYPE_PROBLEM));
        }
    }
}