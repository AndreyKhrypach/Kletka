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
import Khrypach.Andrey.chess.kletka.gui.menu.CustomMenuBarFactory;
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
import javafx.scene.*;
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
import static Khrypach.Andrey.chess.kletka.gui.model.SanGenerator.countAttackersViaReflection;

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
    @Getter
    private int tileSize = BoardSizeController.DEFAULT_TILE_SIZE;
    private BorderPane root;
    private boolean showCoordinates = true;
    @Getter
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
    private Square highlightedSquare = null;

    /**
     * Индикатор режима навигации
     */
    private Label navigationModeIndicator;

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

        TimerPanel timerPanel = new TimerPanel();

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

        BoardSizeController sizeController = mainController.getSizeController();

        CustomMenuBarFactory customMenuFactory = new CustomMenuBarFactory(mainController, primaryStage, sizeController);
        customMenuFactory.setTimerPanel(timerPanel);
        HBox menuBarContainer = customMenuFactory.createCustomMenuBar();
        root.setTop(menuBarContainer);

        mainController.setMenuFactory(customMenuFactory);
        customMenuFactory.updateBooksMenu();

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
            customMenuFactory.updateCoordinatesCheckbox(showCoordinates);
        }

        setupGlobalHotkeys(scene);

        primaryStage.setTitle(lang.get(APP_TITLE));
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setResizable(true);

        sizeController.tileSizeProperty().addListener((obs, oldVal, newVal) -> {

            // Обновляем tileSize в самом BoardView
            tileSize = newVal.intValue();

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

        setupGlobalMouseHandlers(scene);

        log.info("Application started successfully");
    }

    private void setupKeyHandlers(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl) return;

            // Пробел - ход движка
            if (event.getCode() == KeyCode.SPACE) {
                event.consume();

                // ========== ПРОВЕРКА РЕЖИМА КНИГИ ==========
                if (navController != null && navController.getNavigationMode() == NavigationMode.BOOK) {
                    showTemporaryMessage(lang.get(ENGINE_BOOK_MODE_NO_MOVE)); // "В режиме книги движок не может делать ходы"
                    return;
                }

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

                // ========== В РЕЖИМЕ КНИГИ АНАЛИЗ ТОЖЕ НЕ ДОЛЖЕН РАБОТАТЬ ==========
                if (navController != null && navController.getNavigationMode() == NavigationMode.BOOK) {
                    showTemporaryMessage(lang.get(ENGINE_BOOK_MODE_NO_ANALYSIS)); // "В режиме книги анализ отключен"
                    return;
                }

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

        // CTRL+tab - СЛЕДУЮЩИЙ БРАУЗЕР
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

        // CTRL+shift+tab - ПРЕДЫДУЩИЙ БРАУЗЕР
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

        // Ctrl+= (увеличение) - основная клавиатура
        KeyCombination ctrlEquals = new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(ctrlEquals, () -> {
            if (mainController != null && mainController.getSizeController() != null) {
                mainController.getSizeController().increaseSize();
            }
        });

        // Ctrl++ (цифровая клавиатура)
        KeyCombination ctrlPlus = new KeyCodeCombination(KeyCode.PLUS, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(ctrlPlus, () -> {
            if (mainController != null && mainController.getSizeController() != null) {
                mainController.getSizeController().increaseSize();
            }
        });

        // Ctrl+- (уменьшение) - основная клавиатура
        KeyCombination ctrlMinus = new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(ctrlMinus, () -> {
            if (mainController != null && mainController.getSizeController() != null) {
                mainController.getSizeController().decreaseSize();
            }
        });

        // Ctrl+0 (сброс масштаба)
        KeyCombination ctrlZero = new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(ctrlZero, () -> {
            if (mainController != null && mainController.getSizeController() != null) {
                mainController.getSizeController().resetSize();
            }
        });

        // Ctrl+Z — отмена последнего маркера (undo)
        KeyCombination ctrlZ = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(ctrlZ, () -> {
            if (coachTools != null && coachTools.canUndo()) {
                coachTools.undoLastAction();
                // Обновляем состояние в меню
                if (mainController != null && mainController.getMenuFactory() != null) {
                    mainController.getMenuFactory().updateUndoRedoState(
                            coachTools.canUndo(),
                            coachTools.canRedo()
                    );
                }
            } else {
                log.debug("Ctrl+Z: cannot undo (coachTools={}, canUndo={})",
                        coachTools != null ? "exists" : "null",
                        coachTools != null && coachTools.canUndo());
            }
        });

        // Ctrl+Y — повтор последнего маркера (redo)
        KeyCombination ctrlY = new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(ctrlY, () -> {
            if (coachTools != null && coachTools.canRedo()) {
                coachTools.redo();
                // Обновляем состояние в меню
                if (mainController != null && mainController.getMenuFactory() != null) {
                    mainController.getMenuFactory().updateUndoRedoState(
                            coachTools.canUndo(),
                            coachTools.canRedo()
                    );
                }
            } else {
                log.debug("Ctrl+Y: cannot redo (coachTools={}, canRedo={})",
                        coachTools != null ? "exists" : "null",
                        coachTools != null && coachTools.canRedo());
            }
        });
    }

    private void setupGlobalMouseHandlers(Scene scene) {
        // Глобальный обработчик для drag
        scene.setOnMouseDragged(event -> {
            if (coachTools != null && coachTools.getCurrentTool() == ToolType.ARROW) {
                if (event.isPrimaryButtonDown()) {
                    String squareName = getSquareAt(event.getSceneX(), event.getSceneY());
                    if (squareName != null && coachTools.getPendingArrowStart() != null &&
                            !coachTools.getPendingArrowStart().equals(squareName)) {
                        coachTools.updateArrowDrag(squareName);
                        if (markerOverlay != null) {
                            markerOverlay.redraw();
                        }
                        event.consume();
                    }
                }
            }
        });

        scene.setOnMouseReleased(event -> {
            if (coachTools != null && coachTools.getCurrentTool() == ToolType.ARROW) {
                // НЕ ПРОВЕРЯЕМ isPrimaryButtonDown() - при отпускании оно false
                String startSquare = coachTools.getPendingArrowStart();
                String endSquare = getSquareAt(event.getSceneX(), event.getSceneY());

                log.debug("Mouse released: start={}, end={}", startSquare, endSquare);

                if (startSquare != null && endSquare != null && !startSquare.equals(endSquare)) {
                    coachTools.createArrow(startSquare, endSquare);
                    log.debug("Arrow created: {} -> {}", startSquare, endSquare);
                    if (markerOverlay != null) {
                        markerOverlay.redraw();
                    }
                } else if (startSquare != null && startSquare.equals(endSquare)) {
                    // Клик по одной клетке - ничего не делаем или показываем точку
                    log.debug("Same square clicked, no arrow created");
                }

                // Очищаем состояние
                coachTools.cancelPendingArrow();
                coachTools.cancelArrowDrag();
                clearHighlight();
                if (markerOverlay != null) {
                    markerOverlay.redraw();
                }
                event.consume();
            }
        });

        // В setupGlobalMouseHandlers() добавим:
        scene.setOnMouseExited(event -> {
            if (coachTools != null && coachTools.getCurrentTool() == ToolType.ARROW) {
                if (coachTools.isDraggingArrow()) {
                    // Отменяем рисование если мышь вышла за пределы
                    coachTools.cancelPendingArrow();
                    coachTools.cancelArrowDrag();
                    clearHighlight();
                    if (markerOverlay != null) {
                        markerOverlay.redraw();
                    }
                    event.consume();
                }
            }
        });

        // Добавим обработчик клавиши Escape
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                if (coachTools != null && coachTools.getCurrentTool() == ToolType.ARROW) {
                    if (coachTools.isDraggingArrow()) {
                        coachTools.cancelPendingArrow();
                        coachTools.cancelArrowDrag();
                        clearHighlight();
                        if (markerOverlay != null) {
                            markerOverlay.redraw();
                        }
                        event.consume();
                    }
                }
            }
        });
    }

    /**
     * Получает клетку по координатам мыши
     */
    private String getSquareAt(double sceneX, double sceneY) {
        if (boardAndNav == null) return null;

        // Получаем координаты относительно boardAndNav
        javafx.geometry.Bounds bounds = boardAndNav.localToScene(boardAndNav.getBoundsInLocal());
        double localX = sceneX - bounds.getMinX();
        double localY = sceneY - bounds.getMinY();

        // Определяем размеры доски
        double boardWidth = bounds.getWidth();

        // Учитываем паддинг и координаты
        int padding = 12; // из createBoardWithCoordinates()
        double cellSize = (boardWidth - 2 * padding) / 8;

        int col = (int) ((localX - padding) / cellSize);
        int row = (int) ((localY - padding) / cellSize);

        if (col < 0 || col > 7 || row < 0 || row > 7) {
            return null;
        }

        // Учитываем переворот доски
        int chessRow = boardFlipped ? row : 7 - row;
        int chessCol = boardFlipped ? 7 - col : col;

        return convertToSquare(chessRow, chessCol).name();
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

        // ========== ДОБАВЛЯЕМ ОВЕРЛЕЙ ПОВЕРХ ВСЕГО ==========
        boardStack.getChildren().add(markerOverlay);
        StackPane.setAlignment(markerOverlay, Pos.TOP_LEFT);
        markerOverlay.prefWidthProperty().bind(boardAndNav.widthProperty());
        markerOverlay.prefHeightProperty().bind(boardAndNav.heightProperty());

        // ========== ВАЖНО: ПОДНИМАЕМ ОВЕРЛЕЙ НАВЕРХ ==========
        markerOverlay.toFront();

        anchorPane = new AnchorPane();
        anchorPane.getChildren().add(coachTools);
        AnchorPane.setLeftAnchor(coachTools, 0.0);
        AnchorPane.setTopAnchor(coachTools, 20.0);

        anchorPane.getChildren().add(boardStack);
        double leftOffset = coachTools.isPanelExpanded() ? coachTools.getWidth() + 20.0 : coachTools.getWidth() + 10.0;
        AnchorPane.setLeftAnchor(boardStack, leftOffset);
        AnchorPane.setTopAnchor(boardStack, 20.0);

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
            // Буквы (верх и низ)
            for (int row : new int[]{0, 9}) {
                for (int col = 0; col < 8; col++) {
                    String column = boardFlipped ? files[7 - col] : files[col];
                    Label label = createCoordinateLabel(column, tileSize, fontSize + 8, coordStyle);
                    boardGrid.add(label, col + 1, row);
                }
            }

            // Цифры (лево и право)
            for (int col : new int[]{0, 9}) {
                for (int row = 0; row < 8; row++) {
                    String rank = boardFlipped ? ranks[7 - row] : ranks[row];
                    Label label = createCoordinateLabel(rank, fontSize + 8, tileSize, coordStyle);
                    boardGrid.add(label, col, row + 1);
                }
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

    private Label createCoordinateLabel(String text, double width, double height, String style) {
        Label label = new Label(text);
        label.setAlignment(Pos.CENTER);
        label.setPrefSize(width, height);
        label.setStyle(style);
        return label;
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

        // ========== ОБРАБОТЧИКИ ДЛЯ СТРЕЛОК ЧЕРЕЗ MOUSE EVENTS ==========

        // MousePressed - старт рисования стрелки
        tile.setOnMousePressed(e -> {
            if (coachTools != null && coachTools.getCurrentTool() == ToolType.ARROW) {
                // Если это левая кнопка мыши
                if (e.isPrimaryButtonDown()) {
                    String squareName = square.name();
                    coachTools.startArrowDrag(squareName);
                    // Сохраняем начальную клетку
                    coachTools.setPendingArrowStart(squareName);
                    // Подсвечиваем начальную клетку
                    highlightSquare(squareName, Color.YELLOW);
                    e.consume();
                }
            }
        });

        // MouseDragged - рисуем временную стрелку
        tile.setOnMousePressed(e -> {
            if (coachTools != null && coachTools.getCurrentTool() == ToolType.ARROW) {
                if (e.isPrimaryButtonDown()) {
                    String squareName = square.name();
                    log.debug("Arrow: mouse pressed on {}", squareName);
                    coachTools.startArrowDrag(squareName);
                    coachTools.setPendingArrowStart(squareName);
                    highlightSquare(squareName, Color.YELLOW);

                    // ========== ПРИНУДИТЕЛЬНО ОБНОВЛЯЕМ ==========
                    if (markerOverlay != null) {
                        markerOverlay.redraw();
                    }
                    e.consume();
                }
            }
        });

        // MouseReleased - фиксируем стрелку
        tile.setOnMouseReleased(e -> {
            if (coachTools != null && coachTools.getCurrentTool() == ToolType.ARROW) {
                if (e.isPrimaryButtonDown()) {
                    String startSquare = coachTools.getPendingArrowStart();
                    String endSquare = square.name();

                    if (startSquare != null && !startSquare.equals(endSquare)) {
                        // Создаем стрелку
                        coachTools.createArrow(startSquare, endSquare);
                        log.debug("Arrow created via mouse release: {} -> {}", startSquare, endSquare);
                    }

                    // Очищаем состояние
                    coachTools.cancelPendingArrow();
                    coachTools.cancelArrowDrag();
                    clearHighlight();
                    if (markerOverlay != null) {
                        markerOverlay.redraw();
                    }
                    e.consume();
                }
            }
        });

        // ========== СУЩЕСТВУЮЩИЕ ОБРАБОТЧИКИ DRAG-AND-DROP ДЛЯ ФИГУР ==========

        tile.setOnMouseClicked(e -> handleTileClick(square));

        tile.setOnDragDetected(e -> {
            // Если активен инструмент ARROW - не даем перетаскивать фигуры
            if (coachTools != null && coachTools.getCurrentTool() == ToolType.ARROW) {
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
            // Если активен инструмент ARROW - игнорируем
            if (coachTools != null && coachTools.getCurrentTool() == ToolType.ARROW) {
                return;
            }

            if (e.getDragboard().hasString()) {
                e.acceptTransferModes(TransferMode.MOVE);
                e.consume();
            }
        });

        tile.setOnDragDropped(e -> {
            // Если активен инструмент ARROW - игнорируем
            if (coachTools != null && coachTools.getCurrentTool() == ToolType.ARROW) {
                return;
            }

            Dragboard db = e.getDragboard();
            if (db.hasString()) {
                Square fromSquare = Square.valueOf(db.getString());
                List<Move> legalMoves = chessBoard.legalMoves().stream()
                        .filter(m -> m.getFrom() == fromSquare && m.getTo() == square)
                        .toList();

                if (!legalMoves.isEmpty()) {
                    Move move = legalMoves.get(0);
                    Piece movingPiece = chessBoard.getPiece(move.getFrom());
                    boolean isCapture = chessBoard.getPiece(move.getTo()) != Piece.NONE;

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
            e.consume();
        });

        tile.setOnDragDone(e -> {
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
        }

        if (isTerminalPosition()) {
            log.debug("Terminal position, no moves possible");
            return;
        }

        if (isPositionIlLegal()) {
            log.debug("Illegal position, moves not allowed");
            showTemporaryMessage(lang.get(ENGINE_ILLEGAL_POSITION_CONTENT));
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

        if (isPositionIlLegal()) {
            showTemporaryMessage(lang.get(ENGINE_ILLEGAL_POSITION_CONTENT));
            return;
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

        // Уведомляем панель анализа о смене позиции
        if (analysisPanel != null) {
            analysisPanel.onPositionChanged();
        }
    }

    /**
     * Проверяет, является ли позиция легальной
     */
    public boolean isPositionIlLegal() {
        if (chessBoard == null) return true;

        // 1. Проверяем наличие королей (ровно по одному каждого цвета)
        int whiteKingCount = 0;
        int blackKingCount = 0;
        Square whiteKingSquare = null;
        Square blackKingSquare = null;

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                Square square = Square.squareAt(rank * 8 + file);
                Piece piece = chessBoard.getPiece(square);
                if (piece == Piece.WHITE_KING) {
                    whiteKingCount++;
                    whiteKingSquare = square;
                }
                if (piece == Piece.BLACK_KING) {
                    blackKingCount++;
                    blackKingSquare = square;
                }
            }
        }

        // ========== ПРОВЕРКА: РОВНО ПО ОДНОМУ КОРОЛЮ ==========
        if (whiteKingCount != 1 || blackKingCount != 1) {
            log.debug("Position illegal: incorrect king count - white={}, black={}",
                    whiteKingCount, blackKingCount);
            return true;
        }

        // 2. Проверяем, что короли не рядом
        if (whiteKingSquare != null && blackKingSquare != null) {
            int fileDiff = Math.abs(whiteKingSquare.getFile().ordinal() - blackKingSquare.getFile().ordinal());
            int rankDiff = Math.abs(whiteKingSquare.getRank().ordinal() - blackKingSquare.getRank().ordinal());
            if (fileDiff <= 1 && rankDiff <= 1) {
                log.debug("Position illegal: kings are adjacent");
                return true;
            }
        }

        // 3. Проверяем пешки на первой и последней линиях
        for (int file = 0; file < 8; file++) {
            Square rank1 = Square.squareAt(file);  // 1-я линия (rank 0)
            Square rank8 = Square.squareAt(7 * 8 + file);  // 8-я линия (rank 7)

            Piece piece1 = chessBoard.getPiece(rank1);
            Piece piece8 = chessBoard.getPiece(rank8);

            // Белые пешки не могут быть на 8-й линии
            if (piece1 == Piece.WHITE_PAWN || piece8 == Piece.WHITE_PAWN) {
                log.debug("Position illegal: white pawn on 1st or 8th rank");
                return true;
            }
            // Черные пешки не могут быть на 1-й линии
            if (piece1 == Piece.BLACK_PAWN || piece8 == Piece.BLACK_PAWN) {
                log.debug("Position illegal: black pawn on 1st or 8th rank");
                return true;
            }
        }

        // 4. Проверяем шахи с учетом стороны хода
        try {
            Side sideToMove = chessBoard.getSideToMove();
            int whiteAttackers = 0;
            int blackAttackers = 0;

            if (whiteKingSquare != null) {
                whiteAttackers = countAttackersViaReflection(chessBoard, whiteKingSquare, Side.BLACK);
            }

            if (blackKingSquare != null) {
                blackAttackers = countAttackersViaReflection(chessBoard, blackKingSquare, Side.WHITE);
            }

            // Если оба короля под шахом - позиция нелегальна
            if (whiteAttackers > 0 && blackAttackers > 0) {
                log.debug("Position illegal: both kings are in check");
                return true;
            }

            // Проверка на тройной шах
            if (whiteAttackers > 2 || blackAttackers > 2) {
                log.debug("Position illegal: triple+ check detected! whiteAttackers={}, blackAttackers={}",
                        whiteAttackers, blackAttackers);
                return true;
            }

            // Проверка с учетом стороны хода
            if (sideToMove == Side.WHITE) {
                if (blackAttackers > 0) {
                    log.debug("Position illegal: white to move, but black king is in check (attackers={})",
                            blackAttackers);
                    return true;
                }
            } else {
                if (whiteAttackers > 0) {
                    log.debug("Position illegal: black to move, but white king is in check (attackers={})",
                            whiteAttackers);
                    return true;
                }
            }
        } catch (Exception e) {
            log.debug("Error checking king attack: {}", e.getMessage());
        }

        return false;
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

        // ========== ПРОВЕРКА РЕЖИМА КНИГИ ==========
        if (navController != null && navController.getNavigationMode() == NavigationMode.BOOK) {
            showTemporaryMessage(lang.get(ENGINE_BOOK_MODE_NO_MOVE));
            return;
        }

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

            // Пытаемся добавить ход обычным способом
            Boolean addResult = navController.addMove(move, movingPiece, isCapture, promotionPiece);

            // Если addMove вернул null (требуется диалог) - принудительно создаем вариант
            if (addResult == null) {
                log.debug("Engine move requires dialog - forcing new variation creation");

                // Получаем текущие вариацию и узел
                Variation currentVar = navController.getCurrentVariation();
                ParentNode currentNode = navController.getCurrentNode();

                // Используем новый метод для принудительного добавления
                VariationStateSnapshot snapshot = navController.getVariationManager()
                        .forceAddMoveAsNewVariation(move, movingPiece, isCapture, promotionPiece,
                                currentVar, currentNode);

                // Применяем снимок состояния
                navController.applySnapshot(snapshot);
            }

            mainController.updateCurrentGameData();
            navController.restoreBoardFromCurrentNode();

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
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PGN files", "*.pgn")
        );

        String lastDir = AppPreferences.getLastSaveDirectory();
        if (lastDir != null && !lastDir.isEmpty()) {
            File dir = new File(lastDir);
            if (dir.exists() && dir.isDirectory()) {
                fileChooser.setInitialDirectory(dir);
            }
        } else {
            String saveDir = AppPreferences.getSaveDirectory();
            if (saveDir != null && !saveDir.isEmpty()) {
                File dir = new File(saveDir);
                if (dir.exists() && dir.isDirectory()) {
                    fileChooser.setInitialDirectory(dir);
                }
            }
        }

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
     * Обновляет индикатор режима навигации
     */
    public void updateNavigationModeIndicator(NavigationMode mode) {
        if (navigationModeIndicator == null) {
            createNavigationModeIndicator();
        }

        Platform.runLater(() -> {
            String text;
            String style;

            if (mode == NavigationMode.BOOK) {
                text = lang.get(LanguageKeys.BOOK_MODE); // "📖 Книга" / "📖 Book" / "📖 书籍"
                style = "-fx-text-fill: #8b5a2b; -fx-font-weight: bold; -fx-font-size: 13px;";
            } else {
                text = "♟ " + lang.get(LanguageKeys.NAVIGATION_MODE_PGN); // "♟ PGN"
                style = "-fx-text-fill: #2c3e50; -fx-font-weight: normal; -fx-font-size: 13px;";
            }

            navigationModeIndicator.setText(text);
            navigationModeIndicator.setStyle(style);
        });
    }

    /**
     * Создает индикатор режима навигации
     */
    private void createNavigationModeIndicator() {
        navigationModeIndicator = new Label("♟ " + lang.get(LanguageKeys.NAVIGATION_MODE_PGN));
        navigationModeIndicator.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        navigationModeIndicator.setPadding(new Insets(5, 10, 5, 10));
        navigationModeIndicator.setStyle("-fx-background-color: rgba(255,255,255,0.8); " +
                "-fx-background-radius: 5px; " +
                "-fx-border-color: #d2b48c; " +
                "-fx-border-radius: 5px;");

        // Добавляем в правый верхний угол доски
        if (anchorPane != null) {
            anchorPane.getChildren().add(navigationModeIndicator);
            AnchorPane.setRightAnchor(navigationModeIndicator, 20.0);
            AnchorPane.setTopAnchor(navigationModeIndicator, 60.0);
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