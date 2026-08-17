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

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import lombok.Getter;

public enum MoveAnnotation {
    // ========== 1. Оценка хода ==========
    BRILLIANT_MOVE("!!", LanguageKeys.ANNOTATION_BRILLIANT_MOVE, "!! "),
    GOOD_MOVE("!", LanguageKeys.ANNOTATION_GOOD_MOVE, "! "),
    INTERESTING_MOVE("!?", LanguageKeys.ANNOTATION_INTERESTING_MOVE, "!? "),
    DUBIOUS_MOVE("?!", LanguageKeys.ANNOTATION_DUBIOUS_MOVE, "?! "),
    BAD_MOVE("?", LanguageKeys.ANNOTATION_BAD_MOVE, "? "),
    BLUNDER("??", LanguageKeys.ANNOTATION_BLUNDER, "?? "),

    // ========== 2. Оценка позиции ==========
    CLEAR_ADVANTAGE_WHITE(" ± ", LanguageKeys.ANNOTATION_CLEAR_ADVANTAGE_WHITE, " ± "),
    WINNING_WHITE(" +– ", LanguageKeys.ANNOTATION_WINNING_WHITE, " +– "),
    SLIGHT_ADVANTAGE_WHITE(" += ", LanguageKeys.ANNOTATION_SLIGHT_ADVANTAGE_WHITE, " += "),
    EQUALITY(" = ", LanguageKeys.ANNOTATION_EQUALITY, " = "),
    SLIGHT_ADVANTAGE_BLACK(" =+ ", LanguageKeys.ANNOTATION_SLIGHT_ADVANTAGE_BLACK, " =+ "),
    CLEAR_ADVANTAGE_BLACK(" ∓ ", LanguageKeys.ANNOTATION_CLEAR_ADVANTAGE_BLACK, " ∓ "),
    WINNING_BLACK(" –+ ", LanguageKeys.ANNOTATION_WINNING_BLACK, " –+ "),
    UNCLEAR_POSITION(" ∞ ", LanguageKeys.ANNOTATION_UNCLEAR_POSITION, " ∞ "),
    WITH_COMPENSATION(" ≅ ", LanguageKeys.ANNOTATION_WITH_COMPENSATION, " ≅ "),

    // ========== 3. Комментарии и пояснения ==========
    ONLY_MOVE("□", LanguageKeys.ANNOTATION_ONLY_MOVE, "□ "),
    THEORETICAL_NOVELTY("N", LanguageKeys.ANNOTATION_THEORETICAL_NOVELTY, "N "),
    ONLY_AND_BEST_MOVE("□", LanguageKeys.ANNOTATION_ONLY_AND_BEST_MOVE, "□ "), // Тот же символ, но другое значение
    WITH_IDEA("Δ", LanguageKeys.ANNOTATION_WITH_IDEA, "Δ "),
    WITH_INITIATIVE("↑", LanguageKeys.ANNOTATION_WITH_INITIATIVE, "↑ "),
    WITH_COUNTERPLAY("⇄", LanguageKeys.ANNOTATION_WITH_COUNTERPLAY, "⇄ "),
    DEVELOPMENT_ADVANTAGE("↻", LanguageKeys.ANNOTATION_DEVELOPMENT_ADVANTAGE, "↻ "),
    BETTER_WAS("⌓", LanguageKeys.ANNOTATION_BETTER_WAS, "⌓ "),

    // Шах и мат (специальные, добавляются автоматически, но могут быть и ручными)
    CHECK("+", LanguageKeys.ANNOTATION_CHECK, "+"),
    DOUBLE_CHECK("++", LanguageKeys.ANNOTATION_DOUBLE_CHECK, "++"),
    MATE("#", LanguageKeys.ANNOTATION_MATE, "# ");

    @Getter
    private final String symbol;
    private final String descriptionKey;
    @Getter
    private final String displayWithSpace;

    // Конструктор для аннотаций с ключом локализации
    MoveAnnotation(String symbol, String descriptionKey, String displayWithSpace) {
        this.symbol = symbol;
        this.descriptionKey = descriptionKey;
        this.displayWithSpace = displayWithSpace;
    }

    public String getDescription() {
        LanguageManager lang = LanguageManager.getInstance();
        if (descriptionKey != null) {
            return lang.get(descriptionKey);
        }
        return symbol.equals(CHECK.getSymbol()) ? lang.get(LanguageKeys.ANNOTATION_CHECK) :
                symbol.equals(MATE.getSymbol()) ? lang.get(LanguageKeys.ANNOTATION_MATE) : symbol;
    }

    /**
     * Получает аннотацию из строки с поддержкой составных символов
     */
    public static MoveAnnotation fromSymbol(String symbol) {
        // ========== ДОБАВЛЯЕМ ПРОВЕРКУ НА NULL ==========
        if (symbol == null) {
            return null;
        }

        // symbol уже может быть с пробелом
        for (MoveAnnotation ann : values()) {
            if (ann.symbol.equals(symbol)) {
                return ann;
            }
        }
        // Если не нашли, пробуем без пробела
        String trimmed = symbol.trim();
        for (MoveAnnotation ann : values()) {
            if (ann.symbol.trim().equals(trimmed)) {
                return ann;
            }
        }
        return null;
    }
}