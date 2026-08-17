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

package Khrypach.Andrey.chess.kletka.pgn.index;

import Khrypach.Andrey.chess.kletka.pgn.index.model.GameIndexEntry;
import Khrypach.Andrey.chess.kletka.pgn.index.model.PgnIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PgnFileEditor - Редактор PGN файлов")
class PgnFileEditorTest {

    @TempDir
    Path tempDir;

    private Path pgnPath;
    private PgnIndex index;
    private PgnFileEditor editor;

    @BeforeEach
    void setUp() {
        pgnPath = tempDir.resolve("game.pgn");
        index = new PgnIndex();
        editor = new PgnFileEditor(pgnPath, index);
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ appendGame()
    // ============================================================

    @Nested
    @DisplayName("appendGame() - Добавление партии")
    class AppendGameTests {

        @Test
        @DisplayName("Должен добавлять партию в конец файла")
        void shouldAppendGameToEndOfFile() throws IOException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    """;

            // when
            GameIndexEntry entry = editor.appendGame(pgn, 1);

            // then
            assertThat(entry).isNotNull();
            assertThat(entry.getId()).isEqualTo(1);
            assertThat(entry.getOffset()).isEqualTo(0);
            assertThat(entry.getLength()).isEqualTo(pgn.getBytes(StandardCharsets.UTF_8).length);
            assertThat(entry.isDeleted()).isFalse();
            assertThat(entry.getVersion()).isEqualTo(1);

            // Проверяем, что файл создан
            assertThat(Files.exists(pgnPath)).isTrue();
            String content = Files.readString(pgnPath, StandardCharsets.UTF_8);
            assertThat(content).isEqualTo(pgn);
        }

        @Test
        @DisplayName("Должен добавлять несколько партий последовательно")
        void shouldAppendMultipleGamesSequentially() throws IOException {
            // given
            String pgn1 = """
                    [Event "Game 1"]
                    [White "Player A"]
                    [Black "Player B"]
                    [Result "1-0"]
                    
                    1. e4 e5 1-0
                    """;

            String pgn2 = """
                    [Event "Game 2"]
                    [White "Player C"]
                    [Black "Player D"]
                    [Result "0-1"]
                    
                    1. d4 d5 0-1
                    """;

            // when
            GameIndexEntry entry1 = editor.appendGame(pgn1, 1);
            GameIndexEntry entry2 = editor.appendGame(pgn2, 2);

            // then
            assertThat(entry1.getOffset()).isEqualTo(0);
            assertThat(entry2.getOffset()).isGreaterThan(0);

            String content = Files.readString(pgnPath, StandardCharsets.UTF_8);
            assertThat(content).contains("Game 1", "Game 2");
        }

        @Test
        @DisplayName("Должен корректно рассчитывать offset для второй партии")
        void shouldCorrectlyCalculateOffsetForSecondGame() throws IOException {
            // given
            String pgn1 = "[Event \"Game 1\"]\n1. e4 e5 1-0\n";
            String pgn2 = "[Event \"Game 2\"]\n1. d4 d5 0-1\n";

            // when
            GameIndexEntry entry1 = editor.appendGame(pgn1, 1);
            GameIndexEntry entry2 = editor.appendGame(pgn2, 2);

            // then
            assertThat(entry1.getOffset()).isEqualTo(0);
            assertThat(entry2.getOffset()).isEqualTo(entry1.getLength());
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ readGame()
    // ============================================================

    @Nested
    @DisplayName("readGame() - Чтение партии")
    class ReadGameTests {

        @Test
        @DisplayName("Должен читать партию по записи индекса")
        void shouldReadGameByIndexEntry() throws IOException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    """;
            GameIndexEntry entry = editor.appendGame(pgn, 1);

            // when
            String readContent = editor.readGame(entry);

            // then
            assertThat(readContent).isEqualTo(pgn);
        }

        @Test
        @DisplayName("Должен читать партию по смещению и длине")
        void shouldReadGameByOffsetAndLength() throws IOException {
            // given
            String pgn = "[Event \"Game\"]\n1. e4 e5 1-0\n";
            GameIndexEntry entry = editor.appendGame(pgn, 1);

            // when
            String readContent = editor.readGame(entry.getOffset(), entry.getLength());

            // then
            assertThat(readContent).isEqualTo(pgn);
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при чтении удаленной партии")
        void shouldThrowExceptionWhenReadingDeletedGame() throws IOException {
            // given
            String pgn = "[Event \"Game\"]\n1. e4 e5 1-0\n";
            GameIndexEntry entry = editor.appendGame(pgn, 1);

            // Помечаем как удаленную
            GameIndexEntry deletedEntry = GameIndexEntry.builder()
                    .id(entry.getId())
                    .offset(entry.getOffset())
                    .length(entry.getLength())
                    .version(2)
                    .deleted(true)
                    .build();

            // when/then
            assertThatThrownBy(() -> editor.readGame(deletedEntry))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Game is deleted");
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ readBody()
    // ============================================================

    @Nested
    @DisplayName("readBody() - Чтение тела партии")
    class ReadBodyTests {

        @Test
        @DisplayName("Должен читать только тело партии без заголовков")
        void shouldReadOnlyBodyWithoutHeaders() throws IOException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    """;
            GameIndexEntry entry = editor.appendGame(pgn, 1);

            // when
            String body = editor.readBody(entry);

            // then
            assertThat(body).doesNotContain("[Event");
            assertThat(body).doesNotContain("[White");
            assertThat(body).contains("1. e4 e5 2. Nf3 Nc6 1-0");
        }

        @Test
        @DisplayName("Должен читать тело партии без заголовков с лишними пробелами")
        void shouldReadBodyWithoutHeadersWithExtraSpaces() throws IOException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                     \s
                    1. e4 e5 2. Nf3 Nc6 1-0
                   \s""";
            GameIndexEntry entry = editor.appendGame(pgn, 1);

            // when
            String body = editor.readBody(entry);

            // then
            assertThat(body).contains("1. e4 e5 2. Nf3 Nc6 1-0");
        }

        @Test
        @DisplayName("Должен возвращать пустую строку если тела нет")
        void shouldReturnEmptyStringIfNoBody() throws IOException {
            // given
            String pgn = "[Event \"Game\"]\n[White \"Player\"]\n[Result \"*\"]\n";
            GameIndexEntry entry = editor.appendGame(pgn, 1);

            // when
            String body = editor.readBody(entry);

            // then
            assertThat(body).isEmpty();
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при чтении тела удаленной партии")
        void shouldThrowExceptionWhenReadingBodyOfDeletedGame() throws IOException {
            // given
            String pgn = "[Event \"Game\"]\n1. e4 e5 1-0\n";
            GameIndexEntry entry = editor.appendGame(pgn, 1);

            GameIndexEntry deletedEntry = GameIndexEntry.builder()
                    .id(entry.getId())
                    .offset(entry.getOffset())
                    .length(entry.getLength())
                    .version(2)
                    .deleted(true)
                    .build();

            // when/then
            assertThatThrownBy(() -> editor.readBody(deletedEntry))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Game is deleted");
        }
    }

    // ============================================================
    // 4. ТЕСТЫ ДЛЯ updateGame()
    // ============================================================

    @Nested
    @DisplayName("updateGame() - Обновление партии")
    class UpdateGameTests {

        @Test
        @DisplayName("Должен обновлять партию и создавать новую версию")
        void shouldUpdateGameAndCreateNewVersion() throws IOException {
            // given
            String originalPgn = """
                    [Event "Kletka Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    """;
            GameIndexEntry originalEntry = editor.appendGame(originalPgn, 1);
            index.addEntry(originalEntry);

            String updatedPgn = """
                    [Event "Kletka Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 1-0
                    """;

            // when
            GameIndexEntry updatedEntry = editor.updateGame(1, updatedPgn);

            // then
            assertThat(updatedEntry).isNotNull();
            assertThat(updatedEntry.getId()).isEqualTo(1);
            assertThat(updatedEntry.getVersion()).isEqualTo(2);
            assertThat(updatedEntry.isDeleted()).isFalse();
            assertThat(updatedEntry.getOffset()).isGreaterThan(originalEntry.getOffset());

            // Проверяем, что новая версия записана в конец файла
            String content = Files.readString(pgnPath, StandardCharsets.UTF_8);
            assertThat(content).contains("3. Bb5 a6");
        }

        @Test
        @DisplayName("Должен сохранять метаданные при обновлении")
        void shouldPreserveMetadataWhenUpdating() throws IOException {
            // given
            String originalPgn = "[Event \"Game\"]\n1. e4 e5 1-0\n";
            GameIndexEntry originalEntry = editor.appendGame(originalPgn, 1);
            originalEntry.setWhite("Player 1");
            originalEntry.setBlack("Player 2");
            originalEntry.setEco("C44");
            index.addEntry(originalEntry);

            String updatedPgn = "[Event \"Game\"]\n1. e4 e5 2. Nf3 Nc6 1-0\n";

            // when
            GameIndexEntry updatedEntry = editor.updateGame(1, updatedPgn);

            // then
            assertThat(updatedEntry.getWhite()).isEqualTo("Player 1");
            assertThat(updatedEntry.getBlack()).isEqualTo("Player 2");
            assertThat(updatedEntry.getEco()).isEqualTo("C44");
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при обновлении несуществующей партии")
        void shouldThrowExceptionWhenUpdatingNonExistentGame() {
            // when/then
            assertThatThrownBy(() -> editor.updateGame(999, "[Event \"Game\"]"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Game not found");
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ replaceGameInPlace()
    // ============================================================

    @Nested
    @DisplayName("replaceGameInPlace() - Замена на месте")
    class ReplaceGameInPlaceTests {

        @Test
        @DisplayName("Должен заменять содержимое на месте если длины совпадают")
        void shouldReplaceInPlaceWhenLengthsMatch() throws IOException {
            // given
            String originalPgn = "[Event \"Game\"]\n1. e4 e5 1-0\n";
            GameIndexEntry entry = editor.appendGame(originalPgn, 1);

            String newPgn = "[Event \"Game\"]\n1. d4 d5 1-0\n";
            assertThat(newPgn.getBytes(StandardCharsets.UTF_8).length)
                    .isEqualTo(entry.getLength());

            // when
            boolean result = editor.replaceGameInPlace(entry, newPgn);

            // then
            assertThat(result).isTrue();
            String content = Files.readString(pgnPath, StandardCharsets.UTF_8);
            assertThat(content).isEqualTo(newPgn);
        }

        @Test
        @DisplayName("Должен возвращать false если длины не совпадают")
        void shouldReturnFalseWhenLengthsMismatch() throws IOException {
            // given
            String originalPgn = "[Event \"Game\"]\n1. e4 e5 1-0\n";
            GameIndexEntry entry = editor.appendGame(originalPgn, 1);

            String newPgn = "[Event \"Game\"]\n1. e4 e5 2. Nf3 Nc6 1-0\n";
            assertThat(newPgn.getBytes(StandardCharsets.UTF_8).length)
                    .isNotEqualTo(entry.getLength());

            // when
            boolean result = editor.replaceGameInPlace(entry, newPgn);

            // then
            assertThat(result).isFalse();
            String content = Files.readString(pgnPath, StandardCharsets.UTF_8);
            assertThat(content).isEqualTo(originalPgn);
        }

        @Test
        @DisplayName("Должен корректно заменять на месте при одинаковой длине с другим содержимым")
        void shouldReplaceCorrectlyWithSameLengthDifferentContent() throws IOException {
            // given
            String originalPgn = "[Event \"Game\"]\n1. e4 e5 1-0\n";
            GameIndexEntry entry = editor.appendGame(originalPgn, 1);

            String newPgn = "[Event \"Game\"]\n1. d4 d5 1-0\n";
            // Проверяем, что длины одинаковые
            assertThat(newPgn.getBytes(StandardCharsets.UTF_8).length)
                    .isEqualTo(entry.getLength());

            // when
            editor.replaceGameInPlace(entry, newPgn);

            // then
            String content = Files.readString(pgnPath, StandardCharsets.UTF_8);
            assertThat(content).isEqualTo(newPgn);
            assertThat(content).doesNotContain("e4");
            assertThat(content).contains("d4");
        }
    }

    // ============================================================
    // 6. ИНТЕГРАЦИОННЫЕ ТЕСТЫ
    // ============================================================

    @Nested
    @DisplayName("Интеграционные тесты")
    class IntegrationTests {

        @Test
        @DisplayName("Полный цикл: append → read → update → replace → read")
        void shouldCompleteFullLifecycle() throws IOException {
            // given
            String pgn1 = """
                    [Event "Game 1"]
                    [White "Player A"]
                    [Black "Player B"]
                    [Result "1-0"]
                    
                    1. e4 e5 1-0
                    """;
            String pgn2 = """
                    [Event "Game 2"]
                    [White "Player C"]
                    [Black "Player D"]
                    [Result "0-1"]
                    
                    1. d4 d5 0-1
                    """;

            // 1. Append
            GameIndexEntry entry1 = editor.appendGame(pgn1, 1);
            GameIndexEntry entry2 = editor.appendGame(pgn2, 2);
            index.addEntry(entry1);
            index.addEntry(entry2);

            // 2. Read
            String read1 = editor.readGame(entry1);
            String read2 = editor.readGame(entry2);
            assertThat(read1).isEqualTo(pgn1);
            assertThat(read2).isEqualTo(pgn2);

            // 3. Read body
            String body1 = editor.readBody(entry1);
            assertThat(body1).contains("1. e4 e5 1-0");

            // 4. Update
            String updatedPgn1 = """
                    [Event "Game 1"]
                    [White "Player A"]
                    [Black "Player B"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    """;
            editor.updateGame(1, updatedPgn1);

            // 5. Replace in place (если длины совпадают)
            String replacedPgn2 = """
                    [Event "Game 2"]
                    [White "Player C"]
                    [Black "Player D"]
                    [Result "0-1"]
                    
                    1. c4 c5 0-1
                    """;
            if (replacedPgn2.getBytes(StandardCharsets.UTF_8).length == entry2.getLength()) {
                editor.replaceGameInPlace(entry2, replacedPgn2);
            }

            // 6. Read all
            String content = Files.readString(pgnPath, StandardCharsets.UTF_8);
            assertThat(content).contains("Game 1", "Game 2");
            assertThat(content).contains("2. Nf3 Nc6");
        }
    }
}