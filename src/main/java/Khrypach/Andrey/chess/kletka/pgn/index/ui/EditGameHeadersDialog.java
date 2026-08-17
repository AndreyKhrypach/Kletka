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

import Khrypach.Andrey.chess.kletka.database.model.GameData;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

/**
 * Упрощенный диалог для редактирования заголовков партии в PGN браузере
 * (без редактирования тела партии и без изменения типа позиции)
 */
public class EditGameHeadersDialog {

    private static final Logger log = LoggerFactory.getLogger(EditGameHeadersDialog.class);
    private static final LanguageManager lang = LanguageManager.getInstance();

    private final Stage ownerStage;
    private final GameData gameData;
    private final String pgnBody;
    private final int gameId;

    // UI компоненты (точно как в SaveGameDialog)
    private Dialog<GameData> dialog;
    private TabPane tabPane;

    // Вкладка "Игроки и результат"
    private TextField whiteField;
    private TextField blackField;
    private TextField whiteEloField;
    private TextField blackEloField;
    private TextField whiteTeamField;
    private TextField blackTeamField;
    private TextField annotatorField;
    private RadioButton result1_0;
    private RadioButton result0_1;
    private RadioButton resultDraw;
    private RadioButton resultUnknown;

    // Вкладка "Турнир"
    private TextField eventField;
    private TextField siteField;
    private TextField roundField;
    private TextField subroundField;
    private ComboBox<Integer> yearCombo;
    private ComboBox<Integer> monthCombo;
    private ComboBox<Integer> dayCombo;

    // Вкладка "Детали"
    private TextField ecoField;
    private TextField openingField;
    private TextField variationField;
    private TextField timeControlField;
    private TextField sourceField;

    // ========== ПОЛЯ ДЛЯ ПОЗИЦИЙ (ТОЛЬКО ДЛЯ ЧТЕНИЯ) ==========
    private TextField fenField;
    private CheckBox setUpCheckBox;
    private ComboBox<String> positionTypeCombo;

    public EditGameHeadersDialog(Stage ownerStage, GameData gameData, String pgnBody, int gameId) {
        this.ownerStage = ownerStage;
        this.gameData = gameData;
        this.pgnBody = pgnBody;
        this.gameId = gameId;
        createDialog();
        fillFromGameData();
    }

    public GameData showAndWait() {
        return dialog.showAndWait().orElse(null);
    }

    private void createDialog() {
        dialog = new Dialog<>();
        dialog.setTitle(lang.get(PGN_BROWSER_EDIT_HEADERS_TITLE));
        dialog.setHeaderText(String.format(lang.get(PGN_BROWSER_EDIT_HEADERS_HEADER), gameId));
        dialog.initOwner(ownerStage);
        dialog.initModality(Modality.APPLICATION_MODAL);

        ButtonType saveButtonType = new ButtonType(
                lang.get(PGN_BROWSER_EDIT_SAVE),
                ButtonBar.ButtonData.OK_DONE
        );
        ButtonType cancelButtonType = new ButtonType(
                lang.get(PGN_BROWSER_EDIT_CANCEL),
                ButtonBar.ButtonData.CANCEL_CLOSE
        );
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab playersTab = new Tab(lang.get(SAVE_TAB_PLAYERS));
        playersTab.setClosable(false);
        playersTab.setContent(createPlayersGrid());

        Tab tournamentTab = new Tab(lang.get(SAVE_TAB_TOURNAMENT));
        tournamentTab.setClosable(false);
        tournamentTab.setContent(createTournamentGrid());

        Tab detailsTab = new Tab(lang.get(SAVE_TAB_DETAILS));
        detailsTab.setClosable(false);
        detailsTab.setContent(createDetailsGrid());

        tabPane.getTabs().addAll(playersTab, tournamentTab, detailsTab);

        dialog.getDialogPane().setContent(tabPane);
        dialog.getDialogPane().setPrefWidth(650);
        dialog.getDialogPane().setPrefHeight(550);

        dialog.setResultConverter(dialogButton -> {
            log.debug("create dialog completed");
            if (dialogButton == saveButtonType) {
                return collectGameData();
            }
            return null;
        });
    }

    /**
     * Создает GridPane для вкладки "Игроки и результат"
     */
    private GridPane createPlayersGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        whiteField = new TextField();
        whiteField.setPromptText(lang.get(PGN_BROWSER_EDIT_PROMPT_WHITE));
        blackField = new TextField();
        blackField.setPromptText(lang.get(PGN_BROWSER_EDIT_PROMPT_BLACK));
        whiteEloField = new TextField();
        whiteEloField.setPromptText("Elo");
        blackEloField = new TextField();
        blackEloField.setPromptText("Elo");
        whiteTeamField = new TextField();
        whiteTeamField.setPromptText("Team");
        blackTeamField = new TextField();
        blackTeamField.setPromptText("Team");
        annotatorField = new TextField();
        annotatorField.setPromptText("Annotator");

        ToggleGroup resultGroup = new ToggleGroup();
        result1_0 = new RadioButton(lang.get(SAVE_RESULT_1_0));
        result1_0.setToggleGroup(resultGroup);
        result1_0.setUserData("1-0");
        result0_1 = new RadioButton(lang.get(SAVE_RESULT_0_1));
        result0_1.setToggleGroup(resultGroup);
        result0_1.setUserData("0-1");
        resultDraw = new RadioButton(lang.get(SAVE_RESULT_DRAW));
        resultDraw.setToggleGroup(resultGroup);
        resultDraw.setUserData("1/2-1/2");
        resultUnknown = new RadioButton(lang.get(SAVE_RESULT_UNKNOWN));
        resultUnknown.setToggleGroup(resultGroup);
        resultUnknown.setUserData("*");
        resultUnknown.setSelected(true);

        HBox resultBox = new HBox(10, result1_0, result0_1, resultDraw, resultUnknown);

        int row = 0;
        grid.add(new Label(lang.get(SAVE_LABEL_WHITE)), 0, row);
        grid.add(whiteField, 1, row);
        grid.add(new Label(lang.get(SAVE_LABEL_BLACK)), 2, row);
        grid.add(blackField, 3, row);
        row++;

        grid.add(new Label(lang.get(SAVE_LABEL_ELO_WHITE)), 0, row);
        grid.add(whiteEloField, 1, row);
        grid.add(new Label(lang.get(SAVE_LABEL_ELO_BLACK)), 2, row);
        grid.add(blackEloField, 3, row);
        row++;

        grid.add(new Label(lang.get(SAVE_LABEL_WHITE_TEAM)), 0, row);
        grid.add(whiteTeamField, 1, row);
        grid.add(new Label(lang.get(SAVE_LABEL_BLACK_TEAM)), 2, row);
        grid.add(blackTeamField, 3, row);
        row++;

        grid.add(new Label(lang.get(SAVE_LABEL_ANNOTATOR)), 0, row);
        grid.add(annotatorField, 1, row);
        row++;

        grid.add(new Label(lang.get(SAVE_LABEL_RESULT)), 0, row);
        grid.add(resultBox, 1, row, 3, 1);

        return grid;
    }

    /**
     * Создает GridPane для вкладки "Турнир"
     */
    private GridPane createTournamentGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        eventField = new TextField();
        eventField.setPromptText(lang.get(PGN_BROWSER_EDIT_PROMPT_EVENT));
        siteField = new TextField();
        siteField.setPromptText(lang.get(PGN_BROWSER_EDIT_PROMPT_SITE));
        roundField = new TextField();
        roundField.setPromptText(lang.get(PGN_BROWSER_EDIT_PROMPT_ROUND));
        subroundField = new TextField();
        subroundField.setPromptText("Subround");

        yearCombo = new ComboBox<>();
        for (int y = 1900; y <= 2030; y++) {
            yearCombo.getItems().add(y);
        }
        monthCombo = new ComboBox<>();
        for (int m = 1; m <= 12; m++) {
            monthCombo.getItems().add(m);
        }
        dayCombo = new ComboBox<>();
        for (int d = 1; d <= 31; d++) {
            dayCombo.getItems().add(d);
        }

        Button resetDateBtn = new Button(lang.get(SAVE_BUTTON_RESET_DATE));
        resetDateBtn.setOnAction(e -> {
            yearCombo.setValue(null);
            monthCombo.setValue(null);
            dayCombo.setValue(null);
        });

        HBox dateBox = new HBox(10,
                new Label(lang.get(SAVE_LABEL_YEAR)), yearCombo,
                new Label(lang.get(SAVE_LABEL_MONTH)), monthCombo,
                new Label(lang.get(SAVE_LABEL_DAY)), dayCombo,
                resetDateBtn
        );

        int row = 0;
        grid.add(new Label(lang.get(SAVE_LABEL_EVENT)), 0, row);
        grid.add(eventField, 1, row);
        grid.add(new Label(lang.get(SAVE_LABEL_SITE)), 2, row);
        grid.add(siteField, 3, row);
        row++;

        grid.add(new Label(lang.get(SAVE_LABEL_ROUND)), 0, row);
        grid.add(roundField, 1, row);
        grid.add(new Label(lang.get(SAVE_LABEL_SUBROUND)), 2, row);
        grid.add(subroundField, 3, row);
        row++;

        grid.add(new Label(lang.get(SAVE_LABEL_DATE)), 0, row);
        grid.add(dateBox, 1, row, 3, 1);

        return grid;
    }

    /**
     * Создает GridPane для вкладки "Детали"
     * ПОЛЯ ДЛЯ ПОЗИЦИИ - ТОЛЬКО ДЛЯ ЧТЕНИЯ
     */
    private GridPane createDetailsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ecoField = new TextField();
        ecoField.setPromptText("A00-E99");
        openingField = new TextField();
        openingField.setPromptText(lang.get(PGN_BROWSER_EDIT_PROMPT_OPENING));
        variationField = new TextField();
        variationField.setPromptText(lang.get(PGN_BROWSER_EDIT_PROMPT_VARIATION));
        timeControlField = new TextField();
        timeControlField.setPromptText("Time control");
        sourceField = new TextField();
        sourceField.setPromptText("Source");

        // ========== ПОЛЯ ДЛЯ ПОЗИЦИИ - ТОЛЬКО ДЛЯ ЧТЕНИЯ ==========
        fenField = new TextField();
        fenField.setPromptText("FEN (read only)");
        fenField.setEditable(false);  // НЕЛЬЗЯ РЕДАКТИРОВАТЬ
        fenField.setStyle("-fx-background-color: #f0f0f0;");

        setUpCheckBox = new CheckBox(lang.get(SAVE_CHECKBOX_SETUP));
        setUpCheckBox.setDisable(true);  // НЕЛЬЗЯ ИЗМЕНЯТЬ

        positionTypeCombo = new ComboBox<>();
        positionTypeCombo.getItems().addAll(
                lang.get(SAVE_TYPE_GAME),
                lang.get(SAVE_TYPE_POSITION),
                lang.get(SAVE_TYPE_STUDY),
                lang.get(SAVE_TYPE_PROBLEM)
        );
        positionTypeCombo.setDisable(true);  // НЕЛЬЗЯ ИЗМЕНЯТЬ
        positionTypeCombo.setStyle("-fx-opacity: 0.7;");

        // Информация о теле партии (только для чтения)
        Label bodyInfo = new Label();
        bodyInfo.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        if (pgnBody != null && !pgnBody.isEmpty()) {
            int moveCount = countMoves(pgnBody);
            bodyInfo.setText(String.format(lang.get(PGN_BROWSER_EDIT_BODY_INFO), moveCount));
        } else {
            bodyInfo.setText(lang.get(PGN_BROWSER_EDIT_BODY_EMPTY));
        }

        int row = 0;
        grid.add(new Label(lang.get(SAVE_LABEL_ECO)), 0, row);
        grid.add(ecoField, 1, row);
        grid.add(new Label(lang.get(SAVE_LABEL_OPENING)), 2, row);
        grid.add(openingField, 3, row);
        row++;

        grid.add(new Label(lang.get(SAVE_LABEL_VARIATION)), 0, row);
        grid.add(variationField, 1, row);
        grid.add(new Label(lang.get(SAVE_LABEL_TIME_CONTROL)), 2, row);
        grid.add(timeControlField, 3, row);
        row++;

        grid.add(new Label(lang.get(SAVE_LABEL_SOURCE)), 0, row);
        grid.add(sourceField, 1, row, 3, 1);
        row++;

        // Поля для позиции - с пометкой "только для чтения"
        Label readOnlyLabel = new Label(lang.get(PGN_BROWSER_MSG_EDIT_READ_ONLY));
        readOnlyLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #999;");

        grid.add(new Label(lang.get(SAVE_LABEL_FEN)), 0, row);
        grid.add(fenField, 1, row);
        grid.add(setUpCheckBox, 2, row);
        grid.add(readOnlyLabel, 3, row);
        row++;

        grid.add(new Label(lang.get(SAVE_LABEL_TYPE)), 0, row);
        grid.add(positionTypeCombo, 1, row);
        row++;

        grid.add(new Separator(), 0, row, 4, 1);
        row++;
        grid.add(bodyInfo, 0, row, 4, 1);

        return grid;
    }

    /**
     * Подсчитывает количество ходов в теле партии
     */
    private int countMoves(String pgnBody) {
        if (pgnBody == null || pgnBody.isEmpty()) return 0;
        String clean = pgnBody.replaceAll("[!?+×#]", "").trim();
        String[] moves = clean.split("\\s+");
        int count = 0;
        for (String move : moves) {
            if (move.matches("\\d+\\.")) continue;
            if (move.matches("[a-h][1-8][a-h][1-8][qrbn]?")) {
                count++;
            }
        }
        return count;
    }

    /**
     * Заполняет диалог данными из GameData
     */
    private void fillFromGameData() {
        if (gameData == null) return;

        setText(whiteField, gameData.whitePlayer());
        setText(blackField, gameData.blackPlayer());
        setText(whiteEloField, gameData.whiteElo());
        setText(blackEloField, gameData.blackElo());
        setText(whiteTeamField, gameData.whiteTeam());
        setText(blackTeamField, gameData.blackTeam());
        setText(annotatorField, gameData.annotator());

        String result = gameData.result();
        if ("1-0".equals(result)) {
            result1_0.setSelected(true);
        } else if ("0-1".equals(result)) {
            result0_1.setSelected(true);
        } else if ("1/2-1/2".equals(result)) {
            resultDraw.setSelected(true);
        } else {
            resultUnknown.setSelected(true);
        }

        setText(eventField, gameData.event());
        setText(siteField, gameData.site());
        setText(roundField, gameData.round());
        setText(subroundField, gameData.subround());

        if (gameData.date() != null) {
            setComboValue(yearCombo, gameData.date().getYear());
            setComboValue(monthCombo, gameData.date().getMonthValue());
            setComboValue(dayCombo, gameData.date().getDayOfMonth());
        }

        setText(ecoField, gameData.eco());
        setText(openingField, gameData.opening());
        setText(variationField, gameData.variation());
        setText(timeControlField, gameData.timeControl());
        setText(sourceField, gameData.source());

        // ========== ПОЛЯ ДЛЯ ПОЗИЦИИ - ТОЛЬКО ЗАПОЛНЯЕМ, НЕ ДАЕМ РЕДАКТИРОВАТЬ ==========
        String fen = gameData.fen();
        boolean isSetUp = gameData.isSetUp();
        String positionType = gameData.positionType();

        if (fen != null && !fen.isEmpty()) {
            fenField.setText(fen);
        } else {
            fenField.setText(lang.get(PGN_BROWSER_MSG_EDIT_READ_ONLY));
        }

        setUpCheckBox.setSelected(isSetUp);

        String typeDisplay = switch (positionType) {
            case "position" -> lang.get(SAVE_TYPE_POSITION);
            case "study" -> lang.get(SAVE_TYPE_STUDY);
            case "problem" -> lang.get(SAVE_TYPE_PROBLEM);
            default -> lang.get(SAVE_TYPE_GAME);
        };
        positionTypeCombo.setValue(typeDisplay);
    }

    /**
     * Собирает данные из диалога
     * ПОЛЯ ДЛЯ ПОЗИЦИИ БЕРУТСЯ ИЗ ИСХОДНЫХ ДАННЫХ (НЕ ИЗ ДИАЛОГА)
     */
    private GameData collectGameData() {
        // Получаем все значения из полей ввода
        String white = getText(whiteField);
        String black = getText(blackField);
        String whiteElo = getText(whiteEloField);
        String blackElo = getText(blackEloField);
        String whiteTeam = getText(whiteTeamField);
        String blackTeam = getText(blackTeamField);
        String annotator = getText(annotatorField);
        String result = getSelectedResult();

        String event = getText(eventField);
        String site = getText(siteField);
        String round = getText(roundField);
        String subround = getText(subroundField);
        LocalDate date = getSelectedDate();

        String eco = getText(ecoField);
        String opening = getText(openingField);
        String variation = getText(variationField);
        String timeControl = getText(timeControlField);
        String source = getText(sourceField);

        // ========== ПОЛЯ ДЛЯ ПОЗИЦИИ - БЕРУТСЯ ИЗ ИСХОДНЫХ ДАННЫХ ==========
        String fen = gameData != null ? gameData.fen() : "";
        boolean isSetUp = gameData != null && gameData.isSetUp();
        String positionType = gameData != null ? gameData.positionType() : "game";

        // Строим полный PGN с обновленными заголовками и оригинальным телом
        String fullPgn = buildFullPgn(
                white, black, result,
                whiteElo, blackElo,
                event, site, round, subround,
                eco, opening, variation,
                annotator, timeControl, source,
                whiteTeam, blackTeam,
                fen, isSetUp, positionType
        );

        return new GameData(
                white, black, result,
                whiteElo, blackElo,
                event, site, round, subround, date,
                eco, opening, variation,
                annotator, whiteTeam, blackTeam, source,
                "?", "?", timeControl,
                gameData != null ? gameData.plyCount() : "0",
                fullPgn,
                fen,
                isSetUp,
                positionType,
                false
        );
    }

    /**
     * Строит полный PGN с обновленными заголовками и оригинальным телом
     */
    private String buildFullPgn(
            String white, String black, String result,
            String whiteElo, String blackElo,
            String event, String site, String round, String subround,
            String eco, String opening, String variation,
            String annotator, String timeControl, String source,
            String whiteTeam, String blackTeam,
            String fen, boolean isSetUp, String positionType) {

        StringBuilder sb = new StringBuilder();

        sb.append("[Event \"").append(event).append("\"]\n");
        sb.append("[Site \"").append(site).append("\"]\n");
        sb.append("[Date \"").append(getSelectedDate()).append("\"]\n");
        sb.append("[Round \"").append(round).append("\"]\n");

        if (!"?".equals(subround) && !subround.isEmpty()) {
            sb.append("[Subround \"").append(subround).append("\"]\n");
        }

        sb.append("[White \"").append(white).append("\"]\n");
        sb.append("[Black \"").append(black).append("\"]\n");
        sb.append("[Result \"").append(result).append("\"]\n");

        if (!"?".equals(whiteElo) && !whiteElo.isEmpty()) {
            sb.append("[WhiteElo \"").append(whiteElo).append("\"]\n");
        }
        if (!"?".equals(blackElo) && !blackElo.isEmpty()) {
            sb.append("[BlackElo \"").append(blackElo).append("\"]\n");
        }
        if (!"?".equals(eco) && !eco.isEmpty()) {
            sb.append("[ECO \"").append(eco).append("\"]\n");
        }
        if (!"?".equals(opening) && !opening.isEmpty()) {
            sb.append("[Opening \"").append(opening).append("\"]\n");
        }
        if (!"?".equals(variation) && !variation.isEmpty()) {
            sb.append("[Variation \"").append(variation).append("\"]\n");
        }
        if (!"?".equals(annotator) && !annotator.isEmpty()) {
            sb.append("[Annotator \"").append(annotator).append("\"]\n");
        }
        if (!"?".equals(timeControl) && !timeControl.isEmpty()) {
            sb.append("[TimeControl \"").append(timeControl).append("\"]\n");
        }
        if (!"?".equals(source) && !source.isEmpty()) {
            sb.append("[Source \"").append(source).append("\"]\n");
        }
        if (!"?".equals(whiteTeam) && !whiteTeam.isEmpty()) {
            sb.append("[WhiteTeam \"").append(whiteTeam).append("\"]\n");
        }
        if (!"?".equals(blackTeam) && !blackTeam.isEmpty()) {
            sb.append("[BlackTeam \"").append(blackTeam).append("\"]\n");
        }

        if (isSetUp && fen != null && !fen.isEmpty()) {
            sb.append("[SetUp \"1\"]\n");
            sb.append("[FEN \"").append(fen).append("\"]\n");
        }

        if (positionType != null && !"game".equals(positionType)) {
            sb.append("[PositionType \"").append(positionType).append("\"]\n");
        }

        sb.append("\n");

        if (pgnBody != null && !pgnBody.isEmpty()) {
            sb.append(pgnBody);
        }

        return sb.toString();
    }

    /**
     * Получает выбранный результат
     */
    private String getSelectedResult() {
        if (result1_0.isSelected()) return "1-0";
        if (result0_1.isSelected()) return "0-1";
        if (resultDraw.isSelected()) return "1/2-1/2";
        return "*";
    }

    /**
     * Получает выбранную дату
     */
    private LocalDate getSelectedDate() {
        Integer year = yearCombo.getValue();
        Integer month = monthCombo.getValue();
        Integer day = dayCombo.getValue();
        if (year != null && month != null && day != null) {
            try {
                return LocalDate.of(year, month, day);
            } catch (Exception e) {
                return LocalDate.now();
            }
        }
        return LocalDate.now();
    }

    /**
     * Устанавливает текст в поле
     */
    private void setText(TextField field, String value) {
        if (field != null) {
            field.setText(value != null && !"?".equals(value) ? value : "");
        }
    }

    /**
     * Получает текст из поля
     */
    private String getText(TextField field) {
        if (field == null) return "?";
        String text = field.getText();
        return text != null && !text.trim().isEmpty() ? text.trim() : "?";
    }

    /**
     * Устанавливает значение в ComboBox
     */
    private void setComboValue(ComboBox<Integer> combo, Integer value) {
        if (combo != null && value != null) {
            combo.setValue(value);
        }
    }
}