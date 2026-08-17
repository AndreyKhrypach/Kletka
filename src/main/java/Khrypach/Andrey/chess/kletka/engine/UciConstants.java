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

package Khrypach.Andrey.chess.kletka.engine;

public final class UciConstants {
    // Базовые команды UCI
    public static final String UCI = "uci";
    public static final String IS_READY = "isready";
    public static final String UCI_NEW_GAME = "ucinewgame";
    public static final String QUIT = "quit";
    public static final String STOP = "stop";

    // Команды позиции
    public static final String POSITION = "position";
    public static final String POSITION_START_POS = "startpos";
    public static final String POSITION_FEN = "fen";

    // Команды поиска
    public static final String GO = "go";
    public static final String GO_INFINITE = "infinite";
    public static final String GO_DEPTH = "depth";
    public static final String GO_MOVE_TIME = "movetime";
    public static final String GO_NODES = "nodes";
    public static final String GO_WTIME = "wtime";
    public static final String GO_BTIME = "btime";
    public static final String GO_WINC = "winc";
    public static final String GO_BINC = "binc";
    public static final String GO_MOVES_TO_GO = "movestogo";

    // Ответы движка
    public static final String BEST_MOVE = "bestmove";
    public static final String READY_OK = "readyok";
    public static final String UCI_OK = "uciok";
    public static final String INFO = "info";
    public static final String ID_NAME = "id name";

    // Параметры info
    public static final String INFO_DEPTH = "depth";
    public static final String SEL_DEPTH = "seldepth";
    public static final String INFO_SCORE = "score";
    public static final String INFO_CP = "cp";
    public static final String INFO_MATE = "mate";
    public static final String INFO_PV = "pv";
    public static final String INFO_MULTIPV = "multipv";

    // Опции движка
    public static final String SET_OPTION = "setoption";
    public static final String SET_OPTION_NAME = "name";
    public static final String SET_OPTION_VALUE = "value";
    public static final String MULTI_PV = "MultiPV";

    private UciConstants() {} // запрет создания экземпляра
}