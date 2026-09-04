/*
 *
 *  * Copyright (c) 2024 Andrey Khrypach
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

package Khrypach.Andrey.chess.kletka.pgn.index.operation;

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.pgn.index.PgnIndexManager;
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

@DisplayName("PgnBatchOperation - Пакетные операции с PGN")
class PgnBatchOperationTest {

    @TempDir
    Path tempDir;

    private Path pgnPath;
    private PgnIndex index;
    private PgnBatchOperation batchOperation;
    private PgnIndexManager indexManager;
    private final LanguageManager lang= LanguageManager.getInstance();

    @BeforeEach
    void setUp() throws IOException {
        pgnPath = tempDir.resolve("test.pgn");
        indexManager = new PgnIndexManager();

        // Создаем тестовый PGN файл с 20 партиями
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 20; i++) {
            sb.append("[Event \"Test Game ").append(i).append("\"]\n");
            sb.append("[White \"Player ").append(i % 2 + 1).append("\"]\n");
            sb.append("[Black \"Player ").append((i + 1) % 2 + 1).append("\"]\n");
            sb.append("[Result \"1-0\"]\n");
            sb.append("[Deleted \"false\"]\n");
            sb.append("\n");
            sb.append("1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Bxc6 dxc6 5. O-O\n");
        }
        Files.writeString(pgnPath, sb.toString(), StandardCharsets.UTF_8);

        // Создаем индекс
        List<GameIndexEntry> entries = new ArrayList<>();
        long offset = 0;
        int gameLength = sb.toString().length() / 20;
        for (int i = 1; i <= 20; i++) {
            entries.add(GameIndexEntry.builder()
                    .id(i)
                    .offset(offset)
                    .length(gameLength)
                    .version(1)
                    .deleted(false)
                    .white("Player " + (i % 2 + 1))
                    .black("Player " + ((i + 1) % 2 + 1))
                    .result("1-0")
                    .build());
            offset += gameLength;
        }

        index = indexManager.createIndex(pgnPath, entries);
        indexManager.saveIndex(pgnPath, index);
        batchOperation = new PgnBatchOperation(pgnPath, index);
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ deleteGamesBatch()
    // ============================================================

    @Nested
    @DisplayName("deleteGamesBatch() - Пакетное удаление")
    class DeleteGamesBatchTests {

        @Test
        @DisplayName("Должен удалять выбранные партии")
        void shouldDeleteSelectedGames() throws IOException {
            // given
            List<GameIndexEntry> entriesToDelete = List.of(
                    index.getEntryById(1),
                    index.getEntryById(3),
                    index.getEntryById(5),
                    index.getEntryById(7),
                    index.getEntryById(9)
            );

            int initialActiveCount = index.getActiveCount();

            // when
            BatchOperationResult result = batchOperation.deleteGamesBatch(entriesToDelete, null);

            // then
            assertThat(result.successful()).isEqualTo(5);
            assertThat(result.totalRequested()).isEqualTo(5);
            assertThat(result.isComplete()).isTrue();
            assertThat(result.failed()).isEqualTo(0);

            // Проверяем, что партии помечены как удаленные
            PgnIndex updatedIndex = indexManager.loadIndex(pgnPath);
            for (int id : List.of(1, 3, 5, 7, 9)) {
                assertThat(updatedIndex.getEntryById(id).isDeleted()).isTrue();
            }
            // Проверяем, что остальные партии не удалены
            for (int id : List.of(2, 4, 6, 8, 10)) {
                assertThat(updatedIndex.getEntryById(id).isDeleted()).isFalse();
            }
            assertThat(updatedIndex.getActiveCount()).isEqualTo(initialActiveCount - 5);
        }

        @Test
        @DisplayName("Должен обрабатывать прогресс при удалении")
        void shouldHandleProgressDuringDelete() throws IOException {
            // given
            List<GameIndexEntry> entriesToDelete = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                entriesToDelete.add(index.getEntryById(i));
            }

            AtomicInteger progressCount = new AtomicInteger(0);
            List<Integer> progressValues = new ArrayList<>();

            // when
            BatchOperationResult result = batchOperation.deleteGamesBatch(
                    entriesToDelete,
                    processed -> {
                        progressCount.set(processed);
                        progressValues.add(processed);
                    }
            );

            // then
            assertThat(result.successful()).isEqualTo(10);

            // ✅ ИСПРАВЛЕНО: проверяем проценты (10%, 20%, ..., 100%)
            assertThat(progressCount.get()).isEqualTo(100);  // последний прогресс = 100%
            assertThat(progressValues).hasSize(10);
            assertThat(progressValues).contains(10, 50, 100);  // проценты
            assertThat(progressValues).containsExactly(10, 20, 30, 40, 50, 60, 70, 80, 90, 100);
        }

        @Test
        @DisplayName("Должен обрабатывать ошибки при удалении несуществующих партий")
        void shouldHandleErrorsWhenDeletingNonExistentGames() throws IOException {
            // given
            GameIndexEntry nonExistentEntry = GameIndexEntry.builder()
                    .id(999)
                    .offset(0)
                    .length(100)
                    .version(1)
                    .deleted(false)
                    .build();

            List<GameIndexEntry> entriesToDelete = List.of(
                    index.getEntryById(1),
                    nonExistentEntry,
                    index.getEntryById(2),
                    index.getEntryById(3)
            );

            // when
            BatchOperationResult result = batchOperation.deleteGamesBatch(entriesToDelete, null);

            // then
            assertThat(result.successful()).isEqualTo(3);
            assertThat(result.totalRequested()).isEqualTo(4);
            assertThat(result.failed()).isEqualTo(1);
            assertThat(result.failedIds()).contains(999);
            assertThat(result.isComplete()).isFalse();
        }

        @Test
        @DisplayName("Должен пропускать уже удаленные партии")
        void shouldSkipAlreadyDeletedGames() throws IOException {
            // given
            // Сначала удаляем партию 1
            List<GameIndexEntry> firstDelete = List.of(index.getEntryById(1));
            batchOperation.deleteGamesBatch(firstDelete, null);

            // Пытаемся удалить ее снова вместе с другими
            List<GameIndexEntry> entriesToDelete = List.of(
                    index.getEntryById(1),  // уже удалена
                    index.getEntryById(2),
                    index.getEntryById(3)
            );

            // when
            BatchOperationResult result = batchOperation.deleteGamesBatch(entriesToDelete, null);

            // then
            assertThat(result.successful()).isEqualTo(2); // 2 и 3 удалились
            assertThat(result.totalRequested()).isEqualTo(3);
            assertThat(result.failed()).isEqualTo(1);
            assertThat(result.failedIds()).contains(1);
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при превышении лимита")
        void shouldThrowExceptionWhenExceedingLimit() {
            // given
            List<GameIndexEntry> tooManyEntries = new ArrayList<>();
            for (int i = 0; i < PgnBatchOperation.MAX_BATCH_GAMES + 1; i++) {
                tooManyEntries.add(GameIndexEntry.builder()
                        .id(i + 1)
                        .offset(0)
                        .length(100)
                        .version(1)
                        .deleted(false)
                        .build());
            }

            // when/then
            assertThatThrownBy(() -> batchOperation.deleteGamesBatch(tooManyEntries, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(String.format(lang.get(LanguageKeys.PGN_BATCH_EXCEPTION_LIMIT), PgnBatchOperation.MAX_BATCH_GAMES + 1));
        }

        @Test
        @DisplayName("Должен корректно обрабатывать удаление большого количества партий (больше BATCH_SIZE)")
        void shouldHandleDeletingMoreThanBatchSize() throws IOException {
            // given
            // Сначала создаем достаточно партий в файле
            int totalGames = 1500;
            createTestPgnFile(totalGames);
            recreateIndex(totalGames);
            batchOperation = new PgnBatchOperation(pgnPath, index);

            // Теперь удаляем 1100 партий (больше BATCH_SIZE)
            List<GameIndexEntry> entriesToDelete = new ArrayList<>();
            int deleteCount = PgnBatchOperation.BATCH_SIZE * 2 + 100; // 1100
            for (int i = 1; i <= deleteCount; i++) {
                entriesToDelete.add(index.getEntryById(i));
            }

            int initialActiveCount = index.getActiveCount();

            // when
            BatchOperationResult result = batchOperation.deleteGamesBatch(entriesToDelete, null);

            // then
            assertThat(result.successful()).isEqualTo(deleteCount);
            assertThat(result.totalRequested()).isEqualTo(deleteCount);
            assertThat(result.isComplete()).isTrue();

            PgnIndex updatedIndex = indexManager.loadIndex(pgnPath);
            assertThat(updatedIndex.getActiveCount()).isEqualTo(initialActiveCount - deleteCount);
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ pasteGamesBatch()
    // ============================================================

    @Nested
    @DisplayName("pasteGamesBatch() - Пакетная вставка")
    class PasteGamesBatchTests {

        @Test
        @DisplayName("Должен вставлять новые партии")
        void shouldPasteNewGames() throws IOException {
            // given
            List<String> pgnContents = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                pgnContents.add("[Event \"New Game " + i + "\"]\n" +
                        "[White \"New Player\"]\n" +
                        "[Black \"Another Player\"]\n" +
                        "[Result \"1-0\"]\n" +
                        "\n" +
                        "1. d4 d5 2. c4 e6 3. Nc3 Nf6\n");
            }

            int initialCount = index.getGameCount();

            // when
            BatchOperationResult result = batchOperation.pasteGamesBatch(pgnContents, null);

            // then
            assertThat(result.successful()).isEqualTo(5);
            assertThat(result.totalRequested()).isEqualTo(5);
            assertThat(result.isComplete()).isTrue();

            // Проверяем, что партии добавлены
            PgnIndex updatedIndex = indexManager.loadIndex(pgnPath);
            assertThat(updatedIndex.getGameCount()).isEqualTo(initialCount + 5);
            assertThat(updatedIndex.getActiveCount()).isEqualTo(initialCount + 5);

            // Проверяем, что заголовки корректно извлечены
            for (int i = 1; i <= 5; i++) {
                int id = initialCount + i;
                GameIndexEntry entry = updatedIndex.getEntryById(id);
                assertThat(entry).isNotNull();
                assertThat(entry.getWhite()).isEqualTo("New Player");
                assertThat(entry.getBlack()).isEqualTo("Another Player");
                assertThat(entry.getResult()).isEqualTo("1-0");
                assertThat(entry.getEvent()).isEqualTo("New Game " + i);
            }
        }

        @Test
        @DisplayName("Должен обрабатывать прогресс при вставке")
        void shouldHandleProgressDuringPaste() throws IOException {
            // given
            List<String> pgnContents = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                pgnContents.add("[Event \"New Game " + i + "\"]\n" +
                        "[White \"New Player\"]\n" +
                        "[Black \"Another Player\"]\n" +
                        "[Result \"1-0\"]\n" +
                        "\n" +
                        "1. d4 d5 2. c4 e6 3. Nc3 Nf6\n");
            }

            AtomicInteger progressCount = new AtomicInteger(0);
            List<Integer> progressValues = new ArrayList<>();

            // when
            BatchOperationResult result = batchOperation.pasteGamesBatch(
                    pgnContents,
                    processed -> {
                        progressCount.set(processed);
                        progressValues.add(processed);
                    }
            );

            // then
            assertThat(result.successful()).isEqualTo(10);
            assertThat(progressCount.get()).isEqualTo(10);
            assertThat(progressValues).hasSize(10);
            assertThat(progressValues).contains(1, 5, 10);
        }

        @Test
        @DisplayName("Должен частично вставлять при ошибке")
        void shouldPartiallyPasteWhenError() throws IOException {
            // given
            List<String> pgnContents = createPgnContents();

            int initialCount = index.getGameCount();

            // when
            BatchOperationResult result = batchOperation.pasteGamesBatch(pgnContents, null);

            // then
            assertThat(result).isNotNull();
            assertThat(result.totalRequested()).isEqualTo(3);
            assertThat(result.successful()).isEqualTo(2); // 2 валидные партии
            assertThat(result.failed()).isEqualTo(1);    // 1 невалидная

            // Проверяем, что индекс обновился
            PgnIndex updatedIndex = indexManager.loadIndex(pgnPath);
            assertThat(updatedIndex.getGameCount()).isEqualTo(initialCount + 2);
        }

        private static List<String> createPgnContents() {
            List<String> pgnContents = new ArrayList<>();
            pgnContents.add("""
            [Event "Valid Game 1"]
            [White "Player1"]
            [Black "Player2"]
            [Result "1-0"]
            
            1. e4 e5
            """);

            // Совершенно невалидная строка (не PGN)
            pgnContents.add("This is not a valid PGN at all! @#$%^&*()");

            pgnContents.add("""
            [Event "Valid Game 2"]
            [White "Player3"]
            [Black "Player4"]
            [Result "1-0"]
            
            1. d4 d5 2. c4 e6 3. Nc3 Nf6
            """);
            return pgnContents;
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при превышении лимита")
        void shouldThrowExceptionWhenExceedingLimit() {
            // given
            List<String> tooManyContents = new ArrayList<>();
            for (int i = 0; i < PgnBatchOperation.MAX_BATCH_GAMES + 1; i++) {
                tooManyContents.add("""
                        [Event "Test"]
                        [White "Player"]
                        [Black "Player"]
                        [Result "1-0"]
                        
                        1. e4 e5
                        """);
            }

            // when/then
            assertThatThrownBy(() -> batchOperation.pasteGamesBatch(tooManyContents, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(String.format(lang.get(LanguageKeys.PGN_BATCH_EXCEPTION_LIMIT), PgnBatchOperation.MAX_BATCH_GAMES + 1));
        }

        @Test
        @DisplayName("Должен корректно вставлять большое количество партий (больше BATCH_SIZE)")
        void shouldHandlePastingMoreThanBatchSize() throws IOException {
            // given
            int pasteCount = PgnBatchOperation.BATCH_SIZE + 50; // 550

            // Создаем список с 550 партиями
            List<String> pgnContents = new ArrayList<>();
            for (int i = 1; i <= pasteCount; i++) {
                pgnContents.add("[Event \"New Game " + i + "\"]\n" +
                        "[White \"Player" + i + "\"]\n" +
                        "[Black \"Player" + (i + 1) + "\"]\n" +
                        "[Result \"1-0\"]\n" +
                        "\n" +
                        "1. d4 d5 2. c4 e6 3. Nc3 Nf6\n");
            }

            int initialCount = index.getGameCount();

            // when
            BatchOperationResult result = batchOperation.pasteGamesBatch(pgnContents, null);

            // then
            assertThat(result.successful()).isEqualTo(pasteCount);
            assertThat(result.totalRequested()).isEqualTo(pasteCount);
            assertThat(result.isComplete()).isTrue();

            PgnIndex updatedIndex = indexManager.loadIndex(pgnPath);
            assertThat(updatedIndex.getGameCount()).isEqualTo(initialCount + pasteCount);
        }
    }

    // ============================================================
    // 3. ИНТЕГРАЦИОННЫЕ ТЕСТЫ
    // ============================================================

    @Nested
    @DisplayName("Интеграционные тесты")
    class IntegrationTests {

        @Test
        @DisplayName("Полный цикл: удаление → вставка → проверка индекса")
        void shouldCompleteFullCycle() throws IOException {
            // given
            int initialCount = index.getGameCount();

            // 1. Удаляем 5 партий
            List<GameIndexEntry> entriesToDelete = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                entriesToDelete.add(index.getEntryById(i));
            }
            BatchOperationResult deleteResult = batchOperation.deleteGamesBatch(entriesToDelete, null);
            assertThat(deleteResult.successful()).isEqualTo(5);

            // 2. Вставляем 3 новые партии
            List<String> pgnContents = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                pgnContents.add("[Event \"New Game " + i + "\"]\n" +
                        "[White \"New Player\"]\n" +
                        "[Black \"Another Player\"]\n" +
                        "[Result \"1-0\"]\n" +
                        "\n" +
                        "1. d4 d5 2. c4 e6 3. Nc3 Nf6\n");
            }
            BatchOperationResult pasteResult = batchOperation.pasteGamesBatch(pgnContents, null);
            assertThat(pasteResult.successful()).isEqualTo(3);

            // 3. Проверяем индекс
            PgnIndex updatedIndex = indexManager.loadIndex(pgnPath);
            assertThat(updatedIndex.getGameCount()).isEqualTo(initialCount + 3); // 20 - 5 + 3 = 18
            assertThat(updatedIndex.getActiveCount()).isEqualTo(initialCount + 3 - 5); // 20 - 5 + 3 = 18

            // Проверяем, что удаленные партии помечены
            for (int i = 1; i <= 5; i++) {
                assertThat(updatedIndex.getEntryById(i).isDeleted()).isTrue();
            }

            // Проверяем, что новые партии добавлены
            for (int i = 1; i <= 3; i++) {
                int id = initialCount + i;
                GameIndexEntry entry = updatedIndex.getEntryById(id);
                assertThat(entry).isNotNull();
                assertThat(entry.getWhite()).isEqualTo("New Player");
                assertThat(entry.isDeleted()).isFalse();
            }
        }

        @Test
        @DisplayName("Должен сохранять индекс после каждого чанка")
        void shouldSaveIndexAfterEachChunk() throws IOException {
            // given
            // Сначала создаем больше партий в файле
            int totalGames = 1500;
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= totalGames; i++) {
                sb.append("[Event \"Test Game ").append(i).append("\"]\n");
                sb.append("[White \"Player ").append(i % 2 + 1).append("\"]\n");
                sb.append("[Black \"Player ").append((i + 1) % 2 + 1).append("\"]\n");
                sb.append("[Result \"1-0\"]\n");
                sb.append("[Deleted \"false\"]\n");
                sb.append("\n");
                sb.append("1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Bxc6 dxc6 5. O-O\n");
            }
            Files.writeString(pgnPath, sb.toString(), StandardCharsets.UTF_8);

            // Пересоздаем индекс с правильным количеством записей
            List<GameIndexEntry> entries = new ArrayList<>();
            long offset = 0;
            int gameLength = sb.toString().length() / totalGames;
            for (int i = 1; i <= totalGames; i++) {
                entries.add(GameIndexEntry.builder()
                        .id(i)
                        .offset(offset)
                        .length(gameLength)
                        .version(1)
                        .deleted(false)
                        .white("Player " + (i % 2 + 1))
                        .black("Player " + ((i + 1) % 2 + 1))
                        .result("1-0")
                        .build());
                offset += gameLength;
            }

            index = indexManager.createIndex(pgnPath, entries);
            indexManager.saveIndex(pgnPath, index);
            batchOperation = new PgnBatchOperation(pgnPath, index);

            // Теперь удаляем партии
            List<GameIndexEntry> entriesToDelete = new ArrayList<>();
            int deleteCount = PgnBatchOperation.BATCH_SIZE * 2 + 100; // 1100
            for (int i = 1; i <= deleteCount; i++) {
                entriesToDelete.add(index.getEntryById(i));
            }

            int initialActiveCount = index.getActiveCount();

            // when
            BatchOperationResult result = batchOperation.deleteGamesBatch(entriesToDelete, null);

            // then
            assertThat(result.successful()).isEqualTo(deleteCount);
            assertThat(result.totalRequested()).isEqualTo(deleteCount);
            assertThat(result.isComplete()).isTrue();

            // Проверяем, что индекс сохранился и корректен
            PgnIndex updatedIndex = indexManager.loadIndex(pgnPath);
            assertThat(updatedIndex).isNotNull();
            assertThat(updatedIndex.getActiveCount()).isEqualTo(initialActiveCount - deleteCount);
        }
    }

    private void createTestPgnFile(int count) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= count; i++) {
            sb.append("[Event \"Test Game ").append(i).append("\"]\n");
            sb.append("[White \"Player ").append(i % 2 + 1).append("\"]\n");
            sb.append("[Black \"Player ").append((i + 1) % 2 + 1).append("\"]\n");
            sb.append("[Result \"1-0\"]\n");
            sb.append("[Deleted \"false\"]\n");
            sb.append("\n");
            sb.append("1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Bxc6 dxc6 5. O-O\n");
        }
        Files.writeString(pgnPath, sb.toString(), StandardCharsets.UTF_8);
    }

    private void recreateIndex(int count) throws IOException {
        List<GameIndexEntry> entries = new ArrayList<>();
        long offset = 0;
        String content = Files.readString(pgnPath);
        int gameLength = content.length() / count;

        for (int i = 1; i <= count; i++) {
            entries.add(GameIndexEntry.builder()
                    .id(i)
                    .offset(offset)
                    .length(gameLength)
                    .version(1)
                    .deleted(false)
                    .white("Player " + (i % 2 + 1))
                    .black("Player " + ((i + 1) % 2 + 1))
                    .result("1-0")
                    .build());
            offset += gameLength;
        }

        index = indexManager.createIndex(pgnPath, entries);
        indexManager.saveIndex(pgnPath, index);
    }
}