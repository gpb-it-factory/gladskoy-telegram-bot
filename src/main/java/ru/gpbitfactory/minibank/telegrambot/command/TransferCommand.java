package ru.gpbitfactory.minibank.telegrambot.command;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.gpbitfactory.minibank.middle.dto.CreateTransferRequest;
import ru.gpbitfactory.minibank.telegrambot.exception.AmountArgumentNotValidException;
import ru.gpbitfactory.minibank.telegrambot.service.MiddleApiService;
import ru.gpbitfactory.minibank.telegrambot.util.SendMessageBuilder;

import java.math.BigDecimal;

@Slf4j
@Component
public class TransferCommand extends BotCommand {

    private static final String CURRENT_ACCOUNT = "Акционный";

    private final MiddleApiService middleApiService;

    public TransferCommand(MiddleApiService middleApiService) {
        super("transfer", "Перевод средств между счетами пользователей");
        this.middleApiService = middleApiService;
    }

    @Override
    @SneakyThrows
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] arguments) {
        var responseBuilder = SendMessageBuilder.of(chat.getId());
        if (arguments.length != 2) {
            var messageBuilder = new StringBuilder();
            messageBuilder.append("Ты указал неверные реквизиты для перевода средств.\n\n");
            messageBuilder.append("Для успешного перевода введи после команды <code>/transfer</code> через пробел: ");
            messageBuilder.append("<code>username</code> пользователя, которому будет осуществлён перевод и ");
            messageBuilder.append("<code>сумму перевода</code>.\n\n");
            messageBuilder.append("Пример: <code>/transfer myAwesomeFriend 1000</code>\n\n");
            messageBuilder.append("<em>Текущий баланс можно узнать с помощью команды /currentbalance</em>");
            responseBuilder.text(messageBuilder.toString());
            telegramClient.execute(responseBuilder.build());
            return;
        }

        var fromUser = user.getUserName();
        var toUser = arguments[0];

        BigDecimal amount;
        try {
            amount = toBigDecimal(arguments[1]);
        } catch (AmountArgumentNotValidException e) {
            log.error("{}: {}", e.getMessage(), arguments[1]);
            responseBuilder.text(e.getMessage());
            telegramClient.execute(responseBuilder.build());
            return;
        }

        log.info("Поступил запрос на перевод средств от клиента {} клиенту {}", fromUser, toUser);

        var userId = user.getId();
        var client = middleApiService.getClient(userId);
        if (client.isEmpty()) {
            log.warn("Клиент userId: {} ещё не зарегистрирован", userId);
            responseBuilder.text("Ты ещё не зарегистрирован! Чтобы зарегистрироваться нажми на /register.");
            telegramClient.execute(responseBuilder.build());
            return;
        }

        var currentAccount = middleApiService.getClientAccounts(userId).stream()
                .filter(a -> a.getName().equalsIgnoreCase(CURRENT_ACCOUNT))
                .findFirst();

        if (currentAccount.isEmpty()) {
            var messageBuilder = new StringBuilder();
            messageBuilder.append("У тебя пока ещё нет текущего (лицевого) счёта, необходимого для переводов. ");
            messageBuilder.append("Чтобы открыть счёт нажми на /createaccount.");
            responseBuilder.text(messageBuilder.toString());
            telegramClient.execute(responseBuilder.build());
        } else {
            var clientAccount = currentAccount.get();
            if (clientAccount.getAmount().compareTo(amount) < 0) {
                var messageBuilder = new StringBuilder();
                messageBuilder.append("На счету '").append(CURRENT_ACCOUNT).append("' недостаточно средств для перевода. ");
                messageBuilder.append("Текущий баланс: ").append(clientAccount.getAmount()).append(" руб.");
                responseBuilder.text(messageBuilder.toString());
                telegramClient.execute(responseBuilder.build());
                return;
            }

            var request = new CreateTransferRequest(fromUser, toUser, amount);
            var response = middleApiService.createMoneyTransfer(request);
            if (response != null) {
                responseBuilder.text(response.getMessage());
            } else {
                responseBuilder.text("Сервис временно недоступен, повтори попытку позже");
            }

            telegramClient.execute(responseBuilder.build());
        }
    }

    private BigDecimal toBigDecimal(String amount) {
        try {
            var longValue = Long.parseLong(amount);
            return BigDecimal.valueOf(longValue);
        } catch (NumberFormatException e) {
            throw new AmountArgumentNotValidException("Аргумент amount не является числом");
        }
    }
}
