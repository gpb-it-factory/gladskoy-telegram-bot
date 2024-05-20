package ru.gpbitfactory.minibank.telegrambot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TelegramBotApplicationTest {

    /*
     * TODO удалить, после того как будут написаны реальные тесты.
     * Сейчас это нужно для того, чтобы протестировать GitHub Actions.
     */
    @MockBean
    private TelegramBotsLongPollingApplication telegramApplication;

    @Test
    void whenRunTest_thenItShouldBePassed() {
        assertTrue(true);
    }
}
