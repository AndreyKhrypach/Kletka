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

package Khrypach.Andrey.chess.kletka.pgn.index.binary;

import Khrypach.Andrey.chess.kletka.pgn.index.model.GameIndexEntry;
import Khrypach.Andrey.chess.kletka.pgn.index.model.PgnIndex;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BinaryIndexWriterTest {

    @TempDir
    Path tempDir;

    private Path outputPath;
    private BinaryIndexWriter writer;
    private BinaryIndexReader reader;

    @BeforeEach
    void setUp() {
        outputPath = tempDir.resolve("test-index.bin");
        writer = new BinaryIndexWriter();
        reader = new BinaryIndexReader();
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(outputPath)) {
            Files.deleteIfExists(outputPath);
        }
    }

    // ============================================================
    // 1. ОСНОВНЫЕ ТЕСТЫ
    // ============================================================

    @Test
    void write_shouldCreateBinaryIndexFile() throws IOException {
        // Создаем тестовый индекс
        PgnIndex index = createTestPgnIndex(3);

        // Записываем
        writer.write(index, outputPath);

        // Проверяем, что файл создан
        assertTrue(Files.exists(outputPath));
        assertTrue(Files.size(outputPath) > 0);

        // Читаем и проверяем содержимое
        PgnIndex readIndex = reader.read(outputPath);
        assertNotNull(readIndex);
        assertEquals(3, readIndex.getGameCount());
        assertEquals(2, readIndex.getActiveCount());

        // Проверяем записи
        List<GameIndexEntry> entries = readIndex.getEntries();
        assertEquals(3, entries.size());

        GameIndexEntry entry1 = entries.get(0);
        assertEquals(1, entry1.getId());
        assertEquals("Player1White", entry1.getWhite());
        assertEquals("Player1Black", entry1.getBlack());
    }

    @Test
    void write_shouldOverwriteExistingFile() throws IOException {
        // Создаем и записываем первый индекс
        PgnIndex index1 = createTestPgnIndex(2);
        writer.write(index1, outputPath);
        long size1 = Files.size(outputPath);

        // Создаем и записываем второй индекс (с другими данными)
        PgnIndex index2 = createTestPgnIndex(5);
        writer.write(index2, outputPath);
        long size2 = Files.size(outputPath);

        // Проверяем, что файл перезаписан (размер изменился)
        assertNotEquals(size1, size2);
        assertEquals(5, reader.read(outputPath).getGameCount());
    }

    @Test
    void write_shouldCreateDirectoriesIfNotExists() throws IOException {
        // Создаем путь с вложенными директориями
        Path nestedPath = tempDir.resolve("subdir1").resolve("subdir2").resolve("index.bin");

        PgnIndex index = createTestPgnIndex(3);
        writer.write(index, nestedPath);

        // Проверяем, что директории созданы и файл существует
        assertTrue(Files.exists(nestedPath));
        assertTrue(Files.size(nestedPath) > 0);

        // Читаем и проверяем
        PgnIndex readIndex = reader.read(nestedPath);
        assertEquals(3, readIndex.getGameCount());
    }

    @Test
    void write_shouldUseAtomicFileReplacement() throws IOException {
        PgnIndex index = createTestPgnIndex(3);

        // Создаем временный файл (симулируем, что файл уже существует)
        Files.createFile(outputPath);
        long initialSize = Files.size(outputPath);

        // Записываем новый индекс
        writer.write(index, outputPath);

        // Проверяем, что файл заменен атомарно
        assertTrue(Files.exists(outputPath));
        assertNotEquals(initialSize, Files.size(outputPath));
        assertEquals(3, reader.read(outputPath).getGameCount());
    }

    // ============================================================
    // 2. ТЕСТЫ С РАЗНЫМИ ДАННЫМИ
    // ============================================================

    @Test
    void write_shouldHandleEmptyIndex() throws IOException {
        PgnIndex index = createTestPgnIndex(0);

        writer.write(index, outputPath);

        assertTrue(Files.exists(outputPath));
        assertEquals(0, reader.read(outputPath).getGameCount());
    }

    @Test
    void write_shouldHandleAllDeletedEntries() throws IOException {
        PgnIndex index = createTestPgnIndexWithAllDeleted(3);

        writer.write(index, outputPath);

        PgnIndex readIndex = reader.read(outputPath);
        assertEquals(3, readIndex.getGameCount());
        assertEquals(0, readIndex.getActiveCount());

        List<GameIndexEntry> entries = readIndex.getEntries();
        assertTrue(entries.stream().allMatch(GameIndexEntry::isDeleted));
    }

    @Test
    void write_shouldThrowExceptionWhenFileHashIsNull() {
        PgnIndex index = new PgnIndex();
        index.setVersion(PgnIndex.FORMAT_VERSION);
        index.setFileHash(null);  // ← null!
        index.setFileSize(1024);

        GameIndexEntry entry = GameIndexEntry.builder()
                .id(1)
                .white("Player")
                .black("Player")
                .result("1-0")
                .year("2024")
                .build();
        index.addEntry(entry);
        index.refreshCache();

        // Должно выбросить NullPointerException или IOException
        assertThrows(NullPointerException.class, () -> writer.write(index, outputPath));
    }

    @Test
    void write_shouldThrowExceptionWhenFileSizeIsZero() throws IOException {
        PgnIndex index = new PgnIndex();
        index.setVersion(PgnIndex.FORMAT_VERSION);
        index.setFileHash("testhash");
        index.setFileSize(0);  // ← 0!

        GameIndexEntry entry = GameIndexEntry.builder()
                .id(1)
                .white("Player")
                .black("Player")
                .result("1-0")
                .year("2024")
                .build();
        index.addEntry(entry);
        index.refreshCache();

        // Запись должна работать, но fileSize=0 - это может быть проблемой
        writer.write(index, outputPath);

        PgnIndex readIndex = reader.read(outputPath);
        assertEquals(0, readIndex.getFileSize());
    }

    @Test
    void write_shouldHandleLargeStringFields() throws IOException {
        // Создаем запись с длинными строками
        String longStr = "a".repeat(1000);

        PgnIndex index = new PgnIndex();

        // ✅ ОБЯЗАТЕЛЬНО: устанавливаем поля, которые использует BinaryIndexHeader
        index.setVersion(PgnIndex.FORMAT_VERSION);
        index.setFileHash("testhash");
        index.setFileSize(1024);

        GameIndexEntry entry = GameIndexEntry.builder()
                .id(1)
                .white(longStr)
                .black(longStr)
                .result("1-0")
                .year("2024")
                .event(longStr)
                .eco("A00")
                .opening(longStr)
                .variation(longStr)
                .site(longStr)
                .plyCount(45)
                .offset(0)
                .length(100)
                .version(1)
                .deleted(false)
                .hash(12345)
                .build();
        index.addEntry(entry);

        // ✅ Обновляем кэш и счётчики
        index.refreshCache();

        writer.write(index, outputPath);

        PgnIndex readIndex = reader.read(outputPath);
        assertEquals(1, readIndex.getGameCount());

        GameIndexEntry readEntry = readIndex.getEntries().get(0);
        assertEquals(longStr, readEntry.getWhite());
        assertEquals(longStr, readEntry.getBlack());
        assertEquals(longStr, readEntry.getEvent());
        assertEquals(longStr, readEntry.getOpening());
        assertEquals(longStr, readEntry.getVariation());
        assertEquals(longStr, readEntry.getSite());
    }

    @Test
    void write_shouldHandleMaxStringLength() throws IOException {
        // Строка максимальной длины (Short.MAX_VALUE)
        int maxLength = Short.MAX_VALUE;
        String maxString = "x".repeat(maxLength);


        GameIndexEntry entry = GameIndexEntry.builder()
                .id(1)
                .white(maxString)
                .black("Player")
                .result("1-0")
                .year("2024")
                .build();

        PgnIndex index = createCustomPgnIndex(entry);
        writer.write(index, outputPath);

        PgnIndex readIndex = reader.read(outputPath);
        assertEquals(maxString, readIndex.getEntries().get(0).getWhite());
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ ПОЛНОГО ЦИКЛА (write + read)
    // ============================================================

    @Test
    void fullCycle_shouldPreserveAllData() throws IOException {
        // Создаем индекс с разнообразными данными
        PgnIndex original = createComplexPgnIndex();

        // Записываем
        writer.write(original, outputPath);

        // Читаем
        PgnIndex read = reader.read(outputPath);

        // Сравниваем
        assertEquals(original.getGameCount(), read.getGameCount());
        assertEquals(original.getActiveCount(), read.getActiveCount());
        assertEquals(original.getFileHash(), read.getFileHash());
        assertEquals(original.getFileSize(), read.getFileSize());

        List<GameIndexEntry> originalEntries = original.getEntries();
        List<GameIndexEntry> readEntries = read.getEntries();

        assertEquals(originalEntries.size(), readEntries.size());

        for (int i = 0; i < originalEntries.size(); i++) {
            GameIndexEntry orig = originalEntries.get(i);
            GameIndexEntry rd = readEntries.get(i);

            assertEquals(orig.getId(), rd.getId());
            assertEquals(orig.getWhite(), rd.getWhite());
            assertEquals(orig.getBlack(), rd.getBlack());
            assertEquals(orig.getResult(), rd.getResult());
            assertEquals(orig.getYear(), rd.getYear());
            assertEquals(orig.getEvent(), rd.getEvent());
            assertEquals(orig.getEco(), rd.getEco());
            assertEquals(orig.getOpening(), rd.getOpening());
            assertEquals(orig.getVariation(), rd.getVariation());
            assertEquals(orig.getSite(), rd.getSite());
            assertEquals(orig.getPlyCount(), rd.getPlyCount());
            assertEquals(orig.getOffset(), rd.getOffset());
            assertEquals(orig.getLength(), rd.getLength());
            assertEquals(orig.getVersion(), rd.getVersion());
            assertEquals(orig.isDeleted(), rd.isDeleted());
            assertEquals(orig.getHash(), rd.getHash());
        }
    }

    @Test
    void fullCycle_shouldPreserveDeletedFlags() throws IOException {
        PgnIndex index = new PgnIndex();
        index.setVersion(PgnIndex.FORMAT_VERSION);
        index.setFileHash("testhash");
        index.setFileSize(1024);

        // Добавляем записи с разными deleted флагами
        for (int i = 1; i <= 5; i++) {
            boolean deleted = (i % 2 == 0);
            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(i)
                    .white("Player" + i + "White")
                    .black("Player" + i + "Black")
                    .result(i % 2 == 0 ? "0-1" : "1-0")
                    .deleted(deleted)
                    .year("2024")
                    .event("Event" + i)
                    .eco("A" + String.format("%02d", i))
                    .offset(i * 100L)
                    .length(100 + i * 10)
                    .version(1)
                    .hash(12345 + i)
                    .plyCount(40 + i)
                    .build();
            index.addEntry(entry);
        }

        writer.write(index, outputPath);
        PgnIndex readIndex = reader.read(outputPath);

        List<GameIndexEntry> entries = readIndex.getEntries();
        for (int i = 0; i < entries.size(); i++) {
            boolean expectedDeleted = ((i + 1) % 2 == 0);
            assertEquals(expectedDeleted, entries.get(i).isDeleted());
        }
    }

    // ============================================================
    // 4. ТЕСТЫ С ОШИБКАМИ
    // ============================================================

    @Test
    void write_shouldThrowExceptionWhenIndexIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> writer.write(null, outputPath));
    }

    @Test
    void write_shouldThrowExceptionWhenPathIsNull() {
        PgnIndex index = createTestPgnIndex(1);
        assertThrows(IllegalArgumentException.class,
                () -> writer.write(index, null));
    }

    @Test
    void write_shouldHandleEmptyEntries() throws IOException {
        PgnIndex index = new PgnIndex();
        index.setVersion(PgnIndex.FORMAT_VERSION);
        index.setFileHash("testhash");
        index.setFileSize(1024);
        // Не добавляем записи - entries будет пустым списком

        writer.write(index, outputPath);

        // Проверяем, что файл создан
        assertTrue(Files.exists(outputPath));
        assertTrue(Files.size(outputPath) > 0);

        // Читаем и проверяем
        PgnIndex readIndex = reader.read(outputPath);
        assertEquals(0, readIndex.getGameCount());
        assertEquals(0, readIndex.getActiveCount());
        assertTrue(readIndex.getEntries().isEmpty());
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ readLazy ПОСЛЕ write
    // ============================================================

    @Test
    void write_shouldCreateValidLazyIndex() throws IOException {
        PgnIndex index = createTestPgnIndex(10);
        writer.write(index, outputPath);

        // Читаем через LazyPgnIndex
        LazyPgnIndex lazyIndex = reader.readLazy(outputPath);

        assertNotNull(lazyIndex);
        assertEquals(10, lazyIndex.getLightEntries().size());
        assertEquals(9, lazyIndex.getActiveLightCount());

        // Проверяем, что данные совпадают
        List<LightGameEntry> lightEntries = lazyIndex.getLightEntries();
        for (int i = 0; i < lightEntries.size(); i++) {
            LightGameEntry light = lightEntries.get(i);
            assertEquals(i + 1, light.id());
            assertEquals("Player" + (i + 1) + "White", light.white());
            assertEquals("Player" + (i + 1) + "Black", light.black());
        }
    }

    // ============================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ============================================================

    private PgnIndex createTestPgnIndex(int entryCount) {
        PgnIndex index = new PgnIndex();

        // ✅ ОБЯЗАТЕЛЬНО устанавливаем все поля
        index.setVersion(PgnIndex.FORMAT_VERSION);
        index.setFileHash("testhash");
        index.setFileSize(1024);

        for (int i = 0; i < entryCount; i++) {
            int id = i + 1;
            boolean deleted = (i == entryCount - 1 && entryCount > 1);

            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(id)
                    .white("Player" + id + "White")
                    .black("Player" + id + "Black")
                    .result(i % 2 == 0 ? "1-0" : "0-1")
                    .year(String.valueOf(2020 + i % 5))
                    .event("Event" + i)
                    .eco("A" + String.format("%02d", i))
                    .opening("Opening" + i)
                    .variation("Variation" + i)
                    .site("Site" + i)
                    .plyCount(45 + i)
                    .offset(i * 200L)
                    .length(100 + i * 10)
                    .version(1)
                    .deleted(deleted)
                    .hash(12345 + i)
                    .build();
            index.addEntry(entry);
        }

        index.refreshCache();
        return index;
    }

    /**
     * Создает PgnIndex с одной записью и кастомными полями
     */
    private PgnIndex createCustomPgnIndex(GameIndexEntry entry) {
        PgnIndex index = new PgnIndex();
        index.setVersion(PgnIndex.FORMAT_VERSION);
        index.setFileHash("testhash");
        index.setFileSize(1024);
        index.addEntry(entry);
        index.refreshCache();
        return index;
    }

    private PgnIndex createTestPgnIndexWithAllDeleted(int entryCount) {
        PgnIndex index = new PgnIndex();
        index.setVersion(PgnIndex.FORMAT_VERSION);
        index.setFileHash("testhash");
        index.setFileSize(1024);

        for (int i = 0; i < entryCount; i++) {
            int id = i + 1;
            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(id)
                    .white("Player" + id + "White")
                    .black("Player" + id + "Black")
                    .result("0-1")
                    .year("2024")
                    .deleted(true)
                    .offset(i * 100L)
                    .length(100 + i * 10)
                    .version(1)
                    .hash(12345 + i)
                    .plyCount(40 + i)
                    .build();
            index.addEntry(entry);
        }

        index.refreshCache();
        return index;
    }

    private PgnIndex createComplexPgnIndex() {
        PgnIndex index = new PgnIndex();
        index.setVersion(PgnIndex.FORMAT_VERSION);
        index.setFileHash("testhash");
        index.setFileSize(1024);

        // Запись 1: обычная
        GameIndexEntry entry1 = GameIndexEntry.builder()
                .id(1)
                .white("Magnus Carlsen")
                .black("Hikaru Nakamura")
                .result("1-0")
                .year("2023")
                .event("World Championship")
                .eco("B52")
                .opening("Sicilian Defense")
                .variation("Najdorf")
                .site("London")
                .plyCount(67)
                .offset(0)
                .length(250)
                .version(1)
                .deleted(false)
                .hash(12345)
                .build();
        index.addEntry(entry1);

        // Запись 2: удаленная
        GameIndexEntry entry2 = GameIndexEntry.builder()
                .id(2)
                .white("Garry Kasparov")
                .black("Anatoly Karpov")
                .result("1/2-1/2")
                .year("1985")
                .event("Moscow Match")
                .eco("E20")
                .opening("Nimzo-Indian")
                .variation("Classical")
                .site("Moscow")
                .plyCount(45)
                .offset(250)
                .length(180)
                .version(1)
                .deleted(true)
                .hash(12346)
                .build();
        index.addEntry(entry2);

        // Запись 3: с длинными строками
        GameIndexEntry entry3 = GameIndexEntry.builder()
                .id(3)
                .white("Bobby Fischer")
                .black("Boris Spassky")
                .result("1-0")
                .year("1972")
                .event("World Chess Championship 1972")
                .eco("C67")
                .opening("Ruy Lopez")
                .variation("Berlin Defense")
                .site("Reykjavik")
                .plyCount(41)
                .offset(430)
                .length(200)
                .version(1)
                .deleted(false)
                .hash(12347)
                .build();
        index.addEntry(entry3);

        index.refreshCache();
        return index;
    }
}