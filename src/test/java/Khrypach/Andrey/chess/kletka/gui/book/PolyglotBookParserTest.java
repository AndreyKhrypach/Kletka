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

import Khrypach.Andrey.chess.kletka.gui.model.MoveNode;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для PolyglotBookParser (ленивая загрузка через mmap).
 * Использует тестовую книгу Variety.bin из resources/polyglot/.
 */
class PolyglotBookParserTest {

    private PolyglotBookParser parser;
    private Path testBookPath;

    @BeforeEach
    void setUp() throws URISyntaxException {
        parser = new PolyglotBookParser();

        // Загружаем тестовую книгу из ресурсов
        URL bookUrl = getClass().getResource("/polyglot/Variety.bin");
        assertNotNull(bookUrl, "Test book 'Variety.bin' not found in resources");
        testBookPath = Paths.get(bookUrl.toURI());
    }

    @AfterEach
    void tearDown() {
        if (parser != null) {
            parser.close();
        }
    }

    // ======================================================================
    // 1. ТЕСТ ОТКРЫТИЯ КНИГИ
    // ======================================================================

    @Test
    void open_ShouldLoadBookAndReportEntryCount() throws IOException {
        parser.open(testBookPath);

        int totalEntries = parser.getTotalEntries();
        assertTrue(totalEntries > 0,
                "Книга должна содержать записи (найдено: " + totalEntries + ")");
        assertTrue(totalEntries > 1000,
                "В тестовой книге должно быть много записей, найдено: " + totalEntries);
    }

    @Test
    void open_ShouldThrowIOExceptionForInvalidPath() {
        Path invalidPath = Paths.get("non-existent-file.bin");

        assertThrows(IOException.class, () -> parser.open(invalidPath),
                "Для несуществующего файла должно быть выброшено IOException");
    }

    // ======================================================================
    // 2. ТЕСТ findAllEntries
    // ======================================================================

    @Test
    void findAllEntries_ShouldReturnEntriesForStartPosition() throws IOException {
        parser.open(testBookPath);

        // Вычисляем хеш начальной позиции
        Board startBoard = new Board();
        long startKey = ZobristHasher.calculate(startBoard);

        List<PolyglotEntry> entries = parser.findAllEntries(startKey);

        assertNotNull(entries, "Список записей не должен быть null");
        assertFalse(entries.isEmpty(), "Для начальной позиции должны быть записи");

        // Все записи должны иметь правильный ключ
        for (PolyglotEntry entry : entries) {
            assertEquals(startKey, entry.key(),
                    "Все записи должны иметь ключ начальной позиции");
        }
    }

    @Test
    void findAllEntries_ShouldReturnEmptyListForUnknownKey() throws IOException {
        parser.open(testBookPath);

        long unknownKey = 0x0000000000000001L;
        List<PolyglotEntry> entries = parser.findAllEntries(unknownKey);

        assertNotNull(entries, "Список не должен быть null для неизвестного ключа");
        assertTrue(entries.isEmpty(), "Для неизвестного ключа должен возвращаться пустой список");
    }

    @Test
    void findAllEntries_ShouldReturnEmptyListWhenNotOpened() {
        // parser ещё не открыт (или закрыт)
        List<PolyglotEntry> entries = parser.findAllEntries(0x1234567890ABCDEFL);

        assertNotNull(entries, "Список не должен быть null");
        assertTrue(entries.isEmpty(), "Для неоткрытого парсера должен быть пустой список");
    }

    // ======================================================================
    // 3. ТЕСТ createChesslibMove
    // ======================================================================

    @Test
    void createChesslibMove_ShouldCreateMoveWithoutPromotion() {
        short move = (short) ((12 << 6) | 28); // from=E2, to=E4
        PolyglotEntry entry = new PolyglotEntry(0L, move, (short) 0, 0, 0);

        Move chessMove = parser.createChesslibMove(entry);

        assertNotNull(chessMove, "Ход не должен быть null");
        assertEquals(Square.E2, chessMove.getFrom(), "Начальная клетка должна быть E2");
        assertEquals(Square.E4, chessMove.getTo(), "Конечная клетка должна быть E4");
        assertEquals(Piece.NONE, chessMove.getPromotion(), "Превращение должно быть NONE");
    }

    @Test
    void createChesslibMove_ShouldCreateMoveWithPromotion() {
        short move = (short) ((48 << 6) | 56 | (4 << 12)); // from=A7, to=A8, promo=Q
        PolyglotEntry entry = new PolyglotEntry(0L, move, (short) 0, 0, 0);

        Move chessMove = parser.createChesslibMove(entry);

        assertNotNull(chessMove, "Ход не должен быть null");
        assertEquals(Square.A7, chessMove.getFrom(), "Начальная клетка должна быть A7");
        assertEquals(Square.A8, chessMove.getTo(), "Конечная клетка должна быть A8");
        assertEquals(Piece.WHITE_QUEEN, chessMove.getPromotion(),
                "Должно быть превращение в ферзя");
    }

    // ======================================================================
    // 4. ТЕСТ generateVariationName
    // ======================================================================

    @Test
    void generateVariationName_ShouldUseSanForNamedNode() {
        Move move = new Move(Square.E2, Square.E4, Piece.NONE);
        MoveNode node = new MoveNode(move, Piece.WHITE_PAWN, false, null);
        node.setSan("e4");

        String name = parser.generateVariationName(node, 1);

        assertEquals("e4 (1)", name, "Имя должно содержать SAN и ID");
    }

    @Test
    void generateVariationName_ShouldGenerateNameWithSan() {
        Move move = new Move(Square.E2, Square.E4, Piece.NONE);
        MoveNode node = new MoveNode(move, Piece.WHITE_PAWN, false, null);

        String name = parser.generateVariationName(node, 42);

        assertEquals("e4 (42)", name,
                "Имя должно содержать SAN и ID в скобках");
    }

    @Test
    void generateVariationName_ShouldRemoveSpecialCharacters() {
        Board board = new Board();
        board.doMove(new Move(Square.E2, Square.E4));
        board.doMove(new Move(Square.E7, Square.E5));
        board.doMove(new Move(Square.G1, Square.F3));
        board.doMove(new Move(Square.D7, Square.D6));
        board.doMove(new Move(Square.F1, Square.B5));
        board.doMove(new Move(Square.B8, Square.C6));

        Move move = new Move(Square.B5, Square.C6, Piece.NONE);
        board.doMove(move);

        MoveNode node = new MoveNode(move, Piece.WHITE_BISHOP, true, Piece.NONE);
        node.setSavedFenBefore("r1bqkbnr/1ppp1ppp/p1B5/4p3/4P3/5N2/PPPP1PPP/RNBQK2R w KQkq - 0 4");

        String name = parser.generateVariationName(node, 7);

        assertTrue(name.startsWith("Bxc6"),
                "Имя должно начинаться с Bxc6, получено: " + name);
        assertTrue(name.contains("(7)"),
                "Имя должно содержать ID в скобках");
        assertFalse(name.contains("+"),
                "Имя не должно содержать '+', получено: " + name);
        assertTrue(name.contains("x"),
                "Имя должно содержать 'x', получено: " + name);
    }

    @Test
    void generateVariationName_ShouldFallbackForEmptySan() {
        Move move = new Move(Square.E2, Square.E4, Piece.NONE);
        MoveNode node = new MoveNode(move, Piece.WHITE_PAWN, false, null);
        // не задаём savedFenBefore и san — getSan() вернёт fallback

        String name = parser.generateVariationName(node, 99);

        assertTrue(name.endsWith("(99)"), "Имя должно заканчиваться на (99)");
    }

    // ======================================================================
    // 5. ТЕСТ createMoveNode
    // ======================================================================

    @Test
    void createMoveNode_ShouldCreateNodeFromEntry() {
        Board board = new Board();
        short move = (short) ((12 << 6) | 28); // e2-e4
        PolyglotEntry entry = new PolyglotEntry(0L, move, (short) 16384, 0, 100);

        MoveNode node = parser.createMoveNode(entry, board, 1);

        assertNotNull(node, "Узел не должен быть null");
        assertEquals(1, node.getAbsolutePly(), "Ply должен быть 1");
        assertNotNull(node.getMove(), "Ход не должен быть null");
        assertEquals(Square.E2, node.getMove().getFrom(), "Начальная клетка должна быть E2");
        assertEquals(Square.E4, node.getMove().getTo(), "Конечная клетка должна быть E4");
        assertEquals(Piece.WHITE_PAWN, node.getPiece(), "Фигура должна быть белой пешкой");

        // Проверяем комментарий с статистикой
        String comment = node.getComment();
        assertNotNull(comment, "Комментарий не должен быть null");
        assertTrue(comment.contains("weight:16384"), "Комментарий должен содержать вес");
        assertTrue(comment.contains("rating:"), "Комментарий должен содержать рейтинг");

        // Проверяем FEN
        assertNotNull(node.getSavedFenBefore(), "savedFenBefore должен быть установлен");
        assertNotNull(node.getSavedFenAfter(), "savedFenAfter должен быть установлен");
    }

    @Test
    void createMoveNode_ShouldSetFenBeforeAndAfter() {
        Board board = new Board();
        String fenBefore = board.getFen();

        short move = (short) ((12 << 6) | 28); // e2-e4
        PolyglotEntry entry = new PolyglotEntry(0L, move, (short) 16384, 0, 100);

        MoveNode node = parser.createMoveNode(entry, board, 1);

        assertEquals(fenBefore, node.getSavedFenBefore(),
                "savedFenBefore должен совпадать с FEN доски");
        assertNotEquals(fenBefore, node.getSavedFenAfter(),
                "savedFenAfter должен отличаться от FEN доски");
    }

    // ======================================================================
    // 6. ТЕСТ getTotalEntries
    // ======================================================================

    @Test
    void getTotalEntries_ShouldReturnCorrectCount() throws IOException {
        parser.open(testBookPath);

        int totalEntries = parser.getTotalEntries();
        assertTrue(totalEntries > 0, "Количество записей должно быть больше 0");
        assertTrue(totalEntries > 1000,
                "В тестовой книге должно быть много записей, найдено: " + totalEntries);
    }

    // ======================================================================
    // 7. ИНТЕГРАЦИОННЫЙ ТЕСТ
    // ======================================================================

    @Test
    void fullOpenCycle_ShouldNotThrowExceptions() {
        assertDoesNotThrow(() -> {
            parser.open(testBookPath);
            assertTrue(parser.getTotalEntries() > 0);
            parser.close();
        }, "Полный цикл открытия/закрытия не должен выбрасывать исключения");
    }

    @Test
    void close_ShouldBeIdempotent() throws IOException {
        parser.open(testBookPath);
        parser.close();
        // Повторный close не должен бросать
        assertDoesNotThrow(() -> parser.close());
    }
}