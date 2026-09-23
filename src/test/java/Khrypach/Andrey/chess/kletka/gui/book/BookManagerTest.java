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

import Khrypach.Andrey.chess.kletka.database.model.GameTree;
import Khrypach.Andrey.chess.kletka.gui.model.ParentNode;
import Khrypach.Andrey.chess.kletka.gui.model.RootNode;
import Khrypach.Andrey.chess.kletka.gui.model.Variation;
import Khrypach.Andrey.chess.kletka.gui.settings.AppPreferences;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для BookManager.
 * Используем тестовую книгу Variety.bin из resources/polyglot/.
 */
class BookManagerTest {

    private BookManager bookManager;
    private Path testBookPath;
    private Path emptyDir;

    @BeforeEach
    void setUp() throws Exception {
        // Получаем синглтон
        bookManager = BookManager.getInstance();

        // Сбрасываем состояние
        bookManager.clearBook();
        // Если есть метод clearRecent() - вызываем его
         bookManager.clearRecent();

        // Загружаем тестовую книгу из ресурсов
        var bookUrl = getClass().getResource("/polyglot/Variety.bin");
        assertNotNull(bookUrl, "Test book 'Variety.bin' not found in resources");
        testBookPath = Paths.get(bookUrl.toURI());

        // СОЗДАЁМ ВРЕМЕННУЮ ДИРЕКТОРИЮ
        emptyDir = Files.createTempDirectory("kletka_test_books");
        // Убеждаемся, что директория создана
        assertTrue(Files.exists(emptyDir), "Временная директория должна существовать");
    }

    // ======================================================================
    // 1. ТЕСТЫ ЗАГРУЗКИ КНИГИ
    // ======================================================================

    @Test
    void loadBook_ShouldReturnTrueForValidBook() {
        boolean result = bookManager.loadBook(testBookPath);

        assertTrue(result, "Загрузка валидной книги должна вернуть true");
        assertTrue(bookManager.isBookLoaded(), "Флаг bookLoaded должен быть true");
        assertNotNull(bookManager.getCurrentBookPath(), "Путь к книге не должен быть null");
        assertNotNull(bookManager.getCurrentBookTree(), "Дерево книги не должно быть null");
        assertNotNull(bookManager.getCurrentParser(), "Парсер не должен быть null");
        assertTrue(bookManager.getTotalEntries() > 0, "Количество записей должно быть больше 0");
    }

    @Test
    void loadBook_ShouldReturnFalseForNonExistentBook() {
        Path nonExistentPath = Paths.get("non-existent-book.bin");

        boolean result = bookManager.loadBook(nonExistentPath);

        assertFalse(result, "Загрузка несуществующей книги должна вернуть false");
        assertFalse(bookManager.isBookLoaded(), "Флаг bookLoaded должен быть false");
        assertNull(bookManager.getCurrentBookPath(), "Путь к книге должен быть null");
        assertNull(bookManager.getCurrentBookTree(), "Дерево книги должно быть null");
    }

    @Test
    void loadBook_ShouldReturnFalseForNullPath() {
        boolean result = bookManager.loadBook(null);

        assertFalse(result, "Загрузка с null должна вернуть false");
        assertFalse(bookManager.isBookLoaded(), "Флаг bookLoaded должен быть false");
    }

    // ======================================================================
    // 2. ТЕСТ ВЫГРУЗКИ КНИГИ
    // ======================================================================

    @Test
    void clearBook_ShouldResetAllFields() {
        // Сначала загружаем книгу
        bookManager.loadBook(testBookPath);
        assertTrue(bookManager.isBookLoaded(), "Книга должна быть загружена");

        // Выгружаем
        bookManager.clearBook();

        assertFalse(bookManager.isBookLoaded(), "Флаг bookLoaded должен быть false");
        assertNull(bookManager.getCurrentBookPath(), "Путь к книге должен быть null");
        assertNull(bookManager.getCurrentBookTree(), "Дерево книги должно быть null");
        assertEquals(0, bookManager.getTotalEntries(), "Количество записей должно быть 0");
    }

    // ======================================================================
    // 3. ТЕСТЫ ПОЛУЧЕНИЯ СПИСКА КНИГ
    // ======================================================================

    @Test
    void getAvailableBooks_ShouldReturnListOfBinFiles() throws IOException {
        // Создаём несколько тестовых файлов в пустой директории
        Path book1 = emptyDir.resolve("book1.bin");
        Path book2 = emptyDir.resolve("book2.bin");
        Path notBook = emptyDir.resolve("readme.txt");

        Files.createFile(book1);
        Files.createFile(book2);
        Files.createFile(notBook);

        // Сохраняем директорию в настройках
        AppPreferences.saveBookDirectory(emptyDir.toString());

        List<Path> books = bookManager.getAvailableBooks();

        assertNotNull(books, "Список книг не должен быть null");
        assertEquals(2, books.size(), "Должны быть найдены только .bin файлы");
        assertTrue(books.contains(book1), "Список должен содержать book1.bin");
        assertTrue(books.contains(book2), "Список должен содержать book2.bin");
        assertFalse(books.contains(notBook), "Список НЕ должен содержать readme.txt");
    }

    @Test
    void getAvailableBooks_ShouldReturnEmptyListForNonExistentDirectory() {
        // Устанавливаем несуществующую директорию
        Path nonExistentDir = Paths.get("/non/existent/path/for/test");
        AppPreferences.saveBookDirectory(nonExistentDir.toString());

        List<Path> books = bookManager.getAvailableBooks();

        assertNotNull(books, "Список не должен быть null");
        assertTrue(books.isEmpty(), "Список должен быть пустым для несуществующей директории");
    }

    // ======================================================================
    // 4. ТЕСТЫ ИСТОРИИ (RECENT BOOKS)
    // ======================================================================

    @Test
    void addToRecent_ShouldAddBookToHistory() {
        String path1 = "/path/to/book1.bin";
        String path2 = "/path/to/book2.bin";

        bookManager.addToRecent(path1);
        bookManager.addToRecent(path2);

        List<String> recent = bookManager.getRecentBooks();

        assertEquals(2, recent.size(), "В истории должно быть 2 книги");
        assertEquals(path2, recent.get(0), "Последняя добавленная книга должна быть первой");
        assertEquals(path1, recent.get(1), "Предыдущая книга должна быть второй");
    }

    @Test
    void addToRecent_ShouldNotDuplicateEntries() {
        String path = "/path/to/book.bin";

        bookManager.addToRecent(path);
        bookManager.addToRecent(path);

        List<String> recent = bookManager.getRecentBooks();

        assertEquals(1, recent.size(), "Дубликаты не должны добавляться");
        assertEquals(path, recent.get(0), "Путь должен быть в истории один раз");
    }

    @Test
    void addToRecent_ShouldLimitHistorySize() {
        // Добавляем больше книг, чем MAX_RECENT_BOOKS
        for (int i = 0; i < PolyglotConstants.MAX_RECENT_BOOKS + 5; i++) {
            bookManager.addToRecent("/path/to/book" + i + ".bin");
        }

        List<String> recent = bookManager.getRecentBooks();

        assertEquals(PolyglotConstants.MAX_RECENT_BOOKS, recent.size(),
                "История должна быть ограничена " + PolyglotConstants.MAX_RECENT_BOOKS + " записями");
    }

    // ======================================================================
    // 5. ТЕСТ GET_TOTAL_ENTRIES
    // ======================================================================

    @Test
    void getTotalEntries_ShouldReturn0WhenBookNotLoaded() {
        bookManager.clearBook();

        int total = bookManager.getTotalEntries();

        assertEquals(0, total, "Без загруженной книги должно быть 0 записей");
    }

    @Test
    void getTotalEntries_ShouldReturnCorrectCountAfterLoad() {
        bookManager.loadBook(testBookPath);

        int total = bookManager.getTotalEntries();

        assertTrue(total > 0, "Количество записей должно быть больше 0");
        assertEquals(bookManager.getCurrentParser().getTotalEntries(), total,
                "Значение должно совпадать с данными парсера");
    }

    // ======================================================================
    // 6. ТЕСТ СИНГЛТОНА
    // ======================================================================

    @Test
    void getInstance_ShouldReturnSameInstance() {
        BookManager instance1 = BookManager.getInstance();
        BookManager instance2 = BookManager.getInstance();

        assertSame(instance1, instance2, "Должен возвращаться один и тот же экземпляр");
    }

    // ======================================================================
    // 7. ТЕСТ ЗАГРУЗКИ И ДОСТУПА К ДЕРЕВУ
    // ======================================================================

    @Test
    void loadBook_ShouldCreateEmptyTree() {
        bookManager.loadBook(testBookPath);

        GameTree tree = bookManager.getCurrentBookTree();
        assertNotNull(tree, "Дерево не должно быть null");

        RootNode root = tree.getRootNode();
        assertNotNull(root, "Корневой узел не должен быть null");

        // Ленивая модель: после loadBook дерево пустое
        List<Variation> variations = root.getSubVariations();
        assertTrue(variations.isEmpty(),
                "После loadBook дерево должно быть пустым (ленивая модель)");

        // Главная линия существует как объект, но пустая
        Variation mainLine = tree.getMainLine();
        assertNotNull(mainLine, "Главная линия не должна быть null");
    }

    @Test
    void loadVariationsForNode_ShouldPopulateRootVariations() {
        bookManager.loadBook(testBookPath);

        GameTree tree = bookManager.getCurrentBookTree();
        RootNode root = tree.getRootNode();
        Board startBoard = new Board();

        // Загружаем продолжения из книги для корня
        bookManager.loadVariationsForNode(root, startBoard);

        List<Variation> variations = root.getSubVariations();
        assertFalse(variations.isEmpty(),
                "После loadVariationsForNode должны появиться варианты");

        // Проверяем, что next установлен на самый весомый ход
        ParentNode next = root.getNext();
        assertNotNull(next, "next должен быть установлен после загрузки уровня");
        assertFalse(next.isRoot(), "next не должен быть корнем");

        // Проверяем, что первая variation помечена как mainLine
        boolean hasMainLine = variations.stream().anyMatch(Variation::isMainLine);
        assertTrue(hasMainLine, "Должна быть variation с флагом mainLine");

        // Проверяем, что у узлов проставлен absolutePly
        for (Variation var : variations) {
            ParentNode first = var.getFirstNode();
            assertNotNull(first);
            assertTrue(first.getAbsolutePly() >= 1,
                    "Первый ход должен иметь absolutePly >= 1, получено: "
                            + first.getAbsolutePly());
        }
    }

    @Test
    void loadVariationsForNode_ShouldBeIdempotent() {
        bookManager.loadBook(testBookPath);
        GameTree tree = bookManager.getCurrentBookTree();
        RootNode root = tree.getRootNode();
        Board startBoard = new Board();

        bookManager.loadVariationsForNode(root, startBoard);
        int countAfterFirst = root.getSubVariations().size();

        // Сохраняем ссылки на объекты
        var originalVariations = new ArrayList<>(root.getSubVariations());
        ParentNode originalNext = root.getNext();

        // Повторный вызов
        bookManager.loadVariationsForNode(root, startBoard);

        assertEquals(countAfterFirst, root.getSubVariations().size());
        assertSame(originalNext, root.getNext(),
                "next не должен переустанавливаться при повторной загрузке");
        // Опционально: проверить, что все объекты те же
        for (int i = 0; i < originalVariations.size(); i++) {
            assertSame(originalVariations.get(i), root.getSubVariations().get(i),
                    "Variation[" + i + "] не должен пересоздаваться");
        }
    }

    @Test
    void loadVariationsForNode_ShouldNotLoadWhenNoEntries() {
        bookManager.loadBook(testBookPath);

        // Создаём НОВЫЙ узел (не root), для позиции которой нет в книге
        ParentNode emptyNode = new RootNode();  // или из нового дерева
        Board rareBoard = new Board();
        rareBoard.doMove(new Move(Square.A2, Square.A3));  // 1.a3 — редкий дебют

        bookManager.loadVariationsForNode(emptyNode, rareBoard);

        assertTrue(emptyNode.getSubVariations().isEmpty(),
                "Для позиции вне книги не должно быть вариантов");
    }

    @Test
    void loadVariationsForNode_ShouldLoadForDifferentNodesWithSamePosition() {
        bookManager.loadBook(testBookPath);

        // Позиция после 1.d4 d5 2.Nf3 (путь 1)
        Board board1 = new Board();
        board1.doMove(new Move(Square.D2, Square.D4));
        board1.doMove(new Move(Square.D7, Square.D5));
        board1.doMove(new Move(Square.G1, Square.F3));

        // Позиция после 1.Nf3 d5 2.d4 (путь 2) — ТА ЖЕ позиция
        Board board2 = new Board();
        board2.doMove(new Move(Square.G1, Square.F3));
        board2.doMove(new Move(Square.D7, Square.D5));
        board2.doMove(new Move(Square.D2, Square.D4));

        // Zobrist-ключи должны совпадать
        long key1 = ZobristHasher.calculate(board1);
        long key2 = ZobristHasher.calculate(board2);
        assertEquals(key1, key2,
                "Разные пути к одной позиции должны давать одинаковый Zobrist-ключ");

        // Создаём ДВА разных узла для одной позиции
        GameTree tree1 = new GameTree();
        GameTree tree2 = new GameTree();
        ParentNode node1 = tree1.getRootNode();
        ParentNode node2 = tree2.getRootNode();

        // Загружаем для первого узла
        bookManager.loadVariationsForNode(node1, board1);
        int count1 = node1.getSubVariations().size();
        assertTrue(count1 > 0, "Узел 1 должен получить варианты из книги");

        // Загружаем для ВТОРОГО узла (с тем же ключом!)
        bookManager.loadVariationsForNode(node2, board2);
        int count2 = node2.getSubVariations().size();

        assertEquals(count1, count2,
                "Узел 2 должен получить ТО ЖЕ количество вариантов, что и узел 1");
        assertFalse(node2.getSubVariations().isEmpty(),
                "Узел 2 НЕ должен остаться пустым — регрессия глобального кэша!");
    }

    @Test
    void loadVariationsForNode_ShouldNotShareStateBetweenTrees() {
        bookManager.loadBook(testBookPath);

        // Два независимых дерева
        GameTree tree1 = new GameTree();
        GameTree tree2 = new GameTree();

        ParentNode root1 = tree1.getRootNode();
        ParentNode root2 = tree2.getRootNode();

        Board startBoard = new Board();

        // Загружаем для первого
        bookManager.loadVariationsForNode(root1, startBoard);
        int count1 = root1.getSubVariations().size();

        // Загружаем для второго — должен получить СВОИ варианты
        bookManager.loadVariationsForNode(root2, startBoard);
        int count2 = root2.getSubVariations().size();

        assertTrue(count1 > 0, "root1 должен получить варианты");
        assertEquals(count1, count2,
                "root2 должен получить то же количество вариантов (независимое дерево)");

        // Важно: root1 и root2 имеют РАЗНЫЕ объекты Variation
        assertNotSame(root1.getSubVariations().get(0),
                root2.getSubVariations().get(0),
                "Variation должны быть разными объектами");
    }

    @Test
    void loadVariationsForNode_ShouldSkipIfNodeAlreadyHasVariations() {
        bookManager.loadBook(testBookPath);
        GameTree tree = bookManager.getCurrentBookTree();
        ParentNode root = tree.getRootNode();
        Board startBoard = new Board();

        // Первая загрузка
        bookManager.loadVariationsForNode(root, startBoard);
        int countAfterFirst = root.getSubVariations().size();
        assertTrue(countAfterFirst > 0);

        // Сохраняем ссылку на первую Variation
        Variation firstVariation = root.getSubVariations().get(0);

        // Вторая загрузка — должна быть пропущена
        bookManager.loadVariationsForNode(root, startBoard);

        assertEquals(countAfterFirst, root.getSubVariations().size(),
                "Повторная загрузка не должна добавлять варианты");
        assertSame(firstVariation, root.getSubVariations().get(0),
                "Объекты Variation не должны пересоздаваться");
    }

    // ======================================================================
    // 8. ТЕСТ ПУТИ К КНИГЕ
    // ======================================================================

    @Test
    void getCurrentBookPath_ShouldReturnCorrectPath() {
        bookManager.loadBook(testBookPath);

        Path path = bookManager.getCurrentBookPath();

        assertEquals(testBookPath, path, "Путь к книге должен соответствовать загруженному");
    }

    // ======================================================================
    // 9. ТЕСТ ПАРСЕРА
    // ======================================================================

    @Test
    void getCurrentParser_ShouldReturnParserAfterLoad() {
        // Сначала убеждаемся, что парсера нет
        assertNull(bookManager.getCurrentParser(), "Парсер должен быть null до загрузки");

        // Загружаем книгу
        bookManager.loadBook(testBookPath);

        // Проверяем, что парсер появился
        assertNotNull(bookManager.getCurrentParser(), "Парсер не должен быть null после загрузки");

        // Проверяем, что это тот же парсер, который использовался для загрузки
        PolyglotBookParser parser = bookManager.getCurrentParser();
        assertTrue(parser.getTotalEntries() > 0, "Парсер должен содержать записи");
    }
}