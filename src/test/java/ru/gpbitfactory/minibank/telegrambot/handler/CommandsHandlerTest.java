package ru.gpbitfactory.minibank.telegrambot.handler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gpbitfactory.minibank.telegrambot.AbstractUpdateConsumerTest;

import static org.mockito.Mockito.verify;

class CommandsHandlerTest extends AbstractUpdateConsumerTest {

    @Test
    @DisplayName("Если команда не зарегистрирована, клиенту должен вернуться соответствущий ответ")
    void whenConsumeFakeCommand_thenShouldReturnInfoMessage() throws TelegramApiException {
        var updateMessage = buildUpdateMessage("/fake", true);
        updateConsumer.consume(updateMessage);

        var expectedSendMessage = buildExpectedSendMessage("Команда /fake не поддерживается");
        verify(telegramClient).execute(expectedSendMessage);
    }

    @Test
    @DisplayName("Если на вход получено простое сообщение, клиенту должен вернуться стандартный ответ")
    void whenConsumeNonCommandMessage_thenShouldReturnDefaultMessage() throws TelegramApiException {
        var updateMessage = buildUpdateMessage("Привет, друг", false);
        updateConsumer.consume(updateMessage);

        var expectedSendMessage = buildExpectedSendMessage("Я бы рад с тобой поболтать, но пока я этому ещё не научился :)");
        verify(telegramClient).execute(expectedSendMessage);
    }
}