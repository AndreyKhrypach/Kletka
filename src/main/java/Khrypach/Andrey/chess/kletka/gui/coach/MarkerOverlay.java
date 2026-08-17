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

import Khrypach.Andrey.chess.kletka.gui.coach.tools.ArrowData;
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

    private final Canvas canvas;
    private final CoachTools coachTools;
    @Setter
    private Pane boardContainer;

    public MarkerOverlay(CoachTools coachTools) {
        this.coachTools = coachTools;
        this.canvas = new Canvas();
        setMouseTransparent(true);
        getChildren().add(canvas);
        coachTools.setOnMarkersChanged(this::redraw);
    }

    public void redraw() {
        log.trace("redraw called - Arrows count: {}, boardContainer: {}",
                coachTools.getArrows().size(),
                boardContainer != null ? "ok" : "null");

        if (boardContainer == null || coachTools.getArrows().isEmpty()) {
            canvas.setVisible(false);
            return;
        }

        canvas.setVisible(true);

        double width = boardContainer.getWidth();
        double height = boardContainer.getHeight();

        log.trace("boardContainer size: {} x {}", width, height);

        if (width <= 0 || height <= 0) return;

        canvas.setWidth(width);
        canvas.setHeight(height);
        canvas.setLayoutX(0);
        canvas.setLayoutY(0);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        for (ArrowData arrow : coachTools.getArrows().values()) {
            log.trace("Drawing arrow: {} -> {}", arrow.getFromSquare(), arrow.getToSquare());
            drawArrow(gc, arrow.getFromSquare(), arrow.getToSquare(), arrow.getColor().getColor());
        }

        ArrowData tempArrow = coachTools.getTempArrow();
        if (tempArrow != null && coachTools.isDraggingArrow()) {
            log.trace("Drawing temp arrow: {} -> {}", tempArrow.getFromSquare(), tempArrow.getToSquare());
            drawArrow(gc, tempArrow.getFromSquare(), tempArrow.getToSquare(),
                    tempArrow.getColor().getColor().brighter());
        }
    }

    private void drawArrow(GraphicsContext gc, String fromSquareName, String toSquareName, Color color) {
        if (coachTools.getBoardView() == null) return;

        StackPane fromCell = coachTools.getBoardView().getSquarePane(fromSquareName);
        StackPane toCell = coachTools.getBoardView().getSquarePane(toSquareName);

        if (fromCell == null || toCell == null) return;

        javafx.geometry.Bounds fromBounds = fromCell.localToScene(fromCell.getBoundsInLocal());
        javafx.geometry.Bounds toBounds = toCell.localToScene(toCell.getBoundsInLocal());
        javafx.geometry.Bounds overlayBounds = localToScene(getBoundsInLocal());

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
        gc.setLineWidth(10);
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
}