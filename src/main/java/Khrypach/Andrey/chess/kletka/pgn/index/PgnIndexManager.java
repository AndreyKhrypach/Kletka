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

import Khrypach.Andrey.chess.kletka.pgn.index.model.GameIndexEntry;
import Khrypach.Andrey.chess.kletka.pgn.index.model.IndexStatus;
import Khrypach.Andrey.chess.kletka.pgn.index.model.PgnIndex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.CRC32;

/**
 * Менеджер PGN индекса.
 * Отвечает за загрузку, сохранение и проверку целостности индекса.
 */
public class PgnIndexManager {
    private static final Logger log = LoggerFactory.getLogger(PgnIndexManager.class);

    private static final String INDEX_EXTENSION = ".idx";
    private static final String INDEX_BACKUP_EXTENSION = ".idx.bak";

    private final ObjectMapper objectMapper;

    public PgnIndexManager() {
        this.objectMapper = JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
    }

    /**
     * Получает путь к индексному файлу для PGN-файла
     */
    public Path getIndexPath(Path pgnPath) {
        String fileName = pgnPath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
        return pgnPath.getParent().resolve(baseName + INDEX_EXTENSION);
    }

    /**
     * Получает путь к бэкап-файлу индекса
     */
    public Path getIndexBackupPath(Path pgnPath) {
        String fileName = pgnPath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
        return pgnPath.getParent().resolve(baseName + INDEX_BACKUP_EXTENSION);
    }

    /**
     * Проверяет состояние индекса для PGN-файла
     */
    public IndexStatus checkIndex(Path pgnPath) {
        log.info("Checking index for: {}", pgnPath);

        if (!Files.exists(pgnPath)) {
            log.warn("PGN file not found: {}", pgnPath);
            return IndexStatus.FILE_MISSING;
        }

        Path indexPath = getIndexPath(pgnPath);
        if (!Files.exists(indexPath)) {
            log.info("Index file not found: {}", indexPath);
            return IndexStatus.NO_INDEX;
        }

        try {
            PgnIndex index = loadIndex(pgnPath);
            if (index == null) {
                return IndexStatus.INDEX_CORRUPTED;
            }

            if (index.getVersion() != PgnIndex.FORMAT_VERSION) {
                log.warn("Unsupported index version: {}, expected: {}",
                        index.getVersion(), PgnIndex.FORMAT_VERSION);
                return IndexStatus.UNSUPPORTED_VERSION;
            }

            long currentSize = Files.size(pgnPath);
            if (index.getFileSize() != currentSize) {
                log.warn("File size mismatch: index={}, actual={}",
                        index.getFileSize(), currentSize);

                String currentHash = computeFileHash(pgnPath);
                if (!currentHash.equals(index.getFileHash())) {
                    log.warn("File hash mismatch");
                    return IndexStatus.FILE_CHANGED;
                }

                log.info("File hash matches, updating file size");
                return IndexStatus.OK;
            }

            String currentHash = computeFileHash(pgnPath);
            if (!currentHash.equals(index.getFileHash())) {
                log.warn("File hash mismatch");
                return IndexStatus.FILE_CHANGED;
            }

            log.info("Index is valid");
            return IndexStatus.OK;

        } catch (IOException e) {
            log.error("Error checking index: {}", e.getMessage(), e);
            return IndexStatus.INDEX_CORRUPTED;
        }
    }

    /**
     * Загружает индекс из файла
     */
    public PgnIndex loadIndex(Path pgnPath) throws IOException {
        Path indexPath = getIndexPath(pgnPath);
        log.info("Loading index from: {}", indexPath);

        if (!Files.exists(indexPath)) {
            throw new FileNotFoundException("Index file not found: " + indexPath);
        }

        try (InputStream is = Files.newInputStream(indexPath)) {
            PgnIndex index = objectMapper.readValue(is, PgnIndex.class);

            if (index.getEntries() == null) {
                throw new IOException("Index entries are null");
            }

            log.info("Loaded index: {}", index);
            return index;
        } catch (Exception e) {
            log.error("Failed to load index: {}", e.getMessage(), e);
            throw new IOException("Failed to load index: " + e.getMessage(), e);
        }
    }

    /**
     * Сохраняет индекс в файл
     */
    public void saveIndex(Path pgnPath, PgnIndex index) throws IOException {
        Path indexPath = getIndexPath(pgnPath);
        Path backupPath = getIndexBackupPath(pgnPath);

        log.info("Saving index to: {}", indexPath);

        index.setFileSize(Files.size(pgnPath));
        index.setFileHash(computeFileHash(pgnPath));

        if (Files.exists(indexPath)) {
            Files.copy(indexPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Created backup: {}", backupPath);
        }

        try (OutputStream os = Files.newOutputStream(indexPath)) {
            objectMapper.writeValue(os, index);
            log.info("Index saved successfully");
        } catch (Exception e) {
            log.error("Failed to save index: {}", e.getMessage(), e);

            if (Files.exists(backupPath)) {
                Files.copy(backupPath, indexPath, StandardCopyOption.REPLACE_EXISTING);
                log.info("Restored from backup");
            }
            throw new IOException("Failed to save index: " + e.getMessage(), e);
        }

        try {
            Files.deleteIfExists(backupPath);
            log.info("Deleted backup");
        } catch (IOException e) {
            log.warn("Failed to delete backup: {}", e.getMessage());
        }
    }

    /**
     * Вычисляет хеш PGN-файла
     */
    public String computeFileHash(Path pgnPath) throws IOException {
        CRC32 crc = new CRC32();
        byte[] buffer = new byte[8192];

        try (InputStream is = Files.newInputStream(pgnPath)) {
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                crc.update(buffer, 0, bytesRead);
            }
        }

        return Long.toHexString(crc.getValue());
    }

    /**
     * Создает новый индекс из списка записей
     */
    public PgnIndex createIndex(Path pgnPath, java.util.List<GameIndexEntry> entries) throws IOException {
        log.info("Creating new index with {} entries", entries.size());

        long fileSize = Files.size(pgnPath);
        String fileHash = computeFileHash(pgnPath);

        int activeCount = (int) entries.stream()
                .filter(e -> !e.isDeleted())
                .count();

        return PgnIndex.builder()
                .version(PgnIndex.FORMAT_VERSION)
                .fileHash(fileHash)
                .fileSize(fileSize)
                .gameCount(entries.size())
                .activeCount(activeCount)
                .entries(new java.util.ArrayList<>(entries))
                .build();
    }

    /**
     * Обновляет индекс после операции (добавление, редактирование, удаление)
     */
    public void updateIndex(Path pgnPath, PgnIndex index, GameIndexEntry entry) throws IOException {
        log.info("Updating index with entry: {}", entry);

        GameIndexEntry existing = index.getEntryById(entry.getId());
        if (existing != null) {
            index.updateEntry(entry);
        } else {
            index.addEntry(entry);
        }

        saveIndex(pgnPath, index);
    }

    /**
     * Удаляет индексный файл
     */
    public void deleteIndex(Path pgnPath) throws IOException {
        Path indexPath = getIndexPath(pgnPath);
        Path backupPath = getIndexBackupPath(pgnPath);

        log.info("Deleting index: {}", indexPath);

        Files.deleteIfExists(indexPath);
        Files.deleteIfExists(backupPath);
    }

}