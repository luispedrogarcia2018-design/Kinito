package com.kinito.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Representa un mensaje que llega desde el formulario de contacto
 * de la página (puede ser una pregunta o un pedido).
 */
@Entity
@Table(name = "contacto")
public class Contacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String email;

    private String telefono;

    @Column(nullable = false, length = 1000)
    private String mensaje;

    // "contacto" o "pedido"
    private String tipo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    // Relación opcional: a qué cliente pertenece este mensaje
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    public Contacto() {
    }

    public Contacto(String nombre, String email, String telefono, String mensaje, String tipo) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.mensaje = mensaje;
        this.tipo = tipo;
        this.fecha = LocalDateTime.now();
    }

    // Getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
}
