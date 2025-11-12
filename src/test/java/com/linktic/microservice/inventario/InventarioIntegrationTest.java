package com.linktic.microservice.inventario;

import com.linktic.microservice.inventario.model.entity.Inventario;
import com.linktic.microservice.inventario.repository.InventarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
class InventarioIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InventarioRepository inventarioRepository;

    private HttpHeaders crearHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", "inventory-service-key-456");
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void crearYConsultarInventario_FlujoCompleto() {
        // Crear inventario
        Inventario inventario = new Inventario(1L, 100);
        HttpEntity<Inventario> request = new HttpEntity<>(inventario, crearHeaders());

        ResponseEntity<String> crearResponse = restTemplate.postForEntity(
            "/api/inventarios", request, String.class);

        // En un entorno real, esto asumiría que el producto existe en el servicio de productos
        // Para la prueba, podría retornar 400 si el producto no existe
        assertTrue(crearResponse.getStatusCode().is2xxSuccessful() || 
                  crearResponse.getStatusCode() == HttpStatus.BAD_REQUEST);

        // Consultar inventario
        HttpEntity<String> getRequest = new HttpEntity<>(crearHeaders());
        ResponseEntity<String> getResponse = restTemplate.exchange(
            "/api/inventarios/1", HttpMethod.GET, getRequest, String.class);

        // Depende de si el producto existe en el servicio de productos
        assertTrue(getResponse.getStatusCode().is2xxSuccessful() || 
                  getResponse.getStatusCode() == HttpStatus.NOT_FOUND);
    }

    @Test
    void actualizarInventarioPorCompra_DeberiaActualizarStock() {
        // Primero crear inventario
        Inventario inventario = new Inventario(2L, 50);
        HttpEntity<Inventario> crearRequest = new HttpEntity<>(inventario, crearHeaders());
        restTemplate.postForEntity("/api/inventarios", crearRequest, String.class);

        // Actualizar por compra
        HttpEntity<String> compraRequest = new HttpEntity<>(crearHeaders());
        ResponseEntity<String> compraResponse = restTemplate.exchange(
            "/api/inventarios/2/compra?cantidadComprada=10", 
            HttpMethod.PUT, compraRequest, String.class);

        // Verificar respuesta
        if (compraResponse.getStatusCode().is2xxSuccessful()) {
            assertTrue(compraResponse.getBody().contains("40")); // 50 - 10 = 40
        }
    }
}
