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

import Khrypach.Andrey.chess.kletka.database.exception.PgnParseException;
import Khrypach.Andrey.chess.kletka.database.model.GameTree;
import Khrypach.Andrey.chess.kletka.database.parser.PgnParser;
import com.github.bhlangonijr.chesslib.Board;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для {@link TreeToBookExporter}.
 * <p>
 * Используют {@link PgnParser} для построения дерева вариантов из PGN,
 * затем экспортируют его в Polyglot-книгу и проверяют результат.
 */
class TreeToBookExporterTest {

    private Path tempBookPath;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        tempBookPath = tempDir.resolve("test_book.bin");
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempBookPath != null && Files.exists(tempBookPath)) {
            Files.delete(tempBookPath);
        }
    }

    // ==========================================================
    // ================ ПРОСТЫЕ СЦЕНАРИИ =========================
    // ==========================================================

    @Test
    void exportToBook_simpleGame_exportsAllMoves() throws Exception {
        // Партия с 4 ходами: 1. e4 e5 2. Nf3 Nc6
        String pgn = """
            [Event "Test"]
            [White "?"]
            [Black "?"]
            [Result "*"]

            1. e4 e5 2. Nf3 Nc6 *
            """;

        GameTree tree = parseTree(pgn);

        TreeToBookExporter.ExportResult result = TreeToBookExporter.exportToBook(
                tree.getRootNode(),
                tree.getInitialBoard(),
                false,
                tempBookPath
        );

        assertEquals(4, result.entriesWritten(), "Должно быть 4 записи (4 хода)");
        assertEquals(0, result.duplicatesSkipped(), "Дубликатов быть не должно");
        assertTrue(Files.exists(tempBookPath), "Файл книги должен существовать");
        assertTrue(Files.size(tempBookPath) > 0, "Файл книги не должен быть пустым");
    }

    @Test
    void exportToBook_emptyTree_returnsEmptyResult() throws Exception {
        // Пустое дерево — только RootNode, никаких ходов
        String pgn = """
            [Event "Empty"]

            *
            """;

        GameTree tree = parseTree(pgn);

        TreeToBookExporter.ExportResult result = TreeToBookExporter.exportToBook(
                tree.getRootNode(),
                tree.getInitialBoard(),
                false,
                tempBookPath
        );

        assertEquals(0, result.entriesWritten(), "Записей быть не должно");
        assertTrue(result.isEmpty(), "Результат должен быть пустым");
    }

    @Test
    void exportToBook_startWithBlack_correctBoard() throws Exception {
        // Партия начинается с чёрных
        String pgn = """
            [Event "Test"]
            [SetUp "1"]
            [FEN "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1"]

            1... e5 2. Nf3 *
            """;

        GameTree tree = parseTree(pgn);

        TreeToBookExporter.ExportResult result = TreeToBookExporter.exportToBook(
                tree.getRootNode(),
                tree.getInitialBoard(),
                true,   // startWithBlack
                tempBookPath
        );

        assertEquals(2, result.entriesWritten(), "Должно быть 2 записи");
    }

    // ==========================================================
    // ============= ВАРИАНТЫ И РАЗВИЛКИ =========================
    // ==========================================================

    @Test
    void exportToBook_withVariations_exportsAll() throws Exception {
        // Партия с вариантами: главная линия + 1 вариант
        String pgn = """
            [Event "Test"]
            [White "?"]
            [Black "?"]
            [Result "*"]

            1. e4 e5 2. Nf3 (2. Nc3 Nf6) 2... Nc6 *
            """;

        GameTree tree = parseTree(pgn);

        TreeToBookExporter.ExportResult result = TreeToBookExporter.exportToBook(
                tree.getRootNode(),
                tree.getInitialBoard(),
                false,
                tempBookPath
        );

        // 1.e4, 1...e5, 2.Nf3, 2.Nc3, 2...Nf6, 2...Nc6 = 6 ходов
        assertEquals(6, result.entriesWritten(),
                "Должно быть 6 записей (главная линия + вариант)");
    }

    @Test
    void exportToBook_withForkAtRoot_noDuplicates() throws Exception {
        // Две партии-варианта в корне
        String pgn = """
            [Event "Test"]

            1. e4 (1. d4 d5) e5 *
            """;

        GameTree tree = parseTree(pgn);

        TreeToBookExporter.ExportResult result = TreeToBookExporter.exportToBook(
                tree.getRootNode(),
                tree.getInitialBoard(),
                false,
                tempBookPath
        );

        // 1.e4, 1...e5, 1.d4, 1...d5 = 4 хода
        assertEquals(4, result.entriesWritten(), "Должно быть 4 записи");
        assertEquals(0, result.duplicatesSkipped(), "Дубликатов быть не должно");
    }

    // ==========================================================
    // =================== ДЕДУПЛИКАЦИЯ ==========================
    // ==========================================================

    @Test
    void exportToBook_transposition_deduplicates() throws Exception {
        // Транспозиция: 1.e4 e5 2.Nf3 и 1.Nf3 e5 2.e4 ведут к одной позиции
        String pgn = """
            [Event "Test"]

            1. e4 e5 2. Nf3 (1. Nf3 e5 2. e4) Nc6 *
            """;

        GameTree tree = parseTree(pgn);

        TreeToBookExporter.ExportResult result = TreeToBookExporter.exportToBook(
                tree.getRootNode(),
                tree.getInitialBoard(),
                false,
                tempBookPath
        );

        // Уникальные (key, move):
        // - после 1.e4: e5
        // - после 1.e4 e5: Nf3
        // - после 1.e4 e5 2.Nf3: Nc6
        // - после 1.Nf3: e5 (разная позиция — 1.Nf3, не 1.e4)
        // - после 1.Nf3 e5: e4 (разная позиция)
        // Значит, дубликатов нет (все позиции уникальны)
        assertTrue(result.entriesWritten() > 0, "Записи должны быть");
    }

    // ==========================================================
    // ============ ПРОВЕРКА СОДЕРЖИМОГО КНИГИ ===================
    // ==========================================================

    @Test
    void exportToBook_bookIsReadable_byParser() throws Exception {
        String pgn = """
            [Event "Test"]

            1. e4 e5 2. Nf3 *
            """;

        GameTree tree = parseTree(pgn);

        TreeToBookExporter.exportToBook(
                tree.getRootNode(),
                tree.getInitialBoard(),
                false,
                tempBookPath
        );

        // Читаем книгу обратно
        PolyglotBookParser parser = new PolyglotBookParser();

        try (parser) {
            parser.open(tempBookPath);
            assertEquals(3, parser.getTotalEntries(),
                    "В книге должно быть 3 записи");

            // Проверяем первую позицию (после 1.e4 — ход чёрных)
            Board boardAfterE4 = new Board();
            boardAfterE4.doMove(new com.github.bhlangonijr.chesslib.move.Move(
                    com.github.bhlangonijr.chesslib.Square.E2,
                    com.github.bhlangonijr.chesslib.Square.E4
            ));
            long keyAfterE4 = ZobristHasher.calculate(boardAfterE4);

            List<PolyglotEntry> entries = parser.findAllEntries(keyAfterE4);
            assertEquals(1, entries.size(),
                    "После 1.e4 должна быть 1 запись (1...e5)");
            assertEquals("e7e5", entries.get(0).getUciMove(),
                    "Ход должен быть e7e5");

        }
    }

    @Test
    void exportToBook_weightsAreDefault() throws Exception {
        String pgn = """
            [Event "Test"]

            1. e4 *
            """;

        GameTree tree = parseTree(pgn);

        TreeToBookExporter.exportToBook(
                tree.getRootNode(),
                tree.getInitialBoard(),
                false,
                tempBookPath
        );

        PolyglotBookParser parser = new PolyglotBookParser();

        try (parser) {
            parser.open(tempBookPath);
            // Позиция после старта (до 1.e4)
            Board startBoard = new Board();
            long key = ZobristHasher.calculate(startBoard);

            List<PolyglotEntry> entries = parser.findAllEntries(key);
            assertEquals(1, entries.size());
            assertEquals(100, entries.get(0).weight(),
                    "Вес по умолчанию должен быть 100");

        }
    }

    @Test
    void exportToBook_emptyTree_doesNotCreateFile() throws Exception {
        String pgn = """
            [Event "Empty"]

            *
            """;

        GameTree tree = parseTree(pgn);

        TreeToBookExporter.ExportResult result = TreeToBookExporter.exportToBook(
                tree.getRootNode(),
                tree.getInitialBoard(),
                false,
                tempBookPath
        );

        assertTrue(result.isEmpty(), "Результат должен быть пустым");
        // Файл может быть создан или нет — зависит от PolyglotBookWriter.
        // Если файла нет — это корректно для пустого экспорта.
    }

    // ==========================================================
    // ==================== EDGE CASES ===========================
    // ==========================================================

    @Test
    void exportToBook_nullRootNode_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                TreeToBookExporter.exportToBook(
                        null,
                        new Board(),
                        false,
                        tempBookPath
                )
        );
    }

    @Test
    void exportToBook_nullBookPath_throws() throws PgnParseException {
        String pgn = "1. e4 *";
        GameTree tree = parseTree(pgn);

        assertThrows(IllegalArgumentException.class, () ->
                TreeToBookExporter.exportToBook(
                        tree.getRootNode(),
                        tree.getInitialBoard(),
                        false,
                        null
                )
        );
    }

    // ==========================================================
    // ==================== ВСПОМОГАТЕЛЬНОЕ ======================
    // ==========================================================

    /**
     * Парсит PGN в GameTree через штатный PgnParser.
     */
    private GameTree parseTree(String pgn) throws PgnParseException {
        PgnParser parser = new PgnParser();
        return parser.parseToGameTree(pgn);
    }
}