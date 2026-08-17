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

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.model.*;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Менеджер для управления вариантами в дереве партии.
 * Отвечает за создание, перезапись, удаление вариантов и управление главной линией.
 */
public class VariationManager {
    private static final Logger log = LoggerFactory.getLogger(VariationManager.class);
    private final LanguageManager languageManager = LanguageManager.getInstance();

    private final VariationNamingService namingService;
    @Getter
    @Setter
    private BoardReconstructor boardReconstructor;

    @Getter
    private RootNode rootNode;
    @Getter
    private Variation rootVariation;
    @Setter
    @Getter
    private Variation mainLine;

    public VariationManager(RootNode rootNode, Variation rootVariation, Variation mainLine,
                            BoardReconstructor boardReconstructor, VariationNamingService namingService) {
        this.rootNode = rootNode;
        this.rootVariation = rootVariation;
        this.mainLine = mainLine;
        this.boardReconstructor = boardReconstructor;
        this.namingService = namingService;
    }

    public void updateState(RootNode rootNode, Variation rootVariation, Variation mainLine) {
        this.rootNode = rootNode;
        this.rootVariation = rootVariation;
        this.mainLine = mainLine;
    }

    public VariationStateSnapshot addMove(Move move, Piece piece, boolean isCapture, Piece promotionPiece,
                                          Variation currentVariation, ParentNode currentNode) {
        if (move == null) {
            log.error("addMove called with null move");
            return new VariationStateSnapshot(rootNode, rootVariation, mainLine,
                    currentVariation, currentNode, false);
        }

        String currentUci = buildUci(move, promotionPiece);

        log.trace("addMove - currentUci: {}, currentVariation: {}, currentNode: {}",
                currentUci,
                currentVariation != null ? currentVariation.getName() : "null",
                currentNode != null ? (currentNode.isRoot() ? "ROOT" : currentNode.getSan()) : "null");

        Variation updatedVariation = currentVariation;
        ParentNode updatedNode = currentNode;
        Boolean result;

        if (isRootPosition(currentVariation, currentNode)) {
            for (Variation var : rootNode.getSubVariations()) {
                if (var == null || var.isEmpty()) continue;
                ParentNode firstNode = var.getFirstNode();
                if (firstNode != null && !firstNode.isRoot() && firstNode.getUciMove().equals(currentUci)) {
                    log.trace("Move already exists as root variation: {}", var.getName());
                    updatedVariation = var;
                    updatedNode = firstNode;
                    result = false;
                    return new VariationStateSnapshot(rootNode, rootVariation, mainLine,
                            updatedVariation, updatedNode, result);
                }
            }

            boolean hasAnyMoves = false;
            for (Variation var : rootNode.getSubVariations()) {
                if (var != null && !var.isEmpty()) {
                    hasAnyMoves = true;
                    break;
                }
            }

            if (!hasAnyMoves) {
                Variation newVar = addFirstMoveAsMainLineInternal(move, piece, isCapture, promotionPiece);
                updatedVariation = newVar;
                updatedNode = newVar.getFirstNode();
                result = true;
            } else {
                result = null;
            }
            return new VariationStateSnapshot(rootNode, rootVariation, mainLine,
                    updatedVariation, updatedNode, result);
        }

        assert currentVariation != null;
        if (currentVariation.isEmpty()) {
            MoveNode newNode = new MoveNode(move, piece, isCapture, promotionPiece);
            currentVariation.addMove(newNode);
            updatedNode = newNode;
            result = false;
            return new VariationStateSnapshot(rootNode, rootVariation, mainLine,
                    updatedVariation, updatedNode, result);
        }

        if (currentNode != null && currentNode.getNext() != null) {
            ParentNode nextNode = currentNode.getNext();
            if (!nextNode.isRoot() && nextNode.getUciMove().equals(currentUci)) {
                log.trace("Move already exists as next move: {}", nextNode.getSan());
                updatedNode = nextNode;
                result = false;
                return new VariationStateSnapshot(rootNode, rootVariation, mainLine,
                        updatedVariation, updatedNode, result);
            }
        }

        if (currentNode != null && !currentNode.getSubVariations().isEmpty()) {
            for (Variation subVar : currentNode.getSubVariations()) {
                if (subVar.isEmpty()) continue;
                ParentNode firstMove = subVar.getFirstNode();
                if (firstMove != null && !firstMove.isRoot() &&
                        firstMove.getUciMove().equals(currentUci)) {
                    log.trace("Move already exists as variation: {}", subVar.getName());
                    updatedVariation = subVar;
                    updatedNode = firstMove;
                    result = false;
                    return new VariationStateSnapshot(rootNode, rootVariation, mainLine,
                            updatedVariation, updatedNode, result);
                }
            }
        }

        if (currentNode != null && currentNode.getNext() == null) {
            log.trace("Simple add move (no next)");
            MoveNode newNode = new MoveNode(move, piece, isCapture, promotionPiece);
            Board boardBefore = boardReconstructor.reconstruct(currentVariation, currentNode);
            setAbsolutePlyForNode(newNode, boardBefore);

            currentNode.setNext(newNode);
            newNode.setParent(currentNode);
            newNode.setOwningVariation(currentVariation);

            updatedNode = newNode;
            result = false;

            return new VariationStateSnapshot(rootNode, rootVariation, mainLine,
                    updatedVariation, updatedNode, result);
        }

        if (currentNode != null) {
            log.trace("Case 6: needs dialog, currentNode.next exists");
            result = null;
            return new VariationStateSnapshot(rootNode, rootVariation, mainLine,
                    updatedVariation, updatedNode, result);
        }

        log.trace("Fallback: simple add move");
        MoveNode newNode = new MoveNode(move, piece, isCapture, promotionPiece);
        Board boardBefore = boardReconstructor.reconstruct(currentVariation, currentNode);
        setAbsolutePlyForNode(newNode, boardBefore);
        currentVariation.addMove(newNode);
        return new VariationStateSnapshot(rootNode, rootVariation, mainLine,
                updatedVariation, updatedNode, false);
    }

    private boolean isRootPosition(Variation currentVariation, ParentNode currentNode) {
        boolean hasAnyMoves = false;
        for (Variation var : rootNode.getSubVariations()) {
            if (var != null && !var.isEmpty()) {
                hasAnyMoves = true;
                break;
            }
        }

        if (!hasAnyMoves) {
            return true;
        }

        if (currentVariation == rootVariation || currentVariation == null) {
            return currentNode == null || currentNode.isRoot();
        }
        return false;
    }

    private Variation addFirstMoveAsMainLineInternal(Move move, Piece piece, boolean isCapture, Piece promotionPiece) {
        log.trace("addFirstMoveAsMainLineInternal called");

        MoveNode newNode = new MoveNode(move, piece, isCapture, promotionPiece);
        Board startBoard = boardReconstructor.getStartBoard();
        setAbsolutePlyForNode(newNode, startBoard);

        Variation newVariation = new Variation(languageManager.get(LanguageKeys.MAIN_LINE));
        newVariation.addMove(newNode);
        newVariation.setMainLine(true);
        newVariation.setParentVariation(rootVariation);
        newVariation.setParentNodeRef(rootNode);

        newNode.setParent(rootNode);
        newNode.setOwningVariation(newVariation);

        rootNode.getSubVariations().add(newVariation);
        log.trace("rootNode.subVariations size after add = {}", rootNode.getSubVariations().size());
        rootNode.setNext(newNode);

        this.mainLine = newVariation;
        this.mainLine.setMainLine(true);

        log.trace("First variation - set as MAIN_LINE, firstNode = {}",
                newVariation.getFirstNode() != null ? newVariation.getFirstNode().getSan() : "null");

        return newVariation;
    }

    public Variation createNewVariation(Move move, Piece piece, boolean isCapture, Piece promotionPiece,
                                        Variation currentVariation, ParentNode currentNode) {
        if (currentNode == null) {
            log.error("createNewVariation - currentNode is null!");
            return null;
        }

        log.trace("createNewVariation - currentNode: {}, currentNode.next: {}",
                currentNode.getSan(),
                currentNode.getNext() != null ? currentNode.getNext().getSan() : "null");

        Variation oldMainVariation = null;
        for (Variation var : currentNode.getSubVariations()) {
            if (var.isMainLine()) {
                oldMainVariation = var;
                log.trace("Found existing main line in subVariations: {}", var.getName());
                break;
            }
        }

        if (oldMainVariation == null && currentNode.getNext() != null) {
            ParentNode oldNext = currentNode.getNext();
            log.trace("Creating new main line from next: {}", oldNext.getSan());

            Variation correctParentVar = findOwningVariation(oldNext);
            if (correctParentVar == null) {
                correctParentVar = currentVariation;
            }

            boolean correctIsMainLine;
            Variation forkOwner = findOwningVariation(currentNode);
            if (forkOwner != null) {
                correctIsMainLine = forkOwner.isMainLine();
            } else {
                correctIsMainLine = currentVariation.isMainLine();
            }

            oldMainVariation = new Variation("~");
            oldMainVariation.setFirstNode(oldNext);
            oldMainVariation.setMainLine(correctIsMainLine);
            oldMainVariation.setParentVariation(correctParentVar);
            oldMainVariation.setParentNodeRef(currentNode);

            ParentNode current = oldNext;
            ParentNode prev = currentNode;
            while (current != null && !current.isRoot()) {
                current.setParent(prev);
                current.setForkNode(currentNode);
                current.setOwningVariation(oldMainVariation);
                prev = current;
                current = current.getNext();
            }

            currentNode.getSubVariations().add(oldMainVariation);
            log.trace("Added old main line to subVariations");
        }

        MoveNode newNode = new MoveNode(move, piece, isCapture, promotionPiece);
        Board boardBefore = boardReconstructor.reconstruct(currentVariation, currentNode);
        setAbsolutePlyForNode(newNode, boardBefore);

        Variation newVar = new Variation("");
        newVar.setNameGenerated(false);
        newVar.addMove(newNode);
        newVar.setMainLine(false);
        newVar.setParentVariation(currentVariation);
        newVar.setParentNodeRef(currentNode);

        newNode.setParent(currentNode);
        newNode.setForkNode(currentNode);
        newNode.setOwningVariation(newVar);

        currentNode.getSubVariations().add(newVar);
        log.trace("Added new variation to subVariations");

        log.trace("next remains: {}",
                currentNode.getNext() != null ? currentNode.getNext().getSan() : "null");

        if (currentVariation == this.mainLine || currentVariation.isMainLine()) {
            if (oldMainVariation != null) {
                this.mainLine = oldMainVariation;
                this.mainLine.setMainLine(true);
                log.trace("createNewVariation Global mainLine updated to: {} (id={})",
                        this.mainLine.getName(), this.mainLine.getId());
            } else {
                newVar.setMainLine(true);
                this.mainLine = newVar;
                log.trace("Global mainLine updated to new variation: {}", this.mainLine.getName());
            }
        }

        renameVariationsAtNode(currentNode, currentVariation);
        updateAllVariationNames();

        log.trace("createNewVariation END");
        return newVar;
    }

    public ParentNode overwriteInSpecificVariation(Variation targetVariation, Move move,
                                                   Piece piece, boolean isCapture,
                                                   Piece promotionPiece,
                                                   Variation currentVariation, ParentNode currentNode) {
        MoveNode newNode = new MoveNode(move, piece, isCapture, promotionPiece);

        Board boardBefore = boardReconstructor.reconstruct(currentVariation, currentNode);
        setAbsolutePlyForNode(newNode, boardBefore);

        boolean isSubVariation = false;
        if (currentNode != null) {
            for (Variation var : currentNode.getSubVariations()) {
                if (var == targetVariation) {
                    isSubVariation = true;
                    break;
                }
            }
        }

        if (currentNode != null && currentNode.isRoot()) {
            log.trace("Overwrite root variation");

            rootNode.getSubVariations().remove(targetVariation);

            Variation newRootVar = new Variation("");
            newRootVar.addMove(newNode);
            newRootVar.setMainLine(true);
            newRootVar.setParentVariation(rootVariation);
            newRootVar.setParentNodeRef(rootNode);

            newNode.setParent(rootNode);
            newNode.setForkNode(rootNode);
            newNode.setOwningVariation(newRootVar);

            rootNode.getSubVariations().add(newRootVar);

            for (Variation var : rootNode.getSubVariations()) {
                if (var != newRootVar) {
                    var.setMainLine(false);
                    log.trace("Removed isMainLine from variation: {}", var.getName());
                }
            }

            this.mainLine = newRootVar;
            rootNode.setNext(newNode);

            updateAllVariationNames();

            return newNode;
        }

        if (isSubVariation) {
            log.trace("Overwrite sub-variation at fork");

            currentNode.getSubVariations().remove(targetVariation);

            Variation newVar = new Variation("");
            newVar.addMove(newNode);
            newVar.setParentVariation(currentVariation);
            newVar.setParentNodeRef(currentNode);
            newVar.setMainLine(false);

            currentNode.getSubVariations().add(newVar);

            newNode.setParent(currentNode);
            newNode.setForkNode(currentNode);
            newNode.setOwningVariation(newVar);

            ParentNode current = newNode;
            ParentNode prev = currentNode;
            while (current != null && !current.isRoot()) {
                current.setParent(prev);
                current.setForkNode(currentNode);
                current.setOwningVariation(newVar);
                prev = current;
                current = current.getNext();
            }

            renameVariationsAtNode(currentNode, currentVariation);
            updateAllVariationNames();

            return newNode;
        }

        if (targetVariation == currentVariation && currentNode != null) {
            log.trace("Overwrite in current line (main)");

            Variation oldMainLine = null;
            for (Variation var : currentNode.getSubVariations()) {
                if (var.isMainLine()) {
                    oldMainLine = var;
                    break;
                }
            }

            if (oldMainLine != null) {
                log.trace("Removing old main line: {}", oldMainLine.getName());
                currentNode.getSubVariations().remove(oldMainLine);
                oldMainLine.setMainLine(false);
            }

            ParentNode oldNext = currentNode.getNext();

            Variation newMainLine = new Variation("~");
            newMainLine.addMove(newNode);
            newMainLine.setMainLine(true);
            newMainLine.setParentVariation(currentVariation);
            newMainLine.setParentNodeRef(currentNode);

            currentNode.getSubVariations().add(newMainLine);
            log.trace("Added new main line: {}", newMainLine.getName());

            currentNode.setNext(newNode);
            newNode.setParent(currentNode);
            newNode.setForkNode(currentNode);
            newNode.setOwningVariation(newMainLine);

            if (oldNext != null) {
                deleteSubtree(oldNext);
            }

            this.mainLine = newMainLine;
            log.trace("Global mainLine updated to: {}", this.mainLine.getName());

            if (!currentNode.getSubVariations().isEmpty()) {
                syncForkNodeAtNode(currentNode);
            }

            renameVariationsAtNode(currentNode, currentVariation);
            updateAllVariationNames();

            return newNode;
        }

        log.trace("Overwrite in another variation");
        List<ParentNode> movesToDelete = targetVariation.getMoves();
        for (ParentNode nodeToDelete : movesToDelete) {
            deleteSubtree(nodeToDelete);
        }

        targetVariation.clearMoves();
        targetVariation.addMove(newNode);

        newNode.setParent(targetVariation.getParentNodeRef());
        newNode.setForkNode(targetVariation.getParentNodeRef());
        newNode.setOwningVariation(targetVariation);

        updateAllVariationNames();

        return newNode;
    }

    public VariationStateSnapshot makeCurrentVariationMainLine(Variation selectedVariation) {
        log.trace("makeCurrentVariationMainLine - selectedVariation: {} (id={})",
                selectedVariation.getName(), selectedVariation.getId());

        if (selectedVariation == mainLine) {
            log.trace("selectedVariation is null or already mainLine, returning");
            return new VariationStateSnapshot(rootNode, rootVariation, mainLine,
                    mainLine, mainLine.getLastNode(), true);
        }

        if (selectedVariation == rootVariation) {
            log.trace("selectedVariation is rootVariation, returning");
            return new VariationStateSnapshot(rootNode, rootVariation, mainLine,
                    mainLine, mainLine.getLastNode(), true);
        }

        List<String> processedForks = new ArrayList<>();
        Variation currentVar = selectedVariation;
        Variation updatedMainLine = null;
        int iteration = 0;
        List<Variation> pathToRoot = new ArrayList<>();

        while (true) {
            iteration++;
            log.trace("ITERATION {} - currentVar: {}", iteration, getVariationPreview(currentVar));

            ParentNode firstNode = currentVar.getFirstNode();
            if (firstNode == null || firstNode.isRoot()) {
                log.error("firstNode is null or root for: {}", currentVar.getName());
                break;
            }

            log.trace("firstNode: {} (uuid={})", firstNode.getSan(),
                    firstNode.getNodeUuid().substring(0, 8));

            ParentNode forkNode = firstNode.getParent();
            if (forkNode == null) {
                log.error("forkNode is null for: {}", currentVar.getName());
                break;
            }

            String forkNodeDesc = forkNode.isRoot() ? "ROOT" : forkNode.getSan();
            log.trace("forkNode: {} (uuid={})", forkNodeDesc,
                    forkNode.getNodeUuid().substring(0, 8));

            if (forkNode.isRoot()) {
                log.trace("forkNode is ROOT - handling root variation");

                Variation oldMainLineAtFork = forkNode.getMainLineVariation();
                if (oldMainLineAtFork == null) {
                    oldMainLineAtFork = forkNode.getMainLineByName();
                }

                if (oldMainLineAtFork != null && oldMainLineAtFork != currentVar) {
                    log.trace("Root Old main line at fork: {}", getVariationPreview(oldMainLineAtFork));
                    oldMainLineAtFork.setMainLine(false);

                    int siblingIndex = 1;
                    for (Variation sibling : forkNode.getSubVariations()) {
                        if (sibling == oldMainLineAtFork) break;
                        if (!sibling.isMainLine()) siblingIndex++;
                    }

                    String newName = namingService.generateUniqueName(
                            rootVariation,
                            siblingIndex
                    );
                    oldMainLineAtFork.setName(newName);
                    log.trace(" Root Renamed old main line to: {}", newName);

                    ParentNode oldFirstNode = oldMainLineAtFork.getFirstNode();
                    if (oldFirstNode != null && !oldFirstNode.isRoot()) {
                        setForkNodeForTree(oldFirstNode, forkNode, oldMainLineAtFork);
                    }
                }

                for (Variation sibling : forkNode.getSubVariations()) {
                    if (sibling != null && sibling != currentVar) {
                        removeAllMainLineFlagsRecursively(sibling);
                        log.trace("Removed isMainLine from sibling: {}", getVariationPreview(sibling));
                    }
                }

                currentVar.setMainLine(true);
                currentVar.setName("~");
                log.trace("Root Set isMainLine=true for: {}", getVariationPreview(currentVar));

                currentVar.setParentVariation(rootVariation);
                currentVar.setParentNodeRef(forkNode);
                log.trace("Set parentVariation to rootVariation");

                forkNode.setNext(firstNode);
                firstNode.setParent(forkNode);
                log.trace("Updated next of ROOT to: {}", firstNode.getSan());

                ParentNode current = firstNode;
                ParentNode prev = forkNode;
                while (current != null && !current.isRoot()) {
                    current.setParent(prev);
                    if (current != forkNode) {
                        current.setOwningVariation(currentVar);
                        current.setForkNode(forkNode);
                    }
                    prev = current;
                    current = current.getNext();
                }
                log.trace("Root Updated owningVariation for all nodes in new main line");

                updatedMainLine = currentVar;
                processedForks.add("ROOT → " + firstNode.getSan());

                log.trace("Root fork processed → STOPPING");
                break;
            }

            Variation forkOwner = findOwningVariation(forkNode);
            boolean wasMainLine = false;

            if (forkOwner != null) {
                wasMainLine = forkOwner.isMainLine();
                log.trace("forkOwner: {}", getVariationPreview(forkOwner));
                log.trace("forkOwner.isMainLine() = {}", wasMainLine);
            } else {
                log.trace("forkOwner: null (forkNode does not belong to any variation)");
            }

            Variation oldMainLineAtFork = forkNode.getMainLineVariation();
            if (oldMainLineAtFork == null) {
                oldMainLineAtFork = forkNode.getMainLineByName();
            }

            if (oldMainLineAtFork != null && oldMainLineAtFork != currentVar) {
                log.trace("Old main line at fork: {}", getVariationPreview(oldMainLineAtFork));
                oldMainLineAtFork.setMainLine(false);
                log.trace("Removed isMainLine from old main line");

                int siblingIndex = 1;
                for (Variation sibling : forkNode.getSubVariations()) {
                    if (sibling == oldMainLineAtFork) break;
                    if (!sibling.isMainLine()) siblingIndex++;
                }

                Variation parentVariation = currentVar.getParentVariation();
                String newName = namingService.generateUniqueName(
                        parentVariation,
                        siblingIndex
                );
                oldMainLineAtFork.setName(newName);
                log.trace("Renamed old main line to: {}", newName);

                ParentNode oldFirstNode = oldMainLineAtFork.getFirstNode();
                if (oldFirstNode != null && !oldFirstNode.isRoot()) {
                    setForkNodeForTree(oldFirstNode, forkNode, oldMainLineAtFork);
                }
            }

            log.trace("Setting new main line: {}", getVariationPreview(currentVar));

            Variation correctParentVar = findOwningVariation(forkNode);
            if (correctParentVar != null && correctParentVar != currentVar.getParentVariation()) {
                log.trace("Updating parentVariation from {} to {}",
                        currentVar.getParentVariation() != null ? currentVar.getParentVariation().getName() : "null",
                        correctParentVar.getName());
                currentVar.setParentVariation(correctParentVar);
                currentVar.setParentNodeRef(forkNode);
            }

            for (Variation sibling : forkNode.getSubVariations()) {
                if (sibling != null && sibling != currentVar) {
                    removeAllMainLineFlagsRecursively(sibling);
                }
            }

            currentVar.setMainLine(true);
            currentVar.setName("~");
            log.trace("Set isMainLine=true for: {}", getVariationPreview(currentVar));

            if (!firstNode.isRoot()) {
                forkNode.setNext(firstNode);
                firstNode.setParent(forkNode);
                log.trace("Updated next of {} to: {}", forkNodeDesc, firstNode.getSan());
            }

            ParentNode current = firstNode;
            ParentNode prev = forkNode;
            while (current != null && !current.isRoot()) {
                current.setParent(prev);
                if (current != forkNode) {
                    current.setOwningVariation(currentVar);
                    current.setForkNode(forkNode);
                }
                prev = current;
                current = current.getNext();
            }
            log.trace("Updated owningVariation for all nodes in new main line");

            updatedMainLine = currentVar;
            pathToRoot.add(currentVar);

            String forkInfo = forkNodeDesc + " → " + firstNode.getSan();
            processedForks.add(forkInfo);
            log.trace("✅ Processed fork: {}", forkInfo);

            log.trace("Decision: wasMainLine = {}", wasMainLine);

            if (wasMainLine) {
                log.trace("forkOwner was already mainLine → STOPPING");
                break;
            }

            if (isNodeInGlobalMainLine(forkNode)) {
                log.trace("forkNode belongs to GLOBAL main line → STOPPING");
                break;
            }

            Variation parentVar = currentVar.getParentVariation();
            if (parentVar == null || parentVar == rootVariation) {
                log.trace("Reached root or parent is null → STOPPING");
                break;
            }

            log.trace("Moving to parent variation: {}", getVariationPreview(parentVar));
            currentVar = parentVar;
        }

        if (updatedMainLine != null) {
            boolean isRootMainLine = false;
            ParentNode parentNodeRef = updatedMainLine.getParentNodeRef();
            if (parentNodeRef != null && parentNodeRef.isRoot()) {
                isRootMainLine = true;
            }

            if (!isRootMainLine) {
                log.trace("New main line is NESTED (not root)");

                Variation rootPathVariation = findRootPathVariation(updatedMainLine);

                if (rootPathVariation != null) {
                    log.trace("Root path variation: {}", getVariationPreview(rootPathVariation));

                    if (!rootPathVariation.isMainLine()) {
                        rootPathVariation.setMainLine(true);
                        log.trace("Set isMainLine=true on root path variation: {}", rootPathVariation.getName());
                    }

                    if (!rootNode.getSubVariations().contains(rootPathVariation)) {
                        rootNode.getSubVariations().add(rootPathVariation);
                        log.trace("Added root path variation back to root.subVariations");
                    }
                } else {
                    log.warn("Could not find root path variation!");
                }
            }

            if (this.mainLine != null && this.mainLine != updatedMainLine) {
                boolean isRootPath = false;
                if (!isRootMainLine) {
                    Variation rootPathVar = findRootPathVariation(updatedMainLine);
                    if (rootPathVar != null && rootPathVar == this.mainLine) {
                        isRootPath = true;
                        log.trace("Old main line is root path - keeping isMainLine=true");
                    }
                }

                if (!isRootPath) {
                    this.mainLine.setMainLine(false);
                    log.trace("Removed isMainLine from old global mainLine: {}", this.mainLine.getName());
                }
            }

            this.mainLine = updatedMainLine;
            this.mainLine.setMainLine(true);
            this.mainLine.setName(languageManager.get(LanguageKeys.MAIN_LINE));

            log.trace("makeCurrentVariationMainLine Global mainLine updated to: {} (id={})",
                    this.mainLine.getName(), this.mainLine.getId());

            syncAllForkNodesInMainLine();
        }

        log.trace("Processed {} fork(s): {}", processedForks.size(), processedForks);

        updateAllVariationNames();

        log.trace("makeCurrentVariationMainLine COMPLETED");

        return new VariationStateSnapshot(rootNode, rootVariation, this.mainLine,
                this.mainLine, this.mainLine.getLastNode(), true);
    }

    private Variation findRootPathVariation(Variation variation) {
        if (variation == null) return null;

        Variation current = variation;
        while (current != null) {
            ParentNode parentNodeRef = current.getParentNodeRef();
            if (parentNodeRef != null && parentNodeRef.isRoot()) {
                return current;
            }
            current = current.getParentVariation();
        }

        return null;
    }

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
            if (current.getParent() != null) {
                for (Variation var : current.getParent().getSubVariations()) {
                    if (var != null && !var.isEmpty()) {
                        ParentNode firstNode = var.getFirstNode();
                        if (firstNode == current) {
                            return var;
                        }
                    }
                }
            }
            current = current.getParent();
        }

        if (mainLine != null) {
            for (ParentNode move : mainLine.getMoves()) {
                if (move == node) {
                    return mainLine;
                }
            }
        }

        return null;
    }

    private boolean isNodeInGlobalMainLine(ParentNode node) {
        if (node == null || node.isRoot() || mainLine == null) {
            return false;
        }

        Variation owner = findOwningVariation(node);
        if (owner != null && owner == mainLine) {
            log.trace("isNodeInGlobalMainLine: {} → true (owner == mainLine)", node.getSan());
            return true;
        }

        for (ParentNode move : mainLine.getMoves()) {
            if (move == node) {
                log.trace("isNodeInGlobalMainLine: {} → true (found in mainLine.getMoves())", node.getSan());
                return true;
            }
        }

        ParentNode current = rootNode.getNext();
        while (current != null && !current.isRoot()) {
            if (current == node) {
                log.trace("isNodeInGlobalMainLine: {} → true (found in root.next chain)", node.getSan());
                return true;
            }
            current = current.getNext();
        }

        log.trace("isNodeInGlobalMainLine: {} → false", node.getSan());
        return false;
    }

    private void syncAllForkNodesInMainLine() {
        log.trace("Starting syncAllForkNodesInMainLine");

        if (rootNode == null) {
            log.trace("rootNode is null");
            return;
        }

        ParentNode current = rootNode.getNext();
        if (current == null || current.isRoot()) {
            log.trace("No moves in main line");
            return;
        }

        log.trace("Traversing main line via next chain");

        while (current != null && !current.isRoot()) {
            if (current.getSubVariations() != null && !current.getSubVariations().isEmpty()) {
                log.trace("Fork node: {}, subVariations size: {}",
                        current.getSan(), current.getSubVariations().size());

                if (current.getNext() != null && !current.getNext().isRoot()) {
                    String nextUci = current.getNext().getUciMove();
                    log.trace("next: {}", current.getNext().getSan());

                    Variation mainLineVariation = null;
                    for (Variation var : current.getSubVariations()) {
                        if (var == null || var.isEmpty()) continue;
                        ParentNode firstNode = var.getFirstNode();
                        if (firstNode == null || firstNode.isRoot()) continue;

                        if (firstNode.getUciMove().equals(nextUci)) {
                            mainLineVariation = var;
                            log.trace("Found matching variation: {} (firstMove={})",
                                    var.getName(), firstNode.getSan());
                            break;
                        }
                    }

                    if (mainLineVariation != null) {
                        if (!mainLineVariation.isMainLine()) {
                            mainLineVariation.setMainLine(true);
                            log.trace("Set isMainLine=true for {}", mainLineVariation.getName());
                        }

                        for (Variation var : current.getSubVariations()) {
                            if (var != null && var != mainLineVariation && var.isMainLine()) {
                                var.setMainLine(false);
                                log.trace("Set isMainLine=false for {}", var.getName());
                            }
                        }
                    } else {
                        log.warn("No variation found for next: {}", current.getNext().getSan());
                    }
                } else {
                    log.trace("No next (end of main line)");
                    for (Variation var : current.getSubVariations()) {
                        if (var != null && var.isMainLine()) {
                            var.setMainLine(false);
                            log.trace("Set isMainLine=false for {} (end of line)", var.getName());
                        }
                    }
                }
            }

            if (current.getNext() != null && !current.getNext().isRoot()) {
                current = current.getNext();
            } else {
                break;
            }
        }

        log.trace("syncAllForkNodesInMainLine completed");
    }

    private void removeAllMainLineFlagsRecursively(Variation variation) {
        if (variation == null) return;

        variation.setMainLine(false);

        for (ParentNode node : variation.getMoves()) {
            for (Variation subVar : node.getSubVariations()) {
                removeAllMainLineFlagsRecursively(subVar);
            }
        }
    }

    public VariationStateSnapshot deleteCurrentVariation(Variation variationToDelete) {
        log.trace("deleteCurrentVariation - deleting: {}",
                variationToDelete != null ? variationToDelete.getName() : "null");

        if (variationToDelete == mainLine || variationToDelete == rootVariation) {
            log.warn("Cannot delete main line or root variation");
            return new VariationStateSnapshot(rootNode, rootVariation, mainLine,
                    mainLine, mainLine.getLastNode(), false);
        }

        assert variationToDelete != null;
        ParentNode parentNode = variationToDelete.getParentNodeRef();
        Variation parentVariation = variationToDelete.getParentVariation();

        if (parentNode == null || parentVariation == null) {
            log.error("Cannot delete - parentNodeRef or parentVariation is null");
            return new VariationStateSnapshot(rootNode, rootVariation, mainLine,
                    mainLine, mainLine.getLastNode(), false);
        }

        log.trace("Deleting variation: {}", variationToDelete.getName());

        boolean removed = parentNode.getSubVariations().remove(variationToDelete);
        if (removed) {
            deleteVariationTree(variationToDelete);
            renameVariationsAtNode(parentNode, parentVariation);
            updateAllVariationNames();
        }
        return new VariationStateSnapshot(rootNode, rootVariation, mainLine,
                mainLine, mainLine.getLastNode(), true);
    }

    private void syncForkNodeAtNode(ParentNode forkNode) {
        if (forkNode == null) return;

        if (forkNode.getNext() != null && !forkNode.getNext().isRoot()) {
            setForkNodeForTree(forkNode.getNext(), forkNode, null);
        }

        for (Variation subVar : forkNode.getSubVariations()) {
            ParentNode firstNode = subVar.getFirstNode();
            if (firstNode != null && !firstNode.isRoot()) {
                setForkNodeForTree(firstNode, forkNode, subVar);
            }
        }
    }

    public void setForkNodeForTree(ParentNode startNode, ParentNode forkNode, Variation owningVariation) {
        if (startNode == null || startNode.isRoot()) return;

        log.trace("setForkNodeForTree - startNode: {}, forkNode: {}, owningVariation: {}",
                startNode.getSan(),
                forkNode.isRoot() ? "ROOT" : forkNode.getSan(),
                owningVariation == null ? "null" : owningVariation.getName());

        ParentNode current = startNode;
        ParentNode prev = forkNode;

        while (current != null && !current.isRoot()) {
            log.trace("setting parent for: {} -> {}",
                    current.getSan(),
                    prev.isRoot() ? "ROOT" : prev.getSan());

            current.setForkNode(forkNode);
            current.setOwningVariation(owningVariation);

            if (current.getParent() == null || current.getParent() != prev) {
                current.setParent(prev);
            }

            prev = current;
            current = current.getNext();
        }

        current = startNode;
        while (current != null && !current.isRoot()) {
            for (Variation subVar : current.getSubVariations()) {
                ParentNode firstNode = subVar.getFirstNode();
                if (firstNode != null && !firstNode.isRoot()) {
                    setForkNodeForTree(firstNode, current, subVar);
                }
            }
            current = current.getNext();
        }
    }

    private void deleteSubtree(ParentNode startNode) {
        if (startNode == null) return;

        List<ParentNode> nodesToDelete = new ArrayList<>();
        ParentNode current = startNode;
        while (current != null && !current.isRoot()) {
            nodesToDelete.add(current);
            current = current.getNext();
        }

        for (ParentNode node : nodesToDelete) {
            List<Variation> subVarsToDelete = new ArrayList<>(node.getSubVariations());
            for (Variation subVar : subVarsToDelete) {
                deleteVariationTree(subVar);
            }
            node.getSubVariations().clear();

            node.setForkNode(null);
            node.setOwningVariation(null);
            node.setParent(null);
            node.setNext(null);
            node.setComment(null);
            node.setAnnotation(null);
            node.getAdditionalAnnotations().clear();
        }

        if (startNode.getParent() != null) {
            startNode.getParent().setNext(null);
        }
    }

    private void deleteVariationTree(Variation variation) {
        if (variation == null) return;

        List<ParentNode> nodes = variation.getMoves();
        for (ParentNode node : nodes) {
            List<Variation> subVars = new ArrayList<>(node.getSubVariations());
            for (Variation subVar : subVars) {
                deleteVariationTree(subVar);
            }
            node.getSubVariations().clear();

            node.setParent(null);
            node.setNext(null);
            node.setForkNode(null);
            node.setOwningVariation(null);
            node.setComment(null);
            node.setAnnotation(null);
            node.getAdditionalAnnotations().clear();
        }

        variation.clearMoves();
        variation.setParentVariation(null);
        variation.setParentNodeRef(null);
    }

    private void renameVariationsAtNode(ParentNode node, Variation parentVariation) {
        if (node == null || parentVariation == null) return;

        // ========== ПРОСТО ПРОХОДИМ ПО СПИСКУ И НУМЕРУЕМ ==========
        int index = 1;
        for (Variation subVar : node.getSubVariations()) {
            if (subVar == null) continue;

            if (subVar.isMainLine()) {
                subVar.setName("~");
                subVar.setNameGenerated(true);
                continue;
            }

            // Проверяем, есть ли уже имя (из PGN)
            String currentName = subVar.getName();
            boolean hasCustomName = currentName != null &&
                    !currentName.isEmpty() &&
                    !currentName.equals(languageManager.get(LanguageKeys.VARIATION_DEFAULT_NAME)) &&
                    !currentName.equals(languageManager.get(LanguageKeys.ROOT)) &&
                    subVar.isNameGenerated();

            if (hasCustomName) {
                // Имя уже есть, сохраняем
                log.debug("Preserving nested variation name: {}", currentName);
                continue;
            }

            // ========== ГЕНЕРИРУЕМ ИМЯ С ТЕКУЩИМ ИНДЕКСОМ ==========
            String newName = namingService.generateUniqueName(parentVariation, index);
            subVar.setName(newName);
            subVar.setNameGenerated(true);

            index++;
        }
    }

    public void updateAllVariationNames() {
        if (rootVariation != null && rootVariation.getFirstNode() != null &&
                rootVariation.getFirstNode().isRoot()) {
            namingService.updateAllVariationNames(rootVariation);
        }
    }

    private void setAbsolutePlyForNode(ParentNode node, Board boardBeforeMove) {
        if (node == null || node.isRoot()) return;

        int fullMoves = boardBeforeMove.getMoveCounter();
        int absolutePly = getAbsolutePly(node, boardBeforeMove, fullMoves);

        node.setAbsolutePly(absolutePly);
        node.setSavedFenBefore(boardBeforeMove.getFen());

        if (node instanceof MoveNode moveNode) {
            try {
                Board afterBoard = boardBeforeMove.clone();
                afterBoard.doMove(moveNode.getMove());
                node.setSavedFenAfter(afterBoard.getFen());
            } catch (Exception e) {
                log.trace("Error applying move for FEN: {}", e.getMessage());
                node.setSavedFenAfter(boardBeforeMove.getFen());
            }
        }
    }

    private static int getAbsolutePly(ParentNode node, Board boardBeforeMove, int fullMoves) {
        boolean isWhiteToMove = boardBeforeMove.getSideToMove() == com.github.bhlangonijr.chesslib.Side.WHITE;

        int absolutePly;
        if (isWhiteToMove) {
            absolutePly = (fullMoves - 1) * 2 + 1;
        } else {
            absolutePly = (fullMoves - 1) * 2 + 2;
        }

        if (node instanceof MoveNode moveNode) {
            Piece movingPiece = moveNode.getPiece();
            boolean isPieceWhite = movingPiece.getPieceSide() == com.github.bhlangonijr.chesslib.Side.WHITE;

            if ((isPieceWhite && !isWhiteToMove) || (!isPieceWhite && isWhiteToMove)) {
                if (isPieceWhite) {
                    absolutePly = (fullMoves - 1) * 2 + 1;
                } else {
                    absolutePly = (fullMoves - 1) * 2 + 2;
                }
            }
        }

        if (absolutePly < 1) {
            absolutePly = 1;
        }
        return absolutePly;
    }

    private String buildUci(Move move, Piece promotionPiece) {
        String uci = move.getFrom().toString().toLowerCase() +
                move.getTo().toString().toLowerCase();
        if (promotionPiece != null) {
            uci += getPromotionChar(promotionPiece);
        }
        return uci;
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

    private String getVariationPreview(Variation var) {
        if (var == null) return "null";
        if (var.isEmpty()) return var.getName() + " (empty)";

        List<ParentNode> moves = var.getMoves();
        StringBuilder sb = new StringBuilder();
        sb.append(var.getName()).append(" (id=").append(var.getId()).append(")");
        sb.append(" [");
        for (int i = 0; i < Math.min(3, moves.size()); i++) {
            if (i > 0) sb.append(" ");
            sb.append(moves.get(i).getSan());
        }
        if (moves.size() > 3) sb.append(" ...");
        sb.append("]");
        sb.append(" isMainLine=").append(var.isMainLine());
        return sb.toString();
    }

}