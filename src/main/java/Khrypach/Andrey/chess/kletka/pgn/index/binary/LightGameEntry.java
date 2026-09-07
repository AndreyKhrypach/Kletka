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

import Khrypach.Andrey.chess.kletka.pgn.index.model.GameIndexEntry;

/**
 * Лёгкая запись для быстрого отображения в таблице.
 * Содержит только поля, необходимые для отображения и поиска.
 *
 * @param offset Смещение в файле для загрузки полной записи
 * @param length Длина для загрузки полной записи
 */
public record LightGameEntry(int id, String white, String black, String result, String year, String event, String eco,
                             String opening, long offset, int length, boolean deleted, int hash) {

    public LightGameEntry(int id, String white, String black, String result,
                          String year, String event, String eco, String opening,
                          long offset, int length, boolean deleted, int hash) {
        this.id = id;
        this.white = white != null ? white : "";
        this.black = black != null ? black : "";
        this.result = result != null ? result : "*";
        this.year = year != null ? year : "";
        this.event = event != null ? event : "";
        this.eco = eco != null ? eco : "";
        this.opening = opening != null ? opening : "";
        this.offset = offset;
        this.length = length;
        this.deleted = deleted;
        this.hash = hash;
    }

    /**
     * Создаёт лёгкую запись из полной
     */
    public static LightGameEntry fromFull(GameIndexEntry entry) {
        return new LightGameEntry(
                entry.getId(),
                entry.getWhite(),
                entry.getBlack(),
                entry.getResult(),
                entry.getYear(),
                entry.getEvent(),
                entry.getEco(),
                entry.getOpening(),
                entry.getOffset(),
                entry.getLength(),
                entry.isDeleted(),
                entry.getHash()
        );
    }

    /**
     * Проверяет, соответствует ли запись поисковому запросу
     */
    public boolean matches(String query) {
        String lowerQuery = query.toLowerCase();
        return white.toLowerCase().contains(lowerQuery) ||
                black.toLowerCase().contains(lowerQuery) ||
                result.toLowerCase().contains(lowerQuery) ||
                year.toLowerCase().contains(lowerQuery) ||
                event.toLowerCase().contains(lowerQuery) ||
                eco.toLowerCase().contains(lowerQuery) ||
                opening.toLowerCase().contains(lowerQuery);
    }

    @Override
    public String toString() {
        return String.format("LightEntry{id=%d, %s vs %s, %s}", id, white, black, result);
    }
}