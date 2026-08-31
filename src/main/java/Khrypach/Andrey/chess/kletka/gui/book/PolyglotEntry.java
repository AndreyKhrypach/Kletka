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

package Khrypach.Andrey.chess.kletka.gui.book;

import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;

/**
 * Представляет одну запись в Polyglot книге (.bin)
 * Формат: 16 байт
 * - key: 8 байт (Zobrist хеш позиции)
 * - move: 2 байта (16 бит: from(6) + to(6) + promotion(4))
 * - weight: 2 байта (вес хода)
 * - learn: 4 байта (информация для обучения)
 * - games: 4 байта (количество партий)
 *
 * @param key    Zobrist хеш позиции
 * @param move   16-битный ход
 * @param weight Вес хода (0-32767)
 * @param learn  Информация для обучения
 * @param games  Количество партий
 */
public record PolyglotEntry(long key, short move, short weight, int learn, int games) {

    /**
     * Получает начальную клетку из 16-битного хода
     * Биты 6-11 (from) согласно спецификации
     */
    public Square getFromSquare() {
        int fromIndex = (move >> 6) & 0x3F;
        return Square.squareAt(fromIndex);
    }

    /**
     * Получает конечную клетку из 16-битного хода
     * Биты 0-5 (to) согласно спецификации
     */
    public Square getToSquare() {
        int toIndex = move & 0x3F;  // биты 0-5
        return Square.squareAt(toIndex);
    }

    /**
     * Получает фигуру для превращения из 16-битного хода
     * Биты 12-15 (promotion)
     * 0 = none, 1 = knight, 2 = bishop, 3 = rook, 4 = queen
     */
    public Piece getPromotionPiece() {
        int promotionCode = (move >> 12) & 0xF;

        return switch (promotionCode) {
            case 1 -> Piece.WHITE_KNIGHT;
            case 2 -> Piece.WHITE_BISHOP;
            case 3 -> Piece.WHITE_ROOK;
            case 4 -> Piece.WHITE_QUEEN;
            default -> Piece.NONE;
        };
    }

    /**
     * Вычисляет рейтинг на основе веса
     * Приблизительная эло-оценка
     */
    public int getRating() {
        if (weight == 0) return 0;
        // Вес 16384 = 50% = ~2500 Elo
        double ratio = weight / 32767.0;
        return (int) (2500 + (ratio - 0.5) * 1000);
    }

    /**
     * Создает строковое представление хода в формате UCI
     */
    public String getUciMove() {
        Square from = getFromSquare();
        Square to = getToSquare();
        Piece promotion = getPromotionPiece();

        String uci = from.toString().toLowerCase() +
                to.toString().toLowerCase();

        if (promotion != Piece.NONE) {
            uci += getPromotionChar(promotion);
        }

        return uci;
    }

    private String getPromotionChar(Piece piece) {
        return switch (piece) {
            case WHITE_QUEEN, BLACK_QUEEN -> "q";
            case WHITE_ROOK, BLACK_ROOK -> "r";
            case WHITE_BISHOP, BLACK_BISHOP -> "b";
            case WHITE_KNIGHT, BLACK_KNIGHT -> "n";
            default -> "";
        };
    }

    @Override
    public String toString() {
        return String.format("PolyglotEntry{key=0x%016X, move=%s, weight=%d, games=%d}",
                key, getUciMove(), weight, games);
    }
}