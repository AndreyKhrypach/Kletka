/*
 *
 *  * Copyright (c) 2024 Andrey Khrypach
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

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Управление настройками программы через файл config.properties
 * Кроссплатформенное расположение:
 * - Windows: папка с программой (user.dir)
 * - Linux: ~/.config/kletka/ (согласно XDG стандарту)
 * - macOS: ~/Library/Application Support/Kletka/
 */
public class ConfigFilePreferences {

    private static final Logger log = LoggerFactory.getLogger(ConfigFilePreferences.class);

    private static final String CONFIG_FILE_NAME = "config.properties";
    private static final String BASES_DIR_NAME = "bases";
    public static final String KEY_RECENT_BOOK = "book.recent";
    public static final String KEY_BOOK_DIRECTORY = "book.directory";
    public static final String KEY_NAVIGATION_MODE = "navigation.mode";
    public static final String KEY_LAST_SAVE_DIRECTORY = "save.last.directory";
    public static final String KEY_LAST_OPEN_DIRECTORY = "open.last.directory";

    private static ConfigFilePreferences instance;
    private final Properties properties = new Properties();

    @Getter
    private Path configPath;
    private Path basesDir;

    // Ключи настроек
    public static final String KEY_LANGUAGE = "language";
    public static final String KEY_TILE_SIZE = "board.tile.size";
    public static final String KEY_BOARD_FLIPPED = "board.flipped";
    public static final String KEY_SHOW_COORDINATES = "board.show.coordinates";
    public static final String KEY_BOARD_THEME = "board.theme";
    public static final String KEY_SAVE_DIRECTORY = "save.directory";
    public static final String KEY_ENGINE_PATH = "engine.path";
    public static final String KEY_LAST_OPENED_PGN = "last.opened.pgn";

    // Значения по умолчанию
    private static final String DEFAULT_LANGUAGE = "ru";
    private static final int DEFAULT_TILE_SIZE = 80;
    private static final boolean DEFAULT_BOARD_FLIPPED = false;
    private static final boolean DEFAULT_SHOW_COORDINATES = true;
    private static final int DEFAULT_BOARD_THEME = 0; // WOOD

    private ConfigFilePreferences() {
        initDirectories();
        loadProperties();
    }

    public static ConfigFilePreferences getInstance() {
        if (instance == null) {
            instance = new ConfigFilePreferences();
        }
        return instance;
    }

    /**
     * Инициализация директорий в зависимости от ОС
     */
    private void initDirectories() {
        Path appDir = getConfigDirectory();

        this.configPath = appDir.resolve(CONFIG_FILE_NAME);
        this.basesDir = appDir.resolve(BASES_DIR_NAME);

        try {
            if (!Files.exists(appDir)) {
                Files.createDirectories(appDir);
                log.info("Created application directory: {}", appDir);
            }
            if (!Files.exists(basesDir)) {
                Files.createDirectories(basesDir);
                log.info("Created bases directory: {}", basesDir);
            }
        } catch (IOException e) {
            log.error("Failed to create directories: {}", e.getMessage());
            // Fallback
            Path fallbackDir = Paths.get(System.getProperty("user.home"), ".kletka");
            this.configPath = fallbackDir.resolve(CONFIG_FILE_NAME);
            this.basesDir = fallbackDir.resolve(BASES_DIR_NAME);
            try {
                Files.createDirectories(basesDir);
            } catch (IOException ex) {
                log.error("Critical: Cannot create any directory for config", ex);
            }
        }
    }

    private boolean isProgramInProgramFiles() {
        String programPath = System.getProperty("user.dir");
        String programFiles = System.getenv("ProgramFiles");
        String programFilesX86 = System.getenv("ProgramFiles(x86)");

        if (programFiles != null && programPath.startsWith(programFiles)) {
            return true;
        }
        return programFilesX86 != null && programPath.startsWith(programFilesX86);
    }

    private Path getConfigDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        Path configDir;

        if (os.contains("win")) {
            String programPath = System.getProperty("user.dir");

            if (isProgramInProgramFiles()) {
                // ========== ПРОГРАММА В PROGRAM FILES ==========
                // Используем APPDATA
                String appData = System.getenv("APPDATA");
                if (appData != null && !appData.isEmpty()) {
                    configDir = Paths.get(appData, "Kletka");
                } else {
                    // fallback: ProgramData
                    String programData = System.getenv("PROGRAMDATA");
                    configDir = Paths.get(programData, "Kletka");
                }
                log.info("Program in Program Files, using APPDATA: {}", configDir);
            } else {
                // ========== ПРОГРАММА НЕ В PROGRAM FILES ==========
                // Настройки рядом с программой
                configDir = Paths.get(programPath);
                log.info("Program not in Program Files, using program directory: {}", configDir);
            }
        } else if (os.contains("mac")) {
            // macOS: ~/Library/Application Support/Kletka
            String userHome = System.getProperty("user.home");
            configDir = Paths.get(userHome, "Library", "Application Support", "Kletka");
        } else {
            // Linux: ~/.config/kletka/
            String xdgConfig = System.getenv("XDG_CONFIG_HOME");
            if (xdgConfig != null && !xdgConfig.isEmpty()) {
                configDir = Paths.get(xdgConfig, "kletka");
            } else {
                String userHome = System.getProperty("user.home");
                configDir = Paths.get(userHome, ".config", "kletka");
            }
        }

        return configDir;
    }

    /**
     * Загрузка настроек из файла
     */
    private void loadProperties() {
        if (Files.exists(configPath)) {
            try (InputStream input = Files.newInputStream(configPath)) {
                properties.load(input);
                log.info("Loaded configuration from: {}", configPath);
            } catch (IOException e) {
                log.warn("Failed to load config file, using defaults: {}", e.getMessage());
                setDefaults();
                saveProperties();
            }
        } else {
            log.info("Config file not found, will create with defaults: {}", configPath);
            setDefaults();
            saveProperties();
        }
    }

    /**
     * Сохранение настроек в файл
     */
    private void saveProperties() {
        try {
            if (!Files.exists(configPath.getParent())) {
                Files.createDirectories(configPath.getParent());
            }

            try (OutputStream output = Files.newOutputStream(configPath)) {
                properties.store(output, "Kletka Chess Application Configuration");
                log.info("Saved configuration to: {}", configPath);
                log.info("Current properties: {}", properties);
            }
        } catch (IOException e) {
            log.error("Failed to save config file: {}", e.getMessage(), e);
        }
    }

    private void setDefaults() {
        String systemLanguage = detectSystemLanguage();

        properties.setProperty(KEY_LANGUAGE, systemLanguage);
        properties.setProperty(KEY_TILE_SIZE, String.valueOf(DEFAULT_TILE_SIZE));
        properties.setProperty(KEY_BOARD_FLIPPED, String.valueOf(DEFAULT_BOARD_FLIPPED));
        properties.setProperty(KEY_SHOW_COORDINATES, String.valueOf(DEFAULT_SHOW_COORDINATES));
        properties.setProperty(KEY_BOARD_THEME, String.valueOf(DEFAULT_BOARD_THEME));
        properties.setProperty(KEY_SAVE_DIRECTORY, basesDir.toString());

        // ========== ПУТИ ПО УМОЛЧАНИЮ ==========
        properties.setProperty(KEY_LAST_OPEN_DIRECTORY, basesDir.toString());  // ← Открытие
        properties.setProperty(KEY_LAST_SAVE_DIRECTORY, basesDir.toString());  // ← Сохранение
        properties.setProperty(KEY_BOOK_DIRECTORY, basesDir.resolve("book").toString());

        // Если программа в Program Files, используем APPDATA
        if (isProgramInProgramFiles()) {
            String appData = System.getenv("APPDATA");
            if (appData != null && !appData.isEmpty()) {
                Path appDataPath = Paths.get(appData, "Kletka");
                properties.setProperty(KEY_LAST_OPEN_DIRECTORY, appDataPath.toString());
                properties.setProperty(KEY_LAST_SAVE_DIRECTORY, appDataPath.toString());
                properties.setProperty(KEY_BOOK_DIRECTORY, appDataPath.resolve("book").toString());
            }
        } else {
            // Если не в Program Files — используем basesDir
            properties.setProperty(KEY_LAST_OPEN_DIRECTORY, basesDir.toString());
            properties.setProperty(KEY_LAST_SAVE_DIRECTORY, basesDir.toString());
            properties.setProperty(KEY_BOOK_DIRECTORY, basesDir.resolve("book").toString());
        }
    }

    /**
     * Определяет язык из системных настроек ОС
     * Поддерживаются: ru (русский), zh (китайский), en (английский — fallback)
     */
    private String detectSystemLanguage() {
        try {
            // 1. Пробуем получить из переменной LANG (Linux/macOS)
            String langEnv = System.getenv("LANG");
            if (langEnv != null && !langEnv.isEmpty()) {
                log.debug("LANG environment variable: {}", langEnv);

                // Извлекаем язык из формата ru_RU.UTF-8
                String[] parts = langEnv.split("_");
                if (parts.length > 0) {
                    String language = parts[0];
                    if ("ru".equals(language)) {
                        log.info("Detected system language from LANG: Russian");
                        return "ru";
                    } else if ("zh".equals(language)) {
                        log.info("Detected system language from LANG: Chinese");
                        return "zh";
                    }
                }
            }

            // 2. Пробуем из системной локали Java
            String systemLocale = System.getProperty("user.language");
            if (systemLocale != null && !systemLocale.isEmpty()) {
                log.debug("System language property: {}", systemLocale);

                if ("ru".equals(systemLocale)) {
                    log.info("Detected system language: Russian");
                    return "ru";
                } else if ("zh".equals(systemLocale)) {
                    log.info("Detected system language: Chinese");
                    return "zh";
                }
            }

            // 3. Для всех остальных — английский
            log.info("Detected system language: English (default)");
            return "en";

        } catch (Exception e) {
            log.warn("Failed to detect system language, using English as default", e);
            return "en";
        }
    }

    // ========== GETTERS / SETTERS ==========

    public String getLanguage() {
        return properties.getProperty(KEY_LANGUAGE, DEFAULT_LANGUAGE);
    }

    public void setLanguage(String language) {
        properties.setProperty(KEY_LANGUAGE, language);
        saveProperties();
    }

    public int getTileSize() {
        try {
            return Integer.parseInt(properties.getProperty(KEY_TILE_SIZE, String.valueOf(DEFAULT_TILE_SIZE)));
        } catch (NumberFormatException e) {
            return DEFAULT_TILE_SIZE;
        }
    }

    public void setTileSize(int size) {
        properties.setProperty(KEY_TILE_SIZE, String.valueOf(size));
        saveProperties();
    }

    public boolean isBoardFlipped() {
        return Boolean.parseBoolean(properties.getProperty(KEY_BOARD_FLIPPED, String.valueOf(DEFAULT_BOARD_FLIPPED)));
    }

    public void setBoardFlipped(boolean flipped) {
        properties.setProperty(KEY_BOARD_FLIPPED, String.valueOf(flipped));
        saveProperties();
    }

    public boolean isShowCoordinates() {
        return Boolean.parseBoolean(properties.getProperty(KEY_SHOW_COORDINATES, String.valueOf(DEFAULT_SHOW_COORDINATES)));
    }

    public void setShowCoordinates(boolean show) {
        properties.setProperty(KEY_SHOW_COORDINATES, String.valueOf(show));
        saveProperties();
    }

    public int getBoardTheme() {
        try {
            return Integer.parseInt(properties.getProperty(KEY_BOARD_THEME, String.valueOf(DEFAULT_BOARD_THEME)));
        } catch (NumberFormatException e) {
            return DEFAULT_BOARD_THEME;
        }
    }

    public void setBoardTheme(int themeIndex) {
        properties.setProperty(KEY_BOARD_THEME, String.valueOf(themeIndex));
        saveProperties();
    }

    public String getSaveDirectory() {
        String dir = properties.getProperty(KEY_SAVE_DIRECTORY);
        if (dir == null || dir.isEmpty()) {
            return basesDir.toString();
        }
        // Проверяем, существует ли директория
        Path path = Paths.get(dir);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                log.info("Created save directory: {}", path);
            } catch (IOException e) {
                log.warn("Save directory does not exist and cannot be created: {}", dir);
                return basesDir.toString();
            }
        }
        return dir;
    }

    public void setSaveDirectory(String path) {
        if (path != null && !path.isEmpty()) {
            // Проверяем, что директория существует или создаем
            Path dirPath = Paths.get(path);
            try {
                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                    log.info("Created new save directory: {}", path);
                }
                properties.setProperty(KEY_SAVE_DIRECTORY, path);
                saveProperties();
            } catch (IOException e) {
                log.error("Failed to create save directory: {}", path, e);
            }
        }
    }

    public String getEnginePath() {
        return properties.getProperty(KEY_ENGINE_PATH, null);
    }

    public void setEnginePath(String path) {
        if (path != null && !path.isEmpty()) {
            properties.setProperty(KEY_ENGINE_PATH, path);
        } else {
            properties.remove(KEY_ENGINE_PATH);
        }
        saveProperties();
    }

    public String getLastOpenedPgn() {
        return properties.getProperty(KEY_LAST_OPENED_PGN, null);
    }

    public void setLastOpenedPgn(String path) {
        if (path != null && !path.isEmpty()) {
            properties.setProperty(KEY_LAST_OPENED_PGN, path);
        } else {
            properties.remove(KEY_LAST_OPENED_PGN);
        }
        saveProperties();
    }

    public Path getBasesDirectory() {
        return basesDir;
    }

    /**
     * Сброс всех настроек к значениям по умолчанию
     */
    public void resetToDefaults() {
        properties.clear();
        setDefaults();
        saveProperties();
    }

    /**
     * Проверяет, существует ли файл настроек
     */
    public boolean configFileExists() {
        return Files.exists(configPath);
    }

    /**
     * Возвращает путь к файлу настроек
     */
    public String getConfigFilePath() {
        return configPath.toString();
    }

    // ========== МЕТОДЫ ДЛЯ РАБОТЫ С КНИГАМИ ==========

    public String getRecentBook() {
        return properties.getProperty(KEY_RECENT_BOOK, null);
    }

    public void setRecentBook(String path) {
        if (path != null && !path.isEmpty()) {
            properties.setProperty(KEY_RECENT_BOOK, path);
        } else {
            properties.remove(KEY_RECENT_BOOK);
        }
        saveProperties();
    }

    public String getBookDirectory() {
        String dir = properties.getProperty(KEY_BOOK_DIRECTORY);
        if (dir == null || dir.isEmpty()) {
            // По умолчанию: bases/book/
            Path bookDir = basesDir.resolve("book");
            try {
                if (!Files.exists(bookDir)) {
                    Files.createDirectories(bookDir);
                }
                return bookDir.toString();
            } catch (IOException e) {
                log.warn("Failed to create book directory: {}", e.getMessage());
                return basesDir.toString();
            }
        }
        return dir;
    }

    public void setBookDirectory(String path) {
        if (path != null && !path.isEmpty()) {
            properties.setProperty(KEY_BOOK_DIRECTORY, path);
            saveProperties();
        }
    }

    public String getNavigationMode() {
        return properties.getProperty(KEY_NAVIGATION_MODE, "PGN");
    }

    public void setNavigationMode(String mode) {
        if (mode != null && !mode.isEmpty()) {
            properties.setProperty(KEY_NAVIGATION_MODE, mode);
        } else {
            properties.remove(KEY_NAVIGATION_MODE);
        }
        saveProperties();
    }

    public String getLastSaveDirectory() {
        return properties.getProperty(KEY_LAST_SAVE_DIRECTORY, null);
    }

    public void setLastSaveDirectory(String path) {
        if (path != null && !path.isEmpty()) {
            log.info("Setting last save directory: {}", path);
            properties.setProperty(KEY_LAST_SAVE_DIRECTORY, path);
        } else {
            log.info("Removing last save directory");
            properties.remove(KEY_LAST_SAVE_DIRECTORY);
        }
        saveProperties();
        log.info("After save, file exists: {}", Files.exists(configPath));
    }

    public String getLastOpenDirectory() {
        return properties.getProperty(KEY_LAST_OPEN_DIRECTORY, null);
    }

    public void setLastOpenDirectory(String path) {
        if (path != null && !path.isEmpty()) {
            properties.setProperty(KEY_LAST_OPEN_DIRECTORY, path);
        } else {
            properties.remove(KEY_LAST_OPEN_DIRECTORY);
        }
        saveProperties();
    }
}
