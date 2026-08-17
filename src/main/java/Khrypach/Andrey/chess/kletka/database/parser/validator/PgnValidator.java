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

package Khrypach.Andrey.chess.kletka.database.parser.validator;

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Валидатор PGN
 * Проверяет корректность PGN строк без изменения состояния
 */
public class PgnValidator {

    private static final Logger log = LoggerFactory.getLogger(PgnValidator.class);
    private final LanguageManager lang = LanguageManager.getInstance();

    /**
     * Проверяет, является ли PGN позицией (а не партией)
     */
    public boolean isPositionPgn(String pgn) {
        if (pgn == null) return false;
        return pgn.contains("[SetUp \"1\"]") || pgn.contains("[FEN \"");
    }

    /**
     * Определяет тип контента по PGN строке
     */
    public String detectContentType(String pgn) {
        log.debug("Detecting content type");

        if (pgn == null || pgn.isEmpty()) {
            return lang.get(LanguageKeys.GAME_TYPE_GAME);
        }

        // Проверяем наличие SetUp или FEN
        boolean hasSetUp = pgn.contains("[SetUp \"1\"]");
        boolean hasFen = pgn.contains("[FEN \"");

        // Если нет SetUp и нет FEN — это обычная игра
        if (!hasSetUp && !hasFen) {
            return lang.get(LanguageKeys.GAME_TYPE_GAME);
        }

        // Теперь проверяем, что это за тип позиции
        String lowerPgn = pgn.toLowerCase();

        // Проверяем EventType
        if (pgn.contains("[EventType \"tourn\"]")) {
            return lang.get(LanguageKeys.GAME_TYPE_PROBLEM);
        }
        if (pgn.contains("[EventType \"study\"]") || pgn.contains("[EventType \"eth\"]")) {
            return lang.get(LanguageKeys.GAME_TYPE_STUDY);
        }

        // Проверяем ключевые слова в White/Black полях
        String mateKeyword = lang.get(LanguageKeys.PGN_KEYWORD_MATE).toLowerCase();
        String studyKeyword = lang.get(LanguageKeys.PGN_KEYWORD_STUDY).toLowerCase();

        if (lowerPgn.contains(mateKeyword)) {
            return lang.get(LanguageKeys.GAME_TYPE_PROBLEM);
        }
        if (lowerPgn.contains(studyKeyword)) {
            return lang.get(LanguageKeys.GAME_TYPE_STUDY);
        }

        // Если есть SetUp или FEN, но не задача и не этюд — это позиция
        return lang.get(LanguageKeys.GAME_TYPE_POSITION);
    }
}