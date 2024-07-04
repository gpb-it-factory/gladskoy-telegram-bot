package ru.gpbitfactory.minibank.telegrambot.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gpbitfactory.minibank.middle.dto.ClientResponse;
import ru.gpbitfactory.minibank.middle.dto.CreateClientRequestV2;
import ru.gpbitfactory.minibank.middle.dto.CreateClientResponse;
import ru.gpbitfactory.minibank.telegrambot.AbstractUpdateConsumerTest;
import ru.gpbitfactory.minibank.telegrambot.util.MockFeignException;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.when;

@TestPropertySource(properties = "spring.cache.type=none")
class RegisterCommandIntegrationTest extends AbstractUpdateConsumerTest {

    private static final String REGISTER_COMMAND = "/register";

    @Test
    @DisplayName("Успешная регистрация клиента")
    void whenMiddleServiceReturnSuccessfulResponses_thenClientRegistrationShouldBeSuccessful() throws TelegramApiException {
        configureMiddleApiClientMockWithGetClientErrorMessage();

        var sendMessage = consumeCommandAndCaptureSendMessage(REGISTER_COMMAND);

        assertThatSendMessageContainsSubsequence(sendMessage, "Поздравляю, ты успешно зарегистрирован!",
                "Чтобы воспользоваться услугами нашего Мини-банка, осталось только открыть счёт, для этого введи команду /createaccount."
        );
    }

    @Test
    @DisplayName("Клиент уже зарегистрирован ранее")
    void whenClientIsRegisteredAlready_thenBotShouldSendInformationMessage() throws TelegramApiException {
        configureMiddleApiClientMockWithGetClientSuccessResponse();

        var sendMessage = consumeCommandAndCaptureSendMessage(REGISTER_COMMAND);

        assertThatSendMessageIsEqualTo(sendMessage, "Ты уже был зарегистрирован ранее!");
    }

    @Test
    @DisplayName("При получении данных клиента из Middle Service произошла ошибка")
    void whenGetClientReturnedAnError_thenShouldTryToRegisterClient() throws TelegramApiException {
        configureMiddleApiClientMockWithGetClientErrorMessage();

        var sendMessage = consumeCommandAndCaptureSendMessage(REGISTER_COMMAND);

        assertThatSendMessageContainsSubsequence(sendMessage, "Поздравляю, ты успешно зарегистрирован!",
                "Чтобы воспользоваться услугами нашего Мини-банка, осталось только открыть счёт, для этого введи команду /createaccount."
        );
    }

    @Test
    @DisplayName("При регистрации клиента в Middle Service произошла ошибка")
    void whenRegisterClientReturnedAnError_thenBotShouldSendErrorMessage() throws TelegramApiException {
        configureMiddleApiClientMockWithErrorMessageResponses();

        var sendMessage = consumeCommandAndCaptureSendMessage(REGISTER_COMMAND);

        assertThatSendMessageIsEqualTo(sendMessage, "Сейчас регистрация недоступна, повтори попытку позже");
    }

    private void configureMiddleApiClientMockWithGetClientErrorMessage() {
        when(middleApiClient.getClient(DEFAULT_USER_ID)).thenThrow(new MockFeignException(422));

        var createClientRequest = new CreateClientRequestV2(DEFAULT_USER_ID, DEFAULT_USERNAME);
        var createClientRequestResponseEntity = ResponseEntity.ok(new CreateClientResponse());
        when(middleApiClient.createNewClient(createClientRequest)).thenReturn(createClientRequestResponseEntity);
    }

    private void configureMiddleApiClientMockWithGetClientSuccessResponse() {
        var cleanntResponseEntity = ResponseEntity.ok(new ClientResponse());
        when(middleApiClient.getClient(DEFAULT_USER_ID)).thenReturn(cleanntResponseEntity);
    }

    private void configureMiddleApiClientMockWithErrorMessageResponses() {
        when(middleApiClient.getClient(DEFAULT_USER_ID)).thenThrow(new MockFeignException(422));

        var createClientRequest = new CreateClientRequestV2(DEFAULT_USER_ID, DEFAULT_USERNAME);
        when(middleApiClient.createNewClient(createClientRequest)).thenThrow(new MockFeignException(422));
    }


    private void assertThatSendMessageIsEqualTo(SendMessage actual, String expected) {
        assertSoftly(softly -> {
            softly.assertThat(actual.getChatId()).isEqualTo(String.valueOf(DEFAULT_CHAT_ID));
            softly.assertThat(actual.getText())
                    .isEqualTo(expected);
        });
    }

    private void assertThatSendMessageContainsSubsequence(SendMessage actual, String... expected) {
        assertSoftly(softly -> {
            softly.assertThat(actual.getChatId()).isEqualTo(String.valueOf(DEFAULT_CHAT_ID));
            softly.assertThat(actual.getText()).containsSubsequence(expected);
        });
    }
}