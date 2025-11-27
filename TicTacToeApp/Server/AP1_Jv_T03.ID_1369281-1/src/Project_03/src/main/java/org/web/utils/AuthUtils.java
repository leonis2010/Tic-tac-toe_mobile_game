package org.web.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;

@Component
public class AuthUtils {

    // Временное хранилище токенов (в реальном приложении используйте Redis или БД)
    private final Map<String, String> tokenToUsernameMap = new ConcurrentHashMap<>();
    private final Map<String, String> usernameToTokenMap = new ConcurrentHashMap<>();
    private static final String SECRET_KEY;

    static {
        // Генерация безопасного ключа
        byte[] keyBytes = new byte[32]; // 256 бит
        new java.security.SecureRandom().nextBytes(keyBytes);
        SECRET_KEY = java.util.Base64.getEncoder().encodeToString(keyBytes);
    }
//дебаг
//private static final String SECRET_KEY = "mySuperSecretKeyForDevelopment1234567890abcdefghijklmnopqrstuvwxyz";
    public String getSecretKey() {
        return SECRET_KEY;
    }
    /**
     * Извлекает username из заголовка Authorization
     */
    public String extractUsernameFromAuthHeader(String authHeader) {
        if (authHeader == null) {
            System.out.println("=== DEBUG: No auth header provided ===");
            return null;
        }

        try {
            if (authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // 👇 РЕАЛЬНАЯ ПРОВЕРКА JWT ТОКЕНА
                try {
                    Claims claims = Jwts.parser()
                            .setSigningKey(SECRET_KEY)
                            .parseClaimsJws(token)
                            .getBody();

                    String username = claims.getSubject();
                    System.out.println("=== DEBUG: Valid JWT token -> username: " + username + " ===");

                    // ДОПОЛНИТЕЛЬНО: синхронизируем с мапой
                    if (!tokenToUsernameMap.containsKey(token)) {
                        System.out.println("=== DEBUG: JWT valid but token not in map, registering... ===");
                        registerToken(username, token);
                    }

                    return username;

                } catch (ExpiredJwtException e) {
                    System.out.println("=== DEBUG: JWT token EXPIRED at " + e.getClaims().getExpiration() + " ===");
                    // Удаляем истекший токен из мапы
                    tokenToUsernameMap.remove(token);
                    // 👇 ВОЗВРАЩАЕМ NULL - чтобы вызвать 401 ошибку и запустить refresh
                    return null;
                } catch (Exception e) {
                    System.out.println("=== DEBUG: Invalid JWT token: " + e.getMessage() + " ===");
                    return null;
                }

            } else if (authHeader.startsWith("Basic ")) {
                // Basic auth логика (оставляем как есть)
                String base64Credentials = authHeader.substring(6);
                byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
                String credentials = new String(decodedBytes);
                String[] parts = credentials.split(":", 2);
                String username = parts.length > 0 ? parts[0] : null;
                System.out.println("=== DEBUG: Basic auth -> username: " + username + " ===");
                return username;
            } else {
                System.out.println("=== DEBUG: Unsupported auth type: " + authHeader + " ===");
                return null;
            }
        } catch (Exception e) {
            System.out.println("=== ERROR extracting username from auth header: " + e.getMessage());
            return null;
        }
    }

    /**
     * Регистрирует токен для пользователя
     */
    public void registerToken(String username, String token) {
        // Удаляем старый токен если есть
        String oldToken = usernameToTokenMap.get(username);
        if (oldToken != null) {
            tokenToUsernameMap.remove(oldToken);
        }

        tokenToUsernameMap.put(token, username);
        usernameToTokenMap.put(username, token);
        System.out.println("=== DEBUG: Registered token for user: " + username + " ===");
    }

    /**
     * Удаляет токен пользователя
     */
    public void removeToken(String username) {
        String token = usernameToTokenMap.get(username);
        if (token != null) {
            tokenToUsernameMap.remove(token);
            usernameToTokenMap.remove(username);
            System.out.println("=== DEBUG: Removed token for user: " + username + " ===");
        }
    }

    /**
     * Проверяет валидность токена
     */
    public boolean isValidToken(String token) {
        try {
            // Проверяем и JWT валидность и наличие в мапе
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return tokenToUsernameMap.containsKey(token);
        } catch (Exception e) {
            return false;
        }
    }
    // Добавим трекинг активности
    private final Map<String, Long> userLastActivity = new ConcurrentHashMap<>();
    private final Map<String, String> userCurrentActivity = new ConcurrentHashMap<>();
    private static final long INACTIVITY_TIMEOUT_MS = 2 * 60 * 1000; // 2 минуты без активности
    private static final long HEARTBEAT_INTERVAL_MS = 30 * 1000; // 30 секунд

    public void updateUserActivity(String username, String activityName, String action) {
        long currentTime = System.currentTimeMillis();
        userLastActivity.put(username, currentTime);

        if ("START".equals(action)) {
            userCurrentActivity.put(username, activityName);
            System.out.println("=== DEBUG: User " + username + " started activity: " + activityName + " ===");
        } else if ("STOP".equals(action)) {
            userCurrentActivity.remove(username);
            System.out.println("=== DEBUG: User " + username + " stopped activity: " + activityName + " ===");
        }

        System.out.println("=== DEBUG: Current activities: " + userCurrentActivity + " ===");
    }

    public void processHeartbeat(String username) {
        long currentTime = System.currentTimeMillis();
        userLastActivity.put(username, currentTime);
        System.out.println("=== DEBUG: Heartbeat from " + username + " at " + currentTime + " ===");
    }

    /**
     * Проверяет, активен ли пользователь
     */
    public boolean isUserActive(String username) {
        Long lastActivity = userLastActivity.get(username);
        if (lastActivity == null) {
            return false;
        }

        long timeSinceLastActivity = System.currentTimeMillis() - lastActivity;
        boolean isActive = timeSinceLastActivity < INACTIVITY_TIMEOUT_MS;

        System.out.println("=== DEBUG: User " + username + " active: " + isActive +
                " (last activity: " + (timeSinceLastActivity / 1000) + "s ago) ===");

        if (!isActive) {
            // Автоматически разлогиниваем неактивного пользователя
            removeToken(username);
            userLastActivity.remove(username);
            userCurrentActivity.remove(username);
            System.out.println("=== DEBUG: Auto-logout due to inactivity for user: " + username + " ===");
        }

        return isActive;
    }

    /**
     * Проверяет, залогинен ли пользователь (с учетом активности)
     */
    public boolean isUserAlreadyLoggedIn(String username) {
        // Сначала проверяем активность
        if (!isUserActive(username)) {
            return false;
        }

        // Затем проверяем валидность токена
        String token = usernameToTokenMap.get(username);
        if (token == null) {
            return false;
        }

        return isValidToken(token);
    }

    public String generateJwtToken(String username, long expiresInSeconds) {
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + expiresInSeconds * 1000))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public String extractUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            System.out.println("=== DEBUG: Failed to extract username from token: " + e.getMessage() + " ===");
            return null;
        }
    }
}