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
import Khrypach.Andrey.chess.kletka.pgn.index.model.GameIndexEntry;
import Khrypach.Andrey.chess.kletka.pgn.index.model.IndexingProgress;
import Khrypach.Andrey.chess.kletka.pgn.index.model.PgnIndex;
import Khrypach.Andrey.chess.kletka.pgn.index.util.HashUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

/**
 * Перепаковщик PGN-файла.
 * Удаляет мёртвые версии партий, создаёт новый файл и перестраивает индекс.
 */
public class PgnRepacker {
    private static final Logger log = LoggerFactory.getLogger(PgnRepacker.class);
    private static final LanguageManager lang = LanguageManager.getInstance();

    private final PgnIndexManager indexManager;

    public PgnRepacker() {
        this.indexManager = new PgnIndexManager();
    }

    /**
     * Выполняет перепаковку PGN-файла.
     */
    public PgnIndex repack(Path pgnPath, PgnIndex index, Consumer<IndexingProgress> progressCallback) throws IOException {
        log.info("Starting repack of: {}", pgnPath);

        if (!Files.exists(pgnPath)) {
            throw new IOException("PGN file not found: " + pgnPath);
        }

        List<GameIndexEntry> deletedEntries = index.getDeletedEntries();
        if (deletedEntries.isEmpty()) {
            log.info("No deleted games, skipping");
            if (progressCallback != null) {
                progressCallback.accept(IndexingProgress.builder()
                        .status(lang.get(REPACK_STATUS_NO_DELETED))
                        .totalGames(index.getActiveCount())
                        .processedGames(index.getActiveCount())
                        .build());
            }
            return index;
        }

        List<GameIndexEntry> activeEntries = index.getActiveEntries();
        int totalActive = activeEntries.size();

        if (totalActive == 0) {
            log.warn("No active games, cannot repack");
            return index;
        }

        log.info("Found {} deleted games, {} active games", deletedEntries.size(), totalActive);

        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .totalGames(totalActive)
                    .processedGames(0)
                    .status(lang.get(REPACK_STATUS_READING))
                    .build());
        }

        StringBuilder newContent = new StringBuilder();
        List<GameIndexEntry> newEntries = new ArrayList<>();
        int processed = 0;

        try (RandomAccessFile sourceRaf = new RandomAccessFile(pgnPath.toFile(), "r")) {

            for (int i = 0; i < activeEntries.size(); i++) {
                GameIndexEntry entry = activeEntries.get(i);

                byte[] gameBytes = new byte[entry.getLength()];
                sourceRaf.seek(entry.getOffset());
                sourceRaf.readFully(gameBytes);
                String gameContent = new String(gameBytes, StandardCharsets.UTF_8);

                String cleanedContent = ensureDeletedFalse(gameContent);

                if (i > 0) {
                    newContent.append("\n");
                }

                String trimmedContent = cleanedContent.replaceAll("\n+$", "");
                newContent.append(trimmedContent).append("\n");

                long offset = 0;
                for (GameIndexEntry prevEntry : newEntries) {
                    offset += prevEntry.getLength();
                }
                offset += i;

                int length = trimmedContent.getBytes(StandardCharsets.UTF_8).length + 1;

                GameIndexEntry newEntry = GameIndexEntry.builder()
                        .id(entry.getId())
                        .offset(offset)
                        .length(length)
                        .version(1)
                        .deleted(false)
                        .hash(HashUtils.hashString(cleanedContent))
                        .white(entry.getWhite())
                        .black(entry.getBlack())
                        .eco(entry.getEco())
                        .result(entry.getResult())
                        .year(entry.getYear())
                        .event(entry.getEvent())
                        .site(entry.getSite())
                        .opening(entry.getOpening())
                        .variation(entry.getVariation())
                        .plyCount(entry.getPlyCount())
                        .build();

                newEntries.add(newEntry);
                processed++;

                if (progressCallback != null && processed % 10 == 0) {
                    progressCallback.accept(IndexingProgress.builder()
                            .totalGames(totalActive)
                            .processedGames(processed)
                            .status(String.format(lang.get(REPACK_STATUS_PROCESSED), processed, totalActive))
                            .build());
                }
            }
        }

        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .totalGames(totalActive)
                    .processedGames(totalActive)
                    .status(lang.get(REPACK_STATUS_WRITING))
                    .build());
        }

        Path tempPath = pgnPath.getParent().resolve(pgnPath.getFileName() + ".repack.tmp");

        byte[] newContentBytes = newContent.toString().getBytes(StandardCharsets.UTF_8);
        Files.write(tempPath, newContentBytes);

        log.info("Written {} bytes to temp file", newContentBytes.length);

        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .totalGames(totalActive)
                    .processedGames(totalActive)
                    .status(lang.get(REPACK_STATUS_CREATING_INDEX))
                    .build());
        }

        PgnIndex newIndex = PgnIndex.builder()
                .version(PgnIndex.FORMAT_VERSION)
                .fileHash(indexManager.computeFileHash(tempPath))
                .fileSize(Files.size(tempPath))
                .gameCount(newEntries.size())
                .activeCount(newEntries.size())
                .entries(newEntries)
                .build();

        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .totalGames(totalActive)
                    .processedGames(totalActive)
                    .status(lang.get(REPACK_STATUS_SAVING_INDEX))
                    .build());
        }

        try {
            indexManager.saveIndex(tempPath, newIndex);
            log.info("Saved index to temp file");
        } catch (IOException e) {
            log.error("Failed to save temp index", e);
            try { Files.deleteIfExists(tempPath); } catch (IOException ignored) {}
            throw new IOException("Failed to save temp index: " + e.getMessage(), e);
        }

        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .totalGames(totalActive)
                    .processedGames(totalActive)
                    .status(lang.get(REPACK_STATUS_REPLACING))
                    .build());
        }

        Path oldIndexPath = indexManager.getIndexPath(pgnPath);
        Path backupPgnPath = pgnPath.getParent().resolve(pgnPath.getFileName() + ".repack.bak.pgn");
        Path backupIndexPath = pgnPath.getParent().resolve(pgnPath.getFileName() + ".repack.bak.idx");

        try {
            log.info("Creating backups...");

            if (Files.exists(pgnPath)) {
                Files.move(pgnPath, backupPgnPath, StandardCopyOption.REPLACE_EXISTING);
                log.info("Backed up PGN to: {}", backupPgnPath);
            }

            if (Files.exists(oldIndexPath)) {
                Files.move(oldIndexPath, backupIndexPath, StandardCopyOption.REPLACE_EXISTING);
                log.info("Backed up index to: {}", backupIndexPath);
            }

            log.info("Moving temp files to final locations...");

            Files.move(tempPath, pgnPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Moved temp PGN to: {}", pgnPath);

            Path newIndexPath = indexManager.getIndexPath(tempPath);
            if (Files.exists(newIndexPath)) {
                Files.move(newIndexPath, oldIndexPath, StandardCopyOption.REPLACE_EXISTING);
                log.info("Moved temp index to: {}", oldIndexPath);
            } else {
                log.error("Temp index file not found: {}", newIndexPath);
                if (Files.exists(backupPgnPath)) {
                    Files.move(backupPgnPath, pgnPath, StandardCopyOption.REPLACE_EXISTING);
                }
                if (Files.exists(backupIndexPath)) {
                    Files.move(backupIndexPath, oldIndexPath, StandardCopyOption.REPLACE_EXISTING);
                }
                throw new IOException("Temp index file not found: " + newIndexPath);
            }

            try {
                Files.deleteIfExists(backupPgnPath);
                Files.deleteIfExists(backupIndexPath);
                log.info("Deleted backups");
            } catch (IOException e) {
                log.warn("Failed to delete backups: {}", e.getMessage());
            }

        } catch (IOException e) {
            log.error("Failed to replace files", e);
            try {
                if (Files.exists(backupPgnPath)) {
                    Files.move(backupPgnPath, pgnPath, StandardCopyOption.REPLACE_EXISTING);
                    log.info("Restored PGN from backup");
                }
                if (Files.exists(backupIndexPath)) {
                    Files.move(backupIndexPath, oldIndexPath, StandardCopyOption.REPLACE_EXISTING);
                    log.info("Restored index from backup");
                }
            } catch (IOException restoreError) {
                log.error("Failed to restore from backups", restoreError);
                throw new IOException("Failed to replace files and restore from backup: " + e.getMessage(), e);
            }
            throw e;
        }

        if (!Files.exists(pgnPath) || !Files.exists(oldIndexPath)) {
            log.error("Files missing after repack!");
            throw new IOException("Files missing after repack");
        }

        log.info("Repack completed successfully!");
        log.info("New file size: {} bytes", Files.size(pgnPath));
        log.info("New index: {}", newIndex);

        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .totalGames(totalActive)
                    .processedGames(totalActive)
                    .status(String.format(lang.get(REPACK_STATUS_COMPLETE),
                            totalActive, Files.size(pgnPath) / 1024.0))
                    .build());
        }

        return newIndex;
    }

    /**
     * Гарантирует, что тег [Deleted] установлен в "false"
     */
    private String ensureDeletedFalse(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        if (content.contains("[Deleted")) {
            return content.replaceAll(
                    "\\[Deleted\\s+\"[^\"]*\"]",
                    "[Deleted \"false\"]"
            );
        }

        int resultIndex = content.indexOf("[Result");
        if (resultIndex >= 0) {
            int endIndex = content.indexOf(']', resultIndex);
            if (endIndex > resultIndex) {
                return content.substring(0, endIndex + 1) + "\n[Deleted \"false\"]" +
                        content.substring(endIndex + 1);
            }
        }

        int firstBracket = content.indexOf('[');
        if (firstBracket >= 0) {
            return content.substring(0, firstBracket) + "[Deleted \"false\"]\n" +
                    content.substring(firstBracket);
        }

        return "[Deleted \"false\"]\n" + content;
    }

    // ========== МЕТОДЫ ДЛЯ ПРОВЕРКИ ==========

    public boolean hasDeletedGames(PgnIndex index) {
        if (index == null) return false;
        return index.getDeletedEntries() != null && !index.getDeletedEntries().isEmpty();
    }

    public double getGrowthRatio(PgnIndex index) {
        return index != null ? index.getGrowthRatio() : 1.0;
    }

    public int getDeletedCount(PgnIndex index) {
        if (index == null) return 0;
        return index.getGameCount() - index.getActiveCount();
    }

    public RepackStatus getRepackStatus(PgnIndex index) {
        if (index == null || index.getActiveCount() == 0) {
            return new RepackStatus(RepackLevel.OK, 0.0, lang.get(REPACK_DESC_NO_GAMES), false, 0);
        }

        double ratio = getGrowthRatio(index);
        int deletedCount = getDeletedCount(index);
        boolean hasDeleted = deletedCount > 0;

        RepackLevel level;
        String description;

        if (!hasDeleted) {
            level = RepackLevel.OK;
            description = lang.get(REPACK_DESC_NO_DELETED);
        } else if (ratio < 1.2) {
            level = RepackLevel.OK;
            description = String.format(lang.get(REPACK_DESC_HAS_DELETED), ratio);
        } else if (ratio < 1.5) {
            level = RepackLevel.WARNING;
            description = String.format(lang.get(REPACK_DESC_WARNING), ratio);
        } else {
            level = RepackLevel.CRITICAL;
            description = String.format(lang.get(REPACK_DESC_CRITICAL), ratio);
        }

        if (deletedCount > 0) {
            description += String.format(lang.get(REPACK_DESC_DELETED_COUNT), deletedCount);
        }

        return new RepackStatus(level, ratio, description, hasDeleted, deletedCount);
    }

    // ========== ВНУТРЕННИЙ КЛАСС СТАТУСА ==========

    public enum RepackLevel {
        OK,
        WARNING,
        CRITICAL
    }

    public record RepackStatus(RepackLevel level, double ratio, String description, boolean hasDeleted,
                               int deletedCount) {

        @Override
            public String toString() {
                return description;
            }
        }
}