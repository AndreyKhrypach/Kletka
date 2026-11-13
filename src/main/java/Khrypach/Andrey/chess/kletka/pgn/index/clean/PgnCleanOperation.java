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

package Khrypach.Andrey.chess.kletka.pgn.index.clean;

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.pgn.index.PgnIndexManager;
import Khrypach.Andrey.chess.kletka.pgn.index.binary.BinaryIndexReader;
import Khrypach.Andrey.chess.kletka.pgn.index.model.IndexingProgress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

public class PgnCleanOperation {

    private static final Logger log = LoggerFactory.getLogger(PgnCleanOperation.class);
    private final LanguageManager lang = LanguageManager.getInstance();

    public record CleanStats(
            int totalGames,
            int processedGames,
            int removedComments,
            int removedNags,
            int removedSuffixes,
            long elapsedMs
    ) {
        public int totalRemoved() {
            return removedComments + removedNags + removedSuffixes;
        }
    }

    /**
     * Выполняет очистку комментариев из PGN-файла в новый файл.
     *
     * @param sourcePgn  исходный .pgn
     * @param targetPgn  целевой .pgn (новый)
     * @param progress   callback прогресса (может быть null)
     * @param cancelled  флаг отмены (может быть null)
     * @return статистика
     */
    public CleanStats execute(Path sourcePgn,
                              Path targetPgn,
                              Consumer<IndexingProgress> progress,
                              AtomicBoolean cancelled) throws IOException {

        long startTime = System.currentTimeMillis();

        PgnIndexManager indexManager = new PgnIndexManager();
        Path kltPath = indexManager.getIndexPath(sourcePgn);

        if (!Files.exists(kltPath)) {
            throw new IOException("Index not found for: " + sourcePgn);
        }

        // ========== УЗНАЁМ ОБЩЕЕ КОЛИЧЕСТВО ПАРТИЙ ==========
        BinaryIndexReader reader = new BinaryIndexReader();
        int totalGames = reader.readEntryCount(kltPath);
        log.debug("Total games to clean: {}", totalGames);

        // ========== ВРЕМЕННЫЙ ФАЙЛ ==========
        Path tmpPath = targetPgn.resolveSibling(targetPgn.getFileName() + ".tmp");

        // ========== СЧЁТЧИКИ ==========
        int[] counters = new int[4]; // [processed, comments, nags, suffixes]
        int[] total = new int[1];    // [totalGames] — заполним после чтения заголовка
        long[] lastProgressMs = {0};

        // ========== ПРОГРЕСС: НАЧАЛО ==========
        if (progress != null) {
            progress.accept(IndexingProgress.builder()
                    .status(lang.get(CLEAN_PGN_STATUS_STARTING))
                    .totalGames(totalGames) // ← передаём total
                    .processedGames(0)
                    .build());
        }

        try (RandomAccessFile sourceRaf = new RandomAccessFile(sourcePgn.toFile(), "r");
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(new FileOutputStream(tmpPath.toFile()), StandardCharsets.UTF_8),
                     1 << 20)) {

            reader.streamEntries(kltPath, entry -> {
                try {
                    // ========== ОТМЕНА ==========
                    if (cancelled != null && cancelled.get()) {
                        throw new CancellationException();
                    }

                    total[0]++;

                    // ========== ЧТЕНИЕ ПАРТИИ ==========
                    byte[] buf = new byte[entry.getLength()];
                    sourceRaf.seek(entry.getOffset());
                    sourceRaf.readFully(buf);
                    String pgnText = new String(buf, StandardCharsets.UTF_8);

                    // ========== ОЧИСТКА ==========
                    PgnCommentCleaner.CleanResult r = PgnCommentCleaner.clean(pgnText);

                    // ========== ЗАПИСЬ ==========
                    String trimmed = r.cleanedBody().replaceAll("\\s+$", "");
                    writer.write(trimmed);
                    writer.write("\n\n");

                    // ========== СЧЁТЧИКИ ==========
                    counters[0]++;
                    counters[1] += r.removedComments();
                    counters[2] += r.removedNags();
                    counters[3] += r.removedSuffixes();

                    // ========== ПРОГРЕСС (раз в 500 мс) ==========
                    if (progress != null) {
                        long now = System.currentTimeMillis();
                        if (now - lastProgressMs[0] >= 250) {
                            lastProgressMs[0] = now;
                            // total[0] пока неизвестен — покажем сколько обработано
                            progress.accept(IndexingProgress.builder()
                                    .status(String.format(lang.get(CLEAN_PGN_STATUS_PROCESSING),
                                            counters[0], totalGames))
                                    .totalGames(totalGames)
                                    .processedGames(counters[0])
                                    .build());
                        }
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        } catch (CancellationException e) {
            log.info("Clean operation cancelled by user");
            Files.deleteIfExists(tmpPath);
            throw new IOException(lang.get(CLEAN_PGN_STATUS_CANCELLED));
        } catch (RuntimeException e) {
            Files.deleteIfExists(tmpPath);
            if (e.getCause() instanceof IOException io) throw io;
            throw e;
        } catch (IOException e) {
            Files.deleteIfExists(tmpPath);
            throw e;
        }

        // ========== АТОМАРНАЯ ЗАМЕНА ==========
        try {
            Files.move(tmpPath, targetPgn,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException e) {
            Files.move(tmpPath, targetPgn, StandardCopyOption.REPLACE_EXISTING);
        }

        long elapsed = System.currentTimeMillis() - startTime;

        // ========== ПРОГРЕСС: ЗАВЕРШЕНО ==========
        if (progress != null) {
            progress.accept(IndexingProgress.builder()
                    .status(lang.get(CLEAN_PGN_STATUS_COMPLETE))
                    .totalGames(counters[0])
                    .processedGames(counters[0])
                    .build());
        }

        log.info("Clean completed: {} games, {} comments, {} nags, {} suffixes, {} ms",
                counters[0], counters[1], counters[2], counters[3], elapsed);

        return new CleanStats(
                counters[0], counters[0],
                counters[1], counters[2], counters[3],
                elapsed
        );
    }

    private static class CancellationException extends RuntimeException {}
}
