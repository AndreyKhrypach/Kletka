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

import Khrypach.Andrey.chess.kletka.database.eco.EcoEntry;
import Khrypach.Andrey.chess.kletka.database.eco.EcoService;
import Khrypach.Andrey.chess.kletka.engine.UciEngineManager;
import Khrypach.Andrey.chess.kletka.gui.dialogs.DialogCoordinator;
import Khrypach.Andrey.chess.kletka.gui.dialogs.VariationChoiceDialog;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.model.*;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

/**
 * Контроллер для навигации по истории ходов с поддержкой вариантов
 * Новая версия с использованием ParentNode/RootNode/MoveNode
 */
public class MoveNavigationController {
    private static final Logger log = LoggerFactory.getLogger(MoveNavigationController.class);
    private final LanguageManager languageManager = LanguageManager.getInstance();

    private static final long ENGINE_UPDATE_DELAY_MS = 200;

    // Корневой узел (начальная позиция)
    @Getter
    private RootNode rootNode;

    // Корневой вариант (содержит все первые ходы)
    @Getter
    private Variation rootVariation;

    // Главная линия (один из подвариантов корня)
    @Getter
    private Variation mainLine;

    // Текущий вариант и текущий узел
    @Getter
    @Setter
    private Variation currentVariation;
    @Getter
    @Setter
    private ParentNode currentNode;

    // Начальная доска (расставленная позиция)
    @Getter
    private Board initialPosition;

    // Флаг, что партия начинается с черных
    @Getter
    private boolean startWithBlack = false;

    // Сервис именования вариантов
    private final VariationNamingService namingService;

    // Компоненты UI
    @Getter
    private final ChessBoardView boardView;
    private final NotationView notationView;
    private boolean isUpdatingNames = false;

    // Движок
    @Getter
    private final UciEngineManager engineManager;
    private boolean engineUpdateScheduled = false;

    // Стек для навигации между вариантами
    private Stack<NavigationPoint> navigationStack = new Stack<>();

    @Getter
    private Runnable onPositionChanged;

    @Getter
    private PathBuilder pathBuilder;

    @Getter
    private BoardReconstructor boardReconstructor;

    @Getter
    private DialogCoordinator dialogCoordinator;

    @Getter
    private VariationManager variationManager;

    // Текущий найденный дебют
    private String currentEco = "";
    private String currentOpeningName = "";
    private boolean openingFound = false;

    // ← Callback при изменении дерева
    @Getter
    @Setter
    private Runnable onTreeChanged;

    public MoveNavigationController(ChessBoardView boardView, NotationView notationView) {
        this.boardView = boardView;
        this.notationView = notationView;
        this.namingService = new VariationNamingService();
        this.engineManager = UciEngineManager.getInstance();

        // Инициализируем пустое дерево
        initializeEmptyTree();

        this.pathBuilder = new PathBuilder(rootVariation, mainLine);
        this.boardReconstructor = new BoardReconstructor(pathBuilder, initialPosition, startWithBlack);
        this.dialogCoordinator = new DialogCoordinator(rootNode, rootVariation, mainLine);
        this.variationManager = new VariationManager(rootNode, rootVariation, mainLine, boardReconstructor, namingService);
    }

    /**
     * Инициализирует пустое дерево вариантов
     */
    private void initializeEmptyTree() {
        rootNode = new RootNode();
        rootVariation = new Variation(languageManager.get(LanguageKeys.ROOT));
        rootVariation.setFirstNode(rootNode);
        rootVariation.setMainLine(false);

        // Создаем главную линию как подвариант корня
        mainLine = new Variation(languageManager.get(MAIN_LINE));
        mainLine.setMainLine(true);
        mainLine.setParentVariation(rootVariation);
        mainLine.setParentNodeRef(rootNode);

        // НЕ ДОБАВЛЯЕМ mainLine в rootNode.getSubVariations() пока нет ходов!
        // rootNode.getSubVariations().add(mainLine); // <-- УБРАТЬ!

        currentVariation = mainLine;
        currentNode = null;
    }

    /**
     * Сбрасывает контроллер с новой доской
     */
    public void resetWithNewBoard(Board newBoard, boolean startWithBlack) {
        log.debug("resetWithNewBoard - newBoard: {}", newBoard != null ? newBoard.getFen() : "null");

        this.initialPosition = newBoard != null ? newBoard.clone() : null;

        log.debug("resetWithNewBoard - initialPosition: {}", initialPosition != null ? initialPosition.getFen() : "null");

        this.startWithBlack = startWithBlack;

        initializeEmptyTree();

        if (rootVariation != null && rootVariation.getFirstNode() == null) {
            rootVariation.setFirstNode(new RootNode());
        }

        this.pathBuilder = new PathBuilder(rootVariation, mainLine);
        this.boardReconstructor = new BoardReconstructor(pathBuilder, initialPosition, startWithBlack);
        this.dialogCoordinator = new DialogCoordinator(rootNode, rootVariation, mainLine);

        variationManager.updateState(rootNode, rootVariation, mainLine);
        variationManager.setBoardReconstructor(this.boardReconstructor);

        if (boardView != null) {
            if (initialPosition != null) {
                boardView.setBoard(initialPosition.clone());
            } else {
                Board startBoard = new Board();
                if (startWithBlack) startBoard.setSideToMove(Side.BLACK);
                boardView.setBoard(startBoard);
            }
            boardView.refreshBoard();
        }

        if (notationView != null) {
            notationView.refreshFromMainLine();
        }

        notifyPositionChanged();
        resetOpening();
    }

    /**
     * Устанавливает начальную позицию (при расстановке)
     */
    public void setInitialPosition(Board board) {
        this.initialPosition = board != null ? board.clone() : null;
    }

    /**
     * Сбрасывает начальную позицию
     */
    public void resetInitialPosition() {
        this.initialPosition = null;
    }

    /**
     * Переход к первому ходу (корневой узел)
     */
    public void goToFirstMove() {
        if (boardView != null && boardView.getCoachTools() != null &&
                boardView.getCoachTools().isPanelExpanded()) {
            boardView.getCoachTools().togglePanel();
        }

        navigationStack.clear();
        log.trace("Stack cleared - going to first move");

        currentVariation = rootVariation;
        currentNode = rootNode;

        restoreBoardFromRoot();
        updateNotationView();
        updateOpeningDisplay();
        sendCurrentPositionToEngine();

        if (boardView != null) {
            boardView.notifyPositionChanged();
        }
    }

    /**
     * Переход к последнему ходу главной линии
     */
    public void goToLastMove() {
        if (boardView != null && boardView.getCoachTools() != null &&
                boardView.getCoachTools().isPanelExpanded()) {
            boardView.getCoachTools().togglePanel();
        }

        if (mainLine == null || mainLine.getMoveCount() == 0) {
            goToFirstMove();
            return;
        }

        navigationStack.clear();
        currentVariation = mainLine;
        currentNode = mainLine.getLastNode();

        // Если последний узел - корень, берем первый ход
        if (currentNode == null || currentNode.isRoot()) {
            List<ParentNode> moves = mainLine.getMoves();
            if (!moves.isEmpty()) {
                currentNode = moves.get(moves.size() - 1);
            } else {
                goToFirstMove();
                return;
            }
        }

        restoreBoardFromCurrentNode();
        updateNotationView();
        updateOpeningDisplay();
        sendCurrentPositionToEngine();

        if (boardView != null) {
            boardView.notifyPositionChanged();
        }
    }

    /**
     * Переход к предыдущему ходу
     */
    public void goToPreviousMove() {
        log.trace("goToPreviousMove - currentVariation: {}, currentNode: {}",
                currentVariation != null ? currentVariation.getName() + " (id=" + currentVariation.getId() + ")" : "null",
                currentNode != null ? (currentNode.isRoot() ? "ROOT" : currentNode.getSan()) : "null");

        if (isAtRoot()) {
            log.trace("At root - returning");
            return;
        }

        if (currentNode == null) {
            log.trace("currentNode is null, going to root");
            goToFirstMove();
            return;
        }

        ParentNode parent = currentNode.getParent();
        if (parent != null) {
            log.trace("parent: {}", parent.isRoot() ? "ROOT" : parent.getSan());

            if (parent.isRoot()) {
                log.trace("Parent is root, switching to rootVariation");
                currentVariation = rootVariation;
            }

            currentNode = parent;

            log.trace("After transition - currentNode: {}, currentVariation: {}",
                    currentNode.isRoot() ? "ROOT" : currentNode.getSan(),
                    currentVariation != null ? currentVariation.getName() : "null");

            restoreBoardFromCurrentNode();
            updateNotationView();
            updateOpeningDisplay();
            sendCurrentPositionToEngine();
            return;
        }

        log.trace("No parent, going to root");
        goToFirstMove();
    }

    /**
     * Переход к следующему ходу
     */
    public void goToNextMove() {
        log.trace("goToNextMove - currentVariation: {}, currentNode: {}",
                currentVariation != null ? currentVariation.getName() + " (id=" + currentVariation.getId() + ")" : "null",
                currentNode != null ? (currentNode.isRoot() ? "ROOT" : currentNode.getSan()) : "null");

        if (isAtRoot()) {
            log.trace("At root!");
            showAvailableFirstMoves();
            return;
        }

        if (currentNode == null) {
            log.trace("currentNode is null, going to first move");
            goToFirstMove();
            return;
        }

        if (currentNode.getNext() != null) {
            ParentNode nextNode = currentNode.getNext();
            log.trace("Has next move: {}", nextNode.getSan());

            if (hasForkAtNode(currentNode)) {
                navigationStack.push(new NavigationPoint(currentVariation, currentNode));
                log.trace("Saved point to stack");
            }

            currentNode = nextNode;

            if (currentNode.getOwningVariation() == null) {
                currentNode.setOwningVariation(currentVariation);
            }

            restoreBoardFromCurrentNode();
            updateNotationView();
            updateOpeningDisplay();
            sendCurrentPositionToEngine();

            if (hasForkAtNode(currentNode)) {
                boolean isWhiteTurn = (currentNode.getAbsolutePly() % 2 == 0);
                showBranchChoiceDialog(currentNode, isWhiteTurn);
            }

            if (boardView != null) {
                boardView.notifyPositionChanged();
            }
            return;
        }

        if (hasForkAtNode(currentNode)) {
            log.trace("No next, but has sub-variations");
            boolean isWhiteTurn = (currentNode.getAbsolutePly() % 2 == 0);
            showBranchChoiceDialog(currentNode, isWhiteTurn);
            return;
        }

        log.trace("No next moves (end of line)");
    }

    /**
     * Показывает доступные первые ходы из корневой позиции
     */
    private void showAvailableFirstMoves() {
        List<Variation> firstMoves = new ArrayList<>();

        log.trace("showAvailableFirstMoves() - rootNode.getSubVariations() size: {}",
                rootNode.getSubVariations() != null ? rootNode.getSubVariations().size() : 0);

        if (rootNode.getSubVariations() != null) {
            for (int i = 0; i < rootNode.getSubVariations().size(); i++) {
                Variation var = rootNode.getSubVariations().get(i);
                if (var == null) {
                    log.trace("  [{}] NULL", i);
                    continue;
                }
                String firstMove = "EMPTY";
                if (!var.isEmpty() && var.getFirstNode() != null && !var.getFirstNode().isRoot()) {
                    firstMove = var.getFirstNode().getSan() + " (uci=" + var.getFirstNode().getUciMove() + ")";
                }
                log.trace("  [{}] name='{}', id={}, firstMove={}, isMainLine={}, isEmpty={}",
                        i, var.getName(), var.getId(), firstMove, var.isMainLine(), var.isEmpty());
            }
        }

        Variation mainLineVar = null;
        assert rootNode.getSubVariations() != null;
        for (Variation var : rootNode.getSubVariations()) {
            if (var.isMainLine()) {
                mainLineVar = var;
                break;
            }
        }

        log.trace("mainLineVar found: {}", mainLineVar != null ? mainLineVar.getName() + " (id=" + mainLineVar.getId() + ")" : "null");

        if (mainLineVar != null) {
            firstMoves.add(mainLineVar);
            log.trace("Added mainLineVar to firstMoves: {}", mainLineVar.getName());
        }

        for (Variation var : rootNode.getSubVariations()) {
            if (!var.isMainLine()) {
                firstMoves.add(var);
                String firstMove = "EMPTY";
                if (!var.isEmpty() && var.getFirstNode() != null && !var.getFirstNode().isRoot()) {
                    firstMove = var.getFirstNode().getSan();
                }
                log.trace("Added non-main variation to firstMoves: {}, firstMove={}", var.getName(), firstMove);
            }
        }

        log.trace("firstMoves final size: {}", firstMoves.size());
        for (int i = 0; i < firstMoves.size(); i++) {
            Variation var = firstMoves.get(i);
            if (var == null) continue;
            String firstMove = "EMPTY";
            if (!var.isEmpty() && var.getFirstNode() != null && !var.getFirstNode().isRoot()) {
                firstMove = var.getFirstNode().getSan();
            }
            log.trace("  firstMoves[{}] = {}, firstMove={}, id={}, isMainLine={}",
                    i, var.getName(), firstMove, var.getId(), var.isMainLine());
        }

        if (firstMoves.isEmpty()) {
            log.trace("No available first moves");
            return;
        }

        Variation targetVar;
        if (firstMoves.size() == 1) {
            targetVar = firstMoves.get(0);
            log.trace("Only one variation: {}", targetVar.getName());
        } else {
            log.trace("Multiple variations ({}), showing dialog", firstMoves.size());
            boolean nextIsWhite = true;
            showBranchChoiceDialog(rootNode, firstMoves, nextIsWhite);
            return;
        }

        enterVariation(targetVar);

        if (hasForkAtNode(currentNode)) {
            boolean isWhiteTurn = (currentNode.getAbsolutePly() % 2 == 0);
            showBranchChoiceDialog(currentNode, isWhiteTurn);
        }
    }

    /**
     * Вход в вариант с сохранением текущей позиции в стек
     */
    private void enterVariation(Variation targetVariation) {
        if (targetVariation == null || targetVariation.isEmpty()) return;

        if (!isAtRoot()) {
            navigationStack.push(new NavigationPoint(currentVariation, currentNode));
            log.trace("Saved to stack: var={}, node={}",
                    currentVariation.getName(),
                    currentNode != null ? currentNode.getSan() : "null");
        }

        currentVariation = targetVariation;
        currentNode = targetVariation.getFirstNode();

        if (currentNode == null || currentNode.isRoot()) {
            List<ParentNode> moves = targetVariation.getMoves();
            if (!moves.isEmpty()) {
                currentNode = moves.get(0);
            } else {
                currentVariation = rootVariation;
                currentNode = rootNode;
                return;
            }
        }

        if (currentNode.getOwningVariation() == null) {
            currentNode.setOwningVariation(targetVariation);
        }

        log.trace("Entered variation: {}, first node: {}", targetVariation.getName(), currentNode.getSan());

        restoreBoardFromCurrentNode();
        updateNotationView();
        sendCurrentPositionToEngine();
        if (boardView != null) {
            boardView.notifyPositionChanged();
        }
    }

    /**
     * Показывает диалог выбора варианта на развилке
     */
    private void showBranchChoiceDialog(ParentNode node, boolean nextIsWhite) {
        if (node == null || node.getSubVariations() == null || node.getSubVariations().isEmpty()) {
            return;
        }

        List<Variation> allVariations = new ArrayList<>();
        for (Variation var : node.getSubVariations()) {
            if (var != null && !var.isEmpty()) {
                allVariations.add(var);
            }
        }

        if (allVariations.isEmpty()) {
            return;
        }

        showBranchChoiceDialog(node, allVariations, nextIsWhite);
    }

    /**
     * Показывает диалог выбора варианта на развилке с заданным списком вариантов
     */
    private void showBranchChoiceDialog(ParentNode node, List<Variation> variations, boolean nextIsWhite) {
        log.trace("showBranchChoiceDialog - node: {}, variations.size(): {}, nextIsWhite: {}",
                node != null ? (node.isRoot() ? "ROOT" : node.getSan()) : "null",
                variations != null ? variations.size() : 0,
                nextIsWhite);

        if (node != null) {
            log.trace("node.getAbsolutePly(): {}", node.getAbsolutePly());
            log.trace("node.getNext(): {}", node.getNext() != null ? node.getNext().getSan() : "null");
            log.trace("node.getSubVariations().size(): {}",
                    node.getSubVariations() != null ? node.getSubVariations().size() : 0);

            if (node.getSubVariations() != null) {
                for (int i = 0; i < node.getSubVariations().size(); i++) {
                    Variation var = node.getSubVariations().get(i);
                    if (var == null) {
                        log.trace("  subVar[{}] NULL", i);
                        continue;
                    }
                    String firstMove = "EMPTY";
                    if (!var.isEmpty() && var.getFirstNode() != null && !var.getFirstNode().isRoot()) {
                        firstMove = var.getFirstNode().getSan() + " (uci=" + var.getFirstNode().getUciMove() + ")";
                    }
                    log.trace("  subVar[{}] name='{}', id={}, firstMove={}, isMainLine={}",
                            i, var.getName(), var.getId(), firstMove, var.isMainLine());
                }
            }
        }

        if (variations == null || variations.isEmpty()) {
            assert node != null;
            if (node.getNext() != null && !node.getNext().isRoot()) {
                currentNode = node.getNext();
                restoreBoardFromCurrentNode();
                updateNotationView();
                sendCurrentPositionToEngine();
                if (boardView != null) {
                    boardView.notifyPositionChanged();
                }
            }
            return;
        }

        log.trace("showBranchChoiceDialog variations: {}",
                variations.stream().map(v -> v.getFirstNode().getSan()).collect(Collectors.toList()));

        String sideStr = nextIsWhite ? languageManager.get(GAME_WHITE) : languageManager.get(GAME_BLACK);

        variations.sort((v1, v2) -> {
            if (v1.isMainLine() && !v2.isMainLine()) return -1;
            if (!v1.isMainLine() && v2.isMainLine()) return 1;
            return v1.getName().compareTo(v2.getName());
        });

        if (variations.size() == 1) {
            Variation target = variations.get(0);
            if (target != currentVariation) {
                switchToVariation(target);
            } else {
                assert node != null;
                if (node.getNext() != null && !node.getNext().isRoot()) {
                    currentNode = node.getNext();
                    restoreBoardFromCurrentNode();
                    updateNotationView();
                    sendCurrentPositionToEngine();
                    if (boardView != null) {
                        boardView.notifyPositionChanged();
                    }
                }
            }
            return;
        }

        List<VariationChoiceDialog.Choice> choices = new ArrayList<>();
        for (Variation var : variations) {
            log.trace("  showBranchChoiceDialog, variation name={}, first move={}",
                    var.getName(), var.getFirstNode().getSan());
            String moveDesc = getVariationFirstMoveDesc(var);

            String displayName = var.isMainLine() ?
                    languageManager.get(MAIN_LINE) : var.getName();

            if (displayName == null || displayName.isEmpty()) {
                displayName = languageManager.get(VARIATION_DEFAULT_NAME);
            }

            choices.add(new VariationChoiceDialog.Choice(
                    var,
                    String.format("%s (%s): %s", displayName, sideStr, ChessSymbols.convertToChessSymbols(moveDesc)),
                    false
            ));
        }

        VariationChoiceDialog dialog = new VariationChoiceDialog(choices);
        VariationChoiceDialog.Choice selected = dialog.showAndWait();

        if (selected != null && selected.variation() != null) {
            if (selected.variation() != currentVariation) {
                switchToVariation(selected.variation());
            } else {
                assert node != null;
                if (node.getNext() != null && !node.getNext().isRoot()) {
                    navigationStack.push(new NavigationPoint(currentVariation, currentNode));
                    currentNode = node.getNext();
                    restoreBoardFromCurrentNode();
                    updateNotationView();
                    sendCurrentPositionToEngine();
                    if (boardView != null) {
                        boardView.notifyPositionChanged();
                    }
                }
            }
        }
    }

    /**
     * Вспомогательный метод для получения первого хода варианта
     */
    private String getVariationFirstMoveDesc(Variation var) {
        if (var == null) return "?";

        ParentNode firstMove = var.getFirstNode();
        if (firstMove != null && !firstMove.isRoot()) {
            return firstMove.getSan();
        }

        List<ParentNode> moves = var.getMoves();
        if (!moves.isEmpty()) {
            return moves.get(0).getSan();
        }

        return "?";
    }

    /**
     * Переключается на указанный вариант
     */
    private void switchToVariation(Variation variation) {
        if (variation == null) return;

        if (!isAtRoot()) {
            navigationStack.push(new NavigationPoint(currentVariation, currentNode));
        }

        currentVariation = variation;
        currentNode = variation.getFirstNode();

        if (currentNode == null || currentNode.isRoot()) {
            List<ParentNode> moves = variation.getMoves();
            if (!moves.isEmpty()) {
                currentNode = moves.get(0);
            }
        }

        if (currentNode != null && currentNode.getOwningVariation() == null) {
            currentNode.setOwningVariation(variation);
        }

        restoreBoardFromCurrentNode();
        updateNotationView();
        sendCurrentPositionToEngine();
        if (boardView != null) {
            boardView.notifyPositionChanged();
        }

        if (hasForkAtNode(currentNode)) {
            boolean isWhiteTurn = (currentNode.getAbsolutePly() % 2 == 0);
            showBranchChoiceDialog(currentNode, isWhiteTurn);
        }
    }

    /**
     * Навигация к конкретному ходу в варианте
     */
    public void navigateToMoveInVariation(Variation targetVariation, int moveIndex) {
        if (targetVariation == null) return;

        List<ParentNode> moves = targetVariation.getMoves();
        if (moveIndex < 0 || moveIndex >= moves.size()) return;

        ParentNode targetNode = moves.get(moveIndex);

        log.trace("Navigating to: {} in variation: {}", targetNode.getSan(), targetVariation.getName());

        List<ParentNode> path = pathBuilder.buildPathFromParents(targetNode);

        if (path != null && !path.isEmpty()) {
            log.trace("Path via parents found, size: {}", path.size());

            navigationStack.clear();

            for (int i = 0; i < path.size() - 1; i++) {
                ParentNode node = path.get(i);
                if (node.getSubVariations() != null && !node.getSubVariations().isEmpty()) {
                    ParentNode nextNode = path.get(i + 1);
                    Variation foundVar = pathBuilder.findVariationContainingNode(node, nextNode);
                    if (foundVar != null && foundVar != mainLine) {
                        navigationStack.push(new NavigationPoint(foundVar, node));
                    }
                }
            }

            currentVariation = targetVariation;
            currentNode = targetNode;

            if (currentNode.getOwningVariation() == null && targetVariation != mainLine) {
                currentNode.setOwningVariation(targetVariation);
            }

            restoreBoardFromCurrentNode();
            updateNotationView();
            sendCurrentPositionToEngine();

            if (boardView != null) {
                boardView.notifyPositionChanged();
            }
            return;
        }

        log.trace("Fallback: searching via tree traversal");

        navigationStack.clear();

        List<ParentNode> fallbackPath = buildPathViaTraversal(targetNode);
        if (fallbackPath != null && !fallbackPath.isEmpty()) {
            for (int i = 0; i < fallbackPath.size() - 1; i++) {
                ParentNode node = fallbackPath.get(i);
                if (node.getSubVariations() != null && !node.getSubVariations().isEmpty()) {
                    ParentNode nextNode = fallbackPath.get(i + 1);
                    Variation foundVar = pathBuilder.findVariationContainingNode(node, nextNode);
                    if (foundVar != null && foundVar != mainLine) {
                        navigationStack.push(new NavigationPoint(foundVar, node));
                    }
                }
            }
        }

        currentVariation = targetVariation;
        currentNode = targetNode;

        if (currentNode.getOwningVariation() == null && targetVariation != mainLine) {
            currentNode.setOwningVariation(targetVariation);
        }

        restoreBoardFromCurrentNode();
        updateNotationView();
        sendCurrentPositionToEngine();

        if (boardView != null) {
            boardView.notifyPositionChanged();
        }
    }

    /**
     * Вспомогательный метод для поиска пути через обход дерева
     */
    private List<ParentNode> buildPathViaTraversal(ParentNode targetNode) {
        if (targetNode == null || targetNode.isRoot()) {
            return null;
        }

        List<ParentNode> path = new ArrayList<>();
        if (findPathInTree(rootVariation, targetNode, path)) {
            return path;
        }
        return null;
    }

    /**
     * Рекурсивный поиск пути в дереве
     */
    private boolean findPathInTree(Variation variation, ParentNode target, List<ParentNode> path) {
        if (variation == null) return false;

        for (ParentNode node : variation.getMoves()) {
            path.add(node);
            if (node == target) {
                return true;
            }
            if (!node.getSubVariations().isEmpty()) {
                for (Variation subVar : node.getSubVariations()) {
                    if (findPathInTree(subVar, target, path)) {
                        return true;
                    }
                }
            }
            path.remove(path.size() - 1);
        }
        return false;
    }

    /**
     * Добавляет новый ход (основной метод)
     * Возвращает:
     * - true если был создан новый вариант
     * - false если ход добавлен в текущую линию или выполнен переход на существующий
     * - null если требуется диалог (ход не выполнен)
     */
    public Boolean addMove(Move move, Piece piece, boolean isCapture, Piece promotionPiece) {
        if (move == null) {
            log.error("addMove called with null move");
            return false;
        }

        String currentUci = move.getFrom().toString().toLowerCase() +
                move.getTo().toString().toLowerCase();
        if (promotionPiece != null) currentUci += getPromotionChar(promotionPiece);

        log.trace("addMove - currentUci: {}, currentVariation: {}, currentNode: {}",
                currentUci,
                currentVariation != null ? currentVariation.getName() : "null",
                currentNode != null ? (currentNode.isRoot() ? "ROOT" : currentNode.getSan()) : "null");

        VariationStateSnapshot snapshot = variationManager.addMove(move, piece, isCapture, promotionPiece,
                currentVariation, currentNode);

        if (snapshot.operationResult() == null) {
            return null;
        }

        applySnapshot(snapshot);

        restoreBoardFromCurrentNode();
        refreshDisplay();
        notifyPositionChanged();
        sendCurrentPositionToEngine();
        updateOpeningAfterNewMove();

        return snapshot.operationResult();
    }

    /**
     * Применяет снимок состояния к контроллеру
     */
    private void applySnapshot(VariationStateSnapshot snapshot) {
        log.trace("applySnapshot - BEFORE currentNode: {}, snapshot.currentNode: {}",
                currentNode != null ? currentNode.getSan() : "null",
                snapshot.currentNode() != null ? snapshot.currentNode().getSan() : "null");

        this.rootNode = snapshot.rootNode();
        this.rootVariation = snapshot.rootVariation();
        this.mainLine = snapshot.mainLine();
        this.currentVariation = snapshot.currentVariation();
        this.currentNode = snapshot.currentNode();

        log.trace("applySnapshot - AFTER currentNode: {}",
                currentNode != null ? currentNode.getSan() : "null");

        updatePathBuilder();

        if (notationView != null) {
            log.trace("Updating notation...");
            Platform.runLater(() -> {
                notationView.refreshFromMainLine();
                notationView.updateNotationDisplayWithVisitor();
                log.trace("Notation updated");
            });
        } else {
            log.error("notationView is NULL in applySnapshot!");
        }
    }

    /**
     * Просто обновляет отображение нотации (без пересчета имен вариантов)
     * Используется для простых ходов, когда структура вариантов не меняется
     */
    private void refreshDisplay() {
        if (notationView != null) {
            log.trace("refreshDisplay called");
            Platform.runLater(() -> {
                notationView.refreshFromMainLine();
                notationView.updateNotationDisplayWithVisitor();
                log.trace("refreshDisplay completed");
            });
        } else {
            log.error("notationView is NULL in refreshDisplay");
        }
    }

    /**
     * Делает текущий вариант главной линией
     */
    public void makeCurrentVariationMainLine() {
        if (currentVariation == null || currentVariation == mainLine) return;
        if (currentVariation == rootVariation) return;

        VariationStateSnapshot snapshot = variationManager.makeCurrentVariationMainLine(currentVariation);
        applySnapshot(snapshot);

        navigationStack.clear();
        restoreBoardFromCurrentNode();
        updateNotationView();
        sendCurrentPositionToEngine();

        resetOpening();
        updateOpeningAfterNewMove();

        if (notationView != null) {
            Platform.runLater(() -> {
                notationView.refreshFromMainLine();
                notationView.updateNotationDisplayWithVisitor();
            });
        }
    }

    /**
     * Показывает диалог выбора варианта и возвращает выбор пользователя
     * Возвращает null если пользователь отменил
     */
    public VariationChoiceDialog.Choice showVariationDialog(Move move, Piece piece,
                                                            boolean isCapture, Piece promotionPiece) {
        if (dialogCoordinator != null) {
            dialogCoordinator.updateState(currentVariation, currentNode);
            return dialogCoordinator.showVariationDialog(move, piece, isCapture, promotionPiece);
        }
        return null;
    }

    /**
     * Применяет выбор пользователя к дереву
     */
    public void applyVariationChoice(VariationChoiceDialog.Choice choice, Move move,
                                     Piece piece, boolean isCapture, Piece promotionPiece) {
        if (choice == null || move == null) return;

        ParentNode newCurrentNode;

        if (choice.isNewVariation()) {
            Variation newVar = variationManager.createNewVariation(move, piece, isCapture, promotionPiece,
                    currentVariation, currentNode);
            if (newVar != null) {
                navigationStack.push(new NavigationPoint(currentVariation, currentNode));
                currentVariation = newVar;
                currentNode = newVar.getFirstNode();
            }
        } else if (choice.variation() != null) {
            newCurrentNode = variationManager.overwriteInSpecificVariation(
                    choice.variation(), move, piece, isCapture, promotionPiece,
                    currentVariation, currentNode);

            if (newCurrentNode != null) {
                if (choice.variation() != currentVariation) {
                    navigationStack.push(new NavigationPoint(currentVariation, currentNode));
                    currentVariation = choice.variation();
                }
                currentNode = newCurrentNode;
            }
        }

        rootVariation = variationManager.getRootVariation();
        rootNode = variationManager.getRootNode();
        mainLine = variationManager.getMainLine();

        restoreBoardFromCurrentNode();
        updateNotationView();
        updatePathBuilder();
        resetOpening();
        updateOpeningAfterNewMove();

        if (notationView != null) {
            notationView.refreshFromMainLine();
            notationView.updateNotationDisplayWithVisitor();
            notationView.requestLayout();
        }
    }

    /**
     * Публичный метод для удаления варианта
     * Удаляет текущий вариант целиком (если он не главная линия)
     */
    public void deleteCurrentVariation() {
        log.trace("deleteCurrentVariation START");

        if (currentVariation == mainLine || currentVariation == rootVariation) {
            log.warn("Cannot delete main line or root variation");
            return;
        }

        Variation variationToDelete = currentNode.getOwningVariation();

        if (variationToDelete == null) {
            ParentNode temp = currentNode;
            while (temp != null && !temp.isRoot()) {
                if (temp.getOwningVariation() != null) {
                    variationToDelete = temp.getOwningVariation();
                    break;
                }
                temp = temp.getParent();
            }
        }

        if (variationToDelete == null) {
            log.warn("No variation found to delete");
            return;
        }

        ParentNode parentNode = variationToDelete.getParentNodeRef();
        Variation parentVariation = variationToDelete.getParentVariation();

        if (parentNode == null || parentVariation == null) {
            log.error("Cannot delete - parentNodeRef or parentVariation is null");
            return;
        }

        log.trace("Deleting variation: {}", variationToDelete.getName());

        variationManager.deleteCurrentVariation(variationToDelete);

        rootVariation = variationManager.getRootVariation();
        rootNode = variationManager.getRootNode();
        mainLine = variationManager.getMainLine();

        if (parentVariation == rootVariation) {
            currentVariation = mainLine;
        } else {
            currentVariation = parentVariation;
        }
        currentNode = parentNode;
        navigationStack.clear();

        restoreBoardFromCurrentNode();
        updateAllVariationNames();
        sendCurrentPositionToEngine();

        if (boardView != null) {
            boardView.notifyPositionChanged();
        }

        log.trace("deleteCurrentVariation COMPLETED");
    }

    /**
     * Восстанавливает доску из корня
     */
    private void restoreBoardFromRoot() {
        Board board = getStartBoard();
        boardView.setBoard(board);
        boardView.refreshBoard();
    }

    /**
     * Восстанавливает доску из текущего узла
     */
    public void restoreBoardFromCurrentNode() {
        Board board = boardReconstructor.reconstruct(currentVariation, currentNode);
        boardView.setBoard(board);
        boardView.refreshBoard();
    }

    /**
     * Воссоздает доску в позиции указанного узла (публичный метод для NotationView)
     */
    public Board recreateBoardAtNode(Variation variation, ParentNode targetNode) {
        return boardReconstructor.reconstruct(variation, targetNode);
    }

    /**
     * Получает начальную доску
     */
    private Board getStartBoard() {
        if (initialPosition != null) {
            return initialPosition.clone();
        }
        Board board = new Board();
        if (startWithBlack) {
            board.setSideToMove(Side.BLACK);
        }
        return board;
    }

    private String getPromotionChar(Piece piece) {
        return switch (piece) {
            case WHITE_QUEEN, BLACK_QUEEN -> "q";
            case WHITE_ROOK, BLACK_ROOK -> "r";
            case WHITE_BISHOP, BLACK_BISHOP -> "b";
            case WHITE_KNIGHT, BLACK_KNIGHT -> "n";
            default -> "";
        };
    }

    /**
     * Обновляет отображение нотации
     */
    private void updateNotationView() {
        if (notationView != null) {
            log.trace("updateNotationView called");
            Platform.runLater(() -> {
                notationView.refreshFromMainLine();
                notationView.updateNotationDisplayWithVisitor();
                log.trace("updateNotationView completed");
            });
        } else {
            log.error("notationView is NULL in updateNotationView");
        }
    }

    private void notifyPositionChanged() {
        if (onPositionChanged != null) onPositionChanged.run();
    }

    /**
     * Отправляет текущую позицию в движок
     */
    void sendCurrentPositionToEngine() {
        if (boardView.getAnalysisPanel() != null &&
                boardView.getAnalysisPanel().isAnalyzingActive() &&
                engineManager != null && engineManager.isEngineRunning()) {

            long now = System.currentTimeMillis();

            long lastEngineUpdateTime = 0;
            if (now - lastEngineUpdateTime < ENGINE_UPDATE_DELAY_MS) {
                if (!engineUpdateScheduled) {
                    engineUpdateScheduled = true;
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(ENGINE_UPDATE_DELAY_MS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        engineUpdateScheduled = false;
                        sendCurrentPositionToEngineImmediate();
                    });
                }
                return;
            }

            sendCurrentPositionToEngineImmediate();
        }
    }

    private void sendCurrentPositionToEngineImmediate() {
        if (engineManager == null || !engineManager.isEngineRunning()) {
            log.debug("Engine not running, skipping position update");
            return;
        }

        Board currentBoard = boardView.getCurrentBoard();

        engineManager.stopAnalysis();
        engineManager.sendPosition(currentBoard);

        engineManager.startAnalysisWithDepth(14);
    }

    /**
     * Обновляет имена всех вариантов
     */
    public void updateAllVariationNames() {
        if (isUpdatingNames) {
            log.trace("Skipping duplicate update");
            return;
        }

        isUpdatingNames = true;
        try {
            if (mainLine == null) {
                log.debug("mainLine is null (empty game)");
                return;
            }

            ParentNode firstNode = mainLine.getFirstNode();
            if (firstNode == null) {
                log.debug("mainLine has no firstNode yet (empty game)");
                return;
            }

            variationManager.updateAllVariationNames();
        } finally {
            isUpdatingNames = false;
        }
    }

    /**
     * Проверяет, находится ли навигация в корневой позиции
     */
    private boolean isAtRoot() {
        if (currentVariation == rootVariation || currentVariation == null) {
            return currentNode == null || currentNode.isRoot();
        }
        return false;
    }

    /**
     * Проверяет, есть ли развилка на узле
     */
    private boolean hasForkAtNode(ParentNode node) {
        if (node == null) return false;
        if (node.getSubVariations() == null || node.getSubVariations().isEmpty()) {
            return false;
        }
        for (Variation var : node.getSubVariations()) {
            if (var != null && !var.isEmpty()) {
                return true;
            }
        }
        return false;
    }


    /**
     * Создает панель навигации (кнопки)
     */
    public HBox createNavigationPanel() {
        HBox navButtons = new HBox(10);
        navButtons.setAlignment(Pos.CENTER);
        navButtons.setPadding(new Insets(5, 0, 10, 0));
        navButtons.setStyle("-fx-background-color: transparent;");

        ImageView whiteKingIcon = boardView.getWhiteKingIcon();
        ImageView blackKingIcon = boardView.getBlackKingIcon();

        updateTurnIndicators(whiteKingIcon, blackKingIcon);

        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);

        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        HBox navCenterBox = new HBox(10);
        navCenterBox.setAlignment(Pos.CENTER);

        Button firstBtn = createNavButton("⏮", languageManager.get(NAV_TOOLTIP_FIRST));
        Button prevBtn = createNavButton("◀", languageManager.get(NAV_TOOLTIP_PREV));
        Button nextBtn = createNavButton("▶", languageManager.get(NAV_TOOLTIP_NEXT));
        Button lastBtn = createNavButton("⏭", languageManager.get(NAV_TOOLTIP_LAST));

        double buttonWidth = 60;
        firstBtn.setPrefWidth(buttonWidth);
        prevBtn.setPrefWidth(buttonWidth);
        nextBtn.setPrefWidth(buttonWidth);
        lastBtn.setPrefWidth(buttonWidth);

        firstBtn.setOnAction(e -> Platform.runLater(this::goToFirstMove));
        prevBtn.setOnAction(e -> Platform.runLater(this::goToPreviousMove));
        nextBtn.setOnAction(e -> Platform.runLater(this::goToNextMove));
        lastBtn.setOnAction(e -> Platform.runLater(this::goToLastMove));

        navCenterBox.getChildren().addAll(firstBtn, prevBtn, nextBtn, lastBtn);

        navButtons.getChildren().addAll(whiteKingIcon, leftSpacer, navCenterBox, rightSpacer, blackKingIcon);

        return navButtons;
    }

    private Button createNavButton(String symbol, String tooltip) {
        Button btn = new Button(symbol);
        btn.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-min-width: 60px; -fx-min-height: 40px;");
        btn.setTooltip(new Tooltip(tooltip));
        btn.setFocusTraversable(true);
        return btn;
    }

    public void updateTurnIndicators(ImageView whiteIcon, ImageView blackIcon) {
        Side sideToMove = boardView.getCurrentBoard().getSideToMove();
        if (sideToMove == Side.WHITE) {
            whiteIcon.setVisible(true);
            blackIcon.setVisible(false);
        } else {
            whiteIcon.setVisible(false);
            blackIcon.setVisible(true);
        }
    }

    public void handleKeyPress(KeyEvent event) {
        log.trace("handleKeyPress: code={}", event.getCode());
        switch (event.getCode()) {
            case LEFT -> {
                log.trace("LEFT pressed");
                goToPreviousMove();
            }
            case RIGHT -> {
                log.trace("RIGHT pressed");
                goToNextMove();
            }
            case UP -> {
                log.trace("UP pressed");
                goToFirstMove();
            }
            case DOWN -> {
                log.trace("DOWN pressed");
                goToLastMove();
            }
            default -> {
            }
        }
    }

    /**
     * Возвращает текущий общий полуход (ply) от начала партии
     */
    public int getCurrentTotalPly() {
        if (currentNode == null || currentNode.isRoot()) {
            return 0;
        }
        return currentNode.getAbsolutePly();
    }

    /**
     * Обновляет PathBuilder после изменения mainLine или дерева
     */
    private void updatePathBuilder() {
        if (pathBuilder != null) {
            this.pathBuilder = new PathBuilder(rootVariation, mainLine);
            this.boardReconstructor = new BoardReconstructor(pathBuilder, initialPosition, startWithBlack);
            this.dialogCoordinator = new DialogCoordinator(rootNode, rootVariation, mainLine);

            variationManager.updateState(rootNode, rootVariation, mainLine);

            log.trace("PathBuilder updated with new mainLine: {}, currentNode: {}",
                    mainLine != null ? mainLine.getName() : "null",
                    currentNode != null ? currentNode.getSan() : "null");
        }
    }

    public void loadGameTree(RootNode newRootNode, Variation newMainLine,
                             Variation newRootVariation, Board initialBoard) {
        navigationStack.clear();

        this.rootNode = newRootNode;
        this.rootVariation = newRootVariation;
        this.mainLine = newMainLine;
        this.initialPosition = initialBoard != null ? initialBoard.clone() : null;

        this.currentVariation = mainLine;
        this.currentNode = mainLine.getFirstNode();

        this.pathBuilder = new PathBuilder(rootVariation, mainLine);
        this.boardReconstructor = new BoardReconstructor(pathBuilder, initialPosition, startWithBlack);

        if (this.currentNode == null || this.currentNode.isRoot()) {
            List<ParentNode> moves = mainLine.getMoves();
            if (!moves.isEmpty()) {
                this.currentNode = moves.get(0);
            } else {
                this.currentNode = rootNode;
            }
        }

        this.pathBuilder = new PathBuilder(rootVariation, mainLine);
        this.boardReconstructor = new BoardReconstructor(pathBuilder, initialPosition, startWithBlack);
        this.dialogCoordinator = new DialogCoordinator(rootNode, rootVariation, mainLine);
        this.variationManager = new VariationManager(rootNode, rootVariation, mainLine,
                boardReconstructor, namingService);

        restoreBoardFromCurrentNode();
        updateNotationView();
        notifyPositionChanged();
        resetOpening();
        updateOpeningAfterNewMove();

        log.debug("Game tree loaded successfully, main line moves: {}", mainLine.getMoveCount());
    }

    /**
     * Обновляет дебют после добавления нового хода
     * Дебют может ТОЛЬКО уточняться (меняться на более точный)
     */
    private void updateOpeningAfterNewMove() {
        if (notationView == null || mainLine == null || mainLine.isEmpty()) {
            return;
        }

        try {
            EcoService ecoService = EcoService.getInstance();
            EcoEntry entry = ecoService.findOpeningByPgn(rootNode, mainLine);

            if (entry != null) {
                currentEco = entry.eco();
                currentOpeningName = entry.name();
                openingFound = true;
                notationView.updateOpeningDisplay(currentEco, currentOpeningName);
                log.debug("Opening found: {} - {}", currentEco, currentOpeningName);
            }
        } catch (Exception e) {
            log.error("Error updating opening: {}", e.getMessage(), e);
        }
    }

    /**
     * Обновляет отображение дебюта при навигации
     * (показывает сохраненный дебют, не ищет заново)
     */
    private void updateOpeningDisplay() {
        if (notationView == null) return;

        if (openingFound && !currentEco.isEmpty()) {
            notationView.updateOpeningDisplay(currentEco, currentOpeningName);
        } else {
            notationView.updateOpeningDisplay("", "");
        }
    }

    /**
     * Сбрасывает дебют (при новой партии)
     */
    public void resetOpening() {
        currentEco = "";
        currentOpeningName = "";
        openingFound = false;
        if (notationView != null) {
            notationView.updateOpeningDisplay("", "");
        }
    }

    /**
     * Класс для точки навигации
     */
    private record NavigationPoint(Variation variation, ParentNode node) {
    }
}