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

package Khrypach.Andrey.chess.kletka.gui.visitor.impl;

import Khrypach.Andrey.chess.kletka.gui.model.*;
import Khrypach.Andrey.chess.kletka.gui.visitor.VariationTreeVisitor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static Khrypach.Andrey.chess.kletka.gui.model.MoveAnnotation.MATE;

@Slf4j
public class PgnExportVisitor implements VariationTreeVisitor<String> {

    private final StringBuilder pgnBuilder = new StringBuilder();
    private int openParensCount = 0;
    private boolean needSpaceBeforeNext = false;
    private int lastMoveNumber = 0;
    private boolean lastMoveWasWhite = true;
    private boolean lastWasContinuationAfterVariation = false;

    @Getter
    @Setter
    private boolean hasMoveNumberInCurrentVariation = false;
    @Getter
    @Setter
    private boolean inRootVariation = false;
    private boolean firstMainMoveProcessed = false;
    @Getter
    @Setter
    private boolean needMoveNumberForRootVariation = false;

    @Getter
    @Setter
    private boolean hasRootVariations = false;

    @Getter
    @Setter
    private boolean isMainLine = true;
    @Getter
    @Setter
    private int currentDepth = 0;
    @Getter
    @Setter
    private boolean deleted = false;

    @Override
    public void visitRootStart(RootNode rootNode) {
        pgnBuilder.setLength(0);
        deleted = false;
        openParensCount = 0;
        needSpaceBeforeNext = false;
        lastMoveNumber = 0;
        lastMoveWasWhite = true;
        lastWasContinuationAfterVariation = false;
        hasMoveNumberInCurrentVariation = false;
        firstMainMoveProcessed = false;
        hasRootVariations = false;
        needMoveNumberForRootVariation = false;
        inRootVariation = false;
        isMainLine = true;
        currentDepth = 0;
    }

    @Override
    public void visitRootEnd(RootNode rootNode) {
        while (openParensCount > 0) {
            pgnBuilder.append(")");
            openParensCount--;
        }
    }

    @Override
    public void visitMainLineStart(Variation mainLine) {
        appendDeletedHeader();
        isMainLine = true;
        firstMainMoveProcessed = false;
    }

    @Override
    public void visitMainLineMove(MoveNode moveNode, int moveNumber, boolean isWhiteMove, boolean isFirstInLine) {
        isMainLine = true;

        if (needSpaceBeforeNext) {
            pgnBuilder.append(" ");
            needSpaceBeforeNext = false;
        }

        if (isWhiteMove) {
            // Белый ход: всегда номер
            pgnBuilder.append(moveNumber).append(". ");
            lastMoveNumber = moveNumber;
            lastMoveWasWhite = true;
            // ========== ДОБАВЛЯЕМ СБРОС ==========
            lastWasContinuationAfterVariation = false;  // <-- ДОБАВИТЬ!
        } else {
            // Если это первый ход черных в партии
            if (moveNumber == 1 && !firstMainMoveProcessed) {
                pgnBuilder.append(moveNumber).append("... ");
            }
            // Если после варианта - нужен номер с троеточием
            else if (lastWasContinuationAfterVariation) {
                pgnBuilder.append(moveNumber).append("... ");
                lastWasContinuationAfterVariation = false;
            }
            // Если ход черных после хода белых с другим номером
            else if (lastMoveWasWhite && lastMoveNumber != moveNumber) {
                pgnBuilder.append(moveNumber).append(". ");
            }
            // Иначе - просто ход без номера

            lastMoveNumber = moveNumber;
            lastMoveWasWhite = false;
        }

        if (!firstMainMoveProcessed) {
            firstMainMoveProcessed = true;
        }

        appendBetterWasBeforeMove(moveNode);
        pgnBuilder.append(moveNode.getSan());
        appendAllAnnotations(moveNode);
        appendComment(moveNode);

        needSpaceBeforeNext = true;
    }

    @Override
    public void visitMainLineMoveEnd(MoveNode moveNode) {
        // Ничего
    }

    @Override
    public void visitMainLineEnd(Variation mainLine) {
        // Ничего
    }

    // ========== КОРНЕВЫЕ ВАРИАНТЫ ==========

    @Override
    public void visitRootVariationsStart(RootNode rootNode, List<Variation> rootVariations) {
        hasRootVariations = !rootVariations.isEmpty();
        firstMainMoveProcessed = false;
        needMoveNumberForRootVariation = false;
    }

    @Override
    public void visitRootVariationStart(Variation variation) {
        if (variation.isMainLine()) return;

        inRootVariation = true;
        isMainLine = false;
        hasMoveNumberInCurrentVariation = false;

        if (needSpaceBeforeNext) {
            pgnBuilder.append(" ");
            needSpaceBeforeNext = false;
        }

        pgnBuilder.append("(");
        openParensCount++;
        lastWasContinuationAfterVariation = false;
    }

    @Override
    public void visitRootVariationMove(MoveNode moveNode, Variation variation,
                                       int moveNumber, boolean isWhiteMove, boolean isFirstInVariation) {
        if (variation.isMainLine()) return;
        isMainLine = false;

        if (needSpaceBeforeNext) {
            pgnBuilder.append(" ");
            needSpaceBeforeNext = false;
        }

        // Номер хода для корневого варианта
        if (isWhiteMove) {
            pgnBuilder.append(moveNumber).append(". ");
            lastMoveNumber = moveNumber;
            hasMoveNumberInCurrentVariation = true;
        } else {
            // Первый ход черных в корневом варианте - нужен номер с троеточием
            if (isFirstInVariation && moveNumber == 1) {
                pgnBuilder.append(moveNumber).append("... ");
                hasMoveNumberInCurrentVariation = true;
            }
            lastMoveNumber = moveNumber;
        }

        lastMoveWasWhite = isWhiteMove;

        // Добавляем BETTER_WAS перед ходом
        appendBetterWasBeforeMove(moveNode);

        pgnBuilder.append(moveNode.getSan());
        appendAllAnnotations(moveNode);
        appendComment(moveNode);

        needSpaceBeforeNext = true;
        lastWasContinuationAfterVariation = false;
    }

    @Override
    public void visitRootVariationEnd(Variation variation) {
        if (variation.isMainLine()) return;

        inRootVariation = false;
        isMainLine = true;

        if (openParensCount > 0) {
            pgnBuilder.append(")");
            openParensCount--;
            needSpaceBeforeNext = true;
            lastWasContinuationAfterVariation = true;
        }
    }

    @Override
    public void visitRootVariationsEnd(RootNode rootNode) {
        // Ничего
    }

    // ========== ОБЫЧНЫЕ ВАРИАНТЫ ==========

    @Override
    public void visitVariationStart(Variation variation, int depth, ParentNode forkNode) {
        if (variation == null || variation.isMainLine()) return;

        // Проверяем, что это не корневой вариант
        if (variation.getParentNodeRef() != null && variation.getParentNodeRef().isRoot()) {
            return;
        }

        currentDepth = depth;
        isMainLine = false;
        hasMoveNumberInCurrentVariation = false;

        if (needSpaceBeforeNext) {
            pgnBuilder.append(" ");
            needSpaceBeforeNext = false;
        }

        pgnBuilder.append("(");
        openParensCount++;
        lastWasContinuationAfterVariation = false;
    }

    @Override
    public void visitVariationMove(MoveNode moveNode, Variation variation, int depth,
                                   int moveNumber, boolean isWhiteMove, boolean isFirstInVariation) {
        if (variation.isMainLine()) return;
        isMainLine = false;

        if (needSpaceBeforeNext) {
            pgnBuilder.append(" ");
            needSpaceBeforeNext = false;
        }

        // ========== НОМЕР ТОЛЬКО ДЛЯ ПЕРВОГО ХОДА В ВАРИАНТЕ ==========
        if (isFirstInVariation) {
            if (isWhiteMove) {
                pgnBuilder.append(moveNumber).append(". ");
            } else {
                pgnBuilder.append(moveNumber).append("... ");
            }
            lastMoveNumber = moveNumber;
            hasMoveNumberInCurrentVariation = true;
        }
        // Для белых ходов ПОСЛЕ черных - тоже номер
        else if (isWhiteMove && !lastMoveWasWhite) {
            pgnBuilder.append(moveNumber).append(". ");
            lastMoveNumber = moveNumber;
            hasMoveNumberInCurrentVariation = true;
        }

        lastMoveWasWhite = isWhiteMove;

        appendBetterWasBeforeMove(moveNode);
        pgnBuilder.append(moveNode.getSan());
        appendAllAnnotations(moveNode);
        appendComment(moveNode);

        needSpaceBeforeNext = true;
        lastWasContinuationAfterVariation = false;
    }

    @Override
    public void visitVariationEnd(Variation variation) {
        if (variation == null || variation.isMainLine()) return;

        isMainLine = true;

        if (openParensCount > 0) {
            pgnBuilder.append(")");
            openParensCount--;
            needSpaceBeforeNext = true;
            lastWasContinuationAfterVariation = true; // <-- ЭТО ПРАВИЛЬНО
        }
    }

    // ========== АННОТАЦИИ И КОММЕНТАРИИ ==========

    @Override
    public void visitAnnotation(MoveNode moveNode, MoveAnnotation annotation) {
        // Обрабатывается в appendAllAnnotations
    }

    @Override
    public void visitComment(MoveNode moveNode, String comment) {
        // Обрабатывается в appendComment
    }

    @Override
    public String getResult() {
        return pgnBuilder.toString().trim();
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private void appendBetterWasBeforeMove(MoveNode moveNode) {
        if (moveNode.getAnnotation() == MoveAnnotation.BETTER_WAS) {
            pgnBuilder.append(MoveAnnotation.BETTER_WAS.getSymbol()).append(" ");
            return;
        }
        for (MoveAnnotation ann : moveNode.getAdditionalAnnotations()) {
            if (ann == MoveAnnotation.BETTER_WAS) {
                pgnBuilder.append(ann.getSymbol()).append(" ");
                break;
            }
        }
    }

    private void appendAllAnnotations(MoveNode moveNode) {
        // 1. Пользовательские аннотации (кроме BETTER_WAS, шаха/мата)
        MoveAnnotation annotation = moveNode.getAnnotation();
        if (annotation != null &&
                annotation != MoveAnnotation.BETTER_WAS &&
                annotation != MoveAnnotation.CHECK &&
                annotation != MATE &&
                annotation != MoveAnnotation.DOUBLE_CHECK) {
            pgnBuilder.append(annotation.getSymbol());
        }

        for (MoveAnnotation ann : moveNode.getAdditionalAnnotations()) {
            if (ann != MoveAnnotation.BETTER_WAS &&
                    ann != MoveAnnotation.CHECK &&
                    ann != MATE &&
                    ann != MoveAnnotation.DOUBLE_CHECK) {
                pgnBuilder.append(ann.getSymbol());
            }
        }

        // 2. Шах/мат/двойной шах
        if (moveNode.getAnnotation() == MoveAnnotation.DOUBLE_CHECK ||
                moveNode.getAdditionalAnnotations().contains(MoveAnnotation.DOUBLE_CHECK)) {
            pgnBuilder.append(MoveAnnotation.DOUBLE_CHECK.getSymbol());
        } else if (moveNode.getAnnotation() == MATE ||
                moveNode.getAdditionalAnnotations().contains(MATE)) {
            pgnBuilder.append(MATE.getSymbol());
        } else if (moveNode.getAnnotation() == MoveAnnotation.CHECK ||
                moveNode.getAdditionalAnnotations().contains(MoveAnnotation.CHECK)) {
            pgnBuilder.append(MoveAnnotation.CHECK.getSymbol());
        }
    }

    private void appendComment(MoveNode moveNode) {
        String comment = moveNode.getComment();
        if (comment != null && !comment.trim().isEmpty()) {
            pgnBuilder.append(" {").append(comment).append("}");
            needSpaceBeforeNext = true;
        }
    }

    /**
     * Добавляет заголовок Deleted в начало PGN
     */
    private void appendDeletedHeader() {
        if (deleted) {
            pgnBuilder.append("[Deleted \"true\"]\n");
        }
    }
}