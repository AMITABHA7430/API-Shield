
package com.apishield.repository;

import com.apishield.model.ApiClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiClientRepository extends JpaRepository<ApiClient, Long> {

    Optional<ApiClient> findByApiKeyHash(String apiKeyHash);
}
