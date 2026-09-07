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

package Khrypach.Andrey.chess.kletka.pgn.index.binary;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Заголовок бинарного индекса
 * <p>
 * Структура (48 байт):
 * - Magic: 4 байта (0x4B4C5449)
 * - Version: 1 байт
 * - Reserved: 3 байта
 * - Entry Count: 4 байта
 * - Active Count: 4 байта
 * - File Size: 8 байт
 * - File Hash: 8 байт (CRC32 в hex)
 * - Timestamp: 8 байт
 * - Reserved: 8 байт
 */
public record BinaryIndexHeader(int magic, byte version, int entryCount, int activeCount, long fileSize,
                                String fileHash, long timestamp) {

    /**
     * Создаёт новый заголовок
     */
    public static BinaryIndexHeader create(int entryCount, int activeCount,
                                           long fileSize, String fileHash) {
        return new BinaryIndexHeader(
                BinaryIndexConstants.MAGIC,
                BinaryIndexConstants.VERSION,
                entryCount,
                activeCount,
                fileSize,
                fileHash,
                System.currentTimeMillis()
        );
    }

    /**
     * Читает заголовок из потока
     */
    public static BinaryIndexHeader read(DataInputStream dis) throws IOException {
        int magic = dis.readInt();
        if (magic != BinaryIndexConstants.MAGIC) {
            throw new IOException("Invalid magic number: 0x" + Integer.toHexString(magic));
        }

        byte version = dis.readByte();
        if (version != BinaryIndexConstants.VERSION) {
            throw new IOException("Unsupported version: " + version);
        }

        // Reserved 3 байта
        dis.readByte();
        dis.readByte();
        dis.readByte();

        int entryCount = dis.readInt();
        int activeCount = dis.readInt();
        long fileSize = dis.readLong();

        // Читаем хеш как 8 байт (CRC32 в hex)
        byte[] hashBytes = new byte[8];
        dis.readFully(hashBytes);
        String fileHash = new String(hashBytes, StandardCharsets.UTF_8);

        long timestamp = dis.readLong();

        // Reserved 8 байт
        dis.readLong();

        return new BinaryIndexHeader(magic, version, entryCount, activeCount,
                fileSize, fileHash, timestamp);
    }

    /**
     * Записывает заголовок в поток
     */
    public void write(DataOutputStream dos) throws IOException {
        dos.writeInt(magic);
        dos.writeByte(version);
        dos.writeByte(0);
        dos.writeByte(0);
        dos.writeByte(0);
        dos.writeInt(entryCount);
        dos.writeInt(activeCount);
        dos.writeLong(fileSize);

        byte[] hashBytes = fileHash.getBytes(StandardCharsets.UTF_8);
        if (hashBytes.length > 8) {
            throw new IOException("File hash too long: " + hashBytes.length);
        }
        dos.write(hashBytes);
        // Дополняем нулями до 8 байт
        for (int i = hashBytes.length; i < 8; i++) {
            dos.writeByte(0);
        }

        dos.writeLong(timestamp);
        dos.writeLong(0); // Reserved
    }

    /**
     * Проверяет, что заголовок валидный
     */
    public boolean isValid() {
        return magic == BinaryIndexConstants.MAGIC &&
                version == BinaryIndexConstants.VERSION &&
                entryCount >= 0 &&
                activeCount >= 0 &&
                activeCount <= entryCount &&
                fileSize >= 0;
    }

    @Override
    public String toString() {
        return String.format("BinaryIndexHeader{version=%d, entries=%d, active=%d, fileSize=%d, hash=%s}",
                version, entryCount, activeCount, fileSize, fileHash);
    }
}