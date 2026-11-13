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

import Khrypach.Andrey.chess.kletka.pgn.index.PgnIndexManager;
import Khrypach.Andrey.chess.kletka.pgn.index.model.GameIndexEntry;
import Khrypach.Andrey.chess.kletka.pgn.index.model.PgnIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class PgnCleanOperationTest {

    @TempDir
    Path tempDir;

    private Path sourcePgn;
    private Path targetPgn;

    @BeforeEach
    void setUp() throws IOException {
        sourcePgn = tempDir.resolve("source.pgn");
        targetPgn = tempDir.resolve("source_NoComments.pgn");
    }

    @Test
    void execute_shouldCleanCommentsAndPreserveHeaders() throws IOException {
        createPgnWithIndex(
                game("A", "B", "1-0", "1. e4 {comment1} e5 {comment2} 2. Nf3 *"),
                game("C", "D", "0-1", "1. d4 $1 d5 $2 2. c4 *"),
                game("E", "F", "1/2-1/2", "1. Nf3 Nf6 2. g3 g6 *") // без комментариев
        );

        PgnCleanOperation op = new PgnCleanOperation();
        PgnCleanOperation.CleanStats stats = op.execute(
                sourcePgn, targetPgn, null, new AtomicBoolean(false));

        assertEquals(3, stats.processedGames());
        assertTrue(stats.removedComments() >= 2);
        assertTrue(stats.removedNags() >= 2);

        String result = Files.readString(targetPgn, StandardCharsets.UTF_8);
        assertFalse(result.contains("{comment1}"));
        assertFalse(result.contains("{comment2}"));
        assertFalse(result.contains("$1"));
        assertFalse(result.contains("$2"));
        assertTrue(result.contains("[White \"A\"]"));
        assertTrue(result.contains("[White \"C\"]"));
        assertTrue(result.contains("[White \"E\"]"));
    }

    @Test
    void execute_shouldHandleDeletedGames() throws IOException {
        // Одна активная, одна deleted — обе должны попасть в результат
        createPgnWithIndex(
                game("A", "B", "1-0", "1. e4 {c} e5 *"),
                gameDeleted("C", "D", "0-1", "1. d4 {c} d5 *")
        );

        PgnCleanOperation op = new PgnCleanOperation();
        PgnCleanOperation.CleanStats stats = op.execute(
                sourcePgn, targetPgn, null, new AtomicBoolean(false));

        assertEquals(2, stats.processedGames());

        String result = Files.readString(targetPgn, StandardCharsets.UTF_8);
        assertTrue(result.contains("[White \"A\"]"));
        assertTrue(result.contains("[White \"C\"]"));
        assertTrue(result.contains("[Deleted \" true\"]"));
        assertFalse(result.contains("{c}"));
    }

    @Test
    void execute_shouldCancelAndDeleteTmp() throws IOException {
        createPgnWithIndex(
                game("A", "B", "1-0", "1. e4 {c} e5 *"),
                game("C", "D", "0-1", "1. d4 {c} d5 *")
        );

        AtomicBoolean cancelled = new AtomicBoolean(false);
        PgnCleanOperation op = new PgnCleanOperation();

        // Отменяем сразу после старта — execute должен бросить IOException
        cancelled.set(true);

        assertThrows(IOException.class, () ->
                op.execute(sourcePgn, targetPgn, null, cancelled));

        // .tmp должен быть удалён, targetPgn не должен появиться
        assertFalse(Files.exists(targetPgn));
        try (var stream = Files.list(tempDir)) {
            assertTrue(stream.noneMatch(p -> p.getFileName().toString().endsWith(".tmp")));
        }
    }

    @Test
    void execute_shouldThrowWhenIndexMissing() {
        // .pgn есть, .klt нет — должно упасть
        assertThrows(IOException.class, () -> {
            Files.writeString(sourcePgn, "[Event \"?\"]\n\n1. e4 *");
            PgnCleanOperation op = new PgnCleanOperation();
            op.execute(sourcePgn, targetPgn, null, new AtomicBoolean(false));
        });
    }

    // ============================================================
    // ВСПОМОГАТЕЛЬНЫЕ
    // ============================================================

    private String game(String white, String black, String result, String body) {
        return "[Event \"Test\"]\n"
                + "[Site \"?\"]\n"
                + "[Date \"2024.01.01\"]\n"
                + "[Round \"1\"]\n"
                + "[White \"" + white + "\"]\n"
                + "[Black \"" + black + "\"]\n"
                + "[Result \"" + result + "\"]\n"
                + "\n"
                + body + "\n";
    }

    private String gameDeleted(String white, String black, String result, String body) {
        return "[Event \"Test\"]\n"
                + "[Site \"?\"]\n"
                + "[Date \"2024.01.01\"]\n"
                + "[Round \"1\"]\n"
                + "[White \"" + white + "\"]\n"
                + "[Black \"" + black + "\"]\n"
                + "[Result \"" + result + "\"]\n"
                + "[Deleted \" true\"]\n"
                + "\n"
                + body + "\n";
    }

    /**
     * Пишет .pgn и вручную создаёт .klt, чтобы не зависеть от индексатора
     * (он медленный и делает prepare, что меняет файл).
     */
    private void createPgnWithIndex(String... games) throws IOException {
        // 1. Пишем .pgn
        StringBuilder sb = new StringBuilder();
        for (String g : games) {
            sb.append(g).append("\n");
        }
        Files.writeString(sourcePgn, sb.toString(), StandardCharsets.UTF_8);

        // 2. Строим .klt вручную (простой формат, как BinaryIndexWriter)
        // Проще всего — воспользоваться PgnIndexManager, но он делает prepare.
        // Поэтому строим через рефлексию минимальный валидный .klt.
        // ... (см. ниже)
        // Здесь самый простой вариант — скопировать из BinaryIndexReaderTest
        // метод createTestBinaryIndex, но с реальными offset/length из .pgn.

        // Для простоты — используем уже готовый тестовый билдер из BinaryIndexReaderTest
        buildMinimalIndex(games);
    }

    /**
     * Строит .klt с реальными offset/length для каждой партии.
     */
    private void buildMinimalIndex(String[] games) throws IOException {
        // Считаем offset/length в байтах (как в реальном PGN-файле)
        byte[] all = Files.readAllBytes(sourcePgn);
        int pos = 0;
        long[] offsets = new long[games.length];
        int[] lengths = new int[games.length];

        for (int i = 0; i < games.length; i++) {
            offsets[i] = pos;
            byte[] g = games[i].getBytes(StandardCharsets.UTF_8);
            lengths[i] = g.length + 1; // +1 за \n
            pos += g.length + 1;       // + \n между партиями
        }

        // Строим .klt через BinaryIndexWriter
        PgnIndex index = PgnIndex.builder()
                .version(PgnIndex.FORMAT_VERSION)
                .fileHash("test")
                .fileSize(all.length)
                .gameCount(games.length)
                .activeCount(countActive(games))
                .entries(new java.util.ArrayList<>())
                .build();

        for (int i = 0; i < games.length; i++) {
            boolean deleted = games[i].contains("[Deleted \" true\"]");
            index.getEntries().add(GameIndexEntry.builder()
                    .id(i + 1)
                    .offset(offsets[i])
                    .length(lengths[i])
                    .version(1)
                    .deleted(deleted)
                    .hash(0)
                    .white("?")
                    .black("?")
                    .result("*")
                    .build());
        }

        PgnIndexManager mgr = new PgnIndexManager();
        mgr.saveIndex(sourcePgn, index);
    }

    private int countActive(String[] games) {
        int c = 0;
        for (String g : games) {
            if (!g.contains("[Deleted \" true\"]")) c++;
        }
        return c;
    }
}