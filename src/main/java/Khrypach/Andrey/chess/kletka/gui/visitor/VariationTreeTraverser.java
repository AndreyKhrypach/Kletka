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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Движок обхода дерева вариантов с поддержкой динамической главной линии
 * Новая версия: все продолжения хранятся в subVariations,
 * главная линия помечена флагом isMainLine
 */
public class VariationTreeTraverser {

    private static final Logger log = LoggerFactory.getLogger(VariationTreeTraverser.class);

    private Set<ParentNode> alreadyVisitedInVariation = null;

    public <T> T traverse(RootNode rootNode, Variation mainLine, VariationTreeVisitor<T> visitor) {

        alreadyVisitedInVariation = new HashSet<>();

        log.trace("rootNode.subVariations size = {}",
                rootNode.getSubVariations() != null ? rootNode.getSubVariations().size() : 0);
        log.trace("mainLine = {}", mainLine != null ? mainLine.getName() : "null");

        visitor.visitRootStart(rootNode);

        // ============================================================
        // 1. Находим главную линию на корне
        // ============================================================
        Variation mainLineVar = null;
        for (Variation var : rootNode.getSubVariations()) {
            if (var.isMainLine()) {
                mainLineVar = var;
                break;
            }
        }

        if (mainLineVar == null && !rootNode.getSubVariations().isEmpty()) {
            mainLineVar = rootNode.getSubVariations().get(0);
            mainLineVar.setMainLine(true);
        }

        if (mainLineVar == null || mainLineVar.isEmpty()) {
            visitor.visitRootEnd(rootNode);
            return visitor.getResult();
        }

        // ============================================================
        // 2. Выводим главную линию с подвариантами
        // ============================================================
        visitor.visitMainLineStart(mainLineVar);

        List<ParentNode> mainLineMoves = mainLineVar.getMoves();
        Set<ParentNode> alreadyVisited = new HashSet<>();

        for (int i = 0; i < mainLineMoves.size(); i++) {
            ParentNode node = mainLineMoves.get(i);
            if (node instanceof MoveNode moveNode) {

                if (alreadyVisited.contains(node)) {
                    continue;
                }

                int absolutePly = moveNode.getAbsolutePly();
                boolean isWhiteMove = (absolutePly % 2 == 1);
                int moveNumber = (absolutePly + 1) / 2;
                boolean isFirstInLine = (i == 0);

                // ============================================================
                // ЕСЛИ ЭТО ПЕРВЫЙ ХОД — ОБРАБАТЫВАЕМ КОРНЕВЫЕ ВАРИАНТЫ
                // ============================================================
                if (isFirstInLine) {
                    // 1. Выводим первый ход главной линии
                    visitor.visitMainLineMove(moveNode, moveNumber, isWhiteMove, true);

                    // 2. Выводим корневые варианты (из rootNode)
                    List<Variation> rootVariations = new ArrayList<>();
                    for (Variation var : rootNode.getSubVariations()) {
                        if (!var.isMainLine() && !var.isEmpty()) {
                            rootVariations.add(var);
                        }
                    }

                    if (!rootVariations.isEmpty()) {
                        visitor.visitRootVariationsStart(rootNode, rootVariations);
                        for (Variation var : rootVariations) {
                            traverseRootVariation(var, visitor);
                        }
                        visitor.visitRootVariationsEnd(rootNode);
                    }
                }

                // ============================================================
                // ВЫВОДИМ САМ ХОД (если это не первый ход — он уже выведен)
                // ============================================================
                if (!isFirstInLine) {
                    visitor.visitMainLineMove(moveNode, moveNumber, isWhiteMove, false);
                }

                // ============================================================
                // ОБРАБАТЫВАЕМ ПОДВАРИАНТЫ И NEXT
                // ============================================================
                boolean hasSubVariations = !moveNode.getSubVariations().isEmpty();
                ParentNode nextMainLineNode = moveNode.getNext();

                if (hasSubVariations && nextMainLineNode != null && !nextMainLineNode.isRoot() &&
                        nextMainLineNode instanceof MoveNode nextMove) {
                    int nextPly = nextMove.getAbsolutePly();
                    int nextMoveNumber = (nextPly + 1) / 2;
                    boolean nextIsWhite = (nextPly % 2 == 1);

                    visitor.visitMainLineMove(nextMove, nextMoveNumber, nextIsWhite, false);
                    alreadyVisited.add(nextMove);

                    for (Variation subVar : moveNode.getSubVariations()) {
                        if (!subVar.isMainLine() && !subVar.isEmpty()) {
                            traverseVariation(subVar, 1, moveNode, visitor);
                        }
                    }

                    visitor.visitMainLineMoveEnd(nextMove);

                } else if (hasSubVariations) {
                    for (Variation subVar : moveNode.getSubVariations()) {
                        if (!subVar.isMainLine() && !subVar.isEmpty()) {
                            traverseVariation(subVar, 1, moveNode, visitor);
                        }
                    }
                }

                visitor.visitMainLineMoveEnd(moveNode);
            }
        }
        visitor.visitMainLineEnd(mainLineVar);

        visitor.visitRootEnd(rootNode);
        alreadyVisitedInVariation = null;
        return visitor.getResult();
    }

    /**
     * Обходит корневой вариант (альтернатива первому ходу)
     */
    private void traverseRootVariation(Variation variation, VariationTreeVisitor<?> visitor) {
        if (variation == null || variation.isEmpty()) {
            return;
        }

        visitor.visitRootVariationStart(variation);
        traverseLineWithSubVariations(variation.getMoves(), variation, 0, visitor, true);
        visitor.visitRootVariationEnd(variation);
    }

    /**
     * Обходит обычный вариант (не корневой)
     */
    private void traverseVariation(Variation variation, int depth, ParentNode forkNode,
                                   VariationTreeVisitor<?> visitor) {
        if (variation == null || variation.isEmpty() || variation.isMainLine()) {
            return;
        }

        visitor.visitVariationStart(variation, depth, forkNode);
        traverseLineWithSubVariations(variation.getMoves(), variation, depth, visitor, false);
        visitor.visitVariationEnd(variation);
    }

    /**
     * Обходит линию с подвариантами (для вариантов)
     */
    private void traverseLineWithSubVariations(List<ParentNode> nodes,
                                               Variation variation,
                                               int depth,
                                               VariationTreeVisitor<?> visitor,
                                               boolean isRootVariation) {
        for (int i = 0; i < nodes.size(); i++) {
            ParentNode node = nodes.get(i);
            if (!(node instanceof MoveNode moveNode)) {
                continue;
            }

            if (alreadyVisitedInVariation != null && alreadyVisitedInVariation.contains(node)) {
                continue;
            }

            int absolutePly = moveNode.getAbsolutePly();
            boolean isWhiteMove = (absolutePly % 2 == 1);
            int moveNumber = (absolutePly + 1) / 2;
            boolean isFirstInLine = (i == 0);

            boolean hasSubVariations = !moveNode.getSubVariations().isEmpty();
            ParentNode nextNode = moveNode.getNext();

            if (hasSubVariations && nextNode != null && !nextNode.isRoot() && nextNode instanceof MoveNode nextMove) {
                // ========== Есть под варианты И есть следующий ход ==========
                if (isRootVariation) {
                    visitor.visitRootVariationMove(moveNode, variation, moveNumber, isWhiteMove, isFirstInLine);
                } else {
                    visitor.visitVariationMove(moveNode, variation, depth, moveNumber, isWhiteMove, isFirstInLine);
                }

                int nextPly = nextMove.getAbsolutePly();
                int nextMoveNumber = (nextPly + 1) / 2;
                boolean nextIsWhite = (nextPly % 2 == 1);

                if (isRootVariation) {
                    visitor.visitRootVariationMove(nextMove, variation, nextMoveNumber, nextIsWhite, false);
                } else {
                    visitor.visitVariationMove(nextMove, variation, depth, nextMoveNumber, nextIsWhite, false);
                }

                if (alreadyVisitedInVariation == null) {
                    alreadyVisitedInVariation = new HashSet<>();
                }
                alreadyVisitedInVariation.add(nextMove);

                for (Variation subVar : moveNode.getSubVariations()) {
                    if (!subVar.isMainLine() && !subVar.isEmpty()) {
                        ParentNode firstNode = subVar.getFirstNode();
                        if (firstNode != null && !firstNode.isRoot() &&
                                !firstNode.getUciMove().equals(nextNode.getUciMove())) {
                            traverseVariation(subVar, depth + 1, moveNode, visitor);
                        }
                    }
                }

            } else if (hasSubVariations) {
                // ========== Есть подварианты, но нет next ==========
                if (isRootVariation) {
                    visitor.visitRootVariationMove(moveNode, variation, moveNumber, isWhiteMove, isFirstInLine);
                } else {
                    visitor.visitVariationMove(moveNode, variation, depth, moveNumber, isWhiteMove, isFirstInLine);
                }

                for (Variation subVar : moveNode.getSubVariations()) {
                    if (!subVar.isMainLine() && !subVar.isEmpty()) {
                        traverseVariation(subVar, depth + 1, moveNode, visitor);
                    }
                }

            } else {
                // ========== Нет под вариантов — просто выводим ход ==========
                if (isRootVariation) {
                    visitor.visitRootVariationMove(moveNode, variation, moveNumber, isWhiteMove, isFirstInLine);
                } else {
                    visitor.visitVariationMove(moveNode, variation, depth, moveNumber, isWhiteMove, isFirstInLine);
                }
            }
        }
    }
}