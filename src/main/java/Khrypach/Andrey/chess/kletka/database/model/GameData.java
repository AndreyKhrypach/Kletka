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

package Khrypach.Andrey.chess.kletka.database.model;

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;

import java.time.LocalDate;

/**
 * Полные данные о партии для PGN
 * Расширено для поддержки ChessBase полей и позиций
 */
public record GameData(
        // === Игроки и результат ===
        String whitePlayer,
        String blackPlayer,
        String result,
        String whiteElo,
        String blackElo,

        // === Турнир ===
        String event,
        String site,
        String round,
        String subround,
        LocalDate date,

        // === Дебют ===
        String eco,
        String opening,
        String variation,

        // === Аннотатор и команды ===
        String annotator,
        String whiteTeam,
        String blackTeam,
        String source,

        // === Дополнительно ===
        String whiteFideId,
        String blackFideId,
        String timeControl,
        String plyCount,

        // === Тело партии ===
        String pgn,

        // ========== НОВЫЕ ПОЛЯ ДЛЯ ПОЗИЦИЙ ==========
        String fen,              // FEN позиции (если есть)
        boolean isSetUp,         // Флаг установки позиции
        String positionType,      // "game", "position", "study", "problem"
        boolean deleted   // Маркер удаления партии
) {
    public GameData {
        whitePlayer = normalize(whitePlayer, LanguageManager.getInstance().get(LanguageKeys.DEFAULT_PLAYER_NAME));
        blackPlayer = normalize(blackPlayer, LanguageManager.getInstance().get(LanguageKeys.DEFAULT_PLAYER_NAME));
        result = normalize(result, "*");
        whiteElo = normalize(whiteElo, "?");
        blackElo = normalize(blackElo, "?");
        event = normalize(event, "?");
        site = normalize(site, "?");
        round = normalize(round, "?");
        subround = normalize(subround, "?");
        date = date == null ? LocalDate.now() : date;
        eco = normalize(eco, "?");
        opening = normalize(opening, "?");
        variation = normalize(variation, "?");
        annotator = normalize(annotator, "?");
        whiteTeam = normalize(whiteTeam, "?");
        blackTeam = normalize(blackTeam, "?");
        source = normalize(source, "?");
        whiteFideId = normalize(whiteFideId, "?");
        blackFideId = normalize(blackFideId, "?");
        timeControl = normalize(timeControl, "?");
        plyCount = normalize(plyCount, "?");
        pgn = pgn == null ? "" : pgn;
        fen = fen == null ? "" : fen;
        positionType = normalize(positionType, "game");
        // deleted - boolean, не нормализуем
    }

    // Конструктор для обратной совместимости
    public GameData(String whitePlayer, String blackPlayer, String whiteElo,
                    String blackElo, String event, String site, String round, String pgn) {
        this(
                whitePlayer, blackPlayer, "*",
                whiteElo, blackElo,
                event, site, round, "?",
                LocalDate.now(),
                "?", "?", "?",
                "?", "?", "?", "?",
                "?", "?", "?", "?",
                pgn,
                "",     // fen
                false,  // isSetUp
                "game",  // positionType
                false
        );
    }

    private static String normalize(String value, String defaultValue) {
        return (value == null || value.trim().isEmpty()) ? defaultValue : value.trim();
    }

    /**
     * Проверяет, является ли данная запись позицией (не партией)
     */
    public boolean isPosition() {
        return !"game".equals(positionType);
    }

    /**
     * Получает тип контента в виде строки для отображения
     */
    public String getTypeDisplay() {
        return switch(positionType) {
            case "position" -> "◇ " + LanguageManager.getInstance().get(LanguageKeys.GAME_TYPE_POSITION);
            case "study" -> "📖 " + LanguageManager.getInstance().get(LanguageKeys.GAME_TYPE_STUDY);
            case "problem" -> "🧩 " + LanguageManager.getInstance().get(LanguageKeys.GAME_TYPE_PROBLEM);
            default -> "♟ " + LanguageManager.getInstance().get(LanguageKeys.GAME_TYPE_GAME);
        };
    }
}