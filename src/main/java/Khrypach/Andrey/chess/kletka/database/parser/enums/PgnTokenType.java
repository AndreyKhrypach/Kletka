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

package Khrypach.Andrey.chess.kletka.database.parser.enums;

/**
 * Типы токенов PGN
 */
public enum PgnTokenType {
    HEADER_START,      // [
    HEADER_END,        // ]
    HEADER_KEY,        // Event, White, Black и т.д.
    HEADER_VALUE,      // "value"
    MOVE_NUMBER,       // 1.
    MOVE_NUMBER_ELLIPSIS, // 1...
    MOVE,              // e4, Nf3, O-O
    ANNOTATION,        // !, ?, !!, ?!, +-, -+
    NAG,               // $1, $2, $3
    COMMENT_START,     // {
    COMMENT_END,       // }
    COMMENT_TEXT,      // text
    VARIATION_START,   // (
    VARIATION_END,     // )
    RESULT,            // 1-0, 0-1, 1/2-1/2, *
    WHITESPACE,        // пробелы, табы, переносы
    EOF
}
