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

import Khrypach.Andrey.chess.kletka.gui.settings.AppPreferences;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoardSizeController {

    private static final Logger log = LoggerFactory.getLogger(BoardSizeController.class);

    public static final int DEFAULT_TILE_SIZE = 80;
    public static final int MIN_TILE_SIZE = 40;
    public static final int MAX_TILE_SIZE = 100;
    public static final int STEP_SIZE = 10;

    private final IntegerProperty tileSize = new SimpleIntegerProperty(DEFAULT_TILE_SIZE);
    private final Stage primaryStage;
    private boolean isWindowMaximized = false;

    public BoardSizeController(Stage primaryStage, Runnable onSizeChanged) {
        this.primaryStage = primaryStage;

        // ЗАГРУЖАЕМ СОХРАНЕННЫЙ РАЗМЕР
        int savedSize = AppPreferences.getTileSize();
        log.debug("Loaded saved tile size: {}px", savedSize);
        tileSize.set(savedSize);

        // Следим за состоянием окна
        primaryStage.maximizedProperty().addListener((obs, oldVal, newVal) ->
                isWindowMaximized = newVal);

        // Добавляем слушатель изменения размера
        tileSize.addListener((obs, oldVal, newVal) -> {
            // СОХРАНЯЕМ НОВЫЙ РАЗМЕР
            AppPreferences.saveTileSize(newVal.intValue());
            log.debug("Tile size changed: {}px -> {}px, saved", oldVal, newVal);

            if (onSizeChanged != null) {
                onSizeChanged.run();
            }
            if (!isWindowMaximized) {
                updateWindowSize();
            }
        });
    }

    private void updateWindowSize() {
        if (primaryStage != null && primaryStage.getScene() != null) {
            primaryStage.setWidth(calculateWindowWidth());
            primaryStage.setHeight(calculateWindowHeight());
        }
    }

    public double calculateWindowWidth() {
        return getTileSize() * 8 + 300; // 8 колонок + панель нотации
    }

    public double calculateWindowHeight() {
        return getTileSize() * 8 + 100; // 8 рядов + верхняя панель + нижние координаты
    }

    public int getTileSize() {
        return tileSize.get();
    }

    public IntegerProperty tileSizeProperty() {
        return tileSize;
    }

    public void setTileSize(int size) {
        if (size < MIN_TILE_SIZE) size = MIN_TILE_SIZE;
        if (size > MAX_TILE_SIZE) size = MAX_TILE_SIZE;
        log.debug("Tile size set to: {}px", size);
        tileSize.set(size);
    }

    // ========== МЕТОДЫ ДЛЯ ЗУМА ==========

    public void increaseSize() {
        int newSize = getTileSize() + STEP_SIZE;
        if (newSize > MAX_TILE_SIZE) {
            newSize = MAX_TILE_SIZE;
        }
        setTileSize(newSize);
    }

    public void decreaseSize() {
        int newSize = getTileSize() - STEP_SIZE;
        if (newSize < MIN_TILE_SIZE) {
            newSize = MIN_TILE_SIZE;
        }
        setTileSize(newSize);
    }

    public void resetSize() {
        setTileSize(DEFAULT_TILE_SIZE);
    }

}