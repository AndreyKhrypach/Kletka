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

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.pgn.index.model.IndexingProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

public class PgnFilePreparer {
    private static final Logger log = LoggerFactory.getLogger(PgnFilePreparer.class);
    private static final LanguageManager lang = LanguageManager.getInstance();

    /**
     * Размер буфера для BufferedReader / BufferedWriter.
     * 64 KB достаточно, чтобы минимизировать системные вызовы.
     */
    private static final int BUFFER_SIZE = 64 * 1024;

    /**
     * Размер буфера для одной партии (чтобы не аллоцировать заново).
     * Большинство партий укладывается в 8-16 KB.
     */
    private static final int GAME_BUFFER_INITIAL_SIZE = 16 * 1024;

    Path prepareFile(Path pgnPath, Consumer<IndexingProgress> progressCallback) throws IOException {
        log.info("Starting file preparation: {}", pgnPath);

        if (!Files.exists(pgnPath)) {
            throw new IOException("PGN file not found: " + pgnPath);
        }

        Path tempPath = pgnPath.getParent().resolve(pgnPath.getFileName() + ".prepared");
        Path backupPath = pgnPath.getParent().resolve(pgnPath.getFileName() + ".bak");

        long fileSize = Files.size(pgnPath);
        log.debug("File size: {} bytes", fileSize);

        // ========== ПРОГРЕСС: НАЧАЛО ==========
        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .totalGames(0)
                    .processedGames(0)
                    .status(lang.get(PREPARE_STATUS_SCANNING))
                    .build());
        }

        // ========== СЧЁТЧИКИ ==========
        int gameCount = 0;
        int garbageCount = 0;
        long bytesRead = 0;
        int lastProgressPercent = -1;

        StringBuilder gameBuffer = new StringBuilder(GAME_BUFFER_INITIAL_SIZE);
        boolean inGame = false;

        // ========== ОТКРЫВАЕМ ПОТОКИ ==========
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(pgnPath), StandardCharsets.UTF_8),
                BUFFER_SIZE);
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(Files.newOutputStream(tempPath), StandardCharsets.UTF_8),
                     BUFFER_SIZE)) {

            String line;
            while ((line = reader.readLine()) != null) {
                bytesRead += line.length() + 1; // +1 за '\n'

                // ========== НАЧАЛО НОВОЙ ПАРТИИ ==========
                if (line.startsWith("[Event ")) {
                    // Если была предыдущая партия — записываем её
                    if (inGame && !gameBuffer.isEmpty()) {
                        String gameContent = gameBuffer.toString();
                        boolean hasBody = checkGameBody(gameContent);
                        String processed = processGamePart(gameContent, hasBody);
                        writer.write(processed);
                        writer.write('\n');
                        gameCount++;
                    }

                    // Начинаем новую партию
                    gameBuffer.setLength(0);
                    inGame = true;
                }

                if (inGame) {
                    gameBuffer.append(line).append('\n');
                } else {
                    // ========== МУСОР ДО ПЕРВОЙ ПАРТИИ ==========
                    // Если строка не пустая и не заголовок — это мусор
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("[")) {
                        // Создаём искусственную партию из мусора
                        String artificialGame = createArtificialGame(trimmed);
                        String processed = processGamePart(artificialGame, false);
                        writer.write(processed);
                        writer.write('\n');
                        garbageCount++;
                    }
                }

                // ========== ПРОГРЕСС (каждые 5%) ==========
                if (progressCallback != null && fileSize > 0) {
                    int percent = (int) (bytesRead * 100 / fileSize);
                    if (percent >= lastProgressPercent + 5) {
                        lastProgressPercent = percent;
                        progressCallback.accept(IndexingProgress.builder()
                                .totalGames(gameCount + garbageCount)
                                .processedGames(gameCount + garbageCount)
                                .bytesProcessed(bytesRead)
                                .totalBytes(fileSize)
                                .status(String.format(lang.get(PREPARE_STATUS_PROCESSED),
                                        gameCount, gameCount + garbageCount))
                                .build());
                    }
                }
            }

            // ========== ФИНАЛЬНАЯ ПАРТИЯ ==========
            if (inGame && gameBuffer.length() > 0) {
                String gameContent = gameBuffer.toString();
                boolean hasBody = checkGameBody(gameContent);
                String processed = processGamePart(gameContent, hasBody);
                writer.write(processed);
                writer.write('\n');
                gameCount++;
            }
        }

        log.info("File preparation completed: {} games processed, {} garbage blocks converted to deleted games",
                gameCount, garbageCount);

        // ========== ПРОГРЕСС: СОХРАНЕНИЕ ==========
        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .totalGames(gameCount + garbageCount)
                    .processedGames(gameCount + garbageCount)
                    .status(lang.get(PREPARE_STATUS_SAVING))
                    .build());
        }

        // ========== ЗАМЕНА ФАЙЛА ==========
        Files.deleteIfExists(backupPath);
        Files.move(pgnPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        Files.move(tempPath, pgnPath);

        // ========== ПРОГРЕСС: ЗАВЕРШЕНО ==========
        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .totalGames(gameCount + garbageCount)
                    .processedGames(gameCount + garbageCount)
                    .status(String.format(lang.get(PREPARE_STATUS_COMPLETE),
                            gameCount, garbageCount))
                    .build());
        }

        return pgnPath;
    }

    /**
     * Создает искусственную партию из "мусорного" текста
     */
    private String createArtificialGame(String body) {
        return "[Event \"Unknown\"]\n" +
                "[Site \"?\"]\n" +
                "[Date \"????.??.??\"]\n" +
                "[Round \"?\"]\n" +
                "[White \"?\"]\n" +
                "[Black \"?\"]\n" +
                "[Result \"*\"]\n" +
                "[Deleted \"true\"]\n" +
                "\n" +
                body.trim() + "\n";
    }

    /**
     * Проверяет наличие тела в партии (заголовки + тело)
     */
    private boolean checkGameBody(String gamePart) {
        if (gamePart == null || gamePart.isEmpty()) {
            return false;
        }

        int lastBracket = gamePart.lastIndexOf(']');
        if (lastBracket < 0) {
            return false;
        }

        String afterHeaders = gamePart.substring(lastBracket + 1);
        String cleaned = afterHeaders.replaceAll("\\s+", "");

        if (!cleaned.isEmpty()) {
            return !cleaned.startsWith("[");
        }

        return false;
    }

    /**
     * Обрабатывает партию: добавляет/исправляет [Deleted].
     * ВАЖНО: если [Deleted] уже есть — оставляем как есть.
     * Это метаданные приложения (append-only): удалённая партия имеет
     * [Deleted " true"] и не должна "воскресать" при переиндексации.
     * Если тега нет — добавляем на основе hasBody:
     *   hasBody = true  → [Deleted "false"] (активная)
     *   hasBody = false → [Deleted "true"]  (мусор / партия без ходов)
     */
    private String processGamePart(String gamePart, boolean hasBody) {
        // ========== ТЕГ УЖЕ ЕСТЬ — НЕ ТРОГАЕМ ==========
        if (gamePart.contains("[Deleted")) {
            return ensureTrailingNewline(gamePart);
        }

        // ========== ТЕГА НЕТ — ДОБАВЛЯЕМ ==========
        String deletedValue = Boolean.toString(!hasBody);
        String newTag = "[Deleted \"" + deletedValue + "\"]";

        String[] lines = gamePart.split("\n");
        StringBuilder result = new StringBuilder(gamePart.length() + 32);
        boolean inserted = false;

        // Ищем [Result], после него вставляем [Deleted]
        int resultIndex = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().startsWith("[Result")) {
                resultIndex = i;
                break;
            }
        }

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (i == resultIndex) {
                result.append(line).append("\n");
                if (!inserted) {
                    result.append(newTag).append("\n");
                    inserted = true;
                }
                continue;
            }

            if (line.trim().startsWith("[")) {
                result.append(line).append("\n");
                if (i + 1 < lines.length && lines[i + 1].trim().isEmpty()) {
                    if (!inserted) {
                        result.append(newTag).append("\n");
                        inserted = true;
                    }
                }
                continue;
            }

            result.append(line);
            if (i < lines.length - 1) {
                result.append("\n");
            }
        }

        // Fallback: если [Result] не найден — вставляем после последнего заголовка
        if (!inserted) {
            int lastHeaderIndex = -1;
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].trim().startsWith("[")) {
                    lastHeaderIndex = i;
                }
            }

            if (lastHeaderIndex >= 0) {
                StringBuilder newResult = new StringBuilder(gamePart.length() + 32);
                for (int i = 0; i < lines.length; i++) {
                    newResult.append(lines[i]);
                    if (i == lastHeaderIndex) {
                        newResult.append("\n").append(newTag);
                    }
                    if (i < lines.length - 1) {
                        newResult.append("\n");
                    }
                }
                return ensureTrailingNewline(newResult.toString());
            }
        }

        return ensureTrailingNewline(result.toString());
    }

    /**
     * Гарантирует наличие пустой строки в конце партии
     */
    private String ensureTrailingNewline(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        String trimmed = content.replaceAll("\\s+$", "");
        return trimmed + "\n";
    }
}