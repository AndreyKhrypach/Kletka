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

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class BinaryIndexHeaderTest {

    private static final int TEST_ENTRY_COUNT = 100;
    private static final int TEST_ACTIVE_COUNT = 80;
    private static final long TEST_FILE_SIZE = 1024L * 1024; // 1 MB
    private static final String TEST_FILE_HASH = "testhash";
    private static final long TEST_TIMESTAMP = 1234567890L;

    // ============================================================
    // 1. ТЕСТЫ СОЗДАНИЯ
    // ============================================================

    @Test
    void create_shouldCreateValidHeader() {
        // Создаем через фабричный метод
        BinaryIndexHeader header = BinaryIndexHeader.create(
                TEST_ENTRY_COUNT,
                TEST_ACTIVE_COUNT,
                TEST_FILE_SIZE,
                TEST_FILE_HASH
        );

        assertNotNull(header);
        assertEquals(BinaryIndexConstants.MAGIC, header.magic());
        assertEquals(BinaryIndexConstants.VERSION, header.version());
        assertEquals(TEST_ENTRY_COUNT, header.entryCount());
        assertEquals(TEST_ACTIVE_COUNT, header.activeCount());
        assertEquals(TEST_FILE_SIZE, header.fileSize());
        assertEquals(TEST_FILE_HASH, header.fileHash());
        assertTrue(header.timestamp() > 0);
        assertTrue(header.isValid());
    }

    @Test
    void create_shouldSetCurrentTimestamp() {
        long before = System.currentTimeMillis();
        BinaryIndexHeader header = BinaryIndexHeader.create(10, 5, 1024, "hash");
        long after = System.currentTimeMillis();

        assertTrue(header.timestamp() >= before);
        assertTrue(header.timestamp() <= after);
    }

    @Test
    void constructor_shouldCreateHeaderWithAllFields() {
        BinaryIndexHeader header = new BinaryIndexHeader(
                BinaryIndexConstants.MAGIC,
                BinaryIndexConstants.VERSION,
                TEST_ENTRY_COUNT,
                TEST_ACTIVE_COUNT,
                TEST_FILE_SIZE,
                TEST_FILE_HASH,
                TEST_TIMESTAMP
        );

        assertEquals(BinaryIndexConstants.MAGIC, header.magic());
        assertEquals(BinaryIndexConstants.VERSION, header.version());
        assertEquals(TEST_ENTRY_COUNT, header.entryCount());
        assertEquals(TEST_ACTIVE_COUNT, header.activeCount());
        assertEquals(TEST_FILE_SIZE, header.fileSize());
        assertEquals(TEST_FILE_HASH, header.fileHash());
        assertEquals(TEST_TIMESTAMP, header.timestamp());
    }

    // ============================================================
    // 2. ТЕСТЫ СЕРИАЛИЗАЦИИ / ДЕСЕРИАЛИЗАЦИИ (write / read)
    // ============================================================

    @Test
    void write_shouldWriteHeaderToStream() throws IOException {
        // Создаем заголовок
        byte[] data = getData();

        // Проверяем размер (должен быть 48 байт)
        assertEquals(48, data.length, "Header should be exactly 48 bytes");

        // Проверяем магическое число (первые 4 байта)
        assertEquals(BinaryIndexConstants.MAGIC,
                ((data[0] & 0xFF) << 24) | ((data[1] & 0xFF) << 16) |
                        ((data[2] & 0xFF) << 8) | (data[3] & 0xFF));
    }

    private static byte[] getData() throws IOException {
        BinaryIndexHeader original = new BinaryIndexHeader(
                BinaryIndexConstants.MAGIC,
                BinaryIndexConstants.VERSION,
                TEST_ENTRY_COUNT,
                TEST_ACTIVE_COUNT,
                TEST_FILE_SIZE,
                BinaryIndexHeaderTest.TEST_FILE_HASH,
                TEST_TIMESTAMP
        );

        // Записываем в поток
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        original.write(dos);
        dos.flush();

        return baos.toByteArray();
    }

    @Test
    void read_shouldReadHeaderFromStream() throws IOException {
        // Создаем оригинальный заголовок
        BinaryIndexHeader original = new BinaryIndexHeader(
                BinaryIndexConstants.MAGIC,
                BinaryIndexConstants.VERSION,
                TEST_ENTRY_COUNT,
                TEST_ACTIVE_COUNT,
                TEST_FILE_SIZE,
                TEST_FILE_HASH,
                TEST_TIMESTAMP
        );

        // Записываем в массив
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        original.write(dos);
        dos.flush();

        // Читаем обратно
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DataInputStream dis = new DataInputStream(bais);
        BinaryIndexHeader read = BinaryIndexHeader.read(dis);

        // Проверяем, что все поля совпадают
        assertNotNull(read);
        assertEquals(original.magic(), read.magic());
        assertEquals(original.version(), read.version());
        assertEquals(original.entryCount(), read.entryCount());
        assertEquals(original.activeCount(), read.activeCount());
        assertEquals(original.fileSize(), read.fileSize());
        assertEquals(original.fileHash(), read.fileHash());
        assertEquals(original.timestamp(), read.timestamp());
    }

    @Test
    void read_shouldThrowExceptionWhenInvalidMagic() throws IOException {
        // Создаем поток с неверным магическим числом
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(0x12345678); // Неверный magic
        dos.writeByte(BinaryIndexConstants.VERSION);
        dos.writeByte(0);
        dos.writeByte(0);
        dos.writeByte(0);
        dos.writeInt(TEST_ENTRY_COUNT);
        dos.writeInt(TEST_ACTIVE_COUNT);
        dos.writeLong(TEST_FILE_SIZE);
        dos.write(TEST_FILE_HASH.getBytes(StandardCharsets.UTF_8));
        for (int i = TEST_FILE_HASH.length(); i < 8; i++) {
            dos.writeByte(0);
        }
        dos.writeLong(TEST_TIMESTAMP);
        dos.writeLong(0);
        dos.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DataInputStream dis = new DataInputStream(bais);

        assertThrows(IOException.class, () -> BinaryIndexHeader.read(dis),
                "Should throw IOException for invalid magic number");
    }

    @Test
    void read_shouldThrowExceptionWhenUnsupportedVersion() throws IOException {
        // Создаем поток с неверной версией
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(BinaryIndexConstants.MAGIC);
        dos.writeByte(99); // Неверная версия
        dos.writeByte(0);
        dos.writeByte(0);
        dos.writeByte(0);
        dos.writeInt(TEST_ENTRY_COUNT);
        dos.writeInt(TEST_ACTIVE_COUNT);
        dos.writeLong(TEST_FILE_SIZE);
        dos.write(TEST_FILE_HASH.getBytes(StandardCharsets.UTF_8));
        for (int i = TEST_FILE_HASH.length(); i < 8; i++) {
            dos.writeByte(0);
        }
        dos.writeLong(TEST_TIMESTAMP);
        dos.writeLong(0);
        dos.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DataInputStream dis = new DataInputStream(bais);

        assertThrows(IOException.class, () -> BinaryIndexHeader.read(dis),
                "Should throw IOException for unsupported version");
    }

    @Test
    void write_shouldThrowExceptionWhenHashTooLong() {
        // Создаем заголовок с хешем длиннее 8 байт
        BinaryIndexHeader header = new BinaryIndexHeader(
                BinaryIndexConstants.MAGIC,
                BinaryIndexConstants.VERSION,
                TEST_ENTRY_COUNT,
                TEST_ACTIVE_COUNT,
                TEST_FILE_SIZE,
                "this_hash_is_too_long", // > 8 байт
                TEST_TIMESTAMP
        );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        assertThrows(IOException.class, () -> header.write(dos),
                "Should throw IOException when file hash is too long");
    }

    // ============================================================
    // 3. ТЕСТЫ ВАЛИДАЦИИ (isValid)
    // ============================================================

    @Test
    void isValid_shouldReturnTrueForValidHeader() {
        BinaryIndexHeader header = new BinaryIndexHeader(
                BinaryIndexConstants.MAGIC,
                BinaryIndexConstants.VERSION,
                TEST_ENTRY_COUNT,
                TEST_ACTIVE_COUNT,
                TEST_FILE_SIZE,
                TEST_FILE_HASH,
                TEST_TIMESTAMP
        );

        assertTrue(header.isValid());
    }

    @Test
    void isValid_shouldReturnFalseWhenInvalidMagic() {
        BinaryIndexHeader header = new BinaryIndexHeader(
                0x12345678, // Неверный magic
                BinaryIndexConstants.VERSION,
                TEST_ENTRY_COUNT,
                TEST_ACTIVE_COUNT,
                TEST_FILE_SIZE,
                TEST_FILE_HASH,
                TEST_TIMESTAMP
        );

        assertFalse(header.isValid());
    }

    @Test
    void isValid_shouldReturnFalseWhenInvalidVersion() {
        BinaryIndexHeader header = new BinaryIndexHeader(
                BinaryIndexConstants.MAGIC,
                (byte) 99, // Неверная версия
                TEST_ENTRY_COUNT,
                TEST_ACTIVE_COUNT,
                TEST_FILE_SIZE,
                TEST_FILE_HASH,
                TEST_TIMESTAMP
        );

        assertFalse(header.isValid());
    }

    @Test
    void isValid_shouldReturnFalseWhenNegativeEntryCount() {
        BinaryIndexHeader header = new BinaryIndexHeader(
                BinaryIndexConstants.MAGIC,
                BinaryIndexConstants.VERSION,
                -1, // Отрицательное количество
                TEST_ACTIVE_COUNT,
                TEST_FILE_SIZE,
                TEST_FILE_HASH,
                TEST_TIMESTAMP
        );

        assertFalse(header.isValid());
    }

    @Test
    void isValid_shouldReturnFalseWhenNegativeActiveCount() {
        BinaryIndexHeader header = new BinaryIndexHeader(
                BinaryIndexConstants.MAGIC,
                BinaryIndexConstants.VERSION,
                TEST_ENTRY_COUNT,
                -5, // Отрицательное количество активных
                TEST_FILE_SIZE,
                TEST_FILE_HASH,
                TEST_TIMESTAMP
        );

        assertFalse(header.isValid());
    }

    @Test
    void isValid_shouldReturnFalseWhenActiveCountGreaterThanEntryCount() {
        BinaryIndexHeader header = new BinaryIndexHeader(
                BinaryIndexConstants.MAGIC,
                BinaryIndexConstants.VERSION,
                TEST_ENTRY_COUNT,
                TEST_ENTRY_COUNT + 10, // Активных больше чем всего записей
                TEST_FILE_SIZE,
                TEST_FILE_HASH,
                TEST_TIMESTAMP
        );

        assertFalse(header.isValid());
    }

    @Test
    void isValid_shouldReturnFalseWhenNegativeFileSize() {
        BinaryIndexHeader header = new BinaryIndexHeader(
                BinaryIndexConstants.MAGIC,
                BinaryIndexConstants.VERSION,
                TEST_ENTRY_COUNT,
                TEST_ACTIVE_COUNT,
                -1024, // Отрицательный размер
                TEST_FILE_HASH,
                TEST_TIMESTAMP
        );

        assertFalse(header.isValid());
    }

    // ============================================================
    // 4. ТЕСТЫ С ГРАНИЧНЫМИ ЗНАЧЕНИЯМИ
    // ============================================================

    @Test
    void write_shouldHandleEmptyHash() throws IOException {
        // Хеш может быть пустой строкой (8 нулевых байт)
        byte[] data = getDataBytes();
        assertEquals(48, data.length);

        // ✅ ИСПРАВЛЕНО: хеш находится в позициях 24-31 (не 32-39!)
        // Проверяем, что хеш - 8 нулевых байт
        for (int i = 24; i < 32; i++) {
            assertEquals(0, data[i], "Hash bytes should be zero at position " + i);
        }
    }

    private static byte[] getDataBytes() throws IOException {
        BinaryIndexHeader header = new BinaryIndexHeader(
                BinaryIndexConstants.MAGIC,
                BinaryIndexConstants.VERSION,
                TEST_ENTRY_COUNT,
                TEST_ACTIVE_COUNT,
                TEST_FILE_SIZE,
                "", // Пустой хеш
                TEST_TIMESTAMP
        );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        header.write(dos);
        dos.flush();

        return baos.toByteArray();
    }

    @Test
    void write_shouldHandleEmptyHash_v2() throws IOException {
        // Альтернативный подход: использовать известные данные для проверки
        DataInputStream dis = getDataInputStream();

        // Magic (0-3)
        assertEquals(BinaryIndexConstants.MAGIC, dis.readInt());

        // Version (4)
        assertEquals(BinaryIndexConstants.VERSION, dis.readByte());

        // Reserved (5-7) - пропускаем
        dis.readByte();
        dis.readByte();
        dis.readByte();

        // Entry Count (8-11)
        assertEquals(100, dis.readInt());

        // Active Count (12-15)
        assertEquals(80, dis.readInt());

        // File Size (16-23)
        assertEquals(1024L, dis.readLong());

        // ✅ Hash (24-31) - должен быть пустым (8 нулевых байт)
        byte[] hashBytes = new byte[8];
        dis.readFully(hashBytes);

        // ✅ ИСПРАВЛЕНО: проверяем, что все байты хеша равны нулю
        for (int i = 0; i < hashBytes.length; i++) {
            assertEquals(0, hashBytes[i], "Hash byte at position " + i + " should be 0");
        }

        // ✅ ИЛИ: проверяем, что строка состоит только из нулевых символов
        String hash = new String(hashBytes, StandardCharsets.UTF_8);
        assertTrue(hash.chars().allMatch(c -> c == 0),
                "Hash should contain only null characters");

        // ✅ ИЛИ: проверяем, что после удаления нулей строка пустая
        assertEquals("", hash.replace("\0", ""),
                "Hash should be empty after removing null characters");

        // Timestamp (32-39)
        assertEquals(1234567890L, dis.readLong());

        // Reserved (40-47)
        assertEquals(0L, dis.readLong());
    }

    private static DataInputStream getDataInputStream() throws IOException {
        BinaryIndexHeader header = new BinaryIndexHeader(
                BinaryIndexConstants.MAGIC,
                BinaryIndexConstants.VERSION,
                100,
                80,
                1024,
                "", // Пустой хеш
                1234567890L
        );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        header.write(dos);
        dos.flush();

        byte[] data = baos.toByteArray();

        // Проверяем всю структуру заголовка
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        return new DataInputStream(bais);
    }

    @Test
    void write_shouldHandleExactly8ByteHash() throws IOException {
        String exactHash = "12345678"; // Ровно 8 байт
        BinaryIndexHeader header = new BinaryIndexHeader(
                BinaryIndexConstants.MAGIC,
                BinaryIndexConstants.VERSION,
                TEST_ENTRY_COUNT,
                TEST_ACTIVE_COUNT,
                TEST_FILE_SIZE,
                exactHash,
                TEST_TIMESTAMP
        );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        header.write(dos);
        dos.flush();

        byte[] data = baos.toByteArray();

        // ✅ ИСПРАВЛЕНО: хеш в позициях 24-31
        byte[] hashBytes = new byte[8];
        System.arraycopy(data, 24, hashBytes, 0, 8);
        assertEquals(exactHash, new String(hashBytes, StandardCharsets.UTF_8));
    }

    @Test
    void write_shouldHandleShortHash() throws IOException {
        // Хеш короче 8 байт
        String shortHash = "abc";
        byte[] data = getBytes(shortHash);

        // ✅ Проверяем, что хеш дополнен нулями до 8 байт
        // Позиции 24-26: "abc"
        assertEquals('a', data[24]);
        assertEquals('b', data[25]);
        assertEquals('c', data[26]);
        // Позиции 27-31: нули
        for (int i = 27; i < 32; i++) {
            assertEquals(0, data[i], "Hash padding should be zero at position " + i);
        }

        // Читаем обратно через DataInputStream, чтобы проверить
        String hash = getHash(data);

        // Ожидаем "abc\0\0\0\0\0" - но trim уберет нули
        assertEquals(shortHash, hash.trim());
    }

    private static byte[] getBytes(String shortHash) throws IOException {
        BinaryIndexHeader header = new BinaryIndexHeader(
                BinaryIndexConstants.MAGIC,
                BinaryIndexConstants.VERSION,
                TEST_ENTRY_COUNT,
                TEST_ACTIVE_COUNT,
                TEST_FILE_SIZE,
                shortHash,
                TEST_TIMESTAMP
        );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        header.write(dos);
        dos.flush();

        return baos.toByteArray();
    }

    private static String getHash(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);

        // Пропускаем до хеша
        dis.readInt(); // magic
        dis.readByte(); // version
        dis.readByte();
        dis.readByte();
        dis.readByte(); // reserved
        dis.readInt(); // entryCount
        dis.readInt(); // activeCount
        dis.readLong(); // fileSize

        // Читаем хеш
        byte[] hashBytes = new byte[8];
        dis.readFully(hashBytes);
        return new String(hashBytes, StandardCharsets.UTF_8);
    }

    @Test
    void read_shouldHandleEmptyHash() throws IOException {
        // Создаем заголовок с пустым хешем
        DataInputStream dis = getDis();
        BinaryIndexHeader header = BinaryIndexHeader.read(dis);

        assertNotNull(header);

        // ✅ ИСПРАВЛЕНО: обрезаем нулевые символы при сравнении
        assertEquals("", header.fileHash().replace("\0", ""));

        // ✅ ИЛИ: проверяем, что хеш состоит только из нулевых байт
        assertTrue(header.fileHash().chars().allMatch(c -> c == 0));

        // ✅ ИЛИ: проверяем длину после удаления нулей
        assertEquals(0, header.fileHash().replace("\0", "").length());
    }

    private static DataInputStream getDis() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(BinaryIndexConstants.MAGIC);
        dos.writeByte(BinaryIndexConstants.VERSION);
        dos.writeByte(0);
        dos.writeByte(0);
        dos.writeByte(0);
        dos.writeInt(TEST_ENTRY_COUNT);
        dos.writeInt(TEST_ACTIVE_COUNT);
        dos.writeLong(TEST_FILE_SIZE);
        // 8 нулевых байт для хеша (позиции 24-31)
        for (int i = 0; i < 8; i++) {
            dos.writeByte(0);
        }
        dos.writeLong(TEST_TIMESTAMP);
        dos.writeLong(0);
        dos.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        return new DataInputStream(bais);
    }

    // ============================================================
    // 5. ТЕСТЫ ПОЛНОГО ЦИКЛА (запись -> чтение -> валидация)
    // ============================================================

    @Test
    void fullCycle_shouldPreserveAllFields() throws IOException {
        BinaryIndexHeader original = BinaryIndexHeader.create(
                1000,
                750,
                2048,
                "abc12345"
        );

        // Записываем
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        original.write(dos);
        dos.flush();

        // Читаем
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        DataInputStream dis = new DataInputStream(bais);
        BinaryIndexHeader read = BinaryIndexHeader.read(dis);

        // Проверяем все поля
        assertEquals(original.magic(), read.magic());
        assertEquals(original.version(), read.version());
        assertEquals(original.entryCount(), read.entryCount());
        assertEquals(original.activeCount(), read.activeCount());
        assertEquals(original.fileSize(), read.fileSize());
        assertEquals(original.fileHash(), read.fileHash());
        // Timestamp может отличаться на миллисекунды, но мы проверяем что он > 0
        assertTrue(read.timestamp() > 0);
        assertTrue(read.isValid());
    }

    // ============================================================
    // 6. ТЕСТ STRING ПРЕДСТАВЛЕНИЯ
    // ============================================================

    @Test
    void toString_shouldReturnFormattedString() {
        BinaryIndexHeader header = new BinaryIndexHeader(
                BinaryIndexConstants.MAGIC,
                BinaryIndexConstants.VERSION,
                100,
                80,
                1024,
                "testhash",
                1234567890L
        );

        String str = header.toString();

        assertNotNull(str);
        assertTrue(str.contains("version=1"));
        assertTrue(str.contains("entries=100"));
        assertTrue(str.contains("active=80"));
        assertTrue(str.contains("fileSize=1024"));
        assertTrue(str.contains("hash=testhash"));
    }

    // ============================================================
    // 7. ТЕСТЫ RECORD МЕТОДОВ (геттеры)
    // ============================================================

    @Test
    void recordMethods_shouldReturnCorrectValues() {
        BinaryIndexHeader header = new BinaryIndexHeader(
                0x4B4C5449,
                (byte) 1,
                150,
                120,
                2048L,
                "hash1234",
                9876543210L
        );

        assertEquals(0x4B4C5449, header.magic());
        assertEquals(1, header.version());
        assertEquals(150, header.entryCount());
        assertEquals(120, header.activeCount());
        assertEquals(2048L, header.fileSize());
        assertEquals("hash1234", header.fileHash());
        assertEquals(9876543210L, header.timestamp());
    }

    // ============================================================
    // 8. ТЕСТЫ СРАВНЕНИЯ (equals / hashCode)
    // ============================================================

    @Test
    void equals_shouldReturnTrueForIdenticalHeaders() {
        BinaryIndexHeader header1 = new BinaryIndexHeader(
                BinaryIndexConstants.MAGIC,
                BinaryIndexConstants.VERSION,
                100,
                80,
                1024,
                "testhash",
                1234567890L
        );

        BinaryIndexHeader header2 = new BinaryIndexHeader(
                BinaryIndexConstants.MAGIC,
                BinaryIndexConstants.VERSION,
                100,
                80,
                1024,
                "testhash",
                1234567890L
        );

        assertEquals(header1, header2);
        assertEquals(header1.hashCode(), header2.hashCode());
    }

    @Test
    void equals_shouldReturnFalseForDifferentHeaders() {
        BinaryIndexHeader header1 = new BinaryIndexHeader(
                BinaryIndexConstants.MAGIC,
                BinaryIndexConstants.VERSION,
                100,
                80,
                1024,
                "testhash",
                1234567890L
        );

        BinaryIndexHeader header2 = new BinaryIndexHeader(
                BinaryIndexConstants.MAGIC,
                BinaryIndexConstants.VERSION,
                200,
                150,
                2048,
                "another",
                9876543210L
        );

        assertNotEquals(header1, header2);
        assertNotEquals(header1.hashCode(), header2.hashCode());
    }

    @Test
    void equals_shouldReturnFalseForNull() {
        BinaryIndexHeader header = new BinaryIndexHeader(
                BinaryIndexConstants.MAGIC,
                BinaryIndexConstants.VERSION,
                100,
                80,
                1024,
                "testhash",
                1234567890L
        );

        assertNotEquals(null, header);
    }
}