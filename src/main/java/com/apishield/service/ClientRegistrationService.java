
package com.apishield.service;

import com.apishield.model.ApiClient;
import com.apishield.repository.ApiClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientRegistrationService {

    private final ApiClientRepository apiClientRepository;
    private final ApiKeyService apiKeyService;

    public ClientRegistrationService(
            ApiClientRepository apiClientRepository,
            ApiKeyService apiKeyService) {
        this.apiClientRepository = apiClientRepository;
        this.apiKeyService = apiKeyService;
    }

    @Transactional
    public String registerClient(String clientName) {
        String apiKey = apiKeyService.generateApiKey();
        String apiKeyHash = apiKeyService.hashApiKey(apiKey);

        ApiClient client = new ApiClient();
        client.setClientName(clientName);
        client.setApiKeyHash(apiKeyHash);
        client.setActive(true);
        client.setRateLimitPerMinute(3);

        apiClientRepository.save(client);

        return apiKey;
    }
}
