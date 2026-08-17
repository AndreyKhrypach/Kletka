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

import Khrypach.Andrey.chess.kletka.database.parser.PgnParser;
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
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PgnFileIndexer - Индексация PGN файлов")
class PgnFileIndexerTest {

    private PgnFileIndexer indexer;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        indexer = new PgnFileIndexer();
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ indexFile() - ПРОСТЫЕ PGN
    // ============================================================

    @Nested
    @DisplayName("indexFile() - Индексация простых PGN")
    class IndexFileSimpleTests {

        @Test
        @DisplayName("Должен индексировать PGN с одной партией")
        void shouldIndexSingleGame() throws IOException {
            // given
            String pgn = """
            [Event "Kletka Game"]
            [White "Player 1"]
            [Black "Player 2"]
            [Result "1-0"]
            [ECO "C44"]
            [Opening "King's Pawn Game"]
            [PlyCount "10"]
            
            1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 5. O-O 1-0
            """;
            Path pgnFile = createPgnFile("single_game.pgn", pgn);

            // when
            PgnIndex index = indexer.indexFile(pgnFile, null);

            // then
            assertThat(index).isNotNull();
            assertThat(index.getGameCount()).isEqualTo(1);
            assertThat(index.getActiveCount()).isEqualTo(1);
            assertThat(index.getEntries()).hasSize(1);

            GameIndexEntry entry = index.getEntries().get(0);
            assertThat(entry.getWhite()).isEqualTo("Player 1");
            assertThat(entry.getBlack()).isEqualTo("Player 2");
            assertThat(entry.getResult()).isEqualTo("1-0");
            assertThat(entry.getEco()).isEqualTo("C44");
            assertThat(entry.getOpening()).isEqualTo("King's Pawn Game");
            assertThat(entry.getPlyCount()).isEqualTo(10);
            assertThat(entry.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("Должен индексировать PGN с несколькими партиями")
        void shouldIndexMultipleGames() throws IOException {
            // given
            String pgn = """
                    [Event "Game 1"]
                    [White "Player A"]
                    [Black "Player B"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    
                    [Event "Game 2"]
                    [White "Player C"]
                    [Black "Player D"]
                    [Result "0-1"]
                    
                    1. d4 d5 2. c4 e6 0-1
                    
                    [Event "Game 3"]
                    [White "Player E"]
                    [Black "Player F"]
                    [Result "1/2-1/2"]
                    
                    1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 1/2-1/2
                    """;
            Path pgnFile = createPgnFile("multiple_games.pgn", pgn);

            // when
            PgnIndex index = indexer.indexFile(pgnFile, null);

            // then
            assertThat(index).isNotNull();
            assertThat(index.getGameCount()).isEqualTo(3);
            assertThat(index.getActiveCount()).isEqualTo(3);
            assertThat(index.getEntries()).hasSize(3);

            assertThat(index.getEntries())
                    .extracting(GameIndexEntry::getResult)
                    .containsExactly("1-0", "0-1", "1/2-1/2");
        }

        @Test
        @DisplayName("Должен индексировать PGN с русскими заголовками")
        void shouldIndexPgnWithRussianHeaders() throws IOException {
            // given
            String pgn = """
                    [Event "Турнир"]
                    [White "Игрок 1"]
                    [Black "Игрок 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    """;
            Path pgnFile = createPgnFile("russian.pgn", pgn);

            // when
            PgnIndex index = indexer.indexFile(pgnFile, null);

            // then
            assertThat(index).isNotNull();
            assertThat(index.getGameCount()).isEqualTo(1);

            GameIndexEntry entry = index.getEntries().get(0);
            assertThat(entry.getEvent()).isEqualTo("Турнир");
            assertThat(entry.getWhite()).isEqualTo("Игрок 1");
            assertThat(entry.getBlack()).isEqualTo("Игрок 2");
        }

        @Test
        @DisplayName("Должен индексировать PGN с позицией (FEN)")
        void shouldIndexPgnWithFen() throws IOException {
            // given
            String pgn = """
                    [Event "Position"]
                    [SetUp "1"]
                    [FEN "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"]
                    [Result "*"]
                    
                    1. e4 e5 2. Nf3 Nc6 *
                    """;
            Path pgnFile = createPgnFile("position.pgn", pgn);

            // when
            PgnIndex index = indexer.indexFile(pgnFile, null);

            // then
            assertThat(index).isNotNull();
            assertThat(index.getGameCount()).isEqualTo(1);

            GameIndexEntry entry = index.getEntries().get(0);
            assertThat(entry.isDeleted()).isFalse();
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ indexFile() - С ПРОГРЕССОМ
    // ============================================================

    @Nested
    @DisplayName("indexFile() - С прогрессом")
    class IndexFileWithProgressTests {

        @Test
        @DisplayName("Должен вызывать progressCallback при индексации")
        void shouldCallProgressCallback() throws IOException {
            // given
            String pgn = """
                    [Event "Game 1"]
                    [White "Player A"]
                    [Black "Player B"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    
                    [Event "Game 2"]
                    [White "Player C"]
                    [Black "Player D"]
                    [Result "0-1"]
                    
                    1. d4 d5 2. c4 e6 0-1
                    """;
            Path pgnFile = createPgnFile("progress.pgn", pgn);

            AtomicInteger callCount = new AtomicInteger(0);

            // when
            PgnIndex index = indexer.indexFile(pgnFile, progress -> callCount.incrementAndGet());

            // then
            assertThat(index).isNotNull();
            assertThat(callCount.get()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Должен обновлять прогресс с корректными значениями")
        void shouldUpdateProgressWithCorrectValues() throws IOException {
            // given
            String pgn = """
                    [Event "Game 1"]
                    [White "Player A"]
                    [Black "Player B"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    """;
            Path pgnFile = createPgnFile("single_progress.pgn", pgn);

            AtomicInteger totalGames = new AtomicInteger(0);
            AtomicInteger processedGames = new AtomicInteger(0);

            // when
            PgnIndex index = indexer.indexFile(pgnFile, progress -> {
                if (progress.getTotalGames() > 0) {
                    totalGames.set(progress.getTotalGames());
                }
                processedGames.set(progress.getProcessedGames());
            });

            // then
            assertThat(index).isNotNull();
            assertThat(totalGames.get()).isGreaterThan(0);
            assertThat(processedGames.get()).isGreaterThan(0);
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ indexFile() - ОБРАБОТКА ОШИБОК
    // ============================================================

    @Nested
    @DisplayName("indexFile() - Обработка ошибок")
    class IndexFileErrorTests {

        @Test
        @DisplayName("Должен выбрасывать исключение при отсутствии файла")
        void shouldThrowExceptionWhenFileNotFound() {
            // given
            Path nonExistentFile = tempDir.resolve("nonexistent.pgn");

            // when/then
            assertThatThrownBy(() -> indexer.indexFile(nonExistentFile, null))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("PGN file not found");
        }

        @Test
        @DisplayName("Должен обрабатывать пустой PGN файл")
        void shouldHandleEmptyPgnFile() throws IOException {
            // given
            Path emptyFile = createPgnFile("empty.pgn", "");

            // when
            PgnIndex index = indexer.indexFile(emptyFile, null);

            // then
            assertThat(index).isNotNull();
            assertThat(index.getGameCount()).isEqualTo(0);
            assertThat(index.getActiveCount()).isEqualTo(0);
            assertThat(index.getEntries()).isEmpty();
        }

        @Test
        @DisplayName("Должен обрабатывать PGN без заголовков")
        void shouldHandlePgnWithoutHeaders() throws IOException {
            // given
            String pgn = "1. e4 e5 2. Nf3 Nc6 1-0";
            Path pgnFile = createPgnFile("no_headers.pgn", pgn);

            // when
            PgnIndex index = indexer.indexFile(pgnFile, null);

            // then
            assertThat(index).isNotNull();
            assertThat(index.getGameCount()).isEqualTo(0);
        }
    }

    // ============================================================
    // 4. ТЕСТЫ ДЛЯ КОДИРОВКИ
    // ============================================================

    @Nested
    @DisplayName("Кодировка файлов")
    class EncodingTests {

        @Test
        @DisplayName("Должен определять UTF-8 кодировку")
        void shouldDetectUtf8Encoding() throws IOException {
            // given
            String pgn = """
                    [Event "Game"]
                    [White "Player"]
                    [Black "Player"]
                    [Result "1-0"]
                    
                    1. e4 e5 1-0
                    """;
            Path pgnFile = createPgnFile("utf8.pgn", pgn);

            // when
            PgnIndex index = indexer.indexFile(pgnFile, null);

            // then
            assertThat(index).isNotNull();
            assertThat(index.getGameCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Должен обрабатывать UTF-8 с BOM")
        void shouldHandleUtf8WithBom() throws IOException {
            // given
            String pgn = """
                    [Event "Game"]
                    [White "Player"]
                    [Black "Player"]
                    [Result "1-0"]
                    
                    1. e4 e5 1-0
                    """;
            byte[] bytes = pgn.getBytes(StandardCharsets.UTF_8);
            byte[] bomBytes = new byte[bytes.length + 3];
            bomBytes[0] = (byte) 0xEF;
            bomBytes[1] = (byte) 0xBB;
            bomBytes[2] = (byte) 0xBF;
            System.arraycopy(bytes, 0, bomBytes, 3, bytes.length);

            Path pgnFile = tempDir.resolve("bom.pgn");
            Files.write(pgnFile, bomBytes);

            // when
            PgnIndex index = indexer.indexFile(pgnFile, null);

            // then
            assertThat(index).isNotNull();
            assertThat(index.getGameCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Должен обрабатывать Windows-1251 кодировку")
        void shouldHandleWindows1251Encoding() throws IOException {
            // given
            String pgn = """
                    [Event "Игра"]
                    [White "Игрок"]
                    [Black "Игрок"]
                    [Result "1-0"]
                    
                    1. e4 e5 1-0
                    """;
            Path pgnFile = tempDir.resolve("win1251.pgn");
            Files.writeString(pgnFile, pgn, java.nio.charset.Charset.forName("Windows-1251"));

            // when
            PgnIndex index = indexer.indexFile(pgnFile, null);

            // then
            assertThat(index).isNotNull();
            assertThat(index.getGameCount()).isEqualTo(1);

            GameIndexEntry entry = index.getEntries().get(0);
            assertThat(entry.getEvent()).isEqualTo("Игра");
            assertThat(entry.getWhite()).isEqualTo("Игрок");
            assertThat(entry.getBlack()).isEqualTo("Игрок");
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ getPgnParser()
    // ============================================================

    @Nested
    @DisplayName("getPgnParser() - Получение парсера")
    class GetPgnParserTests {

        @Test
        @DisplayName("Должен возвращать не-null PgnParser")
        void shouldReturnNonNullPgnParser() {
            // when
            PgnParser parser = indexer.getPgnParser();

            // then
            assertThat(parser).isNotNull();
        }

        @Test
        @DisplayName("Должен возвращать тот же экземпляр PgnParser при повторных вызовах")
        void shouldReturnSamePgnParserInstance() {
            // when
            PgnParser parser1 = indexer.getPgnParser();
            PgnParser parser2 = indexer.getPgnParser();

            // then
            assertThat(parser1).isSameAs(parser2);
        }
    }

    // ============================================================
    // 6. ТЕСТЫ ДЛЯ БОЛЬШИХ PGN ФАЙЛОВ
    // ============================================================

    @Nested
    @DisplayName("Большие PGN файлы")
    class LargePgnTests {

        @Test
        @DisplayName("Должен индексировать PGN с 100+ партиями")
        void shouldIndexLargePgn() throws IOException {
            // given
            StringBuilder pgnBuilder = new StringBuilder();
            for (int i = 0; i < 100; i++) {
                pgnBuilder.append("[Event \"Game ").append(i).append("\"]\n");
                pgnBuilder.append("[White \"Player A\"]\n");
                pgnBuilder.append("[Black \"Player B\"]\n");
                pgnBuilder.append("[Result \"1-0\"]\n");
                pgnBuilder.append("\n");
                pgnBuilder.append("1. e4 e5 2. Nf3 Nc6 1-0\n\n");
            }
            Path pgnFile = createPgnFile("large.pgn", pgnBuilder.toString());

            // when
            PgnIndex index = indexer.indexFile(pgnFile, null);

            // then
            assertThat(index).isNotNull();
            assertThat(index.getGameCount()).isEqualTo(100);
            assertThat(index.getActiveCount()).isEqualTo(100);
            assertThat(index.getEntries()).hasSize(100);
        }
    }

    // ============================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ============================================================

    private Path createPgnFile(String fileName, String content) throws IOException {
        Path file = tempDir.resolve(fileName);
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }
}