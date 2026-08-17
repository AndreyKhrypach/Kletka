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

package Khrypach.Andrey.chess.kletka.gui.splash;

import Khrypach.Andrey.chess.kletka.gui.logo.LogoGenerator;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SplashScreen {

    private static final LanguageManager lang = LanguageManager.getInstance();

    private Stage splashStage;
    private ProgressBar progressBar;
    private Label statusLabel;

    public void showSplash() {
        Platform.runLater(() -> {
            splashStage = new Stage();
            splashStage.initStyle(StageStyle.TRANSPARENT);

            VBox root = new VBox(20);
            root.setAlignment(Pos.CENTER);
            root.setStyle("-fx-background-color: rgba(30, 30, 50, 0.95); -fx-background-radius: 15;");
            root.setPrefSize(500, 400);

            // Логотип
            Group logo = LogoGenerator.createLogo(400, 300);
            root.getChildren().add(logo);

            // Статус - ИСПРАВЛЕНО!
            statusLabel = new Label(lang.get(LanguageKeys.SPLASH_LOADING_ENGINE));
            statusLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

            // Прогресс бар
            progressBar = new ProgressBar(0);
            progressBar.setPrefWidth(300);
            progressBar.setStyle("-fx-accent: #4CAF50;");

            root.getChildren().addAll(statusLabel, progressBar);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            splashStage.setScene(scene);
            splashStage.centerOnScreen();
            splashStage.show();
        });
    }

    public void updateProgress(double progress, String status) {
        // ВСЕГДА вызываем на JavaFX Application Thread
        Platform.runLater(() -> {
            if (progressBar != null) {
                progressBar.setProgress(progress);
            }
            if (statusLabel != null) {
                statusLabel.setText(status);
            }
        });
    }

    public void hideSplash() {
        Platform.runLater(() -> {
            if (splashStage != null) {
                splashStage.hide();
                splashStage = null;
            }
        });
    }
}