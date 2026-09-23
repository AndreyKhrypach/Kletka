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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static Khrypach.Andrey.chess.kletka.gui.book.PolyglotConstants.ENTRY_SIZE;

/**
 * Запись Polyglot книг в формате .bin.
 * <p>
 * {@link #saveBook(Path, Map)} использует потоковое слияние двух отсортированных
 * последовательностей (старый файл + новые записи). Пик памяти O(N + buffer),
 * где N — число новых записей. Работает на книгах любого размера без риска OOM.
 */
public class PolyglotBookWriter {

    private static final Logger log = LoggerFactory.getLogger(PolyglotBookWriter.class);

    /** Размер буфера чтения/записи (1 МБ). */
    private static final int BUFFER_SIZE = 1 << 20;

    /**
     * Сохраняет книгу с добавлением новых записей.
     * <p>
     * Старый файл уже отсортирован по ключу (требование Polyglot). Новые записи
     * сортируются отдельно и вплавляются в поток на лету — без чтения всей книги
     * в память.
     *
     * @param bookPath     путь к .bin файлу (должен существовать)
     * @param dirtyEntries карта «грязных» записей (key -> список PolyglotEntry)
     */
    public static void saveBook(Path bookPath, Map<Long, List<PolyglotEntry>> dirtyEntries) throws IOException {
        if (dirtyEntries == null || dirtyEntries.isEmpty()) {
            log.debug("No dirty entries to save");
            return;
        }

        // 1. Собираем все новые записи в один плоский список
        List<PolyglotEntry> newEntries = new ArrayList<>();
        for (List<PolyglotEntry> list : dirtyEntries.values()) {
            if (list != null) newEntries.addAll(list);
        }

        newEntries.removeIf(e -> e.weight() < 1);

        if (newEntries.isEmpty()) {
            log.debug("No new entries to write");
            return;
        }

        // 2. Сортируем по ключу (беззнаковое сравнение — требование Polyglot)
        newEntries.sort((a, b) -> Long.compareUnsigned(a.key(), b.key()));

        log.info("Saving book: {}, new entries: {}", bookPath, newEntries.size());

        // 3. Если книги нет или она пустая — просто пишем новые записи
        if (!Files.exists(bookPath) || Files.size(bookPath) == 0) {
            log.debug("Book file does not exist or is empty, creating new");
            writeEntriesToFile(bookPath, newEntries);
            log.info("Book created: {}", bookPath);
            return;
        }

        // 4. Слияние старого файла и новых записей в tmp
        Path tempPath = bookPath.resolveSibling(bookPath.getFileName() + ".tmp");
        MergeStats stats = mergeSorted(bookPath, tempPath, newEntries);
        log.debug("Merge result: written={}, inserted={}, skippedDuplicates={}",
                stats.written, stats.inserted, stats.skipped);

        if (stats.inserted == 0) {
            log.debug("No new entries to insert (all duplicates), skipping save");
            Files.deleteIfExists(tempPath);
            return;
        }

        // 5. Атомарная замена
        Files.move(tempPath, bookPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        log.info("Book saved successfully: {}", bookPath);
    }

    /**
     * Результат слияния — для логов.
     */
    private record MergeStats(long written, long inserted, long skipped) {}

    /**
     * Сливает два отсортированных потока (старый файл + новые записи) в один.
     * <p>
     * Идём по старому файлу последовательно. Перед каждой записью файла
     * «выгружаем» все новые записи, чей ключ меньше или равен ключу файла.
     * Дубликаты (совпадение key+move) пропускаем.
     */
    private static MergeStats mergeSorted(Path inPath, Path outPath, List<PolyglotEntry> newEntries)
            throws IOException {

        long written = 0;
        long inserted = 0;
        long skipped = 0;

        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(inPath), BUFFER_SIZE));
             DataOutputStream out = new DataOutputStream(
                     new BufferedOutputStream(Files.newOutputStream(outPath), BUFFER_SIZE))) {

            int newIdx = 0;
            final int newSize = newEntries.size();

            while (true) {
                // Пытаемся прочитать очередную запись из старого файла
                long fileKey;
                short fileMove;
                int fileWeight;
                int fileLearn;

                try {
                    fileKey = in.readLong();
                    fileMove = in.readShort();
                    fileWeight = in.readUnsignedShort();
                    fileLearn = in.readInt();
                } catch (EOFException eof) {
                    // Старый файл закончился — дописываем оставшиеся новые записи
                    while (newIdx < newSize) {
                        PolyglotEntry ne = newEntries.get(newIdx++);
                        if (ne.weight() < 1) continue; // пропускаем мусорные
                        writeEntry(out, ne);
                        written++;
                        inserted++;
                    }
                    break;
                }

                // Выгружаем все новые записи, чей ключ <= текущего ключа файла
                while (newIdx < newSize
                        && Long.compareUnsigned(newEntries.get(newIdx).key(), fileKey) <= 0) {
                    PolyglotEntry ne = newEntries.get(newIdx);

                    // Дубликат? Проверяем только если ключ совпадает с текущим файловым
                    if (ne.key() == fileKey && ne.move() == fileMove) {
                        skipped++;
                        newIdx++;
                        continue;
                    }

                    // Вставляем новую запись
                    if (ne.weight() >= 1) {
                        writeEntry(out, ne);
                        written++;
                        inserted++;
                    }
                    newIdx++;
                }

                // Пишем запись из старого файла
                out.writeLong(fileKey);
                out.writeShort(fileMove);
                out.writeShort((short) fileWeight);
                out.writeInt(fileLearn);
                written++;
            }
        }

        return new MergeStats(written, inserted, skipped);
    }

    /**
     * Проверка дубликатов до слияния (через бинарный поиск по файлу).
     * <p>
     * Оставлено как опциональный шаг — можно вызвать до {@link #mergeSorted},
     * чтобы не открывать выходной поток, если все записи уже есть.
     * <p>
     * Сейчас не используется: дубликаты обрабатываются прямо в mergeSorted.
     */
    @SuppressWarnings("unused")
    private static List<PolyglotEntry> filterDuplicates(Path bookPath, List<PolyglotEntry> candidates)
            throws IOException {
        List<PolyglotEntry> result = new ArrayList<>();

        try (RandomAccessFile raf = new RandomAccessFile(bookPath.toFile(), "r")) {
            long fileSize = raf.length();
            long entryCount = fileSize / ENTRY_SIZE;

            for (PolyglotEntry candidate : candidates) {
                long lo = 0;
                long hi = entryCount;
                while (lo < hi) {
                    long mid = (lo + hi) >>> 1;
                    raf.seek(mid * ENTRY_SIZE);
                    long midKey = raf.readLong();
                    if (Long.compareUnsigned(midKey, candidate.key()) < 0) {
                        lo = mid + 1;
                    } else {
                        hi = mid;
                    }
                }

                // Читаем все записи с этим ключом, ищем совпадение move
                boolean exists = false;
                long pos = lo;
                while (pos < entryCount) {
                    raf.seek(pos * ENTRY_SIZE);
                    long k = raf.readLong();
                    if (k != candidate.key()) break;
                    short m = raf.readShort();
                    if (m == candidate.move()) {
                        exists = true;
                        break;
                    }
                    pos++;
                }

                if (!exists) {
                    result.add(candidate);
                }
            }
        }
        return result;
    }

    /**
     * Пишет одну запись в поток.
     */
    private static void writeEntry(DataOutputStream out, PolyglotEntry e) throws IOException {
        out.writeLong(e.key());
        out.writeShort(e.move());
        out.writeShort((short) e.weight());
        out.writeInt(e.learn());
    }

    /**
     * Записывает список записей в файл через mmap.
     * Используется только для {@link #createBook} и случаев, когда книга пустая.
     */
    private static void writeEntriesToFile(Path filePath, List<PolyglotEntry> entries) throws IOException {
        MappedByteBuffer buffer = null;

        try (RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "rw");
             FileChannel channel = file.getChannel()) {

            long fileSize = (long) entries.size() * ENTRY_SIZE;
            file.setLength(fileSize);

            buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
            buffer.order(ByteOrder.BIG_ENDIAN);

            for (PolyglotEntry entry : entries) {
                buffer.putLong(entry.key());
                buffer.putShort(entry.move());
                buffer.putShort((short) entry.weight());
                buffer.putInt(entry.learn());
            }
            buffer.force();
        } finally {
            if (buffer != null) {
                unmap(buffer);
            }
        }
    }

    /**
     * Создаёт новую книгу из списка записей.
     * Используется для экспорта дерева вариантов в Polyglot.
     * <p>
     * Если файл существует — перезаписывается. Если нет — создаётся.
     * <p>
     * Записи сортируются по ключу (требование Polyglot).
     */
    public static void createBook(Path bookPath, List<PolyglotEntry> entries) throws IOException {
        if (entries == null || entries.isEmpty()) {
            log.debug("No entries to create book");
            return;
        }

        log.info("Creating new book: {}, entries: {}", bookPath, entries.size());

        List<PolyglotEntry> sortedEntries = new ArrayList<>(entries);
        sortedEntries.sort((e1, e2) -> Long.compareUnsigned(e1.key(), e2.key()));

        Path parent = bookPath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        writeEntriesToFile(bookPath, sortedEntries);
        log.info("New book created: {}", bookPath);
    }

    private static void unmap(ByteBuffer buffer) {
        if (buffer == null) return;

        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Unsafe unsafe = (Unsafe) field.get(null);

            Method method = Unsafe.class.getMethod("invokeCleaner", ByteBuffer.class);
            method.invoke(unsafe, buffer);
        } catch (Exception e) {
            buffer.clear();
            System.gc();
        }
    }
}