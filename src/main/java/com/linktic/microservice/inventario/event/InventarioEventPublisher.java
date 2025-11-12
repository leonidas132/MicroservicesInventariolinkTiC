package com.linktic.microservice.inventario.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class InventarioEventPublisher {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public InventarioEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    public void publicarEventoInventarioCambiado(Long productoId, Integer nuevaCantidad) {
        InventarioCambiadoEvent event = new InventarioCambiadoEvent(this, productoId, nuevaCantidad);
        eventPublisher.publishEvent(event);
    }
}