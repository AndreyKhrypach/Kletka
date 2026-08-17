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
import Khrypach.Andrey.chess.kletka.database.model.GameData;
import Khrypach.Andrey.chess.kletka.database.formatter.PgnFormatter;
import Khrypach.Andrey.chess.kletka.database.repository.GameRepository;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Consumer;

/**
 * Сервис для экспорта PGN файлов
 */
public class ExportService {

    private static final Logger log = LoggerFactory.getLogger(ExportService.class);

    private final LanguageManager lang = LanguageManager.getInstance();

    private final GameRepository repository;
    private final PgnFormatter formatter;

    public ExportService(GameRepository repository) {
        this.repository = repository;
        this.formatter = new PgnFormatter();
    }

    /**
     * Экспортирует список партий в PGN файл
     */
    public void exportToFile(List<GameData> games, File file) throws PgnException {
        if (games == null || games.isEmpty()) {
            throw new PgnException(lang.get(LanguageKeys.EXPORT_ERROR_GAMES_EMPTY));
        }

        if (file == null) {
            throw new PgnException(lang.get(LanguageKeys.EXPORT_ERROR_FILE_NULL));
        }

        try {
            StringBuilder sb = new StringBuilder();
            for (GameData game : games) {
                sb.append(formatter.format(game));
                sb.append("\n\n");
            }

            Files.writeString(file.toPath(), sb.toString());
            log.debug("Exported {} games to: {}", games.size(), file.getName());
        } catch (IOException e) {
            throw new PgnException(String.format(lang.get(LanguageKeys.EXPORT_ERROR_EXPORT_FAILED), file.getName()), e);
        }
    }

    /**
     * Экспортирует все партии из репозитория в файл
     */
    public void exportAllToFile(File file) throws PgnException {
        List<GameData> games = repository.findAll();
        if (games.isEmpty()) {
            throw new PgnException(lang.get(LanguageKeys.EXPORT_ERROR_NO_GAMES));
        }
        exportToFile(games, file);
    }

    /**
     * Экспортирует партии с прогрессом
     */
    public void exportWithProgress(List<GameData> games, File file, Consumer<Integer> progressCallback)
            throws PgnException {
        if (games == null || games.isEmpty()) {
            throw new PgnException(lang.get(LanguageKeys.EXPORT_ERROR_GAMES_EMPTY));
        }

        try {
            StringBuilder sb = new StringBuilder();
            int total = games.size();

            for (int i = 0; i < total; i++) {
                GameData game = games.get(i);
                sb.append(formatter.format(game));
                sb.append("\n\n");

                if (progressCallback != null && (i + 1) % 10 == 0) {
                    progressCallback.accept((int) ((i + 1) * 100.0 / total));
                }
            }

            Files.writeString(file.toPath(), sb.toString());

            if (progressCallback != null) {
                progressCallback.accept(100);
            }

            log.info("Exported {} games to: {}", total, file.getName());
        } catch (IOException e) {
            throw new PgnException(String.format(lang.get(LanguageKeys.EXPORT_ERROR_EXPORT_FAILED), file.getName()), e);
        }
    }

    /**
     * Экспортирует партии в несколько файлов (по количеству игр в файле)
     */
    public void exportToMultipleFiles(List<GameData> games, File directory, int gamesPerFile)
            throws PgnException {
        if (games == null || games.isEmpty()) {
            throw new PgnException(lang.get(LanguageKeys.EXPORT_ERROR_GAMES_EMPTY));
        }

        if (!directory.exists() && !directory.mkdirs()) {
            throw new PgnException(String.format(lang.get(LanguageKeys.EXPORT_ERROR_CREATE_DIR), directory.getPath()));
        }

        int totalFiles = (int) Math.ceil((double) games.size() / gamesPerFile);

        for (int i = 0; i < totalFiles; i++) {
            int start = i * gamesPerFile;
            int end = Math.min(start + gamesPerFile, games.size());
            List<GameData> batch = games.subList(start, end);

            String fileName = String.format("games_%d_of_%d.pgn", i + 1, totalFiles);
            File file = new File(directory, fileName);
            exportToFile(batch, file);
        }

        log.info("Exported {} games to {} files in: {}", games.size(), totalFiles, directory.getPath());
    }

    /**
     * Экспортирует одну партию в PGN строку
     */
    public String exportToString(GameData game) throws PgnException {
        if (game == null) {
            throw new PgnException(lang.get(LanguageKeys.EXPORT_ERROR_GAME_NULL));
        }
        return formatter.format(game);
    }

    /**
     * Экспортирует список партий в PGN строку
     */
    public String exportToString(List<GameData> games) throws PgnException {
        if (games == null || games.isEmpty()) {
            throw new PgnException(lang.get(LanguageKeys.EXPORT_ERROR_GAMES_EMPTY));
        }

        StringBuilder sb = new StringBuilder();
        for (GameData game : games) {
            sb.append(formatter.format(game));
            sb.append("\n\n");
        }
        return sb.toString();
    }
}