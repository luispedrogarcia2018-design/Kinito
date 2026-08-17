package com.kinito.controller;

import com.kinito.model.*;
import com.kinito.repository.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Maneja los pedidos reales: un cliente eligiendo productos y cantidades.
 * GET  /api/pedidos   -> lista todos los pedidos (uso administrativo)
 * POST /api/pedidos   -> el frontend manda el carrito aquí
 */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private ProductoRepository productoRepository;

    @GetMapping
    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> crearPedido(@Valid @RequestBody PedidoRequest request) {
        // 1. Cliente: lo busca por email o lo crea si es nuevo
        Cliente cliente = clienteRepository.findByEmail(request.email())
                .orElseGet(() -> clienteRepository.save(
                        new Cliente(request.nombre(), request.email(), request.telefono(), request.direccion())
                ));

        // 2. Arma el pedido y sus items, validando stock
        Pedido pedido = new Pedido(cliente);
        BigDecimal total = BigDecimal.ZERO;

        for (ItemRequest itemReq : request.items()) {
            Optional<Producto> productoOpt = productoRepository.findById(itemReq.productoId());
            if (productoOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Producto no encontrado: id " + itemReq.productoId());
            }
            Producto producto = productoOpt.get();

            if (producto.getStock() < itemReq.cantidad()) {
                return ResponseEntity.badRequest().body("No hay stock suficiente de: " + producto.getNombre());
            }

            ItemPedido item = new ItemPedido(pedido, producto, itemReq.cantidad(), producto.getPrecio());
            pedido.getItems().add(item);

            total = total.add(producto.getPrecio().multiply(BigDecimal.valueOf(itemReq.cantidad())));

            // Descuenta del inventario
            producto.setStock(producto.getStock() - itemReq.cantidad());
            productoRepository.save(producto);
        }

        pedido.setTotal(total);
        Pedido guardado = pedidoRepository.save(pedido);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    public record ItemRequest(
            @NotNull(message = "Falta el id del producto") Long productoId,
            @NotNull @Min(value = 1, message = "La cantidad debe ser al menos 1") Integer cantidad
    ) {
    }

    public record PedidoRequest(
            @NotBlank(message = "El nombre es obligatorio") String nombre,
            @NotBlank(message = "El email es obligatorio") @Email(message = "Email inválido") String email,
            String telefono,
            String direccion,
            @NotEmpty(message = "El pedido debe tener al menos un producto") List<ItemRequest> items
    ) {
    }
}
