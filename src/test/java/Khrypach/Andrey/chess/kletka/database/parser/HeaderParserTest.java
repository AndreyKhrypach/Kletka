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

package Khrypach.Andrey.chess.kletka.database.parser;

import Khrypach.Andrey.chess.kletka.database.parser.enums.PgnTokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HeaderParser - Парсинг заголовков PGN")
class HeaderParserTest {

    private HeaderParser headerParser;

    @BeforeEach
    void setUp() {
        headerParser = new HeaderParser();
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ parseHeaders() - ОДИН ЗАГОЛОВОК
    // ============================================================

    @Nested
    @DisplayName("Один заголовок")
    class SingleHeaderTests {

        @Test
        @DisplayName("Должен парсить один заголовок Event")
        void shouldParseSingleEventHeader() {
            // given
            List<PgnToken> tokens = createHeaderTokens("Event", "Kletka Game");
            int[] pos = {0};

            // when
            Map<String, String> headers = headerParser.parseHeaders(tokens, pos);

            // then
            assertThat(headers).hasSize(1);
            assertThat(headers).containsEntry("Event", "Kletka Game");
            assertThat(pos[0]).isEqualTo(4); // [ Event "Kletka Game" ]
        }

        @Test
        @DisplayName("Должен парсить один заголовок White")
        void shouldParseSingleWhiteHeader() {
            // given
            List<PgnToken> tokens = createHeaderTokens("White", "Carlsen");
            int[] pos = {0};

            // when
            Map<String, String> headers = headerParser.parseHeaders(tokens, pos);

            // then
            assertThat(headers).hasSize(1);
            assertThat(headers).containsEntry("White", "Carlsen");
        }

        @Test
        @DisplayName("Должен парсить один заголовок Black")
        void shouldParseSingleBlackHeader() {
            // given
            List<PgnToken> tokens = createHeaderTokens("Black", "Nepomniachtchi");
            int[] pos = {0};

            // when
            Map<String, String> headers = headerParser.parseHeaders(tokens, pos);

            // then
            assertThat(headers).hasSize(1);
            assertThat(headers).containsEntry("Black", "Nepomniachtchi");
        }

        @Test
        @DisplayName("Должен парсить один заголовок Result")
        void shouldParseSingleResultHeader() {
            // given
            List<PgnToken> tokens = createHeaderTokens("Result", "1-0");
            int[] pos = {0};

            // when
            Map<String, String> headers = headerParser.parseHeaders(tokens, pos);

            // then
            assertThat(headers).hasSize(1);
            assertThat(headers).containsEntry("Result", "1-0");
        }

        @Test
        @DisplayName("Должен парсить один заголовок ECO")
        void shouldParseSingleEcoHeader() {
            // given
            List<PgnToken> tokens = createHeaderTokens("ECO", "C67");
            int[] pos = {0};

            // when
            Map<String, String> headers = headerParser.parseHeaders(tokens, pos);

            // then
            assertThat(headers).hasSize(1);
            assertThat(headers).containsEntry("ECO", "C67");
        }

        @Test
        @DisplayName("Должен парсить один заголовок Opening")
        void shouldParseSingleOpeningHeader() {
            // given
            List<PgnToken> tokens = createHeaderTokens("Opening", "Ruy Lopez");
            int[] pos = {0};

            // when
            Map<String, String> headers = headerParser.parseHeaders(tokens, pos);

            // then
            assertThat(headers).hasSize(1);
            assertThat(headers).containsEntry("Opening", "Ruy Lopez");
        }

        @Test
        @DisplayName("Должен парсить один заголовок FEN")
        void shouldParseSingleFenHeader() {
            // given
            String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
            List<PgnToken> tokens = createHeaderTokens("FEN", fen);
            int[] pos = {0};

            // when
            Map<String, String> headers = headerParser.parseHeaders(tokens, pos);

            // then
            assertThat(headers).hasSize(1);
            assertThat(headers).containsEntry("FEN", fen);
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ parseHeaders() - НЕСКОЛЬКО ЗАГОЛОВКОВ
    // ============================================================

    @Nested
    @DisplayName("Несколько заголовков")
    class MultipleHeadersTests {

        @Test
        @DisplayName("Должен парсить несколько заголовков")
        void shouldParseMultipleHeaders() {
            // given
            List<PgnToken> tokens = new ArrayList<>();
            // [Event "Kletka Game"]
            tokens.addAll(createHeaderTokenList("Event", "Kletka Game"));
            // [White "Carlsen"]
            tokens.addAll(createHeaderTokenList("White", "Carlsen"));
            // [Black "Nepomniachtchi"]
            tokens.addAll(createHeaderTokenList("Black", "Nepomniachtchi"));
            // [Result "1-0"]
            tokens.addAll(createHeaderTokenList("Result", "1-0"));

            int[] pos = {0};

            // when
            Map<String, String> headers = headerParser.parseHeaders(tokens, pos);

            // then
            assertThat(headers).hasSize(4);
            assertThat(headers).containsEntry("Event", "Kletka Game");
            assertThat(headers).containsEntry("White", "Carlsen");
            assertThat(headers).containsEntry("Black", "Nepomniachtchi");
            assertThat(headers).containsEntry("Result", "1-0");
        }

        @Test
        @DisplayName("Должен парсить полный набор заголовков")
        void shouldParseFullHeaderSet() {
            // given
            List<PgnToken> tokens = new ArrayList<>();
            tokens.addAll(createHeaderTokenList("Event", "World Championship"));
            tokens.addAll(createHeaderTokenList("Site", "Dubai"));
            tokens.addAll(createHeaderTokenList("Date", "2021.12.01"));
            tokens.addAll(createHeaderTokenList("Round", "1"));
            tokens.addAll(createHeaderTokenList("White", "Carlsen"));
            tokens.addAll(createHeaderTokenList("Black", "Nepomniachtchi"));
            tokens.addAll(createHeaderTokenList("Result", "1-0"));
            tokens.addAll(createHeaderTokenList("ECO", "C67"));
            tokens.addAll(createHeaderTokenList("Opening", "Ruy Lopez"));
            tokens.addAll(createHeaderTokenList("Variation", "Berlin Defense"));
            tokens.addAll(createHeaderTokenList("WhiteElo", "2800"));
            tokens.addAll(createHeaderTokenList("BlackElo", "2750"));
            tokens.addAll(createHeaderTokenList("PlyCount", "40"));

            int[] pos = {0};

            // when
            Map<String, String> headers = headerParser.parseHeaders(tokens, pos);

            // then
            assertThat(headers).hasSize(13);
            assertThat(headers).containsEntry("Event", "World Championship");
            assertThat(headers).containsEntry("Site", "Dubai");
            assertThat(headers).containsEntry("Date", "2021.12.01");
            assertThat(headers).containsEntry("Round", "1");
            assertThat(headers).containsEntry("White", "Carlsen");
            assertThat(headers).containsEntry("Black", "Nepomniachtchi");
            assertThat(headers).containsEntry("Result", "1-0");
            assertThat(headers).containsEntry("ECO", "C67");
            assertThat(headers).containsEntry("Opening", "Ruy Lopez");
            assertThat(headers).containsEntry("Variation", "Berlin Defense");
            assertThat(headers).containsEntry("WhiteElo", "2800");
            assertThat(headers).containsEntry("BlackElo", "2750");
            assertThat(headers).containsEntry("PlyCount", "40");
        }

        @Test
        @DisplayName("Должен останавливаться при первом не-заголовке")
        void shouldStopAtFirstNonHeader() {
            // given
            List<PgnToken> tokens = new ArrayList<>();
            tokens.addAll(createHeaderTokenList("Event", "Game"));
            tokens.addAll(createHeaderTokenList("White", "Player"));
            // Дальше идет ход, а не заголовок
            tokens.add(new PgnToken(PgnTokenType.MOVE_NUMBER, "1.", 1, 1));

            int[] pos = {0};

            // when
            Map<String, String> headers = headerParser.parseHeaders(tokens, pos);

            // then
            assertThat(headers).hasSize(2);
            assertThat(headers).containsEntry("Event", "Game");
            assertThat(headers).containsEntry("White", "Player");
            assertThat(pos[0]).isEqualTo(8); // Позиция остановилась на токене хода
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ parseHeaders() - ПУСТЫЕ ВХОДНЫЕ ДАННЫЕ
    // ============================================================

    @Nested
    @DisplayName("Пустые входные данные")
    class EmptyInputTests {

        @Test
        @DisplayName("Должен возвращать пустую карту для пустого списка токенов")
        void shouldReturnEmptyMapForEmptyTokens() {
            // given
            List<PgnToken> tokens = new ArrayList<>();
            int[] pos = {0};

            // when
            Map<String, String> headers = headerParser.parseHeaders(tokens, pos);

            // then
            assertThat(headers).isEmpty();
            assertThat(pos[0]).isEqualTo(0);
        }

        @Test
        @DisplayName("Должен возвращать пустую карту если первый токен не HEADER_START")
        void shouldReturnEmptyMapIfFirstTokenNotHeaderStart() {
            // given
            List<PgnToken> tokens = List.of(
                    new PgnToken(PgnTokenType.MOVE, "e4", 1, 1)
            );
            int[] pos = {0};

            // when
            Map<String, String> headers = headerParser.parseHeaders(tokens, pos);

            // then
            assertThat(headers).isEmpty();
            assertThat(pos[0]).isEqualTo(0);
        }
    }

    // ============================================================
    // 4. ТЕСТЫ ДЛЯ parseHeaders() - ОБРАБОТКА ОШИБОК
    // ============================================================

    @Nested
    @DisplayName("Обработка ошибок")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Должен останавливаться при отсутствии ключа")
        void shouldStopWhenKeyMissing() {
            // given
            List<PgnToken> tokens = new ArrayList<>();
            tokens.add(new PgnToken(PgnTokenType.HEADER_START, "[", 1, 1));
            // Ожидается HEADER_KEY, но идет HEADER_VALUE
            tokens.add(new PgnToken(PgnTokenType.HEADER_VALUE, "Event", 1, 1));
            int[] pos = {0};

            // when
            Map<String, String> headers = headerParser.parseHeaders(tokens, pos);

            // then
            assertThat(headers).isEmpty();
        }

        @Test
        @DisplayName("Должен останавливаться при отсутствии значения")
        void shouldStopWhenValueMissing() {
            // given
            List<PgnToken> tokens = new ArrayList<>();
            tokens.add(new PgnToken(PgnTokenType.HEADER_START, "[", 1, 1));
            tokens.add(new PgnToken(PgnTokenType.HEADER_KEY, "Event", 1, 1));
            // Ожидается HEADER_VALUE, но идет HEADER_END
            tokens.add(new PgnToken(PgnTokenType.HEADER_END, "]", 1, 1));
            int[] pos = {0};

            // when
            Map<String, String> headers = headerParser.parseHeaders(tokens, pos);

            // then
            assertThat(headers).isEmpty();
        }

        @Test
        @DisplayName("Должен останавливаться при отсутствии закрывающей скобки")
        void shouldStopWhenEndMissing() {
            // given
            List<PgnToken> tokens = new ArrayList<>();
            tokens.add(new PgnToken(PgnTokenType.HEADER_START, "[", 1, 1));
            tokens.add(new PgnToken(PgnTokenType.HEADER_KEY, "Event", 1, 1));
            tokens.add(new PgnToken(PgnTokenType.HEADER_VALUE, "Game", 1, 1));
            // Ожидается HEADER_END, но идем дальше
            tokens.add(new PgnToken(PgnTokenType.MOVE, "e4", 1, 1));
            int[] pos = {0};

            // when
            Map<String, String> headers = headerParser.parseHeaders(tokens, pos);

            // then
            assertThat(headers).isEmpty();
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ parseHeaders() - ПОЗИЦИЯ ПОСЛЕ ПАРСИНГА
    // ============================================================

    @Nested
    @DisplayName("Позиция после парсинга")
    class PositionAfterParsingTests {

        @Test
        @DisplayName("Должен корректно обновлять позицию после парсинга нескольких заголовков")
        void shouldUpdatePositionAfterParsingMultipleHeaders() {
            // given
            List<PgnToken> tokens = new ArrayList<>();
            tokens.addAll(createHeaderTokenList("Event", "Game"));
            tokens.addAll(createHeaderTokenList("White", "Player"));
            tokens.add(new PgnToken(PgnTokenType.MOVE_NUMBER, "1.", 1, 1));

            int[] pos = {0};

            // when
            Map<String, String> headers = headerParser.parseHeaders(tokens, pos);

            // then
            assertThat(headers).hasSize(2);
            assertThat(pos[0]).isEqualTo(8); // 2 заголовка * 4 токена = 8
        }

        @Test
        @DisplayName("Должен корректно обновлять позицию после парсинга всех заголовков")
        void shouldUpdatePositionAfterParsingAllHeaders() {
            // given
            List<PgnToken> tokens = new ArrayList<>();
            tokens.addAll(createHeaderTokenList("Event", "Game"));
            tokens.addAll(createHeaderTokenList("White", "Player"));
            tokens.addAll(createHeaderTokenList("Black", "Player"));
            tokens.addAll(createHeaderTokenList("Result", "1-0"));

            int[] pos = {0};

            // when
            Map<String, String> headers = headerParser.parseHeaders(tokens, pos);

            // then
            assertThat(headers).hasSize(4);
            assertThat(pos[0]).isEqualTo(16); // 4 заголовка * 4 токена = 16
        }
    }

    // ============================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ============================================================

    /**
     * Создает список токенов для одного заголовка
     */
    private List<PgnToken> createHeaderTokens(String key, String value) {
        return List.of(
                new PgnToken(PgnTokenType.HEADER_START, "[", 1, 1),
                new PgnToken(PgnTokenType.HEADER_KEY, key, 1, 1),
                new PgnToken(PgnTokenType.HEADER_VALUE, value, 1, 1),
                new PgnToken(PgnTokenType.HEADER_END, "]", 1, 1)
        );
    }

    /**
     * Создает список токенов для одного заголовка (как List)
     */
    private List<PgnToken> createHeaderTokenList(String key, String value) {
        return List.of(
                new PgnToken(PgnTokenType.HEADER_START, "[", 1, 1),
                new PgnToken(PgnTokenType.HEADER_KEY, key, 1, 1),
                new PgnToken(PgnTokenType.HEADER_VALUE, value, 1, 1),
                new PgnToken(PgnTokenType.HEADER_END, "]", 1, 1)
        );
    }
}