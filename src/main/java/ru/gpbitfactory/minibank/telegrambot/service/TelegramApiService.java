package ru.gpbitfactory.minibank.telegrambot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramApiService {

    private static final String START_PROCESS_LOG_MESSAGE = "Поступил запрос на отправку в Telegram сообщения {}";
    private static final String END_PROCESS_LOG_MESSAGE = "Сообщение {} успешно отправлено";
    private static final String ERROR_LOG_MESSAGE = "Во время отправки сообщения клиенту возникла непредвиденная ситуация";

    private final TelegramClient telegramClient;

    /**
     * Оправка в телеграм стандартного сообщения.
     *
     * @param message сообщение для клиента.
     */
    public void sendResponse(BotApiMethod<?> message) {
        var telegramApiMethod = message.getMethod();
        log.info(START_PROCESS_LOG_MESSAGE, telegramApiMethod);
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_LOG_MESSAGE, e);
        }
        log.info(END_PROCESS_LOG_MESSAGE, telegramApiMethod);
    }

    /**
     * Отправка сообщения, в ответ на Callback-запрос (нажатие кнопки).
     *
     * @param callbackQueryId идентификатор Callback-запроса (кнопки).
     * @param message         сообщение для клиента.
     */
    public void sendResponse(String callbackQueryId, BotApiMethod<?> message) {
        var telegramApiMethod = message.getMethod();
        log.info(START_PROCESS_LOG_MESSAGE, telegramApiMethod);

        // На каждое сообщение CallbackQuery необходимо отправлять ответ AnswerCallbackQuery.
        // Иначе нажатая кнопка продолжит находиться в состоянии ожидания (в UI она будет "мигать").
        var callbackAnswer = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .build();
        try {
            telegramClient.execute(callbackAnswer);
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_LOG_MESSAGE, e);
        }
        log.info(END_PROCESS_LOG_MESSAGE, telegramApiMethod);
    }
}
