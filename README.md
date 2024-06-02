# Telegram-бот приложения Мини-банк

[![Java Gradle Build & Test](https://github.com/gpb-it-factory/gladskoy-telegram-bot/actions/workflows/gradle-ci.yml/badge.svg)](https://github.com/gpb-it-factory/gladskoy-telegram-bot/actions/workflows/gradle-ci.yml)

Telegram-бот "Мини-банка" - это фронтенд-часть приложения разрабатываемого в рамках программы [GPB IT Factory Backend 2024](https://gpb.fut.ru/itfactory/backend).
Бот инициирует запросы пользователей в Middle-слой, где происходит их дальнейшая обработка.


## Содержание

1. [Локальный запуск](#локальный-запуск)
2. [Как использовать](#как-использовать)
3. [Архитектура системы](#архитектура-системы)
4. [Интеграции](#интеграции)


### Локальный запуск

1. Получить API-токен с помощью Telegram-бота [@BotFather](https://t.me/botfather) (подробно данный процесс описан в 
[документации](https://core.telegram.org/bots/tutorial#obtain-your-bot-token))
2. Клонировать репозиторий
    ```bash
    git clone git@github.com:gpb-it-factory/gladskoy-telegram-bot.git
    ```
3. Перейти в директорию с проектом
   ```bash
   cd gladskoy-telegram-bot
   ```
4. Запустить приложение (`telegram_bot_name` будет создано при получении API-токена)
    ```bash
    BOT_NAME={telegram_bot_name} \
    BOT_TOKEN={telegram_bot_token} \
    ./gradlew bootRun
    ```
5. Найти в Telegram бота по имени `@{telegram_bot_name}` (пример `@GpbITFactoryGladskoyTelegramBot`)


### Как использовать

- `/start` - точка входа в приложение, запускает бота
- `/ping` - общая информация и описание основных функций


### Архитектура системы

![](src/main/resources/project/architecture.png)

<details>

```plantuml
@startuml architecture
skinparam sequenceMessageAlign center
skinparam ParticipantPadding 20

participant Client
participant TelegramBot
participant MiddleService
participant BackendService

Client -> TelegramBot: HTTP request
activate TelegramBot

TelegramBot -> MiddleService: HTTP request
activate MiddleService

MiddleService -> MiddleService: Business logic

MiddleService -> BackendService: Request
activate BackendService

BackendService --> MiddleService: Response
deactivate BackendService

MiddleService --> TelegramBot: HTTP response
deactivate MiddleService

TelegramBot --> Client: HTTP response
deactivate TelegramBot
@enduml
```
</details>


### Интеграции

- [Middle Service](https://github.com/gpb-it-factory/gladskoy-middle-service)
- [Backend Service]() // TBD
