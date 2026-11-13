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

import Khrypach.Andrey.chess.kletka.gui.settings.AppPreferences;
import Khrypach.Andrey.chess.kletka.pgn.index.PgnIndexingFacade;
import Khrypach.Andrey.chess.kletka.pgn.index.manager.PgnBrowserManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MultiGameBookBuilderTest {

    private Path tempDir;
    private Path pgnFile;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("kletka-test-");
        pgnFile = tempDir.resolve("test_games.pgn");

        // 3 партии: победа белых, победа чёрных, ничья
        String pgn = """
            [Event "Test 1"]
            [Site "?"]
            [Date "2025.01.01"]
            [Round "1"]
            [White "Alice"]
            [Black "Bob"]
            [Result "1-0"]
            
            1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 5. O-O Be7 1-0
            
            [Event "Test 2"]
            [Site "?"]
            [Date "2025.01.01"]
            [Round "2"]
            [White "Carol"]
            [Black "Dave"]
            [Result "0-1"]
            
            1. d4 d5 2. c4 e6 3. Nc3 Nf6 4. Bg5 Be7 0-1
            
            [Event "Test 3"]
            [Site "?"]
            [Date "2025.01.01"]
            [Round "3"]
            [White "Eve"]
            [Black "Frank"]
            [Result "1/2-1/2"]
            
            1. c4 c5 2. Nf3 Nf6 3. Nc3 Nc6 4. g3 g6 1/2-1/2
            """;

        Files.writeString(pgnFile, pgn, StandardCharsets.UTF_8);

        // Проиндексировать
        PgnIndexingFacade facade = new PgnIndexingFacade();
        facade.indexFile(pgnFile, progress -> {});
    }

    @AfterEach
    void tearDown() throws IOException {
        // Удалить временную папку
        if (tempDir != null && Files.exists(tempDir)) {
            try (var stream = Files.walk(tempDir)) {
                stream.sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                        });
            }
        }
    }

    @Test
    void buildTempPath_returnsPathInBasesDirectory() {
        Path sourcePgn = Path.of("/some/dir/my_games.pgn");
        Path result = MultiGameBookBuilder.buildTempPath(sourcePgn);

        assertNotNull(result);
        assertEquals("my_games.MultiGames", result.getFileName().toString());
        assertEquals(AppPreferences.getBasesDirectory(), result.getParent());
    }

    @Test
    void buildTempPath_stripsExtension() {
        Path sourcePgn = Path.of("/some/dir/abc.pgn");
        Path result = MultiGameBookBuilder.buildTempPath(sourcePgn);

        assertEquals("abc.MultiGames", result.getFileName().toString());
    }

    @Test
    void buildTempPath_noExtension_keepsName() {
        Path sourcePgn = Path.of("/some/dir/abc");
        Path result = MultiGameBookBuilder.buildTempPath(sourcePgn);

        assertEquals("abc.MultiGames", result.getFileName().toString());
    }

    @Test
    void build_withThreeGames_createsBook() throws IOException {
        // id партий из индекса: 1, 2, 3
        Collection<Integer> ids = List.of(1, 2, 3);
        Path outputBook = tempDir.resolve("test.bin");

        MultiGameBookBuilder.BuildResult result = MultiGameBookBuilder.build(
                pgnFile,
                ids,
                outputBook,
                PgnToBookConverter.BuildOptions.forMultiGame(),
                (progress, text) -> {}  // no-op
        );

        assertNotNull(result);
        assertEquals(3, result.gamesRequested());
        assertEquals(3, result.gamesProcessed());
        assertEquals(0, result.gamesSkipped());
        assertTrue(result.entriesWritten() > 0, "Book should contain entries");
        assertFalse(result.isEmpty());
        assertTrue(Files.exists(outputBook), "Output book file must exist");
        assertTrue(Files.size(outputBook) > 0, "Output book file must be non-empty");
    }

    @Test
    void build_emptySelection_throws() {
        Collection<Integer> ids = Collections.emptyList();
        Path outputBook = tempDir.resolve("empty.bin");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> MultiGameBookBuilder.build(
                        pgnFile, ids, outputBook,
                        PgnToBookConverter.BuildOptions.forMultiGame(),
                        (p, t) -> {})
        );
        assertTrue(ex.getMessage().contains("No games selected"));
    }

    @Test
    void build_tooManyGames_throws() {
        Collection<Integer> tooMany = new ArrayList<>();
        for (int i = 0; i < PgnBrowserManager.MAX_MULTI_GAME_LOAD + 1; i++) {
            tooMany.add(i);
        }
        Path outputBook = tempDir.resolve("too_many.bin");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> MultiGameBookBuilder.build(
                        pgnFile, tooMany, outputBook,
                        PgnToBookConverter.BuildOptions.forMultiGame(),
                        (p, t) -> {})
        );
        assertTrue(ex.getMessage().contains("Too many games"));
    }

    @Test
    void build_missingPgn_throws() {
        Path missing = tempDir.resolve("missing.pgn");
        Path outputBook = tempDir.resolve("missing.bin");

        IOException ex = assertThrows(
                IOException.class,
                () -> MultiGameBookBuilder.build(
                        missing, List.of(1), outputBook,
                        PgnToBookConverter.BuildOptions.forMultiGame(),
                        (p, t) -> {})
        );
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void build_missingId_skipped() throws IOException {
        // id 999 — нет в индексе
        Collection<Integer> ids = List.of(1, 999, 2);
        Path outputBook = tempDir.resolve("skipped.bin");

        MultiGameBookBuilder.BuildResult result = MultiGameBookBuilder.build(
                pgnFile, ids, outputBook,
                PgnToBookConverter.BuildOptions.forMultiGame(),
                (p, t) -> {}
        );

        assertEquals(3, result.gamesRequested());
        assertTrue(result.gamesSkipped() >= 1, "Missing ID must be skipped");
        assertTrue(result.entriesWritten() > 0);
    }

    @Test
    void build_withThreefoldRepetition_truncatesGame() throws IOException {
        // Партия с 3-кратным повторением (король ходит туда-сюда)
        String pgn = """
            [Event "Repetition"]
            [Result "1/2-1/2"]
            
            1. Nf3 Nf6 2. Ng1 Ng8 3. Nf3 Nf6 4. Ng1 Ng8 5. Nf3 1/2-1/2
            """;
        Path repPgn = tempDir.resolve("repetition.pgn");
        Files.writeString(repPgn, pgn, StandardCharsets.UTF_8);
        new PgnIndexingFacade().indexFile(repPgn, p -> {});

        Path outputBook = tempDir.resolve("rep.bin");

        MultiGameBookBuilder.BuildResult result = MultiGameBookBuilder.build(
                repPgn, List.of(1), outputBook,
                PgnToBookConverter.BuildOptions.forMultiGame(),
                (p, t) -> {}
        );

        // Должно быть обработано без ошибок (обрезано на 3-м повторении)
        assertEquals(1, result.gamesRequested());
        assertEquals(1, result.gamesProcessed());
        assertTrue(result.truncatedByRepetition() >= 1, "Game with repetition must be truncated");
    }

    @Test
    void build_callsProgressCallback() throws IOException {
        List<Double> progressValues = new ArrayList<>();
        List<String> statuses = new ArrayList<>();

        MultiGameBookBuilder.BuildResult result = MultiGameBookBuilder.build(
                pgnFile, List.of(1, 2, 3),
                tempDir.resolve("progress.bin"),
                PgnToBookConverter.BuildOptions.forMultiGame(),
                (progress, text) -> {
                    progressValues.add(progress);
                    statuses.add(text);
                }
        );

        // 1. Прогресс вызывался хотя бы раз
        assertFalse(progressValues.isEmpty(), "Progress callback must be called");

        // 2. Прогресс монотонно возрастает (0.90 → 0.95 → 1.0)
        for (int i = 1; i < progressValues.size(); i++) {
            assertTrue(progressValues.get(i) >= progressValues.get(i - 1),
                    "Progress must be non-decreasing");
        }

        // 3. Последнее значение — 1.0 (завершено)
        assertEquals(1.0, progressValues.get(progressValues.size() - 1), 0.001,
                "Last progress value must be 1.0");

        // 4. Первое значение — 0.90 (assembling), т.к. 3 партии не достигают 100
        assertEquals(0.90, progressValues.get(0), 0.001,
                "First progress value must be 0.90 (assembling)");

        // 5. Тексты статусов заполнены
        assertFalse(statuses.isEmpty());
        assertEquals(progressValues.size(), statuses.size(),
                "Each progress must have a status text");
        assertFalse(statuses.get(statuses.size() - 1).isEmpty(),
                "Last status must not be empty");

        // 6. Книга создана (иначе 0.95 не вызывался — см. код)
        assertFalse(result.isEmpty());
    }

    @Test
    void build_outputBookIsValidPolyglot() throws IOException {
        Path outputBook = tempDir.resolve("valid.bin");

        MultiGameBookBuilder.build(
                pgnFile, List.of(1, 2, 3),
                outputBook,
                PgnToBookConverter.BuildOptions.forMultiGame(),
                (p, t) -> {}
        );

        // Проверяем через PolyglotBookParser
        PolyglotBookParser parser = new PolyglotBookParser();
        try (parser) {
            parser.open(outputBook);
            assertTrue(parser.getTotalEntries() > 0);
        }
    }
}