# Tic-tac-toe_mobile_game
# 🎯 Tic-Tac-Toe Multiplayer Game

<div align="center">

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)

**Full-stack мобильное приложение для игры в крестики-нолики с реальными игроками и компьютером**

[Особенности](#-особенности) • [Архитектура](#-архитектура) • [Установка](#-установка) • [API](#-api)

</div>

## 🎮 О проекте

Это современное мобильное приложение для игры в крестики-нолики с поддержкой:
- **PVP режим** - игра с реальными игроками
- **PVE режим** - игра против ИИ с алгоритмом Minimax
- **Таблица лидеров** и история игр
- **JWT аутентификация** с автоматическим обновлением токенов

## ✨ Особенности

### 🎯 Игровые режимы
- **Против игрока** - создавайте комнаты и приглашайте друзей
- **Против компьютера** - умный ИИ с алгоритмом Minimax
- **Real-time обновления** - мгновенная синхронизация ходов

### 🔐 Безопасность
- **JWT аутентификация** с access/refresh токенами
- **Автоматическое обновление** токенов
- **Валидация ходов** на сервере

### 📊 Статистика
- **История игр** с детальной информацией
- **Таблица лидеров** с рейтингами игроков
- **Профиль игрока** с статистикой побед/поражений

## 🏗️ Архитектура

### 📱 Клиент (Android)
┌─────────────────┐ ┌──────────────────┐ ┌─────────────────┐
│ UI Layer │ ←→ │ ViewModel │ ←→ │ Repository │
│ (Activities) │ │ (State Management)│ │ (Data Access) │
└─────────────────┘ └──────────────────┘ └─────────────────┘
↓
┌─────────────────┐ ┌──────────────────┐ ┌─────────────────┐
│ Local Storage │ ←→ │ API Service │ ←→ │ Auth Interceptor│
│ (SharedPrefs) │ │ (Retrofit) │ │ (Token Refresh) │
└─────────────────┘ └──────────────────┘ └─────────────────┘

### 🖥️ Сервер (Spring Boot)

┌─────────────────┐ ┌──────────────────┐ ┌─────────────────┐
│ Controller │ ←→ │ Service Layer │ ←→ │ Repository │
│ (REST API) │ │ (Business Logic)│ │ (Data Access) │
└─────────────────┘ └──────────────────┘ └─────────────────┘
↓
┌─────────────────┐ ┌──────────────────┐ ┌─────────────────┐
│ Auth Utils │ ←→ │ Game Logic │ ←→ │ In-Memory │
│ (JWT Tokens) │ │ (Minimax AI) │ │ Storage │
└─────────────────┘ └──────────────────┘ └─────────────────┘

## 🛠️ Технологии

### Клиент
- **Kotlin** - основной язык разработки
- **MVVM** - Model-View-ViewModel архитектура
- **Retrofit** - HTTP клиент для API запросов
- **RxJava** - реактивное программирование
- **Dagger Hilt** - dependency injection
- **Room Database** - локальное хранилище

### Сервер
- **Java/Spring Boot** - бэкенд фреймворк
- **JWT** - аутентификация и авторизация
- **Minimax Algorithm** - ИИ для игры против компьютера
- **In-Memory Storage** - хранение игр в памяти
- **REST API** - HTTP API для клиента

## 📦 Установка

### Предварительные требования
- Android Studio Arctic Fox+
- JDK 11+
- Android SDK API 21+

### Запуск клиента
```bash
git clone https://github.com/your-username/tic-tac-toe-app.git
cd tic-tac-toe-app
```
## Открыть в Android Studio
## Собрать и запустить на эмуляторе/устройстве
## Запуск сервера
```bash
cd server
./mvnw spring-boot:run
# Сервер запустится на http://localhost:8088
```

## 🎯 Игровой процесс

1. **Регистрация/Вход в систему**
2. **Выбор режима**: PVP или PVE
3. **Создание игры** или присоединение к существующей
4. **Игра** с real-time обновлениями
5. **Просмотр статистики** после игры

## 🔐 Безопасность

- **JWT токены** с коротким временем жизни
- **Refresh токены** для продления сессии
- **Валидация ходов** на сервере
- **Защита от неавторизованного доступа**

## 🤝 Вклад в проект

Мы приветствуем вклад в проект! Пожалуйста:

1. Форкните репозиторий
2. Создайте feature ветку (`git checkout -b feature/amazing-feature`)
3. Закоммитьте изменения (`git commit -m 'Add amazing feature'`)
4. Запушьте в ветку (`git push origin feature/amazing-feature`)
5. Откройте Pull Request

## 📄 Лицензия

Этот проект распространяется под MIT License - смотрите файл [LICENSE](LICENSE) для деталей.

## 👨‍💻 Автор

**Твое Имя**
- GitHub: [@your-username](https://github.com/your-username)
- Email: your.email@example.com
