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

package Khrypach.Andrey.chess.kletka.pgn.index;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Конвертер PGN-файлов в UTF-8.
 * Определяет кодировку по первым 64 КБ файла и конвертирует при необходимости.
 * <p>
 * Используется перед индексацией (в {@code PgnFileIndexer}) и в {@code MainController}.
 */
public final class PgnEncodingConverter {

    private static final Logger log = LoggerFactory.getLogger(PgnEncodingConverter.class);

    private PgnEncodingConverter() {}

    /**
     * Проверяет кодировку файла и конвертирует в UTF-8, если нужно.
     * Если файл уже в UTF-8 — ничего не делает.
     *
     * @return тот же путь (файл может быть перезаписан в UTF-8)
     */
    public static Path ensureUtf8(Path pgnPath) throws IOException {
        String encoding = detectEncoding(pgnPath);

        if ("UTF-8".equals(encoding) || "UTF-8-BOM".equals(encoding)) {
            return pgnPath;
        }

        Path tempPath = pgnPath.resolveSibling(pgnPath.getFileName() + ".utf8.tmp");
        Path backupPath = pgnPath.resolveSibling(pgnPath.getFileName() + ".bak");

        try (BufferedReader reader = Files.newBufferedReader(pgnPath, Charset.forName(encoding));
             BufferedWriter writer = Files.newBufferedWriter(tempPath, StandardCharsets.UTF_8)) {
            char[] buf = new char[8192];
            int n;
            while ((n = reader.read(buf)) > 0) {
                writer.write(buf, 0, n);
            }
        }

        // ========== ПРОВЕРКА: файл теперь валидный UTF-8? ==========
        verifyUtf8(tempPath);

        // Атомарная замена с бэкапом
        Files.deleteIfExists(backupPath);
        Files.move(pgnPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        Files.move(tempPath, pgnPath);

        return pgnPath;
    }

    /**
     * Строгая проверка, что файл — валидный UTF-8 (с обрезкой хвоста 0..3 байта).
     */
    private static void verifyUtf8(Path path) throws IOException {
        byte[] sample;
        try (InputStream is = Files.newInputStream(path)) {
            sample = is.readNBytes(64 * 1024);
        }
        int maxTrim = Math.min(3, sample.length);
        for (int trim = 0; trim <= maxTrim; trim++) {
            int len = sample.length - trim;
            if (len <= 0) continue;
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            try {
                decoder.decode(ByteBuffer.wrap(sample, 0, len));
                return;  // ок
            } catch (CharacterCodingException ignored) {
                // пробуем с большим trim
            }
        }
        throw new IOException("Conversion failed: file is not valid UTF-8 after conversion: " + path);
    }

    private static String detectEncoding(Path pgnPath) throws IOException {
        byte[] sample;
        try (InputStream is = Files.newInputStream(pgnPath)) {
            sample = is.readNBytes(64 * 1024);
        }

        // ========== BOM ==========
        if (sample.length >= 3 && sample[0] == (byte)0xEF && sample[1] == (byte)0xBB && sample[2] == (byte)0xBF) {
            return "UTF-8-BOM";
        }
        if (sample.length >= 2 && sample[0] == (byte)0xFF && sample[1] == (byte)0xFE) {
            return "UTF-16LE";
        }
        if (sample.length >= 2 && sample[0] == (byte)0xFE && sample[1] == (byte)0xFF) {
            return "UTF-16BE";
        }

        // ========== UTF-8 (строгая валидация с trim) ==========
        int maxTrim = Math.min(3, sample.length);
        for (int trim = 0; trim <= maxTrim; trim++) {
            int len = sample.length - trim;
            if (len <= 0) continue;
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            try {
                decoder.decode(ByteBuffer.wrap(sample, 0, len));
                return "UTF-8";
            } catch (CharacterCodingException e) {
                log.debug("[ENC-DEBUG] UTF-8 failed at trim={}: {}", trim, e.getMessage());
            }
        }

        // ========== CP1251 (единственная авто-определяемая однобайтовая) ==========
        // KOI8-R и ISO-8859-5 в 2025 для PGN не встречаются.
        // Если пользователь столкнётся с ними — выберет вручную.
        String win1251 = new String(sample, Charset.forName("Windows-1251"));
        double score = scoreCyrillicPlausibility(win1251);
        if (score > 0) {
            return "Windows-1251";
        }

        // ========== Fallback: нет не-ASCII → UTF-8 ==========
        return "UTF-8";
    }

    /**
     * Оценка "правдоподобности" кириллического текста (0..1).
     * Чем выше — тем вероятнее, что это правильная кодировка.
     *
     * Логика:
     *  - Считаем только НЕ-ASCII символы (ASCII одинаков во всех кодировках).
     *  - cyrillic   — символы в блоке U+0400..U+04FF (русские буквы).
     *  - latin      — латинские буквы с диакритикой (U+00C0..U+00FF) и обычная латиница.
     *  - suspicious — символы, которых в русском PGN быть не должно
     *                 (валюты, геометрические фигуры, спецсимволы).
     *  - score = cyrRatio - latRatio - 2*susRatio
     *    Идеал: 100% кириллицы, 0% латиницы, 0% подозрительного → score ≈ 1.0
     */
    private static double scoreCyrillicPlausibility(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        int cyrillic = 0;
        int latin = 0;
        int suspicious = 0;
        int nonAscii = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // ASCII не считаем — он одинаков во всех однобайтовых кодировках
            if (c < 0x80) {
                continue;
            }
            nonAscii++;

            if (c >= 0x0400 && c <= 0x04FF) {
                // Кириллица (русские буквы, ё, украинские, и т.д.)
                cyrillic++;
            } else if ((c >= 0x0041 && c <= 0x005A) || (c >= 0x0061 && c <= 0x007A)) {
                // Обычная латиница (не должно быть в русском тексте после не-ASCII)
                latin++;
            } else if (c >= 0x00C0 && c <= 0x00FF) {
                // Латиница с диакритикой (À, Ð, Ñ, ×, ÷...) —
                // типичный признак НЕПРАВИЛЬНОЙ кодировки
                // (CP1251-байты, прочитанные как KOI8-R, дают такие символы)
                latin++;
            } else if (c == 0x2116) {
                // № — допустим в русском тексте
                cyrillic++;
            } else if (c >= 0x2500 && c <= 0x257F) {
                // Псевдографика (─│┌┐) — в PGN не должно быть
                suspicious++;
            } else if (c == 0x00A4 || c == 0x00A6 || c == 0x00A7
                    || c == 0x00A9 || c == 0x00AE || c == 0x00B0
                    || c == 0x00B1 || c == 0x00B5 || c == 0x00B6
                    || c == 0x00B7 || c == 0x00A8 || c == 0x00AF) {
                // ¤ ¦ § © ® ° ± µ ¶ · ¨ ¯ — типичные "мусорные" символы
                // при неправильной кодировке
                suspicious++;
            } else {
                // Всё остальное (эмодзи, стрелки, математика) —
                // в обычном PGN маловероятно, но не критично
                suspicious++;
            }
        }

        if (nonAscii == 0) {
            // Нет не-ASCII → это чистый ASCII, который валиден и в UTF-8, и в CP1251.
            // Возвращаем -1, чтобы detectEncoding НЕ выбрал эту кодировку.
            return -1;
        }

        double cyrRatio = (double) cyrillic / nonAscii;
        double latRatio = (double) latin / nonAscii;
        double susRatio = (double) suspicious / nonAscii;

        return cyrRatio - latRatio - 2.0 * susRatio;
    }

    private static boolean containsCyrillic(String text) {
        return text.codePoints().anyMatch(cp -> cp >= 0x0400 && cp <= 0x04FF);
    }

    private static boolean hasPgnHeaders(String text) {
        return text.contains("[Event") || text.contains("[White")
                || text.contains("[Black") || text.contains("[Result");
    }
}
