package com.linktic.microservice.inventario.event;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventarioCambiadoEvent extends ApplicationEvent {
    private final Long productoId;
    private final Integer nuevaCantidad;
    
    public InventarioCambiadoEvent(Object source, Long productoId, Integer nuevaCantidad) {
        super(source);
        this.productoId = productoId;
        this.nuevaCantidad = nuevaCantidad;
    }

}