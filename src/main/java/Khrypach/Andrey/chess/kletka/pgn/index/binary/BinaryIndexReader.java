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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Чтение бинарного индекса
 */
public class BinaryIndexReader {

    private static final Logger log = LoggerFactory.getLogger(BinaryIndexReader.class);

    /**
     * Читает ВСЕ записи в память (для совместимости)
     */
    public PgnIndex read(Path inputPath) throws IOException {
        log.debug("Reading full binary index from: {}", inputPath);

        // Читаем в память, чтобы не держать mmap (Windows блокирует move/delete)
        byte[] fileBytes;

        try (FileChannel channel = FileChannel.open(inputPath, StandardOpenOption.READ)) {
            long size = channel.size();
            if (size > Integer.MAX_VALUE) {
                throw new IOException("Index file too large: " + size);
            }
            fileBytes = new byte[(int) size];
            ByteBuffer buf = ByteBuffer.wrap(fileBytes);
            while (buf.hasRemaining()) {
                if (channel.read(buf) < 0) break;
            }

            ByteBuffer buffer = ByteBuffer.wrap(fileBytes);

            // Читаем заголовок
            int magic = buffer.getInt();
            if (magic != BinaryIndexConstants.MAGIC) {
                throw new IOException("Invalid magic number: 0x" + Integer.toHexString(magic));
            }

            byte version = buffer.get();
            if (version != BinaryIndexConstants.VERSION) {
                throw new IOException("Unsupported version: " + version);
            }

            // Reserved 3 байта
            buffer.get();
            buffer.get();
            buffer.get();

            int entryCount = buffer.getInt();
            int activeCount = buffer.getInt();
            long fileSize = buffer.getLong();

            byte[] hashBytes = new byte[8];
            buffer.get(hashBytes);
            String fileHash = new String(hashBytes, java.nio.charset.StandardCharsets.UTF_8);

            // ЧИТАЕМ timestamp
            long timestamp = buffer.getLong();  // Читаем timestamp (не пропускаем!)
            buffer.getLong(); // Reserved (пропускаем)

            // Читаем таблицу смещений
            long[] entryOffsets = new long[entryCount];
            for (int i = 0; i < entryCount; i++) {
                entryOffsets[i] = buffer.getLong();
            }

            long dataStart = buffer.position();

            // Читаем все записи
            List<GameIndexEntry> entries = new ArrayList<>(entryCount);
            for (int i = 0; i < entryCount; i++) {
                buffer.position((int) (dataStart + entryOffsets[i]));
                GameIndexEntry entry = readFullEntry(buffer);
                entries.add(entry);
            }

            PgnIndex index = PgnIndex.builder()
                    .version(PgnIndex.FORMAT_VERSION)
                    .fileHash(fileHash)
                    .fileSize(fileSize)
                    .gameCount(entryCount)
                    .activeCount(activeCount)
                    .entries(entries)
                    .build();

            index.refreshCache();

            log.debug("Full index read: {} entries, timestamp: {}", entries.size(), timestamp);
            return index;
        }
    }

    /**
     * Читает ЛЁГКИЙ индекс (только для отображения и поиска).
     * ВАЖНО: читает файл в память через FileChannel.read и закрывает канал.
     * НЕ использует mmap — на Windows mmap держит файл, и move/delete .klt
     * падает с AccessDeniedException, пока MappedByteBuffer не собран GC.
     */
    public LazyPgnIndex readLazy(Path inputPath) throws IOException {
        log.debug("Reading lazy binary index from: {}", inputPath);

        // ========== ЧИТАЕМ ФАЙЛ В ПАМЯТЬ, КАНАЛ ЗАКРЫВАЕМ ==========
        byte[] fileBytes;
        try (FileChannel channel = FileChannel.open(inputPath, StandardOpenOption.READ)) {
            long size = channel.size();
            if (size > Integer.MAX_VALUE) {
                throw new IOException("Index file too large: " + size + " bytes");
            }
            fileBytes = new byte[(int) size];
            ByteBuffer buf = ByteBuffer.wrap(fileBytes);
            while (buf.hasRemaining()) {
                if (channel.read(buf) < 0) {
                    break;
                }
            }
        }
        // Канал закрыт — на Windows файл больше не заблокирован

        ByteBuffer buffer = ByteBuffer.wrap(fileBytes);

        // ========== 1. ЗАГОЛОВОК ==========
        int magic = buffer.getInt();
        if (magic != BinaryIndexConstants.MAGIC) {
            throw new IOException("Invalid magic number: 0x" + Integer.toHexString(magic));
        }

        byte version = buffer.get();
        if (version != BinaryIndexConstants.VERSION) {
            throw new IOException("Unsupported version: " + version);
        }

        buffer.get();
        buffer.get();
        buffer.get();

        int entryCount = buffer.getInt();
        int activeCount = buffer.getInt();
        long fileSize = buffer.getLong();

        byte[] hashBytes = new byte[8];
        buffer.get(hashBytes);
        String fileHash = new String(hashBytes, java.nio.charset.StandardCharsets.UTF_8);

        long timestamp = buffer.getLong();
        buffer.getLong();

        BinaryIndexHeader header = new BinaryIndexHeader(
                magic, version, entryCount, activeCount, fileSize, fileHash, timestamp
        );

        // ========== 2. ТАБЛИЦА СМЕЩЕНИЙ ==========
        long[] entryOffsets = new long[entryCount];
        for (int i = 0; i < entryCount; i++) {
            entryOffsets[i] = buffer.getLong();
        }

        long dataStart = buffer.position();

        // ========== 3. ЛЁГКИЕ ЗАПИСИ ==========
        List<LightGameEntry> lightEntries = new ArrayList<>(entryCount);

        for (int i = 0; i < entryCount; i++) {
            buffer.position((int) (dataStart + entryOffsets[i]));

            int id = buffer.getInt();
            long offset = buffer.getLong();
            int length = buffer.getInt();
            int versionEntry = buffer.getInt();
            boolean deleted = buffer.get() == 1;
            int hash = buffer.getInt();

            String white = readString(buffer);
            String black = readString(buffer);
            String eco = readString(buffer);
            String result = readString(buffer);
            String year = readString(buffer);
            String event = readString(buffer);
            String site = readString(buffer);
            String opening = readString(buffer);
            String variation = readString(buffer);
            int plyCount = buffer.getInt();

            if (log.isTraceEnabled()) {
                log.trace("Reading entry: id={}, offset={}, length={}, version={}, deleted={}, plyCount={}",
                        id, offset, length, versionEntry, deleted, plyCount);
            }

            LightGameEntry light = new LightGameEntry(
                    id, white, black, result, year, event, eco, opening,
                    entryOffsets[i], length, deleted, hash
            );
            lightEntries.add(light);
        }

        log.debug("Lazy index read: {} light entries", lightEntries.size());

        // ========== channel = null — LazyPgnIndex не держит файл ==========
        return new LazyPgnIndex(header, null, buffer, dataStart, lightEntries);
    }

    /**
     * Потоковое чтение всех записей из .klt БЕЗ загрузки в память списка LightGameEntry.
     * Читает заголовок → таблицу offset'ов (entryCount × 8 байт) → записи БЛОКАМИ по 1 МБ.
     * Память: ~(entryCount × 8) + 1 МБ. На 11.7M партий ≈ 95 МБ.
     *
     * @param kltPath путь к .klt
     * @param consumer вызывается для КАЖДОЙ записи (включая deleted — фильтруйте сами)
     */
    public void streamEntries(Path kltPath, Consumer<GameIndexEntry> consumer) throws IOException {
        log.debug("Streaming binary index from: {}", kltPath);

        final int BLOCK_SIZE = 1 << 20; // 1 МБ

        try (FileChannel channel = FileChannel.open(kltPath, StandardOpenOption.READ)) {
            long fileSize = channel.size();

            // ========== 1. ЗАГОЛОВОК ==========
            ByteBuffer headerBuf = ByteBuffer.allocate(BinaryIndexConstants.HEADER_SIZE);
            readFully(channel, headerBuf);  // flip уже сделан

            int magic = headerBuf.getInt();
            if (magic != BinaryIndexConstants.MAGIC) {
                throw new IOException("Invalid magic: 0x" + Integer.toHexString(magic));
            }
            byte version = headerBuf.get();
            if (version != BinaryIndexConstants.VERSION) {
                throw new IOException("Unsupported version: " + version);
            }
            headerBuf.get(); headerBuf.get(); headerBuf.get();

            int entryCount = headerBuf.getInt();
            headerBuf.getInt();
            headerBuf.getLong();
            headerBuf.position(headerBuf.position() + 8); // hash
            headerBuf.getLong(); // timestamp
            headerBuf.getLong(); // reserved

            if (entryCount <= 0) {
                log.debug("Empty index, nothing to stream");
                return;
            }

            // ========== 2. ТАБЛИЦА OFFSET'ОВ ==========
            long offsetsBytes = (long) entryCount * 8L;
            if (offsetsBytes > Integer.MAX_VALUE - 16) {
                throw new IOException("Index too large for in-memory offset table: " + offsetsBytes);
            }
            ByteBuffer offsetsBuf = ByteBuffer.allocate((int) offsetsBytes);
            readFully(channel, offsetsBuf);  // flip уже сделан

            long dataStart = BinaryIndexConstants.HEADER_SIZE + offsetsBytes;

            // ========== 3. БЛОЧНОЕ ЧТЕНИЕ С ГАРАНТИЕЙ ЦЕЛОСТНОСТИ ЗАПИСИ ==========
            ByteBuffer blockBuf = ByteBuffer.allocate(BLOCK_SIZE);
            long blockStart = -1;
            int blockLength = 0;

            for (int i = 0; i < entryCount; i++) {
                long relOffset = offsetsBuf.getLong(i * 8);
                long entryOffset = dataStart + relOffset;

                // ===== ДЛИНА ЗАПИСИ: разница со следующим offset'ом =====
                long nextRelOffset = (i + 1 < entryCount)
                        ? offsetsBuf.getLong((i + 1) * 8)
                        : (fileSize - dataStart);
                long entryLength = nextRelOffset - relOffset;

                if (entryLength < 0 || entryLength > Integer.MAX_VALUE) {
                    throw new IOException(String.format(
                            "Invalid entry length at i=%d: %d (rel=%d, nextRel=%d)",
                            i, entryLength, relOffset, nextRelOffset));
                }

                // ===== ЗАПИСЬ ДОЛЖНА ЦЕЛИКОМ ВЛЕЗАТЬ В БЛОК =====
                boolean fits = (blockStart >= 0)
                        && (entryOffset >= blockStart)
                        && (entryOffset + entryLength <= blockStart + blockLength);

                if (!fits) {
                    blockBuf.clear();
                    long remaining = fileSize - entryOffset;
                    int toRead = (int) Math.min(Math.max((long) BLOCK_SIZE, entryLength), remaining);

                    channel.position(entryOffset);
                    int read = 0;
                    while (read < toRead && blockBuf.hasRemaining()) {
                        int n = channel.read(blockBuf);
                        if (n < 0) break;
                        read += n;
                    }
                    blockBuf.flip();
                    blockStart = entryOffset;
                    blockLength = read;

                    if (entryLength > blockLength) {
                        throw new IOException(String.format(
                                "Entry %d (len=%d) does not fit in block (read=%d) at offset %d",
                                i, entryLength, blockLength, entryOffset));
                    }
                }

                // ===== ПОЗИЦИОНИРУЕМСЯ И ЧИТАЕМ =====
                blockBuf.position((int) (entryOffset - blockStart));
                GameIndexEntry entry = readEntryFromBuffer(blockBuf);
                consumer.accept(entry);
            }
        }

        log.debug("Streaming completed");
    }

    /**
     * Быстро читает только заголовок .klt (48 байт) и возвращает количество партий.
     * Используется для инициализации прогресс-бара ДО начала обработки.
     */
    public int readEntryCount(Path kltPath) throws IOException {
        try (FileChannel channel = FileChannel.open(kltPath, StandardOpenOption.READ)) {
            ByteBuffer headerBuf = ByteBuffer.allocate(BinaryIndexConstants.HEADER_SIZE);
            readFully(channel, headerBuf);

            int magic = headerBuf.getInt();
            if (magic != BinaryIndexConstants.MAGIC) {
                throw new IOException("Invalid magic: 0x" + Integer.toHexString(magic));
            }
            byte version = headerBuf.get();
            if (version != BinaryIndexConstants.VERSION) {
                throw new IOException("Unsupported version: " + version);
            }
            headerBuf.get(); headerBuf.get(); headerBuf.get(); // reserved

            return headerBuf.getInt(); // entryCount
        }
    }

    /** Дочитывает буфер до конца (read может вернуть меньше, чем capacity). */
    private void readFully(FileChannel channel, ByteBuffer buf) throws IOException {
        while (buf.hasRemaining()) {
            if (channel.read(buf) < 0) {
                throw new IOException("Unexpected EOF while reading index");
            }
        }
        buf.flip();
    }

    /** Читает одну запись из позиционированного буфера. */
    private GameIndexEntry readEntryFromBuffer(ByteBuffer buffer) {
        int id = buffer.getInt();
        long offset = buffer.getLong();
        int length = buffer.getInt();
        int version = buffer.getInt();
        boolean deleted = buffer.get() == 1;
        int hash = buffer.getInt();

        String white = readString(buffer);
        String black = readString(buffer);
        String eco = readString(buffer);
        String result = readString(buffer);
        String year = readString(buffer);
        String event = readString(buffer);
        String site = readString(buffer);
        String opening = readString(buffer);
        String variation = readString(buffer);
        int plyCount = buffer.getInt();

        return GameIndexEntry.builder()
                .id(id).offset(offset).length(length).version(version)
                .deleted(deleted).hash(hash)
                .white(white).black(black).eco(eco).result(result)
                .year(year).event(event).site(site)
                .opening(opening).variation(variation).plyCount(plyCount)
                .build();
    }

    /**
     * Читает полную запись
     */
    private GameIndexEntry readFullEntry(ByteBuffer buffer) {
        int id = buffer.getInt();
        long offset = buffer.getLong();
        int length = buffer.getInt();
        int version = buffer.getInt();
        boolean deleted = buffer.get() == 1;
        int hash = buffer.getInt();

        String white = readString(buffer);
        String black = readString(buffer);
        String eco = readString(buffer);
        String result = readString(buffer);
        String year = readString(buffer);
        String event = readString(buffer);
        String site = readString(buffer);
        String opening = readString(buffer);
        String variation = readString(buffer);
        int plyCount = buffer.getInt();

        return GameIndexEntry.builder()
                .id(id)
                .offset(offset)
                .length(length)
                .version(version)
                .deleted(deleted)
                .hash(hash)
                .white(white)
                .black(black)
                .eco(eco)
                .result(result)
                .year(year)
                .event(event)
                .site(site)
                .opening(opening)
                .variation(variation)
                .plyCount(plyCount)
                .build();
    }

    /**
     * Читает строку из буфера
     */
    private String readString(ByteBuffer buffer) {
        short length = buffer.getShort();
        if (length == 0) return "";

        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }
}