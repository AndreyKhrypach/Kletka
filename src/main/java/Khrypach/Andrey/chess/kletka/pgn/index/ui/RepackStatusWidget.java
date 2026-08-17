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

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.pgn.index.PgnRepacker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

/**
 * Виджет для отображения статуса перепаковки в PgnFileBrowser
 */
public class RepackStatusWidget extends VBox {

    private static final LanguageManager lang = LanguageManager.getInstance();

    private final Circle statusCircle;
    private final Label statusLabel;
    private final Label ratioLabel;
    private final ProgressBar ratioBar;
    private final Label deletedLabel;

    public RepackStatusWidget() {
        setSpacing(5);
        setPadding(new Insets(5, 10, 5, 10));
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        statusCircle = new Circle(8);
        statusCircle.setFill(Color.GREEN);

        statusLabel = new Label(lang.get(REPACK_STATUS_OPTIMAL));
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        topRow.getChildren().addAll(statusCircle, statusLabel);

        HBox bottomRow = new HBox(10);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        ratioLabel = new Label(String.format(lang.get(REPACK_STATUS_RATIO), 1.0));
        ratioLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

        ratioBar = new ProgressBar(0);
        ratioBar.setPrefWidth(80);
        ratioBar.setMinHeight(8);
        ratioBar.setStyle("-fx-accent: #4CAF50;");

        deletedLabel = new Label("");
        deletedLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

        bottomRow.getChildren().addAll(ratioLabel, ratioBar, deletedLabel);

        getChildren().addAll(topRow, bottomRow);
    }

    /**
     * Обновляет статус на основе данных индекса
     */
    public void updateStatus(PgnRepacker.RepackStatus status) {
        if (status == null) {
            setVisible(false);
            return;
        }

        setVisible(true);

        // ========== ЦВЕТ И СТАТУС ==========
        Color color;
        String statusText;
        String barColor;

        if (!status.hasDeleted()) {
            color = Color.GREEN;
            statusText = lang.get(REPACK_STATUS_NO_DELETED);
            barColor = "#4CAF50";
        } else {
            switch (status.level()) {
                case OK:
                    color = Color.GREEN;
                    statusText = lang.get(REPACK_STATUS_HAS_DELETED);
                    barColor = "#4CAF50";
                    break;
                case WARNING:
                    color = Color.ORANGE;
                    statusText = lang.get(REPACK_STATUS_WARNING);
                    barColor = "#FF9800";
                    break;
                case CRITICAL:
                    color = Color.RED;
                    statusText = lang.get(REPACK_STATUS_CRITICAL);
                    barColor = "#f44336";
                    break;
                default:
                    color = Color.GRAY;
                    statusText = lang.get(REPACK_STATUS_UNKNOWN);
                    barColor = "#999";
            }
        }

        statusCircle.setFill(color);
        statusLabel.setText(statusText);

        // ========== ПРОГРЕСС-БАР ==========
        double ratio = Math.min(status.ratio() / 2.0, 1.0);
        ratioBar.setProgress(ratio);
        ratioBar.setStyle("-fx-accent: " + barColor + ";");

        ratioLabel.setText(String.format(lang.get(REPACK_STATUS_RATIO), status.ratio()));

        // ========== КОЛИЧЕСТВО УДАЛЁННЫХ ==========
        if (status.hasDeleted()) {
            deletedLabel.setText(String.format(lang.get(REPACK_STATUS_DELETED_COUNT), status.deletedCount()));
            deletedLabel.setVisible(true);
        } else {
            deletedLabel.setVisible(false);
        }

        // ========== TOOLTIP ==========
        String tooltipText = String.format(
                lang.get(REPACK_STATUS_TOOLTIP),
                status.ratio(),
                status.deletedCount(),
                status.deletedCount() > 0 ?
                        (int)(status.deletedCount() / status.ratio()) : 0
        );
        ratioLabel.setTooltip(new Tooltip(tooltipText));
    }

    /**
     * Показывает состояние "Перепаковка выполняется"
     */
    public void showRepacking() {
        statusCircle.setFill(Color.BLUE);
        statusLabel.setText(lang.get(REPACK_STATUS_REPACKING));
        ratioBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        ratioLabel.setText(lang.get(REPACK_STATUS_LOADING));
        deletedLabel.setVisible(false);
        ratioLabel.setTooltip(new Tooltip(lang.get(REPACK_STATUS_REPACKING_TOOLTIP)));
    }
}