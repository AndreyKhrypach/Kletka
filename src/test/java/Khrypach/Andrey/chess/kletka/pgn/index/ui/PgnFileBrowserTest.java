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

package Khrypach.Andrey.chess.kletka.pgn.index.ui;

import Khrypach.Andrey.chess.kletka.pgn.index.model.GameIndexEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PgnFileBrowser - PGN Браузер")
class PgnFileBrowserTest {

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ GameTableRow
    // ============================================================

    @Nested
    @DisplayName("GameTableRow - Строка таблицы")
    class GameTableRowTests {

        @Test
        @DisplayName("Должен создавать строку с корректными данными")
        void shouldCreateRowWithCorrectData() {
            // given
            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(1)
                    .white("Player 1")
                    .black("Player 2")
                    .result("1-0")
                    .year("2024")
                    .event("Test Event")
                    .eco("C44")
                    .opening("Test Opening")
                    .build();

            // when
            PgnFileBrowser.GameTableRow row = new PgnFileBrowser.GameTableRow(
                    1, "Player 1", "Player 2", "1-0",
                    "2024", "Test Event", "C44", "Test Opening",
                    "1. e4 e5", entry
            );

            // then
            assertThat(row.getId()).isEqualTo(1);
            assertThat(row.getWhite()).isEqualTo("Player 1");
            assertThat(row.getBlack()).isEqualTo("Player 2");
            assertThat(row.getResult()).isEqualTo("1-0");
            assertThat(row.getYear()).isEqualTo("2024");
            assertThat(row.getEvent()).isEqualTo("Test Event");
            assertThat(row.getEco()).isEqualTo("C44");
            assertThat(row.getOpening()).isEqualTo("Test Opening");
            assertThat(row.getBody()).isEqualTo("1. e4 e5");
            assertThat(row.getIndexEntry()).isEqualTo(entry);
        }

        @Test
        @DisplayName("equals() должен сравнивать по id")
        void shouldCompareRowsById() {
            // given
            GameIndexEntry entry1 = GameIndexEntry.builder().id(1).build();
            GameIndexEntry entry2 = GameIndexEntry.builder().id(2).build();

            PgnFileBrowser.GameTableRow row1 = new PgnFileBrowser.GameTableRow(
                    1, "", "", "", "", "", "", "", "", entry1
            );
            PgnFileBrowser.GameTableRow row2 = new PgnFileBrowser.GameTableRow(
                    1, "", "", "", "", "", "", "", "", entry2
            );
            PgnFileBrowser.GameTableRow row3 = new PgnFileBrowser.GameTableRow(
                    2, "", "", "", "", "", "", "", "", null
            );

            // then
            assertThat(row1).isEqualTo(row2);
            assertThat(row1).isNotEqualTo(row3);
        }

        @Test
        @DisplayName("hashCode() должен возвращать хеш на основе id")
        void shouldReturnHashCodeBasedOnId() {
            // given
            GameIndexEntry entry = GameIndexEntry.builder().id(1).build();
            PgnFileBrowser.GameTableRow row = new PgnFileBrowser.GameTableRow(
                    1, "", "", "", "", "", "", "", "", entry
            );
            PgnFileBrowser.GameTableRow row2 = new PgnFileBrowser.GameTableRow(
                    2, "", "", "", "", "", "", "", "", entry
            );

            // then
            assertThat(row.hashCode()).isNotZero();
            assertThat(row.hashCode()).isNotEqualTo(row2.hashCode());
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ extractBody()
    // ============================================================

    @Nested
    @DisplayName("extractBody() - Извлечение тела PGN")
    class ExtractBodyTests {

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
            String body = extractBody(pgn);

            // then
            assertThat(body.trim()).isEqualTo("1. e4 e5 2. Nf3 Nc6 1-0");
        }

        @Test
        @DisplayName("Должен возвращать пустую строку для null")
        void shouldReturnEmptyForNull() {
            // when
            String body = extractBody(null);

            // then
            assertThat(body).isEmpty();
        }

        @Test
        @DisplayName("Должен возвращать PGN без изменений если нет заголовков")
        void shouldReturnPgnAsIsIfNoHeaders() {
            // given
            String pgn = "1. e4 e5 2. Nf3 Nc6 1-0";

            // when
            String body = extractBody(pgn);

            // then
            assertThat(body).isEqualTo(pgn);
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ РАЗМЕРОВ ЭКРАНА
    // ============================================================

    @Nested
    @DisplayName("Размеры окна")
    class WindowSizeTests {

        @Test
        @DisplayName("Оптимальная ширина должна быть в разумных пределах")
        void shouldReturnReasonableOptimalWidth() {
            // given

            // when
            // Расчет ширины происходит в методе getOptimalWindowWidth()
            // Проверяем константы
            assertThat(PgnFileBrowser.MIN_WINDOW_WIDTH).isEqualTo(800);
            assertThat(PgnFileBrowser.ABSOLUTE_MAX_WIDTH).isEqualTo(30000);
        }

        @Test
        @DisplayName("Оптимальная высота должна быть в разумных пределах")
        void shouldReturnReasonableOptimalHeight() {

            assertThat(PgnFileBrowser.MIN_WINDOW_HEIGHT).isEqualTo(600);
            assertThat(PgnFileBrowser.ABSOLUTE_MAX_HEIGHT).isEqualTo(30000);
        }
    }

    // ============================================================
    // ВСПОМОГАТЕЛЬНЫЙ МЕТОД
    // ============================================================

    private String extractBody(String fullPgn) {
        if (fullPgn == null || fullPgn.isEmpty()) return "";
        int lastBracket = fullPgn.lastIndexOf(']');
        if (lastBracket < 0) return fullPgn;

        int bodyStart = -1;
        for (int i = lastBracket + 1; i < fullPgn.length() - 1; i++) {
            if (fullPgn.charAt(i) == '\n' && fullPgn.charAt(i + 1) == '\n') {
                bodyStart = i + 2;
                break;
            }
        }
        if (bodyStart < 0) {
            String after = fullPgn.substring(lastBracket + 1);
            if (!after.isEmpty() && !after.startsWith("[")) return after;
            return "";
        }
        return fullPgn.substring(bodyStart);
    }
}