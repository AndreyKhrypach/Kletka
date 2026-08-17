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

package Khrypach.Andrey.chess.kletka.gui.model;

import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Узел, представляющий шахматный ход
 */
@Getter
@Setter
public class MoveNode extends ParentNode {

    private static final Logger log = LoggerFactory.getLogger(MoveNode.class);

    private final Move move;
    private final Piece piece;
    private final boolean isCapture;
    private final Piece promotionPiece;

    // Кэшированный SAN (вычисляется лениво)
    private transient String san;

    public MoveNode(Move move, Piece piece, boolean isCapture, Piece promotionPiece) {
        super();
        this.move = move;
        this.piece = piece;
        this.isCapture = isCapture;
        this.promotionPiece = promotionPiece;
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public String getUciMove() {
        String uci = move.getFrom().toString().toLowerCase() +
                move.getTo().toString().toLowerCase();
        if (promotionPiece != null) {
            uci += getPromotionChar(promotionPiece);
        }
        return uci;
    }

    @Override
    public String getSan() {
        // Если SAN уже вычислен — возвращаем его
        if (san != null) {
            return san;
        }

        // Пытаемся восстановить доску из savedFenBefore
        String fenBefore = getSavedFenBefore();
        if (fenBefore != null && !fenBefore.isEmpty()) {
            try {
                com.github.bhlangonijr.chesslib.Board board =
                        new com.github.bhlangonijr.chesslib.Board();
                board.loadFromFen(fenBefore);
                san = SanGenerator.generateSan(board, move, piece, isCapture, promotionPiece);
                return san;
            } catch (Exception e) {
                // Если не удалось восстановить доску — используем упрощенную генерацию
                log.trace("Failed to generate SAN with FEN: {}", fenBefore, e);
            }
        }

        // Fallback: упрощенная генерация без разрешения неоднозначности
        san = generateSimpleSan();
        return san;
    }

    /**
     * Упрощенная генерация SAN (без разрешения неоднозначности)
     * Используется как fallback
     */
    private String generateSimpleSan() {
        // Рокировка
        if (piece == Piece.WHITE_KING || piece == Piece.BLACK_KING) {
            if (isCastlingMove()) {
                return isKingsideCastling() ? "O-O" : "O-O-O";
            }
        }

        String pieceLetter = getPieceLetter(piece);
        String toSquare = move.getTo().toString().toLowerCase();

        // Пешка
        if (piece == Piece.WHITE_PAWN || piece == Piece.BLACK_PAWN) {
            StringBuilder san = new StringBuilder();
            if (isCapture) {
                String fromFile = move.getFrom().toString().toLowerCase().substring(0, 1);
                san.append(fromFile).append("x");
            }
            san.append(toSquare);
            if (promotionPiece != null) {
                san.append("=").append(getPromotionChar(promotionPiece).toUpperCase());
            }
            return san.toString();
        }

        // Фигура
        StringBuilder san = new StringBuilder();
        san.append(pieceLetter);
        if (isCapture) {
            san.append("x");
        }
        san.append(toSquare);
        return san.toString();
    }

    private String getPieceLetter(Piece piece) {
        return switch (piece) {
            case WHITE_KING, BLACK_KING -> "K";
            case WHITE_QUEEN, BLACK_QUEEN -> "Q";
            case WHITE_ROOK, BLACK_ROOK -> "R";
            case WHITE_BISHOP, BLACK_BISHOP -> "B";
            case WHITE_KNIGHT, BLACK_KNIGHT -> "N";
            default -> "";
        };
    }

    private String getPromotionChar(Piece piece) {
        return switch (piece) {
            case WHITE_ROOK, BLACK_ROOK -> "r";
            case WHITE_BISHOP, BLACK_BISHOP -> "b";
            case WHITE_KNIGHT, BLACK_KNIGHT -> "n";
            default -> "q";
        };
    }

    private boolean isCastlingMove() {
        Square from = move.getFrom();
        Square to = move.getTo();
        return (from == Square.E1 && (to == Square.G1 || to == Square.C1)) ||
                (from == Square.E8 && (to == Square.G8 || to == Square.C8));
    }

    private boolean isKingsideCastling() {
        Square from = move.getFrom();
        Square to = move.getTo();
        return (from == Square.E1 && to == Square.G1) ||
                (from == Square.E8 && to == Square.G8);
    }

    @Override
    public String toString() {
        return "MoveNode{" + getSan() + " [uuid=" + getNodeUuid().substring(0, 8) + "]}";
    }
}