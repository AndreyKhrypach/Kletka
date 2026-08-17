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

import Khrypach.Andrey.chess.kletka.gui.board.ChessSymbols;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.model.*;
import Khrypach.Andrey.chess.kletka.gui.visitor.VariationTreeVisitor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Визитер для генерации HTML-представления дерева вариантов
 * Новая версия: все продолжения хранятся в subVariations,
 * главная линия помечена флагом isMainLine
 */
public class HtmlTreeVisitor implements VariationTreeVisitor<String> {

    private static final Logger log = LoggerFactory.getLogger(HtmlTreeVisitor.class);

    private final LanguageManager languageManager = LanguageManager.getInstance();

    private final StringBuilder html = new StringBuilder();
    private final MoveNode activeNode;
    private final int fontSize;

    private final Stack<Integer> depthStack = new Stack<>();
    private final Stack<Boolean> isLastStack = new Stack<>();
    private int currentDepth = 0;
    private boolean currentIsLast = false;
    private boolean hasContent = false;
    private boolean isMainLine = true;

    private Variation currentVariation = null;
    private final Stack<Variation> variationStack = new Stack<>();

    @Getter
    @Setter
    private boolean firstMainMoveProcessed = false;
    private boolean hasRootVariations = false;
    @Getter
    @Setter
    private boolean isInVariation = false;
    private boolean isStartOfVariation = false;
    private boolean lastMoveWasWhite = false;
    private boolean isResumeAfterVariation = false;
    @Getter
    @Setter
    private int lastMoveNumber = 0;

    private boolean isFirstMainLineMove = true;
    @Getter
    @Setter
    private boolean isFirstRootVariation = true;
    private boolean wasResumeLine = false;
    private String gameResult;

    public HtmlTreeVisitor(MoveNode activeNode, int fontSize) {
        this.activeNode = activeNode;
        this.fontSize = fontSize;
        this.gameResult = "*";
    }

    @Override
    public void visitRootStart(RootNode rootNode) {
        log.trace("visitRootStart: subVariations size = {}",
                rootNode.getSubVariations() != null ? rootNode.getSubVariations().size() : 0);
        html.setLength(0);
        depthStack.clear();
        isLastStack.clear();
        variationStack.clear();
        currentDepth = 0;
        currentIsLast = false;
        hasContent = false;
        firstMainMoveProcessed = false;
        hasRootVariations = false;
        isInVariation = false;
        isStartOfVariation = false;
        lastMoveWasWhite = false;
        isResumeAfterVariation = false;
        lastMoveNumber = 0;
        isFirstMainLineMove = true;
        isFirstRootVariation = true;
        wasResumeLine = false;

        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <style>\n");
        html.append(getCssStyles());
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"tree-container\">\n");
    }

    @Override
    public void visitRootEnd(RootNode rootNode) {
        html.append("    </div>\n");
        html.append("    <script>\n");
        html.append(getJavaScript());
        html.append("    </script>\n");
        html.append("</body>\n");
        html.append("</html>\n");
    }

    @Override
    public void visitMainLineStart(Variation mainLine) {
        currentVariation = mainLine;
        isFirstMainLineMove = true;
        isMainLine = true;
    }

    @Override
    public void visitMainLineMove(MoveNode moveNode, int moveNumber, boolean isWhiteMove, boolean isFirstInLine) {
        log.trace("visitMainLineMove: moveNode={}, moveNumber={}, isWhiteMove={}, isFirstInLine={}, hasContent={}, isFirstMainLineMove={}",
                moveNode.getSan(), moveNumber, isWhiteMove, isFirstInLine, hasContent, isFirstMainLineMove);

        isMainLine = true;
        hasContent = true;
        isInVariation = false;
        isStartOfVariation = false;

        if (isFirstMainLineMove) {
            isFirstMainLineMove = false;
        } else {
            html.append(" ");
        }

        if (isWhiteMove) {
            html.append("<span class=\"move-number\">").append(moveNumber).append(".</span>");
            lastMoveWasWhite = true;
            lastMoveNumber = moveNumber;
            isResumeAfterVariation = false;
        } else {
            boolean needEllipsis = false;

            ParentNode parent = moveNode.getParent();
            if (parent != null && parent.isRoot() && isFirstInLine) {
                needEllipsis = true;
            } else if (isResumeAfterVariation) {
                needEllipsis = true;
                isResumeAfterVariation = false;
            }

            if (needEllipsis) {
                html.append("<span class=\"move-number\">").append(moveNumber).append("...</span>");
            }
            lastMoveWasWhite = false;
            lastMoveNumber = moveNumber;
        }

        String san = ChessSymbols.convertToChessSymbols(moveNode.getSan());
        String dataAttrs = String.format(
                "data-move-id=\"%s\" data-variation-id=\"%d\" data-move-index=\"%d\"",
                moveNode.getNodeUuid(),
                currentVariation != null ? currentVariation.getId() : -1,
                getMoveIndex(moveNode)
        );
        boolean isActive = moveNode == activeNode;
        String activeClass = isActive ? " active" : "";

        html.append("<span class=\"move").append(activeClass).append(" main-line-move").append("\" ")
                .append(dataAttrs)
                .append(" onclick=\"javaBridge.onMoveSelected('")
                .append(moveNode.getNodeUuid())
                .append("')\">")
                .append(san)
                .append("</span>");

        appendAnnotations(moveNode);
    }

    @Override
    public void visitMainLineMoveEnd(MoveNode moveNode) {
        // Ничего
    }

    @Override
    public void visitMainLineEnd(Variation mainLine) {
        if (gameResult != null && !gameResult.isEmpty()) {
            if (!gameResult.contains("*")) {
                html.append("<br>");
            } else {
                html.append(" ");
            }
            html.append("<span class=\"game-result\">")
                    .append(gameResult)
                    .append("</span>");
        }
    }

    @Override
    public void visitRootVariationsStart(RootNode rootNode, List<Variation> rootVariations) {
        hasRootVariations = !rootVariations.isEmpty();
        isFirstRootVariation = true;
        if (hasRootVariations) {
            html.append(" ");
        }
    }

    @Override
    public void visitRootVariationStart(Variation variation) {
        if (variation.isMainLine()) return;

        html.append("<br>");

        isFirstRootVariation = false;
        wasResumeLine = false;

        isInVariation = true;
        isStartOfVariation = true;
        isMainLine = false;

        variationStack.push(currentVariation);
        currentVariation = variation;

        List<Variation> siblings = getSiblings(variation);
        boolean isLast = siblings.indexOf(variation) == siblings.size() - 1;
        currentIsLast = isLast;
        isLastStack.push(isLast);

        String varName = variation.getName();
        if (varName == null || varName.isEmpty() || varName.equals("~") || varName.equals(languageManager.get(LanguageKeys.MAIN_LINE))) {
            varName = "";
        }

        String indentClass = "depth-" + currentDepth;
        String collapsedClass = "expanded";
        String toggleIcon = "▼";

        html.append("<span class=\"variation ").append(indentClass).append(" ").append(collapsedClass).append("\" data-depth=\"").append(currentDepth).append("\">");
        html.append("<span class=\"toggle-icon\" onclick=\"toggleVariation(this)\">").append(toggleIcon).append("</span>");
        if (!varName.isEmpty()) {
            html.append("<span class=\"variation-label\">").append(varName).append("</span>");
            html.append(" ");
        }
        html.append(" ");

        html.append("<span class=\"variation-preview\">");
        List<ParentNode> moves = variation.getMoves();
        if (!moves.isEmpty() && moves.get(0) instanceof MoveNode) {
            String firstSan = ChessSymbols.convertToChessSymbols(moves.get(0).getSan());
            html.append("(").append(firstSan).append(" ...)");
        }
        html.append("</span>");

        html.append("<span class=\"variation-content\" style=\"display:inline;\">");

        String symbol = currentIsLast ? "└─" : "├─";
        html.append("<span class=\"tree-symbol\">").append(symbol).append("</span>");
        html.append(" ");
    }

    @Override
    public void visitRootVariationMove(MoveNode moveNode, Variation variation,
                                       int moveNumber, boolean isWhiteMove, boolean isFirstInVariation) {
        if (variation.isMainLine()) return;
        hasContent = true;

        if (!isFirstInVariation) {
            html.append(" ");
        }

        log.trace("visitRootVariationMove: moveNumber={}, isWhiteMove={}, isFirstInVariation={}, isStartOfVariation={}, isMainLine={}",
                moveNumber, isWhiteMove, isFirstInVariation, isStartOfVariation, isMainLine);

        appendMove(moveNode, moveNumber, isWhiteMove, false);
    }

    @Override
    public void visitRootVariationEnd(Variation variation) {
        if (variation.isMainLine()) return;

        html.append("<span class=\"variation-end\">;</span>");
        html.append("</span>"); // variation-content
        html.append("</span>"); // variation

        wasResumeLine = true;
        int parentDepth = 0;
        String indentClass = "depth-" + parentDepth;
        html.append("<span class=\"resume-line ").append(indentClass).append("\">");

        isResumeAfterVariation = true;
        isInVariation = false;
        isStartOfVariation = false;
        isMainLine = true;

        if (!variationStack.isEmpty()) {
            currentVariation = variationStack.pop();
        }
    }

    @Override
    public void visitRootVariationsEnd(RootNode rootNode) {
        if (hasRootVariations) {
            isResumeAfterVariation = true;
        }
    }

    @Override
    public void visitVariationStart(Variation variation, int depth, ParentNode forkNode) {
        if (variation == null || variation.isMainLine()) return;

        if (!wasResumeLine) {
            html.append("<br>");
        }

        depthStack.push(currentDepth);
        isLastStack.push(currentIsLast);
        variationStack.push(currentVariation);

        currentDepth = depth;
        isInVariation = true;
        isStartOfVariation = true;
        isMainLine = false;

        List<Variation> siblings = getSiblings(variation);
        currentIsLast = siblings.indexOf(variation) == siblings.size() - 1;

        currentVariation = variation;

        String varName = variation.getName();
        if (varName == null || varName.isEmpty() || varName.equals("~") || varName.equals(languageManager.get(LanguageKeys.MAIN_LINE))) {
            varName = "";
        }

        String indentClass = "depth-" + currentDepth;
        String collapsedClass = "expanded";
        String toggleIcon = "▼";

        html.append("<span class=\"variation ").append(indentClass).append(" ").append(collapsedClass).append("\" data-depth=\"").append(currentDepth).append("\">");
        html.append("<span class=\"toggle-icon\" onclick=\"toggleVariation(this)\">").append(toggleIcon).append("</span>");
        if (!varName.isEmpty()) {
            html.append("<span class=\"variation-label\">").append(varName).append("</span>");
            html.append(" ");
        }
        html.append(" ");

        html.append("<span class=\"variation-preview\">");
        List<ParentNode> moves = variation.getMoves();
        if (!moves.isEmpty() && moves.get(0) instanceof MoveNode) {
            String firstSan = ChessSymbols.convertToChessSymbols(moves.get(0).getSan());
            html.append("(").append(firstSan).append(" ...)");
        }
        html.append("</span>");

        html.append("<span class=\"variation-content\" style=\"display:inline;\">");

        String symbol = currentIsLast ? "└─" : "├─";
        html.append("<span class=\"tree-symbol\">").append(symbol).append("</span>");
        html.append(" ");
    }

    @Override
    public void visitVariationMove(MoveNode moveNode, Variation variation, int depth,
                                   int moveNumber, boolean isWhiteMove, boolean isFirstInVariation) {
        if (variation.isMainLine()) return;
        hasContent = true;

        if (!isFirstInVariation) {
            html.append(" ");
        }

        if (isFirstInVariation) {
            isStartOfVariation = true;
            isResumeAfterVariation = false;
        }

        appendMove(moveNode, moveNumber, isWhiteMove, false);
    }

    @Override
    public void visitVariationEnd(Variation variation) {
        if (variation == null || variation.isMainLine()) return;

        html.append("<span class=\"variation-end\">;</span>");
        html.append("</span>"); // variation-content
        html.append("</span>"); // variation

        wasResumeLine = true;
        int parentDepth = currentDepth > 0 ? currentDepth - 1 : 0;
        String indentClass = "depth-" + parentDepth;
        html.append("<span class=\"resume-line ").append(indentClass).append("\">");

        isResumeAfterVariation = true;
        isInVariation = false;
        isStartOfVariation = false;
        isMainLine = true;

        if (!variationStack.isEmpty()) {
            currentVariation = variationStack.pop();
        }
        if (!depthStack.isEmpty()) {
            currentDepth = depthStack.pop();
            currentIsLast = isLastStack.pop();
        }
    }

    @Override
    public void visitAnnotation(MoveNode moveNode, MoveAnnotation annotation) {
        // Обрабатывается в appendMove
    }

    @Override
    public void visitComment(MoveNode moveNode, String comment) {
        // Обрабатывается в appendMove
    }

    @Override
    public String getResult() {
        return html.toString();
    }

    public void setGameResult(String result) {
        this.gameResult = result != null ? result : "*";
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private void appendMove(MoveNode moveNode, int moveNumber, boolean isWhiteMove, boolean isMainLineParam) {
        if (moveNode == null) return;

        boolean isActive = moveNode == activeNode;
        String san = ChessSymbols.convertToChessSymbols(moveNode.getSan());

        boolean isMainLineMove = isMainLineParam;

        if (!isMainLineMove && currentVariation != null && currentVariation.isMainLine()) {
            isMainLineMove = true;
        }
        if (moveNode.getOwningVariation() != null && !moveNode.getOwningVariation().isMainLine()) {
            isMainLineMove = false;
        }

        String dataAttrs = String.format(
                "data-move-id=\"%s\" data-variation-id=\"%d\" data-move-index=\"%d\"",
                moveNode.getNodeUuid(),
                currentVariation != null ? currentVariation.getId() : -1,
                getMoveIndex(moveNode)
        );

        String activeClass = isActive ? " active" : "";
        String mainClass = isMainLineMove ? " main-line-move" : " variation-move";

        if (isWhiteMove) {
            html.append("<span class=\"move-number\">").append(moveNumber).append(".</span>");
            if (isStartOfVariation) {
                isStartOfVariation = false;
            }
            if (isResumeAfterVariation) {
                isResumeAfterVariation = false;
            }
            lastMoveWasWhite = true;
        } else {
            boolean needNumber = false;

            if (moveNumber == 1 && isMainLineMove && !lastMoveWasWhite && !hasContent) {
                needNumber = true;
            } else if (isStartOfVariation && !isMainLineMove) {
                needNumber = true;
                isStartOfVariation = false;
            } else if (isResumeAfterVariation) {
                needNumber = true;
                isResumeAfterVariation = false;
            }

            if (needNumber) {
                html.append("<span class=\"move-number\">").append(moveNumber).append("...</span>");
            }
            lastMoveWasWhite = false;
        }
        lastMoveNumber = moveNumber;

        html.append("<span class=\"move").append(activeClass).append(mainClass).append("\" ")
                .append(dataAttrs)
                .append(" onclick=\"javaBridge.onMoveSelected('")
                .append(moveNode.getNodeUuid())
                .append("')\">")
                .append(san)
                .append("</span>");

        appendAnnotations(moveNode);
    }

    private void appendAnnotations(MoveNode moveNode) {
        if (moveNode.getAnnotation() != null) {
            String ann = moveNode.getAnnotation().getSymbol();
            if (ann != null && !ann.isEmpty()) {
                html.append("<span class=\"annotation\">").append(ann).append("</span>");
            }
        }
        for (MoveAnnotation ann : moveNode.getAdditionalAnnotations()) {
            String symbol = ann.getSymbol();
            if (symbol != null && !symbol.isEmpty()) {
                html.append("<span class=\"annotation\">").append(symbol).append("</span>");
            }
        }
        if (moveNode.getComment() != null && !moveNode.getComment().trim().isEmpty()) {
            html.append("<span class=\"comment\">{").append(moveNode.getComment()).append("}</span>");
        }
    }

    private int getMoveIndex(MoveNode node) {
        if (currentVariation == null) return -1;
        List<ParentNode> moves = currentVariation.getMoves();
        return moves.indexOf(node);
    }

    private List<Variation> getSiblings(Variation variation) {
        List<Variation> siblings = new ArrayList<>();
        if (variation == null) return siblings;

        ParentNode parentNode = variation.getParentNodeRef();
        if (parentNode == null) return siblings;

        for (Variation var : parentNode.getSubVariations()) {
            if (var != null && !var.isMainLine() && !var.isEmpty()) {
                siblings.add(var);
            }
        }
        return siblings;
    }

    // ========== CSS СТИЛИ ==========

    private String getCssStyles() {
        return """
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body { font-family: 'Consolas', 'Segoe UI', monospace; background-color: white; padding: 4px 8px; overflow-x: auto; }
                .tree-container { font-size: %dpx; line-height: 1.6; color: #2c3e50; display: block; max-width: 100%%; word-wrap: break-word; padding: 2px 0; }
                .main-line-move { font-family: 'Segoe UI', 'Liberation Sans', 'Arial', sans-serif; font-weight: 900; color: #1a1a1a; display: inline; white-space: normal; }
                .variation-move { font-weight: normal; color: #2c3e50; display: inline; white-space: normal; }
                .variation.depth-0 { padding-left: 0px; }
                .variation.depth-1 { padding-left: 14px; }
                .variation.depth-2 { padding-left: 12px; }
                .variation.depth-3 { padding-left: 10px; }
                .variation.depth-4 { padding-left: 8px; }
                .variation.depth-5 { padding-left: 6px; }
                .variation.depth-6 { padding-left: 4px; }
                .variation.depth-7 { padding-left: 3px; }
                .variation.depth-8 { padding-left: 2px; }
                .variation.depth-9 { padding-left: 1px; }
                .variation.depth-10 { padding-left: 0px; }
                .variation { display: inline-block; white-space: normal; word-wrap: break-word; max-width: 100%%; }
                .variation-content { display: inline; white-space: normal; word-wrap: break-word; }
                .tree-symbol { color: #8b5a2b; font-weight: bold; font-family: 'Consolas', monospace; font-size: 12px; display: inline; white-space: nowrap; }
                .move-number { color: #6b5a3b; font-weight: normal; display: inline; white-space: nowrap; }
                .move { display: inline; cursor: pointer; padding: 0 2px; border-radius: 2px; transition: background-color 0.1s ease; white-space: nowrap; }
                .move:hover { background-color: #f0e8d8; }
                .move.active { background-color: #d4c4a8; color: #5a3e1b; font-weight: bold; padding: 0 4px; }
                .variation-label { color: #8b5a2b; font-weight: bold; display: inline; margin-right: 2px; white-space: nowrap; }
                .toggle-icon { cursor: pointer; color: #8b5a2b; font-size: 11px; margin-right: 2px; user-select: none; display: inline; white-space: nowrap; }
                .toggle-icon:hover { color: #5a3e1b; font-weight: bold; }
                .variation.collapsed .variation-content { display: none !important; }
                .variation.collapsed .variation-preview { display: inline; color: #666; font-style: italic; white-space: nowrap; }
                .variation.expanded .variation-preview { display: none; }
                .variation.expanded .variation-content { display: inline !important; }
                .annotation { color: #cc3333; font-weight: bold; display: inline; white-space: nowrap; }
                .comment { color: #666666; font-style: italic; display: inline; margin-left: 2px; white-space: normal; }
                .variation-end { color: #8b5a2b; font-weight: bold; display: inline; white-space: nowrap; margin-left: 0px; }
                .game-result {\s
                                 font-weight: bold;\s
                                 color: #1a1a1a;\s
                                 display: inline-block;\s
                                 white-space: nowrap;
                                 margin-top: 4px;
                                 padding: 2px 4px;
                                 background-color: #f5f0e8;
                                 border-radius: 3px;
                             }
                .resume-line { display: block; white-space: normal; word-wrap: break-word; }
                .resume-line.depth-0 { padding-left: 0px; }
                .resume-line.depth-1 { padding-left: 14px; }
                .resume-line.depth-2 { padding-left: 12px; }
                .resume-line.depth-3 { padding-left: 10px; }
                .resume-line.depth-4 { padding-left: 8px; }
                .resume-line.depth-5 { padding-left: 6px; }
                .resume-line.depth-6 { padding-left: 4px; }
                .resume-line.depth-7 { padding-left: 3px; }
                .resume-line.depth-8 { padding-left: 2px; }
                .resume-line.depth-9 { padding-left: 1px; }
                .resume-line.depth-10 { padding-left: 0px; }
                """.formatted(fontSize);
    }

    private String getJavaScript() {
        return """
                console.log('JavaScript loaded!');
                function toggleVariation(element) {
                    var variation = element.closest('.variation');
                    if (!variation) return;
                    var content = variation.querySelector('.variation-content');
                    var preview = variation.querySelector('.variation-preview');
                    var isCollapsed = variation.classList.contains('collapsed');
                    if (isCollapsed) {
                        variation.classList.remove('collapsed');
                        variation.classList.add('expanded');
                        element.textContent = '▼';
                        if (content) content.style.display = 'inline';
                        if (preview) preview.style.display = 'none';
                    } else {
                        variation.classList.remove('expanded');
                        variation.classList.add('collapsed');
                        element.textContent = '▶';
                        if (content) content.style.display = 'none';
                        if (preview) preview.style.display = 'inline';
                    }
                }
                """;
    }
}