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

package Khrypach.Andrey.chess.kletka.database.parser;

import Khrypach.Andrey.chess.kletka.database.exception.PgnParseException;
import Khrypach.Andrey.chess.kletka.database.model.GameData;
import Khrypach.Andrey.chess.kletka.database.model.GameTree;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PgnParser - Главный парсер PGN")
class PgnParserTest {

    private PgnParser parser;
    private final LanguageManager lang = LanguageManager.getInstance();

    @BeforeEach
    void setUp() {
        parser = new PgnParser();
    }

    // ============================================================
    // 1. ТЕСТЫ parse() - ПРОСТАЯ ПАРТИЯ
    // ============================================================

    @Nested
    @DisplayName("parse() - Парсинг одной партии")
    class ParseSingleGameTests {

        @Test
        @DisplayName("Должен парсить простую партию с матом")
        void shouldParseSimpleGameWithMate() throws PgnParseException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [Site "?"]
                    [Date "2026-07-14"]
                    [Round "?"]
                    [White "Player Тест 9"]
                    [Black "Player Тест 9"]
                    [Result "1-0"]
                    [ECO "C46"]
                    [WhiteElo "1700"]
                    [BlackElo "1700"]
                    [Opening "Three Knights Opening"]
                    [Annotator "Хрипач"]
                    [PlyCount "15"]
                    [EventDate "2026.??.??"]
                    
                    1. e4 e5 2. Nf3 Nc6 3. Nc3 d6 4. Bc4 Bg4 5. h3 Bh5 6. Nxe5 Bxd1 7. Bxf7+ Ke7 8. Nd5# 1-0
                    """;

            // when
            GameData gameData = parser.parse(pgn);

            // then
            assertThat(gameData).isNotNull();
            assertThat(gameData.whitePlayer()).isEqualTo("Player Тест 9");
            assertThat(gameData.blackPlayer()).isEqualTo("Player Тест 9");
            assertThat(gameData.result()).isEqualTo("1-0");
            assertThat(gameData.eco()).isEqualTo("C46");
            assertThat(gameData.opening()).isEqualTo("Three Knights Opening");
            assertThat(gameData.annotator()).isEqualTo("Хрипач");
            assertThat(gameData.pgn()).contains("e4", "Nf3", "Nc3", "Nd5#");
        }

        @Test
        @DisplayName("Должен парсить партию с результатом *")
        void shouldParseGameWithUnfinishedResult() throws PgnParseException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "*"]
                    
                    1. e4 e5 2. Nf3 Nc6 *
                    """;

            // when
            GameData gameData = parser.parse(pgn);

            // then
            assertThat(gameData).isNotNull();
            assertThat(gameData.result()).isEqualTo("*");
            assertThat(gameData.pgn()).endsWith("*");
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при пустом PGN")
        void shouldThrowExceptionForEmptyPgn() {
            // when/then
            assertThatThrownBy(() -> parser.parse(""))
                    .isInstanceOf(PgnParseException.class)
                    .hasMessageContaining(lang.get(LanguageKeys.IMPORT_ERROR_PGN_EMPTY));
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при null PGN")
        void shouldThrowExceptionForNullPgn() {
            // when/then
            assertThatThrownBy(() -> parser.parse(null))
                    .isInstanceOf(PgnParseException.class)
                    .hasMessageContaining(lang.get(LanguageKeys.IMPORT_ERROR_PGN_EMPTY));
        }
    }

    // ============================================================
    // 2. ТЕСТЫ parse() - ПАРТИЯ С ВАРИАНТАМИ
    // ============================================================

    @Nested
    @DisplayName("parse() - Парсинг вариантов")
    class ParseVariationsTests {

        @Test
        @DisplayName("Должен парсить партию с одним вариантом")
        void shouldParseGameWithSingleVariation() throws PgnParseException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player тест 3"]
                    [Black "Player Тест 3"]
                    [Result "*"]
                    [ECO "C54"]
                    
                    1. e4 (1. d4 d5 2. c4 dxc4 3. e3 b5 4. a4 c6 5. axb5 cxb5 6. Qf3) 1... e5 2. Nf3 Nc6 3. Bc4 Bc5 4. c3 Nf6 5. d3 d6 6. O-O O-O *
                    """;

            // when
            GameData gameData = parser.parse(pgn);

            // then
            assertThat(gameData).isNotNull();
            assertThat(gameData.pgn()).contains("1. e4", "1... e5", "2. Nf3");
            assertThat(gameData.pgn()).contains("(1. d4 d5 2. c4 dxc4 3. e3 b5 4. a4 c6 5. axb5 cxb5 6. Qf3)");
        }

        @Test
        @DisplayName("Должен парсить партию с вложенными вариантами")
        void shouldParseGameWithNestedVariations() throws PgnParseException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player Тест 4"]
                    [Black "Player Тест 4"]
                    [Result "*"]
                    [ECO "C49"]
                    
                    1. e4 (1. d4 d5 2. c4 dxc4 3. e3 Nf6 (3... b5 4. a4 c6 5. axb5 cxb5 6. Qf3) (3... e5 4. Bxc4 exd4 5. exd4 Nf6 6. Nf3 Be7 7. O-O Nbd7 (7... O-O 8. Nc3 c6 9. h3 Nbd7) 8. Bxf7+ Kxf7 9. Ng5+ Kg6 10. Qd3+ Kh5 11. Qf5) 4. Bxc4 c5 5. Nf3 Bg4 6. Ne5 Bxd1 7. Bxf7#) 1... e5 2. Nf3 Nc6 3. Nc3 Nf6 4. Bb5 Bb4 5. O-O O-O 6. d3 d6 *
                    """;

            // when
            GameData gameData = parser.parse(pgn);

            // then
            assertThat(gameData).isNotNull();
            assertThat(gameData.pgn()).contains("(", ")", "1. e4", "1... e5");
            assertThat(gameData.pgn()).contains("3... b5", "3... e5", "7... O-O");
        }
    }

    // ============================================================
    // 3. ТЕСТЫ parse() - КОММЕНТАРИИ И АННОТАЦИИ
    // ============================================================

    @Nested
    @DisplayName("parse() - Комментарии и аннотации")
    class ParseCommentsAndAnnotationsTests {

        @Test
        @DisplayName("Должен парсить партию с комментариями и аннотациями")
        void shouldParseGameWithCommentsAndAnnotations() throws PgnParseException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player Комментарии"]
                    [Black "Player Коментарии"]
                    [Result "*"]
                    [ECO "C49"]
                    
                    1. e4 e5 2. Nf3 {Основной ход в позиции} Nc6 3. Nc3 (3. Bc4!? {Итальянская партия} Bc5 4. c3 Nf6 5. d3 d6 6. O-O O-O) (3. Bb5 {Испанская партия} a6 4. Bxc6?! (4. Ba4! Nf6 5. O-O Be7 6. Re1 b5 7. Bb3 O-O 8. c3 d6 (8... d5!? {Контра-атака Маршалла}) 9. h3 Na5 10. Bc2 c5 11. d4 Qc7 {Вариант Чигорина})) 3... Nf6 {Дебют 4-х коней} 4. Bb5 Bb4 5. O-O O-O 6. d3 d6 *
                    """;

            // when
            GameData gameData = parser.parse(pgn);

            // then
            assertThat(gameData).isNotNull();
            assertThat(gameData.pgn()).contains("{Основной ход в позиции}");
            assertThat(gameData.pgn()).contains("{Итальянская партия}");
            assertThat(gameData.pgn()).contains("{Испанская партия}");
            assertThat(gameData.pgn()).contains("{Контра-атака Маршалла}");
            assertThat(gameData.pgn()).contains("{Вариант Чигорина}");
            assertThat(gameData.pgn()).contains("4. Bxc6?!", "4. Ba4!", "8... d5!?");
        }

        @Test
        @DisplayName("Должен парсить партию с оценками позиции")
        void shouldParseGameWithPositionEvaluations() throws PgnParseException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player Оценка"]
                    [Black "Player Оценка"]
                    [Result "*"]
                    [ECO "C54"]
                    
                    1. e4 e5 2. Nf3 Nc6 (2... f5 3. exf5 g6 4. fxg6 Nh6 5. gxh7 Rg8 6. hxg8=Q =) 3. Bc4 Bc5 4. c3 Nf6 5. d3 d6 6. O-O O-O *
                    """;

            // when
            GameData gameData = parser.parse(pgn);

            // then
            assertThat(gameData).isNotNull();
            // Проверяем, что партия спарсилась без ошибок
            assertThat(gameData.pgn()).contains("1. e4 e5 2. Nf3 Nc6");
        }
    }

    // ============================================================
    // 4. ТЕСТЫ parse() - РАЗНЫЕ ТИПЫ ХОДОВ
    // ============================================================

    @Nested
    @DisplayName("parse() - Разные типы ходов")
    class ParseDifferentMoveTypesTests {

        @Test
        @DisplayName("Должен парсить партию с рокировкой и превращением")
        void shouldParseGameWithCastlingAndPromotion() throws PgnParseException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Игрок"]
                    [Black "Игрок"]
                    [Result "*"]
                    
                    1. d4 d5 2. c4 e6 3. Nc3 Nf6 4. Bg5 Nbd7 5. cxd5 exd5 6. e3 Be7 7. Nf3 O-O 8. Bd3 c6 9. Qc2 Re8 10. O-O Nf8 11. Rab1 Ng6 12. b4 a6 13. a4 Ne4 14. Bxe7 Qxe7 15. Bxe4 dxe4 16. Nd2 Bf5 17. Rfc1 Rad8 18. b5 axb5 19. axb5 Rd6 20. Qa4 Red8 21. Nc4 R6d7 *
                    """;

            // when
            GameData gameData = parser.parse(pgn);

            // then
            assertThat(gameData).isNotNull();
            assertThat(gameData.pgn()).contains("O-O", "O-O", "axb5", "dxe4");
            assertThat(gameData.pgn()).contains("7. Nf3 O-O", "10. O-O");
        }

        @Test
        @DisplayName("Должен парсить партию с превращением пешки")
        void shouldParseGameWithPromotion() throws PgnParseException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player Promotion"]
                    [Black "Player Promotion"]
                    [Result "*"]
                    
                    1. e4 c5 2. Nf3 d6 3. d4 cxd4 4. Nxd4 Nf6 5. Nc3 a6 6. f3 e6 7. Be3 Be7 8. Qd2 O-O 9. O-O-O d5 10. exd5 Bd7 11. dxe6 Re8 12. exd7 Nc6 13. dxe8=Q+ *
                    """;

            // when
            GameData gameData = parser.parse(pgn);

            // then
            assertThat(gameData).isNotNull();
            assertThat(gameData.pgn()).contains("dxe8=Q+");
            assertThat(gameData.pgn()).contains("O-O", "O-O-O");
        }
    }

    // ============================================================
    // 5. ТЕСТЫ parse() - ОБРАБОТКА ОШИБОК
    // ============================================================

    @Nested
    @DisplayName("parse() - Обработка ошибок")
    class ParseErrorHandlingTests {

        @Test
        @DisplayName("Должен обрабатывать незакрытый комментарий без ошибок")
        void shouldHandleUnclosedComment() throws PgnParseException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player"]
                    [Black "Player"]
                    [Result "*"]
                    
                    1. e4 e5 2. Nf3 {Основной ход в позиции Nc6 3. Nc3 (3. Bc4!? {Итальянская партия Bc5 4. c3 Nf6 5. d3 d6 6. O-O O-O) (3. Bb5 {Испанская партия a6 4. Bxc6?! (4. Ba4! Nf6 5. O-O Be7 6. Re1 b5 7. Bb3 O-O 8. c3 d6 (8... d5!? {Контра-атака Маршалла 9. exd5 Nxd5 10. Nxe5 Nxe5 11. Rxe5 c6 12. d4 Bd6) 9. h3 Na5 10. Bc2 c5 11. d4 Qc7 {Вариант Чигорина) dxc6 5. O-O f6 6. Nc3 Bc5) 3... Nf6 {Дебют 4-х коней 4. Bb5 Bb4 5. O-O O-O 6. d3 d6 *
                    """;

            // when
            GameData gameData = parser.parse(pgn);

            // then
            assertThat(gameData).isNotNull();
            // Парсер должен справиться с незакрытым комментарием
            assertThat(gameData.pgn()).contains("e4", "Nf3");
        }

        @Test
        @DisplayName("Должен обрабатывать незакрытый вариант без ошибок")
        void shouldHandleUnclosedVariation() throws PgnParseException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player"]
                    [Black "Player"]
                    [Result "*"]
                    
                    1. e4 e5 2. Nf3 Nc6 (2... f5 3. exf5 g6 4. fxg6 Nh6 5. gxh7 Rg8 6. hxg8=Q = 3. Bc4 Bc5 4. c3 Nf6 5. d3 d6 6. O-O O-O *
                    """;

            // when
            GameData gameData = parser.parse(pgn);

            // then
            assertThat(gameData).isNotNull();
            // Парсер должен справиться с незакрытым вариантом
        }
    }

    // ============================================================
    // 6. ТЕСТЫ parseMultiple()
    // ============================================================

    @Nested
    @DisplayName("parseMultiple() - Парсинг нескольких партий")
    class ParseMultipleGamesTests {

        @Test
        @DisplayName("Должен парсить несколько партий из одного PGN")
        void shouldParseMultipleGames() throws PgnParseException {
            // given
            String pgn = """
                    [Event "Game 1"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 5. O-O 1-0
                    
                    [Event "Game 2"]
                    [White "Player 3"]
                    [Black "Player 4"]
                    [Result "0-1"]
                    
                    1. d4 d5 2. c4 e6 3. Nc3 Nf6 4. Bg5 Nbd7 5. cxd5 exd5 6. e3 Be7 7. Nf3 O-O 8. Bd3 c6 0-1
                    """;

            // when
            List<GameData> games = parser.parseMultiple(pgn);

            // then
            assertThat(games).hasSize(2);
            assertThat(games.get(0).whitePlayer()).isEqualTo("Player 1");
            assertThat(games.get(0).result()).isEqualTo("1-0");
            assertThat(games.get(1).whitePlayer()).isEqualTo("Player 3");
            assertThat(games.get(1).result()).isEqualTo("0-1");
        }

        @Test
        @DisplayName("Должен возвращать пустой список для пустого PGN")
        void shouldReturnEmptyListForEmptyPgn() throws PgnParseException {
            // when
            List<GameData> games = parser.parseMultiple("");

            // then
            assertThat(games).isEmpty();
        }

        @Test
        @DisplayName("Должен возвращать пустой список для null")
        void shouldReturnEmptyListForNull() throws PgnParseException {
            // when
            List<GameData> games = parser.parseMultiple(null);

            // then
            assertThat(games).isEmpty();
        }
    }

    // ============================================================
    // 7. ТЕСТЫ extractGamesFromPgn()
    // ============================================================

    @Nested
    @DisplayName("extractGamesFromPgn() - Извлечение партий")
    class ExtractGamesTests {

        @Test
        @DisplayName("Должен извлекать партии по результатам")
        void shouldExtractGamesByResults() {
            // given
            String pgn = """
                    [Event "Game 1"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    [Event "Game 2"]
                    [White "Player 3"]
                    [Black "Player 4"]
                    [Result "0-1"]
                    
                    1. d4 d5 2. c4 e6 0-1
                    """;

            // when
            List<String> games = parser.extractGamesFromPgn(pgn);

            // then
            assertThat(games).hasSize(2);
            assertThat(games.get(0)).contains("Game 1");
            assertThat(games.get(1)).contains("Game 2");
        }
    }

    // ============================================================
    // 8. ТЕСТЫ parseToGameTree()
    // ============================================================

    @Nested
    @DisplayName("parseToGameTree() - Парсинг в дерево")
    class ParseToGameTreeTests {

        @Test
        @DisplayName("Должен парсить PGN в GameTree с вариантами")
        void shouldParseToGameTreeWithVariations() throws PgnParseException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player"]
                    [Black "Player"]
                    [Result "*"]
                    
                    1. e4 (1. d4 d5) 1... e5 2. Nf3 Nc6 *
                    """;

            // when
            GameTree gameTree = parser.parseToGameTree(pgn);

            // then
            assertThat(gameTree).isNotNull();
            assertThat(gameTree.getRootNode()).isNotNull();
            assertThat(gameTree.getMainLine()).isNotNull();
            assertThat(gameTree.getRootVariation()).isNotNull();
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при пустом PGN для GameTree")
        void shouldThrowExceptionForEmptyPgnInGameTree() {
            // when/then
            assertThatThrownBy(() -> parser.parseToGameTree(""))
                    .isInstanceOf(PgnParseException.class)
                    .hasMessageContaining(lang.get(LanguageKeys.IMPORT_ERROR_PGN_EMPTY));
        }
    }

    // ============================================================
    // 9. ТЕСТЫ isPositionPgn() и detectContentType()
    // ============================================================

    @Nested
    @DisplayName("isPositionPgn() и detectContentType()")
    class PositionDetectionTests {

        @Test
        @DisplayName("Должен определять позицию по тегам FEN и SetUp")
        void shouldDetectPositionByFenAndSetUp() {
            // given
            String pgn = """
                    [Event "Position"]
                    [SetUp "1"]
                    [FEN "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"]
                    """;

            // when
            boolean isPosition = parser.isPositionPgn(pgn);
            String contentType = parser.detectContentType(pgn);

            // then
            assertThat(isPosition).isTrue();
            assertThat(contentType).isEqualTo(lang.get(LanguageKeys.GAME_TYPE_POSITION));
        }

        @Test
        @DisplayName("Должен определять обычную партию")
        void shouldDetectGame() {
            // given
            String whitePlayer = lang.get(LanguageKeys.DEFAULT_PLAYER_NAME);
            String blackPlayer = lang.get(LanguageKeys.DEFAULT_PLAYER_NAME);
            String pgn = """
                    [Event "Game"]
                    [White "%s 1"]
                    [Black "%s 2"]
                    [Result "*"]
                    
                    1. e4 e5 *
                    """.formatted(whitePlayer, blackPlayer);

            // when
            boolean isPosition = parser.isPositionPgn(pgn);
            String contentType = parser.detectContentType(pgn);

            // then
            assertThat(isPosition).isFalse();
            assertThat(contentType).isEqualTo(lang.get(LanguageKeys.GAME_TYPE_GAME));
        }
    }

    // ============================================================
    // 10. ТЕСТЫ reset()
    // ============================================================

    @Nested
    @DisplayName("reset() - Сброс состояния")
    class ResetTests {

        @Test
        @DisplayName("Должен сбрасывать состояние парсера")
        void shouldResetParserState() {
            // given
            parser.reset();

            // then
            assertThat(parser.getTokenizer()).isNotNull();
            assertThat(parser.getHeaderParser()).isNotNull();
        }
    }
}