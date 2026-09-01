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

package Khrypach.Andrey.chess.kletka.gui.model;

/**
 * Режимы навигации по дереву вариантов
 */
public enum NavigationMode {

    /**
     * Режим PGN - стандартная навигация по партиям
     * - ↑ (Up): В начало варианта
     * - ↓ (Down): В конец главной линии
     * - ← (Left): Предыдущий ход
     * - → (Right): Следующий ход
     * - Home: В начало
     * - End: В конец
     */
    PGN,

    /**
     * Режим книги - навигация по дебютной книге
     * - ↑ (Up): Перемещение на уровень выше
     * - ↓ (Down): Перемещение на уровень ниже
     * - ← (Left): Возврат на предыдущий уровень
     * - → (Right): Выбор варианта / переход на следующий уровень
     * - Home: В корень
     * - End: В конец главной линии
     * - Enter: Выбор варианта (как →)
     */
    BOOK;
}