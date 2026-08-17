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

package Khrypach.Andrey.chess.kletka.core;


import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.Piece;
import com.github.bhlangonijr.chesslib.Side;

import java.util.Arrays;
import java.util.List;

public class Kletka {
    public static void main(String[] args) {
        System.out.println("=== Демонстрация библиотеки chesslib 1.3.6 ===\n");

        // 1. Создание доски и базовые операции
        demonstrateBasicOperations();

        // 2. Генерация легальных ходов
        demonstrateLegalMoves();

        // 3. Проверка специальных ходов
        demonstrateSpecialMoves();

        // 4. Работа с FEN (правильный способ)
        demonstrateFEN();

        // 5. Анализ позиции
        demonstratePositionAnalysis();
    }

    private static void demonstrateBasicOperations() {
        System.out.println("--- 1. Базовые операции ---");

        // Создаем начальную позицию (конструктор без параметров дает начальную позицию)
        Board board = new Board();
        System.out.println("Начальная позиция:");
        System.out.println(board);
        System.out.println("FEN: " + board.getFen());

        // Делаем несколько ходов
        System.out.println("\nДелаем ходы: e4, e5, Nf3");

        // e4 - используем строковое представление хода
        board.doMove("e2e4");
        System.out.println("После e4:");
        System.out.println(board);

        // e5
        board.doMove("e7e5");
        System.out.println("После e5:");
        System.out.println(board);

        // Nf3
        board.doMove("g1f3");
        System.out.println("После Nf3:");
        System.out.println(board);

        // Проверка фигуры на конкретном поле
        Piece piece = board.getPiece(Square.F3);
        System.out.println("На поле f3: " + piece);
        System.out.println("Цвет фигуры: " + piece.getPieceSide());

        System.out.println("Текущая очередь хода: " + board.getSideToMove());
        System.out.println();
    }

    private static void demonstrateLegalMoves() {
        System.out.println("--- 2. Генерация легальных ходов ---");

        Board board = new Board();

        // Получаем все легальные ходы в начальной позиции
        List<Move> legalMoves = board.legalMoves();
        System.out.println("Всего легальных ходов в начальной позиции: " + legalMoves.size());

        // Покажем первые 10 ходов для примера
        System.out.println("Первые 10 ходов:");
        legalMoves.stream()
                .limit(10)
                .forEach(m -> System.out.println("  " + m + " (из " + m.getFrom() + " в " + m.getTo() + ")"));

        System.out.println();
    }

    private static void demonstrateSpecialMoves() {
        System.out.println("--- 3. Специальные ходы (рокировка) ---");

        // Создаем доску и делаем ходы для получения позиции с рокировкой
        Board board = new Board();

        // Очищаем доску и ставим позицию вручную через FEN
        // FEN для позиции с возможностью рокировки
        String fenForCastling = "r3k2r/pppppppp/8/8/8/8/PPPPPPPP/R3K2R w KQkq - 0 1";
        board.loadFromFen(fenForCastling);

        System.out.println("Позиция для рокировки:");
        System.out.println(board);

        // Проверяем легальные ходы
        List<Move> moves = board.legalMoves();
        System.out.println("Все легальные ходы в этой позиции:");
        moves.forEach(m -> System.out.println("  " + m));

        // Пробуем сделать рокировку (короткую)
        System.out.println("\nПробуем сделать короткую рокировку (O-O):");
        try {
            // В chesslib рокировка обозначается как "e1g1" для белых
            board.doMove("e1g1");
            System.out.println("Рокировка выполнена!");
            System.out.println(board);
        } catch (Exception e) {
            System.out.println("Не удалось сделать рокировку: " + e.getMessage());
        }

        System.out.println();
    }

    private static void demonstrateFEN() {
        System.out.println("--- 4. Работа с FEN ---");

        String[] fens = {
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",  // Начальная
                "8/8/8/4k3/8/8/8/4K3 w - - 0 1",                              // Короли
                "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6 0 2" // После e4 e5
        };

        for (String fen : fens) {
            Board board = new Board();
            board.loadFromFen(fen);
            System.out.println("FEN: " + fen);
            System.out.println("Позиция:");
            System.out.println(board);

            // Подсчет фигур через стрим (одномерный массив)
            Piece[] pieces = board.boardToArray();
            long pieceCount = Arrays.stream(pieces)
                    .filter(piece -> piece != Piece.NONE)
                    .count();

            System.out.println("  Очередь хода: " + board.getSideToMove());
            System.out.println("  Количество фигур: " + pieceCount);

            // Можно также посчитать по цветам
            long whitePieces = Arrays.stream(pieces)
                    .filter(piece -> piece != Piece.NONE && piece.getPieceSide() == Side.WHITE)
                    .count();
            long blackPieces = Arrays.stream(pieces)
                    .filter(piece -> piece != Piece.NONE && piece.getPieceSide() == Side.BLACK)
                    .count();

            System.out.println("  Белых фигур: " + whitePieces);
            System.out.println("  Черных фигур: " + blackPieces);
            System.out.println();
        }
    }

    private static void demonstratePositionAnalysis() {
        System.out.println("--- 5. Анализ позиции ---");

        // Создаем позицию для анализа (известная позиция)
        Board board = new Board();

        // Загружаем позицию через FEN
        board.loadFromFen("r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 2 4");

        System.out.println("Позиция для анализа (Итальянская партия):");
        System.out.println(board);

        // Проверяем различные аспекты позиции
        System.out.println("Анализ позиции:");
        System.out.println("  Очередь хода: " + board.getSideToMove());

        // Проверяем, под шахом ли король
        boolean isCheck = board.isKingAttacked();
        System.out.println("  Шах: " + isCheck);

        // Количество легальных ходов
        List<Move> moves = board.legalMoves();
        System.out.println("  Легальных ходов: " + moves.size());

        // Покажем несколько интересных ходов
        System.out.println("  Примеры ходов:");
        moves.stream()
                .limit(5)
                .forEach(m -> System.out.println("    " + m));

        System.out.println();

        // Проверим матовую позицию
        // Знаменитый "детский мат" (4 хода)
        Board mateBoard = new Board();
        mateBoard.loadFromFen("r1bqkbnr/pppp1Qpp/2n5/4p3/2B1P3/8/PPPP1PPP/RNB1K1NR b KQkq - 0 4");
        System.out.println("Детский мат черным:");
        System.out.println(mateBoard);
        System.out.println("Мат? " + mateBoard.isMated()); // true
        System.out.println("Шах? " + mateBoard.isKingAttacked()); // true

        boolean isMate = mateBoard.isMated();
        System.out.println("  Это мат? " + isMate);

        // Проверим, есть ли легальные ходы у черных
        if (!isMate) {
            System.out.println("  Легальные ходы черных:");
            for (Move move : mateBoard.legalMoves()) {
                System.out.println("    " + move);
            }
        }

        System.out.println();
    }
}