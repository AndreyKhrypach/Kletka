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
package Khrypach.Andrey.chess.kletka.database.parser.variation;

import Khrypach.Andrey.chess.kletka.database.exception.PgnParseException;
import Khrypach.Andrey.chess.kletka.database.parser.PgnToken;
import Khrypach.Andrey.chess.kletka.database.parser.enums.PgnTokenType;
import Khrypach.Andrey.chess.kletka.database.parser.move.MoveParser;
import Khrypach.Andrey.chess.kletka.database.parser.tree.GameTreeBuilder;
import Khrypach.Andrey.chess.kletka.gui.model.*;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Парсит варианты в PGN
 * Поддерживает вложенные варианты любой глубины
 */
public class VariationParser {

    private static final Logger log = LoggerFactory.getLogger(VariationParser.class);

    private final MoveParser moveParser;
    private final GameTreeBuilder treeBuilder;

    @Setter
    private List<PgnToken> tokens;
    @Getter
    @Setter
    private int position;
    @Getter
    @Setter
    private Variation currentVariation;
    @Getter
    @Setter
    private ParentNode currentNode;
    @Getter
    @Setter
    private Board currentBoard;

    // Стек для вложенных вариантов
    @Getter
    private Deque<VariationContext> variationStack = new ArrayDeque<>();

    private int variationCounter = 0;

    public VariationParser(MoveParser moveParser, GameTreeBuilder treeBuilder) {
        this.moveParser = moveParser;
        this.treeBuilder = treeBuilder;
    }

    /**
     * Парсит вариант с поддержкой вложенных вариантов любой глубины
     */
    public void parseVariationWithGuiLogic() throws PgnParseException {
        variationCounter++;
        int currentVariationId = variationCounter;
        log.debug("parseVariationWithGuiLogic #{} START (depth: {})",
                currentVariationId, variationStack.size() + 1);

        Variation parentVariation = currentVariation;
        ParentNode parentNode = currentNode;
        Board savedBoard = currentBoard.clone();
        int savedPosition = position;

        log.trace("Parent state: variation={}, node={}, FEN={}",
                parentVariation != null ? parentVariation.getName() : "null",
                parentNode != null ? (parentNode.isRoot() ? "ROOT" : parentNode.getSan()) : "null",
                savedBoard.getFen());

        try {
            VariationStartInfo startInfo = determineVariationStartInfo(parentNode);

            log.trace("Variation start info: isWhite={}, moveNumber={}, isCompleteGame={}",
                    startInfo.isWhiteMove, startInfo.moveNumber, startInfo.isCompleteGame);

            ParentNode forkNode;
            if (parentNode != null && !parentNode.isRoot()) {
                forkNode = parentNode.getParent();

                if (forkNode == null || forkNode.isRoot()) {
                    forkNode = treeBuilder.getRootVariation().getFirstNode();
                }
            } else {
                forkNode = treeBuilder.getRootVariation().getFirstNode();
            }

            log.trace("ForkNode: {}", forkNode.isRoot() ? "ROOT" : forkNode.getSan());

            Board variationBoard = getBoardForVariation(startInfo, forkNode);

            log.trace("Variation board FEN: {}", variationBoard.getFen());

            Variation newVariation = new Variation("");
            newVariation.setParentVariation(parentVariation);
            newVariation.setParentNodeRef(forkNode);
            newVariation.setMainLine(false);

            variationStack.push(new VariationContext(
                    parentVariation,
                    parentNode,
                    savedBoard,
                    forkNode,
                    treeBuilder.getMainLine().getLastNode(),
                    currentVariation == treeBuilder.getMainLine(),
                    startInfo.isWhiteMove,
                    startInfo.moveNumber,
                    startInfo.hasMoveNumberInfo,
                    startInfo.isCompleteGame,
                    savedPosition
            ));

            currentVariation = newVariation;
            currentNode = null;
            currentBoard = variationBoard.clone();

            parseVariationMoves(newVariation, startInfo);

            if (!newVariation.isEmpty()) {
                addVariationToTree(forkNode, newVariation);
                log.trace("Variation #{} added to tree with {} moves",
                        currentVariationId, newVariation.getMoveCount());
            } else {
                log.warn("Variation #{} is empty, not adding to tree", currentVariationId);
            }

            restoreFromVariation();

            log.debug("parseVariationWithGuiLogic #{} END", currentVariationId);

        } catch (Exception e) {
            log.error("Error parsing variation #{}: {}", currentVariationId, e.getMessage(), e);

            if (!variationStack.isEmpty()) {
                VariationContext context = variationStack.pop();
                currentVariation = context.parentVariation;
                currentNode = context.parentNode;
                currentBoard = context.savedBoard.clone();
                position = context.savedPosition;
            } else {
                currentVariation = parentVariation;
                currentNode = parentNode;
                currentBoard = savedBoard;
                position = savedPosition;
            }

            skipToVariationEnd();

            throw new PgnParseException("Error parsing variation #" + currentVariationId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Определяет информацию о начале варианта
     */
    private VariationStartInfo determineVariationStartInfo(ParentNode parentNode) {
        ParentNode forkNode = null;
        boolean isWhiteMove = true;
        int moveNumber = 1;
        boolean hasMoveNumberInfo = false;
        boolean isCompleteGame = false;

        int tempPos = position;
        while (tempPos < tokens.size()) {
            PgnToken token = tokens.get(tempPos);

            if (token.type() == PgnTokenType.MOVE) {
                forkNode = determineForkNode(parentNode);
                break;
            } else if (token.type() == PgnTokenType.MOVE_NUMBER_ELLIPSIS) {
                String numStr = token.value().replace("...", "").trim();
                try {
                    moveNumber = Integer.parseInt(numStr);
                    isWhiteMove = false;
                    hasMoveNumberInfo = true;
                    log.trace("First move is BLACK: {}...", moveNumber);

                    forkNode = parentNode != null ? parentNode : treeBuilder.getRootVariation().getFirstNode();
                    break;
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse move number: {}", token.value());
                }
                break;
            } else if (token.type() == PgnTokenType.MOVE_NUMBER) {
                String numStr = token.value().replace(".", "").trim();
                try {
                    moveNumber = Integer.parseInt(numStr);
                    if (moveNumber == 1) {
                        isCompleteGame = true;
                    }
                    hasMoveNumberInfo = true;
                    log.trace("First move is WHITE: {}.", moveNumber);

                    forkNode = determineForkNodeForWhite(parentNode, moveNumber);
                    break;
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse move number = {}", token.value());
                }
                break;
            }
            tempPos++;
        }

        if (forkNode == null) {
            if (parentNode != null && !parentNode.isRoot()) {
                forkNode = parentNode;
            } else {
                forkNode = treeBuilder.getRootVariation().getFirstNode();
            }
            log.trace("ForkNode fallback to: {}", forkNode.isRoot() ? "ROOT" : forkNode.getSan());
        }

        return new VariationStartInfo(forkNode, isWhiteMove, moveNumber,
                hasMoveNumberInfo, isCompleteGame);
    }

    /**
     * Определяет forkNode для белого хода
     */
    private ParentNode determineForkNodeForWhite(ParentNode parentNode, int moveNumber) {
        if (parentNode == null || parentNode.isRoot()) {
            return treeBuilder.getRootVariation().getFirstNode();
        }

        if (parentNode.getAbsolutePly() % 2 == 1) {
            return parentNode.getParent();
        }

        if (parentNode.getAbsolutePly() % 2 == 0) {
            return parentNode;
        }

        return parentNode.getParent();
    }

    /**
     * Определяет forkNode для варианта
     */
    private ParentNode determineForkNode(ParentNode currentNode) {
        if (currentNode == null || currentNode.isRoot()) {
            log.trace("ForkNode: currentNode is null or root, using root");
            return treeBuilder.getRootVariation().getFirstNode();
        }

        ParentNode forkNode = currentNode.getParent();

        if (forkNode == null || forkNode.isRoot()) {
            log.trace("ForkNode: parent is null or root, using root");
            return treeBuilder.getRootVariation().getFirstNode();
        }

        log.trace("ForkNode determined: {} (parent of {})",
                forkNode.isRoot() ? "ROOT" : forkNode.getSan(),
                currentNode.getSan());

        return forkNode;
    }

    /**
     * Получает доску для варианта
     */
    private Board getBoardForVariation(VariationStartInfo startInfo, ParentNode forkNode) {
        if (startInfo.isCompleteGame) {
            return new Board();
        }

        if (forkNode != null && !forkNode.isRoot()) {
            Board board = getBoardForForkNode(forkNode);
            if (board != null) {
                return board;
            }
        }

        return currentBoard.clone();
    }

    /**
     * Парсит ходы варианта
     */
    private void parseVariationMoves(Variation variation, VariationStartInfo startInfo) {
        log.trace("Parsing moves for variation...");

        boolean currentIsWhite = startInfo.isWhiteMove;
        int currentMoveNumber = startInfo.moveNumber;
        int movesParsed = 0;

        skipMoveNumberTokens();

        while (position < tokens.size()) {
            PgnToken token = tokens.get(position);

            log.trace("Parsing token: position={}, type={}, value='{}'",
                    position, token.type(), token.value());

            switch (token.type()) {
                case MOVE_NUMBER:
                case MOVE_NUMBER_ELLIPSIS:
                    currentMoveNumber = updateMoveNumber(token, currentMoveNumber);
                    currentIsWhite = token.type() == PgnTokenType.MOVE_NUMBER;
                    position++;
                    break;

                case MOVE:
                    String moveText = token.value();

                    if (moveText.startsWith("=") || moveText.matches("[+#]+")) {
                        log.trace("Skipping special token: '{}'", moveText);
                        position++;
                        continue;
                    }

                    boolean moveAdded = parseMoveInVariationWithSide(
                            moveText, variation, currentBoard,
                            currentIsWhite, currentMoveNumber
                    );

                    if (moveAdded) {
                        movesParsed++;
                        log.trace("Move {} added: {} ({} move {})",
                                movesParsed, moveText, currentIsWhite ? "WHITE" : "BLACK", currentMoveNumber);
                    }

                    if (currentIsWhite) {
                        currentIsWhite = false;
                    } else {
                        currentIsWhite = true;
                        currentMoveNumber++;
                    }

                    position++;
                    break;

                case VARIATION_START:
                    log.debug("Nested VARIATION_START at position {}", position);

                    Variation savedVar = currentVariation;
                    ParentNode savedNode = currentNode;
                    Board savedBoard = currentBoard.clone();
                    int savedPos = position;

                    try {
                        position++;
                        parseVariationWithGuiLogic();
                        log.debug("Nested variation parsed successfully");
                    } catch (Exception e) {
                        log.error("Error parsing nested variation at position {}: {}", savedPos, e.getMessage(), e);

                        currentVariation = savedVar;
                        currentNode = savedNode;
                        currentBoard = savedBoard;
                        position = savedPos + 1;

                        skipToVariationEnd();

                        log.warn("Recovered from nested variation error, skipped to position {}", position);
                    }
                    break;

                case VARIATION_END:
                    log.trace("VARIATION_END at position {}, moves parsed: {}", position, movesParsed);
                    position++;
                    return;

                case ANNOTATION:
                    handleAnnotation(token.value());
                    position++;
                    break;

                case COMMENT_TEXT:
                    handleComment(token.value());
                    position++;
                    break;

                case RESULT:
                    log.trace("Found RESULT: {}, exiting variation", token.value());
                    position++;
                    return;

                default:
                    log.trace("Skipping token: {}", token);
                    position++;
                    break;
            }

            if (position >= tokens.size()) {
                log.warn("Reached EOF while parsing variation");
                break;
            }
        }

        log.trace("Variation parsing finished, total moves: {}", movesParsed);
    }

    /**
     * Пропускает токены до закрывающей скобки варианта
     */
    private void skipToVariationEnd() {
        int depth = 1;
        while (position < tokens.size() && depth > 0) {
            PgnToken token = tokens.get(position);
            if (token.type() == PgnTokenType.VARIATION_START) {
                depth++;
            } else if (token.type() == PgnTokenType.VARIATION_END) {
                depth--;
                if (depth == 0) {
                    position++;
                    log.debug("Skipped to VARIATION_END at position {}", position);
                    return;
                }
            }
            position++;
        }
        log.warn("Reached EOF while skipping to variation end");
    }

    /**
     * Пропускает токены номеров ходов
     */
    private void skipMoveNumberTokens() {
        while (position < tokens.size()) {
            PgnToken token = tokens.get(position);
            if (token.type() == PgnTokenType.MOVE_NUMBER ||
                    token.type() == PgnTokenType.MOVE_NUMBER_ELLIPSIS) {
                position++;
            } else {
                break;
            }
        }
    }

    /**
     * Обновляет номер хода из токена
     */
    private int updateMoveNumber(PgnToken token, int currentMoveNumber) {
        String numStr = token.value().replaceAll("[.…]", "").trim();
        try {
            int newNumber = Integer.parseInt(numStr);
            if (newNumber > currentMoveNumber) {
                return newNumber;
            }
        } catch (NumberFormatException e) {
            log.warn(" Failed to parse move number: {}", token.value());
        }
        return currentMoveNumber;
    }

    /**
     * Парсит ход внутри варианта с информацией о стороне и номере хода
     */
    public boolean parseMoveInVariationWithSide(String moveText, Variation variation, Board board,
                                                boolean isWhiteMove, int moveNumber) {
        try {
            log.trace("parseMoveInVariation: '{}' ({} move {})",
                    moveText, isWhiteMove ? "WHITE" : "BLACK", moveNumber);

            String cleanMoveText = cleanMoveText(moveText);
            if (cleanMoveText == null || cleanMoveText.trim().isEmpty()) {
                log.warn("Empty move text after cleaning: {}", moveText);
                return false;
            }

            Move move = moveParser.convertSanToMove(cleanMoveText, board);
            if (move == null) {
                log.warn("Could not convert move: {} (clean: {})", moveText, cleanMoveText);
                return false;
            }

            Piece movingPiece = board.getPiece(move.getFrom());
            Side expectedSide = isWhiteMove ? Side.WHITE : Side.BLACK;

            if (movingPiece.getPieceSide() != expectedSide) {
                log.warn("Move side mismatch! Expected: {}, Actual: {}",
                        expectedSide, movingPiece.getPieceSide());
                return false;
            }

            if (!board.isMoveLegal(move, true)) {
                log.warn("Move not legal: {} on board {}", moveText, board.getFen());
                return false;
            }

            boolean isCapture = board.getPiece(move.getTo()) != Piece.NONE;

            Piece promotionPiece = getPromotionPiece(move, moveText, board);

            MoveNode moveNode = new MoveNode(move, movingPiece, isCapture, promotionPiece);

            int absolutePly = isWhiteMove ? (moveNumber - 1) * 2 + 1 : (moveNumber - 1) * 2 + 2;
            moveNode.setAbsolutePly(absolutePly);

            moveNode.setSavedFenBefore(board.getFen());

            board.doMove(move);
            moveNode.setSavedFenAfter(board.getFen());

            if (currentNode == null) {
                variation.addMove(moveNode);
                moveNode.setParent(variation.getParentNodeRef());
            } else {
                currentNode.setNext(moveNode);
                moveNode.setParent(currentNode);
            }
            currentNode = moveNode;

            moveNode.setOwningVariation(variation);

            syncWithParser(currentNode, board, currentVariation);

            log.trace("Move added: {} (ply={}, moveNumber={})",
                    moveText, absolutePly, moveNumber);
            return true;

        } catch (Exception e) {
            log.warn("Error parsing variation move: {}", moveText, e);
            return false;
        }
    }

    /**
     * Очищает текст хода от оценок позиции
     */
    private String cleanMoveText(String moveText) {
        String cleanMoveText = moveText;

        String[] positionSymbols = {
                "±", "∓", "∞", "≅", "=", "+=", "=+", "+–", "–+"
        };

        for (String symbol : positionSymbols) {
            if (moveText.endsWith(symbol)) {
                String withoutSymbol = moveText.substring(0, moveText.length() - symbol.length());
                if (withoutSymbol.isEmpty() ||
                        withoutSymbol.matches("^[A-Za-z]?[a-h]?[1-8]?[xX]?[a-h][1-8]=?[QRBN]?[+#]?$") ||
                        withoutSymbol.equals("O-O") || withoutSymbol.equals("O-O-O")) {
                    cleanMoveText = withoutSymbol;
                    log.trace("Removed position evaluation '{}' from move '{}'",
                            symbol, moveText);
                    break;
                }
            }
        }

        return cleanMoveText;
    }

    /**
     * Получает фигуру промоушена
     */
    private Piece getPromotionPiece(Move move, String moveText, Board board) {
        if (move.getPromotion() != null && move.getPromotion() != Piece.NONE) {
            return move.getPromotion();
        }

        if (moveText.contains("=")) {
            char promotionChar = moveText.charAt(moveText.indexOf('=') + 1);
            return moveParser.charToPiece(promotionChar, board.getSideToMove());
        }

        return null;
    }

    /**
     * Добавляет вариант в дерево с новой структурой
     */
    public void addVariationToTree(ParentNode forkNode, Variation variation) {
        if (forkNode == null || variation.isEmpty()) {
            log.warn("Cannot add variation: forkNode={}, variation={}", forkNode, variation);
            return;
        }

        log.trace("Adding variation to forkNode: {} (isRoot={})",
                forkNode.isRoot() ? "ROOT" : forkNode.getSan(), forkNode.isRoot());

        ParentNode firstNode = variation.getFirstNode();
        if (firstNode != null && !firstNode.isRoot()) {
            String uci = firstNode.getUciMove();

            for (Variation existing : forkNode.getSubVariations()) {
                if (existing == null || existing.isEmpty()) continue;
                ParentNode existingFirst = existing.getFirstNode();
                if (existingFirst == null || existingFirst.isRoot()) continue;

                if (existingFirst.getUciMove().equals(uci)) {
                    log.warn("DUPLICATE variation detected! firstMove={}, uci={}, skipping add",
                            firstNode.getSan(), uci);
                    log.warn("Existing variation: name={}, id={}",
                            existing.getName(), existing.getId());
                    log.warn("New variation: name={}, id={}",
                            variation.getName(), variation.getId());
                    return;
                }
            }
        }

        Variation oldMainLine = null;
        ParentNode oldNext = forkNode.getNext();

        for (Variation var : forkNode.getSubVariations()) {
            if (var.isMainLine()) {
                oldMainLine = var;
                log.trace("Found existing main line in subVariations: {}", var.getName());
                break;
            }
        }

        if (oldMainLine == null && oldNext != null && !oldNext.isRoot()) {
            log.trace("Creating old main line from next: {}", oldNext.getSan());

            String nextUci = oldNext.getUciMove();
            boolean alreadyExists = false;
            for (Variation var : forkNode.getSubVariations()) {
                if (var == null || var.isEmpty()) continue;
                ParentNode varFirst = var.getFirstNode();
                if (varFirst == null || varFirst.isRoot()) continue;
                if (varFirst.getUciMove().equals(nextUci)) {
                    alreadyExists = true;
                    log.warn("Old main line already exists in subVariations, skipping creation");
                    break;
                }
            }

            if (!alreadyExists) {
                Variation correctParentVar = findOwningVariation(oldNext);
                if (correctParentVar == null) {
                    correctParentVar = currentVariation;
                }

                boolean correctIsMainLine = false;
                Variation forkOwner = findOwningVariation(forkNode);
                if (forkOwner != null) {
                    correctIsMainLine = forkOwner.isMainLine();
                } else if (currentVariation != null) {
                    correctIsMainLine = currentVariation.isMainLine();
                }

                oldMainLine = new Variation("~");
                oldMainLine.setFirstNode(oldNext);
                oldMainLine.setMainLine(correctIsMainLine);
                oldMainLine.setParentVariation(correctParentVar);
                oldMainLine.setParentNodeRef(forkNode);

                ParentNode current = oldNext;
                ParentNode prev = forkNode;
                while (current != null && !current.isRoot()) {
                    current.setParent(prev);
                    current.setForkNode(forkNode);
                    current.setOwningVariation(oldMainLine);
                    prev = current;
                    current = current.getNext();
                }

                forkNode.getSubVariations().add(oldMainLine);
                log.trace("Added old main line to subVariations");
            }
        }

        variation.setMainLine(false);
        variation.setParentNodeRef(forkNode);

        Variation correctParentVar = findOwningVariation(forkNode);
        if (correctParentVar != null) {
            variation.setParentVariation(correctParentVar);
        } else {
            variation.setParentVariation(currentVariation);
        }

        forkNode.getSubVariations().add(variation);
        log.trace("Added new variation to subVariations: {}", variation.getName());

        setupVariationNodes(variation, forkNode);

        log.trace("Variation added successfully: {}", variation.getName());
    }

    /**
     * Настраивает связи для узлов варианта
     */
    private void setupVariationNodes(Variation variation, ParentNode forkNode) {
        List<ParentNode> moves = variation.getMoves();
        if (moves.isEmpty()) return;

        for (int i = 0; i < moves.size(); i++) {
            ParentNode node = moves.get(i);
            node.setForkNode(forkNode);
            node.setOwningVariation(variation);

            if (i == 0) {
                node.setParent(forkNode);
            } else {
                node.setParent(moves.get(i - 1));
                moves.get(i - 1).setNext(node);
            }
        }

        moves.get(moves.size() - 1).setNext(null);

        log.trace("Set up {} nodes for variation", moves.size());
    }

    /**
     * Восстанавливает состояние из стека
     */
    public void restoreFromVariation() {
        log.trace("RESTORING FROM VARIATION - stack size: {}", variationStack.size());

        if (!variationStack.isEmpty()) {
            VariationContext context = variationStack.pop();
            currentVariation = context.parentVariation;
            currentNode = context.parentNode;
            currentBoard = context.savedBoard.clone();

            log.trace("RESTORED: variation={}, node={}, FEN={}",
                    currentVariation != null ? currentVariation.getName() : "null",
                    currentNode != null ? (currentNode.isRoot() ? "ROOT" : currentNode.getSan()) : "null",
                    currentBoard.getFen());
        }
    }

    /**
     * Получает доску для forkNode (после выполнения хода forkNode)
     */
    private Board getBoardForForkNode(ParentNode forkNode) {
        if (forkNode == null || forkNode.isRoot()) {
            return null;
        }

        if (forkNode.getSavedFenAfter() != null && !forkNode.getSavedFenAfter().isEmpty()) {
            try {
                Board board = new Board();
                board.loadFromFen(forkNode.getSavedFenAfter());
                log.trace("Restored board from savedFenAfter: {}", forkNode.getSavedFenAfter());
                return board;
            } catch (Exception e) {
                log.warn("Failed to load savedFenAfter: {}", e.getMessage());
            }
        }

        if (forkNode.getSavedFenBefore() != null && !forkNode.getSavedFenBefore().isEmpty()) {
            try {
                Board board = new Board();
                board.loadFromFen(forkNode.getSavedFenBefore());

                if (forkNode instanceof MoveNode moveNode) {
                    board.doMove(moveNode.getMove());
                    log.trace("Restored board from savedFenBefore + move: {}", board.getFen());
                    return board;
                }
            } catch (Exception e) {
                log.warn("Failed to load savedFenBefore: {}", e.getMessage());
            }
        }

        try {
            if (treeBuilder.getBoardReconstructor() != null) {
                Variation owningVar = findOwningVariation(forkNode);
                if (owningVar != null) {
                    Board reconstructed = treeBuilder.getBoardReconstructor().reconstruct(owningVar, forkNode);
                    if (reconstructed != null) {
                        log.trace("Reconstructed board for forkNode: {}", reconstructed.getFen());
                        return reconstructed;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to reconstruct board for forkNode: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Находит вариант, которому принадлежит узел
     */
    private Variation findOwningVariation(ParentNode node) {
        if (node == null || node.isRoot()) {
            return null;
        }

        if (node.getOwningVariation() != null) {
            return node.getOwningVariation();
        }

        ParentNode current = node;
        while (current != null && !current.isRoot()) {
            if (current.getOwningVariation() != null) {
                return current.getOwningVariation();
            }
            current = current.getParent();
        }

        if (node.getParent() != null) {
            for (Variation var : node.getParent().getSubVariations()) {
                if (var != null && !var.isEmpty()) {
                    ParentNode firstNode = var.getFirstNode();
                    if (firstNode == node) {
                        return var;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Обрабатывает аннотацию
     */
    private void handleAnnotation(String annotationValue) {
        if (currentNode instanceof MoveNode moveNode) {
            MoveAnnotation ann = MoveAnnotation.fromSymbol(annotationValue);
            if (ann == null) {
                String trimmed = annotationValue.trim();
                ann = MoveAnnotation.fromSymbol(trimmed);
            }

            if (ann != null) {
                if (moveNode.getAnnotation() == null) {
                    moveNode.setAnnotation(ann);
                } else {
                    moveNode.addAnnotation(ann);
                }
                log.trace("Annotation {} added to move {}", ann, moveNode.getSan());
            } else {
                log.warn("Unknown annotation: {}", annotationValue);
            }
        }
    }

    /**
     * Обрабатывает комментарий
     */
    private void handleComment(String comment) {
        if (currentNode instanceof MoveNode moveNode) {
            if (comment != null && !comment.trim().isEmpty()) {
                if (moveNode.getComment() != null && !moveNode.getComment().isEmpty()) {
                    moveNode.setComment(moveNode.getComment() + " " + comment.trim());
                } else {
                    moveNode.setComment(comment.trim());
                }
                log.trace("Comment '{}' added to move {}", comment, moveNode.getSan());
            }
        }
    }

    /**
     * Синхронизирует состояние с PgnParser
     */
    public void syncWithParser(ParentNode node, Board board, Variation variation) {
        this.currentNode = node;
        if (board != null) {
            this.currentBoard = board.clone();
        }
        this.currentVariation = variation;
        log.trace("Synced: node={}, variation={}",
                node != null ? node.getSan() : "null",
                variation != null ? variation.getName() : "null");
    }

    public void clear() {
        variationStack.clear();
        variationCounter = 0;
    }

    /**
     * Информация о начале варианта
     */
    private record VariationStartInfo(ParentNode forkNode, boolean isWhiteMove, int moveNumber,
                                      boolean hasMoveNumberInfo, boolean isCompleteGame) {
    }

    /**
     * Контекст варианта для стека
     */
    public static class VariationContext {
        final Variation parentVariation;
        final ParentNode parentNode;
        final Board savedBoard;
        final ParentNode forkNode;
        final ParentNode lastMainLineNode;
        final boolean wasInMainLine;
        final boolean firstMoveIsWhite;
        final int firstMoveNumber;
        final boolean hasMoveNumberInfo;
        final boolean isCompleteGameVariation;
        final int savedPosition;

        public VariationContext(Variation parentVariation, ParentNode parentNode, Board savedBoard,
                                ParentNode forkNode, ParentNode lastMainLineNode, boolean wasInMainLine,
                                boolean firstMoveIsWhite, int firstMoveNumber, boolean hasMoveNumberInfo,
                                boolean isCompleteGameVariation, int savedPosition) {
            this.parentVariation = parentVariation;
            this.parentNode = parentNode;
            this.savedBoard = savedBoard;
            this.forkNode = forkNode;
            this.lastMainLineNode = lastMainLineNode;
            this.wasInMainLine = wasInMainLine;
            this.firstMoveIsWhite = firstMoveIsWhite;
            this.firstMoveNumber = firstMoveNumber;
            this.hasMoveNumberInfo = hasMoveNumberInfo;
            this.isCompleteGameVariation = isCompleteGameVariation;
            this.savedPosition = savedPosition;
        }
    }
}