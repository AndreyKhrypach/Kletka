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

package Khrypach.Andrey.chess.kletka.pgn.index.operation;

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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PgnGameOperation - Операции с PGN играми")
class PgnGameOperationTest {

    @TempDir
    Path tempDir;

    private Path pgnPath;
    private PgnIndex index;
    private PgnGameOperation operation;

    @BeforeEach
    void setUp() throws IOException {
        pgnPath = tempDir.resolve("game.pgn");

        // Создаем PGN файл с одной игрой
        String pgn = """
            [Event "Kletka Game"]
            [White "Player 1"]
            [Black "Player 2"]
            [Result "1-0"]
            
            1. e4 e5 2. Nf3 Nc6 1-0
            """;
        Files.writeString(pgnPath, pgn, StandardCharsets.UTF_8);

        // Создаем запись
        GameIndexEntry entry = GameIndexEntry.builder()
                .id(1)
                .offset(0)
                .length(pgn.getBytes(StandardCharsets.UTF_8).length)
                .version(1)
                .deleted(false)
                .white("Player 1")
                .black("Player 2")
                .result("1-0")
                .event("Kletka Game")
                .build();

        // ========== ИСПРАВЛЕНИЕ: используем ArrayList вместо List.of() ==========
        List<GameIndexEntry> entries = new java.util.ArrayList<>();
        entries.add(entry);

        index = PgnIndex.builder()
                .version(1)
                .fileHash("test")
                .fileSize(pgn.length())
                .gameCount(1)
                .activeCount(1)
                .entries(entries)  // ← изменяемый список
                .build();

        operation = new PgnGameOperation(pgnPath, index);
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ addGame()
    // ============================================================

    @Nested
    @DisplayName("addGame() - Добавление игры")
    class AddGameTests {

        @Test
        @DisplayName("Должен добавлять новую игру в конец файла")
        void shouldAddNewGame() {
            // given
            String newPgn = """
                    [Event "Kletka Game 2"]
                    [White "Player 3"]
                    [Black "Player 4"]
                    [Result "0-1"]
                    
                    1. d4 d5 2. c4 e6 0-1
                    """;

            // when
            PgnGameOperation.OperationResult result = operation.addGame(newPgn);

            // then
            assertThat(result).isNotNull();
            assertThat(result.type()).isEqualTo(PgnGameOperation.OperationType.ADD);
            assertThat(result.gameId()).isEqualTo(2);
            assertThat(result.newEntry()).isNotNull();
            assertThat(index.getGameCount()).isEqualTo(2);
            assertThat(index.getActiveCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Должен сохранять заголовки новой игры")
        void shouldSaveHeadersForNewGame() {
            // given
            String newPgn = """
                    [Event "Kletka Game 2"]
                    [White "Player 3"]
                    [Black "Player 4"]
                    [Result "0-1"]
                    [ECO "D20"]
                    
                    1. d4 d5 2. c4 e6 0-1
                    """;

            // when
            PgnGameOperation.OperationResult result = operation.addGame(newPgn);

            // then
            GameIndexEntry entry = result.newEntry();
            assertThat(entry.getWhite()).isEqualTo("Player 3");
            assertThat(entry.getBlack()).isEqualTo("Player 4");
            assertThat(entry.getResult()).isEqualTo("0-1");
            assertThat(entry.getEco()).isEqualTo("D20");
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при добавлении некорректного PGN")
        void shouldThrowExceptionForInvalidPgn() {
            // given
            String invalidPgn = "invalid pgn";

            // when/then
            assertThatThrownBy(() -> operation.addGame(invalidPgn))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid PGN content");
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ deleteGame()
    // ============================================================

    @Nested
    @DisplayName("deleteGame() - Удаление игры")
    class DeleteGameTests {

        @Test
        @DisplayName("Должен удалять игру по ID")
        void shouldDeleteGameById() throws IOException {
            // when
            PgnGameOperation.OperationResult result = operation.deleteGame(1);

            // then
            assertThat(result).isNotNull();
            assertThat(result.type()).isEqualTo(PgnGameOperation.OperationType.DELETE);
            assertThat(result.gameId()).isEqualTo(1);
            assertThat(result.newEntry().isDeleted()).isTrue();
            assertThat(index.getActiveCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при удалении несуществующей игры")
        void shouldThrowExceptionWhenDeletingNonExistentGame() {
            // when/then
            assertThatThrownBy(() -> operation.deleteGame(999))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Game not found");
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при удалении уже удаленной игры")
        void shouldThrowExceptionWhenDeletingAlreadyDeletedGame() throws IOException {
            // given
            operation.deleteGame(1);

            // when/then
            assertThatThrownBy(() -> operation.deleteGame(1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Game not found");
        }

        @Test
        @DisplayName("Должен добавлять тег Deleted в PGN файл")
        void shouldAddDeletedTagToPgnFile() throws IOException {
            // when
            operation.deleteGame(1);

            // then
            String content = Files.readString(pgnPath, StandardCharsets.UTF_8);
            assertThat(content).contains("[Deleted \" true\"]");
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ duplicateGame()
    // ============================================================

    @Nested
    @DisplayName("duplicateGame() - Дублирование игры")
    class DuplicateGameTests {

        @Test
        @DisplayName("Должен дублировать игру")
        void shouldDuplicateGame() throws IOException {
            // when
            PgnGameOperation.OperationResult result = operation.duplicateGame(1);

            // then
            assertThat(result).isNotNull();
            assertThat(result.type()).isEqualTo(PgnGameOperation.OperationType.DUPLICATE);
            assertThat(result.gameId()).isEqualTo(2);
            assertThat(result.newEntry()).isNotNull();
            assertThat(index.getGameCount()).isEqualTo(2);
            assertThat(index.getActiveCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Должен сохранять все заголовки при дублировании")
        void shouldPreserveHeadersWhenDuplicating() throws IOException {
            // given
            GameIndexEntry original = index.getEntryById(1);

            // when
            PgnGameOperation.OperationResult result = operation.duplicateGame(1);

            // then
            GameIndexEntry duplicated = result.newEntry();
            assertThat(duplicated.getWhite()).isEqualTo(original.getWhite());
            assertThat(duplicated.getBlack()).isEqualTo(original.getBlack());
            assertThat(duplicated.getResult()).isEqualTo(original.getResult());
            assertThat(duplicated.getEvent()).isEqualTo(original.getEvent());
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при дублировании несуществующей игры")
        void shouldThrowExceptionWhenDuplicatingNonExistentGame() {
            // when/then
            assertThatThrownBy(() -> operation.duplicateGame(999))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Game not found");
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при дублировании удаленной игры")
        void shouldThrowExceptionWhenDuplicatingDeletedGame() throws IOException {
            // given
            operation.deleteGame(1);

            // when/then
            assertThatThrownBy(() -> operation.duplicateGame(1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Game not found");
        }
    }

    // ============================================================
    // 4. ИНТЕГРАЦИОННЫЕ ТЕСТЫ
    // ============================================================

    @Nested
    @DisplayName("Интеграционные тесты")
    class IntegrationTests {

        @Test
        @DisplayName("Полный цикл: add → delete → duplicate")
        void shouldCompleteFullCycle() throws Exception {
            // given
            String newPgn = """
                    [Event "Kletka Game 2"]
                    [White "Player 3"]
                    [Black "Player 4"]
                    [Result "0-1"]
                    
                    1. d4 d5 2. c4 e6 0-1
                    """;

            // 1. Add
            PgnGameOperation.OperationResult addResult = operation.addGame(newPgn);
            assertThat(addResult.type()).isEqualTo(PgnGameOperation.OperationType.ADD);
            assertThat(index.getGameCount()).isEqualTo(2);

            // 2. Delete
            PgnGameOperation.OperationResult deleteResult = operation.deleteGame(1);
            assertThat(deleteResult.type()).isEqualTo(PgnGameOperation.OperationType.DELETE);
            assertThat(index.getActiveCount()).isEqualTo(1);

            // 3. Duplicate
            PgnGameOperation.OperationResult duplicateResult = operation.duplicateGame(2);
            assertThat(duplicateResult.type()).isEqualTo(PgnGameOperation.OperationType.DUPLICATE);
            assertThat(index.getGameCount()).isEqualTo(3);
            assertThat(index.getActiveCount()).isEqualTo(2);
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ OperationResult
    // ============================================================

    @Nested
    @DisplayName("OperationResult - Результат операции")
    class OperationResultTests {

        @Test
        @DisplayName("Должен корректно возвращать все поля")
        void shouldReturnAllFields() throws IOException {
            // when
            PgnGameOperation.OperationResult result = operation.deleteGame(1);

            // then
            assertThat(result.type()).isEqualTo(PgnGameOperation.OperationType.DELETE);
            assertThat(result.gameId()).isEqualTo(1);
            assertThat(result.oldEntry()).isNotNull();
            assertThat(result.newEntry()).isNotNull();
            assertThat(result.message()).isNotNull();
        }

        @Test
        @DisplayName("toString() должен возвращать читаемое представление")
        void shouldReturnReadableToString() throws IOException {
            // when
            PgnGameOperation.OperationResult result = operation.deleteGame(1);

            // then
            assertThat(result.toString()).contains("DELETE");
            assertThat(result.toString()).contains("id=1");
        }
    }
}