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

import Khrypach.Andrey.chess.kletka.gui.model.ParentNode;
import Khrypach.Andrey.chess.kletka.gui.model.Variation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PathBuilder {
    private static final Logger log = LoggerFactory.getLogger(PathBuilder.class);

    private final Variation rootVariation;
    private final Variation mainLine;

    public PathBuilder(Variation rootVariation, Variation mainLine) {
        this.rootVariation = rootVariation;
        this.mainLine = mainLine;
    }

    public List<ParentNode> buildPath(ParentNode targetNode) {
        if (targetNode == null || targetNode.isRoot()) {
            return new ArrayList<>();
        }

        log.trace("buildPath for: {}", targetNode.getSan());

        List<ParentNode> path = buildPathFromParents(targetNode);
        if (path != null && !path.isEmpty()) {
            log.trace("Built path via parents, size: {}", path.size());
            return deduplicate(path);
        }

        log.warn("Could not build path to node: {}",
                targetNode.isRoot() ? "ROOT" : targetNode.getSan());
        return new ArrayList<>();
    }

    public List<ParentNode> buildPathFromParents(ParentNode targetNode) {
        if (targetNode == null || targetNode.isRoot()) {
            return new ArrayList<>();
        }

        List<ParentNode> path = new ArrayList<>();
        ParentNode current = targetNode;

        while (current != null && !current.isRoot()) {
            path.add(0, current);
            current = current.getParent();
        }

        return path;
    }

    public Variation findVariationContainingNode(ParentNode parentNode, ParentNode childNode) {
        if (parentNode == null || childNode == null) {
            return null;
        }

        for (Variation var : parentNode.getSubVariations()) {
            ParentNode firstNode = var.getFirstNode();
            if (firstNode == childNode) {
                return var;
            }
        }

        if (parentNode.getNext() == childNode) {
            return mainLine;
        }

        return null;
    }

    private List<ParentNode> deduplicate(List<ParentNode> path) {
        if (path == null || path.isEmpty()) {
            return new ArrayList<>();
        }
        Set<ParentNode> unique = new LinkedHashSet<>(path);
        return new ArrayList<>(unique);
    }
}