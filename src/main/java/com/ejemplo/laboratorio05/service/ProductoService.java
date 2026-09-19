package com.ejemplo.laboratorio05.service;

import com.ejemplo.laboratorio05.exception.RecursoNoEncontradoException;
import com.ejemplo.laboratorio05.model.Producto;
import com.ejemplo.laboratorio05.repository.ProductoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el producto con id " + id));
    }

    @Transactional
    public Producto crear(Producto producto) {
        producto.setId(null);
        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizar(Long id, Producto datos) {
        Producto existente = buscarPorId(id);
        existente.setNombre(datos.getNombre());
        existente.setCategoria(datos.getCategoria());
        existente.setPrecio(datos.getPrecio());
        existente.setStock(datos.getStock());
        return productoRepository.save(existente);
    }

    @Transactional
    public void eliminar(Long id) {
        Producto existente = buscarPorId(id);
        productoRepository.delete(existente);
    }

    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }
}
