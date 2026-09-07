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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Вспомогательный класс для безопасной записи данных
 */
public class IOUtils {

    /**
     * Безопасно записывает данные из ByteBuffer в FileChannel
     * @param channel канал для записи
     * @param buffer буфер с данными
     * @throws IOException если запись не удалась или записано не все данные
     */
    public static void writeFully(FileChannel channel, ByteBuffer buffer) throws IOException {
        int totalBytes = buffer.remaining();
        if (totalBytes == 0) {
            return;
        }

        int written = 0;
        while (buffer.hasRemaining()) {
            written += channel.write(buffer);
        }

        if (written != totalBytes) {
            throw new IOException(String.format(
                    "Failed to write entire buffer: wrote %d bytes but expected %d bytes",
                    written, totalBytes));
        }

        // Принудительно сбрасываем на диск
        channel.force(true);
    }

    /**
     * Безопасно записывает байтовый массив в файл
     */
    public static void writeFully(Path path, byte[] data) throws IOException {
        try (FileChannel channel = FileChannel.open(path,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.READ)) {

            ByteBuffer buffer = ByteBuffer.wrap(data);
            writeFully(channel, buffer);
        }
    }

    /**
     * Проверяет, что файл имеет правильный размер
     */
    public static void verifyFileSize(Path path, long expectedSize) throws IOException {
        long actualSize = Files.size(path);
        if (actualSize != expectedSize) {
            throw new IOException(String.format(
                    "File size mismatch: expected %d bytes, actual %d bytes",
                    expectedSize, actualSize));
        }
    }
}
