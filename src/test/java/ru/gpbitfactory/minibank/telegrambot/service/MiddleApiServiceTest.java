package ru.gpbitfactory.minibank.telegrambot.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.gpbitfactory.minibank.middle.dto.ClientResponse;
import ru.gpbitfactory.minibank.telegrambot.restclient.MiddleServiceClientsApiClient;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MiddleApiServiceTest extends AbstractServiceTest {

    @MockBean
    private MiddleServiceClientsApiClient middleApiClient;

    @Autowired
    private MiddleApiService middleApiService;

    @Test
    @DisplayName("Кеширование данных клиента")
    void whenClientDataIsAddedToClientsCache_thenMiddleApiServiceGetClientMethodShouldBeRequestedOnlyOnce() {
        var clientResponse = new ResponseEntity<>(new ClientResponse(), HttpStatus.OK);
        when(middleApiClient.getClient(1)).thenReturn(clientResponse);

        middleApiService.getClient(1);
        middleApiService.getClient(1);

        verify(middleApiClient, times(1)).getClient(1);
    }
}