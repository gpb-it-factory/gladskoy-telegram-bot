package ru.gpbitfactory.minibank.telegrambot.command;

import feign.FeignException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.gpbitfactory.minibank.telegrambot.util.EditMessageBuilder;
import ru.gpbitfactory.minibank.telegrambot.util.KeyboardButtonBuilder;
import ru.gpbitfactory.minibank.telegrambot.util.SendMessageBuilder;

import java.util.function.Consumer;

@Slf4j
@Component
public class CreateAccountCommand extends BotCommand {

    private final KeyboardButtonCallbackRegistry buttonCallbackRegistry;

    public CreateAccountCommand(KeyboardButtonCallbackRegistry buttonCallbackRegistry) {
        super("createaccount", "Открытие счёта в Мини-банке");
        this.buttonCallbackRegistry = buttonCallbackRegistry;
    }

    @Override
    @SneakyThrows
    public void execute(TelegramClient telegramClient, User user, Chat chat, String[] strings) {
        var nextButton = KeyboardButtonBuilder.buttonBuilder("ДАЛЕЕ", "createAccountNext").build();
        buttonCallbackRegistry.register(nextButton.getCallbackData(), createNextButtonAction(telegramClient));

        var cancelButton = KeyboardButtonBuilder.buttonBuilder("ОТМЕНА", "createAccountCancel").build();
        buttonCallbackRegistry.register(cancelButton.getCallbackData(), createCancelButtonAction(telegramClient));

        var buttonBlock = KeyboardButtonBuilder.keyboardBuilder(nextButton, cancelButton).build();
        var responseBuilder = SendMessageBuilder.of(chat.getId(), buttonBlock);
        responseBuilder.text("Для подтверждения открытия счёта необходимо нажать на кнопку \"Далее\"");
        telegramClient.execute(responseBuilder.build());
    }

    private Consumer<CallbackQuery> createNextButtonAction(TelegramClient telegramClient) {
        return callbackQuery -> {
            try {
                // TODO научиться открывать счёт в Middle Service: https://github.com/gpb-it-factory/gladskoy-telegram-bot/issues/34
                var responseBuilder = EditMessageBuilder.of(callbackQuery);
                responseBuilder.text("""
                        Счёт успешно открыт! Тебе зачислено 5000 бонусных рублей!
                                            
                        Деньгами можно воспользоваться прямо сейчас. Для того чтобы ознакомиться со списком доступных
                        операций, введи команду /help.
                        """);
                sendResponse(telegramClient, callbackQuery.getId(), responseBuilder.build());
            } catch (FeignException e) {
                log.error("Во время создания счёта возникла непредвиденная ситуация", e);
            }
        };
    }

    private Consumer<CallbackQuery> createCancelButtonAction(TelegramClient telegramClient) {
        return callbackQuery -> {
            var responseBuilder = EditMessageBuilder.of(callbackQuery);
            responseBuilder.text("Операция отменена. Если это произошло случайно, введи команду /createaccount снова.");
            sendResponse(telegramClient, callbackQuery.getId(), responseBuilder.build());
        };
    }

    private void sendResponse(TelegramClient telegramClient, String callbackQueryId, BotApiMethod<?> responseMessage) {
        // На каждое сообщение CallbackQuery необходимо отправлять ответ AnswerCallbackQuery.
        // Иначе нажатая кнопка продолжит находиться в состоянии ожидания (в UI она будет "мигать").
        var callbackAnswer = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .build();
        try {
            telegramClient.execute(callbackAnswer);
            telegramClient.execute(responseMessage);
        } catch (TelegramApiException e) {
            log.error("Во время отправки ответа клиенту возникла непредвиденная ситуация", e);
        }
    }
}
