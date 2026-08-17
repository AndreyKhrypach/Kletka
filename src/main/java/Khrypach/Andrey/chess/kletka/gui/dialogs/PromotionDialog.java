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
import com.github.bhlangonijr.chesslib.Piece;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

public class PromotionDialog {

    private static final Logger log = LoggerFactory.getLogger(PromotionDialog.class);

    private final LanguageManager lang = LanguageManager.getInstance();

    private final Stage dialogStage;
    private Piece selectedPiece = null;
    private final boolean isWhite;

    private final Map<Piece, Image> pieceImageCache = new HashMap<>();
    private final Map<Piece, String> pieceImageMap = new HashMap<>();

    public PromotionDialog(Stage owner, boolean isWhite) {
        this.isWhite = isWhite;

        initPieceImageMap();
        loadPieceImages();

        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(owner);
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.setTitle(lang.get(PROMOTION_TITLE));
        dialogStage.setResizable(false);
    }

    private void initPieceImageMap() {
        pieceImageMap.put(Piece.WHITE_QUEEN, "wQ.png");
        pieceImageMap.put(Piece.WHITE_ROOK, "wR.png");
        pieceImageMap.put(Piece.WHITE_BISHOP, "wB.png");
        pieceImageMap.put(Piece.WHITE_KNIGHT, "wN.png");
        pieceImageMap.put(Piece.BLACK_QUEEN, "bQ.png");
        pieceImageMap.put(Piece.BLACK_ROOK, "bR.png");
        pieceImageMap.put(Piece.BLACK_BISHOP, "bB.png");
        pieceImageMap.put(Piece.BLACK_KNIGHT, "bN.png");
    }

    private void loadPieceImages() {
        for (Map.Entry<Piece, String> entry : pieceImageMap.entrySet()) {
            try {
                String imagePath = "/images/pieces/" + entry.getValue();
                Image image = new Image(Objects.requireNonNull(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
                pieceImageCache.put(entry.getKey(), image);
            } catch (Exception e) {
                log.error("{}: {}", lang.get(PROMOTION_IMAGE_LOAD_ERROR), entry.getKey(), e);
            }
        }
    }

    public Piece showAndWait() {
        log.trace("Showing promotion dialog for {}", isWhite ? "white" : "black");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f0f0f0;");
        root.setAlignment(Pos.CENTER);

        Label label = new Label(lang.get(PROMOTION_CHOOSE));
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        HBox piecesBox = new HBox(15);
        piecesBox.setAlignment(Pos.CENTER);

        Piece[] pieces;
        if (isWhite) {
            pieces = new Piece[]{Piece.WHITE_QUEEN, Piece.WHITE_ROOK, Piece.WHITE_BISHOP, Piece.WHITE_KNIGHT};
        } else {
            pieces = new Piece[]{Piece.BLACK_QUEEN, Piece.BLACK_ROOK, Piece.BLACK_BISHOP, Piece.BLACK_KNIGHT};
        }

        for (Piece piece : pieces) {
            Button btn = createPieceButton(piece);
            piecesBox.getChildren().add(btn);
        }

        root.getChildren().addAll(label, piecesBox);

        Scene scene = new Scene(root);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();

        log.trace("Promotion dialog result: {}", selectedPiece);
        return selectedPiece;
    }

    private Button createPieceButton(Piece piece) {
        Button btn = new Button();
        btn.setMinSize(70, 70);
        btn.setStyle("-fx-background-color: #d2b48c; -fx-border-radius: 5; -fx-border-color: #8b5a2b;");

        ImageView img = createPieceImage(piece);
        if (img != null) {
            img.setFitWidth(50);
            img.setFitHeight(50);
            btn.setGraphic(img);
        }

        btn.setOnAction(e -> {
            selectedPiece = piece;
            dialogStage.close();
        });

        return btn;
    }

    private ImageView createPieceImage(Piece piece) {
        Image image = pieceImageCache.get(piece);
        if (image == null) return null;

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(50);
        imageView.setFitHeight(50);
        imageView.setPreserveRatio(true);
        return imageView;
    }
}