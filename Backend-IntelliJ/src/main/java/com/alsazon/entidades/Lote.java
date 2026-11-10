package com.alsazon.entidades;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Lotes")
public class Lote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lote")
    private Integer idLote;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "cantidad_inicial", nullable = false)
    private Double cantidadInicial;

    @Column(name = "cantidad_actual", nullable = false)
    private Double cantidadActual;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "fecha_caducidad", nullable = false)
    private LocalDate fechaCaducidad;

    @Column(name = "costo_total", nullable = false)
    private Double costoTotal;

    public Integer getIdLote() { return idLote; }
    public void setIdLote(Integer idLote) { this.idLote = idLote; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public Double getCantidadInicial() { return cantidadInicial; }
    public void setCantidadInicial(Double cantidadInicial) { this.cantidadInicial = cantidadInicial; }
    public Double getCantidadActual() { return cantidadActual; }
    public void setCantidadActual(Double cantidadActual) { this.cantidadActual = cantidadActual; }
    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    public LocalDate getFechaCaducidad() { return fechaCaducidad; }
    public void setFechaCaducidad(LocalDate fechaCaducidad) { this.fechaCaducidad = fechaCaducidad; }
    public Double getCostoTotal() { return costoTotal; }
    public void setCostoTotal(Double costoTotal) { this.costoTotal = costoTotal; }
}