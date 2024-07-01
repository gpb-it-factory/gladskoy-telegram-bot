package ru.gpbitfactory.minibank.telegrambot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.gpbitfactory.minibank.middle.dto.AccountResponse;
import ru.gpbitfactory.minibank.middle.dto.ClientAccount;
import ru.gpbitfactory.minibank.middle.dto.ClientResponse;
import ru.gpbitfactory.minibank.middle.dto.CreateClientAccountRequest;
import ru.gpbitfactory.minibank.middle.dto.CreateTransferRequest;
import ru.gpbitfactory.minibank.middle.dto.CreateTransferResponse;
import ru.gpbitfactory.minibank.telegrambot.restclient.MiddleServiceClientsApiClient;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MiddleApiService {

    private final MiddleServiceClientsApiClient middleServiceClientsApiClient;
    private final ObjectMapper objectMapper;

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

    public CreateTransferResponse createMoneyTransfer(CreateTransferRequest createTransferRequest) throws JsonProcessingException {
        var fromUsername = createTransferRequest.getFrom();
        var toUsername = createTransferRequest.getTo();

        log.debug("Отправляем запрос в Middle Service на перевод средств от клиента {} клиенту {}", fromUsername, toUsername);
        try {
            return middleServiceClientsApiClient.createTransfer(createTransferRequest).getBody();
        } catch (FeignException e) {
            log.error("Не удалось перевести средства от клиента {} клиенту {}", fromUsername, toUsername, e);
            return objectMapper.readValue(e.contentUTF8(), CreateTransferResponse.class);
        }
    }
}
