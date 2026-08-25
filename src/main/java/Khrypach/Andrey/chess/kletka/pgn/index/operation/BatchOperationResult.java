/*
 *
 *  * Copyright (c) 2024 Andrey Khrypach
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

package Khrypach.Andrey.chess.kletka.pgn.index.operation;

import java.util.ArrayList;
import java.util.List;

public record BatchOperationResult(int totalRequested, int successful, int failed, List<Integer> failedIds,
                                   String message) {
    public BatchOperationResult(int totalRequested, int successful, int failed,
                                List<Integer> failedIds, String message) {
        this.totalRequested = totalRequested;
        this.successful = successful;
        this.failed = failed;
        this.failedIds = failedIds != null ? new ArrayList<>(failedIds) : new ArrayList<>();
        this.message = message;
    }

    public boolean isComplete() {
        return failed == 0;
    }

    @Override
    public String toString() {
        return String.format("BatchResult: %d/%d successful, %d failed. %s",
                successful, totalRequested, failed, message);
    }
}
