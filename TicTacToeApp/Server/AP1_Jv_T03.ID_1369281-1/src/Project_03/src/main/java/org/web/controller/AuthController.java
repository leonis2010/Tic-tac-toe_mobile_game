package org.web.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.web.utils.AuthUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    @Autowired
    private AuthUtils authUtils;

    // Хранилище пользователей: username -> UserData (password + id)
    private static class UserData {
        String password;
        Long id;

        UserData(String password, Long id) {
            this.password = password;
            this.id = id;
        }
    }

    private final Map<String, UserData> users = new ConcurrentHashMap<>();

    @PostMapping("/clear")
    public ResponseEntity<Map<String, String>> clearUsers() {
        users.clear();
        Map<String, String> response = new HashMap<>();
        response.put("message", "All users cleared");
        return ResponseEntity.ok(response);
    }

    // 👇 МЕТОД ДЛЯ ТЕСТИРОВАНИЯ
    @GetMapping("/test")
    public String test() {
        return "AuthController is working!";
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> userData) {
        System.out.println("=== 🟡 DEBUG: AuthController.register() CALLED ===");
        System.out.println("=== 🟡 DEBUG: Request body: " + userData + " ===");

        String username = userData.get("username");
        String password = userData.get("password");

        System.out.println("=== 🟡 DEBUG: Username: " + username + ", Password: " + password + " ===");

        if (username == null || username.trim().isEmpty()) {
            System.out.println("=== 🔴 DEBUG: Username validation failed ===");
            return ResponseEntity.badRequest().body(createErrorResponse("Username is required"));
        }

        if (password == null || password.trim().isEmpty()) {
            System.out.println("=== 🔴 DEBUG: Password validation failed ===");
            return ResponseEntity.badRequest().body(createErrorResponse("Password is required"));
        }

        if (users.containsKey(username)) {
            System.out.println("=== 🔴 DEBUG: User already exists: " + username + " ===");
            return ResponseEntity.badRequest().body(createErrorResponse("User already exists"));
        }

        System.out.println("=== 🟢 DEBUG: User validation passed ===");

        // Генерируем ID
        Long userId = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
        users.put(username, new UserData(password, userId));

        // Генерируем JWT токены
        String accessToken = authUtils.generateJwtToken(username, 30); //15 * 60   15 минут
        String refreshToken = authUtils.generateJwtToken(username, 7 * 24 * 60 * 60); // 7 дней

        System.out.println("=== 🟢 DEBUG: Tokens generated ===");

        // Возвращаем ответ в НОВОМ формате
        Map<String, Object> response = new HashMap<>();
        response.put("id", userId);
        response.put("username", username);
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("expiresIn", 30L);//15 * 60L
        response.put("message", "User registered successfully");

        System.out.println("=== 🟢 DEBUG: Returning NEW JWT format response ===");
        System.out.println("=== 🟢 DEBUG: Response: " + response + " ===");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> userData) {
        System.out.println("=== 🟡 DEBUG: AuthController.login() CALLED ===");

        String username = userData.get("username");
        String password = userData.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(createErrorResponse("Имя пользователя и пароль обязательны"));
        }

        UserData userDataStored = users.get(username);
        if (userDataStored == null || !userDataStored.password.equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Неверное имя пользователя или пароль"));
        }

        // ПРОВЕРКА: пользователь уже залогинен на другом устройстве
        if (authUtils.isUserAlreadyLoggedIn(username)) {
            System.out.println("=== DEBUG: User " + username + " already logged in from another device ===");
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse("Пользователь уже авторизован на другом устройстве"));
        }

        // Генерируем JWT токены
        String accessToken = authUtils.generateJwtToken(username, 30); //15 * 60 15 минут
        String refreshToken = authUtils.generateJwtToken(username, 7 * 24 * 60 * 60); // 7 дней

        // Регистрируем токен в системе
        authUtils.registerToken(username, accessToken);

        // Успешный логин
        Map<String, Object> response = new HashMap<>();
        response.put("id", userDataStored.id);
        response.put("username", username);
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("expiresIn", 30L);//15 * 60L
        response.put("message", "Login successful");

        System.out.println("=== DEBUG: User " + username + " logged in successfully with JWT ===");
        System.out.println("=== DEBUG: Response: " + response + " ===");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        System.out.println("=== 🟡 DEBUG: AuthController.refreshToken() CALLED ===");

        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResponse("Refresh token is required"));
        }

        try {
            // 👇 ИЗВЛЕКАЕМ USERNAME ИЗ REFRESH ТОКЕНА
            String username = authUtils.extractUsernameFromAuthHeader("Bearer " + refreshToken);

            if (username == null) {
                System.out.println("=== DEBUG: Failed to extract username from refresh token ===");
                return ResponseEntity.status(401).body(createErrorResponse("Invalid refresh token"));
            }

            System.out.println("=== DEBUG: Refresh token for user: " + username + " ===");

            // Проверяем валидность refresh токена (используем существующий метод)
            if (!authUtils.isValidToken(refreshToken)) {
                System.out.println("=== DEBUG: Invalid refresh token ===");
                return ResponseEntity.status(401).body(createErrorResponse("Invalid refresh token"));
            }

            // 👇 ГЕНЕРИРУЕМ ТОКЕНЫ ДЛЯ ТОГО ЖЕ ПОЛЬЗОВАТЕЛЯ
            String newAccessToken = authUtils.generateJwtToken(username, 30);
            String newRefreshToken = authUtils.generateJwtToken(username, 7 * 24 * 60 * 60);

            // Регистрируем новый токен
            authUtils.registerToken(username, newAccessToken);

            Map<String, Object> response = new HashMap<>();
            response.put("id", getUserIdByUsername(username)); // ← ДОБАВЛЯЕМ ID
            response.put("username", username); // ← ВОЗВРАЩАЕМ ОРИГИНАЛЬНОГО USERNAME
            response.put("accessToken", newAccessToken);
            response.put("refreshToken", newRefreshToken);
            response.put("expiresIn", 30L);

            System.out.println("=== 🟢 DEBUG: Tokens refreshed for user: " + username + " ===");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("=== DEBUG: Invalid refresh token: " + e.getMessage() + " ===");
            return ResponseEntity.status(401).body(createErrorResponse("Invalid refresh token"));
        }
    }

    private boolean isValidRefreshToken(String refreshToken) {
        try {
            io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.parser()
                    .setSigningKey(authUtils.getSecretKey())
                    .parseClaimsJws(refreshToken)
                    .getBody();

            String username = claims.getSubject();
            // Refresh токен валиден если он есть в мапе и не истек
            return authUtils.isValidToken(refreshToken) &&
                    claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Long getUserIdByUsername(String username) {
        UserData userData = users.get(username);
        return userData != null ? userData.id : null;
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String username = authUtils.extractUsernameFromAuthHeader(authHeader);
            if (username != null) {
                authUtils.removeToken(username);
                System.out.println("=== DEBUG: User " + username + " logged out ===");
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Logged out successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("=== ERROR in logout: " + e.getMessage() + " ===");
            return ResponseEntity.status(500).build();
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        return errorResponse;
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<Map<String, String>> heartbeat(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            if (username != null) {
                authUtils.processHeartbeat(username);
            }

            Map<String, String> response = new HashMap<>();
            response.put("status", "ok");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("=== ERROR in heartbeat: " + e.getMessage() + " ===");
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/activity")
    public ResponseEntity<Map<String, String>> trackActivity(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String activity = request.get("activity");
            String action = request.get("action");

            if (username != null && activity != null && action != null) {
                authUtils.updateUserActivity(username, activity, action);
            }

            Map<String, String> response = new HashMap<>();
            response.put("status", "ok");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("=== ERROR in activity tracking: " + e.getMessage() + " ===");
            return ResponseEntity.status(500).build();
        }
    }
}