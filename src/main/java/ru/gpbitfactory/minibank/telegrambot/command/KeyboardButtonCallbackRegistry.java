package ru.gpbitfactory.minibank.telegrambot.command;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Реестр операций, которые выполняются после нажатия на кнопку в Telegram-чате с ботом.
 */
@Component
public class KeyboardButtonCallbackRegistry {

    private Map<String, Consumer<CallbackQuery>> buttonCallbackActionsRegistry;

    /**
     * Добавление операции в реестр.
     *
     * @param callbackButtonId идентификатор кнопки.
     * @param callbackAction   операция, которая будет выполнена после нажатие на кнопку.
     * @see org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton#callbackData
     */
    public void register(String callbackButtonId, Consumer<CallbackQuery> callbackAction) {
        if (buttonCallbackActionsRegistry == null) {
            buttonCallbackActionsRegistry = new ConcurrentHashMap<>();
        } else if (buttonCallbackActionsRegistry.containsKey(callbackButtonId)) {
            return;
        }

        buttonCallbackActionsRegistry.put(callbackButtonId, callbackAction);
    }

    /**
     * Получение операции из реестра.
     *
     * @param callbackButtonId идентификатор кнопки.
     * @return операцию, которая будет выполнена после нажатия на кнопку.
     */
    public Optional<Consumer<CallbackQuery>> getButtonAction(String callbackButtonId) {
        return Optional.ofNullable(buttonCallbackActionsRegistry.get(callbackButtonId));
    }
}
