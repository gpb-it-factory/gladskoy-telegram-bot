package ru.gpbitfactory.minibank.telegrambot.util;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

@UtilityClass
public class EditMessageBuilder {

    public static EditMessageText.EditMessageTextBuilder of(CallbackQuery callbackQuery) {
        var callbackMessage = callbackQuery.getMessage();
        return EditMessageText.builder()
                .chatId(callbackMessage.getChatId())
                .messageId(callbackMessage.getMessageId());
    }
}
