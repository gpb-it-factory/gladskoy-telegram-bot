package ru.gpbitfactory.minibank.telegrambot.util;

import feign.FeignException;

import java.util.Map;

public final class MockFeignException extends FeignException {

    public MockFeignException(int status) {
        super(status, "Mock error");
    }

    public MockFeignException(int status, String message) {
        super(status, message);
    }

    public MockFeignException(int status, byte[] responseBody) {
        super(status, "Mock error", responseBody, Map.of());
    }
}
