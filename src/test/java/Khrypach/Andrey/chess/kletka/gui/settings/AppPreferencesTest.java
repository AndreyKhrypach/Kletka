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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.prefs.BackingStoreException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AppPreferences - Настройки приложения")
class AppPreferencesTest {

    @BeforeEach
    void setUp() throws BackingStoreException {
        AppPreferences.resetAllPreferences();
        AppPreferences.resetEngineSettings();
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
            String testPath = "/test/save/dir";

            // when
            AppPreferences.saveSaveDirectory(testPath);
            String result = AppPreferences.getSaveDirectory();

            // then
            assertThat(result).isEqualTo(testPath);
        }

        @Test
        @DisplayName("Должен возвращать домашнюю директорию по умолчанию")
        void shouldReturnHomeDirectoryByDefault() {
            // when
            String result = AppPreferences.getSaveDirectory();

            // then
            assertThat(result).isEqualTo(System.getProperty("user.home"));
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
            int size = 110;

            // when
            AppPreferences.saveTileSize(size);
            int result = AppPreferences.getTileSize();

            // then
            assertThat(result).isBetween(BoardSizeController.MIN_TILE_SIZE, BoardSizeController.MAX_TILE_SIZE);
            // Проверяем, что результат кратен STEP_SIZE
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
        @DisplayName("Должен возвращать значение по умолчанию")
        void shouldReturnDefaultTileSize() {
            // when
            int result = AppPreferences.getTileSize();

            // then
            assertThat(result).isEqualTo(BoardSizeController.MIN_TILE_SIZE + BoardSizeController.STEP_SIZE * 2);
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
    // 8. ТЕСТЫ ДЛЯ @Deprecated МЕТОДОВ (для будущей версии)
    // ============================================================

    @Nested
    @DisplayName("@Deprecated методы (для будущей версии 2.0)")
    class DeprecatedMethodsTests {

        @Test
        @DisplayName("saveDatabasePath/getDatabasePath - должны работать")
        void shouldSaveAndGetDatabasePath() {
            // given
            String testPath = "/test/db/path";

            // when
            AppPreferences.saveDatabasePath(testPath);
            String result = AppPreferences.getDatabasePath();

            // then
            assertThat(result).isEqualTo(testPath);
        }

        @Test
        @DisplayName("saveLastOpened/getLastOpened - должны работать")
        void shouldSaveAndGetLastOpened() {
            // given
            String testPath = "/test/last/opened";

            // when
            AppPreferences.saveLastOpened(testPath);
            String result = AppPreferences.getLastOpened();

            // then
            assertThat(result).isEqualTo(testPath);
        }

        @Test
        @DisplayName("resetDatabaseSettings - должен сбрасывать настройки БД")
        void shouldResetDatabaseSettings() {
            // given
            AppPreferences.saveDatabasePath("/test/path");
            AppPreferences.saveLastOpened("/test/last");
            assertThat(AppPreferences.getDatabasePath()).isNotNull();
            assertThat(AppPreferences.getLastOpened()).isNotNull();

            // when
            AppPreferences.resetDatabaseSettings();

            // then
            assertThat(AppPreferences.getDatabasePath()).isNull();
            assertThat(AppPreferences.getLastOpened()).isNull();
        }
    }
}