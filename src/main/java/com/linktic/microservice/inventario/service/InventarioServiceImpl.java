package com.linktic.microservice.inventario.service;

import com.linktic.microservice.inventario.client.ProductoServiceClient;
import com.linktic.microservice.inventario.model.dto.ProductoResponse;
import com.linktic.microservice.inventario.event.InventarioEventPublisher;
import com.linktic.microservice.inventario.exception.InventarioServiceException;
import com.linktic.microservice.inventario.model.entity.Inventario;
import com.linktic.microservice.inventario.repository.InventarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class InventarioServiceImpl implements InventarioService {

    private static final Logger logger = LoggerFactory.getLogger(InventarioServiceImpl.class);

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private ProductoServiceClient productoServiceClient;

    @Autowired
    private InventarioEventPublisher eventPublisher;

    /**
     * Consultar la cantidad disponible de un producto específico por ID
     * Obtiene la información del producto llamando al microservicio de productos
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> consultarCantidadDisponible(Long productoId) {
        logger.info("Consultando cantidad disponible para producto ID: {}", productoId);
        
        ProductoResponse productoResponse = productoServiceClient.obtenerProducto(productoId);
        if (productoResponse == null) {
            throw new InventarioServiceException("Producto no encontrado con ID: " + productoId);
        }

        Optional<Inventario> inventario = inventarioRepository.findByProductoId(productoId);
        Integer cantidadDisponible = inventario.map(Inventario::getCantidad).orElse(0);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("producto_id", productoId);
        respuesta.put("producto_nombre", productoResponse.getData().getAttributes().getNombre());
        respuesta.put("producto_descripcion", productoResponse.getData().getAttributes().getDescripcion());
        respuesta.put("producto_precio", productoResponse.getData().getAttributes().getPrecio());
        respuesta.put("cantidad_disponible", cantidadDisponible);
        respuesta.put("disponible", cantidadDisponible > 0);
        respuesta.put("existe_en_inventario", inventario.isPresent());

        logger.info("Consulta completada - Producto: {}, Cantidad: {}", 
                   productoResponse.getData().getAttributes().getNombre(), 
                   cantidadDisponible);

        return respuesta;
    }

    /**
     * Actualizar la cantidad disponible de un producto específico tras una compra
     */
    @Override
    public Inventario actualizarCantidadPorCompra(Long productoId, Integer cantidadComprada) {
        logger.info("Actualizando inventario por compra - Producto ID: {}, Cantidad comprada: {}", 
                   productoId, cantidadComprada);
        
        if (cantidadComprada <= 0) {
            throw new InventarioServiceException("La cantidad comprada debe ser mayor a 0");
        }
        
        Optional<Inventario> inventarioOpt = inventarioRepository.findByProductoId(productoId);
        Inventario inventario;

        if (inventarioOpt.isPresent()) {
            inventario = inventarioOpt.get();
            int nuevaCantidad = inventario.getCantidad() - cantidadComprada;
           
            if (nuevaCantidad < 0) {
                throw new InventarioServiceException(
                    "Stock insuficiente. Disponible: " + inventario.getCantidad() + 
                    ", Solicitado: " + cantidadComprada
                );
            }
            
            inventario.setCantidad(nuevaCantidad);
        } else {
            throw new InventarioServiceException("No existe inventario para el producto con ID: " + productoId);
        }
        
        Inventario inventarioActualizado = inventarioRepository.save(inventario);
        
        eventPublisher.publicarEventoInventarioCambiado(productoId, inventarioActualizado.getCantidad());
        
        logger.info("Inventario actualizado exitosamente - Producto ID: {}, Nueva cantidad: {}", 
                   productoId, inventarioActualizado.getCantidad());

        return inventarioActualizado;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Inventario> obtenerInventarioPorProductoId(Long productoId) {
        return inventarioRepository.findByProductoId(productoId);
    }

    @Override
    public Inventario crearInventario(Inventario inventario) {

        ProductoResponse producto = productoServiceClient.obtenerProducto(inventario.getProductoId());
        if (producto == null) {
            throw new InventarioServiceException("El producto con ID " + inventario.getProductoId() + " no existe");
        }

        if (inventarioRepository.findByProductoId(inventario.getProductoId()).isPresent()) {
            throw new InventarioServiceException("Ya existe inventario para este producto");
        }

        if (inventario.getCantidad() < 0) {
            throw new InventarioServiceException("La cantidad no puede ser negativa");
        }

        Inventario inventarioCreado = inventarioRepository.save(inventario);
        
        eventPublisher.publicarEventoInventarioCambiado(inventarioCreado.getProductoId(), inventarioCreado.getCantidad());
        
        return inventarioCreado;
    }

    @Override
    public Inventario actualizarInventario(Long productoId, Integer nuevaCantidad) {

        if (nuevaCantidad < 0) {
            throw new InventarioServiceException("La cantidad no puede ser negativa");
        }

        Inventario inventario = inventarioRepository.findByProductoId(productoId)
                .orElseThrow(() -> new InventarioServiceException("Inventario no encontrado"));

        int cantidadAnterior = inventario.getCantidad();
        inventario.setCantidad(nuevaCantidad);
        Inventario inventarioActualizado = inventarioRepository.save(inventario);
        
        if (cantidadAnterior != nuevaCantidad) {
            eventPublisher.publicarEventoInventarioCambiado(productoId, nuevaCantidad);
        }
        
        return inventarioActualizado;
    }

    @Override
    public void eliminarInventario(Long productoId) {
        Inventario inventario = inventarioRepository.findByProductoId(productoId)
                .orElseThrow(() -> new InventarioServiceException("Inventario no encontrado"));
        
        inventarioRepository.delete(inventario);
        
        eventPublisher.publicarEventoInventarioCambiado(productoId, 0);
    }
}