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

import javafx.scene.paint.Color;
import lombok.Getter;

@Getter
public enum MarkerColor {
    BLUE(Color.rgb(0, 100, 255, 0.85)),
    RED(Color.rgb(255, 50, 50, 0.85)),
    GREEN(Color.rgb(50, 200, 50, 0.85));

    private final Color color;

    MarkerColor(Color color) {
        this.color = color;
    }

}