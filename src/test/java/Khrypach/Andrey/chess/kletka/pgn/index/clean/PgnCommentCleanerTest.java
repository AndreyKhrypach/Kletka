/*
 * Copyright (c) 2025 Andrey Khrypach
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package Khrypach.Andrey.chess.kletka.pgn.index.clean;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PgnCommentCleanerTest {

    // ============================================================
    // 1. БАЗОВАЯ ОЧИСТКА { ... }
    // ============================================================

    @Test
    void clean_shouldRemoveBlockComments() {
        String pgn = headers() + "\n\n1. e4 {Открытый центр} e5 {Симметрия} 2. Nf3 *";
        PgnCommentCleaner.CleanResult r = PgnCommentCleaner.clean(pgn);

        assertTrue(r.cleanedBody().contains("1. e4"));
        assertTrue(r.cleanedBody().contains("e5"));
        assertTrue(r.cleanedBody().contains("2. Nf3"));
        assertFalse(r.cleanedBody().contains("Открытый центр"));
        assertFalse(r.cleanedBody().contains("Симметрия"));
        assertFalse(r.cleanedBody().contains("{"));
        assertEquals(2, r.removedComments());
    }

    @Test
    void clean_shouldRemoveMultilineComments() {
        String pgn = headers() + "\n\n1. e4 {This is\na very long\ncomment} e5 *";
        PgnCommentCleaner.CleanResult r = PgnCommentCleaner.clean(pgn);

        assertFalse(r.cleanedBody().contains("very long"));
        assertFalse(r.cleanedBody().contains("{"));
        assertEquals(1, r.removedComments());
    }

    @Test
    void clean_shouldRemoveSemicolonComments() {
        String pgn = headers() + "\n\n1. e4 ; comment to end of line\ne5 *";
        PgnCommentCleaner.CleanResult r = PgnCommentCleaner.clean(pgn);

        assertFalse(r.cleanedBody().contains("comment to end of line"));
        assertEquals(1, r.removedComments());
    }

    // ============================================================
    // 2. NAG $N
    // ============================================================

    @Test
    void clean_shouldRemoveNags() {
        String pgn = headers() + "\n\n1. e4 $1 $10 e5 $2 *";
        PgnCommentCleaner.CleanResult r = PgnCommentCleaner.clean(pgn);

        assertFalse(r.cleanedBody().contains("$1"));
        assertFalse(r.cleanedBody().contains("$10"));
        assertFalse(r.cleanedBody().contains("$2"));
        assertEquals(3, r.removedNags());
    }

    // ============================================================
    // 3. СУФФИКСЫ ! ?
    // ============================================================

    @Test
    void clean_shouldRemoveSuffixes() {
        String pgn = headers() + "\n\n1. e4! e5?! 2. Nf3?? Nc6!! 3. d4!? *";
        PgnCommentCleaner.CleanResult r = PgnCommentCleaner.clean(pgn);

        assertFalse(r.cleanedBody().contains("e4!"));
        assertFalse(r.cleanedBody().contains("e5?!"));
        assertFalse(r.cleanedBody().contains("Nf3??"));
        assertFalse(r.cleanedBody().contains("Nc6!!"));
        assertFalse(r.cleanedBody().contains("d4!?"));
        assertTrue(r.removedSuffixes() >= 5);
    }

    @Test
    void clean_shouldNotTouchExclamationInHeaders() {
        String pgn = "[Event \"FIDE World Cup!\"]\n"
                + "[White \"Smith\"]\n"
                + "[Black \"Jones\"]\n"
                + "[Result \"1-0\"]\n\n"
                + "1. e4 e5 *";
        PgnCommentCleaner.CleanResult r = PgnCommentCleaner.clean(pgn);

        assertTrue(r.cleanedBody().contains("[Event \"FIDE World Cup!\"]"),
                "Заголовок не должен быть изменён");
    }

    // ============================================================
    // 4. ГРАНИЧНЫЕ СЛУЧАИ (баги, которые мы ловили)
    // ============================================================

    @Test
    void clean_shouldNotBreakOnBracketInsideComment() {
        // Это баг, который мы исправляли: ']' внутри комментария ломал findBodyStart
        String pgn = headers() + "\n\n1. e4 {see [sic] this} e5 {Fischer [1963]} 2. Nf3 *";
        PgnCommentCleaner.CleanResult r = PgnCommentCleaner.clean(pgn);

        assertFalse(r.cleanedBody().contains("see"));
        assertFalse(r.cleanedBody().contains("Fischer"));
        assertFalse(r.cleanedBody().contains("{"));
        assertTrue(r.cleanedBody().contains("1. e4"));
        assertTrue(r.cleanedBody().contains("e5"));
        assertTrue(r.cleanedBody().contains("2. Nf3"));
        assertEquals(2, r.removedComments());
    }

    @Test
    void clean_shouldHandlePgnWithoutBody() {
        String pgn = headers(); // только заголовки
        PgnCommentCleaner.CleanResult r = PgnCommentCleaner.clean(pgn);
        assertEquals(0, r.totalRemoved());
        assertEquals(pgn, r.cleanedBody());
    }

    @Test
    void clean_shouldHandleEmptyString() {
        PgnCommentCleaner.CleanResult r = PgnCommentCleaner.clean("");
        assertEquals(0, r.totalRemoved());
        assertEquals("", r.cleanedBody());
    }

    @Test
    void clean_shouldHandleNull() {
        PgnCommentCleaner.CleanResult r = PgnCommentCleaner.clean(null);
        assertEquals(0, r.totalRemoved());
        assertEquals("", r.cleanedBody());
    }

    @Test
    void clean_shouldHandleBodyWithoutHeaders() {
        // PGN без заголовков (мусор / фрагмент)
        String pgn = "1. e4 {comment} e5 *";
        PgnCommentCleaner.CleanResult r = PgnCommentCleaner.clean(pgn);

        // findBodyStart вернёт -1 (нет заголовков) → тело не чистится
        // Это ожидаемое поведение: без заголовков не знаем, где тело
        assertEquals(0, r.totalRemoved());
    }

    @Test
    void clean_shouldHandleSetUpAndFen() {
        String pgn = "[Event \"?\"]\n"
                + "[Site \"?\"]\n"
                + "[Date \"????.??.??\"]\n"
                + "[Round \"?\"]\n"
                + "[White \"?\"]\n"
                + "[Black \"?\"]\n"
                + "[Result \"*\"]\n"
                + "[SetUp \"1\"]\n"
                + "[FEN \"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1\"]\n"
                + "\n"
                + "1. e4 {king pawn} e5 *";
        PgnCommentCleaner.CleanResult r = PgnCommentCleaner.clean(pgn);

        assertTrue(r.cleanedBody().contains("[SetUp \"1\"]"));
        assertTrue(r.cleanedBody().contains("[FEN \""));
        assertFalse(r.cleanedBody().contains("king pawn"));
        assertEquals(1, r.removedComments());
    }

    @Test
    void clean_shouldHandleCastlingSuffix() {
        String pgn = headers() + "\n\n1. e4 e5 2. Nf3 Nc6 3. Bc4 Bc5 4. O-O! *";
        PgnCommentCleaner.CleanResult r = PgnCommentCleaner.clean(pgn);

        assertFalse(r.cleanedBody().contains("O-O!"));
        assertTrue(r.cleanedBody().contains("O-O"));
    }

    // ============================================================
    // ВСПОМОГАТЕЛЬНЫЙ
    // ============================================================

    private String headers() {
        return "[Event \"Test\"]\n"
                + "[Site \"?\"]\n"
                + "[Date \"2024.01.01\"]\n"
                + "[Round \"1\"]\n"
                + "[White \"A\"]\n"
                + "[Black \"B\"]\n"
                + "[Result \"*\"]";
    }
}