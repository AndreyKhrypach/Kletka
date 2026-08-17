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
import Khrypach.Andrey.chess.kletka.pgn.index.model.GameIndexEntry;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;

import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

/**
 * Диалог подтверждения удаления (для одной или нескольких партий)
 */
public class DeleteConfirmDialog {

    private final LanguageManager lang = LanguageManager.getInstance();
    private final Stage stage;
    private boolean confirmed = false;

    /**
     * Диалог для нескольких партий
     */
    public DeleteConfirmDialog(List<GameIndexEntry> entries) {
        this.stage = new Stage();
        stage.initStyle(StageStyle.UTILITY);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(lang.get(DELETE_CONFIRM_TITLE));

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setPrefWidth(450);

        int count = entries.size();

        // Заголовок
        String titleText = count == 1
                ? lang.get(DELETE_CONFIRM_SINGLE_TITLE)
                : String.format(lang.get(DELETE_CONFIRM_MULTIPLE_TITLE), count);
        Label titleLabel = new Label(titleText);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Информация
        StringBuilder infoText = new StringBuilder();
        if (count == 1) {
            GameIndexEntry e = entries.get(0);
            infoText.append(String.format(lang.get(DELETE_CONFIRM_SINGLE_MESSAGE), e.getId()));
            infoText.append(String.format(lang.get(DELETE_CONFIRM_WHITE),
                    e.getWhite().isEmpty() ? lang.get(DELETE_CONFIRM_UNKNOWN) : e.getWhite())).append("\n");
            infoText.append(String.format(lang.get(DELETE_CONFIRM_BLACK),
                    e.getBlack().isEmpty() ? lang.get(DELETE_CONFIRM_UNKNOWN) : e.getBlack())).append("\n");
            infoText.append(String.format(lang.get(DELETE_CONFIRM_RESULT),
                    e.getResult().isEmpty() ? "*" : e.getResult()));
        } else {
            infoText.append(String.format(lang.get(DELETE_CONFIRM_MULTIPLE_MESSAGE), count));
            int showCount = Math.min(count, 7);
            for (int i = 0; i < showCount; i++) {
                GameIndexEntry e = entries.get(i);
                infoText.append(String.format(lang.get(DELETE_CONFIRM_GAME_PREFIX),
                        e.getId(),
                        e.getWhite().isEmpty() ? lang.get(DELETE_CONFIRM_UNKNOWN) : e.getWhite(),
                        e.getBlack().isEmpty() ? lang.get(DELETE_CONFIRM_UNKNOWN) : e.getBlack(),
                        e.getResult().isEmpty() ? "*" : e.getResult()));
            }
            if (count > 7) {
                infoText.append(String.format(lang.get(DELETE_CONFIRM_AND_MORE), count - 7));
            }
        }

        infoText.append(lang.get(DELETE_CONFIRM_WARNING));

        Label infoLabel = new Label(infoText.toString());
        infoLabel.setWrapText(true);
        infoLabel.setStyle("-fx-font-size: 13px;");

        // Кнопки
        Button deleteButton = new Button(lang.get(DELETE_CONFIRM_DELETE_BUTTON));
        deleteButton.setStyle(
                "-fx-background-color: #cc0000; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 8 20 8 20;"
        );
        deleteButton.setOnAction(e -> {
            confirmed = true;
            stage.close();
        });

        Button cancelButton = new Button(lang.get(DELETE_CONFIRM_CANCEL_BUTTON));
        cancelButton.setStyle("-fx-padding: 8 20 8 20;");
        cancelButton.setOnAction(e -> stage.close());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(deleteButton, cancelButton);

        root.getChildren().addAll(titleLabel, infoLabel, buttonBox);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
    }

    public boolean showAndWait() {
        stage.showAndWait();
        return confirmed;
    }
}