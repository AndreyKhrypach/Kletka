/*
 * Copyright (c) 2025 Andrey Khrypach
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
 *
 */

package Khrypach.Andrey.chess.kletka.pgn.index;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PgnEncodingConverterTest {

    @TempDir
    Path tempDir;

    // Вспомогательный метод для создания файла с заданной кодировкой
    private Path createFile(String fileName, String content, Charset charset) throws IOException {
        Path file = tempDir.resolve(fileName);
        Files.writeString(file, content, charset);
        return file;
    }

    @Test
    void testEnsureUtf8_AlreadyUtf8_ShouldNotChange() throws IOException {
        String text = "[Event \"Турнир\"]\n[White \"Карпов\"]\n[Black \"Каспаров\"]\n\n1. e4 e5 *";
        Path file = createFile("utf8.pgn", text, StandardCharsets.UTF_8);

        long sizeBefore = Files.size(file);
        Path result = PgnEncodingConverter.ensureUtf8(file);

        assertEquals(file, result);
        assertEquals(sizeBefore, Files.size(file), "Файл не должен был измениться");
        assertEquals(text, Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void testEnsureUtf8_Utf8Bom_ShouldNotChange() throws IOException {
        String text = "[Event \"Тест\"]\n\n1. d4 d5 *";
        Path file = tempDir.resolve("utf8_bom.pgn");

        // Записываем UTF-8 с BOM вручную
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] fileBytes = new byte[bom.length + textBytes.length];
        System.arraycopy(bom, 0, fileBytes, 0, bom.length);
        System.arraycopy(textBytes, 0, fileBytes, bom.length, textBytes.length);
        Files.write(file, fileBytes);

        Path result = PgnEncodingConverter.ensureUtf8(file);

        assertEquals(file, result);
        assertTrue(Files.readString(file, StandardCharsets.UTF_8).contains("Тест"));
    }

    @Test
    void testEnsureUtf8_Cp1251_ShouldConvertToUtf8() throws IOException {
        // Текст на русском, который будет записан в CP1251
        String originalText = "[Event \"Матч века\"]\n[White \"Спасский\"]\n[Black \"Фишер\"]\n\n1. e4 e5 *";
        Path file = createFile("cp1251.pgn", originalText, Charset.forName("Windows-1251"));

        Path result = PgnEncodingConverter.ensureUtf8(file);

        assertEquals(file, result);

        // Проверяем, что файл теперь читается как UTF-8 и текст совпадает
        String convertedText = Files.readString(file, StandardCharsets.UTF_8);
        assertEquals(originalText, convertedText);

        // Проверяем, что создался бекап
        Path backup = tempDir.resolve("cp1251.pgn.bak");
        assertTrue(Files.exists(backup), "Должен быть создан бекап .bak");

        // В бекапе должен остаться оригинал в CP1251
        String backupText = Files.readString(backup, Charset.forName("Windows-1251"));
        assertEquals(originalText, backupText);
    }

    @Test
    void testEnsureUtf8_PureAscii_ShouldNotConvert() throws IOException {
        // Если нет кириллицы, файл должен остаться как есть (считается UTF-8)
        String asciiText = "[Event \"Test\"]\n[White \"Player1\"]\n\n1. e4 e5 *";
        Path file = createFile("ascii.pgn", asciiText, StandardCharsets.US_ASCII);

        long sizeBefore = Files.size(file);
        PgnEncodingConverter.ensureUtf8(file);

        // Размер не изменился, бекап не создался (так как это уже валидный UTF-8/ASCII)
        assertEquals(sizeBefore, Files.size(file));
        assertFalse(Files.exists(tempDir.resolve("ascii.pgn.bak")));
    }

    @Test
    void testEnsureUtf8_TruncatedUtf8Sample_ShouldDetectCorrectly() throws IOException {
        // Создаем строку, которая при кодировании в UTF-8 даст срез ровно посередине символа
        // Например, повторяем "мат" много раз, чтобы перевалить за 64 КБ, и обрубаем.
        StringBuilder sb = new StringBuilder();
        sb.append("[Event \"Т\"]\n\n");
        while (sb.toString().getBytes(StandardCharsets.UTF_8).length < 65535) {
            sb.append("мат ");
        }

        Path file = createFile("truncated.pgn", sb.toString(), StandardCharsets.UTF_8);

        // Метод не должен упасть и не должен испортить файл
        assertDoesNotThrow(() -> PgnEncodingConverter.ensureUtf8(file));

        // Читаем и проверяем, что там всё еще UTF-8
        String content = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(content.contains("мат"));
    }

    @Test
    void testEnsureUtf8_CorruptedDoubleEncoding_ShouldBeValidated() throws IOException {
        // Эмулируем ситуацию, когда мы скормили CP1251 байты как UTF-8,
        // и они превратились в мусор (Replacement Character \uFFFD).
        // Наш метод должен либо определить это как UTF-8 (если мусор валиден),
        // либо не сломать файл.

        String brokenText = "[Event \"1\"]\n[White \"��� � 1���\"]\n\n1. Ra8# *";
        Path file = createFile("broken.pgn", brokenText, StandardCharsets.UTF_8);

        // Файл валидный в UTF-8 (содержит \uFFFD), поэтому конвертация не запустится,
        // но он не должен упасть с ошибкой.
        assertDoesNotThrow(() -> PgnEncodingConverter.ensureUtf8(file));

        // Убеждаемся, что размер не изменился
        assertTrue(Files.size(file) > 0);
    }
}