package ru.gpbitfactory.minibank.telegrambot.command;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.InaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.gpbitfactory.minibank.middle.dto.AccountResponse;
import ru.gpbitfactory.minibank.middle.dto.ClientAccount;
import ru.gpbitfactory.minibank.middle.dto.ClientResponse;
import ru.gpbitfactory.minibank.middle.dto.CreateClientAccountRequest;
import ru.gpbitfactory.minibank.middle.dto.CreateClientAccountResponse;
import ru.gpbitfactory.minibank.telegrambot.AbstractUpdateConsumerTest;
import ru.gpbitfactory.minibank.telegrambot.service.CreateAccountService;
import ru.gpbitfactory.minibank.telegrambot.util.MockFeignException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateAccountCommandIntegrationTest extends AbstractUpdateConsumerTest {

    private static final String CREATEACCOUNT_COMMAND = "/createaccount";

    private static User user;
    private static Chat chat;

    @Autowired
    private CreateAccountService createAccountService;

    @BeforeAll
    static void beforeAll() {
        user = User.builder()
                .id(DEFAULT_USER_ID)
                .firstName("Test Name")
                .isBot(false)
                .build();
        chat = Chat.builder()
                .id(DEFAULT_CHAT_ID)
                .type("private")
                .build();
    }

    static Stream<Arguments> availableAccountArguments() {
        return Stream.of(
                Arguments.of(new AccountResponse("Акционный", AccountResponse.TypeEnum.PROMO)),
                Arguments.of(new AccountResponse("Депозитный", AccountResponse.TypeEnum.COMMON)),
                Arguments.of(new AccountResponse("Накопительный", AccountResponse.TypeEnum.DEFAULT))
        );
    }

    @ParameterizedTest
    @MethodSource("availableAccountArguments")
    @DisplayName("Клиент зарегистрирован и у него нет открытых счетов")
    void whenClientDoesntHaveAccount_thenShouldSendConfirmationButtonBlock(AccountResponse account) throws TelegramApiException {
        configureMiddleApiClientMockWithEmptyResponseBodyOfGetClient(List.of(account));

        var sendMessage = consumeCommandAndCaptureSendMessage(CREATEACCOUNT_COMMAND);

        assertThatSendMessageContainsConfirmationButtonBlock(sendMessage);
    }

    @Test
    @DisplayName("Клиент зарегистрирован и у него уже есть открытые счета")
    void whenClientHasAccount_thenShouldReturnAvailableAccountsButtonBlockExceptPromo() throws TelegramApiException {
        configureMiddleApiClientMockWithResponseBodyContainsAccounts();

        var sendMessage = consumeCommandAndCaptureSendMessage(CREATEACCOUNT_COMMAND);

        assertThatSendMessageContainsListOfAvailableAccounts(sendMessage);
    }

    @Test
    @DisplayName("Клиент не зарегистрирован")
    void whenClientIsNotRegistered_thenShouldReturnErrorResponse() throws TelegramApiException {
        configureMiddleApiClientMockWithGetClientThrowingFeignException();

        var sendMessage = consumeCommandAndCaptureSendMessage(CREATEACCOUNT_COMMAND);

        assertThat(sendMessage.getText()).isEqualTo(
                "Ты ещё не зарегистрирован! Чтобы зарегистрироваться нажми на /register."
        );
    }

    @Test
    @DisplayName("В Middle Service нет доступных для открытия счетов")
    void whenThereAreNoAvailableAccounts_thenShouldReturnErrorResponse() throws TelegramApiException {
        configureMiddleApiClientMockWithEmptyResponseBodyOfGetClient(List.of());

        var sendMessage = consumeCommandAndCaptureSendMessage(CREATEACCOUNT_COMMAND);

        assertThat(sendMessage.getText()).isEqualTo("Сервис временно недоступен, повтори попытку позже");
    }

    @Test
    @DisplayName("Во время регистрации счёта в Middle Service возникла ошибка")
    void whenErrorOccurredDuringAccountRegistration_thenShouldReturnErrorResponse() throws TelegramApiException {
        configureMiddleApiClientMockWithCreateAccountThrowingFeignException();

        clickOnTheNextButton();
        var editMessage = consumeCallbackQueryAndCaptureEditMessage();

        assertThat(editMessage.getText()).isEqualTo("Сервис временно недоступен, повтори попытку позже");
    }

    @Test
    @DisplayName("Открытие счёта для клиента, у которого нет не одного открытого счёта")
    void whenClientDoesntHaveRegisteredAccount_thenShouldRegisterPromoAccount() throws TelegramApiException {
        configureMiddleApiClientMockWithSuccessResponseBodyOfCreateAccount();

        consumeCommand(CREATEACCOUNT_COMMAND);
        var editMessage = consumeCallbackQueryAndCaptureEditMessage();

        assertThat(editMessage.getText()).containsSubsequence(
                "Счёт 'Акционный' успешно открыт!", "Тебе зачислено 1000.0 бонусных рублей!",
                "Деньгами можно воспользоваться прямо сейчас. Для того, чтобы ознакомиться со списком доступных операций, введи команду /help."
        );
    }

    private void configureMiddleApiClientMockWithSuccessResponseBodyOfCreateAccount() {
        var promoAccount = new AccountResponse("Акционный", AccountResponse.TypeEnum.PROMO);
        promoAccount.setInitAmount(1000D);
        var depositAccount = new AccountResponse("Депозитный", AccountResponse.TypeEnum.COMMON);
        configureMiddleApiClientMockWithEmptyResponseBodyOfGetClient(List.of(promoAccount, depositAccount));

        var createClientAccountResponse = new ResponseEntity<>(new CreateClientAccountResponse(), HttpStatus.CREATED);
        when(middleApiClient.createClientAccount(DEFAULT_USER_ID, new CreateClientAccountRequest("Акционный")))
                .thenReturn(createClientAccountResponse);
    }

    private void configureMiddleApiClientMockWithEmptyResponseBodyOfGetClient(List<AccountResponse> accountResponse) {
        when(middleApiClient.getClient(DEFAULT_USER_ID)).thenReturn(new ResponseEntity<>(new ClientResponse(), HttpStatus.OK));
        when(middleApiClient.getAvailableAccounts()).thenReturn(new ResponseEntity<>(accountResponse, HttpStatus.OK));
    }

    private void configureMiddleApiClientMockWithResponseBodyContainsAccounts() {
        var clientResponse = new ClientResponse();
        clientResponse.addAccountsItem(new ClientAccount("Накопительный", BigDecimal.valueOf(12345.67)));
        when(middleApiClient.getClient(DEFAULT_USER_ID)).thenReturn(new ResponseEntity<>(clientResponse, HttpStatus.OK));

        var promoAccount = new AccountResponse("Акционный", AccountResponse.TypeEnum.PROMO);
        var depositAccount = new AccountResponse("Депозитный", AccountResponse.TypeEnum.COMMON);
        when(middleApiClient.getAvailableAccounts()).thenReturn(new ResponseEntity<>(List.of(promoAccount, depositAccount), HttpStatus.OK));
    }

    private void configureMiddleApiClientMockWithGetClientThrowingFeignException() {
        var depositAccount = new AccountResponse("Депозитный", AccountResponse.TypeEnum.COMMON);
        when(middleApiClient.getAvailableAccounts()).thenReturn(new ResponseEntity<>(List.of(depositAccount), HttpStatus.OK));
        when(middleApiClient.getClient(DEFAULT_USER_ID)).thenThrow(new MockFeignException(404));
    }

    private void configureMiddleApiClientMockWithCreateAccountThrowingFeignException() {
        when(middleApiClient.createClientAccount(DEFAULT_USER_ID, new CreateClientAccountRequest("Акционный")))
                .thenThrow(new MockFeignException(500));
    }

    private void clickOnTheNextButton() {
        var promoAccount = new AccountResponse("Акционный", AccountResponse.TypeEnum.PROMO);
        var accountResponseConsumer = createAccountService.sendConfirmationButtonsBlock(DEFAULT_USER_ID, DEFAULT_CHAT_ID);
        accountResponseConsumer.accept(promoAccount);
    }

    private EditMessageText consumeCallbackQueryAndCaptureEditMessage() throws TelegramApiException {
        var updateCallbackQuery = buildUpdateMessageWithCallbackQuery(getNextButtonCallbackId());
        updateConsumer.consume(updateCallbackQuery);

        var editMessageArguments = ArgumentCaptor.forClass(EditMessageText.class);
        verify(telegramClient).execute(editMessageArguments.capture());

        return editMessageArguments.getValue();
    }

    private void assertThatSendMessageContainsConfirmationButtonBlock(SendMessage actual) {
        var inlineKeyboardRow = getInlineKeyboard(actual);
        assertSoftly(softly -> {
            softly.assertThat(actual.getChatId()).isEqualTo(String.valueOf(DEFAULT_CHAT_ID));
            softly.assertThat(actual.getText()).isEqualTo("Для подтверждения открытия счёта нажми на кнопку \"Далее\"");

            var nextButton = inlineKeyboardRow.get(0);
            softly.assertThat(nextButton.getText()).isEqualTo("ДАЛЕЕ");
            softly.assertThat(nextButton.getCallbackData()).isNotBlank();

            var cancelButton = inlineKeyboardRow.get(1);
            softly.assertThat(cancelButton.getText()).isEqualTo("ОТМЕНА");
            softly.assertThat(cancelButton.getCallbackData()).isNotBlank();
        });
    }

    private void assertThatSendMessageContainsListOfAvailableAccounts(SendMessage actual) {
        var inlineKeyboardRow = getInlineKeyboard(actual);
        assertSoftly(softly -> {
            softly.assertThat(actual.getChatId()).isEqualTo(String.valueOf(DEFAULT_CHAT_ID));
            softly.assertThat(actual.getText()).isEqualTo("Выбери один из доступных для открытия счетов:");
            softly.assertThat(inlineKeyboardRow).size().isEqualTo(1);
            softly.assertThat(inlineKeyboardRow.get(0).getText()).isEqualTo("Депозитный");
            softly.assertThat(inlineKeyboardRow.get(0).getCallbackData()).isNotBlank();
        });
    }


    private InlineKeyboardRow getInlineKeyboard(SendMessage sendMessage) {
        var inlineKeyboardMarkup = (InlineKeyboardMarkup) sendMessage.getReplyMarkup();
        return inlineKeyboardMarkup.getKeyboard().get(0);
    }

    private String getNextButtonCallbackId() throws TelegramApiException {
        var sendMessageArguments = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(sendMessageArguments.capture());

        var sendMessage = sendMessageArguments.getValue();
        var inlineKeyboardMarkup = (InlineKeyboardMarkup) sendMessage.getReplyMarkup();
        var inlineKeyboardRow = inlineKeyboardMarkup.getKeyboard().get(0);

        return inlineKeyboardRow.get(0).getCallbackData();
    }

    private Update buildUpdateMessageWithCallbackQuery(String callbackQueryId) {
        var callbackMessage = new InaccessibleMessage(chat, DEFAULT_MESSAGE_ID, 0);
        var callbackQuery = new CallbackQuery("123", user, callbackMessage, null, callbackQueryId, null, null);
        var update = new Update();
        update.setCallbackQuery(callbackQuery);
        return update;
    }
}