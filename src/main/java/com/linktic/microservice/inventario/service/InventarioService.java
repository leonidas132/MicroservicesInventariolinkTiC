package com.linktic.microservice.inventario.service;

import com.linktic.microservice.inventario.model.entity.Inventario;
import java.util.Map;
import java.util.Optional;

public interface InventarioService {
    
    // Consultar cantidad disponible con información del producto
    Map<String, Object> consultarCantidadDisponible(Long productoId);
    
    // Actualizar cantidad tras una compra
    Inventario actualizarCantidadPorCompra(Long productoId, Integer cantidadComprada);
    
    // Operaciones básicas adicionales
    Optional<Inventario> obtenerInventarioPorProductoId(Long productoId);
    Inventario crearInventario(Inventario inventario);
    Inventario actualizarInventario(Long productoId, Integer nuevaCantidad);
    void eliminarInventario(Long productoId);
}
