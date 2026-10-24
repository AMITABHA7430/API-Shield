
package com.apishield.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;

@RestController
public class GatewayController {

    private final RestClient restClient;

    public GatewayController(
            @Value("${apishield.upstream.base-url}") String upstreamBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(upstreamBaseUrl)
                .build();
    }

    @GetMapping("/gateway/weather")
    public ResponseEntity<String> getWeather() {
        return forwardRequest("/posts/1");
    }

    @GetMapping("/gateway/posts/{id}")
    public ResponseEntity<String> getPost(@PathVariable int id) {
        return forwardRequest("/posts/" + id);
    }

    private ResponseEntity<String> forwardRequest(String upstreamPath) {
        try {
            String upstreamResponse = restClient.get()
                    .uri(upstreamPath)
                    .retrieve()
                    .body(String.class);

            return ResponseEntity.ok(upstreamResponse);

        } catch (RestClientResponseException ex) {
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(ex.getResponseBodyAsString());

        } catch (ResourceAccessException ex) {
            return ResponseEntity
                    .status(502)
                    .body("{\"error\":\"Upstream API unavailable\"}");
        }
    }
}
