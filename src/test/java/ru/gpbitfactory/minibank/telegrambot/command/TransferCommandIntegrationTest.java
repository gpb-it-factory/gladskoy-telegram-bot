package ru.gpbitfactory.minibank.telegrambot.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gpbitfactory.minibank.middle.dto.ClientAccount;
import ru.gpbitfactory.minibank.middle.dto.ClientResponse;
import ru.gpbitfactory.minibank.middle.dto.CreateTransferRequest;
import ru.gpbitfactory.minibank.middle.dto.CreateTransferResponse;
import ru.gpbitfactory.minibank.telegrambot.AbstractUpdateConsumerTest;
import ru.gpbitfactory.minibank.telegrambot.util.MockFeignException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;
import java.util.stream.Stream;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.when;

@TestPropertySource(properties = "spring.cache.type=none")
class TransferCommandIntegrationTest extends AbstractUpdateConsumerTest {

    private static final String TRANSFER_COMMAND = "/transfer";
    private static final String TRANSFER_TO_USERNAME = "toUsername";
    private static final String CURRENT_ACCOUNT = "Акционный";
    private static final BigDecimal CURRENT_ACCOUNT_BALANCE = BigDecimal.valueOf(5000);

    @Test
    @DisplayName("Успешный перевод")
    void whenCommandArgumentsAreValid_thenShouldSentToTelegramUserSuccessMessage() throws TelegramApiException {
        var transferAmount = BigDecimal.valueOf(1000.0);
        configureMiddleApiClientMockWithSuccessResponseBody("Перевод успешно осуществлён", transferAmount);

        var command = buildCommand(TRANSFER_COMMAND, TRANSFER_TO_USERNAME, "1000");
        var sendMessage = consumeCommandAndCaptureSendMessage(command);

        assertThatSendMessageIsEqualTo(sendMessage, "Перевод успешно осуществлён");
    }

    private static Stream<Arguments> transferCommandArguments() {
        return Stream.of(
                Arguments.of(buildCommand(TRANSFER_COMMAND)),
                Arguments.of(buildCommand(TRANSFER_COMMAND, TRANSFER_TO_USERNAME)),
                Arguments.of(buildCommand(TRANSFER_COMMAND, TRANSFER_TO_USERNAME, "123", "overflow"))
        );
    }

    @ParameterizedTest
    @MethodSource("transferCommandArguments")
    @DisplayName("В команде передано некорректное количество аргументов")
    void whenCommandArgumentsAreNotEqualTo2_thenShouldSendToUserHowToProceed(String command) throws TelegramApiException {
        var sendMessage = consumeCommandAndCaptureSendMessage(command);

        assertThatSendMessageContainsSubsequence(sendMessage,
                "Ты указал неверные реквизиты для перевода средств.\n\n",
                "Для успешного перевода введи после команды <code>/transfer</code> через пробел: ",
                "<code>username</code> пользователя, которому будет осуществлён перевод и ",
                "<code>сумму перевода</code>.\n\n",
                "Пример: <code>/transfer myAwesomeFriend 1000</code>\n\n",
                "<em>Текущий баланс можно узнать с помощью команды /currentbalance</em>"
        );
    }

    @Test
    @DisplayName("Вместо суммы перевода передано некорректное значение")
    void whenTransferAmountIsNotValid_thenShouldSendToUserErrorMessage() throws TelegramApiException {
        var command = buildCommand(TRANSFER_COMMAND, TRANSFER_TO_USERNAME, "123fake");
        var sendMessage = consumeCommandAndCaptureSendMessage(command);

        assertThatSendMessageIsEqualTo(sendMessage, "Значение суммы перевода не является числом");
    }

    @Test
    @DisplayName("Клиент ещё не зарегистрирован")
    void whenClientIsNotRegistered_thenShouldSendToUserOfferToRegister() throws TelegramApiException {
        configureMiddleApiClientMockWithGetClientError();

        var command = buildCommand(TRANSFER_COMMAND, TRANSFER_TO_USERNAME, "123");
        var sendMessage = consumeCommandAndCaptureSendMessage(command);

        assertThatSendMessageIsEqualTo(sendMessage, "Ты ещё не зарегистрирован! Чтобы зарегистрироваться нажми на /register.");
    }

    @Test
    @DisplayName("У клиента нет лицевого счёта")
    void whenClientDoesntHaveRegisteredAccount_thenShouldSendToUserOfferToRegisterAccount() throws TelegramApiException {
        configureMiddleApiClientMockWithEmptyClientAccounts();

        var command = buildCommand(TRANSFER_COMMAND, TRANSFER_TO_USERNAME, "123");
        var sendMessage = consumeCommandAndCaptureSendMessage(command);

        assertThatSendMessageContainsSubsequence(sendMessage,
                "У тебя пока ещё нет текущего (лицевого) счёта, необходимого для переводов. ",
                "Чтобы открыть счёт нажми на /createaccount."
        );
    }

    @Test
    @DisplayName("Сумма перевода превышает доступный баланс на лицевом счёте")
    void whenTransferAmountIsGreaterThanCurrentBalance_thenShouldSendToUserErrorMessage() throws TelegramApiException {
        var transferAmount = BigDecimal.valueOf(5001);
        configureMiddleApiClientMockWithSuccessResponseBody("Успех", transferAmount);

        var command = buildCommand(TRANSFER_COMMAND, TRANSFER_TO_USERNAME, "5001");
        var sendMessage = consumeCommandAndCaptureSendMessage(command);

        assertThatSendMessageIsEqualTo(sendMessage,
                "На счету 'Акционный' недостаточно средств для перевода. Текущий баланс: 5000 руб."
        );
    }

    @Test
    @DisplayName("Получатель перевода не зарегистрирован")
    void whenRecipientOfTheTransferIsNotRegistered_thenShouldSendToUserErrorMessage() throws TelegramApiException {
        var transferAmount = BigDecimal.valueOf(1000.0);
        configureMiddleApiClientMockWithCreateTransferError(transferAmount);

        var command = buildCommand(TRANSFER_COMMAND, TRANSFER_TO_USERNAME, "1000");
        var sendMessage = consumeCommandAndCaptureSendMessage(command);

        assertThatSendMessageIsEqualTo(sendMessage, "Клиент toUsername не зарегистрирован");
    }

    private void configureMiddleApiClientMockWithSuccessResponseBody(String responseMessage, BigDecimal amount) {
        var clientAccount = new ClientAccount(CURRENT_ACCOUNT, CURRENT_ACCOUNT_BALANCE);
        var clientResponse = new ClientResponse().addAccountsItem(clientAccount);
        var cleanntResponseEntity = ResponseEntity.ok(clientResponse);
        when(middleApiClient.getClient(DEFAULT_USER_ID)).thenReturn(cleanntResponseEntity);

        var createTransferRequest = CreateTransferRequest.builder()
                .from(DEFAULT_USERNAME)
                .to(TRANSFER_TO_USERNAME)
                .amount(amount)
                .build();
        var createTransferResponse = CreateTransferResponse.builder().message(responseMessage).build();
        var transferResponseEntity = ResponseEntity.ok(createTransferResponse);
        when(middleApiClient.createTransfer(createTransferRequest)).thenReturn(transferResponseEntity);
    }

    private void configureMiddleApiClientMockWithGetClientError() {
        when(middleApiClient.getClient(DEFAULT_USER_ID)).thenThrow(new MockFeignException(422));
    }

    private void configureMiddleApiClientMockWithEmptyClientAccounts() {
        var cleanntResponseEntity = ResponseEntity.ok(new ClientResponse());
        when(middleApiClient.getClient(DEFAULT_USER_ID)).thenReturn(cleanntResponseEntity);
    }

    private void configureMiddleApiClientMockWithCreateTransferError(BigDecimal amount) {
        var clientAccount = new ClientAccount(CURRENT_ACCOUNT, CURRENT_ACCOUNT_BALANCE);
        var clientResponse = new ClientResponse().addAccountsItem(clientAccount);
        var cleanntResponseEntity = ResponseEntity.ok(clientResponse);
        when(middleApiClient.getClient(DEFAULT_USER_ID)).thenReturn(cleanntResponseEntity);

        var createTransferRequest = CreateTransferRequest.builder()
                .from(DEFAULT_USERNAME)
                .to(TRANSFER_TO_USERNAME)
                .amount(amount)
                .build();
        var feignExceptionResponseBody = "{\"message\": \"Клиент " + TRANSFER_TO_USERNAME + " не зарегистрирован\" }";
        var feignException = new MockFeignException(422, feignExceptionResponseBody.getBytes(StandardCharsets.UTF_8));
        when(middleApiClient.createTransfer(createTransferRequest)).thenThrow(feignException);
    }

    private static String buildCommand(String... arguments) {
        var stringJoiner = new StringJoiner(" ");
        for (var argument : arguments) {
            stringJoiner.add(argument);
        }
        return stringJoiner.toString();
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