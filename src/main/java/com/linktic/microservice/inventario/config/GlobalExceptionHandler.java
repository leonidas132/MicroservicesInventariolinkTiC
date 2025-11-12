package com.linktic.microservice.inventario.config;

import com.linktic.microservice.inventario.exception.InventarioServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja excepciones de negocio del servicio de inventario
     */
    @ExceptionHandler(InventarioServiceException.class)
    public ResponseEntity<Map<String, Object>> handleInventarioServiceException(InventarioServiceException ex) {
        logger.warn("Excepción de negocio en inventario: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = crearRespuestaError(
            "400", 
            "Error de negocio", 
            ex.getMessage()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja excepciones cuando no se puede conectar al servicio de productos
     */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, Object>> handleResourceAccessException(ResourceAccessException ex) {
        logger.error("Error de conexión con el servicio de productos: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = crearRespuestaError(
            "503", 
            "Servicio no disponible", 
            "El servicio de productos no está disponible en este momento. Por favor, intente más tarde."
        );
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    /**
     * Maneja excepciones de validación de datos de entrada
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Datos de entrada inválidos: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = crearRespuestaError(
            "400", 
            "Solicitud inválida", 
            ex.getMessage()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja excepciones cuando no se encuentra un recurso
     */
    @ExceptionHandler(org.springframework.web.client.HttpClientErrorException.NotFound.class)
    public ResponseEntity<Map<String, Object>> handleProductoNoEncontrado(org.springframework.web.client.HttpClientErrorException.NotFound ex) {
        logger.warn("Producto no encontrado en servicio externo: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = crearRespuestaError(
            "404", 
            "Producto no encontrado", 
            "El producto solicitado no existe en el sistema"
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Maneja excepciones genéricas no capturadas por otros manejadores
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        logger.error("Error interno no manejado: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = crearRespuestaError(
            "500", 
            "Error interno del servidor", 
            "Ocurrió un error inesperado en el sistema. Por favor, contacte al administrador."
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Método auxiliar para crear respuestas de error en formato JSON API
     */
    private Map<String, Object> crearRespuestaError(String status, String title, String detail) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", status);
        error.put("title", title);
        error.put("detail", detail);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("errors", new Object[]{error});
        
        return errorResponse;
    }
}
