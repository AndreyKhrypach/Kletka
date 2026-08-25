/*
 *
 *  * Copyright (c) 2024 Andrey Khrypach
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

package Khrypach.Andrey.chess.kletka.pgn.index.operation;

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.pgn.index.PgnFileEditor;
import Khrypach.Andrey.chess.kletka.pgn.index.PgnIndexManager;
import Khrypach.Andrey.chess.kletka.pgn.index.model.GameIndexEntry;
import Khrypach.Andrey.chess.kletka.pgn.index.model.PgnIndex;
import Khrypach.Andrey.chess.kletka.pgn.index.util.HashUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PgnBatchOperation {
    private static final Logger log = LoggerFactory.getLogger(PgnBatchOperation.class);
    private final LanguageManager lang = LanguageManager.getInstance();

    private final Path pgnPath;
    private final PgnIndex index;
    private final PgnFileEditor editor;
    private final PgnIndexManager indexManager;

    // Константы для пакетной обработки
    public static final int BATCH_SIZE = 500;
    public static final int MAX_BATCH_GAMES = 100000;

    public PgnBatchOperation(Path pgnPath, PgnIndex index) {
        this.pgnPath = pgnPath;
        this.index = index;
        this.editor = new PgnFileEditor(pgnPath, index);
        this.indexManager = new PgnIndexManager();
    }

    /**
     * Пакетное удаление партий по списку записей индекса
     */
    public BatchOperationResult deleteGamesBatch(List<GameIndexEntry> entries,
                                                 Consumer<Integer> progressCallback) throws IOException {
        log.info("Batch deleting {} games", entries.size());

        if (entries.size() > MAX_BATCH_GAMES) {
            throw new IllegalArgumentException(String.format(lang.get(LanguageKeys.PGN_BATCH_EXCEPTION_LIMIT), entries.size()));
        }

        int successful = 0;
        int failed = 0;
        List<Integer> failedIds = new ArrayList<>();

        // Разбиваем на чанки для производительности
        for (int i = 0; i < entries.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, entries.size());
            List<GameIndexEntry> chunk = entries.subList(i, end);

            for (GameIndexEntry entry : chunk) {
                try {
                    deleteGameInternal(entry);
                    successful++;
                } catch (Exception e) {
                    log.error("Failed to delete game {}: {}", entry.getId(), e.getMessage());
                    failed++;
                    failedIds.add(entry.getId());
                }
                if (progressCallback != null) {
                    progressCallback.accept(successful + failed);
                }
            }

            // Сохраняем индекс после каждого чанка
            if (!chunk.isEmpty()) {
                indexManager.saveIndex(pgnPath, index);
                index.refreshCache();
                log.info("Saved index after processing {} games", successful + failed);
            }
        }

        // Финальное сохранение
        indexManager.saveIndex(pgnPath, index);
        index.refreshCache();

        String message = String.format(lang.get(LanguageKeys.PGN_BROWSER_PASTE_PARTIAL), successful, entries.size(), failed);
        return new BatchOperationResult(entries.size(), successful, failed, failedIds, message);
    }

    /**
     * Пакетное удаление через маркировку [Deleted " true"]
     */
    private void deleteGameInternal(GameIndexEntry entry) throws IOException {
        if (entry.isDeleted()) {
            throw new IllegalArgumentException(String.format(lang.get(LanguageKeys.PGN_BATCH_EXCEPTION_ALREADY_DELETED), entry.getId()));
        }

        // ========== ПРОВЕРЯЕМ, ЧТО ЗАПИСЬ СУЩЕСТВУЕТ В ИНДЕКСЕ ==========
        GameIndexEntry existingEntry = index.getEntryById(entry.getId());
        if (existingEntry == null) {
            throw new IllegalArgumentException(String.format("Game with ID %d not found in index", entry.getId()));
        }

        // Используем существующую запись из индекса, а не переданную
        String pgnContent = editor.readGame(existingEntry);
        String updatedPgnContent = replaceDeletedTag(pgnContent);

        boolean replaced = editor.replaceGameInPlace(existingEntry, updatedPgnContent);

        GameIndexEntry deletedEntry;
        if (replaced) {
            deletedEntry = existingEntry.markDeleted();
            deletedEntry.setHash(HashUtils.hashString(updatedPgnContent));
        } else {
            GameIndexEntry newVersion = editor.updateGame(existingEntry.getId(), updatedPgnContent);
            deletedEntry = newVersion.markDeleted();
        }

        index.updateEntry(deletedEntry);
        index.refreshCache();
    }

    /**
     * Пакетная вставка партий из буфера
     */
    public BatchOperationResult pasteGamesBatch(List<String> pgnContents,
                                                Consumer<Integer> progressCallback) throws IOException {
        log.info("Batch pasting {} games", pgnContents.size());

        if (pgnContents.size() > MAX_BATCH_GAMES) {
            throw new IllegalArgumentException(String.format(lang.get(LanguageKeys.PGN_BATCH_EXCEPTION_LIMIT), pgnContents.size()));
        }

        int successful = 0;
        int failed = 0;
        List<Integer> failedIds = new ArrayList<>();

        for (int i = 0; i < pgnContents.size(); i++) {
            try {
                String pgnContent = pgnContents.get(i);

                // ========== ПРОВЕРКА ВАЛИДНОСТИ PGN ==========
                if (!isValidPgn(pgnContent)) {
                    throw new IllegalArgumentException("Invalid PGN format - missing required headers");
                }

                int newId = index.getNextId();
                GameIndexEntry newEntry = editor.appendGame(pgnContent, newId);
                updateHeaders(newEntry, pgnContent);
                index.addEntry(newEntry);
                successful++;
            } catch (Exception e) {
                log.error("Failed to paste game at index {}: {}", i, e.getMessage());
                failed++;
                failedIds.add(i);
            }
            if (progressCallback != null) {
                progressCallback.accept(i + 1);
            }

            // Сохраняем индекс после каждого чанка
            if ((i + 1) % BATCH_SIZE == 0) {
                indexManager.saveIndex(pgnPath, index);
                index.refreshCache();
                log.info("Saved index after {} games", i + 1);
            }
        }

        // Финальное сохранение
        indexManager.saveIndex(pgnPath, index);
        index.refreshCache();

        String message = String.format(lang.get(LanguageKeys.PGN_BROWSER_PASTE_PARTIAL), successful, pgnContents.size(), failed);
        return new BatchOperationResult(pgnContents.size(), successful, failed, failedIds, message);
    }

    /**
     * Проверяет, является ли строка валидным PGN
     */
    private boolean isValidPgn(String pgnContent) {
        if (pgnContent == null || pgnContent.trim().isEmpty()) {
            return false;
        }

        // Проверяем наличие обязательных заголовков
        boolean hasEvent = pgnContent.contains("[Event");
        boolean hasWhite = pgnContent.contains("[White");
        boolean hasBlack = pgnContent.contains("[Black");
        boolean hasResult = pgnContent.contains("[Result");

        // Также проверяем, что есть тело партии (ходы)
        boolean hasMoves = pgnContent.matches("(?s).*\\d+\\..*"); // содержит цифру с точкой (ход)

        return hasEvent && hasWhite && hasBlack && hasResult && hasMoves;
    }

    /**
     * Заменяет тег [Deleted] в PGN-строке
     */
    private String replaceDeletedTag(String pgnContent) {
        String newValue = " true";
        String newTag = "[Deleted \"" + newValue + "\"]";

        int deletedIndex = pgnContent.indexOf("[Deleted");
        if (deletedIndex >= 0) {
            int endIndex = pgnContent.indexOf(']', deletedIndex);
            if (endIndex > deletedIndex) {
                return pgnContent.substring(0, deletedIndex) + newTag +
                        pgnContent.substring(endIndex + 1);
            }
        }

        int resultIndex = pgnContent.indexOf("[Result");
        if (resultIndex >= 0) {
            int endIndex = pgnContent.indexOf(']', resultIndex);
            if (endIndex > resultIndex) {
                return pgnContent.substring(0, endIndex + 1) + "\n" + newTag +
                        pgnContent.substring(endIndex + 1);
            }
        }

        int firstBracket = pgnContent.indexOf('[');
        if (firstBracket >= 0) {
            return pgnContent.substring(0, firstBracket) + newTag + "\n" +
                    pgnContent.substring(firstBracket);
        }

        return newTag + "\n" + pgnContent;
    }

    private void updateHeaders(GameIndexEntry entry, String pgnContent) {
        try {
            // Парсим заголовки из PGN
            String[] lines = pgnContent.split("\n");
            String white = "?", black = "?", result = "*", event = "Kletka Game";
            String eco = "?", opening = "?", variation = "?", site = "?";
            String year = "";

            for (String line : lines) {
                if (line.startsWith("[White \"")) {
                    white = extractValue(line);
                } else if (line.startsWith("[Black \"")) {
                    black = extractValue(line);
                } else if (line.startsWith("[Result \"")) {
                    result = extractValue(line);
                } else if (line.startsWith("[Event \"")) {
                    event = extractValue(line);
                } else if (line.startsWith("[Site \"")) {
                    site = extractValue(line);
                } else if (line.startsWith("[ECO \"")) {
                    eco = extractValue(line);
                } else if (line.startsWith("[Opening \"")) {
                    opening = extractValue(line);
                } else if (line.startsWith("[Variation \"")) {
                    variation = extractValue(line);
                } else if (line.startsWith("[Date \"")) {
                    String dateStr = extractValue(line);
                    if (dateStr.length() >= 4) {
                        year = dateStr.substring(0, 4);
                    }
                }
            }

            entry.setWhite(white);
            entry.setBlack(black);
            entry.setResult(result);
            entry.setEvent(event);
            entry.setSite(site);
            entry.setEco(eco);
            entry.setOpening(opening);
            entry.setVariation(variation);
            entry.setYear(year);
            entry.setHash(HashUtils.hashString(pgnContent));
        } catch (Exception e) {
            log.warn("Failed to parse headers: {}", e.getMessage());
        }
    }

    private String extractValue(String line) {
        int start = line.indexOf('"');
        int end = line.lastIndexOf('"');
        if (start >= 0 && end > start) {
            return line.substring(start + 1, end);
        }
        return "?";
    }
}