package org.domain.service.impl;

import org.domain.model.Game;
import org.domain.model.GameBoard;
import org.domain.service.GameService;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.awt.*;

public class GameServiceImpl implements GameService {

    @Override
    public Point getNextMoveByMinimax(Game game) {
        GameBoard board = game.getBoard();
        int[][] boardArray = board.getBoard();

        int bestValue = Integer.MIN_VALUE;
        Point bestMove = new Point(-1, -1);

        // Поиск всех пустых клеток
        for (int i = 0; i < board.getSize(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                if (boardArray[i][j] == 0) {
                    // Попробуем сделать ход
                    boardArray[i][j] = -1; // Ход компьютера (нолики)

                    // Вычисляем значение для этого хода
                    int moveValue = minimax(boardArray, 0, false);

                    // Отменяем ход
                    boardArray[i][j] = 0;

                    // Если это лучший ход, запоминаем его
                    if (moveValue > bestValue) {
                        bestMove = new Point(i, j);
                        bestValue = moveValue;
                    }
                }
            }
        }

        return bestMove;
    }

    @Override
    public boolean validateGameBoard(Game currentGame, GameBoard originalBoard) {
        int[][] currentBoard = currentGame.getBoard().getBoard();
        int[][] originalBoardArray = originalBoard.getBoard();

        // Проверяем, что не изменены предыдущие ходы
        for (int i = 0; i < currentGame.getBoard().getSize(); i++) {
            for (int j = 0; j < currentGame.getBoard().getSize(); j++) {
                // Если в оригинальной доске была фигура, а в текущей она исчезла - ошибка
                if (originalBoardArray[i][j] != 0 && currentBoard[i][j] == 0) {
                    return false;
                }
                // Если в оригинальной доске была фигура, а в текущей другая - ошибка
                if (originalBoardArray[i][j] != 0 && originalBoardArray[i][j] != currentBoard[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public List<Game> getAllGames() {
        // Эта реализация не используется для PVP, но нужна для компиляции.
        // Возвращаем пустой список.
        return new ArrayList<>();
    }

    @Override
    public int checkGameEnd(Game game) {
        GameBoard board = game.getBoard();
        int[][] boardArray = board.getBoard();
        int size = board.getSize();

        // Проверка строк
        for (int i = 0; i < size; i++) {
            if (boardArray[i][0] != 0 &&
                    boardArray[i][0] == boardArray[i][1] &&
                    boardArray[i][1] == boardArray[i][2]) {
                return boardArray[i][0];
            }
        }

        // Проверка столбцов
        for (int j = 0; j < size; j++) {
            if (boardArray[0][j] != 0 &&
                    boardArray[0][j] == boardArray[1][j] &&
                    boardArray[1][j] == boardArray[2][j]) {
                return boardArray[0][j];
            }
        }

        // Проверка диагоналей
        if (boardArray[0][0] != 0 &&
                boardArray[0][0] == boardArray[1][1] &&
                boardArray[1][1] == boardArray[2][2]) {
            return boardArray[0][0];
        }

        if (boardArray[0][2] != 0 &&
                boardArray[0][2] == boardArray[1][1] &&
                boardArray[1][1] == boardArray[2][0]) {
            return boardArray[0][2];
        }

        // Проверка на ничью (все клетки заполнены)
        boolean isFull = true;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (boardArray[i][j] == 0) {
                    isFull = false;
                    break;
                }
            }
            if (!isFull) break;
        }

        if (isFull) {
            return 0; // Ничья
        }

        return 2; // Игра продолжается
    }

    @Override
public void saveGame(Game game) {
    throw new UnsupportedOperationException("Save operation not supported in basic implementation");
}

@Override
public Optional<Game> findGameById(UUID gameId) {
    return Optional.empty();
}

    // Вспомогательный метод для алгоритма Минимакс
    private int minimax(int[][] board, int depth, boolean isMaximizing) {
        int score = evaluate(board);

        // Если максимизирующий игрок выиграл
        if (score == 10) {
            return score - depth;
        }

        // Если минимизирующий игрок выиграл
        if (score == -10) {
            return score + depth;
        }

        // Если ничья
        if (isBoardFull(board)) {
            return 0;
        }

        if (isMaximizing) {
            int best = Integer.MIN_VALUE;

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board[i][j] == 0) {
                        board[i][j] = -1; // Ход компьютера
                        best = Math.max(best, minimax(board, depth + 1, false));
                        board[i][j] = 0; // Отменяем ход
                    }
                }
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board[i][j] == 0) {
                        board[i][j] = 1; // Ход игрока
                        best = Math.min(best, minimax(board, depth + 1, true));
                        board[i][j] = 0; // Отменяем ход
                    }
                }
            }
            return best;
        }
    }

    // Оценка текущей позиции
    private int evaluate(int[][] board) {
        // Проверка строк
        for (int row = 0; row < 3; row++) {
            if (board[row][0] == board[row][1] && board[row][1] == board[row][2]) {
                if (board[row][0] == -1) return 10;
                else if (board[row][0] == 1) return -10;
            }
        }

        // Проверка столбцов
        for (int col = 0; col < 3; col++) {
            if (board[0][col] == board[1][col] && board[1][col] == board[2][col]) {
                if (board[0][col] == -1) return 10;
                else if (board[0][col] == 1) return -10;
            }
        }

        // Проверка диагоналей
        if (board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            if (board[0][0] == -1) return 10;
            else if (board[0][0] == 1) return -10;
        }

        if (board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            if (board[0][2] == -1) return 10;
            else if (board[0][2] == 1) return -10;
        }

        return 0;
    }

    // Проверка, заполнена ли доска
    private boolean isBoardFull(int[][] board) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }
}