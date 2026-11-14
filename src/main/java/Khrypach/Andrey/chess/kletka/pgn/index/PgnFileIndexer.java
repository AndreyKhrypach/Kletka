package Khrypach.Andrey.chess.kletka.pgn.index;

import Khrypach.Andrey.chess.kletka.database.parser.PgnParser;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.pgn.index.model.GameIndexEntry;
import Khrypach.Andrey.chess.kletka.pgn.index.model.IndexingProgress;
import Khrypach.Andrey.chess.kletka.pgn.index.model.PgnIndex;
import Khrypach.Andrey.chess.kletka.pgn.index.util.HashUtils;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

public class PgnFileIndexer {
    private static final Logger log = LoggerFactory.getLogger(PgnFileIndexer.class);
    private final LanguageManager lang = LanguageManager.getInstance();
    private static final int READ_BUF = 256 * 1024;

    @Getter
    private final PgnParser pgnParser;

    private final PgnIndexManager indexManager;

    public PgnFileIndexer() {
        this.pgnParser = new PgnParser();
        this.indexManager = new PgnIndexManager();
    }

    public PgnIndex indexFile(Path pgnPath, Consumer<IndexingProgress> progressCallback) throws IOException {
        log.info("Starting indexing of: {}", pgnPath);
        if (!Files.exists(pgnPath)) throw new IOException("PGN file not found: " + pgnPath);

        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .status(lang.get(INDEXING_STATUS_SCANNING_FILE)).build());
        }

        List<GameIndexEntry> entries = scanFile(pgnPath, progressCallback);

        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .status(lang.get(INDEXING_STATUS_CREATING_INDEX))
                    .totalGames(entries.size())
                    .processedGames(entries.size()).build());
        }

        PgnIndex index = indexManager.createIndex(pgnPath, entries);
        log.debug("Indexing completed: {} games, {} active",
                index.getGameCount(), index.getActiveCount());
        return index;
    }

    /**
     * Блочное сканирование. Читаем блоками по 256 КБ, ищем '\n' по байтам,
     * декодируем строку ТОЛЬКО когда нужно (для [Event / [White / и т.д.).
     * Точные byte offset гарантированы.
     */
    private List<GameIndexEntry> scanFile(Path pgnPath, Consumer<IndexingProgress> progressCallback) throws IOException {
        log.info("Scanning file (block): {}", pgnPath);

        int totalGames = countGames(pgnPath);  // быстрый проход readLine()

        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .totalGames(totalGames).processedGames(0)
                    .status(String.format(lang.get(INDEXING_STATUS_SCANNING_GAMES), totalGames))
                    .build());
        }

        List<GameIndexEntry> entries = new ArrayList<>(totalGames > 0 ? totalGames : 1000);
        int gameId = 0;
        int processed = 0;
        int lastProgressPercent = -1;

        StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        try (InputStream in = new BufferedInputStream(Files.newInputStream(pgnPath), READ_BUF)) {
            byte[] buf = new byte[READ_BUF];
            int bufLen;
            long bytesConsumed = 0;         // всего прочитано из файла
            long lineStartByte = 0;         // byte-offset начала текущей строки
            ByteArrayOutputStream lineBytes = new ByteArrayOutputStream(256);

            long gameStartByte = -1;
            StringBuilder gameBuffer = new StringBuilder(16 * 1024);
            boolean inGame = false;
            boolean firstLine = true;

            while ((bufLen = in.read(buf)) != -1) {
                for (int i = 0; i < bufLen; i++) {
                    byte b = buf[i];
                    bytesConsumed++;

                    if (b == '\n') {
                        // Конец строки — декодируем
                        byte[] rawLine = lineBytes.toByteArray();
                        lineBytes.reset();

                        // Убираем '\r' (CRLF → LF)
                        int end = rawLine.length;
                        if (end > 0 && rawLine[end - 1] == '\r') end--;

                        // Убираем BOM с первой строки
                        int start = 0;
                        if (firstLine && end >= 3
                                && (rawLine[0] & 0xFF) == 0xEF
                                && (rawLine[1] & 0xFF) == 0xBB
                                && (rawLine[2] & 0xFF) == 0xBF) {
                            start = 3;
                        }
                        firstLine = false;

                        // Декодируем только если нужно (первая строка или [Event)
                        boolean isEventStart = startsWith(rawLine, start, "[Event ");
                        String line = null;
                        if (isEventStart) {
                            line = new String(rawLine, start, end - start, StandardCharsets.UTF_8);
                        }

                        if (isEventStart) {
                            // Закрываем предыдущую партию
                            if (inGame && !gameBuffer.isEmpty()) {
                                GameIndexEntry entry = parseGameBuffer(
                                        gameBuffer.toString(), ++gameId, gameStartByte);
                                if (entry != null) { entries.add(entry); processed++; }
                            }
                            gameBuffer.setLength(0);
                            gameStartByte = lineStartByte;
                            inGame = true;
                            gameBuffer.append(line).append('\n');
                        } else if (inGame) {
                            // Декодируем строку для тела партии (нужна кириллица)
                            String decoded = new String(rawLine, start, end - start, StandardCharsets.UTF_8);
                            gameBuffer.append(decoded).append('\n');
                        }

                        lineStartByte = bytesConsumed;

                        // Прогресс
                        if (progressCallback != null && totalGames > 0) {
                            int percent = processed * 100 / totalGames;
                            if (percent >= lastProgressPercent + 5) {
                                lastProgressPercent = percent;
                                progressCallback.accept(IndexingProgress.builder()
                                        .totalGames(totalGames).processedGames(processed)
                                        .status(String.format(lang.get(INDEXING_STATUS_PROCESSED),
                                                processed, totalGames)).build());
                            }
                        }
                    } else {
                        lineBytes.write(b);
                    }
                }
            }

            // Последняя строка без '\n'
            if (lineBytes.size() > 0) {
                byte[] rawLine = lineBytes.toByteArray();
                String decoded = new String(rawLine, StandardCharsets.UTF_8);
                if (inGame) gameBuffer.append(decoded).append('\n');
            }

            // Последняя партия
            if (inGame && !gameBuffer.isEmpty()) {
                GameIndexEntry entry = parseGameBuffer(
                        gameBuffer.toString(), ++gameId, gameStartByte);
                if (entry != null) { entries.add(entry); processed++; }
            }
        }

        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .totalGames(totalGames).processedGames(processed)
                    .status(String.format(lang.get(INDEXING_STATUS_COMPLETE), processed)).build());
        }
        log.info("Scan completed: {} games found", entries.size());
        return entries;
    }

    private static boolean startsWith(byte[] arr, int off, String asciiPrefix) {
        if (arr.length - off < asciiPrefix.length()) return false;
        for (int i = 0; i < asciiPrefix.length(); i++) {
            if (arr[off + i] != (byte) asciiPrefix.charAt(i)) return false;
        }
        return true;
    }

    private int countGames(Path pgnPath) throws IOException {
        int count = 0;
        java.nio.charset.CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try (InputStream is = Files.newInputStream(pgnPath);
             Reader reader = new InputStreamReader(is, decoder);
             BufferedReader br = new BufferedReader(reader, 256 * 1024)) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine && !line.isEmpty() && line.charAt(0) == '\uFEFF') line = line.substring(1);
                firstLine = false;
                if (line.startsWith("[Event ")) count++;
            }
        }
        return count;
    }

    private GameIndexEntry parseGameBuffer(String gameContent, int gameId, long byteOffset) {
        try {
            Map<String, String> headers = HeaderOnlyParser.parseHeaders(gameContent);
            boolean deleted = false;
            String deletedStr = headers.getOrDefault("Deleted", "false");
            if (" true".equals(deletedStr) || "true".equals(deletedStr)) deleted = true;

            String result = headers.getOrDefault("Result", "*");
            int contentHash = HashUtils.hashString(gameContent);
            int lengthBytes = gameContent.getBytes(StandardCharsets.UTF_8).length;

            return GameIndexEntry.builder()
                    .id(gameId).offset(byteOffset).length(lengthBytes).version(1)
                    .deleted(deleted).hash(contentHash)
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
            if (date.contains(".")) { String[] p = date.split("\\."); if (p.length >= 1) return p[0]; }
            if (date.contains("-")) { String[] p = date.split("-"); if (p.length >= 1) return p[0]; }
            if (date.matches("\\d{4}.*")) return date.substring(0, 4);
        } catch (Exception ignored) {}
        return "";
    }

    private int parsePlyCount(String plyCount) {
        if (plyCount == null || plyCount.isEmpty()) return 0;
        try { return Integer.parseInt(plyCount); } catch (NumberFormatException e) { return 0; }
    }
}