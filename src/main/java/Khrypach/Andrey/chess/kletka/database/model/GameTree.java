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

package Khrypach.Andrey.chess.kletka.database.model;

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.model.RootNode;
import Khrypach.Andrey.chess.kletka.gui.model.Variation;
import com.github.bhlangonijr.chesslib.Board;
import lombok.Getter;
import lombok.Setter;

/**
 * Дерево вариантов партии
 * Обертка над RootNode и Variation для работы с БД
 */
@Getter
@Setter
public class GameTree {

    private final LanguageManager languageManager = LanguageManager.getInstance();

    private RootNode rootNode;
    private Variation mainLine;
    private Variation rootVariation;
    private String gameId;          // Идентификатор для БД
    private String initialFen;      // Начальная позиция (FEN)
    private boolean startWithBlack;
    private Board initialBoard;
    private String result = "*";
    private boolean deleted = false;

    public GameTree() {
        this.rootNode = new RootNode();
        this.rootVariation = new Variation(languageManager.get(LanguageKeys.ROOT));
        this.rootVariation.setFirstNode(rootNode);
        this.mainLine = new Variation(languageManager.get(LanguageKeys.MAIN_LINE));
        this.mainLine.setMainLine(true);
        this.mainLine.setParentVariation(rootVariation);
        this.mainLine.setParentNodeRef(rootNode);
        this.initialFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        this.startWithBlack = false;
    }

    public GameTree(RootNode rootNode, Variation mainLine, Variation rootVariation) {
        this.rootNode = rootNode;
        this.mainLine = mainLine;
        this.rootVariation = rootVariation;
        this.initialFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        this.startWithBlack = false;
    }

    /**
     * Проверяет, пустое ли дерево (нет ходов)
     */
    public boolean isEmpty() {
        return mainLine == null || mainLine.isEmpty();
    }

    public void setInitialBoard(Board board) {
        this.initialBoard = board != null ? board.clone() : null;
    }

    public Board getInitialBoard() {
        return initialBoard != null ? initialBoard.clone() : new Board();
    }

}