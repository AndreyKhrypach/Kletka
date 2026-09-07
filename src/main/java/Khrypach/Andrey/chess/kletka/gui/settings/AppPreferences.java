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

package Khrypach.Andrey.chess.kletka.gui.settings;

import Khrypach.Andrey.chess.kletka.gui.board.BoardSizeController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Основной класс для работы с настройками программы.
 * Делегирует все операции ConfigFilePreferences.
 */
public class AppPreferences {

    private static final Logger log = LoggerFactory.getLogger(AppPreferences.class);
    private static final ConfigFilePreferences config = ConfigFilePreferences.getInstance();

    // ========== ЯЗЫК ==========
    public static void saveLanguage(String languageCode) {
        log.debug("save language {}", languageCode);
        config.setLanguage(languageCode);
    }

    public static String getLanguage() {
        return config.getLanguage();
    }

    // ========== РАЗМЕР ДОСКИ ==========
    public static void saveTileSize(int size) {
        if (size >= BoardSizeController.MIN_TILE_SIZE && size <= BoardSizeController.MAX_TILE_SIZE) {
            log.debug("save tile size {}", size);
            config.setTileSize(size);
        }
    }

    public static int getTileSize() {
        return config.getTileSize();
    }

    // ========== ПЕРЕВОРОТ ДОСКИ ==========
    public static void saveBoardFlipped(boolean flipped) {
        log.debug("save board flipped {}", flipped);
        config.setBoardFlipped(flipped);
    }

    public static boolean isBoardFlipped() {
        return config.isBoardFlipped();
    }

    // ========== КООРДИНАТЫ ==========
    public static void saveShowCoordinates(boolean show) {
        config.setShowCoordinates(show);
    }

    public static boolean isShowCoordinates() {
        return config.isShowCoordinates();
    }

    // ========== ТЕМА ДОСКИ ==========
    public static void saveBoardTheme(int themeIndex) {
        log.debug("save board theme {}", themeIndex);
        config.setBoardTheme(themeIndex);
    }

    public static int getBoardThemeIndex() {
        return config.getBoardTheme();
    }

    // ========== ДИРЕКТОРИЯ СОХРАНЕНИЯ ==========
    public static void saveSaveDirectory(String path) {
        log.debug("save save directory {}", path);
        config.setSaveDirectory(path);
    }

    public static String getSaveDirectory() {
        return config.getSaveDirectory();
    }

    // ========== ПУТЬ К ДВИЖКУ ==========
    public static void saveEnginePath(String path) {
        log.debug("save engine path {}", path);
        config.setEnginePath(path);
    }

    public static String getEnginePath() {
        return config.getEnginePath();
    }

    // ========== ПОСЛЕДНИЙ ОТКРЫТЫЙ PGN ==========
    public static void saveLastOpenedPgn(String path) {
        config.setLastOpenedPgn(path);
    }

    public static String getLastOpenedPgn() {
        return config.getLastOpenedPgn();
    }

    // ========== ДИРЕКТОРИЯ БАЗ ==========
    public static Path getBasesDirectory() {
        return config.getBasesDirectory();
    }

    // ========== СБРОС НАСТРОЕК ==========
    public static void resetAllPreferences() {
        config.resetToDefaults();
    }

    // ========== ИНФОРМАЦИЯ ==========
    public static String getConfigFilePath() {
        return config.getConfigFilePath();
    }

    public static boolean configFileExists() {
        return config.configFileExists();
    }

    // ========== ПОСЛЕДНЯЯ ОТКРЫТАЯ КНИГА ==========
    public static void saveRecentBook(String path) {
        log.debug("save recent book {}", path);
        config.setRecentBook(path);
    }

    public static String getRecentBook() {
        return config.getRecentBook();
    }

    // ========== ДИРЕКТОРИЯ КНИГ ==========
    public static void saveBookDirectory(String path) {
        log.debug("save book directory {}", path);
        config.setBookDirectory(path);
    }

    public static String getBookDirectory() {
        return config.getBookDirectory();
    }

    // ========== ПОСЛЕДНИЙ РЕЖИМ НАВИГАЦИИ ==========
    public static void saveNavigationMode(String mode) {
        log.debug("save navigation mode {}", mode);
        config.setNavigationMode(mode);
    }

    public static String getNavigationMode() {
        return config.getNavigationMode();
    }

    // ========== ПОСЛЕДНЯЯ ПАПКА СОХРАНЕНИЯ ==========
    public static void saveLastSaveDirectory(String path) {
        log.debug("save last save directory {}", path);
        config.setLastSaveDirectory(path);
    }

    public static String getLastSaveDirectory() {
        return config.getLastSaveDirectory();
    }

    // ========== ПОСЛЕДНЯЯ ПАПКА ОТКРЫТИЯ ==========
    public static void saveLastOpenDirectory(String path) {
        log.debug("save last open directory {}", path);
        config.setLastOpenDirectory(path);
    }

    public static String getLastOpenDirectory() {
        return config.getLastOpenDirectory();
    }

    /**
     * @deprecated Будет использоваться в версии 2.0 (SQLite)
     */
    @Deprecated
    public static void saveDatabasePath(String path) {
        // Пока ничего не делаем
    }

    /**
     * @deprecated Будет использоваться в версии 2.0 (SQLite)
     */
    @Deprecated
    public static String getDatabasePath() {
        return null;
    }

    /**
     * @deprecated Будет использоваться в версии 2.0 (SQLite)
     */
    @Deprecated
    public static void saveLastOpened(String path) {
        // Пока ничего не делаем
    }

    /**
     * @deprecated Будет использоваться в версии 2.0 (SQLite)
     */
    @Deprecated
    public static String getLastOpened() {
        return null;
    }

    /**
     * @deprecated Будет использоваться в версии 2.0 (SQLite)
     */
    @Deprecated
    public static void resetDatabaseSettings() {
        // Пока ничего не делаем
    }

    /**
     * Очищает путь к движку
     */
    public static void resetEngineSettings() {
        config.setEnginePath(null);
    }
}