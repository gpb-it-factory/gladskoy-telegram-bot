package ru.gpbitfactory.minibank.telegrambot.command;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.gpbitfactory.minibank.middle.dto.CreateClientRequest;
import ru.gpbitfactory.minibank.middle.dto.CreateClientRequestV2;
import ru.gpbitfactory.minibank.telegrambot.restclient.MiddleServiceClientsApiClient;
import ru.gpbitfactory.minibank.telegrambot.util.MockFeignException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.ResponseEntity.status;

class RegisterCommandTest {

    private final MiddleServiceClientsApiClient middleService = mock(MiddleServiceClientsApiClient.class);
    private final TelegramClient telegramClient = mock(TelegramClient.class);

    private static User user;
    private static Chat chat;
    private static CreateClientRequestV2 createClientRequest;

    @BeforeAll
    static void beforeAll() {
        user = User.builder()
                .id(11111L)
                .userName("testusername")
                .firstName("Test Name")
                .isBot(false)
                .build();
        chat = Chat.builder()
                .id(22222L)
                .type("private")
                .build();
        createClientRequest = CreateClientRequestV2.builder()
                .telegramUserId(user.getId())
                .telegramUserName(user.getUserName())
                .build();
    }

    @Test
    void whenMiddleServiceReturnSuccessfulResponse_thenBotShouldReturnCongratulationsMessage() throws TelegramApiException {
        when(middleService.createNewClient(createClientRequest)).thenReturn(status(200).build());

        var registerCommand = new RegisterCommand(middleService);
        registerCommand.execute(telegramClient, user, chat, null);

        var expectedMessage = buildSendMessage("Вы успешно зарегистрированы!");
        verify(telegramClient).execute(expectedMessage);
    }

    @Test
    void whenMiddleServiceReturnUnexpectedErrorResponse_thenBotShouldReturnErrorMessage() throws TelegramApiException {
        when(middleService.createNewClient(createClientRequest)).thenThrow(new MockFeignException(422));

        var registerCommand = new RegisterCommand(middleService);
        registerCommand.execute(telegramClient, user, chat, null);

        var expectedMessage = buildSendMessage("Сервис временно недоступен, повторите попытку позже");
        verify(telegramClient).execute(expectedMessage);
    }

    @Test
    void whenUserAlreadyRegistered_thenBotShouldReturnErrorMessage() throws TelegramApiException {
        var errorMessage = String.format("Пользователь с telegramUserId: %s уже зарегистрирован", user.getId());
        when(middleService.createNewClient(createClientRequest)).thenThrow(new MockFeignException(422, errorMessage));

        var registerCommand = new RegisterCommand(middleService);
        registerCommand.execute(telegramClient, user, chat, null);

        var expectedMessage = buildSendMessage("Вы уже зарегистрированы ранее!");
        verify(telegramClient).execute(expectedMessage);
    }

    private SendMessage buildSendMessage(String text) {
        return SendMessage.builder()
                .chatId(chat.getId())
                .text(text)
                .build();
    }
}