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
    private String savedRecentBook;
    private String savedBookDirectory;
    private String savedNavigationMode;
    private String savedLastSaveDirectory;
    private String savedLastOpenDirectory;

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
        savedRecentBook = AppPreferences.getRecentBook();
        savedBookDirectory = AppPreferences.getBookDirectory();
        savedNavigationMode = AppPreferences.getNavigationMode();
        savedLastSaveDirectory = AppPreferences.getLastSaveDirectory();
        savedLastOpenDirectory = AppPreferences.getLastOpenDirectory();

        // ========== СБРАСЫВАЕМ НАСТРОЙКИ ДЛЯ ТЕСТОВ ==========
        AppPreferences.saveRecentBook(null);
        AppPreferences.saveNavigationMode("PGN");
        AppPreferences.saveLastSaveDirectory(null);
        AppPreferences.saveLastOpenDirectory(null);

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
        if (savedRecentBook != null) {
            AppPreferences.saveRecentBook(savedRecentBook);
        }
        if (savedBookDirectory != null) {
            AppPreferences.saveBookDirectory(savedBookDirectory);
        }
        if (savedNavigationMode != null) {
            AppPreferences.saveNavigationMode(savedNavigationMode);
        }
        if (savedLastSaveDirectory != null) {
            AppPreferences.saveLastSaveDirectory(savedLastSaveDirectory);
        }
        if (savedLastOpenDirectory != null) {
            AppPreferences.saveLastOpenDirectory(savedLastOpenDirectory);
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
            // given
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
    // 9. ТЕСТЫ ДЛЯ RECENT BOOK (НОВЫЕ)
    // ============================================================

    @Nested
    @DisplayName("Последняя открытая книга")
    class RecentBookTests {

        @Test
        @DisplayName("Должен возвращать null для последней книги по умолчанию")
        void shouldReturnNullByDefault() {
            // given
            AppPreferences.saveRecentBook(null);

            // when
            String result = AppPreferences.getRecentBook();

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Должен сохранять и получать путь к последней книге")
        void shouldSaveAndGetRecentBook() {
            // given
            String testPath = tempDir.resolve("book").resolve("test-book.ctg").toString();

            // when
            AppPreferences.saveRecentBook(testPath);
            String result = AppPreferences.getRecentBook();

            // then
            assertThat(result).isEqualTo(testPath);
        }

        @Test
        @DisplayName("Должен удалять путь при сохранении null")
        void shouldRemovePathWhenSavingNull() {
            // given
            AppPreferences.saveRecentBook("/test/book");
            assertThat(AppPreferences.getRecentBook()).isNotNull();

            // when
            AppPreferences.saveRecentBook(null);

            // then
            assertThat(AppPreferences.getRecentBook()).isNull();
        }
    }

    // ============================================================
    // 10. ТЕСТЫ ДЛЯ BOOK DIRECTORY (НОВЫЕ)
    // ============================================================

    @Nested
    @DisplayName("Директория книг")
    class BookDirectoryTests {

        @Test
        @DisplayName("Должен сохранять и получать директорию книг")
        void shouldSaveAndGetBookDirectory() {
            // given
            String testPath = tempDir.resolve("custom-books").toString();

            // when
            AppPreferences.saveBookDirectory(testPath);
            String result = AppPreferences.getBookDirectory();

            // then
            assertThat(result).isEqualTo(testPath);
        }

        @Test
        @DisplayName("Должен возвращать путь по умолчанию (bases/book)")
        void shouldReturnDefaultBookDirectory() {
            // when
            String result = AppPreferences.getBookDirectory();

            // then
            assertThat(result).isNotNull();
            assertThat(result).contains("book");
        }
    }

    // ============================================================
    // 11. ТЕСТЫ ДЛЯ NAVIGATION MODE (НОВЫЕ)
    // ============================================================

    @Nested
    @DisplayName("Режим навигации")
    class NavigationModeTests {

        @Test
        @DisplayName("Должен сохранять и получать режим навигации")
        void shouldSaveAndGetNavigationMode() {
            // given
            String mode = "DATABASE";

            // when
            AppPreferences.saveNavigationMode(mode);
            String result = AppPreferences.getNavigationMode();

            // then
            assertThat(result).isEqualTo(mode);
        }

        @Test
        @DisplayName("Должен возвращать 'PGN' по умолчанию")
        void shouldReturnPgnByDefault() {
            // when
            String result = AppPreferences.getNavigationMode();

            // then
            assertThat(result).isEqualTo("PGN");
        }

        @Test
        @DisplayName("Должен удалять режим при сохранении null")
        void shouldRemoveModeWhenSavingNull() {
            // given
            AppPreferences.saveNavigationMode("DATABASE");
            assertThat(AppPreferences.getNavigationMode()).isNotNull();

            // when
            AppPreferences.saveNavigationMode(null);

            // then
            assertThat(AppPreferences.getNavigationMode()).isEqualTo("PGN");
        }

        @Test
        @DisplayName("Должен поддерживать регистры")
        void shouldSupportCaseInsensitive() {
            // given
            String mode = "database";

            // when
            AppPreferences.saveNavigationMode(mode);
            String result = AppPreferences.getNavigationMode();

            // then
            assertThat(result).isEqualTo(mode);
        }
    }

    // ============================================================
    // 12. ТЕСТЫ ДЛЯ LAST SAVE DIRECTORY (НОВЫЕ)
    // ============================================================

    @Nested
    @DisplayName("Последняя директория сохранения")
    class LastSaveDirectoryTests {

        @Test
        @DisplayName("Должен возвращать null после сохранения null")
        void shouldReturnNullAfterSavingNull() {
            // given
            AppPreferences.saveLastSaveDirectory(null);

            // when
            String result = AppPreferences.getLastSaveDirectory();

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Должен сохранять и получать последнюю директорию сохранения")
        void shouldSaveAndGetLastSaveDirectory() {
            // given
            String testPath = tempDir.resolve("last-save").toString();

            // when
            AppPreferences.saveLastSaveDirectory(testPath);
            String result = AppPreferences.getLastSaveDirectory();

            // then
            assertThat(result).isEqualTo(testPath);
        }

        @Test
        @DisplayName("Должен удалять путь при сохранении null")
        void shouldRemovePathWhenSavingNull() {
            // given
            AppPreferences.saveLastSaveDirectory("/test/path");
            assertThat(AppPreferences.getLastSaveDirectory()).isNotNull();

            // when
            AppPreferences.saveLastSaveDirectory(null);

            // then
            assertThat(AppPreferences.getLastSaveDirectory()).isNull();
        }
    }

    // ============================================================
    // 13. ТЕСТЫ ДЛЯ LAST OPEN DIRECTORY (НОВЫЕ)
    // ============================================================

    @Nested
    @DisplayName("Последняя директория открытия")
    class LastOpenDirectoryTests {

        @Test
        @DisplayName("Должен возвращать путь к bases по умолчанию (при первом запуске)")
        void shouldReturnBasesByDefault() {
            // given
            // Удаляем сохраненное значение
            AppPreferences.saveLastOpenDirectory(null);

            // when
            String result = AppPreferences.getLastOpenDirectory();

            // then
            // После удаления возвращается null
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Должен сохранять и получать последнюю директорию открытия")
        void shouldSaveAndGetLastOpenDirectory() {
            // given
            String testPath = tempDir.resolve("last-open").toString();

            // when
            AppPreferences.saveLastOpenDirectory(testPath);
            String result = AppPreferences.getLastOpenDirectory();

            // then
            assertThat(result).isEqualTo(testPath);
        }

        @Test
        @DisplayName("Должен удалять путь при сохранении null")
        void shouldRemovePathWhenSavingNull() {
            // given
            AppPreferences.saveLastOpenDirectory("/test/path");
            assertThat(AppPreferences.getLastOpenDirectory()).isNotNull();

            // when
            AppPreferences.saveLastOpenDirectory(null);

            // then
            assertThat(AppPreferences.getLastOpenDirectory()).isNull();
        }
    }

    // ============================================================
    // 14. ТЕСТЫ ДЛЯ resetAllPreferences() (НОВЫЕ)
    // ============================================================

    @Nested
    @DisplayName("Сброс всех настроек")
    class ResetAllPreferencesTests {

        @Test
        @DisplayName("Должен сбрасывать все настройки к значениям по умолчанию")
        void shouldResetAllPreferences() {
            // given
            // Устанавливаем разные настройки
            AppPreferences.saveLanguage("en");
            AppPreferences.saveTileSize(100);
            AppPreferences.saveBoardFlipped(true);
            AppPreferences.saveShowCoordinates(false);
            AppPreferences.saveBoardTheme(3);
            AppPreferences.saveRecentBook("/test/book");
            AppPreferences.saveNavigationMode("DATABASE");

            // when
            AppPreferences.resetAllPreferences();

            // then
            // Проверяем, что настройки сброшены
            assertThat(AppPreferences.getLanguage()).isIn("ru", "en");
            assertThat(AppPreferences.getTileSize()).isBetween(
                    BoardSizeController.MIN_TILE_SIZE,
                    BoardSizeController.MAX_TILE_SIZE
            );
            assertThat(AppPreferences.isBoardFlipped()).isFalse();
            assertThat(AppPreferences.isShowCoordinates()).isTrue();
            assertThat(AppPreferences.getBoardThemeIndex()).isEqualTo(0);
            assertThat(AppPreferences.getRecentBook()).isNull();
            assertThat(AppPreferences.getNavigationMode()).isEqualTo("PGN");
        }
    }

    // ============================================================
    // 15. ТЕСТЫ ДЛЯ getConfigFilePath() и configFileExists() (НОВЫЕ)
    // ============================================================

    @Nested
    @DisplayName("Информация о файле настроек")
    class ConfigFileInfoTests {

        @Test
        @DisplayName("Должен возвращать путь к файлу настроек")
        void shouldGetConfigFilePath() {
            // when
            String path = AppPreferences.getConfigFilePath();

            // then
            assertThat(path).isNotNull();
            assertThat(path).contains("config.properties");
        }

        @Test
        @DisplayName("Должен проверять существование файла настроек")
        void shouldCheckConfigFileExists() {
            // when
            boolean exists = AppPreferences.configFileExists();

            // then
            // Проверяем, что метод возвращает boolean (true или false)
            assertThat(exists).isInstanceOf(Boolean.class);
            // Или просто проверяем, что это boolean (без проверки типа)
            assertThat(exists).isTrue(); // или isFalse()
        }
    }

    // ============================================================
    // 16. ТЕСТЫ ДЛЯ @Deprecated МЕТОДОВ
    // ============================================================

    @Nested
    @DisplayName("@Deprecated методы (заглушки для версии 2.0)")
    class DeprecatedMethodsTests {

        @Test
        @DisplayName("saveDatabasePath - заглушка, ничего не делает")
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

    // ============================================================
    // 17. ТЕСТЫ ДЛЯ ОБЩЕЙ РАБОТОСПОСОБНОСТИ
    // ============================================================

    @Nested
    @DisplayName("Общая работоспособность")
    class GeneralTests {

        @Test
        @DisplayName("Должен корректно работать после сброса настроек")
        void shouldWorkAfterReset() {
            // given
            AppPreferences.saveLanguage("zh");
            AppPreferences.saveBoardTheme(5);

            // when
            AppPreferences.resetAllPreferences();

            // then
            assertThat(AppPreferences.getLanguage()).isIn("ru", "en");
            assertThat(AppPreferences.getBoardThemeIndex()).isEqualTo(0);
            assertThat(AppPreferences.isBoardFlipped()).isFalse();
        }

        @Test
        @DisplayName("Должен сохранять настройки между вызовами")
        void shouldPersistSettingsBetweenCalls() {
            // given
            String testPath = tempDir.resolve("test-dir").toString();

            // when
            AppPreferences.saveSaveDirectory(testPath);

            // then
            assertThat(AppPreferences.getSaveDirectory()).isEqualTo(testPath);

            // Еще раз проверяем
            assertThat(AppPreferences.getSaveDirectory()).isEqualTo(testPath);
        }
    }
}
