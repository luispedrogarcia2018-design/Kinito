package com.kinito.controller;

import com.kinito.model.Cliente;
import com.kinito.model.Contacto;
import com.kinito.repository.ClienteRepository;
import com.kinito.repository.ContactoRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Recibe los mensajes del formulario de "Contacto / Pedido" de la página.
 * Cada envío:
 *  1. Busca si el cliente ya existe por email; si no, lo crea.
 *  2. Guarda el mensaje/pedido en la tabla "contacto", ligado a ese cliente.
 *
 * GET  /api/contacto   -> lista todos los mensajes recibidos (uso administrativo)
 * POST /api/contacto   -> el formulario del frontend envía aquí
 * GET  /api/clientes   -> lista todos los clientes registrados (uso administrativo)
 */
@RestController
@RequestMapping("/api")
public class ContactoController {

    @Autowired
    private ContactoRepository contactoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping("/contacto")
    public List<Contacto> listarContactos() {
        return contactoRepository.findAll();
    }

    @GetMapping("/clientes")
    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    @PostMapping("/contacto")
    public ResponseEntity<Contacto> recibirContacto(@Valid @RequestBody ContactoRequest request) {
        // Busca al cliente por email, o lo crea si es la primera vez que escribe
        Optional<Cliente> clienteExistente = clienteRepository.findByEmail(request.email());
        Cliente cliente = clienteExistente.orElseGet(() -> clienteRepository.save(
                new Cliente(request.nombre(), request.email(), request.telefono(), request.direccion())
        ));

        Contacto contacto = new Contacto(
                request.nombre(),
                request.email(),
                request.telefono(),
                request.mensaje(),
                "contacto"
        );
        contacto.setCliente(cliente);

        Contacto guardado = contactoRepository.save(contacto);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    /**
     * Forma de los datos que manda el formulario del frontend.
     * La dirección es opcional porque no todos los mensajes son pedidos.
     */
    public record ContactoRequest(
            @NotBlank(message = "El nombre es obligatorio") String nombre,
            @NotBlank(message = "El email es obligatorio") @Email(message = "Email inválido") String email,
            String telefono,
            String direccion,
            @NotBlank(message = "El mensaje no puede estar vacío") String mensaje
    ) {
    }
}
