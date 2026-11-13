/*
 *
 *  * Copyright (c) 2025 Andrey Khrypach
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

package Khrypach.Andrey.chess.kletka.gui.model;

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Сервис именования вариантов.
 *
 * Отвечает за:
 * - Генерацию одного имени (generateUniqueName).
 * - Массовое переименование всего дерева (renameAllIterative) — при парсинге PGN.
 * - Локальное переименование поддерева (renameFromNodeIterative) — при пользовательских действиях.
 *
 * Все обходы — итеративные (BFS), без рекурсии. Это гарантирует:
 * - Отсутствие StackOverflow на глубоких деревьях.
 * - Предсказуемую сложность O(N) по количеству узлов.
 * - Отсутствие реверса списков (continuation всегда последний благодаря
 *   insertBeforeContinuation в VariationManager и VariationParser).
 */
public class VariationNamingService {
    private static final Logger log = LoggerFactory.getLogger(VariationNamingService.class);
    private final LanguageManager languageManager = LanguageManager.getInstance();

    // ============================================================
    // ГЕНЕРАЦИЯ ОДНОГО ИМЕНИ
    // ============================================================

    /**
     * Генерирует уникальное имя для варианта на основе родителя и индекса среди сиблингов.
     * Правила:
     * 1. Если родитель null или ROOT → заглавная буква + ")" (A), B), C)...)
     * 2. Если родитель - главная линия ("~" или "Главная линия") → строчная буква + ")" (a), b), c)...)
     * 3. Если родитель заканчивается на БУКВУ → добавляем ЦИФРУ (A1), A2)...)
     * 4. Если родитель заканчивается на ЦИФРУ или ")" → добавляем БУКВУ (A1a), A1b)...)
     */
    public String generateUniqueName(Variation parentVariation, int siblingIndex) {

        // ========== СЛУЧАЙ 1: Корневой уровень ==========
        if (parentVariation == null ||
                parentVariation.getName().equals(languageManager.get(LanguageKeys.ROOT))) {
            char letter = getUppercaseLetter(siblingIndex);
            return letter + ")";
        }

        String parentName = parentVariation.getName();

        // ========== ПРОВЕРКА: родитель - главная линия ==========
        if (parentName.equals(languageManager.get(LanguageKeys.MAIN_LINE)) ||
                parentName.equals("~")) {
            char letter = getLowercaseLetter(siblingIndex);
            return letter + ")";
        }

        // ========== УДАЛЯЕМ ТОЛЬКО ПОСЛЕДНЮЮ СКОБКУ ==========
        String cleanParentName = parentName;
        if (cleanParentName.endsWith(")")) {
            cleanParentName = cleanParentName.substring(0, cleanParentName.length() - 1);
        }

        // ========== ЕСЛИ ПОСЛЕ УДАЛЕНИЯ СКОБКИ ИМЯ ПУСТОЕ ==========
        if (cleanParentName.isEmpty()) {
            char letter = getLowercaseLetter(siblingIndex);
            return letter + ")";
        }

        // ========== ОПРЕДЕЛЯЕМ ПОСЛЕДНИЙ СИМВОЛ ==========
        char lastChar = cleanParentName.charAt(cleanParentName.length() - 1);
        boolean parentEndsWithLetter = Character.isLetter(lastChar);

        String suffix;
        if (parentEndsWithLetter) {
            // Буква → цифра
            suffix = String.valueOf(siblingIndex);
        } else {
            // Цифра → буква
            char letter = getLowercaseLetter(siblingIndex);
            suffix = String.valueOf(letter);
        }

        return cleanParentName + suffix + ")";
    }

    /**
     * Возвращает ЗАГЛАВНУЮ букву для индекса (только для корневых вариантов)
     */
    public char getUppercaseLetter(int index) {
        String languageCode = languageManager.getCurrentLanguage().getCode();

        if ("ru".equals(languageCode)) {
            char[] cyrillicLetters = {'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ж', 'З', 'И', 'К',
                    'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф',
                    'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Э', 'Ю', 'Я'};
            return cyrillicLetters[(index - 1) % cyrillicLetters.length];
        }

        char[] latinLetters = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
                'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
                'U', 'V', 'W', 'X', 'Y', 'Z'};
        return latinLetters[(index - 1) % latinLetters.length];
    }

    /**
     * Возвращает СТРОЧНУЮ букву для индекса (для всех НЕ корневых вариантов)
     */
    public char getLowercaseLetter(int index) {
        String languageCode = languageManager.getCurrentLanguage().getCode();

        if ("ru".equals(languageCode)) {
            char[] cyrillicLetters = {'а', 'б', 'в', 'г', 'д', 'е', 'ж', 'з', 'и', 'к',
                    'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф',
                    'х', 'ц', 'ч', 'ш', 'щ', 'э', 'ю', 'я'};
            return cyrillicLetters[(index - 1) % cyrillicLetters.length];
        }

        char[] latinLetters = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
                'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
                'u', 'v', 'w', 'x', 'y', 'z'};
        return latinLetters[(index - 1) % latinLetters.length];
    }

    // ============================================================
    // МАССОВОЕ ПЕРЕИМЕНОВАНИЕ (полное дерево)
    // ============================================================

    /**
     * Итеративно (BFS) пересчитывает имена ВСЕХ вариантов в дереве.
     *
     * Используется:
     * - После парсинга PGN (PgnParser.parseMovesWithGuiLogic / parseToGameTree).
     * - При явном запросе полного пересчёта (например, после загрузки из БД).
     *
     * НЕ ИСПОЛЬЗУЕТСЯ при пользовательских действиях (там достаточно
     * renameFromNodeIterative — дерево маленькое).
     *
     * Особенности:
     * - Без рекурсии (BFS с ArrayDeque).
     * - Не вызывает getMoves() в цикле (прямой проход по next-цепочке).
     * - Использует setNameSilent (без JavaFX property update).
     * - Сложность: O(N), где N — общее количество узлов в дереве.
     *
     * @param rootNode      корневой узел дерева
     * @param rootVariation корневой вариант (parent для главной линии)
     */
    public void renameAllIterative(RootNode rootNode, Variation rootVariation, boolean force) {
        if (rootNode == null || rootVariation == null) return;

        Deque<NamingContext> queue = new ArrayDeque<>();
        Set<ParentNode> visited = new HashSet<>();   // ← ЗАЩИТА
        queue.add(new NamingContext(rootNode, rootVariation));

        int processedNodes = 0;
        int renamedVariations = 0;

        while (!queue.isEmpty()) {
            NamingContext ctx = queue.poll();
            ParentNode node = ctx.node();
            Variation parentVariation = ctx.parentVariation();

            if (!visited.add(node)) continue;   // ← ПРОПУСКАЕМ ДУБЛИКАТЫ
            processedNodes++;

            List<Variation> subVars = node.getSubVariations();
            if (subVars == null || subVars.isEmpty()) continue;

            // ========== ШАГ 1: ПЕРЕИМЕНОВЫВАЕМ ПРЯМЫЕ SUBVARIATIONS ==========
            int index = 1;
            for (Variation subVar : subVars) {
                if (subVar == null) continue;

                String oldName = subVar.getName();
                String newName;

                if (subVar.isMainLine()) {
                    // Голова главной линии → "Главная линия"
                    // Continuation главной линии → "~"
                    if (parentVariation == rootVariation || parentVariation == null) {
                        newName = languageManager.get(LanguageKeys.MAIN_LINE);
                    } else {
                        newName = "~";
                    }
                    subVar.setNameGenerated(true);
                } else {
                    boolean hasCustomName = !force
                            && oldName != null
                            && !oldName.isEmpty()
                            && !oldName.equals(languageManager.get(LanguageKeys.VARIATION_DEFAULT_NAME))
                            && !oldName.equals(languageManager.get(LanguageKeys.ROOT))
                            && subVar.isNameGenerated();

                    if (hasCustomName) {
                        index++;
                        continue;
                    }

                    newName = generateUniqueName(parentVariation, index);
                    subVar.setNameGenerated(true);
                    index++;
                }

                if (!newName.equals(oldName)) {
                    subVar.setName(newName);
                    renamedVariations++;
                }
            }

            // ========== ШАГ 2: ДОБАВЛЯЕМ ВЛОЖЕННЫЕ УЗЛЫ В ОЧЕРЕДЬ ==========
            for (Variation subVar : subVars) {
                if (subVar == null) continue;

                // Прямой проход по next-цепочке (без getMoves)
                ParentNode move = subVar.getFirstNode();
                while (move != null && !move.isRoot()) {
                    if (!move.getSubVariations().isEmpty()) {
                        queue.add(new NamingContext(move, subVar));
                    }
                    move = move.getNext();
                }
            }
        }

        log.debug("renameAllIterative: processed {} nodes, renamed {} variations",
                processedNodes, renamedVariations);
    }

    // ============================================================
    // ЛОКАЛЬНОЕ ПЕРЕИМЕНОВАНИЕ (поддерево от узла)
    // ============================================================

    /**
     * Итеративно (BFS) пересчитывает имена ВСЕХ под-вариантов узла node
     * и (рекурсивно вглубь) — их дочерних вариантов.
     *
     * Используется при пользовательских действиях:
     * - VariationManager.createNewVariation
     * - VariationManager.overwriteInSpecificVariation
     * - VariationManager.deleteCurrentVariation
     *
     * Отличается от renameAllIterative тем, что стартует с конкретного
     * узла (а не с rootNode) и пересчитывает только его поддерево.
     *
     * @param startNode       узел, с которого начинается переименование (forkNode)
     * @param parentVariation вариант, которому принадлежит startNode (контекст для имён)
     */
    public void renameFromNodeIterative(ParentNode startNode, Variation parentVariation) {
        if (startNode == null || parentVariation == null) return;

        Deque<NamingContext> queue = new ArrayDeque<>();
        Set<ParentNode> visited = new HashSet<>();
        queue.add(new NamingContext(startNode, parentVariation));

        int processedNodes = 0;
        int renamedVariations = 0;

        while (!queue.isEmpty()) {
            NamingContext ctx = queue.poll();
            ParentNode node = ctx.node();
            Variation parentVar = ctx.parentVariation();
            if (!visited.add(node)) continue;

            processedNodes++;

            List<Variation> subVars = node.getSubVariations();
            if (subVars == null || subVars.isEmpty()) continue;

            // ========== ШАГ 1: ПЕРЕИМЕНОВЫВАЕМ ПРЯМЫЕ SUBVARIATIONS ==========
            int index = 1;
            for (Variation subVar : subVars) {
                if (subVar == null) continue;

                String oldName = subVar.getName();
                String newName;

                if (subVar.isMainLine()) {
                    // В локальном переименовании isMainLine = continuation главной линии
                    // (голова главной линии — только в rootVariation, её здесь не будет)
                    newName = "~";
                    subVar.setNameGenerated(true);
                } else {
                    boolean hasCustomName = oldName != null
                            && !oldName.isEmpty()
                            && !oldName.equals(languageManager.get(LanguageKeys.VARIATION_DEFAULT_NAME))
                            && !oldName.equals(languageManager.get(LanguageKeys.ROOT))
                            && subVar.isNameGenerated();

                    if (hasCustomName) {
                        index++;
                        continue;
                    }

                    newName = generateUniqueName(parentVar, index);
                    subVar.setNameGenerated(true);
                    index++;
                }

                if (!newName.equals(oldName)) {
                    subVar.setName(newName);
                    renamedVariations++;
                }
            }

            // ========== ШАГ 2: ДОБАВЛЯЕМ ВЛОЖЕННЫЕ УЗЛЫ В ОЧЕРЕДЬ ==========
            for (Variation subVar : subVars) {
                if (subVar == null) continue;

                ParentNode move = subVar.getFirstNode();
                while (move != null && !move.isRoot()) {
                    if (!move.getSubVariations().isEmpty()) {
                        queue.add(new NamingContext(move, subVar));
                    }
                    move = move.getNext();
                }
            }
        }

        log.debug("renameFromNodeIterative: processed {} nodes, renamed {} variations",
                processedNodes, renamedVariations);
    }

    // ============================================================
    // ВСПОМОГАТЕЛЬНЫЕ
    // ============================================================

    /**
     * Контекст для BFS-обхода: узел + его родительский вариант.
     */
    private record NamingContext(ParentNode node, Variation parentVariation) {
    }
}