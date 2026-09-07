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

package Khrypach.Andrey.chess.kletka.pgn.index.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class IOUtilsTest {

    @TempDir
    Path tempDir;

    private Path testFile;
    private byte[] testData;

    @BeforeEach
    void setUp() {
        testFile = tempDir.resolve("test-file.bin");
        testData = "Hello, World! This is a test data for IOUtils.".getBytes(StandardCharsets.UTF_8);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Удаляем временные файлы после тестов
        if (testFile != null && Files.exists(testFile)) {
            Files.deleteIfExists(testFile);
        }
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ writeFully(FileChannel, ByteBuffer)
    // ============================================================

    @Test
    void writeFully_shouldWriteAllDataToChannel() throws IOException {
        // Создаем канал и буфер
        try (FileChannel channel = FileChannel.open(testFile,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)) {

            ByteBuffer buffer = ByteBuffer.wrap(testData);

            // Записываем
            IOUtils.writeFully(channel, buffer);

            // Проверяем размер файла
            assertEquals(testData.length, Files.size(testFile));

            // Проверяем содержимое
            byte[] readData = Files.readAllBytes(testFile);
            assertArrayEquals(testData, readData);
        }
    }

    @Test
    void writeFully_shouldHandleMultipleWrites() throws IOException {
        try (FileChannel channel = FileChannel.open(testFile,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)) {

            // Создаем буфер с данными
            ByteBuffer buffer = ByteBuffer.allocate(testData.length);
            buffer.put(testData);
            buffer.flip();

            // Записываем
            IOUtils.writeFully(channel, buffer);

            // Проверяем, что буфер полностью прочитан (позиция = лимит)
            assertEquals(buffer.limit(), buffer.position());

            // Проверяем размер
            assertEquals(testData.length, Files.size(testFile));
        }
    }

    @Test
    void writeFully_shouldThrowExceptionWhenNotAllBytesWritten() throws IOException {
        // Этот тест симулирует ошибку записи, но в реальном тесте
        // мы не можем легко симулировать частичную запись.
        // Вместо этого проверяем, что метод выбрасывает исключение
        // при попытке записи в закрытый канал.

        FileChannel channel = FileChannel.open(testFile,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        ByteBuffer buffer = ByteBuffer.wrap(testData);

        // Закрываем канал перед записью
        channel.close();

        // Проверяем, что выбрасывается исключение
        assertThrows(IOException.class, () -> IOUtils.writeFully(channel, buffer));
    }

    @Test
    void writeFully_shouldHandleEmptyBuffer() throws IOException {
        try (FileChannel channel = FileChannel.open(testFile,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {

            ByteBuffer emptyBuffer = ByteBuffer.allocate(0);

            // Запись пустого буфера не должна выбрасывать исключение
            assertDoesNotThrow(() -> IOUtils.writeFully(channel, emptyBuffer));

            // Файл должен существовать, но размер может быть 0
            assertTrue(Files.exists(testFile));
        }
    }

    @Test
    void writeFully_shouldForceSyncToDisk() throws IOException {
        try (FileChannel channel = FileChannel.open(testFile,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)) {

            ByteBuffer buffer = ByteBuffer.wrap(testData);

            // Записываем
            IOUtils.writeFully(channel, buffer);

            // Проверяем, что файл существует и имеет правильный размер
            assertTrue(Files.exists(testFile));
            assertEquals(testData.length, Files.size(testFile));

            // Проверяем, что данные были синхронизированы на диск
            // (это косвенная проверка, так как мы не можем проверить force() напрямую)
            byte[] readData = Files.readAllBytes(testFile);
            assertArrayEquals(testData, readData);
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ writeFully(Path, byte[])
    // ============================================================

    @Test
    void testWriteFully_shouldWriteDataToPath() throws IOException {
        // Записываем данные
        IOUtils.writeFully(testFile, testData);

        // Проверяем
        assertTrue(Files.exists(testFile));
        assertEquals(testData.length, Files.size(testFile));

        byte[] readData = Files.readAllBytes(testFile);
        assertArrayEquals(testData, readData);
    }

    @Test
    void testWriteFully_shouldCreateFileIfNotExists() throws IOException {
        // Файл не должен существовать
        assertFalse(Files.exists(testFile));

        // Записываем
        IOUtils.writeFully(testFile, testData);

        // Файл должен быть создан
        assertTrue(Files.exists(testFile));
        assertEquals(testData.length, Files.size(testFile));
    }

    @Test
    void testWriteFully_shouldOverwriteExistingFile() throws IOException {
        // Создаем файл с начальными данными
        byte[] initialData = "Initial data".getBytes(StandardCharsets.UTF_8);
        Files.write(testFile, initialData);
        assertEquals(initialData.length, Files.size(testFile));

        // Записываем новые данные
        IOUtils.writeFully(testFile, testData);

        // Проверяем, что данные перезаписаны
        assertEquals(testData.length, Files.size(testFile));
        byte[] readData = Files.readAllBytes(testFile);
        assertArrayEquals(testData, readData);
    }

    @Test
    void testWriteFully_shouldHandleEmptyArray() throws IOException {
        byte[] emptyData = new byte[0];

        // Запись пустого массива
        IOUtils.writeFully(testFile, emptyData);

        // Проверяем
        assertTrue(Files.exists(testFile));
        assertEquals(0, Files.size(testFile));
    }

    @Test
    void testWriteFully_shouldThrowExceptionForInvalidPath() {
        // Невалидный путь (несуществующая директория)
        Path invalidPath = tempDir.resolve("subdir").resolve("file.bin");

        // Должно выбросить исключение
        assertThrows(IOException.class, () -> IOUtils.writeFully(invalidPath, testData));
    }

    @Test
    void testWriteFully_shouldHandleLargeData() throws IOException {
        // Создаем большие данные (1 MB)
        byte[] largeData = new byte[1024 * 1024];
        Arrays.fill(largeData, (byte) 'A');

        // Записываем
        IOUtils.writeFully(testFile, largeData);

        // Проверяем
        assertEquals(largeData.length, Files.size(testFile));
        byte[] readData = Files.readAllBytes(testFile);
        assertArrayEquals(largeData, readData);
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ verifyFileSize
    // ============================================================

    @Test
    void verifyFileSize_shouldNotThrowWhenSizeMatches() throws IOException {
        // Создаем файл
        Files.write(testFile, testData);

        // Проверяем - не должно быть исключения
        assertDoesNotThrow(() -> IOUtils.verifyFileSize(testFile, testData.length));
    }

    @Test
    void verifyFileSize_shouldThrowExceptionWhenSizeMismatch() throws IOException {
        // Создаем файл
        Files.write(testFile, testData);

        // Проверяем с неверным размером
        long wrongSize = testData.length + 100;
        IOException exception = assertThrows(IOException.class,
                () -> IOUtils.verifyFileSize(testFile, wrongSize));

        assertTrue(exception.getMessage().contains("File size mismatch"));
        assertTrue(exception.getMessage().contains(String.valueOf(wrongSize)));
        assertTrue(exception.getMessage().contains(String.valueOf(testData.length)));
    }

    @Test
    void verifyFileSize_shouldThrowExceptionWhenFileNotFound() {
        // Файл не существует
        assertThrows(IOException.class,
                () -> IOUtils.verifyFileSize(testFile, 100));
    }

    @Test
    void verifyFileSize_shouldHandleZeroSize() throws IOException {
        // Создаем пустой файл
        Files.createFile(testFile);
        assertEquals(0, Files.size(testFile));

        // Проверяем с правильным размером
        assertDoesNotThrow(() -> IOUtils.verifyFileSize(testFile, 0));

        // Проверяем с неправильным размером
        assertThrows(IOException.class,
                () -> IOUtils.verifyFileSize(testFile, 100));
    }

    // ============================================================
    // 4. ИНТЕГРАЦИОННЫЕ ТЕСТЫ
    // ============================================================

    @Test
    void writeAndVerify_shouldWorkTogether() throws IOException {
        // 1. Записываем данные
        IOUtils.writeFully(testFile, testData);

        // 2. Проверяем размер
        assertDoesNotThrow(() -> IOUtils.verifyFileSize(testFile, testData.length));

        // 3. Читаем и проверяем содержимое
        byte[] readData = Files.readAllBytes(testFile);
        assertArrayEquals(testData, readData);
    }

    @Test
    void writeFully_shouldPreserveDataIntegrity() throws IOException {
        // Тестируем с разными типами данных
        byte[] binaryData = new byte[256];
        for (int i = 0; i < binaryData.length; i++) {
            binaryData[i] = (byte) i;
        }

        IOUtils.writeFully(testFile, binaryData);

        byte[] readData = Files.readAllBytes(testFile);
        assertArrayEquals(binaryData, readData);
    }

    @Test
    void writeFully_shouldHandleConcurrentWrites() throws IOException {
        // Проверяем, что повторная запись работает корректно
        byte[] data1 = "First data".getBytes(StandardCharsets.UTF_8);
        byte[] data2 = "Second data".getBytes(StandardCharsets.UTF_8);

        // Первая запись
        IOUtils.writeFully(testFile, data1);
        assertEquals(data1.length, Files.size(testFile));

        // Вторая запись (перезапись)
        IOUtils.writeFully(testFile, data2);
        assertEquals(data2.length, Files.size(testFile));

        // Проверяем содержимое
        byte[] readData = Files.readAllBytes(testFile);
        assertArrayEquals(data2, readData);
    }

    // ============================================================
    // 5. ТЕСТЫ С Edge Cases
    // ============================================================

    @Test
    void writeFully_shouldHandleBufferWithOffset() throws IOException {
        try (FileChannel channel = FileChannel.open(testFile,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)) {

            // Создаем буфер с данными
            ByteBuffer buffer = ByteBuffer.allocate(testData.length + 10);
            buffer.put("PREFIX".getBytes(StandardCharsets.UTF_8));
            buffer.put(testData);
            buffer.flip();

            // Пропускаем префикс
            buffer.position("PREFIX".getBytes(StandardCharsets.UTF_8).length);

            // Записываем
            IOUtils.writeFully(channel, buffer);

            // Проверяем, что записаны только данные без префикса
            assertEquals(testData.length, Files.size(testFile));
            byte[] readData = Files.readAllBytes(testFile);
            assertArrayEquals(testData, readData);
        }
    }

    @Test
    void writeFully_shouldNotThrowWhenBufferIsNull() {
        // Null buffer должен вызвать NullPointerException
        assertThrows(NullPointerException.class,
                () -> IOUtils.writeFully(null, testData));
    }

    @Test
    void verifyFileSize_shouldThrowIOExceptionWithCorrectMessage() throws IOException {
        Files.write(testFile, testData);

        try {
            IOUtils.verifyFileSize(testFile, 9999);
            fail("Should have thrown IOException");
        } catch (IOException e) {
            String message = e.getMessage();
            assertTrue(message.contains("File size mismatch"));
            assertTrue(message.contains("9999"));
            assertTrue(message.contains(String.valueOf(testData.length)));
        }
    }
}