package ru.gpbitfactory.minibank.telegrambot.config;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import ru.gpbitfactory.minibank.telegrambot.command.PingCommand;
import ru.gpbitfactory.minibank.telegrambot.command.StartCommand;
import ru.gpbitfactory.minibank.telegrambot.handler.CommandsHandler;

import java.util.List;

@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(TelegramBotProperties.class)
public class TelegramBotConfiguration {

    private final TelegramBotProperties properties;

    @Bean
    public LongPollingUpdateConsumer longPollingUpdateConsumer() {
        var commands = List.of(
                new StartCommand(),
                new PingCommand()
        );
        return new CommandsHandler(properties.getToken(), properties.getName(), commands);
    }

    @Bean
    @SneakyThrows
    public TelegramBotsLongPollingApplication telegramBotsLongPollingApplication() {
        var botsApplication = new TelegramBotsLongPollingApplication();
        botsApplication.registerBot(properties.getToken(), longPollingUpdateConsumer());
        return botsApplication;
    }
}
