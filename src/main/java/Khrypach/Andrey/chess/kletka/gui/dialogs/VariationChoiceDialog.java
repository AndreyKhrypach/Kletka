package Khrypach.Andrey.chess.kletka.gui.dialogs;

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.model.Variation;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class VariationChoiceDialog {
    private static final Logger log = LoggerFactory.getLogger(VariationChoiceDialog.class);

    // Сколько строк показывать без скролла (примерно)
    private static final int VISIBLE_ROWS = 8;
    private static final double DIALOG_WIDTH = 460;
    @Getter
    private static boolean isAnyDialogOpen = false;

    private Choice selectedChoice = null;
    private final List<Choice> choices;
    private final LanguageManager lang = LanguageManager.getInstance();
    private final javafx.stage.Window owner;

    private final List<Button> choiceButtons = new ArrayList<>();

    public VariationChoiceDialog(List<Choice> choices, javafx.stage.Window owner) {
        this.choices = choices;
        this.owner = owner;
    }

    public Choice showAndWait() {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.NONE );
        dialogStage.initStyle(StageStyle.DECORATED);
        dialogStage.setTitle(lang.get(LanguageKeys.DIALOG_VARIATION_TITLE));
        dialogStage.setResizable(false);

        if (owner != null) {
            dialogStage.initOwner(owner);
        }

        // ========== Корневой контейнер ==========
        VBox root = new VBox(15);
        root.getStyleClass().add("variation-dialog-root");
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(DIALOG_WIDTH);

        // ========== Заголовок ==========
        Label label = new Label(lang.get(LanguageKeys.DIALOG_VARIATION_CHOICE));
        label.getStyleClass().add("variation-dialog-title");

        // ========== Контейнер со строками ==========
        VBox choicesBox = new VBox();
        choicesBox.getStyleClass().add("variation-choices-box");
        choicesBox.setFillWidth(true);

        for (int i = 0; i < choices.size(); i++) {
            Choice choice = choices.get(i);
            Button btn = new Button(choice.description());
            btn.getStyleClass().add("variation-row");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setAlignment(Pos.CENTER_LEFT);
            btn.setFocusTraversable(true);

            // Главная линия — отдельный класс
            if (choice.variation() != null && choice.variation().isMainLine()) {
                btn.getStyleClass().add("main-line");
            }

            // Последняя строка — без нижней границы
            if (i == choices.size() - 1) {
                btn.getStyleClass().add("last-row");
            }

            // Одинарный клик мышью — выбор
            btn.setOnAction(e -> {
                selectedChoice = choice;
                Platform.runLater(dialogStage::close);
            });

            // Двойной клик — тоже выбор (страховка)
            btn.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    selectedChoice = choice;
                    Platform.runLater(dialogStage::close);
                }
            });

            // Enter/Space на кнопке — выбор
            btn.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                    selectedChoice = choice;
                    Platform.runLater(dialogStage::close);
                    e.consume();
                }
            });

            choiceButtons.add(btn);
            choicesBox.getChildren().add(btn);
        }

        // ========== ScrollPane — включаем скролл, только если строк много ==========
        ScrollPane scrollPane = new ScrollPane(choicesBox);
        scrollPane.getStyleClass().add("variation-scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefWidth(DIALOG_WIDTH - 40);

        // Показываем без скролла, если строк <= VISIBLE_ROWS
        if (choices.size() <= VISIBLE_ROWS) {
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            // Убираем жесткую высоту. Просто фиксируем максимум, чтобы VBox не растягивал ScrollPane.
            scrollPane.setMaxHeight(Region.USE_PREF_SIZE);
        } else {
            // Если строк много, задаем высоту примерно на 8 строк (можно оставить жестко)
            double h = VISIBLE_ROWS * 32 + 4; // 32 - чуть больше реальной высоты строки
            scrollPane.setPrefHeight(h);
            scrollPane.setMaxHeight(h);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        }
        VBox.setVgrow(scrollPane, Priority.NEVER);

        // ========== Кнопки OK / Cancel ==========
        Button okButton = new Button(lang.get(LanguageKeys.DIALOG_VARIATION_SELECT));
        okButton.getStyleClass().add("variation-ok-button");
        okButton.setOnAction(e -> {
            Button focused = getFocusedChoiceButton();
            if (focused != null) {
                int idx = choiceButtons.indexOf(focused);
                if (idx >= 0 && idx < choices.size()) {
                    selectedChoice = choices.get(idx);
                }
            }
            Platform.runLater(dialogStage::close);
        });

        Button cancelButton = new Button(lang.get(LanguageKeys.DIALOG_VARIATION_CANCEL));
        cancelButton.getStyleClass().add("variation-cancel-button");
        cancelButton.setOnAction(e -> Platform.runLater(dialogStage::close));

        HBox buttonsBox = new HBox(10, okButton, cancelButton);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(5, 0, 0, 0));

        root.getChildren().addAll(label, scrollPane, buttonsBox);

        Scene scene = new Scene(root);
        attachStylesheet(scene);

        // ========== Глобальный обработчик клавиш ==========
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            KeyCode code = event.getCode();

            if (code == KeyCode.UP) {
                moveFocus(-1);
                event.consume();
            } else if (code == KeyCode.DOWN) {
                moveFocus(+1);
                event.consume();
            } else if (code == KeyCode.ENTER || code == KeyCode.RIGHT) {
                Button focused = getFocusedChoiceButton();
                if (focused != null) {
                    int idx = choiceButtons.indexOf(focused);
                    if (idx >= 0 && idx < choices.size()) {
                        selectedChoice = choices.get(idx);
                        log.trace("ENTER/RIGHT — selected choice idx={}", idx);
                        Platform.runLater(dialogStage::close);
                    }
                }
                event.consume();
            } else if (code == KeyCode.ESCAPE || code == KeyCode.LEFT) {
                Platform.runLater(dialogStage::close);
                event.consume();
            }
        });

        dialogStage.setScene(scene);
        dialogStage.sizeToScene();

        // ========== ЗАЩИТА ОТ КЛИКОВ ПО ОСНОВНОМУ ОКНУ ==========
        Node ownerRoot = null;
        if (owner instanceof Stage) {
            Scene ownerScene = ((Stage) owner).getScene();
            if (ownerScene != null) {
                ownerRoot = ownerScene.getRoot();
            }
        }

        final Node finalOwnerRoot = ownerRoot;
        if (finalOwnerRoot != null) {
            finalOwnerRoot.setMouseTransparent(true);
        }

        // ========== ФОКУС ПРИ ОТКРЫТИИ ==========
        dialogStage.setOnShown(e -> {
            if (!choiceButtons.isEmpty()) {
                choiceButtons.get(0).requestFocus();
            }
        });

        // ========== СНЯТИЕ БЛОКИРОВКИ ПРИ ЛЮБОМ ЗАКРЫТИИ ==========
        isAnyDialogOpen = true;
        dialogStage.setOnHidden(e -> {
            if (finalOwnerRoot != null) {
                finalOwnerRoot.setMouseTransparent(false);
            }
            if (owner != null) {
                Platform.runLater(owner::requestFocus);
            }
            isAnyDialogOpen = false;
        });

        // ========== ОДИН ЕДИНСТВЕННЫЙ ВЫЗОВ ==========
        dialogStage.showAndWait();
        return selectedChoice;
    }

    /**
     * Подключает CSS-файл к сцене диалога.
     */
    private void attachStylesheet(Scene scene) {
        URL css = getClass().getResource("/css/variation-dialog.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        } else {
            log.warn("variation-dialog.css not found in /css/ — using inline styles");
        }
    }

    private Button getFocusedChoiceButton() {
        for (Button btn : choiceButtons) {
            if (btn.isFocused()) {
                return btn;
            }
        }
        return choiceButtons.isEmpty() ? null : choiceButtons.get(0);
    }

    private void moveFocus(int delta) {
        if (choiceButtons.isEmpty()) return;

        int currentIdx = -1;
        for (int i = 0; i < choiceButtons.size(); i++) {
            if (choiceButtons.get(i).isFocused()) {
                currentIdx = i;
                break;
            }
        }

        int newIdx;
        if (currentIdx == -1) {
            newIdx = (delta > 0) ? 0 : choiceButtons.size() - 1;
        } else {
            newIdx = currentIdx + delta;
            if (newIdx < 0) newIdx = 0;
            if (newIdx >= choiceButtons.size()) newIdx = choiceButtons.size() - 1;
        }

        choiceButtons.get(newIdx).requestFocus();
    }

    public record Choice(Variation variation, String description, boolean isNewVariation) {

        public Choice(String description, Variation variation, boolean isNewVariation) {
            this(variation, description, isNewVariation);
        }
    }
}