/*
 * Copyright (c) 2025-2026 Andrey Khrypach
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

package Khrypach.Andrey.chess.kletka.gui.book;

import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для PolyglotEntry.
 * Проверяем корректность извлечения битов из 16-битного хода,
 * преобразование в UCI-формат, расчёт рейтинга и работу record-методов.
 */
class PolyglotEntryTest {

    // ======================================================================
    // 1. ТЕСТЫ ИЗВЛЕЧЕНИЯ КЛЕТОК (FROM/TO)
    // ======================================================================

    @Test
    void getFromSquare_ShouldExtractCorrectly() {
        // Ход e2-e4: from = E2 (код 12), to = E4 (код 28)
        // 16-битный ход: from(6 бит) = 12, to(6 бит) = 28, promotion(4 бита) = 0
        // move = (12 << 6) | 28 = 796
        short move = (short) ((12 << 6) | 28);

        PolyglotEntry entry = new PolyglotEntry(0L, move, (short) 0, 0, 0);

        assertEquals(Square.E2, entry.getFromSquare(),
                "Извлечение начальной клетки из хода 796 (E2-E4) должно дать E2");
    }

    @Test
    void getToSquare_ShouldExtractCorrectly() {
        // Ход e2-e4: from = E2 (12), to = E4 (28)
        short move = (short) ((12 << 6) | 28);

        PolyglotEntry entry = new PolyglotEntry(0L, move, (short) 0, 0, 0);

        assertEquals(Square.E4, entry.getToSquare(),
                "Извлечение конечной клетки из хода 796 (E2-E4) должно дать E4");
    }

    // ======================================================================
// 2. ТЕСТЫ ПРЕВРАЩЕНИЯ
// ======================================================================

    @Test
    void getPromotionPiece_ShouldExtractCorrectly() {
        short move = (short) ((48 << 6) | 56 | (4 << 12)); // a7-a8=Q

        PolyglotEntry entry = new PolyglotEntry(0L, move, (short) 0, 0, 0);

        assertEquals(Piece.WHITE_QUEEN, entry.getPromotionPiece(),
                "Превращение в ферзя должно кодироваться как 4");
    }

    @Test
    void getPromotionPiece_ShouldReturnNoneForNoPromotion() {
        short move = (short) ((12 << 6) | 28); // e2-e4

        PolyglotEntry entry = new PolyglotEntry(0L, move, (short) 0, 0, 0);

        assertEquals(Piece.NONE, entry.getPromotionPiece(),
                "Без превращения должен возвращаться Piece.NONE");
    }

    @Test
    void getPromotionPiece_ShouldHandleAllValidCodes() {
        // 0 -> NONE, 1 -> KNIGHT, 2 -> BISHOP, 3 -> ROOK, 4 -> QUEEN
        Object[][] testData = {
                {0, Piece.NONE},
                {1, Piece.WHITE_KNIGHT},
                {2, Piece.WHITE_BISHOP},
                {3, Piece.WHITE_ROOK},
                {4, Piece.WHITE_QUEEN}
        };

        for (Object[] data : testData) {
            int code = (int) data[0];
            Piece expectedPiece = (Piece) data[1];

            short move = (short) (code << 12);
            PolyglotEntry entry = new PolyglotEntry(0L, move, (short) 0, 0, 0);

            assertEquals(expectedPiece, entry.getPromotionPiece(),
                    "Для кода " + code + " ожидается фигура " + expectedPiece);
        }
    }

    @Test
    void getPromotionPiece_ShouldReturnNoneForInvalidCodes() {
        // Для всех невалидных кодов (5-15) должен возвращаться Piece.NONE
        for (int invalidCode = 5; invalidCode <= 15; invalidCode++) {
            short move = (short) (invalidCode << 12);
            PolyglotEntry entry = new PolyglotEntry(0L, move, (short) 0, 0, 0);

            assertEquals(Piece.NONE, entry.getPromotionPiece(),
                    "Для невалидного кода " + invalidCode + " должен возвращаться Piece.NONE");
        }
    }

    // ======================================================================
    // 3. ТЕСТ ПРЕОБРАЗОВАНИЯ В UCI
    // ======================================================================

    @Test
    void getUciMove_ShouldFormatWithoutPromotion() {
        // Ход e2-e4
        short move = (short) ((12 << 6) | 28);
        PolyglotEntry entry = new PolyglotEntry(0L, move, (short) 0, 0, 0);

        assertEquals("e2e4", entry.getUciMove(),
                "Ход e2-e4 должен преобразовываться в 'e2e4'");
    }

    @Test
    void getUciMove_ShouldFormatWithPromotion() {
        // Ход a7-a8=Q
        short move = (short) ((48 << 6) | 56 | (4 << 12));
        PolyglotEntry entry = new PolyglotEntry(0L, move, (short) 0, 0, 0);

        assertEquals("a7a8q", entry.getUciMove(),
                "Превращение в ферзя должно давать 'a7a8q'");
    }

    // ======================================================================
    // 4. ТЕСТ РАСЧЁТА РЕЙТИНГА
    // ======================================================================

    @Test
    void getRating_ShouldReturn0ForZeroWeight() {
        PolyglotEntry entry = new PolyglotEntry(0L, (short) 0, (short) 0, 0, 0);
        assertEquals(0, entry.getRating(),
                "При весе 0 рейтинг должен быть 0");
    }

    @Test
    void getRating_ShouldCalculateCorrectly() {
        // Вес 16384 (половина от 32767) должен дать ~2500
        short weight = (short) 16384;
        PolyglotEntry entry = new PolyglotEntry(0L, (short) 0, weight, 0, 0);

        int rating = entry.getRating();
        assertTrue(rating >= 2450 && rating <= 2550,
                "Рейтинг при весе 16384 должен быть около 2500, получено: " + rating);
    }

    // ======================================================================
    // 5. ТЕСТ STRING ПРЕДСТАВЛЕНИЯ
    // ======================================================================

    @Test
    void testToString_ShouldReturnReadableFormat() {
        short move = (short) ((12 << 6) | 28); // e2-e4
        PolyglotEntry entry = new PolyglotEntry(0x1234567890ABCDEFL, move, (short) 100, 10, 5);

        String str = entry.toString();
        assertTrue(str.contains("key=0x1234567890ABCDEF"),
                "toString должен содержать ключ в hex-формате");
        assertTrue(str.contains("move=e2e4"),
                "toString должен содержать ход в UCI-формате");
        assertTrue(str.contains("weight=100"),
                "toString должен содержать вес");
        assertTrue(str.contains("games=5"),
                "toString должен содержать количество партий");
    }

    // ======================================================================
    // 6. ТЕСТЫ RECORD-МЕТОДОВ (стандартные)
    // ======================================================================

    @Test
    void key_ShouldReturnCorrectValue() {
        long key = 0x1234567890ABCDEFL;
        PolyglotEntry entry = new PolyglotEntry(key, (short) 0, (short) 0, 0, 0);

        assertEquals(key, entry.key(),
                "Метод key() должен возвращать переданное значение");
    }

    @Test
    void move_ShouldReturnCorrectValue() {
        short move = (short) 796; // e2-e4
        PolyglotEntry entry = new PolyglotEntry(0L, move, (short) 0, 0, 0);

        assertEquals(move, entry.move(),
                "Метод move() должен возвращать переданное значение");
    }

    @Test
    void weight_ShouldReturnCorrectValue() {
        short weight = (short) 16384;
        PolyglotEntry entry = new PolyglotEntry(0L, (short) 0, weight, 0, 0);

        assertEquals(weight, entry.weight(),
                "Метод weight() должен возвращать переданное значение");
    }

    @Test
    void learn_ShouldReturnCorrectValue() {
        int learn = 12345;
        PolyglotEntry entry = new PolyglotEntry(0L, (short) 0, (short) 0, learn, 0);

        assertEquals(learn, entry.learn(),
                "Метод learn() должен возвращать переданное значение");
    }

    @Test
    void games_ShouldReturnCorrectValue() {
        int games = 42;
        PolyglotEntry entry = new PolyglotEntry(0L, (short) 0, (short) 0, 0, games);

        assertEquals(games, entry.games(),
                "Метод games() должен возвращать переданное значение");
    }

    // ======================================================================
    // 7. ТЕСТЫ EQUALS И HASHCODE
    // ======================================================================

    @Test
    void testEquals_ShouldBeTrueForSameFields() {
        long key = 0x1234567890ABCDEFL;
        short move = (short) 796;
        short weight = (short) 100;
        int learn = 12345;
        int games = 42;

        PolyglotEntry entry1 = new PolyglotEntry(key, move, weight, learn, games);
        PolyglotEntry entry2 = new PolyglotEntry(key, move, weight, learn, games);

        assertEquals(entry1, entry2,
                "Два объекта с одинаковыми полями должны быть равны");
        assertEquals(entry1.hashCode(), entry2.hashCode(),
                "HashCode должен совпадать для равных объектов");
    }

    @Test
    void testEquals_ShouldBeFalseForDifferentFields() {
        PolyglotEntry entry1 = new PolyglotEntry(0x1L, (short) 100, (short) 50, 10, 5);
        PolyglotEntry entry2 = new PolyglotEntry(0x2L, (short) 200, (short) 60, 20, 10);

        assertNotEquals(entry1, entry2,
                "Объекты с разными полями должны быть не равны");
    }

    @Test
    void testEquals_ShouldBeFalseForNull() {
        PolyglotEntry entry = new PolyglotEntry(0L, (short) 0, (short) 0, 0, 0);

        assertNotEquals(null, entry,
                "Сравнение с null должно давать false");
    }

    @Test
    void testEquals_ShouldBeFalseForDifferentClass() {
        PolyglotEntry entry = new PolyglotEntry(0L, (short) 0, (short) 0, 0, 0);
        String notEntry = "not an entry";

        assertNotEquals(notEntry, entry,
                "Сравнение с объектом другого класса должно давать false");
    }

    // ======================================================================
    // 8. ТЕСТ НА ГРАНИЧНЫЕ ЗНАЧЕНИЯ
    // ======================================================================

    @Test
    void getRating_ShouldHandleMaxWeight() {
        // Максимальный вес = 32767
        short maxWeight = (short) 32767;
        PolyglotEntry entry = new PolyglotEntry(0L, (short) 0, maxWeight, 0, 0);

        int rating = entry.getRating();
        assertTrue(rating >= 3000 && rating <= 3500,
                "Рейтинг при максимальном весе должен быть около 3500, получено: " + rating);
    }
}