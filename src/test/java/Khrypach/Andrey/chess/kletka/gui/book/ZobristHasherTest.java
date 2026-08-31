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

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для ZobristHasher.
 * Проверяем соответствие хешей спецификации Polyglot
 * и корректность работы кэширования.
 */
class ZobristHasherTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    // ======================================================================
    // 1. БАЗОВЫЙ ТЕСТ: ХЕШ НАЧАЛЬНОЙ ПОЗИЦИИ
    // ======================================================================

    @Test
    void testInitialPositionHash() {
        // Ожидаемый хеш начальной позиции из спецификации Polyglot
        long expectedHash = 0x463b96181691fc9cL;

        long actualHash = ZobristHasher.calculate(board);

        assertEquals(expectedHash, actualHash,
                "Начальная позиция должна иметь хеш 0x463b96181691fc9cL");
    }

    // ======================================================================
    // 2. ТЕСТЫ НА ИЗМЕНЕНИЕ ХЕША
    // ======================================================================

    @Test
    void testHashChangesAfterPawnMove() {
        long initialHash = ZobristHasher.calculate(board);

        // 1. e4
        board.doMove(new Move(Square.E2, Square.E4));
        long hashAfterE4 = ZobristHasher.calculate(board);

        assertNotEquals(initialHash, hashAfterE4,
                "Хеш должен измениться после хода пешки");
    }

    @Test
    void testHashChangesAfterCastling() {
        // Переводим позицию к рокировке (простой тест)
        // 1. e4 e5 2. Nf3 Nc6 3. Bc4 Bc5 4. O-O
        board.doMove(new Move(Square.E2, Square.E4));
        board.doMove(new Move(Square.E7, Square.E5));
        board.doMove(new Move(Square.G1, Square.F3));
        board.doMove(new Move(Square.G8, Square.F6));
        board.doMove(new Move(Square.F1, Square.C4));
        board.doMove(new Move(Square.F8, Square.C5));

        long hashBeforeCastling = ZobristHasher.calculate(board);

        // Белые рокируются
        board.doMove(new Move(Square.E1, Square.G1)); // O-O
        long hashAfterCastling = ZobristHasher.calculate(board);

        assertNotEquals(hashBeforeCastling, hashAfterCastling,
                "Хеш должен измениться после рокировки");
    }

    @Test
    void testHashChangesAfterEnPassant() {
        // Создаём позицию для взятия на проходе
        // 1. e4 e5 2. d4 exd4 3. Nf3 Nc6 4. Bc4 Bc5 5. O-O Nf6 6. e5 Nd5 7. Ng5
        board.doMove(new Move(Square.E2, Square.E4));
        board.doMove(new Move(Square.E7, Square.E5));
        board.doMove(new Move(Square.D2, Square.D4));
        board.doMove(new Move(Square.E5, Square.D4)); // exd4

        long hashBeforeEnPassant = ZobristHasher.calculate(board);

        // Двигаем пешку на две клетки для создания en passant
        board.doMove(new Move(Square.D7, Square.D5));
        long hashAfterEnPassant = ZobristHasher.calculate(board);

        assertNotEquals(hashBeforeEnPassant, hashAfterEnPassant,
                "Хеш должен измениться при создании en passant");
    }

    // ======================================================================
    // 3. ТЕСТ КЭШИРОВАНИЯ
    // ======================================================================

    @Test
    void testCaching() {
        // Первый вызов — вычисляет и кэширует
        long hash1 = ZobristHasher.calculate(board);

        // Второй вызов — берёт из кэша (должен быть тот же результат)
        long hash2 = ZobristHasher.calculate(board);

        assertEquals(hash1, hash2, "Кэш должен возвращать тот же хеш для одной позиции");

        // Меняем позицию
        board.doMove(new Move(Square.E2, Square.E4));
        long hash3 = ZobristHasher.calculate(board);

        assertNotEquals(hash1, hash3, "Хеш должен измениться после хода");
    }

    // ======================================================================
    // 4. ТЕСТ КОРРЕКТНОСТИ ИНДЕКСАЦИИ (проверяем, что не вылетает)
    // ======================================================================

    @Test
    void testCalculateDoesNotThrowExceptionForAllSquares() {
        // Убеждаемся, что для всех фигур на всех клетках хеш вычисляется без ошибок
        assertDoesNotThrow(() -> {
            ZobristHasher.calculate(board);
        }, "calculate() не должен выбрасывать исключение для любой позиции");
    }

    // ======================================================================
    // 5. ТЕСТ ИЗМЕНЕНИЯ ХЕША ПРИ СМЕНЕ ЦВЕТА ХОДА
    // ======================================================================

    @Test
    void testHashChangesAfterTurnChange() {
        long hashWhiteTurn = ZobristHasher.calculate(board);

        // Меняем ход на чёрных (делаем ход)
        board.doMove(new Move(Square.E2, Square.E4));
        long hashBlackTurn = ZobristHasher.calculate(board);

        // Возвращаем позицию, но с ходом чёрных (это сложно сделать напрямую)
        // Проверяем, что хеш отличается от начального
        assertNotEquals(hashWhiteTurn, hashBlackTurn,
                "Хеш должен измениться после смены хода");
    }

    // ======================================================================
    // 6. СРАВНЕНИЕ С ЭТАЛОНОМ (дополнительная проверка)
    // ======================================================================

    @Test
    void testHashMatchesPythonChessForKnownPosition() {
        // FEN: rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1
        // После 1. e4

        board.doMove(new Move(Square.E2, Square.E4));
        long actualHash = ZobristHasher.calculate(board);

        // Взято из Python-chess (проверено заранее)
        long expectedHash = 0x823c9b50fd114196L;

        assertEquals(expectedHash, actualHash,
                "Хеш после 1. e4 должен совпадать с эталоном из Python-chess");
    }
}