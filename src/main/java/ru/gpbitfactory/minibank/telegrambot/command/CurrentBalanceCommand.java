package ru.gpbitfactory.minibank.telegrambot.command;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.gpbitfactory.minibank.telegrambot.service.MiddleApiService;
import ru.gpbitfactory.minibank.telegrambot.util.SendMessageBuilder;

@Slf4j
@Component
public class CurrentBalanceCommand extends BotCommand {

    private final MiddleApiService middleApiService;

    public CurrentBalanceCommand(MiddleApiService middleApiService) {
        super("currentbalance", "Получение текущего баланса");
        this.middleApiService = middleApiService;
    }

    @Override
    @SneakyThrows
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] arguments) {
        var userId = user.getId();
        log.info("Поступил запрос на получение баланса клиента userId: {}", userId);
        var responseBuilder = SendMessageBuilder.of(chat.getId());

        var client = middleApiService.getClient(userId);
        if (client.isEmpty()) {
            log.warn("Клиент userId: {} ещё на зарегистрирован", userId);
            responseBuilder.text("Ты ещё не зарегистрирован! Чтобы зарегистрироваться нажми на /register.");
            telegramClient.execute(responseBuilder.build());
            return;
        }

        var clientAccounts = middleApiService.getClientAccounts(userId);
        if (clientAccounts.isEmpty()) {
            responseBuilder.text("Ты пока ещё не открыл не одного счёта. Чтобы открыть счёт нажми на /createaccount.");
            telegramClient.execute(responseBuilder.build());
            return;
        }

        var messageBuilder = new StringBuilder("<b>Список открытых счетов</b>\n\n");
        clientAccounts.forEach(account -> messageBuilder
                .append(" - ").append(account.getName()).append(", текущий баланс: ")
                .append(account.getAmount()).append(" руб.\n")
        );

        responseBuilder.text(messageBuilder.toString());
        telegramClient.execute(responseBuilder.build());
    }
}
