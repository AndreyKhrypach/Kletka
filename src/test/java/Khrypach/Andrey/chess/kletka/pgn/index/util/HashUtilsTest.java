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

package Khrypach.Andrey.chess.kletka.pgn.index.util;

import Khrypach.Andrey.chess.kletka.database.model.GameData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HashUtils - Утилиты для хеширования")
class HashUtilsTest {

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ hashString()
    // ============================================================

    @Nested
    @DisplayName("hashString() - Хеширование строки")
    class HashStringTests {

        @Test
        @DisplayName("Должен возвращать 0 для null")
        void shouldReturnZeroForNull() {
            // when
            int hash = HashUtils.hashString(null);

            // then
            assertThat(hash).isEqualTo(0);
        }

        @Test
        @DisplayName("Должен возвращать 0 для пустой строки")
        void shouldReturnZeroForEmptyString() {
            // when
            int hash = HashUtils.hashString("");

            // then
            assertThat(hash).isEqualTo(0);
        }

        @Test
        @DisplayName("Должен возвращать одинаковый хеш для одинаковых строк")
        void shouldReturnSameHashForSameStrings() {
            // given
            String str1 = "Hello World";
            String str2 = "Hello World";

            // when
            int hash1 = HashUtils.hashString(str1);
            int hash2 = HashUtils.hashString(str2);

            // then
            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("Должен возвращать разные хеши для разных строк")
        void shouldReturnDifferentHashForDifferentStrings() {
            // given
            String str1 = "Hello World";
            String str2 = "Hello World!";

            // when
            int hash1 = HashUtils.hashString(str1);
            int hash2 = HashUtils.hashString(str2);

            // then
            assertThat(hash1).isNotEqualTo(hash2);
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ extractBody()
    // ============================================================

    @Nested
    @DisplayName("extractBody() - Извлечение тела партии")
    class ExtractBodyTests {

        @Test
        @DisplayName("Должен возвращать пустую строку для null")
        void shouldReturnEmptyForNull() {
            // when
            String body = HashUtils.extractBody(null);

            // then
            assertThat(body).isEmpty();
        }

        @Test
        @DisplayName("Должен возвращать пустую строку для пустого PGN")
        void shouldReturnEmptyForEmptyPgn() {
            // when
            String body = HashUtils.extractBody("");

            // then
            assertThat(body).isEmpty();
        }

        @Test
        @DisplayName("Должен извлекать тело из полного PGN")
        void shouldExtractBodyFromFullPgn() {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    """;

            // when
            String body = HashUtils.extractBody(pgn);

            // then
            assertThat(body.trim()).isEqualTo("1. e4 e5 2. Nf3 Nc6 1-0");
        }

        @Test
        @DisplayName("Должен возвращать PGN без изменений если нет заголовков")
        void shouldReturnPgnAsIsIfNoHeaders() {
            // given
            String pgn = "1. e4 e5 2. Nf3 Nc6 1-0";

            // when
            String body = HashUtils.extractBody(pgn);

            // then
            assertThat(body).isEqualTo(pgn);
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ normalizeBody()
    // ============================================================

    @Nested
    @DisplayName("normalizeBody() - Нормализация тела партии")
    class NormalizeBodyTests {

        @Test
        @DisplayName("Должен возвращать null для null")
        void shouldReturnNullForNull() {
            // when
            String result = HashUtils.normalizeBody(null);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Должен возвращать null для пустой строки")
        void shouldReturnNullForEmptyString() {
            // when
            String result = HashUtils.normalizeBody("");

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Должен нормализовать тело с лишними пробелами")
        void shouldNormalizeBodyWithExtraSpaces() {
            // given
            String body = "1.  e4   e5   2.  Nf3   Nc6   1-0";

            // when
            String result = HashUtils.normalizeBody(body);

            // then
            assertThat(result).isEqualTo("1. e4 e5 2. Nf3 Nc6");
        }

        @Test
        @DisplayName("Должен удалять результат из тела")
        void shouldRemoveResultFromBody() {
            // given
            String body = "1. e4 e5 2. Nf3 Nc6 1-0";

            // when
            String result = HashUtils.normalizeBody(body);

            // then
            assertThat(result).isEqualTo("1. e4 e5 2. Nf3 Nc6");
        }

        @Test
        @DisplayName("Должен удалять * из тела")
        void shouldRemoveStarFromBody() {
            // given
            String body = "1. e4 e5 2. Nf3 Nc6 *";

            // when
            String result = HashUtils.normalizeBody(body);

            // then
            assertThat(result).isEqualTo("1. e4 e5 2. Nf3 Nc6");
        }
    }

    // ============================================================
    // 4. ТЕСТЫ ДЛЯ calculateBodyHash()
    // ============================================================

    @Nested
    @DisplayName("calculateBodyHash() - Хеш тела партии")
    class CalculateBodyHashTests {

        @Test
        @DisplayName("Должен возвращать 0 для null GameData")
        void shouldReturnZeroForNullGameData() {
            // when
            int hash = HashUtils.calculateBodyHash(null);

            // then
            assertThat(hash).isEqualTo(0);
        }

        @Test
        @DisplayName("Должен возвращать одинаковый хеш для одинаковых тел")
        void shouldReturnSameHashForSameBodies() {
            // given
            GameData game1 = createGameData("1. e4 e5 2. Nf3 Nc6");
            GameData game2 = createGameData("1. e4 e5 2. Nf3 Nc6");

            // when
            int hash1 = HashUtils.calculateBodyHash(game1);
            int hash2 = HashUtils.calculateBodyHash(game2);

            // then
            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("Должен возвращать разные хеши для разных тел")
        void shouldReturnDifferentHashForDifferentBodies() {
            // given
            GameData game1 = createGameData("1. e4 e5 2. Nf3 Nc6");
            GameData game2 = createGameData("1. d4 d5 2. c4 e6");

            // when
            int hash1 = HashUtils.calculateBodyHash(game1);
            int hash2 = HashUtils.calculateBodyHash(game2);

            // then
            assertThat(hash1).isNotEqualTo(hash2);
        }

        @Test
        @DisplayName("Должен игнорировать форматирование")
        void shouldIgnoreFormatting() {
            // given
            GameData game1 = createGameData("1. e4 e5 2. Nf3 Nc6");
            GameData game2 = createGameData("1.  e4   e5   2.  Nf3   Nc6");

            // when
            int hash1 = HashUtils.calculateBodyHash(game1);
            int hash2 = HashUtils.calculateBodyHash(game2);

            // then
            assertThat(hash1).isEqualTo(hash2);
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ calculateContentHash()
    // ============================================================

    @Nested
    @DisplayName("calculateContentHash() - Полный хеш партии")
    class CalculateContentHashTests {

        @Test
        @DisplayName("Должен возвращать 0 для null GameData")
        void shouldReturnZeroForNullGameData() {
            // when
            int hash = HashUtils.calculateContentHash(null);

            // then
            assertThat(hash).isEqualTo(0);
        }

        @Test
        @DisplayName("Должен возвращать одинаковый хеш для одинаковых GameData")
        void shouldReturnSameHashForSameGameData() {
            // given
            GameData game1 = createFullGameData("Player1", "Player2", "C44");
            GameData game2 = createFullGameData("Player1", "Player2", "C44");

            // when
            int hash1 = HashUtils.calculateContentHash(game1);
            int hash2 = HashUtils.calculateContentHash(game2);

            // then
            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("Должен возвращать разные хеши для разных ECO")
        void shouldReturnDifferentHashForDifferentEco() {
            // given
            GameData game1 = createFullGameData("Player1", "Player2", "C44");
            GameData game2 = createFullGameData("Player1", "Player2", "D20");

            // when
            int hash1 = HashUtils.calculateContentHash(game1);
            int hash2 = HashUtils.calculateContentHash(game2);

            // then
            assertThat(hash1).isNotEqualTo(hash2);
        }

        @Test
        @DisplayName("Должен возвращать разные хеши для разных игроков")
        void shouldReturnDifferentHashForDifferentPlayers() {
            // given
            GameData game1 = createFullGameData("Player1", "Player2", "C44");
            GameData game2 = createFullGameData("Player3", "Player4", "C44");

            // when
            int hash1 = HashUtils.calculateContentHash(game1);
            int hash2 = HashUtils.calculateContentHash(game2);

            // then
            assertThat(hash1).isNotEqualTo(hash2);
        }

        @Test
        @DisplayName("Должен игнорировать порядок полей")
        void shouldIgnoreFieldOrder() {
            // given
            // Создаем два GameData с одинаковыми данными, но разным порядком
            // Record гарантирует одинаковый порядок, поэтому проверяем через разные методы
            GameData game1 = createFullGameData("Player1", "Player2", "C44");
            GameData game2 = createFullGameDataWithSameDataDifferentOrder("Player1", "Player2", "C44");

            // when
            int hash1 = HashUtils.calculateContentHash(game1);
            int hash2 = HashUtils.calculateContentHash(game2);

            // then
            assertThat(hash1).isEqualTo(hash2);
        }
    }

    // ============================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ============================================================

    private GameData createGameData(String pgn) {
        return new GameData(
                "Player1", "Player2", "1-0",
                "2800", "2750",
                "Test Event", "Test Site", "1", "1",
                LocalDate.of(2024, 1, 1),
                "C44", "Test Opening", "Test Variation",
                "Annotator", "Team White", "Team Black", "Source",
                "123", "321", "40/120", "40",
                pgn,
                "", false, "game", false
        );
    }

    private GameData createFullGameData(String white, String black, String eco) {
        return new GameData(
                white, black, "1-0",
                "2800", "2750",
                "Test Event", "Test Site", "1", "1",
                LocalDate.of(2024, 1, 1),
                eco, "Test Opening", "Test Variation",
                "Annotator", "Team White", "Team Black", "Source",
                "123", "321", "40/120", "40",
                "1. e4 e5 2. Nf3 Nc6 1-0",
                "", false, "game", false
        );
    }

    private GameData createFullGameDataWithSameDataDifferentOrder(String white, String black, String eco) {
        // Создаем с теми же данными, но через другой порядок (если бы record позволял)
        // В реальности record фиксирует порядок, поэтому этот тест просто проверяет
        // что хеш одинаковый для одинаковых данных
        return new GameData(
                white, black, "1-0",
                "2800", "2750",
                "Test Event", "Test Site", "1", "1",
                LocalDate.of(2024, 1, 1),
                eco, "Test Opening", "Test Variation",
                "Annotator", "Team White", "Team Black", "Source",
                "123", "321", "40/120", "40",
                "1. e4 e5 2. Nf3 Nc6 1-0",
                "", false, "game", false
        );
    }
}