package com.alsazon.entidades;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Flujo_Caja")
public class FlujoCaja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Integer idMovimiento;

    @Column(name = "tipo_movimiento", nullable = false, length = 50)
    private String tipoMovimiento;

    @Column(nullable = false)
    private Double monto;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Column(nullable = false)
    private String concepto;

    @Column(name = "efectivo_declarado")
    private Double efectivoDeclarado;

    private Double descuadre;

    public Integer getIdMovimiento() { return idMovimiento; }
    public void setIdMovimiento(Integer idMovimiento) { this.idMovimiento = idMovimiento; }
    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }
    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public Double getEfectivoDeclarado() { return efectivoDeclarado; }
    public void setEfectivoDeclarado(Double efectivoDeclarado) { this.efectivoDeclarado = efectivoDeclarado; }
    public Double getDescuadre() { return descuadre; }
    public void setDescuadre(Double descuadre) { this.descuadre = descuadre; }
}