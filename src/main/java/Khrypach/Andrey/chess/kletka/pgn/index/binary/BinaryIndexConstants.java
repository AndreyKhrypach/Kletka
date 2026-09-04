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

package Khrypach.Andrey.chess.kletka.pgn.index.binary;

/**
 * Константы для бинарного индекса
 */
public final class BinaryIndexConstants {

    private BinaryIndexConstants() {}

    /**
     * Магическое число "KLT" (0x4B4C5449)
     */
    public static final int MAGIC = 0x4B4C5449;

    /**
     * Текущая версия формата
     */
    public static final byte VERSION = 1;

    /**
     * Размер заголовка в байтах
     */
    public static final int HEADER_SIZE = 48;

    /**
     * Расширение файла индекса
     */
    public static final String INDEX_EXTENSION = ".klt";
}