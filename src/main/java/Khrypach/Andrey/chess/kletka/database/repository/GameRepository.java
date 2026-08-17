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
import Khrypach.Andrey.chess.kletka.database.model.GameData;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Интерфейс для работы с хранилищем PGN игр
 * Версия 1.0: файловая система
 * Версия 2.0: SQLite
 */
public interface GameRepository {

    // === Базовые CRUD операции ===

    /**
     * Сохраняет партию в хранилище
     */
    void save(GameData game) throws PgnException;

    /**
     * Сохраняет список партий
     */
    void saveAll(List<GameData> games) throws PgnException;

    /**
     * Находит партию по ID (в версии 1.0 - по имени файла)
     */
    Optional<GameData> findById(String id) throws PgnException;

    /**
     * Возвращает все партии
     */
    List<GameData> findAll() throws PgnException;

    /**
     * Удаляет партию по ID
     */
    void delete(String id) throws PgnException;

    /**
     * Удаляет все партии
     */
    void deleteAll() throws PgnException;

    /**
     * Возвращает количество партий
     */
    long count() throws PgnException;

    // === Поиск ===

    /**
     * Поиск по игрокам
     */
    List<GameData> searchByPlayers(String white, String black) throws PgnException;

    /**
     * Поиск по результату
     */
    List<GameData> searchByResult(String result) throws PgnException;

    /**
     * Поиск по дебюту (ECO код)
     */
    List<GameData> searchByEco(String eco) throws PgnException;

    /**
     * Поиск по названию дебюта
     */
    List<GameData> searchByOpening(String opening) throws PgnException;

    /**
     * Поиск по диапазону дат
     */
    List<GameData> searchByDateRange(String startDate, String endDate) throws PgnException;

    /**
     * Поиск по тексту (в любом поле)
     */
    List<GameData> searchByText(String query) throws PgnException;

    // === Пакетная обработка ===

    /**
     * Сохраняет партии пакетами
     * @param games список партий
     * @param batchSize размер пакета
     */
    void saveBatch(List<GameData> games, int batchSize) throws PgnException;

    /**
     * Возвращает партии с пагинацией
     */
    List<GameData> findPaginated(int offset, int limit) throws PgnException;

    // === Дополнительно ===

    /**
     * Проверяет существование партии
     */
    boolean exists(String id) throws PgnException;

    /**
     * Импортирует партии из файла
     */
    List<GameData> importFromFile(File file) throws PgnException;

    /**
     * Экспортирует партии в файл
     */
    void exportToFile(List<GameData> games, File file) throws PgnException;
}