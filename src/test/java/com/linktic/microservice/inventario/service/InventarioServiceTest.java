package com.linktic.microservice.inventario.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;

import com.linktic.microservice.inventario.client.ProductoServiceClient;
import com.linktic.microservice.inventario.event.InventarioEventPublisher;
import com.linktic.microservice.inventario.exception.InventarioServiceException;
import com.linktic.microservice.inventario.model.dto.ProductoResponse;
import com.linktic.microservice.inventario.model.entity.Inventario;
import com.linktic.microservice.inventario.repository.InventarioRepository;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private ProductoServiceClient productoServiceClient;

    @Mock
    private InventarioEventPublisher eventPublisher;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    private Inventario inventario;
    private ProductoResponse productoResponse;

    @BeforeEach
    void setUp() {
        inventario = new Inventario(1L, 50);
        
        productoResponse = new ProductoResponse();
        ProductoResponse.ResponseData data = new ProductoResponse.ResponseData();
        ProductoResponse.Attributes attributes = new ProductoResponse.Attributes();
        attributes.setNombre("Laptop Gaming");
        attributes.setPrecio(new BigDecimal("1299.99"));
        data.setAttributes(attributes);
        data.setId(1L);
        productoResponse.setData(data);
    }

    @Test
    void consultarCantidadDisponible_ProductoExistente_DeberiaRetornarInformacionCompleta() {
        // Arrange
        when(productoServiceClient.obtenerProducto(1L)).thenReturn(productoResponse);
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Optional.of(inventario));

        // Act
        var resultado = inventarioService.consultarCantidadDisponible(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.get("producto_id"));
        assertEquals("Laptop Gaming", resultado.get("producto_nombre"));
        assertEquals(new BigDecimal("1299.99"), resultado.get("producto_precio"));
        assertEquals(50, resultado.get("cantidad_disponible"));
        verify(productoServiceClient, times(1)).obtenerProducto(1L);
    }

    @Test
    void consultarCantidadDisponible_ProductoNoExistente_DeberiaLanzarExcepcion() {
        // Arrange
        when(productoServiceClient.obtenerProducto(99L)).thenReturn(null);

        // Act & Assert
        assertThrows(InventarioServiceException.class, () -> {
            inventarioService.consultarCantidadDisponible(99L);
        });
    }

    @Test
    void consultarCantidadDisponible_ErrorComunicacionProductos_DeberiaLanzarExcepcion() {
        // Arrange
        when(productoServiceClient.obtenerProducto(1L))
            .thenThrow(new ResourceAccessException("Timeout"));

        // Act & Assert
        assertThrows(ResourceAccessException.class, () -> {
            inventarioService.consultarCantidadDisponible(1L);
        });
    }

    @Test
    void actualizarCantidadPorCompra_StockSuficiente_DeberiaActualizarYEmitirEvento() {
        // Arrange
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);

        // Act
        Inventario resultado = inventarioService.actualizarCantidadPorCompra(1L, 10);

        // Assert
        assertNotNull(resultado);
        assertEquals(40, inventario.getCantidad()); // 50 - 10 = 40
        verify(inventarioRepository, times(1)).save(inventario);
        verify(eventPublisher, times(1)).publicarEventoInventarioCambiado(1L, 40);
    }

    @Test
    void actualizarCantidadPorCompra_StockInsuficiente_DeberiaLanzarExcepcion() {
        // Arrange
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Optional.of(inventario));

        // Act & Assert
        assertThrows(InventarioServiceException.class, () -> {
            inventarioService.actualizarCantidadPorCompra(1L, 100); // Solo hay 50
        });
        verify(inventarioRepository, never()).save(any());
        verify(eventPublisher, never()).publicarEventoInventarioCambiado(any(), any());
    }

    @Test
    void actualizarCantidadPorCompra_CantidadInvalida_DeberiaLanzarExcepcion() {
        // Arrange
        Integer cantidadInvalida = 0;

        // Act & Assert
        assertThrows(InventarioServiceException.class, () -> {
            inventarioService.actualizarCantidadPorCompra(1L, cantidadInvalida);
        });
        verify(inventarioRepository, never()).save(any());
        verify(eventPublisher, never()).publicarEventoInventarioCambiado(any(), any());
    }

    @Test
    void crearInventario_ProductoExistente_DeberiaCrearCorrectamente() {
        // Arrange
        when(productoServiceClient.obtenerProducto(1L)).thenReturn(productoResponse);
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Optional.empty());
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);

        // Act
        Inventario resultado = inventarioService.crearInventario(inventario);

        // Assert
        assertNotNull(resultado);
        verify(inventarioRepository, times(1)).save(inventario);
        verify(eventPublisher, times(1)).publicarEventoInventarioCambiado(1L, 50);
    }

    @Test
    void crearInventario_ProductoNoExistente_DeberiaLanzarExcepcion() {
        // Arrange
        when(productoServiceClient.obtenerProducto(99L)).thenReturn(null);

        Inventario inventarioInvalido = new Inventario(99L, 10);

        // Act & Assert
        assertThrows(InventarioServiceException.class, () -> {
            inventarioService.crearInventario(inventarioInvalido);
        });
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void crearInventario_InventarioYaExiste_DeberiaLanzarExcepcion() {
        // Arrange
        when(productoServiceClient.obtenerProducto(1L)).thenReturn(productoResponse);
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Optional.of(inventario));

        // Act & Assert
        assertThrows(InventarioServiceException.class, () -> {
            inventarioService.crearInventario(inventario);
        });
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void crearInventario_CantidadNegativa_DeberiaLanzarExcepcion() {
        // Arrange
        when(productoServiceClient.obtenerProducto(1L)).thenReturn(productoResponse);
        
        Inventario inventarioNegativo = new Inventario(1L, -5);

        // Act & Assert
        assertThrows(InventarioServiceException.class, () -> {
            inventarioService.crearInventario(inventarioNegativo);
        });
        verify(inventarioRepository, never()).save(any());
    }
}