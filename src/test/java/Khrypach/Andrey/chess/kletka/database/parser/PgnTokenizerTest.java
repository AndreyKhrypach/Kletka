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

import Khrypach.Andrey.chess.kletka.database.parser.enums.PgnTokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PgnTokenizer - Токенизация PGN")
class PgnTokenizerTest {

    private PgnTokenizer tokenizer;

    @BeforeEach
    void setUp() {
        tokenizer = new PgnTokenizer();
    }

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ ПУСТЫХ ВХОДНЫХ ДАННЫХ
    // ============================================================

    @Nested
    @DisplayName("Пустые входные данные")
    class EmptyInputTests {

        @Test
        @DisplayName("Должен возвращать пустой список для null")
        void shouldReturnEmptyListForNull() {
            // when
            List<PgnToken> tokens = tokenizer.tokenize(null);

            // then
            assertThat(tokens).isEmpty();
        }

        @Test
        @DisplayName("Должен возвращать пустой список для пустой строки")
        void shouldReturnEmptyListForEmptyString() {
            // when
            List<PgnToken> tokens = tokenizer.tokenize("");

            // then
            assertThat(tokens).isEmpty();
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ ЗАГОЛОВКОВ
    // ============================================================

    @Nested
    @DisplayName("Заголовки")
    class HeaderTests {

        @Test
        @DisplayName("Должен токенизировать простой заголовок")
        void shouldTokenizeSimpleHeader() {
            // given
            String pgn = "[Event \"Kletka Game\"]";

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            assertThat(tokens)
                    .extracting(PgnToken::type)
                    .containsExactly(
                            PgnTokenType.HEADER_START,
                            PgnTokenType.HEADER_KEY,
                            PgnTokenType.HEADER_VALUE,
                            PgnTokenType.HEADER_END,
                            PgnTokenType.EOF
                    );

            assertThat(tokens)
                    .extracting(PgnToken::value)
                    .containsExactly(
                            "[",
                            "Event",
                            "Kletka Game",
                            "]",
                            ""
                    );
        }

        @Test
        @DisplayName("Должен токенизировать заголовки с разными ключами")
        void shouldTokenizeVariousHeaders() {
            // given
            String pgn = "[Event \"Kletka Game\"]\n[White \"Игрок\"]\n[Black \"Соперник\"]";

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            List<String> values = tokens.stream()
                    .filter(t -> t.type() != PgnTokenType.WHITESPACE)
                    .filter(t -> t.type() != PgnTokenType.EOF)
                    .map(PgnToken::value)
                    .toList();

            assertThat(values)
                    .containsExactly(
                            "[", "Event", "Kletka Game", "]",
                            "[", "White", "Игрок", "]",
                            "[", "Black", "Соперник", "]"
                    );
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ ХОДОВ
    // ============================================================

    @Nested
    @DisplayName("Ходы")
    class MoveTests {

        @Test
        @DisplayName("Должен токенизировать простые ходы")
        void shouldTokenizeSimpleMoves() {
            // given
            String pgn = "1. e4 e5 2. Nf3 Nc6";

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            List<String> values = tokens.stream()
                    .filter(t -> t.type() != PgnTokenType.WHITESPACE)
                    .filter(t -> t.type() != PgnTokenType.EOF)
                    .map(PgnToken::value)
                    .toList();

            assertThat(values)
                    .containsExactly(
                            "1.", "e4", "e5", "2.", "Nf3", "Nc6"
                    );
        }

        @Test
        @DisplayName("Должен токенизировать ходы с взятием")
        void shouldTokenizeCaptures() {
            // given
            String pgn = "1. e4 d5 2. exd5 Qxd5";

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            List<String> values = tokens.stream()
                    .filter(t -> t.type() != PgnTokenType.WHITESPACE)
                    .filter(t -> t.type() != PgnTokenType.EOF)
                    .map(PgnToken::value)
                    .toList();

            assertThat(values)
                    .containsExactly(
                            "1.", "e4", "d5", "2.", "exd5", "Qxd5"
                    );
        }

        @Test
        @DisplayName("Должен токенизировать рокировку")
        void shouldTokenizeCastling() {
            // given
            String pgn = "1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 5. O-O";

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            List<String> values = tokens.stream()
                    .filter(t -> t.type() == PgnTokenType.MOVE)
                    .map(PgnToken::value)
                    .toList();

            assertThat(values)
                    .contains("O-O");
        }

        @Test
        @DisplayName("Должен токенизировать превращение пешки")
        void shouldTokenizePromotion() {
            // given
            String pgn = "1. e8=Q";

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            List<String> values = tokens.stream()
                    .filter(t -> t.type() == PgnTokenType.MOVE)
                    .map(PgnToken::value)
                    .toList();

            assertThat(values).contains("e8=Q");
        }

        @Test
        @DisplayName("Должен токенизировать превращение пешки с взятием")
        void shouldTokenizePromotionWithCapture() {
            // given
            String pgn = "1. dxc8=Q";  // Пешка с d7 бьет на c8 и превращается в ферзя

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            List<String> values = tokens.stream()
                    .filter(t -> t.type() == PgnTokenType.MOVE)
                    .map(PgnToken::value)
                    .toList();

            assertThat(values).contains("dxc8=Q");
        }
    }

    // ============================================================
    // 4. ТЕСТЫ ДЛЯ АННОТАЦИЙ
    // ============================================================

    @Nested
    @DisplayName("Аннотации")
    class AnnotationTests {

        @Test
        @DisplayName("Должен токенизировать аннотации ! и ?")
        void shouldTokenizeMoveAnnotations() {
            // given
            String pgn = "1. e4! e5? 2. Nf3!! Nc6??";

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            List<PgnToken> annotations = tokens.stream()
                    .filter(t -> t.type() == PgnTokenType.ANNOTATION)
                    .toList();

            assertThat(annotations)
                    .extracting(PgnToken::value)
                    .containsExactly("!", "?", "!!", "??");
        }

        @Test
        @DisplayName("Должен токенизировать аннотации оценки позиции")
        void shouldTokenizePositionAnnotations() {
            // given
            String pgn = "1. e4 e5 2. Nf3 Nc6 3. Bb5 a6  ±";

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            List<PgnToken> annotations = tokens.stream()
                    .filter(t -> t.type() == PgnTokenType.ANNOTATION)
                    .toList();

            assertThat(annotations)
                    .extracting(PgnToken::value)
                    .contains("±");
        }

        @Test
        @DisplayName("Должен токенизировать NAG аннотации")
        void shouldTokenizeNagAnnotations() {
            // given
            String pgn = "1. e4 $1 e5 $2";

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            List<PgnToken> nags = tokens.stream()
                    .filter(t -> t.type() == PgnTokenType.NAG)
                    .toList();

            assertThat(nags)
                    .extracting(PgnToken::value)
                    .containsExactly("$1", "$2");
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ КОММЕНТАРИЕВ
    // ============================================================

    @Nested
    @DisplayName("Комментарии")
    class CommentTests {

        @Test
        @DisplayName("Должен токенизировать комментарии")
        void shouldTokenizeComments() {
            // given
            String pgn = "1. e4 {Начинаем партию} e5 {Классический ответ}";

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            List<PgnToken> comments = tokens.stream()
                    .filter(t -> t.type() == PgnTokenType.COMMENT_TEXT)
                    .toList();

            assertThat(comments)
                    .extracting(PgnToken::value)
                    .containsExactly("Начинаем партию", "Классический ответ");
        }

        @Test
        @DisplayName("Должен токенизировать пустые комментарии")
        void shouldTokenizeEmptyComments() {
            // given
            String pgn = "1. e4 {} e5";

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            List<PgnToken> comments = tokens.stream()
                    .filter(t -> t.type() == PgnTokenType.COMMENT_TEXT)
                    .toList();

            assertThat(comments)
                    .extracting(PgnToken::value)
                    .containsExactly("");
        }
    }

    // ============================================================
    // 6. ТЕСТЫ ДЛЯ ВАРИАНТОВ
    // ============================================================

    @Nested
    @DisplayName("Варианты")
    class VariationTests {

        @Test
        @DisplayName("Должен токенизировать варианты")
        void shouldTokenizeVariations() {
            // given
            String pgn = "1. e4 (1. d4) e5";

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            List<PgnToken> variationTokens = tokens.stream()
                    .filter(t -> t.type() == PgnTokenType.VARIATION_START ||
                            t.type() == PgnTokenType.VARIATION_END)
                    .toList();

            assertThat(variationTokens)
                    .extracting(PgnToken::value)
                    .containsExactly("(", ")");
        }

        @Test
        @DisplayName("Должен токенизировать вложенные варианты")
        void shouldTokenizeNestedVariations() {
            // given
            String pgn = "1. e4 (1. d4 (1. d5)) e5";

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            List<PgnToken> variationTokens = tokens.stream()
                    .filter(t -> t.type() == PgnTokenType.VARIATION_START ||
                            t.type() == PgnTokenType.VARIATION_END)
                    .toList();

            assertThat(variationTokens)
                    .extracting(PgnToken::value)
                    .containsExactly("(", "(", ")", ")");
        }
    }

    // ============================================================
    // 7. ТЕСТЫ ДЛЯ РЕЗУЛЬТАТОВ
    // ============================================================

    @Nested
    @DisplayName("Результаты")
    class ResultTests {

        @Test
        @DisplayName("Должен токенизировать результат 1-0")
        void shouldTokenizeWhiteWin() {
            // given
            String pgn = "1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 1-0";

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            List<PgnToken> results = tokens.stream()
                    .filter(t -> t.type() == PgnTokenType.RESULT)
                    .toList();

            assertThat(results)
                    .extracting(PgnToken::value)
                    .containsExactly("1-0");
        }

        @Test
        @DisplayName("Должен токенизировать результат 0-1")
        void shouldTokenizeBlackWin() {
            // given
            String pgn = "1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 0-1";

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            List<PgnToken> results = tokens.stream()
                    .filter(t -> t.type() == PgnTokenType.RESULT)
                    .toList();

            assertThat(results)
                    .extracting(PgnToken::value)
                    .containsExactly("0-1");
        }

        @Test
        @DisplayName("Должен токенизировать ничью 1/2-1/2")
        void shouldTokenizeDraw() {
            // given
            String pgn = "1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 1/2-1/2";

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            List<PgnToken> results = tokens.stream()
                    .filter(t -> t.type() == PgnTokenType.RESULT)
                    .toList();

            assertThat(results)
                    .extracting(PgnToken::value)
                    .containsExactly("1/2-1/2");
        }

        @Test
        @DisplayName("Должен токенизировать незаконченную партию *")
        void shouldTokenizeUnfinished() {
            // given
            String pgn = "1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 *";

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            List<PgnToken> results = tokens.stream()
                    .filter(t -> t.type() == PgnTokenType.RESULT)
                    .toList();

            assertThat(results)
                    .extracting(PgnToken::value)
                    .containsExactly("*");
        }
    }

    // ============================================================
    // 8. ТЕСТЫ ДЛЯ ИГНОРИРУЕМЫХ ТОКЕНОВ
    // ============================================================

    @Nested
    @DisplayName("Игнорируемые токены")
    class IgnoredTokensTests {

        @Test
        @DisplayName("Должен игнорировать токен --")
        void shouldIgnoreDoubleDashToken() {
            // given
            String pgn = "1. e4 e5 2. Nf3 -- 3. Nc6";

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            List<String> values = tokens.stream()
                    .filter(t -> t.type() != PgnTokenType.WHITESPACE)
                    .filter(t -> t.type() != PgnTokenType.EOF)
                    .map(PgnToken::value)
                    .toList();

            assertThat(values)
                    .doesNotContain("--");
        }

        @Test
        @DisplayName("Должен игнорировать одиночные # и +")
        void shouldIgnoreSingleCheckMateSymbols() {
            // given
            String pgn = "1. e4+ e5 2. Nf3#";

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            List<String> values = tokens.stream()
                    .filter(t -> t.type() != PgnTokenType.WHITESPACE)
                    .filter(t -> t.type() != PgnTokenType.EOF)
                    .map(PgnToken::value)
                    .toList();

            // '+' и '#' как отдельные токены игнорируются,
            // они должны быть частью хода
            assertThat(values)
                    .contains("e4+", "Nf3#");
        }
    }

    // ============================================================
    // 9. ТЕСТЫ ДЛЯ НОМЕРОВ ХОДОВ
    // ============================================================

    @Nested
    @DisplayName("Номера ходов")
    class MoveNumberTests {

        @Test
        @DisplayName("Должен токенизировать номера ходов")
        void shouldTokenizeMoveNumbers() {
            // given
            String pgn = "1. e4 2. d4 3. Nf3";

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            List<PgnToken> moveNumbers = tokens.stream()
                    .filter(t -> t.type() == PgnTokenType.MOVE_NUMBER)
                    .toList();

            assertThat(moveNumbers)
                    .extracting(PgnToken::value)
                    .containsExactly("1.", "2.", "3.");
        }

        @Test
        @DisplayName("Должен токенизировать номера ходов с многоточием")
        void shouldTokenizeMoveNumbersWithEllipsis() {
            // given
            String pgn = "1. e4 e5 2. Nf3 Nc6 3... a6";

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            List<PgnToken> moveNumbers = tokens.stream()
                    .filter(t -> t.type() == PgnTokenType.MOVE_NUMBER_ELLIPSIS)
                    .toList();

            assertThat(moveNumbers)
                    .extracting(PgnToken::value)
                    .containsExactly("3...");
        }
    }

    // ============================================================
    // 10. ТЕСТЫ ДЛЯ ПОЛНЫХ PGN
    // ============================================================

    @Nested
    @DisplayName("Полные PGN")
    class FullPgnTests {

        @Test
        @DisplayName("Должен корректно токенизировать полную PGN партию")
        void shouldTokenizeFullPgn() {
            // given
            String pgn = """
                    [Event "Kletka Game"]
                    [White "Игрок"]
                    [Black "Соперник"]
                    [Result "1-0"]
                    
                    1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 5. O-O Be7 6. Re1 b5 7. Bb3 d6 8. c3 O-O 9. h3 Nb8 10. d4 Nbd7 11. c4 c6 12. cxb5 axb5 13. Nc3 Bb7 14. Bg5 b4 15. Nb1 h6 16. Bh4 c5 17. dxe5 Nxe5 18. Nxe5 dxe5 19. Qxd8 Rxd8 20. Rxe5 Bf6 21. Re3 g5 22. Bg3 Bxg3 23. Re8+ Rxe8 24. Rxe8+ Kg7 25. Rxb8 Bxb2 26. Bd5 Bf5 27. Nc3 bxc3 28. Bxa8 Bxa1 29. Rxb2 c2 30. Bf3 1-0
                    """;

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            // Проверяем, что есть заголовки
            assertThat(tokens)
                    .extracting(PgnToken::type)
                    .contains(PgnTokenType.HEADER_START, PgnTokenType.HEADER_END);

            // Проверяем, что есть ходы
            assertThat(tokens)
                    .extracting(PgnToken::type)
                    .contains(PgnTokenType.MOVE, PgnTokenType.MOVE_NUMBER);

            // Проверяем, что есть результат
            assertThat(tokens)
                    .extracting(PgnToken::type)
                    .contains(PgnTokenType.RESULT);
        }
    }

    // ============================================================
    // 11. ТЕСТЫ ДЛЯ РАЗЛИЧНЫХ ТОКЕНОВ
    // ============================================================

    @Nested
    @DisplayName("Различные типы токенов")
    class VariousTokenTypesTests {

        @Test
        @DisplayName("Должен обрабатывать специальные символы в заголовках")
        void shouldHandleSpecialCharactersInHeaders() {
            // given
            String pgn = """
                    [Event "Chigorin Memorial"]
                    [Site "Sochi, Russia"]
                    [Date "2024.12.25"]
                    [Round "1.2"]
                    [White "Carlsen, M."]
                    [Black "Nepomniachtchi, I."]
                    [Result "1/2-1/2"]
                    [ECO "C67"]
                    [Opening "Ruy Lopez"]
                    [Variation "Berlin Defense"]
                    """;

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            List<String> headerValues = tokens.stream()
                    .filter(t -> t.type() == PgnTokenType.HEADER_VALUE)
                    .map(PgnToken::value)
                    .toList();

            assertThat(headerValues)
                    .contains(
                            "Chigorin Memorial",
                            "Sochi, Russia",
                            "2024.12.25",
                            "1.2",
                            "Carlsen, M.",
                            "Nepomniachtchi, I.",
                            "1/2-1/2",
                            "C67",
                            "Ruy Lopez",
                            "Berlin Defense"
                    );
        }
    }

    // ============================================================
    // 12. ТЕСТЫ ДЛЯ ОБРАБОТКИ ОШИБОК
    // ============================================================

    @Nested
    @DisplayName("Обработка ошибок")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Должен обрабатывать некорректный PGN без ошибок")
        void shouldHandleInvalidPgn() {
            // given
            String pgn = "1. e4 e5 2. Nf3 Некорректный текст 3. Nc6";

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            // Не должно быть исключений, токенизатор должен обработать
            assertThat(tokens).isNotEmpty();
        }

        @Test
        @DisplayName("Должен обрабатывать PGN с незакрытыми кавычками")
        void shouldHandleUnclosedQuotes() {
            // given
            String pgn = "[Event \"Kletka Game]";

            // when
            List<PgnToken> tokens = tokenizer.tokenize(pgn);

            // then
            assertThat(tokens).isNotEmpty();
        }
    }
}