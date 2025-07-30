/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author jh599
 */
// modelo/Reserva.java
import java.time.LocalDateTime;

public class Reserva {
    private int idReserva;
    private Usuario usuario;
    private Vuelo vuelo;
    private LocalDateTime fechaReserva;
    private String estado;
    private int cantidadAsientos;

    // Constructor vacío
    public Reserva() {}

    // Constructor con parámetros
    public Reserva(int idReserva, Usuario usuario, Vuelo vuelo, LocalDateTime fechaReserva,
                   String estado, int cantidadAsientos) {
        this.idReserva = idReserva;
        this.usuario = usuario;
        this.vuelo = vuelo;
        this.fechaReserva = fechaReserva;
        this.estado = estado;
        this.cantidadAsientos = cantidadAsientos;
    }

    // Getters y Setters
    public int getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(int idReserva) {
        this.idReserva = idReserva;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Vuelo getVuelo() {
        return vuelo;
    }

    public void setVuelo(Vuelo vuelo) {
        this.vuelo = vuelo;
    }

    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(LocalDateTime fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getCantidadAsientos() {
        return cantidadAsientos;
    }

    public void setCantidadAsientos(int cantidadAsientos) {
        this.cantidadAsientos = cantidadAsientos;
    }

    @Override
    public String toString() {
        return "Reserva{" +
                "id=" + idReserva +
                ", usuario=" + usuario.getNombre() +
                ", vuelo=" + vuelo.getNumeroVuelo() +
                ", fecha=" + fechaReserva +
                ", asientos=" + cantidadAsientos +
                ", estado='" + estado + '\'' +
                '}';
    }
}