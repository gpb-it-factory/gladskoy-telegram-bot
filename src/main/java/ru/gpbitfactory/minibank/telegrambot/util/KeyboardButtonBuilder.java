package ru.gpbitfactory.minibank.telegrambot.util;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

@UtilityClass
@SuppressWarnings("rawtypes")
public class KeyboardButtonBuilder {

    public static InlineKeyboardButton.InlineKeyboardButtonBuilder buttonBuilder(String name, String callbackId) {
        return InlineKeyboardButton.builder()
                .text(name)
                .callbackData(callbackId);
    }

    public static InlineKeyboardMarkup.InlineKeyboardMarkupBuilder keyboardBuilder(InlineKeyboardButton... buttons) {
        var inlineKeyboardMarkupBuilder = InlineKeyboardMarkup.builder();
        if (buttons.length != 0) {
            inlineKeyboardMarkupBuilder.keyboardRow(new InlineKeyboardRow(buttons));
        }
        return inlineKeyboardMarkupBuilder;
    }
}
