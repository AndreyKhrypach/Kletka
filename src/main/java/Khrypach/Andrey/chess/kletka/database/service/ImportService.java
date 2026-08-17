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

package Khrypach.Andrey.chess.kletka.database.service;

import Khrypach.Andrey.chess.kletka.database.exception.PgnException;
import Khrypach.Andrey.chess.kletka.database.exception.PgnParseException;
import Khrypach.Andrey.chess.kletka.database.model.GameData;
import Khrypach.Andrey.chess.kletka.database.parser.PgnParser;
import Khrypach.Andrey.chess.kletka.database.repository.GameRepository;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Сервис для импорта PGN файлов
 */
public class ImportService {

    private static final Logger log = LoggerFactory.getLogger(ImportService.class);
    private static final String PGN_EXTENSION = ".pgn";
    private static final String PGN_EXTENSION_UPPER = ".PGN";

    private final LanguageManager lang = LanguageManager.getInstance();

    private final GameRepository repository;
    private final PgnParser parser;

    public ImportService(GameRepository repository) {
        this.repository = repository;
        this.parser = new PgnParser();
    }

    /**
     * Импортирует партии из PGN файла
     */
    public List<GameData> importFromFile(File file) throws PgnException {
        if (file == null || !file.exists()) {
            throw new PgnException(String.format(lang.get(LanguageKeys.IMPORT_ERROR_FILE_NOT_FOUND), file != null ? file.getPath() : "null"));
        }

        try {
            String content = Files.readString(file.toPath());
            List<GameData> games = parser.parseMultiple(content);

            log.info("Imported {} games from: {}", games.size(), file.getName());
            return games;
        } catch (IOException e) {
            throw new PgnException(String.format(lang.get(LanguageKeys.IMPORT_ERROR_READ_FILE), file.getName()), e);
        } catch (PgnParseException e) {
            throw new PgnParseException(String.format(lang.get(LanguageKeys.IMPORT_ERROR_PARSE_PGN), file.getName()), e);
        }
    }

    /**
     * Импортирует партии из PGN строки
     */
    public List<GameData> importFromString(String pgnContent) throws PgnException {
        if (pgnContent == null || pgnContent.trim().isEmpty()) {
            throw new PgnException(lang.get(LanguageKeys.IMPORT_ERROR_PGN_EMPTY));
        }

        try {
            List<GameData> games = parser.parseMultiple(pgnContent);
            log.info("Imported {} games from string", games.size());
            return games;
        } catch (PgnParseException e) {
            throw new PgnParseException(lang.get(LanguageKeys.IMPORT_ERROR_PARSE_GENERAL), e);
        }
    }

    /**
     * Импортирует партии из директории
     */
    public List<GameData> importFromDirectory(File directory) throws PgnException {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            throw new PgnException(String.format(lang.get(LanguageKeys.IMPORT_ERROR_DIR_NOT_FOUND),
                    (directory != null ? directory.getPath() : "null")));
        }

        List<GameData> allGames = new ArrayList<>();
        List<File> pgnFiles = findPgnFiles(directory);

        for (File file : pgnFiles) {
            try {
                List<GameData> games = importFromFile(file);
                allGames.addAll(games);
            } catch (PgnException e) {
                log.warn("Failed to import from: {}, skipping", file.getName(), e);
            }
        }

        log.info("Imported {} games from {} files in directory: {}",
                allGames.size(), pgnFiles.size(), directory.getPath());
        return allGames;
    }

    /**
     * Импортирует и автоматически сохраняет в репозиторий
     */
    public List<GameData> importAndSave(File file) throws PgnException {
        List<GameData> games = importFromFile(file);
        if (!games.isEmpty()) {
            repository.saveAll(games);
            log.debug("Saved {} imported games", games.size());
        }
        return games;
    }

    /**
     * Импортирует из директории и автоматически сохраняет в репозиторий
     */
    public List<GameData> importAndSave(File directory, boolean recursive) throws PgnException {
        List<GameData> games = new ArrayList<>();

        if (recursive) {
            games.addAll(importFromDirectoryRecursive(directory));
        } else {
            games.addAll(importFromDirectory(directory));
        }

        if (!games.isEmpty()) {
            repository.saveAll(games);
            log.info("Saved {} imported games", games.size());
        }

        return games;
    }

    /**
     * Рекурсивно импортирует из директории
     */
    public List<GameData> importFromDirectoryRecursive(File directory) throws PgnException {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            throw new PgnException(lang.get(LanguageKeys.IMPORT_ERROR_DIR_NOT_FOUND_SIMPLE));
        }

        List<GameData> allGames = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(directory.toPath())) {
            List<Path> pgnPaths = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(PGN_EXTENSION) ||
                            p.toString().endsWith(PGN_EXTENSION_UPPER))
                    .toList();

            for (Path path : pgnPaths) {
                try {
                    List<GameData> games = importFromFile(path.toFile());
                    allGames.addAll(games);
                } catch (PgnException e) {
                    log.warn("Failed to import from: {}, skipping", path, e);
                }
            }
        } catch (IOException e) {
            throw new PgnException(String.format(lang.get(LanguageKeys.IMPORT_ERROR_READ_DIR),  directory.getPath()), e);
        }

        return allGames;
    }

    /**
     * Находит все PGN файлы в директории
     */
    private List<File> findPgnFiles(File directory) {
        File[] files = directory.listFiles((dir, name) ->
                name.endsWith(PGN_EXTENSION) || name.endsWith(PGN_EXTENSION_UPPER));
        return files != null ? List.of(files) : List.of();
    }
}