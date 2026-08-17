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

package Khrypach.Andrey.chess.kletka.gui.dialogs;

import Khrypach.Andrey.chess.kletka.gui.board.ChessSymbols;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageKeys;
import Khrypach.Andrey.chess.kletka.gui.languages.LanguageManager;
import Khrypach.Andrey.chess.kletka.gui.model.MoveNode;
import Khrypach.Andrey.chess.kletka.gui.model.ParentNode;
import Khrypach.Andrey.chess.kletka.gui.model.RootNode;
import Khrypach.Andrey.chess.kletka.gui.model.Variation;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Координатор диалогов выбора вариантов
 * Отвечает за создание и показ диалогов, а также за формирование списка выборов
 */
public class DialogCoordinator {
    private final LanguageManager languageManager = LanguageManager.getInstance();

    private final Variation rootVariation;
    @Getter
    private final RootNode rootNode;
    @Getter
    private final Variation mainLine;
    @Getter
    private Variation currentVariation;
    @Getter
    private ParentNode currentNode;

    public DialogCoordinator(RootNode rootNode, Variation rootVariation, Variation mainLine) {
        this.rootNode = rootNode;
        this.rootVariation = rootVariation;
        this.mainLine = mainLine;
        this.currentVariation = mainLine;
        this.currentNode = rootNode;
    }

    /**
     * Обновляет текущее состояние для диалогов
     */
    public void updateState(Variation currentVariation, ParentNode currentNode) {
        this.currentVariation = currentVariation;
        this.currentNode = currentNode;
    }

    /**
     * Показывает диалог выбора варианта и возвращает выбор пользователя
     * Возвращает null если пользователь отменил
     */
    public VariationChoiceDialog.Choice showVariationDialog(Move move, Piece piece,
                                                            boolean isCapture, Piece promotionPiece) {
        if (move == null) return null;

        String moveDesc = getMoveDescription(move, piece, isCapture, promotionPiece);
        String sideStr = piece.getPieceSide() == Side.WHITE ?
                languageManager.get(LanguageKeys.GAME_WHITE) : languageManager.get(LanguageKeys.GAME_BLACK);

        // Случай 1: В корне
        if (currentVariation == rootVariation || (currentNode != null && currentNode.isRoot())) {
            return showRootVariationCreationDialog(moveDesc, sideStr);
        }

        // Случай 2: Есть следующий ход - создаем вариант
        if (currentNode != null && currentNode.getNext() != null) {
            return showVariationCreationDialog(moveDesc, sideStr);
        }

        // Случай 3: Есть подварианты с таким же ходом
        if (currentNode != null && !currentNode.getSubVariations().isEmpty()) {
            String currentUci = move.getFrom().toString().toLowerCase() +
                    move.getTo().toString().toLowerCase();
            if (promotionPiece != null) currentUci += getPromotionChar(promotionPiece);

            for (Variation subVar : currentNode.getSubVariations()) {
                if (!subVar.isEmpty()) {
                    ParentNode firstMove = subVar.getFirstNode();
                    if (firstMove != null && !firstMove.isRoot() &&
                            firstMove.getUciMove().equals(currentUci)) {
                        // Показываем диалог выбора варианта на развилке
                        boolean isWhiteTurn = (currentNode.getAbsolutePly() % 2 == 0);
                        // Добавляем все варианты с этого узла
                        List<Variation> variations = new ArrayList<>(currentNode.getSubVariations());
                        return showBranchChoiceDialog(variations, isWhiteTurn);
                    }
                }
            }
        }

        // Случай 4: Просто создаем новый вариант (если нет next и нет подвариантов)
        if (currentNode != null && currentNode.getNext() == null) {
            return showSimpleVariationCreationDialog(moveDesc, sideStr);
        }

        return null;
    }

    /**
     * Простой диалог создания варианта (когда нет next и нет подвариантов)
     */
    private VariationChoiceDialog.Choice showSimpleVariationCreationDialog(String moveDesc, String sideStr) {
        List<VariationChoiceDialog.Choice> choices = new ArrayList<>();
        choices.add(new VariationChoiceDialog.Choice(
                String.format(languageManager.get(LanguageKeys.DIALOG_VARIATION_NEW), sideStr, moveDesc),
                null, true));

        VariationChoiceDialog dialog = new VariationChoiceDialog(choices);
        return dialog.showAndWait();
    }

    /**
     * Показывает диалог для корневого варианта
     */
    private VariationChoiceDialog.Choice showRootVariationCreationDialog(String moveDesc, String sideStr) {
        List<VariationChoiceDialog.Choice> choices = new ArrayList<>();

        // 1. Новый вариант
        choices.add(new VariationChoiceDialog.Choice(
                String.format(languageManager.get(LanguageKeys.DIALOG_VARIATION_NEW),
                        sideStr, ChessSymbols.convertToChessSymbols(moveDesc)),
                null, true));
        String addDots = sideStr.equals(languageManager.get(LanguageKeys.GAME_WHITE)) ? "." : "...";
        // 2. Существующие корневые варианты
        for (Variation var : rootNode.getSubVariations()) {
            if (!var.isEmpty() && var.getFirstNode() != null) {
                ParentNode firstMove = var.getFirstNode();
                String firstMoveSan = firstMove.getSan();
                if (firstMoveSan == null || firstMoveSan.isEmpty()) {
                    firstMoveSan = "?";
                }

                // Помечаем главную линию
                choices.add(new VariationChoiceDialog.Choice(var,
                        String.format(languageManager.get(var.isMainLine() ? LanguageKeys.DIALOG_VARIATION_REPLACE_MAIN :
                                        LanguageKeys.DIALOG_VARIATION_EXISTING), 1, addDots,
                                ChessSymbols.convertToChessSymbols(firstMoveSan), sideStr),
                        false));
            }
        }

        VariationChoiceDialog dialog = new VariationChoiceDialog(choices);
        return dialog.showAndWait();
    }

    /**
     * Показывает диалог создания варианта на текущем узле
     */
    private VariationChoiceDialog.Choice showVariationCreationDialog(String moveDesc, String sideStr) {
        List<VariationChoiceDialog.Choice> choices = new ArrayList<>();

        // 1. Новый вариант
        choices.add(new VariationChoiceDialog.Choice(
                String.format(languageManager.get(LanguageKeys.DIALOG_VARIATION_NEW), sideStr, moveDesc),
                null, true));

        // 2. Существующие подварианты (ИСКЛЮЧАЕМ ГЛАВНУЮ ЛИНИЮ!)
        int varIndex = 1;
        String addDots = sideStr.equals(languageManager.get(LanguageKeys.GAME_WHITE)) ? "." : "...";
        for (Variation var : currentNode.getSubVariations()) {
            if (!var.isEmpty() && !var.isMainLine()) {  // ← ИСКЛЮЧАЕМ ГЛАВНУЮ ЛИНИЮ!
                ParentNode firstMove = var.getFirstNode();
                String firstMoveSan = firstMove != null ? firstMove.getSan() : "?";
                choices.add(new VariationChoiceDialog.Choice(var,
                        String.format(languageManager.get(LanguageKeys.DIALOG_VARIATION_EXISTING),
                                varIndex,
                                addDots,
                                ChessSymbols.convertToChessSymbols(firstMoveSan),
                                sideStr),
                        false));
                varIndex++;
            }
        }

        // 3. Перезаписать главную линию (если есть next) - ЭТО ЕДИНСТВЕННОЕ МЕСТО ДЛЯ ГЛАВНОЙ ЛИНИИ
        if (currentNode != null && currentNode.getNext() != null) {
            String nextSan = currentNode.getNext().getSan();
            int moveNumber = (currentNode.getAbsolutePly() + 2) / 2; // Правильный номер хода
            String dots = sideStr.equals(languageManager.get(LanguageKeys.GAME_WHITE)) ? "." : "...";
            choices.add(new VariationChoiceDialog.Choice(currentVariation,
                    String.format(languageManager.get(LanguageKeys.DIALOG_VARIATION_REPLACE_MAIN),
                            moveNumber, dots,
                            ChessSymbols.convertToChessSymbols(nextSan)),
                    false));
        }

        VariationChoiceDialog dialog = new VariationChoiceDialog(choices);
        return dialog.showAndWait();
    }

    /**
     * Показывает диалог выбора варианта на развилке
     */
    private VariationChoiceDialog.Choice showBranchChoiceDialog(List<Variation> variations, boolean nextIsWhite) {
        if (variations == null || variations.isEmpty()) {
            return null;
        }

        String sideStr = nextIsWhite ?
                languageManager.get(LanguageKeys.GAME_WHITE) :
                languageManager.get(LanguageKeys.GAME_BLACK);

        // Сортируем: главная линия первая
        variations.sort((v1, v2) -> {
            if (v1.isMainLine() && !v2.isMainLine()) return -1;
            if (!v1.isMainLine() && v2.isMainLine()) return 1;
            return v1.getName().compareTo(v2.getName());
        });

        List<VariationChoiceDialog.Choice> choices = new ArrayList<>();
        for (Variation var : variations) {
            String moveDesc = getVariationFirstMoveDesc(var);

            String displayName = var.isMainLine() ?
                    languageManager.get(LanguageKeys.MAIN_LINE) : var.getName();

            if (displayName == null || displayName.isEmpty()) {
                displayName = languageManager.get(LanguageKeys.VARIATION_DEFAULT_NAME);
            }

            String prefix = var.isMainLine() ? "★ " : "";
            choices.add(new VariationChoiceDialog.Choice(
                    var,
                    String.format("%s%s (%s): %s",
                            prefix,
                            displayName,
                            sideStr,
                            ChessSymbols.convertToChessSymbols(moveDesc)),
                    false
            ));
        }

        VariationChoiceDialog dialog = new VariationChoiceDialog(choices);
        return dialog.showAndWait();
    }

    /**
     * Вспомогательный метод для получения первого хода варианта
     */
    private String getVariationFirstMoveDesc(Variation var) {
        if (var == null) return "?";

        ParentNode firstMove = var.getFirstNode();
        if (firstMove != null && !firstMove.isRoot()) {
            return firstMove.getSan();
        }

        List<ParentNode> moves = var.getMoves();
        if (!moves.isEmpty()) {
            return moves.get(0).getSan();
        }

        return "?";
    }

    /**
     * Получает описание хода
     */
    private String getMoveDescription(Move move, Piece piece, boolean isCapture, Piece promotionPiece) {
        MoveNode tempNode = new MoveNode(move, piece, isCapture, promotionPiece);
        return tempNode.getSan();
    }

    private String getPromotionChar(Piece piece) {
        return switch (piece) {
            case WHITE_QUEEN, BLACK_QUEEN -> "q";
            case WHITE_ROOK, BLACK_ROOK -> "r";
            case WHITE_BISHOP, BLACK_BISHOP -> "b";
            case WHITE_KNIGHT, BLACK_KNIGHT -> "n";
            default -> "";
        };
    }
}
