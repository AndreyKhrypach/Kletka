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

import Khrypach.Andrey.chess.kletka.database.parser.enums.PgnTokenType;
import Khrypach.Andrey.chess.kletka.gui.model.MoveAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Токенизатор PGN
 * Разбивает PGN строку на токены
 */
public class PgnTokenizer {

    private static final Logger log = LoggerFactory.getLogger(PgnTokenizer.class);

    // Регулярные выражения для различных элементов PGN
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "\\[|]|" +
                    "\"[^\"]*\"|" +
                    "\\d+\\s*\\.\\.\\.|" +
                    "\\d+\\s*\\.|" +
                    "\\{([^}]*)}|" +
                    "\\(|\\)|" +
                    "\\$\\d+|" +
                    "[!?]+|" +
                    "[+−]?[−+][+−]?|" +
                    " ± | ∓ | ∞ | ≅ |" +
                    " \\+– | \\+= | = | =\\+ | –\\+ |" +
                    "1-0|0-1|1/2-1/2|\\*|" +
                    "O-O-O|O-O|" +
                    "[A-Za-z]?[a-h]?[1-8]?[xX]?[a-h][1-8]=[QRBN][+#*]*|" +
                    "[A-Za-z]?[a-h]?[1-8]?[xX]?[a-h][1-8][+#*]*|" +
                    "[A-Za-z][a-z]?[1-8]?[xX]?[a-h][1-8]=[QRBN][+#*]*|" +
                    "[A-Za-z][a-z]?[1-8]?[xX]?[a-h][1-8][+#*]*|" +
                    "\\S+"
    );

    private String input;
    private int position;
    private int line;
    private int column;
    private List<PgnToken> tokens;

    /**
     * Токенизирует PGN строку
     */
    public List<PgnToken> tokenize(String input) {
        log.debug("START TOKENIZATION - Input length: {} characters", input != null ? input.length() : 0);

        if (input == null || input.isEmpty()) {
            log.warn("Input is null or empty!");
            return new ArrayList<>();
        }

        boolean hasRussian = input.matches(".*[А-Яа-я].*");
        log.debug("Contains Russian characters: {}", hasRussian);
        if (hasRussian) {
            log.warn("PGN contains Russian characters - may cause issues!");
        }

        long startTime = System.currentTimeMillis();

        this.input = input;
        this.position = 0;
        this.line = 1;
        this.column = 1;
        this.tokens = new ArrayList<>();

        int tokenCount = 0;
        int maxIterations = input.length() * 2;
        int iterations = 0;

        while (position < input.length() && iterations < maxIterations) {
            iterations++;
            char current = input.charAt(position);

            if (iterations % 100 == 0) {
                log.trace("Iteration {}: position={}, char='{}' (0x{})",
                        iterations, position, current, Integer.toHexString(current));
            }

            if (Character.isWhitespace(current)) {
                boolean isPartOfPositionAnnotation = false;
                for (MoveAnnotation ann : MoveAnnotation.values()) {
                    String symbol = ann.getSymbol();
                    if (symbol.startsWith(" ") && position + symbol.length() <= input.length()) {
                        String possible = input.substring(position, position + symbol.length());
                        if (possible.equals(symbol)) {
                            isPartOfPositionAnnotation = true;
                            break;
                        }
                    }
                }

                if (!isPartOfPositionAnnotation) {
                    skipWhitespace();
                    continue;
                }
            }

            PgnToken token = parseNextToken();
            if (token != null) {
                tokens.add(token);
                tokenCount++;
                if (tokenCount % 10 == 0) {
                    log.trace("Token #{}: {}", tokenCount, token);
                }
            } else {
                log.warn("Failed to parse at position {}: '{}' (0x{})",
                        position, current, Integer.toHexString(current));
                position++;
                column++;
            }
        }

        if (iterations >= maxIterations) {
            log.error("Max iterations reached! Possible infinite loop. Position: {}, tokens: {}, last char: '{}'",
                    position, tokens.size(), position < input.length() ? input.charAt(position) : "EOF");
        }

        tokens.add(new PgnToken(PgnTokenType.EOF, "", line, column));

        long endTime = System.currentTimeMillis();
        log.debug("TOKENIZATION COMPLETE - Total tokens: {}, iterations: {}, time: {} ms",
                tokens.size(), iterations, endTime - startTime);
        log.trace("First 10 tokens: {}", getFirstTokens());

        return tokens;
    }

    private String getFirstTokens() {
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(10, tokens.size());
        for (int i = 0; i < limit; i++) {
            if (i > 0) sb.append(", ");
            sb.append(tokens.get(i));
        }
        return sb.toString();
    }

    /**
     * Парсит следующий токен
     */
    private PgnToken parseNextToken() {
        char current = input.charAt(position);

        int startLine = line;
        int startColumn = column;

        log.trace("parseNextToken: position={}, char='{}' (code=0x{}), line={}, col={}",
                position, current, Integer.toHexString(current), line, column);

        if (current == '[') {
            position++;
            column++;
            return new PgnToken(PgnTokenType.HEADER_START, "[", startLine, startColumn);
        }

        if (current == ']') {
            position++;
            column++;
            return new PgnToken(PgnTokenType.HEADER_END, "]", startLine, startColumn);
        }

        if (current == '"') {
            return parseQuotedString();
        }

        if (current == '{') {
            return parseComment();
        }

        if (current == '(') {
            position++;
            column++;
            return new PgnToken(PgnTokenType.VARIATION_START, "(", startLine, startColumn);
        }

        if (current == ')') {
            position++;
            column++;
            return new PgnToken(PgnTokenType.VARIATION_END, ")", startLine, startColumn);
        }

        if (current == '=' && position + 1 < input.length() && input.charAt(position + 1) == ')') {
            position++;
            column++;
            log.trace("Created standalone '=' token from '=)'");
            return new PgnToken(PgnTokenType.VARIATION_END, ")", startLine, startColumn);
        }

        if (position + 2 <= input.length()) {
            String twoChars = input.substring(position, position + 2);
            if (twoChars.equals("!!") || twoChars.equals("??") ||
                    twoChars.equals("!?") || twoChars.equals("?!")) {

                // Проверяем, что это не часть хода
                boolean isPartOfMove = false;
                if (position + 2 < input.length()) {
                    char nextChar = input.charAt(position + 2);
                    if (Character.isLetterOrDigit(nextChar) || nextChar == 'x') {
                        isPartOfMove = true;
                    }
                }

                if (!isPartOfMove) {
                    position += 2;
                    column += 2;
                    log.trace("Annotation: '{}'", twoChars);
                    return new PgnToken(PgnTokenType.ANNOTATION, twoChars, startLine, startColumn);
                }
            }
        }

        for (MoveAnnotation ann : MoveAnnotation.values()) {
            String symbol = ann.getSymbol();
            if (position + symbol.length() <= input.length()) {
                String possible = input.substring(position, position + symbol.length());
                if (possible.equals(symbol)) {
                    boolean isPartOfMove = false;
                    if (!symbol.startsWith(" ")) {
                        if (position + symbol.length() < input.length()) {
                            char nextChar = input.charAt(position + symbol.length());
                            if (Character.isLetterOrDigit(nextChar) || nextChar == 'x') {
                                if (symbol.length() == 1 && (symbol.equals("!") || symbol.equals("?"))) {
                                    if (position + symbol.length() + 1 < input.length()) {
                                        String nextTwo = input.substring(position, position + symbol.length() + 1);
                                        if (nextTwo.equals("!?") || nextTwo.equals("?!")) {
                                            continue;
                                        }
                                    }
                                }
                                isPartOfMove = true;
                            }
                        }
                    }

                    if (!isPartOfMove) {
                        position += symbol.length();
                        column += symbol.length();
                        log.trace("Annotation: '{}'", symbol);
                        return new PgnToken(PgnTokenType.ANNOTATION, symbol, startLine, startColumn);
                    }
                }
            }
        }

        Matcher matcher = TOKEN_PATTERN.matcher(input.substring(position));
        if (matcher.find() && matcher.start() == 0) {
            String tokenValue = matcher.group();

            if (tokenValue.startsWith("=") || (tokenValue.matches("[#+]+") && !tokenValue.isEmpty())) {
                log.trace("Skipping special token: '{}'", tokenValue);
                position += tokenValue.length();
                column += tokenValue.length();
                return null;
            }

            position += tokenValue.length();
            column += tokenValue.length();

            // ========== createToken может вернуть null для "--" ==========
            // Токен пропущен (например, "--"), уже сдвинули позицию
            return createToken(tokenValue, startLine, startColumn);
        }

        log.warn("Unknown token at position {}: '{}' (0x{})",
                position, current, Integer.toHexString(current));
        position++;
        column++;
        return null;
    }

    /**
     * Парсит строку в кавычках
     */
    private PgnToken parseQuotedString() {
        int startLine = line;
        int startColumn = column;

        position++;
        column++;

        StringBuilder value = new StringBuilder();
        while (position < input.length() && input.charAt(position) != '"') {
            if (input.charAt(position) == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
            value.append(input.charAt(position));
            position++;
        }

        if (position < input.length() && input.charAt(position) == '"') {
            position++;
            column++;
        }

        String quotedValue = value.toString();
        log.trace("Quoted string: '{}'", quotedValue);
        return new PgnToken(PgnTokenType.HEADER_VALUE, quotedValue, startLine, startColumn);
    }

    /**
     * Парсит комментарий
     */
    private PgnToken parseComment() {
        int startLine = line;
        int startColumn = column;

        position++;
        column++;

        StringBuilder comment = new StringBuilder();
        while (position < input.length() && input.charAt(position) != '}') {
            if (input.charAt(position) == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
            comment.append(input.charAt(position));
            position++;
        }

        if (position < input.length() && input.charAt(position) == '}') {
            position++;
            column++;
        }

        String commentText = comment.toString().trim();
        log.trace("Comment: '{}'", commentText);
        return new PgnToken(PgnTokenType.COMMENT_TEXT, commentText, startLine, startColumn);
    }


    /**
     * Создает токен на основе значения
     */
    private PgnToken createToken(String value, int line, int column) {
        if (value == null || value.trim().isEmpty()) {
            log.warn("Skipping empty token at line {}, column {}", line, column);
            return null;
        }

        PgnTokenType type = determineTokenType(value);

        // Если тип null - пропускаем токен (например, для "--")
        if (type == null) {
            log.trace("Skipping token with null type: '{}'", value);
            return null;
        }

        PgnToken token = new PgnToken(type, value, line, column);
        log.trace("Created token: type={}, value='{}', line={}, col={}", type, value, line, column);

        if (type == PgnTokenType.VARIATION_START || type == PgnTokenType.VARIATION_END) {
            log.trace("Created token: type={}, value='{}', line={}, col={}", type, value, line, column);
        }

        return token;
    }

    /**
     * Определяет тип токена
     */
    private PgnTokenType determineTokenType(String value) {
        log.trace("determineTokenType for: '{}'", value);

        if (value.contains("(") || value.contains(")")) {
            log.warn("WARNING: Token contains '(' or ')' in value: '{}'", value);
            switch (value) {
                case "O-O", "O-O-O" -> {
                    return PgnTokenType.MOVE;
                }
                case "(" -> {
                    return PgnTokenType.VARIATION_START;
                }
                case ")" -> {
                    return PgnTokenType.VARIATION_END;
                }
            }
        }

        if (value.matches(".*[А-Яа-я].*")) {
            log.warn("Russian characters detected in token: '{}'", value);
            if (value.matches("[А-Яа-яA-Za-z]+")) {
                return PgnTokenType.HEADER_KEY;
            }
        }

        if (isAnnotationSymbol(value)) {
            log.trace("Annotation symbol: '{}'", value);
            return PgnTokenType.ANNOTATION;
        }

        if (value.matches("^[A-Za-z]?[a-h]?[1-8]?[xX]?[a-h][1-8]=?[QRBN]?\\*")) {
            return PgnTokenType.MOVE;
        }

        if (value.matches("1-0|0-1|1/2-1/2|\\*")) {
            return PgnTokenType.RESULT;
        }

        if (value.matches("\\d+\\s*\\.\\.\\.")) {
            return PgnTokenType.MOVE_NUMBER_ELLIPSIS;
        }
        if (value.matches("\\d+\\s*\\.")) {
            return PgnTokenType.MOVE_NUMBER;
        }

        if (value.matches("\\$\\d+")) {
            return PgnTokenType.NAG;
        }

        if (value.matches("[#+]+")) {
            return null;
        }

        if (value.equals("O-O") || value.equals("O-O-O")) {
            return PgnTokenType.MOVE;
        }

        String[] headerKeys = {
                "Event", "Site", "Date", "Round", "White", "Black", "Result",
                "WhiteElo", "BlackElo", "ECO", "Opening", "Variation",
                "Annotator", "WhiteTeam", "BlackTeam", "Source",
                "WhiteFideId", "BlackFideId", "TimeControl", "PlyCount",
                "EventDate", "Subround", "FEN", "SetUp", "EventType"
        };

        for (String key : headerKeys) {
            if (value.equals(key)) {
                return PgnTokenType.HEADER_KEY;
            }
        }

        if (value.matches("^[A-Za-z]?[a-h]?[1-8]?[xX]?[a-h][1-8]=?[QRBN]?[+#]?$") ||
                value.matches("^[A-Za-z][a-h]?[1-8]?[xX]?[a-h][1-8]=?[QRBN]?[+#]?$")) {
            if (value.length() >= 2 || value.matches("[A-Za-z][a-h]?[1-8]?")) {
                return PgnTokenType.MOVE;
            }
        }

        if (value.matches("^[a-h][1-8]$") ||
                value.matches("^[a-h]x[a-h][1-8]$") ||
                value.matches("^[a-h][1-8]=[QRBN]$") ||
                value.matches("^[a-h]x[a-h][1-8]=[QRBN]$")) {
            log.trace("Pawn move: '{}'", value);
            return PgnTokenType.MOVE;
        }

        if (value.matches("[A-Z][a-zA-Z]+")) {
            return PgnTokenType.HEADER_KEY;
        }

        // === Игнорируем специальные токены ===
        if (value.equals("--")) {
            log.trace("Skipping '--' token (no-op in PGN)");
            return null; // Возвращаем null, но parseNextToken должен корректно обработать
        }

        log.warn("Unknown token type for value: '{}'", value);
        return PgnTokenType.MOVE;
    }

    /**
     * Проверяет, является ли строка символом аннотации из MoveAnnotation
     */
    private boolean isAnnotationSymbol(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        // Проверяем точное совпадение
        for (MoveAnnotation ann : MoveAnnotation.values()) {
            if (value.equals(ann.getSymbol())) {
                return true;
            }
        }

        // ========== ИСПРАВЛЕНИЕ: проверяем без пробелов ==========
        String trimmed = value.trim();
        for (MoveAnnotation ann : MoveAnnotation.values()) {
            String symbol = ann.getSymbol().trim();
            if (trimmed.equals(symbol)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Пропускает пробельные символы
     */
    private void skipWhitespace() {
        while (position < input.length() && Character.isWhitespace(input.charAt(position))) {
            if (input.charAt(position) == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
            position++;
        }
    }
}