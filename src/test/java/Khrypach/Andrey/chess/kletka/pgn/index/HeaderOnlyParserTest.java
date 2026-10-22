/*
 * Copyright (c) 2025 Andrey Khrypach
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package Khrypach.Andrey.chess.kletka.pgn.index;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты для {@link HeaderOnlyParser}.
 * Покрывают: корректный парсинг, граничные случаи, устойчивость к ошибкам.
 */
class HeaderOnlyParserTest {

    // ========== 1. Корректный парсинг ==========

    @Nested
    @DisplayName("Корректный парсинг заголовков")
    class CorrectParsing {

        @Test
        @DisplayName("Один заголовок")
        void singleHeader() {
            String pgn = "[White \"Kasparov\"]\n";

            Map<String, String> headers = HeaderOnlyParser.parseHeaders(pgn);

            assertThat(headers).hasSize(1);
            assertThat(headers.get("White")).isEqualTo("Kasparov");
        }

        @Test
        @DisplayName("Полный набор заголовков (как в реальной партии)")
        void fullHeaderSet() {
            String pgn = """
                    [Event "Berlin Casual Games"]
                    [Site "Berlin"]
                    [Date "1837.09.09"]
                    [Round "?"]
                    [White "Mayet, Carl"]
                    [Black "Hanstein, Wilhelm"]
                    [Result "1-0"]
                    [ECO "C40"]
                    [PlyCount "75"]
                    
                    1. e4 e5 2. Bc4 f5 3. Nf3 fxe4 4. Nxe5 d5 5. Qh5+ g6 6. Nxg6 Nf6 7. Qe5+ Be7
                    8. Nxe7 Qxe7 9. Qxe7+ Kxe7 10. Bb3 Rg8 11. Nc3 c6 12. g3 Bh3 13. d3 exd3 14. cxd3
                    Nbd7 15. Be3 Bg2 16. Rg1 Bf3 17. h3 c5 18. Bd1 Bxd1 19. Nxd1 d4 20. Bd2 Ne5 21.
                    Ke2 Nh5 22. g4 Raf8 23. b4 Nf3 24. Rf1 Nxd2 25. Kxd2 Nf4 26. bxc5 Nxh3 27. Rb1
                    Rb8 28. Rh1 Ng5 29. f4 Nf7 30. Nf2 h6 31. Rhe1+ Kd7 32. Re4 Kc6 33. Rxd4 Kxc5
                    34. Rd7 Nd6 35. Rc1+ Kd5 36. Ne4 Rgd8 37. Rcc7 a5 38. Rxb7 {(source: Thassilo
                    von Heydebrand und der Lasa: Berliner Schacherinnerungen, Leipzig 1859, p.
                    71-72.)} 1-0
                    """;

            Map<String, String> headers = HeaderOnlyParser.parseHeaders(pgn);

            assertThat(headers).hasSize(9);
            assertThat(headers.get("Event")).isEqualTo("Berlin Casual Games");
            assertThat(headers.get("Site")).isEqualTo("Berlin");
            assertThat(headers.get("Date")).isEqualTo("1837.09.09");
            assertThat(headers.get("White")).isEqualTo("Mayet, Carl");
            assertThat(headers.get("Black")).isEqualTo("Hanstein, Wilhelm");
            assertThat(headers.get("Result")).isEqualTo("1-0");
            assertThat(headers.get("ECO")).isEqualTo("C40");
            assertThat(headers.get("PlyCount")).isEqualTo("75");
        }

        @Test
        @DisplayName("Windows line endings (\\r\\n)")
        void windowsLineEndings() {
            String pgn = "[White \"Kasparov\"]\r\n[Black \"Karpov\"]\r\n\r\n1. e4 e5\r\n";

            Map<String, String> headers = HeaderOnlyParser.parseHeaders(pgn);

            assertThat(headers).hasSize(2);
            assertThat(headers.get("White")).isEqualTo("Kasparov");
            assertThat(headers.get("Black")).isEqualTo("Karpov");
        }

        @Test
        @DisplayName("Заголовки с пробелами в значении")
        void headerWithSpacesInValue() {
            String pgn = "[Event \"World Championship Match 2025\"]\n";

            Map<String, String> headers = HeaderOnlyParser.parseHeaders(pgn);

            assertThat(headers.get("Event")).isEqualTo("World Championship Match 2025");
        }

        @Test
        @DisplayName("Пустое значение заголовка")
        void emptyHeaderValue() {
            String pgn = "[Annotator \"\"]\n";

            Map<String, String> headers = HeaderOnlyParser.parseHeaders(pgn);

            assertThat(headers).hasSize(1);
            assertThat(headers.get("Annotator")).isEmpty();
        }
    }

    // ========== 2. Граничные случаи ==========

    @Nested
    @DisplayName("Граничные случаи")
    class EdgeCases {

        @Test
        @DisplayName("null на входе")
        void nullInput() {
            Map<String, String> headers = HeaderOnlyParser.parseHeaders(null);

            assertThat(headers).isEmpty();
        }

        @Test
        @DisplayName("Пустая строка")
        void emptyInput() {
            Map<String, String> headers = HeaderOnlyParser.parseHeaders("");

            assertThat(headers).isEmpty();
        }

        @Test
        @DisplayName("Только тело партии, без заголовков")
        void noHeaders() {
            String pgn = "1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 *";

            Map<String, String> headers = HeaderOnlyParser.parseHeaders(pgn);

            assertThat(headers).isEmpty();
        }

        @Test
        @DisplayName("Только пустые строки")
        void onlyWhitespace() {
            String pgn = "   \n\n\t\r\n   \n";

            Map<String, String> headers = HeaderOnlyParser.parseHeaders(pgn);

            assertThat(headers).isEmpty();
        }

        @Test
        @DisplayName("Заголовки с пустыми строками между ними")
        void headersWithBlankLinesBetween() {
            String pgn = """
                    [White "Kasparov"]
                    
                    [Black "Karpov"]
                    
                    1. e4 e5
                    """;

            Map<String, String> headers = HeaderOnlyParser.parseHeaders(pgn);

            assertThat(headers).hasSize(2);
        }

        @Test
        @DisplayName("Один заголовок в самом конце файла, без \\n")
        void headerAtEndWithoutNewline() {
            String pgn = "[White \"Kasparov\"]";

            Map<String, String> headers = HeaderOnlyParser.parseHeaders(pgn);

            assertThat(headers).hasSize(1);
            assertThat(headers.get("White")).isEqualTo("Kasparov");
        }
    }

    // ========== 3. Устойчивость к ошибкам ==========

    @Nested
    @DisplayName("Устойчивость к битым заголовкам")
    class MalformedHeaders {

        @Test
        @DisplayName("Нет открывающей кавычки")
        void missingOpeningQuote() {
            String pgn = "[White Kasparov]\n[Black \"Karpov\"]\n";

            Map<String, String> headers = HeaderOnlyParser.parseHeaders(pgn);

            // White пропущен, Black — прочитан
            assertThat(headers).hasSize(1);
            assertThat(headers.get("Black")).isEqualTo("Karpov");
        }

        @Test
        @DisplayName("Нет закрывающей кавычки")
        void missingClosingQuote() {
            String pgn = "[White \"Kasparov\n[Black \"Karpov\"]\n";

            Map<String, String> headers = HeaderOnlyParser.parseHeaders(pgn);

            // White пропущен (нет закрывающей кавычки на той же строке)
            assertThat(headers).doesNotContainKey("White");
            assertThat(headers.get("Black")).isEqualTo("Karpov");
        }

        @Test
        @DisplayName("Пустой ключ")
        void emptyKey() {
            String pgn = "[ \"Kasparov\"]\n[Black \"Karpov\"]\n";

            Map<String, String> headers = HeaderOnlyParser.parseHeaders(pgn);

            // Пустой ключ попадает в map как ""
            assertThat(headers).containsKey("");
            assertThat(headers.get("Black")).isEqualTo("Karpov");
        }

        @Test
        @DisplayName("Битая строка перед заголовком — пропускается")
        void brokenLineBeforeHeader() {
            String pgn = "this is garbage\n[White \"Kasparov\"]\n";

            Map<String, String> headers = HeaderOnlyParser.parseHeaders(pgn);

            // Парсер останавливается на первой не-заголовочной строке
            assertThat(headers).isEmpty();
        }
    }

    // ========== 4. Остановка на теле партии ==========

    @Nested
    @DisplayName("Остановка на теле партии")
    class StopsAtBody {

        @Test
        @DisplayName("Тело начинается сразу после заголовков")
        void bodyImmediatelyAfterHeaders() {
            String pgn = """
                    [White "Kasparov"]
                    [Black "Karpov"]
                    1. e4 e5 2. Nf3 Nc6
                    """;

            Map<String, String> headers = HeaderOnlyParser.parseHeaders(pgn);

            assertThat(headers).hasSize(2);
        }

        @Test
        @DisplayName("Пропускает пустые строки перед телом")
        void skipsBlankLinesBeforeBody() {
            String pgn = """
                    [White "Kasparov"]
                    
                    
                    
                    1. e4 e5
                    """;

            Map<String, String> headers = HeaderOnlyParser.parseHeaders(pgn);

            assertThat(headers).hasSize(1);
        }

        @Test
        @DisplayName("Не парсит псевдо-заголовки из тела")
        void ignoresBracketInBody() {
            String pgn = """
                    [White "Kasparov"]
                    1. e4 e5 {[this is a comment, not a header]}
                    2. Nf3 Nc6
                    """;

            Map<String, String> headers = HeaderOnlyParser.parseHeaders(pgn);

            assertThat(headers).hasSize(1);
            assertThat(headers).doesNotContainKey("this is a comment, not a header");
        }
    }

    // ========== 5. Производительность (быстрая проверка) ==========

    @Nested
    @DisplayName("Производительность")
    class Performance {

        @Test
        @DisplayName("Парсит 100 000 заголовков за разумное время")
        void parseManyHeadersQuickly() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 100_000; i++) {
                sb.append("[Event \"Game ").append(i).append("\"]\n");
                sb.append("[White \"White ").append(i).append("\"]\n");
                sb.append("[Black \"Black ").append(i).append("\"]\n");
                sb.append("[Result \"1-0\"]\n\n");
            }
            String pgn = sb.toString();

            long start = System.currentTimeMillis();
            Map<String, String> headers = HeaderOnlyParser.parseHeaders(pgn);
            long elapsed = System.currentTimeMillis() - start;

            // Парсер должен обработать 100 000 партий быстро
            assertThat(headers).hasSize(4); // только последние
            assertThat(elapsed).isLessThan(2000); // < 2 секунд
        }
    }
}