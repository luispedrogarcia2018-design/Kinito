package com.kinito;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación Kinito.
 * Al correr esta clase, Spring Boot levanta:
 *  - El servidor web (por defecto en el puerto 8080)
 *  - La base de datos H2 (archivo local, se crea sola)
 *  - Los controladores REST para el frontend
 */
@SpringBootApplication
public class KinitoApplication {
    public static void main(String[] args) {
        SpringApplication.run(KinitoApplication.class, args);
    }
}
