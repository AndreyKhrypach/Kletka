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

package Khrypach.Andrey.chess.kletka.gui.book;

import Khrypach.Andrey.chess.kletka.database.model.GameTree;
import Khrypach.Andrey.chess.kletka.gui.model.MoveNode;
import Khrypach.Andrey.chess.kletka.gui.model.ParentNode;
import Khrypach.Andrey.chess.kletka.gui.model.Variation;
import Khrypach.Andrey.chess.kletka.gui.settings.AppPreferences;
import com.github.bhlangonijr.chesslib.Board;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
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

    @Getter
    private boolean temporaryMultiBook = false;
    @Getter
    private Path temporaryMultiBookSourcePgn = null;
    @Getter
    private int temporaryMultiBookGameCount = 0;

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

        if (currentParser != null) {
            currentParser.close();
            currentParser = null;
        }

        try {
            log.debug("Opening book (mmap): {}", bookPath);

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

            addToRecent(bookPath.toString());
            AppPreferences.saveRecentBook(bookPath.toString());

            log.trace("Book opened: {} entries", totalEntries);
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
        log.info("[BOOK] clearBook called. hasUnsavedChanges before: {}",
                bookEditor.hasUnsavedChanges());

        if (currentParser != null) {
            currentParser.close();
            currentParser = null;
        }
        this.currentBookPath = null;
        this.currentBookTree = null;
        this.bookLoaded = false;
        this.totalEntries = 0;

        clearUndoStack();

        // Если это временная мультикнига — удаляем файл при выгрузке
        if (temporaryMultiBook) {
            discardTemporaryMultiBook();
            log.info("[BOOK] Temporary multi-book discarded on clearBook");
            return;  // дальше обычная очистка не нужна — discard всё сделал
        }

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

        log.debug("=== SAVING BOOK ===");
        log.debug("dirtyEntries size BEFORE clear: {}", bookEditor.getDirtyCount());

        // 1. СНАЧАЛА закрываем mmap — освобождаем файл
        if (currentParser != null) {
            currentParser.close();
            currentParser = null;
        }
        currentBookTree = null;

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

        log.debug("=== SAVE COMPLETE ===");
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


        if (!node.getSubVariations().isEmpty()) {
            return;
        }

        long key = ZobristHasher.calculate(board);
        List<PolyglotEntry> entries = currentParser.findAllEntries(key);

        if (entries.isEmpty()) {
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

        for (int i = 0; i < limit; i++) {
            PolyglotEntry entry = entries.get(i);
            if (entry.weight() < 1) continue;

            if (!currentParser.isLegalMove(entry, board)) {
                log.debug("[BOOK]   move {} NOT LEGAL on board, skipping", entry.getUciMove());
                continue;
            }

            String uci = entry.getUciMove();
            if (existingUcis.contains(uci)) {
                continue;
            }

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

    //===Мультизагрузка партий - временная полиглот книга===

    /**
     * Помечает загруженную книгу как временную мультикнигу.
     */
    public void markAsTemporaryMultiBook(Path sourcePgn, int gameCount) {
        this.temporaryMultiBook = true;
        this.temporaryMultiBookSourcePgn = sourcePgn;
        this.temporaryMultiBookGameCount = gameCount;
        log.info("[MULTI-BOOK] Marked as temporary: source={}, games={}",
                sourcePgn, gameCount);
    }

    /**
     * Превращает временную мультикнигу в постоянную.
     *
     * @param targetPath     куда переименовать; если null — <base>_MultiGames.bin рядом с PGN
     * @param allowOverwrite если true — перезаписать существующий файл;
     *                       если false — при конфликте добавить (1), (2), ...
     * @return фактический путь
     */
    public Path promoteTemporaryToPermanent(Path targetPath, boolean allowOverwrite) throws IOException {
        if (!temporaryMultiBook || currentBookPath == null) {
            throw new IllegalStateException("Not a temporary multi-book");
        }

        // Применяем dirtyEntries
        if (bookEditor.hasUnsavedChanges()) {
            saveBook();
        }

        // Закрываем mmap перед move (Windows)
        if (currentParser != null) {
            currentParser.close();
            currentParser = null;
        }

        Path target = targetPath != null ? targetPath : buildDefaultTargetPath();

        // Автопереименование — только для автоматического сохранения
        if (!allowOverwrite) {
            target = findAvailableTarget(target);
        }

        if (target.getParent() != null && !Files.exists(target.getParent())) {
            Files.createDirectories(target.getParent());
        }

        Path oldPath = currentBookPath;
        try {
            if (allowOverwrite) {
                Files.move(oldPath, target,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } else {
                Files.move(oldPath, target, StandardCopyOption.ATOMIC_MOVE);
            }
        } catch (AtomicMoveNotSupportedException e) {
            if (allowOverwrite) {
                Files.move(oldPath, target, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.move(oldPath, target);
            }
        }

        log.info("[MULTI-BOOK] Promoted: {} → {} (overwrite={})", oldPath, target, allowOverwrite);

        // Сбрасываем флаг
        temporaryMultiBook = false;
        temporaryMultiBookSourcePgn = null;
        temporaryMultiBookGameCount = 0;
        currentBookTree = null;
        bookLoaded = false;
        totalEntries = 0;
        clearUndoStack();

        loadBook(target);
        return target;
    }

    /** Backward-compatible: авто-сохранение с авто-переименованием. */
    public Path promoteTemporaryToPermanent(Path targetPath) throws IOException {
        return promoteTemporaryToPermanent(targetPath, false);
    }

    private Path buildDefaultTargetPath() {
        String base = temporaryMultiBookSourcePgn != null
                ? temporaryMultiBookSourcePgn.getFileName().toString()
                : currentBookPath.getFileName().toString();
        int dot = base.lastIndexOf('.');
        if (dot > 0) base = base.substring(0, dot);

        Path dir = temporaryMultiBookSourcePgn != null && temporaryMultiBookSourcePgn.getParent() != null
                ? temporaryMultiBookSourcePgn.getParent()
                : currentBookPath.getParent();

        return dir.resolve(base + "_MultiGames.bin");
    }

    private Path findAvailableTarget(Path target) {
        if (!Files.exists(target)) return target;

        String fileName = target.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String base = dot > 0 ? fileName.substring(0, dot) : fileName;
        String ext = dot > 0 ? fileName.substring(dot) : "";

        Path dir = target.getParent();
        if (dir == null) return target;

        int n = 1;
        while (n < 10000) {
            Path candidate = dir.resolve(base + "(" + n + ")" + ext);
            if (!Files.exists(candidate)) return candidate;
            n++;
        }
        return target;   // fallback
    }

    /**
     * Удаляет временную мультикнигу (пользователь отказался сохранять).
     */
    public void discardTemporaryMultiBook() {
        if (!temporaryMultiBook) return;

        Path tempPath = currentBookPath;

        if (currentParser != null) {
            currentParser.close();
            currentParser = null;
        }
        currentBookPath = null;
        currentBookTree = null;
        bookLoaded = false;
        totalEntries = 0;
        clearUndoStack();

        temporaryMultiBook = false;
        temporaryMultiBookSourcePgn = null;
        temporaryMultiBookGameCount = 0;

        if (tempPath != null) {
            try {
                Files.deleteIfExists(tempPath);
                log.info("[MULTI-BOOK] Discarded temp file: {}", tempPath);
            } catch (IOException e) {
                log.warn("[MULTI-BOOK] Failed to delete temp file {}: {}", tempPath, e.getMessage());
            }
        }
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