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
import javafx.geometry.Bounds;
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


/**
 * Оверлей для рисования стрелок поверх шахматной доски
 */
public class MarkerOverlay extends Pane {

    private static final Logger log = LoggerFactory.getLogger(MarkerOverlay.class);

    private final ChessBoardView boardView;
    private final Canvas canvas;
    private final CoachTools coachTools;
    @Setter
    private Pane boardContainer;

    public MarkerOverlay(CoachTools coachTools) {
        this.coachTools = coachTools;
        this.boardView = coachTools.getBoardView();
        this.canvas = new Canvas();
        setMouseTransparent(true);
        getChildren().add(canvas);
        coachTools.setOnMarkersChanged(this::redraw);
        log.debug("MarkerOverlay initialized");
    }

    public void redraw() {
        boolean hasArrows = !coachTools.getArrows().isEmpty();
        boolean hasTempArrow = coachTools.getTempArrow() != null;
        boolean hasCrosses = !coachTools.getCrosses().isEmpty();

        if (boardContainer == null || (!hasArrows && !hasTempArrow && !hasCrosses)) {
            canvas.setVisible(false);
            return;
        }

        canvas.setVisible(true);

        double width = boardContainer.getWidth();
        double height = boardContainer.getHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        // Больше не нужен initSquareCenters, координаты берём динамически

        canvas.setWidth(width);
        canvas.setHeight(height);
        canvas.setLayoutX(0);
        canvas.setLayoutY(0);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        // РИСУЕМ КРЕСТИКИ
        for (CrossData cross : coachTools.getCrosses().values()) {
            drawCross(gc, cross.getSquare(), cross.getColor().getColor());
        }

        // Рисуем все сохраненные стрелки
        for (ArrowData arrow : coachTools.getArrows().values()) {
            drawArrow(gc, arrow.getFromSquare(), arrow.getToSquare(), arrow.getColor().getColor());
        }

        // Рисуем временную стрелку
        ArrowData tempArrow = coachTools.getTempArrow();
        if (tempArrow != null) {
            drawArrow(gc, tempArrow.getFromSquare(), tempArrow.getToSquare(),
                    tempArrow.getColor().getColor().brighter());
        }
    }

    /**
     * Рисует крестик на клетке
     */
    private void drawCross(GraphicsContext gc, String squareName, Color color) {
        Point2D center = getSquareCenter(squareName);
        if (center == null) return;

        double x = center.getX();
        double y = center.getY();
        // Размер клетки теперь тоже нужно получать динамически
        double size = boardView.getTileSize() * 0.3;

        gc.setStroke(color);
        gc.setLineWidth(Math.max(3, boardView.getTileSize() * 0.08));
        gc.setLineCap(StrokeLineCap.ROUND);

        gc.strokeLine(x - size, y - size, x + size, y + size);
        gc.strokeLine(x + size, y - size, x - size, y + size);
    }

    private void drawArrow(GraphicsContext gc, String fromSquareName, String toSquareName, Color color) {
        Point2D fromCenter = getSquareCenter(fromSquareName);
        Point2D toCenter = getSquareCenter(toSquareName);

        if (fromCenter == null || toCenter == null) {
            return;
        }

        double startX = fromCenter.getX();
        double startY = fromCenter.getY();
        double endX = toCenter.getX();
        double endY = toCenter.getY();

        // ... остальная логика рисования стрелки остаётся без изменений ...
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

    /**
     * Получает актуальные координаты центра клетки относительно boardContainer.
     * @param squareName Имя клетки (например, "e4").
     * @return Point2D с координатами или null, если клетка не найдена.
     */
    private Point2D getSquareCenter(String squareName) {
        if (boardView == null || boardContainer == null) return null;

        StackPane cell = boardView.getSquarePane(squareName);
        if (cell == null) return null;

        // Получаем границы клетки в системе координат сцены
        Bounds cellBoundsInScene = cell.localToScene(cell.getBoundsInLocal());

        // Получаем границы boardContainer в системе координат сцены
        Bounds boardBoundsInScene = boardContainer.localToScene(boardContainer.getBoundsInLocal());

        // Вычисляем центр клетки относительно boardContainer
        double centerX = cellBoundsInScene.getMinX() + cellBoundsInScene.getWidth() / 2
                - boardBoundsInScene.getMinX();
        double centerY = cellBoundsInScene.getMinY() + cellBoundsInScene.getHeight() / 2
                - boardBoundsInScene.getMinY();

        return new Point2D(centerX, centerY);
    }
}