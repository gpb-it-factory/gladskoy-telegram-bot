package ru.gpbitfactory.minibank.telegrambot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
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
import ru.gpbitfactory.minibank.telegrambot.restclient.MiddleServiceClientsApiClient;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractUpdateConsumerTest {

    protected static final long DEFAULT_USER_ID = 123;
    protected static final long DEFAULT_CHAT_ID = 111;
    protected static final int DEFAULT_MESSAGE_ID = 1;

    @MockBean
    protected TelegramClient telegramClient;

    @MockBean
    protected MiddleServiceClientsApiClient middleApiClient;

    @MockBean
    protected TelegramBotsLongPollingApplication telegramApplication;

    @Autowired
    protected LongPollingSingleThreadUpdateConsumer updateConsumer;

    protected Update buildUpdateMessage(String messageText, boolean isCommand) {
        return buildUpdateMessage(DEFAULT_USER_ID, messageText, isCommand);
    }

    protected Update buildUpdateMessage(long userId, String messageText, boolean isCommand) {
        var messageBuilder = Message.builder()
                .messageId(DEFAULT_MESSAGE_ID)
                .chat(Chat.builder()
                        .id(DEFAULT_CHAT_ID)
                        .type("private")
                        .build())
                .text(messageText)
                .from(User.builder()
                        .id(userId)
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
                .chatId(DEFAULT_CHAT_ID)
                .text(text)
                .build();
    }
}
