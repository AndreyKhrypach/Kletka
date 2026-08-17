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

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class AppPreferences {

    private static final Preferences PREFS = Preferences.userNodeForPackage(AppPreferences.class);

    private static final String PREF_ENGINE_PATH = "engine.path";
    private static final String PREF_ENGINE_ENABLED = "engine.enabled";
    private static final String KEY_DATABASE_PATH = "database.path";
    private static final String KEY_LAST_OPENED = "last.opened";
    private static final String KEY_SAVE_DIRECTORY = "save.directory";
    private static final String PREF_LANGUAGE = "language.code";
    private static final String PREF_TILE_SIZE = "board.tile.size";
    private static final String PREF_BOARD_FLIPPED = "board.flipped";
    private static final String PREF_SHOW_COORDINATES = "board.show.coordinates";

    // НОВОЕ: ключ для сохранения темы доски
    private static final String PREF_BOARD_THEME = "board.theme";

    public static void saveSaveDirectory(String path) {
        PREFS.put(KEY_SAVE_DIRECTORY, path);
    }

    public static String getSaveDirectory() {
        return PREFS.get(KEY_SAVE_DIRECTORY, System.getProperty("user.home"));
    }

    public static void saveEnginePath(String path) {
        if (path != null && !path.isEmpty()) {
            PREFS.put(PREF_ENGINE_PATH, path);
            PREFS.putBoolean(PREF_ENGINE_ENABLED, true);
        }
    }

    public static String getEnginePath() {
        return PREFS.get(PREF_ENGINE_PATH, null);
    }

    public static void resetEngineSettings() {
        PREFS.remove(PREF_ENGINE_PATH);
        PREFS.putBoolean(PREF_ENGINE_ENABLED, false);
    }

    /**
     *
     * @deprecated Будет использоваться в версии 2.0 (SQLite).
     * Тесты будут добавлены при реализации SQLite.
     */
    @Deprecated
    public static void saveDatabasePath(String path) {
        if (path != null && !path.isEmpty()) {
            PREFS.put(KEY_DATABASE_PATH, path);
        }
    }

    /**
     *
     * @deprecated Будет использоваться в версии 2.0 (SQLite).
     * Тесты будут добавлены при реализации SQLite.
     */
    @Deprecated
    public static String getDatabasePath() {
        return PREFS.get(KEY_DATABASE_PATH, null);
    }

    /**
     *
     * @deprecated Будет использоваться в версии 2.0 (SQLite).
     * Тесты будут добавлены при реализации SQLite.
     */
    @Deprecated
    public static void saveLastOpened(String path) {
        if (path != null && !path.isEmpty()) {
            PREFS.put(KEY_LAST_OPENED, path);
        }
    }

    /**
     *
     * @deprecated Будет использоваться в версии 2.0 (SQLite).
     * Тесты будут добавлены при реализации SQLite.
     */
    @Deprecated
    public static String getLastOpened() {
        return PREFS.get(KEY_LAST_OPENED, null);
    }

    public static void saveLanguage(String languageCode) {
        PREFS.put(PREF_LANGUAGE, languageCode);
    }

    public static String getLanguage() {
        return PREFS.get(PREF_LANGUAGE, "ru");
    }

    public static void saveTileSize(int size) {
        if (size >= BoardSizeController.MIN_TILE_SIZE && size <= BoardSizeController.MAX_TILE_SIZE) {
            PREFS.putInt(PREF_TILE_SIZE, size);
        }
    }

    public static int getTileSize() {
        return PREFS.getInt(PREF_TILE_SIZE, BoardSizeController.MIN_TILE_SIZE + BoardSizeController.STEP_SIZE * 2);
    }

    public static void saveBoardFlipped(boolean flipped) {
        PREFS.putBoolean(PREF_BOARD_FLIPPED, flipped);
    }

    public static boolean isBoardFlipped() {
        return PREFS.getBoolean(PREF_BOARD_FLIPPED, false);
    }

    public static void saveShowCoordinates(boolean show) {
        PREFS.putBoolean(PREF_SHOW_COORDINATES, show);
    }

    public static boolean isShowCoordinates() {
        return PREFS.getBoolean(PREF_SHOW_COORDINATES, true);
    }

    // НОВЫЕ МЕТОДЫ ДЛЯ ТЕМЫ ДОСКИ

    /**
     * Сохраняет тему доски (индекс в массиве THEMES)
     */
    public static void saveBoardTheme(int themeIndex) {
        PREFS.putInt(PREF_BOARD_THEME, themeIndex);
    }

    /**
     * Возвращает сохраненный индекс темы доски
     * По умолчанию - WOOD (индекс 0)
     */
    public static int getBoardThemeIndex() {
        return PREFS.getInt(PREF_BOARD_THEME, 0);
    }

    /**
     *
     * @deprecated Будет использоваться в версии 2.0 (SQLite).
     * Тесты будут добавлены при реализации SQLite.
     */
    @Deprecated
    public static void resetDatabaseSettings() {
        PREFS.remove(KEY_DATABASE_PATH);
        PREFS.remove(KEY_LAST_OPENED);
    }

    public static void resetAllPreferences() throws BackingStoreException {
        PREFS.clear();
    }
}