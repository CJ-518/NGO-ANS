package com.ngo.sistema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación NGO SAECA (gestión de garantías, servicio técnico y ventas).
 * @SpringBootApplication activa la configuración automática y escanea este paquete
 * (com.ngo.sistema) en busca de controladores, servicios, repositorios y configuraciones.
 */
@SpringBootApplication
public class SistemaApplication {

	/**
	 * Arranca el servidor web embebido (por defecto en el puerto 8080).
	 */
	public static void main(String[] args) {
		SpringApplication.run(SistemaApplication.class, args);
	}

}
