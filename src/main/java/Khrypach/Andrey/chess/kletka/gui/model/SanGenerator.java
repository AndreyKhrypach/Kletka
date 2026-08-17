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

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Генератор SAN с разрешением неоднозначности
 */
public class SanGenerator {

    private static final Logger log = LoggerFactory.getLogger(SanGenerator.class);

    // ========== ФЛАГ ДЛЯ РАССТАВЛЕННЫХ ПОЗИЦИЙ ==========
    @Getter
    private static boolean isSetupPosition = false;

    /**
     * Устанавливает режим расстановки позиции
     */
    public static void setSetupPosition(boolean setup) {
        isSetupPosition = setup;
        log.trace("Setup position mode: {}", setup);
    }

    /**
     * Сбрасывает режим расстановки позиции
     */
    public static void resetSetupPosition() {
        isSetupPosition = false;
        log.trace("Setup position mode reset");
    }

    public static String generateSan(Board board, Move move, Piece piece,
                                     boolean isCapture, Piece promotionPiece) {
        if (board == null || move == null || piece == null) {
            return getSimpleSan(move, piece, isCapture, promotionPiece);
        }

        // ========== РОКИРОВКА ==========
        if (isCastling(move, piece)) {
            return isKingsideCastling(move) ? "O-O" : "O-O-O";
        }

        // ========== ПЕШКА ==========
        if (piece == Piece.WHITE_PAWN || piece == Piece.BLACK_PAWN) {
            return generatePawnSan(board, move, isCapture, promotionPiece);
        }

        // ========== ФИГУРА (K, Q, R, B, N) ==========
        return generatePieceSan(board, move, piece, isCapture);
    }

    private static boolean isCastling(Move move, Piece piece) {
        if (piece != Piece.WHITE_KING && piece != Piece.BLACK_KING) {
            return false;
        }
        Square from = move.getFrom();
        Square to = move.getTo();
        return (from == Square.E1 && (to == Square.G1 || to == Square.C1)) ||
                (from == Square.E8 && (to == Square.G8 || to == Square.C8));
    }

    private static boolean isKingsideCastling(Move move) {
        Square from = move.getFrom();
        Square to = move.getTo();
        return (from == Square.E1 && to == Square.G1) ||
                (from == Square.E8 && to == Square.G8);
    }

    private static String generatePawnSan(Board board, Move move,
                                          boolean isCapture, Piece promotionPiece) {
        String toSquare = move.getTo().toString().toLowerCase();
        StringBuilder san = new StringBuilder();

        if (isCapture) {
            String fromFile = move.getFrom().toString().toLowerCase().substring(0, 1);
            san.append(fromFile).append("x");
        }

        san.append(toSquare);

        if (promotionPiece != null) {
            san.append("=").append(getPieceLetter(promotionPiece));
        }

        san.append(getCheckMateSymbol(board, move));
        return san.toString();
    }

    private static String generatePieceSan(Board board, Move move, Piece piece,
                                           boolean isCapture) {
        String pieceLetter = getPieceLetter(piece);
        String disambiguation = getDisambiguation(board, move, piece);
        String toSquare = move.getTo().toString().toLowerCase();
        String captureStr = isCapture ? "x" : "";

        return pieceLetter +
                disambiguation +
                captureStr +
                toSquare +
                getCheckMateSymbol(board, move);
    }

        /**
     * Разрешает неоднозначность для фигуры.
     */
    private static String getDisambiguation(Board board, Move move, Piece piece) {
        Square from = move.getFrom();
        Square to = move.getTo();

        List<Move> legalMoves = board.legalMoves();
        List<Square> candidates = new ArrayList<>();

        for (Move legalMove : legalMoves) {
            if (legalMove.getTo() != to) continue;
            if (legalMove.getFrom() == from) continue;

            Piece movingPiece = board.getPiece(legalMove.getFrom());
            if (movingPiece == piece) {
                candidates.add(legalMove.getFrom());
            }
        }

        if (candidates.isEmpty()) {
            return "";
        }

        char fromFile = move.getFrom().toString().toLowerCase().charAt(0);
        int fromRank = move.getFrom().getRank().ordinal() + 1;

        boolean hasSameFile = false;
        boolean hasSameRank = false;

        for (Square candidate : candidates) {
            char candidateFile = candidate.toString().toLowerCase().charAt(0);
            int candidateRank = candidate.getRank().ordinal() + 1;

            if (candidateFile == fromFile) {
                hasSameFile = true;
            }
            if (candidateRank == fromRank) {
                hasSameRank = true;
            }
        }

        StringBuilder result = new StringBuilder();

        if (hasSameFile) {
            result.append(fromRank);
        }
        if (hasSameRank) {
            result.append(fromFile);
        }

        if (!hasSameFile && !hasSameRank) {
            result.append(fromFile);
        }

        return result.toString();
    }

    /**
     * Получает символ шаха/мата для хода.
     * Учитывает режим расстановки позиции.
     */
    private static String getCheckMateSymbol(Board board, Move move) {
        try {
            Board testBoard = board.clone();

            // ========== ПРОВЕРКА: ход легальный? ==========
            if (!testBoard.isMoveLegal(move, true)) {
                return "";
            }

            testBoard.doMove(move);

            if (testBoard.isMated()) {
                log.trace("MATE detected!");
                return MoveAnnotation.MATE.getSymbol(); // "#"
            }

            if (testBoard.isKingAttacked()) {
                Side sideToMove = testBoard.getSideToMove();
                Square kingSquare = testBoard.getKingSquare(sideToMove);
                Side attackerSide = (sideToMove == Side.WHITE) ? Side.BLACK : Side.WHITE;

                int attackersCount = countAttackersViaReflection(testBoard, kingSquare, attackerSide);
                log.trace("attackers = {}", attackersCount);

                if (attackersCount > 1) {
                    return MoveAnnotation.DOUBLE_CHECK.getSymbol(); // "++"
                } else {
                    return MoveAnnotation.CHECK.getSymbol(); // "+"
                }
            }
        } catch (Exception e) {
            // ========== ИЗМЕНЕНО: log.trace вместо log.error ==========
            log.trace("Error in getCheckMateSymbol: {}", e.getMessage());
        }
        return "";
    }

    /**
     * Подсчитывает количество фигур, атакующих короля, используя рефлексию
     */
    private static int countAttackersViaReflection(Board board, Square kingSquare, Side attackerSide) {
        try {
            java.lang.reflect.Method method = Board.class.getDeclaredMethod(
                    "squareAttackedBy",
                    Square.class,
                    Side.class
            );
            method.setAccessible(true);

            long attackersMask = (long) method.invoke(board, kingSquare, attackerSide);
            return Long.bitCount(attackersMask);

        } catch (NoSuchMethodException e) {
            log.trace("Method squareAttackedBy not found: {}", e.getMessage());
            return countAttackersFallback(board, kingSquare, attackerSide);
        } catch (Exception e) {
            log.trace("Reflection error: {}", e.getMessage());
            return countAttackersFallback(board, kingSquare, attackerSide);
        }
    }

    /**
     * Fallback метод для подсчета атакующих
     */
    private static int countAttackersFallback(Board board, Square kingSquare, Side attackerSide) {
        int count = 0;

        for (Square square : Square.values()) {
            Piece piece = board.getPiece(square);
            if (piece == Piece.NONE) continue;
            if (piece.getPieceSide() != attackerSide) continue;

            try {
                Move attackMove = new Move(square, kingSquare, piece);
                Board tempBoard = board.clone();
                tempBoard.setPiece(Piece.NONE, kingSquare);

                if (tempBoard.isMoveLegal(attackMove, false)) {
                    count++;
                }
            } catch (Exception e) {
                // Ход нелегальный — пропускаем
            }
        }

        return count;
    }

    private static String getPieceLetter(Piece piece) {
        return switch (piece) {
            case WHITE_KING, BLACK_KING -> "K";
            case WHITE_QUEEN, BLACK_QUEEN -> "Q";
            case WHITE_ROOK, BLACK_ROOK -> "R";
            case WHITE_BISHOP, BLACK_BISHOP -> "B";
            case WHITE_KNIGHT, BLACK_KNIGHT -> "N";
            default -> "";
        };
    }

    private static String getSimpleSan(Move move, Piece piece,
                                       boolean isCapture, Piece promotionPiece) {
        if (move == null) {
            return "";
        }

        String toSquare = move.getTo().toString().toLowerCase();
        StringBuilder san = new StringBuilder();

        // Рокировка
        if (piece == Piece.WHITE_KING || piece == Piece.BLACK_KING) {
            Square from = move.getFrom();
            Square to = move.getTo();
            if ((from == Square.E1 && to == Square.G1) ||
                    (from == Square.E8 && to == Square.G8)) {
                return "O-O";
            }
            if ((from == Square.E1 && to == Square.C1) ||
                    (from == Square.E8 && to == Square.C8)) {
                return "O-O-O";
            }
        }

        // Пешка
        if (piece == Piece.WHITE_PAWN || piece == Piece.BLACK_PAWN) {
            if (isCapture) {
                String fromFile = move.getFrom().toString().toLowerCase().substring(0, 1);
                san.append(fromFile).append("x");
            }
            san.append(toSquare);
            if (promotionPiece != null) {
                san.append("=").append(getPieceLetter(promotionPiece));
            }
            return san.toString();
        }

        // ========== ИСПРАВЛЕНИЕ: проверка на null ==========
        if (piece == null) {
            san.append(toSquare);
            return san.toString();
        }

        // Фигура
        String pieceLetter = getPieceLetter(piece);
        san.append(pieceLetter);
        if (isCapture) {
            san.append("x");
        }
        san.append(toSquare);

        return san.toString();
    }
}