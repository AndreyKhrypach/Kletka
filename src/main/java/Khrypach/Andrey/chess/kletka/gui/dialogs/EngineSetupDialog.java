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

package Khrypach.Andrey.chess.kletka.gui.dialogs;

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static Khrypach.Andrey.chess.kletka.engine.UciConstants.UCI;
import static Khrypach.Andrey.chess.kletka.engine.UciConstants.UCI_OK;
import static Khrypach.Andrey.chess.kletka.engine.UciConstants.QUIT;
import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

public class EngineSetupDialog {

    private static final Logger log = LoggerFactory.getLogger(EngineSetupDialog.class);
    private final LanguageManager lang = LanguageManager.getInstance();

    private final Stage owner;
    private String selectedEnginePath = null;
    private boolean confirmed = false;

    public EngineSetupDialog(Stage owner) {
        this.owner = owner;
    }

    public String showAndWait() {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(owner);
        dialogStage.setTitle(lang.get(ENGINE_SETUP_DIALOG_TITLE));
        dialogStage.setResizable(false);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f0f0f0;");

        Label header = new Label(lang.get(ENGINE_SETUP_DIALOG_HEADER));
        header.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label info = new Label(lang.get(ENGINE_SETUP_DIALOG_INFO));
        info.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 0, 10, 0));

        Label pathLabel = new Label(lang.get(ENGINE_SETUP_DIALOG_PATH_LABEL));
        pathLabel.setStyle("-fx-font-weight: bold;");

        TextField pathField = new TextField();
        pathField.setPromptText(lang.get(ENGINE_SETUP_DIALOG_PATH_PROMPT));
        pathField.setPrefWidth(400);

        Button browseButton = new Button(lang.get(ENGINE_SETUP_DIALOG_BROWSE));
        browseButton.setStyle("-fx-background-color: #8b5a2b; -fx-text-fill: white;");

        HBox pathBox = new HBox(10);
        pathBox.getChildren().addAll(pathField, browseButton);

        grid.add(pathLabel, 0, 0);
        grid.add(pathBox, 1, 0);

        Button testButton = new Button(lang.get(ENGINE_SETUP_DIALOG_TEST));
        testButton.setStyle("-fx-background-color: #2e8b57; -fx-text-fill: white;");

        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-font-size: 11px;");

        HBox testBox = new HBox(10);
        testBox.setAlignment(Pos.CENTER_LEFT);
        testBox.getChildren().addAll(testButton, statusLabel);

        Button okButton = new Button(lang.get(ENGINE_SETUP_DIALOG_OK));
        okButton.setStyle("-fx-background-color: #2e8b57; -fx-text-fill: white; -fx-font-weight: bold;");
        okButton.setDisable(true);

        Button cancelButton = new Button(lang.get(ENGINE_SETUP_DIALOG_CANCEL));
        cancelButton.setStyle("-fx-background-color: #8b0000; -fx-text-fill: white;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(okButton, cancelButton);

        browseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(lang.get(ENGINE_SETUP_DIALOG_FILE_CHOOSER_TITLE));

            File file = fileChooser.showOpenDialog(dialogStage);
            if (file != null) {
                pathField.setText(file.getAbsolutePath());
                okButton.setDisable(false);
                statusLabel.setText("");
            }
        });

        testButton.setOnAction(e -> {
            String path = pathField.getText();
            if (path == null || path.isEmpty()) {
                statusLabel.setText(lang.get(ENGINE_SETUP_DIALOG_STATUS_SELECT_FILE));
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
                return;
            }

            File engineFile = new File(path);
            if (!engineFile.exists()) {
                statusLabel.setText(lang.get(ENGINE_SETUP_DIALOG_STATUS_FILE_NOT_EXISTS) + ": " + path);
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
                return;
            }

            statusLabel.setText(lang.get(ENGINE_SETUP_DIALOG_STATUS_CHECKING));
            statusLabel.setStyle("-fx-text-fill: orange; -fx-font-size: 11px;");

            new Thread(() -> {
                boolean isValid = testEngine(path);
                javafx.application.Platform.runLater(() -> {
                    if (isValid) {
                        statusLabel.setText(lang.get(ENGINE_SETUP_DIALOG_STATUS_READY));
                        statusLabel.setStyle("-fx-text-fill: green; -fx-font-size: 11px; -fx-font-weight: bold;");
                        okButton.setDisable(false);
                    } else {
                        statusLabel.setText(lang.get(ENGINE_SETUP_DIALOG_STATUS_FAILED));
                        statusLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
                    }
                });
            }).start();
        });

        okButton.setOnAction(e -> {
            selectedEnginePath = pathField.getText();
            confirmed = true;
            dialogStage.close();
        });

        cancelButton.setOnAction(e -> dialogStage.close());

        root.getChildren().addAll(header, info, grid, testBox, buttonBox);

        Scene scene = new Scene(root);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();

        return confirmed ? selectedEnginePath : null;
    }

    private boolean testEngine(String enginePath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(enginePath);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream())
            );

            // Используем CompletableFuture для ожидания uciok
            CompletableFuture<Boolean> uciOkFuture = new CompletableFuture<>();

            // Запускаем читающий поток
            Thread readerThread = createReaderThread(reader, uciOkFuture);
            readerThread.start();

            // Отправляем UCI
            writer.write(UCI + "\n");
            writer.flush();

            // Ждем ответ с тайм-аутом
            boolean foundUciOk = false;
            try {
                foundUciOk = uciOkFuture.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                log.warn("Engine test timeout");
            } catch (Exception e) {
                log.warn("Engine test failed: {}", e.getMessage());
            }

            // Завершаем процесс
            writer.write(QUIT + "\n");
            writer.flush();
            process.destroyForcibly();

            return foundUciOk;

        } catch (Exception e) {
            log.warn("Engine test error: {}", e.getMessage());
            return false;
        }
    }

    private static Thread createReaderThread(BufferedReader reader, CompletableFuture<Boolean> uciOkFuture) {
        Thread readerThread = new Thread(() -> {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.equals(UCI_OK)) {
                        uciOkFuture.complete(true);
                        break;
                    }
                }
            } catch (IOException e) {
                uciOkFuture.completeExceptionally(e);
            }
        });
        readerThread.setDaemon(true);
        return readerThread;
    }
}