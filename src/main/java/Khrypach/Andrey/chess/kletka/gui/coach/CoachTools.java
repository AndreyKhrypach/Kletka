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

package Khrypach.Andrey.chess.kletka.gui.coach;

import Khrypach.Andrey.chess.kletka.gui.board.ChessBoardView;
import Khrypach.Andrey.chess.kletka.gui.coach.tools.ArrowData;
import Khrypach.Andrey.chess.kletka.gui.coach.tools.CrossData;
import Khrypach.Andrey.chess.kletka.gui.coach.tools.MarkerColor;
import Khrypach.Andrey.chess.kletka.gui.coach.tools.ToolType;
import Khrypach.Andrey.chess.kletka.gui.menu.CustomMenuBarFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Инструменты тренера - панель для рисования маркеров на доске
 */
public class CoachTools extends VBox {

    private static final Logger log = LoggerFactory.getLogger(CoachTools.class);

    private final String buttonStyle = "-fx-background-color: #A0522D; -fx-text-fill: white; " +
            "-fx-border-color: #5C4033; -fx-border-width: 2; " +
            "-fx-border-radius: 8; -fx-background-radius: 8;";

    private final String buttonSelectedStyle = "-fx-background-color: #D2691E; -fx-text-fill: yellow; " +
            "-fx-border-color: #FFD700; -fx-border-width: 3; " +
            "-fx-border-radius: 8; -fx-background-radius: 8;";

    // ========== СТИЛЬ ДЛЯ АКТИВНОГО ЛАСТИКА (красный) ==========
    private final String eraseActiveStyle = "-fx-background-color: #cc0000; -fx-text-fill: white; " +
            "-fx-border-color: #FFD700; -fx-border-width: 3; " +
            "-fx-border-radius: 8; -fx-background-radius: 8;";

    @Getter
    @Setter
    private ChessBoardView boardView;
    // Кнопки
    private ToggleButton pencilButton;
    private ToggleButton arrowButton;
    private ToggleButton crossButton;
    private Button blueColorButton;
    private Button redColorButton;
    private Button greenColorButton;
    private Button blackColorButton;
    private Button eraseButton;

    @Getter
    private ToolType currentTool = ToolType.NONE;
    @Getter
    private MarkerColor currentColor = MarkerColor.BLUE;

    @Getter
    private boolean panelExpanded = false;

    // Хранилища маркеров
    private final Map<String, CrossData> crosses = new HashMap<>();
    private final Map<String, ArrowData> arrows = new HashMap<>();

    // ========== ИСТОРИЯ ДЕЙСТВИЙ ДЛЯ ОТМЕНЫ ==========
    private final java.util.Stack<MarkerAction> actionHistory = new java.util.Stack<>();
    // Стек для Redo (восстановление удаленных маркеров)
    private final java.util.Stack<MarkerAction> redoHistory = new java.util.Stack<>();

    // Callback для уведомления о необходимости перерисовки
    @Setter
    private Runnable onMarkersChanged;

    // Временная стрелка (при перетаскивании)
    @Getter
    private ArrowData tempArrow;
    @Getter
    private boolean isDraggingArrow = false;
    private String dragStartSquare;
    private String lastAddedSquare = "";
    private long lastAddTime = 0;
    @Getter
    @Setter
    private String pendingArrowStart = null;

    public CoachTools() {
        setStyle("-fx-background-color: #8B6914; -fx-border-color: #5C4033; -fx-border-width: 0 3 0 0;");
        setPrefWidth(60);
        setMinWidth(60);
        setMaxWidth(60);
        setPadding(new Insets(10, 5, 10, 5));
        setSpacing(10);
        setAlignment(Pos.TOP_CENTER);

        setEffect(new javafx.scene.effect.DropShadow(5, Color.rgb(0, 0, 0, 0.3)));

        initializeButtons();
        layoutButtons();
    }

    private void initializeButtons() {
        ToggleGroup toolGroup = new ToggleGroup();

        pencilButton = new ToggleButton();
        Image pencilImage = loadImage("/images/coach/pencil.png");
        if (pencilImage != null) {
            pencilButton.setGraphic(new ImageView(pencilImage));
        } else {
            pencilButton.setText("✏️");
            pencilButton.setFont(Font.font(20));
        }
        pencilButton.setStyle(buttonStyle);
        pencilButton.setPrefWidth(50);
        pencilButton.setPrefHeight(50);
        pencilButton.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                pencilButton.setStyle(buttonSelectedStyle);
            } else {
                pencilButton.setStyle(buttonStyle);
            }
        });
        pencilButton.setOnAction(e -> togglePanel());

        arrowButton = new ToggleButton();
        Image arrowImage = loadImage("/images/coach/arrow.png");
        if (arrowImage != null) {
            arrowButton.setGraphic(new ImageView(arrowImage));
        } else {
            arrowButton.setText("→");
            arrowButton.setFont(Font.font(20));
        }
        arrowButton.setToggleGroup(toolGroup);
        arrowButton.setStyle(buttonStyle);
        arrowButton.setPrefWidth(50);
        arrowButton.setPrefHeight(50);
        arrowButton.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                arrowButton.setStyle(buttonSelectedStyle);
                currentTool = ToolType.ARROW;
                crossButton.setSelected(false);
            } else {
                arrowButton.setStyle(buttonStyle);
                if (currentTool == ToolType.ARROW) currentTool = ToolType.NONE;
            }
        });

        crossButton = new ToggleButton();
        Image crossImage = loadImage("/images/coach/cross.png");
        if (crossImage != null) {
            crossButton.setGraphic(new ImageView(crossImage));
        } else {
            Text xText = new Text("X");
            xText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
            xText.setFill(Color.RED);
            crossButton.setGraphic(xText);
        }
        crossButton.setToggleGroup(toolGroup);
        crossButton.setStyle(buttonStyle);
        crossButton.setPrefWidth(50);
        crossButton.setPrefHeight(50);
        crossButton.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                crossButton.setStyle(buttonSelectedStyle);
                currentTool = ToolType.CROSS;
                arrowButton.setSelected(false);
            } else {
                crossButton.setStyle(buttonStyle);
                if (currentTool == ToolType.CROSS) currentTool = ToolType.NONE;
            }
        });

        blueColorButton = createColorButton(MarkerColor.BLUE);
        redColorButton = createColorButton(MarkerColor.RED);
        greenColorButton = createColorButton(MarkerColor.GREEN);
        blackColorButton = createColorButton(MarkerColor.BLACK);

        blueColorButton.setStyle("-fx-background-color: #0066ff; -fx-border-radius: 25; -fx-background-radius: 25;" +
                "-fx-border-color: #FFD700; -fx-border-width: 0;");
        redColorButton.setStyle("-fx-background-color: #ff3333; -fx-border-radius: 25; -fx-background-radius: 25;" +
                "-fx-border-color: #FFD700; -fx-border-width: 0;");
        greenColorButton.setStyle("-fx-background-color: #32cd32; -fx-border-radius: 25; -fx-background-radius: 25;" +
                "-fx-border-color: #FFD700; -fx-border-width: 0;");
        blackColorButton.setStyle("-fx-background-color: #000000; -fx-border-radius: 25; -fx-background-radius: 25;" +
                "-fx-border-color: #FFD700; -fx-border-width: 0;");

        highlightColorButton(blueColorButton, true);

        blueColorButton.setOnAction(e -> {
            currentColor = MarkerColor.BLUE;
            highlightColorButton(blueColorButton, true);
            highlightColorButton(redColorButton, false);
            highlightColorButton(greenColorButton, false);
            highlightColorButton(blackColorButton, false);
        });

        redColorButton.setOnAction(e -> {
            currentColor = MarkerColor.RED;
            highlightColorButton(blueColorButton, false);
            highlightColorButton(redColorButton, true);
            highlightColorButton(greenColorButton, false);
            highlightColorButton(blackColorButton, false);
        });

        greenColorButton.setOnAction(e -> {
            currentColor = MarkerColor.GREEN;
            highlightColorButton(blueColorButton, false);
            highlightColorButton(redColorButton, false);
            highlightColorButton(greenColorButton, true);
            highlightColorButton(blackColorButton, false);
        });

        blackColorButton.setOnAction(e -> {
            currentColor = MarkerColor.BLACK;
            highlightColorButton(blueColorButton, false);
            highlightColorButton(redColorButton, false);
            highlightColorButton(greenColorButton, false);
            highlightColorButton(blackColorButton, true);
        });

        // ========== ЛАСТИК - ОТМЕНА ПОСЛЕДНЕГО ДЕЙСТВИЯ ==========
        eraseButton = new Button();
        Image eraseImage = loadImage("/images/coach/eraser.png");
        if (eraseImage != null) {
            eraseButton.setGraphic(new ImageView(eraseImage));
        } else {
            eraseButton.setText("🧽");
            eraseButton.setFont(Font.font(20));
        }
        eraseButton.setStyle(buttonStyle);
        eraseButton.setPrefWidth(50);
        eraseButton.setPrefHeight(50);
        eraseButton.setOnAction(e -> undoLastAction());
        // Добавляем тултип
        javafx.scene.control.Tooltip.install(eraseButton,
                new javafx.scene.control.Tooltip("Отменить последний маркер"));

        // Синхронизация с текущим инструментом
        eraseButton.setOnMousePressed(e -> {
            // Небольшая визуальная обратная связь
            eraseButton.setStyle(eraseActiveStyle);
        });
        eraseButton.setOnMouseReleased(e -> {
            // Возвращаем стиль после клика
            if (actionHistory.isEmpty()) {
                eraseButton.setStyle(buttonStyle);
            }
        });
    }

    private Button createColorButton(MarkerColor color) {
        Button btn = new Button();
        btn.setPrefWidth(40);
        btn.setPrefHeight(40);
        btn.setMinWidth(40);
        btn.setMinHeight(40);

        Rectangle circle = new Rectangle(30, 30);
        circle.setArcWidth(30);
        circle.setArcHeight(30);
        circle.setFill(color.getColor());
        circle.setStroke(Color.WHITE);
        circle.setStrokeWidth(2);

        btn.setGraphic(circle);
        btn.setStyle("-fx-background-color: transparent;");
        return btn;
    }

    private void highlightColorButton(Button button, boolean highlight) {
        if (highlight) {
            button.setStyle(button.getStyle().replace("-fx-border-width: 0;", "-fx-border-width: 3;"));
            button.setStyle(button.getStyle() + "-fx-border-color: #FFD700;");
        } else {
            button.setStyle(button.getStyle().replace("-fx-border-width: 3;", "-fx-border-width: 0;"));
            button.setStyle(button.getStyle().replace("-fx-border-color: #FFD700;", ""));
        }
    }

    private void layoutButtons() {
        getChildren().clear();
        getChildren().add(pencilButton);

        if (panelExpanded) {
            getChildren().addAll(arrowButton, crossButton);

            javafx.scene.control.Separator sep = new javafx.scene.control.Separator();
            sep.setStyle("-fx-background-color: #1a252f;");
            getChildren().add(sep);

            getChildren().addAll(blueColorButton, redColorButton, greenColorButton, blackColorButton);

            javafx.scene.control.Separator sep2 = new javafx.scene.control.Separator();
            sep2.setStyle("-fx-background-color: #1a252f;");
            getChildren().add(sep2);

            getChildren().add(eraseButton);

            setPrefWidth(80);
            setMinWidth(80);
            setMaxWidth(80);
        } else {
            setPrefWidth(60);
            setMinWidth(60);
            setMaxWidth(60);
        }
    }

    public void togglePanel() {
        panelExpanded = !panelExpanded;

        pencilButton.setSelected(panelExpanded);

        if (panelExpanded) {
            setPrefWidth(80);
            setMinWidth(80);
            setMaxWidth(80);
            pencilButton.setStyle(buttonSelectedStyle);
        } else {
            setPrefWidth(60);
            setMinWidth(60);
            setMaxWidth(60);
            clearAllMarkers();
            if (arrowButton != null) {
                arrowButton.setSelected(false);
                arrowButton.setStyle(buttonStyle);
            }
            if (crossButton != null) {
                crossButton.setSelected(false);
                crossButton.setStyle(buttonStyle);
            }
            currentTool = ToolType.NONE;
            pencilButton.setStyle(buttonStyle);
        }

        layoutButtons();

        if (onMarkersChanged != null) {
            onMarkersChanged.run();
        }
    }

    // ========== Управление маркерами ==========

    public void addCross(String square) {
        long now = System.currentTimeMillis();
        if (square.equals(lastAddedSquare) && (now - lastAddTime) < 200) {
            log.trace("Duplicate cross click ignored: {}", square);
            return;
        }
        lastAddedSquare = square;
        lastAddTime = now;

        CrossData existing = crosses.get(square);
        if (existing != null) {
            if (existing.getColor() == currentColor) {
                log.trace("Cross already exists with same color, skipping: {}", square);
                return;
            }
            // Сохраняем действие в историю (обновление цвета)
            pushAction(MarkerAction.updateCross(existing, square, existing.getColor(), currentColor));
            existing.setColor(currentColor);
            if (boardView != null) {
                boardView.updateCrossColor(square, currentColor.getColor());
            }
        } else {
            // Сохраняем действие в историю (создание)
            CrossData newCross = new CrossData(square, currentColor);
            pushAction(MarkerAction.createCross(newCross));
            crosses.put(square, newCross);
            if (boardView != null) {
                boardView.addCrossToSquare(square, currentColor.getColor());
            }
        }
        notifyMarkersChanged();
        updateEraseButtonState();
    }

    public void startArrowDrag(String fromSquare) {
        log.trace("startArrowDrag: fromSquare={}, currentTool={}", fromSquare, currentTool);
        if (currentTool == ToolType.ARROW) {
            isDraggingArrow = true;
            dragStartSquare = fromSquare;
            pendingArrowStart = fromSquare;
            // ========== СОЗДАЕМ ВРЕМЕННУЮ СТРЕЛКУ СРАЗУ ==========
            // Показываем точку старта как маленькую стрелку (саму в себя)
            tempArrow = new ArrowData(fromSquare, fromSquare, currentColor);
            notifyMarkersChanged();
            log.trace("Arrow drag started at: {}", fromSquare);
        } else {
            log.trace("Arrow drag ignored - wrong tool: {}", currentTool);
        }
    }

    public void updateArrowDrag(String toSquare) {
        log.trace("updateArrowDrag: toSquare={}, dragging={}", toSquare, isDraggingArrow);
        if (isDraggingArrow && dragStartSquare != null && !dragStartSquare.equals(toSquare)) {
            tempArrow = new ArrowData(dragStartSquare, toSquare, currentColor);
            notifyMarkersChanged();
            log.trace("Temp arrow updated: {} -> {}", dragStartSquare, toSquare);
        } else {
            log.trace("updateArrowDrag: conditions not met - isDraggingArrow={}, dragStartSquare={}",
                    isDraggingArrow, dragStartSquare);
        }
    }

    public void cancelArrowDrag() {
        isDraggingArrow = false;
        dragStartSquare = null;
        pendingArrowStart = null;
        tempArrow = null;
        notifyMarkersChanged();
    }

    public void clearAllMarkers() {
        crosses.clear();
        arrows.clear();
        actionHistory.clear();
        // ========== ОЧИЩАЕМ REDO СТЕК ==========
        redoHistory.clear();
        tempArrow = null;
        isDraggingArrow = false;
        dragStartSquare = null;
        cancelPendingArrow();

        if (boardView != null) {
            boardView.clearAllCrosses();
        }
        notifyMarkersChanged();
        updateEraseButtonState();
        updateMenuState();
    }

    public Map<String, ArrowData> getArrows() {
        return new HashMap<>(arrows);
    }

    private void notifyMarkersChanged() {
        if (onMarkersChanged != null) {
            onMarkersChanged.run();
        }
    }

    private Image loadImage(String path) {
        try {
            return new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
        } catch (Exception e) {
            return null;
        }
    }

    public void createArrow(String fromSquare, String toSquare) {
        if (fromSquare == null || toSquare == null || fromSquare.equals(toSquare)) return;

        String key = fromSquare + "->" + toSquare;
        ArrowData existing = arrows.get(key);
        if (existing != null) {
            // Сохраняем действие в историю (обновление цвета)
            pushAction(MarkerAction.updateArrow(existing, fromSquare, toSquare, existing.getColor(), currentColor));
            existing.setColor(currentColor);
            log.trace("Updated existing arrow: {}", key);
        } else {
            // Сохраняем действие в историю (создание)
            ArrowData newArrow = new ArrowData(fromSquare, toSquare, currentColor);
            pushAction(MarkerAction.createArrow(newArrow));
            arrows.put(key, newArrow);
            log.trace("Created new arrow: {}", key);
        }
        // ========== ВАЖНО: ЯВНО ВЫЗЫВАЕМ ПЕРЕРИСОВКУ ==========
        notifyMarkersChanged();
        updateEraseButtonState();
        log.trace("notifyMarkersChanged called, arrows size: {}", arrows.size());
    }

    public void cancelPendingArrow() {
        pendingArrowStart = null;
        if (boardView != null) {
            boardView.clearHighlight();
        }
    }

    // ========== ИСТОРИЯ ДЕЙСТВИЙ И ОТМЕНА ==========

    /**
     * Сохраняет действие в историю
     */
    private void pushAction(MarkerAction action) {
        actionHistory.push(action);
        redoHistory.clear();
        log.trace("Action pushed: {}, history size: {}", action.getType(), actionHistory.size());
        updateMenuState();
    }

    /**
     * Отменяет последнее действие (ластик)
     */
    public void undoLastAction() {
        if (actionHistory.isEmpty()) {
            log.trace("No actions to undo");
            flashEraseButton();
            return;
        }

        MarkerAction action = actionHistory.pop();
        // ========== СОХРАНЯЕМ В REDO СТЕК ДЛЯ ВОССТАНОВЛЕНИЯ ==========
        redoHistory.push(action);
        log.trace("Undo action: {}, moved to redo stack", action.getType());

        // Отменяем действие
        undoAction(action);

        notifyMarkersChanged();
        updateEraseButtonState();
        updateMenuState();
    }

    /**
     * Отменяет действие
     */
    private void undoAction(MarkerAction action) {
        switch (action.getType()) {
            case CREATE_CROSS -> {
                CrossData cross = action.getCrossData();
                crosses.remove(cross.getSquare());
                if (boardView != null) {
                    boardView.removeCrossFromSquare(cross.getSquare());
                }
                log.trace("Undo: removed cross at {}", cross.getSquare());
            }
            case UPDATE_CROSS -> {
                CrossData cross = crosses.get(action.getSquare());
                if (cross != null) {
                    cross.setColor(action.getOldColor());
                    if (boardView != null) {
                        boardView.updateCrossColor(action.getSquare(), action.getOldColor().getColor());
                    }
                    log.trace("Undo: restored cross color at {}", action.getSquare());
                }
            }
            case CREATE_ARROW -> {
                ArrowData arrow = action.getArrowData();
                String key = arrow.getFromSquare() + "->" + arrow.getToSquare();
                arrows.remove(key);
                log.trace("Undo: removed arrow from {} to {}", arrow.getFromSquare(), arrow.getToSquare());
            }
            case UPDATE_ARROW -> {
                String key = action.getFromSquare() + "->" + action.getToSquare();
                ArrowData arrow = arrows.get(key);
                if (arrow != null) {
                    arrow.setColor(action.getOldColor());
                    log.trace("Undo: restored arrow color from {} to {}", action.getFromSquare(), action.getToSquare());
                }
            }
        }
    }

    /**
     * Обновляет состояние кнопок Undo/Redo в меню
     */
    private void updateMenuState() {
        if (boardView != null && boardView.getMainController() != null) {
            CustomMenuBarFactory menuFactory = boardView.getMainController().getMenuFactory();
            if (menuFactory != null) {
                menuFactory.updateUndoRedoState(canUndo(), canRedo());
            }
        }
    }

    // ========== МЕТОДЫ ДЛЯ СОСТОЯНИЯ КНОПОК ==========
    public boolean canUndo() {
        return !actionHistory.isEmpty();
    }

    public boolean canRedo() {
        return !redoHistory.isEmpty();
    }

    /**
     * Обновляет состояние кнопки ластика
     */
    private void updateEraseButtonState() {
        if (eraseButton != null) {
            if (actionHistory.isEmpty()) {
                eraseButton.setStyle(buttonStyle);
                eraseButton.setTooltip(new javafx.scene.control.Tooltip("Нет действий для отмены"));
            } else {
                eraseButton.setStyle(buttonStyle);
                eraseButton.setTooltip(new javafx.scene.control.Tooltip(
                        "Отменить последний маркер (" + actionHistory.size() + ")" ));
            }
        }
    }

    /**
     * Визуальная обратная связь при пустой истории
     */
    private void flashEraseButton() {
        if (eraseButton == null) return;
        // Моргаем красным
        eraseButton.setStyle(eraseActiveStyle);
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                javafx.util.Duration.millis(300)
        );
        pause.setOnFinished(e -> {
            eraseButton.setStyle(buttonStyle);
            updateEraseButtonState();
        });
        pause.play();
    }

    public void redo() {
        if (redoHistory.isEmpty()) {
            log.trace("No actions to redo");
            return;
        }

        MarkerAction action = redoHistory.pop();
        log.trace("Redo action: {}", action.getType());

        // ========== ВАЖНО: возвращаем действие в actionHistory ==========
        actionHistory.push(action);
        log.trace("Action returned to undo stack, history size: {}", actionHistory.size());

        // Восстанавливаем действие на доске
        redoAction(action);

        // Обновляем состояние
        updateEraseButtonState();
        updateMenuState();
        notifyMarkersChanged();
    }

    public int getActionHistorySize() {
        return actionHistory.size();
    }

    public int getRedoHistorySize() {
        return redoHistory.size();
    }

    /**
     * Восстанавливает действие из Redo стека
     */
    private void redoAction(MarkerAction action) {
        switch (action.getType()) {
            case CREATE_CROSS -> {
                CrossData cross = action.getCrossData();
                if (cross != null) {
                    crosses.put(cross.getSquare(), cross);
                    if (boardView != null) {
                        boardView.addCrossToSquare(cross.getSquare(), cross.getColor().getColor());
                    }
                    log.trace("Redo: restored cross at {}", cross.getSquare());
                }
            }
            case UPDATE_CROSS -> {
                CrossData cross = crosses.get(action.getSquare());
                if (cross != null) {
                    cross.setColor(action.getNewColor());
                    if (boardView != null) {
                        boardView.updateCrossColor(action.getSquare(), action.getNewColor().getColor());
                    }
                    log.trace("Redo: restored cross color at {}", action.getSquare());
                }
            }
            case CREATE_ARROW -> {
                ArrowData arrow = action.getArrowData();
                if (arrow != null) {
                    String key = arrow.getFromSquare() + "->" + arrow.getToSquare();
                    arrows.put(key, arrow);
                    log.trace("Redo: restored arrow from {} to {}", arrow.getFromSquare(), arrow.getToSquare());
                }
            }
            case UPDATE_ARROW -> {
                String key = action.getFromSquare() + "->" + action.getToSquare();
                ArrowData arrow = arrows.get(key);
                if (arrow != null) {
                    arrow.setColor(action.getNewColor());
                    log.trace("Redo: restored arrow color from {} to {}", action.getFromSquare(), action.getToSquare());
                }
            }
        }
    }

    // ========== ВНУТРЕННИЙ КЛАСС ДЛЯ ИСТОРИИ ==========

    /**
     * Запись о действии для отмены
     */
    @Getter
    public static class MarkerAction {
        private final ActionType type;
        private final CrossData crossData;
        private final ArrowData arrowData;
        private final String square;
        private final String fromSquare;
        private final String toSquare;
        private final MarkerColor oldColor;
        private final MarkerColor newColor;

        private MarkerAction(ActionType type, CrossData crossData, ArrowData arrowData,
                             String square, String fromSquare, String toSquare,
                             MarkerColor oldColor, MarkerColor newColor) {
            this.type = type;
            this.crossData = crossData;
            this.arrowData = arrowData;
            this.square = square;
            this.fromSquare = fromSquare;
            this.toSquare = toSquare;
            this.oldColor = oldColor;
            this.newColor = newColor;
        }

        public static MarkerAction createCross(CrossData cross) {
            return new MarkerAction(ActionType.CREATE_CROSS, cross, null,
                    null, null, null, null, null);
        }

        public static MarkerAction updateCross(CrossData cross, String square,
                                               MarkerColor oldColor, MarkerColor newColor) {
            return new MarkerAction(ActionType.UPDATE_CROSS, cross, null,
                    square, null, null, oldColor, newColor);
        }

        public static MarkerAction createArrow(ArrowData arrow) {
            return new MarkerAction(ActionType.CREATE_ARROW, null, arrow,
                    null, null, null, null, null);
        }

        public static MarkerAction updateArrow(ArrowData arrow, String fromSquare, String toSquare,
                                               MarkerColor oldColor, MarkerColor newColor) {
            return new MarkerAction(ActionType.UPDATE_ARROW, null, arrow,
                    null, fromSquare, toSquare, oldColor, newColor);
        }

        public enum ActionType {
            CREATE_CROSS,
            UPDATE_CROSS,
            CREATE_ARROW,
            UPDATE_ARROW
        }
    }
}