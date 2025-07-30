/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author jh599
 */

// modelo/Aerolinea
public class Aerolinea {
    private int idAerolinea;
    private String nombre;
    private String codigo;

    // Constructor vacío
    public Aerolinea() {}

    // Constructor con parámetros
    public Aerolinea(int idAerolinea, String nombre, String codigo) {
        this.idAerolinea = idAerolinea;
        this.nombre = nombre;
        this.codigo = codigo;
    }

    // Getters y Setters
    public int getIdAerolinea() {
        return idAerolinea;
    }

    public void setIdAerolinea(int idAerolinea) {
        this.idAerolinea = idAerolinea;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    @Override
    public String toString() {
        return "Aerolinea{" +
                "id=" + idAerolinea +
                ", nombre='" + nombre + '\'' +
                ", codigo='" + codigo + '\'' +
                '}';
    }
}