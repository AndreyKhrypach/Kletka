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

package Khrypach.Andrey.chess.kletka.gui;

import Khrypach.Andrey.chess.kletka.gui.board.ChessBoardView;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.splash.SplashScreen;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class KletkaGui extends Application {

    private static final Logger log = LoggerFactory.getLogger(KletkaGui.class);
    private static final LanguageManager lang = LanguageManager.getInstance();
    private SplashScreen splashScreen;

    // Время показа загрузочного экрана (2 секунды)
    private static final int SPLASH_DISPLAY_TIME_MS = 2000;

    static {
        try {
            Path logDir = Paths.get("logs");
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
                System.out.println("Logs directory created: " + logDir.toAbsolutePath());
            }
        } catch (Exception e) {
            // Не критично — логи будем писать в текущую директорию или в stdout
            System.err.println("Could not create logs directory: " + e.getMessage());
        }
        // Инициализация логгера
        log.info(lang.get(LanguageKeys.LOG_INITIALIZED));
    }

    public static void main(String[] args) {
        log.info("=== STARTING APPLICATION ===");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        // ========== 1. СНАЧАЛА ПОКАЗЫВАЕМ ЗАГРУЗОЧНЫЙ ЭКРАН ==========
        splashScreen = new SplashScreen();
        splashScreen.showSplash();
        splashScreen.updateProgress(0.1, lang.get(LanguageKeys.SPLASH_LOADING_ENGINE));

        log.info(lang.get(LanguageKeys.LOG_STARTING_GUI));

        // ========== 2. ПОСЛЕДОВАТЕЛЬНАЯ ЗАГРУЗКА ==========
        // Шаг 1: Инициализация движка (0.1 - 0.4)
        PauseTransition step1 = new PauseTransition(Duration.millis(500));
        step1.setOnFinished(e1 -> {
            splashScreen.updateProgress(0.4, lang.get(LanguageKeys.SPLASH_INITIALIZING_BOARD));

            // Шаг 2: Загрузка доски (0.4 - 0.7)
            PauseTransition step2 = new PauseTransition(Duration.millis(500));
            step2.setOnFinished(e2 -> {
                splashScreen.updateProgress(0.7, lang.get(LanguageKeys.SPLASH_LOADING_GUI));

                // Шаг 3: Создание GUI (0.7 - 1.0)
                PauseTransition step3 = new PauseTransition(Duration.millis(500));
                step3.setOnFinished(e3 -> {
                    try {
                        // Создаем основное приложение
                        ChessBoardView boardView = new ChessBoardView();
                        boardView.start(primaryStage);

                        splashScreen.updateProgress(1.0, lang.get(LanguageKeys.SPLASH_READY));

                        // ========== 3. ЗАДЕРЖКА ДЛЯ ПРОСМОТРА ЛОГОТИПА ==========
                        PauseTransition splashDisplayDelay = createSplashDisplayDelay(primaryStage);
                        splashDisplayDelay.play();

                    } catch (Exception ex) {
                        log.error(lang.get(LanguageKeys.LOG_GUI_ERROR), ex);
                        splashScreen.hideSplash();
                        showErrorDialog(lang.get(LanguageKeys.LOG_GUI_ERROR), ex.getMessage());
                    }
                });
                step3.play();
            });
            step2.play();
        });
        step1.play();
    }

    private PauseTransition createSplashDisplayDelay(Stage primaryStage) {
        PauseTransition splashDisplayDelay = new PauseTransition(
                Duration.millis(SPLASH_DISPLAY_TIME_MS)
        );
        splashDisplayDelay.setOnFinished(e4 -> {
            // Скрываем загрузочный экран и показываем основное окно
            splashScreen.hideSplash();
            if (!primaryStage.isShowing()) {
                primaryStage.show();
            }
            log.info(lang.get(LanguageKeys.LOG_GUI_LOADED));
        });
        return splashDisplayDelay;
    }

    private void showErrorDialog(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void stop() {
        log.info(lang.get(LanguageKeys.LOG_SHUTTING_DOWN));
        if (splashScreen != null) {
            splashScreen.hideSplash();
        }
        Platform.exit();
        System.exit(0);
    }

}