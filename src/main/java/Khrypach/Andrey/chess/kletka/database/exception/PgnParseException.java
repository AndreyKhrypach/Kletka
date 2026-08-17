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

package Khrypach.Andrey.chess.kletka.database.exception;

/**
 * Исключение при парсинге PGN
 */
public class PgnParseException extends PgnException {

    public PgnParseException(String message) {
        super(message);
    }

    public PgnParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public PgnParseException(String message, int line, int column) {
        super(String.format("Ошибка парсинга PGN [%d:%d]: %s", line, column, message));
    }
}