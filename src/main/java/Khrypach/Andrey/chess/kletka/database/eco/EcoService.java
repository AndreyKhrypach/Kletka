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

package Khrypach.Andrey.chess.kletka.database.eco;

import Khrypach.Andrey.chess.kletka.gui.model.ParentNode;
import Khrypach.Andrey.chess.kletka.gui.model.RootNode;
import Khrypach.Andrey.chess.kletka.gui.model.Variation;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class EcoService {

    private static final Logger log = LoggerFactory.getLogger(EcoService.class);
    private static final String[] ECO_FILES = {"a.tsv", "b.tsv", "c.tsv", "d.tsv", "e.tsv"};

    private static EcoService instance;

    // ========== НОВЫЙ ИНДЕКС: Set строк -> Запись о дебюте ==========
    private final Map<Set<String>, EcoEntry> moveSetIndex = new HashMap<>();

    // Старый индекс для обратной совместимости
    private final Map<String, EcoEntry> pgnIndex = new HashMap<>();

    @Getter
    private boolean initialized = false;

    private EcoService() {
        loadEcoDatabase();
    }

    public static synchronized EcoService getInstance() {
        if (instance == null) {
            instance = new EcoService();
        }
        return instance;
    }

    private void loadEcoDatabase() {
        int totalEntries = 0;
        for (String fileName : ECO_FILES) {
            int count = loadEcoFile(fileName);
            totalEntries += count;
        }
        initialized = true;

        if (totalEntries > 0) {
            log.debug("Loaded {} entries", totalEntries);
        } else {
            log.error("WARNING: No entries loaded!");
        }
    }

    private int loadEcoFile(String fileName) {
        int count = 0;
        String[] pathsToTry = {
                "/eco/" + fileName,
                fileName,
                "eco/" + fileName,
                "/" + fileName
        };

        for (String path : pathsToTry) {
            try (InputStream inputStream = getClass().getResourceAsStream(path)) {
                if (inputStream != null) {
                    count = loadFromStream(inputStream, fileName);
                    break;
                }
            } catch (IOException e) {
                log.error("Error loading by path = {} : {}", path, e.getMessage());
            }
        }
        return count;
    }

    private int loadFromStream(InputStream inputStream, String fileName) {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();

                if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("eco")) {
                    continue;
                }

                try {
                    EcoEntry entry = parseTsvLine(trimmed, lineNumber);
                    if (entry != null && entry.pgn() != null && !entry.pgn().isEmpty()) {
                        String normalizedPgn = normalizePgn(entry.pgn());

                        if (!pgnIndex.containsKey(normalizedPgn)) {
                            pgnIndex.put(normalizedPgn, entry);
                        }

                        Set<String> moveSet = extractMoveSet(normalizedPgn);
                        if (!moveSet.isEmpty() && !containsMoveSet(moveSet)) {
                            moveSetIndex.put(moveSet, entry);
                            count++;
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to parse line {} : {}", lineNumber, e.getMessage());
                }
            }

        } catch (IOException e) {
            log.error("IO error reading {}: {}", fileName, e.getMessage());
        }
        return count;
    }

    /**
     * Парсит строку TSV формата:
     * eco	name	pgn
     */
    private EcoEntry parseTsvLine(String line, int lineNumber) {
        String[] columns = line.split("\t");

        if (columns.length < 3) {
            log.warn("Line {} has {} columns, expected at least 3: {}",
                    lineNumber, columns.length, line);
            return null;
        }

        String eco = columns[0].trim();
        String name = columns[1].trim();
        String pgn = columns[2].trim();

        if (eco.isEmpty() || name.isEmpty() || pgn.isEmpty()) {
            return null;
        }

        return new EcoEntry(eco, name, pgn, "", "");
    }

    /**
     * Извлекает Set ходов из PGN строки
     * Убирает номера ходов и аннотации
     */
    private Set<String> extractMoveSet(String pgn) {
        Set<String> moveSet = new HashSet<>();
        if (pgn == null || pgn.isEmpty()) {
            return moveSet;
        }

        String clean = pgn.replaceAll("\\d+\\.", "")
                .replaceAll("[!?+×#]", "")
                .trim();

        String[] moves = clean.split("\\s+");
        for (String move : moves) {
            String trimmed = move.trim();
            if (!trimmed.isEmpty()) {
                moveSet.add(trimmed);
            }
        }

        return moveSet;
    }

    /**
     * Проверяет, существует ли уже такой Set в индексе
     */
    private boolean containsMoveSet(Set<String> moveSet) {
        for (Set<String> existing : moveSetIndex.keySet()) {
            if (existing.equals(moveSet)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Нормализует PGN строку для использования в качестве ключа
     */
    private String normalizePgn(String pgn) {
        if (pgn == null) return "";
        return pgn.replaceAll("\\s+", " ")
                .replace("+", "")
                .replace("#", "")
                .replace("*", "")
                .trim();
    }

    /**
     * Строит полную PGN строку от корня до конца главной линии
     */
    public String buildFullPgnFromTree(RootNode rootNode, Variation mainLine) {
        if (rootNode == null || mainLine == null) {
            return null;
        }

        List<ParentNode> allMoves = new ArrayList<>();
        ParentNode current = rootNode.getNext();

        while (current != null && !current.isRoot()) {
            allMoves.add(current);
            current = current.getNext();
        }

        if (allMoves.isEmpty()) {
            return null;
        }

        StringBuilder pgn = new StringBuilder();
        for (int i = 0; i < allMoves.size(); i++) {
            ParentNode node = allMoves.get(i);
            if (node.isRoot()) continue;

            if (i % 2 == 0) {
                pgn.append((i / 2 + 1)).append(". ");
            }

            String san = node.getSan();
            san = san.replace("#", "").replace("+", "");
            pgn.append(san).append(" ");
        }

        return pgn.toString().trim();
    }

    /**
     * Извлекает Set ходов из дерева
     */
    private Set<String> extractMoveSetFromTree(RootNode rootNode, Variation mainLine) {
        Set<String> moveSet = new HashSet<>();
        if (rootNode == null || mainLine == null) {
            return moveSet;
        }

        ParentNode current = rootNode.getNext();
        while (current != null && !current.isRoot()) {
            String san = current.getSan();
            if (san != null && !san.isEmpty()) {
                san = san.replace("#", "").replace("+", "");
                moveSet.add(san);
            }
            current = current.getNext();
        }

        return moveSet;
    }

    /**
     * Ищет дебют по дереву
     * Использует Set ходов для поиска
     */
    public EcoEntry findOpeningByPgn(RootNode rootNode, Variation mainLine) {
        if (!initialized || rootNode == null || mainLine == null) {
            return null;
        }

        Set<String> ourMoveSet = extractMoveSetFromTree(rootNode, mainLine);
        if (ourMoveSet.isEmpty()) {
            return null;
        }

        EcoEntry entry = findEntryByMoveSet(ourMoveSet);
        if (entry != null) {
            log.trace("Found by move set: {} - {}", entry.eco(), entry.name());
            return entry;
        }

        String fullPgn = buildFullPgnFromTree(rootNode, mainLine);
        if (fullPgn == null || fullPgn.isEmpty()) {
            return null;
        }

        String normalizedPgn = normalizePgn(fullPgn);
        String[] parts = normalizedPgn.split(" ");
        int MAX_ELEMENTS = 54;
        int actualLength = Math.min(parts.length, MAX_ELEMENTS);

        List<String> elements = new ArrayList<>(Arrays.asList(parts).subList(0, actualLength));

        while (!elements.isEmpty()) {
            String testPgn = String.join(" ", elements);
            testPgn = normalizePgn(testPgn);

            entry = pgnIndex.get(testPgn);
            if (entry != null) {
                log.trace("Found by PGN: {} - {}", entry.eco(), entry.name());
                return entry;
            }

            elements.remove(elements.size() - 1);
        }

        return null;
    }

    /**
     * Ищет запись по Set ходов
     */
    private EcoEntry findEntryByMoveSet(Set<String> moveSet) {
        if (moveSet == null || moveSet.isEmpty()) {
            return null;
        }

        for (Map.Entry<Set<String>, EcoEntry> entry : moveSetIndex.entrySet()) {
            if (entry.getKey().equals(moveSet)) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Получает все записи (для отладки)
     */
    public Collection<EcoEntry> getEntries() {
        return moveSetIndex.values();
    }
}