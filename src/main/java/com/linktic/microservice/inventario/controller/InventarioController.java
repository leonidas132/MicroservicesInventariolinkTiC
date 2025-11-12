package com.linktic.microservice.inventario.controller;

import com.linktic.microservice.inventario.model.entity.Inventario;
import com.linktic.microservice.inventario.service.InventarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/inventarios")
public class InventarioController {
    
    @Autowired
    private InventarioService inventarioService;
    
    /**
     * Consultar la cantidad disponible de un producto específico por ID
     * Obtiene la información del producto llamando al microservicio de productos
     */
    @GetMapping("/{productoId}")
    public ResponseEntity<Map<String, Object>> consultarCantidadDisponible(@PathVariable Long productoId) {
        try {
            Map<String, Object> inventarioCompleto = inventarioService.consultarCantidadDisponible(productoId);
            
            Map<String, Object> respuesta = new HashMap<>();
            Map<String, Object> datos = new HashMap<>();
            datos.put("type", "inventarios");
            datos.put("id", productoId);
            datos.put("attributes", inventarioCompleto);
            
            respuesta.put("data", datos);
            return ResponseEntity.ok(respuesta);
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(crearRespuestaError(e.getMessage(), "404"));
        }
    }
    
    /**
     * Actualizar la cantidad disponible de un producto específico tras una compra
     */
    @PutMapping("/{productoId}/compra")
    public ResponseEntity<Map<String, Object>> actualizarCantidadPorCompra(
            @PathVariable Long productoId, 
            @RequestParam Integer cantidadComprada) {
        
        try {
            Inventario inventarioActualizado = inventarioService.actualizarCantidadPorCompra(productoId, cantidadComprada);
            return ResponseEntity.ok(crearRespuestaJsonApi(inventarioActualizado));
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(crearRespuestaError(e.getMessage(), "400"));
        }
    }
    
    /**
     * Crear nuevo registro de inventario
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> crearInventario(@Valid @RequestBody Inventario inventario) {
        try {
            Inventario inventarioCreado = inventarioService.crearInventario(inventario);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(crearRespuestaJsonApi(inventarioCreado));
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(crearRespuestaError(e.getMessage(), "400"));
        }
    }
    
    /**
     * Actualizar cantidad en inventario
     */
    @PutMapping("/{productoId}")
    public ResponseEntity<Map<String, Object>> actualizarInventario(
            @PathVariable Long productoId, 
            @RequestParam Integer cantidad) {
        
        try {
            Inventario inventarioActualizado = inventarioService.actualizarInventario(productoId, cantidad);
            return ResponseEntity.ok(crearRespuestaJsonApi(inventarioActualizado));
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(crearRespuestaError(e.getMessage(), "404"));
        }
    }
    
    private Map<String, Object> crearRespuestaJsonApi(Inventario inventario) {
        Map<String, Object> respuesta = new HashMap<>();
        Map<String, Object> datos = new HashMap<>();
        datos.put("type", "inventarios");
        datos.put("id", inventario.getProductoId());
        
        Map<String, Object> atributos = new HashMap<>();
        atributos.put("producto_id", inventario.getProductoId());
        atributos.put("cantidad", inventario.getCantidad());
        
        datos.put("attributes", atributos);
        respuesta.put("data", datos);
        return respuesta;
    }
    
    private Map<String, Object> crearRespuestaError(String detalle, String status) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", status);
        error.put("title", "Error");
        error.put("detail", detalle);
        
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("errors", new Object[]{error});
        return respuesta;
    }
}
