package com.alsazon.entidades;
import jakarta.persistence.*;

@Entity
@Table(name = "Administradores")
public class Administrador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_admin")
    private Integer idAdmin;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 120)
    private String correo;

    @Column(name = "pin_acceso", nullable = false)
    private String pinAcceso;

    public Integer getIdAdmin() { return idAdmin; }
    public void setIdAdmin(Integer idAdmin) { this.idAdmin = idAdmin; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getPinAcceso() { return pinAcceso; }
    public void setPinAcceso(String pinAcceso) { this.pinAcceso = pinAcceso; }
}