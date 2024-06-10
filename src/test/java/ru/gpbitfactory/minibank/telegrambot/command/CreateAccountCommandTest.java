package ru.gpbitfactory.minibank.telegrambot.command;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.InaccessibleMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gpbitfactory.minibank.telegrambot.AbstractUpdateConsumerTest;
import ru.gpbitfactory.minibank.telegrambot.util.KeyboardButtonBuilder;
import ru.gpbitfactory.minibank.telegrambot.util.SendMessageBuilder;

import static org.mockito.Mockito.verify;

class CreateAccountCommandTest extends AbstractUpdateConsumerTest {

    private static final String NEXT_BUTTON_CALLBACK_ID = "createAccountNext";
    private static final String CANCEL_BUTTON_CALLBACK_ID = "createAccountCancel";
    private static final long CHAT_ID = 111;
    private static final int MESSAGE_ID = 222;
    private static User user;
    private static Chat chat;

    @Autowired
    private CreateAccountCommand createAccountCommand;

    @BeforeAll
    static void beforeAll() {
        user = User.builder()
                .id(12345L)
                .firstName("Test Name")
                .isBot(false)
                .build();
        chat = Chat.builder()
                .id(CHAT_ID)
                .type("private")
                .build();
    }

    @Test
    @DisplayName("После первого вызова команды /createaccount должен быть возращён блок кнопок: ДАЛЕЕ / ОТМЕНА")
    void whenConsumeCreateAccountCommand_thenShouldReturnButtonBlock() throws TelegramApiException {
        var updateMessage = buildUpdateMessage("/createaccount", true);
        updateConsumer.consume(updateMessage);

        var expectedSendMessage = buildExpectedSendMessageWithButtons(
                "Для подтверждения открытия счёта необходимо нажать на кнопку \"Далее\"");
        verify(telegramClient).execute(expectedSendMessage);
    }

    @Test
    @DisplayName("После нажатия на кнопку 'ДАЛЕЕ' аккаунт должен быть успешно зарегистрирован")
    void whenConsumeCallbackWithConfirmation_thenShouldRegisterAccount() throws TelegramApiException {
        // инициализация коллбэков для кнопок
        createAccountCommand.execute(telegramClient, user, chat, null);

        var updateMessage = buildUpdateMessageWithCallbackQuery(NEXT_BUTTON_CALLBACK_ID);
        updateConsumer.consume(updateMessage);

        var expectedEditMessage = buildExpectedEditMessage("""
                Счёт успешно открыт! Тебе зачислено 5000 бонусных рублей!
                                    
                Деньгами можно воспользоваться прямо сейчас. Для того чтобы ознакомиться со списком доступных
                операций, введи команду /help.
                """);
        verify(telegramClient).execute(expectedEditMessage);
    }

    @Test
    @DisplayName("После нажатия на кнопку 'ОТМЕНА' операция должна быть отменена")
    void whenConsumeCallbackWithCancellation_thenShouldInterruptAccountRegistration() throws TelegramApiException {
        // инициализация коллбэков для кнопок
        createAccountCommand.execute(telegramClient, user, chat, null);

        var updateMessage = buildUpdateMessageWithCallbackQuery(CANCEL_BUTTON_CALLBACK_ID);
        updateConsumer.consume(updateMessage);

        var expectedEditMessage = buildExpectedEditMessage(
                "Операция отменена. Если это произошло случайно, введи команду /createaccount снова.");
        verify(telegramClient).execute(expectedEditMessage);
    }

    private Update buildUpdateMessageWithCallbackQuery(String callbackQueryId) {
        var callbackMessage = new InaccessibleMessage(chat, MESSAGE_ID, 0);
        var callbackQuery = new CallbackQuery("123", user, callbackMessage, null, callbackQueryId, null, null);
        var update = new Update();
        update.setCallbackQuery(callbackQuery);
        return update;
    }

    private SendMessage buildExpectedSendMessageWithButtons(String text) {
        var nextButton = KeyboardButtonBuilder.buttonBuilder("ДАЛЕЕ", NEXT_BUTTON_CALLBACK_ID).build();
        var cancelButton = KeyboardButtonBuilder.buttonBuilder("ОТМЕНА", CANCEL_BUTTON_CALLBACK_ID).build();
        var buttonBlock = KeyboardButtonBuilder.keyboardBuilder(nextButton, cancelButton).build();
        return SendMessageBuilder.of(CHAT_ID, buttonBlock).text(text).build();
    }

    private EditMessageText buildExpectedEditMessage(String text) {
        return EditMessageText.builder()
                .messageId(MESSAGE_ID)
                .chatId(String.valueOf(CHAT_ID))
                .text(text)
                .build();
    }
}