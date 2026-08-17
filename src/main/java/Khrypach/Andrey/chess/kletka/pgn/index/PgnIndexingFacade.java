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

package Khrypach.Andrey.chess.kletka.pgn.index;

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.pgn.index.model.IndexStatus;
import Khrypach.Andrey.chess.kletka.pgn.index.model.IndexingProgress;
import Khrypach.Andrey.chess.kletka.pgn.index.model.PgnIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

/**
 * Фасад для работы с индексацией PGN файлов.
 * Объединяет PgnFilePreparer, PgnFileIndexer, PgnIndexManager.
 */
public class PgnIndexingFacade {
    private static final Logger log = LoggerFactory.getLogger(PgnIndexingFacade.class);
    private static final LanguageManager lang = LanguageManager.getInstance();

    private final PgnFilePreparer preparer;
    private final PgnFileIndexer indexer;
    private final PgnIndexManager indexManager;

    public PgnIndexingFacade() {
        this.preparer = new PgnFilePreparer();
        this.indexer = new PgnFileIndexer();
        this.indexManager = new PgnIndexManager();
    }

    /**
     * Выполняет полную индексацию PGN файла
     * Шаги:
     * 1. Подготовка файла (добавление [Deleted]) с прогрессом
     * 2. Создание индекса с прогрессом
     * 3. Сохранение индекса
     */
    public PgnIndex indexFile(Path pgnPath, Consumer<IndexingProgress> progressCallback) throws IOException {
        log.info("Starting full indexing of: {}", pgnPath);

        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .status(lang.get(INDEXING_STEP1))
                    .build());
        }

        Path preparedPath = preparer.prepareFile(pgnPath, progressCallback);

        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .status(lang.get(INDEXING_STEP2))
                    .build());
        }

        PgnIndex index = indexer.indexFile(preparedPath, progressCallback);

        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .status(lang.get(INDEXING_STEP3))
                    .build());
        }

        indexManager.saveIndex(preparedPath, index);

        if (progressCallback != null) {
            progressCallback.accept(IndexingProgress.builder()
                    .status(lang.get(INDEXING_COMPLETE_SUCCESS))
                    .processedGames(index.getGameCount())
                    .totalGames(index.getGameCount())
                    .build());
        }

        log.info("Indexing completed: {}", index);
        return index;
    }

    /**
     * Проверяет состояние индекса
     */
    public IndexStatus checkIndex(Path pgnPath) {
        return indexManager.checkIndex(pgnPath);
    }
}