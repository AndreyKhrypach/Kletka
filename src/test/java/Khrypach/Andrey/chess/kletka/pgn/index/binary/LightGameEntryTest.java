/*
 *
 *  * Copyright (c) 2024 Andrey Khrypach
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

package Khrypach.Andrey.chess.kletka.pgn.index.binary;

import Khrypach.Andrey.chess.kletka.pgn.index.model.GameIndexEntry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LightGameEntryTest {

    // ============================================================
    // 1. ТЕСТЫ КОНСТРУКТОРА
    // ============================================================

    @Test
    void constructor_shouldHandleNullValues() {
        LightGameEntry entry = new LightGameEntry(
                1,
                null,  // white
                null,  // black
                null,  // result
                null,  // year
                null,  // event
                null,  // eco
                null,  // opening
                100,
                200,
                false,
                12345
        );

        assertEquals(1, entry.id());
        assertEquals("", entry.white());
        assertEquals("", entry.black());
        assertEquals("*", entry.result());  // result по умолчанию "*"
        assertEquals("", entry.year());
        assertEquals("", entry.event());
        assertEquals("", entry.eco());
        assertEquals("", entry.opening());
        assertEquals(100, entry.offset());
        assertEquals(200, entry.length());
        assertFalse(entry.deleted());
        assertEquals(12345, entry.hash());
    }

    @Test
    void constructor_shouldHandleEmptyStrings() {
        LightGameEntry entry = new LightGameEntry(
                1,
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                100,
                200,
                false,
                12345
        );

        assertEquals(1, entry.id());
        assertEquals("", entry.white());
        assertEquals("", entry.black());
        assertEquals("", entry.result());  // ✅ Исправлено: пустая строка остается пустой
        assertEquals("", entry.year());
        assertEquals("", entry.event());
        assertEquals("", entry.eco());
        assertEquals("", entry.opening());
    }

    @Test
    void constructor_shouldHandleValidValues() {
        LightGameEntry entry = new LightGameEntry(
                1,
                "Carlsen",
                "Nakamura",
                "1-0",
                "2024",
                "WCC 2024",
                "B52",
                "Sicilian",
                100,
                200,
                false,
                12345
        );

        assertEquals(1, entry.id());
        assertEquals("Carlsen", entry.white());
        assertEquals("Nakamura", entry.black());
        assertEquals("1-0", entry.result());
        assertEquals("2024", entry.year());
        assertEquals("WCC 2024", entry.event());
        assertEquals("B52", entry.eco());
        assertEquals("Sicilian", entry.opening());
        assertEquals(100, entry.offset());
        assertEquals(200, entry.length());
        assertFalse(entry.deleted());
        assertEquals(12345, entry.hash());
    }

    @Test
    void constructor_shouldHandleDeletedFlag() {
        LightGameEntry entry1 = new LightGameEntry(
                1, "White", "Black", "1-0", "2024",
                "Event", "A00", "Opening", 0, 100, false, 12345
        );
        assertFalse(entry1.deleted());

        LightGameEntry entry2 = new LightGameEntry(
                2, "White", "Black", "1-0", "2024",
                "Event", "A00", "Opening", 0, 100, true, 12345
        );
        assertTrue(entry2.deleted());
    }

    // ============================================================
    // 2. ТЕСТЫ fromFull()
    // ============================================================

    @Test
    void fromFull_shouldCreateLightEntryFromFullEntry() {
        GameIndexEntry fullEntry = GameIndexEntry.builder()
                .id(1)
                .white("Carlsen")
                .black("Nakamura")
                .result("1-0")
                .year("2024")
                .event("WCC 2024")
                .eco("B52")
                .opening("Sicilian")
                .offset(100)
                .length(200)
                .deleted(false)
                .hash(12345)
                .build();

        LightGameEntry lightEntry = LightGameEntry.fromFull(fullEntry);

        assertEquals(fullEntry.getId(), lightEntry.id());
        assertEquals(fullEntry.getWhite(), lightEntry.white());
        assertEquals(fullEntry.getBlack(), lightEntry.black());
        assertEquals(fullEntry.getResult(), lightEntry.result());
        assertEquals(fullEntry.getYear(), lightEntry.year());
        assertEquals(fullEntry.getEvent(), lightEntry.event());
        assertEquals(fullEntry.getEco(), lightEntry.eco());
        assertEquals(fullEntry.getOpening(), lightEntry.opening());
        assertEquals(fullEntry.getOffset(), lightEntry.offset());
        assertEquals(fullEntry.getLength(), lightEntry.length());
        assertEquals(fullEntry.isDeleted(), lightEntry.deleted());
        assertEquals(fullEntry.getHash(), lightEntry.hash());
    }

    @Test
    void fromFull_shouldHandleNullFieldsInFullEntry() {
        GameIndexEntry fullEntry = GameIndexEntry.builder()
                .id(1)
                .white(null)
                .black(null)
                .result(null)
                .year(null)
                .event(null)
                .eco(null)
                .opening(null)
                .offset(0)
                .length(100)
                .deleted(false)
                .hash(0)
                .build();

        LightGameEntry lightEntry = LightGameEntry.fromFull(fullEntry);

        assertEquals("", lightEntry.white());
        assertEquals("", lightEntry.black());
        assertEquals("*", lightEntry.result());
        assertEquals("", lightEntry.year());
        assertEquals("", lightEntry.event());
        assertEquals("", lightEntry.eco());
        assertEquals("", lightEntry.opening());
    }

    // ============================================================
    // 3. ТЕСТЫ matches()
    // ============================================================

    @Test
    void matches_shouldReturnTrueWhenWhiteMatches() {
        LightGameEntry entry = new LightGameEntry(
                1, "Carlsen", "Nakamura", "1-0", "2024",
                "WCC 2024", "B52", "Sicilian", 0, 100, false, 12345
        );

        assertTrue(entry.matches("Carlsen"));
        assertTrue(entry.matches("carlsen"));  // регистронезависимо
        assertTrue(entry.matches("CARLSEN"));
        assertTrue(entry.matches("Carl"));     // частичное совпадение
    }

    @Test
    void matches_shouldReturnTrueWhenBlackMatches() {
        LightGameEntry entry = new LightGameEntry(
                1, "Carlsen", "Nakamura", "1-0", "2024",
                "WCC 2024", "B52", "Sicilian", 0, 100, false, 12345
        );

        assertTrue(entry.matches("Nakamura"));
        assertTrue(entry.matches("nakamura"));
        assertTrue(entry.matches("NAKAMURA"));
        assertTrue(entry.matches("Naka"));
    }

    @Test
    void matches_shouldReturnTrueWhenResultMatches() {
        LightGameEntry entry = new LightGameEntry(
                1, "Carlsen", "Nakamura", "1-0", "2024",
                "WCC 2024", "B52", "Sicilian", 0, 100, false, 12345
        );

        assertTrue(entry.matches("1-0"));
        assertTrue(entry.matches("1"));
        assertTrue(entry.matches("0"));
    }

    @Test
    void matches_shouldReturnTrueWhenYearMatches() {
        LightGameEntry entry = new LightGameEntry(
                1, "Carlsen", "Nakamura", "1-0", "2024",
                "WCC 2024", "B52", "Sicilian", 0, 100, false, 12345
        );

        assertTrue(entry.matches("2024"));
        assertTrue(entry.matches("24"));
        assertTrue(entry.matches("202"));
    }

    @Test
    void matches_shouldReturnTrueWhenEventMatches() {
        LightGameEntry entry = new LightGameEntry(
                1, "Carlsen", "Nakamura", "1-0", "2024",
                "WCC 2024", "B52", "Sicilian", 0, 100, false, 12345
        );

        assertTrue(entry.matches("WCC"));
        assertTrue(entry.matches("2024"));
        assertTrue(entry.matches("wcc"));
    }

    @Test
    void matches_shouldReturnTrueWhenEcoMatches() {
        LightGameEntry entry = new LightGameEntry(
                1, "Carlsen", "Nakamura", "1-0", "2024",
                "WCC 2024", "B52", "Sicilian", 0, 100, false, 12345
        );

        assertTrue(entry.matches("B52"));
        assertTrue(entry.matches("b52"));
        assertTrue(entry.matches("B5"));
    }

    @Test
    void matches_shouldReturnTrueWhenOpeningMatches() {
        LightGameEntry entry = new LightGameEntry(
                1, "Carlsen", "Nakamura", "1-0", "2024",
                "WCC 2024", "B52", "Sicilian", 0, 100, false, 12345
        );

        assertTrue(entry.matches("Sicilian"));
        assertTrue(entry.matches("sicilian"));
        assertTrue(entry.matches("Sici"));
    }

    @Test
    void matches_shouldReturnFalseWhenQueryDoesNotMatch() {
        LightGameEntry entry = new LightGameEntry(
                1, "Carlsen", "Nakamura", "1-0", "2024",
                "WCC 2024", "B52", "Sicilian", 0, 100, false, 12345
        );

        assertFalse(entry.matches("Kasparov"));
        assertFalse(entry.matches("1990"));
        assertFalse(entry.matches("C67"));
        assertFalse(entry.matches("Ruy Lopez"));
    }

    @Test
    void matches_shouldHandleEmptyQuery() {
        LightGameEntry entry = new LightGameEntry(
                1, "Carlsen", "Nakamura", "1-0", "2024",
                "WCC 2024", "B52", "Sicilian", 0, 100, false, 12345
        );

        // Пустой запрос должен вернуть true (все записи подходят)
        assertTrue(entry.matches(""));
    }

    @Test
    void matches_shouldHandleQueryWithSpecialCharacters() {
        LightGameEntry entry = new LightGameEntry(
                1, "Carlsen", "Nakamura", "1-0", "2024",
                "WCC 2024", "B52", "Sicilian", 0, 100, false, 12345
        );

        assertTrue(entry.matches("1-0"));
        assertTrue(entry.matches("WCC 2024"));
        assertFalse(entry.matches("WCC 2025"));
    }

    @Test
    void matches_shouldHandleMultipleFieldsMatch() {
        LightGameEntry entry = new LightGameEntry(
                1, "Carlsen", "Nakamura", "1-0", "2024",
                "WCC 2024", "B52", "Sicilian", 0, 100, false, 12345
        );

        // ✅ Проверяем отдельные слова
        assertTrue(entry.matches("Carl"));   // совпадает с white
        assertTrue(entry.matches("Naka"));   // совпадает с black
        assertTrue(entry.matches("2024"));   // совпадает с year и event
        assertTrue(entry.matches("WCC"));    // совпадает с event
        assertTrue(entry.matches("B52"));    // совпадает с eco

        // ✅ Проверяем полную строку, которая есть в одном поле
        assertTrue(entry.matches("WCC 2024")); // совпадает с event

        // ✅ Проверяем поиск по нескольким полям через OR
        // (метод matches ищет в любом поле, но не комбинирует условия)
        assertTrue(entry.matches("Carlsen")); // совпадает с white
        assertTrue(entry.matches("Nakamura")); // совпадает с black

    }

    // ============================================================
    // 4. ТЕСТЫ toString()
    // ============================================================

    @Test
    void toString_shouldReturnFormattedString() {
        LightGameEntry entry = new LightGameEntry(
                1, "Carlsen", "Nakamura", "1-0", "2024",
                "WCC 2024", "B52", "Sicilian", 0, 100, false, 12345
        );

        String str = entry.toString();

        assertNotNull(str);
        assertTrue(str.contains("1"));
        assertTrue(str.contains("Carlsen"));
        assertTrue(str.contains("Nakamura"));
        assertTrue(str.contains("1-0"));
        assertTrue(str.startsWith("LightEntry{"));
        assertTrue(str.endsWith("}"));
    }

    @Test
    void toString_shouldHandleEmptyFields() {
        LightGameEntry entry = new LightGameEntry(
                1, "", "", "*", "", "", "", "", 0, 0, false, 0
        );

        String str = entry.toString();
        assertNotNull(str);
        assertTrue(str.contains("id=1"));
    }

    // ============================================================
    // 5. ТЕСТЫ RECORD МЕТОДОВ (геттеры)
    // ============================================================

    @Test
    void recordMethods_shouldReturnCorrectValues() {
        LightGameEntry entry = new LightGameEntry(
                42,
                "WhitePlayer",
                "BlackPlayer",
                "1/2-1/2",
                "2023",
                "Tournament",
                "C67",
                "Ruy Lopez",
                999,
                888,
                true,
                98765
        );

        assertEquals(42, entry.id());
        assertEquals("WhitePlayer", entry.white());
        assertEquals("BlackPlayer", entry.black());
        assertEquals("1/2-1/2", entry.result());
        assertEquals("2023", entry.year());
        assertEquals("Tournament", entry.event());
        assertEquals("C67", entry.eco());
        assertEquals("Ruy Lopez", entry.opening());
        assertEquals(999, entry.offset());
        assertEquals(888, entry.length());
        assertTrue(entry.deleted());
        assertEquals(98765, entry.hash());
    }

    // ============================================================
    // 6. ТЕСТЫ СРАВНЕНИЯ (equals / hashCode)
    // ============================================================

    @Test
    void equals_shouldReturnTrueForIdenticalEntries() {
        LightGameEntry entry1 = new LightGameEntry(
                1, "Carlsen", "Nakamura", "1-0", "2024",
                "WCC 2024", "B52", "Sicilian", 0, 100, false, 12345
        );

        LightGameEntry entry2 = new LightGameEntry(
                1, "Carlsen", "Nakamura", "1-0", "2024",
                "WCC 2024", "B52", "Sicilian", 0, 100, false, 12345
        );

        assertEquals(entry1, entry2);
        assertEquals(entry1.hashCode(), entry2.hashCode());
    }

    @Test
    void equals_shouldReturnFalseForDifferentEntries() {
        LightGameEntry entry1 = new LightGameEntry(
                1, "Carlsen", "Nakamura", "1-0", "2024",
                "WCC 2024", "B52", "Sicilian", 0, 100, false, 12345
        );

        LightGameEntry entry2 = new LightGameEntry(
                2, "Kasparov", "Karpov", "0-1", "1985",
                "Moscow", "E20", "Nimzo-Indian", 100, 200, true, 54321
        );

        assertNotEquals(entry1, entry2);
        assertNotEquals(entry1.hashCode(), entry2.hashCode());
    }

    @Test
    void equals_shouldReturnFalseForDifferentId() {
        LightGameEntry entry1 = new LightGameEntry(
                1, "Carlsen", "Nakamura", "1-0", "2024",
                "WCC 2024", "B52", "Sicilian", 0, 100, false, 12345
        );

        LightGameEntry entry2 = new LightGameEntry(
                2, "Carlsen", "Nakamura", "1-0", "2024",
                "WCC 2024", "B52", "Sicilian", 0, 100, false, 12345
        );

        assertNotEquals(entry1, entry2);
    }

    @Test
    void equals_shouldReturnFalseForNull() {
        LightGameEntry entry = new LightGameEntry(
                1, "Carlsen", "Nakamura", "1-0", "2024",
                "WCC 2024", "B52", "Sicilian", 0, 100, false, 12345
        );

        assertNotEquals(null, entry);
    }

}