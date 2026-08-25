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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BatchOperationResult - Результат пакетной операции")
class BatchOperationResultTest {

    @Test
    @DisplayName("Должен создавать результат с полным успехом")
    void shouldCreateResultWithFullSuccess() {
        // when
        BatchOperationResult result = new BatchOperationResult(10, 10, 0, List.of(), "All games processed successfully");

        // then
        assertThat(result.totalRequested()).isEqualTo(10);
        assertThat(result.successful()).isEqualTo(10);
        assertThat(result.failed()).isEqualTo(0);
        assertThat(result.failedIds()).isEmpty();
        assertThat(result.message()).isEqualTo("All games processed successfully");
        assertThat(result.isComplete()).isTrue();
    }

    @Test
    @DisplayName("Должен создавать результат с частичным успехом")
    void shouldCreateResultWithPartialSuccess() {
        // given
        List<Integer> failedIds = List.of(3, 7, 9);

        // when
        BatchOperationResult result = new BatchOperationResult(10, 7, 3, failedIds, "Some games failed");

        // then
        assertThat(result.totalRequested()).isEqualTo(10);
        assertThat(result.successful()).isEqualTo(7);
        assertThat(result.failed()).isEqualTo(3);
        assertThat(result.failedIds()).containsExactly(3, 7, 9);
        assertThat(result.message()).isEqualTo("Some games failed");
        assertThat(result.isComplete()).isFalse();
    }

    @Test
    @DisplayName("Должен создавать результат с полным провалом")
    void shouldCreateResultWithFullFailure() {
        // given
        List<Integer> failedIds = List.of(1, 2, 3, 4, 5);

        // when
        BatchOperationResult result = new BatchOperationResult(5, 0, 5, failedIds, "All games failed");

        // then
        assertThat(result.totalRequested()).isEqualTo(5);
        assertThat(result.successful()).isEqualTo(0);
        assertThat(result.failed()).isEqualTo(5);
        assertThat(result.failedIds()).containsExactly(1, 2, 3, 4, 5);
        assertThat(result.message()).isEqualTo("All games failed");
        assertThat(result.isComplete()).isFalse();
    }

    @Test
    @DisplayName("Должен обрабатывать null как пустой список failedIds")
    void shouldHandleNullFailedIds() {
        // when
        BatchOperationResult result = new BatchOperationResult(5, 5, 0, null, "All good");

        // then
        assertThat(result.totalRequested()).isEqualTo(5);
        assertThat(result.successful()).isEqualTo(5);
        assertThat(result.failed()).isEqualTo(0);
        assertThat(result.failedIds()).isEmpty();
        assertThat(result.message()).isEqualTo("All good");
        assertThat(result.isComplete()).isTrue();
    }

    @Test
    @DisplayName("Должен создавать результат с пустым списком failedIds")
    void shouldCreateResultWithEmptyFailedIds() {
        // given
        List<Integer> emptyList = List.of();

        // when
        BatchOperationResult result = new BatchOperationResult(3, 3, 0, emptyList, "Success");

        // then
        assertThat(result.totalRequested()).isEqualTo(3);
        assertThat(result.successful()).isEqualTo(3);
        assertThat(result.failed()).isEqualTo(0);
        assertThat(result.failedIds()).isEmpty();
        assertThat(result.isComplete()).isTrue();
    }

    @Test
    @DisplayName("Должен иметь хорошее строковое представление")
    void shouldHaveGoodStringRepresentation() {
        // given
        BatchOperationResult result = new BatchOperationResult(10, 7, 3, List.of(5, 8), "Test message");

        // when
        String str = result.toString();

        // then
        assertThat(str).contains("7/10");
        assertThat(str).contains("3 failed");
        assertThat(str).contains("Test message");
    }

    @Test
    @DisplayName("Должен корректно возвращать true для isComplete при нулевых ошибках")
    void shouldReturnTrueForIsCompleteWhenNoErrors() {
        // given
        BatchOperationResult success = new BatchOperationResult(10, 10, 0, List.of(), "OK");

        // then
        assertThat(success.isComplete()).isTrue();
    }

    @Test
    @DisplayName("Должен корректно возвращать false для isComplete при наличии ошибок")
    void shouldReturnFalseForIsCompleteWhenHasErrors() {
        // given
        BatchOperationResult partial = new BatchOperationResult(10, 8, 2, List.of(1, 2), "Partial");

        // then
        assertThat(partial.isComplete()).isFalse();
    }

    @Test
    @DisplayName("Должен создавать копию списка failedIds для защиты от изменений")
    void shouldCreateCopyOfFailedIdsList() {
        // given
        List<Integer> originalList = new ArrayList<>();
        originalList.add(1);
        originalList.add(2);

        // when
        BatchOperationResult result = new BatchOperationResult(2, 0, 2, originalList, "Failed");

        // then
        assertThat(result.failedIds()).containsExactly(1, 2);

        // Изменяем оригинальный список
        originalList.add(3);

        // Проверяем, что внутренний список не изменился
        assertThat(result.failedIds()).containsExactly(1, 2);
        assertThat(result.failedIds()).doesNotContain(3);
    }
}