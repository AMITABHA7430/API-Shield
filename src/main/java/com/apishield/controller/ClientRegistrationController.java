
package com.apishield.controller;

import com.apishield.service.ClientRegistrationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clients")
public class ClientRegistrationController {

    private final ClientRegistrationService registrationService;

    public ClientRegistrationController(
            ClientRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    public record RegistrationRequest(
            @NotBlank
            @Size(max = 100)
            String clientName) {
    }

    public record RegistrationResponse(
            String clientName,
            String apiKey,
            String message) {
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> registerClient(
            @Valid @RequestBody RegistrationRequest request) {

        String apiKey = registrationService.registerClient(
                request.clientName().trim());

        RegistrationResponse response = new RegistrationResponse(
                request.clientName().trim(),
                apiKey,
                "Client registered successfully. Store your API key securely."
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
