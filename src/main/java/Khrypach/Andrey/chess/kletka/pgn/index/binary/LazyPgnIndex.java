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
import java.util.HashMap;
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

    /** O(1) поиск LightGameEntry по id */
    private final Map<Integer, LightGameEntry> lightById;

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

        // Строим map один раз
        this.lightById = new HashMap<>(lightEntries.size() * 2);
        for (LightGameEntry e : lightEntries) {
            lightById.put(e.id(), e);
        }

        // Устанавливаем метаданные
        setVersion(PgnIndex.FORMAT_VERSION);
        setFileHash(header.fileHash());
        setFileSize(header.fileSize());
        setGameCount(header.entryCount());
        setActiveCount(header.activeCount());
    }

    // ========== ЛЁГКИЕ ЗАПИСИ (ДЛЯ ПОИСКА И ОТОБРАЖЕНИЯ) ==========

    public List<LightGameEntry> getLightEntriesPage(int start, int count) {
        if (start < 0) start = 0;
        if (start >= lightEntries.size()) return new ArrayList<>();

        int end = Math.min(start + count, lightEntries.size());
        return lightEntries.subList(start, end);
    }

    public List<LightGameEntry> getFirstPage() {
        return getLightEntriesPage(0, 50);
    }

    public List<LightGameEntry> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return lightEntries;
        }

        String lowerQuery = query.toLowerCase().trim();
        return lightEntries.stream()
                .filter(entry -> entry.matches(lowerQuery))
                .collect(Collectors.toList());
    }

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

    public List<GameIndexEntry> loadFullEntries(int start, int count, Consumer<Integer> progressCallback) {
        if (start < 0) start = 0;
        if (start >= lightEntries.size()) return new ArrayList<>();

        int end = Math.min(start + count, lightEntries.size());
        List<GameIndexEntry> result = new ArrayList<>(end - start);

        for (int i = start; i < end; i++) {
            LightGameEntry light = lightEntries.get(i);

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

    public List<GameIndexEntry> loadInitialFullEntries(int count) {
        return loadFullEntries(0, count, null);
    }

    // ========== ПОЛНАЯ ЗАПИСЬ ПО ID (ЛЕНИВО) ==========

    /**
     * Получает полную запись по ID (лениво).
     * O(1) поиск LightGameEntry через map.
     */
    public GameIndexEntry getFullEntry(int id) {
        // Проверяем кэш
        GameIndexEntry cached = fullEntryCache.get(id);
        if (cached != null) {
            return cached;
        }

        // O(1) поиск лёгкой записи
        LightGameEntry light = lightById.get(id);
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

    @Override
    public GameIndexEntry getEntryById(int id) {
        return getFullEntry(id);
    }

    @Override
    public GameIndexEntry getActiveEntryById(int id) {
        LightGameEntry light = lightById.get(id);
        if (light == null || light.deleted()) {
            return null;
        }

        GameIndexEntry cached = fullEntryCache.get(id);
        if (cached != null && !cached.isDeleted()) {
            return cached;
        }

        try {
            return loadFullEntry(light);
        } catch (IOException e) {
            log.error("Failed to load full entry for {}", id, e);
            return null;
        }
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private GameIndexEntry loadFullEntry(LightGameEntry light) throws IOException {
        long offset = dataStart + light.offset();

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

    private String readString() {
        short length = buffer.getShort();
        if (length == 0) return "";

        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    public int getEntryCount() {
        return lightEntries.size();
    }

    public void close() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (IOException e) {
            log.warn("Error closing channel: {}", e.getMessage());
        }
    }

    // ========== ПЕРЕОПРЕДЕЛЁННЫЕ МЕТОДЫ ==========

    @Override
    public List<GameIndexEntry> getEntries() {
        if (!allFullEntriesLoaded) {
            loadAllFullEntries(null);
        }
        return new ArrayList<>(fullEntryCache.values());
    }

    @Override
    public List<GameIndexEntry> getActiveEntries() {
        if (!allFullEntriesLoaded) {
            loadAllFullEntries(null);
        }
        return fullEntryCache.values().stream()
                .filter(e -> !e.isDeleted())
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasDeletedGames(PgnIndex index) {
        if (index == null) return false;
        return lightEntries.stream().anyMatch(LightGameEntry::deleted);
    }

    public int getDeletedCount() {
        return (int) lightEntries.stream().filter(LightGameEntry::deleted).count();
    }

    public List<LightGameEntry> getActiveLightEntries() {
        return lightEntries.stream()
                .filter(entry -> !entry.deleted())
                .collect(Collectors.toList());
    }

    public LightGameEntry getActiveLightEntry(int id) {
        LightGameEntry e = lightById.get(id);
        return (e != null && !e.deleted()) ? e : null;
    }

    public int getActiveLightCount() {
        return (int) lightEntries.stream()
                .filter(entry -> !entry.deleted())
                .count();
    }

    @Override
    public int getNextId() {
        int maxId = lightEntries.stream()
                .mapToInt(LightGameEntry::id)
                .max()
                .orElse(0);
        return maxId + 1;
    }

    @Override
    public int getMaxId() {
        return lightEntries.stream()
                .mapToInt(LightGameEntry::id)
                .max()
                .orElse(0);
    }

    @Override
    public void addEntry(GameIndexEntry entry) {
        log.info("LazyPgnIndex.addEntry() called for ID: {}", entry.getId());

        fullEntryCache.put(entry.getId(), entry);

        LightGameEntry light = LightGameEntry.fromFull(entry);
        lightEntries.add(light);
        lightById.put(light.id(), light);

        setGameCount(lightEntries.size());
        if (!light.deleted()) {
            setActiveCount(getActiveCount() + 1);
        }
    }

    @Override
    public void updateEntry(GameIndexEntry entry) {
        fullEntryCache.put(entry.getId(), entry);

        LightGameEntry newLight = LightGameEntry.fromFull(entry);
        for (int i = 0; i < lightEntries.size(); i++) {
            if (lightEntries.get(i).id() == entry.getId()) {
                LightGameEntry old = lightEntries.get(i);
                if (old.deleted() != newLight.deleted()) {
                    setActiveCount(getActiveCount() + (newLight.deleted() ? -1 : 1));
                }
                lightEntries.set(i, newLight);
                lightById.put(newLight.id(), newLight);
                break;
            }
        }
    }

    @Override
    public int getGameCount() {
        return lightEntries.size();
    }

    @Override
    public int getActiveCount() {
        return super.getActiveCount();
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
        setGameCount(lightEntries.size());
        int active = (int) lightEntries.stream().filter(e -> !e.deleted()).count();
        setActiveCount(active);
    }
}