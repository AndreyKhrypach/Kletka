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

/**
 * Корневой узел дерева вариантов
 * Не имеет хода, служит точкой входа для всех первых ходов
 */
public class RootNode extends ParentNode {

    public RootNode() {
        super();
        this.setAbsolutePly(-1);
    }

    @Override
    public boolean isRoot() {
        return true;
    }

    @Override
    public String getSan() {
        return "";
    }

    @Override
    public String getUciMove() {
        return "";
    }

    @Override
    public String toString() {
        return "RootNode [uuid=" + getNodeUuid().substring(0, 8) + "]";
    }
}