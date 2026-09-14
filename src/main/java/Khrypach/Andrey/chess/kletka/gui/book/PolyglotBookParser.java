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

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import Khrypach.Andrey.chess.kletka.gui.model.MoveNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static Khrypach.Andrey.chess.kletka.gui.book.PolyglotConstants.ENTRY_SIZE;

/**
 * Парсер Polyglot книг (.bin) с ленивой загрузкой.
 * Использует MappedByteBuffer (mmap) — мгновенная загрузка и бинарный поиск.
 */
public class PolyglotBookParser implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(PolyglotBookParser.class);

    private MappedByteBuffer buffer;
    private int entryCount;
    private boolean closed = false;

    public int getTotalEntries() {
        return entryCount;
    }

    /**
     * Открывает файл книги через mmap.
     */
    public void open(Path bookPath) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(bookPath.toFile(), "r");
             FileChannel channel = file.getChannel()) {

            long fileSize = channel.size();
            this.entryCount = (int) (fileSize / ENTRY_SIZE);

            this.buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
            this.buffer.order(ByteOrder.BIG_ENDIAN);
            this.closed = false;

            log.debug("Opened book {}: {} bytes, {} entries",
                    bookPath, fileSize, entryCount);
        }
    }

    /**
     * Ключ по индексу (без создания объекта).
     */
    private long getKeyAt(int index) {
        int position = index * ENTRY_SIZE;
        return buffer.getLong(position);
    }

    /**
     * Бинарный поиск первой записи с ключом >= key (bisect_left).
     */
    private int bisectKeyLeft(long key) {
        int lo = 0;
        int hi = entryCount;

        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            long midKey = getKeyAt(mid);
            if (Long.compareUnsigned(midKey, key) < 0) {
                lo = mid + 1;
            } else {
                hi = mid;
            }
        }
        return lo;
    }

    /**
     * Находит все записи для позиции (по ключу).
     */
    public List<PolyglotEntry> findAllEntries(long key) {
        if (buffer == null || closed) {
            return List.of();
        }

        int index = bisectKeyLeft(key);
        List<PolyglotEntry> result = new ArrayList<>();

        while (index < entryCount) {
            int position = index * ENTRY_SIZE;
            long currentKey = buffer.getLong(position);
            if (currentKey != key) {
                break;
            }

            short move = buffer.getShort(position + 8);
            int weight = buffer.getShort(position + 10) & 0xFFFF;
            int learn = buffer.getInt(position + 12);

            result.add(new PolyglotEntry(currentKey, move, weight, learn, 0));
            index++;
        }

        return result;
    }

    /**
     * Проверяет, легален ли ход на доске.
     */
    public boolean isLegalMove(PolyglotEntry entry, Board board) {
        Square from = entry.getFromSquare();
        Square to = entry.getToSquare();
        Piece promotion = entry.getPromotionPiece();

        for (Move move : board.legalMoves()) {
            if (move.getFrom() == from && move.getTo() == to) {
                Piece legalPromotion = move.getPromotion();
                boolean legalNone = (legalPromotion == null || legalPromotion == Piece.NONE);
                boolean entryNone = (promotion == null || promotion == Piece.NONE);
                if (legalNone && entryNone) {
                    return true;
                }
                if (legalPromotion == promotion) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Создаёт Move из PolyglotEntry.
     */
    public Move createChesslibMove(PolyglotEntry entry) {
        return new Move(entry.getFromSquare(), entry.getToSquare(), entry.getPromotionPiece());
    }

    /**
     * Создаёт MoveNode из записи Polyglot.
     */
    public MoveNode createMoveNode(PolyglotEntry entry, Board board, int ply) {
        Square from = entry.getFromSquare();
        Square to = entry.getToSquare();
        Piece promotionPiece = entry.getPromotionPiece();

        Move move = new Move(from, to, promotionPiece);
        Piece piece = board.getPiece(from);
        boolean isCapture = board.getPiece(to) != Piece.NONE;

        MoveNode node = new MoveNode(move, piece, isCapture, promotionPiece);
        node.setAbsolutePly(ply);

        // ← НОВОЕ: FEN до и после
        node.setSavedFenBefore(board.getFen());

        Board afterBoard = board.clone();
        try {
            afterBoard.doMove(move);
            node.setSavedFenAfter(afterBoard.getFen());
        } catch (Exception e) {
            log.trace("Failed to apply move for FEN: {}", e.getMessage());
            node.setSavedFenAfter(board.getFen());
        }

        String stats = String.format("weight:%d,games:%d,rating:%d",
                entry.weight(), entry.games(), entry.getRating());
        node.setComment(stats);

        return node;
    }

    /**
     * Генерирует имя варианта.
     */
    public String generateVariationName(MoveNode node, int id) {
        String san = node.getSan();
        if (san == null || san.isEmpty()) {
            return "V" + id;
        }
        san = san.replace("+", "").replace("#", "");
        return san + " (" + id + ")";
    }

    /**
     * Освобождает mmap.
     */
    @Override
    public void close() {
        if (buffer == null || closed) return;
        try {
            unmap(buffer);
        } catch (Exception e) {
            log.debug("Failed to unmap buffer, falling back to GC", e);
        }
        buffer = null;
        closed = true;
        log.debug("Book parser closed");
    }

    private static void unmap(ByteBuffer buffer) {
        if (buffer == null) return;
        try {
            Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            sun.misc.Unsafe unsafe = (sun.misc.Unsafe) field.get(null);
            Method method = sun.misc.Unsafe.class.getMethod("invokeCleaner", ByteBuffer.class);
            method.invoke(unsafe, buffer);
        } catch (Exception e) {
            buffer.clear();
            System.gc();
        }
    }
}