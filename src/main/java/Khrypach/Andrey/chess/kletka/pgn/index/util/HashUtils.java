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

package Khrypach.Andrey.chess.kletka.pgn.index.util;

import Khrypach.Andrey.chess.kletka.database.model.GameData;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.CRC32;

/**
 * Утилиты для вычисления хешей
 */
public class HashUtils {

    /**
     * Вычисляет CRC32 хеш для строки
     */
    public static int hashString(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        CRC32 crc = new CRC32();
        crc.update(content.getBytes(StandardCharsets.UTF_8));
        return (int) crc.getValue();
    }

    /**
     * ВЫЧИСЛЯЕТ ХЕШ НА ОСНОВЕ SET
     * Не зависит от порядка тегов!
     */
    public static int calculateContentHash(GameData gameData) {
        if (gameData == null) {
            return 0;
        }

        Set<String> parts = new HashSet<>();

        // ========== ДОБАВЛЯЕМ ВСЕ ЗНАЧИМЫЕ ПОЛЯ ==========
        addIfNotNull(parts, normalize(gameData.whitePlayer()));
        addIfNotNull(parts, normalize(gameData.blackPlayer()));
        addIfNotNull(parts, normalize(gameData.result()));
        addIfNotNull(parts, normalize(gameData.eco()));
        addIfNotNull(parts, normalize(gameData.opening()));
        addIfNotNull(parts, normalize(gameData.event()));
        addIfNotNull(parts, normalize(gameData.site()));
        addIfNotNull(parts, normalize(gameData.round()));
        addIfNotNull(parts, normalize(gameData.subround()));
        addIfNotNull(parts, normalize(gameData.annotator()));
        addIfNotNull(parts, normalize(gameData.whiteElo()));
        addIfNotNull(parts, normalize(gameData.blackElo()));
        addIfNotNull(parts, normalize(gameData.whiteTeam()));
        addIfNotNull(parts, normalize(gameData.blackTeam()));
        addIfNotNull(parts, normalize(gameData.source()));
        addIfNotNull(parts, normalize(gameData.timeControl()));
        addIfNotNull(parts, normalize(gameData.variation()));
        addIfNotNull(parts, normalize(gameData.fen()));
        addIfNotNull(parts, normalize(gameData.positionType()));
        addIfNotNull(parts, String.valueOf(gameData.isSetUp()));

        // ========== ТЕЛО ПАРТИИ ==========
        String body = extractBody(gameData.pgn());
        String normalizedBody = normalizeBody(body);
        addIfNotNull(parts, normalizedBody);

        // ========== ДАТА ==========
        if (gameData.date() != null) {
            addIfNotNull(parts, String.valueOf(gameData.date().getYear()));
            addIfNotNull(parts, String.valueOf(gameData.date().getMonthValue()));
            addIfNotNull(parts, String.valueOf(gameData.date().getDayOfMonth()));
        }

        // ========== СОРТИРУЕМ И СЧИТАЕМ ХЕШ ==========
        List<String> sortedParts = new ArrayList<>(parts);
        Collections.sort(sortedParts);
        String combined = String.join("|", sortedParts);
        return HashUtils.hashString(combined);
    }

    /**
     * Добавляет значение в Set, если оно не null
     */
    private static void addIfNotNull(Set<String> set, String value) {
        if (value != null && !value.isEmpty()) {
            set.add(value);
        }
    }

    /**
     * Нормализует тело партии
     * Возвращает null для пустого тела
     */
    public static String normalizeBody(String body) {
        if (body == null || body.isEmpty()) {
            return null;
        }

        // Убираем переносы строк и лишние пробелы
        String normalized = body.replaceAll("\\s+", " ");

        // Убираем результат в конце
        normalized = normalized.replaceAll("\\s+[0-9.-]+$", "").trim();
        normalized = normalized.replaceAll("\\s+\\*$", "").trim();

        String result = normalized.trim();
        return result.isEmpty() ? null : result;
    }

    /**
     * Нормализует строку для сравнения
     * Возвращает null для пустых строк
     */
    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Извлекает тело партии (без заголовков)
     * НЕ использует trim(), чтобы сохранить все символы!
     */
    public static String extractBody(String fullPgn) {
        if (fullPgn == null || fullPgn.isEmpty()) {
            return "";
        }

        int lastBracket = fullPgn.lastIndexOf(']');
        if (lastBracket < 0) {
            return fullPgn;
        }

        int bodyStart = -1;
        for (int i = lastBracket + 1; i < fullPgn.length() - 1; i++) {
            if (fullPgn.charAt(i) == '\n' && fullPgn.charAt(i + 1) == '\n') {
                bodyStart = i + 2;
                break;
            }
        }

        if (bodyStart < 0) {
            String after = fullPgn.substring(lastBracket + 1);
            if (!after.isEmpty() && !after.startsWith("[")) {
                // НЕ используем trim()!
                return after;
            }
            return "";
        }

        // НЕ используем trim()!
        return fullPgn.substring(bodyStart);
    }

    /**
     * ВЫЧИСЛЯЕТ ХЕШ ТОЛЬКО ТЕЛА ПАРТИИ (БЕЗ ЗАГОЛОВКОВ)
     * Сохраняет порядок, но игнорирует форматирование
     */
    public static int calculateBodyHash(GameData gameData) {
        if (gameData == null) {
            return 0;
        }

        // Извлекаем тело партии (без заголовков)
        String body = extractBody(gameData.pgn());
        if (body.isEmpty()) {
            return 0;
        }

        // ========== НОРМАЛИЗУЕМ ТОЛЬКО ФОРМАТИРОВАНИЕ ==========
        // Убираем лишние пробелы и переносы, но СОХРАНЯЕМ порядок
        String normalized = body.replaceAll("\\s+", " ");

        // Убираем результат в конце (если есть)
        normalized = normalized.replaceAll("\\s+[0-9.-]+$", "").trim();
        normalized = normalized.replaceAll("\\s+\\*$", "").trim();

        // ========== НЕ УБИРАЕМ номера ходов, аннотации, комментарии ==========
        // Они остаются в строке

        if (normalized.isEmpty()) {
            return 0;
        }

        // ========== ИСПОЛЬЗУЕМ CRC32 ОТ НОРМАЛИЗОВАННОЙ СТРОКИ ==========
        // Порядок сохраняется, форматирование игнорируется
        return hashString(normalized);
    }
}