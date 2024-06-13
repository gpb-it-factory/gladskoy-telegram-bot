package ru.gpbitfactory.minibank.telegrambot.restclient;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.gpbitfactory.minibank.middle.dto.CreateClientRequestV2;

@FeignClient("middle-clients-service")
public interface MiddleServiceClientsApiClient {

    @PostMapping("/clients")
    ResponseEntity<Void> createNewClient(@RequestBody @Valid CreateClientRequestV2 createUserRequest);
}
