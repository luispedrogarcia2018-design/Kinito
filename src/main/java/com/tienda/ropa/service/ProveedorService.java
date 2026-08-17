package com.tienda.ropa.service;

import com.tienda.ropa.dto.ProveedorForm;
import com.tienda.ropa.entity.EstadoGeneral;
import com.tienda.ropa.entity.Proveedor;
import com.tienda.ropa.exception.RecursoNoEncontradoException;
import com.tienda.ropa.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Igual patron que ProductoService: crear/editar/listar/desactivar (eliminacion
// logica). Un proveedor con entradas asociadas nunca se borra fisicamente.
@Service
@RequiredArgsConstructor
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    @Transactional(readOnly = true)
    public List<Proveedor> listarActivos() {
        return proveedorRepository.findByEstado(EstadoGeneral.ACTIVO);
    }

    @Transactional(readOnly = true)
    public Proveedor buscarPorId(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado (id=" + id + ")"));
    }

    @Transactional
    public Proveedor crear(ProveedorForm form) {
        Proveedor proveedor = new Proveedor();
        aplicarFormulario(proveedor, form);
        return proveedorRepository.save(proveedor);
    }

    @Transactional
    public Proveedor actualizar(Long id, ProveedorForm form) {
        Proveedor proveedor = buscarPorId(id);
        aplicarFormulario(proveedor, form);
        return proveedorRepository.save(proveedor);
    }

    @Transactional
    public void desactivar(Long id) {
        Proveedor proveedor = buscarPorId(id);
        proveedor.setEstado(EstadoGeneral.INACTIVO);
        proveedorRepository.save(proveedor);
    }

    private void aplicarFormulario(Proveedor proveedor, ProveedorForm form) {
        proveedor.setNombre(form.getNombre().trim());
        proveedor.setEmpresa(form.getEmpresa().trim());
        proveedor.setNit(form.getNit());
        proveedor.setTelefono(form.getTelefono());
        proveedor.setCorreo(form.getCorreo());
        proveedor.setDireccion(form.getDireccion());
        proveedor.setObservaciones(form.getObservaciones());
    }
}
