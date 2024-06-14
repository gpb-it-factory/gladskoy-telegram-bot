package ru.gpbitfactory.minibank.telegrambot.util;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@UtilityClass
public class SendMessageBuilder {

    public static SendMessage.SendMessageBuilder of(long chatId, ReplyKeyboard buttonBlock) {
        return SendMessage.builder()
                .chatId(chatId)
                .replyMarkup(buttonBlock)
                .parseMode(ParseMode.HTML);
    }
}
