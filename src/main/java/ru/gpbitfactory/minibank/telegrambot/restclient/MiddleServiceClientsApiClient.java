package ru.gpbitfactory.minibank.telegrambot.restclient;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.gpbitfactory.minibank.middle.dto.AccountResponse;
import ru.gpbitfactory.minibank.middle.dto.ClientResponse;
import ru.gpbitfactory.minibank.middle.dto.CreateClientAccountRequest;
import ru.gpbitfactory.minibank.middle.dto.CreateClientAccountResponse;
import ru.gpbitfactory.minibank.middle.dto.CreateClientRequestV2;

import java.util.List;

@FeignClient("middle-clients-service")
public interface MiddleServiceClientsApiClient {

    @PostMapping("/v2/clients")
    ResponseEntity<Void> createNewClient(@RequestBody @Valid CreateClientRequestV2 createUserRequest);

    @GetMapping("/v1/clients/{telegramId}")
    ResponseEntity<ClientResponse> getClient(@PathVariable long telegramId);

    @PostMapping("/v1/clients/{telegramId}/accounts")
    ResponseEntity<CreateClientAccountResponse> createClientAccount(@PathVariable long telegramId,
                                                                    @RequestBody @Valid CreateClientAccountRequest request);

    @GetMapping("/v1/accounts")
    ResponseEntity<List<AccountResponse>> getAvailableAccounts();
}
