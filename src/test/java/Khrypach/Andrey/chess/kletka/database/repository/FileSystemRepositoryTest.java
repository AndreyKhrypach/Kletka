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

package Khrypach.Andrey.chess.kletka.database.repository;

import Khrypach.Andrey.chess.kletka.database.exception.PgnException;
import Khrypach.Andrey.chess.kletka.database.model.GameData;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FileSystemRepository - Репозиторий PGN файлов")
class FileSystemRepositoryTest {

    @TempDir
    Path tempDir;

    private FileSystemRepository repository;
    private final LanguageManager lang = LanguageManager.getInstance();

    @BeforeEach
    void setUp() throws PgnException {
        repository = new FileSystemRepository(tempDir.toString());
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ save() И findById()
    // ============================================================

    @Nested
    @DisplayName("save() и findById() - Сохранение и поиск по ID")
    class SaveAndFindByIdTests {

        @Test
        @DisplayName("Должен сохранять и находить партию")
        void shouldSaveAndFindGame() throws PgnException {
            // given
            GameData game = createTestGameData("Carlsen", "Nepomniachtchi");

            // when
            repository.save(game);

            // then
            Optional<GameData> found = repository.findById("Carlsen_Nepomniachtchi_2021-12-01.pgn");
            assertThat(found).isPresent();
            assertThat(found.get().whitePlayer()).isEqualTo("Carlsen");
            assertThat(found.get().blackPlayer()).isEqualTo("Nepomniachtchi");
        }

        @Test
        @DisplayName("Должен сохранять несколько партий")
        void shouldSaveMultipleGames() throws PgnException {
            // given
            GameData game1 = createTestGameData("Player1", "Player2");
            GameData game2 = createTestGameData("Player3", "Player4");

            // when
            repository.save(game1);
            repository.save(game2);

            // then
            assertThat(repository.count()).isEqualTo(2);
        }

        @Test
        @DisplayName("Должен возвращать пустой Optional при поиске несуществующей партии")
        void shouldReturnEmptyForNonExistentGame() {
            // when
            Optional<GameData> found = repository.findById("nonexistent.pgn");

            // then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при сохранении null")
        void shouldThrowExceptionWhenSavingNull() {
            // when/then
            assertThatThrownBy(() -> repository.save(null))
                    .isInstanceOf(PgnException.class)
                    .hasMessageContaining(lang.get(LanguageKeys.REPO_ERROR_GAME_NULL));
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ findAll()
    // ============================================================

    @Nested
    @DisplayName("findAll() - Получение всех партий")
    class FindAllTests {

        @Test
        @DisplayName("Должен возвращать пустой список если нет партий")
        void shouldReturnEmptyListWhenNoGames() throws PgnException {
            // when
            List<GameData> games = repository.findAll();

            // then
            assertThat(games).isEmpty();
        }

        @Test
        @DisplayName("Должен возвращать все сохраненные партии")
        void shouldReturnAllSavedGames() throws PgnException {
            // given
            repository.save(createTestGameData("Player1", "Player2"));
            repository.save(createTestGameData("Player3", "Player4"));
            repository.save(createTestGameData("Player5", "Player6"));

            // when
            List<GameData> games = repository.findAll();

            // then
            assertThat(games).hasSize(3);
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ delete()
    // ============================================================

    @Nested
    @DisplayName("delete() - Удаление партии")
    class DeleteTests {

        @Test
        @DisplayName("Должен удалять партию по ID")
        void shouldDeleteGameById() throws PgnException {
            // given
            GameData game = createTestGameData("Player1", "Player2");
            repository.save(game);
            assertThat(repository.count()).isEqualTo(1);

            // when
            repository.delete("Player1_Player2_2021-12-01.pgn");

            // then
            assertThat(repository.count()).isEqualTo(0);
        }

        @Test
        @DisplayName("Не должен выбрасывать исключение при удалении несуществующей партии")
        void shouldNotThrowExceptionWhenDeletingNonExistentGame() throws PgnException {
            // when/then
            repository.delete("nonexistent.pgn");
            // Никаких исключений
        }
    }

    // ============================================================
    // 4. ТЕСТЫ ДЛЯ deleteAll()
    // ============================================================

    @Nested
    @DisplayName("deleteAll() - Удаление всех партий")
    class DeleteAllTests {

        @Test
        @DisplayName("Должен удалять все партии")
        void shouldDeleteAllGames() throws PgnException {
            // given
            repository.save(createTestGameData("Player1", "Player2"));
            repository.save(createTestGameData("Player3", "Player4"));
            assertThat(repository.count()).isEqualTo(2);

            // when
            repository.deleteAll();

            // then
            assertThat(repository.count()).isEqualTo(0);
        }

        @Test
        @DisplayName("Не должен выбрасывать исключение при удалении из пустого репозитория")
        void shouldNotThrowExceptionWhenDeletingFromEmptyRepository() throws PgnException {
            // when/then
            repository.deleteAll();
            assertThat(repository.count()).isEqualTo(0);
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ count()
    // ============================================================

    @Nested
    @DisplayName("count() - Количество партий")
    class CountTests {

        @Test
        @DisplayName("Должен возвращать 0 для пустого репозитория")
        void shouldReturnZeroForEmptyRepository() throws PgnException {
            // when
            long count = repository.count();

            // then
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("Должен возвращать правильное количество партий")
        void shouldReturnCorrectCount() throws PgnException {
            // given
            repository.save(createTestGameData("Player1", "Player2"));
            repository.save(createTestGameData("Player3", "Player4"));

            // when
            long count = repository.count();

            // then
            assertThat(count).isEqualTo(2);
        }
    }

    // ============================================================
    // 6. ТЕСТЫ ДЛЯ exists()
    // ============================================================

    @Nested
    @DisplayName("exists() - Проверка существования")
    class ExistsTests {

        @Test
        @DisplayName("Должен возвращать true для существующей партии")
        void shouldReturnTrueForExistingGame() throws PgnException {
            // given
            repository.save(createTestGameData("Player1", "Player2"));

            // when
            boolean exists = repository.exists("Player1_Player2_2021-12-01.pgn");

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Должен возвращать false для несуществующей партии")
        void shouldReturnFalseForNonExistentGame() throws PgnException {
            // when
            boolean exists = repository.exists("nonexistent.pgn");

            // then
            assertThat(exists).isFalse();
        }
    }

    // ============================================================
    // 7. ТЕСТЫ ДЛЯ ПОИСКА
    // ============================================================

    @Nested
    @DisplayName("Поиск партий")
    class SearchTests {

        @Test
        @DisplayName("searchByPlayers() должен находить партии по игрокам")
        void shouldSearchByPlayers() throws PgnException {
            // given
            repository.save(createTestGameData("Carlsen", "Nepomniachtchi"));
            repository.save(createTestGameData("Carlsen", "Anand"));
            repository.save(createTestGameData("Karpov", "Kasparov"));

            // when
            List<GameData> results = repository.searchByPlayers("Carlsen", null);

            // then
            assertThat(results).hasSize(2);
            assertThat(results).allMatch(g -> g.whitePlayer().contains("Carlsen"));
        }

        @Test
        @DisplayName("searchByResult() должен находить партии по результату")
        void shouldSearchByResult() throws PgnException {
            // given
            GameData game1 = new GameData(
                    "Player1", "Player2", "1-0",
                    "2800", "2750",
                    "Test Event", "Test Site", "1", "1",
                    LocalDate.of(2021, 12, 1),
                    "C44", "Test Opening", "Test Variation",
                    "Annotator", "Team White", "Team Black", "Source",
                    "123", "321", "40/120", "40",
                    "1. e4 e5 2. Nf3 Nc6 1-0",
                    "", false, "game", false
            );
            GameData game2 = new GameData(
                    "Player3", "Player4", "1-0",
                    "2800", "2750",
                    "Test Event", "Test Site", "1", "1",
                    LocalDate.of(2021, 12, 1),
                    "C44", "Test Opening", "Test Variation",
                    "Annotator", "Team White", "Team Black", "Source",
                    "123", "321", "40/120", "40",
                    "1. e4 e5 2. Nf3 Nc6 1-0",
                    "", false, "game", false
            );
            GameData game3 = new GameData(
                    "Player5", "Player6", "0-1",
                    "2800", "2750",
                    "Test Event", "Test Site", "1", "1",
                    LocalDate.of(2021, 12, 1),
                    "C44", "Test Opening", "Test Variation",
                    "Annotator", "Team White", "Team Black", "Source",
                    "123", "321", "40/120", "40",
                    "1. e4 e5 2. Nf3 Nc6 0-1",
                    "", false, "game", false
            );

            repository.save(game1);
            repository.save(game2);
            repository.save(game3);

            // Проверяем, что партии сохранились
            List<GameData> all = repository.findAll();
            assertThat(all).hasSize(3);

            // when
            List<GameData> results = repository.searchByResult("1-0");

            // then
            assertThat(results).hasSize(2);
            assertThat(results).allMatch(g -> g.result().equals("1-0"));
        }

        @Test
        @DisplayName("searchByEco() должен находить партии по ECO")
        void shouldSearchByEco() throws PgnException {
            // given
            repository.save(createTestGameDataWithEco("Player1", "Player2", "C44"));
            repository.save(createTestGameDataWithEco("Player3", "Player4", "C44"));
            repository.save(createTestGameDataWithEco("Player5", "Player6", "D20"));

            // Проверяем сохранение
            List<GameData> all = repository.findAll();
            assertThat(all).hasSize(3);

            // when
            List<GameData> results = repository.searchByEco("C44");

            // then
            assertThat(results).hasSize(2);
            assertThat(results).allMatch(g -> g.eco().equals("C44"));


        }

        @Test
        @DisplayName("searchByOpening() должен находить партии по названию дебюта")
        void shouldSearchByOpening() throws PgnException {
            // given
            repository.save(createTestGameDataWithOpening("Player1", "Player2", "Ruy Lopez"));
            repository.save(createTestGameDataWithOpening("Player3", "Player4", "Ruy Lopez"));
            repository.save(createTestGameDataWithOpening("Player5", "Player6", "Italian Game"));

            // when
            List<GameData> results = repository.searchByOpening("Ruy Lopez");

            // then
            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("searchByText() должен находить партии по тексту")
        void shouldSearchByText() throws PgnException {
            // given
            repository.save(createTestGameData("Carlsen", "Nepomniachtchi"));

            // when
            List<GameData> results = repository.searchByText("Carlsen");

            // then
            assertThat(results).hasSize(1);
        }
    }

    // ============================================================
    // 8. ТЕСТЫ ДЛЯ saveBatch()
    // ============================================================

    @Nested
    @DisplayName("saveBatch() - Пакетное сохранение")
    class SaveBatchTests {

        @Test
        @DisplayName("Должен сохранять несколько партий пакетно")
        void shouldSaveBatchOfGames() throws PgnException {
            // given
            List<GameData> games = List.of(
                    createTestGameData("Player1", "Player2"),
                    createTestGameData("Player3", "Player4"),
                    createTestGameData("Player5", "Player6")
            );

            // when
            repository.saveBatch(games, 2);

            // then
            assertThat(repository.count()).isEqualTo(3);
        }

        @Test
        @DisplayName("Не должен сохранять пустой список")
        void shouldNotSaveEmptyBatch() throws PgnException {
            // when
            repository.saveBatch(List.of(), 2);

            // then
            assertThat(repository.count()).isEqualTo(0);
        }
    }

    // ============================================================
    // 9. ТЕСТЫ ДЛЯ findPaginated()
    // ============================================================

    @Nested
    @DisplayName("findPaginated() - Пагинация")
    class FindPaginatedTests {

        @Test
        @DisplayName("Должен возвращать страницу результатов")
        void shouldReturnPaginatedResults() throws PgnException {
            // given
            for (int i = 0; i < 5; i++) {
                repository.save(createTestGameData("Player" + i, "Opponent" + i));
            }

            // when
            List<GameData> page = repository.findPaginated(0, 2);

            // then
            assertThat(page).hasSize(2);
        }

        @Test
        @DisplayName("Должен возвращать пустой список при offset больше размера")
        void shouldReturnEmptyListWhenOffsetTooLarge() throws PgnException {
            // given
            repository.save(createTestGameData("Player1", "Player2"));

            // when
            List<GameData> page = repository.findPaginated(10, 2);

            // then
            assertThat(page).isEmpty();
        }
    }

    // ============================================================
    // 10. ТЕСТЫ ДЛЯ importFromFile()
    // ============================================================

    @Nested
    @DisplayName("importFromFile() - Импорт из файла")
    class ImportFromFileTests {

        @Test
        @DisplayName("Должен импортировать партии из PGN файла")
        void shouldImportFromPgnFile() throws PgnException, IOException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    """;
            File pgnFile = tempDir.resolve("import.pgn").toFile();
            Files.writeString(pgnFile.toPath(), pgn);

            // when
            List<GameData> games = repository.importFromFile(pgnFile);

            // then
            assertThat(games).hasSize(1);
            assertThat(games.get(0).whitePlayer()).isEqualTo("Player 1");
            assertThat(games.get(0).blackPlayer()).isEqualTo("Player 2");
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при импорте из несуществующего файла")
        void shouldThrowExceptionWhenImportingNonExistentFile() {
            // given
            File nonExistentFile = new File("nonexistent.pgn");

            // when/then
            assertThatThrownBy(() -> repository.importFromFile(nonExistentFile))
                    .isInstanceOf(PgnException.class);
        }
    }

    // ============================================================
    // 11. ТЕСТЫ ДЛЯ exportToFile()
    // ============================================================

    @Nested
    @DisplayName("exportToFile() - Экспорт в файл")
    class ExportToFileTests {

        @Test
        @DisplayName("Должен экспортировать партии в файл")
        void shouldExportToFile() throws PgnException {
            // given
            GameData game = createTestGameData("Player1", "Player2");
            repository.save(game);

            File exportFile = tempDir.resolve("export.pgn").toFile();

            // when
            repository.exportToFile(List.of(game), exportFile);

            // then
            assertThat(exportFile.exists()).isTrue();
            assertThat(exportFile.length()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при экспорте пустого списка")
        void shouldThrowExceptionWhenExportingEmptyList() {
            // given
            File exportFile = tempDir.resolve("export.pgn").toFile();

            // when/then
            assertThatThrownBy(() -> repository.exportToFile(List.of(), exportFile))
                    .isInstanceOf(PgnException.class)
                    .hasMessageContaining(lang.get(LanguageKeys.REPO_ERROR_GAMES_EMPTY));
        }
    }

    // ============================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ============================================================

    private GameData createTestGameData(String white, String black) {
        String fullPgn = String.format("""
            [Event "Test Event"]
            [Site "Test Site"]
            [Date "2021-12-01"]
            [Round "1"]
            [White "%s"]
            [Black "%s"]
            [Result "1-0"]
            [ECO "C44"]
            [WhiteElo "2800"]
            [BlackElo "2750"]
            [Opening "Test Opening"]
            
            1. e4 e5 2. Nf3 Nc6 1-0
            """, white, black);

        return new GameData(
                white, black, "1-0",
                "2800", "2750",
                "Test Event", "Test Site", "1", "1",
                LocalDate.of(2021, 12, 1),
                "C44", "Test Opening", "Test Variation",
                "Annotator", "Team White", "Team Black", "Source",
                "123", "321", "40/120", "40",
                fullPgn,
                "", false, "game", false
        );
    }

    private GameData createTestGameDataWithEco(String white, String black, String eco) {
        // Создаем полный PGN со всеми заголовками
        String fullPgn = String.format("""
            [Event "Test Event"]
            [Site "Test Site"]
            [Date "2021-12-01"]
            [Round "1"]
            [White "%s"]
            [Black "%s"]
            [Result "1-0"]
            [ECO "%s"]
            [WhiteElo "2800"]
            [BlackElo "2750"]
            [Opening "Test Opening"]
            
            1. e4 e5 2. Nf3 Nc6 1-0
            """, white, black, eco);

        // Создаем GameData с полным PGN

        return new GameData(
                white, black, "1-0",
                "2800", "2750",
                "Test Event", "Test Site", "1", "1",
                LocalDate.of(2021, 12, 1),
                eco, "Test Opening", "Test Variation",
                "Annotator", "Team White", "Team Black", "Source",
                "123", "321", "40/120", "40",
                fullPgn,  // ← ПОЛНЫЙ PGN с заголовками
                "", false, "game", false
        );
    }

    private GameData createTestGameDataWithOpening(String white, String black, String opening) {
        String fullPgn = String.format("""
            [Event "Test Event"]
            [Site "Test Site"]
            [Date "2021-12-01"]
            [Round "1"]
            [White "%s"]
            [Black "C44"]
            [Result "1-0"]
            [Opening "%s"]
            [WhiteElo "2800"]
            [BlackElo "2750"]
            [Opening "%s"]
            
            1. e4 e5 2. Nf3 Nc6 1-0
            """, white, black, opening);

        return new GameData(
                white, black, "1-0",
                "2800", "2750",
                "Test Event", "Test Site", "1", "1",
                LocalDate.of(2021, 12, 1),
                "C44", opening, "Test Variation",
                "Annotator", "Team White", "Team Black", "Source",
                "123", "321", "40/120", "40",
                fullPgn,
                "", false, "game", false
        );
    }
}