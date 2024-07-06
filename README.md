# Telegram-бот приложения Мини-банк

[![Java Gradle Build & Test](https://github.com/gpb-it-factory/gladskoy-telegram-bot/actions/workflows/gradle-ci.yml/badge.svg)](https://github.com/gpb-it-factory/gladskoy-telegram-bot/actions/workflows/gradle-ci.yml)
[![Instructions coverage](.github/badges/instructions.svg)](https://github.com/gpb-it-factory/gladskoy-telegram-bot/actions/workflows/gradle-ci.yml)
[![Branches coverage](.github/badges/branches.svg)](https://github.com/gpb-it-factory/gladskoy-telegram-bot/actions/workflows/gradle-ci.yml)

Telegram-бот "Мини-банка" - это фронтенд-часть приложения разрабатываемого в рамках программы [GPB IT Factory Backend 2024](https://gpb.fut.ru/itfactory/backend).
Бот инициирует запросы пользователей в Middle-слой, где происходит их дальнейшая обработка.


## Содержание

1. [Локальный запуск](#локальный-запуск)
2. [Запуск в Docker](#запуск-в-Docker)
3. [Как использовать](#как-использовать)
4. [Демо-видео работы приложения](#демо-видео-работы-приложения)
5. [Архитектура системы](#архитектура-системы)
6. [Интеграции](#интеграции)


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


### Запуск в Docker

1. Создать общую сеть, если этого не было сделано ранее
    ```bash
    docker network create mini-bank-net
    ```
2. Выполнить скрипт, заменив значения переменных (<...>) корректными значениями
    ```bash
    docker run --network mini-bank-net \
      --name telegram-bot \
      --env MIDDLE_SERVICE_URL=<middle_hostname> \
      --env BOT_NAME=<bot_name> \
      --env BOT_TOKEN=<bot_token> \
      -dp 8088:8088 \
      saneci/mini-bank-telegram-bot:<tag>
    ```

_Актуальные теги тут: https://github.com/gpb-it-factory/gladskoy-telegram-bot/tags, указывать без префикса `v`_


### Как использовать

- `/start` - точка входа в приложение, запускает бота
- `/register` - первичное оформление пользователя
- `/createaccount` - открытие счёта в Мини-банке, в данный момент у клиента может быть только один счёт
- `/currentbalance` - получение текущего баланса открытого пользователем счёта
- `/transfer toUserName amount` - перевод средств между счетами пользователей


### Демо-видео работы приложения

https://github.com/gpb-it-factory/gladskoy-telegram-bot/assets/13638247/e92e2eaa-30e2-4643-9e6a-fd8ac298e3e2


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
