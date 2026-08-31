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
import com.github.bhlangonijr.chesslib.Square;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Оверлей для рисования стрелок поверх шахматной доски
 */
public class MarkerOverlay extends Pane {

    private static final Logger log = LoggerFactory.getLogger(MarkerOverlay.class);

    private final Canvas canvas;
    private final CoachTools coachTools;
    @Setter
    private Pane boardContainer;

    // ========== КЭШ КООРДИНАТ КЛЕТОК ==========
    private final Map<String, Point2D> squareCenters = new HashMap<>();
    private double tileSize = 0;
    private boolean isInitialized = false;

    public MarkerOverlay(CoachTools coachTools) {
        this.coachTools = coachTools;
        this.canvas = new Canvas();
        setMouseTransparent(true);
        getChildren().add(canvas);
        coachTools.setOnMarkersChanged(this::redraw);
    }

    public void redraw() {
        boolean hasArrows = !coachTools.getArrows().isEmpty();
        boolean hasTempArrow = coachTools.getTempArrow() != null;

        if (boardContainer == null || (!hasArrows && !hasTempArrow)) {
            canvas.setVisible(false);
            return;
        }

        canvas.setVisible(true);

        double width = boardContainer.getWidth();
        double height = boardContainer.getHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        // ========== ИНИЦИАЛИЗИРУЕМ КООРДИНАТЫ, ЕСЛИ НУЖНО ==========
        ChessBoardView boardView = coachTools.getBoardView();
        if (!isInitialized || boardView.getTileSize() != tileSize) {
            initSquareCenters();
        }

        canvas.setWidth(width);
        canvas.setHeight(height);
        canvas.setLayoutX(0);
        canvas.setLayoutY(0);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        // Рисуем все сохраненные стрелки (используя кэшированные координаты)
        for (ArrowData arrow : coachTools.getArrows().values()) {
            drawArrowWithCache(gc, arrow.getFromSquare(), arrow.getToSquare(), arrow.getColor().getColor());
        }

        // Рисуем временную стрелку
        ArrowData tempArrow = coachTools.getTempArrow();
        if (tempArrow != null) {
            drawArrowWithCache(gc, tempArrow.getFromSquare(), tempArrow.getToSquare(),
                    tempArrow.getColor().getColor().brighter());
        }
    }

    private void drawArrowWithCache(GraphicsContext gc, String fromSquareName, String toSquareName, Color color) {
        javafx.geometry.Point2D fromCenter = squareCenters.get(fromSquareName);
        javafx.geometry.Point2D toCenter = squareCenters.get(toSquareName);

        if (fromCenter == null || toCenter == null) {
            // Если кэш пуст — используем fallback
            drawArrowFallback(gc, fromSquareName, toSquareName, color);
            return;
        }

        double startX = fromCenter.getX();
        double startY = fromCenter.getY();
        double endX = toCenter.getX();
        double endY = toCenter.getY();

        // ========== ОСТАЛЬНАЯ ЛОГИКА РИСОВАНИЯ (без изменений) ==========
        double angle = Math.atan2(endY - startY, endX - startX);
        double offset = 15;

        startX += Math.cos(angle) * offset;
        startY += Math.sin(angle) * offset;
        endX -= Math.cos(angle) * offset;
        endY -= Math.sin(angle) * offset;

        gc.setStroke(color);
        gc.setLineWidth(6.5);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.strokeLine(startX, startY, endX, endY);

        double wingAngle = Math.toRadians(40);
        double arrowLength = 24;

        double angle1 = angle + Math.PI - wingAngle;
        double angle2 = angle + Math.PI + wingAngle;

        double arrowX1 = endX + Math.cos(angle1) * arrowLength;
        double arrowY1 = endY + Math.sin(angle1) * arrowLength;
        double arrowX2 = endX + Math.cos(angle2) * arrowLength;
        double arrowY2 = endY + Math.sin(angle2) * arrowLength;

        gc.setFill(color);
        double[] xPoints = {endX, arrowX1, arrowX2};
        double[] yPoints = {endY, arrowY1, arrowY2};
        gc.fillPolygon(xPoints, yPoints, 3);
    }

    private void drawArrowFallback(GraphicsContext gc, String fromSquareName, String toSquareName, Color color) {
        // Используем старый метод с localToScene (на случай, если кэш не готов)
        drawArrow(gc, fromSquareName, toSquareName, color);
    }

    private void drawArrow(GraphicsContext gc, String fromSquareName, String toSquareName, Color color) {
        if (coachTools.getBoardView() == null) {
            log.debug("drawArrow: boardView is null");
            return;
        }

        StackPane fromCell = coachTools.getBoardView().getSquarePane(fromSquareName);
        StackPane toCell = coachTools.getBoardView().getSquarePane(toSquareName);

        if (fromCell == null || toCell == null) {
            log.debug("drawArrow: cells not found - from={}, to={}", fromSquareName, toSquareName);
            return;
        }

        // ========== ИСПОЛЬЗУЕМ getBoundsInParent() ВМЕСТО localToScene() ==========
        javafx.geometry.Bounds fromBounds = fromCell.getBoundsInParent();
        javafx.geometry.Bounds toBounds = toCell.getBoundsInParent();
        javafx.geometry.Bounds overlayBounds = getBoundsInParent();

        double startX = fromBounds.getMinX() + fromBounds.getWidth() / 2 - overlayBounds.getMinX();
        double startY = fromBounds.getMinY() + fromBounds.getHeight() / 2 - overlayBounds.getMinY();
        double endX = toBounds.getMinX() + toBounds.getWidth() / 2 - overlayBounds.getMinX();
        double endY = toBounds.getMinY() + toBounds.getHeight() / 2 - overlayBounds.getMinY();

        double angle = Math.atan2(endY - startY, endX - startX);
        double offset = 15;

        startX += Math.cos(angle) * offset;
        startY += Math.sin(angle) * offset;
        endX -= Math.cos(angle) * offset;
        endY -= Math.sin(angle) * offset;

        gc.setStroke(color);
        gc.setLineWidth(5);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.strokeLine(startX, startY, endX, endY);

        double wingAngle = Math.toRadians(40);
        double arrowLength = 24;

        double angle1 = angle + Math.PI - wingAngle;
        double angle2 = angle + Math.PI + wingAngle;

        double arrowX1 = endX + Math.cos(angle1) * arrowLength;
        double arrowY1 = endY + Math.sin(angle1) * arrowLength;
        double arrowX2 = endX + Math.cos(angle2) * arrowLength;
        double arrowY2 = endY + Math.sin(angle2) * arrowLength;

        gc.setFill(color);
        double[] xPoints = {endX, arrowX1, arrowX2};
        double[] yPoints = {endY, arrowY1, arrowY2};
        gc.fillPolygon(xPoints, yPoints, 3);
    }

    private void initSquareCenters() {
        if (coachTools.getBoardView() == null) return;

        squareCenters.clear();

        // Получаем размер клетки из доски
        ChessBoardView boardView = coachTools.getBoardView();

        // Проходим по всем клеткам доски
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                Square square = Square.squareAt(rank * 8 + file);
                String squareName = square.name();

                StackPane cell = boardView.getSquarePane(squareName);
                if (cell != null) {
                    // ========== ПОЛУЧАЕМ КООРДИНАТЫ ОТНОСИТЕЛЬНО boardContainer ==========
                    javafx.geometry.Bounds bounds = cell.getBoundsInParent();
                    double centerX = bounds.getMinX() + bounds.getWidth() / 2;
                    double centerY = bounds.getMinY() + bounds.getHeight() / 2;
                    squareCenters.put(squareName, new javafx.geometry.Point2D(centerX, centerY));
                }
            }
        }

        // Сохраняем размер клетки
        if (!squareCenters.isEmpty()) {
            tileSize = boardView.getTileSize();
        }

        isInitialized = true;
    }
}