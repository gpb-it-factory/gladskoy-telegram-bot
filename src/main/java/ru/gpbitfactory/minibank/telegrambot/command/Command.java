package ru.gpbitfactory.minibank.telegrambot.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Command {

    START("start", "Начало работы с ботом"),
    PING("ping", "Тест, возвращает в ответ строку \"pong\"");

    private final String value;
    private final String description;
}
