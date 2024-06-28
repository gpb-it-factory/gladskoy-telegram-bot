package ru.gpbitfactory.minibank.telegrambot.service;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractServiceTest {

    // Необходимо для того, чтобы не создавалась реальная сессия с Telegram.
    // Если не замокать этот класс, то контекст не загрузится.
    @MockBean
    protected TelegramBotsLongPollingApplication telegramApplication;
}
