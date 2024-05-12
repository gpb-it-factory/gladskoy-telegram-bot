package ru.gpbitfactory.minibank.telegrambot.command;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static ru.gpbitfactory.minibank.telegrambot.command.Command.START;

@Component
public class StartCommand extends BotCommand {

    public StartCommand() {
        super(START.getValue(), START.getDescription());
    }

    @Override
    @SneakyThrows
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] strings) {
        var messageBuilder = new StringBuilder();
        var userName = user.getFirstName();

        messageBuilder.append("Привет, ").append(userName).append("!\n");
        messageBuilder.append("Добро пожаловать в Мини-Банк.\n\n");
        messageBuilder.append("Для того чтобы протестировать бота, введи команду /ping.");

        var answer = new SendMessage(chat.getId().toString(), messageBuilder.toString());
        telegramClient.execute(answer);
    }
}
