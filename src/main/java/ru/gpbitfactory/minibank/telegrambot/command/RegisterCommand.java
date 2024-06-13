package ru.gpbitfactory.minibank.telegrambot.command;

import feign.FeignException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage.SendMessageBuilder;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.gpbitfactory.minibank.middle.api.ClientsApiClient;
import ru.gpbitfactory.minibank.middle.dto.CreateClientRequest;

@Slf4j
@Component
public class RegisterCommand extends BotCommand {

    private final ClientsApiClient middleService;

    public RegisterCommand(ClientsApiClient middleService) {
        super("register", "Регистрация нового клиента");
        this.middleService = middleService;
    }

    @Override
    @SneakyThrows
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] strings) {
        var userId = user.getId();
        log.debug("Поступил запрос на регистрацию клиента {}", userId);
        var responseBuilder = SendMessage.builder().chatId(chat.getId());

        try {
            var createClientRequest = CreateClientRequest.builder()
                    .telegramUserId(userId)
                    .build();
            middleService.createNewClient(createClientRequest);
            responseBuilder.text("Вы успешно зарегистрированы!");
            log.debug("Клиент {} успешно зарегистрирован", userId);
        } catch (FeignException e) {
            setMessageToResponseBuilder(userId, e, responseBuilder);
            log.error("Во время регистрации клиента возникла непредвиденная ситуация", e);
        }

        telegramClient.execute(responseBuilder.build());
    }

    private void setMessageToResponseBuilder(long userId, FeignException e, SendMessageBuilder builder) {
        var verificationMessage = String.format("Пользователь с telegramUserId: %s уже зарегистрирован", userId);
        var clientIsAlreadyRegistered = e.getMessage().contains(verificationMessage);

        if (clientIsAlreadyRegistered) {
            builder.text("Вы уже зарегистрированы ранее!");
        } else {
            builder.text("Сервис временно недоступен, повторите попытку позже").build();
        }
    }
}
