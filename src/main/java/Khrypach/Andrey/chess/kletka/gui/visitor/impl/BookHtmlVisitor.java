/*
 * Copyright (c) 2025-2026 Andrey Khrypach
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package Khrypach.Andrey.chess.kletka.gui.visitor.impl;

import Khrypach.Andrey.chess.kletka.gui.board.ChessSymbols;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.model.*;
import Khrypach.Andrey.chess.kletka.gui.visitor.LevelAwareVisitor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * HTML визитер для дебютных книг (BOOK режим)
 * Отображает все варианты как плоский список на каждом уровне
 */
public class BookHtmlVisitor implements LevelAwareVisitor<String> {

    private static final Logger log = LoggerFactory.getLogger(BookHtmlVisitor.class);
    private final LanguageManager languageManager = LanguageManager.getInstance();

    private final StringBuilder html = new StringBuilder();
    private final int fontSize;
    private int totalEntries = 0;

    // Текущий уровень
    @Getter
    @Setter
    private int currentLevel = -1;
    // Стек уровней для навигации
    private final List<Integer> levelStack = new ArrayList<>();
    @Getter
    @Setter
    private int selectedIndex = 0;

    @Getter
    @Setter
    private boolean showStats = true;

    public BookHtmlVisitor(int fontSize) {
        this.fontSize = fontSize;
    }

    @Override
    public void visitRootStart(RootNode rootNode) {
        log.trace("visitRootStart");
        html.setLength(0);
        currentLevel = -1;
        levelStack.clear();

        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<style>\n");
        html.append(getCssStyles());
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("<input type=\"text\" id=\"hidden-focus\" style=\"position: absolute; opacity: 0; width: 0; height: 0; pointer-events: none;\">\n");
        html.append("<div class=\"book-container\">\n");
    }

    @Override
    public void visitRootEnd(RootNode rootNode) {
        html.append("</div>\n");
        html.append("<script>\n");
        html.append(getJavaScript());
        html.append("</script>\n");
        html.append("</body>\n");
        html.append("</html>\n");
    }

    @Override
    public void visitMainLineStart(Variation mainLine) {
        // В BOOK режиме не используем mainLine отдельно
        // Все варианты отображаются на уровне
    }

    @Override
    public void visitMainLineMove(MoveNode moveNode, int moveNumber,
                                  boolean isWhiteMove, boolean isFirstInLine) {
        // Не используется в BOOK режиме
    }

    @Override
    public void visitMainLineMoveEnd(MoveNode moveNode) {
        // Не используется в BOOK режиме
    }

    @Override
    public void visitMainLineEnd(Variation mainLine) {
        // Не используется в BOOK режиме
    }

    @Override
    public void visitVariationStart(Variation variation, int depth, ParentNode forkNode) {

    }

    @Override
    public void visitVariationMove(MoveNode moveNode, Variation variation, int depth, int moveNumber, boolean isWhiteMove, boolean isFirstInVariation) {

    }

    @Override
    public void visitVariationEnd(Variation variation) {

    }

    @Override
    public void visitAnnotation(MoveNode moveNode, MoveAnnotation annotation) {
        // Обрабатывается в visitLevelMove
    }

    @Override
    public void visitComment(MoveNode moveNode, String comment) {
        // Обрабатывается в visitLevelMove
    }

    @Override
    public String getResult() {
        return html.toString();
    }

    // ========== МЕТОДЫ LevelAwareVisitor ==========

    @Override
    public void visitLevelStart(int level, ParentNode node, Variation variation) {
        log.trace("visitLevelStart - level: {}, node: {}", level,
                node.isRoot() ? "ROOT" : node.getSan());

        currentLevel = level;
        levelStack.add(level);

        //html.append("<div class=\"level level-").append(level).append("\">\n");
        html.append("<div class=\"level\">\n");

        // Добавляем информацию о позиции
        String positionInfo = getPositionInfo(node);
        if (!positionInfo.isEmpty()) {
            html.append("<div class=\"position-info\">")
                    .append(positionInfo)
                    .append("</div>\n");
        }

        html.append("<div class=\"variations-list\">\n");
    }

    @Override
    public void visitLevelVariation(Variation variation, int level, int index,
                                    boolean isActive, boolean isMainLine) {
        log.trace("visitLevelVariation - level: {}, index: {}, name: {}, isMainLine: {}",
                level, index, variation.getName(), isMainLine);

        if (variation.isEmpty()) {
            return;
        }

        isActive = (index == selectedIndex);

        // Получаем ВСЕ ходы варианта
        List<ParentNode> moves = variation.getMoves();
        if (moves.isEmpty()) {
            return;
        }

        // Получаем ТОЛЬКО ПЕРВЫЙ ход варианта
        ParentNode firstNode = variation.getFirstNode();
        if (firstNode == null || firstNode.isRoot()) {
            return;
        }

        int moveNumber = getMoveNumber(firstNode);
        String numberDisplay = moveNumber > 0 ? moveNumber + "." : "";

        String moveSan = ChessSymbols.convertToChessSymbols(firstNode.getSan());

        // Извлекаем статистику из первого хода
        String stats = "";
        if (showStats && firstNode instanceof MoveNode moveNode) {
            stats = extractStats(moveNode);
        }

        // Классы для элемента
        String activeClass = isActive ? " active" : "";
        String mainClass = isMainLine ? " main-line" : "";

        // data атрибуты
        String variationId = String.valueOf(variation.getId());
        String nodeUuid = firstNode.getNodeUuid();

        html.append("<div class=\"variation-item").append(activeClass).append(mainClass).append("\"")
                .append(" data-variation-id=\"").append(variationId).append("\"")
                .append(" data-node-uuid=\"").append(nodeUuid).append("\"")
                .append(" data-level=\"").append(level).append("\"")
                .append(" data-index=\"").append(index).append("\"")
                .append(" onclick=\"javaBridge.onMoveSelected('").append(nodeUuid).append("')\"")
                .append(">\n");

        // Номер хода
        if (!numberDisplay.isEmpty()) {
            html.append("<span class=\"move-number\">")
                    .append(numberDisplay)
                    .append("</span>\n");
        }

        // Только ход (без имени варианта)
        html.append("<span class=\"variation-move\">")
                .append(moveSan)
                .append("</span>\n");

        // Статистика
        if (!stats.isEmpty()) {
            html.append("<span class=\"variation-stats\">")
                    .append(stats)
                    .append("</span>\n");
        }

        // Индикатор главной линии
        if (isMainLine) {
            html.append("<span class=\"main-line-badge\">★</span>");
        }

        html.append("</div>\n");

        html.append("<script>\n");
        html.append("  (function() {\n");
        html.append("    const items = document.querySelectorAll('.variation-item');\n");
        html.append("    const activeIndex = ").append(selectedIndex).append(";\n");
        html.append("    items.forEach((el, i) => {\n");
        html.append("      el.classList.toggle('active', i === activeIndex);\n");
        html.append("      if (i === activeIndex) {\n");
        html.append("        el.scrollIntoView({ block: 'center', behavior: 'smooth' });\n");
        html.append("      }\n");
        html.append("    });\n");
        html.append("  })();\n");
        html.append("</script>\n");
    }

    @Override
    public void visitLevelMove(MoveNode moveNode, Variation variation,
                               int level, int moveIndex, boolean isActive) {
        log.trace("visitLevelMove - level: {}, moveIndex: {}, san: {}, isActive: {}",
                level, moveIndex, moveNode.getSan(), isActive);

        // Если это не первый ход в варианте, показываем его как продолжение
        if (moveIndex > 0) {
            String san = ChessSymbols.convertToChessSymbols(moveNode.getSan());
            String activeClass = isActive ? " active" : "";
            String nodeUuid = moveNode.getNodeUuid();

            html.append("<span class=\"continuation-move").append(activeClass).append("\"")
                    .append(" data-node-uuid=\"").append(nodeUuid).append("\"")
                    .append(" onclick=\"javaBridge.onMoveSelected('").append(nodeUuid).append("')\"")
                    .append(">")
                    .append(san)
                    .append("</span>\n");
        }
    }

    @Override
    public void visitLevelEnd(int level) {
        log.trace("visitLevelEnd - level: {}", level);

        html.append("</div>\n"); // variations-list
        html.append("</div>\n"); // level

        if (!levelStack.isEmpty()) {
            levelStack.remove(levelStack.size() - 1);
        }
    }

    @Override
    public void visitBookInfo(String bookName, int totalEntries, String currentPosition) {
        this.totalEntries = totalEntries;

        html.append("<div class=\"book-info\">\n");
        html.append("<span class=\"book-name\">📖 ").append(escapeHtml(bookName)).append("</span>\n");
        html.append("<span class=\"book-entries\">").append(totalEntries).append(" ")
                .append(languageManager.get(LanguageKeys.BOOK_ENTRIES)).append("</span>\n");
        if (currentPosition != null && !currentPosition.isEmpty()) {
            html.append("<span class=\"book-position\">").append(currentPosition).append("</span>\n");
        }
        html.append("</div>\n");
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    /**
     * Извлекает статистику из комментария узла
     */
    private String extractStats(MoveNode moveNode) {
        String comment = moveNode.getComment();
        if (comment == null || comment.isEmpty()) {
            return "";
        }

        try {
            String[] parts = comment.split(",");
            StringBuilder stats = new StringBuilder();

            for (String part : parts) {
                if (part.startsWith("weight:")) {
                    int weight = Integer.parseInt(part.substring(7));
                    double percentage = (weight / 32767.0) * 100.0;
                    stats.append(String.format("  %.1f%%", percentage));
                } else if (part.startsWith("games:")) {
                    int games = Integer.parseInt(part.substring(6));
                    stats.append(String.format("  (%d)", games));
                } else if (part.startsWith("rating:")) {
                    int rating = Integer.parseInt(part.substring(7));
                    stats.append(String.format("  %d", rating));
                }
            }

            return stats.toString();
        } catch (Exception e) {
            log.debug("Failed to parse stats: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Получает информацию о позиции
     */
    private String getPositionInfo(ParentNode node) {
        if (node == null) {
            return "";
        }

        StringBuilder info = new StringBuilder();

        if (node instanceof MoveNode moveNode) {
            String moveNumber = String.valueOf((moveNode.getAbsolutePly() + 1) / 2);
            boolean isWhiteMove = (moveNode.getAbsolutePly() % 2 == 1);
            info.append(languageManager.get(LanguageKeys.NAVIGATION_MODE))
                    .append(": ")
                    .append(moveNumber)
                    .append(isWhiteMove ? "." : "...");
        } else if (node.isRoot()) {
            info.append(languageManager.get(LanguageKeys.NAVIGATION_MODE))
                    .append(": ")
                    .append(languageManager.get(LanguageKeys.ROOT));
        }

        // ===== ДОБАВЛЯЕМ КОЛИЧЕСТВО ЗАПИСЕЙ =====
        if (totalEntries > 0) {
            info.append("  |  ")
                    .append(totalEntries)
                    .append(" ")
                    .append(languageManager.get(LanguageKeys.BOOK_ENTRIES));
        }

        return info.toString();
    }

    /**
     * Экранирует HTML специальные символы
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#039;");
    }

    // ========== CSS СТИЛИ ==========

    private String getCssStyles() {
        // Вычисляем производные размеры
        int moveFontSize = fontSize;
        int statsFontSize = Math.max(10, fontSize - 2);
        int numberFontSize = Math.max(10, fontSize - 2);
        int infoFontSize = Math.max(10, fontSize - 1);

        return """
                * { margin: 0; padding: 0; box-sizing: border-box; }
                 body {
                     font-family: 'Segoe UI', 'Consolas', monospace;
                     background-color: #f8f5f0;
                     padding: 0;
                     margin: 0;
                 }
                 .book-container {
                     font-size: %dpx;
                     line-height: 1.8;
                     max-width: 100%%;
                     padding: 0;
                     margin: 0;
                 }
                 .book-info {
                     background-color: #f0e8d8;
                     padding: 8px 12px;
                     border-radius: 5px;
                     margin-bottom: 12px;
                     border-left: 4px solid #8b5a2b;
                     display: flex;
                     flex-wrap: wrap;
                     gap: 10px;
                     align-items: center;
                     font-size: %dpx;
                 }
                 .book-name {
                     font-weight: bold;
                     color: #5a3e1b;
                 }
                 .book-entries {
                     color: #666;
                     font-size: %dpx;
                 }
                 .book-position {
                     color: #8b5a2b;
                     font-size: %dpx;
                     margin-left: auto;
                 }
                 .level {
                     margin: 0;
                     padding: 0;
                 }
                 .variation-item {
                     display: flex;
                     align-items: center;
                     padding: 2px 0px;
                     border-radius: 4px;
                     cursor: pointer;
                     transition: background-color 0.15s ease;
                     background-color: transparent;
                     flex-wrap: wrap;
                     gap: 4px 8px;
                     font-family: 'Segoe UI', 'Consolas', monospace;
                     margin: 0;
                     font-size: %dpx;
                     outline: none; /* Убираем outline при фокусе */
                     user-select: none; /* Запрещаем выделение текста */
                 }
                 .move-number {
                     color: #888;
                     font-size: %dpx;
                     min-width: 24px;
                     text-align: right;
                     font-weight: normal;
                 }
                 .variation-move {
                     font-family: 'Segoe UI', 'Arial', sans-serif;
                     font-weight: 600;
                     color: #1a1a1a;
                     font-size: %dpx;
                     padding: 0 2px;
                     min-width: 30px;
                 }
                 .variation-stats {
                     color: #666;
                     font-size: %dpx;
                     font-weight: normal;
                     padding-left: 50px;
                     font-family: 'Consolas', monospace;
                 }
                 .variation-item.active {
                     background-color: #d4c4a8;
                     border-left: 3px solid #8b5a2b;
                     font-weight: bold;
                 }
                 .position-info {
                     color: #666;
                     font-size: %dpx;
                     padding: 2px 0 4px 0;
                     font-style: italic;
                 }
                 .variations-list {
                     display: flex;
                     flex-direction: column;
                     gap: 2px;
                     padding: 0;
                     margin: 0;
                 }
                """.formatted(
                fontSize,          // .book-container
                infoFontSize,      // .book-info
                statsFontSize,     // .book-entries
                infoFontSize,      // .book-position
                moveFontSize,      // .variation-item
                numberFontSize,    // .move-number
                moveFontSize,      // .variation-move
                statsFontSize,     // .variation-stats
                infoFontSize       // .position-info
        );
    }

    // ========== JAVASCRIPT ==========

    private String getJavaScript() {
        return """
        console.log('BookHtmlVisitor JavaScript loaded!');

        // ========== ПЕРЕМЕННЫЕ ==========
        let selectedIndex = 0;
        let activeItem = null;

        // ========== ФУНКЦИЯ ОБНОВЛЕНИЯ ВЫДЕЛЕНИЯ ==========
        function updateSelection() {
            const items = document.querySelectorAll('.variation-item');
            items.forEach((el, i) => {
                el.classList.toggle('active', i === selectedIndex);
                if (i === selectedIndex) {
                    el.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
                    activeItem = el;
                }
            });
        }

        // ========== СНИМАЕМ ФОКУС ==========
        function removeFocus() {
            const hiddenInput = document.getElementById('hidden-focus');
            if (hiddenInput) {
                hiddenInput.focus();
            } else {
                document.body.focus();
            }
        }

        // ========== ОБРАБОТКА КЛИКОВ МЫШКИ ==========
        document.addEventListener('click', function(e) {
            const item = e.target.closest('.variation-item');
            if (item) {
                removeFocus();
                const nodeUuid = item.dataset.nodeUuid;
                if (nodeUuid && window.javaBridge) {
                    window.javaBridge.onMoveSelected(nodeUuid);
                }
            }
        });

        // ========== ОБРАБОТКА КЛАВИАТУРЫ ==========
        document.addEventListener('keydown', function(e) {
            const items = document.querySelectorAll('.variation-item');
            if (items.length === 0) return;

            removeFocus();

            switch(e.key) {
                case 'ArrowUp':
                    e.preventDefault();
                    selectedIndex = Math.max(0, selectedIndex - 1);
                    updateSelection();
                    break;
                case 'ArrowDown':
                    e.preventDefault();
                    selectedIndex = Math.min(items.length - 1, selectedIndex + 1);
                    updateSelection();
                    break;
                case 'ArrowRight':
                case 'Enter':
                    e.preventDefault();
                    if (activeItem) {
                        const nodeUuid = activeItem.dataset.nodeUuid;
                        if (nodeUuid && window.javaBridge) {
                            window.javaBridge.onMoveSelected(nodeUuid);
                        }
                    }
                    break;
                case 'ArrowLeft':
                    e.preventDefault();
                    if (window.javaBridge) {
                        window.javaBridge.onMoveLeft();
                    }
                    break;
                case 'Home':
                    e.preventDefault();
                    if (window.javaBridge) {
                        window.javaBridge.onHomePressed();
                    }
                    break;
                case 'End':
                    e.preventDefault();
                    if (window.javaBridge) {
                        window.javaBridge.onEndPressed();
                    }
                    break;
            }
        });

        // ========== ИНИЦИАЛИЗАЦИЯ ==========
        document.addEventListener('DOMContentLoaded', function() {
            const items = document.querySelectorAll('.variation-item');
            if (items.length > 0) {
                let found = false;
                items.forEach((el, i) => {
                    if (el.classList.contains('active')) {
                        selectedIndex = i;
                        activeItem = el;
                        found = true;
                    }
                });
                if (!found) {
                    selectedIndex = 0;
                    items[0].classList.add('active');
                    activeItem = items[0];
                }
                activeItem.scrollIntoView({ block: 'center' });
            }

            removeFocus();
        });

        // ========== ОБНОВЛЕНИЕ ПОСЛЕ НАВИГАЦИИ ==========
        window.updateNavigationState = function() {
            const items = document.querySelectorAll('.variation-item');
            if (items.length > 0) {
                let found = false;
                items.forEach((el, i) => {
                    if (el.classList.contains('active')) {
                        selectedIndex = i;
                        activeItem = el;
                        found = true;
                    }
                });
                if (!found) {
                    selectedIndex = 0;
                    items[0].classList.add('active');
                    activeItem = items[0];
                }
                activeItem.scrollIntoView({ block: 'center' });
            }
            removeFocus();
        };
    """;
    }

    private int getMoveNumber(ParentNode node) {
        if (node == null || node.isRoot()) return 0;
        int ply = node.getAbsolutePly();
        if (ply <= 0) return 0;
        return (ply + 1) / 2;
    }
}