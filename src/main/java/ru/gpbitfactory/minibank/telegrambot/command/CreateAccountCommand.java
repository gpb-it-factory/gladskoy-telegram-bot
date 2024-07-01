package ru.gpbitfactory.minibank.telegrambot.command;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.gpbitfactory.minibank.middle.dto.AccountResponse;
import ru.gpbitfactory.minibank.telegrambot.service.CreateAccountService;
import ru.gpbitfactory.minibank.telegrambot.service.MiddleApiService;
import ru.gpbitfactory.minibank.telegrambot.util.SendMessageBuilder;

import java.util.function.Predicate;

@Slf4j
@Component
public class CreateAccountCommand extends BotCommand {

    private final Predicate<AccountResponse> accountForNewClient = account -> account.getType().equals(AccountResponse.TypeEnum.PROMO);
    private final CreateAccountService createAccountService;
    private final MiddleApiService middleApiService;

    public CreateAccountCommand(MiddleApiService middleApiService, CreateAccountService createAccountService) {
        super("createaccount", "Открытие счёта в Мини-банке");
        this.middleApiService = middleApiService;
        this.createAccountService = createAccountService;
    }

    @Override
    @SneakyThrows
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] strings) {
        long userId = user.getId();
        long chatId = chat.getId();
        log.info("Поступил запрос на открытие счёта для клиента {}", userId);
        var responseBuilder = SendMessageBuilder.of(chatId);

        var client = middleApiService.getClient(userId);
        if (client.isEmpty()) {
            log.warn("Клиент userId: {} ещё на зарегистрирован", userId);
            responseBuilder.text("Ты ещё не зарегистрирован! Чтобы зарегистрироваться нажми на /register.");
            telegramClient.execute(responseBuilder.build());
            return;
        }

        var availableAccounts = middleApiService.getAvailableAccounts();
        if (availableAccounts.isEmpty()) {
            log.warn("В Middle Service нет счетов, доступных для открытия, отправляем сообщение клиенту");
            responseBuilder.text("Сервис временно недоступен, повтори попытку позже");
            telegramClient.execute(responseBuilder.build());
            return;
        }

        var clientAccounts = middleApiService.getClientAccounts(userId);
        if (clientAccounts.isEmpty()) {
            log.info("У клиента нет открытых счетов, поэтому пробуем открыть ему один из доступных счетов");
            availableAccounts.stream()
                    .filter(accountForNewClient)
                    .findFirst()
                    .or(availableAccounts.stream()::findAny)
                    .ifPresent(createAccountService.sendConfirmationButtonsBlock(userId, chatId));
        } else {
            log.info("У клиента уже есть открытые счета, поэтому предлагаем ему открыть ещё один счёт");
            var accountsExceptForNewClient = availableAccounts.stream().filter(accountForNewClient.negate()).toList();
            createAccountService.sendAccountButtonsBlock(accountsExceptForNewClient, userId, chatId);
        }
    }
}
