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

/**
 * Чтение бинарного индекса
 */
public class BinaryIndexReader {

    private static final Logger log = LoggerFactory.getLogger(BinaryIndexReader.class);

    /**
     * Читает ВСЕ записи в память (для совместимости)
     */
    public PgnIndex read(Path inputPath) throws IOException {
        log.info("Reading full binary index from: {}", inputPath);

        try (FileChannel channel = FileChannel.open(inputPath, StandardOpenOption.READ)) {
            ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());

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

            log.info("Full index read: {} entries, timestamp: {}", entries.size(), timestamp);
            return index;
        }
    }

    /**
     * Читает ЛЁГКИЙ индекс (только для отображения и поиска)
     */
    public LazyPgnIndex readLazy(Path inputPath) throws IOException {
        log.info("Reading lazy binary index from: {}", inputPath);

        FileChannel channel = FileChannel.open(inputPath, StandardOpenOption.READ);
        ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());

        // ========== 1. ЧИТАЕМ ЗАГОЛОВОК ==========
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

        long timestamp = buffer.getLong();
        buffer.getLong(); // Reserved

        BinaryIndexHeader header = new BinaryIndexHeader(
                magic,
                version,
                entryCount,
                activeCount,
                fileSize,
                fileHash,
                timestamp
        );

        // ========== 2. ЧИТАЕМ ТАБЛИЦУ СМЕЩЕНИЙ ==========
        long[] entryOffsets = new long[entryCount];
        for (int i = 0; i < entryCount; i++) {
            entryOffsets[i] = buffer.getLong();
        }

        long dataStart = buffer.position();

        // ========== 3. ЧИТАЕМ ЛЁГКИЕ ЗАПИСИ ==========
        List<LightGameEntry> lightEntries = new ArrayList<>(entryCount);

        for (int i = 0; i < entryCount; i++) {
            buffer.position((int) (dataStart + entryOffsets[i]));

            int id = buffer.getInt();
            // ========== ИСПРАВЛЕНО: читаем все поля, даже если не используем ==========
            long offset = buffer.getLong();      // Нужно прочитать для правильного позиционирования
            int length = buffer.getInt();
            int versionEntry = buffer.getInt();  // Нужно прочитать
            boolean deleted = buffer.get() == 1;
            int hash = buffer.getInt();

            String white = readString(buffer);
            String black = readString(buffer);
            String eco = readString(buffer);
            String result = readString(buffer);
            String year = readString(buffer);
            String event = readString(buffer);
            @SuppressWarnings("unused")
            String site = readString(buffer);    // Нужно прочитать
            String opening = readString(buffer);
            @SuppressWarnings("unused")
            String variation = readString(buffer); // Нужно прочитать
            int plyCount = buffer.getInt();      // Нужно прочитать

            // ========== ИСПРАВЛЕНО: добавляем @SuppressWarnings или используем переменные ==========
            // Можно добавить аннотацию, чтобы убрать предупреждения
            // или использовать переменные в логе при необходимости
            if (log.isTraceEnabled()) {
                log.trace("Reading entry: id={}, offset={}, length={}, version={}, deleted={}, plyCount={}",
                        id, offset, length, versionEntry, deleted, plyCount);
            }

            LightGameEntry light = new LightGameEntry(
                    id,
                    white,
                    black,
                    result,
                    year,
                    event,
                    eco,
                    opening,
                    entryOffsets[i],
                    length,
                    deleted,
                    hash
            );
            lightEntries.add(light);
        }

        log.info("Lazy index read: {} light entries", lightEntries.size());

        return new LazyPgnIndex(header, channel, buffer, dataStart, lightEntries);
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