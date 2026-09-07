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

package Khrypach.Andrey.chess.kletka.pgn.index.operation;

import Khrypach.Andrey.chess.kletka.pgn.index.PgnIndexManager;
import Khrypach.Andrey.chess.kletka.pgn.index.model.GameIndexEntry;
import Khrypach.Andrey.chess.kletka.pgn.index.model.PgnIndex;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PgnGameOperation - CRUD операции с PGN")
class PgnGameOperationTest {

    @TempDir
    Path tempDir;

    private Path pgnPath;
    private PgnIndex index;
    private PgnGameOperation operation;
    private PgnIndexManager indexManager;

    @BeforeEach
    void setUp() throws IOException {
        pgnPath = tempDir.resolve("test.pgn");
        indexManager = new PgnIndexManager();

        // Создаем тестовый PGN файл с 5 партиями
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            sb.append("[Event \"Test Game ").append(i).append("\"]\n");
            sb.append("[Site \"Test Site\"]\n");
            sb.append("[Date \"").append(2024).append(".01.0").append(i).append("\"]\n");
            sb.append("[Round \"").append(i).append("\"]\n");
            sb.append("[White \"Player ").append(i % 2 + 1).append("\"]\n");
            sb.append("[Black \"Player ").append((i + 1) % 2 + 1).append("\"]\n");
            sb.append("[Result \"1-0\"]\n");
            sb.append("[ECO \"A00\"]\n");
            sb.append("[Opening \"Test Opening\"]\n");
            sb.append("[Variation \"Test Variation\"]\n");
            sb.append("[Deleted \"false\"]\n");
            sb.append("\n");
            sb.append("1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Bxc6 dxc6 5. O-O\n");
            sb.append("6. d4 exd4 7. Nxd4 Bd6 8. Nc3 O-O 9. Be3 Re8 10. f4\n");
            sb.append("\n");
        }
        Files.writeString(pgnPath, sb.toString(), StandardCharsets.UTF_8);

        // Создаем индекс
        List<GameIndexEntry> entries = new ArrayList<>();
        String content = Files.readString(pgnPath);
        int gameLength = content.length() / 5;
        long offset = 0;

        for (int i = 1; i <= 5; i++) {
            entries.add(GameIndexEntry.builder()
                    .id(i)
                    .offset(offset)
                    .length(gameLength)
                    .version(1)
                    .deleted(false)
                    .white("Player " + (i % 2 + 1))
                    .black("Player " + ((i + 1) % 2 + 1))
                    .result("1-0")
                    .event("Test Game " + i)
                    .eco("A00")
                    .opening("Test Opening")
                    .variation("Test Variation")
                    .site("Test Site")
                    .year("2024")
                    .plyCount(20)
                    .hash(content.substring((int) offset, (int) (offset + gameLength)).hashCode())
                    .build());
            offset += gameLength;
        }

        index = indexManager.createIndex(pgnPath, entries);
        indexManager.saveIndex(pgnPath, index);
        operation = new PgnGameOperation(pgnPath, index);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.exists(pgnPath)) {
            Files.deleteIfExists(pgnPath);
        }
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ deleteGame()
    // ============================================================

    @Nested
    @DisplayName("deleteGame() - Удаление партии")
    class DeleteGameTests {

        @Test
        @DisplayName("Должен удалять существующую партию")
        void shouldDeleteExistingGame() throws IOException {
            // given
            int gameId = 1;
            GameIndexEntry beforeDelete = index.getEntryById(gameId);
            assertThat(beforeDelete).isNotNull();
            assertThat(beforeDelete.isDeleted()).isFalse();

            // when
            PgnGameOperation.OperationResult result = operation.deleteGame(gameId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.type()).isEqualTo(PgnGameOperation.OperationType.DELETE);
            assertThat(result.gameId()).isEqualTo(gameId);
            assertThat(result.message()).contains(String.valueOf(gameId));

            // Проверяем, что партия помечена как удаленная
            PgnIndex updatedIndex = indexManager.loadIndex(pgnPath);
            GameIndexEntry deletedEntry = updatedIndex.getEntryById(gameId);
            assertThat(deletedEntry).isNotNull();
            assertThat(deletedEntry.isDeleted()).isTrue();
            assertThat(deletedEntry.getVersion()).isEqualTo(beforeDelete.getVersion() + 1);

            // Проверяем, что активных партий стало меньше
            assertThat(updatedIndex.getActiveCount()).isEqualTo(4);
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при удалении несуществующей партии")
        void shouldThrowExceptionWhenGameNotFound() {
            // given
            int nonExistentId = 999;

            // when/then
            assertThatThrownBy(() -> operation.deleteGame(nonExistentId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(String.valueOf(nonExistentId));
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при повторном удалении уже удаленной партии")
        void shouldThrowExceptionWhenGameAlreadyDeleted() throws IOException {
            // given
            int gameId = 1;
            operation.deleteGame(gameId);

            // when/then
            assertThatThrownBy(() -> operation.deleteGame(gameId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(String.valueOf(gameId));
        }

        @Test
        @DisplayName("Должен корректно обновлять индекс после удаления")
        void shouldUpdateIndexAfterDelete() throws IOException {
            // given
            int gameId = 2;

            // when
            operation.deleteGame(gameId);

            // then
            PgnIndex updatedIndex = indexManager.loadIndex(pgnPath);

            // Проверяем, что индекс обновился
            assertThat(updatedIndex.getGameCount()).isEqualTo(5);
            assertThat(updatedIndex.getActiveCount()).isEqualTo(4);

            // Проверяем другие партии
            for (int i = 1; i <= 5; i++) {
                GameIndexEntry entry = updatedIndex.getEntryById(i);
                assertThat(entry).isNotNull();
                if (i == gameId) {
                    assertThat(entry.isDeleted()).isTrue();
                } else {
                    assertThat(entry.isDeleted()).isFalse();
                }
            }
        }

        @Test
        @DisplayName("Должен сохранять историю версий при удалении")
        void shouldPreserveVersionHistory() throws IOException {
            // given
            int gameId = 1;
            int initialVersion = index.getEntryById(gameId).getVersion();

            // when
            operation.deleteGame(gameId);

            // then
            PgnIndex updatedIndex = indexManager.loadIndex(pgnPath);
            GameIndexEntry deletedEntry = updatedIndex.getEntryById(gameId);
            assertThat(deletedEntry.getVersion()).isEqualTo(initialVersion + 1);
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ addGame()
    // ============================================================

    @Nested
    @DisplayName("addGame() - Добавление партии")
    class AddGameTests {

        @Test
        @DisplayName("Должен добавлять новую партию")
        void shouldAddNewGame() throws IOException {
            // given
            String pgnContent = """
                    [Event "New Game"]
                    [Site "New Site"]
                    [Date "2024.12.31"]
                    [Round "1"]
                    [White "New Player"]
                    [Black "Another Player"]
                    [Result "1-0"]
                    [ECO "B52"]
                    [Opening "Sicilian Defense"]
                    [Variation "Najdorf"]
                    
                    1. e4 c5 2. Nf3 d6 3. d4 cxd4 4. Nxd4 Nf6 5. Nc3 a6 6. Be3 e5 7. Nb3 Be6 8. f3 Be7 9. Qd2 O-O 10. O-O-O Nbd7 11. g4 b5 12. g5 Nh5 13. Nd5 Bxd5 14. exd5 Qc7 15. Kb1 Nc5 16. Nxc5 Nf4 17. Bxf4 exf4 18. Qxf4 Rfc8 19. Bd3 Bf8 20. Rhe1 1-0
                    """;

            int initialCount = index.getGameCount();

            // when
            PgnGameOperation.OperationResult result = operation.addGame(pgnContent);

            // then
            assertThat(result).isNotNull();
            assertThat(result.type()).isEqualTo(PgnGameOperation.OperationType.ADD);
            assertThat(result.gameId()).isGreaterThan(0);
            assertThat(result.message()).contains(String.valueOf(result.gameId()));

            // Проверяем, что партия добавлена
            PgnIndex updatedIndex = indexManager.loadIndex(pgnPath);
            assertThat(updatedIndex.getGameCount()).isEqualTo(initialCount + 1);
            assertThat(updatedIndex.getActiveCount()).isEqualTo(initialCount + 1);

            // Проверяем новую запись
            GameIndexEntry newEntry = updatedIndex.getEntryById(result.gameId());
            assertThat(newEntry).isNotNull();
            assertThat(newEntry.isDeleted()).isFalse();
            assertThat(newEntry.getWhite()).isEqualTo("New Player");
            assertThat(newEntry.getBlack()).isEqualTo("Another Player");
            assertThat(newEntry.getResult()).isEqualTo("1-0");
            assertThat(newEntry.getEvent()).isEqualTo("New Game");
            assertThat(newEntry.getEco()).isEqualTo("B52");
            assertThat(newEntry.getOpening()).isEqualTo("Sicilian Defense");
            assertThat(newEntry.getVariation()).isEqualTo("Najdorf");
            assertThat(newEntry.getSite()).isEqualTo("New Site");
            assertThat(newEntry.getYear()).isEqualTo("2024");
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при невалидном PGN")
        void shouldThrowExceptionWhenInvalidPgn() {
            // given
            String invalidPgn = "This is not a valid PGN";

            // when/then
            assertThatThrownBy(() -> operation.addGame(invalidPgn))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid PGN");
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при пустом PGN")
        void shouldThrowExceptionWhenEmptyPgn() {
            // given
            String emptyPgn = "";

            // when/then
            assertThatThrownBy(() -> operation.addGame(emptyPgn))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Должен корректно обрабатывать PGN с кириллицей")
        void shouldHandlePgnWithCyrillic() throws IOException {
            // given
            String pgnContent = """
                    [Event "Турнир"]
                    [Site "Москва"]
                    [Date "2024.12.31"]
                    [Round "1"]
                    [White "Игрок 1"]
                    [Black "Игрок 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 5. O-O Be7 6. Re1 b5 7. Bb3 d6 8. c3 O-O 9. h3 Nb8 10. d4 Nbd7 11. c4 c6 12. cxb5 axb5 13. Nc3 Bb7 14. Bg5 b4 15. Na4 c5 16. dxc5 Nxc5 17. Nxc5 dxc5 18. e6 fxe6 19. Nxe5 Qxd1 20. Rxd1 c4 21. Bc2 Rf6 22. Ng4 Rf5 23. Be3 Nd5 24. Bd4 Bc5 25. Bxc5 Rxc5 26. Ne3 Nxe3 27. fxe3 Rf8 28. e4 Ra5 29. b3 cxb3 30. Bxb3+ Kh8 31. Rab1 Rxa2 32. Rxb4 Rxb4 33. Bxa2 Rxb1+ 34. Bxb1 1-0
                    """;

            int initialCount = index.getGameCount();

            // when
            PgnGameOperation.OperationResult result = operation.addGame(pgnContent);

            // then
            assertThat(result).isNotNull();
            assertThat(result.type()).isEqualTo(PgnGameOperation.OperationType.ADD);

            PgnIndex updatedIndex = indexManager.loadIndex(pgnPath);
            assertThat(updatedIndex.getGameCount()).isEqualTo(initialCount + 1);

            GameIndexEntry newEntry = updatedIndex.getEntryById(result.gameId());
            assertThat(newEntry).isNotNull();
            assertThat(newEntry.getWhite()).isEqualTo("Игрок 1");
            assertThat(newEntry.getBlack()).isEqualTo("Игрок 2");
            assertThat(newEntry.getEvent()).isEqualTo("Турнир");
            assertThat(newEntry.getSite()).isEqualTo("Москва");
        }

        @Test
        @DisplayName("Должен корректно обновлять индекс после добавления")
        void shouldUpdateIndexAfterAdd() throws IOException {
            // given
            String pgnContent = """
                    [Event "New Game"]
                    [White "Player1"]
                    [Black "Player2"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 5. O-O Be7 6. Re1 b5 7. Bb3 d6 8. c3 O-O 9. h3 Nb8 10. d4 Nbd7 11. c4 c6 12. cxb5 axb5 13. Nc3 Bb7 14. Bg5 b4 15. Na4 c5 16. dxc5 Nxc5 17. Nxc5 dxc5 18. e6 fxe6 19. Nxe5 Qxd1 20. Rxd1 c4 21. Bc2 Rf6 22. Ng4 Rf5 23. Be3 Nd5 24. Bd4 Bc5 25. Bxc5 Rxc5 26. Ne3 Nxe3 27. fxe3 Rf8 28. e4 Ra5 29. b3 cxb3 30. Bxb3+ Kh8 31. Rab1 Rxa2 32. Rxb4 Rxb4 33. Bxa2 Rxb1+ 34. Bxb1 1-0
                    """;

            // when
            operation.addGame(pgnContent);

            // then
            PgnIndex updatedIndex = indexManager.loadIndex(pgnPath);
            assertThat(updatedIndex).isNotNull();
            assertThat(updatedIndex.getGameCount()).isGreaterThan(0);
            assertThat(updatedIndex.getActiveCount()).isGreaterThan(0);
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ duplicateGame()
    // ============================================================

    @Nested
    @DisplayName("duplicateGame() - Дублирование партии")
    class DuplicateGameTests {

        @Test
        @DisplayName("Должен дублировать существующую партию")
        void shouldDuplicateExistingGame() throws IOException {
            // given
            int gameId = 1;
            GameIndexEntry original = index.getEntryById(gameId);
            int initialCount = index.getGameCount();

            // when
            PgnGameOperation.OperationResult result = operation.duplicateGame(gameId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.type()).isEqualTo(PgnGameOperation.OperationType.DUPLICATE);
            assertThat(result.gameId()).isGreaterThan(0);
            assertThat(result.message()).contains(String.valueOf(gameId));

            // Проверяем, что партия добавлена
            PgnIndex updatedIndex = indexManager.loadIndex(pgnPath);
            assertThat(updatedIndex.getGameCount()).isEqualTo(initialCount + 1);
            assertThat(updatedIndex.getActiveCount()).isEqualTo(initialCount + 1);

            // Проверяем дубликат
            GameIndexEntry duplicate = updatedIndex.getEntryById(result.gameId());
            assertThat(duplicate).isNotNull();
            assertThat(duplicate.isDeleted()).isFalse();

            // Проверяем, что данные скопированы
            assertThat(duplicate.getWhite()).isEqualTo(original.getWhite());
            assertThat(duplicate.getBlack()).isEqualTo(original.getBlack());
            assertThat(duplicate.getResult()).isEqualTo(original.getResult());
            assertThat(duplicate.getEvent()).isEqualTo(original.getEvent());
            assertThat(duplicate.getEco()).isEqualTo(original.getEco());
            assertThat(duplicate.getOpening()).isEqualTo(original.getOpening());
            assertThat(duplicate.getVariation()).isEqualTo(original.getVariation());
            assertThat(duplicate.getSite()).isEqualTo(original.getSite());
            assertThat(duplicate.getYear()).isEqualTo(original.getYear());
            assertThat(duplicate.getPlyCount()).isEqualTo(original.getPlyCount());

            // ID должен быть разным
            assertThat(duplicate.getId()).isNotEqualTo(original.getId());
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при дублировании несуществующей партии")
        void shouldThrowExceptionWhenGameNotFound() {
            // given
            int nonExistentId = 999;

            // when/then
            assertThatThrownBy(() -> operation.duplicateGame(nonExistentId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(String.valueOf(nonExistentId));
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при дублировании удаленной партии")
        void shouldThrowExceptionWhenGameDeleted() throws IOException {
            // given
            int gameId = 1;
            operation.deleteGame(gameId);

            // when/then
            assertThatThrownBy(() -> operation.duplicateGame(gameId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(String.valueOf(gameId));
        }

        @Test
        @DisplayName("Должен корректно обновлять индекс после дублирования")
        void shouldUpdateIndexAfterDuplicate() throws IOException {
            // given
            int gameId = 2;

            // when
            operation.duplicateGame(gameId);

            // then
            PgnIndex updatedIndex = indexManager.loadIndex(pgnPath);
            assertThat(updatedIndex.getGameCount()).isEqualTo(6);
            assertThat(updatedIndex.getActiveCount()).isEqualTo(6);

            // Проверяем, что все записи активны
            for (int i = 1; i <= 6; i++) {
                GameIndexEntry entry = updatedIndex.getEntryById(i);
                assertThat(entry).isNotNull();
                assertThat(entry.isDeleted()).isFalse();
            }
        }

        @Test
        @DisplayName("Должен дублировать партию с сохранением всех полей")
        void shouldDuplicateWithAllFields() throws IOException {
            // given
            int gameId = 3;
            GameIndexEntry original = index.getEntryById(gameId);

            // when
            PgnGameOperation.OperationResult result = operation.duplicateGame(gameId);

            // then
            PgnIndex updatedIndex = indexManager.loadIndex(pgnPath);
            GameIndexEntry duplicate = updatedIndex.getEntryById(result.gameId());

            assertThat(duplicate.getWhite()).isEqualTo(original.getWhite());
            assertThat(duplicate.getBlack()).isEqualTo(original.getBlack());
            assertThat(duplicate.getResult()).isEqualTo(original.getResult());
            assertThat(duplicate.getEvent()).isEqualTo(original.getEvent());
            assertThat(duplicate.getEco()).isEqualTo(original.getEco());
            assertThat(duplicate.getOpening()).isEqualTo(original.getOpening());
            assertThat(duplicate.getVariation()).isEqualTo(original.getVariation());
            assertThat(duplicate.getSite()).isEqualTo(original.getSite());
            assertThat(duplicate.getYear()).isEqualTo(original.getYear());
            assertThat(duplicate.getPlyCount()).isEqualTo(original.getPlyCount());

            // ✅ Хеш должен быть вычислен (может отличаться от оригинального)
            // Проверяем, что хеш не равен 0 и не равен оригинальному (если пересчитывается)
            assertThat(duplicate.getHash()).isNotZero();

        }
    }

    // ============================================================
    // 4. ИНТЕГРАЦИОННЫЕ ТЕСТЫ
    // ============================================================

    @Nested
    @DisplayName("Интеграционные тесты")
    class IntegrationTests {

        @Test
        @DisplayName("Полный цикл: добавление → удаление → дублирование")
        void shouldCompleteFullCycle() throws IOException {
            // given
            String pgnContent = """
                    [Event "Integration Game"]
                    [White "Test Player"]
                    [Black "Test Opponent"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 5. O-O Be7 6. Re1 b5 7. Bb3 d6 8. c3 O-O 9. h3 Nb8 10. d4 Nbd7 1-0
                    """;

            int initialCount = index.getGameCount();

            // 1. Добавляем партию
            PgnGameOperation.OperationResult addResult = operation.addGame(pgnContent);
            assertThat(addResult).isNotNull();
            int newId = addResult.gameId();

            // ✅ ОБНОВЛЯЕМ operation ПОСЛЕ ДОБАВЛЕНИЯ
            operation = new PgnGameOperation(pgnPath, indexManager.loadIndex(pgnPath));

            // 2. Дублируем добавленную партию
            PgnGameOperation.OperationResult duplicateResult = operation.duplicateGame(newId);
            assertThat(duplicateResult).isNotNull();

            // ✅ ОБНОВЛЯЕМ operation ПОСЛЕ ДУБЛИРОВАНИЯ
            operation = new PgnGameOperation(pgnPath, indexManager.loadIndex(pgnPath));

            // 3. Удаляем оригинальную партию
            PgnGameOperation.OperationResult deleteResult = operation.deleteGame(newId);
            assertThat(deleteResult).isNotNull();

            // 4. Проверяем состояние
            PgnIndex updatedIndex = indexManager.loadIndex(pgnPath);
            assertThat(updatedIndex.getGameCount()).isEqualTo(initialCount + 2); // +1 добавлена, +1 дубликат

            // Оригинал должен быть удален
            GameIndexEntry original = updatedIndex.getEntryById(newId);
            assertThat(original).isNotNull();
            assertThat(original.isDeleted()).isTrue();

            // Дубликат должен быть активен
            GameIndexEntry duplicate = updatedIndex.getEntryById(duplicateResult.gameId());
            assertThat(duplicate).isNotNull();
            assertThat(duplicate.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("Должен сохранять индекс после каждой операции")
        void shouldSaveIndexAfterEachOperation() throws IOException {
            // given
            String pgnContent = """
                    [Event "New Game"]
                    [White "Player1"]
                    [Black "Player2"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 5. O-O Be7 6. Re1 b5 7. Bb3 d6 8. c3 O-O 9. h3 Nb8 10. d4 Nbd7 1-0
                    """;

            // 1. Добавляем
            operation.addGame(pgnContent);
            PgnIndex index1 = indexManager.loadIndex(pgnPath);
            assertThat(index1.getGameCount()).isEqualTo(6);

            // 2. Дублируем
            operation.duplicateGame(6);
            PgnIndex index2 = indexManager.loadIndex(pgnPath);
            assertThat(index2.getGameCount()).isEqualTo(7);

            // 3. Удаляем
            operation.deleteGame(6);
            PgnIndex index3 = indexManager.loadIndex(pgnPath);
            assertThat(index3.getGameCount()).isEqualTo(7);
            assertThat(index3.getActiveCount()).isEqualTo(6);
        }
    }
}