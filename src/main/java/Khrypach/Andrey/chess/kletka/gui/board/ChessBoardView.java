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

package Khrypach.Andrey.chess.kletka.gui.board;

import Khrypach.Andrey.chess.kletka.database.model.GameData;
import Khrypach.Andrey.chess.kletka.gui.coach.CoachTools;
import Khrypach.Andrey.chess.kletka.gui.coach.MarkerOverlay;
import Khrypach.Andrey.chess.kletka.gui.coach.timer.TimerPanel;
import Khrypach.Andrey.chess.kletka.gui.coach.tools.ToolType;
import Khrypach.Andrey.chess.kletka.gui.controllers.MainController;
import Khrypach.Andrey.chess.kletka.gui.dialogs.PromotionDialog;
import Khrypach.Andrey.chess.kletka.gui.dialogs.VariationChoiceDialog;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.menu.MenuBarFactory;
import Khrypach.Andrey.chess.kletka.gui.model.*;
import Khrypach.Andrey.chess.kletka.gui.settings.AppPreferences;
import Khrypach.Andrey.chess.kletka.pgn.index.manager.PgnBrowserManager;
import Khrypach.Andrey.chess.kletka.pgn.index.ui.PgnFileBrowser;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static Khrypach.Andrey.chess.kletka.engine.UciConstants.UCI_NEW_GAME;
import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

public class ChessBoardView extends Application {

    private static final Logger log = LoggerFactory.getLogger(ChessBoardView.class);
    private static final Color BOARD_BORDER_COLOR = Color.rgb(80, 50, 25);

    private final LanguageManager lang = LanguageManager.getInstance();

    private Board chessBoard;
    @Getter
    @Setter
    private Board initialBoard;

    // Массивы для нотации
    private final String[] files = {"a", "b", "c", "d", "e", "f", "g", "h"};
    private final String[] ranks = {"8", "7", "6", "5", "4", "3", "2", "1"};

    // Карта для изображений фигур
    private final Map<Piece, String> pieceImageMap = new HashMap<>();
    private final Map<Piece, Image> pieceImageCache = new HashMap<>();

    private Square selectedSquare = null;
    private List<Move> possibleMoves = new ArrayList<>();
    private Stage primaryStage;

    @Getter
    private NotationView notationView;
    private int tileSize = BoardSizeController.DEFAULT_TILE_SIZE;
    private BorderPane root;
    private boolean showCoordinates = true;
    private boolean boardFlipped = false;

    @Getter
    private MoveNavigationController navController;

    @Getter
    @Setter
    private boolean startWithBlack = false;

    @Getter
    private MainController mainController;
    private BoardTheme.Theme currentTheme = BoardTheme.WOOD;

    @Getter
    private EngineAnalysisPanel analysisPanel;
    @Getter
    private CoachTools coachTools;
    @Getter
    private MarkerOverlay markerOverlay;

    private VBox boardAndNav;
    private AnchorPane anchorPane;
    @Getter
    @Setter
    private GridPane currentBoardGrid;
    private final Map<String, StackPane> squareMap = new HashMap<>();
    private final Map<String, Pane> crossMap = new HashMap<>();
    private Square highlightedSquare = null;
    private TimerPanel timerPanel;

    @Override
    public void start(Stage primaryStage) {
        log.info("Starting ChessBoardView");

        this.primaryStage = primaryStage;
        chessBoard = new Board();
        initialBoard = chessBoard.clone();

        initPieceImageMap();
        loadPieceImages();

        root = new BorderPane();
        root.setUserData(this);

        coachTools = new CoachTools();
        coachTools.setBoardView(this);
        coachTools.setOnMarkersChanged(this::refreshCoachToolsLayout);

        timerPanel = new TimerPanel();

        // Создаем нотацию
        notationView = new NotationView();
        log.debug("NotationView created");

        // Создаем navController
        navController = new MoveNavigationController(this, notationView);
        navController.resetWithNewBoard(chessBoard, false);
        notationView.setNavController(navController);
        log.debug("MoveNavigationController created");

        // Создаем панель анализа
        analysisPanel = new EngineAnalysisPanel(this);
        analysisPanel.setNotationView(notationView);

        // SplitPane для нотации и анализа
        SplitPane rightSplitPane = new SplitPane();
        rightSplitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);
        rightSplitPane.getItems().addAll(notationView, analysisPanel);
        rightSplitPane.setDividerPositions(0.6);

        root.setRight(rightSplitPane);

        // Создаем MainController
        mainController = new MainController(this, primaryStage);
        mainController.setNotationView(notationView);

        MenuBarFactory menuFactory = mainController.getMenuFactory();
        BoardSizeController sizeController = mainController.getSizeController();

        HBox menuBarContainer = menuFactory.createMenuBarWithLanguageButtons();
        root.setTop(menuBarContainer);

        Scene scene = new Scene(root,
                sizeController.calculateWindowWidth(),
                sizeController.calculateWindowHeight());

        int savedSize = sizeController.getTileSize();
        this.tileSize = savedSize;
        log.debug("Applied saved tile size: {}px", savedSize);

        // Загружаем состояние переворота
        boardFlipped = AppPreferences.isBoardFlipped();

        // Загружаем состояние координат
        showCoordinates = AppPreferences.isShowCoordinates();

        int savedThemeIndex = AppPreferences.getBoardThemeIndex();
        if (savedThemeIndex >= 0 && savedThemeIndex < BoardTheme.THEMES.length) {
            this.currentTheme = BoardTheme.THEMES[savedThemeIndex];
            log.debug("Loaded saved theme: {}", currentTheme.name());
        } else {
            this.currentTheme = BoardTheme.WOOD;
            log.debug("Using default theme: WOOD");
        }

        // Обновляем чекбокс в меню
        if (mainController != null && mainController.getMenuFactory() != null) {
            menuFactory.updateCoordinatesCheckbox(showCoordinates);
        }

        setupGlobalHotkeys(scene);

        primaryStage.setTitle(lang.get(APP_TITLE));
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setResizable(true);

        sizeController.tileSizeProperty().addListener((obs, oldVal, newVal) -> {
            int newSize = newVal.intValue();

            // Обновляем tileSize в самом BoardView
            tileSize = newSize;

            // Обновляем крестики
            for (Map.Entry<String, Pane> entry : crossMap.entrySet()) {
                Pane container = entry.getValue();
                if (container != null) {
                    container.setTranslateX(newSize / 2.0);
                    container.setTranslateY(newSize / 2.0);
                    if (container.getChildren().size() >= 2) {
                        double halfSize = newSize * 0.35;
                        javafx.scene.shape.Line line1 = (javafx.scene.shape.Line) container.getChildren().get(0);
                        javafx.scene.shape.Line line2 = (javafx.scene.shape.Line) container.getChildren().get(1);
                        line1.setStartX(-halfSize);
                        line1.setStartY(-halfSize);
                        line1.setEndX(halfSize);
                        line1.setEndY(halfSize);
                        line2.setStartX(halfSize);
                        line2.setStartY(-halfSize);
                        line2.setEndX(-halfSize);
                        line2.setEndY(halfSize);
                    }
                }
            }

            // ========== ГЛАВНОЕ: обновить доску ==========
            Platform.runLater(() -> {
                updateBoardDisplay();
                if (markerOverlay != null) {
                    markerOverlay.redraw();
                }
                root.layout();
            });
        });

        setupKeyHandlers(scene);

        primaryStage.showingProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                Platform.runLater(() -> {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        // Игнорируем
                    }
                    Platform.runLater(() -> {
                        updateBoardDisplay();
                        root.layout();
                    });
                });
            }
        });

        primaryStage.showingProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                Platform.runLater(() -> {
                    updateBoardDisplay();
                    root.layout();
                });
            }
        });

        primaryStage.setOnCloseRequest(event -> {
            // Останавливаем анализ
            if (analysisPanel != null) {
                analysisPanel.shutdown();
            }
            // Останавливаем движок
            if (navController != null && navController.getEngineManager() != null) {
                navController.getEngineManager().stopEngine();
            }
            // Принудительный выход
            Platform.exit();
        });

        // Показываем только если окно еще не показано
        if (!primaryStage.isShowing()) {
            primaryStage.show();
        }
        setHandCursorForBoard();
        scene.getRoot().requestFocus();
        root.setOnMouseClicked(e -> scene.getRoot().requestFocus());



        log.info("Application started successfully");
    }

    private void setupKeyHandlers(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl) return;

            // Пробел - ход движка
            if (event.getCode() == KeyCode.SPACE) {
                event.consume();
                if (analysisPanel != null && analysisPanel.isAnalyzingActive()) {
                    makeEngineMove();
                } else {
                    showTemporaryMessage(lang.get(ENGINE_ANALYSIS_NOT_ACTIVE));
                }
                return;
            }

            // Enter - запуск/остановка анализа
            if (event.getCode() == KeyCode.ENTER && event.isShiftDown()) {
                event.consume();
                if (analysisPanel != null) {
                    analysisPanel.toggleAnalysisByKey();
                }
                return;
            }

            if (event.getCode() == KeyCode.H && !event.isControlDown()) {
                event.consume();
                if (notationView != null) {
                    notationView.setNotationVisible(!notationView.isNotationVisible());
                }
                return;
            }

            // Навигация
            navController.handleKeyPress(event);
        });
    }

    private void setupGlobalHotkeys(Scene scene) {
        KeyCombination ctrlN = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(ctrlN, () -> {
            if (mainController != null) Platform.runLater(() -> mainController.resetGame());
        });

        KeyCombination ctrlP = new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(ctrlP, () -> {
            if (mainController != null) Platform.runLater(() -> mainController.setupPosition());
        });

        KeyCombination ctrlO = new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(ctrlO, () -> Platform.runLater(this::openPgnFile));

        KeyCombination ctrlS = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(ctrlS, () -> Platform.runLater(this::savePgnFile));

        KeyCombination ctrlF = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(ctrlF, () -> {
            if (mainController != null) Platform.runLater(() -> mainController.flipBoard());
        });

        // F11 - СЛЕДУЮЩАЯ ПАРТИЯ
        KeyCombination f11 = new KeyCodeCombination(KeyCode.F11);
        scene.getAccelerators().put(f11, () -> {
            if (mainController != null) {
                mainController.loadNextGameFromBrowser();
            }
        });

        // Ctrl + F11 - ПРЕДЫДУЩАЯ ПАРТИЯ
        KeyCombination ctrlF11 = new KeyCodeCombination(KeyCode.F11, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(ctrlF11, () -> {
            if (mainController != null) {
                mainController.loadPreviousGameFromBrowser();
            }
        });

        // CTRL+TAB - СЛЕДУЮЩИЙ БРАУЗЕР
        KeyCombination ctrlTab = new KeyCodeCombination(KeyCode.TAB, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(ctrlTab, () -> {
            PgnBrowserManager manager = PgnBrowserManager.getInstance();
            Collection<PgnFileBrowser> browsers = manager.getAllBrowsers();
            if (browsers.isEmpty()) return;

            PgnFileBrowser active = manager.getActiveBrowser();
            if (active == null) {
                // Если нет активного - берем первый
                PgnFileBrowser first = browsers.iterator().next();
                first.showWindow();
                manager.setActiveBrowser(first);
                return;
            }

            // Находим следующий браузер
            boolean found = false;
            PgnFileBrowser next = null;
            for (PgnFileBrowser b : browsers) {
                if (found) {
                    next = b;
                    break;
                }
                if (b == active) {
                    found = true;
                }
            }
            if (next == null) {
                // Зацикливаем - берем первый
                next = browsers.iterator().next();
            }

            next.showWindow();
            manager.setActiveBrowser(next);
        });

        // CTRL+SHIFT+TAB - ПРЕДЫДУЩИЙ БРАУЗЕР
        KeyCombination ctrlShiftTab = new KeyCodeCombination(KeyCode.TAB,
                KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        scene.getAccelerators().put(ctrlShiftTab, () -> {
            PgnBrowserManager manager = PgnBrowserManager.getInstance();
            Collection<PgnFileBrowser> browsers = manager.getAllBrowsers();
            if (browsers.isEmpty()) return;

            PgnFileBrowser active = manager.getActiveBrowser();
            if (active == null) {
                // Если нет активного - берем последний
                PgnFileBrowser last = null;
                for (PgnFileBrowser b : browsers) {
                    last = b;
                }
                if (last != null) {
                    last.showWindow();
                    manager.setActiveBrowser(last);
                }
                return;
            }

            // Находим предыдущий браузер
            PgnFileBrowser prev = null;
            PgnFileBrowser lastSeen = null;
            for (PgnFileBrowser b : browsers) {
                if (b == active) {
                    prev = lastSeen;
                    break;
                }
                lastSeen = b;
            }
            if (prev == null) {
                // Если не нашли - берем последний
                for (PgnFileBrowser b : browsers) {
                    prev = b;
                }
            }

            if (prev != null) {
                prev.showWindow();
                manager.setActiveBrowser(prev);
            }
        });

        // CTRL+W - ЗАКРЫТЬ ТЕКУЩИЙ БРАУЗЕР
        KeyCombination ctrlW = new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(ctrlW, () -> {
            PgnBrowserManager manager = PgnBrowserManager.getInstance();
            PgnFileBrowser active = manager.getActiveBrowser();
            if (active != null) {
                manager.closeBrowser(active);
                // Меню обновится через слушатель
            }
        });
    }

    private void initPieceImageMap() {
        pieceImageMap.put(Piece.WHITE_KING, "wK.png");
        pieceImageMap.put(Piece.WHITE_QUEEN, "wQ.png");
        pieceImageMap.put(Piece.WHITE_ROOK, "wR.png");
        pieceImageMap.put(Piece.WHITE_BISHOP, "wB.png");
        pieceImageMap.put(Piece.WHITE_KNIGHT, "wN.png");
        pieceImageMap.put(Piece.WHITE_PAWN, "wP.png");
        pieceImageMap.put(Piece.BLACK_KING, "bK.png");
        pieceImageMap.put(Piece.BLACK_QUEEN, "bQ.png");
        pieceImageMap.put(Piece.BLACK_ROOK, "bR.png");
        pieceImageMap.put(Piece.BLACK_BISHOP, "bB.png");
        pieceImageMap.put(Piece.BLACK_KNIGHT, "bN.png");
        pieceImageMap.put(Piece.BLACK_PAWN, "bP.png");
    }

    private void loadPieceImages() {
        for (Map.Entry<Piece, String> entry : pieceImageMap.entrySet()) {
            try {
                String imagePath = "/images/pieces/" + entry.getValue();
                Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
                pieceImageCache.put(entry.getKey(), image);
            } catch (Exception e) {
                log.error("{}: {}", lang.get(ENGINE_IMAGE_LOAD_ERROR), entry.getKey(), e);
            }
        }
    }

    private void updateBoardDisplay() {
        GridPane boardWithCoordinates = createBoardWithCoordinates();
        currentBoardGrid = boardWithCoordinates;

        HBox navPanel = navController.createNavigationPanel();
        navPanel.setAlignment(Pos.CENTER);
        navPanel.prefWidthProperty().bind(boardWithCoordinates.widthProperty());
        navPanel.maxWidthProperty().bind(boardWithCoordinates.widthProperty());

        boardAndNav = new VBox(10);
        boardAndNav.setAlignment(Pos.TOP_LEFT);
        boardAndNav.getChildren().addAll(boardWithCoordinates, navPanel);

        StackPane boardStack = new StackPane();
        boardStack.setAlignment(Pos.TOP_LEFT);
        boardStack.getChildren().add(boardAndNav);
        StackPane.setAlignment(boardAndNav, Pos.TOP_LEFT);

        if (markerOverlay == null) {
            markerOverlay = new MarkerOverlay(coachTools);
        }
        markerOverlay.setBoardContainer(boardAndNav);
        boardStack.getChildren().add(markerOverlay);
        StackPane.setAlignment(markerOverlay, Pos.TOP_LEFT);
        markerOverlay.prefWidthProperty().bind(boardAndNav.widthProperty());
        markerOverlay.prefHeightProperty().bind(boardAndNav.heightProperty());

        anchorPane = new AnchorPane();
        anchorPane.getChildren().add(coachTools);
        AnchorPane.setLeftAnchor(coachTools, 0.0);
        AnchorPane.setTopAnchor(coachTools, 20.0);

        anchorPane.getChildren().add(boardStack);
        double leftOffset = coachTools.isPanelExpanded() ? coachTools.getWidth() + 20.0 : coachTools.getWidth() + 10.0;
        AnchorPane.setLeftAnchor(boardStack, leftOffset);
        AnchorPane.setTopAnchor(boardStack, 20.0);

        anchorPane.getChildren().add(timerPanel);
        AnchorPane.setRightAnchor(timerPanel, 20.0);
        AnchorPane.setTopAnchor(timerPanel, 20.0);

        root.setCenter(anchorPane);
        root.layout();
    }

    private GridPane createBoardWithCoordinates() {
        GridPane boardGrid = new GridPane();
        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.setPadding(new Insets(12));
        boardGrid.setStyle(
                "-fx-background-color: " + toRgbString(Color.rgb(80, 50, 25)) + ";" +
                        "-fx-border-color: black;" +
                        "-fx-border-width: 3px;" +
                        "-fx-border-style: solid;"
        );

        int fontSize = Math.max(10, tileSize / 6);
        String coordStyle = "-fx-font-size: " + fontSize + "px; -fx-font-weight: bold; -fx-text-fill: #f5e6d3; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 1, 0.5, 0, 0);";

        if (showCoordinates) {
            for (int col = 0; col < 8; col++) {
                Label label = new Label(files[col]);
                label.setAlignment(Pos.CENTER);
                label.setPrefSize(tileSize, fontSize + 8);
                label.setStyle(coordStyle);
                boardGrid.add(label, col + 1, 0);
            }
            for (int col = 0; col < 8; col++) {
                Label label = new Label(files[col]);
                label.setAlignment(Pos.CENTER);
                label.setPrefSize(tileSize, fontSize + 8);
                label.setStyle(coordStyle);
                boardGrid.add(label, col + 1, 9);
            }
            for (int row = 0; row < 8; row++) {
                String rank = boardFlipped ? ranks[7 - row] : ranks[row];
                Label label = new Label(rank);
                label.setAlignment(Pos.CENTER);
                label.setPrefSize(fontSize + 8, tileSize);
                label.setStyle(coordStyle);
                boardGrid.add(label, 0, row + 1);
            }
            for (int row = 0; row < 8; row++) {
                String rank = boardFlipped ? ranks[7 - row] : ranks[row];
                Label label = new Label(rank);
                label.setAlignment(Pos.CENTER);
                label.setPrefSize(fontSize + 8, tileSize);
                label.setStyle(coordStyle);
                boardGrid.add(label, 9, row + 1);
            }
        }

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane tile = createTile(row, col);
                boardGrid.add(tile, col + 1, row + 1);
            }
        }

        currentBoardGrid = boardGrid;
        return boardGrid;
    }

    private StackPane createTile(int row, int col) {
        StackPane tile = new StackPane();
        tile.setMinSize(tileSize, tileSize);
        tile.setMaxSize(tileSize, tileSize);
        tile.setPrefSize(tileSize, tileSize);
        tile.setAlignment(Pos.CENTER);

        int chessRow = boardFlipped ? row : 7 - row;
        int chessCol = boardFlipped ? 7 - col : col;
        Square square = convertToSquare(chessRow, chessCol);

        Rectangle background = createBoardTile(row, col);
        background.setWidth(tileSize);
        background.setHeight(tileSize);
        tile.getChildren().add(background);

        if (selectedSquare == square) {
            Rectangle highlight = new Rectangle(tileSize, tileSize);
            highlight.setFill(Color.rgb(255, 255, 0, 0.3));
            tile.getChildren().add(highlight);
        }

        if (isMoveTarget(square)) {
            Circle marker = new Circle((double) tileSize / 6);
            marker.setFill(Color.rgb(0, 255, 0, 0.5));
            tile.getChildren().add(marker);
        }

        Piece piece = chessBoard.getPiece(square);
        if (piece != Piece.NONE) {
            ImageView pieceImage = createPieceImage(piece);
            if (pieceImage != null) {
                tile.getChildren().add(pieceImage);
            }
        }

        squareMap.put(square.name(), tile);
        final boolean[] isArrowDrag = {false};

        tile.setOnMouseClicked(e -> handleTileClick(square));

        tile.setOnDragDetected(e -> {
            if (coachTools != null && coachTools.getCurrentTool() == ToolType.ARROW) {
                isArrowDrag[0] = true;
                Dragboard db = tile.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(square.name());
                db.setContent(content);
                WritableImage transparent = new WritableImage(1, 1);
                db.setDragView(transparent);
                coachTools.startArrowDrag(square.name());
                e.consume();
                return;
            }

            if (isTerminalPosition()) {
                showTemporaryMessage(lang.get(LanguageKeys.ENGINE_TERMINAL_POSITION));
                return;
            }

            Piece pieceAtSquare = chessBoard.getPiece(square);
            if (pieceAtSquare != Piece.NONE && pieceAtSquare.getPieceSide() == chessBoard.getSideToMove()) {
                Dragboard db = tile.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(square.name());
                db.setContent(content);

                ImageView pieceImage = createPieceImage(pieceAtSquare);
                if (pieceImage != null) {
                    double imageSize = tileSize * 0.9;
                    pieceImage.setFitWidth(imageSize);
                    pieceImage.setFitHeight(imageSize);
                    StackPane tempPane = new StackPane(pieceImage);
                    tempPane.setStyle("-fx-background-color: transparent;");
                    SnapshotParameters params = new SnapshotParameters();
                    params.setFill(Color.TRANSPARENT);
                    WritableImage snapshot = tempPane.snapshot(params, null);
                    db.setDragView(snapshot, imageSize / 2, imageSize / 2);
                }
                e.consume();
            }
        });

        tile.setOnDragOver(e -> {
            if (isArrowDrag[0] && e.getDragboard().hasString()) {
                e.acceptTransferModes(TransferMode.MOVE);
                coachTools.updateArrowDrag(square.name());
                e.consume();
            } else if (e.getDragboard().hasString() && !isArrowDrag[0]) {
                e.acceptTransferModes(TransferMode.MOVE);
                e.consume();
            }
        });

        tile.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            if (isArrowDrag[0] && db.hasString()) {
                String fromSquare = db.getString();
                coachTools.finishArrowDrag(fromSquare, square.name());
                if (markerOverlay != null) markerOverlay.redraw();
                e.setDropCompleted(true);
            } else if (db.hasString() && !isArrowDrag[0]) {
                Square fromSquare = Square.valueOf(db.getString());
                List<Move> legalMoves = chessBoard.legalMoves().stream()
                        .filter(m -> m.getFrom() == fromSquare && m.getTo() == square)
                        .toList();

                if (!legalMoves.isEmpty()) {
                    Move move = legalMoves.get(0);
                    Piece movingPiece = chessBoard.getPiece(move.getFrom());
                    boolean isCapture = chessBoard.getPiece(move.getTo()) != Piece.NONE;

                    // Проверяем превращение
                    Piece promotionPiece = null;
                    if (movingPiece == Piece.WHITE_PAWN && move.getTo().getRank().ordinal() == 7) {
                        log.debug("Drag&Drop - PROMOTION for white pawn");
                        PromotionDialog dialog = new PromotionDialog(primaryStage, true);
                        promotionPiece = dialog.showAndWait();
                        if (promotionPiece == null) promotionPiece = Piece.WHITE_QUEEN;
                    } else if (movingPiece == Piece.BLACK_PAWN && move.getTo().getRank().ordinal() == 0) {
                        log.debug("Drag&Drop - PROMOTION for black pawn");
                        PromotionDialog dialog = new PromotionDialog(primaryStage, false);
                        promotionPiece = dialog.showAndWait();
                        if (promotionPiece == null) promotionPiece = Piece.BLACK_QUEEN;
                    }

                    executeMove(move, movingPiece, isCapture, promotionPiece);
                    e.setDropCompleted(true);
                } else {
                    e.setDropCompleted(false);
                }
            }
            isArrowDrag[0] = false;
            e.consume();
        });

        tile.setOnDragDone(e -> {
            if (isArrowDrag[0]) {
                coachTools.cancelArrowDrag();
                isArrowDrag[0] = false;
            }
            tile.setCursor(Cursor.OPEN_HAND);
            e.consume();
        });

        return tile;
    }

    private Rectangle createBoardTile(int row, int col) {
        Rectangle rect = new Rectangle(tileSize, tileSize);
        Color lightColor = currentTheme.lightColor();
        Color darkColor = currentTheme.darkColor();
        rect.setFill((row + col) % 2 == 0 ? lightColor : darkColor);
        rect.setStroke(BOARD_BORDER_COLOR);
        rect.setStrokeWidth(1);
        return rect;
    }

    private Square convertToSquare(int row, int col) {
        return Square.squareAt(row * 8 + col);
    }

    private boolean isMoveTarget(Square square) {
        return possibleMoves.stream().anyMatch(move -> move.getTo() == square);
    }

    private String toRgbString(Color color) {
        return String.format("rgb(%d, %d, %d)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private void handleTileClick(Square clickedSquare) {
        if (coachTools != null && coachTools.isPanelExpanded()) {
            ToolType tool = coachTools.getCurrentTool();
            if (tool == ToolType.CROSS) {
                coachTools.addCross(clickedSquare.name());
                return;
            }
            if (tool == ToolType.ARROW) {
                coachTools.handleArrowClick(clickedSquare.name());
                return;
            }
        }

        if (isTerminalPosition()) {
            log.debug("Terminal position, no moves possible");
            return;
        }

        if (selectedSquare == null) {
            Piece piece = chessBoard.getPiece(clickedSquare);
            if (piece != Piece.NONE && piece.getPieceSide() == chessBoard.getSideToMove()) {
                selectedSquare = clickedSquare;
                possibleMoves = chessBoard.legalMoves().stream()
                        .filter(move -> move.getFrom() == clickedSquare)
                        .collect(Collectors.toList());
                refreshBoard();
            }
        } else {
            if (selectedSquare == clickedSquare) {
                selectedSquare = null;
                possibleMoves.clear();
                refreshBoard();
            } else {
                Move move = possibleMoves.stream()
                        .filter(m -> m.getTo() == clickedSquare)
                        .findFirst()
                        .orElse(null);

                if (move != null) {
                    Piece movingPiece = chessBoard.getPiece(move.getFrom());
                    boolean isCapture = chessBoard.getPiece(move.getTo()) != Piece.NONE;
                    Piece promotionPiece = null;

                    log.trace("Move: {} -> {}", move.getFrom(), move.getTo());
                    log.trace("Moving piece: {}", movingPiece);
                    log.trace("To rank: {} (ordinal={})", move.getTo().getRank(), move.getTo().getRank().ordinal());
                    log.trace("Is white pawn? {}", movingPiece == Piece.WHITE_PAWN);
                    log.trace("Is rank 7? {}", move.getTo().getRank().ordinal() == 7);

                    if (movingPiece == Piece.WHITE_PAWN && move.getTo().getRank().ordinal() == 7) {
                        log.debug("PROMOTION DETECTED for white");
                        PromotionDialog dialog = new PromotionDialog(primaryStage, true);
                        promotionPiece = dialog.showAndWait();
                        if (promotionPiece == null) promotionPiece = Piece.WHITE_QUEEN;
                        log.trace("Selected promotion white piece: {}", promotionPiece);
                    } else if (movingPiece == Piece.BLACK_PAWN && move.getTo().getRank().ordinal() == 0) {
                        log.debug("PROMOTION DETECTED for black");
                        PromotionDialog dialog = new PromotionDialog(primaryStage, false);
                        promotionPiece = dialog.showAndWait();
                        if (promotionPiece == null) promotionPiece = Piece.BLACK_QUEEN;
                        log.trace("Selected promotion black piece: {}", promotionPiece);
                    }

                    executeMove(move, movingPiece, isCapture, promotionPiece);
                }
            }
        }
        requestFocusOnScene();
    }

    /**
     * Выполняет ход с правильной последовательностью: сначала диалог, потом ход
     */
    private void executeMove(Move move, Piece movingPiece, boolean isCapture, Piece promotionPiece) {
        if (coachTools != null && coachTools.isPanelExpanded()) {
            coachTools.togglePanel();
        }

        Board boardBeforeMove = chessBoard.clone();

        Boolean addResult = navController.addMove(move, movingPiece, isCapture, promotionPiece);

        if (addResult == null) {
            VariationChoiceDialog.Choice choice = navController.showVariationDialog(
                    move, movingPiece, isCapture, promotionPiece);

            if (choice == null) {
                log.debug("User cancelled - reverting everything");
                selectedSquare = null;
                possibleMoves.clear();
                chessBoard = boardBeforeMove.clone();
                refreshBoard();
                notationView.refreshFromMainLine();
                requestFocusOnScene();
                return;
            }

            navController.applyVariationChoice(choice, move, movingPiece, isCapture, promotionPiece);
        } else {
            log.trace("Move handled by navController, result: {}", addResult);
        }

        mainController.updateCurrentGameData();

        navController.restoreBoardFromCurrentNode();

        if (analysisPanel != null) {
            updateEngineAfterMove();
        }

        // ========== ГАРАНТИРУЕМ ОБНОВЛЕНИЕ НОТАЦИИ ==========
        if (notationView != null) {
            log.trace("Forcing notation update...");
            Platform.runLater(() -> {
                notationView.refreshFromMainLine();
                notationView.updateNotationDisplayWithVisitor();
                log.trace("Notation update completed");
            });
        } else {
            log.error("notationView is NULL!");
        }

        checkGameEnd();

        selectedSquare = null;
        possibleMoves.clear();

        refreshBoard();
        notifyPositionChanged();
        requestFocusOnScene();
    }

    private void updateEngineAfterMove() {
        navController.getEngineManager().stopAnalysis();

        try {
            navController.getEngineManager().sendCommand(UCI_NEW_GAME);
        } catch (IOException e) {
            log.debug("Engine doesn't support ucinewgame");
        }

        navController.getEngineManager().sendPosition(chessBoard);

        if (analysisPanel.isAnalyzingActive()) {
            navController.getEngineManager().startInfiniteAnalysis();
        }
    }

    private void checkGameEnd() {
        // Проверяем, находимся ли мы в главной линии
        boolean isMainLinePosition = isInMainLine();

        if (chessBoard.isMated()) {
            String winnerKey = chessBoard.getSideToMove() == Side.WHITE ? GAME_BLACK : GAME_WHITE;
            String winner = lang.get(winnerKey);
            String result = chessBoard.getSideToMove() == Side.WHITE ? "0-1" : "1-0";

            String message = isMainLinePosition
                    ? lang.get(GAME_CHECKMATE, winner) + " (" + result + ")"
                    : "⚠️ " + lang.get(GAME_CHECKMATE, winner) + " ";
            showGameOverMessage(message);

            if (isMainLinePosition) {
                notationView.setGameResult(result);
                updateGameDataResult(result);
            } else {
                notationView.refreshFromMainLine();
            }

            selectedSquare = null;
            possibleMoves.clear();
            refreshBoard();

        } else if (chessBoard.isStaleMate()) {
            String message = isMainLinePosition
                    ? lang.get(GAME_STALEMATE)
                    : "⚠️ " + lang.get(GAME_STALEMATE) + " ";
            showGameOverMessage(message);

            if (isMainLinePosition) {
                notationView.setGameResult("1/2-1/2");
                updateGameDataResult("1/2-1/2");
            } else {
                notationView.refreshFromMainLine();
            }

            selectedSquare = null;
            possibleMoves.clear();
            refreshBoard();

        } else if (chessBoard.isInsufficientMaterial()) {
            String message = isMainLinePosition
                    ? lang.get(GAME_INSUFFICIENT_MATERIAL)
                    : "⚠️ " + lang.get(GAME_INSUFFICIENT_MATERIAL) + " ";
            showGameOverMessage(message);

            if (isMainLinePosition) {
                notationView.setGameResult("1/2-1/2");
                updateGameDataResult("1/2-1/2");
            } else {
                notationView.refreshFromMainLine();
            }

            selectedSquare = null;
            possibleMoves.clear();
            refreshBoard();
        }
    }

    /**
     * Обновляет результат в GameData (обновлено для новых полей)
     */
    private void updateGameDataResult(String result) {
        if (notationView == null) return;

        GameData oldData = notationView.getCurrentGameData();

        if (oldData == null) {
            // Если GameData еще нет, создаем дефолтный с новыми полями
            GameData defaultData = new GameData(
                    "Player", "Player", result,
                    "?", "?",
                    "Kletka Game", "?", "?",
                    "?", LocalDate.now(),
                    "?", "?", "?",
                    "?", "?", "?", "?",
                    "?", "?", "?", "?",
                    "",
                    "",     // fen
                    false,  // isSetUp
                    "game",  // positionType
                    false
            );
            notationView.setCurrentGameData(defaultData);
            return;
        }

        // Создаем новый GameData с обновленным результатом, сохраняя все поля
        GameData newData = new GameData(
                oldData.whitePlayer(),
                oldData.blackPlayer(),
                result,
                oldData.whiteElo(),
                oldData.blackElo(),
                oldData.event(),
                oldData.site(),
                oldData.round(),
                oldData.subround(),
                oldData.date(),
                oldData.eco(),
                oldData.opening(),
                oldData.variation(),
                oldData.annotator(),
                oldData.whiteTeam(),
                oldData.blackTeam(),
                oldData.source(),
                oldData.whiteFideId(),
                oldData.blackFideId(),
                oldData.timeControl(),
                oldData.plyCount(),
                oldData.pgn(),
                oldData.fen(),
                oldData.isSetUp(),
                oldData.positionType(),
                oldData.deleted()
        );
        notationView.setCurrentGameData(newData);
    }

    private void showGameOverMessage(String message) {
        log.info("Game over: {}", message);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(lang.get(NOTIFICATION_INFO));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private ImageView createPieceImage(Piece piece) {
        Image image = pieceImageCache.get(piece);
        if (image == null) return null;
        ImageView imageView = new ImageView(image);
        double imageSize = tileSize * 0.9;
        imageView.setFitWidth(imageSize);
        imageView.setFitHeight(imageSize);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    public void refreshBoard() {
        Platform.runLater(this::updateBoardDisplay);
    }

    public void flipBoard() {
        boardFlipped = !boardFlipped;
        AppPreferences.saveBoardFlipped(boardFlipped);
        refreshBoard();
    }

    public void setShowCoordinates(boolean show) {
        this.showCoordinates = show;
        AppPreferences.saveShowCoordinates(show);
        refreshBoard();
    }

    public void setBoard(Board board) {
        if (board != null) {
            this.chessBoard = board.clone();
            this.initialBoard = this.chessBoard.clone();
            refreshBoard();
        }
    }

    public Board getCurrentBoard() {
        return chessBoard.clone();
    }

    public void resetGame() {
        Board newBoard = new Board();
        this.chessBoard = newBoard.clone();
        this.initialBoard = newBoard.clone();
        selectedSquare = null;
        possibleMoves.clear();

        if (navController != null) {
            navController.resetWithNewBoard(this.chessBoard, false);
            navController.resetInitialPosition();
        }

        if (notationView != null) {
            notationView.resetGameResult();
        }

        startWithBlack = false;
        refreshBoard();
        primaryStage.setTitle(lang.get(APP_TITLE));
        notifyPositionChanged();
    }

    public void forceResetGame() {
        Board newBoard = new Board();
        this.chessBoard = newBoard.clone();
        this.initialBoard = newBoard.clone();
        selectedSquare = null;
        possibleMoves.clear();

        if (navController != null) {
            navController.resetWithNewBoard(this.chessBoard, false);
        }

        if (notationView != null) {
            notationView.resetGameResult();
        }

        startWithBlack = false;
        refreshBoard();
        primaryStage.setTitle(lang.get(APP_TITLE));
        notifyPositionChanged();
    }

    public void setupNewPosition(Board newBoard, boolean startWithBlack) {
        if (coachTools != null && coachTools.isPanelExpanded()) {
            coachTools.togglePanel();
        }

        selectedSquare = null;
        possibleMoves.clear();
        this.startWithBlack = startWithBlack;

        if (navController != null) {
            navController.resetWithNewBoard(newBoard, startWithBlack);
            navController.setInitialPosition(newBoard);

            // Проверяем, что дерево корректно инициализировано
            if (navController.getRootVariation() != null &&
                    navController.getRootVariation().getFirstNode() != null) {
                navController.updateAllVariationNames();
            }

            // Обновляем доску
            chessBoard = newBoard.clone();
            refreshBoard();
        }

        notifyPositionChanged();
    }

    public void setBoardTheme(BoardTheme.Theme theme) {
        this.currentTheme = theme;
        refreshBoard();
    }

    public void notifyPositionChanged() {
        if (analysisPanel != null) {
            analysisPanel.onPositionChanged();
        }
    }

    public void requestFocusOnScene() {
        Scene scene = primaryStage.getScene();
        if (scene != null && scene.getRoot() != null) {
            scene.getRoot().requestFocus();
        }
    }

    private void makeEngineMove() {
        if (analysisPanel == null) return;
        if (isTerminalPosition()) {
            showTemporaryMessage(lang.get(ENGINE_TERMINAL_POSITION));
            return;
        }
        if (!analysisPanel.isAnalyzingActive()) {
            showTemporaryMessage(lang.get(ENGINE_ANALYSIS_NOT_ACTIVE));
            return;
        }

        String bestMoveUci = analysisPanel.getBestMoveFromAnalysis();
        if (bestMoveUci == null || bestMoveUci.isEmpty()) {
            showTemporaryMessage(lang.get(ENGINE_NOT_ANALYZED));
            return;
        }

        try {
            Move move = navController.getEngineManager().convertUciToMove(bestMoveUci);

            if (!chessBoard.isMoveLegal(move, true)) {
                log.warn("Engine move is illegal: {}", bestMoveUci);
                showTemporaryMessage(lang.get(ENGINE_ILLEGAL_MOVE) + ": " + bestMoveUci);
                return;
            }

            Piece movingPiece = chessBoard.getPiece(move.getFrom());
            boolean isCapture = chessBoard.getPiece(move.getTo()) != Piece.NONE;

            // Определяем promotionPiece
            Piece promotionPiece = null;

            if (movingPiece == Piece.WHITE_PAWN && move.getTo().getRank().ordinal() == 7) {
                log.debug("EngineMove - PROMOTION for white pawn");
                PromotionDialog dialog = new PromotionDialog(primaryStage, true);
                promotionPiece = dialog.showAndWait();
                if (promotionPiece == null) promotionPiece = Piece.WHITE_QUEEN;
            } else if (movingPiece == Piece.BLACK_PAWN && move.getTo().getRank().ordinal() == 0) {
                log.debug("EngineMove - PROMOTION for black pawn");
                PromotionDialog dialog = new PromotionDialog(primaryStage, false);
                promotionPiece = dialog.showAndWait();
                if (promotionPiece == null) promotionPiece = Piece.BLACK_QUEEN;
            }

            // Останавливаем анализ
            navController.getEngineManager().stopAnalysis();

            // Передаем promotionPiece в addMove (может быть null)
            navController.addMove(move, movingPiece, isCapture, promotionPiece);

            mainController.updateCurrentGameData();

            notationView.refreshFromMainLine();
            checkGameEnd();
            updateEngineAfterMove();

            selectedSquare = null;
            possibleMoves.clear();
            refreshBoard();

        } catch (Exception e) {
            log.error("Error executing engine move", e);
            showTemporaryMessage(lang.get(LanguageKeys.ENGINE_MOVE_ERROR) + e.getMessage());
        }
    }

    private void showTemporaryMessage(String message) {
        Popup popup = new Popup();
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 12px; -fx-background-color: #333; -fx-text-fill: white; -fx-padding: 8;");
        popup.getContent().add(messageLabel);

        if (root != null) {
            Scene scene = root.getScene();
            double centerX = scene.getWindow().getX() + scene.getWidth() / 2 - 100;
            double centerY = scene.getWindow().getY() + scene.getHeight() / 4;
            popup.show(root, centerX, centerY);
            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(event -> popup.hide());
            delay.play();
        }
    }

    private void setHandCursorForBoard() {
        Scene scene = primaryStage.getScene();
        if (scene != null) {
            scene.setCursor(Cursor.OPEN_HAND);
        }
    }

    private void openPgnFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(lang.get(MENU_FILE_OPEN_PGN));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PGN files", "*.pgn"));
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null && mainController != null) {
            mainController.loadPgnFile(file);
        }
    }

    private void savePgnFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(lang.get(MENU_FILE_SAVE_PGN));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PGN files", "*.pgn"));
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null && mainController != null) {
            mainController.savePgnFile(file);
        }
    }

    public void refreshCoachToolsLayout() {
        if (anchorPane != null && coachTools != null && boardAndNav != null) {
            Platform.runLater(() -> {
                double leftOffset = coachTools.isPanelExpanded() ? coachTools.getWidth() + 20.0 : coachTools.getWidth() + 10.0;
                AnchorPane.setLeftAnchor(boardAndNav, leftOffset);
                root.layout();
                if (markerOverlay != null) markerOverlay.redraw();
            });
        }
    }

    public boolean isTerminalPosition() {
        return chessBoard.isMated() || chessBoard.isStaleMate() || chessBoard.isInsufficientMaterial();
    }

    public StackPane getSquarePane(String squareName) {
        return squareMap.get(squareName);
    }

    public void addCrossToSquare(String squareName, Color color) {
        if (crossMap.containsKey(squareName)) {
            updateCrossColor(squareName, color);
            return;
        }

        StackPane cell = squareMap.get(squareName);
        if (cell == null) return;

        Pane crossContainer = new Pane();
        crossContainer.setMouseTransparent(true);

        // Устанавливаем размеры контейнера
        crossContainer.setPrefSize(tileSize, tileSize);
        crossContainer.setMaxSize(tileSize, tileSize);
        crossContainer.setMinSize(tileSize, tileSize);

        // Смещаем в центр клетки
        crossContainer.setTranslateX(tileSize / 2.0);
        crossContainer.setTranslateY(tileSize / 2.0);

        // Создаем линии
        double halfSize = tileSize * 0.25;
        javafx.scene.shape.Line line1 = new javafx.scene.shape.Line(
                -halfSize, -halfSize, halfSize, halfSize
        );
        javafx.scene.shape.Line line2 = new javafx.scene.shape.Line(
                halfSize, -halfSize, -halfSize, halfSize
        );

        line1.setStroke(color);
        line2.setStroke(color);
        line1.setStrokeWidth(Math.max(3, tileSize * 0.08));
        line2.setStrokeWidth(Math.max(3, tileSize * 0.08));
        line1.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        line2.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);

        crossContainer.getChildren().addAll(line1, line2);
        cell.getChildren().add(crossContainer);
        crossMap.put(squareName, crossContainer);
    }

    public void removeCrossFromSquare(String squareName) {
        Pane cross = crossMap.remove(squareName);
        if (cross != null) {
            StackPane cell = squareMap.get(squareName);
            if (cell != null) cell.getChildren().remove(cross);
        }
    }

    public void clearAllCrosses() {
        for (String squareName : new ArrayList<>(crossMap.keySet())) {
            removeCrossFromSquare(squareName);
        }
        crossMap.clear();
    }

    public void updateCrossColor(String squareName, Color color) {
        Pane cross = crossMap.get(squareName);
        if (cross != null && cross.getChildren().size() >= 2) {
            javafx.scene.shape.Line line1 = (javafx.scene.shape.Line) cross.getChildren().get(0);
            javafx.scene.shape.Line line2 = (javafx.scene.shape.Line) cross.getChildren().get(1);
            line1.setStroke(color);
            line2.setStroke(color);
        }
    }

    public ImageView getWhiteKingIcon() {
        return createTurnIcon(Piece.WHITE_KING, lang.get(GAME_WHITE));
    }

    public ImageView getBlackKingIcon() {
        return createTurnIcon(Piece.BLACK_KING, lang.get(GAME_BLACK));
    }

    private ImageView createTurnIcon(Piece piece, String tooltipText) {
        Image image = pieceImageCache.get(piece);
        if (image == null) return null;
        ImageView icon = new ImageView(image);
        double iconSize = tileSize * 0.5;
        icon.setFitWidth(iconSize);
        icon.setFitHeight(iconSize);
        icon.setPreserveRatio(true);
        Tooltip.install(icon, new Tooltip(tooltipText));
        return icon;
    }

    public void highlightSquare(String squareName, Color color) {
        clearHighlight(); // сначала очищаем предыдущую подсветку

        StackPane cell = squareMap.get(squareName);
        if (cell != null) {
            Rectangle highlight = new Rectangle(tileSize, tileSize);
            highlight.setFill(color);
            highlight.setOpacity(0.3);
            highlight.setUserData("arrow_highlight");
            cell.getChildren().add(highlight);
            highlightedSquare = Square.valueOf(squareName);
        }
    }

    public void clearHighlight() {
        if (highlightedSquare != null) {
            StackPane cell = squareMap.get(highlightedSquare.name());
            if (cell != null) {
                cell.getChildren().removeIf(node -> node instanceof Rectangle &&
                        "arrow_highlight".equals(node.getUserData()));
            }
            highlightedSquare = null;
        }
    }

    /**
     * Проверяет, находится ли текущая позиция в главной линии
     */
    private boolean isInMainLine() {
        if (navController == null) return true;

        Variation currentVar = navController.getCurrentVariation();
        Variation mainLine = navController.getMainLine();

        // Если мы в корне или в главной линии - возвращаем true
        if (currentVar == null || mainLine == null) return true;

        return currentVar.isMainLine();
    }
}