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

import Khrypach.Andrey.chess.kletka.database.model.GameData;
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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

/**
 * Создает индекс для PGN-файла.
 * Сканирует PGN файл, парсит заголовки и создает записи для каждой партии.
 */
public class PgnFileIndexer {
    private static final Logger log = LoggerFactory.getLogger(PgnFileIndexer.class);
    private static final LanguageManager lang = LanguageManager.getInstance();

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
     * Индексирует PGN-файл с прогрессом
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
     * Сканирует PGN-файл и создает записи индекса.
     * Поддерживает автоопределение кодировки.
     */
    private List<GameIndexEntry> scanFile(Path pgnPath, Consumer<IndexingProgress> progressCallback) throws IOException {
        log.info("Scanning file: {}", pgnPath);

        byte[] fileBytes = Files.readAllBytes(pgnPath);
        String encoding = detectEncoding(fileBytes);
        log.debug("Detected encoding: {}", encoding);

        String content;
        if ("UTF-8-BOM".equals(encoding)) {
            content = new String(fileBytes, 3, fileBytes.length - 3, StandardCharsets.UTF_8);
        } else {
            content = new String(fileBytes, Charset.forName(encoding));
        }

        byte[] utf8Bytes = content.getBytes(StandardCharsets.UTF_8);

        List<GameIndexEntry> entries = new ArrayList<>();
        int fileSize = utf8Bytes.length;

        byte[] searchPattern = "[Event \"".getBytes(StandardCharsets.UTF_8);
        int currentOffset = 0;
        int gameId = 0;
        int processed = 0;
        int totalGames = countGames(utf8Bytes, searchPattern);

        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .totalGames(totalGames)
                    .processedGames(0)
                    .status(String.format(lang.get(INDEXING_STATUS_SCANNING_GAMES), totalGames))
                    .build());
        }

        while (currentOffset < fileSize) {
            int gameStart = indexOfBytes(utf8Bytes, searchPattern, currentOffset);
            if (gameStart < 0) {
                break;
            }

            int gameEnd = findGameEndInBytes(utf8Bytes, gameStart);
            int length = gameEnd - gameStart;

            byte[] gameBytes = new byte[length];
            System.arraycopy(utf8Bytes, gameStart, gameBytes, 0, length);
            String originalGameContent = new String(gameBytes, StandardCharsets.UTF_8);

            try {
                List<PgnToken> gameTokens = tokenizer.tokenize(originalGameContent);
                if (!gameTokens.isEmpty()) {
                    int[] pos = new int[]{0};
                    Map<String, String> headers = headerParser.parseHeaders(gameTokens, pos);

                    boolean deleted = false;
                    String deletedStr = headers.getOrDefault("Deleted", "false");
                    if (" true".equals(deletedStr) || "true".equals(deletedStr)) {
                        deleted = true;
                    }

                    String result = headers.getOrDefault("Result", "*");

                    int bodyStart = findBodyStart(utf8Bytes, gameStart, gameEnd);
                    int bodyLength = gameEnd - bodyStart;

                    String body = "";
                    if (bodyStart > 0 && bodyStart < gameEnd && bodyLength > 0) {
                        byte[] bodyBytes = new byte[bodyLength];
                        System.arraycopy(utf8Bytes, bodyStart, bodyBytes, 0, bodyLength);
                        body = new String(bodyBytes, StandardCharsets.UTF_8);
                    }

                    GameData gameData = createGameDataFromHeaders(headers, body);
                    int contentHash = HashUtils.calculateContentHash(gameData);

                    GameIndexEntry entry = GameIndexEntry.builder()
                            .id(++gameId)
                            .offset(gameStart)
                            .length(length)
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

                    entries.add(entry);
                    processed++;

                    if (progressCallback != null && processed % 10 == 0) {
                        progressCallback.accept(IndexingProgress.builder()
                                .totalGames(totalGames)
                                .processedGames(processed)
                                .status(String.format(lang.get(INDEXING_STATUS_PROCESSED), processed, totalGames))
                                .build());
                    }
                }
            } catch (Exception e) {
                log.error("Failed to parse game at offset {}", gameStart, e);
            }

            currentOffset = gameEnd;
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
     * Определяет кодировку файла по байтам
     */
    private String detectEncoding(byte[] bytes) {
        if (bytes.length >= 3 && bytes[0] == (byte) 0xEF &&
                bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
            return "UTF-8-BOM";
        }

        try {
            String test = new String(bytes, StandardCharsets.UTF_8);
            if (!test.contains("\uFFFD")) {
                if (containsCyrillic(test) || hasPgnHeaders(test)) {
                    return "UTF-8";
                }
            }
        } catch (Exception e) {
            // Игнорируем
        }

        try {
            String win1251 = new String(bytes, Charset.forName("Windows-1251"));
            if (containsCyrillic(win1251) && hasPgnHeaders(win1251)) {
                return "Windows-1251";
            }
        } catch (Exception e) {
            // Игнорируем
        }

        try {
            String koi8r = new String(bytes, Charset.forName("KOI8-R"));
            if (containsCyrillic(koi8r) && hasPgnHeaders(koi8r)) {
                return "KOI8-R";
            }
        } catch (Exception e) {
            // Игнорируем
        }

        return "UTF-8";
    }

    /**
     * Проверяет наличие кириллических символов в строке
     */
    private boolean containsCyrillic(String text) {
        if (text == null || text.isEmpty()) return false;
        return text.codePoints().anyMatch(cp -> cp >= 0x0400 && cp <= 0x04FF);
    }

    /**
     * Проверяет наличие PGN заголовков в строке
     */
    private boolean hasPgnHeaders(String text) {
        if (text == null || text.isEmpty()) return false;
        return text.contains("[Event") || text.contains("[White") ||
                text.contains("[Black") || text.contains("[Result");
    }

    /**
     * Подсчитывает количество партий в файле
     */
    private int countGames(byte[] fileBytes, byte[] searchPattern) {
        int count = 0;
        int pos = 0;
        while (pos < fileBytes.length) {
            int start = indexOfBytes(fileBytes, searchPattern, pos);
            if (start < 0) break;
            count++;
            pos = start + 1;
        }
        return count;
    }

    /**
     * Создает GameData из заголовков и тела
     */
    private GameData createGameDataFromHeaders(Map<String, String> headers, String body) {
        String white = headers.getOrDefault("White", "?");
        String black = headers.getOrDefault("Black", "?");
        String result = headers.getOrDefault("Result", "*");
        String event = headers.getOrDefault("Event", "Kletka Game");
        String site = headers.getOrDefault("Site", "?");
        String round = headers.getOrDefault("Round", "?");
        String subround = headers.getOrDefault("Subround", "?");
        String eco = headers.getOrDefault("ECO", "?");
        String opening = headers.getOrDefault("Opening", "?");
        String variation = headers.getOrDefault("Variation", "?");
        String annotator = headers.getOrDefault("Annotator", "?");
        String whiteElo = headers.getOrDefault("WhiteElo", "?");
        String blackElo = headers.getOrDefault("BlackElo", "?");
        String whiteTeam = headers.getOrDefault("WhiteTeam", "?");
        String blackTeam = headers.getOrDefault("BlackTeam", "?");
        String source = headers.getOrDefault("Source", "?");
        String timeControl = headers.getOrDefault("TimeControl", "?");
        String fen = headers.getOrDefault("FEN", "");
        String positionType = headers.getOrDefault("PositionType", "game");
        boolean isSetUp = "1".equals(headers.getOrDefault("SetUp", "0"));
        String plyCountStr = headers.getOrDefault("PlyCount", "0");

        LocalDate date = parseDate(headers.getOrDefault("Date", ""));

        String fullPgn = buildFullPgn(headers, body);

        return new GameData(
                white, black, result,
                whiteElo, blackElo,
                event, site, round, subround, date,
                eco, opening, variation,
                annotator, whiteTeam, blackTeam, source,
                "?", "?", timeControl,
                plyCountStr,
                fullPgn,
                fen,
                isSetUp,
                positionType,
                false
        );
    }

    private String buildFullPgn(Map<String, String> headers, String body) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append("[").append(entry.getKey()).append(" \"").append(entry.getValue()).append("\"]\n");
        }

        sb.append("\n");
        if (body != null && !body.isEmpty()) {
            sb.append(body);
        }

        return sb.toString();
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return LocalDate.now();
        }
        try {
            if (dateStr.contains(".")) {
                String[] parts = dateStr.split("\\.");
                if (parts.length >= 3) {
                    return LocalDate.of(
                            Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2])
                    );
                }
            }
            if (dateStr.contains("-")) {
                String[] parts = dateStr.split("-");
                if (parts.length >= 3) {
                    return LocalDate.of(
                            Integer.parseInt(parts[0]),
                            Integer.parseInt(parts[1]),
                            Integer.parseInt(parts[2])
                    );
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return LocalDate.now();
    }

    /**
     * Находит КОНЕЦ партии в БАЙТОВОМ массиве.
     * Ищет результат (1-0, 0-1, 1/2-1/2, *) в байтах.
     */
    private int findGameEndInBytes(byte[] fileBytes, int gameStart) {
        log.trace("findGameEndInBytes: gameStart={}, fileBytes.length={}", gameStart, fileBytes.length);

        byte[] eventPattern = "[Event \"".getBytes(StandardCharsets.UTF_8);
        int nextGameStart = indexOfBytes(fileBytes, eventPattern, gameStart + 1);

        if (nextGameStart > 0 && nextGameStart < fileBytes.length) {
            log.trace("Found next game at {}, returning", nextGameStart);
            return nextGameStart;
        }

        byte[][] resultPatterns = {
                "1-0".getBytes(StandardCharsets.UTF_8),
                "0-1".getBytes(StandardCharsets.UTF_8),
                "1/2-1/2".getBytes(StandardCharsets.UTF_8),
                "*".getBytes(StandardCharsets.UTF_8)
        };

        int latestResultEnd = -1;

        for (byte[] pattern : resultPatterns) {
            int index = indexOfBytes(fileBytes, pattern, gameStart);
            while (index > 0 && index < fileBytes.length) {
                byte prevByte = fileBytes[index - 1];
                if (prevByte == ' ' || prevByte == '\n' || prevByte == '\r') {
                    int endOfLine = indexOfByte(fileBytes, index);
                    if (endOfLine < 0 || endOfLine >= fileBytes.length) {
                        endOfLine = fileBytes.length - 1;
                    }
                    int endOffset = Math.min(endOfLine + 1, fileBytes.length);
                    if (endOffset > latestResultEnd) {
                        latestResultEnd = endOffset;
                        log.trace("Found result at index {}, endOffset={}", index, endOffset);
                    }
                    break;
                }
                index = indexOfBytes(fileBytes, pattern, index + 1);
            }
        }

        if (latestResultEnd > 0 && latestResultEnd <= fileBytes.length) {
            log.trace("Using result end: {}", latestResultEnd);
            return latestResultEnd;
        }

        log.trace("No result or next game found, returning fileBytes.length: {}", fileBytes.length);
        return fileBytes.length;
    }

    /**
     * Ищет байтовый паттерн в байтовом массиве
     */
    private int indexOfBytes(byte[] array, byte[] pattern, int start) {
        if (pattern.length == 0 || start >= array.length) return -1;
        int maxStart = array.length - pattern.length;
        for (int i = start; i <= maxStart; i++) {
            boolean found = true;
            for (int j = 0; j < pattern.length; j++) {
                if (array[i + j] != pattern[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }

    /**
     * Ищет один байт в байтовом массиве
     */
    private int indexOfByte(byte[] array, int start) {
        for (int i = start; i < array.length; i++) {
            if (array[i] == (byte) 10) return i;
        }
        return -1;
    }

    /**
     * Находит начало тела партии в байтах
     * Ищет: последний ']' + разделитель (\n\n)
     */
    private int findBodyStart(byte[] fileBytes, int gameStart, int gameEnd) {
        int lastBracket = -1;
        for (int i = gameStart; i < gameEnd; i++) {
            if (fileBytes[i] == ']') {
                lastBracket = i;
            }
        }

        if (lastBracket < 0) return gameEnd;

        int bodyStart = -1;
        for (int i = lastBracket + 1; i < gameEnd - 1; i++) {
            if (fileBytes[i] == '\n' && fileBytes[i + 1] == '\n') {
                bodyStart = i + 2;
                break;
            }
        }

        if (bodyStart < 0) {
            for (int i = lastBracket + 1; i < gameEnd; i++) {
                byte b = fileBytes[i];
                if (b != ' ' && b != '\n' && b != '\r' && b != '\t') {
                    bodyStart = i;
                    break;
                }
            }
        }

        return bodyStart > 0 ? bodyStart : gameEnd;
    }

    /**
     * Извлекает год из даты
     */
    private String extractYear(String date) {
        if (date == null || date.isEmpty()) {
            return "";
        }
        try {
            if (date.contains(".")) {
                String[] parts = date.split("\\.");
                if (parts.length >= 1) {
                    return parts[0];
                }
            }
            if (date.contains("-")) {
                String[] parts = date.split("-");
                if (parts.length >= 1) {
                    return parts[0];
                }
            }
            if (date.matches("\\d{4}.*")) {
                return date.substring(0, 4);
            }
        } catch (Exception e) {
            // ignore
        }
        return "";
    }

    /**
     * Парсит количество полуходов
     */
    private int parsePlyCount(String plyCount) {
        if (plyCount == null || plyCount.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(plyCount);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}