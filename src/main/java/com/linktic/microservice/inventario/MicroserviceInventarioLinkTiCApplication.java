package com.linktic.microservice.inventario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class MicroserviceInventarioLinkTiCApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroserviceInventarioLinkTiCApplication.class, args);
	}

}
