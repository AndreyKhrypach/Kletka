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

import Khrypach.Andrey.chess.kletka.pgn.index.model.IndexStatus;
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
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PgnIndexingFacade - Фасад индексации PGN")
class PgnIndexingFacadeTest {

    private PgnIndexingFacade facade;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        facade = new PgnIndexingFacade();
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ indexFile()
    // ============================================================

    @Nested
    @DisplayName("indexFile() - Полная индексация")
    class IndexFileTests {

        @Test
        @DisplayName("Должен индексировать PGN файл с одной партией")
        void shouldIndexSingleGame() throws IOException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    """;
            Path pgnFile = createPgnFile("single.pgn", pgn);

            // when
            PgnIndex index = facade.indexFile(pgnFile, null);

            // then
            assertThat(index).isNotNull();
            assertThat(index.getGameCount()).isEqualTo(1);
            assertThat(index.getActiveCount()).isEqualTo(1);

            // Проверяем, что индексный файл создан
            PgnIndexManager manager = new PgnIndexManager();
            Path indexPath = manager.getIndexPath(pgnFile);
            assertThat(Files.exists(indexPath)).isTrue();
        }

        @Test
        @DisplayName("Должен индексировать PGN файл с несколькими партиями")
        void shouldIndexMultipleGames() throws IOException {
            // given
            String pgn = """
                    [Event "Game 1"]
                    [White "Player A"]
                    [Black "Player B"]
                    [Result "1-0"]
                    
                    1. e4 e5 1-0
                    
                    [Event "Game 2"]
                    [White "Player C"]
                    [Black "Player D"]
                    [Result "0-1"]
                    
                    1. d4 d5 0-1
                    
                    [Event "Game 3"]
                    [White "Player E"]
                    [Black "Player F"]
                    [Result "1/2-1/2"]
                    
                    1. e4 e5 2. Nf3 Nc6 1/2-1/2
                    """;
            Path pgnFile = createPgnFile("multiple.pgn", pgn);

            // when
            PgnIndex index = facade.indexFile(pgnFile, null);

            // then
            assertThat(index).isNotNull();
            assertThat(index.getGameCount()).isEqualTo(3);
            assertThat(index.getActiveCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Должен вызывать progressCallback при индексации")
        void shouldCallProgressCallback() throws IOException {
            // given
            String pgn = """
                    [Event "Game 1"]
                    [White "Player A"]
                    [Black "Player B"]
                    [Result "1-0"]
                    
                    1. e4 e5 1-0
                    
                    [Event "Game 2"]
                    [White "Player C"]
                    [Black "Player D"]
                    [Result "0-1"]
                    
                    1. d4 d5 0-1
                    """;
            Path pgnFile = createPgnFile("progress.pgn", pgn);
            AtomicInteger callCount = new AtomicInteger(0);

            // when
            PgnIndex index = facade.indexFile(pgnFile, progress -> {
                callCount.incrementAndGet();
                assertThat(progress).isNotNull();
            });

            // then
            assertThat(index).isNotNull();
            assertThat(callCount.get()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Должен обновлять прогресс с корректными статусами")
        void shouldUpdateProgressWithCorrectStatuses() throws IOException {
            // given
            String pgn = """
                    [Event "Game 1"]
                    [White "Player A"]
                    [Black "Player B"]
                    [Result "1-0"]
                    
                    1. e4 e5 1-0
                    """;
            Path pgnFile = createPgnFile("status.pgn", pgn);

            AtomicInteger statusCount = new AtomicInteger(0);

            // when
            PgnIndex index = facade.indexFile(pgnFile, progress -> {
                statusCount.incrementAndGet();
                assertThat(progress.getStatus()).isNotNull();
            });

            // then
            assertThat(index).isNotNull();
            assertThat(statusCount.get()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при отсутствии файла")
        void shouldThrowExceptionWhenFileNotFound() {
            // given
            Path nonExistentFile = tempDir.resolve("nonexistent.pgn");

            // when/then
            assertThatThrownBy(() -> facade.indexFile(nonExistentFile, null))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("PGN file not found");
        }

        @Test
        @DisplayName("Должен индексировать PGN с русскими заголовками")
        void shouldIndexPgnWithRussianHeaders() throws IOException {
            // given
            String pgn = """
                    [Event "Турнир"]
                    [White "Игрок 1"]
                    [Black "Игрок 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 1-0
                    """;
            Path pgnFile = createPgnFile("russian.pgn", pgn);

            // when
            PgnIndex index = facade.indexFile(pgnFile, null);

            // then
            assertThat(index).isNotNull();
            assertThat(index.getGameCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Должен добавлять тег Deleted при индексации")
        void shouldAddDeletedTagDuringIndexing() throws IOException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 1-0
                    """;
            Path pgnFile = createPgnFile("deleted.pgn", pgn);

            // when
            PgnIndex index = facade.indexFile(pgnFile, null);

            // then
            assertThat(index).isNotNull();
            assertThat(index.getGameCount()).isEqualTo(1);

            // Проверяем, что тег Deleted добавлен в файл
            String content = Files.readString(pgnFile, StandardCharsets.UTF_8);
            assertThat(content).contains("[Deleted");
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ checkIndex()
    // ============================================================

    @Nested
    @DisplayName("checkIndex() - Проверка состояния индекса")
    class CheckIndexTests {

        @Test
        @DisplayName("Должен возвращать FILE_MISSING если PGN отсутствует")
        void shouldReturnFileMissingWhenPgnNotFound() {
            // given
            Path nonExistentPath = tempDir.resolve("nonexistent.pgn");

            // when
            IndexStatus status = facade.checkIndex(nonExistentPath);

            // then
            assertThat(status).isEqualTo(IndexStatus.FILE_MISSING);
        }

        @Test
        @DisplayName("Должен возвращать NO_INDEX если индекс отсутствует")
        void shouldReturnNoIndexWhenIndexNotFound() throws IOException {
            // given
            Path pgnFile = createPgnFile("no_index.pgn", "[Event \"Game\"]\n1. e4 e5 1-0\n");

            // when
            IndexStatus status = facade.checkIndex(pgnFile);

            // then
            assertThat(status).isEqualTo(IndexStatus.NO_INDEX);
        }

        @Test
        @DisplayName("Должен возвращать OK если индекс валидный")
        void shouldReturnOkWhenIndexValid() throws IOException {
            // given
            String pgn = """
                    [Event "Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 1-0
                    """;
            Path pgnFile = createPgnFile("valid.pgn", pgn);

            // Создаем индекс
            facade.indexFile(pgnFile, null);

            // when
            IndexStatus status = facade.checkIndex(pgnFile);

            // then
            assertThat(status).isEqualTo(IndexStatus.OK);
        }

        @Test
        @DisplayName("Должен возвращать FILE_CHANGED если файл изменился после индексации")
        void shouldReturnFileChangedWhenFileModified() throws IOException {
            // given
            String pgn1 = """
                    [Event "Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 1-0
                    """;
            Path pgnFile = createPgnFile("changed.pgn", pgn1);

            // Создаем индекс
            facade.indexFile(pgnFile, null);

            // Изменяем файл
            String pgn2 = """
                    [Event "Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. d4 d5 1-0
                    """;
            Files.writeString(pgnFile, pgn2, StandardCharsets.UTF_8);

            // when
            IndexStatus status = facade.checkIndex(pgnFile);

            // then
            assertThat(status).isEqualTo(IndexStatus.FILE_CHANGED);
        }

        @Test
        @DisplayName("Должен возвращать INDEX_CORRUPTED для поврежденного индекса")
        void shouldReturnIndexCorruptedForCorruptedIndex() throws IOException {
            // given
            String pgn = """
                    [Event "Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 1-0
                    """;
            Path pgnFile = createPgnFile("corrupted.pgn", pgn);

            // Создаем индекс
            facade.indexFile(pgnFile, null);

            // Повреждаем индекс
            PgnIndexManager manager = new PgnIndexManager();
            Path indexPath = manager.getIndexPath(pgnFile);
            Files.writeString(indexPath, "corrupted data", StandardCharsets.UTF_8);

            // when
            IndexStatus status = facade.checkIndex(pgnFile);

            // then
            assertThat(status).isEqualTo(IndexStatus.INDEX_CORRUPTED);
        }
    }

    // ============================================================
    // 3. ИНТЕГРАЦИОННЫЕ ТЕСТЫ
    // ============================================================

    @Nested
    @DisplayName("Интеграционные тесты")
    class IntegrationTests {

        @Test
        @DisplayName("Полный цикл: checkIndex (NO_INDEX) → indexFile → checkIndex (OK) → modify → checkIndex (FILE_CHANGED)")
        void shouldCompleteFullCycle() throws IOException {
            // given
            String pgn = """
                    [Event "Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 1-0
                    """;
            Path pgnFile = createPgnFile("cycle.pgn", pgn);

            // 1. Проверяем до индексации
            IndexStatus statusBefore = facade.checkIndex(pgnFile);
            assertThat(statusBefore).isEqualTo(IndexStatus.NO_INDEX);

            // 2. Индексируем
            PgnIndex index = facade.indexFile(pgnFile, null);
            assertThat(index).isNotNull();
            assertThat(index.getGameCount()).isEqualTo(1);

            // 3. Проверяем после индексации
            IndexStatus statusAfter = facade.checkIndex(pgnFile);
            assertThat(statusAfter).isEqualTo(IndexStatus.OK);

            // 4. Модифицируем файл
            String modifiedPgn = """
                    [Event "Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. d4 d5 1-0
                    """;
            Files.writeString(pgnFile, modifiedPgn, StandardCharsets.UTF_8);

            // 5. Проверяем после модификации
            IndexStatus statusModified = facade.checkIndex(pgnFile);
            assertThat(statusModified).isEqualTo(IndexStatus.FILE_CHANGED);
        }

        @Test
        @DisplayName("Должен создавать индекс с корректными метаданными")
        void shouldCreateIndexWithCorrectMetadata() throws IOException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    [ECO "C44"]
                    [Opening "King's Pawn Game"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    """;
            Path pgnFile = createPgnFile("metadata.pgn", pgn);

            // when
            PgnIndex index = facade.indexFile(pgnFile, null);

            // then
            assertThat(index).isNotNull();
            assertThat(index.getVersion()).isEqualTo(1);
            assertThat(index.getFileHash()).isNotNull();
            assertThat(index.getFileSize()).isGreaterThan(0);

            PgnIndexManager manager = new PgnIndexManager();
            Path indexPath = manager.getIndexPath(pgnFile);
            assertThat(Files.exists(indexPath)).isTrue();
            assertThat(Files.size(indexPath)).isGreaterThan(0);
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
}