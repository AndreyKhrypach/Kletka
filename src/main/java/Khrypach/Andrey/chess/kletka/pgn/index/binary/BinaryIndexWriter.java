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
 */

package Khrypach.Andrey.chess.kletka.pgn.index.binary;

import Khrypach.Andrey.chess.kletka.pgn.index.model.GameIndexEntry;
import Khrypach.Andrey.chess.kletka.pgn.index.model.PgnIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.List;

/**
 * Запись бинарного индекса
 */
public class BinaryIndexWriter {

    private static final Logger log = LoggerFactory.getLogger(BinaryIndexWriter.class);

    /**
     * Записывает индекс в бинарный файл
     */
    public void write(PgnIndex index, Path outputPath) throws IOException {
        if (index == null) {
            throw new IllegalArgumentException("Index cannot be null");
        }

        if (outputPath == null) {
            throw new IllegalArgumentException("Output path cannot be null");
        }

        log.debug("Writing binary index to: {}", outputPath);

        List<GameIndexEntry> entries = index.getEntries();
        if (entries == null) {
            throw new IllegalArgumentException("Index entries cannot be null");
        }

        int entryCount = entries.size();
        int activeCount = index.getActiveCount();

        if (outputPath.getParent() != null && !Files.exists(outputPath.getParent())) {
            Files.createDirectories(outputPath.getParent());
        }

        assert outputPath.getParent() != null;
        Path tempPath = outputPath.getParent().resolve(outputPath.getFileName() + ".tmp");

        try (FileOutputStream fos = new FileOutputStream(tempPath.toFile());
             BufferedOutputStream bos = new BufferedOutputStream(fos, 1 << 20);  // 1 MB
             DataOutputStream dos = new DataOutputStream(bos)) {

            // Заголовок
            BinaryIndexHeader header = BinaryIndexHeader.create(
                    entryCount, activeCount,
                    index.getFileSize(), index.getFileHash()
            );
            header.write(dos);

            // Таблица смещений (заглушки)
            long offsetTableStart = dos.size();
            long[] entryOffsets = new long[entryCount];
            for (int i = 0; i < entryCount; i++) {
                dos.writeLong(0);
            }

            // Записи
            long dataStart = dos.size();
            for (int i = 0; i < entryCount; i++) {
                entryOffsets[i] = dos.size() - dataStart;
                writeEntry(dos, entries.get(i));
            }

            // Возврат и заполнение таблицы смещений
            dos.flush();
            FileChannel channel = fos.getChannel();
            channel.position(offsetTableStart);

            for (int i = 0; i < entryCount; i++) {
                dos.writeLong(entryOffsets[i]);
            }
            dos.flush();

        } catch (IOException e) {
            Files.deleteIfExists(tempPath);
            throw e;
        }

        // ========== АТОМАРНАЯ ЗАМЕНА ==========
        // ATOMIC_MOVE + REPLACE_EXISTING: на POSIX и Windows работает атомарно,
        // нет окна, когда файла нет. Если ФС не поддерживает — фолбэк.
        try {
            Files.move(tempPath, outputPath,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            log.warn("Atomic move not supported, falling back: {}", e.getMessage());
            Files.move(tempPath, outputPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (AccessDeniedException e) {
            log.warn("Atomic move denied (file likely in use), trying delete+move: {}", e.getMessage());
            try {
                Files.deleteIfExists(outputPath);
            } catch (AccessDeniedException ex) {
                Files.deleteIfExists(tempPath);
                throw new IOException(
                        "Cannot replace index file because it is still in use: " + outputPath +
                                ". Close the PGN browser for this file and retry.", ex);
            }
            Files.move(tempPath, outputPath, StandardCopyOption.REPLACE_EXISTING);
        }

        log.debug("Binary index written: {} bytes", Files.size(outputPath));
    }

    /**
     * Записывает одну запись в поток
     */
    private void writeEntry(DataOutputStream dos, GameIndexEntry entry) throws IOException {
        dos.writeInt(entry.getId());
        dos.writeLong(entry.getOffset());
        dos.writeInt(entry.getLength());
        dos.writeInt(entry.getVersion());
        dos.writeByte(entry.isDeleted() ? 1 : 0);
        dos.writeInt(entry.getHash());

        writeString(dos, entry.getWhite());
        writeString(dos, entry.getBlack());
        writeString(dos, entry.getEco());
        writeString(dos, entry.getResult());
        writeString(dos, entry.getYear());
        writeString(dos, entry.getEvent());
        writeString(dos, entry.getSite());
        writeString(dos, entry.getOpening());
        writeString(dos, entry.getVariation());

        dos.writeInt(entry.getPlyCount());
    }

    /**
     * Записывает строку как short длина + UTF-8 байты
     */
    private void writeString(DataOutputStream dos, String value) throws IOException {
        if (value == null) {
            dos.writeShort(0);
            return;
        }

        byte[] bytes = value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (bytes.length > Short.MAX_VALUE) {
            throw new IOException("String too long: %d" + bytes.length);
        }
        dos.writeShort((short) bytes.length);
        dos.write(bytes);
    }
}