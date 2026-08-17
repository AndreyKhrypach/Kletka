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

package Khrypach.Andrey.chess.kletka.gui.board;

import Khrypach.Andrey.chess.kletka.gui.model.MoveNode;
import Khrypach.Andrey.chess.kletka.gui.model.ParentNode;
import Khrypach.Andrey.chess.kletka.gui.model.RootNode;
import Khrypach.Andrey.chess.kletka.gui.model.Variation;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JavaScriptBridge {

    private static final Logger log = LoggerFactory.getLogger(JavaScriptBridge.class);

    private final MoveNavigationController navController;
    private final NotationView notationView;

    public JavaScriptBridge(MoveNavigationController navController, NotationView notationView) {
        this.navController = navController;
        this.notationView = notationView;
    }

    @SuppressWarnings("unused")
    public void onMoveSelected(String nodeUuid) {
        log.debug("[JS Bridge] onMoveSelected called with UUID: {}", nodeUuid);

        Platform.runLater(() -> {
            if (navController == null) {
                log.warn("[JS Bridge] navController is null");
                return;
            }

            MoveNode targetNode = findNodeByUuid(nodeUuid);
            if (targetNode == null) {
                log.warn("[JS Bridge] Node not found for UUID: {}", nodeUuid);
                return;
            }

            log.debug("[JS Bridge] Found node: {} (san: {})", targetNode.getNodeUuid(), targetNode.getSan());

            Variation targetVariation = findVariationForNode(targetNode);
            if (targetVariation == null) {
                log.warn("[JS Bridge] Variation not found for node: {}", targetNode.getSan());
                return;
            }

            List<ParentNode> moves = targetVariation.getMoves();
            int moveIndex = moves.indexOf(targetNode);
            if (moveIndex < 0) {
                log.warn("[JS Bridge] Move index not found for node: {}", targetNode.getSan());
                return;
            }

            log.debug("[JS Bridge] Navigating to: variation={}, index={}, move={}",
                    targetVariation.getName(), moveIndex, targetNode.getSan());

            navController.navigateToMoveInVariation(targetVariation, moveIndex);

            if (notationView != null) {
                notationView.refreshDisplay();
            }
        });
    }

    /**
     * Поиск узла по UUID во всем дереве
     */
    private MoveNode findNodeByUuid(String uuid) {
        if (navController == null) return null;

        RootNode rootNode = navController.getRootNode();
        if (rootNode == null) return null;

        // Обходим все варианты в корне
        for (Variation var : rootNode.getSubVariations()) {
            MoveNode found = findNodeInVariation(var, uuid);
            if (found != null) return found;
        }

        return null;
    }

    /**
     * Рекурсивный поиск узла в варианте и его подвариантах
     */
    private MoveNode findNodeInVariation(Variation variation, String uuid) {
        if (variation == null) return null;

        // Проверяем все ходы в текущем варианте
        for (ParentNode node : variation.getMoves()) {
            if (node instanceof MoveNode moveNode) {
                if (moveNode.getNodeUuid().equals(uuid)) {
                    return moveNode;
                }
            }
            // Проверяем подварианты
            if (!node.getSubVariations().isEmpty()) {
                for (Variation subVar : node.getSubVariations()) {
                    MoveNode found = findNodeInVariation(subVar, uuid);
                    if (found != null) return found;
                }
            }
        }

        return null;
    }

    /**
     * Поиск варианта для узла
     */
    private Variation findVariationForNode(MoveNode node) {
        if (navController == null) return null;

        RootNode rootNode = navController.getRootNode();
        if (rootNode == null) return null;

        // Проверяем все варианты в корне
        for (Variation var : rootNode.getSubVariations()) {
            if (var.getMoves().contains(node)) {
                return var;
            }
            // Проверяем подварианты
            Variation found = findVariationRecursive(var, node);
            if (found != null) return found;
        }

        return null;
    }

    /**
     * Рекурсивный поиск варианта
     */
    private Variation findVariationRecursive(Variation variation, MoveNode targetNode) {
        if (variation == null) return null;

        for (ParentNode node : variation.getMoves()) {
            if (!node.getSubVariations().isEmpty()) {
                for (Variation subVar : node.getSubVariations()) {
                    if (subVar.getMoves().contains(targetNode)) {
                        return subVar;
                    }
                    Variation found = findVariationRecursive(subVar, targetNode);
                    if (found != null) return found;
                }
            }
        }
        return null;
    }
}