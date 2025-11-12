package com.linktic.microservice.inventario.model.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductoResponse {
	private ResponseData data;

    @Data
    public static class ResponseData {
        private String type;
        private Long id;
        private Attributes attributes;
    }

    @Data
    public static class Attributes {
        private String nombre;
        private String descripcion;
        private BigDecimal precio;
    }

}
