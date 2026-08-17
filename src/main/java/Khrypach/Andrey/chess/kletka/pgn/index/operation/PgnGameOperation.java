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

package Khrypach.Andrey.chess.kletka.pgn.index.operation;

import Khrypach.Andrey.chess.kletka.database.model.GameData;
import Khrypach.Andrey.chess.kletka.database.parser.PgnParser;
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

import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

/**
 * Сервис для выполнения операций над PGN-файлом с использованием индекса.
 * Реализует CRUD: Create, Read, Update, Delete + Duplicate.
 */
public class PgnGameOperation {
    private static final Logger log = LoggerFactory.getLogger(PgnGameOperation.class);
    private static final LanguageManager lang = LanguageManager.getInstance();

    private final Path pgnPath;
    private final PgnIndex index;
    private final PgnFileEditor editor;
    private final PgnIndexManager indexManager;
    private final PgnParser parser;

    public PgnGameOperation(Path pgnPath, PgnIndex index) {
        this.pgnPath = pgnPath;
        this.index = index;
        this.editor = new PgnFileEditor(pgnPath, index);
        this.indexManager = new PgnIndexManager();
        this.parser = new PgnParser();
    }

    /**
     * Удаляет партию.
     * Физически заменяет [Deleted "false"] на [Deleted " true"] в PGN-файле.
     */
    public OperationResult deleteGame(int gameId) throws IOException {
        log.info("Deleting game ID: {}", gameId);

        GameIndexEntry oldEntry = index.getActiveEntryById(gameId);
        if (oldEntry == null) {
            throw new IllegalArgumentException("Game not found or already deleted: " + gameId);
        }

        String pgnContent = editor.readGame(oldEntry);
        log.trace("Original content length: {} bytes", pgnContent.length());

        String updatedPgnContent = replaceDeletedTag(pgnContent, true);
        log.trace("Updated content length: {} bytes", updatedPgnContent.length());

        boolean replaced = editor.replaceGameInPlace(oldEntry, updatedPgnContent);

        GameIndexEntry deletedEntry;
        if (replaced) {
            log.trace("Successfully replaced in place at offset {}", oldEntry.getOffset());

            deletedEntry = oldEntry.markDeleted();
            deletedEntry.setHash(HashUtils.hashString(updatedPgnContent));

        } else {
            log.warn("In-place replace failed (length mismatch), appending new version");

            GameIndexEntry newVersion = editor.updateGame(gameId, updatedPgnContent);
            deletedEntry = newVersion.markDeleted();
        }

        indexManager.updateIndex(pgnPath, index, deletedEntry);
        index.refreshCache();

        log.info("Game {} marked as deleted", gameId);

        return new OperationResult(
                OperationType.DELETE,
                gameId,
                oldEntry,
                deletedEntry,
                String.format(lang.get(PGN_OP_DELETE_SUCCESS), gameId)
        );
    }

    /**
     * Заменяет тег [Deleted] в PGN-строке
     */
    private String replaceDeletedTag(String pgnContent, boolean deleted) {
        String newValue = deleted ? " true" : "false";
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

    /**
     * Добавляет новую партию в конец файла.
     */
    public OperationResult addGame(String pgnContent) throws IllegalArgumentException {
        log.info("Adding new game");

        try {
            GameData gameData = parser.parse(pgnContent);
            if (gameData == null ||
                    gameData.whitePlayer() == null ||
                    gameData.whitePlayer().equals("?")) {
                throw new IllegalArgumentException("Invalid PGN content");
            }

            int newId = index.getNextId();
            GameIndexEntry newEntry = editor.appendGame(pgnContent, newId);
            updateHeaders(newEntry, pgnContent);

            recalculateBodyOffset(newEntry, pgnContent);

            index.addEntry(newEntry);
            indexManager.saveIndex(pgnPath, index);
            index.refreshCache();

            return new OperationResult(
                    OperationType.ADD,
                    newId,
                    null,
                    newEntry,
                    String.format(lang.get(PGN_OP_ADD_SUCCESS), newId)
            );
        }catch (Exception e) {
            throw new IllegalArgumentException("Invalid PGN content: " + e.getMessage(), e);
        }
    }

    /**
     * Пересчитывает bodyOffset и bodyLength для записи
     */
    private void recalculateBodyOffset(GameIndexEntry entry, String pgnContent) {
        if (entry == null || pgnContent == null || pgnContent.isEmpty()) {
            return;
        }

        int lastBracket = pgnContent.lastIndexOf(']');
        if (lastBracket < 0) {
            return;
        }

        int bodyStart = -1;
        for (int i = lastBracket + 1; i < pgnContent.length() - 1; i++) {
            if (pgnContent.charAt(i) == '\n' && pgnContent.charAt(i + 1) == '\n') {
                bodyStart = i + 2;
                break;
            }
        }

        if (bodyStart < 0) {
            for (int i = lastBracket + 1; i < pgnContent.length(); i++) {
                char c = pgnContent.charAt(i);
                if (c != ' ' && c != '\n' && c != '\r' && c != '\t') {
                    bodyStart = i;
                    break;
                }
            }
        }

        if (bodyStart < 0) {
            return;
        }

        long bodyOffset = entry.getOffset() + bodyStart;
        int bodyLength = entry.getLength() - bodyStart;

        log.trace("Recalculated bodyOffset={}, bodyLength={}", bodyOffset, bodyLength);
    }

    /**
     * Дублирует существующую партию.
     */
    public OperationResult duplicateGame(int gameId) throws IOException {
        log.info("Duplicating game ID: {}", gameId);

        GameIndexEntry sourceEntry = index.getActiveEntryById(gameId);
        if (sourceEntry == null) {
            throw new IllegalArgumentException("Game not found or deleted: " + gameId);
        }

        String pgnContent = editor.readGame(sourceEntry);

        int newId = index.getNextId();
        GameIndexEntry newEntry = editor.appendGame(pgnContent, newId);

        newEntry.setWhite(sourceEntry.getWhite());
        newEntry.setBlack(sourceEntry.getBlack());
        newEntry.setEco(sourceEntry.getEco());
        newEntry.setResult(sourceEntry.getResult());
        newEntry.setYear(sourceEntry.getYear());
        newEntry.setEvent(sourceEntry.getEvent());
        newEntry.setSite(sourceEntry.getSite());
        newEntry.setOpening(sourceEntry.getOpening());
        newEntry.setVariation(sourceEntry.getVariation());
        newEntry.setPlyCount(sourceEntry.getPlyCount());
        newEntry.setHash(HashUtils.hashString(pgnContent));

        index.addEntry(newEntry);
        indexManager.saveIndex(pgnPath, index);
        index.refreshCache();

        log.info("Game {} duplicated as ID: {}", gameId, newId);

        return new OperationResult(
                OperationType.DUPLICATE,
                newId,
                sourceEntry,
                newEntry,
                String.format(lang.get(PGN_OP_DUPLICATE_SUCCESS), gameId, newId)
        );
    }

    private void updateHeaders(GameIndexEntry entry, String pgnContent) {
        try {
            GameData gameData = parser.parse(pgnContent);
            if (gameData != null) {
                entry.setWhite(gameData.whitePlayer());
                entry.setBlack(gameData.blackPlayer());
                entry.setEco(gameData.eco());
                entry.setResult(gameData.result());
                entry.setYear(gameData.date() != null ?
                        String.valueOf(gameData.date().getYear()) : "");
                entry.setEvent(gameData.event());
                entry.setSite(gameData.site());
                entry.setOpening(gameData.opening());
                entry.setVariation(gameData.variation());
                entry.setPlyCount(parsePlyCount(gameData.plyCount()));
            }
        } catch (Exception e) {
            log.warn("Failed to parse headers for entry {}: {}", entry.getId(), e.getMessage());
        }
        entry.setHash(HashUtils.hashString(pgnContent));
    }

    private int parsePlyCount(String plyCount) {
        if (plyCount == null || plyCount.isEmpty()) return 0;
        try {
            return Integer.parseInt(plyCount);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ КЛАССЫ ==========

    public enum OperationType {
        EDIT,
        DELETE,
        ADD,
        DUPLICATE
    }

    public record OperationResult(OperationType type, int gameId, GameIndexEntry oldEntry, GameIndexEntry newEntry,
                                  String message) {

        @Override
            public String toString() {
                return String.format("%s [id=%d] %s", type, gameId, message);
            }
        }
}