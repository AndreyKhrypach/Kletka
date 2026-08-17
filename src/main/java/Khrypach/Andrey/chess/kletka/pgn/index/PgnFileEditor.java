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
import Khrypach.Andrey.chess.kletka.pgn.index.model.PgnIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Редактор PGN-файла с использованием индекса.
 * Обеспечивает чтение и запись партий по индексу через RandomAccessFile.
 */
public class PgnFileEditor {
    private static final Logger log = LoggerFactory.getLogger(PgnFileEditor.class);

    private final Path pgnPath;
    private final PgnIndex index;

    public PgnFileEditor(Path pgnPath, PgnIndex index) {
        this.pgnPath = pgnPath;
        this.index = index;
    }

    /**
     * Читает партию по записи индекса
     */
    public String readGame(GameIndexEntry entry) throws IOException {
        if (entry.isDeleted()) {
            throw new IllegalArgumentException("Game is deleted: " + entry.getId());
        }

        String result = readGame(entry.getOffset(), entry.getLength());
        log.trace("readGame - entry.getLength() = {}, result.length() = {}",
                entry.getLength(), result.length());

        return result;
    }

    /**
     * Читает партию по смещению и длине
     */
    public String readGame(long offset, int length) throws IOException {
        log.trace("Reading game at offset={}, length={}", offset, length);

        byte[] buffer = new byte[length];

        try (RandomAccessFile raf = new RandomAccessFile(pgnPath.toFile(), "r")) {
            raf.seek(offset);
            raf.readFully(buffer);
        }

        return new String(buffer, StandardCharsets.UTF_8);
    }

    /**
     * Читает только тело партии (без заголовков)
     * Работает в БАЙТАХ
     */
    public String readBody(GameIndexEntry entry) throws IOException {
        if (entry.isDeleted()) {
            throw new IllegalArgumentException("Game is deleted: " + entry.getId());
        }

        byte[] gameBytes = new byte[entry.getLength()];
        try (RandomAccessFile raf = new RandomAccessFile(pgnPath.toFile(), "r")) {
            raf.seek(entry.getOffset());
            raf.readFully(gameBytes);
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

    /**
     * Записывает новую партию в конец файла
     * Возвращает запись индекса для новой партии
     */
    public GameIndexEntry appendGame(String pgnContent, int newId) throws IOException {
        log.info("Appending new game with ID: {}", newId);

        byte[] contentBytes = pgnContent.getBytes(StandardCharsets.UTF_8);

        long offset;
        int length;

        try (RandomAccessFile raf = new RandomAccessFile(pgnPath.toFile(), "rw")) {
            offset = raf.length();
            raf.seek(offset);
            raf.write(contentBytes);
            length = contentBytes.length;
        }

        return GameIndexEntry.builder()
                .id(newId)
                .offset(offset)
                .length(length)
                .version(1)
                .deleted(false)
                .build();
    }

    /**
     * Записывает новую версию партии в конец файла
     * Возвращает обновленную запись индекса
     */
    public GameIndexEntry updateGame(int gameId, String newPgnContent) throws IOException {
        log.info("Updating game ID: {}", gameId);

        GameIndexEntry oldEntry = index.getEntryById(gameId);
        if (oldEntry == null) {
            throw new IllegalArgumentException("Game not found: " + gameId);
        }

        byte[] contentBytes = newPgnContent.getBytes(StandardCharsets.UTF_8);

        long newOffset;
        int newLength;

        try (RandomAccessFile raf = new RandomAccessFile(pgnPath.toFile(), "rw")) {
            newOffset = raf.length();
            raf.seek(newOffset);
            raf.write(contentBytes);
            newLength = contentBytes.length;
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

    /**
     * Заменяет содержимое партии на месте (без изменения offset)
     * Используется только когда длина содержимого совпадает
     */
    public boolean replaceGameInPlace(GameIndexEntry entry, String newPgnContent) throws IOException {
        byte[] contentBytes = newPgnContent.getBytes(StandardCharsets.UTF_8);

        if (contentBytes.length != entry.getLength()) {
            log.warn("Content length mismatch: {} != {}, cannot replace in place",
                    contentBytes.length, entry.getLength());
            return false;
        }

        try (RandomAccessFile raf = new RandomAccessFile(pgnPath.toFile(), "rw")) {
            raf.seek(entry.getOffset());
            raf.write(contentBytes);
        }

        log.info("Replaced game {} in place at offset {}",
                entry.getId(), entry.getOffset());
        return true;
    }

}