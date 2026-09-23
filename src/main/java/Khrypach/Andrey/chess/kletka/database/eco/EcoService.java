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

    // ========== ИНДЕКС ПО SET ХОДОВ ==========
    // Ключ: Set SAN-ходов (без порядка, без номеров).
    // Значение: запись о дебюте.
    // При конфликте (одинаковый Set от разных записей) — побеждает первая (по порядку загрузки).
    private final Map<Set<String>, EcoEntry> moveSetIndex = new HashMap<>();

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
            log.debug("Loaded {} entries, moveSetIndex size: {}",
                    totalEntries, moveSetIndex.size());
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

                        Set<String> moveSet = extractMoveSet(normalizedPgn);
                        if (!moveSet.isEmpty()) {
                            // Первая запись побеждает при конфликте (устойчивое поведение).
                            // Альтернатива — перезаписывать всегда (тогда победит последняя).
                            moveSetIndex.putIfAbsent(moveSet, entry);
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
     * Парсит строку TSV формата: eco \t name \t pgn
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
     * Извлекает массив ходов из PGN (без номеров ходов и аннотаций).
     */
    private String[] extractMovesArray(String pgn) {
        if (pgn == null || pgn.isEmpty()) {
            return new String[0];
        }
        String clean = pgn.replaceAll("\\d+\\.", "")
                .replaceAll("[!?+×#]", "")
                .trim();
        if (clean.isEmpty()) {
            return new String[0];
        }
        return clean.split("\\s+");
    }

    /**
     * Извлекает Set ходов из PGN строки.
     */
    private Set<String> extractMoveSet(String pgn) {
        return new HashSet<>(Arrays.asList(extractMovesArray(pgn)));
    }

    /**
     * Нормализует PGN строку для использования в качестве ключа.
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
     * Строит список SAN-ходов от корня до конца главной линии.
     */
    public List<String> buildMovesListFromTree(RootNode rootNode, Variation mainLine) {
        List<String> moves = new ArrayList<>();
        if (rootNode == null || mainLine == null) return moves;

        ParentNode current = rootNode.getNext();
        while (current != null && !current.isRoot()) {
            String san = current.getSan();
            if (san != null && !san.isEmpty()) {
                san = san.replace("#", "").replace("+", "");
                moves.add(san);
            }
            current = current.getNext();
        }
        return moves;
    }

    /**
     * Строит список SAN-ходов от корня до указанного узла
     * (по цепочке parent снизу вверх, затем разворачиваем).
     */
    private List<String> buildMovesListForNode(ParentNode targetNode) {
        List<String> moves = new ArrayList<>();
        if (targetNode == null || targetNode.isRoot()) return moves;

        ParentNode current = targetNode;
        while (current != null && !current.isRoot()) {
            String san = current.getSan();
            if (san != null && !san.isEmpty()) {
                san = san.replace("#", "").replace("+", "");
                moves.add(0, san);   // добавляем в начало
            }
            current = current.getParent();
        }
        return moves;
    }

    /**
     * Ищет дебют по дереву вариантов (по главной линии).
     * <p>
     * Алгоритм:
     * <ol>
     *     <li>Строим список ходов от корня до конца главной линии.</li>
     *     <li>Для длины N (от полной до 1) строим Set из первых N ходов
     *         и ищем совпадение в {@link #moveSetIndex}.</li>
     *     <li>Если найдено — возвращаем. Иначе — null.</li>
     * </ol>
     * <p>
     * Это позволяет корректно определять дебюты при транспозициях:
     * порядок ходов не важен, важен только Set.
     */
    public EcoEntry findOpeningByPgn(RootNode rootNode, Variation mainLine) {
        if (!initialized || rootNode == null || mainLine == null) {
            return null;
        }

        List<String> movesList = buildMovesListFromTree(rootNode, mainLine);
        return findOpeningByMovesList(movesList);
    }

    /**
     * Ищет дебют для конкретного узла дерева (в режиме книги).
     * Строит путь от корня до узла и ищет совпадение по префиксам Set.
     */
    public EcoEntry findOpeningForNode(RootNode rootNode, ParentNode targetNode) {
        if (!initialized || rootNode == null || targetNode == null || targetNode.isRoot()) {
            return null;
        }

        List<String> movesList = buildMovesListForNode(targetNode);
        return findOpeningByMovesList(movesList);
    }

    /**
     * Ищет дебют по списку ходов.
     * <p>
     * Начинает с полной длины, убирает последний ход до тех пор,
     * пока не найдёт совпадение в {@link #moveSetIndex} (или список не опустеет).
     * <p>
     * Логика «дебют только уточняется»: если точного совпадения нет —
     * возвращаем null, и вызывающий код оставляет предыдущее значение.
     */
    private EcoEntry findOpeningByMovesList(List<String> movesList) {
        if (movesList == null || movesList.isEmpty()) {
            return null;
        }

        for (int end = movesList.size(); end > 0; end--) {
            Set<String> prefixSet = new HashSet<>(movesList.subList(0, end));
            EcoEntry entry = moveSetIndex.get(prefixSet);
            if (entry != null) {
                log.trace("Found opening by prefix Set ({} moves): {} - {}",
                        end, entry.eco(), entry.name());
                return entry;
            }
        }

        log.trace("No opening found for moves list (length={})", movesList.size());
        return null;
    }

    /**
     * Получает все записи (для отладки).
     */
    public Collection<EcoEntry> getEntries() {
        return new HashSet<>(moveSetIndex.values());
    }
}