package com.linktic.microservice.inventario.exception;

public class InventarioServiceException extends RuntimeException {
    
	private static final long serialVersionUID = 1L;
	
    public InventarioServiceException(String message) {
        super(message);
    }
    
    public InventarioServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}