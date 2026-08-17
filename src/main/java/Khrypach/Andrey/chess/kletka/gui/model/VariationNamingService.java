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

package Khrypach.Andrey.chess.kletka.gui.model;

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class VariationNamingService {
    private static final Logger log = LoggerFactory.getLogger(VariationNamingService.class);
    private final LanguageManager languageManager = LanguageManager.getInstance();

    /**
     * Генерирует уникальное имя для варианта на основе родителя и индекса среди сиблингов
     * Правила:
     * 1. Если родитель null или ROOT → заглавная буква + ")" (A), B), C)...)
     * 2. Если родитель - главная линия ("~" или "Главная линия") → строчная буква + ")" (a), b), c)...)
     * 3. Если родитель заканчивается на БУКВУ → добавляем ЦИФРУ (A1), A2)...)
     * 4. Если родитель заканчивается на ЦИФРУ или ")" → добавляем БУКВУ (A1a), A1b)...)
     */
    public String generateUniqueName(Variation parentVariation, int siblingIndex) {
        log.debug("generateUniqueName: parent='{}', siblingIndex={}",
                parentVariation != null ? parentVariation.getName() : "null", siblingIndex);

        // ========== СЛУЧАЙ 1: Корневой уровень ==========
        if (parentVariation == null ||
                parentVariation.getName().equals(languageManager.get(LanguageKeys.ROOT))) {
            char letter = getUppercaseLetter(siblingIndex);
            String result = letter + ")";
            log.debug("generateUniqueName → root level: {} (index={})", result, siblingIndex);
            return result;
        }

        String parentName = parentVariation.getName();

        // ========== ПРОВЕРКА: родитель - главная линия ==========
        if (parentName.equals(languageManager.get(LanguageKeys.MAIN_LINE)) ||
                parentName.equals("~")) {
            char letter = getLowercaseLetter(siblingIndex);
            String result = letter + ")";
            log.debug("generateUniqueName → main line parent: {} (index={})", result, siblingIndex);
            return result;
        }

        // ========== УДАЛЯЕМ ТОЛЬКО ПОСЛЕДНЮЮ СКОБКУ ==========
        String cleanParentName = parentName;
        if (cleanParentName.endsWith(")")) {
            cleanParentName = cleanParentName.substring(0, cleanParentName.length() - 1);
        }

        // ========== ЕСЛИ ПОСЛЕ УДАЛЕНИЯ СКОБКИ ИМЯ ПУСТОЕ ==========
        if (cleanParentName.isEmpty()) {
            char letter = getLowercaseLetter(siblingIndex);
            String result = letter + ")";
            log.debug("generateUniqueName → empty parent: {} (index={})", result, siblingIndex);
            return result;
        }

        // ========== ОПРЕДЕЛЯЕМ ПОСЛЕДНИЙ СИМВОЛ ==========
        char lastChar = cleanParentName.charAt(cleanParentName.length() - 1);
        boolean parentEndsWithLetter = Character.isLetter(lastChar);

        String suffix;
        if (parentEndsWithLetter) {
            // Буква → цифра
            suffix = String.valueOf(siblingIndex);
            log.debug("generateUniqueName → parent ends with letter '{}', adding digit '{}'",
                    lastChar, suffix);
        } else {
            // Цифра → буква
            char letter = getLowercaseLetter(siblingIndex);
            suffix = String.valueOf(letter);
            log.debug("generateUniqueName → parent ends with digit '{}', adding letter '{}'",
                    lastChar, suffix);
        }

        String result = cleanParentName + suffix + ")";
        log.debug("generateUniqueName → result: '{}'", result);
        return result;
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

    /**
     * Обновляет имена всех вариантов в дереве
     * Сохраняет имена, которые уже есть (из PGN), генерирует только для безымянных
     */
    public void updateAllVariationNames(Variation rootVariation) {
        if (rootVariation == null) return;

        ParentNode firstNode = rootVariation.getFirstNode();
        if (firstNode == null || !firstNode.isRoot()) {
            return;
        }

        RootNode rootNode = (RootNode) firstNode;
        List<Variation> firstLevelVars = rootNode.getSubVariations();

        int siblingCounter = 1;
        for (Variation var : firstLevelVars) {
            if (var == null) continue;

            if (var.isMainLine()) {
                var.setName("~");
                // ========== ИСПРАВЛЕНИЕ: parent = var, а не rootVariation ==========
                updateNamesRecursive(var);
                continue;
            }

            String currentName = var.getName();
            boolean hasCustomName = currentName != null &&
                    !currentName.isEmpty() &&
                    !currentName.equals(languageManager.get(LanguageKeys.VARIATION_DEFAULT_NAME)) &&
                    !currentName.equals(languageManager.get(LanguageKeys.ROOT));

            if (hasCustomName) {
                log.debug("Preserving existing variation name: {}", currentName);
                // ========== ИСПРАВЛЕНИЕ: parent = var, а не rootVariation ==========
                updateNamesRecursive(var);
                continue;
            }

            String newName = generateUniqueName(null, siblingCounter);
            var.setName(newName);
            log.debug("Generated root variation name: {} (counter={})", newName, siblingCounter);
            siblingCounter++;

            // ========== ИСПРАВЛЕНИЕ: parent = var ==========
            updateNamesRecursive(var);
        }
    }

    /**
     * Рекурсивно обновляет имена для всех под-вариантов
     * Сохраняет имена, которые уже есть (из PGN), генерирует только для безымянных
     */
    private void updateNamesRecursive(Variation current) {
        if (current == null) return;

        List<ParentNode> moves = current.getMoves();
        if (moves == null) return;

        for (ParentNode node : moves) {
            if (node == null || node.isRoot()) continue;

            List<Variation> subVars = node.getSubVariations();
            if (subVars == null || subVars.isEmpty()) continue;

            // ========== РЕВЕРСИМ, ЕСЛИ ПЕРВЫЙ — ПРОДОЛЖЕНИЕ ==========
            boolean firstIsContinuation = isFirstIsContinuation(node, subVars);

            if (firstIsContinuation) {
                Collections.reverse(subVars);
            }

            // ========== ДАЕМ ИМЕНА ВСЕМ ВАРИАНТАМ ==========
            int counter = 0;
            for (Variation subVar : subVars) {
                if (subVar == null) continue;

                if (subVar.isMainLine()) {
                    subVar.setName("~");
                    updateNamesRecursive(subVar);
                    continue;
                }

                // ========== ВСЕ ВАРИАНТЫ ПОЛУЧАЮТ ИМЕНА ==========
                String newName = generateUniqueName(current, counter + 1);
                subVar.setName(newName);
                subVar.setNameGenerated(true);
                log.debug("Generated nested variation name: {} (parent={}, counter={})",
                        newName, current.getName(), counter);

                updateNamesRecursive(subVar);
                counter++;
            }
        }
    }

    private static boolean isFirstIsContinuation(ParentNode node, List<Variation> subVars) {
        ParentNode nextNode = node.getNext();
        String nextUci = nextNode != null && !nextNode.isRoot() ? nextNode.getUciMove() : null;

        boolean firstIsContinuation = false;
        if (!subVars.isEmpty()) {
            Variation first = subVars.get(0);
            if (first != null && !first.isMainLine()) {
                ParentNode firstNode = first.getFirstNode();
                firstIsContinuation = firstNode != null && !firstNode.isRoot() &&
                        nextUci != null && firstNode.getUciMove().equals(nextUci);
            }
        }
        return firstIsContinuation;
    }
}