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
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AppPreferences - Настройки приложения")
class AppPreferencesTest {

    @TempDir
    static Path tempDir;

    // Сохраняем настройки перед тестами
    private String savedEnginePath;
    private String savedLanguage;
    private int savedTileSize;
    private boolean savedBoardFlipped;
    private boolean savedShowCoordinates;
    private int savedBoardTheme;
    private String savedSaveDirectory;

    @BeforeEach
    void setUp() {
        // ========== СОХРАНЯЕМ ТЕКУЩИЕ НАСТРОЙКИ ==========
        savedEnginePath = AppPreferences.getEnginePath();
        savedLanguage = AppPreferences.getLanguage();
        savedTileSize = AppPreferences.getTileSize();
        savedBoardFlipped = AppPreferences.isBoardFlipped();
        savedShowCoordinates = AppPreferences.isShowCoordinates();
        savedBoardTheme = AppPreferences.getBoardThemeIndex();
        savedSaveDirectory = AppPreferences.getSaveDirectory();

        // ========== УСТАНАВЛИВАЕМ ЯЗЫК ДЛЯ ТЕСТОВ ==========
        AppPreferences.saveLanguage("ru");
    }

    @AfterEach
    void tearDown() {
        // ========== ВОССТАНАВЛИВАЕМ НАСТРОЙКИ ==========
        if (savedEnginePath != null) {
            AppPreferences.saveEnginePath(savedEnginePath);
        }
        if (savedLanguage != null) {
            AppPreferences.saveLanguage(savedLanguage);
        }
        AppPreferences.saveTileSize(savedTileSize);
        AppPreferences.saveBoardFlipped(savedBoardFlipped);
        AppPreferences.saveShowCoordinates(savedShowCoordinates);
        AppPreferences.saveBoardTheme(savedBoardTheme);
        if (savedSaveDirectory != null) {
            AppPreferences.saveSaveDirectory(savedSaveDirectory);
        }
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ saveDirectory
    // ============================================================

    @Nested
    @DisplayName("Сохранение директории")
    class SaveDirectoryTests {

        @Test
        @DisplayName("Должен сохранять и получать директорию сохранения")
        void shouldSaveAndGetSaveDirectory() {
            // given - создаем временную директорию
            String testPath = tempDir.resolve("test-save-dir").toString();

            // when
            AppPreferences.saveSaveDirectory(testPath);
            String result = AppPreferences.getSaveDirectory();

            // then
            assertThat(result).isEqualTo(testPath);
        }

        @Test
        @DisplayName("Должен возвращать папку bases по умолчанию")
        void shouldReturnBasesDirectoryByDefault() {
            // when
            String result = AppPreferences.getSaveDirectory();

            // then
            assertThat(result).isNotNull();
            assertThat(result).contains("bases");
        }

        @Test
        @DisplayName("Должен создавать директорию если её нет")
        void shouldCreateDirectoryIfNotExists() {
            // given
            String testPath = tempDir.resolve("new-bases-dir").toString();

            // when
            AppPreferences.saveSaveDirectory(testPath);
            String result = AppPreferences.getSaveDirectory();

            // then
            assertThat(result).isEqualTo(testPath);
            // Проверяем что директория создалась
            java.io.File dir = new java.io.File(testPath);
            assertThat(dir.exists()).isTrue();
            assertThat(dir.isDirectory()).isTrue();
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ ENGINE PATH
    // ============================================================

    @Nested
    @DisplayName("Настройки движка")
    class EngineSettingsTests {

        @Test
        @DisplayName("Должен сохранять и получать путь к движку")
        void shouldSaveAndGetEnginePath() {
            // given
            String testPath = "/usr/bin/stockfish";

            // when
            AppPreferences.saveEnginePath(testPath);
            String result = AppPreferences.getEnginePath();

            // then
            assertThat(result).isEqualTo(testPath);
        }

        @Test
        @DisplayName("Должен игнорировать null при сохранении пути")
        void shouldIgnoreNullWhenSavingEnginePath() {
            // when
            AppPreferences.saveEnginePath(null);

            // then
            assertThat(AppPreferences.getEnginePath()).isNull();
        }

        @Test
        @DisplayName("Должен игнорировать пустую строку при сохранении пути")
        void shouldIgnoreEmptyWhenSavingEnginePath() {
            // when
            AppPreferences.saveEnginePath("");

            // then
            assertThat(AppPreferences.getEnginePath()).isNull();
        }

        @Test
        @DisplayName("Должен сбрасывать настройки движка")
        void shouldResetEngineSettings() {
            // given
            AppPreferences.saveEnginePath("/test/path");
            assertThat(AppPreferences.getEnginePath()).isNotNull();

            // when
            AppPreferences.resetEngineSettings();

            // then
            assertThat(AppPreferences.getEnginePath()).isNull();
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ ЯЗЫКА
    // ============================================================

    @Nested
    @DisplayName("Настройки языка")
    class LanguageTests {

        @Test
        @DisplayName("Должен сохранять и получать код языка")
        void shouldSaveAndGetLanguage() {
            // given
            String languageCode = "en";

            // when
            AppPreferences.saveLanguage(languageCode);
            String result = AppPreferences.getLanguage();

            // then
            assertThat(result).isEqualTo(languageCode);
        }

        @Test
        @DisplayName("Должен возвращать 'ru' по умолчанию")
        void shouldReturnRussianByDefault() {
            // when
            String result = AppPreferences.getLanguage();

            // then
            assertThat(result).isEqualTo("ru");
        }

        @Test
        @DisplayName("Должен определять язык из системной локали")
        void shouldDetectLanguageFromSystem() {
            // when
            String result = AppPreferences.getLanguage();

            // then
            // Язык должен быть одним из поддерживаемых: ru, en, zh
            assertThat(result).isIn("ru", "en", "zh");
        }
    }

    // ============================================================
    // 4. ТЕСТЫ ДЛЯ РАЗМЕРА КЛЕТКИ
    // ============================================================

    @Nested
    @DisplayName("Настройки размера клетки")
    class TileSizeTests {

        @Test
        @DisplayName("Должен сохранять размер клетки в допустимом диапазоне")
        void shouldSaveTileSizeInValidRange() {
            // given
            int size = 90;

            // when
            AppPreferences.saveTileSize(size);
            int result = AppPreferences.getTileSize();

            // then
            assertThat(result).isBetween(BoardSizeController.MIN_TILE_SIZE, BoardSizeController.MAX_TILE_SIZE);
            assertThat(result % BoardSizeController.STEP_SIZE).isEqualTo(0);
        }

        @Test
        @DisplayName("Должен игнорировать размер меньше минимального")
        void shouldIgnoreSizeBelowMin() {
            // given
            int size = BoardSizeController.MIN_TILE_SIZE - 10;

            // when
            AppPreferences.saveTileSize(size);
            int result = AppPreferences.getTileSize();

            // then
            assertThat(result).isNotEqualTo(size);
        }

        @Test
        @DisplayName("Должен игнорировать размер больше максимального")
        void shouldIgnoreSizeAboveMax() {
            // given
            int size = BoardSizeController.MAX_TILE_SIZE + 10;

            // when
            AppPreferences.saveTileSize(size);
            int result = AppPreferences.getTileSize();

            // then
            assertThat(result).isNotEqualTo(size);
        }

        @Test
        @DisplayName("Должен возвращать значение по умолчанию (80)")
        void shouldReturnDefaultTileSize() {
            // when
            int result = AppPreferences.getTileSize();

            // then
            assertThat(result).isEqualTo(BoardSizeController.DEFAULT_TILE_SIZE);
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ ПЕРЕВОРОТА ДОСКИ
    // ============================================================

    @Nested
    @DisplayName("Настройки переворота доски")
    class BoardFlippedTests {

        @Test
        @DisplayName("Должен сохранять и получать состояние переворота")
        void shouldSaveAndGetBoardFlipped() {
            // when
            AppPreferences.saveBoardFlipped(true);
            boolean result = AppPreferences.isBoardFlipped();

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Должен возвращать false по умолчанию")
        void shouldReturnFalseByDefault() {
            // when
            boolean result = AppPreferences.isBoardFlipped();

            // then
            assertThat(result).isFalse();
        }
    }

    // ============================================================
    // 6. ТЕСТЫ ДЛЯ КООРДИНАТ
    // ============================================================

    @Nested
    @DisplayName("Настройки отображения координат")
    class ShowCoordinatesTests {

        @Test
        @DisplayName("Должен сохранять и получать состояние координат")
        void shouldSaveAndGetShowCoordinates() {
            // when
            AppPreferences.saveShowCoordinates(false);
            boolean result = AppPreferences.isShowCoordinates();

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Должен возвращать true по умолчанию")
        void shouldReturnTrueByDefault() {
            // when
            boolean result = AppPreferences.isShowCoordinates();

            // then
            assertThat(result).isTrue();
        }
    }

    // ============================================================
    // 7. ТЕСТЫ ДЛЯ ТЕМЫ ДОСКИ
    // ============================================================

    @Nested
    @DisplayName("Настройки темы доски")
    class BoardThemeTests {

        @Test
        @DisplayName("Должен сохранять и получать индекс темы")
        void shouldSaveAndGetBoardTheme() {
            // given
            int themeIndex = 2;

            // when
            AppPreferences.saveBoardTheme(themeIndex);
            int result = AppPreferences.getBoardThemeIndex();

            // then
            assertThat(result).isEqualTo(themeIndex);
        }

        @Test
        @DisplayName("Должен возвращать 0 по умолчанию")
        void shouldReturnZeroByDefault() {
            // when
            int result = AppPreferences.getBoardThemeIndex();

            // then
            assertThat(result).isEqualTo(0);
        }
    }

    // ============================================================
    // 8. ТЕСТЫ ДЛЯ BASES DIRECTORY
    // ============================================================

    @Nested
    @DisplayName("Директория баз данных (bases)")
    class BasesDirectoryTests {

        @Test
        @DisplayName("Должен возвращать путь к папке bases")
        void shouldGetBasesDirectory() {
            // when
            Path result = AppPreferences.getBasesDirectory();

            // then
            assertThat(result).isNotNull();
            assertThat(result.toString()).contains("bases");
        }
    }

    // ============================================================
    // 9. ТЕСТЫ ДЛЯ @Deprecated МЕТОДОВ
    // ============================================================

    @Nested
    @DisplayName("@Deprecated методы (заглушки для версии 2.0)")
    class DeprecatedMethodsTests {

        @Test
        @DisplayName("saveDatabasePath - заглушка, возвращает null")
        void saveDatabasePathShouldDoNothing() {
            // given
            String testPath = "/test/db/path";

            // when
            AppPreferences.saveDatabasePath(testPath);
            String result = AppPreferences.getDatabasePath();

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("saveLastOpened - заглушка, возвращает null")
        void saveLastOpenedShouldDoNothing() {
            // given
            String testPath = "/test/last/opened";

            // when
            AppPreferences.saveLastOpened(testPath);
            String result = AppPreferences.getLastOpened();

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("resetDatabaseSettings - заглушка, ничего не делает")
        void resetDatabaseSettingsShouldDoNothing() {
            // given
            AppPreferences.saveDatabasePath("/test/path");
            AppPreferences.saveLastOpened("/test/last");

            // when
            AppPreferences.resetDatabaseSettings();

            // then
            assertThat(AppPreferences.getDatabasePath()).isNull();
            assertThat(AppPreferences.getLastOpened()).isNull();
        }
    }
}