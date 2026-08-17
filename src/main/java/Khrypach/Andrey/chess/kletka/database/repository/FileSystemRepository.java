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

package Khrypach.Andrey.chess.kletka.database.repository;

import Khrypach.Andrey.chess.kletka.database.exception.PgnException;
import Khrypach.Andrey.chess.kletka.database.exception.PgnNotFoundException;
import Khrypach.Andrey.chess.kletka.database.exception.PgnParseException;
import Khrypach.Andrey.chess.kletka.database.model.GameData;
import Khrypach.Andrey.chess.kletka.database.parser.PgnParser;
import Khrypach.Andrey.chess.kletka.database.formatter.PgnFormatter;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Реализация репозитория для работы с PGN файлами (версия 1.0)
 */
public class FileSystemRepository implements GameRepository {

    private static final Logger log = LoggerFactory.getLogger(FileSystemRepository.class);
    private static final String PGN_EXTENSION = ".pgn";
    private static final String PGN_EXTENSION_UPPER = ".PGN";

    private final LanguageManager lang = LanguageManager.getInstance();

    private final Path gamesDirectory;
    private final PgnParser parser;
    private final PgnFormatter formatter;

    public FileSystemRepository(String directoryPath) throws PgnException {
        this.gamesDirectory = Paths.get(directoryPath);
        this.parser = new PgnParser();
        this.formatter = new PgnFormatter();
        initializeDirectory();
    }

    public FileSystemRepository(Path directoryPath) throws PgnException {
        this.gamesDirectory = directoryPath;
        this.parser = new PgnParser();
        this.formatter = new PgnFormatter();
        initializeDirectory();
    }

    private void initializeDirectory() throws PgnException {
        try {
            if (!Files.exists(gamesDirectory)) {
                Files.createDirectories(gamesDirectory);
                log.info("Created games directory: {}", gamesDirectory);
            }
        } catch (IOException e) {
            throw new PgnException(String.format(lang.get(LanguageKeys.REPO_ERROR_CREATE_DIR), gamesDirectory), e);
        }
    }

    // === Базовые CRUD операции ===

    @Override
    public void save(GameData game) throws PgnException {
        if (game == null) {
            throw new PgnException(lang.get(LanguageKeys.REPO_ERROR_GAME_NULL));
        }

        String fileName = generateFileName(game);
        Path filePath = gamesDirectory.resolve(fileName);

        try {
            String pgnContent = formatter.format(game);
            // ========== ИСПОЛЬЗУЕМ UTF-8 ==========
            Files.writeString(filePath, pgnContent);
            log.info("Saved game to: {}", filePath);
        } catch (IOException e) {
            throw new PgnException(String.format(lang.get(LanguageKeys.REPO_ERROR_SAVE_GAME), filePath), e);
        }
    }

    @Override
    public void saveAll(List<GameData> games) throws PgnException {
        if (games == null || games.isEmpty()) {
            return;
        }

        for (GameData game : games) {
            save(game);
        }
        log.info("Saved {} games", games.size());
    }

    @Override
    public Optional<GameData> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }

        // Ищем файл по ID (без расширения)
        String fileName = id.endsWith(PGN_EXTENSION) ? id : id + PGN_EXTENSION;
        Path filePath = gamesDirectory.resolve(fileName);

        if (!Files.exists(filePath)) {
            // Пробуем с другим расширением
            if (id.endsWith(PGN_EXTENSION_UPPER)) {
                filePath = gamesDirectory.resolve(id.toLowerCase());
            } else {
                filePath = gamesDirectory.resolve(id + PGN_EXTENSION_UPPER);
            }

            if (!Files.exists(filePath)) {
                return Optional.empty();
            }
        }

        try {
            GameData game = loadFromFile(filePath);
            return Optional.of(game);
        } catch (PgnException e) {
            log.error("Error loading game: {}", filePath, e);
            return Optional.empty();
        }
    }

    @Override
    public List<GameData> findAll() throws PgnException {
        List<GameData> games = new ArrayList<>();

        try (Stream<Path> paths = Files.list(gamesDirectory)) {
            List<Path> pgnFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(PGN_EXTENSION) ||
                            p.toString().endsWith(PGN_EXTENSION_UPPER))
                    .toList();

            for (Path filePath : pgnFiles) {
                try {
                    GameData game = loadFromFile(filePath);
                    if (game != null) {
                        games.add(game);
                    }
                } catch (PgnException e) {
                    log.warn("Failed to load game from: {}, skipping", filePath, e);
                }
            }
        } catch (IOException e) {
            throw new PgnException(String.format(lang.get(LanguageKeys.REPO_ERROR_READ_DIR), gamesDirectory), e);
        }

        return games;
    }

    @Override
    public void delete(String id) throws PgnException {
        if (id == null || id.trim().isEmpty()) {
            return;
        }

        String fileName = id.endsWith(PGN_EXTENSION) ? id : id + PGN_EXTENSION;
        Path filePath = gamesDirectory.resolve(fileName);

        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Deleted game: {}", filePath);
            }
        } catch (IOException e) {
            throw new PgnException(String.format(lang.get(LanguageKeys.REPO_ERROR_DELETE_GAME), filePath), e);
        }
    }

    @Override
    public void deleteAll() throws PgnException {
        try (Stream<Path> paths = Files.list(gamesDirectory)) {
            List<Path> pgnFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(PGN_EXTENSION) ||
                            p.toString().endsWith(PGN_EXTENSION_UPPER))
                    .toList();

            for (Path filePath : pgnFiles) {
                try {
                    Files.delete(filePath);
                } catch (IOException e) {
                    log.warn("Failed to delete: {}", filePath, e);
                }
            }
            log.info("Deleted {} games", pgnFiles.size());
        } catch (IOException e) {
            throw new PgnException(lang.get(LanguageKeys.REPO_ERROR_DELETE_ALL), e);
        }
    }

    @Override
    public long count() throws PgnException {
        try (Stream<Path> paths = Files.list(gamesDirectory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(PGN_EXTENSION) ||
                            p.toString().endsWith(PGN_EXTENSION_UPPER))
                    .count();
        } catch (IOException e) {
            throw new PgnException(lang.get(LanguageKeys.REPO_ERROR_COUNT), e);
        }
    }

    // === Поиск ===

    @Override
    public List<GameData> searchByPlayers(String white, String black) throws PgnException {
        List<GameData> allGames = findAll();
        return allGames.stream()
                .filter(game -> matchesPlayer(game.whitePlayer(), white) &&
                        matchesPlayer(game.blackPlayer(), black))
                .collect(Collectors.toList());
    }

    @Override
    public List<GameData> searchByResult(String result) throws PgnException {
        if (result == null || result.trim().isEmpty()) {
            return findAll();
        }

        List<GameData> allGames = findAll();
        return allGames.stream()
                .filter(game -> result.equals(game.result()))
                .collect(Collectors.toList());
    }

    @Override
    public List<GameData> searchByEco(String eco) throws PgnException {
        if (eco == null || eco.trim().isEmpty()) {
            return findAll();
        }

        List<GameData> allGames = findAll();
        return allGames.stream()
                .filter(game -> eco.equalsIgnoreCase(game.eco()))
                .collect(Collectors.toList());
    }

    @Override
    public List<GameData> searchByOpening(String opening) throws PgnException {
        if (opening == null || opening.trim().isEmpty()) {
            return findAll();
        }

        List<GameData> allGames = findAll();
        return allGames.stream()
                .filter(game -> game.opening() != null &&
                        game.opening().toLowerCase().contains(opening.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<GameData> searchByDateRange(String startDate, String endDate) throws PgnException {
        List<GameData> allGames = findAll();
        return allGames.stream()
                .filter(game -> {
                    String date = game.date() != null ? game.date().toString() : "";
                    return date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<GameData> searchByText(String query) throws PgnException {
        if (query == null || query.trim().isEmpty()) {
            return findAll();
        }

        String lowerQuery = query.toLowerCase();
        List<GameData> allGames = findAll();

        return allGames.stream()
                .filter(game ->
                        game.whitePlayer().toLowerCase().contains(lowerQuery) ||
                                game.blackPlayer().toLowerCase().contains(lowerQuery) ||
                                (game.event() != null && game.event().toLowerCase().contains(lowerQuery)) ||
                                (game.opening() != null && game.opening().toLowerCase().contains(lowerQuery)) ||
                                (game.pgn() != null && game.pgn().toLowerCase().contains(lowerQuery))
                )
                .collect(Collectors.toList());
    }

    // === Пакетная обработка ===

    @Override
    public void saveBatch(List<GameData> games, int batchSize) throws PgnException {
        if (games == null || games.isEmpty()) {
            return;
        }

        for (int i = 0; i < games.size(); i += batchSize) {
            int end = Math.min(i + batchSize, games.size());
            List<GameData> batch = games.subList(i, end);
            saveAll(batch);
        }
        log.info("Saved {} games in batches of {}", games.size(), batchSize);
    }

    @Override
    public List<GameData> findPaginated(int offset, int limit) throws PgnException {
        List<GameData> allGames = findAll();

        if (offset >= allGames.size()) {
            return Collections.emptyList();
        }

        int end = Math.min(offset + limit, allGames.size());
        return allGames.subList(offset, end);
    }

    // === Дополнительно ===

    @Override
    public boolean exists(String id) throws PgnException {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }

        String fileName = id.endsWith(PGN_EXTENSION) ? id : id + PGN_EXTENSION;
        Path filePath = gamesDirectory.resolve(fileName);
        return Files.exists(filePath);
    }

    @Override
    public List<GameData> importFromFile(File file) throws PgnException {
        if (file == null || !file.exists()) {
            throw new PgnNotFoundException(file != null ? file.getPath() : "null");
        }

        try {
            // ========== ИСПОЛЬЗУЕМ UTF-8 ==========
            String content = Files.readString(file.toPath());
            List<GameData> games = parser.parseMultiple(content);
            log.info("Imported {} games from: {}", games.size(), file.getName());
            return games;
        } catch (IOException e) {
            throw new PgnException(String.format(lang.get(LanguageKeys.REPO_ERROR_READ_FILE),file.getName()), e);
        } catch (PgnParseException e) {
            throw new PgnParseException(String.format(lang.get(LanguageKeys.REPO_ERROR_PARSE_PGN)) + file.getName(), e);
        }
    }

    @Override
    public void exportToFile(List<GameData> games, File file) throws PgnException {
        if (games == null || games.isEmpty()) {
            throw new PgnException(lang.get(LanguageKeys.REPO_ERROR_GAMES_EMPTY));
        }

        try {
            StringBuilder sb = new StringBuilder();
            for (GameData game : games) {
                sb.append(formatter.format(game));
                sb.append("\n\n");
            }

            Files.writeString(file.toPath(), sb.toString());
            log.info("Exported {} games to: {}", games.size(), file.getName());
        } catch (IOException e) {
            throw new PgnException(String.format(lang.get(LanguageKeys.REPO_ERROR_EXPORT),file.getName()), e);
        }
    }

    // === Вспомогательные методы ===

    private GameData loadFromFile(Path filePath) throws PgnException {
        try {
            // ========== ИСПОЛЬЗУЕМ UTF-8 ==========
            String content = Files.readString(filePath);
            return parser.parse(content);
        } catch (IOException e) {
            throw new PgnException("Не удалось прочитать файл: " + filePath, e);
        }
    }

    private String generateFileName(GameData game) {
        String white = game.whitePlayer().replaceAll("[^a-zA-Z0-9]", "_");
        String black = game.blackPlayer().replaceAll("[^a-zA-Z0-9]", "_");
        String date = game.date() != null ? game.date().toString() : "unknown";

        return String.format("%s_%s_%s%s", white, black, date, PGN_EXTENSION);
    }

    private boolean matchesPlayer(String player, String search) {
        if (search == null || search.trim().isEmpty()) {
            return true;
        }
        return player != null && player.toLowerCase().contains(search.toLowerCase());
    }
}