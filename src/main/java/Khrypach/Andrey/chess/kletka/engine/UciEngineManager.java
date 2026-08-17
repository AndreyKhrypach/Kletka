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

package Khrypach.Andrey.chess.kletka.engine;

import Khrypach.Andrey.chess.kletka.gui.board.ChessSymbols;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.model.AnalysisInfo;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveConversionException;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static Khrypach.Andrey.chess.kletka.engine.UciConstants.*;

public class UciEngineManager {

    private static final Logger log = LoggerFactory.getLogger(UciEngineManager.class);
    private static final int ENGINE_INIT_TIMEOUT_SECONDS = 5;

    private static volatile UciEngineManager instance;

    private final LanguageManager languageManager = LanguageManager.getInstance();
    private final Map<Integer, AnalysisInfo> multiPvInfo = new HashMap<>();

    // Атомарные переменные для потокобезопасности
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicReference<Process> engineProcess = new AtomicReference<>();
    private final AtomicReference<BufferedWriter> engineWriter = new AtomicReference<>();
    private final AtomicReference<BufferedReader> engineReader = new AtomicReference<>();
    private final AtomicReference<Thread> readerThread = new AtomicReference<>();

    // CompletableFuture для синхронизации инициализации
    private CompletableFuture<Void> uciInitFuture;
    private CompletableFuture<Void> readyInitFuture;
    private CompletableFuture<String> currentBestMoveFuture = null;

    // Состояние движка
    @Getter
    private String engineName = "UCI Engine";
    private String bestMoveResult;

    @Getter
    private String lastBestMove = null;
    @Getter
    private String lastPv = "";
    @Getter
    private int lastDepth = 0;
    @Getter
    private int lastScore = 0;
    @Getter
    private int lastSeldepth = 0;
    @Getter
    private int lastNodes = 0;
    @Getter
    private int lastNps = 0;
    @Getter
    private int lastTime = 0;
    @Getter
    private int currentMultiPv = 1;

    public static UciEngineManager getInstance() {
        if (instance == null) {
            synchronized (UciEngineManager.class) {
                if (instance == null) {
                    instance = new UciEngineManager();
                }
            }
        }
        return instance;
    }

    private UciEngineManager() {
        log.debug("UciEngineManager singleton created");
    }

    /**
     * Запускает движок с указанным путем
     */
    public void startEngine(String enginePath) throws IOException {
        if (isEngineRunning()) {
            stopEngine();
        }

        log.info("Starting engine: {}", enginePath);

        ProcessBuilder processBuilder = new ProcessBuilder(enginePath);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        engineProcess.set(process);

        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(process.getOutputStream())
        );
        BufferedWriter oldWriter = engineWriter.getAndSet(writer);
        closeQuietly(oldWriter);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
        );
        BufferedReader oldReader = engineReader.getAndSet(reader);
        closeQuietly(oldReader);

        running.set(true);

        // Запускаем читающий поток
        Thread thread = new Thread(this::readEngineOutput);
        thread.setDaemon(true);
        thread.setName("Stockfish-Reader");
        Thread oldThread = readerThread.getAndSet(thread);
        if (oldThread != null && oldThread.isAlive()) {
            oldThread.interrupt();
        }
        thread.start();

        // Инициализация с использованием CompletableFuture
        initializeEngine();
    }

    /**
     * Инициализация движка с ожиданием uciok и readyok
     */
    private void initializeEngine() throws IOException {
        log.debug("Initializing engine...");

        // Создаем CompletableFuture для uciok
        uciInitFuture = new CompletableFuture<>();
        readyInitFuture = new CompletableFuture<>();

        // Отправляем UCI команду
        sendCommand(UCI);

        try {
            // Ждем uciok с таймаутом
            uciInitFuture.get(ENGINE_INIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            log.info("Engine responded with uciok");

            // Отправляем isready и ждем readyok
            sendCommand(IS_READY);
            readyInitFuture.get(ENGINE_INIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            log.info("Engine responded with readyok");

            // Устанавливаем MultiPV по умолчанию
            setMultiPV(1);
            log.info("Engine initialized and ready");

        } catch (TimeoutException e) {
            log.error("Engine initialization timeout");
            throw new IOException("Engine initialization timeout", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Engine initialization interrupted", e);
        } catch (ExecutionException e) {
            log.error("Engine initialization failed", e);
            throw new IOException("Engine initialization failed", e.getCause());
        }
    }

    /**
     * Читающий поток для обработки вывода движка
     */
    private void readEngineOutput() {
        BufferedReader reader = engineReader.get();
        if (reader == null) {
            log.error("Engine reader is null, exiting reader thread");
            return;
        }

        log.debug("Engine reader thread started");

        try {
            String line;
            while (running.get() && !Thread.currentThread().isInterrupted() &&
                    (line = reader.readLine()) != null) {

                log.trace("Received: {}", line);

                if (line.startsWith(BEST_MOVE)) {
                    bestMoveResult = line.substring(BEST_MOVE.length()).trim();
                    log.info("Received bestmove: {}", bestMoveResult);

                    // Завершаем ожидающий future
                    CompletableFuture<String> future = currentBestMoveFuture;
                    if (future != null && !future.isDone()) {
                        future.complete(bestMoveResult);
                        currentBestMoveFuture = null;
                    }

                } else if (line.startsWith(INFO)) {
                    parseMultiPvInfo(line);

                } else if (line.startsWith(ID_NAME)) {
                    String name = line.substring(8).trim();
                    if (!name.isEmpty()) {
                        engineName = name;
                        log.info("Engine name: {}", engineName);
                    }

                } else if (line.equals(UCI_OK)) {
                    log.debug("Received uciok");
                    if (uciInitFuture != null && !uciInitFuture.isDone()) {
                        uciInitFuture.complete(null);
                    }

                } else if (line.equals(READY_OK)) {
                    log.debug("Received readyok");
                    if (readyInitFuture != null && !readyInitFuture.isDone()) {
                        readyInitFuture.complete(null);
                    }
                }
            }
        } catch (IOException e) {
            if (running.get() && !Thread.currentThread().isInterrupted()) {
                log.error("Error reading engine output: {}", e.getMessage());
            } else {
                log.debug("Reader thread interrupted");
            }
        } catch (Exception e) {
            log.error("Unexpected error in reader thread: {}", e.getMessage(), e);
        } finally {
            log.debug("Engine reader thread finished");

            if (uciInitFuture != null && !uciInitFuture.isDone()) {
                uciInitFuture.completeExceptionally(new IOException("Reader thread stopped"));
            }
            if (readyInitFuture != null && !readyInitFuture.isDone()) {
                readyInitFuture.completeExceptionally(new IOException("Reader thread stopped"));
            }

            // Завершаем currentBestMoveFuture если он еще активен
            CompletableFuture<String> future = currentBestMoveFuture;
            if (future != null && !future.isDone()) {
                future.completeExceptionally(new IOException("Reader thread stopped"));
                currentBestMoveFuture = null;
            }
        }
    }

    /**
     * Отправляет команду движку
     */
    public void sendCommand(String command) throws IOException {
        BufferedWriter writer = engineWriter.get();
        if (writer == null) {
            log.warn("Cannot send command '{}' - engineWriter is null", command);
            return;
        }
        writer.write(command + "\n");
        writer.flush();
        log.trace("Sent command: {}", command);
    }

    /**
     * Устанавливает MultiPV
     */
    public void setMultiPV(int lines) {
        this.currentMultiPv = lines;
        try {
            sendCommand(SET_OPTION + " " + SET_OPTION_NAME + " " + MULTI_PV +
                    " " + SET_OPTION_VALUE + " " + lines);
            log.debug("MultiPV set to {}", lines);
        } catch (IOException e) {
            log.error("Failed to set MultiPV: {}", e.getMessage());
        }
    }

    /**
     * Отправляет позицию на доске
     */
    public void sendPosition(Board board) {
        try {
            String fen = board.getFen();
            log.trace("Sending position: {}", fen);
            sendCommand(POSITION + " " + POSITION_FEN + " " + fen);
        } catch (IOException e) {
            log.error("{}: {}",
                    languageManager.get(LanguageKeys.ENGINE_SEND_POSITION_ERROR),
                    e.getMessage()
            );
        }
    }

    /**
     * Запускает анализ с ограничением по глубине
     */
    public void startAnalysisWithDepth(int depth) {
        try {
            sendCommand(GO + " " + GO_DEPTH + " " + depth);
            log.info("Analysis with depth {} started", depth);
        } catch (IOException e) {
            log.error("Failed to start analysis with depth: {}", e.getMessage());
        }
    }

    /**
     * Запускает анализ с ограничением по времени
     */
    public void startAnalysisWithTime(int moveTimeMs) {
        try {
            sendCommand(GO + " " + GO_MOVE_TIME + " " + moveTimeMs);
            log.info("Analysis with time {} ms started", moveTimeMs);
        } catch (IOException e) {
            log.error("Failed to start analysis with time: {}", e.getMessage());
        }
    }

    /**
     * Запускает бесконечный анализ
     */
    public void startInfiniteAnalysis() {
        try {
            sendCommand(GO + " " + GO_INFINITE);
            log.debug("Infinite analysis started");
        } catch (IOException e) {
            log.error("Failed to start infinite analysis: {}", e.getMessage());
        }
    }

    /**
     * Останавливает анализ
     */
    public void stopAnalysis() {
        if (!isEngineRunning()) {
            log.debug("Engine not running, skipping stopAnalysis");
            return;
        }
        try {
            sendCommand(STOP);
            Thread.sleep(50);
            sendCommand(STOP);
        } catch (IOException e) {
            log.error("Failed to stop analysis: {}", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Асинхронно получает лучший ход
     */
    public CompletableFuture<String> getBestMoveAsync(int moveTimeMs) {
        CompletableFuture<String> future = new CompletableFuture<>();
        bestMoveResult = null;
        currentBestMoveFuture = future;

        try {
            sendCommand(GO + " " + GO_MOVE_TIME + " " + moveTimeMs);

            // Устанавливаем тайм-аут
            future.orTimeout(moveTimeMs + 5000, TimeUnit.MILLISECONDS)
                    .exceptionally(throwable -> {
                        // Если future завершился по тайм-ауту, очищаем ссылку
                        if (currentBestMoveFuture == future) {
                            currentBestMoveFuture = null;
                        }

                        if (throwable instanceof TimeoutException) {
                            throw new RuntimeException(
                                    languageManager.get(LanguageKeys.ENGINE_TIMEOUT_ERROR),
                                    throwable
                            );
                        }
                        throw new RuntimeException(throwable);
                    });

        } catch (IOException e) {
            future.completeExceptionally(e);
            if (currentBestMoveFuture == future) {
                currentBestMoveFuture = null;
            }
        }

        return future;
    }

    /**
     * Проверяет, запущен ли движок
     */
    public boolean isEngineRunning() {
        Process process = engineProcess.get();
        return process != null && process.isAlive() && running.get();
    }

    /**
     * Останавливает движок
     */
    public void stopEngine() {
        log.info("Stopping engine...");
        running.set(false);

        // Завершаем CompletableFuture
        if (uciInitFuture != null && !uciInitFuture.isDone()) {
            uciInitFuture.cancel(true);
        }
        if (readyInitFuture != null && !readyInitFuture.isDone()) {
            readyInitFuture.cancel(true);
        }

        // Отправляем QUIT
        try {
            BufferedWriter writer = engineWriter.get();
            if (writer != null) {
                sendCommand(QUIT);
                writer.flush();
            }
        } catch (IOException e) {
            log.debug("Error sending QUIT: {}", e.getMessage());
        }

        // Прерываем читающий поток
        Thread thread = readerThread.get();
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
            try {
                thread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Закрываем ресурсы
        closeQuietly(engineWriter.getAndSet(null));
        closeQuietly(engineReader.getAndSet(null));

        // Уничтожаем процесс
        Process process = engineProcess.getAndSet(null);
        if (process != null && process.isAlive()) {
            process.destroy();
            try {
                if (!process.waitFor(2, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                    process.waitFor(1, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
            }
        }

        log.info("Engine stopped completely");
    }

    /**
     * Закрывает Closeable ресурс без выбрасывания исключения
     */
    private void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                log.trace("Error closing resource: {}", e.getMessage());
            }
        }
    }

    // ==================== Парсинг вывода движка ====================

    private void parseMultiPvInfo(String infoLine) {
        String[] parts = infoLine.split(" ");

        int pvIndex = 1;
        int depth = 0;
        int seldepth = 0;
        int score = 0;
        boolean scoreIsMate = false;
        boolean isLowerbound = false;
        boolean isUpperbound = false;
        String pv = "";
        String currmove = "";
        int currmovenumber = 0;
        int nodes = 0;
        int nps = 0;
        int hashfull = 0;
        int tbhits = 0;
        int time = 0;

        for (int i = 0; i < parts.length; i++) {
            switch (parts[i]) {
                case INFO_MULTIPV:
                    if (i + 1 < parts.length) {
                        try {
                            pvIndex = Integer.parseInt(parts[i + 1]);
                        } catch (NumberFormatException e) {
                            log.warn("Invalid MultiPV number: {}", parts[i + 1]);
                        }
                    }
                    break;

                case INFO_DEPTH:
                    if (i + 1 < parts.length) {
                        try {
                            depth = Integer.parseInt(parts[i + 1]);
                        } catch (NumberFormatException e) {
                            log.warn("Invalid depth number: {}", parts[i + 1]);
                        }
                    }
                    break;

                case SEL_DEPTH:
                    if (i + 1 < parts.length) {
                        try {
                            seldepth = Integer.parseInt(parts[i + 1]);
                        } catch (NumberFormatException e) {
                            log.warn("Invalid seldepth number: {}", parts[i + 1]);
                        }
                    }
                    break;

                case INFO_SCORE:
                    if (i + 2 < parts.length) {
                        switch (parts[i + 1]) {
                            case INFO_CP -> {
                                try {
                                    score = Integer.parseInt(parts[i + 2]);
                                } catch (NumberFormatException e) {
                                    log.warn("Invalid CP score: {}", parts[i + 2]);
                                }
                                scoreIsMate = false;
                            }
                            case INFO_MATE -> {
                                try {
                                    int mateIn = Integer.parseInt(parts[i + 2]);
                                    score = mateIn > 0 ? 30000 - mateIn : -30000 - mateIn;
                                } catch (NumberFormatException e) {
                                    log.warn("Invalid mate number: {}", parts[i + 2]);
                                }
                                scoreIsMate = true;
                            }
                            case "lowerbound" -> {
                                isLowerbound = true;
                                if (i + 3 < parts.length && parts[i + 2].equals(INFO_CP)) {
                                    try {
                                        score = Integer.parseInt(parts[i + 3]);
                                    } catch (NumberFormatException e) {
                                        log.warn("Invalid lowerbound CP score: {}", parts[i + 3]);
                                    }
                                }
                            }
                            case "upperbound" -> {
                                isUpperbound = true;
                                if (i + 3 < parts.length && parts[i + 2].equals(INFO_CP)) {
                                    try {
                                        score = Integer.parseInt(parts[i + 3]);
                                    } catch (NumberFormatException e) {
                                        log.warn("Invalid upperbound CP score: {}", parts[i + 3]);
                                    }
                                }
                            }
                        }
                    }
                    break;

                case INFO_PV:
                    StringBuilder pvBuilder = new StringBuilder();
                    for (int j = i + 1; j < parts.length; j++) {
                        if (parts[j].equals("currmove") || parts[j].equals("currmovenumber") ||
                                parts[j].equals("nodes") || parts[j].equals("nps") ||
                                parts[j].equals("hashfull") || parts[j].equals("tbhits") ||
                                parts[j].equals("time") || parts[j].equals("wdl") ||
                                parts[j].equals("seldepth") || parts[j].equals("depth") ||
                                parts[j].equals("score")) {
                            break;
                        }
                        if (parts[j].matches("[a-h][1-8][a-h][1-8][qrbn]?")) {
                            if (!pvBuilder.isEmpty()) pvBuilder.append(" ");
                            pvBuilder.append(parts[j]);
                        }
                    }
                    pv = pvBuilder.toString();
                    break;

                case "currmove":
                    if (i + 1 < parts.length) {
                        currmove = parts[i + 1];
                    }
                    break;

                case "currmovenumber":
                    if (i + 1 < parts.length) {
                        try {
                            currmovenumber = Integer.parseInt(parts[i + 1]);
                        } catch (NumberFormatException e) {
                            log.warn("Invalid currmovenumber: {}", parts[i + 1]);
                        }
                    }
                    break;

                case "nodes":
                    if (i + 1 < parts.length) {
                        try {
                            nodes = Integer.parseInt(parts[i + 1]);
                        } catch (NumberFormatException e) {
                            log.warn("Invalid nodes number: {}", parts[i + 1]);
                        }
                    }
                    break;

                case "nps":
                    if (i + 1 < parts.length) {
                        try {
                            nps = Integer.parseInt(parts[i + 1]);
                        } catch (NumberFormatException e) {
                            log.warn("Invalid nps number: {}", parts[i + 1]);
                        }
                    }
                    break;

                case "hashfull":
                    if (i + 1 < parts.length) {
                        try {
                            hashfull = Integer.parseInt(parts[i + 1]);
                        } catch (NumberFormatException e) {
                            log.warn("Invalid hashfull number: {}", parts[i + 1]);
                        }
                    }
                    break;

                case "tbhits":
                    if (i + 1 < parts.length) {
                        try {
                            tbhits = Integer.parseInt(parts[i + 1]);
                        } catch (NumberFormatException e) {
                            log.warn("Invalid tbhits number: {}", parts[i + 1]);
                        }
                    }
                    break;

                case "time":
                    if (i + 1 < parts.length) {
                        try {
                            time = Integer.parseInt(parts[i + 1]);
                        } catch (NumberFormatException e) {
                            log.warn("Invalid time number: {}", parts[i + 1]);
                        }
                    }
                    break;

                case "wdl":
                    if (i + 1 < parts.length) {
                    }
                    break;
            }
        }

        if (pv.isEmpty() && !currmove.isEmpty()) {
            pv = currmove;
            log.trace("Using currmove as PV: {}", currmove);
        }

        if (pv.isEmpty()) {
            return;
        }

        // Ограничиваем PV до 20 полуходов
        String[] pvMoves = pv.split(" ");
        if (pvMoves.length > 20) {
            String[] limitedPv = new String[20];
            System.arraycopy(pvMoves, 0, limitedPv, 0, 20);
            pv = String.join(" ", limitedPv);
        }

        log.trace("PARSED: multipv={}, depth={}, score={}, pv='{}'",
                pvIndex, depth, score, pv);

        AnalysisInfo info = new AnalysisInfo();
        info.setDepth(depth);
        info.setScore(score);
        info.setScoreIsMate(scoreIsMate);
        info.setPv(pv);
        info.setSelDepth(seldepth);
        info.setNodes(nodes);
        info.setNps(nps);
        info.setTime(time);
        info.setHashFull(hashfull);
        info.setTbHits(tbhits);
        info.setLowerBound(isLowerbound);
        info.setUpperbound(isUpperbound);
        info.setCurrMove(currmove);
        info.setCurrMoveNumber(currmovenumber);

        if (pvIndex == 1) {
            lastDepth = depth;
            lastScore = score;
            lastPv = pv;
            lastSeldepth = seldepth;
            lastNodes = nodes;
            lastNps = nps;
            lastTime = time;
        }

        multiPvInfo.put(pvIndex, info);
    }

    public AnalysisInfo getAnalysisInfo(int lineNumber) {
        return multiPvInfo.getOrDefault(lineNumber, new AnalysisInfo());
    }

    // ==================== Конвертация ходов ====================

    public Move convertUciToMove(String uciMove) throws MoveConversionException {
        if (uciMove == null || uciMove.length() < 4) {
            throw new MoveConversionException(
                    languageManager.get(LanguageKeys.ENGINE_INVALID_UCI_MOVE) + ": " + uciMove
            );
        }

        try {
            String fromStr = uciMove.substring(0, 2).toUpperCase();
            String toStr = uciMove.substring(2, 4).toUpperCase();

            Square from = Square.valueOf(fromStr);
            Square to = Square.valueOf(toStr);

            return new Move(from, to);
        } catch (Exception e) {
            throw new MoveConversionException(
                    languageManager.get(LanguageKeys.ENGINE_CONVERT_UCI_ERROR) + ": " + uciMove, e
            );
        }
    }

    public String convertUciToChessNotation(String uciMove, Board board) {
        if (uciMove == null || uciMove.length() < 4) return uciMove;

        try {
            String fromStr = uciMove.substring(0, 2).toUpperCase();
            String toStr = uciMove.substring(2, 4).toUpperCase();

            Square from = Square.valueOf(fromStr);
            Square to = Square.valueOf(toStr);

            Piece movingPiece = board.getPiece(from);

            if (movingPiece == Piece.NONE) {
                if (from == Square.E1 && to == Square.G1) return "O-O";
                if (from == Square.E1 && to == Square.C1) return "O-O-O";
                if (from == Square.E8 && to == Square.G8) return "O-O";
                if (from == Square.E8 && to == Square.C8) return "O-O-O";
                return uciMove;
            }

            if ((movingPiece == Piece.WHITE_KING || movingPiece == Piece.BLACK_KING)) {
                int fromFile = from.getFile().ordinal();
                int toFile = to.getFile().ordinal();
                if (Math.abs(toFile - fromFile) == 2) {
                    return toFile - fromFile > 0 ? "O-O" : "O-O-O";
                }
            }

            boolean isCapture = board.getPiece(to) != Piece.NONE;
            boolean isPromotion = uciMove.length() > 4;
            String promotionPiece = "";

            if (isPromotion) {
                String promo = uciMove.substring(4);
                Piece promoPiece = getPieceFromChar(promo, movingPiece);
                promotionPiece = "=" + ChessSymbols.getSymbol(promoPiece);
            }

            String result;
            if (movingPiece == Piece.WHITE_PAWN || movingPiece == Piece.BLACK_PAWN) {
                if (isCapture) {
                    String fromFile = from.toString().toLowerCase().substring(0, 1);
                    result = fromFile + "x" + to.toString().toLowerCase();
                } else {
                    result = to.toString().toLowerCase();
                }
                result += promotionPiece;
            } else {
                String pieceSymbol = ChessSymbols.getSymbol(movingPiece);
                if (isCapture) {
                    result = pieceSymbol + "x" + to.toString().toLowerCase();
                } else {
                    result = pieceSymbol + to.toString().toLowerCase();
                }
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to convert UCI to notation: {}", uciMove, e);
            return uciMove;
        }
    }

    private Piece getPieceFromChar(String promoChar, Piece movingPiece) {
        boolean isWhite = movingPiece.getPieceSide() == com.github.bhlangonijr.chesslib.Side.WHITE;
        return switch (promoChar) {
            case "r" -> isWhite ? Piece.WHITE_ROOK : Piece.BLACK_ROOK;
            case "b" -> isWhite ? Piece.WHITE_BISHOP : Piece.BLACK_BISHOP;
            case "n" -> isWhite ? Piece.WHITE_KNIGHT : Piece.BLACK_KNIGHT;
            default -> isWhite ? Piece.WHITE_QUEEN : Piece.BLACK_QUEEN;
        };
    }
}