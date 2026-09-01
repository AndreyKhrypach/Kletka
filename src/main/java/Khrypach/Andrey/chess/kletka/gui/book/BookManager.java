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
import Khrypach.Andrey.chess.kletka.gui.settings.AppPreferences;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Менеджер для управления дебютными книгами Polyglot
 */
public class BookManager {

    private static final Logger log = LoggerFactory.getLogger(BookManager.class);
    private static BookManager instance;

    /**
     * -- GETTER --
     *  Получает путь к текущей книге
     */
    @Getter
    @Setter
    private Path currentBookPath;
    /**
     * -- GETTER --
     *  Получает текущее дерево книги
     */
    @Getter
    @Setter
    private GameTree currentBookTree;
    /**
     * -- GETTER --
     *  Проверяет, загружена ли книга
     */
    @Getter
    @Setter
    private boolean bookLoaded = false;
    private final List<String> recentBooks = new ArrayList<>();

    @Getter
    private PolyglotBookParser currentParser;

    private int totalEntries;


    private BookManager() {
        loadRecentBooks();
    }

    public static BookManager getInstance() {
        if (instance == null) {
            instance = new BookManager();
        }
        return instance;
    }

    /**
     * Загружает книгу по указанному пути
     */
    public boolean loadBook(Path bookPath) {
        if (bookPath == null || !Files.exists(bookPath)) {
            log.warn("Book file not found: {}", bookPath);
            return false;
        }

        try {
            log.info("Loading book: {}", bookPath);
            PolyglotBookParser parser = new PolyglotBookParser();
            GameTree tree = parser.parse(bookPath);

            if (tree == null || tree.isEmpty()) {
                log.warn("Book is empty or could not be parsed: {}", bookPath);
                return false;
            }

            this.currentParser = parser;
            this.currentBookPath = bookPath;
            this.currentBookTree = tree;
            this.bookLoaded = true;

            this.totalEntries = parser.getTotalEntries();
            log.info("Book loaded: {} entries", totalEntries);

            addToRecent(bookPath.toString());
            AppPreferences.saveRecentBook(bookPath.toString());

            return true;

        } catch (IOException e) {
            log.error("Failed to load book: {}", bookPath, e);
            return false;
        }
    }

    /**
     * Выгружает текущую книгу
     */
    public void clearBook() {
        this.currentBookPath = null;
        this.currentBookTree = null;
        this.bookLoaded = false;
        this.currentParser = null;
        AppPreferences.saveRecentBook(null);
        this.totalEntries = 0;
        log.debug("Book cleared");
    }

    /**
     * Получает список доступных книг из папки
     */
    public List<Path> getAvailableBooks() {
        List<Path> books = new ArrayList<>();
        Path bookDir = Paths.get(AppPreferences.getBookDirectory());

        if (!Files.exists(bookDir)) {
            return books;
        }

        // try-with-resources гарантирует закрытие стрима
        try (Stream<Path> stream = Files.walk(bookDir, 1)) {
            stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".bin"))
                    .forEach(books::add);
        } catch (IOException e) {
            log.error("Failed to scan book directory: {}", bookDir, e);
        }

        return books;
    }

    /**
     * Добавляет книгу в историю
     */
    public void addToRecent(String path) {
        recentBooks.remove(path);
        recentBooks.add(0, path);
        if (recentBooks.size() > PolyglotConstants.MAX_RECENT_BOOKS) {
            recentBooks.remove(PolyglotConstants.MAX_RECENT_BOOKS);
        }
    }

    /**
     * Загружает список последних книг из настроек
     */
    private void loadRecentBooks() {
        String recent = AppPreferences.getRecentBook();
        if (recent != null && !recent.isEmpty()) {
            recentBooks.add(recent);
        }
    }

    /**
     * Получает историю книг
     */
    public List<String> getRecentBooks() {
        return new ArrayList<>(recentBooks);
    }

    public int getTotalEntries() {
        if (currentBookTree == null) return 0;
        // Можно получить из парсера, если сохранить его
        return totalEntries;
    }

    /**
     * Очищает историю книг (для тестов)
     */
    public void clearRecent() {
        recentBooks.clear();
    }
}