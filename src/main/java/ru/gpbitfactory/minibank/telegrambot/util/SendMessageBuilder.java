package ru.gpbitfactory.minibank.telegrambot.util;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@UtilityClass
@SuppressWarnings("rawtypes")
public class SendMessageBuilder {

    public static SendMessage.SendMessageBuilder of(long chatId) {
        return of(chatId, null);
    }

    public static SendMessage.SendMessageBuilder of(long chatId, ReplyKeyboard buttonBlock) {
        var sendMessageBuilder = SendMessage.builder()
                .chatId(chatId)
                .parseMode(ParseMode.HTML);
        if (buttonBlock != null) {
            sendMessageBuilder.replyMarkup(buttonBlock);
        }
        return sendMessageBuilder;
    }
}
