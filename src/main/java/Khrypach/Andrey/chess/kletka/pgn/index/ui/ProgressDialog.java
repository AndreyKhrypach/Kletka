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

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import lombok.Setter;

/**
 * Диалог прогресса для массовых операций
 */
public class ProgressDialog {

    private final Stage stage;
    private final Label statusLabel;
    private final Label detailLabel;
    private final ProgressBar progressBar;
    @Getter
    private boolean cancelled = false;
    @Setter
    private Runnable onCancel;

    public ProgressDialog(String title, String initialMessage) {
        this.stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle(title);
        stage.setResizable(false);
        stage.setOnCloseRequest(event -> {
            cancelled = true;
            if (onCancel != null) {
                onCancel.run();
            }
        });

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setMinWidth(400);

        statusLabel = new Label(initialMessage);
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        detailLabel = new Label("");
        detailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(350);
        progressBar.setMinHeight(20);

        root.getChildren().addAll(statusLabel, detailLabel, progressBar);

        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    public void show() {
        Platform.runLater(stage::show);
    }

    public void close() {
        Platform.runLater(stage::close);
    }

    public void updateProgress(double progress, String status, String detail) {
        Platform.runLater(() -> {
            progressBar.setProgress(progress);
            statusLabel.setText(status);
            detailLabel.setText(detail);
        });
    }

}