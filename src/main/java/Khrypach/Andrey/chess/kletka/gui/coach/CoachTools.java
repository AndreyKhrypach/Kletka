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

        blueColorButton.setStyle("-fx-background-color: #0066ff; -fx-border-radius: 25; -fx-background-radius: 25;" +
                "-fx-border-color: #FFD700; -fx-border-width: 0;");
        redColorButton.setStyle("-fx-background-color: #ff3333; -fx-border-radius: 25; -fx-background-radius: 25;" +
                "-fx-border-color: #FFD700; -fx-border-width: 0;");
        greenColorButton.setStyle("-fx-background-color: #32cd32; -fx-border-radius: 25; -fx-background-radius: 25;" +
                "-fx-border-color: #FFD700; -fx-border-width: 0;");

        highlightColorButton(blueColorButton, true);

        blueColorButton.setOnAction(e -> {
            currentColor = MarkerColor.BLUE;
            highlightColorButton(blueColorButton, true);
            highlightColorButton(redColorButton, false);
            highlightColorButton(greenColorButton, false);
        });

        redColorButton.setOnAction(e -> {
            currentColor = MarkerColor.RED;
            highlightColorButton(blueColorButton, false);
            highlightColorButton(redColorButton, true);
            highlightColorButton(greenColorButton, false);
        });

        greenColorButton.setOnAction(e -> {
            currentColor = MarkerColor.GREEN;
            highlightColorButton(blueColorButton, false);
            highlightColorButton(redColorButton, false);
            highlightColorButton(greenColorButton, true);
        });

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
        eraseButton.setOnAction(e -> clearAllMarkers());
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

            getChildren().addAll(blueColorButton, redColorButton, greenColorButton);

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
            existing.setColor(currentColor);
            if (boardView != null) {
                boardView.updateCrossColor(square, currentColor.getColor());
            }
        } else {
            crosses.put(square, new CrossData(square, currentColor));
            if (boardView != null) {
                boardView.addCrossToSquare(square, currentColor.getColor());
            }
        }
        notifyMarkersChanged();
    }

    public void startArrowDrag(String fromSquare) {
        log.trace("startArrowDrag: fromSquare={}, currentTool={}", fromSquare, currentTool);
        if (currentTool == ToolType.ARROW) {
            isDraggingArrow = true;
            dragStartSquare = fromSquare;
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
        }
    }

    public void finishArrowDrag(String fromSquare, String toSquare) {
        log.trace("finishArrowDrag: {} -> {}", fromSquare, toSquare);
        if (isDraggingArrow && fromSquare != null && toSquare != null && !fromSquare.equals(toSquare)) {
            createArrow(fromSquare, toSquare);
        }
        isDraggingArrow = false;
        dragStartSquare = null;
        tempArrow = null;
    }

    public void cancelArrowDrag() {
        isDraggingArrow = false;
        dragStartSquare = null;
        tempArrow = null;
        notifyMarkersChanged();
    }

    public void clearAllMarkers() {
        crosses.clear();
        arrows.clear();
        tempArrow = null;
        isDraggingArrow = false;
        dragStartSquare = null;
        cancelPendingArrow();

        if (boardView != null) {
            boardView.clearAllCrosses();
        }
        notifyMarkersChanged();
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
            existing.setColor(currentColor);
            log.trace("Updated existing arrow: {}", key);
        } else {
            arrows.put(key, new ArrowData(fromSquare, toSquare, currentColor));
            log.trace("Created new arrow: {}", key);
        }
        notifyMarkersChanged();
        log.trace("notifyMarkersChanged called, arrows size: {}", arrows.size());
    }

    public void handleArrowClick(String square) {
        if (currentTool != ToolType.ARROW) return;

        if (pendingArrowStart == null) {
            pendingArrowStart = square;
            log.trace("Arrow start set at: {}", square);
            if (boardView != null) {
                boardView.highlightSquare(square, Color.YELLOW);
            }
        } else {
            if (!pendingArrowStart.equals(square)) {
                createArrow(pendingArrowStart, square);
                log.trace("Arrow created: {} -> {}", pendingArrowStart, square);
            }
            pendingArrowStart = null;
            if (boardView != null) {
                boardView.clearHighlight();
            }
        }
    }

    public void cancelPendingArrow() {
        pendingArrowStart = null;
        if (boardView != null) {
            boardView.clearHighlight();
        }
    }
}