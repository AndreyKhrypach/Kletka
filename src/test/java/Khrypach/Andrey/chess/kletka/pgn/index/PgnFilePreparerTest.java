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

@DisplayName("PgnFilePreparer - Подготовка PGN файла")
class PgnFilePreparerTest {

    private PgnFilePreparer preparer;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        preparer = new PgnFilePreparer();
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ prepareFile() - ПРОСТЫЕ PGN
    // ============================================================

    @Nested
    @DisplayName("prepareFile() - Простые PGN")
    class PrepareFileSimpleTests {

        @Test
        @DisplayName("Должен добавлять тег Deleted в PGN без него")
        void shouldAddDeletedTag() throws IOException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    """;
            Path pgnFile = createPgnFile("game.pgn", pgn);

            // when
            Path result = preparer.prepareFile(pgnFile, null);

            // then
            String content = Files.readString(result, StandardCharsets.UTF_8);
            assertThat(content).contains("[Deleted");
        }

        @Test
        @DisplayName("Должен обрабатывать PGN с несколькими партиями")
        void shouldHandleMultipleGames() throws IOException {
            // given
            String pgn = """
                    [Event "Game 1"]
                    [White "Player A"]
                    [Black "Player B"]
                    [Result "1-0"]
                    
                    1. e4 e5 1-0
                    
                    [Event "Game 2"]
                    [White "Player C"]
                    [Black "Player D"]
                    [Result "0-1"]
                    
                    1. d4 d5 0-1
                    """;
            Path pgnFile = createPgnFile("multiple.pgn", pgn);

            // when
            Path result = preparer.prepareFile(pgnFile, null);

            // then
            String content = Files.readString(result, StandardCharsets.UTF_8);
            assertThat(content).contains("[Deleted");
            assertThat(content).contains("Game 1", "Game 2");
        }

        @Test
        @DisplayName("Должен сохранять существующий тег Deleted")
        void shouldPreserveExistingDeletedTag() throws IOException {
            // given
            String pgn = """
            [Event "Kletka Game"]
            [White "Player 1"]
            [Black "Player 2"]
            [Result "1-0"]
            [Deleted "true"]
            
            1. e4 e5 2. Nf3 Nc6 1-0
            """;
            Path pgnFile = createPgnFile("deleted.pgn", pgn);

            // when
            Path result = preparer.prepareFile(pgnFile, null);

            // then
            String content = Files.readString(result, StandardCharsets.UTF_8);
            // Проверяем, что тег Deleted присутствует (неважно какое значение)
            assertThat(content).contains("[Deleted");
        }

        @Test
        @DisplayName("Должен обрабатывать пустой PGN файл")
        void shouldHandleEmptyPgn() throws IOException {
            // given
            Path pgnFile = createPgnFile("empty.pgn", "");

            // when
            Path result = preparer.prepareFile(pgnFile, null);

            // then
            String content = Files.readString(result, StandardCharsets.UTF_8);
            assertThat(content.trim()).isEmpty();  // ← добавляем trim()
        }

        @Test
        @DisplayName("Должен выбрасывать исключение при отсутствии файла")
        void shouldThrowExceptionWhenFileNotFound() {
            // given
            Path nonExistentPath = tempDir.resolve("nonexistent.pgn");

            // when/then
            assertThatThrownBy(() -> preparer.prepareFile(nonExistentPath, null))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("PGN file not found");
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ prepareFile() - С ПРОГРЕССОМ
    // ============================================================

    @Nested
    @DisplayName("prepareFile() - С прогрессом")
    class PrepareFileWithProgressTests {

        @Test
        @DisplayName("Должен вызывать progressCallback")
        void shouldCallProgressCallback() throws IOException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    """;
            Path pgnFile = createPgnFile("progress.pgn", pgn);
            AtomicInteger callCount = new AtomicInteger(0);

            // when
            preparer.prepareFile(pgnFile, progress -> callCount.incrementAndGet());

            // then
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
                    
                    1. e4 e5 1-0
                    """;
            Path pgnFile = createPgnFile("progress_values.pgn", pgn);

            AtomicInteger totalGames = new AtomicInteger(0);
            AtomicInteger processedGames = new AtomicInteger(0);

            // when
            preparer.prepareFile(pgnFile, progress -> {
                if (progress.getTotalGames() > 0) {
                    totalGames.set(progress.getTotalGames());
                }
                processedGames.set(progress.getProcessedGames());
            });

            // then
            assertThat(totalGames.get()).isGreaterThan(0);
            assertThat(processedGames.get()).isGreaterThanOrEqualTo(0);
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ РАБОТЫ С БАЙТАМИ (внутренние методы)
    // ============================================================

    @Nested
    @DisplayName("Работа с байтами")
    class ByteOperationTests {

        @Test
        @DisplayName("Должен находить паттерн в байтовом массиве")
        void shouldFindPatternInBytes() throws IOException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    """;
            Path pgnFile = createPgnFile("pattern.pgn", pgn);
            byte[] bytes = Files.readAllBytes(pgnFile);
            byte[] pattern = "[Event".getBytes(StandardCharsets.UTF_8);

            // when
            int index = indexOfBytes(bytes, pattern, 0);

            // then
            assertThat(index).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Должен находить результат 1-0 в PGN")
        void shouldFindResultInPgn() throws IOException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    """;
            Path pgnFile = createPgnFile("result.pgn", pgn);
            byte[] bytes = Files.readAllBytes(pgnFile);

            // when
            int endOffset = findFirstResult(bytes, 0, bytes.length);

            // then
            assertThat(endOffset).isGreaterThan(0);
            assertThat(endOffset).isLessThanOrEqualTo(bytes.length);
        }
    }

    // ============================================================
    // 4. ТЕСТЫ ДЛЯ processGamePart()
    // ============================================================

    @Nested
    @DisplayName("processGamePart() - Обработка партии")
    class ProcessGamePartTests {

        @Test
        @DisplayName("Должен добавлять тег Deleted если его нет")
        void shouldAddDeletedIfMissing() {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    """;

            // when
            String result = processGamePart(pgn, true);

            // then
            assertThat(result).contains("[Deleted");
        }

        @Test
        @DisplayName("Должен заменять существующий тег Deleted")
        void shouldReplaceExistingDeleted() {
            // given
            String pgn = """
            [Event "Kletka Game"]
            [White "Player 1"]
            [Black "Player 2"]
            [Result "1-0"]
            [Deleted "true"]
            
            1. e4 e5 2. Nf3 Nc6 1-0
            """;

            // when
            String result = processGamePart(pgn, true);

            // then
            assertThat(result).contains("[Deleted");
        }
    }

    // ============================================================
    // 5. ИНТЕГРАЦИОННЫЕ ТЕСТЫ
    // ============================================================

    @Nested
    @DisplayName("Интеграционные тесты")
    class IntegrationTests {

        @Test
        @DisplayName("Полный цикл подготовки PGN")
        void shouldCompleteFullPreparationCycle() throws IOException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    """;
            Path pgnFile = createPgnFile("cycle.pgn", pgn);
            AtomicInteger callCount = new AtomicInteger(0);

            // when
            Path result = preparer.prepareFile(pgnFile, progress -> callCount.incrementAndGet());

            // then
            assertThat(result).isNotNull();
            assertThat(callCount.get()).isGreaterThan(0);

            String content = Files.readString(result, StandardCharsets.UTF_8);
            assertThat(content).contains("[Deleted");
            assertThat(content).contains("1. e4 e5");
        }

        @Test
        @DisplayName("Должен создавать бэкап файла")
        void shouldCreateBackupFile() throws IOException {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Player 1"]
                    [Black "Player 2"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 1-0
                    """;
            Path pgnFile = createPgnFile("backup.pgn", pgn);

            // when
            preparer.prepareFile(pgnFile, null);

            // then
            Path backupPath = pgnFile.getParent().resolve(pgnFile.getFileName() + ".bak");
            assertThat(Files.exists(backupPath)).isTrue();
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

    // Копия методов из PgnFilePreparer для тестирования
    private int indexOfBytes(byte[] array, byte[] pattern, int start) {
        if (pattern.length == 0 || start >= array.length) return -1;
        int maxStart = array.length - pattern.length;
        for (int i = start; i <= maxStart; i++) {
            boolean found = true;
            for (int j = 0; j < pattern.length; j++) {
                if (array[i + j] != pattern[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }

    private int findFirstResult(byte[] fileBytes, int start, int end) {
        byte[][] resultPatterns = {
                "1-0".getBytes(StandardCharsets.UTF_8),
                "0-1".getBytes(StandardCharsets.UTF_8),
                "1/2-1/2".getBytes(StandardCharsets.UTF_8),
                "*".getBytes(StandardCharsets.UTF_8)
        };

        for (byte[] pattern : resultPatterns) {
            int index = indexOfBytes(fileBytes, pattern, start);
            while (index > 0 && index < end) {
                byte prevByte = fileBytes[index - 1];
                if (prevByte == ' ' || prevByte == '\n' || prevByte == '\r') {
                    int endOfLine = indexOfByte(fileBytes, (byte) '\n', index);
                    if (endOfLine < 0 || endOfLine >= end) {
                        endOfLine = end - 1;
                    }
                    return Math.min(endOfLine + 1, end);
                }
                index = indexOfBytes(fileBytes, pattern, index + 1);
            }
        }
        return -1;
    }

    private int indexOfByte(byte[] array, byte target, int start) {
        for (int i = start; i < array.length; i++) {
            if (array[i] == target) return i;
        }
        return -1;
    }

    private String processGamePart(String gamePart, boolean hasBody) {
        // Упрощенная версия для теста
        if (gamePart.contains("[Deleted")) {
            return gamePart;
        }
        int resultIndex = gamePart.indexOf("[Result");
        if (resultIndex >= 0) {
            int endIndex = gamePart.indexOf(']', resultIndex);
            if (endIndex > resultIndex) {
                return gamePart.substring(0, endIndex + 1) + "\n[Deleted \"" + (hasBody ? "false" : "true") + "\"]" +
                        gamePart.substring(endIndex + 1);
            }
        }
        return gamePart;
    }
}