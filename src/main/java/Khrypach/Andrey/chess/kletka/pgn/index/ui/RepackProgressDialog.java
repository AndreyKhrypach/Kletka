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
import Khrypach.Andrey.chess.kletka.pgn.index.model.IndexingProgress;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

/**
 * Диалог отображения прогресса перепаковки PGN-файла
 */
public class RepackProgressDialog {
    private static final Logger log = LoggerFactory.getLogger(RepackProgressDialog.class);
    private static final LanguageManager lang = LanguageManager.getInstance();

    private final Stage stage;
    private final Label titleLabel;
    private final Label statusLabel;
    private final Label progressLabel;
    private final ProgressBar progressBar;
    private final ProgressIndicator progressIndicator;

    @Getter
    private volatile boolean cancelled = false;
    @Setter
    private Runnable onCancel;

    @Setter
    private boolean autoClose = true;

    public RepackProgressDialog() {
        this.stage = new Stage();
        stage.initStyle(StageStyle.UTILITY);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(lang.get(REPACK_DIALOG_TITLE));
        stage.setResizable(false);
        stage.setOnCloseRequest(e -> {
            if (onCancel != null) {
                cancelled = true;
                onCancel.run();
            }
            e.consume(); // Не даем закрыть просто так
        });

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(450);

        titleLabel = new Label(lang.get(REPACK_DIALOG_TITLE));
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        titleLabel.setAlignment(Pos.CENTER);

        statusLabel = new Label(lang.get(REPACK_DIALOG_STATUS_PREPARING));
        statusLabel.setStyle("-fx-font-size: 14px;");
        statusLabel.setWrapText(true);
        statusLabel.setAlignment(Pos.CENTER);

        progressLabel = new Label("0%");
        progressLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(380);
        progressBar.setMinHeight(20);

        progressIndicator = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
        progressIndicator.setPrefSize(60, 60);

        VBox progressContainer = new VBox(10, progressIndicator, progressBar, progressLabel);
        progressContainer.setAlignment(Pos.CENTER);

        root.getChildren().addAll(titleLabel, statusLabel, progressContainer);

        Scene scene = new Scene(root);
        stage.setScene(scene);
    }

    /**
     * Устанавливает заголовок диалога
     */
    public void setTitle(String title) {
        stage.setTitle(title);
        titleLabel.setText(title);
    }

    /**
     * Показывает диалог
     */
    public void show() {
        log.info("[RepackProgressDialog] Showing dialog");
        Platform.runLater(() -> {
            stage.show();
            stage.toFront();
        });
    }

    /**
     * Закрывает диалог
     */
    public void close() {
        log.info("[RepackProgressDialog] Closing dialog");
        Platform.runLater(stage::close);
    }

    /**
     * Обновляет прогресс
     */
    public void updateProgress(IndexingProgress progress) {
        Platform.runLater(() -> {
            if (progress == null) return;

            if (progress.getStatus() != null) {
                statusLabel.setText(progress.getStatus());
            }

            if (progress.getTotalGames() > 0) {
                double pct = progress.getProgress();
                progressBar.setProgress(Math.min(pct, 1.0));
                progressLabel.setText(String.format(lang.get(REPACK_DIALOG_PROGRESS_FORMAT),
                        progress.getProcessedGames(),
                        progress.getTotalGames(),
                        pct * 100));

                progressIndicator.setVisible(!(pct >= 0.01));
            } else {
                progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
                progressLabel.setText(String.format(lang.get(REPACK_DIALOG_GAMES_PROCESSED),
                        progress.getProcessedGames()));
            }

            // Если завершено - закрываем автоматически
            if (progress.getProcessedGames() >= progress.getTotalGames() &&
                    progress.getTotalGames() > 0) {
                progressBar.setProgress(1.0);
                progressLabel.setText(lang.get(REPACK_DIALOG_COMPLETE));
                progressIndicator.setVisible(false);

                if (autoClose) {
                    // Закрываем через 1 секунду
                    new Thread(() -> {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignored) {}
                        Platform.runLater(this::close);
                    }).start();
                }
            }
        });
    }

    /**
     * Показывает ошибку в диалоге
     */
    public void showError(String title, String message) {
        Platform.runLater(() -> {
            statusLabel.setText(String.format(lang.get(REPACK_DIALOG_ERROR), title));
            progressLabel.setText(message);
            progressBar.setProgress(1.0);
            progressIndicator.setVisible(false);
        });
    }
}