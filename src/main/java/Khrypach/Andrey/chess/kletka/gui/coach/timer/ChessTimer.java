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

package Khrypach.Andrey.chess.kletka.gui.coach.timer;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

/**
 * Шахматный таймер для тренировок
 */
public class ChessTimer {
    private int totalSeconds;
    @Getter
    private int remainingSeconds;
    private Timeline timeline;
    @Getter
    private boolean isRunning = false;
    @Setter
    private Consumer<Integer> onTick; // колбэк при каждом тике (секунда)
    @Setter
    private Runnable onTimeOut; // колбэк при достижении 00:00

    public ChessTimer(int initialSeconds) {
        this.totalSeconds = initialSeconds;
        this.remainingSeconds = initialSeconds;
    }

    public void start() {
        if (isRunning) return;
        if (remainingSeconds <= 0) return;

        isRunning = true;
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    public void pause() {
        if (!isRunning) return;
        if (timeline != null) {
            timeline.stop();
        }
        isRunning = false;
    }

    public void reset() {
        pause();
        remainingSeconds = totalSeconds;
        if (onTick != null) {
            onTick.accept(remainingSeconds);
        }
    }

    public void setTime(int seconds) {
        this.totalSeconds = seconds;
        this.remainingSeconds = seconds;
        if (onTick != null) {
            onTick.accept(remainingSeconds);
        }
    }

    private void tick() {
        if (remainingSeconds > 0) {
            remainingSeconds--;
            if (onTick != null) {
                onTick.accept(remainingSeconds);
            }
        }

        if (remainingSeconds <= 0) {
            pause();
            if (onTimeOut != null) {
                onTimeOut.run();
            }
        }
    }

    public void stop() {
        pause();
    }
}