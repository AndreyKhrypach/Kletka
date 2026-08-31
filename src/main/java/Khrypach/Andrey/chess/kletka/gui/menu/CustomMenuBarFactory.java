/*
 *
 *  * Copyright (c) 2024 Andrey Khrypach
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

package Khrypach.Andrey.chess.kletka.gui.menu;

import Khrypach.Andrey.chess.kletka.gui.board.BoardTheme;
import Khrypach.Andrey.chess.kletka.gui.board.ChessBoardView;
import Khrypach.Andrey.chess.kletka.gui.board.NotationView;
import Khrypach.Andrey.chess.kletka.gui.book.BookManager;
import Khrypach.Andrey.chess.kletka.gui.coach.timer.TimerPanel;
import Khrypach.Andrey.chess.kletka.gui.controllers.MainController;
import Khrypach.Andrey.chess.kletka.gui.board.BoardSizeController;
import Khrypach.Andrey.chess.kletka.gui.dialogs.DonateDialog;
import Khrypach.Andrey.chess.kletka.gui.dialogs.PreferencesDialog;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.model.RootNode;
import Khrypach.Andrey.chess.kletka.gui.model.Variation;
import Khrypach.Andrey.chess.kletka.gui.settings.AppPreferences;
import Khrypach.Andrey.chess.kletka.pgn.index.manager.PgnBrowserManager;
import Khrypach.Andrey.chess.kletka.pgn.index.ui.PgnFileBrowser;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

public class CustomMenuBarFactory {

    private static final Logger log = LoggerFactory.getLogger(CustomMenuBarFactory.class);

    private final LanguageManager lang = LanguageManager.getInstance();

    private final MainController controller;
    private final Stage primaryStage;
    private final BoardSizeController sizeController;

    // Поля для меню
    private CheckMenuItem coordinatesItem;
    private MenuItem undoMarkerItem;
    private MenuItem redoMarkerItem;

    // Языковые кнопки
    private StackPane ruWrapper;
    private StackPane cnWrapper;
    private StackPane usWrapper;

    // Тема
    private RadioMenuItem woodThemeItem;
    private RadioMenuItem classicThemeItem;
    private RadioMenuItem greenThemeItem;
    private RadioMenuItem blueThemeItem;
    private ToggleGroup themeToggleGroup;

    private MenuItem windowsClipboardStatusItem;
    private MenuItem windowsClearClipboardItem;
    private MenuItem windowsCloseAllItem;
    private ContextMenu windowsContextMenu;

    // ========== ХРАНИМ ССЫЛКИ НА ОТКРЫТЫЕ МЕНЮ ==========
    private ContextMenu currentlyOpenMenu = null;
    private Button currentlyOpenButton = null;

    private Menu recentBooksMenu;
    private Menu availableBooksMenu;

    @Getter
    @Setter
    private TimerPanel timerPanel;

    public CustomMenuBarFactory(MainController controller, Stage primaryStage, BoardSizeController sizeController) {
        this.controller = controller;
        this.primaryStage = primaryStage;
        this.sizeController = sizeController;
    }

    public HBox createCustomMenuBar() {
        HBox menuContainer = new HBox(0);
        menuContainer.setStyle("-fx-background-color: #f0f0f0;");
        menuContainer.setPadding(new Insets(0, 10, 0, 10));
        menuContainer.setAlignment(Pos.CENTER_LEFT);

        // Создаем кнопки меню
        Button fileButton = createMenuButton(lang.get(MENU_FILE));
        Button databaseButton = createMenuButton(lang.get(MENU_DATABASE));
        Button editButton = createMenuButton(lang.get(MENU_EDIT));
        Button viewButton = createMenuButton(lang.get(MENU_VIEW));
        Button windowsButton = createMenuButton(lang.get(MENU_WINDOWS));
        Button engineButton = createMenuButton(lang.get(MENU_ENGINE));
        Button booksButton = createMenuButton(lang.get(MENU_BOOKS));
        Button languageButton = createMenuButton(lang.get(MENU_LANGUAGE));
        Button helpButton = createMenuButton(lang.get(MENU_HELP));

        // Создаем контекстные меню для каждой кнопки
        ContextMenu fileContext = createFileMenu();
        ContextMenu databaseContext = createDatabaseMenu();
        ContextMenu editContext = createEditMenu();
        ContextMenu viewContext = createViewMenu();
        ContextMenu windowsContext = createWindowsMenu();
        ContextMenu engineContext = createEngineMenu();
        ContextMenu booksContext = createBooksMenu();
        ContextMenu languageContext = createLanguageMenu();
        ContextMenu helpContext = createHelpMenu();

        // Привязываем контекстные меню к кнопкам
        fileButton.setOnAction(e -> showContextMenu(fileButton, fileContext));
        databaseButton.setOnAction(e -> showContextMenu(databaseButton, databaseContext));
        editButton.setOnAction(e -> showContextMenu(editButton, editContext));
        viewButton.setOnAction(e -> showContextMenu(viewButton, viewContext));
        windowsButton.setOnAction(e -> showContextMenu(windowsButton, windowsContext));
        engineButton.setOnAction(e -> showContextMenu(engineButton, engineContext));
        booksButton.setOnAction(e -> showContextMenu(booksButton, booksContext));
        languageButton.setOnAction(e -> showContextMenu(languageButton, languageContext));
        helpButton.setOnAction(e -> showContextMenu(helpButton, helpContext));

        // Добавляем все кнопки
        menuContainer.getChildren().addAll(
                fileButton,
                databaseButton,
                editButton,
                viewButton,
                windowsButton,
                engineButton,
                booksButton,
                languageButton,
                helpButton
        );

        // ========== ОТСТУП ПОСЛЕ HELP (ширина = ширина часов) ==========
        // Создаём регион-спейсер с шириной, равной ширине часов
        Region spacer = new Region();
        if (timerPanel != null) {
            // Устанавливаем ширину, равную ширине часов (можно динамически или фиксированно)
            spacer.setPrefWidth(80);  // ← Можно увеличить до 30-40 для большего отступа
        }
        HBox.setHgrow(spacer, Priority.NEVER);
        menuContainer.getChildren().add(spacer);

        // ========== ЧАСЫ ==========
        if (timerPanel != null) {
            timerPanel.setMenuMode(true);
            timerPanel.setPrefWidth(160);
            menuContainer.getChildren().add(timerPanel);
        }

        // ========== ПРАВАЯ ЧАСТЬ: ЯЗЫКИ ==========
        HBox spacer3 = new HBox();
        HBox.setHgrow(spacer3, Priority.ALWAYS);
        menuContainer.getChildren().add(spacer3);

        HBox languageButtons = createLanguageButtons();
        menuContainer.getChildren().add(languageButtons);

        // Подписываемся на обновление меню окон
        PgnBrowserManager.getInstance().addBrowserListListener(this::updateWindowsMenu);

        return menuContainer;
    }

    /**
     * Создает кнопку меню
     */
    private Button createMenuButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-text-fill: #333333; " +
                        "-fx-font-size: 13px; " +
                        "-fx-padding: 5 10 5 10; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-color: transparent; " +
                        "-fx-border-width: 0 0 2 0;"
        );

        // Подсветка при наведении
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: #e0e0e0; " +
                        "-fx-text-fill: #333333; " +
                        "-fx-font-size: 13px; " +
                        "-fx-padding: 5 10 5 10; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-color: #4CAF50; " +
                        "-fx-border-width: 0 0 2 0;"
        ));

        button.setOnMouseExited(e -> {
            // Если меню открыто для этой кнопки - не убираем подсветку
            if (currentlyOpenButton != button) {
                button.setStyle(
                        "-fx-background-color: transparent; " +
                                "-fx-text-fill: #333333; " +
                                "-fx-font-size: 13px; " +
                                "-fx-padding: 5 10 5 10; " +
                                "-fx-cursor: hand; " +
                                "-fx-border-color: transparent; " +
                                "-fx-border-width: 0 0 2 0;"
                );
            }
        });

        // Блокируем фокус
        button.setFocusTraversable(false);

        return button;
    }

    /**
     * Показывает контекстное меню под кнопкой
     */
    private void showContextMenu(Button button, ContextMenu contextMenu) {
        // Закрываем текущее открытое меню
        closeCurrentMenu();

        // Сохраняем ссылку на открытое меню
        currentlyOpenMenu = contextMenu;
        currentlyOpenButton = button;

        // Подсвечиваем кнопку
        button.setStyle(
                "-fx-background-color: #e0e0e0; " +
                        "-fx-text-fill: #333333; " +
                        "-fx-font-size: 13px; " +
                        "-fx-padding: 5 10 5 10; " +
                        "-fx-cursor: hand; " +
                        "-fx-border-color: #4CAF50; " +
                        "-fx-border-width: 0 0 2 0;"
        );

        // Показываем меню
        contextMenu.show(button,
                button.localToScreen(0, button.getHeight()).getX(),
                button.localToScreen(0, button.getHeight()).getY());

        // Возвращаем фокус на доску
        returnFocusToBoard();

        // Обработчик закрытия меню
        contextMenu.setOnHidden(e -> {
            closeCurrentMenu();
            returnFocusToBoard();
        });
    }

    /**
     * Закрывает текущее открытое меню
     */
    private void closeCurrentMenu() {
        if (currentlyOpenMenu != null) {
            currentlyOpenMenu.hide();
            currentlyOpenMenu = null;
        }
        if (currentlyOpenButton != null) {
            // Убираем подсветку с кнопки
            currentlyOpenButton.setStyle(
                    "-fx-background-color: transparent; " +
                            "-fx-text-fill: #333333; " +
                            "-fx-font-size: 13px; " +
                            "-fx-padding: 5 10 5 10; " +
                            "-fx-cursor: hand; " +
                            "-fx-border-color: transparent; " +
                            "-fx-border-width: 0 0 2 0;"
            );
            currentlyOpenButton = null;
        }
    }

    /**
     * Возвращает фокус на доску
     */
    private void returnFocusToBoard() {
        Platform.runLater(() -> {
            ChessBoardView boardView = controller.getBoardView();
            if (boardView != null) {
                boardView.requestFocusOnScene();
            }
        });
    }

    // ========== СОЗДАНИЕ КОНТЕКСТНЫХ МЕНЮ ==========

    private ContextMenu createFileMenu() {
        ContextMenu menu = new ContextMenu();
        menu.setStyle("-fx-open-on-hover: false;");

        MenuItem newGameItem = new MenuItem(lang.get(MENU_FILE_NEW_GAME));
        newGameItem.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        newGameItem.setOnAction(e -> {
            controller.resetGame();
            returnFocusToBoard();
        });

        MenuItem openPgnItem = new MenuItem(lang.get(MENU_FILE_OPEN_PGN));
        openPgnItem.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        openPgnItem.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(lang.get(MENU_FILE_OPEN_PGN));
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(lang.get(FILE_FILTER_PGN), "*.pgn")
            );
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                controller.loadPgnFile(file);
            }
            returnFocusToBoard();
        });

        MenuItem openPgnBrowserItem = new MenuItem(lang.get(MENU_FILE_OPEN_BROWSER));
        openPgnBrowserItem.setAccelerator(KeyCombination.keyCombination("Ctrl+B"));
        openPgnBrowserItem.setOnAction(e -> {
            controller.showPgnBrowser();
            returnFocusToBoard();
        });

        MenuItem refreshBrowserItem = new MenuItem(lang.get(MENU_FILE_REFRESH_BROWSER));
        refreshBrowserItem.setAccelerator(KeyCombination.keyCombination("Ctrl+R"));
        refreshBrowserItem.setOnAction(e -> {
            controller.refreshPgnBrowser();
            returnFocusToBoard();
        });

        MenuItem savePgnItem = new MenuItem(lang.get(MENU_FILE_SAVE_PGN));
        savePgnItem.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        savePgnItem.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(lang.get(MENU_FILE_SAVE_PGN));
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(lang.get(FILE_FILTER_PGN), "*.pgn")
            );
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                controller.savePgnFile(file);
            }
            returnFocusToBoard();
        });

        MenuItem exportCurrentItem = new MenuItem(lang.get(MENU_FILE_EXPORT_CURRENT));
        exportCurrentItem.setAccelerator(KeyCombination.keyCombination("Ctrl+E"));
        exportCurrentItem.setOnAction(e -> {
            controller.exportCurrentGameToPgn();
            returnFocusToBoard();
        });

        MenuItem importClipboardItem = new MenuItem(lang.get(MENU_FILE_IMPORT_CLIPBOARD));
        importClipboardItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+V"));
        importClipboardItem.setOnAction(e -> {
            controller.importPgnFromClipboard();
            returnFocusToBoard();
        });

        SeparatorMenuItem separator1 = new SeparatorMenuItem();

        MenuItem setupPositionItem = new MenuItem(lang.get(MENU_FILE_SETUP_POSITION));
        setupPositionItem.setAccelerator(KeyCombination.keyCombination("Ctrl+P"));
        setupPositionItem.setOnAction(e -> {
            controller.setupPosition();
            returnFocusToBoard();
        });

        SeparatorMenuItem separator2 = new SeparatorMenuItem();

        MenuItem exitItem = new MenuItem(lang.get(MENU_FILE_EXIT));
        exitItem.setAccelerator(KeyCombination.keyCombination("Alt+F4"));
        exitItem.setOnAction(e -> {
            if (primaryStage != null) {
                primaryStage.close();
            }
        });

        menu.getItems().addAll(
                newGameItem,
                openPgnItem,
                openPgnBrowserItem,
                refreshBrowserItem,
                savePgnItem,
                exportCurrentItem,
                importClipboardItem,
                separator1,
                setupPositionItem,
                separator2,
                exitItem
        );

        return menu;
    }

    private ContextMenu createDatabaseMenu() {
        ContextMenu menu = new ContextMenu();
        menu.setStyle("-fx-open-on-hover: false;");

        MenuItem connectItem = new MenuItem(lang.get(MENU_DATABASE_CONNECT));
        connectItem.setAccelerator(KeyCombination.keyCombination("Ctrl+D"));
        connectItem.setOnAction(e -> {
            controller.connectToDatabase();
            returnFocusToBoard();
        });

        MenuItem openLastItem = new MenuItem(lang.get(MENU_DATABASE_OPEN_LAST));
        openLastItem.setOnAction(e -> {
            controller.openLastDatabase();
            returnFocusToBoard();
        });

        SeparatorMenuItem separator1 = new SeparatorMenuItem();

        MenuItem importItem = new MenuItem(lang.get(MENU_DATABASE_IMPORT));
        importItem.setAccelerator(KeyCombination.keyCombination("Ctrl+I"));
        importItem.setOnAction(e -> {
            controller.importPgnToDatabase();
            returnFocusToBoard();
        });

        MenuItem searchItem = new MenuItem(lang.get(MENU_DATABASE_SEARCH));
        searchItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+F"));
        searchItem.setOnAction(e -> {
            controller.searchDatabase();
            returnFocusToBoard();
        });

        SeparatorMenuItem separator2 = new SeparatorMenuItem();

        MenuItem statsItem = new MenuItem(lang.get(MENU_DATABASE_STATS));
        statsItem.setOnAction(e -> {
            controller.showOpeningStatistics();
            returnFocusToBoard();
        });

        MenuItem infoItem = new MenuItem(lang.get(MENU_DATABASE_INFO));
        infoItem.setOnAction(e -> {
            controller.showDatabaseInfo();
            returnFocusToBoard();
        });

        menu.getItems().addAll(
                connectItem,
                openLastItem,
                separator1,
                importItem,
                searchItem,
                separator2,
                statsItem,
                infoItem
        );

        return menu;
    }

    private ContextMenu createEditMenu() {
        ContextMenu menu = new ContextMenu();
        menu.setStyle("-fx-open-on-hover: false;");

        undoMarkerItem = new MenuItem(lang.get(MENU_EDIT_UNDO_MARKER));
        undoMarkerItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
        undoMarkerItem.setOnAction(e -> {
            ChessBoardView boardView = controller.getBoardView();
            if (boardView != null && boardView.getCoachTools() != null) {
                boardView.getCoachTools().undoLastAction();
            }
            returnFocusToBoard();
        });

        redoMarkerItem = new MenuItem(lang.get(MENU_EDIT_REDO_MARKER));
        redoMarkerItem.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));
        redoMarkerItem.setOnAction(e -> {
            ChessBoardView boardView = controller.getBoardView();
            if (boardView != null && boardView.getCoachTools() != null) {
                boardView.getCoachTools().redo();
            }
            returnFocusToBoard();
        });

        SeparatorMenuItem separator = new SeparatorMenuItem();

        MenuItem preferencesItem = new MenuItem(lang.get(MENU_EDIT_PREFERENCES));
        preferencesItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        preferencesItem.setOnAction(e -> {
            showPreferencesDialog();
            returnFocusToBoard();
        });

        menu.getItems().addAll(undoMarkerItem, redoMarkerItem, separator, preferencesItem);

        updateUndoRedoState(false, false);

        return menu;
    }

    private ContextMenu createViewMenu() {
        ContextMenu menu = new ContextMenu();
        menu.setStyle("-fx-open-on-hover: false;");

        MenuItem flipBoardItem = new MenuItem(lang.get(MENU_VIEW_FLIP_BOARD));
        flipBoardItem.setAccelerator(KeyCombination.keyCombination("Ctrl+F"));
        flipBoardItem.setOnAction(e -> {
            controller.flipBoard();
            returnFocusToBoard();
        });

        coordinatesItem = new CheckMenuItem(lang.get(MENU_VIEW_COORDINATES));
        coordinatesItem.setSelected(AppPreferences.isShowCoordinates());
        coordinatesItem.setOnAction(e -> {
            controller.toggleCoordinates(coordinatesItem.isSelected());
            AppPreferences.saveShowCoordinates(coordinatesItem.isSelected());
            returnFocusToBoard();
        });

        SeparatorMenuItem separator = new SeparatorMenuItem();

        // Подменю темы
        Menu themeSubMenu = createBoardThemeMenu();

        Menu zoomMenu = new Menu(lang.get(MENU_VIEW_ZOOM));
        MenuItem zoomInItem = new MenuItem(lang.get(MENU_VIEW_ZOOM_IN));
        zoomInItem.setAccelerator(new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.CONTROL_DOWN));
        zoomInItem.setOnAction(e -> {
            sizeController.increaseSize();
            returnFocusToBoard();
        });

        MenuItem zoomOutItem = new MenuItem(lang.get(MENU_VIEW_ZOOM_OUT));
        zoomOutItem.setAccelerator(KeyCombination.keyCombination("Ctrl+-"));
        zoomOutItem.setOnAction(e -> {
            sizeController.decreaseSize();
            returnFocusToBoard();
        });

        MenuItem zoomResetItem = new MenuItem(lang.get(MENU_VIEW_ZOOM_RESET));
        zoomResetItem.setAccelerator(KeyCombination.keyCombination("Ctrl+0"));
        zoomResetItem.setOnAction(e -> {
            sizeController.resetSize();
            returnFocusToBoard();
        });

        zoomMenu.getItems().addAll(zoomInItem, zoomOutItem, zoomResetItem);

        MenuItem toggleNotationItem = new MenuItem(lang.get(MENU_VIEW_TOGGLE_NOTATION));
        toggleNotationItem.setAccelerator(new KeyCodeCombination(KeyCode.H));
        toggleNotationItem.setOnAction(e -> {
            ChessBoardView boardView = controller.getBoardView();
            if (boardView != null) {
                NotationView notationView = boardView.getNotationView();
                if (notationView != null) {
                    notationView.setNotationVisible(!notationView.isNotationVisible());
                }
            }
            returnFocusToBoard();
        });

        menu.getItems().addAll(
                flipBoardItem,
                coordinatesItem,
                separator,
                themeSubMenu,
                zoomMenu,
                toggleNotationItem
        );

        loadSavedTheme();

        return menu;
    }

    private Menu createBoardThemeMenu() {
        Menu themeMenu = new Menu(lang.get(MENU_BOARD_THEME));
        themeToggleGroup = new ToggleGroup();

        BoardTheme.Theme[] themes = BoardTheme.THEMES;

        woodThemeItem = createThemeMenuItem(themes[0], 0);
        classicThemeItem = createThemeMenuItem(themes[1], 1);
        greenThemeItem = createThemeMenuItem(themes[2], 2);
        blueThemeItem = createThemeMenuItem(themes[3], 3);

        themeMenu.getItems().addAll(
                woodThemeItem,
                classicThemeItem,
                greenThemeItem,
                blueThemeItem
        );

        return themeMenu;
    }

    private RadioMenuItem createThemeMenuItem(BoardTheme.Theme theme, int index) {
        RadioMenuItem item = new RadioMenuItem(theme.name());
        item.setToggleGroup(themeToggleGroup);
        item.setUserData(index);

        item.setOnAction(e -> {
            ChessBoardView boardView = controller.getBoardView();
            if (boardView != null) {
                boardView.setBoardTheme(theme);
                AppPreferences.saveBoardTheme(index);
            }
            returnFocusToBoard();
        });

        return item;
    }

    private void loadSavedTheme() {
        int savedThemeIndex = AppPreferences.getBoardThemeIndex();
        if (savedThemeIndex < 0 || savedThemeIndex >= BoardTheme.THEMES.length) {
            savedThemeIndex = 0;
        }

        RadioMenuItem selectedItem;
        switch (savedThemeIndex) {
            case 1 -> selectedItem = classicThemeItem;
            case 2 -> selectedItem = greenThemeItem;
            case 3 -> selectedItem = blueThemeItem;
            default -> selectedItem = woodThemeItem;
        }

        if (selectedItem != null) {
            selectedItem.setSelected(true);
            ChessBoardView boardView = controller.getBoardView();
            if (boardView != null && savedThemeIndex < BoardTheme.THEMES.length) {
                boardView.setBoardTheme(BoardTheme.THEMES[savedThemeIndex]);
            }
        }
    }

    private ContextMenu createWindowsMenu() {
        // ========== СОЗДАЕМ КОНТЕКСТНОЕ МЕНЮ НАПРЯМУЮ, БЕЗ ВЛОЖЕННОГО MENU ==========
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-open-on-hover: false;");

        // Добавляем пункты напрямую в контекстное меню
        windowsClipboardStatusItem = new MenuItem(lang.get(MENU_WINDOWS_CLIPBOARD_EMPTY));
        windowsClipboardStatusItem.setDisable(true);
        contextMenu.getItems().add(windowsClipboardStatusItem);

        contextMenu.getItems().add(new SeparatorMenuItem());

        windowsClearClipboardItem = new MenuItem(lang.get(MENU_WINDOWS_CLEAR_CLIPBOARD));
        windowsClearClipboardItem.setOnAction(e -> {
            PgnBrowserManager.getInstance().clearClipboard();
            updateWindowsMenu();
            returnFocusToBoard();
        });
        windowsClearClipboardItem.setDisable(true);
        contextMenu.getItems().add(windowsClearClipboardItem);

        contextMenu.getItems().add(new SeparatorMenuItem());

        windowsCloseAllItem = new MenuItem(lang.get(MENU_WINDOWS_CLOSE_ALL));
        windowsCloseAllItem.setAccelerator(new KeyCodeCombination(KeyCode.W,
                KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        windowsCloseAllItem.setOnAction(e -> {
            PgnBrowserManager.getInstance().closeAllBrowsers();
            updateWindowsMenu();
            returnFocusToBoard();
        });
        windowsCloseAllItem.setDisable(true);
        contextMenu.getItems().add(windowsCloseAllItem);

        // Сохраняем ссылку на контекстное меню для обновления
        this.windowsContextMenu = contextMenu;

        // Обновляем меню
        updateWindowsMenu();

        return contextMenu;
    }

    private ContextMenu createEngineMenu() {
        ContextMenu menu = new ContextMenu();
        menu.setStyle("-fx-open-on-hover: false;");

        MenuItem engineConfigureItem = new MenuItem(lang.get(MENU_ENGINE_CONFIGURE));
        engineConfigureItem.setOnAction(e -> {
            controller.configureEngine();
            returnFocusToBoard();
        });

        MenuItem engineAnalyzeItem = new MenuItem(lang.get(MENU_ENGINE_ANALYZE));
        engineAnalyzeItem.setAccelerator(KeyCombination.keyCombination("Ctrl+A"));
        engineAnalyzeItem.setOnAction(e -> {
            controller.showBestMove();
            returnFocusToBoard();
        });

        menu.getItems().addAll(engineConfigureItem, engineAnalyzeItem);
        return menu;
    }

    private ContextMenu createBooksMenu() {
        ContextMenu menu = new ContextMenu();
        menu.setStyle("-fx-open-on-hover: false;");

        MenuItem loadBookItem = new MenuItem(lang.get(MENU_BOOKS_LOAD));
        loadBookItem.setAccelerator(new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN));
        loadBookItem.setOnAction(e -> {
            if (controller != null) {
                controller.openPolyglotBook();
            }
            returnFocusToBoard();
        });
        menu.getItems().add(loadBookItem);

        MenuItem clearBookItem = new MenuItem(lang.get(MENU_BOOKS_CLEAR));
        clearBookItem.setAccelerator(new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        clearBookItem.setOnAction(e -> {
            if (controller != null) {
                controller.clearBook();
            }
            returnFocusToBoard();
        });
        menu.getItems().add(clearBookItem);

        menu.getItems().add(new SeparatorMenuItem());

        // ========== СОХРАНЯЕМ ССЫЛКИ НА ПОДМЕНЮ ==========
        recentBooksMenu = new Menu(lang.get(MENU_BOOKS_RECENT));
        updateRecentBooksMenu(recentBooksMenu);
        menu.getItems().add(recentBooksMenu);

        availableBooksMenu = new Menu(lang.get(MENU_BOOKS_AVAILABLE));
        updateAvailableBooksMenu(availableBooksMenu);
        menu.getItems().add(availableBooksMenu);

        menu.getItems().add(new SeparatorMenuItem());

        MenuItem bookInfoItem = new MenuItem(lang.get(MENU_BOOKS_INFO));
        bookInfoItem.setOnAction(e -> {
            showBookInfo();
            returnFocusToBoard();
        });
        menu.getItems().add(bookInfoItem);

        return menu;
    }

    /**
     * Обновляет меню "Книги" (последние и доступные книги)
     */
    public void updateBooksMenu() {
        if (recentBooksMenu != null) {
            updateRecentBooksMenu(recentBooksMenu);
        }
        if (availableBooksMenu != null) {
            updateAvailableBooksMenu(availableBooksMenu);
        }
    }

    private ContextMenu createLanguageMenu() {
        ContextMenu menu = new ContextMenu();
        menu.setStyle("-fx-open-on-hover: false;");

        CheckMenuItem russianItem = new CheckMenuItem(lang.get(MENU_LANGUAGE_RUSSIAN));
        russianItem.setOnAction(e -> changeLanguage("ru"));

        CheckMenuItem englishItem = new CheckMenuItem(lang.get(MENU_LANGUAGE_ENGLISH));
        englishItem.setOnAction(e -> changeLanguage("en"));

        CheckMenuItem chineseItem = new CheckMenuItem(lang.get(MENU_LANGUAGE_CHINESE));
        chineseItem.setOnAction(e -> changeLanguage("zh"));

        menu.getItems().addAll(russianItem, englishItem, chineseItem);

        // Обновляем состояние
        String currentLang = lang.getCurrentLanguage().getCode();
        russianItem.setSelected("ru".equals(currentLang));
        englishItem.setSelected("en".equals(currentLang));
        chineseItem.setSelected("zh".equals(currentLang));

        return menu;
    }

    private ContextMenu createHelpMenu() {
        ContextMenu menu = new ContextMenu();
        menu.setStyle("-fx-open-on-hover: false;");

        MenuItem shortcutsItem = new MenuItem(lang.get(MENU_HELP_SHORTCUTS));
        shortcutsItem.setAccelerator(KeyCombination.keyCombination("Ctrl+H"));
        shortcutsItem.setOnAction(e -> {
            controller.showShortcuts();
            returnFocusToBoard();
        });

        MenuItem aboutItem = new MenuItem(lang.get(MENU_HELP_ABOUT));
        aboutItem.setOnAction(e -> {
            controller.showAboutDialog();
            returnFocusToBoard();
        });

        MenuItem githubItem = new MenuItem(lang.get(MENU_HELP_GITHUB));
        githubItem.setOnAction(e -> {
            openGitHubPage();
            returnFocusToBoard();
        });

        MenuItem checkUpdatesItem = new MenuItem(lang.get(MENU_HELP_CHECK_UPDATES));
        checkUpdatesItem.setOnAction(e -> {
            checkForUpdates();
            returnFocusToBoard();
        });

        MenuItem donateItem = new MenuItem(lang.get(MENU_HELP_DONATE));
        donateItem.setOnAction(e -> {
            DonateDialog dialog = new DonateDialog(primaryStage);
            dialog.showAndWait();
            returnFocusToBoard();
        });

        menu.getItems().addAll(
                shortcutsItem,
                new SeparatorMenuItem(),
                githubItem,
                checkUpdatesItem,
                new SeparatorMenuItem(),
                donateItem,
                aboutItem
        );

        return menu;
    }

    // ========== ЯЗЫКОВЫЕ КНОПКИ ==========

    private HBox createLanguageButtons() {
        Button ruButton = createLanguageButton("/images/flags/Rus.png", "Русский", "ru");
        Button cnButton = createLanguageButton("/images/flags/Zng.png", "中文", "zh");
        Button usButton = createLanguageButton("/images/flags/Eng.png", "English", "en");

        Button donateButton = createDonateButton();

        ruWrapper = wrapWithBorder(ruButton);
        cnWrapper = wrapWithBorder(cnButton);
        usWrapper = wrapWithBorder(usButton);

        updateActiveLanguageBorder(lang.getCurrentLanguage().getCode());

        ruButton.setOnAction(e -> changeLanguage("ru"));
        cnButton.setOnAction(e -> changeLanguage("zh"));
        usButton.setOnAction(e -> changeLanguage("en"));

        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER_RIGHT);
        box.setPadding(new Insets(0, 0, 0, 0));
        box.getChildren().addAll(ruWrapper, cnWrapper, usWrapper, donateButton);

        return box;
    }

    private StackPane wrapWithBorder(Button button) {
        StackPane wrapper = new StackPane(button);
        wrapper.setMinSize(38, 38);
        wrapper.setMaxSize(38, 38);
        wrapper.setPrefSize(38, 38);
        wrapper.setPadding(Insets.EMPTY);
        wrapper.setStyle(
                "-fx-background-color: transparent; " +
                        "-fx-border-color: transparent; " +
                        "-fx-border-width: 3px; " +
                        "-fx-border-radius: 50%;"
        );
        wrapper.setOnMouseClicked(e -> button.fire());
        return wrapper;
    }

    private void updateActiveLanguageBorder(String activeLang) {
        String activeStyle =
                "-fx-background-color: transparent; " +
                        "-fx-border-color: #4CAF50; " +
                        "-fx-border-width: 3px; " +
                        "-fx-border-radius: 50%;";

        String normalStyle =
                "-fx-background-color: transparent; " +
                        "-fx-border-color: transparent; " +
                        "-fx-border-width: 3px; " +
                        "-fx-border-radius: 50%;";

        ruWrapper.setStyle("ru".equals(activeLang) ? activeStyle : normalStyle);
        cnWrapper.setStyle("zh".equals(activeLang) ? activeStyle : normalStyle);
        usWrapper.setStyle("en".equals(activeLang) ? activeStyle : normalStyle);
    }

    private Button createLanguageButton(String flagPath, String tooltip, String languageCode) {
        Button button = new Button();
        button.setTooltip(new Tooltip(tooltip));
        button.setFocusTraversable(false);
        button.setUserData(languageCode);

        try {
            Image flagImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(flagPath)));
            if (!flagImage.isError()) {
                ImageView imageView = new ImageView(flagImage);
                imageView.setFitWidth(32);
                imageView.setFitHeight(32);
                imageView.setPreserveRatio(false);
                button.setGraphic(imageView);
            } else {
                button.setText("🏳️");
            }
        } catch (Exception e) {
            log.trace("Failed to load flag image: {}", flagPath);
            button.setText("🏳️");
        }

        button.setMinSize(32, 32);
        button.setMaxSize(32, 32);
        button.setPrefSize(32, 32);
        button.setPadding(Insets.EMPTY);

        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(16, 16, 16);
        button.setClip(clip);

        button.setStyle(
                "-fx-cursor: hand; " +
                        "-fx-background-color: transparent; " +
                        "-fx-padding: 0; " +
                        "-fx-border-color: transparent;"
        );

        return button;
    }

    private Button createDonateButton() {
        Button button = new Button(lang.get(MENU_HELP_DONATE));
        button.setStyle(
                "-fx-background-color: #d0d0d0; " +
                        "-fx-text-fill: #1a1a1a; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 12px; " +
                        "-fx-padding: 5 10 5 10; " +
                        "-fx-border-radius: 4; " +
                        "-fx-background-radius: 4; " +
                        "-fx-cursor: hand;"
        );
        button.setTooltip(new Tooltip(lang.get(MENU_HELP_DONATE)));
        button.setFocusTraversable(false);

        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #bebebe; " +
                                "-fx-text-fill: #1a1a1a; " +
                                "-fx-font-weight: bold; " +
                                "-fx-font-size: 12px; " +
                                "-fx-padding: 5 10 5 10; " +
                                "-fx-border-radius: 4; " +
                                "-fx-background-radius: 4; " +
                                "-fx-cursor: hand;"
                )
        );
        button.setOnMouseExited(e ->
                button.setStyle(
                        "-fx-background-color: #d0d0d0; " +
                                "-fx-text-fill: #1a1a1a; " +
                                "-fx-font-weight: bold; " +
                                "-fx-font-size: 12px; " +
                                "-fx-padding: 5 10 5 10; " +
                                "-fx-border-radius: 4; " +
                                "-fx-background-radius: 4; " +
                                "-fx-cursor: hand;"
                )
        );

        button.setOnAction(e -> {
            DonateDialog dialog = new DonateDialog(primaryStage);
            dialog.showAndWait();
            returnFocusToBoard();
        });

        return button;
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private void changeLanguage(String languageCode) {
        String languageName = getLanguageDisplayName(languageCode);
        LanguageManager.getInstance().setLanguage(languageCode);
        updateActiveLanguageBorder(languageCode);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(lang.get(LANG_CHANGE_TITLE));
        alert.setHeaderText(String.format(lang.get(LANG_CHANGE_HEADER), languageName));
        alert.setContentText(lang.get(LANG_CHANGE_CONTENT));
        alert.getButtonTypes().setAll(new ButtonType(lang.get(LANG_CHANGE_BUTTON_OK)));
        alert.showAndWait();
        returnFocusToBoard();
    }

    private String getLanguageDisplayName(String code) {
        return switch (code) {
            case "ru" -> lang.get(MENU_LANGUAGE_RUSSIAN);
            case "en" -> lang.get(MENU_LANGUAGE_ENGLISH);
            case "zh" -> lang.get(MENU_LANGUAGE_CHINESE);
            default -> code;
        };
    }

    private void updateRecentBooksMenu(Menu recentBooksMenu) {
        recentBooksMenu.getItems().clear();
        BookManager bookManager = BookManager.getInstance();
        List<String> recentBooks = bookManager.getRecentBooks();

        if (recentBooks.isEmpty()) {
            MenuItem emptyItem = new MenuItem(lang.get(MENU_BOOKS_NO_RECENT));
            emptyItem.setDisable(true);
            recentBooksMenu.getItems().add(emptyItem);
            return;
        }

        for (String bookPath : recentBooks) {
            Path path = Paths.get(bookPath);
            String fileName = path.getFileName().toString();
            MenuItem item = new MenuItem(fileName);
            item.setOnAction(e -> {
                if (controller != null) {
                    controller.loadPolyglotBook(path.toFile());
                }
                returnFocusToBoard();
            });
            recentBooksMenu.getItems().add(item);
        }
    }

    private void updateAvailableBooksMenu(Menu availableBooksMenu) {
        availableBooksMenu.getItems().clear();
        BookManager bookManager = BookManager.getInstance();
        List<Path> availableBooks = bookManager.getAvailableBooks();

        if (availableBooks.isEmpty()) {
            MenuItem emptyItem = new MenuItem(lang.get(MENU_BOOKS_NO_AVAILABLE));
            emptyItem.setDisable(true);
            availableBooksMenu.getItems().add(emptyItem);
            return;
        }

        for (Path bookPath : availableBooks) {
            String fileName = bookPath.getFileName().toString();
            MenuItem item = new MenuItem(fileName);
            item.setOnAction(e -> {
                if (controller != null) {
                    controller.loadPolyglotBook(bookPath.toFile());
                }
                returnFocusToBoard();
            });
            availableBooksMenu.getItems().add(item);
        }
    }

    private void showBookInfo() {
        BookManager bookManager = BookManager.getInstance();
        if (!bookManager.isBookLoaded()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(lang.get(MENU_BOOKS_INFO));
            alert.setHeaderText(null);
            alert.setContentText(lang.get(MENU_BOOKS_NO_BOOK_LOADED));
            alert.showAndWait();
            return;
        }

        Path bookPath = bookManager.getCurrentBookPath();
        String bookName = bookPath != null ? bookPath.getFileName().toString() : "?";
        int totalEntries = bookManager.getTotalEntries();

        int moveCount = 0;
        int variationCount = 0;
        if (controller != null && controller.getBoardView().getNavController() != null) {
            Variation mainLine = controller.getBoardView().getNavController().getMainLine();
            if (mainLine != null) {
                moveCount = mainLine.getMoveCount();
            }
            RootNode rootNode = controller.getBoardView().getNavController().getRootNode();
            if (rootNode != null) {
                variationCount = rootNode.getSubVariations().size();
            }
        }

        String fileSize = "?";
        try {
            if (bookPath != null) {
                long size = Files.size(bookPath);
                fileSize = formatFileSize(size);
            }
        } catch (IOException e) {
            fileSize = "?";
        }

        String info = String.format(
                """
                        📖 ИНФОРМАЦИЯ О КНИГЕ
                        ─────────────────────
                        %s: %s
                        %s: %s
                        %s: %d
                        %s: %d
                        %s: %d""",
                lang.get(MENU_BOOKS_INFO_NAME), bookName,
                lang.get(MENU_BOOKS_INFO_SIZE), fileSize,
                lang.get(MENU_BOOKS_INFO_ENTRIES), totalEntries,
                lang.get(MENU_BOOKS_INFO_VARIATIONS), variationCount,
                lang.get(MENU_BOOKS_INFO_MOVES), moveCount
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(lang.get(MENU_BOOKS_INFO));
        alert.setHeaderText(null);
        alert.setContentText(info);
        alert.showAndWait();
    }

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

    /**
     * Обновляет меню "Окна" - список открытых PGN браузеров
     */
    private void updateWindowsMenu() {
        Platform.runLater(() -> {
            if (windowsContextMenu == null) {
                return;
            }

            try {
                // Получаем все открытые браузеры
                Collection<PgnFileBrowser> browsers = PgnBrowserManager.getInstance().getAllBrowsers();
                PgnFileBrowser activeBrowser = PgnBrowserManager.getInstance().getActiveBrowser();

                // ========== ОЧИЩАЕМ ВСЕ ПУНКТЫ ==========
                windowsContextMenu.getItems().clear();

                // ========== ДОБАВЛЯЕМ СЛУЖЕБНЫЕ ПУНКТЫ ==========
                if (windowsClipboardStatusItem != null) {
                    windowsContextMenu.getItems().add(windowsClipboardStatusItem);
                }
                windowsContextMenu.getItems().add(new SeparatorMenuItem());
                if (windowsClearClipboardItem != null) {
                    windowsContextMenu.getItems().add(windowsClearClipboardItem);
                }
                windowsContextMenu.getItems().add(new SeparatorMenuItem());
                if (windowsCloseAllItem != null) {
                    windowsContextMenu.getItems().add(windowsCloseAllItem);
                }

                if (browsers.isEmpty()) {
                    // Нет открытых браузеров - показываем сообщение
                    MenuItem noBrowsersItem = new MenuItem(lang.get(MENU_WINDOWS_NO_FILES));
                    noBrowsersItem.setDisable(true);
                    windowsContextMenu.getItems().add(0, noBrowsersItem);

                    if (windowsCloseAllItem != null) {
                        windowsCloseAllItem.setDisable(true);
                    }
                } else {
                    if (windowsCloseAllItem != null) {
                        windowsCloseAllItem.setDisable(false);
                    }

                    // ========== ИСПОЛЬЗУЕМ Set ДЛЯ ОТСЛЕЖИВАНИЯ УНИКАЛЬНЫХ ФАЙЛОВ ==========
                    Set<String> addedFiles = new HashSet<>();

                    // Добавляем пункты для каждого браузера
                    for (PgnFileBrowser browser : browsers) {
                        Path path = browser.getPgnPath();
                        String filePath = path.toString();

                        if (addedFiles.contains(filePath)) {
                            log.debug("Skipping duplicate: {}", filePath);
                            continue;
                        }
                        addedFiles.add(filePath);

                        MenuItem browserItem = createBrowserMenuItem(browser, path, activeBrowser);
                        browserItem.setId("browser_" + filePath.hashCode());

                        browserItem.setOnAction(e -> {
                            browser.showWindow();
                            PgnBrowserManager.getInstance().setActiveBrowser(browser);
                            updateWindowsMenu();
                        });

                        // Добавляем в начало (перед служебными)
                        windowsContextMenu.getItems().add(0, browserItem);
                    }
                }

                // Обновляем статус буфера обмена
                updateClipboardStatus();

            } catch (Exception e) {
                log.trace("Error updating windows menu: {}", e.getMessage());
            }
        });
    }

    /**
     * Создает пункт меню для браузера
     */
    private MenuItem createBrowserMenuItem(PgnFileBrowser browser, Path path, PgnFileBrowser activeBrowser) {
        String fileName = path.getFileName().toString();
        int gameCount = browser.getCurrentIndex() != null ?
                browser.getCurrentIndex().getActiveCount() : 0;

        // Формируем отображаемое имя
        String displayName = String.format("%s (%d)", fileName, gameCount);

        // Отмечаем активный браузер
        boolean isActive = browser == activeBrowser;
        if (isActive) {
            displayName = "✅ " + displayName;
        }

        MenuItem item = new MenuItem(displayName);
        item.setId("browser_" + path.toString().hashCode());
        return item;
    }

    /**
     * Обновляет статус буфера обмена в меню "Окна"
     */
    private void updateClipboardStatus() {
        if (windowsClipboardStatusItem == null || windowsClearClipboardItem == null) {
            return;
        }

        boolean hasClipboard = PgnBrowserManager.getInstance().hasClipboardContent();
        PgnBrowserManager.ClipboardContent content = PgnBrowserManager.getInstance().getClipboardContent();

        if (hasClipboard && content != null) {
            String sourceName = content.sourceFile().getFileName().toString();
            windowsClipboardStatusItem.setText(String.format(
                    lang.get(MENU_WINDOWS_CLIPBOARD_CONTENT),
                    content.count(),
                    sourceName
            ));
            windowsClipboardStatusItem.setDisable(false);
            windowsClearClipboardItem.setDisable(false);
        } else {
            windowsClipboardStatusItem.setText(lang.get(MENU_WINDOWS_CLIPBOARD_EMPTY));
            windowsClipboardStatusItem.setDisable(true);
            windowsClearClipboardItem.setDisable(true);
        }
    }

    public void updateUndoRedoState(boolean canUndo, boolean canRedo) {
        if (undoMarkerItem != null) {
            undoMarkerItem.setDisable(!canUndo);
            if (canUndo) {
                ChessBoardView boardView = controller.getBoardView();
                if (boardView != null && boardView.getCoachTools() != null) {
                    int size = boardView.getCoachTools().getActionHistorySize();
                    undoMarkerItem.setText(lang.get(MENU_EDIT_UNDO_MARKER) + " (" + size + ")");
                }
            } else {
                undoMarkerItem.setText(lang.get(MENU_EDIT_UNDO_MARKER));
            }
        }
        if (redoMarkerItem != null) {
            redoMarkerItem.setDisable(!canRedo);
            if (canRedo) {
                ChessBoardView boardView = controller.getBoardView();
                if (boardView != null && boardView.getCoachTools() != null) {
                    int size = boardView.getCoachTools().getRedoHistorySize();
                    redoMarkerItem.setText(lang.get(MENU_EDIT_REDO_MARKER) + " (" + size + ")");
                }
            } else {
                redoMarkerItem.setText(lang.get(MENU_EDIT_REDO_MARKER));
            }
        }
    }

    public void updateCoordinatesCheckbox(boolean selected) {
        if (coordinatesItem != null) {
            coordinatesItem.setSelected(selected);
        }
    }

    private void showPreferencesDialog() {
        PreferencesDialog dialog = new PreferencesDialog(primaryStage);
        dialog.showAndWait();

        int newSize = AppPreferences.getTileSize();
        if (sizeController != null) {
            sizeController.setTileSize(newSize);
        }

        boolean showCoords = AppPreferences.isShowCoordinates();
        if (controller != null) {
            controller.toggleCoordinates(showCoords);
            updateCoordinatesCheckbox(showCoords);
        }

        boolean flipped = AppPreferences.isBoardFlipped();
        if (controller != null) {
            ChessBoardView boardView = controller.getBoardView();
            if (boardView != null && boardView.isBoardFlipped() != flipped) {
                controller.flipBoard();
            }
        }

        int themeIndex = AppPreferences.getBoardThemeIndex();
        if (controller != null) {
            ChessBoardView boardView = controller.getBoardView();
            if (boardView != null && themeIndex >= 0 && themeIndex < BoardTheme.THEMES.length) {
                boardView.setBoardTheme(BoardTheme.THEMES[themeIndex]);
            }
            updateThemeMenuCheckmarks(themeIndex);
        }
    }

    private void updateThemeMenuCheckmarks(int themeIndex) {
        RadioMenuItem selectedItem = null;
        switch (themeIndex) {
            case 0 -> selectedItem = woodThemeItem;
            case 1 -> selectedItem = classicThemeItem;
            case 2 -> selectedItem = greenThemeItem;
            case 3 -> selectedItem = blueThemeItem;
        }
        if (selectedItem != null) {
            selectedItem.setSelected(true);
        }
    }

    private void openGitHubPage() {
        try {
            String url = "https://github.com/AndreyKhrypach/Kletka";
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (Exception e) {
            showError(lang.get(MENU_HELP_GITHUB_ERROR));
        }
    }

    private void checkForUpdates() {
        Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
        loadingAlert.setTitle(lang.get(MENU_HELP_CHECKING_UPDATES));
        loadingAlert.setHeaderText(null);
        loadingAlert.setContentText(lang.get(MENU_HELP_CHECKING_UPDATES_MSG));
        loadingAlert.show();

        new Thread(() -> {
            try {
                String apiUrl = "https://api.github.com/repos/AndreyKhrypach/Kletka/releases/latest";
                java.net.URL url = new java.net.URL(apiUrl);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    String json = readAnswer(connection);
                    String latestVersion = extractVersionFromJson(json);
                    String currentVersion = getCurrentVersion();

                    Platform.runLater(() -> {
                        loadingAlert.close();
                        if (isNewerVersion(latestVersion, currentVersion)) {
                            showUpdateAvailableDialog(latestVersion);
                        } else {
                            showNoUpdatesDialog();
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        loadingAlert.close();
                        showError(lang.get(MENU_HELP_UPDATE_CHECK_ERROR));
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingAlert.close();
                    showError(lang.get(MENU_HELP_UPDATE_CHECK_ERROR) + ": " + e.getMessage());
                });
            }
        }).start();
    }

    private String readAnswer(HttpURLConnection connection) throws IOException {
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private String extractVersionFromJson(String json) {
        String tagKey = "\"tag_name\":";
        int tagIndex = json.indexOf(tagKey);
        if (tagIndex < 0) return null;

        int startQuote = json.indexOf("\"", tagIndex + tagKey.length());
        if (startQuote < 0) return null;

        int endQuote = json.indexOf("\"", startQuote + 1);
        if (endQuote < 0) return null;

        String tag = json.substring(startQuote + 1, endQuote);
        if (tag.startsWith("v")) {
            tag = tag.substring(1);
        }
        return tag;
    }

    private String getCurrentVersion() {
        try {
            java.util.Properties props = new java.util.Properties();
            java.io.InputStream is = getClass().getResourceAsStream("/version.properties");
            if (is != null) {
                props.load(is);
                return props.getProperty("version", "1.1.0");
            }
        } catch (Exception e) {
            // ignore
        }
        return "1.1.0";
    }

    private boolean isNewerVersion(String latest, String current) {
        if (latest == null || current == null) return false;
        String[] latestParts = latest.split("\\.");
        String[] currentParts = current.split("\\.");

        int maxLength = Math.max(latestParts.length, currentParts.length);
        for (int i = 0; i < maxLength; i++) {
            int latestVal = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;
            int currentVal = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
            if (latestVal > currentVal) return true;
            if (latestVal < currentVal) return false;
        }
        return false;
    }

    private void showUpdateAvailableDialog(String latestVersion) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(lang.get(MENU_HELP_UPDATE_AVAILABLE));
        alert.setHeaderText(String.format(lang.get(MENU_HELP_UPDATE_AVAILABLE_HEADER), latestVersion));
        alert.setContentText(lang.get(MENU_HELP_UPDATE_AVAILABLE_MSG));

        ButtonType downloadButton = new ButtonType(lang.get(MENU_HELP_UPDATE_DOWNLOAD));
        ButtonType laterButton = new ButtonType(lang.get(MENU_HELP_UPDATE_LATER));
        alert.getButtonTypes().setAll(downloadButton, laterButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == downloadButton) {
                try {
                    java.awt.Desktop.getDesktop().browse(
                            java.net.URI.create("https://github.com/AndreyKhrypach/Kletka/releases/latest")
                    );
                } catch (Exception e) {
                    showError(lang.get(MENU_HELP_GITHUB_ERROR));
                }
            }
            returnFocusToBoard();
        });
    }

    private void showNoUpdatesDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(lang.get(MENU_HELP_NO_UPDATES));
        alert.setHeaderText(null);
        alert.setContentText(lang.get(MENU_HELP_NO_UPDATES_MSG));
        alert.showAndWait();
        returnFocusToBoard();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(lang.get(NOTIFICATION_ERROR));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        returnFocusToBoard();
    }
}
