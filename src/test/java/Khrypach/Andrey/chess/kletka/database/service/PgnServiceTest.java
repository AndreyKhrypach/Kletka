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

package Khrypach.Andrey.chess.kletka.database.service;

import Khrypach.Andrey.chess.kletka.database.exception.PgnException;
import Khrypach.Andrey.chess.kletka.database.model.GameData;
import Khrypach.Andrey.chess.kletka.database.repository.FileSystemRepository;
import Khrypach.Andrey.chess.kletka.database.repository.GameRepository;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PgnService - Сервис PGN")
class PgnServiceTest {

    @TempDir
    Path tempDir;

    private PgnService pgnService;
    private GameRepository repository;
    private final LanguageManager lang = LanguageManager.getInstance();

    @BeforeEach
    void setUp() throws PgnException {
        repository = new FileSystemRepository(tempDir.toString());
        pgnService = new PgnService(repository);
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ saveGame() И loadAllGames()
    // ============================================================

    @Nested
    @DisplayName("saveGame() и loadAllGames()")
    class SaveAndLoadTests {

        @Test
        @DisplayName("Должен сохранять и загружать партию")
        void shouldSaveAndLoadGame() throws PgnException {
            // given
            GameData game = createTestGameData("Player1", "Player2");

            // when
            pgnService.saveGame(game);
            List<GameData> games = pgnService.loadAllGames();

            // then
            assertThat(games).hasSize(1);
            assertThat(games.get(0).whitePlayer()).isEqualTo("Player1");
            assertThat(games.get(0).blackPlayer()).isEqualTo("Player2");
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при сохранении null")
        void shouldThrowExceptionWhenSavingNull() {
            // when/then
            assertThatThrownBy(() -> pgnService.saveGame(null))
                    .isInstanceOf(PgnException.class)
                    .hasMessageContaining(lang.get(LanguageKeys.PGN_SERVICE_ERROR_GAME_NULL));
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ getGameCount()
    // ============================================================

    @Nested
    @DisplayName("getGameCount() - Количество партий")
    class GetGameCountTests {

        @Test
        @DisplayName("Должен возвращать 0 если нет партий")
        void shouldReturnZeroWhenNoGames() throws PgnException {
            // when
            long count = pgnService.getGameCount();

            // then
            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("Должен возвращать правильное количество партий")
        void shouldReturnCorrectCount() throws PgnException {
            // given
            pgnService.saveGame(createTestGameData("Player1", "Player2"));
            pgnService.saveGame(createTestGameData("Player3", "Player4"));

            // when
            long count = pgnService.getGameCount();

            // then
            assertThat(count).isEqualTo(2);
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ importFromPgnString()
    // ============================================================

    @Nested
    @DisplayName("importFromPgnString() - Импорт из строки")
    class ImportFromPgnStringTests {

        @Test
        @DisplayName("Должен импортировать партию из PGN строки")
        void shouldImportFromPgnString() throws PgnException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    """;

            // when
            List<GameData> games = pgnService.importFromPgnString(pgn);

            // then
            assertThat(games).hasSize(1);
            assertThat(games.get(0).whitePlayer()).isEqualTo("Player 1");
            assertThat(games.get(0).blackPlayer()).isEqualTo("Player 2");
            assertThat(games.get(0).result()).isEqualTo("1-0");
        }

        @Test
        @DisplayName("Должен выбрасывать исключение для пустой строки")
        void shouldThrowExceptionForEmptyString() {
            // given
            String emptyPgn = "";

            // when/then
            assertThatThrownBy(() -> pgnService.importFromPgnString(emptyPgn))
                    .isInstanceOf(PgnException.class)
                    .hasMessageContaining(lang.get(LanguageKeys.IMPORT_ERROR_PGN_EMPTY));
        }
    }

    // ============================================================
    // 4. ТЕСТЫ ДЛЯ ПОИСКА
    // ============================================================

    @Nested
    @DisplayName("Поиск")
    class SearchTests {

        @Test
        @DisplayName("searchByPlayers() должен находить партии по игрокам")
        void shouldSearchByPlayers() throws PgnException {
            // given
            pgnService.saveGame(createTestGameData("Carlsen", "Nepomniachtchi"));
            pgnService.saveGame(createTestGameData("Carlsen", "Anand"));

            // when
            List<GameData> results = pgnService.searchByPlayers("Carlsen", null);

            // then
            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("searchByResult() должен находить партии по результату")
        void shouldSearchByResult() throws PgnException {
            // given
            pgnService.saveGame(createTestGameDataWithResult("Player1", "Player2", "1-0"));
            pgnService.saveGame(createTestGameDataWithResult("Player3", "Player4", "0-1"));

            // when
            List<GameData> results = pgnService.searchByResult("1-0");

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).result()).isEqualTo("1-0");
        }

        @Test
        @DisplayName("searchByEco() должен находить партии по ECO")
        void shouldSearchByEco() throws PgnException {
            // given
            pgnService.saveGame(createTestGameDataWithEco("Player1", "Player2", "C44"));
            pgnService.saveGame(createTestGameDataWithEco("Player3", "Player4", "D20"));

            // when
            List<GameData> results = pgnService.searchByEco("C44");

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).eco()).isEqualTo("C44");
        }

        @Test
        @DisplayName("searchByOpening() должен находить партии по дебюту")
        void shouldSearchByOpening() throws PgnException {
            // given
            pgnService.saveGame(createTestGameDataWithOpening("Player1", "Player2", "Ruy Lopez"));
            pgnService.saveGame(createTestGameDataWithOpening("Player3", "Player4", "Italian"));

            // when
            List<GameData> results = pgnService.searchByOpening("Ruy Lopez");

            // then
            assertThat(results).hasSize(1);
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

    private GameData createTestGameDataWithResult(String white, String black, String result) {
        return new GameData(
                white, black, result,
                "2800", "2750",
                "Test Event", "Test Site", "1", "1",
                LocalDate.of(2024, 1, 1),
                "C44", "Test Opening", "Test Variation",
                "Annotator", "Team White", "Team Black", "Source",
                "123", "321", "40/120", "40",
                "1. e4 e5 2. Nf3 Nc6 " + result,
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