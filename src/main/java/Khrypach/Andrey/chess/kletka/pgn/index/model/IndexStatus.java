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

package Khrypach.Andrey.chess.kletka.pgn.index.model;

/**
 * Статус индекса при проверке
 */
public enum IndexStatus {
    /**
     * Индекс актуален, все в порядке
     */
    OK,

    /**
     * Индекс отсутствует
     */
    NO_INDEX,

    /**
     * Файл изменился с момента создания индекса
     */
    FILE_CHANGED,

    /**
     * Файл не найден
     */
    FILE_MISSING,

    /**
     * Индекс поврежден или нечитаем
     */
    INDEX_CORRUPTED,

    /**
     * Версия индекса не поддерживается
     */
    UNSUPPORTED_VERSION
}