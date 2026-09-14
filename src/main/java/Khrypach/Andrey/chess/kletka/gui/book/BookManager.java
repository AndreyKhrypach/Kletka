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
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.model.MoveNode;
import Khrypach.Andrey.chess.kletka.gui.model.ParentNode;
import Khrypach.Andrey.chess.kletka.gui.model.RootNode;
import Khrypach.Andrey.chess.kletka.gui.model.Variation;
import Khrypach.Andrey.chess.kletka.gui.settings.AppPreferences;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Менеджер для управления дебютными книгами Polyglot.
 * Держит mmap-парсер и кэш загруженных позиций.
 */
public class BookManager {

    private static final Logger log = LoggerFactory.getLogger(BookManager.class);
    private static BookManager instance;

    private final LanguageManager lang = LanguageManager.getInstance();

    @Getter
    @Setter
    private Path currentBookPath;
    @Getter
    @Setter
    private GameTree currentBookTree;
    @Getter
    @Setter
    private boolean bookLoaded = false;
    private final List<String> recentBooks = new ArrayList<>();

    @Getter
    @Setter
    private PolyglotBookParser currentParser;

    @Getter
    private int totalEntries;

    @Getter
    private final BookEditor bookEditor = new BookEditor();

    // Кэш загруженных позиций (Zobrist ключи)
    private final Set<Long> loadedKeys = new HashSet<>();

    // Стек для undo
    private final Deque<BookAction> undoStack = new ArrayDeque<>();

    private BookManager() {
        loadRecentBooks();
    }

    public static BookManager getInstance() {
        if (instance == null) {
            instance = new BookManager();
        }
        return instance;
    }

    // ========== КЭШ ЗАГРУЖЕННЫХ ПОЗИЦИЙ ==========

    public boolean isKeyLoaded(long key) {
        return loadedKeys.contains(key);
    }

    public void markKeyLoaded(long key) {
        loadedKeys.add(key);
    }

    // ========== ЗАГРУЗКА / ВЫГРУЗКА КНИГИ ==========

    /**
     * Открывает книгу и создаёт пустое дерево.
     * Дерево наполняется лениво через {@link #loadVariationsForNode(ParentNode, Board)}.
     */
    public boolean loadBook(Path bookPath) {
        if (bookPath == null || !Files.exists(bookPath)) {
            log.warn("Book file not found: {}", bookPath);
            return false;
        }

        try {
            log.info("Opening book (mmap): {}", bookPath);

            PolyglotBookParser parser = new PolyglotBookParser();
            parser.open(bookPath);

            // Пустое дерево
            GameTree tree = new GameTree();
            Board initialBoard = new Board();
            tree.setInitialBoard(initialBoard);
            tree.getRootNode().setSavedFenAfter(initialBoard.getFen());

            this.currentParser = parser;
            this.currentBookPath = bookPath;
            this.currentBookTree = tree;
            this.bookLoaded = true;
            this.totalEntries = parser.getTotalEntries();

            // Сброс кэша — книга другая
            loadedKeys.clear();

            addToRecent(bookPath.toString());
            AppPreferences.saveRecentBook(bookPath.toString());

            log.info("Book opened: {} entries", totalEntries);
            return true;

        } catch (IOException e) {
            log.error("Failed to open book: {}", bookPath, e);
            return false;
        }
    }

    /**
     * Выгружает книгу и закрывает mmap.
     */
    public void clearBook() {
        if (currentParser != null) {
            currentParser.close();
            currentParser = null;
        }
        this.currentBookPath = null;
        this.currentBookTree = null;
        this.bookLoaded = false;
        this.totalEntries = 0;

        loadedKeys.clear();
        clearUndoStack();

        AppPreferences.saveRecentBook(null);
        log.debug("Book cleared");
    }

    /**
     * Сохраняет изменения и перезагружает книгу (mmap закрывается и открывается заново).
     */
    public void saveBook() throws IOException {
        if (!bookEditor.hasUnsavedChanges()) return;

        Path bookPath = getCurrentBookPath();
        if (bookPath == null) return;

        log.info("=== SAVING BOOK ===");
        log.info("dirtyEntries size BEFORE clear: {}", bookEditor.getDirtyCount());

        // 1. СНАЧАЛА закрываем mmap — освобождаем файл
        if (currentParser != null) {
            currentParser.close();
            currentParser = null;
        }
        currentBookTree = null;
        loadedKeys.clear();

        // 2. Даём Windows время освободить handle (после unmap + GC)
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 3. Теперь сохраняем — файл свободен
        PolyglotBookWriter.saveBook(bookPath, bookEditor.getDirtyEntries());

        // 4. Очищаем изменения
        bookEditor.clear();
        clearUndoStack();

        // 5. Перезагружаем книгу
        loadBook(bookPath);

        log.info("=== SAVE COMPLETE ===");
    }

    // ========== ЛЕНИВАЯ ЗАГРУЗКА УРОВНЯ ==========

    /**
     * Загружает ходы из книги для указанного узла.
     * Создаёт подварианты (subVariations) для node.
     *
     * @param node  узел дерева, для которого грузим продолжения
     * @param board позиция в этом узле
     */
    public void loadVariationsForNode(ParentNode node, Board board) {
        if (!bookLoaded || currentParser == null || node == null || board == null) {
            return;
        }

        long key = ZobristHasher.calculate(board);
        if (loadedKeys.contains(key)) {
            log.trace("Level already loaded for key 0x{}", Long.toHexString(key));
            return;
        }

        List<PolyglotEntry> entries = currentParser.findAllEntries(key);
        if (entries.isEmpty()) {
            loadedKeys.add(key);
            log.debug("No entries for key 0x{}", Long.toHexString(key));
            return;
        }

        // Сортировка по весу (убывание) — самые популярные ходы первыми
        entries.sort((a, b) -> Integer.compare(b.weight(), a.weight()));

        int limit = Math.min(entries.size(), PolyglotConstants.MAX_MOVES_PER_POSITION);

        // Собираем уже существующие UCI, чтобы не дублировать
        Set<String> existingUcis = new HashSet<>();
        for (Variation existing : node.getSubVariations()) {
            if (existing == null || existing.isEmpty()) continue;
            ParentNode first = existing.getFirstNode();
            if (first != null && !first.isRoot()) {
                existingUcis.add(first.getUciMove());
            }
        }

        // Также не дублируем next, если он уже установлен
        if (node.getNext() != null && !node.getNext().isRoot()) {
            existingUcis.add(node.getNext().getUciMove());
        }

        Variation parentVariation = node.getOwningVariation();
        if (parentVariation == null) {
            parentVariation = currentBookTree.getRootVariation();
        }

        int variationId = node.getSubVariations().size();
        int addedCount = 0;

        for (int i = 0; i < limit; i++) {
            PolyglotEntry entry = entries.get(i);
            if (entry.weight() < 1) continue;

            Move move = currentParser.createChesslibMove(entry);
            if (!currentParser.isLegalMove(entry, board)) {
                log.trace("Move {} not legal, skipping", entry.getUciMove());
                continue;
            }

            String uci = entry.getUciMove();
            if (existingUcis.contains(uci)) continue;

            int ply = Math.max(1, node.getAbsolutePly() + 1);
            MoveNode moveNode = currentParser.createMoveNode(entry, board, ply);

            Variation subVar = new Variation(
                    currentParser.generateVariationName(moveNode, variationId++)
            );
            subVar.addMove(moveNode);
            subVar.setMainLine(false);
            subVar.setParentVariation(parentVariation);
            subVar.setParentNodeRef(node);

            moveNode.setParent(node);
            moveNode.setForkNode(node);
            moveNode.setOwningVariation(subVar);

            node.getSubVariations().add(subVar);
            existingUcis.add(uci);
            addedCount++;
        }

        // Устанавливаем next = самому весомому ходу (первому варианту), если ещё нет
        if (node.getNext() == null && !node.getSubVariations().isEmpty()) {
            Variation firstVar = node.getSubVariations().get(0);
            ParentNode firstMove = firstVar.getFirstNode();
            if (firstMove != null && !firstMove.isRoot()) {
                node.setNext(firstMove);
                firstVar.setMainLine(true);
            }
        }

        // Помечаем, что уровень загружен (даже если ничего не добавили)
        loadedKeys.add(key);

        log.debug("Loaded {} moves for position 0x{} (total subVariations={})",
                addedCount, Long.toHexString(key), node.getSubVariations().size());
    }

    // ========== ДУБЛИКАТЫ ==========

    /**
     * Проверяет, существует ли ход в книге.
     * Смотрит и в dirtyEntries, и в текущем mmap через loadedKeys/узел.
     */
    public boolean hasMoveInBook(long key, String uciMove) {
        return bookEditor.hasEntry(key, uciMove);
    }

    public boolean addMoveToBook(long key, PolyglotEntry entry) {
        return bookEditor.addEntry(key, entry);
    }

    // ========== UNDO ==========

    public void pushBookAction(BookAction action) {
        if (action == null) return;
        undoStack.push(action);
        log.debug("Book action pushed. Stack size: {}", undoStack.size());
    }

    public boolean canUndoBook() {
        return !undoStack.isEmpty() && bookLoaded;
    }

    public void undoBookAction() {
        if (!canUndoBook()) return;

        BookAction action = undoStack.pop();
        boolean removed = bookEditor.removeEntry(action.key(), action.entry().getUciMove());

        if (removed) {
            log.debug("Entry removed from dirtyEntries");
        } else {
            log.warn("Entry not found in dirtyEntries, cannot undo");
        }
    }

    public BookAction peekBookAction() {
        return undoStack.isEmpty() ? null : undoStack.peek();
    }

    public void clearUndoStack() {
        undoStack.clear();
    }

    // ========== ИНФОРМАЦИЯ ==========

    public List<Path> getAvailableBooks() {
        List<Path> books = new ArrayList<>();
        Path bookDir = Paths.get(AppPreferences.getBookDirectory());

        if (!Files.exists(bookDir)) {
            return books;
        }

        try (Stream<Path> stream = Files.walk(bookDir, 1)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".bin"))
                    .forEach(books::add);
        } catch (IOException e) {
            log.error("Failed to scan book directory: {}", bookDir, e);
        }
        return books;
    }

    public void addToRecent(String path) {
        recentBooks.remove(path);
        recentBooks.add(0, path);
        if (recentBooks.size() > PolyglotConstants.MAX_RECENT_BOOKS) {
            recentBooks.remove(PolyglotConstants.MAX_RECENT_BOOKS);
        }
    }

    private void loadRecentBooks() {
        String recent = AppPreferences.getRecentBook();
        if (recent != null && !recent.isEmpty()) {
            recentBooks.add(recent);
        }
    }

    public List<String> getRecentBooks() {
        return new ArrayList<>(recentBooks);
    }

    public void clearRecent() {
        recentBooks.clear();
    }

    public boolean hasUnsavedChanges() {
        return bookEditor.hasUnsavedChanges();
    }

    public void clearUnsavedChanges() {
        bookEditor.clear();
    }
}