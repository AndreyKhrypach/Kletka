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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
        log.debug("Detected encoding for {}: {}", pgnPath.getFileName(), encoding);

        if ("UTF-8".equals(encoding) || "UTF-8-BOM".equals(encoding)) {
            return pgnPath;
        }

        log.info("Converting {} from {} to UTF-8", pgnPath.getFileName(), encoding);

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

        // Атомарная замена с бэкапом
        Files.deleteIfExists(backupPath);
        Files.move(pgnPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        Files.move(tempPath, pgnPath);

        return pgnPath;
    }

    private static String detectEncoding(Path pgnPath) throws IOException {
        byte[] sample;
        try (InputStream is = Files.newInputStream(pgnPath)) {
            sample = is.readNBytes(64 * 1024);
        }

        if (sample.length >= 3
                && sample[0] == (byte) 0xEF
                && sample[1] == (byte) 0xBB
                && sample[2] == (byte) 0xBF) {
            return "UTF-8-BOM";
        }

        // Пробуем UTF-8
        try {
            String test = new String(sample, StandardCharsets.UTF_8);
            if (!test.contains("\uFFFD")) {
                if (containsCyrillic(test) || hasPgnHeaders(test)) {
                    return "UTF-8";
                }
            }
        } catch (Exception ignored) {}

        // Пробуем Windows-1251
        for (String charsetName : new String[]{"Windows-1251", "KOI8-R", "ISO-8859-5"}) {
            try {
                String test = new String(sample, Charset.forName(charsetName));
                if (containsCyrillic(test) && hasPgnHeaders(test)) {
                    return charsetName;
                }
            } catch (Exception ignored) {}
        }

        log.warn("Could not detect encoding for {}, using UTF-8 as fallback", pgnPath.getFileName());
        return "UTF-8";
    }

    private static boolean containsCyrillic(String text) {
        return text.codePoints().anyMatch(cp -> cp >= 0x0400 && cp <= 0x04FF);
    }

    private static boolean hasPgnHeaders(String text) {
        return text.contains("[Event") || text.contains("[White")
                || text.contains("[Black") || text.contains("[Result");
    }
}
