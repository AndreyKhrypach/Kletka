/*
 * Copyright (c) 2025 Andrey Khrypach
 * ...
 */

package Khrypach.Andrey.chess.kletka.pgn.index;

import Khrypach.Andrey.chess.kletka.pgn.index.model.GameIndexEntry;
import Khrypach.Andrey.chess.kletka.pgn.index.model.PgnIndex;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Редактор PGN-файла с использованием индекса.
 * Обеспечивает чтение и запись партий по индексу через RandomAccessFile.
 * ========== BATCH-РЕЖИМ ==========
 * Для массовых операций (вставка/удаление 100k+ партий) используйте:
 *   try (PgnFileEditor editor = new PgnFileEditor(pgnPath, index).batch()) {
 *       for (...) {
 *           editor.appendGame(pgn, id);       // ОДИН RandomAccessFile на весь цикл
 *           editor.readGame(entry);
 *       }
 *   }
 * Без batch() каждый вызов открывает/закрывает RandomAccessFile
 * (медленно на 100k+ операций).
 * ВАЖНО: batch-режим НЕ потокобезопасен. Все операции — в одном потоке.
 */
public class PgnFileEditor implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(PgnFileEditor.class);

    private final Path pgnPath;
    @Getter
    @Setter
    private PgnIndex index;

    // ========== BATCH-РЕЖИМ ==========
    private RandomAccessFile batchRaf;   // null = не в batch-режиме

    public PgnFileEditor(Path pgnPath, PgnIndex index) {
        this.pgnPath = pgnPath;
        this.index = index;
    }

    /**
     * Открывает RandomAccessFile и переводит редактор в batch-режим.
     * Все последующие операции используют один и тот же файл.
     * Закрыть через close() (try-with-resources).
     *
     * @return this (для try-with-resources)
     */
    public PgnFileEditor batch() throws IOException {
        if (batchRaf != null) {
            return this;  // уже в batch-режиме
        }
        this.batchRaf = new RandomAccessFile(pgnPath.toFile(), "rw");
        log.debug("PgnFileEditor: batch mode opened for {}", pgnPath);
        return this;
    }

    @Override
    public void close() {
        if (batchRaf != null) {
            try {
                batchRaf.close();
                log.debug("PgnFileEditor: batch mode closed for {}", pgnPath);
            } catch (IOException e) {
                log.warn("Failed to close batch RandomAccessFile: {}", e.getMessage());
            }
            batchRaf = null;
        }
    }

    private boolean isBatch() {
        return batchRaf != null;
    }

    // ========== ЧТЕНИЕ ==========

    public String readGame(GameIndexEntry entry) throws IOException {
        if (entry.isDeleted()) {
            throw new IllegalArgumentException("Game is deleted: " + entry.getId());
        }

        String result = readGame(entry.getOffset(), entry.getLength());
        log.trace("readGame - entry.getLength() = {}, result.length() = {}",
                entry.getLength(), result.length());
        return result;
    }

    public String readGame(long offset, int length) throws IOException {
        log.trace("Reading game at offset={}, length={}", offset, length);

        byte[] buffer = new byte[length];

        if (isBatch()) {
            batchRaf.seek(offset);
            batchRaf.readFully(buffer);
        } else {
            try (RandomAccessFile raf = new RandomAccessFile(pgnPath.toFile(), "r")) {
                raf.seek(offset);
                raf.readFully(buffer);
            }
        }

        return new String(buffer, StandardCharsets.UTF_8);
    }

    public String readBody(GameIndexEntry entry) throws IOException {
        if (entry.isDeleted()) {
            throw new IllegalArgumentException("Game is deleted: " + entry.getId());
        }

        byte[] gameBytes = new byte[entry.getLength()];

        if (isBatch()) {
            batchRaf.seek(entry.getOffset());
            batchRaf.readFully(gameBytes);
        } else {
            try (RandomAccessFile raf = new RandomAccessFile(pgnPath.toFile(), "r")) {
                raf.seek(entry.getOffset());
                raf.readFully(gameBytes);
            }
        }

        int lastBracket = -1;
        for (int i = 0; i < gameBytes.length; i++) {
            if (gameBytes[i] == ']') {
                lastBracket = i;
            }
        }

        if (lastBracket < 0) {
            return new String(gameBytes, StandardCharsets.UTF_8);
        }

        int bodyStart = -1;
        for (int i = lastBracket + 1; i < gameBytes.length; i++) {
            byte b = gameBytes[i];
            if (b != ' ' && b != '\n' && b != '\r' && b != '\t') {
                bodyStart = i;
                break;
            }
        }

        if (bodyStart < 0) {
            return "";
        }

        int bodyLength = gameBytes.length - bodyStart;
        return new String(gameBytes, bodyStart, bodyLength, StandardCharsets.UTF_8);
    }

    // ========== ЗАПИСЬ ==========

    public GameIndexEntry appendGame(String pgnContent, int newId) throws IOException {
        log.info("Appending new game with ID: {}", newId);

        byte[] contentBytes = pgnContent.getBytes(StandardCharsets.UTF_8);
        long offset;
        int length;

        if (isBatch()) {
            offset = batchRaf.length();
            batchRaf.seek(offset);
            batchRaf.write(contentBytes);
            length = contentBytes.length;
        } else {
            try (RandomAccessFile raf = new RandomAccessFile(pgnPath.toFile(), "rw")) {
                offset = raf.length();
                raf.seek(offset);
                raf.write(contentBytes);
                length = contentBytes.length;
            }
        }

        return GameIndexEntry.builder()
                .id(newId)
                .offset(offset)
                .length(length)
                .version(1)
                .deleted(false)
                .build();
    }

    public GameIndexEntry updateGame(int gameId, String newPgnContent) throws IOException {
        log.info("Updating game ID: {}", gameId);

        GameIndexEntry oldEntry = index.getEntryById(gameId);
        if (oldEntry == null) {
            throw new IllegalArgumentException("Game not found: " + gameId);
        }

        byte[] contentBytes = newPgnContent.getBytes(StandardCharsets.UTF_8);
        long newOffset;
        int newLength;

        if (isBatch()) {
            newOffset = batchRaf.length();
            batchRaf.seek(newOffset);
            batchRaf.write(contentBytes);
            newLength = contentBytes.length;
        } else {
            try (RandomAccessFile raf = new RandomAccessFile(pgnPath.toFile(), "rw")) {
                newOffset = raf.length();
                raf.seek(newOffset);
                raf.write(contentBytes);
                newLength = contentBytes.length;
            }
        }

        return GameIndexEntry.builder()
                .id(gameId)
                .offset(newOffset)
                .length(newLength)
                .version(oldEntry.getVersion() + 1)
                .deleted(false)
                .white(oldEntry.getWhite())
                .black(oldEntry.getBlack())
                .eco(oldEntry.getEco())
                .result(oldEntry.getResult())
                .year(oldEntry.getYear())
                .event(oldEntry.getEvent())
                .site(oldEntry.getSite())
                .opening(oldEntry.getOpening())
                .variation(oldEntry.getVariation())
                .plyCount(oldEntry.getPlyCount())
                .hash(oldEntry.getHash())
                .build();
    }

    public boolean replaceGameInPlace(GameIndexEntry entry, String newContent) {
        if (entry == null || newContent == null) {
            return false;
        }

        byte[] newBytes = newContent.getBytes(StandardCharsets.UTF_8);
        int newLength = newBytes.length;

        if (newLength != entry.getLength()) {
            log.warn("Length mismatch: old={}, new={}, cannot replace in place",
                    entry.getLength(), newLength);
            return false;
        }

        try {
            if (isBatch()) {
                batchRaf.seek(entry.getOffset());
                batchRaf.write(newBytes);
                log.debug("Replaced game {} in place at offset {}", entry.getId(), entry.getOffset());
                return true;
            }

            try (RandomAccessFile raf = new RandomAccessFile(pgnPath.toFile(), "rw")) {
                raf.seek(entry.getOffset());
                raf.write(newBytes);
                log.debug("Replaced game {} in place at offset {}", entry.getId(), entry.getOffset());
                return true;
            }
        } catch (IOException e) {
            log.error("Failed to replace game in place", e);
            return false;
        }
    }
}