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

package Khrypach.Andrey.chess.kletka.database.parser;

import Khrypach.Andrey.chess.kletka.database.parser.enums.PgnTokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Парсит заголовки PGN
 */
public class HeaderParser {

    private static final Logger log = LoggerFactory.getLogger(HeaderParser.class);

    /**
     * Парсит заголовки из токенов
     * Предполагается, что текущая позиция - начало заголовка
     */
    public Map<String, String> parseHeaders(List<PgnToken> tokens, int[] position) {

        Map<String, String> headers = new HashMap<>();

        while (position[0] < tokens.size()) {
            PgnToken token = tokens.get(position[0]);

            // Если не начало заголовка - выходим
            if (token.type() != PgnTokenType.HEADER_START) {
                break;
            }

            position[0]++; // пропускаем [

            // Ожидаем ключ заголовка
            String key;
            if (position[0] < tokens.size() && tokens.get(position[0]).type() == PgnTokenType.HEADER_KEY) {
                key = tokens.get(position[0]).value();
                position[0]++;
                log.debug("[HEADER] Header key: '{}'", key);
            } else {
                log.warn("[HEADER] Expected HEADER_KEY at position {}, got {}", position[0],
                        position[0] < tokens.size() ? tokens.get(position[0]).type() : "EOF");
                break;
            }

            // Ожидаем значение заголовка
            String value;
            if (position[0] < tokens.size() && tokens.get(position[0]).type() == PgnTokenType.HEADER_VALUE) {
                value = tokens.get(position[0]).value();
                position[0]++;
                log.debug("[HEADER] Header value: '{}'", value);
            } else {
                log.warn("[HEADER] Expected HEADER_VALUE at position {}, got {}", position[0],
                        position[0] < tokens.size() ? tokens.get(position[0]).type() : "EOF");
                break;
            }

            // Ожидаем закрывающую скобку
            if (position[0] < tokens.size() && tokens.get(position[0]).type() == PgnTokenType.HEADER_END) {
                position[0]++;
                log.debug("[HEADER] Header end found");
            } else {
                log.warn("[HEADER] Expected HEADER_END at position {}, got {}", position[0],
                        position[0] < tokens.size() ? tokens.get(position[0]).type() : "EOF");
                break;
            }

            headers.put(key, value);
        }

        return headers;
    }
}