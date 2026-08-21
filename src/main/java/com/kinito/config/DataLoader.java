package com.kinito.config;

import com.kinito.model.*;
import com.kinito.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

/**
 * Al arrancar la aplicación, si no hay categorías todavía,
 * carga categorías, productos y un par de pedidos de ejemplo
 * para poder probar la página con datos reales.
 */
@Configuration
public class DataLoader implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;

    public DataLoader(CategoriaRepository categoriaRepository, ProductoRepository productoRepository,
                       ClienteRepository clienteRepository, PedidoRepository pedidoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    public void run(String... args) {
        if (categoriaRepository.count() > 0) {
            return; // ya hay datos, no duplicar
        }

        // ===== Categorías =====
        Categoria camisas = categoriaRepository.save(new Categoria("Camisas", "Camisas de uso diario y formales"));
        Categoria pantalones = categoriaRepository.save(new Categoria("Pantalones", "Jeans y pantalones de vestir"));
        Categoria vestidos = categoriaRepository.save(new Categoria("Vestidos", "Vestidos casuales y de fiesta"));
        Categoria sudaderas = categoriaRepository.save(new Categoria("Sudaderas", "Sudaderas y hoodies"));
        Categoria faldas = categoriaRepository.save(new Categoria("Faldas", "Faldas cortas y midi"));
        Categoria chamarras = categoriaRepository.save(new Categoria("Chamarras", "Chamarras y chaquetas"));

        // ===== Inventario =====
        Producto camisaOxford = productoRepository.save(new Producto(
                "Camisa Oxford Clásica", "Camisa de algodón, corte regular, ideal para diario u oficina.",
                camisas, "M", "Blanco", new BigDecimal("249.00"), 15,
                "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=500"));

        Producto jeanSlim = productoRepository.save(new Producto(
                "Jean Slim Fit", "Pantalón de mezclilla stretch, corte entubado.",
                pantalones, "32", "Azul oscuro", new BigDecimal("399.00"), 20,
                "https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=500"));

        Producto vestidoFloral = productoRepository.save(new Producto(
                "Vestido Midi Floral", "Vestido midi de tela ligera, estampado floral, ideal para primavera.",
                vestidos, "S", "Multicolor", new BigDecimal("459.00"), 10,
                "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=500"));

        Producto sudaderaOversize = productoRepository.save(new Producto(
                "Sudadera Oversize", "Sudadera con capucha, fit amplio, felpa interior suave.",
                sudaderas, "L", "Gris", new BigDecimal("329.00"), 25,
                "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=500"));

        Producto faldaPlisada = productoRepository.save(new Producto(
                "Falda Plisada", "Falda midi plisada, cintura alta, cae con movimiento.",
                faldas, "M", "Negro", new BigDecimal("289.00"), 12,
                "https://images.unsplash.com/photo-1583496661160-fb5886a1f101?w=500"));

        Producto chamarraBomber = productoRepository.save(new Producto(
                "Chamarra Bomber", "Chamarra estilo bomber, resistente al viento, forro interior.",
                chamarras, "L", "Verde olivo", new BigDecimal("599.00"), 8,
                "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500"));

        // ===== Clientes y pedidos de ejemplo, para ver el flujo completo =====
        Cliente clienteDemo = clienteRepository.save(
                new Cliente("María Pérez", "maria.demo@ejemplo.com", "5555-1234", "Zona 10, Ciudad"));

        Pedido pedidoDemo = new Pedido(clienteDemo);
        pedidoDemo.setEstado("CONFIRMADO");

        ItemPedido item1 = new ItemPedido(pedidoDemo, camisaOxford, 2, camisaOxford.getPrecio());
        ItemPedido item2 = new ItemPedido(pedidoDemo, jeanSlim, 1, jeanSlim.getPrecio());
        pedidoDemo.setItems(List.of(item1, item2));

        BigDecimal total = camisaOxford.getPrecio().multiply(BigDecimal.valueOf(2))
                .add(jeanSlim.getPrecio());
        pedidoDemo.setTotal(total);

        pedidoRepository.save(pedidoDemo);

        System.out.println(">>> Kinito: datos de ejemplo cargados (" + productoRepository.count() + " productos, "
                + categoriaRepository.count() + " categorías, 1 pedido demo).");
    }
}
