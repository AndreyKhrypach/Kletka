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

package Khrypach.Andrey.chess.kletka.gui.coach.tools;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
public class ArrowData {private final String fromSquare;
    private final String toSquare;
    @Setter
    private MarkerColor color;

    public ArrowData(String fromSquare, String toSquare, MarkerColor color) {
        this.fromSquare = fromSquare;
        this.toSquare = toSquare;
        this.color = color;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ArrowData that = (ArrowData) obj;
        return fromSquare.equals(that.getFromSquare()) && toSquare.equals(that.getToSquare());
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromSquare, toSquare);
    }

}
