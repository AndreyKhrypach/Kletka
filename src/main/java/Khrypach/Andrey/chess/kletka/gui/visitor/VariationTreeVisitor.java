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

package Khrypach.Andrey.chess.kletka.gui.visitor;

import Khrypach.Andrey.chess.kletka.gui.model.*;

import java.util.List;

/**
 * Visitor для обхода дерева вариантов
 * @param <T> тип результата обхода
 */
public interface VariationTreeVisitor<T> {

    /**
     * Начало обхода корневого узла
     */
    void visitRootStart(RootNode rootNode);

    /**
     * Конец обхода корневого узла
     */
    void visitRootEnd(RootNode rootNode);

    /**
     * Посещение основного продолжения (главной линии)
     */
    void visitMainLineStart(Variation mainLine);

    /**
     * Посещение перед ходом в главной линии
     */
    void visitMainLineMove(MoveNode moveNode, int moveNumber, boolean isWhiteMove, boolean isFirstInLine);

    /**
     * Посещение после хода в главной линии
     */
    void visitMainLineMoveEnd(MoveNode moveNode);

    /**
     * Конец главной линии
     */
    void visitMainLineEnd(Variation mainLine);

    /**
     * Начало варианта (не главной линии)
     */
    void visitVariationStart(Variation variation, int depth, ParentNode forkNode);

    /**
     * Посещение хода в варианте
     */
    void visitVariationMove(MoveNode moveNode, Variation variation, int depth,
                            int moveNumber, boolean isWhiteMove, boolean isFirstInVariation);

    /**
     * Конец варианта
     */
    void visitVariationEnd(Variation variation);

    /**
     * Начало обработки корневых вариантов (альтернатив первому ходу)
     */
    default void visitRootVariationsStart(RootNode rootNode, List<Variation> rootVariations) {
        // По умолчанию ничего не делаем
    }

    /**
     * Конец обработки корневых вариантов
     */
    default void visitRootVariationsEnd(RootNode rootNode) {
        // По умолчанию ничего не делаем
    }

    /**
     * Начало корневого варианта
     */
    default void visitRootVariationStart(Variation variation) {
        // По умолчанию ничего не делаем
    }

    /**
     * Ход в корневом варианте
     */
    default void visitRootVariationMove(MoveNode moveNode, Variation variation,
                                        int moveNumber, boolean isWhiteMove, boolean isFirstInVariation) {
        // По умолчанию ничего не делаем
    }

    /**
     * Конец корневого варианта
     */
    default void visitRootVariationEnd(Variation variation) {
        // По умолчанию ничего не делаем
    }

    /**
     * Посещение комментария/аннотации
     */
    void visitAnnotation(MoveNode moveNode, MoveAnnotation annotation);

    /**
     * Посещение комментария
     */
    void visitComment(MoveNode moveNode, String comment);

    /**
     * Получить результат обхода
     */
    T getResult();
}