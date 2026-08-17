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

package Khrypach.Andrey.chess.kletka.database.parser.move;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Парсит отдельные ходы в PGN
 * Конвертирует SAN в Move и обратно
 * <p>
 * Все методы являются pure-функциями (без состояния)
 */
public class MoveParser {

    private static final Logger log = LoggerFactory.getLogger(MoveParser.class);

    /**
     * Конвертирует SAN в Move
     * Перенесено из PgnParser.convertSanToMove()
     */
    public Move convertSanToMove(String san, Board board) {
        log.trace("convertSanToMove: '{}', board FEN: {}", san, board.getFen());

        if (san == null || san.trim().isEmpty()) {
            log.warn("SAN is null or empty");
            return null;
        }

        try {
            String cleanSan = san;

            if (cleanSan.endsWith("*")) {
                cleanSan = cleanSan.substring(0, cleanSan.length() - 1);
                log.trace("Removed '*' suffix in: {} -> {}", san, cleanSan);
            }
            cleanSan = cleanSan.replaceAll("[+#]+$", "");

            if ("O-O".equals(cleanSan)) {
                Side side = board.getSideToMove();
                if (side == Side.WHITE) {
                    return new Move(Square.E1, Square.G1);
                } else {
                    return new Move(Square.E8, Square.G8);
                }
            }
            if ("O-O-O".equals(cleanSan)) {
                Side side = board.getSideToMove();
                if (side == Side.WHITE) {
                    return new Move(Square.E1, Square.C1);
                } else {
                    return new Move(Square.E8, Square.C8);
                }
            }

            Move foundMove = findMoveBySan(cleanSan, board);
            if (foundMove != null) {
                log.trace("Found move: {} -> {}", foundMove.getFrom(), foundMove.getTo());
                return foundMove;
            }

            log.warn("No legal move found for SAN: {} (clean: {})", san, cleanSan);
            return null;

        } catch (Exception e) {
            log.warn("Error converting SAN {}: {}", san, e.getMessage());
            return null;
        }
    }

    /**
     * Находит ход по SAN с учетом превращения
     * Перенесено из PgnParser.findMoveBySan()
     */
    public Move findMoveBySan(String san, Board board) {
        log.trace("findMoveBySan: '{}', board FEN: {}", san, board.getFen());

        String cleanSan = san;

        if (cleanSan.endsWith("*")) {
            cleanSan = cleanSan.substring(0, cleanSan.length() - 1);
            log.trace("Removed '*' suffix: {} -> {}", san, cleanSan);
        }
        cleanSan = cleanSan.replaceAll("[+#]+$", "");

        Piece promotionPiece = null;

        if (cleanSan.contains("=")) {
            int eqIndex = cleanSan.indexOf('=');
            if (eqIndex + 1 < cleanSan.length()) {
                char promoChar = cleanSan.charAt(eqIndex + 1);
                Side side = board.getSideToMove();
                promotionPiece = charToPiece(promoChar, side);
                cleanSan = cleanSan.substring(0, eqIndex);
                log.trace("Promotion detected: {} -> {}", san, promotionPiece);
            }
        }

        cleanSan = cleanSan.replaceAll("[+#]", "");
        List<Move> legalMoves = board.legalMoves();

        if (legalMoves.isEmpty()) {
            return null;
        }

        Move foundMove = findMoveInternal(cleanSan, legalMoves, board);
        if (foundMove != null && promotionPiece != null) {
            return new Move(foundMove.getFrom(), foundMove.getTo(), promotionPiece);
        }
        return foundMove;
    }

    /**
     * Внутренний метод поиска хода с правильным разрешением неоднозначности
     * Перенесено из PgnParser.findMoveInternal()
     */
    private Move findMoveInternal(String san, List<Move> legalMoves, Board board) {
        // ========== 1. РОКИРОВКА ==========
        if ("O-O".equals(san)) {
            Side side = board.getSideToMove();
            for (Move move : legalMoves) {
                if (side == Side.WHITE) {
                    if (move.getFrom() == Square.E1 && move.getTo() == Square.G1) {
                        return move;
                    }
                } else {
                    if (move.getFrom() == Square.E8 && move.getTo() == Square.G8) {
                        return move;
                    }
                }
            }
        }
        if ("O-O-O".equals(san)) {
            Side side = board.getSideToMove();
            for (Move move : legalMoves) {
                if (side == Side.WHITE) {
                    if (move.getFrom() == Square.E1 && move.getTo() == Square.C1) {
                        return move;
                    }
                } else {
                    if (move.getFrom() == Square.E8 && move.getTo() == Square.C8) {
                        return move;
                    }
                }
            }
        }

        // ========== 2. ПЕШЕЧНЫЕ ХОДЫ ==========
        if (san.matches("^[a-h][1-8]$")) {
            Square target = Square.valueOf(san.toUpperCase());
            for (Move move : legalMoves) {
                if (move.getTo() == target) {
                    Piece piece = board.getPiece(move.getFrom());
                    if (piece == Piece.WHITE_PAWN || piece == Piece.BLACK_PAWN) {
                        return move;
                    }
                }
            }
        }

        if (san.matches("^[a-h]x[a-h][1-8]$")) {
            char fromFile = san.charAt(0);
            String targetStr = san.substring(2, 4);
            Square target = Square.valueOf(targetStr.toUpperCase());

            for (Move move : legalMoves) {
                if (move.getTo() == target) {
                    Piece piece = board.getPiece(move.getFrom());
                    if (piece == Piece.WHITE_PAWN || piece == Piece.BLACK_PAWN) {
                        String fromFileStr = move.getFrom().toString().toLowerCase().substring(0, 1);
                        if (fromFileStr.charAt(0) == fromFile) {
                            return move;
                        }
                    }
                }
            }
        }

        // ========== 3. ХОДЫ ФИГУР (С РАЗРЕШЕНИЕМ НЕОДНОЗНАЧНОСТИ) ==========
        if (san.matches("^[KQRBN][a-hx1-8]+$")) {
            char pieceChar = san.charAt(0);
            String rest = san.substring(1);

            char disambiguationFile = 0;
            int disambiguationRank = -1;
            boolean hasDisambiguation = false;

            String disambigPart = rest.replaceAll("[x]", "");
            if (disambigPart.length() > 2) {
                String maybeDisambig = disambigPart.substring(0, disambigPart.length() - 2);
                if (maybeDisambig.length() == 1) {
                    char c = maybeDisambig.charAt(0);
                    if (c >= 'a' && c <= 'h') {
                        disambiguationFile = c;
                        hasDisambiguation = true;
                    } else if (c >= '1' && c <= '8') {
                        disambiguationRank = c - '0';
                        hasDisambiguation = true;
                    }
                } else if (maybeDisambig.length() == 2) {
                    char c1 = maybeDisambig.charAt(0);
                    char c2 = maybeDisambig.charAt(1);
                    if (c1 >= 'a' && c1 <= 'h' && c2 >= '1' && c2 <= '8') {
                        disambiguationFile = c1;
                        disambiguationRank = c2 - '0';
                        hasDisambiguation = true;
                    }
                }
            }

            String targetPart = rest.replaceAll("[x]", "");
            String target = targetPart.substring(targetPart.length() - 2);
            Square targetSquare = Square.valueOf(target.toUpperCase());

            for (Move move : legalMoves) {
                if (move.getTo() != targetSquare) continue;

                Piece piece = board.getPiece(move.getFrom());
                if (piece == Piece.NONE) continue;
                if (getPieceChar(piece) != pieceChar) continue;

                if (hasDisambiguation) {
                    String fromStr = move.getFrom().toString().toLowerCase();
                    char fromFile = fromStr.charAt(0);
                    int fromRank = move.getFrom().getRank().ordinal() + 1;

                    boolean fileMatches = (disambiguationFile == 0 || fromFile == disambiguationFile);
                    boolean rankMatches = (disambiguationRank == -1 || fromRank == disambiguationRank);

                    if (fileMatches && rankMatches) {
                        return move;
                    }
                } else {
                    return move;
                }
            }
        }

        // ========== 4. ПОИСК ПО ЧАСТИЧНОМУ СОВПАДЕНИЮ (FALLBACK) ==========
        String targetStr = san.replaceAll("[KQRBNx]", "").trim();
        if (targetStr.length() >= 2) {
            String lastTwo = targetStr.substring(targetStr.length() - 2);
            try {
                Square target = Square.valueOf(lastTwo.toUpperCase());
                for (Move move : legalMoves) {
                    if (move.getTo() == target) {
                        return move;
                    }
                }
            } catch (Exception e) {
                // Не удалось распарсить клетку
            }
        }

        return null;
    }

    /**
     * Преобразует символ в Piece
     * Перенесено из PgnParser.charToPiece()
     */
    public Piece charToPiece(char c, Side side) {
        boolean isWhite = side == Side.WHITE;
        return switch (Character.toUpperCase(c)) {
            case 'R' -> isWhite ? Piece.WHITE_ROOK : Piece.BLACK_ROOK;
            case 'B' -> isWhite ? Piece.WHITE_BISHOP : Piece.BLACK_BISHOP;
            case 'N' -> isWhite ? Piece.WHITE_KNIGHT : Piece.BLACK_KNIGHT;
            default -> isWhite ? Piece.WHITE_QUEEN : Piece.BLACK_QUEEN;
        };
    }

    /**
     * Возвращает символ фигуры
     * Перенесено из PgnParser.getPieceChar()
     */
    public char getPieceChar(Piece piece) {
        return switch (piece) {
            case WHITE_KING, BLACK_KING -> 'K';
            case WHITE_QUEEN, BLACK_QUEEN -> 'Q';
            case WHITE_ROOK, BLACK_ROOK -> 'R';
            case WHITE_BISHOP, BLACK_BISHOP -> 'B';
            case WHITE_KNIGHT, BLACK_KNIGHT -> 'N';
            default -> ' ';
        };
    }
}