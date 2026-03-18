package com.example.demostracion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "novedades")
public class Novedad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idNovedades")
    private Long idNovedades;

    @Column(name = "TipoNovedad", nullable = false, length = 100)
    private String tipoNovedad;

    @Column(name = "Descripcion", columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @Column(name = "Evidencia")
    private String evidencia; // Aquí solo guardamos el nombre del archivo

    @Column(name = "Estado", nullable = false, length = 50)
    private String estado;

    @ManyToOne
    @JoinColumn(name = "Id_Usuario", referencedColumnName = "id_Usuario")
    private Usuario usuario;

    // ✅ Getters y Setters
    public Long getIdNovedades() {
        return idNovedades;
    }

    public void setIdNovedades(Long idNovedades) {
        this.idNovedades = idNovedades;
    }

    public String getTipoNovedad() {
        return tipoNovedad;
    }

    public void setTipoNovedad(String tipoNovedad) {
        this.tipoNovedad = tipoNovedad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEvidencia() {
        return evidencia;
    }

    public void setEvidencia(String evidencia) {
        this.evidencia = evidencia;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
