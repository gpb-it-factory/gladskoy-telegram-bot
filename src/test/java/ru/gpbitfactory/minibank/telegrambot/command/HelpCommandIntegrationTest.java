package ru.gpbitfactory.minibank.telegrambot.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gpbitfactory.minibank.telegrambot.AbstractUpdateConsumerTest;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@SpringBootTest
class HelpCommandIntegrationTest extends AbstractUpdateConsumerTest {

    private static final String HELP_COMMAND = "/help";

    @Test
    @DisplayName("Вывод справочной информации о доступных командах")
    void whenConsumeHelpCommand_thenShouldReturnListOfCommandWithItsDescription() throws TelegramApiException {
        var sendMessage = consumeCommandAndCaptureSendMessage(HELP_COMMAND);

        assertThatSendMessageContainsSubsequence(sendMessage,
                "<b>Бот приложения Мини-банк</b>", "Список команд:",
                "/createaccount - Открытие счёта в Мини-банке", "/currentbalance - Получение текущего баланса",
                "/register - Регистрация нового клиента", "/start - Начало работы с ботом"
        );
    }

    private void assertThatSendMessageContainsSubsequence(SendMessage actual, String... expected) {
        assertSoftly(softly -> {
            softly.assertThat(actual.getChatId()).isEqualTo(String.valueOf(DEFAULT_CHAT_ID));
            softly.assertThat(actual.getText()).containsSubsequence(expected);
        });
    }
}