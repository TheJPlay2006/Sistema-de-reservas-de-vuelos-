/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author jh599
 */
// modelo/Vuelo.java

import java.time.LocalDateTime;

public class Vuelo {
    private int idVuelo;
    private Aerolinea aerolinea;
    private String numeroVuelo;
    private String origen;
    private String destino;
    private LocalDateTime fechaSalida;
    private LocalDateTime fechaLlegada;
    private int asientosTotales;
    private int asientosDisponibles;
    private double precio;
    private int escalas;
    private String estado;

    // Constructor vacío
    public Vuelo() {}

    // Constructor con parámetros
    public Vuelo(int idVuelo, Aerolinea aerolinea, String numeroVuelo, String origen,
                 String destino, LocalDateTime fechaSalida, LocalDateTime fechaLlegada,
                 int asientosTotales, int asientosDisponibles, double precio, int escalas, String estado) {
        this.idVuelo = idVuelo;
        this.aerolinea = aerolinea;
        this.numeroVuelo = numeroVuelo;
        this.origen = origen;
        this.destino = destino;
        this.fechaSalida = fechaSalida;
        this.fechaLlegada = fechaLlegada;
        this.asientosTotales = asientosTotales;
        this.asientosDisponibles = asientosDisponibles;
        this.precio = precio;
        this.escalas = escalas;
        this.estado = estado;
    }

    // Getters y Setters
    public int getIdVuelo() {
        return idVuelo;
    }

    public void setIdVuelo(int idVuelo) {
        this.idVuelo = idVuelo;
    }

    public Aerolinea getAerolinea() {
        return aerolinea;
    }

    public void setAerolinea(Aerolinea aerolinea) {
        this.aerolinea = aerolinea;
    }

    public String getNumeroVuelo() {
        return numeroVuelo;
    }

    public void setNumeroVuelo(String numeroVuelo) {
        this.numeroVuelo = numeroVuelo;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public LocalDateTime getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDateTime fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public LocalDateTime getFechaLlegada() {
        return fechaLlegada;
    }

    public void setFechaLlegada(LocalDateTime fechaLlegada) {
        this.fechaLlegada = fechaLlegada;
    }

    public int getAsientosTotales() {
        return asientosTotales;
    }

    public void setAsientosTotales(int asientosTotales) {
        this.asientosTotales = asientosTotales;
    }

    public int getAsientosDisponibles() {
        return asientosDisponibles;
    }

    public void setAsientosDisponibles(int asientosDisponibles) {
        this.asientosDisponibles = asientosDisponibles;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getEscalas() {
        return escalas;
    }

    public void setEscalas(int escalas) {
        this.escalas = escalas;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Vuelo{" +
                "id=" + idVuelo +
                ", aerolínea=" + aerolinea.getNombre() +
                ", número='" + numeroVuelo + '\'' +
                ", " + origen + " → " + destino +
                ", salida=" + fechaSalida +
                ", asientosDisp=" + asientosDisponibles +
                ", precio=" + precio +
                '}';
    }
}
