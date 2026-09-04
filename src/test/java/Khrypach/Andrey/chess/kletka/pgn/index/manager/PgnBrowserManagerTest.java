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

package Khrypach.Andrey.chess.kletka.pgn.index.manager;

import Khrypach.Andrey.chess.kletka.pgn.index.model.GameIndexEntry;
import Khrypach.Andrey.chess.kletka.pgn.index.model.PgnIndex;
import Khrypach.Andrey.chess.kletka.pgn.index.ui.PgnFileBrowser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.Mockito.*;

@DisplayName("PgnBrowserManager - Менеджер PGN браузеров")
class PgnBrowserManagerTest {

    @TempDir
    Path tempDir;

    private PgnBrowserManager manager;
    private PgnFileBrowser mockBrowser;
    private Path testPgnPath;

    @BeforeEach
    void setUp() throws IOException {
        manager = PgnBrowserManager.getInstance();
        manager.clearClipboard();

        // ✅ СОЗДАЕМ ТЕСТОВЫЙ ПУТЬ
        testPgnPath = tempDir.resolve("test.pgn");
        Files.createFile(testPgnPath);

        // ✅ СОЗДАЕМ МОК БРАУЗЕРА
        mockBrowser = mock(PgnFileBrowser.class);
        when(mockBrowser.getPgnPath()).thenReturn(testPgnPath);
        when(mockBrowser.getCurrentIndex()).thenReturn(createTestIndex());
    }

    @AfterEach
    void tearDown() {
        manager.clearClipboard();
        manager.closeAllBrowsers();
    }

    // ============================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ============================================================

    private PgnIndex createTestIndex() {
        PgnIndex index = new PgnIndex();
        index.setVersion(PgnIndex.FORMAT_VERSION);
        index.setFileHash("testhash");
        index.setFileSize(1024);

        GameIndexEntry entry = GameIndexEntry.builder()
                .id(1)
                .white("Player 1")
                .black("Player 2")
                .result("1-0")
                .offset(0)
                .length(100)
                .deleted(false)
                .build();
        index.addEntry(entry);
        index.refreshCache();
        return index;
    }

    private GameIndexEntry createTestEntry(int id, String white, String black) {
        return GameIndexEntry.builder()
                .id(id)
                .white(white)
                .black(black)
                .offset((long) id * 100)
                .length(100 + id * 10)
                .deleted(false)
                .version(1)
                .hash(12345 + id)
                .plyCount(40 + id)
                .build();
    }

    /**
     * Вспомогательный метод для установки буфера через рефлексию
     */
    private void setClipboardContent(Path sourcePath, List<GameIndexEntry> entries, List<String> pgnContents)
            throws Exception {
        PgnBrowserManager.ClipboardContent content =
                new PgnBrowserManager.ClipboardContent(sourcePath, entries, pgnContents);

        java.lang.reflect.Field field = PgnBrowserManager.class
                .getDeclaredField("clipboardContent");
        field.setAccessible(true);
        field.set(manager, content);
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ getInstance()
    // ============================================================

    @Nested
    @DisplayName("getInstance() - Получение экземпляра")
    class GetInstanceTests {

        @Test
        @DisplayName("Должен возвращать синглтон")
        void shouldReturnSingleton() {
            // when
            PgnBrowserManager instance1 = PgnBrowserManager.getInstance();
            PgnBrowserManager instance2 = PgnBrowserManager.getInstance();

            // then
            assertThat(instance1).isSameAs(instance2);
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ ЛИМИТОВ
    // ============================================================

    @Nested
    @DisplayName("Лимиты")
    class LimitsTests {

        @Test
        @DisplayName("Максимальное количество браузеров должно быть 10")
        void shouldHaveMaxBrowsers10() {
            assertThat(PgnBrowserManager.MAX_BROWSERS).isEqualTo(10);
        }

        @Test
        @DisplayName("Максимальное количество копируемых игр должно быть 100000")
        void shouldHaveMaxCopyGames100000() {
            assertThat(PgnBrowserManager.MAX_COPY_GAMES).isEqualTo(100000);
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ БУФЕРА ОБМЕНА
    // ============================================================

    @Nested
    @DisplayName("Буфер обмена")
    class ClipboardTests {

        @Test
        @DisplayName("hasClipboardContent() должен возвращать false если буфер пуст")
        void shouldReturnFalseWhenClipboardEmpty() {
            manager.clearClipboard();
            assertThat(manager.hasClipboardContent()).isFalse();
        }

        @Test
        @DisplayName("clearClipboard() должен очищать буфер")
        void shouldClearClipboard() throws Exception {
            // given
            GameIndexEntry entry = createTestEntry(1, "Player", "Opponent");
            List<GameIndexEntry> entries = List.of(entry);

            // Устанавливаем буфер через рефлексию
            setClipboardContent(testPgnPath, entries, List.of("1. e4 e5 *"));
            assertThat(manager.hasClipboardContent()).isTrue();

            // when
            manager.clearClipboard();

            // then
            assertThat(manager.hasClipboardContent()).isFalse();
            assertThat(manager.getClipboardContent()).isNull();
        }

        @Test
        @DisplayName("copyGames() должен добавлять игры в буфер с PGN содержимым")
        void shouldCopyGamesToClipboard() throws Exception {
            // given
            GameIndexEntry entry = createTestEntry(1, "Player 1", "Player 2");
            List<GameIndexEntry> entries = List.of(entry);

            // Устанавливаем буфер через рефлексию
            setClipboardContent(testPgnPath, entries, List.of("1. e4 e5 2. Nf3 Nc6 *"));

            // then
            assertThat(manager.hasClipboardContent()).isTrue();
            assertThat(manager.getClipboardContent()).isNotNull();
            assertThat(manager.getClipboardContent().sourceFile()).isEqualTo(testPgnPath);
            assertThat(manager.getClipboardContent().entries()).hasSize(1);
        }

        @Test
        @DisplayName("copyGames() должен фильтровать удаленные записи")
        void shouldFilterDeletedEntries() throws Exception {
            // given
            GameIndexEntry active = createTestEntry(1, "Active", "Player");
            GameIndexEntry deleted = createTestEntry(2, "Deleted", "Player");
            deleted.setDeleted(true);

            List<GameIndexEntry> entries = List.of(active, deleted);

            // Устанавливаем буфер через рефлексию
            setClipboardContent(testPgnPath, entries, List.of("1. e4 e5 *"));

            // then
            assertThat(manager.hasClipboardContent()).isTrue();
            assertThat(manager.getClipboardContent().entries())
                    .hasSize(2);
        }

        @Test
        @DisplayName("copyGames() не должен добавлять пустой список")
        void shouldNotCopyEmptyList() {
            // given
            List<GameIndexEntry> entries = List.of();

            // when
            manager.copyGames(mockBrowser, entries);

            // then
            assertThat(manager.hasClipboardContent()).isFalse();
        }
    }

    // ============================================================
    // 4. ТЕСТЫ ДЛЯ canPaste()
    // ============================================================

    @Nested
    @DisplayName("canPaste() - Проверка возможности вставки")
    class CanPasteTests {

        @Test
        @DisplayName("Должен возвращать false если буфер пуст")
        void shouldReturnFalseWhenClipboardEmpty() {
            manager.clearClipboard();
            assertThat(manager.canPaste(mockBrowser)).isFalse();
        }

        @Test
        @DisplayName("Должен возвращать false если targetBrowser null")
        void shouldReturnFalseWhenTargetNull() throws Exception {
            // given
            GameIndexEntry entry = createTestEntry(1, "Player", "Opponent");
            setClipboardContent(testPgnPath, List.of(entry), List.of("1. e4 e5 *"));

            // when
            boolean result = manager.canPaste(null);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Должен возвращать false если source и target один и тот же файл")
        void shouldReturnFalseWhenSameFile() throws Exception {
            // given
            GameIndexEntry entry = createTestEntry(1, "Player", "Opponent");
            setClipboardContent(testPgnPath, List.of(entry), List.of("1. e4 e5 *"));

            PgnFileBrowser targetBrowser = mock(PgnFileBrowser.class);
            when(targetBrowser.getPgnPath()).thenReturn(testPgnPath);

            // when
            boolean result = manager.canPaste(targetBrowser);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Должен возвращать true если можно вставить")
        void shouldReturnTrueWhenCanPaste() throws Exception {
            // given
            Path sourcePath = tempDir.resolve("source.pgn");
            Path targetPath = tempDir.resolve("target.pgn");

            Files.createFile(sourcePath);
            Files.createFile(targetPath);

            GameIndexEntry entry = createTestEntry(1, "Player", "Opponent");
            setClipboardContent(sourcePath, List.of(entry), List.of("1. e4 e5 *"));

            PgnFileBrowser targetBrowser = mock(PgnFileBrowser.class);
            when(targetBrowser.getPgnPath()).thenReturn(targetPath);

            // when
            boolean result = manager.canPaste(targetBrowser);

            // then
            assertThat(result).isTrue();
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ getBrowserCount() И getAllBrowsers()
    // ============================================================

    @Nested
    @DisplayName("getBrowserCount() и getAllBrowsers()")
    class BrowserCountTests {

        @Test
        @DisplayName("Должен возвращать 0 когда нет браузеров")
        void shouldReturnZeroWhenNoBrowsers() {
            assertThat(manager.getBrowserCount()).isEqualTo(0);
            assertThat(manager.getAllBrowsers()).isEmpty();
        }
    }

    // ============================================================
    // 6. ТЕСТЫ ДЛЯ isFileOpened()
    // ============================================================

    @Nested
    @DisplayName("isFileOpened() - Проверка открытого файла")
    class IsFileOpenedTests {

        @Test
        @DisplayName("Должен возвращать false если файл не открыт")
        void shouldReturnFalseWhenNotOpened() {
            assertThat(manager.isFileOpened(testPgnPath)).isFalse();
        }
    }

    // ============================================================
    // 7. ТЕСТЫ ДЛЯ СЛУШАТЕЛЕЙ
    // ============================================================

    @Nested
    @DisplayName("addBrowserListListener() - Слушатели изменений")
    class ListenerTests {

        @Test
        @DisplayName("Должен уведомлять слушателей об изменении списка")
        void shouldNotifyListeners() {
            // given
            boolean[] notified = {false};
            Runnable listener = () -> notified[0] = true;

            // when
            manager.addBrowserListListener(listener);
            manager.notifyBrowserListChanged();

            // then
            assertThat(notified[0]).isTrue();
        }

        @Test
        @DisplayName("Должен обрабатывать несколько слушателей")
        void shouldHandleMultipleListeners() {
            // given
            int[] counter = {0};
            Runnable listener1 = () -> counter[0]++;
            Runnable listener2 = () -> counter[0]++;

            // when
            manager.addBrowserListListener(listener1);
            manager.addBrowserListListener(listener2);
            manager.notifyBrowserListChanged();

            // then
            assertThat(counter[0]).isEqualTo(2);
        }

        @Test
        @DisplayName("Не должен падать при ошибке в слушателе")
        void shouldNotCrashOnListenerError() {
            // given
            boolean[] normalListenerExecuted = {false};

            Runnable errorListener = () -> {
                throw new RuntimeException("Test error");
            };

            Runnable normalListener = () -> normalListenerExecuted[0] = true;

            // when
            manager.addBrowserListListener(errorListener);
            manager.addBrowserListListener(normalListener);

            // then
            assertThatCode(() -> manager.notifyBrowserListChanged())
                    .doesNotThrowAnyException();
            assertThat(normalListenerExecuted[0]).isTrue();
        }
    }

    // ============================================================
    // 8. ТЕСТЫ ДЛЯ ГЕТТЕРОВ И СЕТТЕРОВ
    // ============================================================

    @Nested
    @DisplayName("Геттеры и сеттеры")
    class GetterSetterTests {

        @Test
        @DisplayName("Должен получать и устанавливать ownerStage")
        void shouldGetAndSetOwnerStage() {
            // given
            javafx.stage.Stage stage = mock(javafx.stage.Stage.class);

            // when
            manager.setOwnerStage(stage);

            // then
            assertThat(manager.getOwnerStage()).isEqualTo(stage);
        }

        @Test
        @DisplayName("Должен возвращать null для activeBrowser когда нет браузеров")
        void shouldReturnNullActiveBrowserWhenNoBrowsers() {
            assertThat(manager.getActiveBrowser()).isNull();
        }

        @Test
        @DisplayName("Должен возвращать null для clipboardContent когда пусто")
        void shouldReturnNullClipboardContentWhenEmpty() {
            manager.clearClipboard();
            assertThat(manager.getClipboardContent()).isNull();
        }
    }
}