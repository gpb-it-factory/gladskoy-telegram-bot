package ru.gpbitfactory.minibank.telegrambot.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.gpbitfactory.minibank.middle.dto.AccountResponse;
import ru.gpbitfactory.minibank.middle.dto.ClientAccount;
import ru.gpbitfactory.minibank.middle.dto.ClientResponse;
import ru.gpbitfactory.minibank.middle.dto.CreateClientAccountRequest;
import ru.gpbitfactory.minibank.middle.dto.CreateClientRequestV2;
import ru.gpbitfactory.minibank.telegrambot.restclient.MiddleServiceClientsApiClient;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MiddleApiService {

    private final MiddleServiceClientsApiClient middleServiceClientsApiClient;

    public List<AccountResponse> getAvailableAccounts() {
        log.debug("Запрашиваем в Middle Service счета, доступные для открытия");
        try {
            var responseBody = middleServiceClientsApiClient.getAvailableAccounts().getBody();
            if (responseBody != null) {
                log.debug("Найдено {} счетов, доступных для открытия", responseBody.size());
                return responseBody;
            }
        } catch (FeignException e) {
            log.error("Не удалось получить  счета, доступные для открытия", e);
        }
        return List.of();
    }

    public List<ClientAccount> getClientAccounts(long userId) {
        log.debug("Запрашиваем в Middle Service данные об открытых счетах клиента userId: {}", userId);
        try {
            var responseBody = middleServiceClientsApiClient.getClient(userId).getBody();
            if (responseBody != null) {
                log.debug("У клиента userId: {} открыто {} счетов", userId, responseBody.getAccounts().size());
                return responseBody.getAccounts();
            }
        } catch (FeignException e) {
            log.error("Не удалось получить данные об открытых счетах клиента userId: {}", userId, e);
        }
        return List.of();
    }

    @Cacheable(cacheNames = "clients", key = "#userId", unless = "#result == null")
    public Optional<ClientResponse> getClient(long userId) {
        log.debug("Запрашиваем в Middle Service данные клиента userId: {}", userId);
        try {
            var responseBody = middleServiceClientsApiClient.getClient(userId).getBody();
            log.debug("Данные клиента userId: {} успешно получены", userId);
            return Optional.ofNullable(responseBody);
        } catch (FeignException e) {
            log.error("Не удалось получить данные клиента userId: {}", userId, e);
            return Optional.empty();
        }
    }

    public boolean createClientAccount(long userId, CreateClientAccountRequest request) {
        log.debug("Отправляем запрос в Middle Service на открытие счёта {} для клиента userId: {}", request.getAccountName(), userId);
        try {
            var responseBody = middleServiceClientsApiClient.createClientAccount(userId, request).getBody();
            if (responseBody != null) {
                log.debug(responseBody.getMessage());
                return true;
            }
        } catch (FeignException e) {
            log.error("Не удалось открыть счёт {} для клиента userId: {}", request.getAccountName(), userId, e);
        }
        return false;
    }

    public boolean createNewClient(CreateClientRequestV2 request) {
        log.debug("Отправляем в Middle Service запрос на регистрацию клиента userId: {}", request.getTelegramUserId());
        try {
            var responseBody = middleServiceClientsApiClient.createNewClient(request).getBody();
            if (responseBody != null) {
                log.debug(responseBody.getMessage());
                return true;
            }
        } catch (FeignException e) {
            log.error("Не удалось зарегистрировать клиента userId: {}", request.getTelegramUserId(), e);
        }
        return false;
    }
}
