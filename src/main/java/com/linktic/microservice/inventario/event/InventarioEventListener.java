package com.linktic.microservice.inventario.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class InventarioEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(InventarioEventListener.class);
    
    @EventListener
    public void manejarInventarioCambiado(InventarioCambiadoEvent event) {
        logger.info("EVENTO: Inventario cambiado - Producto ID: {}, Nueva cantidad: {}", 
                   event.getProductoId(), event.getNuevaCantidad());
        
        logger.info("==== EVENTO DE INVENTARIO ====");
        logger.info("Producto ID: " + event.getProductoId());
        logger.info("Nueva cantidad: " + event.getNuevaCantidad());
        logger.info("Timestamp: " + java.time.LocalDateTime.now());
        logger.info("==============================");
    }
}
