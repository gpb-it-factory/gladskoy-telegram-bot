package ru.gpbitfactory.minibank.telegrambot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.EntityType;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@SpringBootTest
public abstract class AbstractUpdateConsumerTest {

    @MockBean
    protected TelegramClient telegramClient;

    @MockBean
    protected TelegramBotsLongPollingApplication telegramApplication;

    @Autowired
    protected LongPollingSingleThreadUpdateConsumer updateConsumer;

    protected Update buildUpdateMessage(String messageText, boolean isCommand) {
        var messageBuilder = Message.builder()
                .messageId(1)
                .chat(Chat.builder()
                        .id(111L)
                        .type("private")
                        .build())
                .text(messageText)
                .from(User.builder()
                        .id(222L)
                        .firstName("Test User")
                        .isBot(false)
                        .build());

        if (isCommand) {
            messageBuilder.entities(List.of(MessageEntity.builder()
                    .type(EntityType.BOTCOMMAND)
                    .offset(0)
                    .length(0)
                    .build()));
        }

        var update = new Update();
        update.setMessage(messageBuilder.build());
        return update;
    }

    protected SendMessage buildExpectedSendMessage(String text) {
        return SendMessage.builder()
                .chatId(111L)
                .text(text)
                .build();
    }
}
