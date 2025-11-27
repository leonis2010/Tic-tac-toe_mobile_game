package org.web.controller;


import org.domain.service.GameService;
import org.domain.model.Game;
import org.web.mapper.DtoToGameMapper;
import org.web.mapper.GameToDtoMapper;
import org.web.model.GameDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.web.model.GameHistoryDto;
import org.web.model.LeaderboardDto;
import org.web.utils.AuthUtils;

import java.util.*;
import java.util.stream.Collectors;

@RestController//Это контроллер - класс, который обрабатывает HTTP запросы
@RequestMapping("/api/games")//Не нужно повторять /api/games в каждом методе и Все пути начинаются
@CrossOrigin(origins = "*")// разрешает запросы с любых сайтов
public class GameController {

    // Внедрение зависимостей через конструктор
    private final GameService gameService;
    private final GameToDtoMapper gameToDtoMapper;
    private final DtoToGameMapper dtoToGameMapper;

    @Autowired
    public GameController(
            GameService gameService,
            GameToDtoMapper gameToDtoMapper,
            DtoToGameMapper dtoToGameMapper,
            AuthUtils authUtils
    ) {
        this.gameService = gameService;
        this.gameToDtoMapper = gameToDtoMapper;
        this.dtoToGameMapper = dtoToGameMapper;
        this.authUtils = authUtils;
        System.out.println("🟢🟢🟢=== DEBUG: GameController - GameService type: " + gameService.getClass().getName() + " ===");
    }


    @PostMapping("/start")
    public ResponseEntity<GameDto> startNewGame(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            System.out.println("=== DEBUG: GameController.startNewGame called ===");
// 👇 ПРОВЕРКА АВТОРИЗАЦИИ
            ResponseEntity<GameDto> authResponse = checkAuthorization(authHeader);
            if (authResponse != null) {
                return authResponse;
            }
            // Извлечь имя пользователя из заголовка Authorization
            String username = extractUsernameFromAuthHeader(authHeader);
            System.out.println("=== DEBUG: Extracted username: " + username + " ===");

            Game newGame = new Game();

            // Установить создателя игры
            newGame.setCreatorUsername(username);
            newGame.setCurrentPlayerUsername(username);
            newGame.setStatus("WAITING_FOR_PLAYERS");

            // Сохраняем новую игру
            gameService.saveGame(newGame);

            System.out.println("=== DEBUG: New game created - ID: " + newGame.getId() +
                    ", creator: " + newGame.getCreatorUsername() +
                    ", status: " + newGame.getStatus() + " ===");

            GameDto gameDto = gameToDtoMapper.mapToDto(newGame);
            return new ResponseEntity<>(gameDto, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("=== ERROR in startNewGame: " + e.getMessage());
            e.printStackTrace();
            GameDto errorDto = new GameDto();
            errorDto.setStatus("ERROR: " + e.getMessage());
            return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @PostMapping("/start/computer")
    public ResponseEntity<GameDto> startNewGameWithComputer(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            System.out.println("=== DEBUG: startNewGameWithComputer called ===");
            // 👇 ПРОВЕРКА АВТОРИЗАЦИИ
            ResponseEntity<GameDto> authResponse = checkAuthorization(authHeader);
            if (authResponse != null) {
                return authResponse;
            }
            String username = extractUsernameFromAuthHeader(authHeader);
            Game newGame = new Game();
            newGame.setCreatorUsername(username);
            newGame.setCurrentPlayerUsername(username);
            newGame.setPlayer2Username("COMPUTER");
            newGame.setStatus("IN_PROGRESS");
            newGame.setGameType("PVE");

            // СОХРАНЯЕМ И СРАЗУ ПРОВЕРЯЕМ
            gameService.saveGame(newGame);

            // ПРОВЕРКА ПЕРЕД ВОЗВРАТОМ
            Optional<Game> savedGame = gameService.findGameById(newGame.getId());
            if (savedGame.isPresent()) {
                System.out.println("=== DEBUG: SAVED GAME VERIFICATION - Type: " + savedGame.get().getGameType() + " ===");
            }

            System.out.println("=== DEBUG: New COMPUTER game created - ID: " + newGame.getId() +
                    ", type: " + newGame.getGameType() +
                    ", creator: " + newGame.getCreatorUsername() +
                    ", player2: " + newGame.getPlayer2Username() +
                    ", status: " + newGame.getStatus() + " ===");

            GameDto gameDto = gameToDtoMapper.mapToDto(newGame);
            return new ResponseEntity<>(gameDto, HttpStatus.CREATED);

        } catch (Exception e) {
            System.out.println("=== ERROR in startNewGameWithComputer: " + e.getMessage());
            e.printStackTrace();
            GameDto errorDto = new GameDto();
            errorDto.setStatus("ERROR: " + e.getMessage());
            return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Autowired
    private AuthUtils authUtils;

    private String extractUsernameFromAuthHeader(String authHeader) {
        return authUtils.extractUsernameFromAuthHeader(authHeader);
    }


    @PostMapping("/start/player")
    public ResponseEntity<GameDto> startNewGameWithPlayer(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            System.out.println("=== DEBUG: startNewGameWithPlayer called ===");
            // 👇 ПРОВЕРКА АВТОРИЗАЦИИ
            ResponseEntity<GameDto> authResponse = checkAuthorization(authHeader);
            if (authResponse != null) {
                return authResponse;
            }
            String username = extractUsernameFromAuthHeader(authHeader);

            Game newGame = new Game();
            newGame.setCreatorUsername(username);
            newGame.setCurrentPlayerUsername(username);
            newGame.setStatus("WAITING_FOR_PLAYERS");
            newGame.setGameType("PVP");

            gameService.saveGame(newGame);

            System.out.println("=== DEBUG: New PLAYER game created - ID: " + newGame.getId() +
                    ", type: " + newGame.getGameType() +
                    ", status: " + newGame.getStatus() + " ===");

            GameDto gameDto = gameToDtoMapper.mapToDto(newGame);
            return new ResponseEntity<>(gameDto, HttpStatus.CREATED);

        } catch (Exception e) {
            System.out.println("=== ERROR in startNewGameWithPlayer: " + e.getMessage());
            e.printStackTrace();
            GameDto errorDto = new GameDto();
            errorDto.setStatus("ERROR: " + e.getMessage());
            return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

@GetMapping("/test")
public String gameTest() {
    return "GameController is working!";
}

@GetMapping("/simple")
public ResponseEntity<Map<String, String>> simpleGame() {
    Map<String, String> response = new HashMap<>();
    response.put("status", "OK");
    response.put("message", "Simple game endpoint works");
    return ResponseEntity.ok(response);
}

    @PostMapping("/{gameId}")
    public ResponseEntity<GameDto> makeMove(
            @PathVariable UUID gameId,
            @RequestBody GameDto updatedGameDto) {
        System.out.println("🟢🟢🟢 DEBUG: makeMove METHOD CALLED! gameId: " + gameId);

        try {
            System.out.println("=== DEBUG: GameController.makeMove() called for gameId: " + gameId + " ===");

            // 1. Получаем оригинальную игру ДО любых изменений
            Optional<Game> optionalOriginalGame = gameService.findGameById(gameId);
            if (optionalOriginalGame.isEmpty()) {
                System.out.println("=== ERROR: Game with ID " + gameId + " not found ===");
                GameDto errorDto = new GameDto();
                errorDto.setStatus("ERROR: Game not found");
                return new ResponseEntity<>(errorDto, HttpStatus.NOT_FOUND);
            }
            Game originalGame = optionalOriginalGame.get();

            // 👇 СОХРАНИТЕ gameType ИЗ ОРИГИНАЛЬНОЙ ИГРЫ
            String originalGameType = originalGame.getGameType();
            System.out.println("=== DEBUG: Original game type: " + originalGameType + " ===");

            System.out.println("=== DEBUG: Client DTO - currentPlayer: " + updatedGameDto.getCurrentPlayerUsername() + " ===");

            // 2. Преобразуем DTO в доменную модель
            Game updatedGame = dtoToGameMapper.mapToDomain(updatedGameDto);

            // 👇 ВОССТАНОВИТЕ gameType ИЗ ОРИГИНАЛЬНОЙ ИГРЫ
            updatedGame.setGameType(originalGameType);

            System.out.println("=== DEBUG: After mapping - currentPlayer: " + updatedGame.getCurrentPlayerUsername() +
                    ", gameType: " + updatedGame.getGameType() + " ===");

            // 3. Валидируем ход игрока
            if (!gameService.validateGameBoard(updatedGame, originalGame.getBoard())) {
                System.out.println("=== ERROR: Player move validation failed ===");
                GameDto errorDto = new GameDto();
                errorDto.setStatus("ERROR: Invalid move - validation failed");
                return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
            }

            // 4. Обновляем доску игры на сервере (gameType уже установлен)
            updatedGame.setBoard(updatedGameDto.getBoard().toDomain());

            // 5. Проверяем, не закончилась ли игра после хода игрока
            int gameResult = gameService.checkGameEnd(updatedGame);
            if (gameResult != 2) {
                String status = getStatusString(gameResult);
                String winner = determineWinner(gameResult, updatedGame);

                updatedGame.setStatus(status);
                updatedGame.setWinner(winner);
                updatedGame.setCurrentPlayerUsername(null);

                GameDto responseDto = this.gameToDtoMapper.mapToDto(updatedGame);
                gameService.saveGame(updatedGame);
                return new ResponseEntity<>(responseDto, HttpStatus.OK);
            }
            System.out.println("=== DEBUG: After validation - currentPlayer: " + updatedGame.getCurrentPlayerUsername());

            System.out.println("=== DEBUG: GAME TYPE DIAGNOSTICS ===");
            System.out.println("=== DEBUG: - player2Username: '" + updatedGame.getPlayer2Username() + "'");
            System.out.println("=== DEBUG: - is null: " + (updatedGame.getPlayer2Username() == null));
            System.out.println("=== DEBUG: - isEmpty: " + (updatedGame.getPlayer2Username() != null && updatedGame.getPlayer2Username().isEmpty()));
            System.out.println("=== DEBUG: - equals 'Waiting for player...': " + "Waiting for player...".equals(updatedGame.getPlayer2Username()));
            System.out.println("=== DEBUG: - equals 'COMPUTER': " + "COMPUTER".equals(updatedGame.getPlayer2Username()));
            System.out.println("=== DEBUG: - Should be PVE: " + (updatedGame.getPlayer2Username() == null || updatedGame.getPlayer2Username().isEmpty() || "COMPUTER".equals(updatedGame.getPlayer2Username())));
            //6. ОПРЕДЕЛЯЕМ ТИП ИГРЫ ПО gameType ВМЕСТО player2Username
            boolean isPvP = "PVP".equals(updatedGame.getGameType());
            System.out.println("=== DEBUG: GAME TYPE DIAGNOSTICS ===");
            System.out.println("=== DEBUG: - gameType: '" + updatedGame.getGameType() + "'");
            System.out.println("=== DEBUG: - isPvP: " + isPvP);
            System.out.println("=== DEBUG: - player2Username: '" + updatedGame.getPlayer2Username() + "'");

            if (isPvP) {
                System.out.println("=== DEBUG: Processing PVP move ===");
                // Это PVP - переключаем игроков
                String currentPlayer = originalGame.getCurrentPlayerUsername();
                String nextPlayer = currentPlayer.equals(updatedGame.getCreatorUsername())
                        ? updatedGame.getPlayer2Username()
                        : updatedGame.getCreatorUsername();

                updatedGame.setCurrentPlayerUsername(nextPlayer);
                updatedGame.setPlayerTurn(true);
            } else {
                System.out.println("=== DEBUG: Processing PVE move ===");
                // Это PVE - ход компьютера
                java.awt.Point computerMove = gameService.getNextMoveByMinimax(updatedGame);
                System.out.println("=== DEBUG: Computer move calculated: " + computerMove + " ===");

                if (computerMove.x != -1 && computerMove.y != -1) {
                    updatedGame.getBoard().setCell(computerMove.x, computerMove.y, -1);
                    System.out.println("=== DEBUG: Computer made move at [" + computerMove.x + "," + computerMove.y + "] ===");

                    // Проверка после хода компьютера
                    int computerGameResult = gameService.checkGameEnd(updatedGame);
                    if (computerGameResult != 2) {
                        String status = getStatusString(computerGameResult);
                        String winner = determineWinner(computerGameResult, updatedGame);

                        updatedGame.setStatus("PLAYER_WON");
                        updatedGame.setWinner(winner);
                        updatedGame.setCurrentPlayerUsername(null);

                        GameDto responseDto = this.gameToDtoMapper.mapToDto(updatedGame);
                        gameService.saveGame(updatedGame);
                        return new ResponseEntity<>(responseDto, HttpStatus.OK);
                    }

                    updatedGame.setPlayerTurn(true);
                    updatedGame.setCurrentPlayerUsername(updatedGame.getCreatorUsername());
                    System.out.println("=== DEBUG: After computer move - next player: " + updatedGame.getCurrentPlayerUsername() + " ===");
                } else {
                    System.out.println("=== DEBUG: No valid computer move found! ===");
                }
            }
            System.out.println("=== DEBUG: After game logic - currentPlayer: " + updatedGame.getCurrentPlayerUsername());
            // 7. Проверяем результат после хода компьютера (для PVE)
            int finalStatus = gameService.checkGameEnd(updatedGame);
            updatedGame.setStatus(getStatusString(finalStatus));

            // 👇 ДОБАВИТЬ ЗДЕСЬ - ПЕРЕД СОХРАНЕНИЕМ
            System.out.println("=== DEBUG: Before save - currentPlayer: " + updatedGame.getCurrentPlayerUsername() + " ===");

            GameDto responseDto = this.gameToDtoMapper.mapToDto(updatedGame);

            // 8. Сохраняем обновленную игру
            gameService.saveGame(updatedGame);

            System.out.println("=== DEBUG: GameController.makeMove() returning status: " + responseDto.getStatus() + " ===");
            return new ResponseEntity<>(responseDto, HttpStatus.OK);

        } catch (Exception e) {
            System.out.println("=== ERROR: GameController.makeMove() failed: " + e.getMessage() + " ===");
            e.printStackTrace();
            GameDto errorDto = new GameDto();
            errorDto.setStatus("ERROR: Internal server error - " + e.getMessage());
            return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String determineWinner(int gameResult, Game game) {
        String winner;
        switch (gameResult) {
            case 1: // Крестики (X) победили - это создатель игры
                winner = game.getCreatorUsername();
                break;
            case -1: // Нолики (O) победили
                // Если есть второй игрок - это он, иначе компьютер
                if (game.getPlayer2Username() != null && !game.getPlayer2Username().isEmpty()) {
                    winner = game.getPlayer2Username();
                } else {
                    winner = "COMPUTER";
                }
                break;
            case 0: // Ничья
                // Для PVE игр - специальное сообщение
                if ("PVE".equals(game.getGameType())) {
                    winner = "DRAW_PVE"; // ← СПЕЦИАЛЬНЫЙ СТАТУС ДЛЯ PVE НИЧЬЕЙ
                } else {
                    winner = "DRAW"; // ← ОБЫЧНАЯ НИЧЬЯ ДЛЯ PVP
                }
                break;
            default:
                winner = "UNKNOWN";
        }

        System.out.println("=== DEBUG: determineWinner - gameResult: " + gameResult +
                ", gameType: " + game.getGameType() +
                ", winner: " + winner + " ===");

        return winner;
    }

    /**
     * Получение текущего состояния игры
     */
    @GetMapping("/{gameId}")
    public ResponseEntity<GameDto> getGame(@PathVariable UUID gameId,
                                           @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // 1. Извлекаем username из заголовка Authorization
            String username = extractUsernameFromAuthHeader(authHeader);

            // 2. Находим игру
            Optional<Game> optionalGame = gameService.findGameById(gameId);
            if (optionalGame.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // 3. Добавляем игрока в активные
            playerJoinedGame(gameId, username);

            // 4. Возвращаем игру С ПОЛЕМ WINNER
            Game game = optionalGame.get();
            GameDto gameDto = gameToDtoMapper.mapToDto(game);

            // 👇 ДОБАВИТЬ ЛОГИРОВАНИЕ ДЛЯ ДИАГНОСТИКИ
            System.out.println("=== DEBUG: getGame - returning game with winner: " + gameDto.getWinner() + " ===");

            return ResponseEntity.ok(gameDto);

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Вспомогательные методы

    private boolean isValidMove(Game originalGame, Game updatedGame) {
        int[][] originalBoard = originalGame.getBoard().getBoard();
        int[][] updatedBoard = updatedGame.getBoard().getBoard();

        // Проверим, что размеры совпадают
        if (originalBoard.length != updatedBoard.length) {
            return false;
        }

        int differences = 0;
        int playerMoveRow = -1;
        int playerMoveCol = -1;

        // Ищем различия между досками
        for (int i = 0; i < originalBoard.length; i++) {
            for (int j = 0; j < originalBoard[i].length; j++) {
                if (originalBoard[i][j] != updatedBoard[i][j]) {
                    differences++;
                    playerMoveRow = i;
                    playerMoveCol = j;
                }
            }
        }

        // Должно быть ровно одно различие (ход игрока)
        if (differences != 1) {
            System.out.println("Invalid move: " + differences + " differences found");
            return false;
        }

        // Клетка должна была быть пустой, а стала занята игроком (1)
        boolean isValid = originalBoard[playerMoveRow][playerMoveCol] == 0 &&
                updatedBoard[playerMoveRow][playerMoveCol] == 1;

        if (!isValid) {
            System.out.println("Invalid cell state: was " + originalBoard[playerMoveRow][playerMoveCol] +
                    ", became " + updatedBoard[playerMoveRow][playerMoveCol]);
        }

        return isValid;
    }

    private String getStatusString(int gameStatus) {
        switch (gameStatus) {
            case 1:
            case -1:
                return "PLAYER_WON";
            case 0:
                return "DRAW";
            case 2:
                return "IN_PROGRESS";
            default:
                return "IN_PROGRESS";
        }
    }

    @GetMapping("")
    public ResponseEntity<List<GameDto>> getAllGames() {
        try {
            System.out.println("=== DEBUG: GameController.getAllGames() called ===");
            List<Game> allGames = gameService.getAllGames();

            // ФИЛЬТРАЦИЯ: только доступные PVP игры
            List<Game> availableGames = allGames.stream()
                    .filter(game -> "PVP".equals(game.getGameType())) // только PVP
                    .filter(game -> isGameAvailableForJoin(game)) // проверка доступности для присоединения
                    .collect(Collectors.toList());

            System.out.println("=== DEBUG: Returning " + availableGames.size() + " available PVP games ===");

            List<GameDto> gameDtos = availableGames.stream()
                    .map(gameToDtoMapper::mapToDto)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(gameDtos);
        } catch (Exception e) {
            System.out.println("=== ERROR: GameController.getAllGames() failed: " + e.getMessage() + " ===");
            e.printStackTrace();
            GameDto errorDto = new GameDto();
            errorDto.setStatus("ERROR: Failed to retrieve games - " + e.getMessage());
            return ResponseEntity.status(500).body(List.of(errorDto));
        }
    }

    // Вспомогательный метод для проверки доступности игры для присоединения
    private boolean isGameAvailableForJoin(Game game) {
        // Игра доступна для присоединения если:
        return "PVP".equals(game.getGameType()) && // только PVP игры
                "WAITING_FOR_PLAYERS".equals(game.getStatus()) && // ожидает игроков
                (game.getPlayer2Username() == null || // нет второго игрока
                        game.getPlayer2Username().isEmpty() ||
                        "Waiting for player...".equals(game.getPlayer2Username())); // или ожидает игрока
    }

    /**
 * Присоединение к существующей игре, ожидающей второго игрока.
 * Путь: POST /api/games/{gameId}/join
 */
    @PostMapping("/{gameId}/join")
    public ResponseEntity<GameDto> joinGame(@PathVariable UUID gameId,
                                            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            System.out.println("=== DEBUG: GameController.joinGame() called for gameId: " + gameId + " ===");

            // Извлечь имя пользователя из заголовка Authorization
            String joiningUsername = extractUsernameFromAuthHeader(authHeader);
            if (joiningUsername == null) {
                System.out.println("=== 🔴 DEBUG: Unauthorized - cannot extract username ===");
                // 👇 ВОЗВРАЩАЕМ 401 - чтобы клиентский Interceptor запустил refresh
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }


            Optional<Game> optionalGame = gameService.findGameById(gameId);

            if (optionalGame.isEmpty()) {
                System.out.println("=== DEBUG: Game not found ===");
                GameDto errorDto = new GameDto();
                errorDto.setStatus("ERROR: Game not found");
                return new ResponseEntity<>(errorDto, HttpStatus.NOT_FOUND);
            }

            Game game = optionalGame.get();

            // ПРОВЕРКИ ДОСТУПНОСТИ ИГРЫ
            System.out.println("=== DEBUG: Game availability check ===");
            System.out.println("=== DEBUG: - Status: " + game.getStatus());
            System.out.println("=== DEBUG: - Player2: '" + game.getPlayer2Username() + "'");
            System.out.println("=== DEBUG: - GameType: " + game.getGameType());
            System.out.println("=== DEBUG: - Creator: " + game.getCreatorUsername());

            // 1. Проверить что игра PVP
            if (!"PVP".equals(game.getGameType())) {
                System.out.println("=== DEBUG: Cannot join non-PVP game ===");
                GameDto errorDto = new GameDto();
                errorDto.setStatus("ERROR: Cannot join this type of game");
                return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
            }

            // 2. Проверить что игра ожидает игроков
            if (!"WAITING_FOR_PLAYERS".equals(game.getStatus())) {
                System.out.println("=== DEBUG: Game is not waiting for players ===");
                GameDto errorDto = new GameDto();
                errorDto.setStatus("ERROR: Game is not available for joining");
                return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
            }

            // 3. Проверить что есть свободное место (второй игрок не установлен)
            if (game.getPlayer2Username() != null &&
                    !game.getPlayer2Username().isEmpty() &&
                    !"Waiting for player...".equals(game.getPlayer2Username())) {
                System.out.println("=== DEBUG: Game is already full ===");
                GameDto errorDto = new GameDto();
                errorDto.setStatus("ERROR: Game is full");
                return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
            }

            // 4. Проверить что пользователь не создатель игры
            if (joiningUsername.equals(game.getCreatorUsername())) {
                System.out.println("=== DEBUG: Creator cannot join their own game ===");
                GameDto errorDto = new GameDto();
                errorDto.setStatus("ERROR: Cannot join your own game");
                return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
            }

            // ВСЕ ПРОВЕРКИ ПРОЙДЕНЫ - присоединяем игрока

            // Установить второго игрока
            game.setPlayer2Username(joiningUsername);
            game.setStatus("IN_PROGRESS");
            // Установить текущего игрока - создатель ходит первым
            game.setCurrentPlayerUsername(game.getCreatorUsername());

            // Сохранить обновлённую игру
            gameService.saveGame(game);

            GameDto gameDto = gameToDtoMapper.mapToDto(game);
            System.out.println("=== DEBUG: GameController.joinGame() successful for gameId: " + gameId +
                    ", creator: " + game.getCreatorUsername() + " (X)" +
                    ", player2: " + joiningUsername + " (O)" +
                    ", first move: " + game.getCreatorUsername() +
                    ", status now IN_PROGRESS ===");
            return ResponseEntity.ok(gameDto);

        } catch (Exception e) {
            System.out.println("=== ERROR: GameController.joinGame() failed: " + e.getMessage() + " ===");
            e.printStackTrace();
            GameDto errorDto = new GameDto();
            errorDto.setStatus("ERROR: Failed to join game - " + e.getMessage());
            return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Игрок заходит в игру - добавляем в активные
     */
    private void playerJoinedGame(UUID gameId, String username) {
        try {
            Optional<Game> optionalGame = gameService.findGameById(gameId);
            if (optionalGame.isPresent()) {
                Game game = optionalGame.get();
                gameService.saveGame(game);
            }
        } catch (Exception e) {
            System.out.println("=== ERROR adding active player: " + e.getMessage());
        }
    }

    /**
     * Игрок выходит из игры - удаляем из активных
     */
    @PostMapping("/{gameId}/player-left")
    public ResponseEntity<GameDto> playerLeftGame(@PathVariable UUID gameId,
                                                  @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // 👇 ПРОВЕРКА АВТОРИЗАЦИИ
            ResponseEntity<GameDto> authResponse = checkAuthorization(authHeader);
            if (authResponse != null) {
                return authResponse;
            }
            String username = extractUsernameFromAuthHeader(authHeader);
            System.out.println("=== DEBUG: Player " + username + " left game: " + gameId + " ===");

            Optional<Game> optionalGame = gameService.findGameById(gameId);
            if (optionalGame.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Game game = optionalGame.get();

            // 👇 ЕСЛИ СОЗДАТЕЛЬ ВЫШЕЛ ИЗ ОЖИДАЮЩЕЙ PVP ИГРЫ - ПРЕВРАЩАЕМ В PVE
            if (username.equals(game.getCreatorUsername()) &&
                    "WAITING_FOR_PLAYERS".equals(game.getStatus()) &&
                    "PVP".equals(game.getGameType())) {

                System.out.println("=== DEBUG: Converting waiting PVP game to PVE: " + gameId + " ===");

                game.setGameType("PVE");
                game.setStatus("PLAYER_LEFT");

                gameService.saveGame(game);
                GameDto gameDto = gameToDtoMapper.mapToDto(game);
                return ResponseEntity.ok(gameDto);
            }

            // Если игра PVP и в процессе, помечаем как завершенную
            if ("PVP".equals(game.getGameType()) && "IN_PROGRESS".equals(game.getStatus())) {
                System.out.println("=== DEBUG: Marking PVP game as finished due to player leave ===");

                // Определяем оставшегося игрока как победителя
                String remainingPlayer = game.getCreatorUsername().equals(username)
                        ? game.getPlayer2Username()
                        : game.getCreatorUsername();

                // ИЗМЕНИТЬ: устанавливаем специальный статус
                game.setStatus("PLAYER_LEFT");
                game.setWinner(remainingPlayer);
                game.setCurrentPlayerUsername(null);

                System.out.println("=== DEBUG: Player left, winner: " + remainingPlayer + " ===");
            }

            gameService.saveGame(game);
            GameDto gameDto = gameToDtoMapper.mapToDto(game);
            return ResponseEntity.ok(gameDto);

        } catch (Exception e) {
            System.out.println("=== ERROR in playerLeftGame: " + e.getMessage() + " ===");
            e.printStackTrace();
            GameDto errorDto = new GameDto();
            errorDto.setStatus("ERROR: " + e.getMessage());
            return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Очистка неактивных игр
     */
    @PostMapping("/cleanup-inactive")
    public ResponseEntity<Map<String, String>> cleanupInactiveGames() {
        try {
            System.out.println("=== DEBUG: Cleaning up inactive games ===");

            List<Game> allGames = gameService.getAllGames();
            int removedCount = 0;

            for (Game game : allGames) {
//                GameEntity entity = gameToEntityMapper.mapToEntity(game);

                // Удаляем игры без активных игроков
//                if (!entity.hasActivePlayers()) {
//                    System.out.println("=== Removing inactive game: " + game.getId());
//                    // gameService.deleteGame(game.getId());
//                    removedCount++;
//                }
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Removed " + removedCount + " inactive games");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Cleanup failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Игрок выходит из игры
     * DELETE /api/games/{gameId}/leave
     */
    @DeleteMapping("/{gameId}/leave")
    public ResponseEntity<Void> leaveGame(@PathVariable UUID gameId,
                                          @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String username = extractUsernameFromAuthHeader(authHeader);
            playerLeftGame(gameId, username);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Проверяет авторизацию пользователя и возвращает username
     * Если пользователь не авторизован - возвращает UNAUTHORIZED response
     */
    private ResponseEntity<GameDto> checkAuthorization(String authHeader) {
        String username = extractUsernameFromAuthHeader(authHeader);
        if (username == null) {
            System.out.println("=== DEBUG: Unauthorized user detected ===");
            GameDto errorDto = new GameDto();
            errorDto.setStatus("ERROR: Unauthorized - please login again");
            return new ResponseEntity<>(errorDto, HttpStatus.UNAUTHORIZED);
        }
        return null; // null означает что авторизация успешна
    }

    /**
     * Получение истории игр текущего пользователя
     */
    @GetMapping("/history")
    public ResponseEntity<List<GameHistoryDto>> getGameHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            System.out.println("=== DEBUG: GameController.getGameHistory() called ===");

            // Проверка авторизации
            String username = extractUsernameFromAuthHeader(authHeader);
            if (username == null) {
                System.out.println("=== DEBUG: Unauthorized access to game history ===");
                return ResponseEntity.status(401).build();
            }

            System.out.println("=== DEBUG: Getting game history for user: " + username + " ===");

            // Получаем все игры
            List<Game> allGames = gameService.getAllGames();

            // Фильтруем завершенные игры, где пользователь участвовал
            List<GameHistoryDto> history = allGames.stream()
                    .filter(game -> isGameCompleted(game) && isUserInGame(game, username))
                    .map(game -> mapGameToHistoryDto(game, username))
                    .sorted((g1, g2) -> Long.compare(g2.getGameDate().getTime(), g1.getGameDate().getTime())) // Сортировка по дате (новые first)
                    .collect(Collectors.toList());

            System.out.println("=== DEBUG: Returning " + history.size() + " history items ===");
            return ResponseEntity.ok(history);

        } catch (Exception e) {
            System.out.println("=== ERROR: GameController.getGameHistory() failed: " + e.getMessage() + " ===");
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // Вспомогательные методы для GameController
    private boolean isGameCompleted(Game game) {
        return "PLAYER_WON".equals(game.getStatus()) ||
                "DRAW".equals(game.getStatus()) ||
                "PLAYER_LEFT".equals(game.getStatus());
    }

    private boolean isUserInGame(Game game, String username) {
        return username.equals(game.getCreatorUsername()) ||
                username.equals(game.getPlayer2Username());
    }

    private GameHistoryDto mapGameToHistoryDto(Game game, String currentUsername) {
        // Определяем результат для текущего пользователя
        String result = determineGameResult(game, currentUsername);

        // Определяем имя второго игрока
        String player2 = game.getPlayer2Username();
        if (player2 == null || player2.isEmpty() || "Waiting for player...".equals(player2)) {
            player2 = "COMPUTER";
        }
        // 👇 ИСПОЛЬЗУЕМ РЕАЛЬНУЮ ДАТУ СОЗДАНИЯ ИГРЫ
        Date gameDate = game.getCreatedAt() != null ? game.getCreatedAt() : new Date();

        GameHistoryDto dto = new GameHistoryDto(
                game.getId(),
                game.getCreatorUsername(),
                player2,
                game.getGameType(),
                result,
                gameDate, // В реальном приложении нужно хранить дату создания игры
                game.getWinner()
        );
        dto.setCurrentUserUsername(currentUsername);

        return dto;
    }

    private String determineGameResult(Game game, String currentUsername) {
        if ("DRAW".equals(game.getStatus()) || "DRAW_PVE".equals(game.getWinner())) {
            return "DRAW";
        }

        if (currentUsername.equals(game.getWinner())) {
            return "WIN";
        } else {
            return "LOSE";
        }
    }

    // Добавить в ./main/java/org/web/controller/GameController.java

    /**
     * Получение таблицы лидеров (топ-20 игроков)
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardDto>> getLeaderboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            System.out.println("=== DEBUG: GameController.getLeaderboard() called ===");

            // Проверка авторизации
            String username = extractUsernameFromAuthHeader(authHeader);
            if (username == null) {
                System.out.println("=== DEBUG: Unauthorized access to leaderboard ===");
                return ResponseEntity.status(401).build();
            }

            // Получаем все завершенные игры
            List<Game> allGames = gameService.getAllGames();
            List<Game> completedGames = allGames.stream()
                    .filter(this::isGameCompleted)
                    .collect(Collectors.toList());

            System.out.println("=== DEBUG: Total completed games for leaderboard: " + completedGames.size() + " ===");

            // Рассчитываем статистику для каждого пользователя
            Map<String, LeaderboardDto> leaderboardMap = calculateLeaderboardStats(completedGames);

            // Преобразуем в список и сортируем по рейтингу
            List<LeaderboardDto> leaderboard = new ArrayList<>(leaderboardMap.values());
            leaderboard.sort((a, b) -> Integer.compare(b.getRating(), a.getRating())); // по убыванию рейтинга

            // Берем топ-20
            int limit = Math.min(leaderboard.size(), 20);
            List<LeaderboardDto> top20 = leaderboard.subList(0, limit);

            System.out.println("=== DEBUG: Returning leaderboard with " + top20.size() + " players ===");
            return ResponseEntity.ok(top20);

        } catch (Exception e) {
            System.out.println("=== ERROR: GameController.getLeaderboard() failed: " + e.getMessage() + " ===");
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Рассчитывает статистику для таблицы лидеров
     */
    private Map<String, LeaderboardDto> calculateLeaderboardStats(List<Game> completedGames) {
        Map<String, LeaderboardDto> statsMap = new HashMap<>();

        for (Game game : completedGames) {
            // Обрабатываем только игры с определенным результатом
            if (!isGameCompleted(game)) continue;

            // Учитываем обоих игроков в PVP играх
            if ("PVP".equals(game.getGameType())) {
                processPlayerStats(statsMap, game.getCreatorUsername(), game, true);
                if (game.getPlayer2Username() != null && !game.getPlayer2Username().isEmpty() &&
                        !"Waiting for player...".equals(game.getPlayer2Username()) &&
                        !"COMPUTER".equals(game.getPlayer2Username())) {
                    processPlayerStats(statsMap, game.getPlayer2Username(), game, false);
                }
            }
            // Учитываем только создателя в PVE играх
            else if ("PVE".equals(game.getGameType())) {
                processPlayerStats(statsMap, game.getCreatorUsername(), game, true);
            }
        }

        return statsMap;
    }

    /**
     * Обрабатывает статистику для одного игрока
     */
    private void processPlayerStats(Map<String, LeaderboardDto> statsMap, String username, Game game, boolean isPlayer1) {
        if (username == null || username.isEmpty()) return;

        LeaderboardDto stats = statsMap.getOrDefault(username,
                new LeaderboardDto(username, 0, 0, 0, 0, 0.0, 0));

        // Увеличиваем количество сыгранных игр
        stats.setGamesPlayed(stats.getGamesPlayed() + 1);

        // Определяем результат для игрока
        String result = determinePlayerResult(game, username);

        switch (result) {
            case "WIN":
                stats.setGamesWon(stats.getGamesWon() + 1);
                break;
            case "LOSE":
                stats.setGamesLost(stats.getGamesLost() + 1);
                break;
            case "DRAW":
                stats.setGamesDrawn(stats.getGamesDrawn() + 1);
                break;
        }

        // Пересчитываем процент побед
        if (stats.getGamesPlayed() > 0) {
            double winRate = (double) stats.getGamesWon() / stats.getGamesPlayed() * 100;
            stats.setWinRate(Math.round(winRate * 100.0) / 100.0); // округляем до 2 знаков
        }

        // Рассчитываем рейтинг (можно настроить формулу)
        stats.setRating(calculateRating(stats));

        statsMap.put(username, stats);
    }

    /**
     * Рассчитывает рейтинг игрока
     */
    private int calculateRating(LeaderboardDto stats) {
        // Формула рейтинга: победы * 10 + ничьи * 5 - поражения * 2
        return stats.getGamesWon() * 10 + stats.getGamesDrawn() * 5 - stats.getGamesLost() * 2;
    }

    /**
     * Определяет результат игры для конкретного игрока
     */
    private String determinePlayerResult(Game game, String username) {
        if ("DRAW".equals(game.getStatus()) || "DRAW".equals(game.getWinner())) {
            return "DRAW";
        }

        if (username.equals(game.getWinner())) {
            return "WIN";
        } else {
            return "LOSE";
        }
    }
}