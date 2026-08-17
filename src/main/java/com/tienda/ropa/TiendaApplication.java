package com.tienda.ropa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicacion. Esta es la clase que se ejecuta
 * para levantar el servidor (arranca en el puerto configurado en
 * application.properties, por defecto 8080).
 */
@SpringBootApplication
public class TiendaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TiendaApplication.class, args);
    }
}
