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

package Khrypach.Andrey.chess.kletka.gui.controllers;

import Khrypach.Andrey.chess.kletka.database.exception.PgnException;
import Khrypach.Andrey.chess.kletka.database.model.GameData;
import Khrypach.Andrey.chess.kletka.database.model.GameTree;
import Khrypach.Andrey.chess.kletka.database.parser.PgnParser;
import Khrypach.Andrey.chess.kletka.database.repository.FileSystemRepository;
import Khrypach.Andrey.chess.kletka.database.repository.GameRepository;
import Khrypach.Andrey.chess.kletka.database.service.PgnService;
import Khrypach.Andrey.chess.kletka.engine.UciEngineManager;
import Khrypach.Andrey.chess.kletka.gui.board.*;
import Khrypach.Andrey.chess.kletka.gui.dialogs.EngineSetupDialog;
import Khrypach.Andrey.chess.kletka.gui.dialogs.PositionSetupDialog;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.logo.LogoGenerator;
import Khrypach.Andrey.chess.kletka.gui.menu.MenuBarFactory;
import Khrypach.Andrey.chess.kletka.gui.dialogs.SaveGameDialog;
import Khrypach.Andrey.chess.kletka.gui.settings.AppPreferences;
import Khrypach.Andrey.chess.kletka.pgn.index.PgnFileEditor;
import Khrypach.Andrey.chess.kletka.pgn.index.PgnIndexManager;
import Khrypach.Andrey.chess.kletka.pgn.index.PgnIndexingFacade;
import Khrypach.Andrey.chess.kletka.pgn.index.manager.PgnBrowserManager;
import Khrypach.Andrey.chess.kletka.pgn.index.model.GameIndexEntry;
import Khrypach.Andrey.chess.kletka.pgn.index.model.IndexStatus;
import Khrypach.Andrey.chess.kletka.pgn.index.model.PgnIndex;
import Khrypach.Andrey.chess.kletka.pgn.index.operation.PgnGameOperation;
import Khrypach.Andrey.chess.kletka.pgn.index.ui.IndexingProgressDialog;
import Khrypach.Andrey.chess.kletka.pgn.index.ui.PgnFileBrowser;
import Khrypach.Andrey.chess.kletka.pgn.index.util.HashUtils;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);
    private static final String DEFAULT_DATABASE_DIR = "chess_games";

    private final LanguageManager lang = LanguageManager.getInstance();

    @Getter
    private final UciEngineManager engineManager = UciEngineManager.getInstance();
    @Getter
    private final ChessBoardView boardView;
    private final Stage primaryStage;
    @Getter
    private final BoardSizeController sizeController;
    @Getter
    private MenuBarFactory menuFactory;
    @Setter
    private NotationView notationView;

    private PgnService pgnService;
    private GameRepository gameRepository;

    private IndexingProgressDialog progressDialog;
    @Getter
    private int loadedGameHash;
    private int startBodyHash = 0;
    private int loadedFullHash = 0;

    public MainController(ChessBoardView boardView, Stage primaryStage) {
        log.info("Initializing MainController");
        this.boardView = boardView;
        this.primaryStage = primaryStage;
        this.sizeController = new BoardSizeController(primaryStage, boardView::refreshBoard);

        PgnBrowserManager.getInstance().setOwnerStage(primaryStage);

        initPgnService();

        this.menuFactory = new MenuBarFactory(this, primaryStage, sizeController);

        if (engineManager == null || !engineManager.isEngineRunning()) {
            initEngine();
        }

        primaryStage.setOnCloseRequest(event -> {
            if (hasBodyChanges()) {
                event.consume();
                showSaveDialogBeforeNewGameWithCallback(() -> {
                    log.info("Primary stage closing...");
                    closePgnBrowser();
                    if (engineManager != null) {
                        engineManager.stopEngine();
                    }
                    Platform.exit();
                });
                return;
            }

            log.info("Primary stage closing...");
            closePgnBrowser();
            if (engineManager != null) {
                engineManager.stopEngine();
            }
            Platform.exit();
        });
    }

    private void initPgnService() {
        Path fallbackPath = Paths.get(DEFAULT_DATABASE_DIR);
        try {
            String userHome = System.getProperty("user.home");
            String dbPath = userHome + File.separator + ".chess-kletka" + File.separator + DEFAULT_DATABASE_DIR;

            Path dbDirPath = Paths.get(dbPath);
            try {
                Files.createDirectories(dbDirPath);
            } catch (IOException e) {
                log.warn("Could not create directory in user home: {}", e.getMessage());
                // Используем текущую директорию как fallback
                dbDirPath = fallbackPath;
                Files.createDirectories(dbDirPath);
            }

            gameRepository = new FileSystemRepository(dbDirPath.toString());
            pgnService = new PgnService(gameRepository);
            log.debug("PGN Service initialized at: {}", dbDirPath);
        } catch (PgnException | IOException e) {
            log.error("Failed to initialize PGN service", e);
            // Пытаемся использовать текущую директорию
            try {
                Files.createDirectories(fallbackPath);
                gameRepository = new FileSystemRepository(DEFAULT_DATABASE_DIR);
                pgnService = new PgnService(gameRepository);
            } catch (Exception ex) {
                log.error("Failed to initialize fallback PGN service", ex);
            }
        }
    }

    private NotationView getNotationView() {
        if (notationView == null) {
            notationView = boardView.getNotationView();
        }
        return notationView;
    }

    public void resetGame() {
        log.debug("Resetting game via MainController");

        if (boardView.isTerminalPosition()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(lang.get(NEW_GAME_TITLE));
            alert.setHeaderText(lang.get(NEW_GAME_HEADER));
            alert.setContentText(lang.get(NEW_GAME_CONTENT));

            ButtonType yesButton = new ButtonType(lang.get(CONFIRM_YES), ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType(lang.get(CONFIRM_NO), ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(yesButton, noButton);

            if (alert.showAndWait().orElse(noButton) == yesButton) {
                if (boardView.getNavController() != null) {
                    boardView.getNavController().resetInitialPosition();
                }
                boardView.forceResetGame();
                if (notationView != null) {
                    notationView.clearGameData();
                }
                startBodyHash = 0;
                loadedFullHash = 0;
            }
            return;
        }

        if (hasBodyChanges()) {
            showSaveDialogBeforeNewGame();
        } else {
            if (boardView.getNavController() != null) {
                boardView.getNavController().resetInitialPosition();
            }
            boardView.resetGame();
            if (notationView != null) {
                notationView.clearGameData();
            }
            startBodyHash = 0;
            loadedFullHash = 0;
        }
    }

    public void flipBoard() {
        boardView.flipBoard();
    }

    public void toggleCoordinates(boolean show) {
        boardView.setShowCoordinates(show);
    }

    public void connectToDatabase() {
        showFutureImplementationMessage();
    }

    public void openLastDatabase() {
        showFutureImplementationMessage();
    }

    private void showFutureImplementationMessage() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(lang.get(PREFERENCES_TITLE));
        alert.setHeaderText(null);
        alert.setContentText(lang.get(FEATURE_NOT_IMPLEMENTED));
        alert.showAndWait();
    }

    public void searchDatabase() {
        if (pgnService == null) {
            showNotification(lang.get(DB_NOT_INITIALIZED));
            return;
        }

        Dialog<List<GameData>> dialog = new Dialog<>();
        dialog.setTitle(lang.get(DB_SEARCH_TITLE));
        dialog.setHeaderText(lang.get(DB_SEARCH_HEADER));

        ButtonType searchButtonType = new ButtonType(lang.get(DB_SEARCH_BUTTON), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(searchButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField whiteField = new TextField();
        whiteField.setPromptText(lang.get(DB_SEARCH_WHITE));
        TextField blackField = new TextField();
        blackField.setPromptText(lang.get(DB_SEARCH_BLACK));
        TextField resultField = new TextField();
        resultField.setPromptText(lang.get(DB_SEARCH_RESULT));
        TextField ecoField = new TextField();
        ecoField.setPromptText(lang.get(DB_SEARCH_ECO));
        TextField openingField = new TextField();
        openingField.setPromptText(lang.get(DB_SEARCH_OPENING));

        grid.add(new Label(lang.get(DB_SEARCH_WHITE) + ":"), 0, 0);
        grid.add(whiteField, 1, 0);
        grid.add(new Label(lang.get(DB_SEARCH_BLACK) + ":"), 0, 1);
        grid.add(blackField, 1, 1);
        grid.add(new Label(lang.get(DB_SEARCH_RESULT) + ":"), 0, 2);
        grid.add(resultField, 1, 2);
        grid.add(new Label(lang.get(DB_SEARCH_ECO) + ":"), 0, 3);
        grid.add(ecoField, 1, 3);
        grid.add(new Label(lang.get(DB_SEARCH_OPENING) + ":"), 0, 4);
        grid.add(openingField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == searchButtonType) {
                try {
                    String white = whiteField.getText().trim();
                    String black = blackField.getText().trim();
                    String result = resultField.getText().trim();
                    String eco = ecoField.getText().trim();
                    String opening = openingField.getText().trim();

                    List<GameData> results;
                    if (!white.isEmpty() || !black.isEmpty()) {
                        results = pgnService.searchByPlayers(
                                white.isEmpty() ? null : white,
                                black.isEmpty() ? null : black
                        );
                    } else if (!result.isEmpty()) {
                        results = pgnService.searchByResult(result);
                    } else if (!eco.isEmpty()) {
                        results = pgnService.searchByEco(eco);
                    } else if (!opening.isEmpty()) {
                        results = pgnService.searchByOpening(opening);
                    } else {
                        results = pgnService.loadAllGames();
                    }
                    return results;
                } catch (PgnException e) {
                    showError(lang.get(DB_SEARCH_ERROR), e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(results -> {
            if (!results.isEmpty()) {
                showSearchResults(results);
            } else {
                showNotification(lang.get(DB_NO_RESULTS));
            }
        });
    }

    private void showSearchResults(List<GameData> results) {
        Dialog<GameData> dialog = new Dialog<>();
        dialog.setTitle(lang.get(DB_RESULTS_TITLE));
        dialog.setHeaderText(String.format(lang.get(DB_RESULTS_HEADER), results.size()));

        ListView<GameData> listView = new ListView<>();
        listView.getItems().addAll(results);

        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(GameData game, boolean empty) {
                super.updateItem(game, empty);
                if (empty || game == null) {
                    setText(null);
                } else {
                    setText(String.format("%s vs %s | %s | %s",
                            game.whitePlayer(), game.blackPlayer(),
                            game.result(), game.event()));
                }
            }
        });

        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                GameData selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    loadGameFromDatabase(selected);
                    dialog.close();
                }
            }
        });

        ButtonType loadButton = new ButtonType(lang.get(DB_LOAD_BUTTON), ButtonBar.ButtonData.OK_DONE);
        ButtonType closeButton = new ButtonType(lang.get(LanguageKeys.MAIN_CLOSE_BUTTON), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(loadButton, closeButton);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loadButton) {
                return listView.getSelectionModel().getSelectedItem();
            }
            return null;
        });

        dialog.getDialogPane().setContent(listView);
        dialog.getDialogPane().setPrefWidth(600);
        dialog.getDialogPane().setPrefHeight(400);

        dialog.showAndWait().ifPresent(this::loadGameFromDatabase);
    }

    private void loadGameFromDatabase(GameData gameData) {
        if (gameData == null) {
            showNotification(lang.get(DB_LOAD_ERROR));
            return;
        }

        try {
            String pgn = gameData.pgn();
            log.trace("PGN from GameData:\n{}", pgn);
            if (pgn == null || pgn.isEmpty()) {
                showNotification(lang.get(DB_LOAD_ERROR) + ": " + lang.get(LanguageKeys.MAIN_PGN_EMPTY));
                return;
            }

            PgnParser parser = new PgnParser();
            GameTree gameTree = parser.parseToGameTree(pgn);

            if (gameTree == null || gameTree.isEmpty()) {
                showNotification(lang.get(LanguageKeys.MAIN_LOAD_GAME_EMPTY_TREE));
                return;
            }

            MoveNavigationController navController = boardView.getNavController();
            if (navController != null) {
                navController.loadGameTree(
                        gameTree.getRootNode(),
                        gameTree.getMainLine(),
                        gameTree.getRootVariation(),
                        gameTree.getInitialBoard()
                );
            }

            updateGameHashes(gameData);

            if (notationView != null) {
                notationView.updateGameData(gameData);
            }

            boardView.refreshBoard();
            notationView.refreshFromMainLine();

            showNotification(lang.get(DB_LOAD_SUCCESS) + ": " +
                    gameData.whitePlayer() + " vs " + gameData.blackPlayer());

        } catch (Exception e) {
            showError(lang.get(DB_LOAD_ERROR), e.getMessage());
        }

        updateLoadedGameHash(gameData);
    }

    public void importPgnToDatabase() {
        showFutureImplementationMessage();
    }

    public void importPgnFromClipboard() {
        if (pgnService == null) {
            showNotification(lang.get(DB_NOT_INITIALIZED));
            return;
        }

        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        if (!clipboard.hasString()) {
            showNotification(lang.get(PGN_CLIPBOARD_EMPTY));
            return;
        }

        String pgnContent = clipboard.getString();
        if (pgnContent == null || pgnContent.trim().isEmpty()) {
            showNotification(lang.get(PGN_CLIPBOARD_EMPTY));
            return;
        }

        try {
            List<GameData> games = pgnService.importFromPgnString(pgnContent);
            if (games.isEmpty()) {
                showNotification(lang.get(PGN_IMPORT_NO_GAMES));
                return;
            }

            if (games.size() == 1) {
                loadGameFromDatabase(games.get(0));
                showNotification(lang.get(PGN_IMPORT_CLIPBOARD_SUCCESS));
            } else {
                showSearchResults(games);
            }
        } catch (PgnException e) {
            showError(lang.get(PGN_IMPORT_ERROR), e.getMessage());
        }
    }

    public void exportCurrentGameToPgn() {
        if (pgnService == null) {
            showNotification(lang.get(DB_NOT_INITIALIZED));
            return;
        }

        SaveGameDialog dialog = new SaveGameDialog(primaryStage, boardView, notationView);
        GameData gameData = dialog.showAndWait();

        if (gameData != null) {
            try {
                pgnService.saveGame(gameData);

                if (notationView != null) {
                    notationView.updateGameData(gameData);
                }

                showNotification(lang.get(LanguageKeys.MAIN_GAME_SAVED_TO_DB));
            } catch (PgnException e) {
                showError(lang.get(PGN_SAVE_ERROR), e.getMessage());
            }
        }
    }

    public void showOpeningStatistics() {
        // 1. Проверяем наличие браузеров
        PgnBrowserManager manager = PgnBrowserManager.getInstance();
        Collection<PgnFileBrowser> browsers = manager.getAllBrowsers();

        if (browsers.isEmpty()) {
            showNotification(lang.get(DB_LOAD_PGN_FIRST)); // "Загрузите PGN файл в программу"
            return;
        }

        // 2. Находим активный браузер или берем первый
        PgnFileBrowser activeBrowser = manager.getActiveBrowser();

        // 3. Получаем индекс
        PgnIndex index = activeBrowser.getCurrentIndex();
        if (index == null) {
            showNotification(lang.get(DB_INDEX_NOT_LOADED)); // "Индекс не загружен"
            return;
        }

        // 4. Собираем статистику из индекса
        List<GameIndexEntry> entries = index.getActiveEntries();
        if (entries.isEmpty()) {
            showNotification(lang.get(DB_NO_GAMES));
            return;
        }

        Map<String, Integer> ecoStats = new HashMap<>();
        Map<String, Integer> openingStats = new HashMap<>();

        for (GameIndexEntry entry : entries) {
            String eco = entry.getEco();
            if (eco != null && !eco.isEmpty() && !"?".equals(eco)) {
                ecoStats.put(eco, ecoStats.getOrDefault(eco, 0) + 1);
            }

            String opening = entry.getOpening();
            if (opening != null && !opening.isEmpty() && !"?".equals(opening)) {
                openingStats.put(opening, openingStats.getOrDefault(opening, 0) + 1);
            }
        }

        // 5. Формируем и показываем статистику
        StringBuilder stats = new StringBuilder();
        stats.append("=== ").append(lang.get(DB_STATS_ECO)).append(" ===\n");
        ecoStats.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(e -> stats.append(e.getKey()).append(": ").append(e.getValue()).append("\n"));

        stats.append("\n=== ").append(lang.get(DB_STATS_OPENING)).append(" ===\n");
        openingStats.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(e -> stats.append(e.getKey()).append(": ").append(e.getValue()).append("\n"));

        stats.append("\n").append(lang.get(DB_STATS_TOTAL)).append(": ").append(entries.size());

        showInfo(lang.get(DB_STATS_TITLE), stats.toString());
    }

    public void showDatabaseInfo() {
        // 1. Проверяем наличие браузеров
        PgnBrowserManager manager = PgnBrowserManager.getInstance();
        Collection<PgnFileBrowser> browsers = manager.getAllBrowsers();

        if (browsers.isEmpty()) {
            showNotification(lang.get(DB_LOAD_PGN_FIRST));
            return;
        }

        // 2. Находим активный браузер или берем первый
        PgnFileBrowser activeBrowser = manager.getActiveBrowser();

        // 3. Получаем индекс
        PgnIndex index = activeBrowser.getCurrentIndex();
        if (index == null) {
            showNotification(lang.get(DB_INDEX_NOT_LOADED));
            return;
        }

        // 4. Собираем информацию из индекса
        Path pgnPath = activeBrowser.getPgnPath();
        String fileName = pgnPath.getFileName().toString();
        String filePath = pgnPath.getParent().toString();

        int totalGames = index.getGameCount();
        int activeGames = index.getActiveCount();
        int deletedGames = totalGames - activeGames;

        // Размер файла
        String fileSize = "?";
        try {
            long size = Files.size(pgnPath);
            fileSize = formatFileSize(size);
        } catch (IOException e) {
            log.warn("Failed to get file size", e);
        }

        // Информация о версии индекса
        int version = index.getVersion();
        boolean needsRepack = index.needsRepack();
        double growthRatio = index.getGrowthRatio();

        // 5. Формируем информацию
        String info = String.format(
                """
                        === %s ===
                        %s: %s
                        %s: %s
                        %s: %d
                        %s: %d
                        %s: %d
                        %s: %s
                        %s: %d
                        %s: %.1f%%
                        %s: %s""",

                lang.get(DB_INFO_TITLE),

                // Имя файла
                lang.get(DB_INFO_FILENAME), fileName,

                // Путь
                lang.get(DB_INFO_PATH), filePath,

                // Общее количество партий
                lang.get(DB_STATS_TOTAL), totalGames,

                // Активные партии
                lang.get(DB_INFO_ACTIVE_GAMES), activeGames,

                // Удаленные партии
                lang.get(DB_INFO_DELETED_GAMES), deletedGames,

                // Размер файла
                lang.get(DB_INFO_FILE_SIZE), fileSize,

                // Версия индекса
                lang.get(DB_INFO_INDEX_VERSION), version,

                // Соотношение роста
                lang.get(DB_INFO_GROWTH_RATIO), (growthRatio - 1) * 100,

                // Нужна ли перепаковка
                lang.get(PGN_BROWSER_AUTO_REPACK_TITLE), needsRepack ?
                        lang.get(CONFIRM_YES) : lang.get(CONFIRM_NO)
        );

        showInfo(lang.get(DB_INFO_TITLE), info);
    }

    /**
     * Форматирует размер файла в читаемый вид
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    public void showBestMove() {
        analyzePosition();
    }

    public void loadPgnFile(File file) {
        if (file == null) return;

        log.info("Opening PGN file: {}", file.getAbsolutePath());

        try {
            Path pgnPath = file.toPath();

            PgnIndexingFacade facade = new PgnIndexingFacade();
            IndexStatus status = facade.checkIndex(pgnPath);

            if (status == IndexStatus.NO_INDEX || status == IndexStatus.FILE_CHANGED) {
                log.info("Index needed, starting automatic indexing...");
                indexPgnFileWithProgress(pgnPath, () -> openPgnWithBrowser(pgnPath));
                return;
            }

            openPgnWithBrowser(pgnPath);

        } catch (Exception e) {
            log.error("Failed to open PGN file", e);
            showError(lang.get(LanguageKeys.MAIN_OPEN_PGN_ERROR_MSG), e.getMessage());
        }
    }

    private void openPgnWithBrowser(Path pgnPath) {
        try {
            PgnBrowserManager manager = PgnBrowserManager.getInstance();

            if (manager.isFileOpened(pgnPath)) {
                PgnFileBrowser existing = manager.getBrowser(pgnPath);
                existing.showWindow();
                return;
            }

            manager.openBrowser(pgnPath, this::onGameSelectedFromBrowser);

        } catch (IllegalStateException e) {
            showError(lang.get(LanguageKeys.MAIN_BROWSER_LIMIT_MSG), e.getMessage());
        } catch (Exception e) {
            log.error("Failed to open with browser", e);
            showError(lang.get(LanguageKeys.MAIN_OPEN_ERROR_MSG), e.getMessage());
        }
    }

    private void indexPgnFileWithProgress(Path pgnPath, Runnable onComplete) {
        log.info("Indexing with progress: {}", pgnPath);

        Platform.runLater(() -> {
            progressDialog = new IndexingProgressDialog();
            progressDialog.setTitle(lang.get(LanguageKeys.MAIN_INDEXING_TITLE_MSG));
            progressDialog.show();
        });

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        new Thread(() -> {
            try {
                Path preparedPath = prepareFileEncoding(pgnPath);

                PgnIndexingFacade facade = new PgnIndexingFacade();

                PgnIndex index = facade.indexFile(preparedPath, progress -> Platform.runLater(() -> {
                    if (progressDialog != null) {
                        progressDialog.updateProgress(progress);
                    }
                }));

                Platform.runLater(() -> {
                    if (progressDialog != null) {
                        progressDialog.close();
                        progressDialog = null;
                    }
                    showNotification(String.format(lang.get(LanguageKeys.MAIN_INDEXING_COMPLETE),
                            index.getGameCount(), index.getActiveCount()));
                    if (onComplete != null) {
                        onComplete.run();
                    }
                });

            } catch (IOException e) {
                log.error("Indexing failed", e);
                Platform.runLater(() -> {
                    if (progressDialog != null) {
                        progressDialog.close();
                        progressDialog = null;
                    }
                    showError(lang.get(LanguageKeys.MAIN_INDEXING_ERROR_MSG), e.getMessage());
                });
            }
        }).start();
    }

    private Path prepareFileEncoding(Path pgnPath) throws IOException {
        log.info("Checking encoding for: {}", pgnPath);

        byte[] bytes = Files.readAllBytes(pgnPath);

        String detectedEncoding = detectEncoding(bytes);
        log.info("Detected encoding: {}", detectedEncoding);

        if ("UTF-8".equals(detectedEncoding) || "UTF-8-BOM".equals(detectedEncoding)) {
            log.info("File is already UTF-8, no conversion needed");
            return pgnPath;
        }

        log.info("Converting from {} to UTF-8", detectedEncoding);

        String content;
        content = new String(bytes, Charset.forName(detectedEncoding));

        Path tempPath = pgnPath.getParent().resolve(pgnPath.getFileName() + ".utf8.tmp");
        Files.writeString(tempPath, content);

        Path backupPath = pgnPath.getParent().resolve(pgnPath.getFileName() + ".bak");
        Files.deleteIfExists(backupPath);
        Files.move(pgnPath, backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Files.move(tempPath, pgnPath);

        log.info("File converted to UTF-8: {}", pgnPath);
        return pgnPath;
    }

    private String detectEncoding(byte[] bytes) {
        if (bytes.length >= 3 && bytes[0] == (byte) 0xEF &&
                bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
            return "UTF-8-BOM";
        }

        try {
            String test = new String(bytes, StandardCharsets.UTF_8);
            if (!test.contains("\uFFFD")) {
                if (containsCyrillic(test)) {
                    return "UTF-8";
                }
                if (hasPgnHeaders(test)) {
                    return "UTF-8";
                }
            }
        } catch (Exception e) {
            // Игнорируем
        }

        try {
            String win1251 = new String(bytes, Charset.forName("Windows-1251"));
            if (containsCyrillic(win1251) && hasPgnHeaders(win1251)) {
                return "Windows-1251";
            }
        } catch (Exception e) {
            // Игнорируем
        }

        try {
            String koi8r = new String(bytes, Charset.forName("KOI8-R"));
            if (containsCyrillic(koi8r) && hasPgnHeaders(koi8r)) {
                return "KOI8-R";
            }
        } catch (Exception e) {
            // Игнорируем
        }

        try {
            String iso8859_5 = new String(bytes, Charset.forName("ISO-8859-5"));
            if (containsCyrillic(iso8859_5) && hasPgnHeaders(iso8859_5)) {
                return "ISO-8859-5";
            }
        } catch (Exception e) {
            // Игнорируем
        }

        log.warn("Could not detect encoding, using UTF-8 as fallback");
        return "UTF-8";
    }

    private boolean containsCyrillic(String text) {
        if (text == null || text.isEmpty()) return false;
        return text.codePoints().anyMatch(cp -> cp >= 0x0400 && cp <= 0x04FF);
    }

    private boolean hasPgnHeaders(String text) {
        if (text == null || text.isEmpty()) return false;
        return text.contains("[Event") || text.contains("[White") ||
                text.contains("[Black") || text.contains("[Result");
    }

    /**
     * Метод для резервного использования если индекс не работает
     * не удалять
     * @Deprecated
     */
    @Deprecated
    private void openPgnWithoutIndex(Path pgnPath) {
        try {
            PgnBrowserManager manager = PgnBrowserManager.getInstance();
            manager.openBrowser(pgnPath, this::onGameSelectedFromBrowser);
        } catch (Exception e) {
            log.error("Failed to open without index", e);
            showError(lang.get(LanguageKeys.MAIN_OPEN_ERROR_MSG), e.getMessage());
        }
    }

    private void onGameSelectedFromBrowser(GameData gameData) {
        if (gameData == null) {
            log.warn("Game selection returned null");
            return;
        }

        try {
            if (gameData.isPosition()) {
                String fen = gameData.fen();

                if (fen != null && !fen.isEmpty()) {
                    Board board = new Board();
                    board.loadFromFen(fen);
                    boolean startWithBlack = board.getSideToMove() == Side.BLACK;

                    boardView.setupNewPosition(board, startWithBlack);

                    String pgnBody = gameData.pgn();
                    if (pgnBody != null && !pgnBody.isEmpty()) {
                        String fullPgn = buildFullPgnForPosition(gameData);

                        PgnParser parser = new PgnParser();
                        GameTree gameTree = parser.parseToGameTree(fullPgn);

                        if (gameTree != null && !gameTree.isEmpty()) {
                            MoveNavigationController navController = boardView.getNavController();
                            if (navController != null) {
                                navController.loadGameTree(
                                        gameTree.getRootNode(),
                                        gameTree.getMainLine(),
                                        gameTree.getRootVariation(),
                                        gameTree.getInitialBoard()
                                );

                                navController.goToFirstMove();

                                Board fenBoard = new Board();
                                fenBoard.loadFromFen(fen);
                                boardView.setBoard(fenBoard);
                            }
                        }
                    }

                    if (notationView != null) {
                        notationView.setGameResult(gameData.result());
                        notationView.updateGameData(gameData);
                        notationView.refreshDisplay();
                    }

                    boardView.refreshBoard();
                    showNotification(String.format("%s %s %s", lang.get(LanguageKeys.MAIN_LOADED_POSITION),
                            gameData.getTypeDisplay(), lang.get(LanguageKeys.MAIN_POSITION_SOLVE_HINT)));
                    return;
                }
            }

            String pgn = gameData.pgn();
            if (pgn == null || pgn.isEmpty()) {
                showNotification(lang.get(LanguageKeys.MAIN_PGN_EMPTY));
                return;
            }

            PgnParser parser = new PgnParser();
            GameTree gameTree = parser.parseToGameTree(pgn);

            if (gameTree == null || gameTree.isEmpty()) {
                showNotification(lang.get(LanguageKeys.MAIN_LOAD_GAME_EMPTY_TREE));
                return;
            }

            MoveNavigationController navController = boardView.getNavController();
            if (navController != null) {
                navController.loadGameTree(
                        gameTree.getRootNode(),
                        gameTree.getMainLine(),
                        gameTree.getRootVariation(),
                        gameTree.getInitialBoard()
                );
            }

            updateGameHashes(gameData);

            if (notationView != null) {
                notationView.setGameResult(gameData.result());
                notationView.updateGameData(gameData);
                loadedGameHash = HashUtils.calculateContentHash(gameData);
            }

            boardView.refreshBoard();

            if (notationView != null) {
                notationView.refreshDisplay();
            }

            showNotification(lang.get(LanguageKeys.MAIN_GAME_LOADED) +
                    gameData.whitePlayer() + " vs " + gameData.blackPlayer());

        } catch (Exception e) {
            log.error("Failed to load game from browser", e);
            showError(lang.get(LanguageKeys.DB_LOAD_ERROR), e.getMessage());
        }

        updateLoadedGameHash(gameData);
    }

    private String buildFullPgnForPosition(GameData gameData) {
        StringBuilder sb = new StringBuilder();

        sb.append("[Event \"").append(gameData.event()).append("\"]\n");
        sb.append("[Site \"").append(gameData.site()).append("\"]\n");
        sb.append("[Date \"").append(gameData.date()).append("\"]\n");
        sb.append("[Round \"").append(gameData.round()).append("\"]\n");
        sb.append("[White \"").append(gameData.whitePlayer()).append("\"]\n");
        sb.append("[Black \"").append(gameData.blackPlayer()).append("\"]\n");
        sb.append("[Result \"").append(gameData.result()).append("\"]\n");

        if (gameData.isSetUp() && gameData.fen() != null && !gameData.fen().isEmpty()) {
            sb.append("[SetUp \"1\"]\n");
            sb.append("[FEN \"").append(gameData.fen()).append("\"]\n");
        }

        if (gameData.positionType() != null && !"game".equals(gameData.positionType())) {
            sb.append("[PositionType \"").append(gameData.positionType()).append("\"]\n");
        }

        if (gameData.plyCount() != null && !"?".equals(gameData.plyCount())) {
            sb.append("[PlyCount \"").append(gameData.plyCount()).append("\"]\n");
        }

        sb.append("\n");
        sb.append(gameData.pgn());

        return sb.toString();
    }

    public void showPgnBrowser() {
        PgnBrowserManager manager = PgnBrowserManager.getInstance();

        if (manager.getBrowserCount() > 0) {
            Collection<PgnFileBrowser> browsers = manager.getAllBrowsers();
            if (!browsers.isEmpty()) {
                browsers.iterator().next().showWindow();
            }
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(lang.get(LanguageKeys.MENU_FILE_OPEN_PGN));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PGN files", "*.pgn")
        );
        File file = fileChooser.showOpenDialog(primaryStage);

        if (file != null) {
            loadPgnFile(file);
        }
    }

    public void refreshPgnBrowser() {
        PgnBrowserManager manager = PgnBrowserManager.getInstance();
        PgnFileBrowser activeBrowser = manager.getActiveBrowser();

        if (activeBrowser != null && activeBrowser.isShowing()) {
            activeBrowser.refresh();
            showNotification(lang.get(LanguageKeys.MAIN_REFRESHED_MSG));
        } else {
            showNotification(lang.get(LanguageKeys.MAIN_NO_ACTIVE_BROWSER_MSG));
        }
    }

    public void closePgnBrowser() {
        log.info("Closing all PGN browsers");
        PgnBrowserManager.getInstance().closeAllBrowsers();
    }

    public void savePgnFile(File file) {
        if (file == null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(lang.get(MENU_FILE_SAVE_PGN));
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PGN files", "*.pgn")
            );

            String saveDir = AppPreferences.getSaveDirectory();
            if (saveDir != null && !saveDir.isEmpty()) {
                File dir = new File(saveDir);
                if (dir.exists() && dir.isDirectory()) {
                    fileChooser.setInitialDirectory(dir);
                }
            }

            file = fileChooser.showSaveDialog(primaryStage);
            if (file == null) return;
            AppPreferences.saveSaveDirectory(file.getParent());
        }

        try {
            NotationView nv = getNotationView();
            if (nv == null) {
                showNotification(lang.get(PGN_SAVE_ERROR));
                return;
            }

            SaveGameDialog dialog = new SaveGameDialog(primaryStage, boardView, notationView);
            GameData gameData = dialog.showAndWait();

            if (gameData == null) {
                return;
            }

            if (notationView != null) {
                notationView.updateGameData(gameData);
            }

            String currentPgn = nv.getCurrentPGN(gameData);
            if (currentPgn == null || currentPgn.isEmpty()) {
                showNotification(lang.get(PGN_SAVE_EMPTY));
                return;
            }

            String plainPgn = convertUnicodeToPlain(currentPgn);

            Path path = file.toPath();
            String content = plainPgn;

            if (Files.exists(path)) {
                content = "\n\n" + plainPgn;
            }

            Files.writeString(
                    path,
                    content,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND
            );

            updateGameHashes(gameData);

            String fileName = file.getName();
            showNotification(lang.get(LanguageKeys.PGN_SAVE_SUCCESS) + fileName);

        } catch (Exception e) {
            showError(lang.get(PGN_SAVE_ERROR), e.getMessage());
        }
    }

    private String convertUnicodeToPlain(String pgn) {
        if (pgn == null || pgn.isEmpty()) return pgn;

        Map<String, String> unicodeToLetter = new HashMap<>();
        unicodeToLetter.put("♔", "K");
        unicodeToLetter.put("♕", "Q");
        unicodeToLetter.put("♖", "R");
        unicodeToLetter.put("♗", "B");
        unicodeToLetter.put("♘", "N");
        unicodeToLetter.put("♙", "P");
        unicodeToLetter.put("♚", "K");
        unicodeToLetter.put("♛", "Q");
        unicodeToLetter.put("♜", "R");
        unicodeToLetter.put("♝", "B");
        unicodeToLetter.put("♞", "N");
        unicodeToLetter.put("♟", "P");

        String result = pgn;
        for (Map.Entry<String, String> entry : unicodeToLetter.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public void loadNextGameFromBrowser() {
        PgnBrowserManager manager = PgnBrowserManager.getInstance();
        PgnFileBrowser active = manager.getActiveBrowser();

        if (active != null && active.hasGames()) {
            log.debug("F11 -> next game from active browser ");
            active.loadNextGame();
        } else {
            log.debug("F11:not active browser or games");
        }
    }

    public void loadPreviousGameFromBrowser() {
        PgnBrowserManager manager = PgnBrowserManager.getInstance();
        PgnFileBrowser active = manager.getActiveBrowser();

        if (active != null && active.hasGames()) {
            log.debug("Ctrl+F11 -> previous game from active browser ");
            active.loadPreviousGame();
        } else {
            log.debug("Ctrl+F11: not active browser or games");
        }
    }

    private void showSaveDialogBeforeNewGameWithCallback(Runnable onComplete) {
        if (!hasBodyChanges()) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(lang.get(SAVE_GAME_TITLE));
        alert.setHeaderText(lang.get(SAVE_GAME_HEADER));
        alert.setContentText(lang.get(SAVE_GAME_CONTENT));

        ButtonType saveButton = new ButtonType(lang.get(SAVE_GAME_SAVE));
        ButtonType noSaveButton = new ButtonType(lang.get(SAVE_GAME_DONT_SAVE));
        ButtonType cancelButton = new ButtonType(lang.get(SAVE_GAME_CANCEL), ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(saveButton, noSaveButton, cancelButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == saveButton) {
                SaveGameDialog dialog = new SaveGameDialog(primaryStage, boardView, notationView);
                GameData gameData = dialog.showAndWait();
                if (gameData != null && pgnService != null) {
                    try {
                        pgnService.saveGame(gameData);
                        if (notationView != null) {
                            notationView.updateGameData(gameData);
                        }
                        updateGameHashes(gameData);
                        showNotification(lang.get(LanguageKeys.PGN_SAVE_SUCCESS));
                    } catch (PgnException e) {
                        showError(lang.get(PGN_SAVE_ERROR), e.getMessage());
                    }
                }
                if (onComplete != null) {
                    onComplete.run();
                }
            } else if (response == noSaveButton) {
                startBodyHash = 0;
                loadedFullHash = 0;
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }

    private void showSaveDialogBeforeNewGame() {
        if (!hasBodyChanges()) {
            boardView.resetGame();
            if (notationView != null) {
                notationView.clearGameData();
            }
            startBodyHash = 0;
            loadedFullHash = 0;
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(lang.get(SAVE_GAME_TITLE));
        alert.setHeaderText(lang.get(SAVE_GAME_HEADER));
        alert.setContentText(lang.get(SAVE_GAME_CONTENT));

        ButtonType saveButton = new ButtonType(lang.get(SAVE_GAME_SAVE));
        ButtonType noSaveButton = new ButtonType(lang.get(SAVE_GAME_DONT_SAVE));
        ButtonType cancelButton = new ButtonType(lang.get(SAVE_GAME_CANCEL), ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(saveButton, noSaveButton, cancelButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == saveButton) {
                SaveGameDialog dialog = new SaveGameDialog(primaryStage, boardView, notationView);
                GameData editedGameData = dialog.showAndWait();

                if (editedGameData != null) {
                    saveGameDataWithChoice(editedGameData);
                }

                boardView.resetGame();
                if (notationView != null) {
                    notationView.clearGameData();
                }
                startBodyHash = 0;
                loadedFullHash = 0;

            } else if (response == noSaveButton) {
                boardView.resetGame();
                if (notationView != null) {
                    notationView.clearGameData();
                }
                startBodyHash = 0;
                loadedFullHash = 0;
            }
        });
    }

    private void saveGameDataWithChoice(GameData gameData) {
        Alert choiceDialog = new Alert(Alert.AlertType.CONFIRMATION);
        choiceDialog.setTitle(lang.get(LanguageKeys.MAIN_SAVE_GAME_CHOICE_TITLE));
        choiceDialog.setHeaderText(lang.get(LanguageKeys.MAIN_SAVE_GAME_CHOICE_HEADER));
        choiceDialog.setContentText(lang.get(LanguageKeys.MAIN_SAVE_GAME_CHOICE_CONTENT));

        ButtonType pgnButton = new ButtonType(lang.get(LanguageKeys.MAIN_SAVE_TO_PGN_FILE));
        ButtonType dbButton = new ButtonType(lang.get(LanguageKeys.MAIN_SAVE_TO_DATABASE));
        ButtonType cancelButton = new ButtonType(lang.get(LanguageKeys.MAIN_SAVE_CANCEL), ButtonBar.ButtonData.CANCEL_CLOSE);

        choiceDialog.getButtonTypes().setAll(pgnButton, dbButton, cancelButton);

        choiceDialog.showAndWait().ifPresent(choice -> {
            if (choice == pgnButton) {
                saveToPgnFile(gameData);
            } else if (choice == dbButton) {
                saveToDatabase(gameData);
            }
        });
    }

    private void saveToPgnFile(GameData gameData) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(lang.get(LanguageKeys.MAIN_SAVE_PGN_FILE_TITLE));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PGN files", "*.pgn")
        );

        PgnBrowserManager manager = PgnBrowserManager.getInstance();
        PgnFileBrowser activeBrowser = manager.getActiveBrowser();

        if (activeBrowser != null && activeBrowser.isShowing()) {
            Path currentPath = activeBrowser.getPgnPath();
            if (currentPath != null) {
                File defaultFile = currentPath.toFile();
                if (defaultFile.exists()) {
                    fileChooser.setInitialDirectory(defaultFile.getParentFile());
                    fileChooser.setInitialFileName(defaultFile.getName());
                }
            }
        }

        if (fileChooser.getInitialDirectory() == null) {
            String saveDir = AppPreferences.getSaveDirectory();
            if (saveDir != null && !saveDir.isEmpty()) {
                File dir = new File(saveDir);
                if (dir.exists() && dir.isDirectory()) {
                    fileChooser.setInitialDirectory(dir);
                }
            }
        }

        File selectedFile = fileChooser.showSaveDialog(primaryStage);
        if (selectedFile == null) {
            return;
        }

        AppPreferences.saveSaveDirectory(selectedFile.getParent());

        try {
            Path pgnPath = selectedFile.toPath();
            boolean fileExists = Files.exists(pgnPath);

            PgnIndexManager indexManager = new PgnIndexManager();
            PgnIndex index = null;

            if (fileExists) {
                Path indexPath = indexManager.getIndexPath(pgnPath);
                if (Files.exists(indexPath)) {
                    try {
                        index = indexManager.loadIndex(pgnPath);
                    } catch (IOException e) {
                        log.warn("Failed to load index, will create new: {}", e.getMessage());
                    }
                }
            }

            if (index == null) {
                PgnFileEditor editor = new PgnFileEditor(pgnPath, null);
                GameIndexEntry newEntry = editor.appendGame(gameData.pgn(), 1);

                newEntry.setWhite(gameData.whitePlayer());
                newEntry.setBlack(gameData.blackPlayer());
                newEntry.setEco(gameData.eco());
                newEntry.setResult(gameData.result());
                newEntry.setYear(gameData.date() != null ? String.valueOf(gameData.date().getYear()) : "");
                newEntry.setEvent(gameData.event());
                newEntry.setSite(gameData.site());
                newEntry.setOpening(gameData.opening());
                newEntry.setVariation(gameData.variation());
                newEntry.setPlyCount(parsePlyCount(gameData.plyCount()));
                newEntry.setHash(HashUtils.calculateContentHash(gameData));

                PgnIndex newIndex = PgnIndex.builder()
                        .version(PgnIndex.FORMAT_VERSION)
                        .fileHash(indexManager.computeFileHash(pgnPath))
                        .fileSize(Files.size(pgnPath))
                        .gameCount(1)
                        .activeCount(1)
                        .entries(new java.util.ArrayList<>(List.of(newEntry)))
                        .build();

                indexManager.saveIndex(pgnPath, newIndex);
                showNotification(String.format(lang.get(LanguageKeys.MAIN_GAME_SAVED_FILE), selectedFile.getName()));
            } else {
                PgnGameOperation operation = new PgnGameOperation(pgnPath, index);
                operation.addGame(gameData.pgn());

                if (activeBrowser != null && activeBrowser.isShowing() &&
                        activeBrowser.getPgnPath().equals(pgnPath)) {
                    activeBrowser.refresh();
                }

                showNotification(String.format(lang.get(LanguageKeys.MAIN_GAME_SAVED_FILE), selectedFile.getName()));
            }

        } catch (Exception e) {
            log.error("Failed to save to PGN file", e);
            showError(lang.get(LanguageKeys.PGN_SAVE_ERROR), e.getMessage());
        }
    }

    private void saveToDatabase(GameData gameData) {
        if (pgnService == null) {
            showNotification(lang.get(LanguageKeys.MAIN_DB_NOT_INITIALIZED_MSG));
            return;
        }

        try {
            pgnService.saveGame(gameData);
            showNotification(lang.get(LanguageKeys.MAIN_GAME_SAVED_TO_DB));
        } catch (PgnException e) {
            log.error("Failed to save to database", e);
            showError(lang.get(LanguageKeys.PGN_SAVE_ERROR), e.getMessage());
        }
    }

    private int parsePlyCount(String plyCount) {
        if (plyCount == null || plyCount.isEmpty()) return 0;
        try {
            return Integer.parseInt(plyCount);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void updateLoadedGameHash(GameData gameData) {
        if (gameData != null) {
            loadedGameHash = HashUtils.calculateContentHash(gameData);
        } else {
            loadedGameHash = 0;
        }
    }

    private void updateGameHashes(GameData gameData) {
        if (gameData == null) {
            startBodyHash = 0;
            loadedFullHash = 0;
            return;
        }

        startBodyHash = HashUtils.calculateBodyHash(gameData);
        loadedFullHash = HashUtils.calculateContentHash(gameData);

        log.trace("updateGameHashes: startBodyHash={}, loadedFullHash={}",
                startBodyHash, loadedFullHash);
    }

    private boolean hasBodyChanges() {
        if (notationView == null) return false;

        GameData currentData = notationView.getCurrentGameData();
        if (currentData == null) return false;

        int currentBodyHash = HashUtils.calculateBodyHash(currentData);
        boolean hasChanges = currentBodyHash != startBodyHash;

        log.trace("hasBodyChanges: startBodyHash={}, currentBodyHash={}, hasChanges={}",
                startBodyHash, currentBodyHash, hasChanges);

        return hasChanges;
    }

    public void updateCurrentGameData() {
        if (notationView == null) return;

        String currentPgn = notationView.getCurrentPGN();
        if (currentPgn == null || currentPgn.isEmpty()) return;

        try {
            PgnParser parser = new PgnParser();
            GameData currentData = parser.parse(currentPgn);
            if (currentData != null) {
                notationView.updateGameData(currentData);
            }
        } catch (Exception e) {
            log.warn("Failed to update current GameData: {}", e.getMessage());
        }
    }

    public void setupPosition() {
        PositionSetupDialog dialog = new PositionSetupDialog(primaryStage, boardView.getCurrentBoard());
        Board newBoard = dialog.showAndWait();

        if (newBoard != null) {
            Side sideToMove = newBoard.getSideToMove();
            boolean startWithBlack = (sideToMove == Side.BLACK);

            boardView.setupNewPosition(newBoard, startWithBlack);

            if (notationView != null) {
                GameData currentData = notationView.getCurrentGameData();
                String fen = newBoard.getFen();

                GameData newData = new GameData(
                        currentData != null ? currentData.whitePlayer() : lang.get(LanguageKeys.DEFAULT_PLAYER_NAME),
                        currentData != null ? currentData.blackPlayer() : lang.get(LanguageKeys.DEFAULT_PLAYER_NAME),
                        currentData != null ? currentData.result() : "*",
                        currentData != null ? currentData.whiteElo() : "?",
                        currentData != null ? currentData.blackElo() : "?",
                        currentData != null ? currentData.event() : "Kletka Game",
                        currentData != null ? currentData.site() : "?",
                        currentData != null ? currentData.round() : "?",
                        currentData != null ? currentData.subround() : "?",
                        LocalDate.now(),
                        currentData != null ? currentData.eco() : "?",
                        currentData != null ? currentData.opening() : "?",
                        currentData != null ? currentData.variation() : "?",
                        currentData != null ? currentData.annotator() : "?",
                        currentData != null ? currentData.whiteTeam() : "?",
                        currentData != null ? currentData.blackTeam() : "?",
                        currentData != null ? currentData.source() : "?",
                        currentData != null ? currentData.whiteFideId() : "?",
                        currentData != null ? currentData.blackFideId() : "?",
                        currentData != null ? currentData.timeControl() : "?",
                        "0",
                        "",
                        fen,
                        true,
                        "position",
                        false
                );
                notationView.updateGameData(newData);
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(lang.get(NOTIFICATION_INFO));
            alert.setHeaderText(null);
            alert.setContentText(lang.get(POSITION_SET_SUCCESS,
                    startWithBlack ? lang.get(GAME_BLACK).toLowerCase() : lang.get(GAME_WHITE).toLowerCase()));
            alert.showAndWait();
        }
    }

    private void initEngine() {
        if (engineManager.isEngineRunning()) {
            return;
        }

        String savedPath = AppPreferences.getEnginePath();

        if (savedPath != null && !savedPath.isEmpty()) {
            File engineFile = new File(savedPath);
            if (engineFile.exists()) {
                try {
                    engineManager.startEngine(savedPath);
                    log.debug("Engine started from saved path: {}", savedPath);
                    return;
                } catch (IOException e) {
                    log.error("Failed to start engine from saved path: {}", e.getMessage());
                    AppPreferences.resetEngineSettings();
                }
            } else {
                AppPreferences.resetEngineSettings();
            }
        }

        showEngineSetupDialog();
    }

    private void showEngineSetupDialog() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(lang.get(ENGINE_SETUP_TITLE));
            alert.setHeaderText(lang.get(ENGINE_SETUP_HEADER));
            alert.setContentText(lang.get(ENGINE_SETUP_CONTENT));

            ButtonType setupButton = new ButtonType(lang.get(ENGINE_SETUP_BUTTON), ButtonBar.ButtonData.OK_DONE);
            ButtonType laterButton = new ButtonType(lang.get(ENGINE_SETUP_LATER), ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(setupButton, laterButton);

            alert.showAndWait().ifPresent(response -> {
                if (response == setupButton) {
                    EngineSetupDialog dialog = new EngineSetupDialog(primaryStage);
                    String enginePath = dialog.showAndWait();

                    if (enginePath != null && !enginePath.isEmpty()) {
                        try {
                            engineManager.startEngine(enginePath);
                            AppPreferences.saveEnginePath(enginePath);
                            showNotification(lang.get(ENGINE_SETUP_SUCCESS));
                        } catch (IOException e) {
                            showError(lang.get(ENGINE_SETUP_ERROR_TITLE),
                                    lang.get(ENGINE_SETUP_ERROR_CONTENT) + ": " + e.getMessage());
                        }
                    }
                }
            });
        });
    }

    public void configureEngine() {
        if (engineManager != null && engineManager.isEngineRunning()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle(lang.get(ENGINE_SWITCH_TITLE));
            confirm.setHeaderText(lang.get(ENGINE_SWITCH_HEADER));
            confirm.setContentText(lang.get(ENGINE_SWITCH_CONTENT));

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
            engineManager.stopEngine();
        }

        EngineSetupDialog dialog = new EngineSetupDialog(primaryStage);
        String enginePath = dialog.showAndWait();

        if (enginePath != null && !enginePath.isEmpty()) {
            try {
                assert engineManager != null;
                engineManager.startEngine(enginePath);
                AppPreferences.saveEnginePath(enginePath);
                showNotification(lang.get(ENGINE_SWITCH_SUCCESS));
            } catch (IOException e) {
                showError(lang.get(ENGINE_SETUP_ERROR_TITLE),
                        lang.get(ENGINE_SETUP_ERROR_CONTENT) + ": " + e.getMessage());
            }
        }
    }

    public void analyzePosition() {
        if (engineManager == null || !engineManager.isEngineRunning()) {
            showNotification(lang.get(ENGINE_NOT_RUNNING));
            return;
        }

        Board currentBoard = boardView.getCurrentBoard();
        engineManager.sendPosition(currentBoard);

        engineManager.getBestMoveAsync(3000).thenAccept(bestMoveUci -> Platform.runLater(() -> {
            try {
                Move bestMove = engineManager.convertUciToMove(bestMoveUci);
                String message = String.format(lang.get(ANALYSIS_BEST_MOVE),
                        bestMoveUci,
                        bestMove.getFrom() + "->" + bestMove.getTo());
                showInfo(lang.get(ANALYSIS_TITLE), message);
            } catch (Exception e) {
                showError(lang.get(ANALYSIS_ERROR_TITLE),
                        lang.get(ANALYSIS_ERROR_CONTENT) + ": " + bestMoveUci);
            }
        })).exceptionally(throwable -> {
            Platform.runLater(() -> showError(lang.get(ANALYSIS_ERROR_TITLE), throwable.getMessage()));
            return null;
        });
    }

    public void showShortcuts() {
        // Формируем список горячих клавиш с группировкой

                // Файл
        String shortcuts = "═══════════════════════════════════════\n" +
                lang.get(SHORTCUT_FILE) + ":\n" +
                "  " + lang.get(SHORTCUT_NEW_GAME) + "\n" +
                "  " + lang.get(SHORTCUT_OPEN_PGN) + "\n" +
                "  " + lang.get(SHORTCUT_SAVE_PGN) + "\n" +
                "  " + lang.get(SHORTCUT_EXPORT_CURRENT) + "\n" +
                "  " + lang.get(SHORTCUT_IMPORT_CLIPBOARD) + "\n" +
                "  " + lang.get(SHORTCUT_SETUP_POSITION) + "\n" +
                "  " + lang.get(SHORTCUT_EXIT) + "\n" +

                // Вид
                "\n" + lang.get(SHORTCUT_VIEW) + ":\n" +
                "  " + lang.get(SHORTCUT_FLIP_BOARD) + "\n" +
                "  " + lang.get(SHORTCUT_ZOOM_IN) + "\n" +
                "  " + lang.get(SHORTCUT_ZOOM_OUT) + "\n" +
                "  " + lang.get(SHORTCUT_ZOOM_RESET) + "\n" +
                "  " + lang.get(SHORTCUT_TOGGLE_NOTATION) + "\n" +

                // Навигация
                "\n" + lang.get(SHORTCUT_NAVIGATION) + ":\n" +
                "  " + lang.get(SHORTCUT_NAV_PREV) + "\n" +
                "  " + lang.get(SHORTCUT_NAV_NEXT) + "\n" +
                "  " + lang.get(SHORTCUT_NAV_FIRST) + "\n" +
                "  " + lang.get(SHORTCUT_NAV_LAST) + "\n" +

                // Движок
                "\n" + lang.get(SHORTCUT_ENGINE) + ":\n" +
                "  " + lang.get(SHORTCUT_ENGINE_MOVE) + "\n" +
                "  " + lang.get(SHORTCUT_ENGINE_ANALYZE) + "\n" +
                "  " + lang.get(SHORTCUT_ENGINE_CONFIGURE) + "\n" +

                // PGN/Браузер
                "\n" + lang.get(SHORTCUT_PGN) + ":\n" +
                "  " + lang.get(SHORTCUT_OPEN_BROWSER) + "\n" +
                "  " + lang.get(SHORTCUT_REFRESH_BROWSER) + "\n" +
                "  " + lang.get(SHORTCUT_NEXT_GAME) + "\n" +
                "  " + lang.get(SHORTCUT_PREV_GAME) + "\n" +
                "  " + lang.get(SHORTCUT_NEXT_BROWSER) + "\n" +
                "  " + lang.get(SHORTCUT_PREV_BROWSER) + "\n" +
                "  " + lang.get(SHORTCUT_CLOSE_BROWSER) + "\n" +

                // База данных
                "\n" + lang.get(SHORTCUT_DATABASE) + ":\n" +
                "  " + lang.get(SHORTCUT_CONNECT_DB) + "\n" +
                "  " + lang.get(SHORTCUT_IMPORT_DB) + "\n" +
                "  " + lang.get(SHORTCUT_SEARCH_DB) + "\n" +

                // Окна
                "\n" + lang.get(SHORTCUT_WINDOWS) + ":\n" +
                "  " + lang.get(SHORTCUT_CLOSE_ALL_BROWSERS) + "\n" +
                "\n═══════════════════════════════════════";

        // Показываем диалог с информацией
        showInfo(lang.get(SHORTCUTS_TITLE), shortcuts);
    }

    public void showAboutDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(lang.get(ABOUT_TITLE));

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        Group logo = LogoGenerator.createLogo(300, 225);
        content.getChildren().add(logo);

        Label infoLabel = new Label(lang.get(ABOUT_CONTENT));
        infoLabel.setWrapText(true);
        infoLabel.setStyle("-fx-font-size: 12px;");
        content.getChildren().add(infoLabel);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showNotification(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(lang.get(NOTIFICATION_INFO));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}