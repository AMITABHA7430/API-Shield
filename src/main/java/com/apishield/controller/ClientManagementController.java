
package com.apishield.controller;

import com.apishield.model.ApiClient;
import com.apishield.repository.ApiClientRepository;
import com.apishield.service.ApiKeyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clients")
public class ClientManagementController {

    private final ApiClientRepository repository;
    private ApiKeyService apiKeyService;

    public ClientManagementController(
            ApiClientRepository repository,
            ApiKeyService apiKeyService) {
        this.repository = repository;
        this.apiKeyService = apiKeyService;
    }

    @PatchMapping("/{id}/revoke")
    public ResponseEntity<String> revokeClient(
            @PathVariable Long id) {

        ApiClient client = repository.findById(id)
                .orElse(null);

        if (client == null) {
            return ResponseEntity.notFound().build();
        }

        client.setActive(false);
        repository.save(client);

        return ResponseEntity.ok(
                "{\"message\":\"API key revoked successfully\"}"
        );
    }

    @PostMapping("/{id}/rotate-key")
    public ResponseEntity<String> rotateApiKey(
            @PathVariable Long id) {

        ApiClient client = repository.findById(id)
                .orElse(null);

        if (client == null) {
            return ResponseEntity.notFound().build();
        }

        if (!client.isActive()) {
            return ResponseEntity.badRequest()
                    .body("{\"error\":\"Client is inactive\"}");
        }

        String newApiKey = apiKeyService.generateApiKey();
        String newKeyHash = apiKeyService.hashApiKey(newApiKey);

        client.setApiKeyHash(newKeyHash);
        repository.save(client);

        return ResponseEntity.ok(
                "{\"apiKey\":\"" + newApiKey + "\"}"
        );
    }

}
