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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LazyPgnIndexTest {

    @TempDir
    Path tempDir;

    private Path testIndexFile;
    private LazyPgnIndex lazyIndex;
    List<LightGameEntry> testLightEntries;

    @BeforeEach
    void setUp() throws IOException {
        testIndexFile = tempDir.resolve("test-index.bin");

        // Создаем тестовые лёгкие записи (12 аргументов)
        testLightEntries = List.of(
                // id, white, black, result, year, event, eco, opening, offset, length, deleted, hash
                new LightGameEntry(1, "Carlsen", "Nakamura", "1-0", "2024",
                        "WCC 2024", "B52", "Sicilian", 0, 100, false, 12345),
                new LightGameEntry(2, "Nakamura", "Carlsen", "0-1", "2024",
                        "WCC 2024", "C67", "Ruy Lopez", 100, 120, false, 12346),
                new LightGameEntry(3, "Kasparov", "Karpov", "1/2-1/2", "1985",
                        "Moscow", "E20", "Nimzo-Indian", 220, 90, true, 12347)
        );

        // Создаем бинарный файл
        createTestBinaryIndex(testLightEntries);

        // Читаем индекс через BinaryIndexReader (как в реальном приложении)
        BinaryIndexReader reader = new BinaryIndexReader();
        lazyIndex = reader.readLazy(testIndexFile);

        // Проверяем, что индекс создался
        assertNotNull(lazyIndex);
        assertEquals(3, lazyIndex.getLightEntries().size());
    }

    @AfterEach
    void tearDown() {
        if (lazyIndex != null) {
            lazyIndex.close();
        }
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ ЛЁГКИХ ЗАПИСЕЙ (ПАГИНАЦИЯ И ПОИСК)
    // ============================================================

    @Test
    void getLightEntriesPage_shouldReturnCorrectPage() {
        List<LightGameEntry> page1 = lazyIndex.getLightEntriesPage(0, 2);
        assertEquals(2, page1.size());
        assertEquals(1, page1.get(0).id());
        assertEquals(2, page1.get(1).id());

        List<LightGameEntry> page2 = lazyIndex.getLightEntriesPage(2, 5);
        assertEquals(1, page2.size());
        assertEquals(3, page2.get(0).id());

        List<LightGameEntry> page3 = lazyIndex.getLightEntriesPage(10, 5);
        assertTrue(page3.isEmpty());

        List<LightGameEntry> page4 = lazyIndex.getLightEntriesPage(-5, 3);
        assertEquals(3, page4.size());
    }

    @Test
    void getFirstPage_shouldReturnFirst50Entries() {
        List<LightGameEntry> firstPage = lazyIndex.getFirstPage();
        assertEquals(3, firstPage.size());
        assertEquals(1, firstPage.get(0).id());
    }

    @Test
    void search_shouldFilterEntriesByQuery() {
        // Поиск по имени игрока
        List<LightGameEntry> results = lazyIndex.search("Carlsen");
        assertEquals(2, results.size()); // Carlsen есть в 2 записях

        // Поиск по ECO
        results = lazyIndex.search("B52");
        assertEquals(1, results.size());
        assertEquals(1, results.get(0).id());

        // Поиск по году
        results = lazyIndex.search("2024");
        assertEquals(2, results.size());

        // Поиск с удалением (deleted записи не должны исключаться при поиске)
        results = lazyIndex.search("Kasparov");
        assertEquals(1, results.size());
        assertEquals(3, results.get(0).id());

        // Пустой запрос возвращает все
        results = lazyIndex.search("");
        assertEquals(3, results.size());

        // null запрос возвращает все
        results = lazyIndex.search(null);
        assertEquals(3, results.size());
    }

    @Test
    void search_shouldSupportPagination() {
        List<LightGameEntry> page = lazyIndex.search("Carlsen", 0, 1);
        assertEquals(1, page.size());
        assertEquals(1, page.get(0).id());

        page = lazyIndex.search("Carlsen", 1, 1);
        assertEquals(1, page.size());
        assertEquals(2, page.get(0).id());

        page = lazyIndex.search("Carlsen", 10, 1);
        assertTrue(page.isEmpty());
    }

    // ============================================================
    // 2. ТЕСТЫ ЗАГРУЗКИ ПОЛНЫХ ЗАПИСЕЙ
    // ============================================================

    @Test
    void loadFullEntries_shouldLoadEntriesInBlocks() {
        // Загружаем первые 2 записи
        List<GameIndexEntry> fullEntries = lazyIndex.loadFullEntries(0, 2, null);

        assertEquals(2, fullEntries.size());
        assertEquals(1, fullEntries.get(0).getId());
        assertEquals("Carlsen", fullEntries.get(0).getWhite());
        assertEquals(2, fullEntries.get(1).getId());
        assertEquals("Nakamura", fullEntries.get(1).getWhite());

        // Проверяем, что кэш заполнился (нужен getter или reflection)
        assertEquals(2, lazyIndex.getFullEntryCache().size());
    }

    @Test
    void loadFullEntries_shouldUseCacheWhenAvailable() {
        lazyIndex.loadFullEntries(0, 1, null);

        List<GameIndexEntry> cachedEntries = lazyIndex.loadFullEntries(0, 1, null);
        assertEquals(1, cachedEntries.size());
        assertEquals(1, cachedEntries.get(0).getId());

        assertEquals(1, lazyIndex.getFullEntryCache().size());
    }

    @Test
    void loadFullEntries_shouldHandleProgressCallback() {
        final int[] progressValues = new int[1];

        lazyIndex.loadFullEntries(0, 3, (progress) -> progressValues[0] = progress);

        assertTrue(progressValues[0] >= 0);
    }

    @Test
    void loadInitialFullEntries_shouldLoadFirstNEntries() {
        List<GameIndexEntry> initial = lazyIndex.loadInitialFullEntries(2);

        assertEquals(2, initial.size());
        assertEquals(1, initial.get(0).getId());
        assertEquals(2, initial.get(1).getId());
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ ПОЛУЧЕНИЯ ОДНОЙ ПОЛНОЙ ЗАПИСИ
    // ============================================================

    @Test
    void getFullEntry_shouldLoadEntryLazily() {
        assertFalse(lazyIndex.getFullEntryCache().containsKey(1));

        GameIndexEntry entry = lazyIndex.getFullEntry(1);

        assertNotNull(entry);
        assertEquals(1, entry.getId());
        assertEquals("Carlsen", entry.getWhite());

        assertTrue(lazyIndex.getFullEntryCache().containsKey(1));
    }

    @Test
    void getFullEntry_shouldReturnNullForNonExistentId() {
        GameIndexEntry entry = lazyIndex.getFullEntry(999);
        assertNull(entry);
    }

    @Test
    void getFullEntry_shouldReturnCachedEntryIfAlreadyLoaded() {
        lazyIndex.getFullEntry(1);

        assertTrue(lazyIndex.getFullEntryCache().containsKey(1));

        GameIndexEntry first = lazyIndex.getFullEntry(1);
        GameIndexEntry second = lazyIndex.getFullEntry(1);
        assertSame(first, second);
    }

    // ============================================================
    // 4. ТЕСТЫ ЗАГРУЗКИ ВСЕХ ЗАПИСЕЙ
    // ============================================================

    @Test
    void loadAllFullEntries_shouldLoadAllEntries() {
        assertFalse(lazyIndex.isAllFullEntriesLoaded());
        assertEquals(0, lazyIndex.getFullEntryCache().size());

        lazyIndex.loadAllFullEntries(null);

        assertTrue(lazyIndex.isAllFullEntriesLoaded());
        assertEquals(3, lazyIndex.getFullEntryCache().size());
    }

    @Test
    void loadAllFullEntries_shouldSkipAlreadyLoadedEntries() {
        lazyIndex.getFullEntry(1);
        assertEquals(1, lazyIndex.getFullEntryCache().size());

        lazyIndex.loadAllFullEntries(null);

        assertEquals(3, lazyIndex.getFullEntryCache().size());
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ РАБОТЫ С ID
    // ============================================================

    @Test
    void getEntryById_shouldWorkLazily() {
        GameIndexEntry entry = lazyIndex.getEntryById(1);
        assertNotNull(entry);
        assertEquals(1, entry.getId());
        assertTrue(lazyIndex.getFullEntryCache().containsKey(1));
    }

    @Test
    void getActiveEntryById_shouldReturnOnlyActiveEntries() {
        GameIndexEntry activeEntry = lazyIndex.getActiveEntryById(3);
        assertNull(activeEntry);

        activeEntry = lazyIndex.getActiveEntryById(1);
        assertNotNull(activeEntry);
        assertEquals(1, activeEntry.getId());
    }

    @Test
    void getNextId_shouldReturnMaxIdPlusOne() {
        int nextId = lazyIndex.getNextId();
        assertEquals(4, nextId);
    }

    @Test
    void getMaxId_shouldReturnMaxId() {
        int maxId = lazyIndex.getMaxId();
        assertEquals(3, maxId);
    }

    // ============================================================
    // 6. ТЕСТЫ ДЛЯ АКТИВНЫХ/УДАЛЁННЫХ ЗАПИСЕЙ
    // ============================================================

    @Test
    void getActiveLightEntries_shouldReturnOnlyNonDeleted() {
        List<LightGameEntry> active = lazyIndex.getActiveLightEntries();
        assertEquals(2, active.size());
        assertTrue(active.stream().noneMatch(LightGameEntry::deleted));
    }

    @Test
    void getActiveLightEntry_shouldReturnOnlyActiveById() {
        LightGameEntry active = lazyIndex.getActiveLightEntry(3);
        assertNull(active);

        active = lazyIndex.getActiveLightEntry(1);
        assertNotNull(active);
        assertEquals(1, active.id());
    }

    @Test
    void getActiveLightCount_shouldCountNonDeleted() {
        int count = lazyIndex.getActiveLightCount();
        assertEquals(2, count);
    }

    // ============================================================
    // 7. ТЕСТЫ МОДИФИКАЦИИ
    // ============================================================

    @Test
    void addEntry_shouldAddToBothLightAndFull() {
        GameIndexEntry newEntry = GameIndexEntry.builder()
                .id(4)
                .white("Fischer")
                .black("Spassky")
                .result("1-0")
                .year("1972")
                .build();

        lazyIndex.addEntry(newEntry);

        assertEquals(4, lazyIndex.getGameCount());
        assertEquals(3, lazyIndex.getActiveCount());

        List<LightGameEntry> lightEntries = lazyIndex.getLightEntries();
        assertEquals(4, lightEntries.size());

        assertTrue(lazyIndex.getFullEntryCache().containsKey(4));
    }

    @Test
    void updateEntry_shouldUpdateBothLightAndFull() {
        GameIndexEntry updatedEntry = GameIndexEntry.builder()
                .id(1)
                .white("Carlsen-Magnus")
                .black("Nakamura-Hikaru")
                .result("1-0")
                .year("2025")
                .build();

        lazyIndex.updateEntry(updatedEntry);

        LightGameEntry light = lazyIndex.getLightEntries().get(0);
        assertEquals("Carlsen-Magnus", light.white());

        GameIndexEntry cached = lazyIndex.getFullEntryCache().get(1);
        assertEquals("Carlsen-Magnus", cached.getWhite());

        assertEquals(3, lazyIndex.getGameCount());
    }

    // ============================================================
    // 8. ТЕСТЫ МЕТАДАННЫХ
    // ============================================================

    @Test
    void getDeletedEntries_shouldReturnOnlyDeleted() {
        lazyIndex.loadAllFullEntries(null);

        List<GameIndexEntry> deleted = lazyIndex.getDeletedEntries();
        assertEquals(1, deleted.size());
        assertEquals(3, deleted.get(0).getId());
        assertTrue(deleted.get(0).isDeleted());
    }

    @Test
    void getActiveEntries_shouldReturnOnlyActive() {
        lazyIndex.loadAllFullEntries(null);

        List<GameIndexEntry> active = lazyIndex.getActiveEntries();
        assertEquals(2, active.size());
        assertTrue(active.stream().noneMatch(GameIndexEntry::isDeleted));
    }

    @Test
    void hasDeletedGames_shouldDetectDeletedEntries() {
        assertTrue(lazyIndex.hasDeletedGames(lazyIndex));
    }

    @Test
    void getGrowthRatio_shouldCalculateCorrectly() {
        double ratio = lazyIndex.getGrowthRatio();
        // (100 + 120 + 90) / (100 + 120) = 310 / 220 = ~1.409
        assertEquals(1.409, ratio, 0.001);
    }

    @Test
    void needsRepack_shouldReturnFalseWhenRatioLow() {
        // Изначально: 2 активные записи (length 100 и 120) и 1 удаленная (length 90)
        // activeSize = 220, totalSize = 310, ratio = 310/220 = 1.41 < 2.0
        assertFalse(lazyIndex.needsRepack());

        // Добавляем много удаленных записей с ДЛИНОЙ > 0
        for (int i = 4; i <= 10; i++) {
            GameIndexEntry deleted = GameIndexEntry.builder()
                    .id(i)
                    .deleted(true)
                    .white("Deleted" + i)
                    .black("Player")
                    .length(150)  // ← КЛЮЧЕВОЕ: устанавливаем длину для удаленных записей
                    .offset(i * 200L)
                    .hash(i * 11111)
                    .build();
            lazyIndex.addEntry(deleted);
        }

        // Теперь: активные = 220, удаленные = 7 * 150 = 1050, total = 1270
        // ratio = 1270/220 = 5.77 > 2.0
        assertTrue(lazyIndex.needsRepack());
    }

    // ============================================================
    // 9. ТЕСТ ОБЩЕЙ ФУНКЦИОНАЛЬНОСТИ
    // ============================================================

    @Test
    void getEntries_shouldLoadAllIfNotLoaded() {
        assertFalse(lazyIndex.isAllFullEntriesLoaded());

        List<GameIndexEntry> entries = lazyIndex.getEntries();

        assertEquals(3, entries.size());
        assertTrue(lazyIndex.isAllFullEntriesLoaded());
    }

    @Test
    void refreshCache_shouldRecalculateCounters() {
        lazyIndex.refreshCache();
        assertEquals(3, lazyIndex.getGameCount());
        assertEquals(2, lazyIndex.getActiveCount());
    }

    @Test
    void close_shouldCloseChannel() {
        lazyIndex.close();
        assertDoesNotThrow(() -> lazyIndex.close());
    }

    // ============================================================
    // 10. ТЕСТ ЗАГОЛОВКА
    // ============================================================

    @Test
    void header_shouldBeValid() {
        BinaryIndexHeader header = new BinaryIndexHeader(
                BinaryIndexConstants.MAGIC,
                BinaryIndexConstants.VERSION,
                100,
                80,
                1024,
                "testhash",
                System.currentTimeMillis()
        );

        assertTrue(header.isValid());
        assertEquals(100, header.entryCount());
        assertEquals(80, header.activeCount());
        assertEquals("testhash", header.fileHash());
    }

    // ============================================================
    // ВСПОМОГАТЕЛЬНЫЙ МЕТОД СОЗДАНИЯ БИНАРНОГО ФАЙЛА
    // ============================================================

    private void createTestBinaryIndex(List<LightGameEntry> entries) throws IOException {
        try (FileChannel channel = FileChannel.open(testIndexFile,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ);
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(baos)) {

            // ========== 1. ЗАГОЛОВОК (48 байт) ==========
            BinaryIndexHeader header = new BinaryIndexHeader(
                    BinaryIndexConstants.MAGIC,
                    BinaryIndexConstants.VERSION,
                    entries.size(),
                    (int) entries.stream().filter(e -> !e.deleted()).count(),
                    1024,
                    "testhash",
                    System.currentTimeMillis()
            );
            header.write(dos);
            dos.flush();

            // ========== 2. ТАБЛИЦА СМЕЩЕНИЙ ==========
            long tableOffset = baos.size();
            for (int i = 0; i < entries.size(); i++) {
                dos.writeLong(0);
            }
            dos.flush();

            // ========== 3. ЗАПИСЫВАЕМ ДАННЫЕ ==========
            long dataStart = baos.size();
            long[] actualOffsets = new long[entries.size()];

            for (int i = 0; i < entries.size(); i++) {
                LightGameEntry entry = entries.get(i);
                actualOffsets[i] = baos.size() - dataStart;

                dos.writeInt(entry.id());
                dos.writeLong(entry.offset());
                dos.writeInt(entry.length());
                dos.writeInt(1);
                dos.writeByte(entry.deleted() ? 1 : 0);
                dos.writeInt(entry.hash());

                writeString(dos, entry.white());
                writeString(dos, entry.black());
                writeString(dos, entry.eco());
                writeString(dos, entry.result());
                writeString(dos, entry.year());
                writeString(dos, entry.event());
                writeString(dos, "");
                writeString(dos, entry.opening());
                writeString(dos, "");
                dos.writeInt(45);
            }

            dos.flush();

            // ========== 4. ОБНОВЛЯЕМ ТАБЛИЦУ СМЕЩЕНИЙ ==========
            byte[] allData = baos.toByteArray();
            ByteBuffer buffer = ByteBuffer.wrap(allData);
            buffer.position((int) tableOffset);
            for (long offset : actualOffsets) {
                buffer.putLong(offset);
            }

            // ========== 5. ЗАПИСЫВАЕМ В ФАЙЛ С ПРОВЕРКОЙ ==========
            buffer.position(0);
            int totalBytes = buffer.remaining();
            int written = 0;

            while (buffer.hasRemaining()) {
                written += channel.write(buffer);
            }

            // ✅ ПРОВЕРКА: убеждаемся, что записано ровно столько байт, сколько нужно
            if (written != totalBytes) {
                throw new IOException(String.format(
                        "Failed to write entire buffer: wrote %d bytes but expected %d bytes",
                        written, totalBytes));
            }

            // ✅ ДОПОЛНИТЕЛЬНАЯ ПРОВЕРКА: принудительно сбрасываем на диск
            channel.force(true);
        }
    }

    // Вспомогательный метод для записи строки через DataOutputStream
    private void writeString(DataOutputStream dos, String str) throws IOException {
        if (str == null) str = "";
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        dos.writeShort(bytes.length);
        dos.write(bytes);
    }
}
