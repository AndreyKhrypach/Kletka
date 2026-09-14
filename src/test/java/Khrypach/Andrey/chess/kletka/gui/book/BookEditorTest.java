/*
 * Copyright (c) 2025-2026 Andrey Khrypach
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
 */

package Khrypach.Andrey.chess.kletka.gui.book;

import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты для {@link BookEditor}.
 * <p>
 * Проверяют управление «грязными» записями, дедупликацию по UCI,
 * очистку и удаление.
 */
class BookEditorTest {

    private BookEditor editor;

    @BeforeEach
    void setUp() {
        editor = new BookEditor();
    }

    // ======================================================================
    // ХЕЛПЕРЫ
    // ======================================================================

    /**
     * Создаёт PolyglotEntry из UCI хода (например, "e2e4").
     * Использует PolyglotMoveCodec — предполагается, что он есть.
     * Если его нет — можно вручную закодировать через битовые сдвиги.
     */
    private static PolyglotEntry entryFromUci(String uci, int weight) {
        Square from = Square.valueOf(uci.substring(0, 2).toUpperCase());
        Square to = Square.valueOf(uci.substring(2, 4).toUpperCase());
        Piece promotion = uci.length() > 4
                ? promotionFromChar(uci.charAt(4))
                : Piece.NONE;

        Move move = new Move(from, to, promotion);
        short code = encode(move, promotion);
        return new PolyglotEntry(0L, code, weight, 0, 0);
    }

    private static Piece promotionFromChar(char c) {
        return switch (Character.toLowerCase(c)) {
            case 'n' -> Piece.WHITE_KNIGHT;
            case 'b' -> Piece.WHITE_BISHOP;
            case 'r' -> Piece.WHITE_ROOK;
            case 'q' -> Piece.WHITE_QUEEN;
            default -> Piece.NONE;
        };
    }

    /**
     * Локальное кодирование — если PolyglotMoveCodec ещё не создан.
     * После его появления заменить на {@code PolyglotMoveCodec.encode(...)}.
     */
    private static short encode(Move move, Piece promotion) {
        int from = move.getFrom().ordinal();
        int to = move.getTo().ordinal();
        int promo = switch (promotion) {
            case WHITE_KNIGHT, BLACK_KNIGHT -> 1;
            case WHITE_BISHOP, BLACK_BISHOP -> 2;
            case WHITE_ROOK, BLACK_ROOK -> 3;
            case WHITE_QUEEN, BLACK_QUEEN -> 4;
            default -> 0;
        };
        return (short) ((promo << 12) | (from << 6) | to);
    }

    // ======================================================================
    // 1. НАЧАЛЬНОЕ СОСТОЯНИЕ
    // ======================================================================

    @Test
    @DisplayName("Новый BookEditor пуст")
    void newEditor_ShouldBeEmpty() {
        assertThat(editor.hasUnsavedChanges()).isFalse();
        assertThat(editor.getDirtyCount()).isZero();
        assertThat(editor.getDirtyEntries()).isEmpty();
    }

    // ======================================================================
    // 2. addEntry
    // ======================================================================

    @Test
    @DisplayName("addEntry добавляет новую запись")
    void addEntry_NewEntry_ShouldReturnTrue() {
        PolyglotEntry entry = entryFromUci("e2e4", 10);

        boolean added = editor.addEntry(100L, entry);

        assertThat(added).isTrue();
        assertThat(editor.hasUnsavedChanges()).isTrue();
        assertThat(editor.getDirtyCount()).isEqualTo(1);
        assertThat(editor.getDirtyEntries()).containsKey(100L);
        assertThat(editor.getDirtyEntries().get(100L)).containsExactly(entry);
    }

    @Test
    @DisplayName("addEntry возвращает false для полного дубликата (key + UCI)")
    void addEntry_DuplicateEntry_ShouldReturnFalse() {
        PolyglotEntry entry = entryFromUci("e2e4", 10);
        editor.addEntry(100L, entry);

        // Тот же key, тот же UCI — должен отклонить
        PolyglotEntry same = entryFromUci("e2e4", 999);
        boolean added = editor.addEntry(100L, same);

        assertThat(added).isFalse();
        assertThat(editor.getDirtyCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("addEntry принимает разные UCI при одном key")
    void addEntry_DifferentUci_SameKey_ShouldAcceptBoth() {
        PolyglotEntry e1 = entryFromUci("e2e4", 10);
        PolyglotEntry e2 = entryFromUci("d2d4", 20);

        assertThat(editor.addEntry(100L, e1)).isTrue();
        assertThat(editor.addEntry(100L, e2)).isTrue();

        assertThat(editor.getDirtyCount()).isEqualTo(2);
        assertThat(editor.getDirtyEntries().get(100L)).containsExactly(e1, e2);
    }

    @Test
    @DisplayName("addEntry принимает одинаковый UCI при разных key")
    void addEntry_SameUci_DifferentKey_ShouldAcceptBoth() {
        PolyglotEntry e1 = entryFromUci("e2e4", 10);
        PolyglotEntry e2 = entryFromUci("e2e4", 10);

        assertThat(editor.addEntry(100L, e1)).isTrue();
        assertThat(editor.addEntry(200L, e2)).isTrue();

        assertThat(editor.getDirtyCount()).isEqualTo(2);
        assertThat(editor.getDirtyEntries()).containsKeys(100L, 200L);
    }

    @Test
    @DisplayName("addEntry принимает запись с weight = 0 (фильтрация — в writer)")
    void addEntry_ZeroWeight_ShouldBeAccepted() {
        PolyglotEntry entry = entryFromUci("e2e4", 0);

        assertThat(editor.addEntry(100L, entry)).isTrue();
        assertThat(editor.getDirtyCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("addEntry принимает несколько записей с одним key подряд")
    void addEntry_ManyEntriesSameKey_ShouldAcceptAll() {
        for (int i = 0; i < 5; i++) {
            PolyglotEntry entry = entryFromUci(pickUci(i), 10);
            assertThat(editor.addEntry(100L, entry)).isTrue();
        }

        assertThat(editor.getDirtyCount()).isEqualTo(5);
        assertThat(editor.getDirtyEntries().get(100L)).hasSize(5);
    }

    // ======================================================================
    // 3. hasEntry
    // ======================================================================

    @Test
    @DisplayName("hasEntry возвращает false для пустой карты")
    void hasEntry_EmptyEditor_ShouldReturnFalse() {
        assertThat(editor.hasEntry(100L, "e2e4")).isFalse();
    }

    @Test
    @DisplayName("hasEntry возвращает true для добавленной записи")
    void hasEntry_ExistingEntry_ShouldReturnTrue() {
        editor.addEntry(100L, entryFromUci("e2e4", 10));

        assertThat(editor.hasEntry(100L, "e2e4")).isTrue();
    }

    @Test
    @DisplayName("hasEntry возвращает false для другого ключа")
    void hasEntry_DifferentKey_ShouldReturnFalse() {
        editor.addEntry(100L, entryFromUci("e2e4", 10));

        assertThat(editor.hasEntry(200L, "e2e4")).isFalse();
    }

    @Test
    @DisplayName("hasEntry возвращает false для другого UCI")
    void hasEntry_DifferentUci_ShouldReturnFalse() {
        editor.addEntry(100L, entryFromUci("e2e4", 10));

        assertThat(editor.hasEntry(100L, "d2d4")).isFalse();
    }

    @Test
    @DisplayName("hasEntry не находит запись, если ключ удалён")
    void hasEntry_AfterRemoveKey_ShouldReturnFalse() {
        editor.addEntry(100L, entryFromUci("e2e4", 10));
        assertThat(editor.hasEntry(100L, "e2e4")).isTrue();

        editor.removeEntry(100L, "e2e4");

        assertThat(editor.hasEntry(100L, "e2e4")).isFalse();
    }

    // ======================================================================
    // 4. hasUnsavedChanges
    // ======================================================================

    @Test
    @DisplayName("hasUnsavedChanges = false в пустом редакторе")
    void hasUnsavedChanges_Empty_ShouldBeFalse() {
        assertThat(editor.hasUnsavedChanges()).isFalse();
    }

    @Test
    @DisplayName("hasUnsavedChanges = true после addEntry")
    void hasUnsavedChanges_AfterAdd_ShouldBeTrue() {
        editor.addEntry(100L, entryFromUci("e2e4", 10));

        assertThat(editor.hasUnsavedChanges()).isTrue();
    }

    @Test
    @DisplayName("hasUnsavedChanges = false после clear")
    void hasUnsavedChanges_AfterClear_ShouldBeFalse() {
        editor.addEntry(100L, entryFromUci("e2e4", 10));
        editor.clear();

        assertThat(editor.hasUnsavedChanges()).isFalse();
    }

    @Test
    @DisplayName("hasUnsavedChanges = false после удаления всех записей")
    void hasUnsavedChanges_AfterRemoveAll_ShouldBeFalse() {
        editor.addEntry(100L, entryFromUci("e2e4", 10));
        editor.removeEntry(100L, "e2e4");

        assertThat(editor.hasUnsavedChanges()).isFalse();
    }

    // ======================================================================
    // 5. getDirtyCount
    // ======================================================================

    @Test
    @DisplayName("getDirtyCount = 0 в пустом редакторе")
    void getDirtyCount_Empty_ShouldBeZero() {
        assertThat(editor.getDirtyCount()).isZero();
    }

    @Test
    @DisplayName("getDirtyCount считает все записи по всем ключам")
    void getDirtyCount_CountsAllEntries() {
        editor.addEntry(100L, entryFromUci("e2e4", 10));
        editor.addEntry(100L, entryFromUci("d2d4", 10));
        editor.addEntry(200L, entryFromUci("e2e4", 10));

        assertThat(editor.getDirtyCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("getDirtyCount не увеличивается при попытке добавить дубликат")
    void getDirtyCount_Duplicate_ShouldNotIncrease() {
        editor.addEntry(100L, entryFromUci("e2e4", 10));
        editor.addEntry(100L, entryFromUci("e2e4", 999)); // дубликат

        assertThat(editor.getDirtyCount()).isEqualTo(1);
    }

    // ======================================================================
    // 6. clear
    // ======================================================================

    @Test
    @DisplayName("clear очищает все записи")
    void clear_RemovesAllEntries() {
        editor.addEntry(100L, entryFromUci("e2e4", 10));
        editor.addEntry(200L, entryFromUci("d2d4", 10));
        assertThat(editor.getDirtyCount()).isEqualTo(2);

        editor.clear();

        assertThat(editor.getDirtyCount()).isZero();
        assertThat(editor.getDirtyEntries()).isEmpty();
        assertThat(editor.hasUnsavedChanges()).isFalse();
    }

    @Test
    @DisplayName("clear на пустом редакторе не бросает исключение")
    void clear_Empty_ShouldNotThrow() {
        editor.clear();

        assertThat(editor.getDirtyCount()).isZero();
        assertThat(editor.hasUnsavedChanges()).isFalse();
    }

    @Test
    @DisplayName("clear идемпотентен")
    void clear_CalledTwice_ShouldBeIdempotent() {
        editor.addEntry(100L, entryFromUci("e2e4", 10));

        editor.clear();
        editor.clear();

        assertThat(editor.getDirtyCount()).isZero();
    }

    // ======================================================================
    // 7. removeEntry
    // ======================================================================

    @Test
    @DisplayName("removeEntry удаляет существующую запись")
    void removeEntry_ExistingEntry_ShouldReturnTrue() {
        editor.addEntry(100L, entryFromUci("e2e4", 10));

        boolean removed = editor.removeEntry(100L, "e2e4");

        assertThat(removed).isTrue();
        assertThat(editor.hasEntry(100L, "e2e4")).isFalse();
        assertThat(editor.getDirtyCount()).isZero();
    }

    @Test
    @DisplayName("removeEntry удаляет ключ из карты, если список опустел")
    void removeEntry_LastEntryFor_Key_ShouldRemoveKey() {
        editor.addEntry(100L, entryFromUci("e2e4", 10));

        editor.removeEntry(100L, "e2e4");

        assertThat(editor.getDirtyEntries()).doesNotContainKey(100L);
    }

    @Test
    @DisplayName("removeEntry не удаляет ключ, если остались другие записи")
    void removeEntry_LeavesKeyIfOtherEntriesExist() {
        editor.addEntry(100L, entryFromUci("e2e4", 10));
        editor.addEntry(100L, entryFromUci("d2d4", 10));

        editor.removeEntry(100L, "e2e4");

        assertThat(editor.getDirtyEntries()).containsKey(100L);
        assertThat(editor.getDirtyCount()).isEqualTo(1);
        assertThat(editor.hasEntry(100L, "e2e4")).isFalse();
        assertThat(editor.hasEntry(100L, "d2d4")).isTrue();
    }

    @Test
    @DisplayName("removeEntry возвращает false для несуществующего UCI")
    void removeEntry_WrongUci_ShouldReturnFalse() {
        editor.addEntry(100L, entryFromUci("e2e4", 10));

        boolean removed = editor.removeEntry(100L, "d2d4");

        assertThat(removed).isFalse();
        assertThat(editor.getDirtyCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("removeEntry возвращает false для несуществующего ключа")
    void removeEntry_WrongKey_ShouldReturnFalse() {
        editor.addEntry(100L, entryFromUci("e2e4", 10));

        boolean removed = editor.removeEntry(200L, "e2e4");

        assertThat(removed).isFalse();
        assertThat(editor.getDirtyCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("removeEntry возвращает false на пустом редакторе")
    void removeEntry_EmptyEditor_ShouldReturnFalse() {
        boolean removed = editor.removeEntry(100L, "e2e4");

        assertThat(removed).isFalse();
    }

    // ======================================================================
    // 8. getDirtyEntries
    // ======================================================================

    @Test
    @DisplayName("getDirtyEntries возвращает ту же самую карту (не копию)")
    void getDirtyEntries_ReturnsSameInstance() {
        Map<Long, List<PolyglotEntry>> first = editor.getDirtyEntries();
        Map<Long, List<PolyglotEntry>> second = editor.getDirtyEntries();

        assertThat(first).isSameAs(second);
    }

    @Test
    @DisplayName("getDirtyEntries возвращает корректное содержимое")
    void getDirtyEntries_ReturnsCorrectContent() {
        PolyglotEntry e1 = entryFromUci("e2e4", 10);
        PolyglotEntry e2 = entryFromUci("d2d4", 20);
        editor.addEntry(100L, e1);
        editor.addEntry(100L, e2);

        Map<Long, List<PolyglotEntry>> dirty = editor.getDirtyEntries();

        assertThat(dirty).hasSize(1);
        assertThat(dirty.get(100L)).containsExactly(e1, e2);
    }

    // ======================================================================
    // ХЕЛПЕР: циклический список UCI для массовых тестов
    // ======================================================================

    private static String pickUci(int index) {
        String[] moves = {
                "e2e4", "d2d4", "g1f3", "c2c4", "b1c3",
                "a2a3", "h2h3", "g2g3", "f2f4", "b2b3"
        };
        return moves[index % moves.length];
    }
}