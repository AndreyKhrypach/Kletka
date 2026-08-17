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
import Khrypach.Andrey.chess.kletka.gui.model.SanGenerator;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

public class PositionSetupDialog {

    private static final Logger log = LoggerFactory.getLogger(PositionSetupDialog.class);
    private static final Color BOARD_BORDER_COLOR = Color.rgb(80, 50, 25);
    private final LanguageManager lang = LanguageManager.getInstance();

    private final Stage dialogStage;
    private final Board resultBoard;
    private Side sideToMove;
    private boolean confirmed = false;

    private final Map<Piece, Image> pieceImageCache = new HashMap<>();
    private final Map<Piece, String> pieceImageMap = new HashMap<>();

    private static final int TILE_SIZE = 60;

    private GridPane boardGrid;
    private TextField fenField;

    private Piece selectedPiece = null;
    private boolean deleteMode = false;
    private ToggleButton deleteModeBtn;

    private Square dragSourceSquare = null;

    private VBox whitePieces;
    private VBox blackPieces;

    private CheckBox whiteKingSideCastling;
    private CheckBox whiteQueenSideCastling;
    private CheckBox blackKingSideCastling;
    private CheckBox blackQueenSideCastling;

    private final String[] files = {"a", "b", "c", "d", "e", "f", "g", "h"};
    private final String[] ranks = {"8", "7", "6", "5", "4", "3", "2", "1"};

    private RadioButton whiteMoveBtn;
    private RadioButton blackMoveBtn;

    private boolean isUpdatingSideToMove = false;

    public PositionSetupDialog(Stage owner, Board initialBoard) {
        this.resultBoard = initialBoard.clone();
        this.sideToMove = initialBoard.getSideToMove();

        initPieceImageMap();
        loadPieceImages();

        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(owner);
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.setTitle(lang.get(SETUP_TITLE));
        dialogStage.setResizable(true);
        dialogStage.setMinWidth(900);
        dialogStage.setMinHeight(700);
    }

    private void initPieceImageMap() {
        pieceImageMap.put(Piece.WHITE_KING, "wK.png");
        pieceImageMap.put(Piece.WHITE_QUEEN, "wQ.png");
        pieceImageMap.put(Piece.WHITE_ROOK, "wR.png");
        pieceImageMap.put(Piece.WHITE_BISHOP, "wB.png");
        pieceImageMap.put(Piece.WHITE_KNIGHT, "wN.png");
        pieceImageMap.put(Piece.WHITE_PAWN, "wP.png");
        pieceImageMap.put(Piece.BLACK_KING, "bK.png");
        pieceImageMap.put(Piece.BLACK_QUEEN, "bQ.png");
        pieceImageMap.put(Piece.BLACK_ROOK, "bR.png");
        pieceImageMap.put(Piece.BLACK_BISHOP, "bB.png");
        pieceImageMap.put(Piece.BLACK_KNIGHT, "bN.png");
        pieceImageMap.put(Piece.BLACK_PAWN, "bP.png");
    }

    private void loadPieceImages() {
        for (Map.Entry<Piece, String> entry : pieceImageMap.entrySet()) {
            try {
                String imagePath = "/images/pieces/" + entry.getValue();
                Image image = new Image(Objects.requireNonNull(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
                pieceImageCache.put(entry.getKey(), image);
            } catch (Exception e) {
                log.error("{}: {}", lang.get(SETUP_IMAGE_LOAD_ERROR), entry.getKey(), e);
            }
        }
    }

    public Board showAndWait() {
        SanGenerator.setSetupPosition(true);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f0f0;");
        root.setPadding(new Insets(15));

        VBox leftPanel = createLeftPanel();
        ScrollPane leftScrollPane = new ScrollPane(leftPanel);
        leftScrollPane.setFitToWidth(true);
        leftScrollPane.setPrefWidth(200);
        leftScrollPane.setStyle("-fx-background: #f0f0f0; -fx-background-color: #f0f0f0;");

        VBox centerPanel = createCenterPanel();

        VBox rightPanel = createRightPanel();
        ScrollPane rightScrollPane = new ScrollPane(rightPanel);
        rightScrollPane.setFitToWidth(true);
        rightScrollPane.setPrefWidth(280);
        rightScrollPane.setStyle("-fx-background: #f0f0f0; -fx-background-color: #f0f0f0;");

        root.setLeft(leftScrollPane);
        root.setCenter(centerPanel);
        root.setRight(rightScrollPane);

        Scene scene = new Scene(root);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();

        return confirmed ? resultBoard : null;
    }

    private VBox createLeftPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(10));
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setStyle("-fx-background-color: #d2b48c; -fx-border-radius: 5; -fx-border-color: #8b5a2b; -fx-border-width: 1;");

        Label instruction = new Label(lang.get(SETUP_SELECT_PIECE));
        instruction.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label whiteLabel = new Label(lang.get(SETUP_WHITE));
        whiteLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #5a3e1b;");

        whitePieces = new VBox(8);
        whitePieces.setAlignment(Pos.TOP_CENTER);

        Piece[] whitePiecesArray = {
                Piece.WHITE_KING, Piece.WHITE_QUEEN, Piece.WHITE_ROOK,
                Piece.WHITE_BISHOP, Piece.WHITE_KNIGHT, Piece.WHITE_PAWN
        };

        for (Piece piece : whitePiecesArray) {
            Button btn = createPieceButton(piece);
            btn.setMaxWidth(Double.MAX_VALUE);
            whitePieces.getChildren().add(btn);
        }

        Label blackLabel = new Label(lang.get(SETUP_BLACK));
        blackLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #5a3e1b;");
        blackLabel.setPadding(new Insets(10, 0, 0, 0));

        blackPieces = new VBox(8);
        blackPieces.setAlignment(Pos.TOP_CENTER);

        Piece[] blackPiecesArray = {
                Piece.BLACK_KING, Piece.BLACK_QUEEN, Piece.BLACK_ROOK,
                Piece.BLACK_BISHOP, Piece.BLACK_KNIGHT, Piece.BLACK_PAWN
        };

        for (Piece piece : blackPiecesArray) {
            Button btn = createPieceButton(piece);
            btn.setMaxWidth(Double.MAX_VALUE);
            blackPieces.getChildren().add(btn);
        }

        Button clearSelectedBtn = new Button(lang.get(SETUP_CLEAR_SELECTION));
        clearSelectedBtn.setStyle("-fx-background-color: #8b5a2b; -fx-text-fill: white; -fx-font-weight: bold;");
        clearSelectedBtn.setMaxWidth(Double.MAX_VALUE);
        clearSelectedBtn.setOnAction(e -> {
            selectedPiece = null;
            updateButtonStyles(null);
            updateBoardGrid();
        });

        panel.getChildren().addAll(instruction, whiteLabel, whitePieces, blackLabel, blackPieces, clearSelectedBtn);
        return panel;
    }

    private VBox createCenterPanel() {
        VBox panel = new VBox(10);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setPadding(new Insets(10));

        boardGrid = createBoardWithCoordinates();

        HBox modePanel = new HBox(10);
        modePanel.setAlignment(Pos.CENTER);
        modePanel.setPadding(new Insets(10, 0, 0, 0));

        deleteModeBtn = new ToggleButton(lang.get(SETUP_DELETE_MODE));
        deleteModeBtn.setStyle("-fx-font-size: 12px; -fx-padding: 5 15 5 15;");

        deleteModeBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
            deleteMode = newVal;
            if (deleteMode) {
                deleteModeBtn.setStyle("-fx-font-size: 12px; -fx-padding: 5 15 5 15; -fx-background-color: #dc143c; -fx-text-fill: white; -fx-font-weight: bold;");
                selectedPiece = null;
                updateButtonStyles(null);
                showTemporaryTooltip(deleteModeBtn);
            } else {
                deleteModeBtn.setStyle("-fx-font-size: 12px; -fx-padding: 5 15 5 15; -fx-background-color: #f0f0f0; -fx-text-fill: black;");
            }
            updateBoardGrid();
        });

        modePanel.getChildren().add(deleteModeBtn);

        Label infoLabel = new Label(lang.get(SETUP_INSTRUCTION));
        infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #5a3e1b; -fx-font-style: italic;");
        infoLabel.setPadding(new Insets(5, 0, 0, 0));

        Label deleteInfoLabel = new Label(lang.get(SETUP_DELETE_INSTRUCTION));
        deleteInfoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #5a3e1b; -fx-font-style: italic;");

        deleteModeBtn.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                infoLabel.setVisible(false);
                deleteInfoLabel.setVisible(true);
            } else {
                infoLabel.setVisible(true);
                deleteInfoLabel.setVisible(false);
            }
        });
        deleteInfoLabel.setVisible(false);

        panel.getChildren().addAll(boardGrid, modePanel, infoLabel, deleteInfoLabel);
        return panel;
    }

    private void showTemporaryTooltip(ButtonBase button) {
        Tooltip tooltip = new Tooltip(lang.get(SETUP_DELETE_MODE_TOOLTIP));
        tooltip.setShowDelay(javafx.util.Duration.millis(100));
        tooltip.setHideDelay(javafx.util.Duration.seconds(2));
        Tooltip.install(button, tooltip);
        button.setTooltip(tooltip);
        tooltip.show(button, button.getScene().getWindow().getX() + button.getLayoutX(),
                button.getScene().getWindow().getY() + button.getLayoutY() + 30);
    }

    private VBox createRightPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #d2b48c; -fx-border-radius: 5; -fx-border-color: #8b5a2b; -fx-border-width: 1;");

        Label castlingLabel = new Label(lang.get(SETUP_CASTLING_RIGHTS));
        castlingLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label whiteCastlingLabel = new Label(lang.get(SETUP_WHITE));
        whiteCastlingLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        whiteKingSideCastling = new CheckBox(lang.get(SETUP_CASTLING_WHITE_KING));
        whiteKingSideCastling.setSelected(true);
        whiteQueenSideCastling = new CheckBox(lang.get(SETUP_CASTLING_WHITE_QUEEN));
        whiteQueenSideCastling.setSelected(true);

        Label blackCastlingLabel = new Label(lang.get(SETUP_BLACK));
        blackCastlingLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        blackCastlingLabel.setPadding(new Insets(10, 0, 0, 0));

        blackKingSideCastling = new CheckBox(lang.get(SETUP_CASTLING_BLACK_KING));
        blackKingSideCastling.setSelected(true);
        blackQueenSideCastling = new CheckBox(lang.get(SETUP_CASTLING_BLACK_QUEEN));
        blackQueenSideCastling.setSelected(true);

        whiteKingSideCastling.setOnAction(e -> updateFenFromCastling());
        whiteQueenSideCastling.setOnAction(e -> updateFenFromCastling());
        blackKingSideCastling.setOnAction(e -> updateFenFromCastling());
        blackQueenSideCastling.setOnAction(e -> updateFenFromCastling());

        Button resetCastlingBtn = getResetCastlingBtn();

        Label moveLabel = new Label(lang.get(SETUP_SIDE_TO_MOVE));
        moveLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        moveLabel.setPadding(new Insets(10, 0, 0, 0));

        ToggleGroup sideGroup = new ToggleGroup();
        whiteMoveBtn = new RadioButton(lang.get(SETUP_WHITE));
        whiteMoveBtn.setToggleGroup(sideGroup);
        whiteMoveBtn.setSelected(sideToMove == Side.WHITE);

        blackMoveBtn = new RadioButton(lang.get(SETUP_BLACK));
        blackMoveBtn.setToggleGroup(sideGroup);
        blackMoveBtn.setSelected(sideToMove == Side.BLACK);

        ChangeListener<Toggle> sideChangeListener = (obs, old, newVal) -> {
            if (isUpdatingSideToMove || newVal == null) return;

            Side newSide = (newVal == whiteMoveBtn) ? Side.WHITE : Side.BLACK;
            if (newSide != sideToMove) {
                isUpdatingSideToMove = true;

                resultBoard.setSideToMove(newSide);
                if (isKingInCheck(newSide)) {
                    showTemporaryNotification(lang.get(SETUP_KING_IN_CHECK,
                            newSide == Side.WHITE ? lang.get(SETUP_WHITE).toLowerCase() : lang.get(SETUP_BLACK).toLowerCase()));

                    resultBoard.setSideToMove(sideToMove);

                    if (sideToMove == Side.WHITE) {
                        whiteMoveBtn.setSelected(true);
                    } else {
                        blackMoveBtn.setSelected(true);
                    }
                } else {
                    sideToMove = newSide;
                    updateFenField();
                }

                isUpdatingSideToMove = false;
            }
        };

        sideGroup.selectedToggleProperty().addListener(sideChangeListener);

        HBox moveBox = new HBox(20);
        moveBox.setAlignment(Pos.CENTER);
        moveBox.getChildren().addAll(whiteMoveBtn, blackMoveBtn);

        Label fenLabel = new Label(lang.get(SETUP_FEN));
        fenLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        fenLabel.setPadding(new Insets(10, 0, 0, 0));

        fenField = new TextField();
        fenField.setPromptText(lang.get(SETUP_FEN_PROMPT));
        fenField.setOnAction(e -> {
            try {
                resultBoard.loadFromFen(fenField.getText());
                resultBoard.setSideToMove(sideToMove);
                loadCastlingFromFen(fenField.getText());
                updateBoardGrid();
            } catch (Exception ex) {
                showAlert(lang.get(SETUP_FEN_INVALID));
            }
        });

        Button copyFenBtn = getCopyFenBtn();

        Label controlLabel = new Label(lang.get(SETUP_CONTROL));
        controlLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        controlLabel.setPadding(new Insets(10, 0, 0, 0));

        Button startBtn = new Button(lang.get(SETUP_START_POS));
        startBtn.setStyle("-fx-background-color: #8b5a2b; -fx-text-fill: white;");
        startBtn.setMaxWidth(Double.MAX_VALUE);
        startBtn.setOnAction(e -> {
            String startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
            loadFen(startFen);
        });

        Button clearBtn = new Button(lang.get(SETUP_CLEAR_ALL));
        clearBtn.setStyle("-fx-background-color: #8b5a2b; -fx-text-fill: white;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            String emptyFen = "8/8/8/8/8/8/8/8 w - - 0 1";
            loadFen(emptyFen);
        });

        Button okBtn = getOkBtn();

        Button cancelBtn = new Button(lang.get(SETUP_CANCEL));
        cancelBtn.setStyle("-fx-background-color: #8b0000; -fx-text-fill: white; -fx-font-weight: bold;");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setOnAction(e -> dialogStage.close());

        panel.getChildren().addAll(
                castlingLabel,
                whiteCastlingLabel, whiteKingSideCastling, whiteQueenSideCastling,
                blackCastlingLabel, blackKingSideCastling, blackQueenSideCastling,
                resetCastlingBtn,
                moveLabel, moveBox,
                fenLabel, fenField, copyFenBtn,
                controlLabel, startBtn, clearBtn,
                new Separator(),
                okBtn, cancelBtn
        );

        return panel;
    }

    private Button getOkBtn() {
        Button okBtn = new Button(lang.get(SETUP_APPLY));
        okBtn.setStyle("-fx-background-color: #2e8b57; -fx-text-fill: white; -fx-font-weight: bold;");
        okBtn.setMaxWidth(Double.MAX_VALUE);
        okBtn.setOnAction(e -> {
            confirmed = true;
            validateAndFixCastlingRights();
            validateAndFixSideToMove();
            resultBoard.setSideToMove(sideToMove);
            dialogStage.close();
        });
        return okBtn;
    }

    private Button getCopyFenBtn() {
        Button copyFenBtn = new Button(lang.get(SETUP_COPY_FEN));
        copyFenBtn.setStyle("-fx-background-color: #8b5a2b; -fx-text-fill: white;");
        copyFenBtn.setMaxWidth(Double.MAX_VALUE);
        copyFenBtn.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(fenField.getText());
            clipboard.setContent(content);
            showAlert(lang.get(SETUP_FEN_COPIED));
        });
        return copyFenBtn;
    }

    private Button getResetCastlingBtn() {
        Button resetCastlingBtn = new Button(lang.get(SETUP_RESET_CASTLING));
        resetCastlingBtn.setStyle("-fx-background-color: #8b5a2b; -fx-text-fill: white;");
        resetCastlingBtn.setMaxWidth(Double.MAX_VALUE);
        resetCastlingBtn.setOnAction(e -> {
            whiteKingSideCastling.setSelected(true);
            whiteQueenSideCastling.setSelected(true);
            blackKingSideCastling.setSelected(true);
            blackQueenSideCastling.setSelected(true);
            updateFenFromCastling();
        });
        return resetCastlingBtn;
    }

    private GridPane createBoardWithCoordinates() {
        GridPane boardGrid = new GridPane();
        boardGrid.setAlignment(Pos.CENTER);
        boardGrid.setPadding(new Insets(10));

        boardGrid.setStyle(
                "-fx-background-color: " + toRgbString(BOARD_BORDER_COLOR) + ";" +
                        "-fx-border-color: black;" +
                        "-fx-border-width: 3px;" +
                        "-fx-border-style: solid;"
        );

        String coordStyle = "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #f5e6d3; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 1, 0.5, 0, 0);";

        for (int col = 0; col < 8; col++) {
            Label label = new Label(files[col]);
            label.setAlignment(Pos.CENTER);
            label.setPrefSize(TILE_SIZE, 20);
            label.setStyle(coordStyle);
            boardGrid.add(label, col + 1, 0);
        }

        for (int col = 0; col < 8; col++) {
            Label label = new Label(files[col]);
            label.setAlignment(Pos.CENTER);
            label.setPrefSize(TILE_SIZE, 20);
            label.setStyle(coordStyle);
            boardGrid.add(label, col + 1, 9);
        }

        for (int row = 0; row < 8; row++) {
            Label label = new Label(ranks[row]);
            label.setAlignment(Pos.CENTER);
            label.setPrefSize(20, TILE_SIZE);
            label.setStyle(coordStyle);
            boardGrid.add(label, 0, row + 1);
        }

        for (int row = 0; row < 8; row++) {
            Label label = new Label(ranks[row]);
            label.setAlignment(Pos.CENTER);
            label.setPrefSize(20, TILE_SIZE);
            label.setStyle(coordStyle);
            boardGrid.add(label, 9, row + 1);
        }

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Square square = Square.squareAt((7 - row) * 8 + col);
                StackPane cell = createCell(square, row, col);
                boardGrid.add(cell, col + 1, row + 1);
            }
        }

        boardGrid.setOnDragOver(event -> {
            if (deleteMode) {
                event.acceptTransferModes(TransferMode.NONE);
                return;
            }

            Dragboard db = event.getDragboard();
            if (db.hasString() && db.getString().startsWith("MOVE:") && dragSourceSquare != null) {
                event.acceptTransferModes(TransferMode.MOVE);
            } else {
                event.acceptTransferModes(TransferMode.NONE);
            }
            event.consume();
        });

        return boardGrid;
    }

    private StackPane createCell(Square square, int row, int col) {
        StackPane cell = new StackPane();
        cell.setMinSize(TILE_SIZE, TILE_SIZE);
        cell.setPrefSize(TILE_SIZE, TILE_SIZE);
        cell.setStyle("-fx-border-color: #5a3e1b; -fx-border-width: 1;");

        Color baseColor = (row + col) % 2 == 0 ?
                Color.rgb(240, 217, 181) : Color.rgb(181, 136, 99);
        cell.setStyle("-fx-background-color: " + toRgbString(baseColor) + ";");

        Piece piece = resultBoard.getPiece(square);
        ImageView pieceImage;

        cell.setOnDragOver(event -> {
            if (deleteMode) {
                event.acceptTransferModes(TransferMode.NONE);
                return;
            }

            Dragboard db = event.getDragboard();
            if (db.hasString() && db.getString().startsWith("MOVE:") && dragSourceSquare != null) {
                event.acceptTransferModes(TransferMode.MOVE);

                if (piece == Piece.NONE) {
                    Rectangle highlight = new Rectangle(TILE_SIZE, TILE_SIZE);
                    highlight.setFill(Color.rgb(0, 255, 0, 0.2));
                    highlight.setStroke(Color.GREEN);
                    highlight.setStrokeWidth(2);
                    cell.getChildren().add(highlight);

                    cell.setOnDragExited(exitEvent -> cell.getChildren().removeIf(node -> node instanceof Rectangle &&
                            ((Rectangle) node).getFill() instanceof Color &&
                            ((Color) ((Rectangle) node).getFill()).getOpacity() < 0.5));
                }
            } else {
                event.acceptTransferModes(TransferMode.NONE);
            }
            event.consume();
        });

        cell.setOnDragDropped(event -> {
            if (deleteMode) {
                event.setDropCompleted(false);
                event.consume();
                return;
            }

            Dragboard db = event.getDragboard();
            if (db.hasString() && db.getString().startsWith("MOVE:") && dragSourceSquare != null) {
                Piece movingPiece = resultBoard.getPiece(dragSourceSquare);

                if (movingPiece != Piece.NONE && !dragSourceSquare.equals(square)) {
                    removePieceFromSquare(dragSourceSquare);
                    resultBoard.setPiece(movingPiece, square);
                    updateFenField();
                    updateBoardGrid();
                    showQuickMoveFeedback(cell);
                    event.setDropCompleted(true);
                } else {
                    event.setDropCompleted(false);
                }

                dragSourceSquare = null;
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });

        if (piece != Piece.NONE) {
            pieceImage = createPieceImage(piece);
            if (pieceImage != null) {
                cell.getChildren().add(pieceImage);
            }

            if (deleteMode) {
                Rectangle deleteOverlay = new Rectangle(TILE_SIZE, TILE_SIZE);
                deleteOverlay.setFill(Color.rgb(255, 0, 0, 0.3));
                deleteOverlay.setStroke(Color.RED);
                deleteOverlay.setStrokeWidth(2);
                cell.getChildren().add(deleteOverlay);
            }

            if (!deleteMode && pieceImage != null) {
                setupDragAndDrop(cell, pieceImage, square);
            }
        }

        cell.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (deleteMode) {
                    if (resultBoard.getPiece(square) != Piece.NONE) {
                        removePieceFromSquare(square);
                        updateFenField();
                        updateBoardGrid();
                        showQuickDeleteFeedback(cell);
                    }
                } else if (selectedPiece != null) {
                    resultBoard.setPiece(selectedPiece, square);
                    updateFenField();
                    updateBoardGrid();
                }
            }
        });

        cell.setOnContextMenuRequested(e -> {
            if (!deleteMode && selectedPiece != null) {
                Piece oppositeColorPiece = getOppositeColorPiece(selectedPiece);
                if (oppositeColorPiece != null) {
                    resultBoard.setPiece(oppositeColorPiece, square);
                    updateFenField();
                    updateBoardGrid();
                    selectedPiece = oppositeColorPiece;
                    updateButtonStyles(selectedPiece);
                }
            }
        });

        return cell;
    }

    private void setupDragAndDrop(StackPane cell, ImageView pieceImage, Square targetSquare) {
        pieceImage.setOnDragDetected(event -> {
            if (deleteMode) return;

            dragSourceSquare = targetSquare;

            Dragboard db = pieceImage.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString("MOVE:" + targetSquare.toString());
            db.setContent(content);

            ImageView dragView = new ImageView(pieceImage.getImage());
            dragView.setFitWidth(TILE_SIZE * 0.8);
            dragView.setFitHeight(TILE_SIZE * 0.8);

            db.setDragView(dragView.snapshot(null, null),
                    dragView.getFitWidth() / 2,
                    dragView.getFitHeight() / 2);

            event.consume();
        });

        cell.setOnDragOver(event -> {
            if (deleteMode) {
                event.acceptTransferModes(TransferMode.NONE);
                return;
            }

            Dragboard db = event.getDragboard();
            if (db.hasString() && db.getString().startsWith("MOVE:") && dragSourceSquare != null) {
                event.acceptTransferModes(TransferMode.MOVE);

                if (!cell.getChildren().isEmpty() && cell.getChildren().get(cell.getChildren().size() - 1) instanceof Rectangle) {
                    log.trace("Highlight already exists on cell, skipping creation");
                    return;
                }
                Rectangle highlight = new Rectangle(TILE_SIZE, TILE_SIZE);
                highlight.setFill(Color.rgb(0, 255, 0, 0.2));
                highlight.setStroke(Color.GREEN);
                highlight.setStrokeWidth(2);
                cell.getChildren().add(highlight);

                cell.setOnDragExited(exitEvent ->
                        cell.getChildren().removeIf(node ->
                                node instanceof Rectangle rect &&
                                        rect.getFill() instanceof Color color &&
                                        color.getOpacity() < 0.5
                        )
                );
            } else {
                event.acceptTransferModes(TransferMode.NONE);
            }
            event.consume();
        });

        cell.setOnDragDropped(event -> {
            if (deleteMode) {
                event.setDropCompleted(false);
                event.consume();
                return;
            }

            Dragboard db = event.getDragboard();
            if (db.hasString() && db.getString().startsWith("MOVE:") && dragSourceSquare != null) {
                Piece movingPiece = resultBoard.getPiece(dragSourceSquare);

                if (movingPiece != Piece.NONE && !dragSourceSquare.equals(targetSquare)) {
                    removePieceFromSquare(dragSourceSquare);
                    resultBoard.setPiece(movingPiece, targetSquare);
                    updateFenField();
                    updateBoardGrid();
                    showQuickMoveFeedback(cell);
                    event.setDropCompleted(true);
                } else {
                    event.setDropCompleted(false);
                }

                dragSourceSquare = null;
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });

        pieceImage.setOnDragDone(event -> {
            dragSourceSquare = null;
            event.consume();
        });

        pieceImage.setOnDragEntered(event -> {
            if (!deleteMode) {
                pieceImage.setOpacity(0.7);
            }
        });

        pieceImage.setOnDragExited(event -> pieceImage.setOpacity(1.0));
    }

    private void showQuickDeleteFeedback(StackPane cell) {
        Rectangle flash = new Rectangle(TILE_SIZE, TILE_SIZE);
        flash.setFill(Color.rgb(255, 255, 255, 0.5));
        cell.getChildren().add(flash);
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(150));
        delay.setOnFinished(e -> cell.getChildren().remove(flash));
        delay.play();
    }

    private void showQuickMoveFeedback(StackPane cell) {
        Rectangle flash = new Rectangle(TILE_SIZE, TILE_SIZE);
        flash.setFill(Color.rgb(0, 255, 0, 0.3));
        cell.getChildren().add(flash);
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(200));
        delay.setOnFinished(e -> cell.getChildren().remove(flash));
        delay.play();
    }

    private void removePieceFromSquare(Square square) {
        try {
            String fen = resultBoard.getFen();
            String[] parts = fen.split(" ");
            String[] rows = parts[0].split("/");
            int row = 7 - square.getRank().ordinal();
            int col = square.getFile().ordinal();

            String oldRow = rows[row];
            StringBuilder newRow = new StringBuilder();

            int pos = 0;
            for (int i = 0; i < oldRow.length(); i++) {
                char c = oldRow.charAt(i);
                if (Character.isDigit(c)) {
                    int emptyCount = Character.getNumericValue(c);
                    if (pos <= col && col < pos + emptyCount) {
                        int before = col - pos;
                        int after = emptyCount - before - 1;
                        if (before > 0) newRow.append(before);
                        newRow.append('1');
                        if (after > 0) newRow.append(after);
                    } else {
                        newRow.append(c);
                    }
                    pos += emptyCount;
                } else {
                    if (pos == col) {
                        newRow.append('1');
                    } else {
                        newRow.append(c);
                    }
                    pos++;
                }
            }

            rows[row] = newRow.toString();
            String newBoardPart = String.join("/", rows);
            String newFen = newBoardPart + " " + parts[1] + " " + parts[2] + " " +
                    parts[3] + " " + parts[4] + " " + parts[5];
            resultBoard.loadFromFen(newFen);
        } catch (Exception e) {
            log.error("Error when trying remove piece: {}", e.getMessage());
        }
    }

    private Button createPieceButton(Piece piece) {
        Button btn = new Button();
        btn.setMinHeight(50);
        btn.setStyle("-fx-background-color: #f0f0f0; -fx-border-radius: 5;");
        btn.setUserData(piece);

        ImageView img = createPieceImage(piece);
        if (img != null) {
            img.setFitWidth(40);
            img.setFitHeight(40);
            btn.setGraphic(img);
        }

        btn.setOnAction(e -> {
            if (!deleteMode) {
                selectedPiece = piece;
                updateButtonStyles(piece);
                btn.setStyle("-fx-background-color: #ffd700; -fx-border-radius: 5; -fx-border-width: 2; -fx-border-color: #8b5a2b;");
                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(200));
                delay.setOnFinished(ev -> updateButtonStyles(selectedPiece));
                delay.play();
            } else {
                Tooltip tooltip = new Tooltip(lang.get(LanguageKeys.SETUP_DELETE_MODE_ACTIVE_TOOLTIP));
                tooltip.setShowDelay(javafx.util.Duration.millis(0));
                Tooltip.install(btn, tooltip);
                tooltip.show(btn, btn.getScene().getWindow().getX() + btn.getLayoutX(),
                        btn.getScene().getWindow().getY() + btn.getLayoutY() + 30);
                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
                delay.setOnFinished(ev -> tooltip.hide());
                delay.play();
            }
        });

        return btn;
    }

    private void updateButtonStyles(Piece selected) {
        for (javafx.scene.Node node : whitePieces.getChildren()) {
            if (node instanceof Button btn) {
                var btnPiece = (Piece) btn.getUserData();
                if (btnPiece != null && btnPiece == selected) {
                    btn.setStyle("-fx-background-color: #ffd700; -fx-border-radius: 5; -fx-border-width: 2; -fx-border-color: #8b5a2b;");
                } else {
                    btn.setStyle("-fx-background-color: #f0f0f0; -fx-border-radius: 5;");
                }
            }
        }

        for (javafx.scene.Node node : blackPieces.getChildren()) {
            if (node instanceof Button btn) {
                var btnPiece = (Piece) btn.getUserData();
                if (btnPiece != null && btnPiece == selected) {
                    btn.setStyle("-fx-background-color: #ffd700; -fx-border-radius: 5; -fx-border-width: 2; -fx-border-color: #8b5a2b;");
                } else {
                    btn.setStyle("-fx-background-color: #f0f0f0; -fx-border-radius: 5;");
                }
            }
        }
    }

    private void updateFenFromCastling() {
        String currentFen = resultBoard.getFen();
        String[] parts = currentFen.split(" ");

        var whiteKing = resultBoard.getPiece(Square.E1);
        var whiteRookA1 = resultBoard.getPiece(Square.A1);
        var whiteRookH1 = resultBoard.getPiece(Square.H1);
        var blackKing = resultBoard.getPiece(Square.E8);
        var blackRookA8 = resultBoard.getPiece(Square.A8);
        var blackRookH8 = resultBoard.getPiece(Square.H8);

        StringBuilder castlingRights = new StringBuilder();

        if (whiteKingSideCastling.isSelected() && whiteKing == Piece.WHITE_KING && whiteRookH1 == Piece.WHITE_ROOK) {
            castlingRights.append("K");
        }

        if (whiteQueenSideCastling.isSelected() && whiteKing == Piece.WHITE_KING && whiteRookA1 == Piece.WHITE_ROOK) {
            castlingRights.append("Q");
        }

        if (blackKingSideCastling.isSelected() && blackKing == Piece.BLACK_KING && blackRookH8 == Piece.BLACK_ROOK) {
            castlingRights.append("k");
        }

        if (blackQueenSideCastling.isSelected() && blackKing == Piece.BLACK_KING && blackRookA8 == Piece.BLACK_ROOK) {
            castlingRights.append("q");
        }

        String newCastlingRights = !castlingRights.isEmpty() ? castlingRights.toString() : "-";
        String currentRights = parts.length > 2 ? parts[2] : "-";

        if (!newCastlingRights.equals(currentRights)) {
            whiteKingSideCastling.setSelected(false);
            whiteQueenSideCastling.setSelected(false);
            blackKingSideCastling.setSelected(false);
            blackQueenSideCastling.setSelected(false);

            if (newCastlingRights.contains("K")) whiteKingSideCastling.setSelected(true);
            if (newCastlingRights.contains("Q")) whiteQueenSideCastling.setSelected(true);
            if (newCastlingRights.contains("k")) blackKingSideCastling.setSelected(true);
            if (newCastlingRights.contains("q")) blackQueenSideCastling.setSelected(true);

            String newFen = parts[0] + " " + parts[1] + " " + newCastlingRights + " " +
                    (parts.length > 3 ? parts[3] : "-") + " " +
                    (parts.length > 4 ? parts[4] : "0") + " " +
                    (parts.length > 5 ? parts[5] : "1");

            try {
                resultBoard.loadFromFen(newFen);
                updateFenField();
            } catch (Exception e) {
                log.error("Error updating FEN: {}", e.getMessage());
            }
        }
    }

    private void loadCastlingFromFen(String fen) {
        String[] parts = fen.split(" ");
        if (parts.length >= 3) {
            String castlingRights = parts[2];
            whiteKingSideCastling.setSelected(castlingRights.contains("K"));
            whiteQueenSideCastling.setSelected(castlingRights.contains("Q"));
            blackKingSideCastling.setSelected(castlingRights.contains("k"));
            blackQueenSideCastling.setSelected(castlingRights.contains("q"));
        }
    }

    private void loadFen(String fen) {
        try {
            resultBoard.loadFromFen(fen);
            resultBoard.setSideToMove(sideToMove);

            validateAndFixCastlingRights();

            selectedPiece = null;
            updateButtonStyles(null);
            updateBoardGrid();

        } catch (Exception e) {
            showAlert(String.format(lang.get(LanguageKeys.SETUP_LOAD_POSITION_ERROR), e.getMessage()));
        }
    }

    private ImageView createPieceImage(Piece piece) {
        Image image = pieceImageCache.get(piece);
        if (image == null) return null;
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(TILE_SIZE * 0.8);
        imageView.setFitHeight(TILE_SIZE * 0.8);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    private String toRgbString(Color color) {
        return String.format("rgb(%d, %d, %d)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private void updateBoardGrid() {
        GridPane newBoardGrid = createBoardWithCoordinates();
        if (boardGrid.getParent() instanceof VBox parent) {
            int index = parent.getChildren().indexOf(boardGrid);
            parent.getChildren().set(index, newBoardGrid);
            boardGrid = newBoardGrid;
        }
    }

    private void updateFenField() {
        if (fenField != null) {
            fenField.setText(resultBoard.getFen());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(lang.get(NOTIFICATION_INFO));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Piece getOppositeColorPiece(Piece piece) {
        if (piece == null) return null;
        return switch (piece) {
            case WHITE_KING -> Piece.BLACK_KING;
            case WHITE_QUEEN -> Piece.BLACK_QUEEN;
            case WHITE_ROOK -> Piece.BLACK_ROOK;
            case WHITE_BISHOP -> Piece.BLACK_BISHOP;
            case WHITE_KNIGHT -> Piece.BLACK_KNIGHT;
            case WHITE_PAWN -> Piece.BLACK_PAWN;
            case BLACK_KING -> Piece.WHITE_KING;
            case BLACK_QUEEN -> Piece.WHITE_QUEEN;
            case BLACK_ROOK -> Piece.WHITE_ROOK;
            case BLACK_BISHOP -> Piece.WHITE_BISHOP;
            case BLACK_KNIGHT -> Piece.WHITE_KNIGHT;
            case BLACK_PAWN -> Piece.WHITE_PAWN;
            default -> null;
        };
    }

    private void validateAndFixCastlingRights() {
        String fen = resultBoard.getFen();
        String[] parts = fen.split(" ");
        if (parts.length < 3) return;

        String currentCastling = parts[2];

        Piece whiteKing = resultBoard.getPiece(Square.E1);
        Piece whiteRookA1 = resultBoard.getPiece(Square.A1);
        Piece whiteRookH1 = resultBoard.getPiece(Square.H1);
        Piece blackKing = resultBoard.getPiece(Square.E8);
        Piece blackRookA8 = resultBoard.getPiece(Square.A8);
        Piece blackRookH8 = resultBoard.getPiece(Square.H8);

        StringBuilder newCastling = new StringBuilder();

        if (currentCastling.contains("K") && whiteKing == Piece.WHITE_KING && whiteRookH1 == Piece.WHITE_ROOK) {
            newCastling.append("K");
        }

        if (currentCastling.contains("Q") && whiteKing == Piece.WHITE_KING && whiteRookA1 == Piece.WHITE_ROOK) {
            newCastling.append("Q");
        }

        if (currentCastling.contains("k") && blackKing == Piece.BLACK_KING && blackRookH8 == Piece.BLACK_ROOK) {
            newCastling.append("k");
        }

        if (currentCastling.contains("q") && blackKing == Piece.BLACK_KING && blackRookA8 == Piece.BLACK_ROOK) {
            newCastling.append("q");
        }

        String newCastlingRights = !newCastling.isEmpty() ? newCastling.toString() : "-";

        if (!newCastlingRights.equals(currentCastling)) {
            whiteKingSideCastling.setSelected(false);
            whiteQueenSideCastling.setSelected(false);
            blackKingSideCastling.setSelected(false);
            blackQueenSideCastling.setSelected(false);

            if (newCastlingRights.contains("K")) whiteKingSideCastling.setSelected(true);
            if (newCastlingRights.contains("Q")) whiteQueenSideCastling.setSelected(true);
            if (newCastlingRights.contains("k")) blackKingSideCastling.setSelected(true);
            if (newCastlingRights.contains("q")) blackQueenSideCastling.setSelected(true);

            String newFen = parts[0] + " " + parts[1] + " " + newCastlingRights + " " +
                    (parts.length > 3 ? parts[3] : "-") + " " +
                    (parts.length > 4 ? parts[4] : "0") + " " +
                    (parts.length > 5 ? parts[5] : "1");

            try {
                resultBoard.loadFromFen(newFen);
                updateFenField();
            } catch (Exception e) {
                log.error("Error on correction FEN: {}", e.getMessage());
            }
        }
    }

    private boolean isKingInCheck(Side side) {
        Square kingSquare = null;
        Piece targetKing = (side == Side.WHITE) ? Piece.WHITE_KING : Piece.BLACK_KING;

        for (Square square : Square.values()) {
            if (resultBoard.getPiece(square) == targetKing) {
                kingSquare = square;
                break;
            }
        }

        if (kingSquare == null) {
            return false;
        }

        try {
            Side originalSide = resultBoard.getSideToMove();

            resultBoard.setSideToMove(side == Side.WHITE ? Side.BLACK : Side.WHITE);

            var legalMoves = resultBoard.legalMoves();

            resultBoard.setSideToMove(originalSide);

            for (var move : legalMoves) {
                if (move.getTo() == kingSquare) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("Error checking check: {}", e.getMessage());
        }

        return false;
    }

    private void validateAndFixSideToMove() {
        Side currentSide = resultBoard.getSideToMove();
        Side newSide = (currentSide == Side.WHITE) ? Side.BLACK : Side.WHITE;

        boolean opponentKingInCheck = isKingInCheck(newSide);

        log.trace("validateAndFixSideToMove - Current side: {}, Opponent side: {}, Opponent king in check: {}",
                currentSide, newSide, opponentKingInCheck);

        if (opponentKingInCheck) {

            log.trace("Changing side from {} to {}", currentSide, newSide);

            resultBoard.setSideToMove(newSide);
            sideToMove = newSide;

            if (whiteMoveBtn != null && blackMoveBtn != null) {
                isUpdatingSideToMove = true;
                if (newSide == Side.WHITE) {
                    whiteMoveBtn.setSelected(true);
                } else {
                    blackMoveBtn.setSelected(true);
                }
                isUpdatingSideToMove = false;
            }

            showTemporaryNotification(String.format(lang.get(LanguageKeys.SETUP_SIDE_CHANGED_NOTIFICATION),
                    (newSide == Side.WHITE ? lang.get(LanguageKeys.SETUP_SIDE_CHANGED_WHITE) : lang.get(LanguageKeys.SETUP_SIDE_CHANGED_BLACK)),
                    (newSide == Side.WHITE ? lang.get(LanguageKeys.SETUP_SIDE_CHANGED_WHITE) : lang.get(LanguageKeys.SETUP_SIDE_CHANGED_BLACK))));
        } else {
            log.trace("Side to move is correct: {}", currentSide);
        }
    }

    private void showTemporaryNotification(String message) {
        if (dialogStage.getScene() == null) return;

        Label notification = new Label(message);
        notification.setStyle("-fx-background-color: #333333; -fx-text-fill: white; " +
                "-fx-padding: 8 15 8 15; -fx-background-radius: 5; " +
                "-fx-font-size: 12px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 3, 0.5, 0, 0);");
        notification.setOpacity(0);

        StackPane rootPane = (StackPane) dialogStage.getScene().getRoot();
        StackPane.setAlignment(notification, Pos.BOTTOM_CENTER);
        StackPane.setMargin(notification, new Insets(0, 0, 20, 0));
        rootPane.getChildren().add(notification);

        javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), notification);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));

        javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), notification);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> rootPane.getChildren().remove(notification));

        fadeIn.setOnFinished(e -> pause.play());
        pause.setOnFinished(e -> fadeOut.play());
        fadeIn.play();
    }
}