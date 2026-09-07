/*
 * Copyright (c) 2025-2026 Andrey Khrypach
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
 */

package Khrypach.Andrey.chess.kletka.pgn.index;

import Khrypach.Andrey.chess.kletka.pgn.index.binary.BinaryIndexConstants;
import Khrypach.Andrey.chess.kletka.pgn.index.binary.BinaryIndexReader;
import Khrypach.Andrey.chess.kletka.pgn.index.binary.BinaryIndexWriter;
import Khrypach.Andrey.chess.kletka.pgn.index.binary.LazyPgnIndex;
import Khrypach.Andrey.chess.kletka.pgn.index.model.GameIndexEntry;
import Khrypach.Andrey.chess.kletka.pgn.index.model.IndexStatus;
import Khrypach.Andrey.chess.kletka.pgn.index.model.PgnIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.zip.CRC32;

/**
 * Менеджер PGN индекса (бинарный формат)
 */
public class PgnIndexManager {
    private static final Logger log = LoggerFactory.getLogger(PgnIndexManager.class);

    private final BinaryIndexReader reader = new BinaryIndexReader();
    private final BinaryIndexWriter writer = new BinaryIndexWriter();

    /**
     * Получает путь к индексному файлу для PGN-файла
     */
    public Path getIndexPath(Path pgnPath) {
        String fileName = pgnPath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
        return pgnPath.getParent().resolve(baseName + BinaryIndexConstants.INDEX_EXTENSION);
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
     * Загружает бинарный индекс из файла
     */
    public PgnIndex loadIndex(Path pgnPath) throws IOException {
        Path indexPath = getIndexPath(pgnPath);
        log.info("Loading binary index from: {}", indexPath);

        if (!Files.exists(indexPath)) {
            throw new FileNotFoundException("Index file not found: " + indexPath);
        }

        return reader.read(indexPath);
    }

    /**
     * Загружает ЛЁГКИЙ индекс (для быстрого отображения)
     */
    public LazyPgnIndex loadLazyIndex(Path pgnPath) throws IOException {
        Path indexPath = getIndexPath(pgnPath);
        log.info("Loading lazy binary index from: {}", indexPath);

        if (!Files.exists(indexPath)) {
            throw new FileNotFoundException("Index file not found: " + indexPath);
        }

        BinaryIndexReader reader = new BinaryIndexReader();
        return reader.readLazy(indexPath);
    }

    /**
     * Сохраняет индекс в бинарный файл
     */
    public void saveIndex(Path pgnPath, PgnIndex index) throws IOException {
        log.info("Saving binary index for: {}", pgnPath);

        // Обновляем метаданные
        index.setFileSize(Files.size(pgnPath));
        index.setFileHash(computeFileHash(pgnPath));

        writer.write(index, getIndexPath(pgnPath));

        // Просто логируем, если старый индекс существует.
        Path oldJsonIndex = pgnPath.getParent().resolve(
                pgnPath.getFileName().toString().replaceAll("\\.[^.]+$", "") + ".idx"
        );
        if (Files.exists(oldJsonIndex)) {
            log.info("Old JSON index found: {}. User can delete it manually if not needed.", oldJsonIndex);
        }
    }

    /**
     * Удаляет индексный файл
     */
    public void deleteIndex(Path pgnPath) throws IOException {
        Path indexPath = getIndexPath(pgnPath);
        log.info("Deleting index: {}", indexPath);
        Files.deleteIfExists(indexPath);
    }

    /**
     * Вычисляет хеш PGN-файла
     */
    public String computeFileHash(Path pgnPath) throws IOException {
        CRC32 crc = new CRC32();
        byte[] buffer = new byte[8192];

        try (java.io.InputStream is = Files.newInputStream(pgnPath)) {
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                crc.update(buffer, 0, bytesRead);
            }
        }

        return String.format("%08x", crc.getValue());
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
                .entries(new ArrayList<>(entries))
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
}