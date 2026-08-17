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

package Khrypach.Andrey.chess.kletka.pgn.index.ui;

import Khrypach.Andrey.chess.kletka.database.model.GameData;
import Khrypach.Andrey.chess.kletka.database.parser.PgnParser;
import Khrypach.Andrey.chess.kletka.gui.board.ChessBoardView;
import Khrypach.Andrey.chess.kletka.gui.board.ChessSymbols;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.pgn.index.PgnFileEditor;
import Khrypach.Andrey.chess.kletka.pgn.index.PgnIndexManager;
import Khrypach.Andrey.chess.kletka.pgn.index.PgnRepacker;
import Khrypach.Andrey.chess.kletka.pgn.index.manager.PgnBrowserManager;
import Khrypach.Andrey.chess.kletka.pgn.index.model.GameIndexEntry;
import Khrypach.Andrey.chess.kletka.pgn.index.model.PgnIndex;
import Khrypach.Andrey.chess.kletka.pgn.index.operation.PgnGameOperation;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.*;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

/**
 * PGN Браузер - независимый экземпляр для работы с одним PGN файлом
 */
public class PgnFileBrowser {

    private static final Logger log = LoggerFactory.getLogger(PgnFileBrowser.class);
    private static final LanguageManager lang = LanguageManager.getInstance();

    private static final double WINDOW_WIDTH_RATIO = 0.95;
    private static final double WINDOW_HEIGHT_RATIO = 0.90;
    static final double MIN_WINDOW_WIDTH = 800;
    static final double MIN_WINDOW_HEIGHT = 600;
    static final double ABSOLUTE_MAX_WIDTH = 30000;
    static final double ABSOLUTE_MAX_HEIGHT = 30000;

    // ========== УНИКАЛЬНЫЙ ИДЕНТИФИКАТОР БРАУЗЕРА ==========
    @Getter
    private final String browserId;
    @Getter
    private final Path pgnPath;

    // ========== СОСТОЯНИЕ БРАУЗЕРА ==========
    @Getter
    @Setter
    private boolean active = false;

    @Getter
    private PgnIndex currentIndex;
    private boolean useIndexMode = false;
    private int currentGameIndex = -1;

    // ========== КОМПОНЕНТЫ UI ==========
    private Stage stage;
    private final Stage ownerStage;
    private TableView<GameTableRow> tableView;
    private TableColumn<GameTableRow, Integer> idColumn;
    private TableColumn<GameTableRow, String> whiteColumn;
    private TableColumn<GameTableRow, String> blackColumn;
    private TableColumn<GameTableRow, String> resultColumn;
    private TableColumn<GameTableRow, String> yearColumn;
    private TableColumn<GameTableRow, String> eventColumn;
    private TableColumn<GameTableRow, String> ecoColumn;
    private TableColumn<GameTableRow, String> openingColumn;
    private TableColumn<GameTableRow, String> bodyColumn;

    private Label statusLabel;
    private Label totalLabel;
    private Label selectedLabel;
    private ProgressIndicator progressIndicator;
    private TextField searchField;

    // ========== КНОПКИ ==========
    private Button editButton;
    private Button deleteButton;
    private Button duplicateButton;
    private Button copyButton;
    private Button pasteButton;
    private Button repackButton;
    private HBox buttonPanel;
    private boolean menuCreated = false;
    private final Object menuLock = new Object();

    // ========== ДАННЫЕ (синхронизированные списки) ==========
    private final List<GameTableRow> allRows = Collections.synchronizedList(new ArrayList<>());
    private final List<GameData> cachedGames = Collections.synchronizedList(new ArrayList<>());

    // ========== ПАГИНАЦИЯ ==========
    private static final int PAGE_SIZE = 50;
    private int currentPage = 0;
    private boolean isLoadingMore = false;
    private boolean allLoaded = false;

    // ========== КОЛБЭКИ ==========
    private Consumer<GameData> onGameSelected;
    @Getter
    @Setter
    private Runnable onDataLoaded;

    // ========== DEPENDENCIES ==========
    @Getter
    @Setter
    private ChessBoardView boardView;
    private final PgnRepacker repacker;
    private RepackProgressDialog repackDialog;
    private boolean isRepacking = false;
    private RepackStatusWidget repackStatusWidget;

    // ========== КОНСТРУКТОР ==========
    public PgnFileBrowser(Path pgnPath, Stage ownerStage) {
        this.pgnPath = pgnPath;
        this.browserId = pgnPath.toAbsolutePath().toString();
        this.ownerStage = ownerStage;
        this.repacker = new PgnRepacker();
        log.info("Created browser for: {}", pgnPath);
    }

    // ========== ПОКАЗ БРАУЗЕРА ==========
    public void show(Consumer<GameData> onGameSelected) {
        this.onGameSelected = onGameSelected;
        createStage();
        loadGames();
        stage.show();
        stage.toFront();
        updateTitle();
    }

    // ========== СОЗДАНИЕ ОКНА ==========
    private void createStage() {
        stage = new Stage();
        stage.initModality(Modality.NONE);
        stage.initStyle(StageStyle.DECORATED);

        double windowWidth = getOptimalWindowWidth();
        double windowHeight = getOptimalWindowHeight();

        stage.setMinWidth(MIN_WINDOW_WIDTH);
        stage.setMinHeight(MIN_WINDOW_HEIGHT);

        stage.setWidth(windowWidth);
        stage.setHeight(windowHeight);

        // ========== ОБРАБОТЧИК ИЗМЕНЕНИЯ РАЗМЕРА ОКНА==========
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            double newWidth = newVal.doubleValue();
            // Проверяем только на абсолютный максимум (безопасность Gdk)
            if (newWidth > ABSOLUTE_MAX_WIDTH) {
                stage.setWidth(ABSOLUTE_MAX_WIDTH);
            } else {
                updateTableSizes();
            }
        });

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            double newHeight = newVal.doubleValue();
            if (newHeight > ABSOLUTE_MAX_HEIGHT) {
                stage.setHeight(ABSOLUTE_MAX_HEIGHT);
            } else {
                updateTableSizes();
            }
        });


        if (ownerStage != null) {
            ownerStage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, (WindowEvent event) -> {
                log.info("Owner stage closing (filter), closing browser: {}", pgnPath.getFileName());
                if (stage != null && stage.isShowing()) {
                    stage.close();
                    stage = null;
                }
            });
        }

        stage.setTitle("PGN Browser - " + pgnPath.getFileName());

        stage.setOnCloseRequest((WindowEvent event) -> {
            PgnBrowserManager.getInstance().onBrowserClosed(this);
            stage.close();
            stage = null;
            log.info("Window closed for: {}", pgnPath);
        });

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10, 25, 10, 25));

        HBox topPanel = createTopPanel();
        root.setTop(topPanel);

        tableView = createTableView();

        double tableWidth = windowWidth - 50;
        double tableHeight = windowHeight - 180;

        tableView.setPrefWidth(tableWidth);
        tableView.setPrefHeight(tableHeight);

        ScrollPane scrollPane = new ScrollPane(tableView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefWidth(tableWidth);
        scrollPane.setPrefHeight(tableHeight);

        root.setCenter(scrollPane);

        VBox bottomContainer = createBottomPanel();
        root.setBottom(bottomContainer);

        initContextMenu();
        setupEventHandlers();

        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    // ========== СОЗДАНИЕ ТАБЛИЦЫ ==========
    @SuppressWarnings("unchecked")
    private TableView<GameTableRow> createTableView() {
        TableView<GameTableRow> table = new TableView<>();
        table.setStyle("-fx-font-size: 13px;");

        double rowHeight = 30;
        table.setRowFactory(tv -> {
            TableRow<GameTableRow> row = new TableRow<>();
            row.setPrefHeight(rowHeight);
            row.setMaxHeight(rowHeight);
            row.setMinHeight(rowHeight);
            return row;
        });

        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // ========== ШИРИНА КОЛОНОК ==========
        // Используем Math.min(ширина_экрана * 0.88, 30000) — абсолютный максимум
        double totalWidth = getOptimalWindowWidth() - 60;

        idColumn = new TableColumn<>(lang.get(PGN_BROWSER_COLUMN_ID));
        idColumn.setPrefWidth(Math.min(totalWidth * 0.04, 80));
        idColumn.setMinWidth(40);
        idColumn.setMaxWidth(80);
        idColumn.setResizable(false);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        whiteColumn = new TableColumn<>(lang.get(PGN_BROWSER_COLUMN_WHITE));
        whiteColumn.setPrefWidth(Math.min(totalWidth * 0.13, 350));
        whiteColumn.setMinWidth(80);
        whiteColumn.setMaxWidth(400);
        whiteColumn.setCellValueFactory(new PropertyValueFactory<>("white"));

        blackColumn = new TableColumn<>(lang.get(PGN_BROWSER_COLUMN_BLACK));
        blackColumn.setPrefWidth(Math.min(totalWidth * 0.13, 350));
        blackColumn.setMinWidth(80);
        blackColumn.setMaxWidth(400);
        blackColumn.setCellValueFactory(new PropertyValueFactory<>("black"));

        resultColumn = new TableColumn<>(lang.get(PGN_BROWSER_COLUMN_RESULT));
        resultColumn.setPrefWidth(Math.min(totalWidth * 0.06, 100));
        resultColumn.setMinWidth(60);
        resultColumn.setMaxWidth(100);
        resultColumn.setResizable(false);
        resultColumn.setCellValueFactory(new PropertyValueFactory<>("result"));
        resultColumn.setStyle("-fx-alignment: CENTER;");

        yearColumn = new TableColumn<>(lang.get(PGN_BROWSER_COLUMN_YEAR));
        yearColumn.setPrefWidth(Math.min(totalWidth * 0.05, 80));
        yearColumn.setMinWidth(50);
        yearColumn.setMaxWidth(80);
        yearColumn.setResizable(false);
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        yearColumn.setStyle("-fx-alignment: CENTER;");

        eventColumn = new TableColumn<>(lang.get(PGN_BROWSER_COLUMN_EVENT));
        eventColumn.setPrefWidth(Math.min(totalWidth * 0.10, 250));
        eventColumn.setMinWidth(80);
        eventColumn.setMaxWidth(300);
        eventColumn.setCellValueFactory(new PropertyValueFactory<>("event"));

        ecoColumn = new TableColumn<>(lang.get(PGN_BROWSER_COLUMN_ECO));
        ecoColumn.setPrefWidth(Math.min(totalWidth * 0.05, 80));
        ecoColumn.setMinWidth(45);
        ecoColumn.setMaxWidth(80);
        ecoColumn.setResizable(false);
        ecoColumn.setCellValueFactory(new PropertyValueFactory<>("eco"));
        ecoColumn.setStyle("-fx-alignment: CENTER;");

        openingColumn = new TableColumn<>(lang.get(PGN_BROWSER_COLUMN_OPENING));
        openingColumn.setPrefWidth(Math.min(totalWidth * 0.15, 400));
        openingColumn.setMinWidth(100);
        openingColumn.setMaxWidth(500);
        openingColumn.setCellValueFactory(new PropertyValueFactory<>("opening"));

        bodyColumn = new TableColumn<>(lang.get(PGN_BROWSER_COLUMN_BODY));
        bodyColumn.setPrefWidth(Math.min(totalWidth * 0.25, 600));
        bodyColumn.setMinWidth(80);
        bodyColumn.setMaxWidth(800);

        bodyColumn.setCellFactory(col -> new TableCell<>() {
            private final TextField textField = new TextField();
            private final Tooltip tooltip = new Tooltip();

            {
                textField.setStyle(
                        "-fx-font-family: 'Consolas', monospace; " +
                                "-fx-font-size: 12px; " +
                                "-fx-text-fill: black; " +
                                "-fx-background-color: transparent; " +
                                "-fx-border-color: transparent; " +
                                "-fx-padding: 0 5 0 5;"
                );
                textField.setEditable(false);
                textField.setFocusTraversable(false);
                textField.setMaxHeight(30);
                textField.setMinHeight(30);
                textField.setPrefHeight(30);
                textField.setMaxWidth(Double.MAX_VALUE);

                tooltip.setShowDelay(javafx.util.Duration.millis(500));
                tooltip.setHideDelay(javafx.util.Duration.millis(5000));

                textField.setOnMouseEntered(event -> {
                    String text = textField.getText();
                    if (text != null && !text.isEmpty()) {
                        tooltip.setText(text);
                        tooltip.show(textField, event.getScreenX(), event.getScreenY() + 20);
                    }
                });

                textField.setOnMouseExited(event -> tooltip.hide());

                setGraphic(textField);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setPadding(new Insets(0, 0, 0, 0));
                setPrefHeight(30);
                setMinHeight(30);
                setMaxHeight(30);
                setMaxWidth(Double.MAX_VALUE);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    textField.setText("");
                    tooltip.setText("");
                    setTooltip(null);
                    return;
                }

                textField.setText(item);
                tooltip.setText(item);
            }
        });

        bodyColumn.widthProperty().addListener((obs, oldVal, newVal) -> tableView.refresh());

        bodyColumn.setCellValueFactory(new PropertyValueFactory<>("body"));

        table.getColumns().addAll(
                idColumn, whiteColumn, blackColumn, resultColumn,
                yearColumn, eventColumn, ecoColumn, openingColumn, bodyColumn
        );

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.getSelectionModel().getSelectedItems().addListener(
                (ListChangeListener<GameTableRow>) change -> updateButtonsState()
        );

        return table;
    }

    private void updateTableSizes() {
        if (stage == null || tableView == null) return;

        double windowWidth = stage.getWidth();
        double windowHeight = stage.getHeight();

        // Ограничиваем только абсолютным максимумом (безопасность Gdk)
        double tableWidth = Math.max(MIN_WINDOW_WIDTH - 50,
                Math.min(windowWidth - 50, ABSOLUTE_MAX_WIDTH - 50));
        double tableHeight = Math.max(200,
                Math.min(windowHeight - 180, ABSOLUTE_MAX_HEIGHT - 180));

        tableView.setPrefWidth(tableWidth);
        tableView.setPrefHeight(tableHeight);

        // Обновляем ширину колонок на основе текущей ширины таблицы
        updateColumnWidths(tableWidth);
    }

    private void updateColumnWidths(double tableWidth) {
        if (tableWidth <= 0 || tableView == null) return;

        double adjustedWidth = tableWidth - 10;

        idColumn.setPrefWidth(Math.min(adjustedWidth * 0.04, 80));
        whiteColumn.setPrefWidth(Math.min(adjustedWidth * 0.13, 350));
        blackColumn.setPrefWidth(Math.min(adjustedWidth * 0.13, 350));
        resultColumn.setPrefWidth(Math.min(adjustedWidth * 0.06, 100));
        yearColumn.setPrefWidth(Math.min(adjustedWidth * 0.05, 80));
        eventColumn.setPrefWidth(Math.min(adjustedWidth * 0.10, 250));
        ecoColumn.setPrefWidth(Math.min(adjustedWidth * 0.05, 80));
        openingColumn.setPrefWidth(Math.min(adjustedWidth * 0.20, 500));
        bodyColumn.setPrefWidth(Math.min(adjustedWidth * 0.25, 600));
    }

    // ========== ИНИЦИАЛИЗАЦИЯ КОНТЕКСТНОГО МЕНЮ ==========
    private void initContextMenu() {
        synchronized (menuLock) {
            if (menuCreated) {
                return;
            }

            log.debug("Creating context menu BEFORE data load");

            ContextMenu tableViewContextMenu = new ContextMenu();

            MenuItem loadItem = new MenuItem(lang.get(PGN_BROWSER_CONTEXT_LOAD));
            loadItem.setOnAction(e -> {
                GameTableRow selected = tableView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    loadGame(selected);
                }
            });

            MenuItem copyItem = new MenuItem(lang.get(PGN_BROWSER_CONTEXT_COPY));
            copyItem.setOnAction(e -> copySelectedGames());

            MenuItem deleteItem = new MenuItem(lang.get(PGN_BROWSER_CONTEXT_DELETE));
            deleteItem.setStyle("-fx-text-fill: #cc0000;");
            deleteItem.setOnAction(e -> deleteSelectedGames());

            SeparatorMenuItem sep1 = new SeparatorMenuItem();
            SeparatorMenuItem sep2 = new SeparatorMenuItem();

            MenuItem selectAllItem = new MenuItem(lang.get(PGN_BROWSER_CONTEXT_SELECT_ALL));
            selectAllItem.setOnAction(e -> tableView.getSelectionModel().selectAll());

            tableViewContextMenu.getItems().addAll(
                    loadItem,
                    copyItem,
                    sep1,
                    deleteItem,
                    sep2,
                    selectAllItem
            );

            tableView.setContextMenu(tableViewContextMenu);
            menuCreated = true;

            log.debug("Context menu created successfully");
        }
    }

    // ========== ОБНОВЛЕНИЕ СОСТОЯНИЯ КНОПОК ==========
    public void updateButtonsState() {
        int selectedCount = tableView.getSelectionModel().getSelectedItems().size();
        boolean singleSelected = selectedCount == 1;
        boolean hasSelected = selectedCount > 0;

        if (editButton != null) editButton.setDisable(!singleSelected);
        if (duplicateButton != null) duplicateButton.setDisable(!singleSelected);
        if (deleteButton != null) {
            deleteButton.setDisable(!hasSelected);
            if (hasSelected) {
                deleteButton.setText(String.format(lang.get(PGN_BROWSER_BUTTON_DELETE_COUNT), selectedCount));
            } else {
                deleteButton.setText(lang.get(PGN_BROWSER_BUTTON_DELETE));
            }
        }
        if (copyButton != null) copyButton.setDisable(!hasSelected);

        if (pasteButton != null) {
            pasteButton.setDisable(!PgnBrowserManager.getInstance().canPaste(this));
        }
    }

    // ========== ВЕРХНЯЯ ПАНЕЛЬ ==========
    private HBox createTopPanel() {
        HBox topPanel = new HBox(10);
        topPanel.setPadding(new Insets(0, 0, 10, 0));

        Label searchLabel = new Label(lang.get(PGN_BROWSER_SEARCH_LABEL));
        searchField = new TextField();
        searchField.setPromptText(lang.get(PGN_BROWSER_SEARCH_PROMPT));
        searchField.setPrefWidth(350);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterGames(newVal));

        Button clearButton = new Button(lang.get(PGN_BROWSER_SEARCH_CLEAR));
        clearButton.setOnAction(e -> searchField.clear());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        statusLabel = new Label(lang.get(PGN_BROWSER_STATUS_LOADING));
        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(20, 20);
        progressIndicator.setVisible(false);

        topPanel.getChildren().addAll(
                searchLabel, searchField, clearButton,
                spacer, statusLabel, progressIndicator
        );

        return topPanel;
    }

    // ========== НИЖНЯЯ ПАНЕЛЬ ==========
    private VBox createBottomPanel() {
        VBox bottomContainer = new VBox(5);
        bottomContainer.setPadding(new Insets(10, 0, 0, 0));

        // ========== ПАНЕЛЬ УПРАВЛЕНИЯ ОКНОМ ==========
        HBox windowControlPanel = new HBox(8);
        windowControlPanel.setPadding(new Insets(5, 10, 5, 10));
        windowControlPanel.setAlignment(Pos.CENTER_RIGHT);
        windowControlPanel.setStyle("-fx-background-color: #e8e0d8; -fx-background-radius: 4;");

        // Кнопка Свернуть
        Button minimizeButton = new Button("—");
        minimizeButton.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 2 10 2 10;");
        minimizeButton.setTooltip(new Tooltip(lang.get(SHORTCUT_MINIMIZE_BROWSER)));
        minimizeButton.setOnAction(e -> { if (stage != null) stage.setIconified(true); });

        // Кнопка Развернуть
        Button maximizeButton = new Button("⛶");
        maximizeButton.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 2 10 2 10;");
        maximizeButton.setTooltip(new Tooltip(lang.get(SHORTCUT_MAXIMIZE_BROWSER)));
        maximizeButton.setOnAction(e -> {
            if (stage != null) {
                stage.setMaximized(!stage.isMaximized());
                // текст кнопки меняется автоматически через listener
            }
        });

        // Обновление состояния кнопки
        if (stage != null) {
            stage.maximizedProperty().addListener((obs, oldVal, newVal) ->
                    maximizeButton.setTooltip(new Tooltip(lang.get(SHORTCUT_MAXIMIZE_BROWSER))));
        }

        // Кнопка Закрыть (красная)
        Button closeWindowButton = new Button("✕");
        closeWindowButton.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 2 10 2 10; -fx-text-fill: #cc0000;");
        closeWindowButton.setTooltip(new Tooltip(lang.get(SHORTCUT_CLOSE_BROWSER)));
        closeWindowButton.setOnAction(e -> PgnBrowserManager.getInstance().closeBrowser(this));

        windowControlPanel.getChildren().addAll(minimizeButton, maximizeButton, closeWindowButton);

        // ========== ПАНЕЛЬ КНОПОК ДЕЙСТВИЙ ==========
        buttonPanel = createButtonPanel();
        buttonPanel.setPadding(new Insets(0, 0, 5, 0));

        // ========== СТАТУС-ПАНЕЛЬ ==========
        HBox statusPanel = new HBox(15);
        statusPanel.setPadding(new Insets(10, 0, 0, 0));

        totalLabel = new Label(String.format(lang.get(PGN_BROWSER_STATUS_TOTAL), 0));
        selectedLabel = new Label(String.format(lang.get(PGN_BROWSER_STATUS_SELECTED), 0));

        Label pageInfo = new Label(String.format(lang.get(PGN_BROWSER_STATUS_SHOWN), 0, 0));
        pageInfo.setId("pageInfo");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Кнопка закрытия (старая, можно оставить или убрать)
        Button closeButton = new Button(lang.get(PGN_BROWSER_STATUS_CLOSE));
        closeButton.setOnAction(e -> PgnBrowserManager.getInstance().closeBrowser(this));

        statusPanel.getChildren().addAll(
                totalLabel,
                selectedLabel,
                spacer,
                pageInfo,
                closeButton
        );

        bottomContainer.getChildren().addAll(windowControlPanel, buttonPanel, statusPanel);
        return bottomContainer;
    }

    // ========== ПАНЕЛЬ КНОПОК ==========
    private HBox createButtonPanel() {
        HBox box = new HBox(10);
        box.setPadding(new Insets(5, 0, 5, 0));
        box.setAlignment(Pos.CENTER_LEFT);

        editButton = new Button(lang.get(PGN_BROWSER_BUTTON_EDIT));
        editButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        editButton.setOnAction(e -> editSelectedGame());
        editButton.setDisable(true);

        deleteButton = new Button(lang.get(PGN_BROWSER_BUTTON_DELETE));
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> deleteSelectedGames());
        deleteButton.setDisable(true);

        duplicateButton = new Button(lang.get(PGN_BROWSER_BUTTON_DUPLICATE));
        duplicateButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        duplicateButton.setOnAction(e -> duplicateSelectedGame());
        duplicateButton.setDisable(true);

        copyButton = new Button(lang.get(PGN_BROWSER_BUTTON_COPY));
        copyButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold;");
        copyButton.setOnAction(e -> copySelectedGames());
        copyButton.setDisable(true);

        pasteButton = new Button(lang.get(PGN_BROWSER_BUTTON_PASTE));
        pasteButton.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; -fx-font-weight: bold;");
        pasteButton.setOnAction(e -> pasteGames());
        pasteButton.setDisable(true);

        repackButton = new Button(lang.get(PGN_BROWSER_BUTTON_REPACK));
        repackButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold;");
        repackButton.setOnAction(e -> manualRepack());
        repackButton.setDisable(true);

        repackStatusWidget = new RepackStatusWidget();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        box.getChildren().addAll(
                editButton,
                deleteButton,
                duplicateButton,
                copyButton,
                pasteButton,
                repackButton,
                spacer,
                repackStatusWidget
        );

        return box;
    }

    public void updateStatus() {
        updateTitle();
        PgnBrowserManager.getInstance().notifyBrowserListChanged();
    }

    // ========== ОБРАБОТЧИКИ СОБЫТИЙ ==========
    private void setupEventHandlers() {
        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                int selectedCount = tableView.getSelectionModel().getSelectedItems().size();
                if (selectedCount == 1) {
                    GameTableRow selected = tableView.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        loadGame(selected);
                    }
                }
            }
        });

        tableView.setOnKeyPressed(event -> {
            if (isRepacking) {
                event.consume();
                return;
            }

            if (event.isControlDown() && event.getCode() == KeyCode.A) {
                tableView.getSelectionModel().selectAll();
                event.consume();
                return;
            }

            switch (event.getCode()) {
                case DELETE -> {
                    deleteSelectedGames();
                    event.consume();
                }
                case ENTER -> {
                    if (event.isControlDown()) {
                        editSelectedGame();
                        event.consume();
                    } else {
                        int selectedCount = tableView.getSelectionModel().getSelectedItems().size();
                        if (selectedCount == 1) {
                            GameTableRow selected = tableView.getSelectionModel().getSelectedItem();
                            if (selected != null) {
                                loadGame(selected);
                                event.consume();
                            }
                        }
                    }
                }
                case D -> {
                    if (event.isControlDown()) {
                        duplicateSelectedGame();
                        event.consume();
                    }
                }
                default -> {
                }
            }
        });
    }

    // ========== ЗАГРУЗКА ДАННЫХ ==========
    private void loadGames() {
        progressIndicator.setVisible(true);
        statusLabel.setText(lang.get(PGN_BROWSER_STATUS_CHECKING_INDEX));
        currentPage = 0;
        allLoaded = false;

        Platform.runLater(() -> {
            tableView.getItems().clear();
            allRows.clear();
        });

        new Thread(() -> {
            try {
                PgnIndexManager indexManager = new PgnIndexManager();
                Path indexPath = indexManager.getIndexPath(pgnPath);

                if (Files.exists(indexPath)) {
                    log.debug("Index found, using index mode");
                    useIndexMode = true;
                    loadFromIndex(indexManager);
                } else {
                    log.debug("No index found, using direct parse mode");
                    useIndexMode = false;
                    loadByParsing();
                }
            } catch (Exception e) {
                log.error("Failed to load games", e);
                Platform.runLater(() -> {
                    statusLabel.setText(String.format(lang.get(PGN_BROWSER_STATUS_ERROR), e.getMessage()));
                    progressIndicator.setVisible(false);
                });
            }
        }).start();
    }

    private void loadFromIndex(PgnIndexManager indexManager) throws IOException {
        long startTime = System.currentTimeMillis();

        Platform.runLater(() -> statusLabel.setText(lang.get(PGN_BROWSER_STATUS_LOADING_INDEX)));

        currentIndex = indexManager.loadIndex(pgnPath);

        List<GameIndexEntry> entries = currentIndex.getActiveEntries();

        Platform.runLater(() -> {
            allRows.clear();
            int id = 0;
            for (GameIndexEntry entry : entries) {
                allRows.add(new GameTableRow(
                        ++id,
                        entry.getWhite().isEmpty() ? "?" : entry.getWhite(),
                        entry.getBlack().isEmpty() ? "?" : entry.getBlack(),
                        entry.getResult(),
                        entry.getYear().isEmpty() ? "????" : entry.getYear(),
                        entry.getEvent().isEmpty() ? "?" : entry.getEvent(),
                        entry.getEco().isEmpty() ? "" : entry.getEco(),
                        entry.getOpening().isEmpty() ? "" : entry.getOpening(),
                        "",
                        entry
                ));
            }

            totalLabel.setText(String.format(lang.get(PGN_BROWSER_STATUS_TOTAL), allRows.size()));
            selectedLabel.setText(String.format(lang.get(PGN_BROWSER_STATUS_SELECTED), 0));

            loadMoreRowsInternal();

            progressIndicator.setVisible(false);
            statusLabel.setText(lang.get(PGN_BROWSER_STATUS_READY));
            setupPagination();
            updateRepackStatus();
            checkAutoRepack();
            updateTitle();

            updateButtonsState();
            updateStatus();

            log.info("Index load completed in {} ms",
                    System.currentTimeMillis() - startTime);

            if (onDataLoaded != null) {
                onDataLoaded.run();
            }
        });
    }

    private void loadByParsing() throws Exception {
        long startTime = System.currentTimeMillis();

        Platform.runLater(() -> statusLabel.setText(lang.get(PGN_BROWSER_STATUS_PARSING)));

        String pgnContent = Files.readString(pgnPath);
        PgnParser parser = new PgnParser();

        List<GameData> parsedGames = parser.parseMultiple(pgnContent);

        cachedGames.clear();
        cachedGames.addAll(parsedGames);

        Platform.runLater(() -> {
            allRows.clear();
            int id = 0;
            for (GameData game : cachedGames) {
                String bodyPgn = extractBody(game.pgn());
                String unicodeBody = ChessSymbols.convertToChessSymbols(bodyPgn);
                if (unicodeBody.length() > 150) {
                    unicodeBody = unicodeBody.substring(0, 150) + "...";
                }

                allRows.add(new GameTableRow(
                        ++id,
                        game.whitePlayer(),
                        game.blackPlayer(),
                        game.result(),
                        game.date() != null ? String.valueOf(game.date().getYear()) : "????",
                        game.event(),
                        game.eco(),
                        game.opening(),
                        unicodeBody,
                        null
                ));
            }

            tableView.getItems().addAll(allRows);
            currentPage = 1;
            allLoaded = true;
            totalLabel.setText(String.format(lang.get(PGN_BROWSER_STATUS_TOTAL), allRows.size()));
            selectedLabel.setText(String.format(lang.get(PGN_BROWSER_STATUS_SELECTED), 0));
            progressIndicator.setVisible(false);
            statusLabel.setText(String.format(lang.get(PGN_BROWSER_STATUS_READY_WITH_COUNT), allRows.size()));
            updateTitle();

            log.info("Parsing completed in {} ms",
                    System.currentTimeMillis() - startTime);

            if (onDataLoaded != null) {
                onDataLoaded.run();
            }
        });
    }

    public void loadFirstGame() {
        if (!allRows.isEmpty()) {
            GameTableRow firstRow = allRows.get(0);
            if (firstRow != null) {
                log.debug("Loading first game: {} vs {}",
                        firstRow.getWhite(), firstRow.getBlack());

                int index = tableView.getItems().indexOf(firstRow);
                if (index >= 0) {
                    currentGameIndex = index;
                    tableView.getSelectionModel().select(index);
                    tableView.scrollTo(index);
                    loadGame(index);
                } else {
                    currentGameIndex = 0;
                    loadGame(firstRow);
                }
            }
        } else {
            log.warn("No games to load (allRows is empty)");
        }
    }

    // ========== ПАГИНАЦИЯ ==========
    private void setupPagination() {
        ScrollBar verticalScrollBar = findVerticalScrollBar(tableView);
        if (verticalScrollBar != null) {
            verticalScrollBar.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (isLoadingMore || allLoaded) return;
                if (newVal.doubleValue() > 0.95) {
                    loadMoreRows();
                }
            });
        }
    }

    private ScrollBar findVerticalScrollBar(TableView<?> tableView) {
        for (Node node : tableView.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar bar) {
                if (bar.getOrientation() == Orientation.VERTICAL) {
                    return bar;
                }
            }
        }
        return null;
    }

    private void loadMoreRows() {
        if (isLoadingMore || allLoaded) return;
        if (allRows.isEmpty()) return;

        isLoadingMore = true;
        statusLabel.setText(lang.get(PGN_BROWSER_STATUS_LOADING_MORE));

        Platform.runLater(this::loadMoreRowsInternal);
    }

    private void loadMoreRowsInternal() {
        int startIndex = currentPage * PAGE_SIZE;
        int totalSize = allRows.size();

        if (startIndex >= totalSize) {
            allLoaded = true;
            isLoadingMore = false;
            statusLabel.setText(lang.get(PGN_BROWSER_STATUS_ALL_LOADED));
            return;
        }

        int endIndex = Math.min(startIndex + PAGE_SIZE, totalSize);
        List<GameTableRow> rowsToLoad = new ArrayList<>(allRows.subList(startIndex, endIndex));

        new Thread(() -> {
            try {
                PgnFileEditor editor = null;
                if (useIndexMode && currentIndex != null) {
                    editor = new PgnFileEditor(pgnPath, currentIndex);
                }

                for (GameTableRow row : rowsToLoad) {
                    try {
                        String unicodeBody = "";
                        if (useIndexMode && row.getIndexEntry() != null) {
                            GameIndexEntry entry = row.getIndexEntry();
                            assert editor != null;
                            String bodyPgn = editor.readBody(entry);
                            unicodeBody = ChessSymbols.convertToChessSymbols(bodyPgn);
                            if (unicodeBody.length() > 150) {
                                unicodeBody = unicodeBody.substring(0, 150) + "...";
                            }
                        } else if (!cachedGames.isEmpty()) {
                            int idx = row.getId() - 1;
                            if (idx >= 0 && idx < cachedGames.size()) {
                                GameData game = cachedGames.get(idx);
                                String bodyPgn = extractBody(game.pgn());
                                unicodeBody = ChessSymbols.convertToChessSymbols(bodyPgn);
                                if (unicodeBody.length() > 150) {
                                    unicodeBody = unicodeBody.substring(0, 150) + "...";
                                }
                            }
                        }
                        row.setBody(unicodeBody);
                    } catch (Exception e) {
                        log.warn("Failed to load body for row {}: {}", row.getId(), e.getMessage());
                    }
                }

                Platform.runLater(() -> {
                    tableView.getItems().addAll(rowsToLoad);
                    currentPage++;
                    isLoadingMore = false;
                    int loaded = tableView.getItems().size();
                    statusLabel.setText(String.format(lang.get(PGN_BROWSER_STATUS_LOADED), loaded, allRows.size()));
                    if (loaded >= allRows.size()) {
                        allLoaded = true;
                        statusLabel.setText(String.format(lang.get(PGN_BROWSER_STATUS_ALL_LOADED_WITH_COUNT), allRows.size()));
                    }
                });

            } catch (Exception e) {
                log.error("Failed to load more rows", e);
                Platform.runLater(() -> {
                    isLoadingMore = false;
                    statusLabel.setText(String.format(lang.get(PGN_BROWSER_STATUS_ERROR_LOADING), e.getMessage()));
                });
            }
        }).start();
    }

    // ========== ФИЛЬТРАЦИЯ ==========
    private void filterGames(String searchText) {
        // ========== ЕСЛИ ПОЛЕ ПУСТОЕ - ПОКАЗЫВАЕМ ВСЕ СТРОКИ ==========
        if (searchText == null || searchText.trim().isEmpty()) {
            Platform.runLater(() -> {
                // Показываем все строки из allRows
                tableView.getItems().setAll(allRows);
                totalLabel.setText(String.format(lang.get(PGN_BROWSER_FILTER_TOTAL), allRows.size()));
            });
            return;
        }

        // ========== ФИЛЬТРАЦИЯ ==========
        String lowerSearch = searchText.toLowerCase().trim();
        List<GameTableRow> filtered = new ArrayList<>();

        for (GameTableRow row : allRows) {
            if (row.getWhite().toLowerCase().contains(lowerSearch) ||
                    row.getBlack().toLowerCase().contains(lowerSearch) ||
                    row.getOpening().toLowerCase().contains(lowerSearch) ||
                    row.getEco().toLowerCase().contains(lowerSearch) ||
                    row.getEvent().toLowerCase().contains(lowerSearch) ||
                    row.getResult().toLowerCase().contains(lowerSearch) ||
                    row.getBody().toLowerCase().contains(lowerSearch)) {
                filtered.add(row);
            }
        }

        Platform.runLater(() -> {
            tableView.getItems().setAll(filtered);
            totalLabel.setText(String.format(lang.get(PGN_BROWSER_FILTER_FOUND), filtered.size(), allRows.size()));
        });
    }

    // ========== ЗАГРУЗКА ПАРТИИ ПО ИНДЕКСУ ==========
    private void loadGame(int index) {
        if (index < 0 || index >= tableView.getItems().size()) {
            log.warn("Invalid game index: {}", index);
            return;
        }

        GameTableRow row = tableView.getItems().get(index);
        if (row != null) {
            currentGameIndex = index;
            loadGame(row);
        }
    }

    // ========== ЗАГРУЗКА ПАРТИИ ПО СТРОКЕ ==========
    private void loadGame(GameTableRow row) {
        if (row == null) return;

        int index = tableView.getItems().indexOf(row);
        if (index >= 0) {
            currentGameIndex = index;
            tableView.getSelectionModel().select(index);
            tableView.scrollTo(index);
        }

        progressIndicator.setVisible(true);
        statusLabel.setText(lang.get(PGN_BROWSER_STATUS_LOADING_GAME));

        new Thread(() -> {
            try {
                GameData gameData;
                if (useIndexMode && row.getIndexEntry() != null) {
                    PgnFileEditor editor = new PgnFileEditor(pgnPath, currentIndex);
                    String pgnContent = editor.readGame(row.getIndexEntry());
                    PgnParser parser = new PgnParser();
                    gameData = parser.parse(pgnContent);
                } else if (!cachedGames.isEmpty()) {
                    int idx = row.getId() - 1;
                    if (idx >= 0 && idx < cachedGames.size()) {
                        gameData = cachedGames.get(idx);
                    } else {
                        throw new IOException("Game not found in cache");
                    }
                } else {
                    throw new IOException("No data source available");
                }

                GameData finalGameData = gameData;

                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    statusLabel.setText(lang.get(PGN_BROWSER_STATUS_READY));

                    if (stage != null) {
                        stage.setIconified(true);
                    }

                    PgnBrowserManager.getInstance().setActiveBrowser(this);
                    updateStatus();

                    if (onGameSelected != null) {
                        onGameSelected.accept(finalGameData);
                    }
                });
            } catch (Exception e) {
                log.error("Failed to load game", e);
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    statusLabel.setText(String.format(lang.get(PGN_BROWSER_STATUS_ERROR), e.getMessage()));
                });
            }
        }).start();
    }

    // ========== НАВИГАЦИЯ ==========
    public void loadNextGame() {
        int total = tableView.getItems().size();
        if (total == 0) {
            log.debug("No next game to load");
            return;
        }

        if (currentGameIndex < 0 || currentGameIndex >= total) {
            currentGameIndex = 0;
        } else {
            currentGameIndex = (currentGameIndex + 1) % total;
        }

        log.debug("Loading next game: index={}, total={}", currentGameIndex, total);

        tableView.getSelectionModel().select(currentGameIndex);
        tableView.scrollTo(currentGameIndex);

        loadGame(currentGameIndex);
    }

    public void loadPreviousGame() {
        int total = tableView.getItems().size();
        if (total == 0) {
            log.debug("No previous game to load");
            return;
        }

        if (currentGameIndex < 0 || currentGameIndex >= total) {
            currentGameIndex = 0;
        } else {
            currentGameIndex = (currentGameIndex - 1 + total) % total;
        }

        log.debug("Loading previous game: index={}, total={}", currentGameIndex, total);

        tableView.getSelectionModel().select(currentGameIndex);
        tableView.scrollTo(currentGameIndex);

        loadGame(currentGameIndex);
    }

    public boolean hasGames() {
        return tableView != null && !tableView.getItems().isEmpty();
    }

    // ========== РЕДАКТИРОВАНИЕ ==========
    private void editSelectedGame() {
        if (isRepacking) {
            showNotification(lang.get(PGN_BROWSER_MSG_REPACK_IN_PROGRESS));
            return;
        }

        int selectedCount = tableView.getSelectionModel().getSelectedItems().size();
        if (selectedCount != 1) {
            showNotification(lang.get(PGN_BROWSER_MSG_SELECT_ONE));
            return;
        }

        GameTableRow selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getIndexEntry() == null) {
            showNotification(lang.get(PGN_BROWSER_MSG_EDIT_UNAVAILABLE));
            return;
        }

        // ========== ОТКРЫВАЕМ ДИАЛОГ РЕДАКТИРОВАНИЯ ЗАГОЛОВКОВ ==========
        progressIndicator.setVisible(true);
        statusLabel.setText(lang.get(PGN_BROWSER_EDIT_LOADING));

        new Thread(() -> {
            try {
                // 1. Загружаем полное содержимое партии
                PgnFileEditor editor = new PgnFileEditor(pgnPath, currentIndex);
                String fullPgn = editor.readGame(selected.getIndexEntry());

                // 2. Парсим в GameData
                PgnParser parser = new PgnParser();
                GameData gameData = parser.parse(fullPgn);

                // 3. Извлекаем тело партии (без заголовков)
                String body = extractBody(fullPgn);

                // 4. Получаем ID из записи индекса
                int gameId = selected.getIndexEntry().getId();

                // 5. Показываем диалог редактирования в UI потоке
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);

                    EditGameHeadersDialog dialog = new EditGameHeadersDialog(
                            stage,
                            gameData,
                            body,
                            gameId
                    );

                    GameData updatedGameData = dialog.showAndWait();

                    if (updatedGameData != null) {
                        // Сохраняем изменения
                        saveEditedGame(updatedGameData, selected);
                    } else {
                        statusLabel.setText(lang.get(PGN_BROWSER_STATUS_READY));
                    }
                });

            } catch (Exception e) {
                log.error("Failed to load game for editing", e);
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    statusLabel.setText(String.format(lang.get(PGN_BROWSER_STATUS_ERROR), e.getMessage()));
                    showNotification(String.format(lang.get(PGN_BROWSER_MSG_EDIT_ERROR), e.getMessage()));
                });
            }
        }).start();
    }

    /**
     * Сохраняет отредактированную партию
     */
    private void saveEditedGame(GameData updatedGameData, GameTableRow row) {
        progressIndicator.setVisible(true);
        statusLabel.setText(lang.get(PGN_BROWSER_EDIT_SAVING));

        new Thread(() -> {
            try {
                // 1. Удаляем старую версию
                PgnGameOperation operation = new PgnGameOperation(pgnPath, currentIndex);
                operation.deleteGame(row.getIndexEntry().getId());

                // 2. Добавляем новую версию с обновленными данными
                String newPgn = updatedGameData.pgn();
                operation.addGame(newPgn);

                // 3. Перезагружаем индекс
                PgnIndexManager indexManager = new PgnIndexManager();
                currentIndex = indexManager.loadIndex(pgnPath);

                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    statusLabel.setText(lang.get(PGN_BROWSER_EDIT_SAVED));
                    refreshAfterOperation();
                    showNotification(lang.get(PGN_BROWSER_MSG_EDIT_SUCCESS));
                });

            } catch (Exception e) {
                log.error("Failed to save edited game", e);
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    statusLabel.setText(String.format(lang.get(PGN_BROWSER_STATUS_ERROR), e.getMessage()));
                    showNotification(String.format(lang.get(PGN_BROWSER_MSG_EDIT_ERROR), e.getMessage()));
                });
            }
        }).start();
    }

    // ========== УДАЛЕНИЕ ==========
    private void deleteSelectedGames() {
        if (isRepacking) {
            showNotification(lang.get(PGN_BROWSER_MSG_REPACK_IN_PROGRESS));
            return;
        }

        List<GameTableRow> selected = new ArrayList<>(tableView.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) {
            showNotification(lang.get(PGN_BROWSER_MSG_SELECT_GAMES));
            return;
        }

        // Проверяем, что все партии имеют индекс
        for (GameTableRow row : selected) {
            if (row.getIndexEntry() == null) {
                showNotification(lang.get(PGN_BROWSER_MSG_DELETE_UNAVAILABLE));
                return;
            }
        }

        int total = selected.size();

        // ========== НОВЫЙ ДИАЛОГ ПОДТВЕРЖДЕНИЯ ==========
        List<GameIndexEntry> entries = new ArrayList<>();
        for (GameTableRow row : selected) {
            entries.add(row.getIndexEntry());
        }

        DeleteConfirmDialog dialog = new DeleteConfirmDialog(entries);
        if (!dialog.showAndWait()) {
            return; // Пользователь отменил
        }

        // ========== БЛОКИРУЕМ UI ==========
        setOperationsEnabled(false);
        progressIndicator.setVisible(true);
        statusLabel.setText(String.format(lang.get(PGN_BROWSER_DELETING), total));

        // ========== ПРОГРЕСС ДЛЯ БОЛЬШИХ ОПЕРАЦИЙ ==========
        if (total > 100) {
            ProgressDialog progressDialog = new ProgressDialog(
                    String.format(lang.get(PGN_BROWSER_DELETING), total),
                    lang.get(PGN_BROWSER_START_DELETING)
            );
            progressDialog.show();

            new Thread(() -> {
                try {
                    PgnGameOperation operation = new PgnGameOperation(pgnPath, currentIndex);
                    int deletedCount = 0;
                    int processed = 0;

                    for (GameTableRow row : selected) {
                        try {
                            operation.deleteGame(row.getIndexEntry().getId());
                            deletedCount++;
                            processed++;

                            if (processed % 10 == 0) {
                                double progress = (double) processed / total;
                                progressDialog.updateProgress(progress,
                                        String.format(lang.get(PGN_BROWSER_DELETING_PROCEED), processed, total),
                                        String.format(lang.get(PGN_BROWSER_DELETED), deletedCount));
                            }
                        } catch (Exception e) {
                            log.warn("Failed to delete game for big operations {}: {}", row.getId(), e.getMessage());
                        }
                    }

                    progressDialog.updateProgress(1.0, String.format(lang.get(PGN_BROWSER_DELETED), deletedCount),
                            lang.get(PGN_BROWSER_STATUS_OPERATION_FINISHED));
                    Thread.sleep(500);
                    progressDialog.close();

                    int finalDeletedCount = deletedCount;
                    Platform.runLater(() -> {
                        progressIndicator.setVisible(false);
                        statusLabel.setText(String.format(lang.get(PGN_BROWSER_DELETED), finalDeletedCount));
                        refreshAfterOperation();
                        showNotification(String.format(lang.get(PGN_BROWSER_MSG_DELETE_SUCCESS), finalDeletedCount));
                        setOperationsEnabled(true);
                    });
                } catch (Exception e) {
                    log.error("Failed to delete games", e);
                    Platform.runLater(() -> {
                        progressIndicator.setVisible(false);
                        statusLabel.setText(String.format(lang.get(PGN_BROWSER_STATUS_ERROR), e.getMessage()));
                        progressDialog.close();
                        showNotification(String.format(lang.get(PGN_BROWSER_MSG_DELETE_ERROR), e.getMessage()));
                        setOperationsEnabled(true);
                    });
                }
            }).start();

            return;
        }

        // ========== МАЛЫЕ ОПЕРАЦИИ - БЕЗ ПРОГРЕССА ==========
        new Thread(() -> {
            try {
                PgnGameOperation operation = new PgnGameOperation(pgnPath, currentIndex);
                int deletedCount = 0;

                for (GameTableRow row : selected) {
                    try {
                        operation.deleteGame(row.getIndexEntry().getId());
                        deletedCount++;
                    } catch (Exception e) {
                        log.warn("Failed to delete game for small operations{}: {}", row.getId(), e.getMessage());
                    }
                }

                int finalDeletedCount = deletedCount;
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    statusLabel.setText(String.format(lang.get(PGN_BROWSER_DELETED), finalDeletedCount));
                    refreshAfterOperation();
                    showNotification(String.format(lang.get(PGN_BROWSER_MSG_DELETE_SUCCESS), finalDeletedCount));
                    setOperationsEnabled(true);
                });
            } catch (Exception e) {
                log.error("Failed to delete games", e);
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    statusLabel.setText(String.format(lang.get(PGN_BROWSER_STATUS_ERROR), e.getMessage()));
                    showNotification(String.format(lang.get(PGN_BROWSER_MSG_DELETE_ERROR), e.getMessage()));
                    setOperationsEnabled(true);
                });
            }
        }).start();
    }

    // ========== ДУБЛИРОВАНИЕ ==========
    private void duplicateSelectedGame() {
        if (isRepacking) {
            showNotification(lang.get(PGN_BROWSER_MSG_REPACK_IN_PROGRESS));
            return;
        }

        int selectedCount = tableView.getSelectionModel().getSelectedItems().size();
        if (selectedCount != 1) {
            showNotification(lang.get(PGN_BROWSER_MSG_SELECT_ONE_DUPLICATE));
            return;
        }

        GameTableRow selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getIndexEntry() == null) {
            showNotification(lang.get(PGN_BROWSER_MSG_DUPLICATE_UNAVAILABLE));
            return;
        }

        progressIndicator.setVisible(true);
        statusLabel.setText(lang.get(PGN_BROWSER_DUPLICATING));

        new Thread(() -> {
            try {
                PgnGameOperation operation = new PgnGameOperation(pgnPath, currentIndex);
                PgnGameOperation.OperationResult result = operation.duplicateGame(
                        selected.getIndexEntry().getId()
                );

                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    statusLabel.setText(result.message());
                    refreshAfterOperation();
                    showNotification(result.message());
                });

            } catch (Exception e) {
                log.error("Failed to duplicate game", e);
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    statusLabel.setText(String.format(lang.get(PGN_BROWSER_STATUS_ERROR), e.getMessage()));
                    showNotification(String.format(lang.get(PGN_BROWSER_MSG_DUPLICATE_ERROR), e.getMessage()));
                });
            }
        }).start();
    }

    // ========== КОПИРОВАНИЕ ==========
    private void copySelectedGames() {
        if (isRepacking) {
            showNotification(lang.get(PGN_BROWSER_MSG_REPACK_IN_PROGRESS));
            return;
        }

        List<GameTableRow> selected = new ArrayList<>(tableView.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) {
            showNotification(lang.get(PGN_BROWSER_MSG_SELECT_GAMES_COPY));
            return;
        }

        if (selected.size() > 1000) {
            showNotification(String.format(lang.get(PGN_BROWSER_MSG_COPY_LIMIT), selected.size()));
            return;
        }

        for (GameTableRow row : selected) {
            if (row.getIndexEntry() == null) {
                showNotification(lang.get(PGN_BROWSER_MSG_COPY_UNAVAILABLE));
                return;
            }
        }

        List<GameIndexEntry> entries = new ArrayList<>();
        for (GameTableRow row : selected) {
            entries.add(row.getIndexEntry());
        }

        setOperationsEnabled(false);

        if (entries.size() > 100) {
            ProgressDialog progressDialog = new ProgressDialog(
                    lang.get(PGN_BROWSER_MSG_COPY),
                    lang.get(PGN_BROWSER_MSG_COPY_START)
            );
            progressDialog.show();
            progressDialog.setOnCancel(() -> {
            });

            new Thread(() -> {
                try {
                    PgnBrowserManager.getInstance().copyGamesWithProgress(this, entries, progressDialog);

                    Platform.runLater(() -> {
                        showNotification(String.format(lang.get(PGN_BROWSER_MSG_COPY_SUCCESS), entries.size(), pgnPath.getFileName()));
                        updateButtonsState();
                        setOperationsEnabled(true);
                    });
                } catch (Exception e) {
                    log.error("Failed to copy games", e);
                    Platform.runLater(() -> {
                        progressDialog.close();
                        showNotification(String.format(lang.get(PGN_BROWSER_MSG_COPY_ERROR), e.getMessage()));
                        setOperationsEnabled(true);
                    });
                }
            }).start();

            return;
        }

        PgnBrowserManager.getInstance().copyGames(this, entries);
        showNotification(String.format(lang.get(PGN_BROWSER_MSG_COPY_SUCCESS), entries.size(), pgnPath.getFileName()));

        setOperationsEnabled(true);
        updateButtonsState();
    }

    // ========== ВСТАВКА ==========
    private void pasteGames() {
        if (isRepacking) {
            showNotification(lang.get(PGN_BROWSER_MSG_REPACK_IN_PROGRESS));
            return;
        }

        PgnBrowserManager manager = PgnBrowserManager.getInstance();

        if (!manager.canPaste(this)) {
            showNotification(lang.get(PGN_BROWSER_MSG_PASTE_UNAVAILABLE));
            return;
        }

        PgnBrowserManager.ClipboardContent content = manager.getClipboardContent();
        if (content == null || content.entries().isEmpty()) {
            showNotification(lang.get(PGN_BROWSER_MSG_CLIPBOARD_EMPTY));
            return;
        }

        int total = content.entries().size();

        try {
            manager.checkDiskSpace(pgnPath, total);
        } catch (IOException e) {
            showError(lang.get(PGN_BROWSER_DISK_SPACE_ERROR), e.getMessage());
            return;
        }

        Alert confirm = createConfirm(total, content);

        ButtonType yesButton = new ButtonType(lang.get(PGN_BROWSER_BUTTON_PASTE), ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType(lang.get(PGN_BROWSER_CONFIRM_PASTE_NO), ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(yesButton, noButton);

        if (confirm.showAndWait().orElse(noButton) != yesButton) {
            return;
        }

        setOperationsEnabled(false);
        progressIndicator.setVisible(true);
        statusLabel.setText(String.format(lang.get(PGN_BROWSER_PASTING), total));

        if (total > 100) {
            ProgressDialog progressDialog = new ProgressDialog(
                    lang.get(PGN_BROWSER_MSG_PASTE_GAMES),
                    lang.get(PGN_BROWSER_MSG_PASTE_START)
            );
            progressDialog.show();
            progressDialog.setOnCancel(() -> {
            });

            new Thread(() -> {
                try {
                    int inserted = manager.pasteGamesWithProgress(this, content, progressDialog);

                    Platform.runLater(() -> {
                        progressIndicator.setVisible(false);
                        statusLabel.setText(String.format(lang.get(PGN_BROWSER_PASTED), inserted));
                        refreshAfterOperation();
                        showNotification(String.format(lang.get(PGN_BROWSER_MSG_PASTE_SUCCESS), inserted, pgnPath.getFileName()));
                        setOperationsEnabled(true);
                        updateButtonsState();
                    });
                } catch (Exception e) {
                    log.error("Failed to paste games", e);
                    Platform.runLater(() -> {
                        progressIndicator.setVisible(false);
                        statusLabel.setText(String.format(lang.get(PGN_BROWSER_MSG_PASTE_ERROR), e.getMessage()));
                        progressDialog.close();
                        showNotification(String.format(lang.get(PGN_BROWSER_MSG_PASTE_ERROR), e.getMessage()));
                        setOperationsEnabled(true);
                    });
                }
            }).start();

            return;
        }

        new Thread(() -> {
            try {
                int inserted = manager.pasteGames(this, content);

                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    statusLabel.setText(String.format(lang.get(PGN_BROWSER_PASTED), inserted));
                    refreshAfterOperation();
                    showNotification(String.format(lang.get(PGN_BROWSER_MSG_PASTE_SUCCESS), inserted, pgnPath.getFileName()));
                    setOperationsEnabled(true);
                    updateButtonsState();
                });
            } catch (Exception e) {
                log.error("Failed to paste games", e);
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    statusLabel.setText(String.format(lang.get(PGN_BROWSER_MSG_PASTE_ERROR), e.getMessage()));
                    showNotification(String.format(lang.get(PGN_BROWSER_MSG_PASTE_ERROR), e.getMessage()));
                    setOperationsEnabled(true);
                });
            }
        }).start();
    }

    private Alert createConfirm(int total, PgnBrowserManager.ClipboardContent content) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(lang.get(PGN_BROWSER_CONFIRM_PASTE_TITLE));
        confirm.setHeaderText(String.format(lang.get(PGN_BROWSER_CONFIRM_PASTE_HEADER), total));

        File targetFile = pgnPath.toFile();
        long freeSpace = targetFile.getFreeSpace();
        String freeSpaceMB = String.format("%.1f", freeSpace / (1024.0 * 1024.0));
        confirm.setContentText(
                String.format(lang.get(PGN_BROWSER_CONFIRM_PASTE_SOURCE), content.sourceFile().getFileName()) + "\n" +
                        String.format(lang.get(PGN_BROWSER_CONFIRM_PASTE_TARGET), pgnPath.getFileName()) + "\n\n" +
                        String.format(lang.get(PGN_BROWSER_CONFIRM_PASTE_COUNT), total) + "\n" +
                        String.format(lang.get(PGN_BROWSER_CONFIRM_PASTE_FREE_SPACE), freeSpaceMB) + "\n\n" +
                        lang.get(PGN_BROWSER_CONFIRM_PASTE_INFO)
        );
        return confirm;
    }

    // ========== ПЕРЕПАКОВКА ==========
    private void updateRepackStatus() {
        if (currentIndex == null || repackStatusWidget == null) return;

        PgnRepacker.RepackStatus status = repacker.getRepackStatus(currentIndex);
        repackStatusWidget.updateStatus(status);

        if (repackButton != null) {
            boolean canRepack = !isRepacking && status.hasDeleted();
            repackButton.setDisable(!canRepack);

            if (isRepacking) {
                repackButton.setText(String.format(lang.get(PGN_BROWSER_BUTTON_REPACK_COUNT), status.deletedCount()));
            } else if (status.hasDeleted()) {
                repackButton.setText(String.format(lang.get(PGN_BROWSER_BUTTON_REPACK_COUNT), status.deletedCount()));
            } else {
                repackButton.setText(lang.get(PGN_BROWSER_BUTTON_REPACK));
            }
        }
    }

    private void checkAutoRepack() {
        if (currentIndex == null || isRepacking || repacker == null) return;

        if (currentIndex.needsRepack()) {
            double ratio = currentIndex.getGrowthRatio();
            int activeCount = currentIndex.getActiveCount();
            int deletedCount = repacker.getDeletedCount(currentIndex);

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(lang.get(PGN_BROWSER_AUTO_REPACK_TITLE));
            alert.setHeaderText(String.format(lang.get(PGN_BROWSER_AUTO_REPACK_HEADER), ratio));
            alert.setContentText(String.format(
                    lang.get(PGN_BROWSER_AUTO_REPACK_CONTENT),
                    activeCount, deletedCount
            ));

            ButtonType yesButton = new ButtonType(lang.get(PGN_BROWSER_AUTO_REPACK_YES), ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType(lang.get(PGN_BROWSER_AUTO_REPACK_NO), ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(yesButton, noButton);

            alert.showAndWait().ifPresent(response -> {
                if (response == yesButton) {
                    startRepack();
                } else {
                    updateRepackStatus();
                }
            });
        } else {
            updateRepackStatus();
        }
    }

    private void manualRepack() {
        if (isRepacking || currentIndex == null) return;

        if (!repacker.hasDeletedGames(currentIndex)) {
            showNotification(lang.get(PGN_BROWSER_MSG_NO_DELETED_GAMES));
            return;
        }

        int deletedCount = repacker.getDeletedCount(currentIndex);
        int activeCount = currentIndex.getActiveCount();
        double ratio = repacker.getGrowthRatio(currentIndex);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(lang.get(PGN_BROWSER_CONFIRM_REPACK_TITLE));
        confirm.setHeaderText(lang.get(PGN_BROWSER_CONFIRM_REPACK_HEADER));
        confirm.setContentText(String.format(
                lang.get(PGN_BROWSER_CONFIRM_REPACK_CONTENT),
                deletedCount, activeCount, ratio
        ));

        ButtonType yesButton = new ButtonType(lang.get(PGN_BROWSER_CONFIRM_REPACK_YES), ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType(lang.get(PGN_BROWSER_CONFIRM_REPACK_NO), ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(yesButton, noButton);

        if (confirm.showAndWait().orElse(noButton) != yesButton) {
            return;
        }

        startRepack();
    }

    private void startRepack() {
        if (isRepacking || currentIndex == null) return;

        isRepacking = true;
        setOperationsEnabled(false);
        repackStatusWidget.showRepacking();

        repackDialog = new RepackProgressDialog();
        repackDialog.setTitle(lang.get(PGN_BROWSER_REPACK_TITLE));
        repackDialog.setAutoClose(true);
        repackDialog.setOnCancel(() -> Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(lang.get(PGN_BROWSER_REPACK_TITLE));
            alert.setHeaderText(null);
            alert.setContentText(lang.get(PGN_BROWSER_REPACK_IN_PROGRESS));
            alert.showAndWait();
        }));
        repackDialog.show();

        new Thread(() -> {
            try {
                PgnIndex newIndex = repacker.repack(
                        pgnPath,
                        currentIndex,
                        progress -> Platform.runLater(() -> {
                            if (repackDialog != null) {
                                repackDialog.updateProgress(progress);
                            }
                        })
                );

                currentIndex = newIndex;

                Platform.runLater(() -> {
                    isRepacking = false;
                    setOperationsEnabled(true);
                    refreshAfterRepack(newIndex);
                    showNotification(String.format(lang.get(PGN_BROWSER_REPACK_SUCCESS), newIndex.getActiveCount()));
                });

            } catch (Exception e) {
                log.error("Repack failed", e);
                Platform.runLater(() -> {
                    if (repackDialog != null) {
                        repackDialog.showError(
                                String.format(lang.get(PGN_BROWSER_REPACK_ERROR), e.getMessage()),e.getMessage()
                        );
                    }
                    isRepacking = false;
                    setOperationsEnabled(true);
                    updateRepackStatus();
                    showNotification(String.format(lang.get(PGN_BROWSER_REPACK_ERROR), e.getMessage()));
                });
            }
        }).start();
    }

    private void refreshAfterRepack(PgnIndex newIndex) {
        updateRepackStatus();
        refreshAfterOperation();
        totalLabel.setText(String.format(lang.get(PGN_BROWSER_STATUS_TOTAL), newIndex.getActiveCount()));
        if (repackButton != null) {
            repackButton.setDisable(false);
        }
        updateTitle();
    }

    private void refreshAfterOperation() {
        try {
            PgnIndexManager indexManager = new PgnIndexManager();
            currentIndex = indexManager.loadIndex(pgnPath);

            List<GameIndexEntry> entries = currentIndex.getActiveEntries();

            Platform.runLater(() -> {
                allRows.clear();
                int id = 0;
                for (GameIndexEntry entry : entries) {
                    allRows.add(new GameTableRow(
                            ++id,
                            entry.getWhite().isEmpty() ? "?" : entry.getWhite(),
                            entry.getBlack().isEmpty() ? "?" : entry.getBlack(),
                            entry.getResult(),
                            entry.getYear().isEmpty() ? "????" : entry.getYear(),
                            entry.getEvent().isEmpty() ? "?" : entry.getEvent(),
                            entry.getEco().isEmpty() ? "" : entry.getEco(),
                            entry.getOpening().isEmpty() ? "" : entry.getOpening(),
                            "",
                            entry
                    ));
                }

                tableView.getItems().clear();
                currentPage = 0;
                allLoaded = false;
                loadMoreRowsInternal();
                totalLabel.setText(String.format(lang.get(PGN_BROWSER_FILTER_TOTAL), allRows.size()));
                updateRepackStatus();
                checkAutoRepack();
                updateTitle();
            });

        } catch (Exception e) {
            log.error("Failed to refresh after operation", e);
        }
    }

    private void setOperationsEnabled(boolean enabled) {
        if (buttonPanel != null) {
            for (Node node : buttonPanel.getChildren()) {
                if (node instanceof Button btn) {
                    if (btn == repackButton) {
                        btn.setDisable(isRepacking);
                        if (isRepacking) {
                            btn.setText(lang.get(PGN_BROWSER_BUTTON_REPACK_IN_PROGRESS));
                        } else {
                            btn.setText(lang.get(PGN_BROWSER_BUTTON_REPACK));
                        }
                    } else {
                        btn.setDisable(!enabled);
                    }
                }
            }
        }
        tableView.setDisable(!enabled);
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========
    private String extractBody(String fullPgn) {
        if (fullPgn == null || fullPgn.isEmpty()) return "";
        int lastBracket = fullPgn.lastIndexOf(']');
        if (lastBracket < 0) return fullPgn;

        int bodyStart = -1;
        for (int i = lastBracket + 1; i < fullPgn.length() - 1; i++) {
            if (fullPgn.charAt(i) == '\n' && fullPgn.charAt(i + 1) == '\n') {
                bodyStart = i + 2;
                break;
            }
        }
        if (bodyStart < 0) {
            String after = fullPgn.substring(lastBracket + 1);
            if (!after.isEmpty() && !after.startsWith("[")) return after;
            return "";
        }
        return fullPgn.substring(bodyStart);
    }

    public void updateTitle() {
        if (stage == null) return;
        String title = String.format(lang.get(PGN_BROWSER_TITLE), pgnPath.getFileName());
        if (active) {
            title += lang.get(PGN_BROWSER_TITLE_ACTIVE);
        }
        if (currentIndex != null) {
            title += String.format(lang.get(PGN_BROWSER_TITLE_GAMES), currentIndex.getActiveCount());
        }
        stage.setTitle(title);
    }

    private void showNotification(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(lang.get(NOTIFICATION_INFO));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showWindow() {
        if (stage != null) {
            if (stage.isIconified()) stage.setIconified(false);
            stage.show();
            stage.toFront();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void closeWindow() {
        if (stage != null) {
            stage.close();
            stage = null;
        }
    }

    public boolean isShowing() {
        return stage != null && stage.isShowing();
    }

    public void refresh() {
        // Проверяем, что мы на FX потоке
        if (Platform.isFxApplicationThread()) {
            loadGames();
        } else {
            Platform.runLater(this::loadGames);
        }
    }

    // ========== ПОЛУЧЕНИЕ РАЗМЕРОВ ЭКРАНА ==========
    private Rectangle2D getScreenBounds() {
        Screen screen = Screen.getPrimary();
        if (screen == null) {
            return new Rectangle2D(0, 0, 1920, 1080);
        }
        return screen.getVisualBounds();
    }

    private double getOptimalWindowWidth() {
        double screenWidth = getScreenBounds().getWidth();
        double calculated = screenWidth * WINDOW_WIDTH_RATIO;
        // Ограничиваем только абсолютным максимумом (безопасность Gdk)
        return Math.max(MIN_WINDOW_WIDTH, Math.min(calculated, ABSOLUTE_MAX_WIDTH));
    }

    private double getOptimalWindowHeight() {
        double screenHeight = getScreenBounds().getHeight();
        double calculated = screenHeight * WINDOW_HEIGHT_RATIO;
        return Math.max(MIN_WINDOW_HEIGHT, Math.min(calculated, ABSOLUTE_MAX_HEIGHT));
    }

    // ========== ВНУТРЕННИЙ КЛАСС ==========
    @Getter
    @Setter
    public static class GameTableRow {
        private final int id;
        private final String white;
        private final String black;
        private final String result;
        private final String year;
        private final String event;
        private final String eco;
        private final String opening;
        private String body;
        private final GameIndexEntry indexEntry;

        public GameTableRow(int id, String white, String black, String result,
                            String year, String event, String eco, String opening,
                            String body, GameIndexEntry indexEntry) {
            this.id = id;
            this.white = white;
            this.black = black;
            this.result = result;
            this.year = year;
            this.event = event;
            this.eco = eco;
            this.opening = opening;
            this.body = body;
            this.indexEntry = indexEntry;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GameTableRow that = (GameTableRow) o;
            return id == that.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}