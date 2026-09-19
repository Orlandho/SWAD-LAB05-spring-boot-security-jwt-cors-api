package com.ejemplo.laboratorio05.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres")
    @Column(nullable = false, length = 80)
    private String nombre;

    @NotBlank(message = "La categoría es obligatoria")
    @Size(min = 3, max = 40, message = "La categoría debe tener entre 3 y 40 caracteres")
    @Column(nullable = false, length = 40)
    private String categoria;

    @Positive(message = "El precio debe ser mayor que cero")
    @Column(nullable = false)
    private double precio;

    @PositiveOrZero(message = "El stock no puede ser negativo")
    @Column(nullable = false)
    private int stock;

    public Producto() {
    }

    public Producto(Long id, String nombre, String categoria, double precio, int stock) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
    }

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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    @Transient
    @JsonProperty("estadoStock")
    public String getEstadoStock() {
        return stock < 5 ? "INSUFICIENTE" : "OK";
    }
}
