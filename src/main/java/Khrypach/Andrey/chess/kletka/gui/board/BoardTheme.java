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

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import javafx.scene.paint.Color;

public class BoardTheme {

    public record Theme(String name, Color lightColor, Color darkColor) {
    }

    // Деревянная тема (теплая)
    public static final Theme WOOD = new Theme(
            LanguageManager.getInstance().get(LanguageKeys.MENU_BOARD_THEME_WOOD),
            Color.rgb(240, 217, 181),  // светло-бежевый
            Color.rgb(181, 136, 99)     // коричневый
    );

    // Классическая тема (белые и серые клетки)
    public static final Theme CLASSIC = new Theme(
            LanguageManager.getInstance().get(LanguageKeys.MENU_BOARD_THEME_CLASSIC),
            Color.rgb(240, 240, 240),  // почти белый
            Color.rgb(130, 130, 130)    // серый
    );

    // Зеленая тема
    public static final Theme GREEN = new Theme(
            LanguageManager.getInstance().get(LanguageKeys.MENU_BOARD_THEME_GREEN),
            Color.rgb(222, 184, 135),  // светлый деревянный
            Color.rgb(85, 107, 47)      // темно-зеленый
    );

    // Синяя тема
    public static final Theme BLUE = new Theme(
            LanguageManager.getInstance().get(LanguageKeys.MENU_BOARD_THEME_BLUE),
            Color.rgb(222, 235, 247),  // светло-голубой
            Color.rgb(70, 130, 180)     // синий
    );

    public static final Theme[] THEMES = {WOOD, CLASSIC, GREEN, BLUE};
}