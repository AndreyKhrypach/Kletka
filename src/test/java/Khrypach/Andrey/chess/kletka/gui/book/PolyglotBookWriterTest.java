/*
 * Copyright (c) 2025-2026 Andrey Khrypach
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
 */

package Khrypach.Andrey.chess.kletka.gui.book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Khrypach.Andrey.chess.kletka.gui.book.PolyglotConstants.ENTRY_SIZE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты для {@link PolyglotBookWriter#saveBook(Path, Map)}.
 * <p>
 * Класс использует потоковое слияние — тесты проверяют корректность
 * слияния, дедупликации, сортировки и целостности файла.
 * <p>
 * {@code createBook} помечен {@code @Deprecated} и будет покрыт тестами в 1.5.
 */
class PolyglotBookWriterTest {

    @TempDir
    Path tempDir;

    private Path bookPath;

    @BeforeEach
    void setUp() {
        bookPath = tempDir.resolve("test_book.bin");
    }

    // ======================================================================
    // 1. ПУСТЫЕ / НЕКОРРЕКТНЫЕ ВХОДНЫЕ ДАННЫЕ
    // ======================================================================

    @Test
    @DisplayName("saveBook с пустым dirtyEntries не должен создавать файл")
    void saveBook_EmptyDirtyEntries_ShouldNotCreateFile() throws IOException {
        PolyglotBookWriter.saveBook(bookPath, Map.of());

        assertThat(bookPath).doesNotExist();
    }

    @Test
    @DisplayName("saveBook с null не должен бросать исключение")
    void saveBook_NullDirtyEntries_ShouldNotThrow() throws IOException {
        PolyglotBookWriter.saveBook(bookPath, null);

        assertThat(bookPath).doesNotExist();
    }

    // ======================================================================
    // 2. СОЗДАНИЕ НОВОЙ КНИГИ (когда файла нет)
    // ======================================================================

    @Test
    @DisplayName("saveBook создаёт новую книгу, если файл не существует")
    void saveBook_NewFile_ShouldCreateBook() throws IOException {
        Map<Long, List<PolyglotEntry>> dirty = new HashMap<>();
        dirty.put(100L, List.of(entry(100L, (short) 0x1234, 100)));

        PolyglotBookWriter.saveBook(bookPath, dirty);

        assertThat(bookPath).exists();
        assertThat(Files.size(bookPath)).isEqualTo(ENTRY_SIZE);

        List<PolyglotEntry> read = readAllEntries(bookPath);
        assertThat(read).hasSize(1);
        assertThat(read.get(0).key()).isEqualTo(100L);
        assertThat(read.get(0).move()).isEqualTo((short) 0x1234);
        assertThat(read.get(0).weight()).isEqualTo(100);
    }

    @Test
    @DisplayName("saveBook создаёт книгу, если файл пустой (0 байт)")
    void saveBook_EmptyFile_ShouldCreateBook() throws IOException {
        Files.createFile(bookPath);
        assertThat(Files.size(bookPath)).isZero();

        Map<Long, List<PolyglotEntry>> dirty = new HashMap<>();
        dirty.put(200L, List.of(entry(200L, (short) 0x5678, 50)));

        PolyglotBookWriter.saveBook(bookPath, dirty);

        List<PolyglotEntry> read = readAllEntries(bookPath);
        assertThat(read).hasSize(1);
        assertThat(read.get(0).key()).isEqualTo(200L);
    }

    // ======================================================================
    // 3. СЛИЯНИЕ С СУЩЕСТВУЮЩЕЙ КНИГОЙ
    // ======================================================================

    @Test
    @DisplayName("saveBook добавляет запись в существующую книгу")
    void saveBook_ExistingFile_ShouldMergeEntry() throws IOException {
        // 1. Создаём книгу с одной записью
        Map<Long, List<PolyglotEntry>> initial = new HashMap<>();
        initial.put(100L, List.of(entry(100L, (short) 0x1111, 10)));
        PolyglotBookWriter.saveBook(bookPath, initial);

        // 2. Добавляем вторую запись (другой ключ)
        Map<Long, List<PolyglotEntry>> dirty = new HashMap<>();
        dirty.put(200L, List.of(entry(200L, (short) 0x2222, 20)));
        PolyglotBookWriter.saveBook(bookPath, dirty);

        // 3. Проверяем — обе записи на месте
        List<PolyglotEntry> read = readAllEntries(bookPath);
        assertThat(read).hasSize(2);
        assertThat(read.get(0).key()).isEqualTo(100L);
        assertThat(read.get(0).move()).isEqualTo((short) 0x1111);
        assertThat(read.get(1).key()).isEqualTo(200L);
        assertThat(read.get(1).move()).isEqualTo((short) 0x2222);
    }

    @Test
    @DisplayName("saveBook сохраняет сортировку по ключу при слиянии")
    void saveBook_ShouldKeepSortedByKey() throws IOException {
        // Создаём книгу с ключами 100, 300, 500
        Map<Long, List<PolyglotEntry>> initial = new HashMap<>();
        initial.put(100L, List.of(entry(100L, (short) 0x1, 10)));
        initial.put(300L, List.of(entry(300L, (short) 0x3, 10)));
        initial.put(500L, List.of(entry(500L, (short) 0x5, 10)));
        PolyglotBookWriter.saveBook(bookPath, initial);

        // Добавляем 200 и 400 — должны встать между
        Map<Long, List<PolyglotEntry>> dirty = new HashMap<>();
        dirty.put(200L, List.of(entry(200L, (short) 0x2, 10)));
        dirty.put(400L, List.of(entry(400L, (short) 0x4, 10)));
        PolyglotBookWriter.saveBook(bookPath, dirty);

        List<PolyglotEntry> read = readAllEntries(bookPath);
        assertThat(read).hasSize(5);
        assertThat(read).extracting(PolyglotEntry::key)
                .containsExactly(100L, 200L, 300L, 400L, 500L);
    }

    @Test
    @DisplayName("saveBook добавляет запись с тем же ключом, но другим ходом")
    void saveBook_SameKey_DifferentMove_ShouldAddBoth() throws IOException {
        // Создаём книгу с записью (key=100, move=0x1111)
        Map<Long, List<PolyglotEntry>> initial = new HashMap<>();
        initial.put(100L, List.of(entry(100L, (short) 0x1111, 10)));
        PolyglotBookWriter.saveBook(bookPath, initial);

        // Добавляем (key=100, move=0x2222) — тот же ключ, другой ход
        Map<Long, List<PolyglotEntry>> dirty = new HashMap<>();
        dirty.put(100L, List.of(entry(100L, (short) 0x2222, 20)));
        PolyglotBookWriter.saveBook(bookPath, dirty);

        List<PolyglotEntry> read = readAllEntries(bookPath);
        assertThat(read).hasSize(2);
        assertThat(read).allMatch(e -> e.key() == 100L);
        assertThat(read).extracting(PolyglotEntry::move)
                .containsExactlyInAnyOrder((short) 0x1111, (short) 0x2222);
    }

    @Test
    @DisplayName("saveBook добавляет несколько записей с одним ключом за раз")
    void saveBook_MultipleEntriesSameKey_ShouldAddAll() throws IOException {
        Map<Long, List<PolyglotEntry>> dirty = new HashMap<>();
        dirty.put(100L, List.of(
                entry(100L, (short) 0x1111, 10),
                entry(100L, (short) 0x2222, 20),
                entry(100L, (short) 0x3333, 30)
        ));

        PolyglotBookWriter.saveBook(bookPath, dirty);

        List<PolyglotEntry> read = readAllEntries(bookPath);
        assertThat(read).hasSize(3);
        assertThat(read).allMatch(e -> e.key() == 100L);
    }

    // ======================================================================
    // 4. ДЕДУПЛИКАЦИЯ
    // ======================================================================

    @Test
    @DisplayName("saveBook пропускает полный дубликат (key + move)")
    void saveBook_DuplicateEntry_ShouldSkip() throws IOException {
        // Создаём книгу с записью
        Map<Long, List<PolyglotEntry>> initial = new HashMap<>();
        initial.put(100L, List.of(entry(100L, (short) 0x1111, 10)));
        PolyglotBookWriter.saveBook(bookPath, initial);

        // Пытаемся добавить ту же запись (key=100, move=0x1111, weight=999)
        Map<Long, List<PolyglotEntry>> dirty = new HashMap<>();
        dirty.put(100L, List.of(entry(100L, (short) 0x1111, 999)));
        PolyglotBookWriter.saveBook(bookPath, dirty);

        // Должна остаться одна запись с исходным weight=10
        List<PolyglotEntry> read = readAllEntries(bookPath);
        assertThat(read).hasSize(1);
        assertThat(read.get(0).weight()).isEqualTo(10);
    }

    @Test
    @DisplayName("saveBook не создаёт дубликаты при повторном вызове")
    void saveBook_RepeatedCall_ShouldNotDuplicate() throws IOException {
        Map<Long, List<PolyglotEntry>> dirty = new HashMap<>();
        dirty.put(100L, List.of(entry(100L, (short) 0x1111, 10)));

        PolyglotBookWriter.saveBook(bookPath, dirty);
        PolyglotBookWriter.saveBook(bookPath, dirty);
        PolyglotBookWriter.saveBook(bookPath, dirty);

        List<PolyglotEntry> read = readAllEntries(bookPath);
        assertThat(read).hasSize(1);
    }

    // ======================================================================
    // 5. ФИЛЬТРАЦИЯ ЗАПИСЕЙ С weight < 1
    // ======================================================================

    @Test
    @DisplayName("saveBook пропускает записи с weight < 1")
    void saveBook_WeightLessThanOne_ShouldSkip() throws IOException {
        Map<Long, List<PolyglotEntry>> dirty = new HashMap<>();
        dirty.put(100L, List.of(entry(100L, (short) 0x1111, 0)));

        PolyglotBookWriter.saveBook(bookPath, dirty);

        assertThat(bookPath).doesNotExist();
    }

    // ======================================================================
    // 6. ЦЕЛОСТНОСТЬ ФАЙЛА
    // ======================================================================

    @Test
    @DisplayName("saveBook пишет корректные значения (key, move, weight, learn)")
    void saveBook_ShouldWriteAllFieldsCorrectly() throws IOException {
        PolyglotEntry entry = new PolyglotEntry(
                0x1122334455667788L,
                (short) 0xABCD,
                32767,
                12345,
                0
        );

        Map<Long, List<PolyglotEntry>> dirty = new HashMap<>();
        dirty.put(entry.key(), List.of(entry));

        PolyglotBookWriter.saveBook(bookPath, dirty);

        List<PolyglotEntry> read = readAllEntries(bookPath);
        assertThat(read).hasSize(1);
        PolyglotEntry r = read.get(0);
        assertThat(r.key()).isEqualTo(0x1122334455667788L);
        assertThat(r.move()).isEqualTo((short) 0xABCD);
        assertThat(r.weight()).isEqualTo(32767);
        assertThat(r.learn()).isEqualTo(12345);
    }

    @Test
    @DisplayName("saveBook корректно сохраняет максимальный weight (65535)")
    void saveBook_MaxWeight_ShouldRoundTrip() throws IOException {
        PolyglotEntry entry = new PolyglotEntry(1L, (short) 0x1, 65535, 0, 0);

        Map<Long, List<PolyglotEntry>> dirty = new HashMap<>();
        dirty.put(1L, List.of(entry));

        PolyglotBookWriter.saveBook(bookPath, dirty);

        List<PolyglotEntry> read = readAllEntries(bookPath);
        assertThat(read.get(0).weight()).isEqualTo(65535);
    }

    @Test
    @DisplayName("saveBook корректно работает с беззнаковыми ключами (Long.compareUnsigned)")
    void saveBook_UnsignedKeys_ShouldSortCorrectly() throws IOException {
        // Ключи, где знаковое сравнение даёт неправильный порядок
        long key1 = 0x0000000000000001L;  // маленький положительный
        long key2 = 0x8000000000000000L;  // Long.MIN_VALUE, но как unsigned — большой
        long key3 = 0xFFFFFFFFFFFFFFFFL;  // -1 как знаковое, максимум как unsigned

        Map<Long, List<PolyglotEntry>> dirty = new HashMap<>();
        dirty.put(key3, List.of(entry(key3, (short) 0x3, 10)));
        dirty.put(key1, List.of(entry(key1, (short) 0x1, 10)));
        dirty.put(key2, List.of(entry(key2, (short) 0x2, 10)));

        PolyglotBookWriter.saveBook(bookPath, dirty);

        List<PolyglotEntry> read = readAllEntries(bookPath);
        assertThat(read).hasSize(3);
        // unsigned-порядок: 0x01 < 0x80... < 0xFF...
        assertThat(read).extracting(PolyglotEntry::key)
                .containsExactly(key1, key2, key3);
    }

    // ======================================================================
    // 7. ПЕРЕЗАПИСЬ АТОМАРНАЯ
    // ======================================================================

    @Test
    @DisplayName("saveBook не оставляет .tmp файл после успешного сохранения")
    void saveBook_ShouldNotLeaveTempFile() throws IOException {
        Map<Long, List<PolyglotEntry>> dirty = new HashMap<>();
        dirty.put(100L, List.of(entry(100L, (short) 0x1111, 10)));

        PolyglotBookWriter.saveBook(bookPath, dirty);

        Path tempFile = bookPath.resolveSibling(bookPath.getFileName() + ".tmp");
        assertThat(tempFile).doesNotExist();
    }

    @Test
    @DisplayName("saveBook не создаёт .tmp, если все записи — дубликаты")
    void saveBook_AllDuplicates_ShouldNotCreateTemp() throws IOException {
        Map<Long, List<PolyglotEntry>> dirty = new HashMap<>();
        dirty.put(100L, List.of(entry(100L, (short) 0x1111, 10)));

        PolyglotBookWriter.saveBook(bookPath, dirty);

        // Повторный вызов — все дубликаты
        PolyglotBookWriter.saveBook(bookPath, dirty);

        Path tempFile = bookPath.resolveSibling(bookPath.getFileName() + ".tmp");
        assertThat(tempFile).doesNotExist();
    }

    // ======================================================================
    // ХЕЛПЕРЫ
    // ======================================================================

    private static PolyglotEntry entry(long key, short move, int weight) {
        return new PolyglotEntry(key, move, weight, 0, 0);
    }

    private static List<PolyglotEntry> readAllEntries(Path path) throws IOException {
        List<PolyglotEntry> result = new ArrayList<>();
        if (!Files.exists(path) || Files.size(path) == 0) {
            return result;
        }

        long fileSize = Files.size(path);
        int count = (int) (fileSize / ENTRY_SIZE);

        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(path), 1 << 20))) {
            for (int i = 0; i < count; i++) {
                long key = dis.readLong();
                short move = dis.readShort();
                int weight = dis.readUnsignedShort();
                int learn = dis.readInt();
                result.add(new PolyglotEntry(key, move, weight, learn, 0));
            }
        }
        return result;
    }
}