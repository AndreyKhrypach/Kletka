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

package Khrypach.Andrey.chess.kletka.pgn.index.model;

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import lombok.Builder;
import lombok.Data;

/**
 * Прогресс индексации для UI
 */
@Data
@Builder
public class IndexingProgress {
    private int totalGames;
    private int processedGames;
    private long bytesProcessed;
    private long totalBytes;
    private String currentGame;
    private String status;

    private static final LanguageManager lang = LanguageManager.getInstance();

    /**
     * Получает прогресс в процентах (0.0 - 1.0)
     */
    public double getProgress() {
        if (totalGames == 0) return 0.0;
        return (double) processedGames / totalGames;
    }

    /**
     * Получает форматированное сообщение о прогрессе
     */
    public String getProgressMessage() {
        return String.format(lang.get(LanguageKeys.INDEXING_PROGRESS_MESSAGE),
                processedGames, totalGames, getProgress() * 100);
    }

    public static IndexingProgress starting(int totalGames) {
        return IndexingProgress.builder()
                .totalGames(totalGames)
                .processedGames(0)
                .bytesProcessed(0)
                .totalBytes(0)
                .status(lang.get(LanguageKeys.INDEXING_STATUS_STARTING))
                .build();
    }

    public static IndexingProgress complete(int totalGames) {
        return IndexingProgress.builder()
                .totalGames(totalGames)
                .processedGames(totalGames)
                .bytesProcessed(0)
                .totalBytes(0)
                .status(lang.get(LanguageKeys.INDEXING_STATUS_COMPLETE))
                .build();
    }
}