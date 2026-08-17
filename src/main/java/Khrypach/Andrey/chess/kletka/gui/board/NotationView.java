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

package Khrypach.Andrey.chess.kletka.gui.board;

import Khrypach.Andrey.chess.kletka.database.model.GameData;
import Khrypach.Andrey.chess.kletka.gui.dialogs.MoveAnnotationDialog;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.model.*;
import Khrypach.Andrey.chess.kletka.gui.visitor.VariationTreeTraverser;
import Khrypach.Andrey.chess.kletka.gui.visitor.impl.HtmlTreeVisitor;
import Khrypach.Andrey.chess.kletka.gui.visitor.impl.PgnExportVisitor;
import com.github.bhlangonijr.chesslib.Board;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import netscape.javascript.JSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Objects;

import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

public class NotationView extends VBox {

    private static final int MIN_FONT_SIZE = 11;
    private static final int MAX_FONT_SIZE = 18;

    private static final Logger log = LoggerFactory.getLogger(NotationView.class);
    private final LanguageManager lang = LanguageManager.getInstance();

    private int currentMoveFontSize = 14;
    private int currentNameFontSize = 13;
    private int currentNumberFontSize = 12;

    private final ScrollPane scrollPane;
    private final WebView webView;
    private final WebEngine webEngine;
    private JavaScriptBridge jsBridge;

    private MoveNavigationController navController;

    @Getter
    private String gameResult = "*";

    @Getter
    @Setter
    private GameData currentGameData;

    private final Label titleLabel;
    private final Label playersInfoLabel;

    private final HBox buttonBox;

    private final Button toggleVisibilityButton;
    @Getter
    private boolean isNotationVisible = true;
    private static final String EYE_OPEN = "👁️";
    private static final String EYE_CLOSED = "🚫👁️";

    private final VBox notationContentContainer;

    public NotationView() {
        setSpacing(10);
        setPadding(new Insets(20));
        setPrefWidth(600);
        setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #8b5a2b; -fx-border-width: 2 0 0 2;");

        titleLabel = new Label(lang.get(NOTATION_TITLE));
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #5a3e1b;");
        titleLabel.setWrapText(true);

        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.getChildren().addAll(titleLabel);

        playersInfoLabel = new Label();
        playersInfoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #5a3e1b;");
        playersInfoLabel.setWrapText(true);
        updatePlayersInfo(null);

        toggleVisibilityButton = new Button(EYE_OPEN);
        toggleVisibilityButton.setStyle(
                "-fx-font-size: 18px; " +
                        "-fx-background-color: transparent; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 0 5 0 5;"
        );
        toggleVisibilityButton.setTooltip(new Tooltip("Показать/скрыть запись партии"));
        toggleVisibilityButton.setOnAction(e -> toggleNotationVisibility());

        notationContentContainer = new VBox();
        notationContentContainer.setSpacing(5);

        webView = new WebView();
        webView.setContextMenuEnabled(false);
        webView.setFontScale(1.0);
        webView.setStyle("-fx-background-color: white; -fx-border-color: #d2b48c; -fx-border-width: 1;");

        webView.setOnMouseClicked(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                handleRightClick(event.getScreenX(), event.getScreenY());
            }
        });

        webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        webEngine.setUserAgent("Kletka Chess Client");

        jsBridge = new JavaScriptBridge(null, this);

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                try {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaBridge", jsBridge);
                    log.debug("JavaScript bridge registered");
                } catch (Exception e) {
                    log.error("Failed to register JS bridge", e);
                }
            }
        });

        scrollPane = new ScrollPane(webView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background: white; -fx-border-color: #8b5a2b;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPadding(Insets.EMPTY);
        scrollPane.setStyle(
                "-fx-background: white; " +
                        "-fx-border-color: #8b5a2b; " +
                        "-fx-padding: 0; " +
                        "-fx-background-insets: 0;"
        );

        webView.setOnScroll(event -> {
            double deltaY = event.getDeltaY();
            double newVvalue = scrollPane.getVvalue() - deltaY / 1000.0;
            scrollPane.setVvalue(Math.max(0, Math.min(1, newVvalue)));
            event.consume();
        });

        notationContentContainer.getChildren().add(scrollPane);
        buttonBox = createButtonBox();

        getChildren().addAll(titleBox, toggleVisibilityButton, playersInfoLabel, notationContentContainer, buttonBox);
    }

    public void setNavController(MoveNavigationController navController) {
        this.navController = navController;
        this.jsBridge = new JavaScriptBridge(navController, this);

        Platform.runLater(() -> {
            if (webEngine.getLoadWorker().getState() == javafx.concurrent.Worker.State.SUCCEEDED) {
                try {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaBridge", jsBridge);
                    log.debug("JavaScript bridge updated");
                } catch (Exception e) {
                    log.error("Failed to update JS bridge", e);
                }
            }
        });
    }

    private void handleRightClick(double screenX, double screenY) {
        if (navController == null) {
            log.warn("navController is null, cannot show context menu");
            return;
        }

        ParentNode currentNode = navController.getCurrentNode();
        if (currentNode == null || currentNode.isRoot()) {
            log.trace("At root, no context menu");
            return;
        }

        if (!(currentNode instanceof MoveNode moveNode)) {
            return;
        }

        Variation currentVariation = navController.getCurrentVariation();
        if (currentVariation == null) {
            return;
        }

        log.trace("Showing context menu for: {} in variation: {}",
                moveNode.getSan(), currentVariation.getName());

        ContextMenu contextMenu = createContextMenu(currentVariation, moveNode);
        contextMenu.show(webView, screenX, screenY);
    }

    private void toggleNotationVisibility() {
        isNotationVisible = !isNotationVisible;
        notationContentContainer.setVisible(isNotationVisible);
        notationContentContainer.setManaged(isNotationVisible);
        toggleVisibilityButton.setText(isNotationVisible ? EYE_OPEN : EYE_CLOSED);
        toggleVisibilityButton.setTooltip(new Tooltip(
                isNotationVisible ? lang.get(NOTATION_TOGGLE_HIDE) : lang.get(NOTATION_TOGGLE_SHOW)
        ));
        requestLayout();
    }

    public void setNotationVisible(boolean visible) {
        if (this.isNotationVisible != visible) {
            toggleNotationVisibility();
        }
    }

    private HBox createButtonBox() {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);

        Button resetButton = new Button(lang.get(NOTATION_NEW_GAME));
        resetButton.setStyle("-fx-background-color: #8b5a2b; -fx-text-fill: white; -fx-font-weight: bold;");
        resetButton.setOnAction(e -> {
            if (getScene() != null && getScene().getRoot() != null) {
                ChessBoardView boardView = (ChessBoardView) getScene().getRoot().getUserData();
                if (boardView != null && boardView.getMainController() != null) {
                    boardView.getMainController().resetGame();
                }
            }
        });

        Button copyButton = new Button(lang.get(NOTATION_COPY_PGN));
        copyButton.setStyle("-fx-background-color: #a0522d; -fx-text-fill: white;");
        copyButton.setOnAction(e -> copyPgnToClipboard());

        Button copyUnicodeButton = new Button(lang.get(NOTATION_COPY_PGN_UNICODE));
        copyUnicodeButton.setStyle("-fx-background-color: #2e8b57; -fx-text-fill: white; -fx-font-weight: bold;");
        copyUnicodeButton.setOnAction(e -> copyPgnUnicodeToClipboard());

        Button zoomOutButton = new Button("🔍-");
        zoomOutButton.setStyle("-fx-background-color: #4682b4; -fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 40px;");
        zoomOutButton.setOnAction(e -> decreaseFontSize());

        Button zoomInButton = new Button("🔍+");
        zoomInButton.setStyle("-fx-background-color: #4682b4; -fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 40px;");
        zoomInButton.setOnAction(e -> increaseFontSize());

        Label fontSizeLabel = new Label(currentMoveFontSize + "px");
        fontSizeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #5a3e1b; -fx-min-width: 40px;");
        fontSizeLabel.setUserData("fontSizeLabel");

        box.getChildren().addAll(resetButton, copyButton, copyUnicodeButton, zoomOutButton, zoomInButton, fontSizeLabel);
        return box;
    }

    private void increaseFontSize() {
        if (currentMoveFontSize < MAX_FONT_SIZE) {
            currentMoveFontSize = Math.min(currentMoveFontSize + 1, MAX_FONT_SIZE);
            currentNameFontSize = Math.min(currentNameFontSize + 1, MAX_FONT_SIZE - 1);
            currentNumberFontSize = Math.min(currentNumberFontSize + 1, MAX_FONT_SIZE - 2);
            refreshDisplay();
        }
    }

    private void decreaseFontSize() {
        if (currentMoveFontSize > MIN_FONT_SIZE) {
            currentMoveFontSize = Math.max(currentMoveFontSize - 1, MIN_FONT_SIZE);
            currentNameFontSize = Math.max(currentNameFontSize - 1, MIN_FONT_SIZE - 1);
            currentNumberFontSize = Math.max(currentNumberFontSize - 1, MIN_FONT_SIZE - 2);
            refreshDisplay();
        }
    }

    private void updateFontSizeLabel() {
        if (buttonBox != null) {
            for (var child : buttonBox.getChildren()) {
                if (child instanceof Label && "fontSizeLabel".equals(child.getUserData())) {
                    ((Label) child).setText(currentMoveFontSize + "px");
                    break;
                }
            }
        }
    }

    public void refreshDisplay() {
        updateFontSizeLabel();
        updateNotationDisplayWithVisitor();
    }

    public void updateNotationDisplayWithVisitor() {
        Platform.runLater(() -> {
            if (navController == null) {
                log.warn("navController is null");
                return;
            }

            Variation mainLine = navController.getMainLine();
            RootNode rootNode = (RootNode) navController.getRootVariation().getFirstNode();

            if (rootNode == null || mainLine == null) {
                webEngine.loadContent("<html><body style='font-family: monospace; padding: 20px; color: #888;'>" +
                        lang.get(NOTATION_NO_MOVES) + "</body></html>");
                return;
            }

            MoveNode activeNode = null;
            ParentNode currentNode = navController.getCurrentNode();
            if (currentNode instanceof MoveNode) {
                activeNode = (MoveNode) currentNode;
            }

            HtmlTreeVisitor htmlVisitor = new HtmlTreeVisitor(activeNode, currentMoveFontSize);
            htmlVisitor.setGameResult(gameResult);

            VariationTreeTraverser traverser = new VariationTreeTraverser();
            traverser.traverse(rootNode, mainLine, htmlVisitor);

            String html = htmlVisitor.getResult();
            if (html == null || html.isEmpty()) {
                webEngine.loadContent("<html><body style='font-family: monospace; padding: 20px; color: #888;'>" +
                        lang.get(NOTATION_NO_DATA) + "</body></html>");
                return;
            }

            webEngine.loadContent(html);
            log.trace("HTML loaded, length: {}", html.length());

            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    try {
                        JSObject window = (JSObject) webEngine.executeScript("window");
                        window.setMember("javaBridge", jsBridge);
                        log.trace("JavaScript bridge re-registered");
                    } catch (Exception e) {
                        log.error("Failed to re-register JS bridge", e);
                    }
                    scrollToActiveMove();
                }
            });

            if (webEngine.getLoadWorker().getState() == javafx.concurrent.Worker.State.SUCCEEDED) {
                try {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaBridge", jsBridge);
                    log.trace("JavaScript bridge registered (already loaded)");
                } catch (Exception e) {
                    log.error("Failed to register JS bridge", e);
                }
                scrollToActiveMove();
            }
        });
    }

    private void scrollToActiveMove() {
        if (webEngine == null) return;

        try {
            webEngine.executeScript(
                    "try { " +
                            "    var active = document.querySelector('.move.active'); " +
                            "    if (active) { " +
                            "        active.scrollIntoView({ block: 'center', behavior: 'smooth' }); " +
                            "    } " +
                            "} catch(e) { }"
            );
        } catch (Exception e) {
            log.trace("Scroll to active move failed: {}", e.getMessage());
        }
    }

    public ContextMenu createContextMenu(Variation variation, MoveNode node) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem annotateItem;
        if (node.getComment() != null && !node.getComment().isEmpty()) {
            annotateItem = new MenuItem("✏️ " + lang.get(CONTEXT_MENU_EDIT_COMMENT));
        } else {
            annotateItem = new MenuItem("🏷️ " + lang.get(CONTEXT_MENU_ADD_ANNOTATION));
        }
        annotateItem.setOnAction(e -> showAnnotationDialog(node));
        contextMenu.getItems().add(annotateItem);

        if (node.getAnnotation() != null || (node.getComment() != null && !node.getComment().isEmpty())) {
            MenuItem removeAnnotationItem = new MenuItem("🗑️ " + lang.get(CONTEXT_MENU_REMOVE_ANNOTATION));
            removeAnnotationItem.setOnAction(e -> {
                node.setAnnotation(null);
                node.setComment(null);
                node.getAdditionalAnnotations().clear();

                if (navController.getBoardView().getMainController() != null) {
                    navController.getBoardView().getMainController().updateCurrentGameData();
                }

                updateNotationDisplayWithVisitor();
            });
            contextMenu.getItems().add(removeAnnotationItem);
        }

        contextMenu.getItems().add(new SeparatorMenuItem());

        if (variation != navController.getMainLine() && variation != navController.getRootVariation()) {
            MenuItem makeMainItem = new MenuItem(lang.get(CONTEXT_MENU_MAKE_MAIN));
            makeMainItem.setOnAction(e -> {
                if (navController != null) {
                    navController.makeCurrentVariationMainLine();
                    updateNotationDisplayWithVisitor();
                }
            });
            contextMenu.getItems().add(makeMainItem);
        }

        if (node.getNext() != null || !node.getSubVariations().isEmpty()) {
            contextMenu.getItems().add(new SeparatorMenuItem());
            MenuItem deleteAfterItem = new MenuItem(lang.get(CONTEXT_MENU_DELETE_AFTER));
            deleteAfterItem.setStyle("-fx-text-fill: #cc0000;");
            deleteAfterItem.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle(lang.get(CONFIRM_DELETE_TITLE));
                confirm.setHeaderText(lang.get(CONFIRM_DELETE_AFTER_HEADER,
                        ChessSymbols.convertToChessSymbols(node.getSan())));
                confirm.setContentText(lang.get(CONFIRM_DELETE_CONTENT));
                ButtonType yesButton = new ButtonType(lang.get(CONFIRM_DELETE_YES), ButtonBar.ButtonData.YES);
                ButtonType noButton = new ButtonType(lang.get(CONFIRM_DELETE_NO), ButtonBar.ButtonData.CANCEL_CLOSE);
                confirm.getButtonTypes().setAll(yesButton, noButton);
                confirm.showAndWait().ifPresent(response -> {
                    if (response == yesButton) {
                        deleteAllMovesAfter(node);
                    }
                });
            });
            contextMenu.getItems().add(deleteAfterItem);
        }

        if (!variation.isMainLine()) {
            contextMenu.getItems().add(new SeparatorMenuItem());
            MenuItem deleteVariationItem = new MenuItem(lang.get(CONTEXT_MENU_DELETE_VARIATION));
            deleteVariationItem.setStyle("-fx-text-fill: #cc0000;");
            deleteVariationItem.setOnAction(e -> deleteCurrentVariation());
            contextMenu.getItems().add(deleteVariationItem);
        }

        if (variation == navController.getMainLine()) {
            contextMenu.getItems().add(new SeparatorMenuItem());
            MenuItem whiteWin = new MenuItem(lang.get(CONTEXT_MENU_RESULT_WHITE_WIN));
            whiteWin.setOnAction(e -> setGameResult("1-0"));
            MenuItem blackWin = new MenuItem(lang.get(CONTEXT_MENU_RESULT_BLACK_WIN));
            blackWin.setOnAction(e -> setGameResult("0-1"));
            MenuItem draw = new MenuItem(lang.get(CONTEXT_MENU_RESULT_DRAW));
            draw.setOnAction(e -> setGameResult("1/2-1/2"));
            MenuItem unknown = new MenuItem(lang.get(CONTEXT_MENU_RESULT_UNKNOWN));
            unknown.setOnAction(e -> setGameResult("*"));
            contextMenu.getItems().addAll(whiteWin, blackWin, draw, unknown);
        }

        return contextMenu;
    }

    private void deleteAllMovesAfter(MoveNode node) {
        node.getSubVariations().clear();
        node.setNext(null);

        if (node.getParent() != null) {
            node.getParent().setNext(node);
        }

        ParentNode currentNode = navController.getCurrentNode();
        if (currentNode != null && currentNode != node && isNodeDescendantOf(node, currentNode)) {
            navController.setCurrentNode(node);
        }

        updateNotationDisplayWithVisitor();

        Board boardAfter = navController.recreateBoardAtNode(navController.getCurrentVariation(), node);
        if (navController.getBoardView() != null) {
            navController.getBoardView().setBoard(boardAfter);
            navController.getBoardView().refreshBoard();
        }
    }

    private void deleteCurrentVariation() {
        if (navController == null) return;

        Variation currentVar = navController.getCurrentVariation();

        if (currentVar == navController.getMainLine()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(lang.get(NOTIFICATION_WARNING));
            alert.setHeaderText(null);
            alert.setContentText(lang.get(CONTEXT_MENU_CANNOT_DELETE_MAIN));
            alert.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(lang.get(CONFIRM_DELETE_VARIATION_TITLE));
        confirm.setHeaderText(lang.get(CONFIRM_DELETE_VARIATION_HEADER, currentVar.getName()));
        confirm.setContentText(lang.get(CONFIRM_DELETE_VARIATION_CONTENT));

        ButtonType yesButton = new ButtonType(lang.get(CONFIRM_DELETE_YES), ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType(lang.get(CONFIRM_DELETE_NO), ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(yesButton, noButton);

        confirm.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                navController.deleteCurrentVariation();
                updateNotationDisplayWithVisitor();
            }
        });
    }

    private boolean isNodeDescendantOf(ParentNode ancestor, ParentNode descendant) {
        ParentNode current = descendant;
        while (current != null) {
            if (current == ancestor) return true;
            current = current.getParent();
        }
        return false;
    }

    private void showAnnotationDialog(MoveNode node) {
        Stage stage = (Stage) getScene().getWindow();
        MoveAnnotationDialog dialog = new MoveAnnotationDialog(stage, node.getSan(), node.getAnnotation(), node.getComment());

        dialog.showAndWait().ifPresent(result -> {
            if (result.hasAnnotation()) {
                node.setAnnotation(result.annotation());
            } else if (node.getAnnotation() != null) {
                node.setAnnotation(null);
            }

            node.setComment(result.comment() != null && !result.comment().trim().isEmpty()
                    ? result.comment()
                    : null);

            if (navController.getBoardView().getMainController() != null) {
                navController.getBoardView().getMainController().updateCurrentGameData();
            }

            updateNotationDisplayWithVisitor();
        });
    }

    public void setGameResult(String result) {
        this.gameResult = result;
        updateNotationDisplayWithVisitor();
    }

    public void resetGameResult() {
        this.gameResult = "*";
        updateNotationDisplayWithVisitor();
    }

    public boolean isEmpty() {
        if (navController == null) return true;
        Variation mainLine = navController.getMainLine();
        return mainLine == null || mainLine.isEmpty();
    }

    public void refreshFromMainLine() {
        log.trace("refreshFromMainLine called");
        updateNotationDisplayWithVisitor();
    }

    public void updateGameData(GameData gameData) {
        this.currentGameData = gameData;

        if (gameData != null && gameData.result() != null) {
            this.gameResult = gameData.result();
        } else {
            this.gameResult = "*";
        }

        updatePlayersInfo(gameData);

        if (gameData != null) {
            String eco = gameData.eco();
            String opening = gameData.opening();
            if (eco != null && !"?".equals(eco) && opening != null && !"?".equals(opening)) {
                updateOpeningDisplay(eco, opening);
            }
        }

        refreshDisplay();
    }

    public void clearGameData() {
        this.currentGameData = null;
        this.gameResult = "*";
        updatePlayersInfo(null);
        updateOpeningDisplay("", "");
        refreshDisplay();
    }

    public String getCurrentPGN() {
        return getCurrentPGN(Objects.requireNonNullElseGet(currentGameData, this::createDefaultGameData));
    }

    public String getCurrentPGN(GameData gameData) {
        log.trace("getCurrentPGN called with gameData: result={}, plyCount={}, fen={}, isSetUp={}",
                gameData != null ? gameData.result() : "null",
                gameData != null ? gameData.plyCount() : "null",
                gameData != null ? gameData.fen() : "null",
                gameData != null ? gameData.isSetUp() : "null");

        StringBuilder pgn = new StringBuilder();

        if (gameData == null) {
            gameData = createDefaultGameData();
        }

        pgn.append("[Event \"").append(gameData.event()).append("\"]\n");
        pgn.append("[Site \"").append(gameData.site()).append("\"]\n");
        pgn.append("[Date \"").append(gameData.date()).append("\"]\n");
        pgn.append("[Round \"").append(gameData.round()).append("\"]\n");
        pgn.append("[White \"").append(gameData.whitePlayer()).append("\"]\n");
        pgn.append("[Black \"").append(gameData.blackPlayer()).append("\"]\n");
        String resultToUse = gameData.result();
        if (resultToUse == null || "*".equals(resultToUse)) {
            resultToUse = this.gameResult;
        }
        pgn.append("[Result \"").append(resultToUse).append("\"]\n");

        if (gameData.eco() != null && !"?".equals(gameData.eco())) {
            pgn.append("[ECO \"").append(gameData.eco()).append("\"]\n");
        }
        if (gameData.whiteElo() != null && !"?".equals(gameData.whiteElo())) {
            pgn.append("[WhiteElo \"").append(gameData.whiteElo()).append("\"]\n");
        }
        if (gameData.blackElo() != null && !"?".equals(gameData.blackElo())) {
            pgn.append("[BlackElo \"").append(gameData.blackElo()).append("\"]\n");
        }
        if (gameData.opening() != null && !"?".equals(gameData.opening())) {
            pgn.append("[Opening \"").append(gameData.opening()).append("\"]\n");
        }
        if (gameData.variation() != null && !"?".equals(gameData.variation())) {
            pgn.append("[Variation \"").append(gameData.variation()).append("\"]\n");
        }
        if (gameData.annotator() != null && !"?".equals(gameData.annotator())) {
            pgn.append("[Annotator \"").append(gameData.annotator()).append("\"]\n");
        }
        if (gameData.whiteTeam() != null && !"?".equals(gameData.whiteTeam())) {
            pgn.append("[WhiteTeam \"").append(gameData.whiteTeam()).append("\"]\n");
        }
        if (gameData.blackTeam() != null && !"?".equals(gameData.blackTeam())) {
            pgn.append("[BlackTeam \"").append(gameData.blackTeam()).append("\"]\n");
        }
        if (gameData.source() != null && !"?".equals(gameData.source())) {
            pgn.append("[Source \"").append(gameData.source()).append("\"]\n");
        }
        if (gameData.timeControl() != null && !"?".equals(gameData.timeControl())) {
            pgn.append("[TimeControl \"").append(gameData.timeControl()).append("\"]\n");
        }

        if (gameData.plyCount() != null && !"?".equals(gameData.plyCount())) {
            pgn.append("[PlyCount \"").append(gameData.plyCount()).append("\"]\n");
        }

        if (gameData.date() != null) {
            int year = gameData.date().getYear();
            pgn.append("[EventDate \"").append(year).append(".??.??\"]\n");
        }

        if (gameData.isSetUp() && gameData.fen() != null && !gameData.fen().isEmpty()) {
            pgn.append("[SetUp \"1\"]\n");
            pgn.append("[FEN \"").append(gameData.fen()).append("\"]\n");
        }

        if (gameData.deleted()) {
            pgn.append("[Deleted \" true\"]\n");
        } else {
            pgn.append("[Deleted \"false\"]\n");
        }

        pgn.append("\n");

        String moves = getMovesFromTree();
        if (moves != null && !moves.isEmpty()) {
            pgn.append(moves);
        }

        if (!pgn.toString().trim().isEmpty()) {
            pgn.append(' ');
        }
        pgn.append(resultToUse);

        return pgn.toString();
    }

    private GameData createDefaultGameData() {
        return new GameData(
                lang.get(LanguageKeys.DEFAULT_PLAYER_NAME), lang.get(LanguageKeys.DEFAULT_PLAYER_NAME), "*",
                "?", "?",
                "Kletka Game", "?", "?",
                "?", LocalDate.now(),
                "?", "?", "?",
                "?", "?", "?", "?",
                "?", "?", "?", "?",
                "",
                "",
                false,
                "game",
                false
        );
    }

    private String getMovesFromTree() {
        if (navController == null) {
            return "";
        }

        Variation mainLine = navController.getMainLine();
        RootNode rootNode = (RootNode) navController.getRootVariation().getFirstNode();

        if (rootNode == null || mainLine == null) {
            return "";
        }

        PgnExportVisitor pgnVisitor = new PgnExportVisitor();
        VariationTreeTraverser traverser = new VariationTreeTraverser();
        return traverser.traverse(rootNode, mainLine, pgnVisitor);
    }

    private void copyPgnToClipboard() {
        String pgn = getCurrentPGN();
        ClipboardContent content = new ClipboardContent();
        content.putString(pgn);
        Clipboard.getSystemClipboard().setContent(content);
        showNotification(lang.get(NOTATION_PGN_COPIED));
    }

    private void copyPgnUnicodeToClipboard() {
        log.trace("copyPgnUnicodeToClipboard called");
        String pgn = getCurrentPGN();
        log.trace("PGN length: {}", pgn.length());
        String[] parts = pgn.split("\n\n", 2);
        String headers = parts[0];
        String body = parts.length > 1 ? parts[1] : "";

        String unicodeBody = ChessSymbols.convertToChessSymbols(body);
        String unicodePgn = headers + "\n\n" + unicodeBody;

        ClipboardContent content = new ClipboardContent();
        content.putString(unicodePgn);
        Clipboard.getSystemClipboard().setContent(content);
        showNotification(lang.get(NOTATION_PGN_UNICODE_COPIED));
    }

    private void showNotification(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(lang.get(NOTIFICATION_INFO));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void updatePlayersInfo(GameData gameData) {
        if (playersInfoLabel == null) return;

        String whiteName = lang.get(LanguageKeys.DEFAULT_PLAYER_NAME);
        String blackName = lang.get(LanguageKeys.DEFAULT_PLAYER_NAME);
        String whiteElo = "";
        String blackElo = "";

        if (gameData != null) {
            whiteName = gameData.whitePlayer() != null && !gameData.whitePlayer().isEmpty()
                    ? gameData.whitePlayer() : lang.get(LanguageKeys.DEFAULT_PLAYER_NAME);
            blackName = gameData.blackPlayer() != null && !gameData.blackPlayer().isEmpty()
                    ? gameData.blackPlayer() : lang.get(LanguageKeys.DEFAULT_PLAYER_NAME);

            whiteElo = gameData.whiteElo() != null && !"?".equals(gameData.whiteElo())
                    ? gameData.whiteElo() : "";
            blackElo = gameData.blackElo() != null && !"?".equals(gameData.blackElo())
                    ? gameData.blackElo() : "";
        }

        String whiteSymbol = "⬜";
        String blackSymbol = "■";

        StringBuilder info = new StringBuilder();

        info.append(whiteSymbol).append(" ").append(whiteName);
        if (!whiteElo.isEmpty()) {
            info.append(" (").append(whiteElo).append(")");
        }

        info.append("  —  ");

        info.append(blackSymbol).append(" ").append(blackName);
        if (!blackElo.isEmpty()) {
            info.append(" (").append(blackElo).append(")");
        }

        playersInfoLabel.setText(info.toString());
    }

    public void updateOpeningDisplay(String eco, String openingName) {
        if (titleLabel == null) return;

        if (eco != null && !eco.isEmpty() && openingName != null && !openingName.isEmpty()) {
            titleLabel.setText(lang.get(NOTATION_TITLE) + "  " + eco + " - " + openingName);
        } else {
            titleLabel.setText(lang.get(NOTATION_TITLE));
        }
    }

}