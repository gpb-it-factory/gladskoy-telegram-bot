package ru.gpbitfactory.minibank.telegrambot.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gpbitfactory.minibank.telegrambot.AbstractUpdateConsumerTest;

import static org.mockito.Mockito.verify;

class StartCommandTest extends AbstractUpdateConsumerTest {

    @Test
    @DisplayName("После обработки команды /start клиенту должно вернуться приветствие от бота")
    void whenConsumeStartCommand_thenShouldReturnCongratulationMessage() throws TelegramApiException {
        var updateMessage = buildUpdateMessage("/start", true);
        updateConsumer.consume(updateMessage);

        var expectedSendMessage = buildExpectedSendMessage("""
                Привет, Test User!
                Добро пожаловать в Мини-Банк.\n
                """);
        verify(telegramClient).execute(expectedSendMessage);
    }
}
