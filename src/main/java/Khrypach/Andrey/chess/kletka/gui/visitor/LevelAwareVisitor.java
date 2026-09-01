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

package Khrypach.Andrey.chess.kletka.gui.visitor;

import Khrypach.Andrey.chess.kletka.gui.model.MoveNode;
import Khrypach.Andrey.chess.kletka.gui.model.ParentNode;
import Khrypach.Andrey.chess.kletka.gui.model.Variation;

/**
 * Расширенный Visitor для обхода дерева с поддержкой уровней
 * Используется для BOOK режима навигации
 */
public interface LevelAwareVisitor<T> extends VariationTreeVisitor<T> {

    /**
     * Начало уровня
     * @param level номер уровня (0 = корень)
     * @param node узел, на котором начинается уровень
     * @param variation вариант, которому принадлежит уровень
     */
    void visitLevelStart(int level, ParentNode node, Variation variation);

    /**
     * Посещение варианта на уровне
     * @param variation вариант
     * @param level номер уровня
     * @param index индекс варианта в списке
     * @param isActive активен ли вариант (выбран пользователем)
     * @param isMainLine является ли главной линией
     */
    void visitLevelVariation(Variation variation, int level, int index,
                             boolean isActive, boolean isMainLine);

    /**
     * Посещение хода в варианте на уровне
     * @param moveNode узел хода
     * @param variation вариант
     * @param level номер уровня
     * @param moveIndex индекс хода в варианте
     * @param isActive активен ли ход
     */
    void visitLevelMove(MoveNode moveNode, Variation variation,
                        int level, int moveIndex, boolean isActive);

    /**
     * Конец уровня
     * @param level номер уровня
     */
    void visitLevelEnd(int level);

    /**
     * Посещение информации о книге
     * @param bookName название книги
     * @param totalEntries общее количество записей
     * @param currentPosition текущая позиция
     */
    void visitBookInfo(String bookName, int totalEntries, String currentPosition);
}