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

package Khrypach.Andrey.chess.kletka.pgn.index;

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.pgn.index.model.IndexingProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

public class PgnFilePreparer {
    private static final Logger log = LoggerFactory.getLogger(PgnFilePreparer.class);
    private static final LanguageManager lang = LanguageManager.getInstance();

    Path prepareFile(Path pgnPath, Consumer<IndexingProgress> progressCallback) throws IOException {
        log.info("Starting file preparation: {}", pgnPath);

        if (!Files.exists(pgnPath)) {
            throw new IOException("PGN file not found: " + pgnPath);
        }

        byte[] fileContent = Files.readAllBytes(pgnPath);
        int fileSize = fileContent.length;

        byte[] eventPattern = "[Event \"".getBytes(StandardCharsets.UTF_8);

        List<Integer> gameStarts = new ArrayList<>();
        int pos = 0;
        while (pos < fileSize) {
            int start = indexOfBytes(fileContent, eventPattern, pos);
            if (start < 0) break;
            gameStarts.add(start);
            pos = start + 1;
        }

        log.debug("Found {} game starts", gameStarts.size());

        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .totalGames(gameStarts.size())
                    .processedGames(0)
                    .status(lang.get(PREPARE_STATUS_SCANNING))
                    .build());
        }

        List<ContentBlock> blocks = new ArrayList<>();
        int currentOffset = 0;
        int totalGames = gameStarts.size();
        int processed = 0;
        int garbageCount = 0;

        for (int i = 0; i < gameStarts.size(); i++) {
            int gameStart = gameStarts.get(i);
            int nextGameStart = (i + 1 < gameStarts.size()) ? gameStarts.get(i + 1) : fileSize;

            if (currentOffset < gameStart) {
                int length = gameStart - currentOffset;
                byte[] betweenBytes = new byte[length];
                System.arraycopy(fileContent, currentOffset, betweenBytes, 0, length);
                String betweenText = new String(betweenBytes, StandardCharsets.UTF_8);
                String trimmed = betweenText.trim();

                if (!trimmed.isEmpty() && !trimmed.startsWith("[")) {
                    blocks.add(ContentBlock.createGarbage(currentOffset, length, betweenText));
                    garbageCount++;
                    log.debug("Found garbage before game {} at offset {}, length {}",
                            i + 1, currentOffset, length);
                }
            }

            int gameEnd = findFirstResult(fileContent, gameStart, nextGameStart);
            if (gameEnd < 0 || gameEnd > nextGameStart) {
                gameEnd = nextGameStart;
            }

            int length = gameEnd - gameStart;
            byte[] gameBytes = new byte[length];
            System.arraycopy(fileContent, gameStart, gameBytes, 0, length);
            String gameContent = new String(gameBytes, StandardCharsets.UTF_8);

            boolean hasBody = checkGameBody(gameContent);
            blocks.add(ContentBlock.createGame(gameStart, length, gameContent, hasBody));

            currentOffset = gameEnd;
            processed++;

            if (progressCallback != null && processed % 10 == 0) {
                progressCallback.accept(IndexingProgress.builder()
                        .totalGames(totalGames)
                        .processedGames(processed)
                        .status(String.format(lang.get(PREPARE_STATUS_PROCESSED), processed, totalGames))
                        .build());
            }
        }

        if (currentOffset < fileSize) {
            int length = fileSize - currentOffset;
            byte[] remainingBytes = new byte[length];
            System.arraycopy(fileContent, currentOffset, remainingBytes, 0, length);
            String remainingText = new String(remainingBytes, StandardCharsets.UTF_8);
            String trimmed = remainingText.trim();

            if (!trimmed.isEmpty() && !trimmed.startsWith("[")) {
                blocks.add(ContentBlock.createGarbage(currentOffset, length, remainingText));
                garbageCount++;
                log.debug("Found garbage at end of file at offset {}, length {}",
                        currentOffset, length);
            }
        }

        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .totalGames(totalGames + garbageCount)
                    .processedGames(0)
                    .status(lang.get(PREPARE_STATUS_BUILDING))
                    .build());
        }

        StringBuilder result = new StringBuilder();
        int gameCount = 0;
        int garbageProcessed = 0;

        for (ContentBlock block : blocks) {
            if (block.isGarbage()) {
                String artificialGame = createArtificialGame(block.content());
                String processedPart = processGamePart(artificialGame, false);
                if (!result.isEmpty()) {
                    result.append("\n");
                }
                result.append(processedPart);
                garbageProcessed++;
            } else {
                String processedPart = processGamePart(block.content(), block.hasBody());
                if (!result.isEmpty()) {
                    result.append("\n");
                }
                result.append(processedPart);
                gameCount++;
            }

            if (progressCallback != null && (gameCount + garbageProcessed) % 5 == 0) {
                int total = blocks.size();
                int current = gameCount + garbageProcessed;
                progressCallback.accept(IndexingProgress.builder()
                        .totalGames(total)
                        .processedGames(current)
                        .status(String.format(lang.get(PREPARE_STATUS_BUILDING_BLOCKS), current, total))
                        .build());
            }
        }

        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .totalGames(blocks.size())
                    .processedGames(blocks.size())
                    .status(lang.get(PREPARE_STATUS_SAVING))
                    .build());
        }

        String finalResult = result.toString();
        if (!finalResult.endsWith("\n\n")) {
            if (finalResult.endsWith("\n")) {
                finalResult = finalResult + "\n";
            } else {
                finalResult = finalResult + "\n\n";
            }
        }

        Path tempPath = pgnPath.getParent().resolve(pgnPath.getFileName() + ".prepared");
        Files.writeString(tempPath, finalResult);

        Path backupPath = pgnPath.getParent().resolve(pgnPath.getFileName() + ".bak");
        Files.deleteIfExists(backupPath);
        Files.move(pgnPath, backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Files.move(tempPath, pgnPath);

        log.info("File preparation completed: {} games processed, {} garbage blocks converted to deleted games",
                gameCount, garbageCount);

        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .totalGames(gameCount + garbageCount)
                    .processedGames(gameCount + garbageCount)
                    .status(String.format(lang.get(PREPARE_STATUS_COMPLETE), gameCount, garbageCount))
                    .build());
        }

        return pgnPath;
    }

    /**
     * Находит ПЕРВЫЙ результат (1-0, 0-1, 1/2-1/2, *) в диапазоне
     */
    private int findFirstResult(byte[] fileBytes, int start, int end) {
        byte[][] resultPatterns = {
                "1-0".getBytes(StandardCharsets.UTF_8),
                "0-1".getBytes(StandardCharsets.UTF_8),
                "1/2-1/2".getBytes(StandardCharsets.UTF_8),
                "*".getBytes(StandardCharsets.UTF_8)
        };

        for (byte[] pattern : resultPatterns) {
            int index = indexOfBytes(fileBytes, pattern, start);
            while (index > 0 && index < end) {
                byte prevByte = fileBytes[index - 1];
                if (prevByte == ' ' || prevByte == '\n' || prevByte == '\r') {
                    int endOfLine = indexOfByte(fileBytes, (byte) '\n', index);
                    if (endOfLine < 0 || endOfLine >= end) {
                        endOfLine = end - 1;
                    }
                    int endOffset = Math.min(endOfLine + 1, end);

                    log.trace("Found result at index {}, endOffset={}", index, endOffset);
                    return endOffset;
                }
                index = indexOfBytes(fileBytes, pattern, index + 1);
            }
        }

        return -1;
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
     * Ищет байтовый паттерн в байтовом массиве
     */
    private int indexOfBytes(byte[] array, byte[] pattern, int start) {
        if (pattern.length == 0 || start >= array.length) return -1;
        int maxStart = array.length - pattern.length;
        for (int i = start; i <= maxStart; i++) {
            boolean found = true;
            for (int j = 0; j < pattern.length; j++) {
                if (array[i + j] != pattern[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }

    /**
     * Ищет один байт в байтовом массиве
     */
    private int indexOfByte(byte[] array, byte target, int start) {
        for (int i = start; i < array.length; i++) {
            if (array[i] == target) return i;
        }
        return -1;
    }

    /**
     * Обрабатывает партию: добавляет/исправляет [Deleted]
     */
    private String processGamePart(String gamePart, boolean hasBody) {
        String deletedValue = Boolean.toString(!hasBody);

        if (gamePart.contains("[Deleted")) {
            String result = gamePart.replaceAll(
                    "\\[Deleted\\s+\"[^\"]*\"]",
                    "[Deleted \"" + deletedValue + "\"]"
            );
            return ensureTrailingNewline(result);
        }

        String[] lines = gamePart.split("\n");
        StringBuilder result = new StringBuilder();
        boolean inserted = false;

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
                    result.append("[Deleted \"").append(deletedValue).append("\"]\n");
                    inserted = true;
                }
                continue;
            }

            if (line.trim().startsWith("[")) {
                result.append(line).append("\n");
                if (i + 1 < lines.length && lines[i + 1].trim().isEmpty()) {
                    if (!inserted) {
                        result.append("[Deleted \"").append(deletedValue).append("\"]\n");
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

        if (!inserted) {
            int lastHeaderIndex = -1;
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].trim().startsWith("[")) {
                    lastHeaderIndex = i;
                }
            }

            if (lastHeaderIndex >= 0) {
                StringBuilder newResult = new StringBuilder();
                for (int i = 0; i < lines.length; i++) {
                    newResult.append(lines[i]);
                    if (i == lastHeaderIndex) {
                        newResult.append("\n[Deleted \"").append(deletedValue).append("\"]");
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

    // ========== ВСПОМОГАТЕЛЬНЫЙ КЛАСС ==========

    private record ContentBlock(long offset, int length, String content, boolean isGarbage, boolean hasBody) {

        static ContentBlock createGame(long offset, int length, String content, boolean hasBody) {
                return new ContentBlock(offset, length, content, false, hasBody);
            }

            static ContentBlock createGarbage(long offset, int length, String content) {
                return new ContentBlock(offset, length, content, true, false);
            }
        }
}