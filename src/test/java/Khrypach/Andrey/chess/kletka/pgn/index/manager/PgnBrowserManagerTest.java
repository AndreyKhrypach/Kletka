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

package Khrypach.Andrey.chess.kletka.pgn.index.manager;

import Khrypach.Andrey.chess.kletka.pgn.index.model.GameIndexEntry;
import Khrypach.Andrey.chess.kletka.pgn.index.ui.PgnFileBrowser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("PgnBrowserManager - Менеджер PGN браузеров")
class PgnBrowserManagerTest {

    private PgnBrowserManager manager;
    private PgnFileBrowser mockBrowser;

    @BeforeEach
    void setUp() {
        manager = PgnBrowserManager.getInstance();
        manager.clearClipboard();

        // Создаем мок браузера
        mockBrowser = mock(PgnFileBrowser.class);
        when(mockBrowser.getPgnPath()).thenReturn(Path.of("test.pgn"));
    }

    @AfterEach
    void tearDown() {
        manager.clearClipboard();
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
        @DisplayName("Максимальное количество копируемых игр должно быть 1000")
        void shouldHaveMaxCopyGames1000() {
            assertThat(PgnBrowserManager.MAX_COPY_GAMES).isEqualTo(1000);
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ clearClipboard() И hasClipboardContent()
    // ============================================================

    @Nested
    @DisplayName("clearClipboard() и hasClipboardContent()")
    class ClipboardTests {

        @Test
        @DisplayName("hasClipboardContent() должен возвращать false если буфер пуст")
        void shouldReturnFalseWhenClipboardEmpty() {
            // given
            manager.clearClipboard();

            // then
            assertThat(manager.hasClipboardContent()).isFalse();
        }

        @Test
        @DisplayName("clearClipboard() должен очищать буфер")
        void shouldClearClipboard() {
            // given
            GameIndexEntry entry = GameIndexEntry.builder().id(1).build();
            List<GameIndexEntry> entries = List.of(entry);

            // when
            manager.copyGames(mockBrowser, entries);
            assertThat(manager.hasClipboardContent()).isTrue();

            manager.clearClipboard();

            // then
            assertThat(manager.hasClipboardContent()).isFalse();
            assertThat(manager.getClipboardContent()).isNull();
        }

        @Test
        @DisplayName("copyGames() должен добавлять игры в буфер")
        void shouldCopyGamesToClipboard() {
            // given
            GameIndexEntry entry = GameIndexEntry.builder()
                    .id(1)
                    .white("Player 1")
                    .black("Player 2")
                    .build();
            List<GameIndexEntry> entries = List.of(entry);

            // when
            manager.copyGames(mockBrowser, entries);

            // then
            assertThat(manager.hasClipboardContent()).isTrue();
            assertThat(manager.getClipboardContent()).isNotNull();
            assertThat(manager.getClipboardContent().count()).isEqualTo(1);
            assertThat(manager.getClipboardContent().sourceFile()).isEqualTo(Path.of("test.pgn"));
        }

        @Test
        @DisplayName("copyGames() должен выбрасывать исключение при превышении лимита")
        void shouldThrowExceptionWhenCopyLimitExceeded() {
            // given
            List<GameIndexEntry> entries = new ArrayList<>();
            for (int i = 0; i < PgnBrowserManager.MAX_COPY_GAMES + 1; i++) {
                entries.add(GameIndexEntry.builder().id(i).build());
            }

            // when/then
            assertThatThrownBy(() -> manager.copyGames(mockBrowser, entries))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("1000");
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
            // given
            manager.clearClipboard();

            // when
            boolean result = manager.canPaste(mockBrowser);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Должен возвращать false если targetBrowser null")
        void shouldReturnFalseWhenTargetNull() {
            // given
            GameIndexEntry entry = GameIndexEntry.builder().id(1).build();
            manager.copyGames(mockBrowser, List.of(entry));

            // when
            boolean result = manager.canPaste(null);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Должен возвращать false если source и target один и тот же файл")
        void shouldReturnFalseWhenSameFile() {
            // given
            PgnFileBrowser sameBrowser = mock(PgnFileBrowser.class);
            when(sameBrowser.getPgnPath()).thenReturn(Path.of("test.pgn"));

            GameIndexEntry entry = GameIndexEntry.builder().id(1).build();
            manager.copyGames(mockBrowser, List.of(entry));

            // when
            boolean result = manager.canPaste(sameBrowser);

            // then
            assertThat(result).isFalse();
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ ClipboardContent
    // ============================================================

    @Nested
    @DisplayName("ClipboardContent - Буфер обмена")
    class ClipboardContentTests {

        @Test
        @DisplayName("Должен создавать с правильными данными")
        void shouldCreateWithCorrectData() {
            // given
            Path path = Path.of("test.pgn");
            List<GameIndexEntry> entries = List.of(GameIndexEntry.builder().id(1).build());
            int count = 1;
            long timestamp = System.currentTimeMillis();

            // when
            PgnBrowserManager.ClipboardContent content =
                    new PgnBrowserManager.ClipboardContent(path, entries, count, timestamp);

            // then
            assertThat(content.sourceFile()).isEqualTo(path);
            assertThat(content.count()).isEqualTo(1);
            assertThat(content.timestamp()).isEqualTo(timestamp);
        }

        @Test
        @DisplayName("Должен возвращать читаемое строковое представление")
        void shouldReturnReadableToString() {
            // given
            Path path = Path.of("test.pgn");
            List<GameIndexEntry> entries = List.of(GameIndexEntry.builder().id(1).build());
            PgnBrowserManager.ClipboardContent content =
                    new PgnBrowserManager.ClipboardContent(path, entries, 1, System.currentTimeMillis());

            // then
            assertThat(content.toString()).contains("test.pgn");
            assertThat(content.toString()).contains("count=1");
        }
    }

    // ============================================================
    // 6. ТЕСТЫ ДЛЯ addBrowserListListener()
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
    }
}