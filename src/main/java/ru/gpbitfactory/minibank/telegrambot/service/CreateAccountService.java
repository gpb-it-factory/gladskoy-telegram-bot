package ru.gpbitfactory.minibank.telegrambot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.gpbitfactory.minibank.middle.dto.AccountResponse;
import ru.gpbitfactory.minibank.middle.dto.CreateClientAccountRequest;
import ru.gpbitfactory.minibank.telegrambot.command.KeyboardButtonCallbackRegistry;
import ru.gpbitfactory.minibank.telegrambot.util.EditMessageBuilder;
import ru.gpbitfactory.minibank.telegrambot.util.KeyboardButtonBuilder;
import ru.gpbitfactory.minibank.telegrambot.util.SendMessageBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateAccountService {

    private static final String CONFIRM_REGISTRATION_TG_MESSAGE = "Для подтверждения открытия счёта нажми на кнопку \"Далее\"";
    private static final String CONFIRM_REGISTRATION_LOG_MESSAGE = "Запрашиваем у клиента подтверждение открытия счёта '{}'";
    private static final int MAX_ACCOUNT_BUTTONS_IN_ROW = 2;

    private final MiddleApiService middleApiService;
    private final TelegramApiService telegramApiService;
    private final KeyboardButtonCallbackRegistry buttonCallbackRegistry;

    public Consumer<AccountResponse> sendConfirmationButtonsBlock(long userId, long chatId) {
        return account -> {
            log.debug(CONFIRM_REGISTRATION_LOG_MESSAGE, account.getName());
            var confirmButtonBlock = createConfirmationButtonsBlock(userId, account.getName(), account.getInitAmount());
            var responseBuilder = SendMessageBuilder.of(chatId, confirmButtonBlock);
            responseBuilder.text(CONFIRM_REGISTRATION_TG_MESSAGE);
            telegramApiService.sendResponse(responseBuilder.build());
        };
    }

    public void sendAccountButtonsBlock(List<AccountResponse> accounts, long userId, long chatId) {
        var keyboardButtons = new ArrayList<InlineKeyboardButton>();
        accounts.forEach(account -> {
            var accountButton = KeyboardButtonBuilder.buttonBuilder(account.getName(), generateCallbackId()).build();
            var accountButtonAction = createAccountButtonAction(userId, account.getName(), account.getInitAmount());
            buttonCallbackRegistry.register(accountButton.getCallbackData(), accountButtonAction);
            keyboardButtons.add(accountButton);
        });

        var keyboardRow = new InlineKeyboardRow();
        var buttonBlockBuilder = KeyboardButtonBuilder.keyboardBuilder();
        var buttonsAlreadyInRow = 0;

        // Создаём блок кнопок 2 x n
        for (var button : keyboardButtons) {
            if (buttonsAlreadyInRow < MAX_ACCOUNT_BUTTONS_IN_ROW) {
                keyboardRow.add(button);
                buttonsAlreadyInRow++;
            }

            if (buttonsAlreadyInRow == MAX_ACCOUNT_BUTTONS_IN_ROW || keyboardButtons.indexOf(button) == keyboardButtons.size() - 1) {
                buttonBlockBuilder.keyboardRow(new InlineKeyboardRow(keyboardRow));
                keyboardRow.clear();
                buttonsAlreadyInRow = 0;
            }
        }

        log.debug("Отправляем клиенту блок кнопок со счетами, доступными для открытия");
        var responseBuilder = SendMessageBuilder.of(chatId, buttonBlockBuilder.build())
                .text("Выбери один из доступных для открытия счетов:");
        telegramApiService.sendResponse(responseBuilder.build());
    }

    private Consumer<CallbackQuery> createAccountButtonAction(long userId, String accountName, Double accountInitAmount) {
        return callbackQuery -> {
            log.debug(CONFIRM_REGISTRATION_LOG_MESSAGE, accountName);
            var confirmButtonBlock = createConfirmationButtonsBlock(userId, accountName, accountInitAmount);
            var responseBuilder = EditMessageBuilder.of(callbackQuery, confirmButtonBlock);
            responseBuilder.text(CONFIRM_REGISTRATION_TG_MESSAGE);
            telegramApiService.sendResponse(callbackQuery.getId(), responseBuilder.build());
        };
    }

    private InlineKeyboardMarkup createConfirmationButtonsBlock(long userId, String accountName, Double accountInitAmount) {
        var nextButton = KeyboardButtonBuilder.buttonBuilder("ДАЛЕЕ", generateCallbackId()).build();
        var nextButtonAction = createNextButtonAction(userId, accountName, accountInitAmount);
        buttonCallbackRegistry.register(nextButton.getCallbackData(), nextButtonAction);

        var cancelButton = KeyboardButtonBuilder.buttonBuilder("ОТМЕНА", generateCallbackId()).build();
        var cancelButtonAction = createCancelButtonAction(userId);
        buttonCallbackRegistry.register(cancelButton.getCallbackData(), cancelButtonAction);

        return KeyboardButtonBuilder.keyboardBuilder(nextButton, cancelButton).build();
    }

    private Consumer<CallbackQuery> createNextButtonAction(long userId, String accountName, Double accountInitAmount) {
        return callbackQuery -> {
            log.info("Клиент подтвердил открытие счёта '{}'", accountName);
            var responseBuilder = EditMessageBuilder.of(callbackQuery);
            var createClientAccountRequest = CreateClientAccountRequest.builder()
                    .accountName(accountName)
                    .build();
            var accountIsRegistered = middleApiService.createClientAccount(userId, createClientAccountRequest);
            if (accountIsRegistered) {
                log.info("Клиенту {} успешно открыт счёт '{}'", userId, accountName);
                var messageBuilder = new StringBuilder(String.format("Счёт '%s' успешно открыт!%n%n", accountName));
                if (accountInitAmount != null) {
                    messageBuilder.append(String.format("Тебе зачислено %s бонусных рублей!%n", accountInitAmount));
                    messageBuilder.append("Деньгами можно воспользоваться прямо сейчас. ");
                }
                messageBuilder.append("Для того, чтобы ознакомиться со списком доступных операций, введи команду /help.");
                responseBuilder.text(messageBuilder.toString());
            } else {
                responseBuilder.text("Сервис временно недоступен, повтори попытку позже");
            }

            telegramApiService.sendResponse(callbackQuery.getId(), responseBuilder.build());
        };
    }

    private Consumer<CallbackQuery> createCancelButtonAction(long userId) {
        return callbackQuery -> {
            log.info("Клиент {} отменил операцию открытия счёта", userId);
            var responseBuilder = EditMessageBuilder.of(callbackQuery);
            responseBuilder.text("Операция отменена. Если это произошло случайно, введи команду /createaccount снова.");
            telegramApiService.sendResponse(callbackQuery.getId(), responseBuilder.build());
        };
    }

    private String generateCallbackId() {
        return UUID.randomUUID().toString();
    }
}
