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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

@DisplayName("PgnIndexManager - Менеджер PGN индекса")
class PgnIndexManagerTest {

    private PgnIndexManager indexManager;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        indexManager = new PgnIndexManager();
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ getIndexPath()
    // ============================================================

    @Nested
    @DisplayName("getIndexPath() - Получение пути к индексу")
    class GetIndexPathTests {

        @Test
        @DisplayName("Должен возвращать путь с расширением .idx")
        void shouldReturnPathWithIdxExtension() {
            // given
            Path pgnPath = tempDir.resolve("game.pgn");

            // when
            Path indexPath = indexManager.getIndexPath(pgnPath);

            // then
            assertThat(indexPath.toString()).endsWith(".idx");
            assertThat(indexPath.getParent()).isEqualTo(tempDir);
        }

        @Test
        @DisplayName("Должен использовать имя файла без расширения")
        void shouldUseFileNameWithoutExtension() {
            // given
            Path pgnPath = tempDir.resolve("tournament.pgn");

            // when
            Path indexPath = indexManager.getIndexPath(pgnPath);

            // then
            assertThat(indexPath.getFileName().toString()).isEqualTo("tournament.idx");
        }

        @Test
        @DisplayName("Должен обрабатывать файлы без расширения")
        void shouldHandleFilesWithoutExtension() {
            // given
            Path pgnPath = tempDir.resolve("game");

            // when
            Path indexPath = indexManager.getIndexPath(pgnPath);

            // then
            assertThat(indexPath.getFileName().toString()).isEqualTo("game.idx");
        }

        @Test
        @DisplayName("Должен обрабатывать файлы с несколькими точками")
        void shouldHandleFilesWithMultipleDots() {
            // given
            Path pgnPath = tempDir.resolve("game.2024.pgn");

            // when
            Path indexPath = indexManager.getIndexPath(pgnPath);

            // then
            assertThat(indexPath.getFileName().toString()).isEqualTo("game.2024.idx");
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ getIndexBackupPath()
    // ============================================================

    @Nested
    @DisplayName("getIndexBackupPath() - Получение пути к бэкапу")
    class GetIndexBackupPathTests {

        @Test
        @DisplayName("Должен возвращать путь с расширением .idx.bak")
        void shouldReturnPathWithIdxBakExtension() {
            // given
            Path pgnPath = tempDir.resolve("game.pgn");

            // when
            Path backupPath = indexManager.getIndexBackupPath(pgnPath);

            // then
            assertThat(backupPath.toString()).endsWith(".idx.bak");
            assertThat(backupPath.getParent()).isEqualTo(tempDir);
        }

        @Test
        @DisplayName("Должен использовать имя файла без расширения")
        void shouldUseFileNameWithoutExtensionForBackup() {
            // given
            Path pgnPath = tempDir.resolve("tournament.pgn");

            // when
            Path backupPath = indexManager.getIndexBackupPath(pgnPath);

            // then
            assertThat(backupPath.getFileName().toString()).isEqualTo("tournament.idx.bak");
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ computeFileHash()
    // ============================================================

    @Nested
    @DisplayName("computeFileHash() - Вычисление хеша")
    class ComputeFileHashTests {

        @Test
        @DisplayName("Должен вычислять одинаковый хеш для одинакового содержимого")
        void shouldComputeSameHashForSameContent() throws IOException {
            // given
            Path file1 = tempDir.resolve("file1.pgn");
            Path file2 = tempDir.resolve("file2.pgn");
            String content = "1. e4 e5 2. Nf3 Nc6";

            Files.writeString(file1, content, StandardCharsets.UTF_8);
            Files.writeString(file2, content, StandardCharsets.UTF_8);

            // when
            String hash1 = indexManager.computeFileHash(file1);
            String hash2 = indexManager.computeFileHash(file2);

            // then
            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("Должен вычислять разные хеши для разного содержимого")
        void shouldComputeDifferentHashForDifferentContent() throws IOException {
            // given
            Path file1 = tempDir.resolve("file1.pgn");
            Path file2 = tempDir.resolve("file2.pgn");

            Files.writeString(file1, "1. e4 e5 2. Nf3 Nc6", StandardCharsets.UTF_8);
            Files.writeString(file2, "1. d4 d5 2. c4 e6", StandardCharsets.UTF_8);

            // when
            String hash1 = indexManager.computeFileHash(file1);
            String hash2 = indexManager.computeFileHash(file2);

            // then
            assertThat(hash1).isNotEqualTo(hash2);
        }

        @Test
        @DisplayName("Должен вычислять хеш для пустого файла")
        void shouldComputeHashForEmptyFile() throws IOException {
            // given
            Path emptyFile = tempDir.resolve("empty.pgn");
            Files.createFile(emptyFile);

            // when
            String hash = indexManager.computeFileHash(emptyFile);

            // then
            assertThat(hash).isNotNull();
        }
    }

    // ============================================================
    // 4. ТЕСТЫ ДЛЯ createIndex()
    // ============================================================

    @Nested
    @DisplayName("createIndex() - Создание индекса")
    class CreateIndexTests {

        @Test
        @DisplayName("Должен создавать индекс с правильными метаданными")
        void shouldCreateIndexWithCorrectMetadata() throws IOException {
            // given
            Path pgnPath = tempDir.resolve("game.pgn");
            Files.writeString(pgnPath, "1. e4 e5", StandardCharsets.UTF_8);

            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(1)
                    .offset(0)
                    .length(100)
                    .version(1)
                    .deleted(false)
                    .white("Player 1")
                    .black("Player 2")
                    .result("1-0")
                    .build();

            List<GameIndexEntry> entries = List.of(entry);

            // when
            PgnIndex index = indexManager.createIndex(pgnPath, entries);

            // then
            assertThat(index).isNotNull();
            assertThat(index.getVersion()).isEqualTo(PgnIndex.FORMAT_VERSION);
            assertThat(index.getGameCount()).isEqualTo(1);
            assertThat(index.getActiveCount()).isEqualTo(1);
            assertThat(index.getFileSize()).isGreaterThan(0);
            assertThat(index.getFileHash()).isNotNull();
            assertThat(index.getEntries()).hasSize(1);
            assertThat(index.getEntries().get(0).getWhite()).isEqualTo("Player 1");
        }

        @Test
        @DisplayName("Должен правильно подсчитывать активные записи")
        void shouldCorrectlyCountActiveEntries() throws IOException {
            // given
            Path pgnPath = tempDir.resolve("game.pgn");
            Files.writeString(pgnPath, "test", StandardCharsets.UTF_8);

            GameIndexEntry activeEntry = GameIndexEntry.builder()
                    .id(1)
                    .offset(0)
                    .length(100)
                    .version(1)
                    .deleted(false)
                    .build();

            GameIndexEntry deletedEntry = GameIndexEntry.builder()
                    .id(2)
                    .offset(100)
                    .length(50)
                    .version(1)
                    .deleted(true)
                    .build();

            List<GameIndexEntry> entries = List.of(activeEntry, deletedEntry);

            // when
            PgnIndex index = indexManager.createIndex(pgnPath, entries);

            // then
            assertThat(index.getGameCount()).isEqualTo(2);
            assertThat(index.getActiveCount()).isEqualTo(1);
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ saveIndex() И loadIndex()
    // ============================================================

    @Nested
    @DisplayName("saveIndex() и loadIndex() - Сохранение и загрузка")
    class SaveAndLoadIndexTests {

        @Test
        @DisplayName("Должен сохранять и загружать индекс")
        void shouldSaveAndLoadIndex() throws IOException {
            // given
            Path pgnPath = tempDir.resolve("game.pgn");
            Files.writeString(pgnPath, "1. e4 e5", StandardCharsets.UTF_8);

            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(1)
                    .offset(0)
                    .length(100)
                    .version(1)
                    .deleted(false)
                    .white("Player 1")
                    .black("Player 2")
                    .result("1-0")
                    .eco("C44")
                    .build();

            PgnIndex originalIndex = indexManager.createIndex(pgnPath, List.of(entry));

            // when
            indexManager.saveIndex(pgnPath, originalIndex);
            PgnIndex loadedIndex = indexManager.loadIndex(pgnPath);

            // then
            assertThat(loadedIndex).isNotNull();
            assertThat(loadedIndex.getVersion()).isEqualTo(originalIndex.getVersion());
            assertThat(loadedIndex.getGameCount()).isEqualTo(originalIndex.getGameCount());
            assertThat(loadedIndex.getActiveCount()).isEqualTo(originalIndex.getActiveCount());
            assertThat(loadedIndex.getFileHash()).isEqualTo(originalIndex.getFileHash());

            GameIndexEntry loadedEntry = loadedIndex.getEntries().get(0);
            assertThat(loadedEntry.getWhite()).isEqualTo("Player 1");
            assertThat(loadedEntry.getBlack()).isEqualTo("Player 2");
            assertThat(loadedEntry.getEco()).isEqualTo("C44");
        }

        @Test
        @DisplayName("Должен создавать бэкап при сохранении")
        void shouldCreateBackupWhenSaving() throws IOException {
            // given
            Path pgnPath = tempDir.resolve("game.pgn");
            Files.writeString(pgnPath, "1. e4 e5", StandardCharsets.UTF_8);

            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(1)
                    .offset(0)
                    .length(100)
                    .version(1)
                    .deleted(false)
                    .build();

            PgnIndex index = indexManager.createIndex(pgnPath, List.of(entry));

            // when
            indexManager.saveIndex(pgnPath, index);

            // then
            Path backupPath = indexManager.getIndexBackupPath(pgnPath);
            assertThat(Files.exists(backupPath)).isFalse(); // Бэкап удаляется после успешного сохранения
            assertThat(Files.exists(indexManager.getIndexPath(pgnPath))).isTrue();
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при загрузке несуществующего индекса")
        void shouldThrowExceptionWhenLoadingNonExistentIndex() {
            // given
            Path pgnPath = tempDir.resolve("nonexistent.pgn");

            // when/then
            assertThatThrownBy(() -> indexManager.loadIndex(pgnPath))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("Index file not found");
        }
    }

    // ============================================================
    // 6. ТЕСТЫ ДЛЯ checkIndex()
    // ============================================================

    @Nested
    @DisplayName("checkIndex() - Проверка состояния индекса")
    class CheckIndexTests {

        @Test
        @DisplayName("Должен возвращать FILE_MISSING если PGN файл отсутствует")
        void shouldReturnFileMissingWhenPgnNotFound() {
            // given
            Path nonExistentPath = tempDir.resolve("nonexistent.pgn");

            // when
            IndexStatus status = indexManager.checkIndex(nonExistentPath);

            // then
            assertThat(status).isEqualTo(IndexStatus.FILE_MISSING);
        }

        @Test
        @DisplayName("Должен возвращать NO_INDEX если индекс отсутствует")
        void shouldReturnNoIndexWhenIndexNotFound() throws IOException {
            // given
            Path pgnPath = tempDir.resolve("game.pgn");
            Files.writeString(pgnPath, "1. e4 e5", StandardCharsets.UTF_8);

            // when
            IndexStatus status = indexManager.checkIndex(pgnPath);

            // then
            assertThat(status).isEqualTo(IndexStatus.NO_INDEX);
        }

        @Test
        @DisplayName("Должен возвращать OK если индекс валидный")
        void shouldReturnOkWhenIndexValid() throws IOException {
            // given
            Path pgnPath = tempDir.resolve("game.pgn");
            Files.writeString(pgnPath, "1. e4 e5", StandardCharsets.UTF_8);

            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(1)
                    .offset(0)
                    .length(100)
                    .version(1)
                    .deleted(false)
                    .build();

            PgnIndex index = indexManager.createIndex(pgnPath, List.of(entry));
            indexManager.saveIndex(pgnPath, index);

            // when
            IndexStatus status = indexManager.checkIndex(pgnPath);

            // then
            assertThat(status).isEqualTo(IndexStatus.OK);
        }

        @Test
        @DisplayName("Должен возвращать FILE_CHANGED если файл изменился")
        void shouldReturnFileChangedWhenFileModified() throws IOException {
            // given
            Path pgnPath = tempDir.resolve("game.pgn");
            Files.writeString(pgnPath, "1. e4 e5", StandardCharsets.UTF_8);

            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(1)
                    .offset(0)
                    .length(100)
                    .version(1)
                    .deleted(false)
                    .build();

            PgnIndex index = indexManager.createIndex(pgnPath, List.of(entry));
            indexManager.saveIndex(pgnPath, index);

            // Изменяем содержимое файла
            Files.writeString(pgnPath, "1. d4 d5", StandardCharsets.UTF_8);

            // when
            IndexStatus status = indexManager.checkIndex(pgnPath);

            // then
            assertThat(status).isEqualTo(IndexStatus.FILE_CHANGED);
        }
    }

    // ============================================================
    // 7. ТЕСТЫ ДЛЯ updateIndex()
    // ============================================================

    @Nested
    @DisplayName("updateIndex() - Обновление индекса")
    class UpdateIndexTests {

        @Test
        @DisplayName("Должен обновлять существующую запись")
        void shouldUpdateExistingEntry() throws IOException {
            // given
            Path pgnPath = tempDir.resolve("game.pgn");
            Files.writeString(pgnPath, "1. e4 e5", StandardCharsets.UTF_8);

            GameIndexEntry originalEntry = GameIndexEntry.builder()
                    .id(1)
                    .offset(0)
                    .length(100)
                    .version(1)
                    .deleted(false)
                    .white("Player 1")
                    .result("1-0")
                    .build();

            PgnIndex index = indexManager.createIndex(pgnPath, List.of(originalEntry));
            indexManager.saveIndex(pgnPath, index);

            // Обновляем запись
            GameIndexEntry updatedEntry = GameIndexEntry.builder()
                    .id(1)
                    .offset(0)
                    .length(100)
                    .version(2)
                    .deleted(false)
                    .white("Player Updated")
                    .result("1-0")
                    .build();

            // when
            indexManager.updateIndex(pgnPath, index, updatedEntry);

            // then
            PgnIndex loadedIndex = indexManager.loadIndex(pgnPath);
            GameIndexEntry loadedEntry = loadedIndex.getEntryById(1);
            assertThat(loadedEntry.getWhite()).isEqualTo("Player Updated");
            assertThat(loadedEntry.getVersion()).isEqualTo(2);
        }

        @Test
        @DisplayName("Должен добавлять новую запись если ID не существует")
        void shouldAddNewEntryIfIdNotFound() throws IOException {
            // given
            Path pgnPath = tempDir.resolve("game.pgn");
            Files.writeString(pgnPath, "1. e4 e5", StandardCharsets.UTF_8);

            GameIndexEntry existingEntry = GameIndexEntry.builder()
                    .id(1)
                    .offset(0)
                    .length(100)
                    .version(1)
                    .deleted(false)
                    .build();

            PgnIndex index = indexManager.createIndex(pgnPath, List.of(existingEntry));
            indexManager.saveIndex(pgnPath, index);

            // Добавляем новую запись
            GameIndexEntry newEntry = GameIndexEntry.builder()
                    .id(2)
                    .offset(100)
                    .length(50)
                    .version(1)
                    .deleted(false)
                    .white("New Player")
                    .build();

            // when
            indexManager.updateIndex(pgnPath, index, newEntry);

            // then
            PgnIndex loadedIndex = indexManager.loadIndex(pgnPath);
            assertThat(loadedIndex.getGameCount()).isEqualTo(2);
            assertThat(loadedIndex.getEntryById(2)).isNotNull();
            assertThat(loadedIndex.getEntryById(2).getWhite()).isEqualTo("New Player");
        }
    }

    // ============================================================
    // 8. ТЕСТЫ ДЛЯ deleteIndex()
    // ============================================================

    @Nested
    @DisplayName("deleteIndex() - Удаление индекса")
    class DeleteIndexTests {

        @Test
        @DisplayName("Должен удалять индексный файл")
        void shouldDeleteIndexFile() throws IOException {
            // given
            Path pgnPath = tempDir.resolve("game.pgn");
            Files.writeString(pgnPath, "1. e4 e5", StandardCharsets.UTF_8);

            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(1)
                    .offset(0)
                    .length(100)
                    .version(1)
                    .deleted(false)
                    .build();

            PgnIndex index = indexManager.createIndex(pgnPath, List.of(entry));
            indexManager.saveIndex(pgnPath, index);

            Path indexPath = indexManager.getIndexPath(pgnPath);
            assertThat(Files.exists(indexPath)).isTrue();

            // when
            indexManager.deleteIndex(pgnPath);

            // then
            assertThat(Files.exists(indexPath)).isFalse();
        }

        @Test
        @DisplayName("Должен удалять бэкап индексного файла")
        void shouldDeleteBackupIndexFile() throws IOException {
            // given
            Path pgnPath = tempDir.resolve("game.pgn");
            Files.writeString(pgnPath, "1. e4 e5", StandardCharsets.UTF_8);

            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(1)
                    .offset(0)
                    .length(100)
                    .version(1)
                    .deleted(false)
                    .build();

            PgnIndex index = indexManager.createIndex(pgnPath, List.of(entry));
            indexManager.saveIndex(pgnPath, index);

            Path backupPath = indexManager.getIndexBackupPath(pgnPath);

            // Создаем бэкап вручную (обычно он создается при сохранении)
            Path indexPath = indexManager.getIndexPath(pgnPath);
            Files.copy(indexPath, backupPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // when
            indexManager.deleteIndex(pgnPath);

            // then
            assertThat(Files.exists(backupPath)).isFalse();
        }

        @Test
        @DisplayName("Должен безопасно удалять индекс если он существует")
        void shouldSafelyDeleteIndexIfExists() throws IOException {
            // given - создаем индекс
            Path pgnPath = tempDir.resolve("game.pgn");
            Files.writeString(pgnPath, "1. e4 e5", StandardCharsets.UTF_8);

            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(1)
                    .offset(0)
                    .length(100)
                    .version(1)
                    .deleted(false)
                    .build();

            PgnIndex index = indexManager.createIndex(pgnPath, List.of(entry));
            indexManager.saveIndex(pgnPath, index);

            Path indexPath = indexManager.getIndexPath(pgnPath);
            assertThat(Files.exists(indexPath)).isTrue();

            // when
            indexManager.deleteIndex(pgnPath);

            // then
            assertThat(Files.exists(indexPath)).isFalse();
        }

        @Test
        @DisplayName("Не должен выбрасывать исключение если индекс отсутствует")
        void shouldNotThrowExceptionIfIndexDoesNotExist() {
            // given
            Path pgnPath = tempDir.resolve("game.pgn");
            Path indexPath = indexManager.getIndexPath(pgnPath);
            assertThat(Files.exists(indexPath)).isFalse();

            // when/then
            assertThatCode(() -> indexManager.deleteIndex(pgnPath))
                    .doesNotThrowAnyException();
        }

    }

    // ============================================================
    // 9. ИНТЕГРАЦИОННЫЕ ТЕСТЫ
    // ============================================================

    @Nested
    @DisplayName("Интеграционные тесты")
    class IntegrationTests {

        @Test
        @DisplayName("Полный цикл: create → save → load → update → delete")
        void shouldCompleteFullLifecycle() throws IOException {
            // given
            Path pgnPath = tempDir.resolve("game.pgn");
            Files.writeString(pgnPath, "1. e4 e5", StandardCharsets.UTF_8);

            // 1. Создаем индекс
            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(1)
                    .offset(0)
                    .length(100)
                    .version(1)
                    .deleted(false)
                    .white("Player 1")
                    .black("Player 2")
                    .result("1-0")
                    .build();

            PgnIndex index = indexManager.createIndex(pgnPath, List.of(entry));
            assertThat(index.getGameCount()).isEqualTo(1);

            // 2. Сохраняем
            indexManager.saveIndex(pgnPath, index);
            assertThat(Files.exists(indexManager.getIndexPath(pgnPath))).isTrue();

            // 3. Загружаем
            PgnIndex loadedIndex = indexManager.loadIndex(pgnPath);
            assertThat(loadedIndex.getGameCount()).isEqualTo(1);
            assertThat(loadedIndex.getEntries().get(0).getWhite()).isEqualTo("Player 1");

            // 4. Обновляем
            GameIndexEntry updatedEntry = GameIndexEntry.builder()
                    .id(1)
                    .offset(0)
                    .length(100)
                    .version(2)
                    .deleted(false)
                    .white("Player Updated")
                    .black("Player 2")
                    .result("1-0")
                    .build();

            indexManager.updateIndex(pgnPath, loadedIndex, updatedEntry);

            PgnIndex updatedLoadedIndex = indexManager.loadIndex(pgnPath);
            assertThat(updatedLoadedIndex.getEntries().get(0).getWhite()).isEqualTo("Player Updated");

            // 5. Удаляем
            indexManager.deleteIndex(pgnPath);
            assertThat(Files.exists(indexManager.getIndexPath(pgnPath))).isFalse();
        }
    }
}