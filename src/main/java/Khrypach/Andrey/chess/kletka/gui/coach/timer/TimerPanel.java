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

import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Optional;

import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

/**
 * Панель шахматного таймера (четырехугольник с продолжением для кнопок)
 */
public class TimerPanel extends VBox {

    private static final String TIMER_BG_COLOR = "#2c2c2c";
    private static final String TIMER_BORDER_COLOR = "#ffd700";
    private static final String BUTTON_BG_COLOR = "#4a4a4a";
    private static final String BUTTON_HOVER_COLOR = "#666666";

    private final LanguageManager lang = LanguageManager.getInstance();

    private Text timeText;
    private final ChessTimer timer;
    private Button startPauseButton;
    private Button settingsButton;
    private Timeline blinkingTimeline;
    private int blinkCount = 0;
    private static final int MAX_BLINKS = 4; // 4 изменения цвета = 2 полных мигания

    public TimerPanel() {
        timer = new ChessTimer(300); // По умолчанию 5 минут (300 секунд)
        setupUI();
        setupTimerCallbacks();
    }

    private void setupUI() {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(10, 15, 10, 15));
        setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-border-color: %s; " +
                        "-fx-border-width: 3; " +
                        "-fx-border-radius: 10; " +
                        "-fx-background-radius: 10;",
                TIMER_BG_COLOR, TIMER_BORDER_COLOR
        ));
        setEffect(new DropShadow(5, Color.BLACK));

        timeText = new Text("05:00");
        timeText.setFont(Font.font("Courier New", 28));
        timeText.setFill(Color.WHITE);
        timeText.setStyle("-fx-font-weight: bold;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        startPauseButton = createStyledButton("▶");
        startPauseButton.setOnAction(e -> toggleTimer());

        settingsButton = createStyledButton("⏰");
        settingsButton.setOnAction(e -> showTimeSettingsDialog());

        buttonBox.getChildren().addAll(startPauseButton, settingsButton);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(timeText, spacer, buttonBox);

        setMinWidth(120);
        setPrefWidth(140);
        setMaxWidth(180);
    }

    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-min-width: 40px; " +
                        "-fx-min-height: 40px; " +
                        "-fx-max-width: 40px; " +
                        "-fx-max-height: 40px; " +
                        "-fx-border-radius: 20; " +
                        "-fx-background-radius: 20; " +
                        "-fx-cursor: hand;",
                BUTTON_BG_COLOR
        ));

        button.setOnMouseEntered(e ->
                button.setStyle(button.getStyle().replace(BUTTON_BG_COLOR, BUTTON_HOVER_COLOR))
        );
        button.setOnMouseExited(e ->
                button.setStyle(button.getStyle().replace(BUTTON_HOVER_COLOR, BUTTON_BG_COLOR))
        );

        return button;
    }

    private void setupTimerCallbacks() {
        timer.setOnTick(seconds -> javafx.application.Platform.runLater(() -> updateTimeDisplay(seconds)));

        timer.setOnTimeOut(() -> javafx.application.Platform.runLater(() -> {
            startPauseButton.setText("▶");
            startBlinkingTwoTimes();
        }));
    }

    private void updateTimeDisplay(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;

        if (minutes > 60) {
            minutes = 60;
            secs = 0;
        }

        String timeStr = String.format("%02d:%02d", minutes, secs);
        timeText.setText(timeStr);

        if (seconds > 0 && blinkingTimeline != null && blinkingTimeline.getStatus() == Animation.Status.RUNNING) {
            stopBlinking();
        }
    }

    private void startBlinkingTwoTimes() {
        if (blinkingTimeline != null && blinkingTimeline.getStatus() == Animation.Status.RUNNING) {
            blinkingTimeline.stop();
        }

        blinkCount = 0;
        timeText.setFill(Color.RED);

        blinkingTimeline = new Timeline(
                new KeyFrame(Duration.millis(300), e -> {
                    blinkCount++;

                    if (timeText.getFill() == Color.RED) {
                        timeText.setFill(Color.WHITE);
                    } else {
                        timeText.setFill(Color.RED);
                    }

                    if (blinkCount >= MAX_BLINKS) {
                        blinkingTimeline.stop();
                        timeText.setFill(Color.WHITE);
                        blinkingTimeline = null;
                    }
                })
        );
        blinkingTimeline.setCycleCount(MAX_BLINKS);
        blinkingTimeline.play();
    }

    private void stopBlinking() {
        if (blinkingTimeline != null) {
            blinkingTimeline.stop();
            blinkingTimeline = null;
        }
        blinkCount = 0;
        timeText.setFill(Color.WHITE);
    }

    private void toggleTimer() {
        if (timer.isRunning()) {
            timer.pause();
            startPauseButton.setText("▶");
            stopBlinking();
        } else {
            if (timer.getRemainingSeconds() <= 0) {
                resetToCurrentPreset();
            }
            timer.start();
            startPauseButton.setText("⏸");
        }
    }

    private void resetToCurrentPreset() {
        int currentTotal = timer.getRemainingSeconds();
        if (currentTotal <= 0) {
            currentTotal = 300;
        }
        timer.setTime(currentTotal);
        stopBlinking();
    }

    private void showTimeSettingsDialog() {
        javafx.stage.Popup popup = new javafx.stage.Popup();
        VBox popupContent = new VBox(8);
        popupContent.setStyle(
                "-fx-background-color: #3a3a3a; " +
                        "-fx-border-color: #ffd700; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8; " +
                        "-fx-padding: 10;"
        );
        popupContent.setEffect(new DropShadow(5, Color.BLACK));

        Text title = new Text(lang.get(TIMER_SELECT_TIME));
        title.setFill(Color.WHITE);
        title.setFont(Font.font(14));

        HBox presetButtons = new HBox(8);
        presetButtons.setAlignment(Pos.CENTER);

        int[] presets = {60, 120, 180, 300, 600};
        String[] presetLabels = {
                lang.get(TIMER_PRESET_1_MIN),
                lang.get(TIMER_PRESET_2_MIN),
                lang.get(TIMER_PRESET_3_MIN),
                lang.get(TIMER_PRESET_5_MIN),
                lang.get(TIMER_PRESET_10_MIN)
        };

        for (int i = 0; i < presets.length; i++) {
            final int seconds = presets[i];
            Button presetBtn = new Button(presetLabels[i]);
            presetBtn.setStyle(createButtonStyle());
            presetBtn.setOnAction(e -> {
                setTimerTime(seconds);
                popup.hide();
            });
            presetButtons.getChildren().add(presetBtn);
        }

        Button customBtn = new Button("✋");
        customBtn.setStyle(createButtonStyle());
        customBtn.setTooltip(new Tooltip(lang.get(TIMER_CUSTOM_TOOLTIP)));
        customBtn.setOnAction(e -> {
            popup.hide();
            showCustomTimeDialog();
        });

        HBox customBox = new HBox(8);
        customBox.setAlignment(Pos.CENTER);
        customBox.getChildren().add(customBtn);

        popupContent.getChildren().addAll(title, presetButtons, customBox);
        popup.getContent().add(popupContent);

        popup.show(settingsButton.getScene().getWindow(),
                settingsButton.localToScreen(0, 0).getX(),
                settingsButton.localToScreen(0, 0).getY() - popupContent.getHeight());

        popupContent.setOnMouseExited(e -> {
            if (!popupContent.isHover() && !settingsButton.isHover()) {
                popup.hide();
            }
        });
    }

    private String createButtonStyle() {
        return
                "-fx-background-color: #5a5a5a; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 12px; " +
                        "-fx-padding: 8 12; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand;";
    }

    private void showCustomTimeDialog() {
        TextInputDialog dialog = new TextInputDialog("300");
        dialog.setTitle(lang.get(TIMER_CUSTOM_TITLE));
        dialog.setHeaderText(lang.get(TIMER_CUSTOM_HEADER));
        dialog.setContentText(lang.get(TIMER_CUSTOM_CONTENT));

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(input -> {
            try {
                int seconds = Integer.parseInt(input);
                if (seconds > 3600) {
                    seconds = 3600;
                    showWarning(lang.get(TIMER_MAX_TIME_WARNING));
                }
                if (seconds < 1) {
                    seconds = 1;
                }
                setTimerTime(seconds);
            } catch (NumberFormatException e) {
                showWarning(lang.get(TIMER_INVALID_NUMBER));
            }
        });
    }

    private void setTimerTime(int seconds) {
        timer.pause();
        startPauseButton.setText("▶");
        timer.setTime(seconds);
        stopBlinking();
        updateTimeDisplay(seconds);
    }

    private void showWarning(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.WARNING
        );
        alert.setTitle(lang.get(NOTIFICATION_WARNING));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void reset() {
        timer.reset();
        if (timer.isRunning()) {
            timer.pause();
            startPauseButton.setText("▶");
        }
        stopBlinking();
        updateTimeDisplay(timer.getRemainingSeconds());
    }

    public void stop() {
        timer.stop();
        startPauseButton.setText("▶");
        stopBlinking();
    }
}