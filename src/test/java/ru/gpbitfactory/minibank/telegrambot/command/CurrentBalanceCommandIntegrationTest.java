package ru.gpbitfactory.minibank.telegrambot.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gpbitfactory.minibank.middle.dto.ClientAccount;
import ru.gpbitfactory.minibank.middle.dto.ClientResponse;
import ru.gpbitfactory.minibank.telegrambot.AbstractUpdateConsumerTest;
import ru.gpbitfactory.minibank.telegrambot.util.MockFeignException;

import java.math.BigDecimal;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.when;

@TestPropertySource(properties = "spring.cache.type=none")
class CurrentBalanceCommandIntegrationTest extends AbstractUpdateConsumerTest {

    private static final String CURRENTBALANCE_COMMAND = "/currentbalance";

    @Test
    @DisplayName("Клиент зарегистрирован и у него открыт счёт Акционный")
    void whenClientIsRegisteredAndHasRegisteredAccount_thenShouldReturnCurrentAccountBalance() throws TelegramApiException {
        configureMiddleApiClientMockWithResponseBodyContainsAccounts();

        var sendMessage = consumeCommandAndCaptureSendMessage(CURRENTBALANCE_COMMAND);

        assertThatSendMessageContainsSubsequence(sendMessage, "<b>Список открытых счетов</b>",
                " - Акционный, текущий баланс: 10 руб."
        );
    }

    @Test
    @DisplayName("Клиент ещё не зарегистрирован")
    void whenClientIsNotRegisteredYet_thenShouldSendOfferToRegister() throws TelegramApiException {
        configureMiddleApiClientMockThrowingFeignException();

        var sendMessage = consumeCommandAndCaptureSendMessage(CURRENTBALANCE_COMMAND);

        assertThatSendMessageIsEqualTo(sendMessage,
                "Ты ещё не зарегистрирован! Чтобы зарегистрироваться нажми на /register."
        );
    }

    @Test
    @DisplayName("У клиента нет открытых счетов")
    void whenClientDoesntHaveRegisteredAccount_thenShouldSendOfferToRegisterAccount() throws TelegramApiException {
        configureMiddleApiClientMockWithEmptyResponseBody();

        var sendMessage = consumeCommandAndCaptureSendMessage(CURRENTBALANCE_COMMAND);

        assertThatSendMessageIsEqualTo(sendMessage,
                "Ты пока ещё не открыл не одного счёта. Чтобы открыть счёт нажми на /createaccount."
        );
    }

    private void configureMiddleApiClientMockWithResponseBodyContainsAccounts() {
        var clientAccount = new ClientAccount("Акционный", BigDecimal.TEN);
        var clientResponse = new ClientResponse().addAccountsItem(clientAccount);
        when(middleApiClient.getClient(DEFAULT_USER_ID)).thenReturn(new ResponseEntity<>(clientResponse, HttpStatus.OK));
    }

    private void configureMiddleApiClientMockWithEmptyResponseBody() {
        var clientResponse = new ResponseEntity<>(new ClientResponse(), HttpStatus.OK);
        when(middleApiClient.getClient(DEFAULT_USER_ID)).thenReturn(clientResponse);
    }

    private void configureMiddleApiClientMockThrowingFeignException() {
        when(middleApiClient.getClient(DEFAULT_USER_ID)).thenThrow(new MockFeignException(422));
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