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

package Khrypach.Andrey.chess.kletka.gui.model;

import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MoveNode - Узел шахматного хода")
class MoveNodeTest {

    // ============================================================
    // 1. ТЕСТЫ ДЛЯ СОЗДАНИЯ MOVE NODE
    // ============================================================

    @Nested
    @DisplayName("Создание MoveNode")
    class CreationTests {

        @Test
        @DisplayName("Должен создавать MoveNode с корректными параметрами")
        void shouldCreateMoveNodeWithCorrectParams() {
            // given
            Move move = new Move(Square.E2, Square.E4, Piece.WHITE_PAWN);

            // when
            MoveNode node = new MoveNode(move, Piece.WHITE_PAWN, false, null);

            // then
            assertThat(node).isNotNull();
            assertThat(node.getMove()).isEqualTo(move);
            assertThat(node.getPiece()).isEqualTo(Piece.WHITE_PAWN);
            assertThat(node.isCapture()).isFalse();
            assertThat(node.getPromotionPiece()).isNull();
            assertThat(node.isRoot()).isFalse();
        }

        @Test
        @DisplayName("Должен создавать MoveNode с взятием")
        void shouldCreateMoveNodeWithCapture() {
            // given
            Move move = new Move(Square.E4, Square.D5, Piece.WHITE_PAWN);

            // when
            MoveNode node = new MoveNode(move, Piece.WHITE_PAWN, true, null);

            // then
            assertThat(node.isCapture()).isTrue();
        }

        @Test
        @DisplayName("Должен создавать MoveNode с превращением")
        void shouldCreateMoveNodeWithPromotion() {
            // given
            Move move = new Move(Square.E7, Square.E8, Piece.WHITE_PAWN);

            // when
            MoveNode node = new MoveNode(move, Piece.WHITE_PAWN, false, Piece.WHITE_QUEEN);

            // then
            assertThat(node.getPromotionPiece()).isEqualTo(Piece.WHITE_QUEEN);
        }
    }

    // ============================================================
    // 2. ТЕСТЫ ДЛЯ isRoot()
    // ============================================================

    @Nested
    @DisplayName("isRoot() - Проверка корневого узла")
    class IsRootTests {

        @Test
        @DisplayName("Должен возвращать false для MoveNode")
        void shouldReturnFalseForMoveNode() {
            // given
            Move move = new Move(Square.E2, Square.E4, Piece.WHITE_PAWN);
            MoveNode node = new MoveNode(move, Piece.WHITE_PAWN, false, null);

            // when
            boolean result = node.isRoot();

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Должен возвращать false для любого MoveNode")
        void shouldAlwaysReturnFalse() {
            // given
            Move move1 = new Move(Square.E2, Square.E4, Piece.WHITE_PAWN);
            Move move2 = new Move(Square.D2, Square.D4, Piece.WHITE_PAWN);
            MoveNode node1 = new MoveNode(move1, Piece.WHITE_PAWN, false, null);
            MoveNode node2 = new MoveNode(move2, Piece.WHITE_PAWN, false, null);

            // then
            assertThat(node1.isRoot()).isFalse();
            assertThat(node2.isRoot()).isFalse();
        }
    }

    // ============================================================
    // 3. ТЕСТЫ ДЛЯ getUciMove()
    // ============================================================

    @Nested
    @DisplayName("getUciMove() - Генерация UCI нотации")
    class UciMoveTests {

        @Test
        @DisplayName("Должен генерировать UCI для пешечного хода")
        void shouldGenerateUciForPawnMove() {
            // given
            Move move = new Move(Square.E2, Square.E4, Piece.WHITE_PAWN);
            MoveNode node = new MoveNode(move, Piece.WHITE_PAWN, false, null);

            // when
            String uci = node.getUciMove();

            // then
            assertThat(uci).isEqualTo("e2e4");
        }

        @Test
        @DisplayName("Должен генерировать UCI для хода коня")
        void shouldGenerateUciForKnightMove() {
            // given
            Move move = new Move(Square.G1, Square.F3, Piece.WHITE_KNIGHT);
            MoveNode node = new MoveNode(move, Piece.WHITE_KNIGHT, false, null);

            // when
            String uci = node.getUciMove();

            // then
            assertThat(uci).isEqualTo("g1f3");
        }

        @Test
        @DisplayName("Должен генерировать UCI для рокировки")
        void shouldGenerateUciForCastling() {
            // given
            Move move = new Move(Square.E1, Square.G1, Piece.WHITE_KING);
            MoveNode node = new MoveNode(move, Piece.WHITE_KING, false, null);

            // when
            String uci = node.getUciMove();

            // then
            assertThat(uci).isEqualTo("e1g1");
        }

        @Test
        @DisplayName("Должен генерировать UCI для превращения в ферзя")
        void shouldGenerateUciForPromotionToQueen() {
            // given
            Move move = new Move(Square.E7, Square.E8, Piece.WHITE_PAWN);
            MoveNode node = new MoveNode(move, Piece.WHITE_PAWN, false, Piece.WHITE_QUEEN);

            // when
            String uci = node.getUciMove();

            // then
            assertThat(uci).isEqualTo("e7e8q");
        }

        @Test
        @DisplayName("Должен генерировать UCI для превращения в коня")
        void shouldGenerateUciForPromotionToKnight() {
            // given
            Move move = new Move(Square.E7, Square.E8, Piece.WHITE_PAWN);
            MoveNode node = new MoveNode(move, Piece.WHITE_PAWN, false, Piece.WHITE_KNIGHT);

            // when
            String uci = node.getUciMove();

            // then
            assertThat(uci).isEqualTo("e7e8n");
        }

        @Test
        @DisplayName("Должен генерировать UCI для превращения в ладью")
        void shouldGenerateUciForPromotionToRook() {
            // given
            Move move = new Move(Square.E7, Square.E8, Piece.WHITE_PAWN);
            MoveNode node = new MoveNode(move, Piece.WHITE_PAWN, false, Piece.WHITE_ROOK);

            // when
            String uci = node.getUciMove();

            // then
            assertThat(uci).isEqualTo("e7e8r");
        }

        @Test
        @DisplayName("Должен генерировать UCI для превращения в слона")
        void shouldGenerateUciForPromotionToBishop() {
            // given
            Move move = new Move(Square.E7, Square.E8, Piece.WHITE_PAWN);
            MoveNode node = new MoveNode(move, Piece.WHITE_PAWN, false, Piece.WHITE_BISHOP);

            // when
            String uci = node.getUciMove();

            // then
            assertThat(uci).isEqualTo("e7e8b");
        }
    }

    // ============================================================
    // 4. ТЕСТЫ ДЛЯ getSan()
    // ============================================================

    @Nested
    @DisplayName("getSan() - Генерация SAN")
    class SanTests {

        @Test
        @DisplayName("Должен генерировать SAN для пешечного хода")
        void shouldGenerateSanForPawnMove() {
            // given
            Move move = new Move(Square.E2, Square.E4, Piece.WHITE_PAWN);
            MoveNode node = new MoveNode(move, Piece.WHITE_PAWN, false, null);

            // when
            String san = node.getSan();

            // then
            assertThat(san).isEqualTo("e4");
        }

        @Test
        @DisplayName("Должен генерировать SAN для хода коня")
        void shouldGenerateSanForKnightMove() {
            // given
            Move move = new Move(Square.G1, Square.F3, Piece.WHITE_KNIGHT);
            MoveNode node = new MoveNode(move, Piece.WHITE_KNIGHT, false, null);

            // when
            String san = node.getSan();

            // then
            assertThat(san).isEqualTo("Nf3");
        }

        @Test
        @DisplayName("Должен генерировать SAN для взятия пешки")
        void shouldGenerateSanForPawnCapture() {
            // given
            Move move = new Move(Square.E4, Square.D5, Piece.WHITE_PAWN);
            MoveNode node = new MoveNode(move, Piece.WHITE_PAWN, true, null);

            // when
            String san = node.getSan();

            // then
            assertThat(san).isEqualTo("exd5");
        }

        @Test
        @DisplayName("Должен генерировать SAN для рокировки в короткую сторону")
        void shouldGenerateSanForKingsideCastling() {
            // given
            Move move = new Move(Square.E1, Square.G1, Piece.WHITE_KING);
            MoveNode node = new MoveNode(move, Piece.WHITE_KING, false, null);

            // when
            String san = node.getSan();

            // then
            assertThat(san).isEqualTo("O-O");
        }

        @Test
        @DisplayName("Должен генерировать SAN для рокировки в длинную сторону")
        void shouldGenerateSanForQueensideCastling() {
            // given
            Move move = new Move(Square.E1, Square.C1, Piece.WHITE_KING);
            MoveNode node = new MoveNode(move, Piece.WHITE_KING, false, null);

            // when
            String san = node.getSan();

            // then
            assertThat(san).isEqualTo("O-O-O");
        }

        @Test
        @DisplayName("Должен генерировать SAN для превращения в ферзя")
        void shouldGenerateSanForPromotionToQueen() {
            // given
            Move move = new Move(Square.E7, Square.E8, Piece.WHITE_PAWN);
            MoveNode node = new MoveNode(move, Piece.WHITE_PAWN, false, Piece.WHITE_QUEEN);

            // when
            String san = node.getSan();

            // then
            assertThat(san).isEqualTo("e8=Q");
        }

        @Test
        @DisplayName("Должен генерировать SAN для превращения с взятием")
        void shouldGenerateSanForPromotionWithCapture() {
            // given
            Move move = new Move(Square.D7, Square.C8, Piece.WHITE_PAWN);
            MoveNode node = new MoveNode(move, Piece.WHITE_PAWN, true, Piece.WHITE_QUEEN);

            // when
            String san = node.getSan();

            // then
            assertThat(san).isEqualTo("dxc8=Q");
        }

        @Test
        @DisplayName("Должен кэшировать SAN после первого вычисления")
        void shouldCacheSanAfterFirstCalculation() throws Exception {
            // given
            Move move = new Move(Square.E2, Square.E4, Piece.WHITE_PAWN);
            MoveNode node = new MoveNode(move, Piece.WHITE_PAWN, false, null);

            // when
            String san1 = node.getSan();
            String san2 = node.getSan();

            // then
            assertThat(san1).isEqualTo(san2);

            // Проверяем, что san закэширован через рефлексию
            java.lang.reflect.Field sanField = MoveNode.class.getDeclaredField("san");
            sanField.setAccessible(true);
            String cachedSan = (String) sanField.get(node);
            assertThat(cachedSan).isEqualTo(san1);
        }

        @Test
        @DisplayName("Должен использовать FEN для генерации SAN если он установлен")
        void shouldUseFenForSanGeneration() {
            // given
            Move move = new Move(Square.G1, Square.F3, Piece.WHITE_KNIGHT);
            MoveNode node = new MoveNode(move, Piece.WHITE_KNIGHT, false, null);

            String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
            node.setSavedFenBefore(fen);

            // when
            String san = node.getSan();

            // then
            assertThat(san).isEqualTo("Nf3");
        }

        @Test
        @DisplayName("Должен использовать fallback при ошибке FEN")
        void shouldUseFallbackWhenFenError() {
            // given
            Move move = new Move(Square.E2, Square.E4, Piece.WHITE_PAWN);
            MoveNode node = new MoveNode(move, Piece.WHITE_PAWN, false, null);

            // Некорректный FEN
            node.setSavedFenBefore("invalid fen");

            // when
            String san = node.getSan();

            // then
            assertThat(san).isEqualTo("e4");
        }
    }

    // ============================================================
    // 5. ТЕСТЫ ДЛЯ toString()
    // ============================================================

    @Nested
    @DisplayName("toString() - Строковое представление")
    class ToStringTests {

        @Test
        @DisplayName("Должен возвращать строковое представление с SAN и UUID")
        void shouldReturnToStringWithSanAndUuid() {
            // given
            Move move = new Move(Square.E2, Square.E4, Piece.WHITE_PAWN);
            MoveNode node = new MoveNode(move, Piece.WHITE_PAWN, false, null);
            node.setSavedFenBefore("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

            // when
            String toString = node.toString();

            // then
            assertThat(toString).startsWith("MoveNode{");
            assertThat(toString).contains("e4");
            assertThat(toString).contains("uuid=");
            assertThat(toString).endsWith("]}");
        }

        @Test
        @DisplayName("Должен содержать короткий UUID (8 символов)")
        void shouldContainShortUuid() {
            // given
            Move move = new Move(Square.E2, Square.E4, Piece.WHITE_PAWN);
            MoveNode node = new MoveNode(move, Piece.WHITE_PAWN, false, null);
            node.setSavedFenBefore("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

            // when
            String toString = node.toString();
            String uuid = node.getNodeUuid().substring(0, 8);

            // then
            assertThat(toString).contains(uuid);
            assertThat(uuid).hasSize(8);
        }
    }

    // ============================================================
    // 6. ТЕСТЫ ДЛЯ ГЕТТЕРОВ
    // ============================================================

    @Nested
    @DisplayName("Геттеры - Получение полей")
    class GettersTests {

        @Test
        @DisplayName("Должен возвращать move")
        void shouldReturnMove() {
            // given
            Move move = new Move(Square.E2, Square.E4, Piece.WHITE_PAWN);
            MoveNode node = new MoveNode(move, Piece.WHITE_PAWN, false, null);

            // then
            assertThat(node.getMove()).isEqualTo(move);
            assertThat(node.getMove().getFrom()).isEqualTo(Square.E2);
            assertThat(node.getMove().getTo()).isEqualTo(Square.E4);
        }

        @Test
        @DisplayName("Должен возвращать piece")
        void shouldReturnPiece() {
            // given
            Move move = new Move(Square.E2, Square.E4, Piece.WHITE_PAWN);
            MoveNode node = new MoveNode(move, Piece.WHITE_PAWN, false, null);

            // then
            assertThat(node.getPiece()).isEqualTo(Piece.WHITE_PAWN);
        }

        @Test
        @DisplayName("Должен возвращать isCapture")
        void shouldReturnIsCapture() {
            // given
            Move move1 = new Move(Square.E2, Square.E4, Piece.WHITE_PAWN);
            Move move2 = new Move(Square.E4, Square.D5, Piece.WHITE_PAWN);
            MoveNode nonCapture = new MoveNode(move1, Piece.WHITE_PAWN, false, null);
            MoveNode capture = new MoveNode(move2, Piece.WHITE_PAWN, true, null);

            // then
            assertThat(nonCapture.isCapture()).isFalse();
            assertThat(capture.isCapture()).isTrue();
        }

        @Test
        @DisplayName("Должен возвращать promotionPiece")
        void shouldReturnPromotionPiece() {
            // given
            Move move = new Move(Square.E7, Square.E8, Piece.WHITE_PAWN);
            MoveNode queen = new MoveNode(move, Piece.WHITE_PAWN, false, Piece.WHITE_QUEEN);
            MoveNode knight = new MoveNode(move, Piece.WHITE_PAWN, false, Piece.WHITE_KNIGHT);
            MoveNode none = new MoveNode(move, Piece.WHITE_PAWN, false, null);

            // then
            assertThat(queen.getPromotionPiece()).isEqualTo(Piece.WHITE_QUEEN);
            assertThat(knight.getPromotionPiece()).isEqualTo(Piece.WHITE_KNIGHT);
            assertThat(none.getPromotionPiece()).isNull();
        }

        @Test
        @DisplayName("Должен возвращать san (кэшированный)")
        void shouldReturnCachedSan() {
            // given
            Move move = new Move(Square.E2, Square.E4, Piece.WHITE_PAWN);
            MoveNode node = new MoveNode(move, Piece.WHITE_PAWN, false, null);
            node.setSavedFenBefore("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

            // when
            String san1 = node.getSan();
            String san2 = node.getSan();

            // then
            assertThat(san1).isEqualTo("e4");
            assertThat(san2).isEqualTo("e4");
        }
    }

    // ============================================================
    // 7. ТЕСТЫ ДЛЯ СЕТТЕРОВ (унаследованных от ParentNode)
    // ============================================================

    @Nested
    @DisplayName("Сеттеры - Установка полей (унаследованные от ParentNode)")
    class SettersTests {

        private MoveNode node;

        @BeforeEach
        void setUp() {
            Move move = new Move(Square.E2, Square.E4, Piece.WHITE_PAWN);
            node = new MoveNode(move, Piece.WHITE_PAWN, false, null);
        }

        @Test
        @DisplayName("Должен устанавливать absolutePly")
        void shouldSetAbsolutePly() {
            // when
            node.setAbsolutePly(5);

            // then
            assertThat(node.getAbsolutePly()).isEqualTo(5);
        }

        @Test
        @DisplayName("Должен устанавливать savedFenBefore")
        void shouldSetSavedFenBefore() {
            // given
            String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

            // when
            node.setSavedFenBefore(fen);

            // then
            assertThat(node.getSavedFenBefore()).isEqualTo(fen);
        }

        @Test
        @DisplayName("Должен устанавливать savedFenAfter")
        void shouldSetSavedFenAfter() {
            // given
            String fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1";

            // when
            node.setSavedFenAfter(fen);

            // then
            assertThat(node.getSavedFenAfter()).isEqualTo(fen);
        }

        @Test
        @DisplayName("Должен устанавливать parent")
        void shouldSetParent() {
            // given
            Move move2 = new Move(Square.D2, Square.D4, Piece.WHITE_PAWN);
            MoveNode parent = new MoveNode(move2, Piece.WHITE_PAWN, false, null);

            // when
            node.setParent(parent);

            // then
            assertThat(node.getParent()).isEqualTo(parent);
        }

        @Test
        @DisplayName("Должен устанавливать next")
        void shouldSetNext() {
            // given
            Move move2 = new Move(Square.D2, Square.D4, Piece.WHITE_PAWN);
            MoveNode next = new MoveNode(move2, Piece.WHITE_PAWN, false, null);

            // when
            node.setNext(next);

            // then
            assertThat(node.getNext()).isEqualTo(next);
        }

        @Test
        @DisplayName("Должен устанавливать comment")
        void shouldSetComment() {
            // given
            String comment = "Отличный ход!";

            // when
            node.setComment(comment);

            // then
            assertThat(node.getComment()).isEqualTo(comment);
        }

        @Test
        @DisplayName("Должен устанавливать annotation")
        void shouldSetAnnotation() {
            // when
            node.setAnnotation(MoveAnnotation.GOOD_MOVE);

            // then
            assertThat(node.getAnnotation()).isEqualTo(MoveAnnotation.GOOD_MOVE);
        }

        @Test
        @DisplayName("Должен устанавливать hasNagComment")
        void shouldSetHasNagComment() {
            // when
            node.setHasNagComment(true);

            // then
            assertThat(node.isHasNagComment()).isTrue();
        }

        @Test
        @DisplayName("Должен устанавливать subVariations")
        void shouldSetSubVariations() {
            // given
            Variation variation = new Variation();
            java.util.List<Variation> variations = java.util.List.of(variation);

            // when
            node.setSubVariations(variations);

            // then
            assertThat(node.getSubVariations()).containsExactly(variation);
        }
    }

    // ============================================================
    // 8. ТЕСТЫ ДЛЯ addAnnotation() (унаследованный от ParentNode)
    // ============================================================

    @Nested
    @DisplayName("addAnnotation() - Добавление аннотаций (унаследованный от ParentNode)")
    class AddAnnotationTests {

        private MoveNode node;

        @BeforeEach
        void setUp() {
            Move move = new Move(Square.E2, Square.E4, Piece.WHITE_PAWN);
            node = new MoveNode(move, Piece.WHITE_PAWN, false, null);
        }

        @Test
        @DisplayName("Должен добавлять аннотацию")
        void shouldAddAnnotation() {
            // when
            node.addAnnotation(MoveAnnotation.GOOD_MOVE);

            // then
            assertThat(node.getAnnotation()).isEqualTo(MoveAnnotation.GOOD_MOVE);
        }

        @Test
        @DisplayName("Должен добавлять несколько аннотаций")
        void shouldAddMultipleAnnotations() {
            // when
            node.addAnnotation(MoveAnnotation.GOOD_MOVE);
            node.addAnnotation(MoveAnnotation.WITH_INITIATIVE);

            // then
            assertThat(node.getAnnotation()).isEqualTo(MoveAnnotation.GOOD_MOVE);
            assertThat(node.getAdditionalAnnotations()).contains(MoveAnnotation.WITH_INITIATIVE);
        }

        @Test
        @DisplayName("Должен игнорировать null")
        void shouldIgnoreNull() {
            // when
            node.addAnnotation(null);

            // then
            assertThat(node.getAnnotation()).isNull();
            assertThat(node.getAdditionalAnnotations()).isEmpty();
        }

        @Test
        @DisplayName("Должен не добавлять дубликаты")
        void shouldNotAddDuplicates() {
            // when
            node.addAnnotation(MoveAnnotation.GOOD_MOVE);
            node.addAnnotation(MoveAnnotation.GOOD_MOVE);

            // then
            assertThat(node.getAnnotation()).isEqualTo(MoveAnnotation.GOOD_MOVE);
            assertThat(node.getAdditionalAnnotations()).isEmpty();
        }
    }

    // ============================================================
    // 9. ТЕСТЫ ДЛЯ РАБОТЫ С ВАРИАНТАМИ (унаследованные от ParentNode)
    // ============================================================

    @Nested
    @DisplayName("Работа с вариантами (унаследованные от ParentNode)")
    class VariationMethodsTests {

        private MoveNode node;

        @BeforeEach
        void setUp() {
            Move move = new Move(Square.E2, Square.E4, Piece.WHITE_PAWN);
            node = new MoveNode(move, Piece.WHITE_PAWN, false, null);
        }

        @Test
        @DisplayName("getMainLineVariation() должен возвращать null если subVariations пуст")
        void shouldReturnNullForEmptySubVariations() {
            // then
            assertThat(node.getMainLineVariation()).isNull();
        }

        @Test
        @DisplayName("getMainLineVariation() должен находить главную линию")
        void shouldFindMainLineVariation() {
            // given
            Variation mainLine = new Variation();
            mainLine.setMainLine(true);
            Variation subLine = new Variation();
            subLine.setMainLine(false);

            node.getSubVariations().add(mainLine);
            node.getSubVariations().add(subLine);

            // when
            Variation result = node.getMainLineVariation();

            // then
            assertThat(result).isEqualTo(mainLine);
        }

        @Test
        @DisplayName("getMainLineByName() должен находить вариант с именем '~'")
        void shouldFindVariationByName() {
            // given
            Variation mainLine = new Variation();
            mainLine.setName("~");
            Variation subLine = new Variation();
            subLine.setName("Other");

            node.getSubVariations().add(mainLine);
            node.getSubVariations().add(subLine);

            // when
            Variation result = node.getMainLineByName();

            // then
            assertThat(result).isEqualTo(mainLine);
        }
    }
}