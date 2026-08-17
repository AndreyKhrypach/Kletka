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

import Khrypach.Andrey.chess.kletka.database.eco.EcoEntry;
import Khrypach.Andrey.chess.kletka.database.eco.EcoService;
import Khrypach.Andrey.chess.kletka.database.model.GameData;
import Khrypach.Andrey.chess.kletka.database.parser.PgnParser;
import Khrypach.Andrey.chess.kletka.gui.board.ChessBoardView;
import Khrypach.Andrey.chess.kletka.gui.board.MoveNavigationController;
import Khrypach.Andrey.chess.kletka.gui.board.NotationView;
import Khrypach.Andrey.chess.kletka.gui.controllers.MainController;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.model.ParentNode;
import Khrypach.Andrey.chess.kletka.gui.model.RootNode;
import Khrypach.Andrey.chess.kletka.gui.model.Variation;
import com.github.bhlangonijr.chesslib.Board;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

/**
 * Диалог сохранения партии с данными (игроки, результат, турнир, дебют)
 * Как в ChessBase
 */
public class SaveGameDialog {

    private static final Logger log = LoggerFactory.getLogger(SaveGameDialog.class);
    private final LanguageManager lang = LanguageManager.getInstance();

    private final Stage ownerStage;
    private final ChessBoardView boardView;
    private final NotationView notationView;
    private final GameData existingData;
    private final boolean isEditMode;
    private final String pgnBody;

    // UI компоненты
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

    private TextField fenField;
    private CheckBox setUpCheckBox;
    private ComboBox<String> positionTypeCombo;

    @Getter
    private GameData resultGameData;

    @Getter
    @Setter
    private MainController mainController;

    public SaveGameDialog(Stage ownerStage, ChessBoardView boardView, NotationView notationView) {
        this.ownerStage = ownerStage;
        this.boardView = boardView;
        this.notationView = notationView;
        this.existingData = null;
        this.pgnBody = null;
        this.isEditMode = false;
        createDialog();

        fillFromCurrentGame();
    }

    /**
     * Показывает диалог и возвращает GameData
     */
    public GameData showAndWait() {
        return dialog.showAndWait().orElse(null);
    }

    /**
     * Создает диалог с тремя вкладками
     */
    private void createDialog() {
        dialog = new Dialog<>();
        dialog.setTitle(isEditMode ? lang.get(SAVE_DIALOG_TITLE_EDIT) : lang.get(SAVE_DIALOG_TITLE_SAVE));
        dialog.setHeaderText(isEditMode ? lang.get(SAVE_DIALOG_HEADER_EDIT) : lang.get(SAVE_DIALOG_HEADER_SAVE));
        dialog.initOwner(ownerStage);

        ButtonType saveButtonType = new ButtonType(
                isEditMode ? lang.get(SAVE_BUTTON_SAVE_CHANGES) : lang.get(SAVE_BUTTON_SAVE),
                ButtonBar.ButtonData.OK_DONE
        );
        ButtonType cancelButtonType = new ButtonType(lang.get(SAVE_BUTTON_CANCEL), ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType helpButtonType = new ButtonType(lang.get(SAVE_BUTTON_HELP), ButtonBar.ButtonData.HELP);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType, helpButtonType);

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
            if (dialogButton == saveButtonType) {
                resultGameData = collectGameData();
                return resultGameData;
            }
            if (dialogButton == helpButtonType) {
                showHelpDialog();
                return null;
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
        whiteField.setId("whitePlayer");
        blackField = new TextField();
        blackField.setId("blackPlayer");
        whiteEloField = new TextField();
        whiteEloField.setId("whiteElo");
        blackEloField = new TextField();
        blackEloField.setId("blackElo");
        whiteTeamField = new TextField();
        whiteTeamField.setId("whiteTeam");
        blackTeamField = new TextField();
        blackTeamField.setId("blackTeam");
        annotatorField = new TextField();
        annotatorField.setId("annotator");

        ToggleGroup resultGroup = new ToggleGroup();

        result1_0 = new RadioButton(lang.get(SAVE_RESULT_1_0));
        result1_0.setToggleGroup(resultGroup);
        result1_0.setId("result_1_0");
        result1_0.setUserData("1-0");

        result0_1 = new RadioButton(lang.get(SAVE_RESULT_0_1));
        result0_1.setToggleGroup(resultGroup);
        result0_1.setId("result_0_1");
        result0_1.setUserData("0-1");

        resultDraw = new RadioButton(lang.get(SAVE_RESULT_DRAW));
        resultDraw.setToggleGroup(resultGroup);
        resultDraw.setId("result_draw");
        resultDraw.setUserData("1/2-1/2");

        resultUnknown = new RadioButton(lang.get(SAVE_RESULT_UNKNOWN));
        resultUnknown.setToggleGroup(resultGroup);
        resultUnknown.setId("result_unknown");
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
        eventField.setId("event");
        siteField = new TextField();
        siteField.setId("site");
        roundField = new TextField();
        roundField.setId("round");
        subroundField = new TextField();
        subroundField.setId("subround");

        yearCombo = new ComboBox<>();
        for (int y = 2000; y <= 2030; y++) {
            yearCombo.getItems().add(y);
        }
        yearCombo.setId("year");

        monthCombo = new ComboBox<>();
        for (int m = 1; m <= 12; m++) {
            monthCombo.getItems().add(m);
        }
        monthCombo.setId("month");

        dayCombo = new ComboBox<>();
        for (int d = 1; d <= 31; d++) {
            dayCombo.getItems().add(d);
        }
        dayCombo.setId("day");

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
     */
    private GridPane createDetailsGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ecoField = new TextField();
        ecoField.setId("eco");
        ecoField.setPromptText("A00-E99");

        openingField = new TextField();
        openingField.setId("opening");

        variationField = new TextField();
        variationField.setId("variation");

        timeControlField = new TextField();
        timeControlField.setId("timeControl");

        sourceField = new TextField();
        sourceField.setId("source");

        fenField = new TextField();
        fenField.setId("fen");
        fenField.setPromptText("FEN позиции (если есть)");

        setUpCheckBox = new CheckBox(lang.get(SAVE_CHECKBOX_SETUP));
        setUpCheckBox.setId("setUp");

        positionTypeCombo = new ComboBox<>();
        positionTypeCombo.getItems().addAll(
                lang.get(SAVE_TYPE_GAME),
                lang.get(SAVE_TYPE_POSITION),
                lang.get(SAVE_TYPE_STUDY),
                lang.get(SAVE_TYPE_PROBLEM)
        );
        positionTypeCombo.setValue(lang.get(SAVE_TYPE_GAME));
        positionTypeCombo.setId("positionType");

        setUpCheckBox.setOnAction(e -> {
            if (setUpCheckBox.isSelected()) {
                positionTypeCombo.setValue(lang.get(SAVE_TYPE_POSITION));

                MoveNavigationController navController = boardView != null ? boardView.getNavController() : null;
                if (navController != null) {
                    Board initialBoard = navController.getInitialPosition();
                    if (initialBoard != null) {
                        fenField.setText(initialBoard.getFen());
                        fenField.setDisable(false);
                        return;
                    }
                }

                if (boardView != null) {
                    Board currentBoard = boardView.getCurrentBoard();
                    if (currentBoard != null) {
                        fenField.setText(currentBoard.getFen());
                        fenField.setDisable(false);
                        return;
                    }
                }
                fenField.setDisable(false);
            } else {
                positionTypeCombo.setValue(lang.get(SAVE_TYPE_GAME));
                fenField.setDisable(true);
                fenField.clear();
            }
        });
        fenField.setDisable(true);

        Button detectButton = new Button(lang.get(SAVE_BUTTON_DETECT_OPENING));
        detectButton.setStyle("-fx-font-weight: bold; -fx-background-color: #4CAF50; -fx-text-fill: white;");
        detectButton.setOnAction(e -> detectOpening());

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

        grid.add(new Label(lang.get(SAVE_LABEL_FEN)), 0, row);
        grid.add(fenField, 1, row);
        grid.add(setUpCheckBox, 2, row);
        row++;

        grid.add(new Label(lang.get(SAVE_LABEL_TYPE)), 0, row);
        grid.add(positionTypeCombo, 1, row);
        row++;

        grid.add(detectButton, 0, row, 4, 1);

        return grid;
    }

    /**
     * Определяет дебют по PGN телу или из boardView
     */
    private void detectOpening() {
        try {
            EcoService ecoService = EcoService.getInstance();
            if (!ecoService.isInitialized()) {
                showMessage(lang.get(SAVE_MSG_ECO_NOT_LOADED));
                return;
            }

            if (pgnBody != null && !pgnBody.isEmpty()) {
                log.debug("Detecting opening from PGN body (length: {})", pgnBody.length());

                EcoEntry entry = findOpeningByPgnBody(pgnBody);
                if (entry != null) {
                    ecoField.setText(entry.eco());
                    openingField.setText(entry.name());
                    showMessage(String.format(lang.get(SAVE_MSG_OPENING_FOUND), entry.eco(), entry.name()));
                    log.debug("Opening detected: {} - {}", entry.eco(), entry.name());
                    return;
                }
            }

            if (boardView != null) {
                MoveNavigationController navController = boardView.getNavController();
                if (navController != null) {
                    Variation mainLine = navController.getMainLine();
                    RootNode rootNode = (RootNode) navController.getRootVariation().getFirstNode();

                    if (mainLine != null && !mainLine.isEmpty() && rootNode != null) {
                        EcoEntry entry = ecoService.findOpeningByPgn(rootNode, mainLine);
                        if (entry != null) {
                            ecoField.setText(entry.eco());
                            openingField.setText(entry.name());
                            showMessage(String.format(lang.get(SAVE_MSG_OPENING_FOUND), entry.eco(), entry.name()));
                            log.debug("Opening detected from board: {} - {}", entry.eco(), entry.name());
                            return;
                        }
                    }
                }
            }

            showMessage(lang.get(SAVE_MSG_OPENING_NOT_FOUND));

        } catch (Exception e) {
            log.error("Error detecting opening", e);
            showMessage(String.format(lang.get(SAVE_MSG_OPENING_ERROR), e.getMessage()));
        }
    }

    /**
     * Ищет дебют по PGN телу
     */
    private EcoEntry findOpeningByPgnBody(String body) {
        if (body == null || body.isEmpty()) {
            return null;
        }

        try {
            String fullPgn = buildFullPgnWithBody(body);
            if (fullPgn == null || fullPgn.isEmpty()) {
                return null;
            }

            PgnParser parser = new PgnParser();
            Khrypach.Andrey.chess.kletka.database.model.GameTree gameTree = parser.parseToGameTree(fullPgn);

            if (gameTree == null || gameTree.isEmpty()) {
                log.warn("Failed to parse PGN tree");
                return null;
            }

            EcoService ecoService = EcoService.getInstance();
            return ecoService.findOpeningByPgn(gameTree.getRootNode(), gameTree.getMainLine());

        } catch (Exception e) {
            log.error("Error finding opening by PGN body", e);
            return null;
        }
    }

    /**
     * Строит полный PGN с заголовками и телом
     */
    private String buildFullPgnWithBody(String body) {
        if (body == null || body.isEmpty()) {
            return null;
        }

        return "[Event \"" + getText(eventField) + "\"]\n" +
                "[Site \"" + getText(siteField) + "\"]\n" +
                "[Date \"" + LocalDate.now() + "\"]\n" +
                "[Round \"" + getText(roundField) + "\"]\n" +
                "[White \"" + getText(whiteField) + "\"]\n" +
                "[Black \"" + getText(blackField) + "\"]\n" +
                "[Result \"" + getSelectedResult() + "\"]\n" +
                "[Deleted \"false\"]\n" +
                "\n" +
                body +
                " " + getSelectedResult();
    }

    /**
     * Показывает вспомогательное сообщение
     */
    private void showMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(lang.get(NOTIFICATION_INFO));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Заполняет диалог данными из текущей партии
     */
    private void fillFromCurrentGame() {
        GameData gameData = createGameDataFromCurrentGame();
        if (gameData != null) {
            fillFromGameData(gameData);
        }
    }

    /**
     * Создает GameData из текущей партии (обновлено для позиций)
     */
    private GameData createGameDataFromCurrentGame() {
        if (notationView == null) return null;

        String pgn = notationView.getCurrentPGN();
        if (pgn == null || pgn.isEmpty()) return null;

        String white = lang.get(DEFAULT_PLAYER_NAME);
        String black = lang.get(DEFAULT_PLAYER_NAME);
        String result = "*";
        String event = "Kletka Game";
        String site = "?";
        String round = "?";
        String whiteElo = "?";
        String blackElo = "?";

        String fen = "";
        boolean isSetUp = false;
        String positionType = "game";

        String[] lines = pgn.split("\n");
        for (String line : lines) {
            if (line.startsWith("[White ")) {
                white = extractTagValue(line);
            } else if (line.startsWith("[Black ")) {
                black = extractTagValue(line);
            } else if (line.startsWith("[Event ")) {
                event = extractTagValue(line);
            } else if (line.startsWith("[Site ")) {
                site = extractTagValue(line);
            } else if (line.startsWith("[Round ")) {
                round = extractTagValue(line);
            } else if (line.startsWith("[WhiteElo ")) {
                whiteElo = extractTagValue(line);
            } else if (line.startsWith("[BlackElo ")) {
                blackElo = extractTagValue(line);
            } else if (line.startsWith("[Result ")) {
                result = extractTagValue(line);
            } else if (line.startsWith("[FEN ")) {
                fen = extractTagValue(line);
            } else if (line.startsWith("[SetUp ")) {
                isSetUp = "1".equals(extractTagValue(line));
            } else if (line.startsWith("[PositionType ")) {
                positionType = extractTagValue(line);
            }
        }

        if ("*".equals(result)) {
            String gameResult = notationView.getGameResult();
            if (gameResult != null && !"*".equals(gameResult)) {
                result = gameResult;
            }
        }

        int fullMoves = calculatePlyCount();

        return new GameData(
                white, black, result,
                whiteElo, blackElo,
                event, site, round, "?",
                LocalDate.now(),
                "?", "?", "?",
                "?", "?", "?", "?",
                "?", "?", "?",
                String.valueOf(fullMoves),
                pgn,
                fen,
                isSetUp,
                positionType,
                false
        );
    }

    /**
     * Извлекает значение из тега PGN
     */
    private String extractTagValue(String tagLine) {
        int start = tagLine.indexOf('"');
        int end = tagLine.lastIndexOf('"');
        if (start != -1 && end != -1 && start < end) {
            return tagLine.substring(start + 1, end);
        }
        return "?";
    }

    /**
     * Вычисляет количество полуходов (ply)
     * Для режима редактирования используем existingData или pgnBody
     */
    private int calculatePlyCount() {
        if (isEditMode && existingData != null) {
            String plyCount = existingData.plyCount();
            if (plyCount != null && !plyCount.isEmpty() && !"?".equals(plyCount)) {
                try {
                    return Integer.parseInt(plyCount);
                } catch (NumberFormatException e) {
                    // ignore
                }
            }

            String pgn = existingData.pgn();
            if (pgn != null && !pgn.isEmpty()) {
                return calculatePlyFromPgn(pgn);
            }
        }

        if (pgnBody != null && !pgnBody.isEmpty()) {
            return calculatePlyFromPgn(pgnBody);
        }

        if (boardView != null) {
            MoveNavigationController navController = boardView.getNavController();
            if (navController != null) {
                Variation mainLine = navController.getMainLine();
                if (mainLine != null && !mainLine.isEmpty()) {
                    ParentNode lastNode = mainLine.getLastNode();
                    if (lastNode != null && !lastNode.isRoot()) {
                        return lastNode.getAbsolutePly();
                    }
                }
            }
        }

        return 0;
    }

    /**
     * Вычисляет количество полуходов из PGN строки
     */
    private int calculatePlyFromPgn(String pgn) {
        if (pgn == null || pgn.isEmpty()) {
            return 0;
        }

        try {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\.");
            java.util.regex.Matcher matcher = pattern.matcher(pgn);

            int lastMoveNumber = 0;
            while (matcher.find()) {
                try {
                    int num = Integer.parseInt(matcher.group(1));
                    if (num > lastMoveNumber) {
                        lastMoveNumber = num;
                    }
                } catch (NumberFormatException e) {
                    // ignore
                }
            }

            if (lastMoveNumber > 0) {
                String afterLastNumber = pgn.substring(pgn.lastIndexOf(lastMoveNumber + "."));
                boolean hasBlackMove = afterLastNumber.matches(".*\\d+\\.\\s*\\S+\\s+\\S+.*");
                return hasBlackMove ? lastMoveNumber * 2 : lastMoveNumber * 2 - 1;
            }

            String clean = pgn.replaceAll("[!?+×#]", "").trim();
            String[] moves = clean.split("\\s+");
            return moves.length;

        } catch (Exception e) {
            log.warn("Failed to calculate ply from PGN: {}", e.getMessage());
            return 0;
        }
    }

    private GameData collectGameData() {
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

        String fen = getText(fenField);
        boolean isSetUp = setUpCheckBox.isSelected();

        // Получаем тип из комбобокса с локализацией
        String typeDisplay = positionTypeCombo.getValue();
        String positionType;
        if (lang.get(SAVE_TYPE_POSITION).equals(typeDisplay)) {
            positionType = "position";
        } else if (lang.get(SAVE_TYPE_STUDY).equals(typeDisplay)) {
            positionType = "study";
        } else if (lang.get(SAVE_TYPE_PROBLEM).equals(typeDisplay)) {
            positionType = "problem";
        } else {
            positionType = "game";
        }

        if (isSetUp && (fen.isEmpty() || "?".equals(fen))) {
            if (boardView != null) {
                Board currentBoard = boardView.getCurrentBoard();
                if (currentBoard != null) {
                    fen = currentBoard.getFen();
                }
            }
        }

        String pgn;
        if (isEditMode && existingData != null) {
            String body = extractBody(existingData.pgn());
            pgn = buildPgnWithHeadersAndBody(white, black, result, event, site, round,
                    eco, opening, annotator, whiteElo, blackElo,
                    timeControl, source, whiteTeam, blackTeam,
                    body, fen, isSetUp, positionType);
        } else if (notationView != null) {
            pgn = notationView.getCurrentPGN();
        } else {
            pgn = "";
        }

        int fullMoves = calculatePlyCount();

        return new GameData(
                white, black, result,
                whiteElo, blackElo,
                event, site, round, subround, date,
                eco, opening, variation,
                annotator, whiteTeam, blackTeam, source,
                "?", "?", timeControl,
                String.valueOf(fullMoves),
                pgn,
                fen,
                isSetUp,
                positionType,
                false
        );
    }

    /**
     * Извлекает тело партии (без заголовков)
     */
    private String extractBody(String fullPgn) {
        if (fullPgn == null || fullPgn.isEmpty()) {
            return "";
        }

        int lastBracket = fullPgn.lastIndexOf(']');
        if (lastBracket < 0) {
            return fullPgn;
        }

        int bodyStart = -1;
        for (int i = lastBracket + 1; i < fullPgn.length() - 1; i++) {
            if (fullPgn.charAt(i) == '\n' && fullPgn.charAt(i + 1) == '\n') {
                bodyStart = i + 2;
                break;
            }
        }

        if (bodyStart < 0) {
            String after = fullPgn.substring(lastBracket + 1).trim();
            if (!after.isEmpty() && !after.startsWith("[")) {
                return after;
            }
            return "";
        }

        return fullPgn.substring(bodyStart).trim();
    }

    /**
     * Строит PGN с заголовками и телом
     */
    private String buildPgnWithHeadersAndBody(String white, String black, String result,
                                              String event, String site, String round,
                                              String eco, String opening, String annotator,
                                              String whiteElo, String blackElo,
                                              String timeControl, String source,
                                              String whiteTeam, String blackTeam,
                                              String body, String fen, boolean isSetUp,
                                              String positionType) {
        StringBuilder sb = new StringBuilder();

        sb.append("[Event \"").append(event).append("\"]\n");
        sb.append("[Site \"").append(site).append("\"]\n");
        sb.append("[Date \"").append(LocalDate.now()).append("\"]\n");
        sb.append("[Round \"").append(round).append("\"]\n");
        sb.append("[White \"").append(white).append("\"]\n");
        sb.append("[Black \"").append(black).append("\"]\n");
        sb.append("[Result \"").append(result).append("\"]\n");

        if (eco != null && !"?".equals(eco) && !eco.isEmpty()) {
            sb.append("[ECO \"").append(eco).append("\"]\n");
        }
        if (whiteElo != null && !"?".equals(whiteElo) && !whiteElo.isEmpty()) {
            sb.append("[WhiteElo \"").append(whiteElo).append("\"]\n");
        }
        if (blackElo != null && !"?".equals(blackElo) && !blackElo.isEmpty()) {
            sb.append("[BlackElo \"").append(blackElo).append("\"]\n");
        }
        if (opening != null && !"?".equals(opening) && !opening.isEmpty()) {
            sb.append("[Opening \"").append(opening).append("\"]\n");
        }
        if (annotator != null && !"?".equals(annotator) && !annotator.isEmpty()) {
            sb.append("[Annotator \"").append(annotator).append("\"]\n");
        }
        if (timeControl != null && !"?".equals(timeControl) && !timeControl.isEmpty()) {
            sb.append("[TimeControl \"").append(timeControl).append("\"]\n");
        }
        if (source != null && !"?".equals(source) && !source.isEmpty()) {
            sb.append("[Source \"").append(source).append("\"]\n");
        }
        if (whiteTeam != null && !"?".equals(whiteTeam) && !whiteTeam.isEmpty()) {
            sb.append("[WhiteTeam \"").append(whiteTeam).append("\"]\n");
        }
        if (blackTeam != null && !"?".equals(blackTeam) && !blackTeam.isEmpty()) {
            sb.append("[BlackTeam \"").append(blackTeam).append("\"]\n");
        }

        if (isSetUp && fen != null && !fen.isEmpty()) {
            sb.append("[SetUp \"1\"]\n");
            sb.append("[FEN \"").append(fen).append("\"]\n");
        }

        if (positionType != null && !"game".equals(positionType)) {
            sb.append("[PositionType \"").append(positionType).append("\"]\n");
        }

        sb.append("[Deleted \"false\"]\n");
        sb.append("\n");

        if (body != null && !body.isEmpty()) {
            sb.append(body);
        }

        return sb.toString();
    }

    /**
     * Получает выбранный результат из RadioButton
     */
    private String getSelectedResult() {
        if (result1_0.isSelected()) return "1-0";
        if (result0_1.isSelected()) return "0-1";
        if (resultDraw.isSelected()) return "1/2-1/2";
        return "*";
    }

    /**
     * Получает выбранную дату из ComboBox
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
     * Заполняет диалог данными из GameData (для редактирования)
     */
    public void fillFromGameData(GameData gameData) {
        if (gameData == null) {
            log.warn("fillFromGameData: gameData is null");
            return;
        }

        log.debug("fillFromGameData START");

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

        String fen = gameData.fen();
        boolean isSetUp = gameData.isSetUp();
        String positionType = gameData.positionType();

        if (isSetUp && fen != null && !fen.isEmpty()) {
            setUpCheckBox.setSelected(true);
            fenField.setText(fen);
            fenField.setDisable(false);
            // Устанавливаем тип в комбобокс
            String typeDisplay = switch (positionType) {
                case "position" -> lang.get(SAVE_TYPE_POSITION);
                case "study" -> lang.get(SAVE_TYPE_STUDY);
                case "problem" -> lang.get(SAVE_TYPE_PROBLEM);
                default -> lang.get(SAVE_TYPE_GAME);
            };
            positionTypeCombo.setValue(typeDisplay);
        } else {
            setUpCheckBox.setSelected(false);
            fenField.clear();
            fenField.setDisable(true);
            positionTypeCombo.setValue(lang.get(SAVE_TYPE_GAME));
        }
    }

    // ========== Утилитные методы для работы с UI ==========

    private void setText(TextField field, String value) {
        if (field != null) {
            field.setText(value != null && !"?".equals(value) ? value : "");
        }
    }

    private String getText(TextField field) {
        if (field == null) return "?";
        String text = field.getText();
        return text != null && !text.trim().isEmpty() ? text.trim() : "?";
    }

    private void setComboValue(ComboBox<Integer> combo, Integer value) {
        if (combo != null && value != null) {
            combo.setValue(value);
        }
    }

    /**
     * Показывает диалог помощи (обновлен для позиций)
     */
    private void showHelpDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(lang.get(SAVE_HELP_TITLE));
        alert.setHeaderText(lang.get(SAVE_HELP_HEADER));
        alert.setContentText(lang.get(SAVE_HELP_CONTENT));
        alert.showAndWait();
    }
}