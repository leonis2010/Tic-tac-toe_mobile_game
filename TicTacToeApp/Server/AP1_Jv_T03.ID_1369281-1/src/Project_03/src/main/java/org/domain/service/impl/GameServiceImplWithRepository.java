package org.domain.service.impl;

import org.datasource.mapper.EntityToGameMapper;
import org.datasource.mapper.GameToEntityMapper;
import org.datasource.model.GameEntity;
import org.datasource.repository.GameRepository;
import org.domain.model.Game;
import org.domain.model.GameBoard;
import org.domain.service.GameService;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
public class GameServiceImplWithRepository implements GameService {

    private final GameRepository gameRepository;
    private final GameToEntityMapper toEntityMapper;
    private final EntityToGameMapper toDomainMapper;

    public GameServiceImplWithRepository(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
        this.toEntityMapper = new GameToEntityMapper();
        this.toDomainMapper = new EntityToGameMapper();
    }

    @Override
    public void saveGame(Game game) {
        System.out.println("=== DEBUG: GameServiceImplWithRepository.saveGame() called for game: " + game.getId() + " ===");
        System.out.println("=== DEBUG: - GameType: " + game.getGameType() + " ===");
        System.out.println("=== DEBUG: - CreatedAt: " + game.getCreatedAt() + " ==="); // ← ЛОГИРУЕМ ДАТУ

        GameEntity gameEntity = toEntityMapper.mapToEntity(game);
        GameEntity savedEntity = gameRepository.save(gameEntity);

        System.out.println("=== DEBUG: Game saved successfully, repository now has games: " +
                gameRepository.findAll().size() + " ===");
    }
    
    @Override
    public Optional<Game> findGameById(UUID gameId) {
        Optional<GameEntity> gameEntity = gameRepository.findById(gameId);
        return gameEntity.map(toDomainMapper::mapToDomain);
    }

    @Override
    public Point getNextMoveByMinimax(Game game) {
        // Здесь можно сначала сохранить игру, если нужно
        GameBoard board = game.getBoard();
        int[][] boardArray = board.getBoard();

        int bestValue = Integer.MIN_VALUE;
        Point bestMove = new Point(-1, -1);

        for (int i = 0; i < board.getSize(); i++) {
            for (int j = 0; j < board.getSize(); j++) {
                if (boardArray[i][j] == 0) {
                    boardArray[i][j] = -1;
                    int moveValue = minimax(boardArray, 0, false);
                    boardArray[i][j] = 0;

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
    public List<Game> getAllGames() {
        System.out.println("=== DEBUG: GameServiceImplWithRepository.getAllGames() called ===");
        List<GameEntity> entities = gameRepository.findAll();
        List<Game> games = entities.stream()
                .map(this.toDomainMapper::mapToDomain)
                .collect(Collectors.toList());
    
        System.out.println("=== DEBUG: GameServiceImplWithRepository.getAllGames() returning " + games.size() + " games ===");
        return games;
    }
    @Override
    public boolean validateGameBoard(Game currentGame, org.domain.model.GameBoard originalBoard) {
        int[][] currentBoard = currentGame.getBoard().getBoard();
        int[][] originalBoardArray = originalBoard.getBoard();
    
        // ДОБАВИМ ЛОГИРОВАНИЕ ДЛЯ ОТЛАДКИ
        System.out.println("=== VALIDATION DEBUG ===");
        System.out.println("Original board:");
        for (int i = 0; i < 3; i++) {
            System.out.println(java.util.Arrays.toString(originalBoardArray[i]));
        }
        System.out.println("Current board:");
        for (int i = 0; i < 3; i++) {
            System.out.println(java.util.Arrays.toString(currentBoard[i]));
        }
    
        int changes = 0;
        Point changePosition = null;
        int changeValue = 0;
    
        // Проверяем каждую клетку
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (originalBoardArray[i][j] != currentBoard[i][j]) {
                    changes++;
                    changePosition = new Point(i, j);
                    changeValue = currentBoard[i][j];
                    
                    // Проверяем что изменение валидно
                    // 1. Нельзя менять существующие фигуры (0 → 0 не считается изменением)
                    if (originalBoardArray[i][j] != 0) {
                        System.out.println("VALIDATION FAILED: Cannot change existing figure at [" + i + "," + j + "]");
                        return false;
                    }
                    // 2. Новая фигура должна быть 1 (X) или -1 (O)
                    if (currentBoard[i][j] != 1 && currentBoard[i][j] != -1) {
                        System.out.println("VALIDATION FAILED: Invalid figure value at [" + i + "," + j + "]");
                        return false;
                    }
                }
            }
        }

        if (changes != 1) {
            System.out.println("VALIDATION FAILED: Expected 1 change, got " + changes);
            return false;
        }
    
        System.out.println("VALIDATION PASSED: Valid move at [" + changePosition.x + "," + changePosition.y + "] with value " + changeValue);
        return true;
    }


    @Override
    public int checkGameEnd(Game game) {
        GameBoard board = game.getBoard();
        int[][] boardArray = board.getBoard();
        int size = board.getSize();

        for (int i = 0; i < size; i++) {
            if (boardArray[i][0] != 0 &&
                    boardArray[i][0] == boardArray[i][1] &&
                    boardArray[i][1] == boardArray[i][2]) {
                return boardArray[i][0];
            }
        }

        for (int j = 0; j < size; j++) {
            if (boardArray[0][j] != 0 &&
                    boardArray[0][j] == boardArray[1][j] &&
                    boardArray[1][j] == boardArray[2][j]) {
                return boardArray[0][j];
            }
        }

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
            return 0;
        }

        return 2;
    }

    private int minimax(int[][] board, int depth, boolean isMaximizing) {
        int score = evaluate(board);

        if (score == 10) {
            return score - depth;
        }

        if (score == -10) {
            return score + depth;
        }

        if (isBoardFull(board)) {
            return 0;
        }

        if (isMaximizing) {
            int best = Integer.MIN_VALUE;

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board[i][j] == 0) {
                        board[i][j] = -1;
                        best = Math.max(best, minimax(board, depth + 1, false));
                        board[i][j] = 0;
                    }
                }
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board[i][j] == 0) {
                        board[i][j] = 1;
                        best = Math.min(best, minimax(board, depth + 1, true));
                        board[i][j] = 0;
                    }
                }
            }
            return best;
        }
    }

    private int evaluate(int[][] board) {
        for (int row = 0; row < 3; row++) {
            if (board[row][0] == board[row][1] && board[row][1] == board[row][2]) {
                if (board[row][0] == -1) return 10;
                else if (board[row][0] == 1) return -10;
            }
        }

        for (int col = 0; col < 3; col++) {
            if (board[0][col] == board[1][col] && board[1][col] == board[2][col]) {
                if (board[0][col] == -1) return 10;
                else if (board[0][col] == 1) return -10;
            }
        }

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