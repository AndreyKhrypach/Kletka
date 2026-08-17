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

import Khrypach.Andrey.chess.kletka.engine.UciEngineManager;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.model.AnalysisInfo;
import Khrypach.Andrey.chess.kletka.gui.model.MoveAnnotation;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys.*;

public class EngineAnalysisPanel extends VBox {

    private static final Logger log = LoggerFactory.getLogger(EngineAnalysisPanel.class);
    private final LanguageManager lang = LanguageManager.getInstance();

    private final UciEngineManager engineManager;
    private final ChessBoardView boardView;
    @Getter
    @Setter
    private NotationView notationView;

    // UI компоненты
    private final Label engineStatusLabel;
    private final Button startStopButton;
    private final ProgressIndicator thinkingIndicator;

    // Компоненты для линий анализа
    private final VBox analysisLinesContainer;
    private final List<AnalysisLine> analysisLines = new ArrayList<>();
    private int currentLinesCount = 1;
    private static final int MAX_LINES = 5;
    private static final int MIN_LINES = 1;

    private final Button addLineButton;
    private final Button removeLineButton;

    private AnimationTimer analysisTimer;
    private  AtomicBoolean isAnalyzing = new AtomicBoolean(false);

    private final Label currentEvaluationLabel;
    private final Label currentDepthLabel;

    public EngineAnalysisPanel(ChessBoardView boardView) {
        this.engineManager = UciEngineManager.getInstance();
        this.boardView = boardView;

        setSpacing(10);
        setPadding(new Insets(15));
        setPrefWidth(400);
        setMinWidth(350);
        setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #8b5a2b; -fx-border-width: 0 0 0 2;");

        // Верхняя панель с заголовком и кнопками управления
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("🔍 " + lang.get(ANALYSIS_TITLE));
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #5a3e1b;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Кнопки + и -
        addLineButton = new Button("+");
        addLineButton.setStyle("-fx-background-color: #2e8b57; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-min-width: 30px; -fx-min-height: 30px;");
        addLineButton.setOnAction(e -> addAnalysisLine());
        addLineButton.setTooltip(new Tooltip(lang.get(ANALYSIS_ADD_LINE_TOOLTIP, MAX_LINES)));

        removeLineButton = new Button("-");
        removeLineButton.setStyle("-fx-background-color: #8b0000; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-min-width: 30px; -fx-min-height: 30px;");
        removeLineButton.setOnAction(e -> removeAnalysisLine());
        removeLineButton.setTooltip(new Tooltip(lang.get(ANALYSIS_REMOVE_LINE_TOOLTIP, MIN_LINES)));
        removeLineButton.setDisable(true);

        titleBox.getChildren().addAll(title, spacer, addLineButton, removeLineButton);

        // Панель статуса
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        engineStatusLabel = new Label("⚙️ " + lang.get(ANALYSIS_ENGINE_STOPPED));
        engineStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ff6b6b; -fx-font-weight: bold;");

        thinkingIndicator = new ProgressIndicator();
        thinkingIndicator.setVisible(false);
        thinkingIndicator.setMaxSize(20, 20);

        startStopButton = new Button("▶");
        startStopButton.setStyle("-fx-background-color: #2e8b57; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-min-width: 60px; -fx-min-height: 30px;");
        startStopButton.setOnAction(e -> toggleAnalysis());
        startStopButton.setTooltip(new Tooltip(lang.get(ANALYSIS_TOGGLE_TOOLTIP)));

        Region statusSpacer = new Region();
        HBox.setHgrow(statusSpacer, Priority.ALWAYS);

        statusBox.getChildren().addAll(engineStatusLabel, thinkingIndicator, statusSpacer, startStopButton);

        // Панель текущей оценки
        HBox evaluationBox = new HBox(10);
        evaluationBox.setAlignment(Pos.CENTER_LEFT);
        evaluationBox.setPadding(new Insets(5, 0, 5, 0));
        evaluationBox.setStyle("-fx-background-color: #e8e0d0; -fx-border-color: #d2b48c; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label evalTitle = new Label(lang.get(ANALYSIS_CURRENT_EVAL) + ":");
        evalTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #5a3e1b;");

        currentEvaluationLabel = new Label("—");
        currentEvaluationLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-min-width: 60px;");

        currentDepthLabel = new Label("");
        currentDepthLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");

        Region evalSpacer = new Region();
        HBox.setHgrow(evalSpacer, Priority.ALWAYS);

        evaluationBox.getChildren().addAll(evalTitle, currentEvaluationLabel, currentDepthLabel, evalSpacer);

        // Контейнер для линий анализа
        analysisLinesContainer = new VBox(5);
        analysisLinesContainer.setPadding(new Insets(5, 0, 5, 0));

        // Добавляем первую линию
        addInitialAnalysisLine();

        // ScrollPane для линий анализа
        ScrollPane linesScrollPane = new ScrollPane(analysisLinesContainer);
        linesScrollPane.setFitToWidth(true);
        linesScrollPane.setPrefHeight(350);
        linesScrollPane.setStyle("-fx-background: #f5f5f5; -fx-background-color: #f5f5f5; -fx-border-color: #d2b48c;");
        linesScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Добавляем все в панель
        getChildren().addAll(
                titleBox,
                new Separator(),
                statusBox,
                new Separator(),
                evaluationBox,
                new Separator(),
                linesScrollPane
        );

        setupAnalysisTimer();
    }

    private void addInitialAnalysisLine() {
        AnalysisLine firstLine = new AnalysisLine(1);
        analysisLines.add(firstLine);
        analysisLinesContainer.getChildren().add(firstLine);
    }

    private void addAnalysisLine() {
        if (currentLinesCount < MAX_LINES) {
            currentLinesCount++;
            int lineNumber = currentLinesCount;
            AnalysisLine newLine = new AnalysisLine(lineNumber);
            analysisLines.add(newLine);
            analysisLinesContainer.getChildren().add(newLine);

            addLineButton.setDisable(currentLinesCount >= MAX_LINES);
            removeLineButton.setDisable(currentLinesCount <= MIN_LINES);

            if (isAnalyzing.get() && engineManager.isEngineRunning()) {
                restartAnalysisWithMultiPV();
            }
        }
    }

    private void removeAnalysisLine() {
        if (currentLinesCount > MIN_LINES && !analysisLines.isEmpty()) {
            analysisLines.remove(analysisLines.size() - 1);
            analysisLinesContainer.getChildren().remove(analysisLinesContainer.getChildren().size() - 1);
            currentLinesCount--;

            addLineButton.setDisable(currentLinesCount >= MAX_LINES);
            removeLineButton.setDisable(currentLinesCount <= MIN_LINES);

            if (isAnalyzing.get() && engineManager.isEngineRunning()) {
                restartAnalysisWithMultiPV();
            }
        }
    }

    private void restartAnalysisWithMultiPV() {
        new Thread(() -> {
            try {
                Platform.runLater(() -> {
                    if (isAnalyzing.get()) {
                        engineManager.stopAnalysis();
                    }
                });

                Thread.sleep(100);

                engineManager.setMultiPV(currentLinesCount);

                Thread.sleep(50);

                Board currentBoard = boardView.getCurrentBoard();
                engineManager.sendPosition(currentBoard);

                // ========== ИСПОЛЬЗУЕМ ГЛУБИНУ 14 ПОЛУХОДОВ (7 ХОДОВ) ==========
                engineManager.startAnalysisWithDepth(14);

                Platform.runLater(() -> {
                    for (AnalysisLine line : analysisLines) {
                        line.clear();
                    }
                });
            } catch (InterruptedException e) {
                log.debug("Analysis restart interrupted");
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void toggleAnalysis() {
        if (!engineManager.isEngineRunning()) {
            showEngineNotRunningAlert();
            return;
        }

        if (isAnalyzing.get()) {
            stopAnalysis();
        } else {
            startAnalysis();
        }
    }

    void startAnalysis() {
        if (!engineManager.isEngineRunning()) {
            showEngineNotRunningAlert();
            return;
        }

        log.debug("Starting analysis with {} lines", currentLinesCount);
        isAnalyzing.set(true);
        String engineDisplayName = engineManager.getEngineName();
        engineStatusLabel.setText("🟢 " + engineDisplayName + ": " + lang.get(ANALYSIS_ANALYZING));
        engineStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2e8b57; -fx-font-weight: bold;");
        startStopButton.setText("⏸");
        startStopButton.setStyle("-fx-background-color: #dc143c; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-min-width: 60px; -fx-min-height: 30px;");
        thinkingIndicator.setVisible(true);

        engineManager.setMultiPV(currentLinesCount);

        Board currentBoard = boardView.getCurrentBoard();
        engineManager.sendPosition(currentBoard);

        engineManager.startAnalysisWithDepth(14);
    }

    private void stopAnalysis() {
        log.debug("Stopping analysis");
        isAnalyzing.set(false);
        engineStatusLabel.setText("⚙️ " + engineManager.getEngineName() + ": " + lang.get(ANALYSIS_ENGINE_STOPPED));
        engineStatusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ff6b6b; -fx-font-weight: bold;");
        engineManager.stopAnalysis();
        startStopButton.setText("▶");
        startStopButton.setStyle("-fx-background-color: #2e8b57; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-min-width: 60px; -fx-min-height: 30px;");
        thinkingIndicator.setVisible(false);

        for (AnalysisLine line : analysisLines) {
            line.clear();
        }
    }

    private void setupAnalysisTimer() {
        analysisTimer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (isAnalyzing.get() && (now - lastUpdate) > 200_000_000) {
                    lastUpdate = now;
                    updateAnalysisInfo();
                }
            }
        };
        analysisTimer.start();
    }

    private void updateAnalysisInfo() {
        if (!isAnalyzing.get()) return;

        Board currentBoard = boardView.getCurrentBoard();
        boolean isWhiteToMove = currentBoard.getSideToMove() == Side.WHITE;

        for (int i = 0; i < analysisLines.size(); i++) {
            int lineNumber = i + 1;
            AnalysisInfo info = engineManager.getAnalysisInfo(lineNumber);

            if (info != null) {
                int rawScore = info.getScore();
                int displayScore = isWhiteToMove ? rawScore : -rawScore;

                String pvToShow = info.getPv();

                // ===== ФИЛЬТРАЦИЯ ДЛЯ UI =====
                // Проверяем, есть ли данные для отображения
                if (pvToShow != null && !pvToShow.isEmpty()) {
                    String[] moves = pvToShow.split(" ");
                    int depth = info.getDepth();
                    int score = info.getScore();

                    // Условия пропуска нестабильного анализа:
                    boolean shouldSkip = false;

                    if (depth > 20 && score == 0 && moves.length < 3) {
                        shouldSkip = true;
                        log.trace("Skipping unstable analysis: depth={}, score=0, moves={}",
                                depth, moves.length);
                    } else if (depth > 25 && moves.length < 3) {
                        shouldSkip = true;
                        log.trace("Skipping incomplete PV at depth {}: {} moves",
                                depth, moves.length);
                    } else if (depth > 15 && moves.length < 2) {
                        shouldSkip = true;
                        log.trace("Skipping too short PV at depth {}: {} moves",
                                depth, moves.length);
                    }

                    if (shouldSkip) {
                        continue; // Не обновляем, оставляем предыдущее значение
                    }

                    // ===== ОБНОВЛЕНИЕ UI =====
                    analysisLines.get(i).update(
                            pvToShow,
                            info.getDepth(),
                            displayScore,
                            info.isScoreIsMate(),
                            currentBoard
                    );

                } else if (info.getCurrMove() != null && !info.getCurrMove().isEmpty()) {
                    // PV пустой, но есть currmove - показываем как fallback
                    pvToShow = "🔍 " + info.getCurrMove() + " ...";
                    analysisLines.get(i).update(
                            pvToShow,
                            info.getDepth(),
                            displayScore,
                            info.isScoreIsMate(),
                            currentBoard
                    );
                } else {
                    // Нет данных для отображения - очищаем линию
                    analysisLines.get(i).update(
                            "",
                            info.getDepth(),
                            displayScore,
                            info.isScoreIsMate(),
                            currentBoard
                    );
                }

                // ===== ОБНОВЛЕНИЕ EVALUATION LABEL (для первой линии) =====
                if (lineNumber == 1) {
                    MoveAnnotation evalSymbol = scoreToEvaluationSymbol(displayScore, info.isScoreIsMate());
                    currentEvaluationLabel.setText(evalSymbol.getSymbol() + " " +
                            formatScoreNumber(displayScore, info.isScoreIsMate()));

                    if (displayScore > 50) {
                        currentEvaluationLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-min-width: 80px; -fx-text-fill: #2e8b57;");
                    } else if (displayScore < -50) {
                        currentEvaluationLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-min-width: 80px; -fx-text-fill: #dc143c;");
                    } else {
                        currentEvaluationLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-min-width: 80px; -fx-text-fill: #ffa500;");
                    }

                    String depthInfo = "";
                    if (info.getDepth() > 0) {
                        depthInfo = "(" + lang.get(ANALYSIS_DEPTH) + ": " + info.getDepth();
                        if (info.getSelDepth() > 0) {
                            depthInfo += "/" + info.getSelDepth();
                        }
                        depthInfo += ")";
                    }
                    currentDepthLabel.setText(depthInfo);
                }
            }
        }
    }

    /**
     * Форматирует числовое значение оценки
     */
    private String formatScoreNumber(int scoreCp, boolean isMate) {
        if (isMate) {
            int mateIn = scoreCp > 0 ? 30000 - scoreCp : -30000 - scoreCp;
            return lang.get(ANNOTATION_MATE) + " " + mateIn;
        }
        double score = scoreCp / 100.0;
        if (score > 0) {
            return String.format("+%.2f", score);
        } else if (score < 0) {
            return String.format("%.2f", score);
        } else {
            return "0.00";
        }
    }

    /**
     * Получает текущий полуход из навигационного контроллера
     */
    private int getCurrentPlyFromNavigation() {
        if (boardView != null && boardView.getNavController() != null) {
            return boardView.getNavController().getCurrentTotalPly();
        }
        return 0;
    }

    /**
     * Конвертирует оценку в шахматный символ из MoveAnnotation
     */
    private MoveAnnotation scoreToEvaluationSymbol(int scoreCp, boolean isMate) {
        if (isMate) {
            return scoreCp > 0 ? MoveAnnotation.WINNING_WHITE : MoveAnnotation.WINNING_BLACK;
        }

        double score = scoreCp / 100.0;

        if (Math.abs(score) < 0.35) {
            return MoveAnnotation.EQUALITY;
        } else if (score >= 0.35 && score < 0.7) {
            return MoveAnnotation.SLIGHT_ADVANTAGE_WHITE;
        } else if (score <= -0.35 && score > -0.7) {
            return MoveAnnotation.SLIGHT_ADVANTAGE_BLACK;
        } else if (score >= 0.7 && score < 1.5) {
            return MoveAnnotation.CLEAR_ADVANTAGE_WHITE;
        } else if (score <= -0.7 && score > -1.5) {
            return MoveAnnotation.CLEAR_ADVANTAGE_BLACK;
        } else if (score >= 1.5) {
            return MoveAnnotation.WINNING_WHITE;
        } else if (score <= -1.5) {
            return MoveAnnotation.WINNING_BLACK;
        }

        return MoveAnnotation.EQUALITY;
    }

    private void showEngineNotRunningAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(lang.get(ANALYSIS_ENGINE_NOT_RUNNING_TITLE));
        alert.setHeaderText(lang.get(ANALYSIS_ENGINE_NOT_RUNNING_HEADER));
        alert.setContentText(lang.get(ANALYSIS_ENGINE_NOT_RUNNING_CONTENT));
        alert.showAndWait();
    }

    public void onPositionChanged() {
        if (isAnalyzing.get() && engineManager.isEngineRunning()) {
            log.trace("Position changed, clearing analysis lines");
            Platform.runLater(() -> {
                for (AnalysisLine line : analysisLines) {
                    line.clear();
                }
            });
        }
    }

    public void shutdown() {
        log.debug("Shutting down analysis panel");
        if (analysisTimer != null) {
            analysisTimer.stop();
        }
        if (isAnalyzing.get()) {
            stopAnalysis();
        }
    }

    public String getBestMoveFromAnalysis() {
        if (!isAnalyzing.get() || !engineManager.isEngineRunning()) {
            return null;
        }

        AnalysisInfo info = engineManager.getAnalysisInfo(1);
        if (info != null && info.getPv() != null && !info.getPv().isEmpty()) {
            String[] pvMoves = info.getPv().split(" ");
            if (pvMoves.length > 0) {
                return pvMoves[0];
            }
        }

        return engineManager.getLastBestMove();
    }

    public void toggleAnalysisByKey() {
        toggleAnalysis();
    }

    public boolean isAnalyzingActive() {
        return isAnalyzing.get();
    }

    /**
     * Класс для отображения одной линии анализа (компактная версия)
     */
    private class AnalysisLine extends HBox {
        private static final Logger log = LoggerFactory.getLogger(AnalysisLine.class);
        private final Label evaluationLabel;
        private final Label depthLabel;
        private final TextFlow pvTextFlow;

        public AnalysisLine(int number) {
            setSpacing(8);
            setPadding(new Insets(6, 8, 6, 8));
            setAlignment(Pos.CENTER_LEFT);
            setStyle("-fx-background-color: white; -fx-border-color: #d2b48c; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");

            Label numberLabel = new Label(number + ".");
            numberLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-min-width: 25px; -fx-text-fill: #5a3e1b;");

            evaluationLabel = new Label("—");
            evaluationLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-min-width: 45px;");
            evaluationLabel.setAlignment(Pos.CENTER_RIGHT);

            depthLabel = new Label("");
            depthLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888; -fx-min-width: 45px;");

            pvTextFlow = new TextFlow();
            pvTextFlow.setLineSpacing(2);
            HBox.setHgrow(pvTextFlow, Priority.ALWAYS);

            getChildren().addAll(numberLabel, evaluationLabel, depthLabel, pvTextFlow);
        }

        public void update(String pv, int depth, int score, boolean isMate, Board board) {
            Platform.runLater(() -> {
                MoveAnnotation evalSymbol = scoreToEvaluationSymbol(score, isMate);
                evaluationLabel.setText(evalSymbol.getSymbol());

                if (score > 50) {
                    evaluationLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-min-width: 45px; -fx-text-fill: #2e8b57;");
                } else if (score < -50) {
                    evaluationLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-min-width: 45px; -fx-text-fill: #dc143c;");
                } else {
                    evaluationLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-min-width: 45px; -fx-text-fill: #ffa500;");
                }

                if (depth > 0) {
                    depthLabel.setText("(" + depth + ")");
                } else {
                    depthLabel.setText("");
                }

                if (pv != null && !pv.isEmpty()) {
                    displayPv(pv, board);
                } else {
                    pvTextFlow.getChildren().clear();
                    Text emptyText = new Text("—");
                    emptyText.setStyle("-fx-fill: #999; -fx-font-size: 11px;");
                    pvTextFlow.getChildren().add(emptyText);
                }
            });
        }

        /**
         * Отображает PV (Principal Variation) с правильной нумерацией
         */
        private void displayPv(String pv, Board board) {
            if (pv == null || pv.isEmpty()) {
                return;
            }

            String[] moves = pv.split(" ");
            pvTextFlow.getChildren().clear();

            Board tempBoard = board.clone();
            int currentPly = getCurrentPlyFromNavigation();
            boolean isWhiteToMove = tempBoard.getSideToMove() == Side.WHITE;

            int currentMoveNumber = (currentPly / 2) + 1;
            boolean nextIsWhite = isWhiteToMove;

            for (int i = 0; i < Math.min(moves.length, 14); i++) {
                String uciMove = moves[i];
                boolean isWhiteMove = nextIsWhite;

                if (isWhiteMove) {
                    Text numberText = new Text(currentMoveNumber + ".");
                    numberText.setStyle("-fx-font-weight: bold; -fx-fill: #5a3e1b; -fx-font-size: 11px;");
                    pvTextFlow.getChildren().add(numberText);
                    pvTextFlow.getChildren().add(new Text(" "));
                } else if (i == 0) {
                    Text dotsText = new Text(currentMoveNumber + "...");
                    dotsText.setStyle("-fx-font-weight: bold; -fx-fill: #5a3e1b; -fx-font-size: 11px;");
                    pvTextFlow.getChildren().add(dotsText);
                    pvTextFlow.getChildren().add(new Text(" "));
                }

                String chessNotation = engineManager.convertUciToChessNotation(uciMove, tempBoard);
                String cleanNotation = chessNotation.replaceFirst("^\\?", "");

                Text moveText = new Text(cleanNotation);
                moveText.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 11px; -fx-fill: #2c3e50;");
                pvTextFlow.getChildren().add(moveText);
                pvTextFlow.getChildren().add(new Text(" "));

                try {
                    Move move = new Move(
                            Square.valueOf(uciMove.substring(0, 2).toUpperCase()),
                            Square.valueOf(uciMove.substring(2, 4).toUpperCase())
                    );
                    tempBoard.doMove(move);
                } catch (Exception e) {
                    log.trace("Error applying move in PV display: {}", uciMove);
                }

                if (isWhiteMove) {
                    currentMoveNumber++;
                }
                nextIsWhite = !isWhiteMove;
            }

            if (moves.length > 20) {
                Text dotsText = new Text("...");
                dotsText.setStyle("-fx-font-style: italic; -fx-fill: #999; -fx-font-size: 11px;");
                pvTextFlow.getChildren().add(dotsText);
            }
        }

        public void clear() {
            Platform.runLater(() -> {
                evaluationLabel.setText("—");
                depthLabel.setText("");
                pvTextFlow.getChildren().clear();
                Text emptyText = new Text("—");
                emptyText.setStyle("-fx-fill: #999; -fx-font-size: 11px;");
                pvTextFlow.getChildren().add(emptyText);
            });
        }
    }
}