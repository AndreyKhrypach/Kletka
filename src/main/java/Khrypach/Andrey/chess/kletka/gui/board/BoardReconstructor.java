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

package Khrypach.Andrey.chess.kletka.gui.board;

import Khrypach.Andrey.chess.kletka.gui.model.MoveNode;
import Khrypach.Andrey.chess.kletka.gui.model.ParentNode;
import Khrypach.Andrey.chess.kletka.gui.model.Variation;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BoardReconstructor {
    private static final Logger log = LoggerFactory.getLogger(BoardReconstructor.class);

    private final PathBuilder pathBuilder;
    private final Board initialPosition;
    private final boolean startWithBlack;

    public BoardReconstructor(PathBuilder pathBuilder, Board initialPosition, boolean startWithBlack) {
        this.pathBuilder = pathBuilder;
        this.initialPosition = initialPosition != null ? initialPosition.clone() : null;
        this.startWithBlack = startWithBlack;
    }

    /**
     * Восстанавливает доску в позиции указанного узла
     */
    public Board reconstruct(Variation variation, ParentNode targetNode) {
        log.trace("RECONSTRUCT - Variation: {}, Target node: {}",
                variation != null ? variation.getName() : "null",
                targetNode != null ? targetNode.getSan() : "null"
        );

        Board board = getStartBoard();
        log.trace("Start board FEN: {}", board.getFen());

        if (targetNode == null || targetNode.isRoot()) {
            log.trace("Target is root or null, returning start board");
            return board;
        }

        List<ParentNode> path = pathBuilder.buildPath(targetNode);
        log.trace("Path size: {}", path.size());

        if (path.isEmpty()) {
            log.warn("Empty path for node: {}", targetNode.getSan());
            return board;
        }

        // Удаляем дубликаты
        Set<ParentNode> uniqueNodes = new LinkedHashSet<>(path);
        path.clear();
        path.addAll(uniqueNodes);

        log.trace("Path size after dedup: {}", path.size());

        // Применяем все ходы
        for (int i = 0; i < path.size(); i++) {
            ParentNode node = path.get(i);
            log.trace("  [{}] {}", i, node.getSan());

            if (!node.isRoot() && node instanceof MoveNode moveNode) {
                Move move = moveNode.getMove();
                try {
                    if (board.isMoveLegal(move, true)) {
                        board.doMove(move);
                        log.trace("    Applied move: {}", node.getSan());
                    } else {
                        log.warn("Move NOT legal: {}", node.getSan());
                    }
                } catch (Exception e) {
                    log.error("Error applying move {}: {}", node.getSan(), e.getMessage());
                    break;
                }
            }
        }

        log.trace("Final board FEN: {}", board.getFen());
        return board;
    }

    public Board getStartBoard() {
        if (initialPosition != null) {
            return initialPosition.clone();
        }
        Board board = new Board();
        if (startWithBlack) {
            board.setSideToMove(Side.BLACK);
        }
        return board;
    }
}