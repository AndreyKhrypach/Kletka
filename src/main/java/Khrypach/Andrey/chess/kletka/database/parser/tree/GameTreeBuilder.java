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

package Khrypach.Andrey.chess.kletka.database.parser.tree;

import Khrypach.Andrey.chess.kletka.gui.board.BoardReconstructor;
import Khrypach.Andrey.chess.kletka.gui.board.PathBuilder;
import Khrypach.Andrey.chess.kletka.gui.board.VariationManager;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.model.*;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Строит дерево игры из PGN
 * Управляет состоянием дерева и добавлением ходов
 */
public class GameTreeBuilder {

    private static final Logger log = LoggerFactory.getLogger(GameTreeBuilder.class);
    private final LanguageManager lang = LanguageManager.getInstance();

    // ========== ГЕТТЕРЫ ==========
    // Состояние дерева
    @Getter
    private Variation rootVariation;
    @Getter
    private Variation mainLine;
    // ========== СЕТТЕРЫ ==========
    @Setter
    @Getter
    private Variation currentVariation;
    @Setter
    @Getter
    private ParentNode currentNode;
    @Setter
    @Getter
    private Board currentBoard;
    @Setter
    @Getter
    private Board initialBoard;

    // Сервисы
    @Setter
    @Getter
    private BoardReconstructor boardReconstructor;
    @Setter
    @Getter
    private VariationManager variationManager;
    @Getter
    private VariationNamingService namingService;
    @Getter
    private PathBuilder pathBuilder;

    // Счетчики
    @Getter
    private int moveCounter = 0;

    /**
     * Инициализирует GUI-логику для парсинга
     * Перенесено из PgnParser.initializeGuiLogic()
     */
    public void initializeGuiLogic() {
        log.debug("Initializing GUI logic...");

        RootNode rootNode = createRootNode();

        rootVariation = new Variation(lang.get(LanguageKeys.ROOT));
        rootVariation.setFirstNode(rootNode);
        rootVariation.setMainLine(false);

        mainLine = createMainLine();
        mainLine.setParentVariation(rootVariation);
        mainLine.setParentNodeRef(rootNode);

        rootNode.getSubVariations().add(mainLine);

        namingService = new VariationNamingService();
        pathBuilder = new PathBuilder(rootVariation, mainLine);

        currentBoard = new Board();

        this.initialBoard = currentBoard.clone();

        boardReconstructor = new BoardReconstructor(pathBuilder, initialBoard, false);

        variationManager = new VariationManager(rootNode, rootVariation, mainLine,
                boardReconstructor, namingService);

        currentVariation = mainLine;
        currentNode = null;
    }

    /**
     * Создает корневой узел
     * Перенесено из PgnParser.createRootNode()
     */
    public RootNode createRootNode() {
        return new RootNode();
    }

    /**
     * Создает главную линию
     * Перенесено из PgnParser.createMainLine()
     */
    public Variation createMainLine() {
        Variation mainLine = new Variation(lang.get(LanguageKeys.MAIN_LINE));
        mainLine.setMainLine(true);
        return mainLine;
    }

    /**
     * Добавляет ход в дерево с учетом новой структуры
     * При добавлении хода на развилке:
     * - Если у currentNode есть subVariations, проверяем, не создаем ли мы новый вариант
     * - Если создается вариант, главная линия сохраняется как отдельный подвариант
     */
    public void addMoveToTree(MoveNode moveNode) {
        log.trace("addMoveToTree: {}, currentNode={}, currentVariation={}",
                moveNode.getSan(),
                currentNode != null ? (currentNode.isRoot() ? "ROOT" : currentNode.getSan()) : "null",
                currentVariation != null ? currentVariation.getName() : "null");

        moveNode.setOwningVariation(currentVariation);

        if (currentNode == null) {
            if (currentVariation == mainLine && currentVariation.isEmpty()) {
                currentVariation.addMove(moveNode);

                RootNode rootNode = (RootNode) rootVariation.getFirstNode();
                if (rootNode != null) {
                    rootNode.setNext(moveNode);
                    moveNode.setParent(rootNode);
                    moveNode.setForkNode(rootNode);
                }

                if (variationManager != null) {
                    variationManager.setMainLine(currentVariation);
                }
                log.trace("Added first move to mainLine");
            } else {
                currentVariation.addMove(moveNode);

                ParentNode parentNode = currentVariation.getParentNodeRef();
                if (parentNode != null) {
                    moveNode.setParent(parentNode);
                    moveNode.setForkNode(parentNode);
                }
                log.trace("Added first move to variation: {}", currentVariation.getName());
            }

            currentNode = moveNode;
            moveCounter++;
            return;
        }

        if (!currentNode.getSubVariations().isEmpty()) {
            log.trace("currentNode has subVariations (fork), checking if new variation should be created");

            String uci = moveNode.getUciMove();
            for (Variation var : currentNode.getSubVariations()) {
                if (var == null || var.isEmpty()) continue;
                ParentNode firstMove = var.getFirstNode();
                if (firstMove != null && !firstMove.isRoot() &&
                        firstMove.getUciMove().equals(uci)) {
                    log.trace("Move already exists as variation: {}", var.getName());
                    currentNode = firstMove;
                    moveCounter++;
                    return;
                }
            }

            if (currentNode.getNext() == null) {
                currentNode.setNext(moveNode);
                moveNode.setParent(currentNode);
                moveNode.setForkNode(currentNode);

                if (currentVariation.isMainLine()) {
                    moveNode.setOwningVariation(currentVariation);
                }

                currentNode = moveNode;
                moveCounter++;
                log.trace("Added move to end of line: {}", moveNode.getSan());
                return;
            }

            log.trace("Fork node with next exists, new variation will be created by VariationParser");
            return;
        }

        if (currentNode.getNext() == null) {
            currentNode.setNext(moveNode);
            moveNode.setParent(currentNode);

            if (currentNode.getForkNode() != null) {
                moveNode.setForkNode(currentNode.getForkNode());
            }

            if (currentVariation != null) {
                moveNode.setOwningVariation(currentVariation);
            }

            currentNode = moveNode;
            moveCounter++;
            log.trace("Added move to end: {}", moveNode.getSan());
        } else {
            ParentNode nextNode = currentNode.getNext();
            if (nextNode.getUciMove().equals(moveNode.getUciMove())) {
                currentNode = nextNode;
                log.trace("Moved to existing next: {}", nextNode.getSan());
            } else {
                log.warn("Cannot add move - next exists and doesn't match. " +
                        "This should be handled by VariationManager.createNewVariation()");
            }
        }
    }

    /**
     * Устанавливает absolutePly для узла
     * Перенесено из PgnParser.setAbsolutePlyForNode()
     */
    public void setAbsolutePlyForNode(MoveNode moveNode) {
        if (moveNode == null || currentBoard == null) {
            return;
        }

        int fullMoves = currentBoard.getMoveCounter();
        boolean isWhiteToMove = currentBoard.getSideToMove() == Side.WHITE;
        int absolutePly;
        if (isWhiteToMove) {
            absolutePly = (fullMoves - 1) * 2 + 1;
        } else {
            absolutePly = (fullMoves - 1) * 2 + 2;
        }
        moveNode.setAbsolutePly(absolutePly);
    }

    public void reset() {
        rootVariation = null;
        mainLine = null;
        currentVariation = null;
        currentNode = null;
        currentBoard = null;
        initialBoard = null;
        boardReconstructor = null;
        variationManager = null;
        namingService = null;
        pathBuilder = null;
        moveCounter = 0;
    }

}