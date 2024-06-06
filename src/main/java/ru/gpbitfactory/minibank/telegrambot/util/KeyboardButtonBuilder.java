package ru.gpbitfactory.minibank.telegrambot.util;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

@UtilityClass
public class KeyboardButtonBuilder {

    public static InlineKeyboardButton.InlineKeyboardButtonBuilder buttonBuilder(String name, String callbackId) {
        return InlineKeyboardButton.builder()
                .text(name)
                .callbackData(callbackId);
    }

    public static InlineKeyboardMarkup.InlineKeyboardMarkupBuilder keyboardBuilder(InlineKeyboardButton... buttons) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(buttons));
    }
}
