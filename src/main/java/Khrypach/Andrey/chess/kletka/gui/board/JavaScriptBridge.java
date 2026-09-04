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

import Khrypach.Andrey.chess.kletka.gui.model.*;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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

            // ========== ВЫПОЛНЯЕМ НАВИГАЦИЮ ==========
            navController.navigateToMoveInVariation(targetVariation, moveIndex);

            // ========== ЯВНО ОБНОВЛЯЕМ НОТАЦИЮ ==========
            if (notationView != null) {
                Platform.runLater(() -> {
                    notationView.refreshFromMainLine();
                    notationView.updateNotationDisplayWithVisitor();
                    log.debug("[JS Bridge] Notation refreshed after navigation");
                });
            }

            // ========== ОБНОВЛЯЕМ ВЫДЕЛЕНИЕ В BOOK VISITOR ==========
            // Передаем новый индекс в BookHtmlVisitor через navController
            if (navController.getNavigationMode() == NavigationMode.BOOK) {
                int selectedIndex = navController.getSelectedVariationIndex();
                // JavaScript обновит выделение через DOM
                log.debug("[JS Bridge] BOOK mode - selected index: {}", selectedIndex);
            }
        });
    }

    @SuppressWarnings("unused")
    public void onMoveUp() {
        log.debug("[JS Bridge] onMoveUp called");
        Platform.runLater(() -> {
            if (navController != null) {
                // Имитируем нажатие клавиши UP
                KeyEvent event = new KeyEvent(
                        KeyEvent.KEY_PRESSED,
                        null, null, KeyCode.UP,
                        false, false, false, false
                );
                navController.handleKeyPress(event);
            }
        });
    }

    @SuppressWarnings("unused")
    public void onMoveDown() {
        log.debug("[JS Bridge] onMoveDown called");
        Platform.runLater(() -> {
            if (navController != null) {
                KeyEvent event = new KeyEvent(
                        KeyEvent.KEY_PRESSED,
                        null, null, KeyCode.DOWN,
                        false, false, false, false
                );
                navController.handleKeyPress(event);
            }
        });
    }

    @SuppressWarnings("unused")
    public void onMoveLeft() {
        log.debug("[JS Bridge] onMoveLeft called");
        Platform.runLater(() -> {
            if (navController != null) {
                KeyEvent event = new KeyEvent(
                        KeyEvent.KEY_PRESSED,
                        null, null, KeyCode.LEFT,
                        false, false, false, false
                );
                navController.handleKeyPress(event);
            }
        });
    }

    @SuppressWarnings("unused")
    public void onMoveRight() {
        log.debug("[JS Bridge] onMoveRight called");
        Platform.runLater(() -> {
            if (navController != null) {
                KeyEvent event = new KeyEvent(
                        KeyEvent.KEY_PRESSED,
                        null, null, KeyCode.RIGHT,
                        false, false, false, false
                );
                navController.handleKeyPress(event);
            }
        });
    }

    @SuppressWarnings("unused")
    public void onHomePressed() {
        log.debug("[JS Bridge] onHomePressed called");
        Platform.runLater(() -> {
            if (navController != null) {
                KeyEvent event = new KeyEvent(
                        KeyEvent.KEY_PRESSED,
                        null, null, KeyCode.HOME,
                        false, false, false, false
                );
                navController.handleKeyPress(event);
            }
        });
    }

    @SuppressWarnings("unused")
    public void onEndPressed() {
        log.debug("[JS Bridge] onEndPressed called");
        Platform.runLater(() -> {
            if (navController != null) {
                KeyEvent event = new KeyEvent(
                        KeyEvent.KEY_PRESSED,
                        null, null, KeyCode.END,
                        false, false, false, false
                );
                navController.handleKeyPress(event);
            }
        });
    }

    @SuppressWarnings("unused")
    public void onEnterPressed() {
        log.debug("[JS Bridge] onEnterPressed called");
        Platform.runLater(() -> {
            if (navController != null) {
                KeyEvent event = new KeyEvent(
                        KeyEvent.KEY_PRESSED,
                        null, null, KeyCode.ENTER,
                        false, false, false, false
                );
                navController.handleKeyPress(event);
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