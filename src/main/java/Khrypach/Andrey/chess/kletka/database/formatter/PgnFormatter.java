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

package Khrypach.Andrey.chess.kletka.database.formatter;

import Khrypach.Andrey.chess.kletka.database.model.GameData;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;

import java.time.format.DateTimeFormatter;

/**
 * Форматирует GameData в PGN строку
 * Использует PgnExportVisitor для тела партии
 */
public class PgnFormatter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final LanguageManager lang = LanguageManager.getInstance();

    /**
     * Преобразует GameData в PGN строку
     */
    public String format(GameData game) {
        if (game == null) {
            return "";
        }

        String pgn = game.pgn();
        if (pgn != null && !pgn.trim().isEmpty()) {
            return pgn;
        }

        StringBuilder sb = new StringBuilder();

        // === Заголовки ===
        sb.append(formatHeader("Event", game.event()));
        sb.append(formatHeader("Site", game.site()));
        sb.append(formatHeader("Date", game.date() != null ? game.date().format(DATE_FORMATTER) : "????.??.??"));
        sb.append(formatHeader("Round", game.round()));
        sb.append(formatHeader("White", game.whitePlayer()));
        sb.append(formatHeader("Black", game.blackPlayer()));
        sb.append(formatHeader("Result", game.result()));
        sb.append(formatHeader("FEN", game.fen()));
        if (game.positionType() != null && !lang.get(LanguageKeys.GAME_TYPE_GAME).equals(game.positionType())) {
            sb.append(formatHeader("PositionType", game.positionType()));
        }
        sb.append(formatHeader("SetUp", game.isSetUp() ? "1" : "0"));

        // Дополнительные заголовки (если есть)
        if (game.whiteElo() != null && !"?".equals(game.whiteElo())) {
            sb.append(formatHeader("WhiteElo", game.whiteElo()));
        }
        if (game.blackElo() != null && !"?".equals(game.blackElo())) {
            sb.append(formatHeader("BlackElo", game.blackElo()));
        }
        if (game.eco() != null && !"?".equals(game.eco())) {
            sb.append(formatHeader("ECO", game.eco()));
        }
        if (game.opening() != null && !"?".equals(game.opening())) {
            sb.append(formatHeader("Opening", game.opening()));
        }
        if (game.variation() != null && !"?".equals(game.variation())) {
            sb.append(formatHeader("Variation", game.variation()));
        }
        if (game.annotator() != null && !"?".equals(game.annotator())) {
            sb.append(formatHeader("Annotator", game.annotator()));
        }
        if (game.whiteTeam() != null && !"?".equals(game.whiteTeam())) {
            sb.append(formatHeader("WhiteTeam", game.whiteTeam()));
        }
        if (game.blackTeam() != null && !"?".equals(game.blackTeam())) {
            sb.append(formatHeader("BlackTeam", game.blackTeam()));
        }
        if (game.source() != null && !"?".equals(game.source())) {
            sb.append(formatHeader("Source", game.source()));
        }
        if (game.subround() != null && !"?".equals(game.subround())) {
            sb.append(formatHeader("Subround", game.subround()));
        }
        if (game.whiteFideId() != null && !"?".equals(game.whiteFideId())) {
            sb.append(formatHeader("WhiteFideId", game.whiteFideId()));
        }
        if (game.blackFideId() != null && !"?".equals(game.blackFideId())) {
            sb.append(formatHeader("BlackFideId", game.blackFideId()));
        }
        if (game.timeControl() != null && !"?".equals(game.timeControl())) {
            sb.append(formatHeader("TimeControl", game.timeControl()));
        }
        if (game.plyCount() != null && !"?".equals(game.plyCount())) {
            sb.append(formatHeader("PlyCount", game.plyCount()));
        }

        // Пустая строка после заголовков
        sb.append("\n");

        // === Тело партии ===
        String body = formatBody(game);
        if (!body.isEmpty()) {
            sb.append(body);
        }

        return sb.toString();
    }

    /**
     * Форматирует заголовок PGN
     */
    private String formatHeader(String key, String value) {
        if (value == null || value.isEmpty()) {
            value = "?";
        }
        return String.format("[%s \"%s\"]\n", key, value);
    }

    /**
     * Форматирует тело партии (ходы)
     * Использует PgnExportVisitor
     */
    private String formatBody(GameData game) {
        String pgn = game.pgn();

        // Если есть готовый PGN, используем его
        if (pgn != null && !pgn.trim().isEmpty()) {
            return pgn;
        }
        return "";
    }
}