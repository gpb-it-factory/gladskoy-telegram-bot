package ru.gpbitfactory.minibank.telegrambot.command;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.gpbitfactory.minibank.telegrambot.util.SendMessageBuilder;

import java.util.List;

@Slf4j
@Component
public class HelpCommand extends BotCommand {

    private final List<IBotCommand> botCommands;

    public HelpCommand(List<IBotCommand> botCommands) {
        super("help", "Основная информация и список команд");
        this.botCommands = botCommands;
    }

    @Override
    @SneakyThrows
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] arguments) {
        var messageBuilder = new StringBuilder("<b>Бот приложения Мини-банк</b>\n\n");
        messageBuilder.append("Список команд:\n\n");

        for (var command : this.botCommands) {
            messageBuilder.append("/")
                    .append(command.getCommandIdentifier()).append(" - ")
                    .append(command.getDescription()).append("\n");
        }

        var responseBuilder = SendMessageBuilder.of(chat.getId());
        responseBuilder.text(messageBuilder.toString());
        telegramClient.execute(responseBuilder.build());
    }
}
