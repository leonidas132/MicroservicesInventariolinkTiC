package com.linktic.microservice.inventario.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.linktic.microservice.inventario.model.dto.ProductoResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ProductoServiceClient productoServiceClient;

    @Test
    void obtenerProducto_ProductoExistente_DeberiaRetornarProducto() {
        // Arrange
        ProductoResponse response = new ProductoResponse();
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(ProductoResponse.class)
        )).thenReturn(ResponseEntity.ok(response));

        // Act
        ProductoResponse resultado = productoServiceClient.obtenerProducto(1L);

        // Assert
        assertNotNull(resultado);
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(), eq(ProductoResponse.class));
    }

    @Test
    void obtenerProducto_ProductoNoExistente_DeberiaRetornarNull() {
        // Arrange
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(ProductoResponse.class)
        )).thenThrow(HttpClientErrorException.NotFound.class);

        // Act
        ProductoResponse resultado = productoServiceClient.obtenerProducto(99L);

        // Assert
        assertNull(resultado);
    }

    @Test
    void obtenerProducto_ErrorComunicacion_DeberiaReintentarYLanzarExcepcion() {
    	  // Arrange
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(ProductoResponse.class)
        )).thenThrow(new ResourceAccessException("Timeout"));

        // Act & Assert
        assertThrows(ResourceAccessException.class, () -> {
            productoServiceClient.obtenerProducto(1L);
        });

        // Verificar que se intenta 3 veces si usas @Retryable
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(), eq(ProductoResponse.class));
}
}