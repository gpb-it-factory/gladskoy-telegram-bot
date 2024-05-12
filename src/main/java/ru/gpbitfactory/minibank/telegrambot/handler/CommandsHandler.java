package ru.gpbitfactory.minibank.telegrambot.handler;

import lombok.SneakyThrows;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.extensions.bots.commandbot.CommandLongPollingTelegramBot;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public class CommandsHandler extends CommandLongPollingTelegramBot {

    public CommandsHandler(String botToken, String botName, List<BotCommand> commands) {
        super(new OkHttpTelegramClient(botToken), true, () -> botName);
        commands.forEach(this::register);
    }

    @Override
    @SneakyThrows
    public void processNonCommandUpdate(Update update) {
        var message = update.getMessage();
        if (message != null && message.hasText()) {
            var botMessage = SendMessage.builder()
                    .chatId(message.getChatId())
                    .text("") // нужно инициализировать обязательно, т.к. поле text is marked non-null
                    .build();

            if (message.isCommand()) {
                botMessage.setText(String.format("Команда %s не поддерживается", message.getText()));
            } else {
                botMessage.setText("Я бы рад с тобой поболтать, но пока я этому ещё не научился :)");
            }

            telegramClient.execute(botMessage);
        }
    }
}
