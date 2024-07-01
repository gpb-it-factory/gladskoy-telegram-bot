package ru.gpbitfactory.minibank.telegrambot.util;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@UtilityClass
@SuppressWarnings("rawtypes")
public class EditMessageBuilder {

    public static EditMessageText.EditMessageTextBuilder of(CallbackQuery callbackQuery) {
        return of(callbackQuery, null);
    }

    public static EditMessageText.EditMessageTextBuilder of(CallbackQuery callbackQuery, InlineKeyboardMarkup buttonBlock) {
        var callbackMessage = callbackQuery.getMessage();
        var editMessageBuilder = EditMessageText.builder()
                .chatId(callbackMessage.getChatId())
                .messageId(callbackMessage.getMessageId());
        if (buttonBlock != null) {
            editMessageBuilder.replyMarkup(buttonBlock);
        }
        return editMessageBuilder;
    }
}
