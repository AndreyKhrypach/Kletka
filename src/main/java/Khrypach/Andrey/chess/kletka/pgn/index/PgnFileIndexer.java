/*
 *
 *  * Copyright (c) 2025 Andrey Khrypach
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

import Khrypach.Andrey.chess.kletka.database.parser.HeaderParser;
import Khrypach.Andrey.chess.kletka.database.parser.PgnParser;
import Khrypach.Andrey.chess.kletka.database.parser.PgnToken;
import Khrypach.Andrey.chess.kletka.database.parser.PgnTokenizer;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.pgn.index.model.GameIndexEntry;
import Khrypach.Andrey.chess.kletka.pgn.index.model.IndexingProgress;
import Khrypach.Andrey.chess.kletka.pgn.index.model.PgnIndex;
import Khrypach.Andrey.chess.kletka.pgn.index.util.HashUtils;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

/**
 * Создает индекс для PGN-файла.
 * <p>
 * ВАЖНО: файл должен быть в UTF-8 (после {@link PgnFilePreparer}).
 * Сканирование потоковое — не держит весь файл в памяти.
 * {@code offset} и {@code length} считаются в БАЙТАХ (для корректной работы
 * с {@link PgnFileEditor} и кириллицей в именах).
 */
public class PgnFileIndexer {
    private static final Logger log = LoggerFactory.getLogger(PgnFileIndexer.class);
    private static final LanguageManager lang = LanguageManager.getInstance();

    private static final int BUFFER_SIZE = 64 * 1024;
    private static final int GAME_BUFFER_INITIAL_SIZE = 16 * 1024;

    @Getter
    private final PgnParser pgnParser;
    private final HeaderParser headerParser;
    private final PgnTokenizer tokenizer;
    private final PgnIndexManager indexManager;

    public PgnFileIndexer() {
        this.pgnParser = new PgnParser();
        this.headerParser = new HeaderParser();
        this.tokenizer = new PgnTokenizer();
        this.indexManager = new PgnIndexManager();
    }

    /**
     * Индексирует PGN-файл с прогрессом.
     * Потоковое сканирование — RAM ~50 MB независимо от размера файла.
     */
    public PgnIndex indexFile(Path pgnPath, Consumer<IndexingProgress> progressCallback) throws IOException {
        log.info("Starting indexing of: {}", pgnPath);

        if (!Files.exists(pgnPath)) {
            throw new IOException("PGN file not found: " + pgnPath);
        }

        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .status(lang.get(INDEXING_STATUS_SCANNING_FILE))
                    .build());
        }

        List<GameIndexEntry> entries = scanFile(pgnPath, progressCallback);

        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .status(lang.get(INDEXING_STATUS_CREATING_INDEX))
                    .totalGames(entries.size())
                    .processedGames(entries.size())
                    .build());
        }

        PgnIndex index = indexManager.createIndex(pgnPath, entries);

        log.info("Indexing completed: {} games, {} active",
                index.getGameCount(), index.getActiveCount());

        return index;
    }

    /**
     * Потоково сканирует PGN-файл.
     * <p>
     * ВАЖНО: файл должен быть UTF-8 (после {@link PgnFilePreparer}).
     * offset/length считаются в БАЙТАХ, а не в символах.
     */
    private List<GameIndexEntry> scanFile(Path pgnPath, Consumer<IndexingProgress> progressCallback) throws IOException {
        log.info("Scanning file (streaming): {}", pgnPath);

        // ========== 1. СЧИТАЕМ ПАРТИИ (для прогресса) ==========
        int totalGames = countGames(pgnPath);
        log.debug("Total games: {}", totalGames);

        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .totalGames(totalGames)
                    .processedGames(0)
                    .status(String.format(lang.get(INDEXING_STATUS_SCANNING_GAMES), totalGames))
                    .build());
        }

        // ========== 2. СКАНИРУЕМ ПОТОКОВО ==========
        List<GameIndexEntry> entries = new ArrayList<>(totalGames > 0 ? totalGames : 1000);

        int gameId = 0;
        int processed = 0;
        int lastProgressPercent = -1;

        // Создаем декодер, который заменяет битые байты на символ замены (?) вместо падения
        java.nio.charset.CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(java.nio.charset.CodingErrorAction.REPLACE)
                .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPLACE);

        try (BufferedReader reader = new BufferedReader(
                new java.io.InputStreamReader(Files.newInputStream(pgnPath), decoder), BUFFER_SIZE)) {

            StringBuilder gameBuffer = new StringBuilder(GAME_BUFFER_INITIAL_SIZE);
            long currentByteOffset = 0;
            long gameStartByteOffset = 0;
            boolean inGame = false;

            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                // ========== УБИРАЕМ BOM С ПЕРВОЙ СТРОКИ ==========
                if (firstLine && !line.isEmpty() && line.charAt(0) == '\uFEFF') {
                    line = line.substring(1);
                }
                firstLine = false;
                // ===================================================

                // Размер строки в БАЙТАХ
                int lineBytes = line.getBytes(StandardCharsets.UTF_8).length + 1;

                if (line.startsWith("[Event ")) {
                    // Закрываем предыдущую партию
                    if (inGame && gameBuffer.length() > 0) {
                        GameIndexEntry entry = parseGameBuffer(
                                gameBuffer.toString(), ++gameId, gameStartByteOffset);
                        if (entry != null) {
                            entries.add(entry);
                            processed++;
                        }
                    }

                    // Начинаем новую
                    gameBuffer.setLength(0);
                    gameStartByteOffset = currentByteOffset;
                    inGame = true;
                }

                if (inGame) {
                    gameBuffer.append(line).append('\n');
                }

                currentByteOffset += lineBytes;

                // Прогресс каждые 5%
                if (progressCallback != null && totalGames > 0) {
                    int percent = processed * 100 / totalGames;
                    if (percent >= lastProgressPercent + 5) {
                        lastProgressPercent = percent;
                        progressCallback.accept(IndexingProgress.builder()
                                .totalGames(totalGames)
                                .processedGames(processed)
                                .status(String.format(lang.get(INDEXING_STATUS_PROCESSED),
                                        processed, totalGames))
                                .build());
                    }
                }
            }

            // Последняя партия
            if (inGame && gameBuffer.length() > 0) {
                GameIndexEntry entry = parseGameBuffer(
                        gameBuffer.toString(), ++gameId, gameStartByteOffset);
                if (entry != null) {
                    entries.add(entry);
                    processed++;
                }
            }
        }

        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .totalGames(totalGames)
                    .processedGames(processed)
                    .status(String.format(lang.get(INDEXING_STATUS_COMPLETE), processed))
                    .build());
        }

        log.info("Scan completed: {} games found", entries.size());
        return entries;
    }

    /**
     * Считает количество партий (по строкам, начинающимся с {@code [Event "}).
     */
    private int countGames(Path pgnPath) throws IOException {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(
                Files.newBufferedReader(pgnPath, StandardCharsets.UTF_8), BUFFER_SIZE)) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                // Убираем BOM с первой строки
                if (firstLine && !line.isEmpty() && line.charAt(0) == '\uFEFF') {
                    line = line.substring(1);
                }
                firstLine = false;

                if (line.startsWith("[Event ")) count++;
            }
        }
        return count;
    }

    /**
     * Парсит одну партию из буфера в {@link GameIndexEntry}.
     * <p>
     * {@code length} считается в БАЙТАХ (UTF-8), а не в символах.
     */
    private GameIndexEntry parseGameBuffer(String gameContent, int gameId, long byteOffset) {
        try {
            Map<String, String> headers = HeaderOnlyParser.parseHeaders(gameContent);

            boolean deleted = false;
            String deletedStr = headers.getOrDefault("Deleted", "false");
            if (" true".equals(deletedStr) || "true".equals(deletedStr)) {
                deleted = true;
            }

            String result = headers.getOrDefault("Result", "*");

            // ========== ОПТИМИЗАЦИЯ: ХЕШ СЫРОГО PGN (без GameData) ==========
            int contentHash = HashUtils.hashString(gameContent);

            // ========== ДЛИНА В БАЙТАХ ==========
            int lengthBytes = gameContent.getBytes(StandardCharsets.UTF_8).length;

            return GameIndexEntry.builder()
                    .id(gameId)
                    .offset(byteOffset)
                    .length(lengthBytes)
                    .version(1)
                    .deleted(deleted)
                    .hash(contentHash)
                    .white(headers.getOrDefault("White", ""))
                    .black(headers.getOrDefault("Black", ""))
                    .eco(headers.getOrDefault("ECO", ""))
                    .result(result)
                    .year(extractYear(headers.get("Date")))
                    .event(headers.getOrDefault("Event", ""))
                    .site(headers.getOrDefault("Site", ""))
                    .opening(headers.getOrDefault("Opening", ""))
                    .variation(headers.getOrDefault("Variation", ""))
                    .plyCount(parsePlyCount(headers.get("PlyCount")))
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse game at byte offset {}: {}", byteOffset, e.getMessage());
            return null;
        }
    }

    private String extractYear(String date) {
        if (date == null || date.isEmpty()) return "";
        try {
            if (date.contains(".")) {
                String[] parts = date.split("\\.");
                if (parts.length >= 1) return parts[0];
            }
            if (date.contains("-")) {
                String[] parts = date.split("-");
                if (parts.length >= 1) return parts[0];
            }
            if (date.matches("\\d{4}.*")) {
                return date.substring(0, 4);
            }
        } catch (Exception e) {
            // ignore
        }
        return "";
    }

    private int parsePlyCount(String plyCount) {
        if (plyCount == null || plyCount.isEmpty()) return 0;
        try {
            return Integer.parseInt(plyCount);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}