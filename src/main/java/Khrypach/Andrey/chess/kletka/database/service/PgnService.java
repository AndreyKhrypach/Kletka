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
import Khrypach.Andrey.chess.kletka.database.repository.GameRepository;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Главный сервис для работы с PGN
 * Инкапсулирует работу с репозиторием и предоставляет API для UI
 */
public class PgnService {

    private static final Logger log = LoggerFactory.getLogger(PgnService.class);

    private final LanguageManager lang = LanguageManager.getInstance();

    @Getter
    private final GameRepository repository;
    private final ImportService importService;
    private final ExportService exportService;

    public PgnService(GameRepository repository) {
        this.repository = repository;
        this.importService = new ImportService(repository);
        this.exportService = new ExportService(repository);
    }

    // === Базовые операции ===

    /**
     * Сохраняет партию
     */
    public void saveGame(GameData game) throws PgnException {
        if (game == null) {
            throw new PgnException(lang.get(LanguageKeys.PGN_SERVICE_ERROR_GAME_NULL));
        }
        repository.save(game);
        log.debug("Game saved: {} vs {}", game.whitePlayer(), game.blackPlayer());
    }

    /**
     * Сохраняет список партий
     *
     * @deprecated Будет использоваться в версии 2.0 (SQLite).
     * Тесты будут добавлены при реализации SQLite.
     */
    @Deprecated
    public void saveGames(List<GameData> games) throws PgnException {
        if (games == null || games.isEmpty()) {
            return;
        }
        repository.saveAll(games);
        log.debug("Saved {} games", games.size());
    }

    /**
     * Загружает партию по ID
     *
     * @deprecated Будет использоваться в версии 2.0 (SQLite).
     * Тесты будут добавлены при реализации SQLite.
     */
    @Deprecated
    public Optional<GameData> loadGame(String id) throws PgnException {
        return repository.findById(id);
    }

    /**
     * Загружает все партии
     */
    public List<GameData> loadAllGames() throws PgnException {
        return repository.findAll();
    }

    /**
     * Удаляет партию
     *
     * @deprecated Будет использоваться в версии 2.0 (SQLite).
     * Тесты будут добавлены при реализации SQLite.
     */
    @Deprecated
    public void deleteGame(String id) throws PgnException {
        repository.delete(id);
        log.debug("Game deleted: {}", id);
    }

    /**
     * Удаляет все партии
     *
     * @deprecated Будет использоваться в версии 2.0 (SQLite).
     * Тесты будут добавлены при реализации SQLite.
     */
    @Deprecated
    public void deleteAllGames() throws PgnException {
        repository.deleteAll();
        log.debug("All games deleted");
    }

    /**
     * Возвращает количество партий
     */
    public long getGameCount() throws PgnException {
        return repository.count();
    }

    // === Поиск ===

    /**
     * Поиск по игрокам
     */
    public List<GameData> searchByPlayers(String white, String black) throws PgnException {
        return repository.searchByPlayers(white, black);
    }

    /**
     * Поиск по результату
     */
    public List<GameData> searchByResult(String result) throws PgnException {
        return repository.searchByResult(result);
    }

    /**
     * Поиск по ECO коду
     */
    public List<GameData> searchByEco(String eco) throws PgnException {
        return repository.searchByEco(eco);
    }

    /**
     * Поиск по названию дебюта
     */
    public List<GameData> searchByOpening(String opening) throws PgnException {
        return repository.searchByOpening(opening);
    }

    /**
     * Поиск по тексту
     *
     * @deprecated Будет использоваться в версии 2.0 (SQLite).
     * Тесты будут добавлены при реализации SQLite.
     */
    @Deprecated
    public List<GameData> searchByText(String query) throws PgnException {
        return repository.searchByText(query);
    }

    // === Импорт/Экспорт ===

    /**
     * Импортирует партии из PGN файла
     *
     * @deprecated Будет использоваться в версии 2.0 (SQLite).
     * Тесты будут добавлены при реализации SQLite.
     */
    @Deprecated
    public List<GameData> importFromPgnFile(File file) throws PgnException {
        return importService.importFromFile(file);
    }

    /**
     * Импортирует партии из PGN строки
     */
    public List<GameData> importFromPgnString(String pgnContent) throws PgnException {
        return importService.importFromString(pgnContent);
    }

    /**
     * Импортирует партии из директории
     *
     * @deprecated Будет использоваться в версии 2.0 (SQLite).
     * Тесты будут добавлены при реализации SQLite.
     */
    @Deprecated
    public List<GameData> importFromDirectory(File directory) throws PgnException {
        return importService.importFromDirectory(directory);
    }

    /**
     * Экспортирует партии в PGN файл
     *
     * @deprecated Будет использоваться в версии 2.0 (SQLite).
     * Тесты будут добавлены при реализации SQLite.
     */
    @Deprecated
    public void exportToPgnFile(List<GameData> games, File file) throws PgnException {
        exportService.exportToFile(games, file);
    }

    /**
     * Экспортирует одну партию в PGN файл
     *
     * @deprecated Будет использоваться в версии 2.0 (SQLite).
     * Тесты будут добавлены при реализации SQLite.
     */
    @Deprecated
    public void exportToPgnFile(GameData game, File file) throws PgnException {
        exportService.exportToFile(List.of(game), file);
    }

    /**
     * Экспортирует партии с прогрессом
     *
     * @deprecated Будет использоваться в версии 2.0 (SQLite).
     * Тесты будут добавлены при реализации SQLite.
     */
    @Deprecated
    public void exportWithProgress(List<GameData> games, File file, Consumer<Integer> progressCallback)
            throws PgnException {
        exportService.exportWithProgress(games, file, progressCallback);
    }

    // === Дополнительно ===

    /**
     * Проверяет существование партии
     *
     * @deprecated Будет использоваться в версии 2.0 (SQLite).
     * Тесты будут добавлены при реализации SQLite.
     */
    @Deprecated
    public boolean gameExists(String id) throws PgnException {
        return repository.exists(id);
    }

    /**
     * Экспортирует партию в PGN строку
     *
     * @deprecated Будет использоваться в версии 2.0 (SQLite).
     * Тесты будут добавлены при реализации SQLite.
     */
    @Deprecated
    public String exportToString(GameData game) throws PgnException {
        if (game == null) {
            throw new PgnException(lang.get(LanguageKeys.PGN_SERVICE_ERROR_GAME_NULL));
        }
        return exportService.exportToString(game);
    }

    /**
     * Экспортирует список партий в PGN строку
     *
     * @deprecated Будет использоваться в версии 2.0 (SQLite).
     * Тесты будут добавлены при реализации SQLite.
     */
    @Deprecated
    public String exportToString(List<GameData> games) throws PgnException {
        if (games == null || games.isEmpty()) {
            throw new PgnException(lang.get(LanguageKeys.PGN_SERVICE_ERROR_GAMES_EMPTY));
        }
        return exportService.exportToString(games);
    }

}