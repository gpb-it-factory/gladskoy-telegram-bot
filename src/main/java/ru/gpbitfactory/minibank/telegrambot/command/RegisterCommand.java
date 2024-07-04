package ru.gpbitfactory.minibank.telegrambot.command;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.gpbitfactory.minibank.middle.dto.CreateClientRequestV2;
import ru.gpbitfactory.minibank.telegrambot.service.MiddleApiService;
import ru.gpbitfactory.minibank.telegrambot.util.SendMessageBuilder;

@Slf4j
@Component
public class RegisterCommand extends BotCommand {

    private final MiddleApiService middleApiService;

    public RegisterCommand(MiddleApiService middleApiService) {
        super("register", "Регистрация нового клиента");
        this.middleApiService = middleApiService;
    }

    @Override
    @SneakyThrows
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] strings) {
        var userId = user.getId();
        log.info("Поступил запрос на регистрацию клиента userId: {}", userId);
        var responseBuilder = SendMessageBuilder.of(chat.getId());

        var client = middleApiService.getClient(userId);
        if (client.isPresent()) {
            log.info("Клиент userId: {} уже зарегистрирован", userId);
            responseBuilder.text("Ты уже был зарегистрирован ранее!");
            telegramClient.execute(responseBuilder.build());
            return;
        }

        var createClientRequest = new CreateClientRequestV2(userId, user.getUserName());
        var clientIsRegistered = middleApiService.createNewClient(createClientRequest);
        if (clientIsRegistered) {
            log.info("Клиент userId: {} успешно зарегистрирован", userId);
            var messageBuilder = new StringBuilder();
            messageBuilder.append("Поздравляю, ты успешно зарегистрирован!\n\n");
            messageBuilder.append("Чтобы воспользоваться услугами нашего Мини-банка, осталось только открыть счёт, ");
            messageBuilder.append("для этого введи команду /createaccount.");
            responseBuilder.text(messageBuilder.toString());
        } else {
            responseBuilder.text("Сейчас регистрация недоступна, повтори попытку позже");
        }

        telegramClient.execute(responseBuilder.build());
    }
}
