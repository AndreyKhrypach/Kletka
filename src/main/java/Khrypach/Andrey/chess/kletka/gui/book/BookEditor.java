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

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Редактор Polyglot книги — управляет временными изменениями
 * Хранит "грязные" записи, которые еще не сохранены в файл
 */
@Getter
public class BookEditor {

    private static final Logger log = LoggerFactory.getLogger(BookEditor.class);

    /**
     * -- GETTER --
     *  Получает все "грязные" записи
     */
    // Карта "грязных" записей: key -> список PolyglotEntry
    private final Map<Long, List<PolyglotEntry>> dirtyEntries = new HashMap<>();

    /**
     * Добавляет запись в книгу (временное изменение)
     * @param key Zobrist хеш позиции
     * @param entry запись для добавления
     * @return true если запись добавлена, false если уже существует
     */
    public boolean addEntry(long key, PolyglotEntry entry) {
        log.info("[BOOK EDITOR] addEntry called from: {}",
                Thread.currentThread().getStackTrace()[2]);
        // Проверяем, есть ли уже такая запись
        if (hasEntry(key, entry.getUciMove())) {
            log.trace("Entry already exists: key=0x{}, move={}",
                    Long.toHexString(key), entry.getUciMove());
            return false;
        }

        // Добавляем запись
        dirtyEntries.computeIfAbsent(key, k -> new ArrayList<>()).add(entry);
        log.trace("Added dirty entry: key=0x{}, move={}, weight={}",
                Long.toHexString(key), entry.getUciMove(), entry.weight());
        return true;
    }

    /**
     * Проверяет, существует ли уже такой ход в "грязных" записях
     */
    public boolean hasEntry(long key, String uciMove) {
        List<PolyglotEntry> entries = dirtyEntries.get(key);
        if (entries == null || entries.isEmpty()) {
            return false;
        }

        return entries.stream().anyMatch(e -> e.getUciMove().equals(uciMove));
    }

    /**
     * Проверяет, есть ли несохраненные изменения
     */
    public boolean hasUnsavedChanges() {
        return !dirtyEntries.isEmpty();
    }

    /**
     * Получает количество "грязных" записей
     */
    public int getDirtyCount() {
        return dirtyEntries.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    /**
     * Очищает все временные изменения (после сохранения)
     */
    public void clear() {
        log.info("[BOOK EDITOR] clear called. Before: {} entries", dirtyEntries.size());
        int count = getDirtyCount();
        dirtyEntries.clear();
        if (count > 0) {
            log.debug("Cleared {} dirty entries", count);
        }
    }

    /**
     * Удаляет запись из dirtyEntries
     * @param key Zobrist хеш позиции
     * @param uciMove UCI хода
     * @return true если запись была удалена, false если не найдена
     */
    public boolean removeEntry(long key, String uciMove) {
        List<PolyglotEntry> entries = dirtyEntries.get(key);
        if (entries == null || entries.isEmpty()) {
            return false;
        }

        boolean removed = entries.removeIf(entry -> entry.getUciMove().equals(uciMove));

        if (removed && entries.isEmpty()) {
            dirtyEntries.remove(key);
        }

        if (removed) {
            log.debug("Entry removed: key=0x{}, uci={}", Long.toHexString(key), uciMove);
        }

        return removed;
    }
}