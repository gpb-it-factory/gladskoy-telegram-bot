package ru.gpbitfactory.minibank.telegrambot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("telegram-bot")
public class TelegramBotProperties {

    private String name;
    private String token;
    private boolean allowCommandsWithParameters;
}
