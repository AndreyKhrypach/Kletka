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
 *
 */

package Khrypach.Andrey.chess.kletka.gui.book;

import Khrypach.Andrey.chess.kletka.gui.book.enums.Outcome;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.settings.AppPreferences;
import Khrypach.Andrey.chess.kletka.pgn.index.PgnFileEditor;
import Khrypach.Andrey.chess.kletka.pgn.index.PgnIndexManager;
import Khrypach.Andrey.chess.kletka.pgn.index.binary.LazyPgnIndex;
import Khrypach.Andrey.chess.kletka.pgn.index.binary.LightGameEntry;
import Khrypach.Andrey.chess.kletka.pgn.index.model.GameIndexEntry;
import Khrypach.Andrey.chess.kletka.pgn.index.manager.PgnBrowserManager;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;

import static Khrypach.Andrey.chess.kletka.gui.book.PgnToBookConverter.moveToUci;

/**
 * Собирает временную Polyglot-книгу только из выбранных партий PGN-файла.
 * <p>
 * Использует существующий {@link PgnToBookConverter} для накопления статистики
 * и {@link PolyglotBookWriter} для записи.
 * <p>
 * Временный файл называется {@code <base>.MultiGames} (без .bin) и создаётся
 * в папке {@code bases} Kletka — чтобы антивирус не реагировал на расширение .bin.
 */
public final class MultiGameBookBuilder {

    private static final Logger log = LoggerFactory.getLogger(MultiGameBookBuilder.class);
    private static final LanguageManager lang = LanguageManager.getInstance();

    private MultiGameBookBuilder() {}

    public record BuildResult(
            int gamesRequested,
            int gamesProcessed,
            int gamesSkipped,
            int truncatedByRepetition,   // ← новое
            int entriesWritten,
            long elapsedMs
    ) {
        public boolean isEmpty() {
            return entriesWritten == 0;
        }
    }

    /**
     * Возвращает путь для временного файла мультикниги:
     * {@code <bases>/<filename>.MultiGames} (без .bin).
     */
    public static Path buildTempPath(Path sourcePgn) {
        String base = sourcePgn.getFileName().toString();
        int dot = base.lastIndexOf('.');
        if (dot > 0) base = base.substring(0, dot);

        Path tempDir = AppPreferences.getBasesDirectory();
        try {
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }
        } catch (IOException e) {
            log.warn("Failed to create bases dir: {}", e.getMessage());
        }
        return tempDir.resolve(base + ".MultiGames");
    }

    /**
     * Строит Polyglot-книгу ТОЛЬКО из выбранных партий.
     *
     * @param pgnPath    PGN-файл (должен быть проиндексирован)
     * @param selectedIds id выбранных партий (из LightGameEntry.id())
     * @param outputBook путь к выходному файлу (может не иметь .bin)
     * @param options    параметры (maxPly, minGames)
     * @param progressCb колбэк прогресса (0.0..1.0, текст)
     */
    public static BuildResult build(
            Path pgnPath,
            Collection<Integer> selectedIds,
            Path outputBook,
            PgnToBookConverter.BuildOptions options,
            BiConsumer<Double, String> progressCb) throws IOException {

        long start = System.currentTimeMillis();

        if (selectedIds == null || selectedIds.isEmpty()) {
            throw new IllegalArgumentException("No games selected");
        }
        if (selectedIds.size() > PgnBrowserManager.MAX_MULTI_GAME_LOAD) {
            throw new IllegalArgumentException(
                    "Too many games: " + selectedIds.size() +
                            " (max " + PgnBrowserManager.MAX_MULTI_GAME_LOAD + ")");
        }
        if (pgnPath == null || !Files.exists(pgnPath)) {
            throw new IOException("PGN file not found: " + pgnPath);
        }

        log.info("[MULTI-BOOK] Building book: pgn={}, selected={}, out={}",
                pgnPath.getFileName(), selectedIds.size(), outputBook.getFileName());

        // 1. Ленивый индекс (O(1) доступ по id)
        PgnIndexManager indexManager = new PgnIndexManager();
        LazyPgnIndex lazyIndex = indexManager.loadLazyIndex(pgnPath);

        // 2. Статистика: key -> uci -> [wins, losses, draws]
        Map<Long, Map<String, int[]>> stats = new HashMap<>();

        int total = selectedIds.size();
        int processed = 0;
        int skipped = 0;
        int truncatedByRepetition = 0;

        // 3. Один RandomAccessFile на весь цикл
        try (PgnFileEditor editor = new PgnFileEditor(pgnPath, lazyIndex).batch()) {
            for (Integer id : selectedIds) {
                if (id == null) { skipped++; processed++; continue; }

                processed++;

                try {
                    LightGameEntry light = lazyIndex.getActiveLightEntry(id);
                    if (light == null) {
                        skipped++;
                        continue;
                    }

                    GameIndexEntry fullEntry = lazyIndex.getFullEntry(id);
                    if (fullEntry == null || fullEntry.isDeleted()) {
                        skipped++;
                        continue;
                    }

                    String rawPgn = editor.readGame(fullEntry);
                    if (rawPgn == null || rawPgn.isEmpty()) {
                        skipped++;
                        continue;
                    }

                    // SetUp/FEN пропускаем — книга исказится
                    if (rawPgn.contains("[SetUp \"1\"]")) {
                        skipped++;
                        continue;
                    }

                    Outcome winner = PgnToBookConverter.parseOutcome(rawPgn);
                    if (winner == Outcome.UNKNOWN) {
                        winner = Outcome.DRAW;
                    }

                    String body = PgnToBookConverter.extractBody(rawPgn);
                    List<String> sans = PgnToBookConverter.extractMainLineSans(body);

                    if (sans.isEmpty()) {
                        skipped++;
                        continue;
                    }

                    // ========== СВОЙ ЦИКЛ С ПРОВЕРКОЙ ПОВТОРЕНИЙ ==========
                    Board board = new Board();
                    Map<Long, Integer> positionCounts = new HashMap<>();
                    int ply = 0;
                    boolean truncated = false;
                    Outcome finalWinner = winner;   // для switch

                    for (String san : sans) {
                        if (ply >= options.maxPly()) break;

                        Move move = PgnToBookConverter.sanToMove(board, san);
                        if (move == null) break;

                        long key = ZobristHasher.calculateWithoutCache(board);

                        int count = positionCounts.getOrDefault(key, 0);
                        if (count >= 2) {
                            truncated = true;
                            break;
                        }
                        positionCounts.put(key, count + 1);

                        String uci = moveToUci(move);
                        int[] counters = stats
                                .computeIfAbsent(key, k -> new HashMap<>())
                                .computeIfAbsent(uci, u -> new int[3]);

                        switch (finalWinner) {
                            case WHITE_WIN -> {
                                if (board.getSideToMove() == Side.WHITE) counters[0]++;
                                else counters[1]++;
                            }
                            case BLACK_WIN -> {
                                if (board.getSideToMove() == Side.BLACK) counters[0]++;
                                else counters[1]++;
                            }
                            case DRAW -> counters[2]++;
                            default -> { /* UNKNOWN уже заменён на DRAW */ }
                        }

                        board.doMove(move);
                        ply++;
                    }

                    if (truncated) truncatedByRepetition++;

                } catch (Exception e) {
                    log.debug("[MULTI-BOOK] Failed to process game {}: {}", id, e.getMessage());
                    skipped++;
                }

                // Прогресс каждые 100 игр
                if (progressCb != null && processed % 100 == 0) {
                    progressCb.accept((double) processed / total,
                            String.format(lang.get(LanguageKeys.BOOK_BUILD_PROCESSING ), processed, total));
                }
            }
        }

        if (progressCb != null) {
            progressCb.accept(0.90, lang.get(LanguageKeys.BOOK_BUILD_ASSEMBLING));
        }

        // 4. Сборка PolyglotEntry (без записи)
        List<PolyglotEntry> entries = PgnToBookConverter.buildEntries(stats, options);

        // 5. Запись файла
        if (!entries.isEmpty()) {
            if (progressCb != null) {
                progressCb.accept(0.95, lang.get(LanguageKeys.BOOK_BUILD_WRITING));
            }
            PolyglotBookWriter.createBook(outputBook, entries);
        }

        if (progressCb != null) {
            progressCb.accept(1.0, lang.get(LanguageKeys.BOOK_BUILD_DONE));
        }

        long elapsed = System.currentTimeMillis() - start;
        log.debug("[MULTI-BOOK] Done: requested={}, processed={}, skipped={}, truncatedByRepetition={}, entries={}, elapsed={}ms",
                total, processed - skipped, skipped, truncatedByRepetition, entries.size(), elapsed);

        return new BuildResult(total, processed - skipped, skipped, truncatedByRepetition, entries.size(), elapsed);
    }


}
