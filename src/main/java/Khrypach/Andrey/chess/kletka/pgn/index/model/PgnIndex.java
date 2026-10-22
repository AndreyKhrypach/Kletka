/*
 *
 *  * Copyright (c) 2025 Andrey Khrypach
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

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Весь индекс PGN-файла.
 * Содержит метаданные и список записей о партиях.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PgnIndex implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(PgnIndex.class);

    @Serial
    private static final long serialVersionUID = 1L;

    // === Версия формата индекса ===
    public static final int FORMAT_VERSION = 1;

    // === Мета-информация ===
    private int version;                // Версия формата индекса
    private String fileHash;            // Хеш всего PGN-файла (для детекции внешних изменений)
    private long fileSize;              // Размер PGN-файла на момент создания индекса
    private int gameCount;              // Общее количество партий (включая удалённые)
    private int activeCount;            // Количество активных (не удалённых) партий

    // === Записи о партиях ===
    @Builder.Default
    private List<GameIndexEntry> entries = new ArrayList<>();

    // === Вспомогательные индексы ===
    private transient List<Integer> activeIds;      // Список ID активных партий (кэш)

    /**
     * Получает запись по ID
     */
    public GameIndexEntry getEntryById(int id) {
        return entries.stream()
                .filter(e -> e.getId() == id)
                .findFirst()
                .orElse(null);
    }

    /**
     * Получает активную запись по ID (не удаленную)
     */
    public GameIndexEntry getActiveEntryById(int id) {
        return entries.stream()
                .filter(e -> e.getId() == id && !e.isDeleted())
                .findFirst()
                .orElse(null);
    }

    /**
     * Получает все активные записи
     */
    public List<GameIndexEntry> getActiveEntries() {
        return entries.stream()
                .filter(e -> !e.isDeleted())
                .collect(Collectors.toList());
    }

    /**
     * Получает все удаленные записи
     */
    public List<GameIndexEntry> getDeletedEntries() {
        return entries.stream()
                .filter(GameIndexEntry::isDeleted)
                .collect(Collectors.toList());
    }

    /**
     * Получает максимальный ID в индексе
     */
    public int getMaxId() {
        return entries.stream()
                .mapToInt(GameIndexEntry::getId)
                .max()
                .orElse(0);
    }

    /**
     * Получает следующий свободный ID
     */
    public int getNextId() {
        int maxId = entries.stream()
                .mapToInt(GameIndexEntry::getId)
                .max()
                .orElse(0);
        return maxId + 1;
    }

    /**
     * Добавляет новую запись в индекс.
     * O(1) — ID монотонно растут (getNextId), дубликата быть не может.
     * Счётчики инкрементируются, refreshCache() НЕ вызывается —
     * его нужно вызвать один раз в конце батча.
     */
    public void addEntry(GameIndexEntry entry) {
        if (entry == null) return;

        entries.add(entry);
        gameCount = entries.size();
        if (!entry.isDeleted()) {
            activeCount++;
        }
    }

    /**
     * Обновляет существующую запись.
     * O(n) поиск по ID (неизбежно для ArrayList), но без стримов и без refreshCache().
     */
    public void updateEntry(GameIndexEntry entry) {
        if (entry == null) return;

        for (int i = 0; i < entries.size(); i++) {
            GameIndexEntry old = entries.get(i);
            if (old.getId() == entry.getId()) {
                // Корректируем activeCount, если флаг deleted изменился
                if (old.isDeleted() != entry.isDeleted()) {
                    activeCount += entry.isDeleted() ? -1 : 1;
                }
                entries.set(i, entry);
                gameCount = entries.size();
                return;
            }
        }
        log.warn("Entry with ID {} not found for update", entry.getId());
    }

    /**
     * Проверяет, есть ли удалённые партии для перепаковки
     */
    public boolean hasDeletedGames(PgnIndex index) {
        if (index == null) return false;
        return index.getDeletedEntries() != null && !index.getDeletedEntries().isEmpty();
    }

    /**
     * Проверяет, нужно ли выполнить repack (очистку)
     * Возвращает true, если файл разросся в 2+ раза
     */
    public boolean needsRepack() {
        if (activeCount == 0) return false;

        long activeSize = entries.stream()
                .filter(e -> !e.isDeleted())
                .mapToLong(GameIndexEntry::getLength)
                .sum();

        if (activeSize == 0) return false;

        long totalSize = entries.stream()
                .mapToLong(GameIndexEntry::getLength)
                .sum();

        return (double) totalSize / activeSize > 2.0;
    }

    /**
     * Вычисляет отношение размера файла к размеру активных данных
     */
    public double getGrowthRatio() {
        if (activeCount == 0) return 1.0;

        long activeSize = entries.stream()
                .filter(e -> !e.isDeleted())
                .mapToLong(GameIndexEntry::getLength)
                .sum();

        long totalSize = entries.stream()
                .mapToLong(GameIndexEntry::getLength)
                .sum();

        if (activeSize == 0) return 1.0;
        return (double) totalSize / activeSize;
    }

    /**
     * Получает кэшированный список ID активных партий
     */
    public List<Integer> getActiveIds() {
        if (activeIds == null) {
            activeIds = entries.stream()
                    .filter(e -> !e.isDeleted())
                    .map(GameIndexEntry::getId)
                    .collect(Collectors.toList());
        }
        return activeIds;
    }

    /**
     * Обновляет кэш активных ID.
     * Вызывать один раз в конце батча или после серии изменений.
     */
    public void refreshCache() {
        this.activeIds = entries.stream()
                .filter(e -> !e.isDeleted())
                .map(GameIndexEntry::getId)
                .collect(Collectors.toList());

        // Пересчитываем счётчики на всякий случай (O(n), но редко)
        this.gameCount = entries.size();
        this.activeCount = this.activeIds.size();
    }

    @Override
    public String toString() {
        return String.format("PgnIndex{version=%d, games=%d, active=%d, fileSize=%d}",
                version, gameCount, activeCount, fileSize);
    }
}