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

package Khrypach.Andrey.chess.kletka.pgn.index.manager;

import Khrypach.Andrey.chess.kletka.database.model.GameData;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.pgn.index.PgnFileEditor;
import Khrypach.Andrey.chess.kletka.pgn.index.model.GameIndexEntry;
import Khrypach.Andrey.chess.kletka.pgn.index.model.PgnIndex;
import Khrypach.Andrey.chess.kletka.pgn.index.operation.BatchOperationResult;
import Khrypach.Andrey.chess.kletka.pgn.index.operation.PgnBatchOperation;
import Khrypach.Andrey.chess.kletka.pgn.index.ui.PgnFileBrowser;
import Khrypach.Andrey.chess.kletka.pgn.index.ui.ProgressDialog;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

/**
 * Централизованный менеджер для управления всеми PGN браузерами.
 * Реализует паттерн Singleton.
 */
public class PgnBrowserManager {

    private static final Logger log = LoggerFactory.getLogger(PgnBrowserManager.class);
    private static final LanguageManager lang = LanguageManager.getInstance();

    // === Singleton ===
    private static final PgnBrowserManager INSTANCE = new PgnBrowserManager();

    public static PgnBrowserManager getInstance() {
        return INSTANCE;
    }

    // === Лимиты ===
    public static final int MAX_BROWSERS = 10;
    public static final int MAX_COPY_GAMES = 100000;  // 100k

    // === Хранилище браузеров ===
    private final Map<Path, PgnFileBrowser> browsers = new LinkedHashMap<>();

    // === Активный браузер ===
    @Getter
    private PgnFileBrowser activeBrowser;

    /**
     * -- GETTER --
     *  Получает содержимое буфера
     */
    // === Буфер обмена ===
    @Getter
    private ClipboardContent clipboardContent;

    // === Слушатели ===
    private final List<Runnable> onBrowserListChanged = new ArrayList<>();

    @Getter
    @Setter
    private Stage ownerStage;

    private PgnBrowserManager() {
        log.debug("Initialized");
    }

    // ========== ОТКРЫТИЕ БРАУЗЕРА ==========

    /**
     * Открывает новый браузер для указанного PGN файла.
     */
    public PgnFileBrowser openBrowser(Path pgnPath, Consumer<GameData> onGameSelected) {
        log.info("Opening browser for: {}", pgnPath);

        // 1. Проверяем, не открыт ли уже этот файл
        if (browsers.containsKey(pgnPath)) {
            PgnFileBrowser existing = browsers.get(pgnPath);
            log.info("File already opened, showing existing browser");
            existing.showWindow();
            return existing;
        }

        // 2. Проверяем лимит
        if (browsers.size() >= MAX_BROWSERS) {
            throw new IllegalStateException(
                    String.format(lang.get(PGN_BROWSER_LIMIT_REACHED), MAX_BROWSERS)
            );
        }

        // 3. Создаем новый браузер с owner
        boolean isFirst = browsers.isEmpty();
        PgnFileBrowser browser = new PgnFileBrowser(pgnPath, ownerStage);

        // 4. Устанавливаем callback для авто-загрузки
        if (isFirst) {
            browser.setOnDataLoaded(() -> {
                log.debug("First browser data loaded, loading first game...");
                browser.loadFirstGame();
            });
        }

        // 5. Показываем браузер
        browser.show(onGameSelected);

        // 6. Сохраняем в карту
        browsers.put(pgnPath, browser);

        // 7. Если это первый браузер - делаем активным
        if (isFirst) {
            browser.setActive(true);
            activeBrowser = browser;
        } else {
            log.debug("Browser opened (not first, no auto-load)");
        }

        // 8. Обновляем заголовки
        updateAllTitles();

        // 9. Уведомляем слушателей
        notifyBrowserListChanged();

        log.info("Browser opened. Total: {}", browsers.size());
        return browser;
    }

    // ========== ЗАКРЫТИЕ БРАУЗЕРА ==========

    /**
     * Закрывает указанный браузер
     */
    public void closeBrowser(PgnFileBrowser browser) {
        if (browser == null) return;

        Path pgnPath = browser.getPgnPath();
        log.info("Closing browser for: {}", pgnPath);

        if (clipboardContent != null && clipboardContent.sourceFile().equals(pgnPath)) {
            log.info("Clearing clipboard - source file is being closed");
            clearClipboard();
        }

        browser.closeWindow();

        browsers.remove(pgnPath);

        if (activeBrowser == browser) {
            activeBrowser = browsers.isEmpty() ? null : browsers.values().iterator().next();
            if (activeBrowser != null) {
                activeBrowser.setActive(true);
                activeBrowser.updateStatus();
            }
        }

        updateAllTitles();
        notifyBrowserListChanged();

        log.info("Browser closed. Total: {}", browsers.size());
    }

    /**
     * Закрывает все браузеры
     */
    public void closeAllBrowsers() {
        log.info("Closing all browsers");

        // Создаем копию списка
        List<PgnFileBrowser> toClose = new ArrayList<>(browsers.values());

        if (toClose.isEmpty()) {
            log.debug("No browsers to close");
            return;
        }

        // Закрываем в UI потоке
        Platform.runLater(() -> {
            for (PgnFileBrowser browser : toClose) {
                try {
                    log.debug("Closing browser: {}", browser.getPgnPath().getFileName());
                    browser.closeWindow();
                } catch (Exception e) {
                    log.warn("Error closing browser: {}", e.getMessage());
                }
            }

            browsers.clear();
            activeBrowser = null;
            clearClipboard();
            notifyBrowserListChanged();

            log.info("All browsers closed ({} closed)", toClose.size());
        });
    }

    // ========== УПРАВЛЕНИЕ АКТИВНЫМ БРАУЗЕРОМ ==========

    /**
     * Устанавливает активный браузер
     */
    public void setActiveBrowser(PgnFileBrowser browser) {
        if (browser == null) {
            activeBrowser = null;
            return;
        }

        if (browser != activeBrowser) {
            if (activeBrowser != null) {
                activeBrowser.setActive(false);
                activeBrowser.updateStatus();
            }

            activeBrowser = browser;
            activeBrowser.setActive(true);
            activeBrowser.updateStatus();

            // ========== ПРИНУДИТЕЛЬНЫЙ ЗАХВАТ ФОКУСА ==========
            Platform.runLater(() -> {
                if (activeBrowser != null && activeBrowser.isShowing()) {
                    Stage stage = activeBrowser.getStage(); // Нужно добавить геттер для stage
                    if (stage != null) {
                        stage.requestFocus();
                        stage.toFront();
                        log.debug("🔧 Focus set to active browser: {}", activeBrowser.getPgnPath().getFileName());
                    }
                }
            });

            updateAllTitles();
            notifyBrowserListChanged();
            log.debug("Active browser: {}", browser.getPgnPath().getFileName());
        }
    }

    // ========== ПОИСК БРАУЗЕРОВ ==========

    /**
     * Проверяет, открыт ли файл
     */
    public boolean isFileOpened(Path pgnPath) {
        return browsers.containsKey(pgnPath);
    }

    /**
     * Получает браузер по пути
     */
    public PgnFileBrowser getBrowser(Path pgnPath) {
        return browsers.get(pgnPath);
    }

    /**
     * Получает все браузеры
     */
    public Collection<PgnFileBrowser> getAllBrowsers() {
        return new ArrayList<>(browsers.values());
    }

    /**
     * Получает количество открытых браузеров
     */
    public int getBrowserCount() {
        return browsers.size();
    }

    // ========== БУФЕР ОБМЕНА ==========

    /**
     * Копирует партии в буфер обмена
     */
    public void copyGames(PgnFileBrowser sourceBrowser, List<GameIndexEntry> entries) {
        if (sourceBrowser == null || entries == null || entries.isEmpty()) {
            log.warn("copyGames: invalid parameters");
            return;
        }

        try {
            // ========== ФИЛЬТРУЕМ УДАЛЁННЫЕ ==========
            List<GameIndexEntry> activeEntries = entries.stream()
                    .filter(entry -> !entry.isDeleted())
                    .collect(Collectors.toList());

            if (activeEntries.isEmpty()) {
                log.warn("No active games to copy (all selected are deleted)");
                return;
            }

            List<String> pgnContents = new ArrayList<>();
            PgnFileEditor editor = new PgnFileEditor(sourceBrowser.getPgnPath(), sourceBrowser.getCurrentIndex());

            for (GameIndexEntry entry : activeEntries) {
                try {
                    String pgn = editor.readGame(entry);
                    if (pgn != null && !pgn.isEmpty()) {
                        pgnContents.add(pgn);
                    }
                } catch (Exception e) {
                    log.error("Failed to read game {}: {}", entry.getId(), e.getMessage());
                }
            }

            if (pgnContents.isEmpty()) {
                log.warn("No PGN content read for copying");
                return;
            }

            // ========== СОХРАНЯЕМ В БУФЕР С PGN СОДЕРЖИМЫМ ==========
            clipboardContent = new ClipboardContent(
                    sourceBrowser.getPgnPath(),
                    activeEntries,
                    pgnContents  // <-- ТЕПЕРЬ ПЕРЕДАЁМ PGN СОДЕРЖИМОЕ
            );

            log.info("Copied {} games to clipboard", pgnContents.size());

            // Обновляем состояние кнопок
            updateAllPasteButtons();

        } catch (Exception e) {
            log.error("Failed to copy games", e);
            throw new RuntimeException("Failed to copy games: " + e.getMessage(), e);
        }
    }

    /**
     * Копирует партии в буфер обмена с прогрессом
     */
    public void copyGamesWithProgress(PgnFileBrowser sourceBrowser, List<GameIndexEntry> entries,
                                      ProgressDialog progressDialog) throws Exception {
        if (entries == null || entries.isEmpty()) {
            log.warn("Cannot copy empty list");
            return;
        }

        if (entries.size() > MAX_COPY_GAMES) {
            throw new IllegalArgumentException(
                    String.format(lang.get(PGN_BROWSER_COPY_LIMIT), MAX_COPY_GAMES)
            );
        }

        int total = entries.size();
        boolean showProgress = total > 100;

        if (showProgress) {
            progressDialog.updateProgress(0, lang.get(PGN_BROWSER_COPY_PREPARING),
                    String.format(lang.get(PGN_BROWSER_COPY_TOTAL), total));
        }

        // Собираем записи в буфер
        clipboardContent = new ClipboardContent(
                sourceBrowser.getPgnPath(),
                new ArrayList<>(entries),
                entries.size(),
                System.currentTimeMillis()
        );

        if (showProgress) {
            progressDialog.updateProgress(1.0,
                    String.format(lang.get(PGN_BROWSER_COPY_COMPLETE), total),
                    String.format(lang.get(PGN_BROWSER_COPY_SOURCE), sourceBrowser.getPgnPath().getFileName()));
            // Задержка, чтобы пользователь увидел завершение
            Thread.sleep(500);
            progressDialog.close();
        }

        log.debug("Copied {} games (big amount)from {}", entries.size(),
                sourceBrowser.getPgnPath().getFileName());

        // Обновляем состояние кнопок во всех браузерах
        updateAllPasteButtons();
    }

    /**
     * Проверяет, можно ли вставить партии в целевой браузер
     */
    public boolean canPaste(PgnFileBrowser targetBrowser) {
        if (clipboardContent == null || clipboardContent.entries().isEmpty()) {
            return false;
        }

        if (targetBrowser == null) {
            return false;
        }

        // Нельзя вставить в тот же файл
        if (clipboardContent.sourceFile().equals(targetBrowser.getPgnPath())) {
            return false;
        }

        // Проверяем, существует ли исходный файл
        return java.nio.file.Files.exists(clipboardContent.sourceFile());
    }

    /**
     * Вставляет партии из буфера обмена
     */
    public int pasteGames(PgnFileBrowser targetBrowser, ClipboardContent content) {
        if (targetBrowser == null || content == null || content.pgnContents().isEmpty()) {
            log.warn("pasteGames: invalid parameters");
            return 0;
        }

        try {
            List<String> activePgns = content.pgnContents().stream()
                    .filter(pgn -> pgn != null && !pgn.isEmpty())
                    .collect(Collectors.toList());

            if (activePgns.isEmpty()) {
                log.warn("No valid PGN content to paste");
                return 0;
            }

            PgnBatchOperation batchOp = new PgnBatchOperation(
                    targetBrowser.getPgnPath(),
                    targetBrowser.getCurrentIndex()
            );

            BatchOperationResult result = batchOp.pasteGamesBatch(activePgns, null);

            // ========== ОБНОВЛЯЕМ ИНДЕКС В БРАУЗЕРЕ ==========
            PgnIndex updatedIndex = batchOp.getIndex();
            if (updatedIndex != null) {
                targetBrowser.setCurrentIndex(updatedIndex);
                log.info("Index updated after paste: {} active entries",
                        updatedIndex.getActiveCount());
            }

            log.info("Pasted {} games", result.successful());
            return result.successful();

        } catch (Exception e) {
            log.error("Failed to paste games", e);
            throw new RuntimeException("Failed to paste games: " + e.getMessage(), e);
        }
    }

    /**
     * Вставляет партии из буфера в целевой браузер с прогрессом
     * Использует пакетную обработку для больших объемов
     */
    public int pasteGamesWithProgress(PgnFileBrowser targetBrowser, ClipboardContent content,
                                      ProgressDialog progressDialog) throws Exception {
        if (content == null || content.entries().isEmpty()) {
            return 0;
        }

        if (!canPaste(targetBrowser)) {
            throw new IllegalStateException(lang.get(PGN_BROWSER_PASTE_UNAVAILABLE));
        }

        int total = content.entries().size();
        Path targetPath = targetBrowser.getPgnPath();

        // ========== ПРОВЕРКА МЕСТА ==========
        checkDiskSpace(targetPath, total);

        boolean showProgress = total > 100;

        if (showProgress) {
            progressDialog.updateProgress(0, lang.get(PGN_BROWSER_PASTE_PREPARING),
                    String.format(lang.get(PGN_BROWSER_PASTE_TOTAL), total));
        }

        log.info("Pasting {} games into {}", total, targetPath.getFileName());

        PgnIndex targetIndex = targetBrowser.getCurrentIndex();
        if (targetIndex == null) {
            throw new IllegalStateException(
                    String.format(lang.get(PGN_BROWSER_NO_INDEX), targetPath)
            );
        }

        // ========== ИСПОЛЬЗУЕМ BATCH ОПЕРАЦИЮ ==========
        PgnBatchOperation batchOp = new PgnBatchOperation(targetPath, targetIndex);
        PgnFileEditor sourceEditor = new PgnFileEditor(content.sourceFile(), null);

        // Собираем все PGN содержимое в список
        List<String> pgnContents = new ArrayList<>(total);

        try {
            // Читаем все партии из исходного файла
            if (showProgress) {
                progressDialog.updateProgress(0.1,
                        lang.get(PGN_BROWSER_PASTE_READING),
                        String.format(lang.get(PGN_BROWSER_PASTE_READING_PROGRESS), 0, total));
            }

            int readCount = 0;
            for (GameIndexEntry entry : content.entries()) {
                try {
                    String pgnContent = sourceEditor.readGame(entry);
                    pgnContents.add(pgnContent);
                    readCount++;

                    if (showProgress && readCount % 100 == 0) {
                        double progress = 0.1 + (0.3 * (double) readCount / total);
                        progressDialog.updateProgress(progress,
                                String.format(lang.get(PGN_BROWSER_PASTE_PROGRESS), readCount, total),
                                String.format(lang.get(PGN_BROWSER_PASTE_ADDED), readCount));
                    }
                } catch (IOException e) {
                    if (e.getMessage().contains("No space left on device") ||
                            e.getMessage().contains("Not enough space")) {
                        throw new IOException(
                                String.format(lang.get(PGN_BROWSER_DISK_SPACE_INSUFFICIENT), readCount)
                        );
                    }
                    throw e;
                }
            }

            if (showProgress) {
                progressDialog.updateProgress(0.4,
                        lang.get(PGN_BROWSER_PASTE_WRITING),
                        String.format(lang.get(PGN_BROWSER_PASTE_TOTAL), total));
            }

            // ========== ПАКЕТНАЯ ВСТАВКА ==========
            BatchOperationResult result = batchOp.pasteGamesBatch(pgnContents, processed -> {
                if (showProgress) {
                    double progress = 0.4 + (0.6 * (double) processed / total);
                    Platform.runLater(() -> progressDialog.updateProgress(progress,
                            String.format(lang.get(PGN_BROWSER_PASTE_PROGRESS), processed, total),
                            String.format(lang.get(PGN_BROWSER_PASTE_ADDED), processed)));
                }
            });

            // ========== ОБНОВЛЯЕМ ИНДЕКС В БРАУЗЕРЕ ==========
            PgnIndex updatedIndex = batchOp.getIndex();
            if (updatedIndex != null) {
                targetBrowser.setCurrentIndex(updatedIndex);
                log.info("Index updated after batch paste: {} active entries",
                        updatedIndex.getActiveCount());
            }

            // ========== ОБНОВЛЕНИЕ UI ==========
            if (result.successful() > 0) {
                // Индекс уже сохранен в batchOp, просто обновляем кэш
                targetIndex.refreshCache();
                Platform.runLater(targetBrowser::refresh);
            }

            if (showProgress) {
                progressDialog.updateProgress(1.0,
                        String.format(lang.get(PGN_BROWSER_PASTE_COMPLETE), result.successful()),
                        String.format(lang.get(PGN_BROWSER_PASTE_TARGET), targetPath.getFileName()));
                Thread.sleep(500);
                progressDialog.close();
            }

            clearClipboard();
            log.info("Successfully pasted {} games (batch)", result.successful());

            // Если были ошибки - бросаем исключение с деталями
            if (!result.isComplete()) {
                throw new IOException(String.format(lang.get(PGN_BROWSER_PASTE_PARTIAL),
                        result.successful(), result.totalRequested(), result.failed()));
            }

            return result.successful();

        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (!errorMessage.contains(lang.get(PGN_BROWSER_DISK_SPACE_ERROR))) {
                errorMessage = String.format(lang.get(PGN_BROWSER_PASTE_INTERRUPTED),
                        pgnContents.size(), e.getMessage());
            }

            if (progressDialog != null) {
                progressDialog.close();
            }

            throw new IOException(errorMessage);
        }
    }

    /**
     * Очищает буфер обмена
     */
    public void clearClipboard() {
        if (clipboardContent != null) {
            log.info("Clearing clipboard (source: {})",
                    clipboardContent.sourceFile().getFileName());
            clipboardContent = null;
            updateAllPasteButtons();
        }
    }

    /**
     * Проверяет, достаточно ли места на диске для вставки
     */
    public void checkDiskSpace(Path targetPath, int estimatedGames) throws IOException {
        File targetFile = targetPath.toFile();
        long freeSpace = targetFile.getFreeSpace();

        // Оцениваем размер: примерно 2KB на партию (с запасом)
        long estimatedSize = estimatedGames * 2048L;

        // Нужно минимум в 2 раза больше места (для временных файлов)
        long requiredSpace = estimatedSize * 2;

        String freeSpaceMB = String.format("%.1f", freeSpace / (1024.0 * 1024.0));
        String requiredSpaceMB = String.format("%.1f", requiredSpace / (1024.0 * 1024.0));

        if (freeSpace < requiredSpace) {
            throw new IOException(
                    lang.get(PGN_BROWSER_DISK_SPACE_ERROR) + "\n\n" +
                            String.format(lang.get(PGN_BROWSER_DISK_SPACE_CHECK),
                                    Double.parseDouble(requiredSpaceMB),
                                    Double.parseDouble(freeSpaceMB))
            );
        }

        log.debug("Disk space OK: {} MB free, need {} MB", freeSpaceMB, requiredSpaceMB);
    }

    /**
     * Проверяет, есть ли партии в буфере
     */
    public boolean hasClipboardContent() {
        return clipboardContent != null && !clipboardContent.entries().isEmpty();
    }

    // ========== ОБНОВЛЕНИЕ UI ==========

    private void updateAllTitles() {
        for (PgnFileBrowser browser : browsers.values()) {
            browser.updateTitle();
        }
    }

    private void updateAllPasteButtons() {
        for (PgnFileBrowser browser : browsers.values()) {
            browser.updateButtonsState();
        }
    }

    /**
     * Уведомляет слушателей об изменении списка браузеров
     */
    public void notifyBrowserListChanged() {
        for (Runnable listener : onBrowserListChanged) {
            try {
                listener.run();
            } catch (Exception e) {
                log.error("Error in listener", e);
            }
        }
    }

    /**
     * Регистрирует слушатель изменений списка браузеров
     */
    public void addBrowserListListener(Runnable listener) {
        if (listener != null) {
            onBrowserListChanged.add(listener);
        }
    }

    // ========== ОБРАБОТКА ЗАКРЫТИЯ БРАУЗЕРА ==========

    /**
     * Вызывается при закрытии браузера (из самого браузера)
     */
    public void onBrowserClosed(PgnFileBrowser browser) {
        // Удаляем из карты, если он там есть
        Path path = browser.getPgnPath();
        if (browsers.containsKey(path) && browsers.get(path) == browser) {
            browsers.remove(path);
        }

        // Если это активный браузер - назначаем новый
        if (activeBrowser == browser) {
            activeBrowser = browsers.isEmpty() ? null : browsers.values().iterator().next();
            if (activeBrowser != null) {
                activeBrowser.setActive(true);
            }
        }

        updateAllTitles();
        notifyBrowserListChanged();
        log.info("Browser closed via callback. Total: {}", browsers.size());
    }

    // ========== ВНУТРЕННИЙ КЛАСС ДЛЯ БУФЕРА (как record) ==========

    public record ClipboardContent(
            Path sourceFile,
            List<GameIndexEntry> entries,
            List<String> pgnContents,
            int count,
            long timestamp
    ) {
        // Конструктор с PGN содержимым
        public ClipboardContent(Path sourceFile, List<GameIndexEntry> entries, List<String> pgnContents) {
            this(
                    sourceFile,
                    List.copyOf(entries),
                    List.copyOf(pgnContents),
                    pgnContents.size(),
                    System.currentTimeMillis()
            );
        }

        // Конструктор без PGN содержимого (для обратной совместимости)
        public ClipboardContent(Path sourceFile, List<GameIndexEntry> entries, int count, long timestamp) {
            this(
                    sourceFile,
                    List.copyOf(entries),
                    new ArrayList<>(),
                    count,
                    timestamp
            );
        }

        public boolean isEmpty() {
            return entries == null || entries.isEmpty() || pgnContents == null || pgnContents.isEmpty();
        }

        @Override
        public String toString() {
            return String.format("ClipboardContent{source=%s, entries=%d, pgns=%d}",
                    sourceFile != null ? sourceFile.getFileName() : "null",
                    entries != null ? entries.size() : 0,
                    pgnContents != null ? pgnContents.size() : 0);
        }
    }
}