package com.tienda.ropa.service;

import com.tienda.ropa.dto.ClienteForm;
import com.tienda.ropa.entity.Cliente;
import com.tienda.ropa.entity.EstadoGeneral;
import com.tienda.ropa.exception.RecursoNoEncontradoException;
import com.tienda.ropa.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Mismo patron que ProductoService/ProveedorService: crear/editar/listar/desactivar.
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public List<Cliente> listarActivos() {
        return clienteRepository.findByEstado(EstadoGeneral.ACTIVO);
    }

    @Transactional(readOnly = true)
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado (id=" + id + ")"));
    }

    @Transactional
    public Cliente crear(ClienteForm form) {
        Cliente cliente = new Cliente();
        aplicarFormulario(cliente, form);
        return clienteRepository.save(cliente);
    }

    @Transactional
    public Cliente actualizar(Long id, ClienteForm form) {
        Cliente cliente = buscarPorId(id);
        aplicarFormulario(cliente, form);
        return clienteRepository.save(cliente);
    }

    @Transactional
    public void desactivar(Long id) {
        Cliente cliente = buscarPorId(id);
        cliente.setEstado(EstadoGeneral.INACTIVO);
        clienteRepository.save(cliente);
    }

    private void aplicarFormulario(Cliente cliente, ClienteForm form) {
        cliente.setNombre(form.getNombre().trim());
        cliente.setApellido(form.getApellido());
        cliente.setNit(form.getNit());
        cliente.setTelefono(form.getTelefono());
        cliente.setCorreo(form.getCorreo());
        cliente.setDireccion(form.getDireccion());
    }
}
