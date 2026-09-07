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
import Khrypach.Andrey.chess.kletka.pgn.index.model.PgnIndex;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Ленивый индекс с лёгкими записями в памяти.
 * - Все лёгкие записи (LightGameEntry) загружены в память для быстрого поиска
 * - Полные записи (GameIndexEntry) загружаются по требованию блоками
 */
public class LazyPgnIndex extends PgnIndex {

    private static final Logger log = LoggerFactory.getLogger(LazyPgnIndex.class);

    // ========== ПОЛЯ ==========
    private final FileChannel channel;
    private final ByteBuffer buffer;
    private final long dataStart;

    @Getter
    private final List<LightGameEntry> lightEntries;

    // Кэш полных записей (загружаются по требованию)
    @Getter
    private final Map<Integer, GameIndexEntry> fullEntryCache = new ConcurrentHashMap<>();

    @Getter
    private volatile boolean allFullEntriesLoaded = false;

    // ========== КОНСТРУКТОР ==========
    public LazyPgnIndex(
            BinaryIndexHeader header,
            FileChannel channel,
            ByteBuffer buffer,
            long dataStart,
            List<LightGameEntry> lightEntries) {

        super();

        this.channel = channel;
        this.buffer = buffer;
        this.dataStart = dataStart;
        this.lightEntries = lightEntries;

        // Устанавливаем метаданные
        setVersion(PgnIndex.FORMAT_VERSION);
        setFileHash(header.fileHash());
        setFileSize(header.fileSize());
        setGameCount(header.entryCount());
        setActiveCount(header.activeCount());
    }

    // ========== ЛЁГКИЕ ЗАПИСИ (ДЛЯ ПОИСКА И ОТОБРАЖЕНИЯ) ==========

    /**
     * Получает лёгкие записи с пагинацией
     */
    public List<LightGameEntry> getLightEntriesPage(int start, int count) {
        if (start < 0) start = 0;
        if (start >= lightEntries.size()) return new ArrayList<>();

        int end = Math.min(start + count, lightEntries.size());
        return lightEntries.subList(start, end);
    }

    /**
     * Получает первую страницу лёгких записей (50)
     */
    public List<LightGameEntry> getFirstPage() {
        return getLightEntriesPage(0, 50);
    }

    /**
     * Поиск по лёгким записям (все в памяти)
     */
    public List<LightGameEntry> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return lightEntries;
        }

        String lowerQuery = query.toLowerCase().trim();
        return lightEntries.stream()
                .filter(entry -> entry.matches(lowerQuery))
                .collect(Collectors.toList());
    }

    /**
     * Поиск с пагинацией
     */
    public List<LightGameEntry> search(String query, int start, int count) {
        if (query == null || query.trim().isEmpty()) {
            return getLightEntriesPage(start, count);
        }

        String lowerQuery = query.toLowerCase().trim();
        List<LightGameEntry> filtered = lightEntries.stream()
                .filter(entry -> entry.matches(lowerQuery))
                .collect(Collectors.toList());

        if (start < 0) start = 0;
        if (start >= filtered.size()) return new ArrayList<>();

        int end = Math.min(start + count, filtered.size());
        return filtered.subList(start, end);
    }

    // ========== ЗАГРУЗКА ПОЛНЫХ ЗАПИСЕЙ (БЛОКАМИ) ==========

    /**
     * Загружает полные записи для указанного диапазона лёгких записей
     * @param start начальный индекс в lightEntries
     * @param count количество записей для загрузки
     * @param progressCallback колбэк прогресса
     * @return список полных записей
     */
    public List<GameIndexEntry> loadFullEntries(int start, int count, Consumer<Integer> progressCallback) {
        if (start < 0) start = 0;
        if (start >= lightEntries.size()) return new ArrayList<>();

        int end = Math.min(start + count, lightEntries.size());
        List<GameIndexEntry> result = new ArrayList<>(end - start);

        for (int i = start; i < end; i++) {
            LightGameEntry light = lightEntries.get(i);

            // Проверяем кэш
            GameIndexEntry cached = fullEntryCache.get(light.id());
            if (cached != null) {
                result.add(cached);
            } else {
                try {
                    GameIndexEntry full = loadFullEntry(light);
                    fullEntryCache.put(light.id(), full);
                    result.add(full);
                } catch (IOException e) {
                    log.error("loadFullEntries: Failed to load full entry for id {}: {}", light.id(), e.getMessage());
                }
            }

            if (progressCallback != null && (i - start) % 10 == 0) {
                progressCallback.accept(i - start);
            }
        }

        return result;
    }

    /**
     * Загружает первые N полных записей (для начальной загрузки)
     */
    public List<GameIndexEntry> loadInitialFullEntries(int count) {
        return loadFullEntries(0, count, null);
    }

    // ========== ПОЛНАЯ ЗАПИСЬ ПО ID (ЛЕНИВО) ==========

    /**
     * Получает полную запись по ID (лениво)
     */
    public GameIndexEntry getFullEntry(int id) {
        // Проверяем кэш
        GameIndexEntry cached = fullEntryCache.get(id);
        if (cached != null) {
            return cached;
        }

        // Ищем лёгкую запись
        LightGameEntry light = lightEntries.stream()
                .filter(e -> e.id() == id)
                .findFirst()
                .orElse(null);

        if (light == null) {
            return null;
        }

        // Загружаем полную запись
        try {
            GameIndexEntry full = loadFullEntry(light);
            fullEntryCache.put(id, full);
            return full;
        } catch (IOException e) {
            log.error("Failed to load full entry for id {}: {}", id, e.getMessage());
            return null;
        }
    }

    // ========== ЗАГРУЗКА ВСЕХ ПОЛНЫХ ЗАПИСЕЙ ==========

    /**
     * Загружает все полные записи
     */
    public void loadAllFullEntries(Consumer<Integer> progressCallback) {
        if (allFullEntriesLoaded) return;

        log.info("Loading all full entries...");
        int loaded = 0;

        for (LightGameEntry light : lightEntries) {
            if (!fullEntryCache.containsKey(light.id())) {
                try {
                    GameIndexEntry full = loadFullEntry(light);
                    fullEntryCache.put(light.id(), full);
                } catch (IOException e) {
                    log.error("Failed to load full entry: {}", e.getMessage());
                }
            }
            loaded++;
            if (progressCallback != null && loaded % 100 == 0) {
                progressCallback.accept(loaded);
            }
        }

        allFullEntriesLoaded = true;
        log.info("All {} full entries loaded", fullEntryCache.size());
    }

    /**
     * Переопределяем метод для поиска по ID
     */
    @Override
    public GameIndexEntry getEntryById(int id) {
        return getFullEntry(id);
    }

    @Override
    public GameIndexEntry getActiveEntryById(int id) {
        return lightEntries.stream()
                .filter(e -> e.id() == id && !e.deleted())
                .findFirst()
                .map(light -> {
                    // Если есть в кэше - возвращаем оттуда
                    GameIndexEntry cached = fullEntryCache.get(id);
                    if (cached != null && !cached.isDeleted()) {
                        return cached;
                    }
                    // Иначе загружаем
                    try {
                        return loadFullEntry(light);
                    } catch (IOException e) {
                        log.error("Failed to load full entry for {}", id, e);
                        return null;
                    }
                })
                .orElse(null);
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    /**
     * Загружает полную запись из буфера по лёгкой записи
     */
    private GameIndexEntry loadFullEntry(LightGameEntry light) throws IOException {
        long offset = dataStart + light.offset();

        // Позиционируемся в буфере
        buffer.position((int) offset);

        int id = buffer.getInt();
        long offsetVal = buffer.getLong();
        int length = buffer.getInt();
        int version = buffer.getInt();
        boolean deleted = buffer.get() == 1;
        int hash = buffer.getInt();

        String white = readString();
        String black = readString();
        String eco = readString();
        String result = readString();
        String year = readString();
        String event = readString();
        String site = readString();
        String opening = readString();
        String variation = readString();
        int plyCount = buffer.getInt();

        return GameIndexEntry.builder()
                .id(id)
                .offset(offsetVal)
                .length(length)
                .version(version)
                .deleted(deleted)
                .hash(hash)
                .white(white)
                .black(black)
                .eco(eco)
                .result(result)
                .year(year)
                .event(event)
                .site(site)
                .opening(opening)
                .variation(variation)
                .plyCount(plyCount)
                .build();
    }

    /**
     * Читает строку из буфера
     */
    private String readString() {
        short length = buffer.getShort();
        if (length == 0) return "";

        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * Получает количество записей
     */
    public int getEntryCount() {
        return lightEntries.size();
    }

    /**
     * Закрывает ресурсы
     */
    public void close() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (IOException e) {
            log.warn("Error closing channel: {}", e.getMessage());
        }
    }

    // ========== ПЕРЕОПРЕДЕЛЁННЫЙ МЕТОД ==========

    @Override
    public List<GameIndexEntry> getEntries() {
        if (!allFullEntriesLoaded) {
            loadAllFullEntries(null);
        }
        return new ArrayList<>(fullEntryCache.values());
    }

    /**
     * Переопределяем метод для получения активных записей
     */
    @Override
    public List<GameIndexEntry> getActiveEntries() {
        if (!allFullEntriesLoaded) {
            loadAllFullEntries(null);
        }
        return fullEntryCache.values().stream()
                .filter(e -> !e.isDeleted())
                .collect(Collectors.toList());
    }

    /**
     * Переопределяем метод для проверки наличия удалённых
     */
    @Override
    public boolean hasDeletedGames(PgnIndex index) {
        if (index == null) return false;
        return lightEntries.stream().anyMatch(LightGameEntry::deleted);
    }

    /**
     * Переопределяем метод для получения количества удалённых
     */
    public int getDeletedCount() {
        return (int) lightEntries.stream().filter(LightGameEntry::deleted).count();
    }

    /**
     * Получает только активные (не удалённые) лёгкие записи
     */
    public List<LightGameEntry> getActiveLightEntries() {
        return lightEntries.stream()
                .filter(entry -> !entry.deleted())
                .collect(Collectors.toList());
    }

    /**
     * Получает активную лёгкую запись по ID
     */
    public LightGameEntry getActiveLightEntry(int id) {
        return lightEntries.stream()
                .filter(e -> e.id() == id && !e.deleted())
                .findFirst()
                .orElse(null);
    }

    /**
     * Получает количество активных записей
     */
    public int getActiveLightCount() {
        return (int) lightEntries.stream()
                .filter(entry -> !entry.deleted())
                .count();
    }

    @Override
    public int getNextId() {
        log.debug("LazyPgnIndex.getNextId() called");
        int maxId = lightEntries.stream()
                .mapToInt(LightGameEntry::id)
                .max()
                .orElse(0);
        log.debug("Max ID from lightEntries: {}", maxId);
        return maxId + 1;
    }

    @Override
    public int getMaxId() {
        return lightEntries.stream()
                .mapToInt(LightGameEntry::id)
                .max()
                .orElse(0);
    }

    // В LazyPgnIndex.java
    @Override
    public void addEntry(GameIndexEntry entry) {
        log.info("LazyPgnIndex.addEntry() called for ID: {}", entry.getId());

        // Добавляем в кэш полных записей
        fullEntryCache.put(entry.getId(), entry);

        // Создаём LightGameEntry из полной записи
        LightGameEntry light = LightGameEntry.fromFull(entry);

        // Добавляем в список лёгких записей
        lightEntries.add(light);

        // Обновляем счётчики
        setGameCount(lightEntries.size());
        setActiveCount((int) lightEntries.stream().filter(e -> !e.deleted()).count());

        log.info("After add: lightEntries={}, gameCount={}, activeCount={}",
                lightEntries.size(), getGameCount(), getActiveCount());
    }

    @Override
    public void updateEntry(GameIndexEntry entry) {
        fullEntryCache.put(entry.getId(), entry);
        for (int i = 0; i < lightEntries.size(); i++) {
            if (lightEntries.get(i).id() == entry.getId()) {
                lightEntries.set(i, LightGameEntry.fromFull(entry));
                break;
            }
        }
        setGameCount(lightEntries.size());
        setActiveCount((int) lightEntries.stream().filter(e -> !e.deleted()).count());
    }

    @Override
    public int getGameCount() {
        return lightEntries.size();
    }

    @Override
    public int getActiveCount() {
        return (int) lightEntries.stream().filter(e -> !e.deleted()).count();
    }

    @Override
    public List<GameIndexEntry> getDeletedEntries() {
        if (!allFullEntriesLoaded) {
            loadAllFullEntries(null);
        }
        return fullEntryCache.values().stream()
                .filter(GameIndexEntry::isDeleted)
                .collect(Collectors.toList());
    }

    @Override
    public double getGrowthRatio() {
        if (getActiveCount() == 0) return 1.0;

        long activeSize = lightEntries.stream()
                .filter(e -> !e.deleted())
                .mapToLong(LightGameEntry::length)
                .sum();

        long totalSize = lightEntries.stream()
                .mapToLong(LightGameEntry::length)
                .sum();

        if (activeSize == 0) return 1.0;
        return (double) totalSize / activeSize;
    }

    @Override
    public boolean needsRepack() {
        if (getActiveCount() == 0) return false;

        long activeSize = lightEntries.stream()
                .filter(e -> !e.deleted())
                .mapToLong(LightGameEntry::length)
                .sum();

        if (activeSize == 0) return false;

        long totalSize = lightEntries.stream()
                .mapToLong(LightGameEntry::length)
                .sum();

        return (double) totalSize / activeSize > 2.0;
    }

    @Override
    public void refreshCache() {
        // Для LazyPgnIndex кэш обновляется автоматически
        // Но пересчитываем счётчики
        setGameCount(lightEntries.size());
        setActiveCount((int) lightEntries.stream().filter(e -> !e.deleted()).count());
    }
}