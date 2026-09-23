/*
 * Copyright (c) 2025-2026 Andrey Khrypach
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package Khrypach.Andrey.chess.kletka.gui.board;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;

/**
 * Форматирует шахматную позицию в ASCII-диаграмму для буфера обмена.
 * <p>
 * Формат:
 * <pre>
 * Position: rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq - 0 1
 *
 *   +------------------------+
 * 8 | r  n  b  q  k  b  n  r |
 * 7 | p  p  p  p  p  p  p  p |
 * 6 | .  .  .  .  .  .  .  . |
 * 5 | .  .  .  .  .  .  .  . |
 * 4 | .  .  .  .  .  .  .  . |
 * 3 | .  .  .  .  .  .  .  . |
 * 2 | P  P  P  P  P  P  P  P |
 * 1 | R  N  B  Q  K  B  N  R |
 *   +------------------------+
 *     a  b  c  d  e  f  g  h
 * </pre>
 * <p>
 * Всегда от белых, независимо от флипа на доске.
 */
public final class PositionAsciiFormatter {

    private PositionAsciiFormatter() {
        // утилитарный класс
    }

    /**
     * Форматирует позицию в строку для буфера обмена.
     *
     * @param board доска (не null)
     * @return FEN + пустая строка + ASCII-диаграмма
     */
    public static String format(Board board) {
        if (board == null) return "";

        // Размеры
        final int CELL_WIDTH = 2;                        // фигура + пробел
        final int BOARD_INNER_WIDTH = 8 * CELL_WIDTH;    // 16
        final String HORIZONTAL = "-".repeat(BOARD_INNER_WIDTH);

        StringBuilder sb = new StringBuilder();

        // 1. FEN
        sb.append("Position: ").append(board.getFen()).append("\n\n");

        // 2. Открывающий ``` для Markdown
        sb.append("```\n");

        // 3. Верхняя граница
        sb.append("  +").append(HORIZONTAL).append("+\n");

        // 4. Ряды от 8 к 1
        for (int rank = 7; rank >= 0; rank--) {
            sb.append(rank + 1).append(" | ");
            for (int file = 0; file < 8; file++) {
                Square sq = Square.squareAt(rank * 8 + file);
                Piece piece = board.getPiece(sq);
                sb.append(pieceToChar(piece)).append(' ');
            }
            sb.append("|\n");
        }

        // 5. Нижняя граница
        sb.append("  +").append(HORIZONTAL).append("+\n");

        // 6. Буквы файлов — выровнены под фигуры
        sb.append("    ");  // 4 пробела = "8 | " = та же ширина
        for (int file = 0; file < 8; file++) {
            sb.append((char) ('a' + file)).append(' ');
        }
        sb.append("\n");

        // 7. Закрывающий ```
        sb.append("```");

        return sb.toString();
    }

    /**
     * Преобразует фигуру в символ FEN.
     * Пустая клетка — точка.
     */
    private static char pieceToChar(Piece piece) {
        if (piece == null || piece == Piece.NONE) return '.';
        return switch (piece) {
            case WHITE_PAWN -> 'P';
            case WHITE_KNIGHT -> 'N';
            case WHITE_BISHOP -> 'B';
            case WHITE_ROOK -> 'R';
            case WHITE_QUEEN -> 'Q';
            case WHITE_KING -> 'K';
            case BLACK_PAWN -> 'p';
            case BLACK_KNIGHT -> 'n';
            case BLACK_BISHOP -> 'b';
            case BLACK_ROOK -> 'r';
            case BLACK_QUEEN -> 'q';
            case BLACK_KING -> 'k';
            default -> '?';
        };
    }
}