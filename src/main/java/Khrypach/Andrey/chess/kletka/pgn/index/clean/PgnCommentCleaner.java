/*
 * Copyright (c) 2025 Andrey Khrypach
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
 *
 */

package Khrypach.Andrey.chess.kletka.pgn.index.clean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Очистка комментариев/аннотаций/NAG из ТЕЛА партии PGN.
 * Заголовки [Event "..."] и т.п. НЕ трогаются.
 */
public final class PgnCommentCleaner {

    private static final Logger log = LoggerFactory.getLogger(PgnCommentCleaner.class);

    // {...}
    private static final Pattern COMMENT_BRACE = Pattern.compile("\\{[^}]*}");
    // ;... до конца строки
    private static final Pattern COMMENT_SEMICOLON = Pattern.compile(";[^\\n]*");
    // $N
    private static final Pattern NAG = Pattern.compile("\\$\\d+");
    // Суффиксы SAN: ! ? !! ?? !? ?! после буквы/цифры/символа хода/рокады
    // (?<=[...]) — lookbehind: символ перед суффиксом
    private static final Pattern SUFFIX = Pattern.compile("(?<=[a-hKQRBNO0-9x+#=O])[!?]{1,2}");

    private PgnCommentCleaner() {}

    public record CleanResult(
            String cleanedBody,
            int removedComments,
            int removedNags,
            int removedSuffixes
    ) {
        public int totalRemoved() {
            return removedComments + removedNags + removedSuffixes;
        }
    }

    /**
     * Очищает тело PGN-партии. Заголовки не трогает.
     *
     * @param fullPgn полный текст партии (заголовки + тело)
     * @return CleanResult с очищенным текстом и счётчиками
     */
    public static CleanResult clean(String fullPgn) {
        if (fullPgn == null || fullPgn.isEmpty()) {
            return new CleanResult(fullPgn == null ? "" : fullPgn, 0, 0, 0);
        }

        int bodyStart = findBodyStart(fullPgn);
        if (bodyStart < 0) {
            // нет тела — нечего чистить
            return new CleanResult(fullPgn, 0, 0, 0);
        }

        String headers = fullPgn.substring(0, bodyStart);
        String body = fullPgn.substring(bodyStart);

        // ========== 1. { ... } ==========
        int braceCount = countMatches(body, COMMENT_BRACE);
        String cleaned = COMMENT_BRACE.matcher(body).replaceAll("");

        // ========== 2. ; ... ==========
        int semiCount = countMatches(cleaned, COMMENT_SEMICOLON);
        cleaned = COMMENT_SEMICOLON.matcher(cleaned).replaceAll("");

        // ========== 3. $N ==========
        int nagCount = countMatches(cleaned, NAG);
        cleaned = NAG.matcher(cleaned).replaceAll("");

        // ========== 4. Суффиксы ! ? ==========
        int suffixCount = countMatches(cleaned, SUFFIX);
        cleaned = SUFFIX.matcher(cleaned).replaceAll("");

        // ========== 5. Схлопываем пробелы ==========
        cleaned = cleaned.replaceAll("[ \\t]+", " ");
        cleaned = cleaned.replaceAll(" *\\n *", "\n");
        cleaned = cleaned.replaceAll("\\n{3,}", "\n\n");

        return new CleanResult(
                headers + cleaned,
                braceCount + semiCount,
                nagCount,
                suffixCount
        );
    }

    /**
     * Находит начало тела: первая пустая строка после блока заголовков.
     * НЕ использует lastIndexOf(']') — потому что ']' может быть в комментариях тела.
     * @return позиция начала тела, или -1 если тела нет
     */
    private static int findBodyStart(String fullPgn) {
        int i = 0;
        int len = fullPgn.length();
        boolean seenHeader = false;

        while (i < len) {
            // Пропускаем пробелы/табы в начале строки
            while (i < len && (fullPgn.charAt(i) == ' ' || fullPgn.charAt(i) == '\t')) i++;
            if (i >= len) break;

            char c = fullPgn.charAt(i);

            // Пустая строка
            if (c == '\n' || c == '\r') {
                if (seenHeader) {
                    // Пропускаем все \n / \r
                    while (i < len && (fullPgn.charAt(i) == '\n' || fullPgn.charAt(i) == '\r')) i++;
                    return i;
                }
                i++;
                continue;
            }

            // Заголовок
            if (c == '[') {
                seenHeader = true;
                while (i < len && fullPgn.charAt(i) != '\n' && fullPgn.charAt(i) != '\r') i++;
                continue;
            }

            // Тело без пустой строки после заголовков (битый PGN)
            if (seenHeader) {
                return i;
            }

            // Мусор до заголовков — пропускаем
            while (i < len && fullPgn.charAt(i) != '\n') i++;
        }

        return -1;
    }

    private static int countMatches(String text, Pattern pattern) {
        Matcher m = pattern.matcher(text);
        int count = 0;
        while (m.find()) count++;
        return count;
    }
}
