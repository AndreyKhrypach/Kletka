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

import com.github.bhlangonijr.chesslib.Piece;
import java.util.HashMap;
import java.util.Map;

public class ChessSymbols {

    private static final Map<Piece, String> SYMBOL_MAP = new HashMap<>();
    private static final Map<Character, String> LETTER_TO_SYMBOL = new HashMap<>();

    static {
        // Белые фигуры
        SYMBOL_MAP.put(Piece.WHITE_KING, "♔");
        SYMBOL_MAP.put(Piece.WHITE_QUEEN, "♕");
        SYMBOL_MAP.put(Piece.WHITE_ROOK, "♖");
        SYMBOL_MAP.put(Piece.WHITE_BISHOP, "♗");
        SYMBOL_MAP.put(Piece.WHITE_KNIGHT, "♘");
        SYMBOL_MAP.put(Piece.WHITE_PAWN, "♙");

        // Черные фигуры
        SYMBOL_MAP.put(Piece.BLACK_KING, "♚");
        SYMBOL_MAP.put(Piece.BLACK_QUEEN, "♛");
        SYMBOL_MAP.put(Piece.BLACK_ROOK, "♜");
        SYMBOL_MAP.put(Piece.BLACK_BISHOP, "♝");
        SYMBOL_MAP.put(Piece.BLACK_KNIGHT, "♞");
        SYMBOL_MAP.put(Piece.BLACK_PAWN, "♟");

        // Для преобразования букв в символы (независимо от цвета)
        LETTER_TO_SYMBOL.put('K', "♔");
        LETTER_TO_SYMBOL.put('Q', "♕");
        LETTER_TO_SYMBOL.put('R', "♖");
        LETTER_TO_SYMBOL.put('B', "♗");
        LETTER_TO_SYMBOL.put('N', "♘");
        LETTER_TO_SYMBOL.put('P', "♙");
    }

    public static String getSymbol(Piece piece) {
        String symbol = SYMBOL_MAP.get(piece);
        return symbol != null ? symbol : "?";
    }

    /**
     * Преобразует текстовую нотацию в строку с Unicode символами
     * Поддерживает:
     * - Обычные ходы: Nf3 -> ♘f3
     * - Взятия: Nxf3 -> ♘xf3
     * - Превращения: hxg8=Q -> hxg8=♕
     * - Шах/мат: Qh5+ -> ♕h5+, Qh5# -> ♕h5#
     * - Рокировки: O-O, O-O-O
     */
    public static String convertToChessSymbols(String text) {
        if (text == null || text.isEmpty()) return text;

        // Рокировку не трогаем
        if (text.equals("O-O") || text.equals("O-O-O")) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        int i = 0;

        while (i < text.length()) {
            char c = text.charAt(i);

            // Обработка превращения (например: "hxg8=Q")
            if (c == '=' && i + 1 < text.length()) {
                result.append('=');
                char pieceChar = text.charAt(i + 1);
                String symbol = LETTER_TO_SYMBOL.get(pieceChar);
                if (symbol != null) {
                    result.append(symbol);
                } else {
                    result.append(pieceChar);
                }
                i += 2;
                continue;
            }

            // Обработка букв фигур (K, Q, R, B, N)
            if (LETTER_TO_SYMBOL.containsKey(c)) {
                result.append(LETTER_TO_SYMBOL.get(c));
            } else {
                result.append(c);
            }
            i++;
        }

        return result.toString();
    }

    /**
     * Получает Unicode символ фигуры по буквенному обозначению
     */
    public static String getSymbolFromLetter(char letter) {
        return LETTER_TO_SYMBOL.get(letter);
    }

    /**
     * Получает Unicode символ фигуры по буквенному обозначению (строка)
     */
    public static String getSymbolFromLetter(String letter) {
        if (letter == null || letter.isEmpty()) return null;
        return LETTER_TO_SYMBOL.get(letter.charAt(0));
    }
}