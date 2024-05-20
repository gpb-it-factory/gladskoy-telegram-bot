package ru.gpbitfactory.minibank.telegrambot.command;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class PingCommand extends BotCommand {

    public PingCommand() {
        super("ping", "Возвращает в ответ строку \"pong\"");
    }

    @Override
    @SneakyThrows
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] strings) {
        var answer = new SendMessage(chat.getId().toString(), "pong");
        telegramClient.execute(answer);
    }
}
