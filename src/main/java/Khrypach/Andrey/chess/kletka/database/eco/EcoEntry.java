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

package Khrypach.Andrey.chess.kletka.database.eco;


/**
 * Запись о дебюте из ECO базы данных
 *
 * @param eco  Код ECO (например, B91)
 * @param name Название дебюта на английском
 * @param pgn  PGN последовательность ходов
 * @param uci  UCI последовательность ходов
 * @param epd  EPD позиция (начальная позиция дебюта)
 */

public record EcoEntry(String eco, String name, String pgn, String uci, String epd) {
}