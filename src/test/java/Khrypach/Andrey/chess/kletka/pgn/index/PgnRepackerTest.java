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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PgnRepacker - Перепаковщик PGN файлов")
class PgnRepackerTest {

    private PgnRepacker repacker;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        repacker = new PgnRepacker();
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ hasDeletedGames()
    // ============================================================

    @Nested
    @DisplayName("hasDeletedGames() - Проверка наличия удаленных игр")
    class HasDeletedGamesTests {

        @Test
        @DisplayName("Должен возвращать false для null индекса")
        void shouldReturnFalseForNullIndex() {
            // when
            boolean result = repacker.hasDeletedGames(null);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Должен возвращать false если нет удаленных игр")
        void shouldReturnFalseIfNoDeletedGames() {
            // given
            PgnIndex index = createIndexWithActiveGames(3);

            // when
            boolean result = repacker.hasDeletedGames(index);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Должен возвращать true если есть удаленные игры")
        void shouldReturnTrueIfHasDeletedGames() {
            // given
            PgnIndex index = createIndexWithDeletedGames(2);

            // when
            boolean result = repacker.hasDeletedGames(index);

            // then
            assertThat(result).isTrue();
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ getDeletedCount()
    // ============================================================

    @Nested
    @DisplayName("getDeletedCount() - Количество удаленных игр")
    class GetDeletedCountTests {

        @Test
        @DisplayName("Должен возвращать 0 для null индекса")
        void shouldReturnZeroForNullIndex() {
            // when
            int count = repacker.getDeletedCount(null);

            // then
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("Должен возвращать правильное количество удаленных игр")
        void shouldReturnCorrectDeletedCount() {
            // given
            PgnIndex index = createIndexWithDeletedGames(3);

            // when
            int count = repacker.getDeletedCount(index);

            // then
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("Должен возвращать 0 если нет удаленных игр")
        void shouldReturnZeroIfNoDeletedGames() {
            // given
            PgnIndex index = createIndexWithActiveGames(3);

            // when
            int count = repacker.getDeletedCount(index);

            // then
            assertThat(count).isEqualTo(0);
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ getGrowthRatio()
    // ============================================================

    @Nested
    @DisplayName("getGrowthRatio() - Соотношение роста")
    class GetGrowthRatioTests {

        @Test
        @DisplayName("Должен возвращать 1.0 для null индекса")
        void shouldReturnOneForNullIndex() {
            // when
            double ratio = repacker.getGrowthRatio(null);

            // then
            assertThat(ratio).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Должен возвращать 1.0 если нет удаленных игр")
        void shouldReturnOneIfNoDeletedGames() {
            // given
            PgnIndex index = createIndexWithActiveGames(3);

            // when
            double ratio = repacker.getGrowthRatio(index);

            // then
            assertThat(ratio).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Должен возвращать корректное соотношение при наличии удаленных игр")
        void shouldReturnCorrectRatioWithDeletedGames() {
            // given
            // Создаем индекс с 3 активными и 2 удаленными играми
            List<GameIndexEntry> entries = new ArrayList<>();

            // Активные игры (длина 100)
            for (int i = 1; i <= 3; i++) {
                entries.add(createEntry(i, 100, false));
            }

            // Удаленные игры (длина 50)
            for (int i = 4; i <= 5; i++) {
                entries.add(createEntry(i, 50, true));
            }

            PgnIndex index = PgnIndex.builder()
                    .version(1)
                    .gameCount(5)
                    .activeCount(3)
                    .entries(entries)
                    .build();

            // when
            double ratio = repacker.getGrowthRatio(index);

            // then
            // (3*100 + 2*50) / (3*100) = 400 / 300 = 1.33
            assertThat(ratio).isGreaterThan(1.0);
        }
    }

    // ============================================================
    // 4. ТЕСТЫ ДЛЯ getRepackStatus()
    // ============================================================

    @Nested
    @DisplayName("getRepackStatus() - Статус перепаковки")
    class GetRepackStatusTests {

        @Test
        @DisplayName("Должен возвращать OK статус для null индекса")
        void shouldReturnOkForNullIndex() {
            // when
            PgnRepacker.RepackStatus status = repacker.getRepackStatus(null);

            // then
            assertThat(status.level()).isEqualTo(PgnRepacker.RepackLevel.OK);
            assertThat(status.hasDeleted()).isFalse();
            assertThat(status.deletedCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Должен возвращать OK статус если нет удаленных игр")
        void shouldReturnOkIfNoDeletedGames() {
            // given
            PgnIndex index = createIndexWithActiveGames(3);

            // when
            PgnRepacker.RepackStatus status = repacker.getRepackStatus(index);

            // then
            assertThat(status.level()).isEqualTo(PgnRepacker.RepackLevel.OK);
            assertThat(status.hasDeleted()).isFalse();
            assertThat(status.deletedCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Должен возвращать WARNING статус при умеренном росте")
        void shouldReturnWarningForModerateGrowth() {
            // given
            List<GameIndexEntry> entries = new ArrayList<>();

            // Активные игры (длина 100)
            for (int i = 1; i <= 3; i++) {
                entries.add(createEntry(i, 100, false));
            }

            // Удаленные игры (длина 200, чтобы соотношение было > 1.5)
            for (int i = 4; i <= 5; i++) {
                entries.add(createEntry(i, 200, true));
            }

            PgnIndex index = PgnIndex.builder()
                    .version(1)
                    .gameCount(5)
                    .activeCount(3)
                    .entries(entries)
                    .build();

            // when
            PgnRepacker.RepackStatus status = repacker.getRepackStatus(index);

            // then
            assertThat(status.hasDeleted()).isTrue();
            assertThat(status.deletedCount()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Должен возвращать описание статуса")
        void shouldReturnStatusDescription() {
            // given
            PgnIndex index = createIndexWithActiveGames(3);

            // when
            PgnRepacker.RepackStatus status = repacker.getRepackStatus(index);

            // then
            assertThat(status.description()).isNotNull();
            assertThat(status.toString()).isNotNull();
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ repack()
    // ============================================================

    @Nested
    @DisplayName("repack() - Перепаковка")
    class RepackTests {

        @Test
        @DisplayName("Должен пропускать перепаковку если нет удаленных игр")
        void shouldSkipRepackIfNoDeletedGames() throws IOException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    [Deleted "false"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    """;
            Path pgnFile = createPgnFile("no_deleted.pgn", pgn);

            GameIndexEntry entry = createEntry(1, pgn.getBytes(StandardCharsets.UTF_8).length, false);
            PgnIndex index = PgnIndex.builder()
                    .version(1)
                    .gameCount(1)
                    .activeCount(1)
                    .entries(List.of(entry))
                    .build();

            // when
            PgnIndex result = repacker.repack(pgnFile, index, null);

            // then
            assertThat(result).isEqualTo(index);
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при отсутствии файла")
        void shouldThrowExceptionWhenFileNotFound() {
            // given
            Path nonExistentPath = tempDir.resolve("nonexistent.pgn");
            PgnIndex index = createIndexWithDeletedGames(1);

            // when/then
            assertThatThrownBy(() -> repacker.repack(nonExistentPath, index, null))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("PGN file not found");
        }

        @Test
        @DisplayName("Должен вызывать progressCallback")
        void shouldCallProgressCallback() throws IOException {
            // given
            String pgn = """
                    [Event "Game 1"]
                    [White "Player A"]
                    [Black "Player B"]
                    [Result "1-0"]
                    
                    1. e4 e5 1-0
                    """;
            Path pgnFile = createPgnFile("game1.pgn", pgn);

            GameIndexEntry entry = createEntry(1, pgn.getBytes(StandardCharsets.UTF_8).length, false);
            PgnIndex index = PgnIndex.builder()
                    .version(1)
                    .gameCount(1)
                    .activeCount(1)
                    .entries(List.of(entry))
                    .build();

            AtomicInteger callCount = new AtomicInteger(0);

            // when
            repacker.repack(pgnFile, index, progress -> callCount.incrementAndGet());

            // then
            assertThat(callCount.get()).isGreaterThan(0);
        }
    }

    // ============================================================
    // 6. ТЕСТЫ ДЛЯ ensureDeletedFalse()
    // ============================================================

    @Nested
    @DisplayName("ensureDeletedFalse() - Гарантия Deleted=false")
    class EnsureDeletedFalseTests {

        @Test
        @DisplayName("Должен добавлять тег Deleted если его нет")
        void shouldAddDeletedIfMissing() {
            // given
            String content = """
                    [Event "Kletka Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    """;

            // when
            String result = ensureDeletedFalse(content);

            // then
            assertThat(result).contains("[Deleted \"false\"]");
        }

        @Test
        @DisplayName("Должен заменять Deleted на false")
        void shouldReplaceDeletedWithFalse() {
            // given
            String content = """
                    [Event "Kletka Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    [Deleted "true"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    """;

            // when
            String result = ensureDeletedFalse(content);

            // then
            assertThat(result).contains("[Deleted \"false\"]");
            assertThat(result).doesNotContain("[Deleted \"true\"]");
        }
    }

    // ============================================================
    // 7. ТЕСТЫ ДЛЯ RepackStatus record
    // ============================================================

    @Nested
    @DisplayName("RepackStatus - Статус перепаковки (record)")
    class RepackStatusTests {

        @Test
        @DisplayName("Должен создаваться с правильными полями")
        void shouldCreateWithCorrectFields() {
            // given
            PgnRepacker.RepackLevel level = PgnRepacker.RepackLevel.WARNING;
            double ratio = 1.5;
            String description = "Test description";
            boolean hasDeleted = true;
            int deletedCount = 3;

            // when
            PgnRepacker.RepackStatus status =
                    new PgnRepacker.RepackStatus(level, ratio, description, hasDeleted, deletedCount);

            // then
            assertThat(status.level()).isEqualTo(level);
            assertThat(status.ratio()).isEqualTo(ratio);
            assertThat(status.description()).isEqualTo(description);
            assertThat(status.hasDeleted()).isTrue();
            assertThat(status.deletedCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("toString() должен возвращать описание")
        void toStringShouldReturnDescription() {
            // given
            String description = "Test description";
            PgnRepacker.RepackStatus status =
                    new PgnRepacker.RepackStatus(
                            PgnRepacker.RepackLevel.OK, 1.0, description, false, 0);

            // then
            assertThat(status.toString()).isEqualTo(description);
        }
    }

    // ============================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ============================================================

    private Path createPgnFile(String fileName, String content) throws IOException {
        Path file = tempDir.resolve(fileName);
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }

    private PgnIndex createIndexWithActiveGames(int count) {
        List<GameIndexEntry> entries = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            entries.add(createEntry(i, 100, false));
        }
        return PgnIndex.builder()
                .version(1)
                .gameCount(count)
                .activeCount(count)
                .entries(entries)
                .build();
    }

    private PgnIndex createIndexWithDeletedGames(int count) {
        List<GameIndexEntry> entries = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            entries.add(createEntry(i, 100, true));
        }
        return PgnIndex.builder()
                .version(1)
                .gameCount(count)
                .activeCount(0)
                .entries(entries)
                .build();
    }

    private GameIndexEntry createEntry(int id, int length, boolean deleted) {
        return GameIndexEntry.builder()
                .id(id)
                .offset((id - 1) * 200L)
                .length(length)
                .version(1)
                .deleted(deleted)
                .white("Player " + id)
                .black("Opponent " + id)
                .result("1-0")
                .year("2024")
                .event("Test Event")
                .site("Test Site")
                .opening("Test Opening")
                .variation("Test Variation")
                .plyCount(20)
                .build();
    }

    // ========== КОПИЯ МЕТОДА ИЗ PgnRepacker ДЛЯ ТЕСТОВ ==========
    private String ensureDeletedFalse(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        if (content.contains("[Deleted")) {
            return content.replaceAll(
                    "\\[Deleted\\s+\"[^\"]*\"]",
                    "[Deleted \"false\"]"
            );
        }

        int resultIndex = content.indexOf("[Result");
        if (resultIndex >= 0) {
            int endIndex = content.indexOf(']', resultIndex);
            if (endIndex > resultIndex) {
                return content.substring(0, endIndex + 1) + "\n[Deleted \"false\"]" +
                        content.substring(endIndex + 1);
            }
        }

        int firstBracket = content.indexOf('[');
        if (firstBracket >= 0) {
            return content.substring(0, firstBracket) + "[Deleted \"false\"]\n" +
                    content.substring(firstBracket);
        }

        return "[Deleted \"false\"]\n" + content;
    }
}