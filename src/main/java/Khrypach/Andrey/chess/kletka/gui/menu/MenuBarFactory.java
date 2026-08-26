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

package Khrypach.Andrey.chess.kletka.gui.menu;

import Khrypach.Andrey.chess.kletka.gui.board.BoardTheme;
import Khrypach.Andrey.chess.kletka.gui.board.ChessBoardView;
import Khrypach.Andrey.chess.kletka.gui.board.NotationView;
import Khrypach.Andrey.chess.kletka.gui.controllers.MainController;
import Khrypach.Andrey.chess.kletka.gui.board.BoardSizeController;
import Khrypach.Andrey.chess.kletka.gui.dialogs.DonateDialog;
import Khrypach.Andrey.chess.kletka.gui.dialogs.PreferencesDialog;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
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
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.CheckMenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

public class MenuBarFactory {

    private static final Logger log = LoggerFactory.getLogger(MenuBarFactory.class);

    private final LanguageManager lang = LanguageManager.getInstance();

    private final MainController controller;
    private final Stage primaryStage;
    private final BoardSizeController sizeController;

    private Menu windowsMenu;

    private CheckMenuItem coordinatesItem;

    private MenuItem windowsClipboardStatusItem;
    private MenuItem windowsClearClipboardItem;
    private MenuItem windowsCloseAllItem;

    private MenuItem undoMarkerItem;
    private MenuItem redoMarkerItem;

    // ========== ПУНКТЫ МЕНЮ ЯЗЫКА ==========
    private CheckMenuItem russianItem;
    private CheckMenuItem englishItem;
    private CheckMenuItem chineseItem;

    private StackPane ruWrapper;
    private StackPane cnWrapper;
    private StackPane usWrapper;

    private RadioMenuItem woodThemeItem;
    private RadioMenuItem classicThemeItem;
    private RadioMenuItem greenThemeItem;
    private RadioMenuItem blueThemeItem;
    private ToggleGroup themeToggleGroup;

    public MenuBarFactory(MainController controller, Stage primaryStage, BoardSizeController sizeController) {
        this.controller = controller;
        this.primaryStage = primaryStage;
        this.sizeController = sizeController;
    }

    public HBox createMenuBarWithLanguageButtons() {
        MenuBar menuBar = new MenuBar();

        // Создаем все меню
        // ========== МЕНЮ ==========
        Menu fileMenu = createFileMenu();
        Menu databaseMenu = createDatabaseMenu();
        Menu editMenu = createEditMenu();
        Menu viewMenu = createViewMenu();
        windowsMenu = createWindowsMenu();
        Menu engineMenu = createEngineMenu();
        Menu languageMenu = createLanguageMenu();
        Menu helpMenu = createHelpMenu();

        menuBar.getMenus().addAll(
                fileMenu,
                databaseMenu,
                editMenu,
                viewMenu,
                windowsMenu,
                engineMenu,
                languageMenu,
                helpMenu
        );

        PgnBrowserManager.getInstance().addBrowserListListener(this::updateWindowsMenu);

        HBox languageButtons = createLanguageButtons();

        HBox container = new HBox();
        container.setAlignment(Pos.CENTER_LEFT);
        container.setStyle("-fx-background-color: #f0f0f0;");
        container.setPadding(new Insets(0, 0, 0, 0));

        HBox.setHgrow(menuBar, Priority.ALWAYS);

        container.getChildren().addAll(menuBar, languageButtons);


        return container;
    }

    // ========== МЕНЮ "ЯЗЫК" ==========
    private Menu createLanguageMenu() {
        Menu menu = new Menu(lang.get(MENU_LANGUAGE));

        russianItem = new CheckMenuItem(lang.get(MENU_LANGUAGE_RUSSIAN));
        russianItem.setOnAction(e -> changeLanguage("ru"));

        englishItem = new CheckMenuItem(lang.get(MENU_LANGUAGE_ENGLISH));
        englishItem.setOnAction(e -> changeLanguage("en"));

        chineseItem = new CheckMenuItem(lang.get(MENU_LANGUAGE_CHINESE));
        chineseItem.setOnAction(e -> changeLanguage("zh"));

        menu.getItems().addAll(russianItem, englishItem, chineseItem);

        updateLanguageMenuCheckmarks();

        return menu;
    }

    /**
     * Обновляет состояние галочек в меню "Язык"
     */
    private void updateLanguageMenuCheckmarks() {
        String currentLang = lang.getCurrentLanguage().getCode();

        if (russianItem != null) {
            russianItem.setSelected("ru".equals(currentLang));
        }
        if (englishItem != null) {
            englishItem.setSelected("en".equals(currentLang));
        }
        if (chineseItem != null) {
            chineseItem.setSelected("zh".equals(currentLang));
        }
    }

    // ========== КНОПКИ ЯЗЫКОВ ==========
    private HBox createLanguageButtons() {
        LanguageManager lang = LanguageManager.getInstance();

        // ========== КНОПКИ ЯЗЫКОВ ==========
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
        box.setPadding(new Insets(0, 12, 0, 0));
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

    /**
     * Создает кнопку Donate
     */
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

        // При наведении - чуть темнее
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
        });

        return button;
    }

    private void changeLanguage(String languageCode) {
        String languageName = getLanguageDisplayName(languageCode);

        LanguageManager.getInstance().setLanguage(languageCode);

        if (languageCode.equals("ru") || languageCode.equals("en") || languageCode.equals("zh")) {
            updateActiveLanguageBorder(languageCode);
        }

        // ========== ОБНОВЛЯЕМ ГАЛОЧКИ В МЕНЮ ==========
        updateLanguageMenuCheckmarks();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(lang.get(LANG_CHANGE_TITLE));
        alert.setHeaderText(String.format(lang.get(LANG_CHANGE_HEADER), languageName));
        alert.setContentText(lang.get(LANG_CHANGE_CONTENT));
        alert.getButtonTypes().setAll(new ButtonType(lang.get(LANG_CHANGE_BUTTON_OK)));
        alert.showAndWait();
    }

    private String getLanguageDisplayName(String code) {
        return switch (code) {
            case "ru" -> lang.get(MENU_LANGUAGE_RUSSIAN);
            case "en" -> lang.get(MENU_LANGUAGE_ENGLISH);
            case "zh" -> lang.get(MENU_LANGUAGE_CHINESE);
            default -> code;
        };
    }

    // ========== СОЗДАНИЕ МЕНЮ ==========

    private Menu createFileMenu() {
        Menu menu = new Menu(lang.get(MENU_FILE));

        // ========== ПУНКТЫ МЕНЮ ==========
        MenuItem newGameItem = new MenuItem(lang.get(MENU_FILE_NEW_GAME));
        newGameItem.setId("newGameMenuItem");
        newGameItem.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        newGameItem.setOnAction(e -> controller.resetGame());

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
        });

        MenuItem openPgnBrowserItem = new MenuItem(lang.get(MENU_FILE_OPEN_BROWSER));
        openPgnBrowserItem.setAccelerator(KeyCombination.keyCombination("Ctrl+B"));
        openPgnBrowserItem.setOnAction(e -> controller.showPgnBrowser());

        MenuItem refreshBrowserItem = new MenuItem(lang.get(MENU_FILE_REFRESH_BROWSER));
        refreshBrowserItem.setAccelerator(KeyCombination.keyCombination("Ctrl+R"));
        refreshBrowserItem.setOnAction(e -> controller.refreshPgnBrowser());

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
        });

        MenuItem exportCurrentItem = new MenuItem(lang.get(MENU_FILE_EXPORT_CURRENT));
        exportCurrentItem.setAccelerator(KeyCombination.keyCombination("Ctrl+E"));
        exportCurrentItem.setOnAction(e -> controller.exportCurrentGameToPgn());

        MenuItem importClipboardItem = new MenuItem(lang.get(MENU_FILE_IMPORT_CLIPBOARD));
        importClipboardItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+V"));
        importClipboardItem.setOnAction(e -> controller.importPgnFromClipboard());

        SeparatorMenuItem separator1 = new SeparatorMenuItem();

        MenuItem setupPositionItem = new MenuItem(lang.get(MENU_FILE_SETUP_POSITION));
        setupPositionItem.setAccelerator(KeyCombination.keyCombination("Ctrl+P"));
        setupPositionItem.setOnAction(e -> controller.setupPosition());

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

    private Menu createDatabaseMenu() {
        Menu menu = new Menu(lang.get(MENU_DATABASE));

        MenuItem connectItem = new MenuItem(lang.get(MENU_DATABASE_CONNECT));
        connectItem.setAccelerator(KeyCombination.keyCombination("Ctrl+D"));
        connectItem.setOnAction(e -> controller.connectToDatabase());

        MenuItem openLastItem = new MenuItem(lang.get(MENU_DATABASE_OPEN_LAST));
        openLastItem.setOnAction(e -> controller.openLastDatabase());

        SeparatorMenuItem separator1 = new SeparatorMenuItem();

        MenuItem importItem = new MenuItem(lang.get(MENU_DATABASE_IMPORT));
        importItem.setAccelerator(KeyCombination.keyCombination("Ctrl+I"));
        importItem.setOnAction(e -> controller.importPgnToDatabase());

        MenuItem searchItem = new MenuItem(lang.get(MENU_DATABASE_SEARCH));
        searchItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+F"));
        searchItem.setOnAction(e -> controller.searchDatabase());

        SeparatorMenuItem separator2 = new SeparatorMenuItem();

        MenuItem statsItem = new MenuItem(lang.get(MENU_DATABASE_STATS));
        statsItem.setOnAction(e -> controller.showOpeningStatistics());

        MenuItem infoItem = new MenuItem(lang.get(MENU_DATABASE_INFO));
        infoItem.setOnAction(e -> controller.showDatabaseInfo());

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

    private Menu createEditMenu() {
        Menu menu = new Menu(lang.get(MENU_EDIT));

        // ========== UNDO/REDO ДЛЯ МАРКЕРОВ ==========
        undoMarkerItem = new MenuItem(lang.get(MENU_EDIT_UNDO_MARKER));
        undoMarkerItem.setId("undoMarkerMenuItem");
        undoMarkerItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
        // Tooltip НЕЛЬЗЯ добавить к MenuItem — убираем!
        undoMarkerItem.setOnAction(e -> {
            ChessBoardView boardView = controller.getBoardView();
            if (boardView != null && boardView.getCoachTools() != null) {
                boardView.getCoachTools().undoLastAction();
            }
        });

        redoMarkerItem = new MenuItem(lang.get(MENU_EDIT_REDO_MARKER));
        redoMarkerItem.setId("redoMarkerMenuItem");
        redoMarkerItem.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));
        // Tooltip НЕЛЬЗЯ добавить к MenuItem — убираем!
        redoMarkerItem.setOnAction(e -> {
            ChessBoardView boardView = controller.getBoardView();
            if (boardView != null && boardView.getCoachTools() != null) {
                boardView.getCoachTools().redo();
            }
        });

        // ========== НАСТРОЙКИ (Ctrl+Shift+O) ==========
        SeparatorMenuItem separator = new SeparatorMenuItem();

        MenuItem preferencesItem = new MenuItem(lang.get(MENU_EDIT_PREFERENCES));
        preferencesItem.setId("preferencesMenuItem");
        preferencesItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        preferencesItem.setOnAction(e -> showPreferencesDialog());

        menu.getItems().addAll(undoMarkerItem, redoMarkerItem, separator, preferencesItem);

        // ========== ПЕРВОНАЧАЛЬНОЕ СОСТОЯНИЕ ==========
        updateUndoRedoState(false, false);

        return menu;
    }

    public void updateUndoRedoState(boolean canUndo, boolean canRedo) {
        if (undoMarkerItem != null) {
            undoMarkerItem.setDisable(!canUndo);
            // Обновляем текст с подсказкой о количестве действий
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

    private Menu createViewMenu() {
        Menu menu = new Menu(lang.get(MENU_VIEW));

        MenuItem flipBoardItem = new MenuItem(lang.get(MENU_VIEW_FLIP_BOARD));
        flipBoardItem.setAccelerator(KeyCombination.keyCombination("Ctrl+F"));
        flipBoardItem.setOnAction(e -> controller.flipBoard());

        coordinatesItem = new CheckMenuItem(lang.get(MENU_VIEW_COORDINATES));
        coordinatesItem.setSelected(AppPreferences.isShowCoordinates());
        coordinatesItem.setOnAction(e -> {
            controller.toggleCoordinates(coordinatesItem.isSelected());
            AppPreferences.saveShowCoordinates(coordinatesItem.isSelected());
        });

        SeparatorMenuItem separator = new SeparatorMenuItem();

        // НОВОЕ: подменю для темы доски
        // ========== ПУНКТЫ МЕНЮ "ТЕМА ДОСКИ" ==========
        Menu boardThemeMenu = createBoardThemeMenu();

        // НОВОЕ: загружаем сохраненную тему
        loadSavedTheme();

        Menu zoomMenu = new Menu(lang.get(MENU_VIEW_ZOOM));
        MenuItem zoomInItem = new MenuItem(lang.get(MENU_VIEW_ZOOM_IN));
        zoomInItem.setAccelerator(KeyCombination.keyCombination("Ctrl+="));
        zoomInItem.setOnAction(e -> sizeController.increaseSize());

        MenuItem zoomOutItem = new MenuItem(lang.get(MENU_VIEW_ZOOM_OUT));
        zoomOutItem.setAccelerator(KeyCombination.keyCombination("Ctrl+-"));
        zoomOutItem.setOnAction(e -> sizeController.decreaseSize());

        MenuItem zoomResetItem = new MenuItem(lang.get(MENU_VIEW_ZOOM_RESET));
        zoomResetItem.setAccelerator(KeyCombination.keyCombination("Ctrl+0"));
        zoomResetItem.setOnAction(e -> sizeController.resetSize());

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
        });

        // Добавляем все пункты в меню (подменю темы)
        menu.getItems().addAll(
                flipBoardItem,
                coordinatesItem,
                separator,
                boardThemeMenu,  // <-- ПОДМЕНЮ С ТЕМАМИ
                zoomMenu,
                toggleNotationItem
        );

        return menu;
    }

    /**
     * Создает подменю "Тема доски"
     */
    private Menu createBoardThemeMenu() {
        // Создаем подменю
        Menu themeMenu = new Menu(lang.get(MENU_BOARD_THEME));

        // Группа для RadioMenuItem (чтобы можно было выбрать только одну тему)
        themeToggleGroup = new ToggleGroup();

        // Получаем все темы из BoardTheme
        BoardTheme.Theme[] themes = BoardTheme.THEMES;

        // Создаем пункты для каждой темы
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

    /**
     * Создает RadioMenuItem для конкретной темы
     */
    private RadioMenuItem createThemeMenuItem(BoardTheme.Theme theme, int index) {
        RadioMenuItem item = new RadioMenuItem(theme.name());
        item.setToggleGroup(themeToggleGroup);
        item.setUserData(index);

        item.setOnAction(e -> {
            // Применяем тему к доске
            ChessBoardView boardView = controller.getBoardView();
            if (boardView != null) {
                boardView.setBoardTheme(theme);
                // Сохраняем выбор в настройках
                AppPreferences.saveBoardTheme(index);
            }
        });

        return item;
    }

    /**
     * Загружает сохраненную тему и устанавливает соответствующий RadioMenuItem
     */
    private void loadSavedTheme() {
        int savedThemeIndex = AppPreferences.getBoardThemeIndex();

        // Проверяем, что индекс в пределах массива
        if (savedThemeIndex < 0 || savedThemeIndex >= BoardTheme.THEMES.length) {
            savedThemeIndex = 0;
        }

        // Находим соответствующий RadioMenuItem по индексу
        RadioMenuItem selectedItem;
        switch (savedThemeIndex) {
            case 1 -> selectedItem = classicThemeItem;
            case 2 -> selectedItem = greenThemeItem;
            case 3 -> selectedItem = blueThemeItem;
            default -> selectedItem = woodThemeItem;
        }

        // Устанавливаем выбранный пункт
        if (selectedItem != null) {
            selectedItem.setSelected(true);

            // Применяем тему к доске (если доска уже создана)
            ChessBoardView boardView = controller.getBoardView();
            if (boardView != null && savedThemeIndex < BoardTheme.THEMES.length) {
                boardView.setBoardTheme(BoardTheme.THEMES[savedThemeIndex]);
            }
        }
    }

    private Menu createEngineMenu() {
        Menu menu = new Menu(lang.get(MENU_ENGINE));

        MenuItem engineConfigureItem = new MenuItem(lang.get(MENU_ENGINE_CONFIGURE));
        engineConfigureItem.setOnAction(e -> controller.configureEngine());

        MenuItem engineAnalyzeItem = new MenuItem(lang.get(MENU_ENGINE_ANALYZE));
        engineAnalyzeItem.setAccelerator(KeyCombination.keyCombination("Ctrl+A"));
        engineAnalyzeItem.setOnAction(e -> controller.showBestMove());

        menu.getItems().addAll(engineConfigureItem, engineAnalyzeItem);
        return menu;
    }

    private Menu createHelpMenu() {
        Menu menu = new Menu(lang.get(MENU_HELP));

        MenuItem shortcutsItem = new MenuItem(lang.get(MENU_HELP_SHORTCUTS));
        shortcutsItem.setAccelerator(KeyCombination.keyCombination("Ctrl+H"));
        shortcutsItem.setOnAction(e -> controller.showShortcuts());

        MenuItem aboutItem = new MenuItem(lang.get(MENU_HELP_ABOUT));
        aboutItem.setOnAction(e -> controller.showAboutDialog());

        MenuItem githubItem = new MenuItem(lang.get(MENU_HELP_GITHUB));
        githubItem.setOnAction(e -> openGitHubPage());

        MenuItem checkUpdatesItem = new MenuItem(lang.get(MENU_HELP_CHECK_UPDATES));
        checkUpdatesItem.setOnAction(e -> checkForUpdates());

        MenuItem donateItem = new MenuItem(lang.get(MENU_HELP_DONATE));
        donateItem.setOnAction(e -> {
            DonateDialog dialog = new DonateDialog(primaryStage);
            dialog.showAndWait();
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

    private Menu createWindowsMenu() {
        Menu menu = new Menu(lang.get(MENU_WINDOWS));

        windowsClipboardStatusItem = new MenuItem(lang.get(MENU_WINDOWS_CLIPBOARD_EMPTY));
        windowsClipboardStatusItem.setDisable(true);

        windowsClearClipboardItem = new MenuItem(lang.get(MENU_WINDOWS_CLEAR_CLIPBOARD));
        windowsClearClipboardItem.setOnAction(e -> {
            PgnBrowserManager.getInstance().clearClipboard();
            updateWindowsMenu();
        });
        windowsClearClipboardItem.setDisable(true);

        SeparatorMenuItem sep = new SeparatorMenuItem();

        windowsCloseAllItem = new MenuItem(lang.get(MENU_WINDOWS_CLOSE_ALL));
        windowsCloseAllItem.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        windowsCloseAllItem.setOnAction(e -> {
            PgnBrowserManager.getInstance().closeAllBrowsers();
            updateWindowsMenu();
        });
        windowsCloseAllItem.setDisable(true);

        menu.getItems().addAll(
                windowsClipboardStatusItem,
                sep,
                windowsClearClipboardItem,
                new SeparatorMenuItem(),
                windowsCloseAllItem
        );

        updateWindowsMenu();

        return menu;
    }

    // ========== ОБНОВЛЕНИЕ МЕНЮ "ОКНА" ==========
    private void updateWindowsMenu() {
        if (windowsMenu == null) return;

        Platform.runLater(() -> {
            try {
                Collection<PgnFileBrowser> browsers = PgnBrowserManager.getInstance().getAllBrowsers();
                PgnFileBrowser activeBrowser = PgnBrowserManager.getInstance().getActiveBrowser();

                // Удаляем старые пункты с браузерами (сохраняем только первые 3 служебных)
                int itemsToKeep = 4;
                while (windowsMenu.getItems().size() > itemsToKeep) {
                    windowsMenu.getItems().remove(0);
                }

                int insertIndex = 0;

                if (browsers.isEmpty()) {
                    MenuItem noBrowsersItem = new MenuItem(lang.get(MENU_WINDOWS_NO_FILES));
                    noBrowsersItem.setDisable(true);
                    windowsMenu.getItems().add(insertIndex, noBrowsersItem);

                    windowsCloseAllItem.setDisable(true);
                } else {
                    windowsCloseAllItem.setDisable(false);

                    for (PgnFileBrowser browser : browsers) {
                        Path path = browser.getPgnPath();
                        String fileName = path.getFileName().toString();
                        int gameCount = browser.getCurrentIndex() != null ?
                                browser.getCurrentIndex().getActiveCount() : 0;

                        String displayName = String.format(lang.get(MENU_WINDOWS_BROWSER_ITEM),
                                fileName, gameCount);

                        boolean isActive = browser == activeBrowser;
                        if (isActive) {
                            displayName = "✅ " + displayName;
                        }

                        MenuItem browserItem = new MenuItem(displayName);
                        browserItem.setId("browser_" + path.toString().hashCode());

                        browserItem.setOnAction(e -> {
                            browser.showWindow();
                            PgnBrowserManager.getInstance().setActiveBrowser(browser);
                            updateWindowsMenu();
                        });

                        windowsMenu.getItems().add(insertIndex, browserItem);
                        insertIndex++;
                    }
                }

                boolean hasClipboard = PgnBrowserManager.getInstance().hasClipboardContent();
                PgnBrowserManager.ClipboardContent content = PgnBrowserManager.getInstance().getClipboardContent();

                if (hasClipboard && content != null) {
                    String sourceName = content.sourceFile().getFileName().toString();
                    windowsClipboardStatusItem.setText(String.format(lang.get(MENU_WINDOWS_CLIPBOARD_CONTENT),
                            content.count(), sourceName));
                    windowsClipboardStatusItem.setDisable(false);
                    windowsClearClipboardItem.setDisable(false);
                } else {
                    windowsClipboardStatusItem.setText(lang.get(MENU_WINDOWS_CLIPBOARD_EMPTY));
                    windowsClipboardStatusItem.setDisable(true);
                    windowsClearClipboardItem.setDisable(true);
                }

            } catch (Exception e) {
                log.trace("Error updating windows menu: {}", e.getMessage());
            }
        });
    }

    /**
     * Показывает диалог настроек
     */
    private void showPreferencesDialog() {
        PreferencesDialog dialog = new PreferencesDialog(primaryStage);
        dialog.showAndWait();

        // После закрытия диалога - обновляем UI

        // Обновляем размер доски
        int newSize = AppPreferences.getTileSize();
        if (sizeController != null) {
            sizeController.setTileSize(newSize);
        }

        // Обновляем координаты
        boolean showCoords = AppPreferences.isShowCoordinates();
        if (controller != null) {
            controller.toggleCoordinates(showCoords);
            // Обновляем чекбокс в меню
            updateCoordinatesCheckbox(showCoords);
        }

        // Обновляем переворот доски
        boolean flipped = AppPreferences.isBoardFlipped();
        if (controller != null) {
            ChessBoardView boardView = controller.getBoardView();
            if (boardView != null) {
                // Проверяем текущее состояние переворота
                if (boardView.isBoardFlipped() != flipped) {
                    controller.flipBoard();
                }
            }
        }

        // Обновляем тему доски
        int themeIndex = AppPreferences.getBoardThemeIndex();
        if (controller != null) {
            ChessBoardView boardView = controller.getBoardView();
            if (boardView != null && themeIndex >= 0 && themeIndex < BoardTheme.THEMES.length) {
                boardView.setBoardTheme(BoardTheme.THEMES[themeIndex]);
            }
            // Обновляем радиокнопки в меню темы
            updateThemeMenuCheckmarks(themeIndex);
        }

        // Обновляем язык в меню
        updateLanguageMenuCheckmarks();
    }

    /**
     * Обновляет состояние радиокнопок темы доски в меню
     */
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

    /**
     * Метод для обновления состояния чекбокса координат
     */
    public void updateCoordinatesCheckbox(boolean selected) {
        if (coordinatesItem != null) {
            coordinatesItem.setSelected(selected);
        }
    }

    /**
     * Открывает страницу проекта на GitHub
     */
    private void openGitHubPage() {
        try {
            String url = "https://github.com/AndreyKhrypach/Kletka";
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (Exception e) {
            showError(lang.get(MENU_HELP_GITHUB_ERROR));
        }
    }

    /**
     * Проверяет наличие обновлений на GitHub
     */
    private void checkForUpdates() {
        // Показываем индикатор загрузки
        Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
        loadingAlert.setTitle(lang.get(MENU_HELP_CHECKING_UPDATES));
        loadingAlert.setHeaderText(null);
        loadingAlert.setContentText(lang.get(MENU_HELP_CHECKING_UPDATES_MSG));
        loadingAlert.show();

        new Thread(() -> {
            try {
                // Получаем последний релиз с GitHub API
                String apiUrl = "https://api.github.com/repos/AndreyKhrypach/Kletka/releases/latest";
                java.net.URL url = new java.net.URL(apiUrl);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    // Читаем ответ
                    String json = readAnswer(connection);
                    String latestVersion = extractVersionFromJson(json);
                    String currentVersion = getCurrentVersion();

                    javafx.application.Platform.runLater(() -> {
                        loadingAlert.close();
                        if (isNewerVersion(latestVersion, currentVersion)) {
                            showUpdateAvailableDialog(latestVersion);
                        } else {
                            showNoUpdatesDialog();
                        }
                    });
                } else {
                    javafx.application.Platform.runLater(() -> {
                        loadingAlert.close();
                        showError(lang.get(MENU_HELP_UPDATE_CHECK_ERROR));
                    });
                }
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    loadingAlert.close();
                    showError(lang.get(MENU_HELP_UPDATE_CHECK_ERROR) + ": " + e.getMessage());
                });
            }
        }).start();
    }

    private static String readAnswer(HttpURLConnection connection) throws IOException {
        java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(connection.getInputStream())
        );
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Парсим JSON вручную (без библиотек, чтобы не добавлять зависимости)
        return response.toString();
    }

    /**
     * Извлекает версию из JSON ответа GitHub API
     */
    private String extractVersionFromJson(String json) {
        // Ищем "tag_name": "v1.1.0"
        String tagKey = "\"tag_name\":";
        int tagIndex = json.indexOf(tagKey);
        if (tagIndex < 0) return null;

        int startQuote = json.indexOf("\"", tagIndex + tagKey.length());
        if (startQuote < 0) return null;

        int endQuote = json.indexOf("\"", startQuote + 1);
        if (endQuote < 0) return null;

        String tag = json.substring(startQuote + 1, endQuote);
        // Удаляем префикс "v" если есть
        if (tag.startsWith("v")) {
            tag = tag.substring(1);
        }
        return tag;
    }

    /**
     * Возвращает текущую версию программы
     */
    private String getCurrentVersion() {
        // Читаем из pom.xml или из ресурсов
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
        return "1.1.0"; // fallback
    }

    /**
     * Сравнивает версии
     */
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

    /**
     * Показывает диалог о наличии обновления
     */
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
        });
    }

    /**
     * Показывает диалог "Нет обновлений"
     */
    private void showNoUpdatesDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(lang.get(MENU_HELP_NO_UPDATES));
        alert.setHeaderText(null);
        alert.setContentText(lang.get(MENU_HELP_NO_UPDATES_MSG));
        alert.showAndWait();
    }

    /**
     * Показывает ошибку
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(lang.get(NOTIFICATION_ERROR));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}