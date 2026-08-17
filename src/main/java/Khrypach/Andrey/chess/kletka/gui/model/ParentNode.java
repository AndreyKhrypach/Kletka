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

import java.util.*;

/**
 * Базовый узел дерева вариантов
 * Может быть корневым узлом (move == null) или узлом хода
 */
@Getter
@Setter
public abstract class ParentNode {

    private final LanguageManager languageManager = LanguageManager.getInstance();
    // Связи
    private ParentNode parent;          // предыдущий узел в линии
    private ParentNode next;            // следующий узел в линии
    private ParentNode forkNode;  // Ссылка на узел-развилку (null если нет развилки или узел сам является развилкой)
    private Variation owningVariation;  // Ссылка на вариант, которому принадлежит узел (null для основного продолжения)
    private List<Variation> subVariations = new ArrayList<>(); // под-варианты от этого узла

    // Идентификация
    private String nodeUuid;            // уникальный ID узла (не меняется)
    private int absolutePly = -1;       // абсолютный номер полу-хода (для корня = -1)

    // FEN для восстановления
    private String savedFenBefore;      // FEN до выполнения (если есть ход)
    private String savedFenAfter;       // FEN после выполнения (если есть ход)

    // Аннотации и комментарии (общие для всех узлов)
    private MoveAnnotation annotation;
    private Set<MoveAnnotation> additionalAnnotations = new HashSet<>();
    private String comment;
    private boolean hasNagComment;

    public ParentNode() {
        this.nodeUuid = UUID.randomUUID().toString();
    }

    /**
     * Проверяет, является ли узел корневым (не имеет хода)
     */
    public abstract boolean isRoot();

    /**
     * Возвращает SAN представление (для корня - пустую строку)
     */
    public abstract String getSan();

    /**
     * Возвращает UCI представление (для корня - пустую строку)
     */
    public abstract String getUciMove();

    /**
     * Добавляет аннотацию
     */
    public void addAnnotation(MoveAnnotation annotation) {
        if (annotation == null) return;

        // Проверяем, есть ли уже такая аннотация
        if (this.annotation == annotation) {
            return; // Уже есть как основная
        }
        if (this.additionalAnnotations.contains(annotation)) {
            return; // Уже есть в дополнительных
        }

        // Добавляем
        if (this.annotation == null) {
            this.annotation = annotation;
        } else {
            this.additionalAnnotations.add(annotation);
        }
    }

    /**
     * Возвращает вариант с флагом isMainLine=true из subVariations
     * Если такого нет - возвращает null
     */
    public Variation getMainLineVariation() {
        if (subVariations == null || subVariations.isEmpty()) {
            return null;
        }
        for (Variation var : subVariations) {
            if (var.isMainLine()) {
                return var;
            }
        }
        return null;
    }

    /**
     * Возвращает вариант с именем "~" из subVariations (для поиска старой главной линии)
     */
    public Variation getMainLineByName() {
        if (subVariations == null || subVariations.isEmpty()) {
            return null;
        }
        for (Variation var : subVariations) {
            if ("~".equals(var.getName()) || languageManager.get(LanguageKeys.MAIN_LINE).equals(var.getName())) {
                return var;
            }
        }
        return null;
    }
}