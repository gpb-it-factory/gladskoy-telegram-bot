package ru.gpbitfactory.minibank.telegrambot.exception;

public class AmountArgumentNotValidException extends RuntimeException {

    public AmountArgumentNotValidException(String message) {
        super(message);
    }
}
