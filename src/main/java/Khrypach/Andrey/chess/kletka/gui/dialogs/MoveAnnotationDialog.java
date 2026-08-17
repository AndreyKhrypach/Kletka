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

import Khrypach.Andrey.chess.kletka.gui.board.ChessSymbols;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.model.MoveAnnotation;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Optional;

public class MoveAnnotationDialog {

    private static final String STYLE_DEFAULT = "-fx-font-size: 13px; -fx-font-weight: bold; -fx-min-width: 260px; " +
            "-fx-max-width: 300px; -fx-min-height: 50px; -fx-background-color: #e0e0e0; -fx-text-fill: #000000; " +
            "-fx-wrap-text: true; -fx-alignment: CENTER_LEFT; -fx-padding: 8px 12px; -fx-cursor: hand;";
    private static final String STYLE_SELECTED = "-fx-font-size: 13px; -fx-font-weight: bold; -fx-min-width: 260px; " +
                    "-fx-max-width: 300px; -fx-min-height: 50px; -fx-background-color: #4CAF50; -fx-text-fill: white; " +
                    "-fx-wrap-text: true; -fx-alignment: CENTER_LEFT; -fx-padding: 8px 12px; -fx-cursor: hand;";

    private final Stage owner;
    private MoveAnnotation selectedAnnotation;
    private String comment;
    private final String moveSan;
    private final MoveAnnotation existingAnnotation;  // Существующая аннотация
    private final String existingComment;              // Существующий комментарий

    // Конструктор для редактирования существующего комментария
    public MoveAnnotationDialog(Stage owner, String moveSan, MoveAnnotation existingAnnotation, String existingComment) {
        this.owner = owner;
        this.moveSan = moveSan;
        this.existingAnnotation = existingAnnotation;
        this.existingComment = existingComment;
        this.selectedAnnotation = existingAnnotation;
        this.comment = existingComment;
    }

    public Optional<Result> showAndWait() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle(LanguageManager.getInstance().get(LanguageKeys.ANNOTATION_DIALOG_TITLE));
        dialog.initStyle(StageStyle.UTILITY);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");

        // ========== ЗАГОЛОВОК С ХОДОМ В ФИГУРНОЙ НОТАЦИИ ==========
        VBox headerBox = new VBox(5);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setStyle("-fx-background-color: #e8e0d0; -fx-padding: 10px; -fx-border-radius: 5px;");

        Label titleLabel = new Label(LanguageManager.getInstance().get(LanguageKeys.ANNOTATION_DIALOG_SELECT));
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #5a3e1b;");

        // Отображаем ход в фигурной нотации
        String chessSymbolMove = ChessSymbols.convertToChessSymbols(moveSan);
        Text moveDisplay = new Text(chessSymbolMove);
        moveDisplay.setFont(Font.font("Segue UI", FontWeight.BOLD, 28));
        moveDisplay.setStyle("-fx-fill: #2c3e50;");

        Label moveLabel = new Label(LanguageManager.getInstance().get(LanguageKeys.ANNOTATION_DIALOG_MOVE));
        moveLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666; -fx-font-weight: normal;");

        headerBox.getChildren().addAll(titleLabel, moveLabel, moveDisplay);
        root.getChildren().add(headerBox);
        // ==========================================================

        TabPane tabPane = new TabPane();

        // Вкладка "Оценка хода"
        Tab moveEvalTab = new Tab(LanguageManager.getInstance().get(LanguageKeys.ANNOTATION_TAB_MOVE_EVAL));
        moveEvalTab.setClosable(false);
        GridPane moveEvalGrid = createAnnotationGrid(
                MoveAnnotation.GOOD_MOVE,
                MoveAnnotation.BRILLIANT_MOVE,
                MoveAnnotation.INTERESTING_MOVE,
                MoveAnnotation.DUBIOUS_MOVE,
                MoveAnnotation.BAD_MOVE,
                MoveAnnotation.BLUNDER
        );
        moveEvalTab.setContent(moveEvalGrid);

        // Вкладка "Оценка позиции"
        Tab positionEvalTab = new Tab(LanguageManager.getInstance().get(LanguageKeys.ANNOTATION_TAB_POSITION_EVAL));
        positionEvalTab.setClosable(false);
        GridPane positionEvalGrid = createAnnotationGrid(
                MoveAnnotation.SLIGHT_ADVANTAGE_WHITE,
                MoveAnnotation.CLEAR_ADVANTAGE_WHITE,
                MoveAnnotation.WINNING_WHITE,
                MoveAnnotation.EQUALITY,
                MoveAnnotation.SLIGHT_ADVANTAGE_BLACK,
                MoveAnnotation.CLEAR_ADVANTAGE_BLACK,
                MoveAnnotation.WINNING_BLACK,
                MoveAnnotation.UNCLEAR_POSITION,
                MoveAnnotation.WITH_COMPENSATION
        );
        positionEvalTab.setContent(positionEvalGrid);

        // Вкладка "Комментарии"
        Tab commentaryTab = new Tab(LanguageManager.getInstance().get(LanguageKeys.ANNOTATION_TAB_COMMENTARY));
        commentaryTab.setClosable(false);
        GridPane commentaryGrid = createAnnotationGrid(
                MoveAnnotation.ONLY_MOVE,
                MoveAnnotation.THEORETICAL_NOVELTY,
                MoveAnnotation.WITH_IDEA,
                MoveAnnotation.WITH_INITIATIVE,
                MoveAnnotation.WITH_COUNTERPLAY,
                MoveAnnotation.DEVELOPMENT_ADVANTAGE,
                MoveAnnotation.BETTER_WAS
        );
        commentaryTab.setContent(commentaryGrid);

        tabPane.getTabs().addAll(moveEvalTab, positionEvalTab, commentaryTab);

        Separator separator = new Separator();

        // Поле для комментария
        Label commentLabel = new Label(LanguageManager.getInstance().get(LanguageKeys.ANNOTATION_DIALOG_COMMENT));
        commentLabel.setStyle("-fx-font-weight: bold;");

        TextArea commentArea = new TextArea();
        commentArea.setPromptText(LanguageManager.getInstance().get(LanguageKeys.ANNOTATION_DIALOG_COMMENT_PROMPT));
        commentArea.setPrefRowCount(3);
        commentArea.setWrapText(true);

        // ========== ЗАГРУЖАЕМ СУЩЕСТВУЮЩИЙ КОММЕНТАРИЙ ==========
        if (existingComment != null && !existingComment.isEmpty()) {
            commentArea.setText(existingComment);
        }
        // ======================================================

        // Кнопки
        Button clearButton = new Button(LanguageManager.getInstance().get(LanguageKeys.ANNOTATION_DIALOG_CLEAR));
        clearButton.setStyle("-fx-cursor: hand;");
        clearButton.setOnAction(e -> {
            selectedAnnotation = null;
            commentArea.clear();
        });

        Button okButton = new Button(LanguageManager.getInstance().get(LanguageKeys.ANNOTATION_DIALOG_OK));
        okButton.setStyle("-fx-background-color: #2e8b57; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        okButton.setOnAction(e -> {
            comment = commentArea.getText();
            dialog.close();
        });

        Button cancelButton = new Button(LanguageManager.getInstance().get(LanguageKeys.ANNOTATION_DIALOG_CANCEL));
        cancelButton.setStyle("-fx-cursor: hand;");
        cancelButton.setOnAction(e -> {
            selectedAnnotation = null;
            comment = null;
            dialog.close();
        });

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(clearButton, okButton, cancelButton);

        root.getChildren().addAll(tabPane, separator, commentLabel, commentArea, buttonBox);

        Scene scene = new Scene(root, 650, 680);
        dialog.setScene(scene);

        // ========== ПОДСВЕЧИВАЕМ ВЫБРАННУЮ АННОТАЦИЮ ==========
        dialog.setOnShown(e -> {
            if (existingAnnotation != null) {
                // Находим и подсвечиваем кнопку с существующей аннотацией
                highlightAnnotationInAllTabs(root, existingAnnotation);
            }
        });
        // ======================================================

        dialog.showAndWait();

        if (selectedAnnotation != null || (comment != null && !comment.trim().isEmpty())) {
            return Optional.of(new Result(selectedAnnotation, comment));
        }
        return Optional.empty();
    }

    private GridPane createAnnotationGrid(MoveAnnotation... annotations) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.setAlignment(Pos.TOP_LEFT);

        int row = 0;
        int col = 0;

        for (MoveAnnotation annotation : annotations) {
            Button btn = new Button(annotation.getSymbol() + "  " + annotation.getDescription());
            btn.setUserData(annotation); // Сохраняем аннотацию в кнопке
            btn.setStyle(STYLE_DEFAULT);
            btn.setTooltip(new Tooltip(annotation.getDescription()));
            btn.setOnAction(e -> {
                if (selectedAnnotation == annotation) {
                    selectedAnnotation = null;
                    clearButtonSelectionInGrid(grid);
                } else {
                    selectedAnnotation = annotation;
                    clearButtonSelectionInGrid(grid);
                    btn.setStyle(STYLE_SELECTED);
                }
            });

            // Если это существующая аннотация, сразу подсвечиваем
            if (existingAnnotation != null && existingAnnotation == annotation) {
                btn.setStyle(STYLE_SELECTED);
                selectedAnnotation = annotation;
            }

            grid.add(btn, col, row);

            col++;
            if (col >= 2) {
                col = 0;
                row++;
            }
        }

        return grid;
    }

    private void clearButtonSelectionInGrid(GridPane grid) {
        for (var node : grid.getChildren()) {
            if (node instanceof Button) {
                node.setStyle(STYLE_DEFAULT);
            }
        }
    }

    // Подсвечивает аннотацию во всех вкладках
    private void highlightAnnotationInAllTabs(VBox root, MoveAnnotation annotation) {
        for (var node : root.getChildren()) {
            if (node instanceof TabPane tabPane) {
                for (Tab tab : tabPane.getTabs()) {
                    if (tab.getContent() instanceof GridPane grid) {
                        for (var btnNode : grid.getChildren()) {
                            if (btnNode instanceof Button btn && btn.getUserData() == annotation) {
                                btn.setStyle(STYLE_SELECTED);
                            }
                        }
                    }
                }
            }
        }
    }

    public record Result(MoveAnnotation annotation, String comment) {

        public boolean hasAnnotation() {
            return annotation != null;
        }

        public boolean hasComment() {
            return comment != null && !comment.trim().isEmpty();
        }
    }
}