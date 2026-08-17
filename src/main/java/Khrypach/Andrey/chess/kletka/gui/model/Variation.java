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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * Представляет вариант (вариацию) в дереве партии
 * Вариант - это "взгляд" на поддерево, начинающееся с определенного узла
 */
@Getter
@Setter
public class Variation {

    private static int nextId = 0;
    private final int id;
    private String name;
    private final StringProperty displayName = new SimpleStringProperty();
    private String uuid;

    // Ссылка на первый узел варианта (может быть RootNode или MoveNode)
    private ParentNode firstNode;

    // Флаг главной линии
    private boolean isMainLine;

    // Родительский вариант (откуда пришли)
    private Variation parentVariation;

    // Родительский узел (в каком узле находится этот вариант как под-вариант)
    private ParentNode parentNodeRef;

    private transient Variation rootVariation;

    // Метаданные для восстановления
    private String parentFen;
    private int parentPly = -1;

    @Getter
    private boolean nameGenerated = false;

    public Variation(String name) {
        this.id = nextId++;
        this.name = name;
        this.uuid = UUID.randomUUID().toString();
        this.isMainLine = false;
        updateDisplayName();
    }

    public Variation() {
        this(LanguageManager.getInstance().get(LanguageKeys.VARIATION_DEFAULT_NAME));
    }

    /**
     * Получает список ходов варианта (обходит цепочку next)
     */
    public List<ParentNode> getMoves() {
        List<ParentNode> moves = new ArrayList<>();
        if (firstNode == null || firstNode.isRoot()) {
            return moves;
        }

        ParentNode current = firstNode;
        while (current != null && !current.isRoot()) {
            moves.add(current);
            current = current.getNext();
        }
        return moves;
    }

    /**
     * Добавляет ход в конец варианта
     */
    public void addMove(MoveNode moveNode) {
        if (moveNode == null) return;

        if (firstNode == null || firstNode.isRoot()) {
            firstNode = moveNode;
        } else {
            ParentNode last = getLastNode();
            if (last != null) {
                last.setNext(moveNode);
                moveNode.setParent(last);
            }
        }
        updateDisplayName();
    }

    /**
     * Очищает все ходы варианта
     */
    public void clearMoves() {
        firstNode = null;
        updateDisplayName();
    }

    /**
     * Получает последний узел варианта
     */
    public ParentNode getLastNode() {
        if (firstNode == null || firstNode.isRoot()) return null;

        ParentNode current = firstNode;
        while (current.getNext() != null && !current.getNext().isRoot()) {
            current = current.getNext();
        }
        return current;
    }

    /**
     * Получает длину варианта (количество ходов)
     */
    public int getMoveCount() {
        int count = 0;
        ParentNode current = firstNode;
        while (current != null && !current.isRoot()) {
            count++;
            current = current.getNext();
        }
        return count;
    }

    /**
     * Проверяет, пуст ли вариант
     */
    public boolean isEmpty() {
        return firstNode == null || firstNode.isRoot();
    }

    private void updateDisplayName() {
        StringBuilder sb = new StringBuilder(name);
        List<ParentNode> moves = getMoves();
        if (!moves.isEmpty()) {
            sb.append(" (");
            for (int i = 0; i < Math.min(3, moves.size()); i++) {
                if (i > 0) sb.append(" ");
                sb.append(moves.get(i).getSan());
            }
            if (moves.size() > 3) sb.append("...");
            sb.append(")");
        }
        displayName.set(sb.toString());
    }

    public void setName(String name) {
        this.name = name;
        updateDisplayName();
    }

    public ParentNode getFirstNode() {
        // Если это главная линия и firstNode является корнем,
        // то реальный первый ход - это следующий за корнем узел
        if (this.isMainLine() && (firstNode == null || firstNode.isRoot())) {
            // Ищем первый реальный ход в цепочке next от корня
            if (firstNode != null && !firstNode.isRoot()) {
                return firstNode;
            }

            // Если firstNode корень, ищем следующий узел
            ParentNode current = firstNode;
            while (current != null && !current.isRoot()) {
                current = current.getNext();
            }
            if (current != null && !current.isRoot()) {
                return current;
            }

            // Если не нашли, пробуем найти через rootNode
            ParentNode rootNode = getParentNodeRef();
            if (rootNode != null && rootNode.isRoot()) {
                ParentNode next = rootNode.getNext();
                if (next != null && !next.isRoot()) {
                    return next;
                }
            }
        }

        return firstNode;
    }

    @Override
    public String toString() {
        return name + " [id=" + id + ", uuid=" + uuid.substring(0, 8) + "]";
    }

    public String getDebugInfo() {
        return String.format("%s [uuid=%s, id=%d, mainLine=%s]",
                name, uuid.substring(0, 8), id, isMainLine);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Variation other)) return false;
        return uuid.equals(other.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}