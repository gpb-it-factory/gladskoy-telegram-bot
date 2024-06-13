package ru.gpbitfactory.minibank.telegrambot.util;

import feign.FeignException;

public final class MockFeignException extends FeignException {

    public MockFeignException(int status) {
        super(status, "Mock error");
    }

    public MockFeignException(int status, String message) {
        super(status, message);
    }
}
