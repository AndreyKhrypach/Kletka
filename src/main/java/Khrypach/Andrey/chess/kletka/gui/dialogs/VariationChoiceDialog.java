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

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.model.Variation;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class VariationChoiceDialog {
    private static final Logger log = LoggerFactory.getLogger(VariationChoiceDialog.class);

    private Choice selectedChoice = null;
    private final List<Choice> choices;
    private ListView<String> listView;
    private final LanguageManager lang = LanguageManager.getInstance();

    private final javafx.stage.Window owner;

    public VariationChoiceDialog(List<Choice> choices, javafx.stage.Window owner) {
        this.choices = choices;
        this.owner = owner;
    }

    public Choice showAndWait() {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.setTitle(lang.get(LanguageKeys.DIALOG_VARIATION_TITLE));
        dialogStage.setResizable(false);

        if (owner != null) {
            dialogStage.initOwner(owner);
        }

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f0f0f0;");
        root.setAlignment(Pos.CENTER);
        root.setFocusTraversable(true);

        Label label = new Label(lang.get(LanguageKeys.DIALOG_VARIATION_CHOICE));
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        listView = new ListView<>();
        listView.setPrefWidth(400);
        listView.setPrefHeight(200);
        listView.setFocusTraversable(true);

        for (Choice choice : choices) {
            listView.getItems().add(choice.description);
        }

        log.trace("=== VariationChoiceDialog ===");
        log.trace("  choices.size(): {}", choices.size());

        if (!choices.isEmpty()) {
            listView.getSelectionModel().select(0);
            log.trace("  auto-selected index 0: '{}'", choices.get(0).description());
        }

        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                int index = listView.getSelectionModel().getSelectedIndex();
                if (index >= 0) {
                    selectedChoice = choices.get(index);
                    dialogStage.close();
                }
            }
        });

        Button okButton = new Button(lang.get(LanguageKeys.DIALOG_VARIATION_SELECT));
        okButton.setStyle("-fx-background-color: #8b5a2b; -fx-text-fill: white; -fx-font-weight: bold;");
        okButton.setOnAction(e -> {
            int index = listView.getSelectionModel().getSelectedIndex();
            if (index >= 0) {
                selectedChoice = choices.get(index);
                dialogStage.close();
            }
        });

        Button cancelButton = new Button(lang.get(LanguageKeys.DIALOG_VARIATION_CANCEL));
        cancelButton.setOnAction(e -> dialogStage.close());

        root.getChildren().addAll(label, listView, okButton, cancelButton);

        Scene scene = new Scene(root);

        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.ENTER) {
                int index = listView.getSelectionModel().getSelectedIndex();
                log.trace("ENTER/RIGHT pressed, selected index: {}", index);
                if (index >= 0) {
                    selectedChoice = choices.get(index);

                    log.trace("Returning choice: desc='{}', isNewVariation={}, varName='{}'",
                            selectedChoice.description(), selectedChoice.isNewVariation(),
                            selectedChoice.variation() != null ? selectedChoice.variation().getName() : "null");

                    Platform.runLater(dialogStage::close);
                }
                event.consume();
            } else if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.ESCAPE) {
                Platform.runLater(dialogStage::close);
                event.consume();
            } else if (event.getCode() == KeyCode.UP) {
                int index = listView.getSelectionModel().getSelectedIndex();
                if (index > 0) {
                    listView.getSelectionModel().select(index - 1);
                    listView.scrollTo(index - 1);
                }
                event.consume();
            } else if (event.getCode() == KeyCode.DOWN) {
                int index = listView.getSelectionModel().getSelectedIndex();
                if (index < choices.size() - 1) {
                    listView.getSelectionModel().select(index + 1);
                    listView.scrollTo(index + 1);
                }
                event.consume();
            }
        });

        listView.addEventFilter(KeyEvent.KEY_PRESSED, event -> scene.getRoot().fireEvent(event));

        dialogStage.setScene(scene);

        // ========== ПРИНУДИТЕЛЬНЫЙ ФОКУС ДЛЯ macOS ==========
        dialogStage.setOnShowing(e -> Platform.runLater(() -> {
            listView.requestFocus();
            dialogStage.toFront();
        }));

        dialogStage.setOnShown(e -> Platform.runLater(() -> {
            dialogStage.requestFocus();
            listView.requestFocus();
            dialogStage.toFront();
        }));

        dialogStage.showAndWait();
        return selectedChoice;
    }

    public record Choice(Variation variation, String description, boolean isNewVariation) {

        public Choice(String description, Variation variation, boolean isNewVariation) {
            this(variation, description, isNewVariation);
        }
    }
}
