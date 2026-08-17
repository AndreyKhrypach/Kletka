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

package Khrypach.Andrey.chess.kletka.pgn.index.model;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;


/**
 * Запись в индексе PGN файла.
 * Хранит информацию о партии: смещение, длину, версию, флаг удаления и кэшированные заголовки.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameIndexEntry implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // === Идентификация ===
    private int id;                     // Уникальный номер партии
    private long offset;                // Позиция в PGN-файле (в байтах)
    private int length;                 // Длина партии в байтах
    private int version;                // Версия партии (увеличивается при каждом редактировании)
    private boolean deleted;            // Флаг удаления (true - партия удалена)

    // === Хеш для детекции изменений ===
    private int hash;                   // Хеш содержимого партии

    // === Кэшированные заголовки ===
    private String white;
    private String black;
    private String eco;
    private String result;
    private String year;
    private String event;
    private String site;
    private String opening;
    private String variation;
    private int plyCount;

    /**
     * Проверяет, активна ли запись (не удалена)
     */
    public boolean isActive() {
        return !deleted;
    }

    /**
     * Создает новую версию записи (для редактирования)
     */
    public GameIndexEntry newVersion(long newOffset, int newLength, int newHash) {
        return GameIndexEntry.builder()
                .id(this.id)
                .offset(newOffset)
                .length(newLength)
                .version(this.version + 1)
                .deleted(false)
                .hash(newHash)
                .white(this.white)
                .black(this.black)
                .eco(this.eco)
                .result(this.result)
                .year(this.year)
                .event(this.event)
                .site(this.site)
                .opening(this.opening)
                .variation(this.variation)
                .plyCount(this.plyCount)
                .build();
    }

    /**
     * Создает запись-маркер удаления (версия увеличивается, deleted = true)
     */
    public GameIndexEntry markDeleted() {
        return GameIndexEntry.builder()
                .id(this.id)
                .offset(this.offset)
                .length(this.length)
                .version(this.version + 1)
                .deleted(true)
                .hash(this.hash)
                .white(this.white)
                .black(this.black)
                .eco(this.eco)
                .result(this.result)
                .year(this.year)
                .event(this.event)
                .site(this.site)
                .opening(this.opening)
                .variation(this.variation)
                .plyCount(this.plyCount)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameIndexEntry that = (GameIndexEntry) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("GameIndexEntry{id=%d, offset=%d, length=%d, version=%d, deleted=%s, %s vs %s}",
                id, offset, length, version, deleted, white, black);
    }
}