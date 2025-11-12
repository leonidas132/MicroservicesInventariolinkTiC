package com.linktic.microservice.inventario.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.linktic.microservice.inventario.model.dto.ProductoResponse;

@Component
public class ProductoServiceClient {
    
    private final RestTemplate restTemplate;
    
    @Value("${app.product-service.url}")
    private String productServiceUrl;
    
    @Value("${app.product-service.api-key}")
    private String productServiceApiKey;
    
    @Value("${app.product-service.max-retries}")
    private int maxRetries;
    
    public ProductoServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @Retryable(
        value = {ResourceAccessException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    public ProductoResponse obtenerProducto(Long productoId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", productServiceApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<ProductoResponse> response = restTemplate.exchange(
                productServiceUrl + "/api/productos/" + productoId,
                HttpMethod.GET,
                entity,
                ProductoResponse.class
            );
            
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }
}
