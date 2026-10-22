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

package Khrypach.Andrey.chess.kletka.pgn.index;

import java.util.HashMap;
import java.util.Map;

/**
 * Лёгкий парсер ТОЛЬКО заголовков PGN.
 * НЕ токенизирует тело — читает только строки, начинающиеся с '['.
 * Возвращает Map заголовков.
 */
public final class HeaderOnlyParser {

    private HeaderOnlyParser() {}

    /**
     * Парсит только заголовки из начала gameContent.
     * Останавливается, как только встретил первую НЕ-заголовочную строку.
     *
     * @return Map<Key, Value> заголовков (может быть пустым)
     */
    public static Map<String, String> parseHeaders(String gameContent) {
        Map<String, String> headers = new HashMap<>(16);

        if (gameContent == null || gameContent.isEmpty()) {
            return headers;
        }

        int pos = 0;
        int len = gameContent.length();

        while (pos < len) {
            // Пропускаем пустые строки и пробелы в начале строки
            while (pos < len && (gameContent.charAt(pos) == ' ' ||
                    gameContent.charAt(pos) == '\t')) {
                pos++;
            }

            // Конец строки
            if (pos >= len) break;
            if (gameContent.charAt(pos) == '\n' || gameContent.charAt(pos) == '\r') {
                pos++;
                continue;
            }

            // Если не '[' — тело началось, стоп
            if (gameContent.charAt(pos) != '[') {
                break;
            }

            // Нашли начало заголовка — парсим строку
            int lineEnd = pos;
            while (lineEnd < len && gameContent.charAt(lineEnd) != '\n' && gameContent.charAt(lineEnd) != '\r') {
                lineEnd++;
            }

            String line = gameContent.substring(pos, lineEnd);
            parseHeaderLine(line, headers);

            pos = lineEnd;
        }

        return headers;
    }

    /**
     * Парсит одну строку вида: [Key "Value"]
     */
    private static void parseHeaderLine(String line, Map<String, String> headers) {
        // Пропускаем '['
        int i = 1;
        int len = line.length();

        // Читаем Key
        int keyStart = i;
        while (i < len && line.charAt(i) != ' ' && line.charAt(i) != ']') {
            i++;
        }
        if (i >= len) return;
        String key = line.substring(keyStart, i);

        // Пропускаем пробелы
        while (i < len && line.charAt(i) == ' ') i++;
        if (i >= len || line.charAt(i) != '"') return;
        i++; // пропускаем открывающую кавычку

        // Читаем Value до закрывающей кавычки
        int valStart = i;
        while (i < len && line.charAt(i) != '"') {
            i++;
        }
        if (i >= len) return;
        String value = line.substring(valStart, i);

        headers.put(key, value);
    }
}
