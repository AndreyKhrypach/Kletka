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

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AnalysisInfo {

    private final LanguageManager lang = LanguageManager.getInstance();

    private int depth = 0;
    private int selDepth = 0;
    private int score = 0;
    private boolean scoreIsMate = false;
    private String pv = "";
    private int nodes = 0;
    private int nps = 0;
    private int time = 0;
    private int hashFull = 0;
    private int tbHits = 0;
    private boolean lowerBound = false;
    private boolean upperbound = false;
    private String currMove = "";
    private int currMoveNumber = 0;

    public String getFormattedScore() {
        if (scoreIsMate) {
            int mateIn = score > 0 ? 30000 - score : -30000 - score;
            String mateLabel = lang.get(LanguageKeys.ANNOTATION_MATE);
            return mateIn > 0 ? mateLabel + " " + mateIn : mateLabel + " -" + Math.abs(mateIn);
        } else {
            double eval = score / 100.0;
            if (eval > 0) return String.format("+%.2f", eval);
            if (eval < 0) return String.format("%.2f", eval);
            return "0.00";
        }
    }

    public String getFirstMove() {
        if (pv == null || pv.isEmpty()) return null;
        String[] moves = pv.split(" ");
        return moves.length > 0 ? moves[0] : null;
    }

    public String getStatusInfo() {
        StringBuilder sb = new StringBuilder();
        if (depth > 0) sb.append("depth: ").append(depth);
        if (selDepth > 0) sb.append(", seldepth: ").append(selDepth);
        if (nodes > 0) sb.append(", nodes: ").append(nodes);
        if (nps > 0) sb.append(", nps: ").append(nps);
        if (time > 0) sb.append(", time: ").append(time).append("ms");
        if (hashFull > 0) sb.append(", hash: ").append(hashFull).append("%");
        if (tbHits > 0) sb.append(", tbhits: ").append(tbHits);
        if (lowerBound) sb.append(" [lowerbound]");
        if (upperbound) sb.append(" [upperbound]");
        return sb.toString();
    }

    public boolean hasPv() {
        return pv != null && !pv.isEmpty() && !pv.equals(currMove);
    }
}