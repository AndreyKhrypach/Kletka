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

package Khrypach.Andrey.chess.kletka.database.parser;

import Khrypach.Andrey.chess.kletka.database.exception.PgnParseException;
import Khrypach.Andrey.chess.kletka.database.model.GameData;
import Khrypach.Andrey.chess.kletka.database.model.GameTree;
import Khrypach.Andrey.chess.kletka.database.parser.enums.PgnTokenType;
import Khrypach.Andrey.chess.kletka.database.parser.move.MoveParser;
import Khrypach.Andrey.chess.kletka.database.parser.tree.GameTreeBuilder;
import Khrypach.Andrey.chess.kletka.database.parser.validator.PgnValidator;
import Khrypach.Andrey.chess.kletka.database.parser.variation.VariationParser;
import Khrypach.Andrey.chess.kletka.gui.board.BoardReconstructor;
import Khrypach.Andrey.chess.kletka.gui.board.PathBuilder;
import Khrypach.Andrey.chess.kletka.gui.board.VariationManager;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.model.*;
import Khrypach.Andrey.chess.kletka.gui.visitor.VariationTreeTraverser;
import Khrypach.Andrey.chess.kletka.gui.visitor.impl.PgnExportVisitor;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Главный парсер PGN
 * Парсит PGN строку в GameData
 * Поддерживает вложенные варианты любой глубины
 * <p>
 * Координатор - делегирует работу специализированным классам
 */
public class PgnParser {

    private static final Logger log = LoggerFactory.getLogger(PgnParser.class);
    private final LanguageManager lang = LanguageManager.getInstance();

    // Компоненты
    @Getter
    private final PgnTokenizer tokenizer;
    @Getter
    private final HeaderParser headerParser;
    private final MoveParser moveParser;
    private final PgnValidator validator;
    private final GameTreeBuilder treeBuilder;
    private final VariationParser variationParser;

    // Состояние парсинга
    private List<PgnToken> tokens;
    private int position;
    private String currentGameResult = "*";

    // ========== ПОЛЯ ДЛЯ ПОЗИЦИЙ ==========
    private String currentFen = "";
    private boolean currentSetUp = false;
    private String currentPositionType = "game";

    public PgnParser() {
        this.tokenizer = new PgnTokenizer();
        this.headerParser = new HeaderParser();
        this.moveParser = new MoveParser();
        this.validator = new PgnValidator();
        this.treeBuilder = new GameTreeBuilder();
        this.variationParser = new VariationParser(moveParser, treeBuilder);
        reset();
    }

    /**
     * Парсит одну PGN партию с использованием GUI-логики
     */
    public GameData parse(String pgn) throws PgnParseException {
        reset();

        log.debug("START PARSING SINGLE GAME WITH GUI LOGIC - PGN length: {} characters",
                pgn != null ? pgn.length() : 0);
        log.trace("FULL INPUT PGN:\n{}", pgn);

        if (pgn == null || pgn.trim().isEmpty()) {
            throw new PgnParseException(lang.get(LanguageKeys.IMPORT_ERROR_PGN_EMPTY));
        }

        String last100 = pgn.length() > 100 ? pgn.substring(pgn.length() - 100) : pgn;
        log.trace("Last 100 chars of input: '{}'", last100);

        boolean hasResult = pgn.matches(".*(1-0|0-1|1/2-1/2|\\*)\\s*$");
        log.trace("Has result at end: {}", hasResult);

        long startTime = System.currentTimeMillis();
        this.currentGameResult = "*";

        this.currentFen = "";
        this.currentSetUp = false;
        this.currentPositionType = "game";

        try {
            tokens = tokenizer.tokenize(pgn);
            if (tokens.isEmpty()) {
                throw new PgnParseException(lang.get(LanguageKeys.FAILED_TOKENIZE_PGN));
            }
            position = 0;

            int[] pos = new int[]{0};
            Map<String, String> headers = headerParser.parseHeaders(tokens, pos);
            position = pos[0];

            String fen = headers.getOrDefault("FEN", "");
            String setUp = headers.getOrDefault("SetUp", "0");
            String eventType = headers.getOrDefault("EventType", "");

            this.currentFen = fen;
            this.currentSetUp = "1".equals(setUp);

            if (this.currentSetUp && !fen.isEmpty()) {
                if ("tourn".equals(eventType)) {
                    this.currentPositionType = lang.get(LanguageKeys.GAME_TYPE_PROBLEM).toLowerCase();
                } else if (lang.get(LanguageKeys.GAME_TYPE_GAME).toLowerCase().equals(eventType) || "eth".equals(eventType)) {
                    this.currentPositionType = lang.get(LanguageKeys.GAME_TYPE_STUDY).toLowerCase();
                } else {
                    String white = headers.getOrDefault("White", "");
                    String black = headers.getOrDefault("Black", "");
                    String mateKeyword = lang.get(LanguageKeys.PGN_KEYWORD_MATE).toLowerCase();
                    String studyKeyword = lang.get(LanguageKeys.PGN_KEYWORD_STUDY).toLowerCase();
                    if (white.toLowerCase().contains(mateKeyword) || black.toLowerCase().contains(mateKeyword)) {
                        this.currentPositionType = lang.get(LanguageKeys.GAME_TYPE_PROBLEM).toLowerCase();
                    } else if (white.toLowerCase().contains(studyKeyword) || black.toLowerCase().contains(studyKeyword)) {
                        this.currentPositionType = lang.get(LanguageKeys.GAME_TYPE_STUDY).toLowerCase();
                    } else {
                        this.currentPositionType = lang.get(LanguageKeys.GAME_TYPE_POSITION).toLowerCase();
                    }
                }
            }

            log.debug("Position detected: fen='{}', setUp={}, type='{}'", fen, this.currentSetUp, this.currentPositionType);

            GameData gameData = createGameDataFromHeaders(headers);

            treeBuilder.initializeGuiLogic();

            if (this.currentSetUp && !fen.isEmpty()) {
                try {
                    Board fenBoard = new Board();
                    fenBoard.loadFromFen(fen);
                    treeBuilder.setInitialBoard(fenBoard.clone());
                    treeBuilder.setCurrentBoard(fenBoard.clone());

                    PathBuilder pathBuilder = treeBuilder.getPathBuilder();
                    BoardReconstructor boardReconstructor = new BoardReconstructor(
                            pathBuilder,
                            treeBuilder.getInitialBoard(),
                            false
                    );
                    treeBuilder.setBoardReconstructor(boardReconstructor);

                    VariationManager variationManager = new VariationManager(
                            (RootNode) treeBuilder.getRootVariation().getFirstNode(),
                            treeBuilder.getRootVariation(),
                            treeBuilder.getMainLine(),
                            boardReconstructor,
                            treeBuilder.getNamingService()
                    );
                    treeBuilder.setVariationManager(variationManager);

                    variationParser.setCurrentBoard(treeBuilder.getCurrentBoard());

                    log.debug("Loaded FEN position: {}", fen);
                } catch (Exception e) {
                    log.warn("Failed to load FEN: {}, using default board", e.getMessage());
                }
            }

            variationParser.setTokens(tokens);
            variationParser.setPosition(position);
            variationParser.setCurrentVariation(treeBuilder.getCurrentVariation());
            variationParser.setCurrentNode(treeBuilder.getCurrentNode());
            variationParser.setCurrentBoard(treeBuilder.getCurrentBoard());
            variationParser.clear();

            String moves = parseMovesWithGuiLogic();

            position = variationParser.getPosition();
            treeBuilder.setCurrentVariation(variationParser.getCurrentVariation());
            treeBuilder.setCurrentNode(variationParser.getCurrentNode());
            treeBuilder.setCurrentBoard(variationParser.getCurrentBoard());

            String result = this.currentGameResult;
            if (result == null || "*".equals(result) || result.isEmpty()) {
                result = headers.getOrDefault("Result", "*");
            }

            String movesStr = moves;
            if ("*".equals(result) && !movesStr.endsWith("*")) {
                movesStr = movesStr.trim() + " *";
                log.trace("Added missing '*' to PGN");
            }

            gameData = new GameData(
                    gameData.whitePlayer(),
                    gameData.blackPlayer(),
                    result,
                    gameData.whiteElo(),
                    gameData.blackElo(),
                    gameData.event(),
                    gameData.site(),
                    gameData.round(),
                    gameData.subround(),
                    gameData.date(),
                    gameData.eco(),
                    gameData.opening(),
                    gameData.variation(),
                    gameData.annotator(),
                    gameData.whiteTeam(),
                    gameData.blackTeam(),
                    gameData.source(),
                    gameData.whiteFideId(),
                    gameData.blackFideId(),
                    gameData.timeControl(),
                    String.valueOf(getTotalPly()),
                    movesStr,
                    this.currentFen,
                    this.currentSetUp,
                    this.currentPositionType,
                    gameData.deleted()
            );

            long endTime = System.currentTimeMillis();
            log.debug("PARSING COMPLETE in {} ms", endTime - startTime);

            return gameData;

        } catch (Exception e) {
            log.error("Error parsing PGN: {}", e.getMessage(), e);
            throw new PgnParseException(String.format(lang.get(LanguageKeys.REPO_ERROR_PARSE_PGN), e.getMessage()), e);
        }
    }

    /**
     * Парсит ходы с использованием GUI-логики
     * Теперь использует variationParser
     */
    private String parseMovesWithGuiLogic() throws PgnParseException {
        log.debug("parseMovesWithGuiLogic() called, position: {}", position);
        log.trace("Remaining tokens (first 50): ");
        for (int i = position; i < Math.min(position + 50, tokens.size()); i++) {
            log.trace("  token[{}] = {}", i, tokens.get(i));
        }

        while (position < tokens.size()) {
            PgnToken token = tokens.get(position);
            log.trace("TOKEN: position={}, type={}, value='{}'", position, token.type(), token.value());

            switch (token.type()) {
                case MOVE_NUMBER:
                case MOVE_NUMBER_ELLIPSIS:
                    log.trace("Move number: {}", token.value());
                    position++;
                    break;

                case MOVE:
                    String moveText = token.value();
                    log.trace("Processing move: '{}'", moveText);

                    if (moveText.matches("[+#]+")) {
                        ParentNode prevNode = treeBuilder.getCurrentNode();
                        if (prevNode instanceof MoveNode moveNode) {
                            MoveAnnotation ann = null;
                            if (moveText.contains(MoveAnnotation.MATE.getSymbol())) {
                                ann = MoveAnnotation.MATE;
                            } else if (moveText.contains(MoveAnnotation.CHECK.getSymbol())) {
                                ann = MoveAnnotation.CHECK;
                            }
                            if (ann != null) {
                                if (moveNode.getAnnotation() == null) {
                                    moveNode.setAnnotation(ann);
                                } else {
                                    moveNode.addAnnotation(ann);
                                }
                                log.trace("Added annotation {} to move {}", ann, moveNode.getSan());
                            }
                        }
                        position++;
                        break;
                    }

                    if (moveText.endsWith("*")) {
                        String cleanMove = moveText.substring(0, moveText.length() - 1);
                        log.trace("Move with '*' suffix: '{}', clean: '{}'", moveText, cleanMove);

                        parseMoveWithGuiLogic(cleanMove);

                        this.currentGameResult = "*";
                        log.trace("Set result to '*' from move suffix");

                        position++;
                        break;
                    }

                    if (moveText.startsWith("=")) {
                        log.trace("Skipping promotion part token: '{}'", moveText);
                        position++;
                        break;
                    }

                    parseMoveWithGuiLogic(moveText);

                    variationParser.syncWithParser(
                            treeBuilder.getCurrentNode(),
                            treeBuilder.getCurrentBoard(),
                            treeBuilder.getCurrentVariation()
                    );

                    position++;
                    break;

                case VARIATION_START:
                    log.trace("VARIATION_START at position {}", position);

                    Variation beforeVar = treeBuilder.getCurrentVariation();
                    ParentNode beforeNode = treeBuilder.getCurrentNode();
                    Board beforeBoard = treeBuilder.getCurrentBoard().clone();

                    try {
                        position++;

                        variationParser.setTokens(tokens);
                        variationParser.setPosition(position);
                        variationParser.syncWithParser(beforeNode, beforeBoard, beforeVar);

                        variationParser.parseVariationWithGuiLogic();

                        position = variationParser.getPosition();

                        treeBuilder.setCurrentVariation(beforeVar);
                        treeBuilder.setCurrentNode(beforeNode);
                        treeBuilder.setCurrentBoard(beforeBoard);

                        variationParser.syncWithParser(beforeNode, beforeBoard, beforeVar);

                        log.trace("After variation, restored to node: {}",
                                beforeNode != null ? beforeNode.getSan() : "null");

                    } catch (PgnParseException e) {
                        log.error("Error parsing variation: {}", e.getMessage(), e);

                        treeBuilder.setCurrentVariation(beforeVar);
                        treeBuilder.setCurrentNode(beforeNode);
                        treeBuilder.setCurrentBoard(beforeBoard);

                        skipToVariationEnd();

                        log.warn("Recovered from variation error, skipped to position {}", position);
                    }
                    break;

                case VARIATION_END:
                    log.warn("Unexpected VARIATION_END at position {}, skipping", position);
                    position++;
                    break;

                case ANNOTATION:
                case NAG:
                    parseAnnotation(token.value());
                    position++;
                    break;

                case COMMENT_TEXT:
                    parseComment(token.value());
                    position++;
                    break;

                case RESULT:
                    log.trace("Result token: {}", token.value());
                    this.currentGameResult = token.value();
                    position++;
                    break;

                case EOF:
                    log.trace("EOF reached");
                    position++;
                    break;

                default:
                    log.warn("Unhandled token type: {}", token.type());
                    position++;
                    break;
            }

            if (position > 0 && position <= tokens.size()) {
                PgnToken prevToken = tokens.get(position - 1);
                if (prevToken.type() == PgnTokenType.RESULT) {
                    log.trace("Detected RESULT token, stopping parse");
                    break;
                }
            }
        }

        VariationManager variationManager = treeBuilder.getVariationManager();
        if (variationManager != null) {
            variationManager.setMainLine(treeBuilder.getMainLine());
            variationManager.updateAllVariationNames();
        }

        RootNode rootNode = (RootNode) treeBuilder.getRootVariation().getFirstNode();
        PgnExportVisitor pgnVisitor = new PgnExportVisitor();
        VariationTreeTraverser traverser = new VariationTreeTraverser();

        String result = traverser.traverse(rootNode, treeBuilder.getMainLine(), pgnVisitor);
        log.debug("PgnExportVisitor result: '{}'", result);

        return result;
    }

    /**
     * Пропускает токены до закрывающей скобки варианта
     */
    private void skipToVariationEnd() {
        int depth = 0;
        boolean foundEnd = false;

        while (position < tokens.size()) {
            PgnToken token = tokens.get(position);

            if (token.type() == PgnTokenType.VARIATION_START) {
                depth++;
            } else if (token.type() == PgnTokenType.VARIATION_END) {
                if (depth == 0) {
                    foundEnd = true;
                    position++;
                    break;
                } else {
                    depth--;
                }
            }
            position++;
        }

        if (foundEnd) {
            log.debug("Skipped to VARIATION_END at position {}", position);
        } else {
            log.warn("Reached EOF while skipping to variation end");
        }
    }

    /**
     * Парсит ход с использованием GUI-логики
     * Теперь использует treeBuilder
     */
    private void parseMoveWithGuiLogic(String moveText) throws PgnParseException {
        log.trace("parseMoveWithGuiLogic: '{}'", moveText);

        try {
            String cleanMoveText = moveText;
            boolean hasResultStar = false;
            boolean hasCheck = false;
            boolean hasMate = false;

            String positionEval;

            String[] positionSymbols = {
                    "±", "∓", "∞", "≅", "=", "+=", "=+", "+–", "–+"
            };

            for (String symbol : positionSymbols) {
                if (moveText.endsWith(symbol)) {
                    String withoutSymbol = moveText.substring(0, moveText.length() - symbol.length());

                    if (withoutSymbol.endsWith("=") && withoutSymbol.length() >= 3) {
                        char beforeEqual = withoutSymbol.charAt(withoutSymbol.length() - 2);
                        if (beforeEqual >= 'a' && beforeEqual <= 'h') {
                            continue;
                        }
                    }

                    if (withoutSymbol.isEmpty() ||
                            withoutSymbol.matches("^[A-Za-z]?[a-h]?[1-8]?[xX]?[a-h][1-8]=?[QRBN]?[+#]?$") ||
                            withoutSymbol.equals("O-O") || withoutSymbol.equals("O-O-O")) {
                        cleanMoveText = withoutSymbol;
                        positionEval = symbol;
                        log.trace("Removed position evaluation '{}' from move '{}' (without space)",
                                positionEval, moveText);
                        break;
                    }
                }
            }

            if (cleanMoveText == null || cleanMoveText.trim().isEmpty()) {
                log.warn("Empty move text after removing position evaluation: {}", moveText);
                return;
            }

            String moveForSuffixCheck = cleanMoveText;

            if (moveForSuffixCheck.endsWith("*")) {
                cleanMoveText = moveForSuffixCheck.substring(0, moveForSuffixCheck.length() - 1);
                hasResultStar = true;
                moveForSuffixCheck = cleanMoveText;
            }
            if (moveForSuffixCheck.endsWith(MoveAnnotation.MATE.getSymbol())) {
                cleanMoveText = moveForSuffixCheck.substring(0, moveForSuffixCheck.length() - 1);
                hasMate = true;
                moveForSuffixCheck = cleanMoveText;
            }
            if (moveForSuffixCheck.endsWith(MoveAnnotation.CHECK.getSymbol())) {
                cleanMoveText = moveForSuffixCheck.substring(0, moveForSuffixCheck.length() - 1);
                hasCheck = true;
            }

            if (hasResultStar || hasCheck || hasMate) {
                log.trace("Move '{}' cleaned to: '{}' (hasResultStar={}, hasCheck={}, hasMate={})",
                        moveText, cleanMoveText, hasResultStar, hasCheck, hasMate);
            }

            if (cleanMoveText == null || cleanMoveText.trim().isEmpty()) {
                log.warn("Empty move text after all cleaning: {}", moveText);
                return;
            }

            Board currentBoard = treeBuilder.getCurrentBoard();

            Move move = moveParser.convertSanToMove(cleanMoveText, currentBoard);
            if (move == null) {
                log.warn("Could not convert move: {} (clean: {})", moveText, cleanMoveText);
                return;
            }

            if (!currentBoard.isMoveLegal(move, true)) {
                log.error("Move is NOT legal: {} on board {}", cleanMoveText, currentBoard.getFen());
                return;
            }

            Piece movingPiece = currentBoard.getPiece(move.getFrom());
            boolean isCapture = currentBoard.getPiece(move.getTo()) != Piece.NONE;

            Piece promotionPiece = null;
            if (move.getPromotion() != null && move.getPromotion() != Piece.NONE) {
                promotionPiece = move.getPromotion();
                log.trace("Promotion from Move: {}", promotionPiece);
            } else if (cleanMoveText.contains("=")) {
                char promotionChar = cleanMoveText.charAt(cleanMoveText.indexOf('=') + 1);
                promotionPiece = moveParser.charToPiece(promotionChar, currentBoard.getSideToMove());
                log.trace("Promotion from SAN: {}", promotionPiece);
            }

            String fenBefore = currentBoard.getFen();

            MoveNode moveNode = new MoveNode(move, movingPiece, isCapture, promotionPiece);

            treeBuilder.setAbsolutePlyForNode(moveNode);

            moveNode.setSavedFenBefore(fenBefore);

            currentBoard.doMove(move);
            moveNode.setSavedFenAfter(currentBoard.getFen());
            log.trace("parseMoveWithGuiLogic moveNode san = {}, fenBefore = {}, fenAfter = {}",
                    moveNode.getSan(), moveNode.getSavedFenBefore(), moveNode.getSavedFenAfter());

            treeBuilder.addMoveToTree(moveNode);

            log.trace("Move added: {}, FEN after: {}", cleanMoveText, currentBoard.getFen());

        } catch (Exception e) {
            log.error("Error parsing move {}: {}", moveText, e.getMessage(), e);
            throw new PgnParseException(String.format(lang.get(LanguageKeys.ERROR_PARSING_MOVE),  moveText), e);
        }
    }

    /**
     * Парсит аннотацию
     */
    private void parseAnnotation(String annotation) {
        ParentNode currentNode = treeBuilder.getCurrentNode();
        if (currentNode instanceof MoveNode moveNode) {
            MoveAnnotation ann = MoveAnnotation.fromSymbol(annotation);

            if (ann == null) {
                String trimmed = annotation.trim();
                ann = MoveAnnotation.fromSymbol(trimmed);
            }

            if (ann != null) {
                log.trace("Annotation mapped to: {}", ann);
                if (moveNode.getAnnotation() == null) {
                    moveNode.setAnnotation(ann);
                } else {
                    moveNode.addAnnotation(ann);
                }
            } else {
                log.warn("Unknown annotation symbol: {}", annotation);
            }
        } else {
            log.warn("Annotation {} but currentNode is null or not MoveNode", annotation);
        }
    }

    /**
     * Парсит комментарий
     */
    private void parseComment(String comment) {
        log.trace("parseComment: '{}'", comment);
        ParentNode currentNode = treeBuilder.getCurrentNode();

        if (currentNode instanceof MoveNode moveNode) {
            if (comment != null && !comment.trim().isEmpty()) {
                moveNode.setComment(comment.trim());
                log.trace("Comment set on node: {}", comment);
            }
        }
    }

    /**
     * Полностью сбрасывает состояние парсера
     */
    public void reset() {
        log.debug("Resetting parser state");
        this.tokens = null;
        this.position = 0;
        this.currentGameResult = "*";
        this.currentFen = "";
        this.currentSetUp = false;
        this.currentPositionType = "game";

        treeBuilder.reset();
        variationParser.clear();
    }

    /**
     * Парсит несколько PGN партий с поддержкой позиций
     */
    public List<GameData> parseMultiple(String pgn) throws PgnParseException {
        log.debug("START PARSING MULTIPLE GAMES - Input length: {} characters",
                pgn != null ? pgn.length() : 0);

        if (pgn == null || pgn.trim().isEmpty()) {
            log.warn("Input is null or empty, returning empty list");
            return new ArrayList<>();
        }

        long startTime = System.currentTimeMillis();
        List<GameData> games = new ArrayList<>();

        List<String> gameStrings = extractGamesFromPgn(pgn);
        log.debug("Found {} potential games", gameStrings.size());

        for (int i = 0; i < gameStrings.size(); i++) {
            String gameStr = gameStrings.get(i);
            if (gameStr.trim().isEmpty()) {
                log.trace("Game {} is empty, skipping", i);
                continue;
            }

            log.debug("Parsing game {}/{}...", i + 1, gameStrings.size());
            try {
                long gameStart = System.currentTimeMillis();

                PgnParser gameParser = new PgnParser();
                GameData game = gameParser.parse(gameStr);

                long gameEnd = System.currentTimeMillis();
                if (game != null) {
                    games.add(game);
                    log.debug("Game {} parsed successfully in {} ms: {} vs {}, type={}",
                            i + 1, gameEnd - gameStart, game.whitePlayer(), game.blackPlayer(),
                            game.positionType());
                }
            } catch (PgnParseException e) {
                log.warn("Failed to parse game {}: {}", i + 1, e.getMessage());
                this.reset();
            }
        }

        long endTime = System.currentTimeMillis();
        log.debug("MULTIPLE PARSING COMPLETE - Successfully parsed: {} games, Total time: {} ms",
                games.size(), endTime - startTime);

        return games;
    }

    /**
     * Улучшенное извлечение партий
     */
    public List<String> extractGamesFromPgn(String pgn) {
        List<String> games = new ArrayList<>();

        Pattern resultPattern = Pattern.compile("(1-0|0-1|1/2-1/2|\\*)(?=\\s*\\[Event|\\s*$)");
        Matcher resultMatcher = resultPattern.matcher(pgn);

        List<Integer> resultEnds = new ArrayList<>();
        while (resultMatcher.find()) {
            resultEnds.add(resultMatcher.end());
        }

        if (!resultEnds.isEmpty()) {
            int lastStart = 0;
            for (int endPos : resultEnds) {
                String gamePart = pgn.substring(lastStart, endPos).trim();
                if (!gamePart.isEmpty()) {
                    games.add(gamePart);
                }
                lastStart = endPos;
            }
            if (lastStart < pgn.length()) {
                String lastPart = pgn.substring(lastStart).trim();
                if (lastPart.startsWith("[")) {
                    games.add(lastPart);
                }
            }

            games = createValidGames(games);
        }

        if (games.isEmpty()) {
            Pattern headerPattern = Pattern.compile("\\[Event \"[^\"]*\"]");
            Matcher headerMatcher = headerPattern.matcher(pgn);
            List<Integer> headerStarts = new ArrayList<>();
            while (headerMatcher.find()) {
                headerStarts.add(headerMatcher.start());
            }

            for (int i = 0; i < headerStarts.size(); i++) {
                int start = headerStarts.get(i);
                int end = (i + 1 < headerStarts.size()) ? headerStarts.get(i + 1) : pgn.length();
                String game = pgn.substring(start, end).trim();
                if (!game.isEmpty()) {
                    games.add(game);
                }
            }
        }

        games.removeIf(game -> {
            String trimmed = game.trim();
            return !trimmed.startsWith("[");
        });

        if (!games.isEmpty()) {
            String lastGame = games.get(games.size() - 1);
            if (!lastGame.matches(".*(1-0|0-1|1/2-1/2|\\*)\\s*$")) {
                int lastGameEnd = pgn.lastIndexOf(lastGame) + lastGame.length();
                if (lastGameEnd < pgn.length()) {
                    String remaining = pgn.substring(lastGameEnd).trim();
                    if (remaining.matches("(1-0|0-1|1/2-1/2|\\*).*")) {
                        String result = remaining.replaceAll("(1-0|0-1|1/2-1/2|\\*).*", "$1");
                        games.set(games.size() - 1, lastGame + " " + result);
                    }
                }
            }
        }

        log.debug("Extracted {} games from PGN", games.size());
        return games;
    }

    private static List<String> createValidGames(List<String> games) {
        List<String> validGames = new ArrayList<>();
        for (String game : games) {
            int bracketIndex = game.indexOf('[');
            if (bracketIndex > 0) {
                if (!validGames.isEmpty()) {
                    String last = validGames.remove(validGames.size() - 1);
                    validGames.add(last + " " + game.substring(0, bracketIndex).trim());
                    validGames.add(game.substring(bracketIndex).trim());
                } else {
                    validGames.add(game);
                }
            } else {
                validGames.add(game);
            }
        }
        return validGames;
    }

    /**
     * Создает GameData из заголовков
     */
    private GameData createGameDataFromHeaders(Map<String, String> headers) {
        log.debug("Creating GameData from headers...");

        String white = headers.getOrDefault("White", "?");
        String black = headers.getOrDefault("Black", "?");
        String result = headers.getOrDefault("Result", "*");
        String whiteElo = headers.getOrDefault("WhiteElo", "?");
        String blackElo = headers.getOrDefault("BlackElo", "?");
        String event = headers.getOrDefault("Event", "?");
        String site = headers.getOrDefault("Site", "?");
        String round = headers.getOrDefault("Round", "?");
        String subround = headers.getOrDefault("Subround", "?");
        String eco = headers.getOrDefault("ECO", "?");
        String opening = headers.getOrDefault("Opening", "?");
        String variation = headers.getOrDefault("Variation", "?");
        String annotator = headers.getOrDefault("Annotator", "?");
        String whiteTeam = headers.getOrDefault("WhiteTeam", "?");
        String blackTeam = headers.getOrDefault("BlackTeam", "?");
        String source = headers.getOrDefault("Source", "?");
        String whiteFideId = headers.getOrDefault("WhiteFideId", "?");
        String blackFideId = headers.getOrDefault("BlackFideId", "?");
        String timeControl = headers.getOrDefault("TimeControl", "?");

        String fen = headers.getOrDefault("FEN", "");
        String setUp = headers.getOrDefault("SetUp", "0");
        boolean isSetUp = "1".equals(setUp);

        String deletedStr = headers.getOrDefault("Deleted", "false");
        boolean deleted = "true".equalsIgnoreCase(deletedStr) || "1".equals(deletedStr);

        String positionType = this.currentPositionType;
        if ("game".equals(positionType) && isSetUp && !fen.isEmpty()) {
            positionType = "position";
        }

        LocalDate date = parseDate(headers.getOrDefault("Date", LocalDate.now().toString()));

        return new GameData(
                white, black, result,
                whiteElo, blackElo,
                event, site, round, subround, date,
                eco, opening, variation,
                annotator, whiteTeam, blackTeam, source,
                whiteFideId, blackFideId, timeControl,
                "?",
                "",
                fen,
                isSetUp,
                positionType,
                deleted
        );
    }

    /**
     * Парсит дату
     */
    private LocalDate parseDate(String dateStr) {
        try {
            if (dateStr == null || dateStr.isEmpty()) {
                return LocalDate.now();
            }

            if (dateStr.equals("????.??.??") || dateStr.equals("????-??-??") ||
                    dateStr.matches("^\\?{4}\\.\\?{2}\\.\\?{2}$")) {
                return LocalDate.now();
            }

            if (dateStr.contains("?")) {
                String[] parts;
                if (dateStr.contains(".")) {
                    parts = dateStr.split("\\.");
                } else if (dateStr.contains("-")) {
                    parts = dateStr.split("-");
                } else {
                    parts = new String[]{dateStr};
                }

                String year = "2000";
                String month = "1";
                String day = "1";

                if (parts.length > 0) {
                    year = parts[0].replaceAll("\\?", "0");
                    if (year.isEmpty() || year.matches("0+")) {
                        year = "2000";
                    }
                }
                if (parts.length > 1) {
                    month = parts[1].replaceAll("\\?", "1");
                    if (month.isEmpty() || month.matches("0+")) {
                        month = "1";
                    }
                }
                if (parts.length > 2) {
                    day = parts[2].replaceAll("\\?", "1");
                    if (day.isEmpty() || day.matches("0+")) {
                        day = "1";
                    }
                }

                try {
                    return LocalDate.of(
                            Integer.parseInt(year),
                            Math.min(Integer.parseInt(month), 12),
                            Math.min(Integer.parseInt(day), 28)
                    );
                } catch (NumberFormatException e) {
                    return LocalDate.now();
                }
            }

            if (dateStr.contains(".")) {
                String[] parts = dateStr.split("\\.");
                if (parts.length >= 3) {
                    try {
                        int year = Integer.parseInt(parts[0]);
                        int month = Integer.parseInt(parts[1]);
                        int day = Integer.parseInt(parts[2]);
                        return LocalDate.of(year, month, day);
                    } catch (NumberFormatException e) {
                        log.warn("Failed to parse date parts with '.' : {}", dateStr);
                    }
                }
            }

            if (dateStr.contains("-")) {
                String[] parts = dateStr.split("-");
                if (parts.length >= 3) {
                    try {
                        int year = Integer.parseInt(parts[0]);
                        int month = Integer.parseInt(parts[1]);
                        int day = Integer.parseInt(parts[2]);
                        return LocalDate.of(year, month, day);
                    } catch (NumberFormatException e) {
                        log.warn("Failed to parse date parts with '-' : {}", dateStr);
                    }
                }
            }

            if (dateStr.matches("\\d{4}.*")) {
                try {
                    String yearStr = dateStr.substring(0, 4).replaceAll("\\?", "0");
                    int year = Integer.parseInt(yearStr);
                    if (year < 1000 || year > 9999) {
                        year = 2000;
                    }
                    return LocalDate.of(year, 1, 1);
                } catch (NumberFormatException e) {
                    return LocalDate.now();
                }
            }

        } catch (Exception e) {
            log.warn("Failed to parse date: {}", dateStr, e);
        }

        return LocalDate.now();
    }

    /**
     * Получает общее количество полуходов
     * Теперь считаем по максимальному absolutePly в дереве
     */
    private int getTotalPly() {
        int maxPly;

        RootNode rootNode = (RootNode) treeBuilder.getRootVariation().getFirstNode();
        if (rootNode == null) return 0;

        maxPly = findMaxPly(rootNode);

        return maxPly;
    }

    /**
     * Рекурсивно находит максимальный absolutePly в дереве
     */
    private int findMaxPly(ParentNode node) {
        if (node == null) return 0;

        int maxPly = 0;

        if (!node.isRoot()) {
            maxPly = Math.max(maxPly, node.getAbsolutePly());
        }

        for (Variation var : node.getSubVariations()) {
            if (var == null) continue;
            for (ParentNode child : var.getMoves()) {
                maxPly = Math.max(maxPly, findMaxPly(child));
            }
        }

        if (node.getNext() != null && !node.getNext().isRoot()) {
            maxPly = Math.max(maxPly, findMaxPly(node.getNext()));
        }

        return maxPly;
    }

    /**
     * Парсит PGN и возвращает GameTree с деревом вариантов
     */
    public GameTree parseToGameTree(String pgn) throws PgnParseException {
        if (pgn == null || pgn.trim().isEmpty()) {
            throw new PgnParseException(lang.get(LanguageKeys.IMPORT_ERROR_PGN_EMPTY));
        }

        log.debug("parseToGameTree - Input PGN length: {}", pgn.length());
        log.trace("parseToGameTree - Input PGN first 500 chars:\n{}",
                pgn.length() > 500 ? pgn.substring(0, 500) + "..." : pgn);

        try {
            tokens = tokenizer.tokenize(pgn);
            position = 0;

            int[] pos = new int[]{0};
            Map<String, String> headers = headerParser.parseHeaders(tokens, pos);
            position = pos[0];

            log.debug("Headers parsed: {}", headers.size());

            treeBuilder.initializeGuiLogic();

            String fen = headers.getOrDefault("FEN", "");
            String setUp = headers.getOrDefault("SetUp", "0");
            boolean isSetUp = "1".equals(setUp);

            if (isSetUp && !fen.isEmpty()) {
                try {
                    Board fenBoard = new Board();
                    fenBoard.loadFromFen(fen);
                    treeBuilder.setInitialBoard(fenBoard.clone());
                    treeBuilder.setCurrentBoard(fenBoard.clone());

                    PathBuilder pathBuilder = treeBuilder.getPathBuilder();
                    BoardReconstructor boardReconstructor = new BoardReconstructor(
                            pathBuilder,
                            treeBuilder.getInitialBoard(),
                            false
                    );
                    treeBuilder.setBoardReconstructor(boardReconstructor);

                    VariationManager variationManager = new VariationManager(
                            (RootNode) treeBuilder.getRootVariation().getFirstNode(),
                            treeBuilder.getRootVariation(),
                            treeBuilder.getMainLine(),
                            boardReconstructor,
                            treeBuilder.getNamingService()
                    );
                    treeBuilder.setVariationManager(variationManager);

                    variationParser.syncWithParser(
                            treeBuilder.getCurrentNode(),
                            treeBuilder.getCurrentBoard(),
                            treeBuilder.getCurrentVariation()
                    );

                    log.debug("Loaded FEN: {}", fen);
                } catch (Exception e) {
                    log.warn("Failed to load FEN: {}", e.getMessage());
                }
            }

            variationParser.setTokens(tokens);
            variationParser.setPosition(position);
            variationParser.syncWithParser(
                    treeBuilder.getCurrentNode(),
                    treeBuilder.getCurrentBoard(),
                    treeBuilder.getCurrentVariation()
            );
            variationParser.clear();

            parseMovesWithGuiLogic();
            treeBuilder.getNamingService().updateAllVariationNames(treeBuilder.getRootVariation());

            GameTree gameTree = new GameTree(
                    (RootNode) treeBuilder.getRootVariation().getFirstNode(),
                    treeBuilder.getMainLine(),
                    treeBuilder.getRootVariation()
            );
            gameTree.setDeleted(headers.containsKey("Deleted") &&
                    "true".equalsIgnoreCase(headers.get("Deleted")));
            gameTree.setInitialFen(treeBuilder.getInitialBoard().getFen());
            gameTree.setStartWithBlack(false);
            gameTree.setInitialBoard(treeBuilder.getInitialBoard());

            String result = this.currentGameResult;
            if (result == null || "*".equals(result)) {
                result = headers.getOrDefault("Result", "*");
            }
            gameTree.setResult(result);


            log.debug("RootNode subVariations count: {}",
                    treeBuilder.getRootVariation().getFirstNode().getSubVariations().size());
            log.debug("Main line moves: {}", treeBuilder.getMainLine().getMoveCount());

            return gameTree;

        } catch (Exception e) {
            throw new PgnParseException(String.format(lang.get(LanguageKeys.ERROR_PARSING_PGN_TO_TREE), e.getMessage()), e);
        }
    }

    /**
     * Проверяет, является ли PGN позицией (а не партией)
     * Делегирует PgnValidator
     */
    public boolean isPositionPgn(String pgn) {
        return validator.isPositionPgn(pgn);
    }

    /**
     * Определяет тип контента по PGN строке
     * Делегирует PgnValidator
     */
    public String detectContentType(String pgn) {
        return validator.detectContentType(pgn);
    }

    // ========== МЕТОДЫ ДЛЯ СОВМЕСТИМОСТИ ==========

    /**
     * @deprecated Используйте PgnValidator.isPositionPgn()
     */
    @Deprecated
    public boolean isPositionPgnLegacy(String pgn) {
        return isPositionPgn(pgn);
    }

    /**
     * @deprecated Используйте PgnValidator.detectContentType()
     */
    @Deprecated
    public String detectContentTypeLegacy(String pgn) {
        return detectContentType(pgn);
    }

}