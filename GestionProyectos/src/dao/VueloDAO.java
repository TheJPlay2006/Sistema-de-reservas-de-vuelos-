/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author jh599
 */
// dao/VueloDAO.java
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import modelo.Aerolinea;
import modelo.Vuelo;
import java.sql.Date;
import util.ConexionBD; 

/**
 * Clase DAO para gestionar operaciones de Vuelo en la base de datos.
 * 
 * @author [Tu nombre]
 */
public class VueloDAO {

    /**
     * Busca vuelos disponibles según origen, destino y fecha de salida.
     * 
     * @param origen Origen del vuelo (ej: "Bogotá"). Puede ser null para omitir.
     * @param destino Destino del vuelo (ej: "Medellín"). Puede ser null para omitir.
     * @param fecha Fecha de salida (sin hora). Puede ser null para omitir.
     * @return Lista de vuelos que cumplen con los filtros.
     */
    public List<Vuelo> buscarVuelos(String origen, String destino, LocalDate fecha) {
        List<Vuelo> vuelos = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        // Consulta SQL con filtros opcionales
        String sql = """
            SELECT 
                v.id_vuelo, v.numero_vuelo, v.origen, v.destino,
                v.fecha_salida, v.fecha_llegada, v.asientos_totales,
                v.asientos_disponibles, v.precio, v.escalas, v.estado,
                a.id_aerolinea, a.nombre AS nombre_aerolinea, a.codigo
            FROM Vuelo v
            INNER JOIN Aerolinea a ON v.id_aerolinea = a.id_aerolinea
            WHERE 1=1
            """;

        // Construcción dinámica de condiciones
        List<Object> params = new ArrayList<>();

        if (origen != null && !origen.trim().isEmpty()) {
            sql += " AND v.origen LIKE ?";
            params.add("%" + origen.trim() + "%");
        }
        if (destino != null && !destino.trim().isEmpty()) {
            sql += " AND v.destino LIKE ?";
            params.add("%" + destino.trim() + "%");
        }
        if (fecha != null) {
            sql += " AND CAST(v.fecha_salida AS DATE) = ?";
            params.add(Date.valueOf(fecha)); // Convertir LocalDate a SQL Date
        }
        sql += " ORDER BY v.fecha_salida";

        try {
        conn = ConexionBD.getConnection();
            stmt = conn.prepareStatement(sql);

            // Asignar parámetros dinámicamente
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            rs = stmt.executeQuery();

            while (rs.next()) {
                Aerolinea aerolinea = new Aerolinea(
                    rs.getInt("id_aerolinea"),
                    rs.getString("nombre_aerolinea"),
                    rs.getString("codigo")
                );

                Vuelo vuelo = new Vuelo(
                    rs.getInt("id_vuelo"),
                    aerolinea,
                    rs.getString("numero_vuelo"),
                    rs.getString("origen"),
                    rs.getString("destino"),
                    rs.getTimestamp("fecha_salida").toLocalDateTime(),
                    rs.getTimestamp("fecha_llegada").toLocalDateTime(),
                    rs.getInt("asientos_totales"),
                    rs.getInt("asientos_disponibles"),
                    rs.getDouble("precio"),
                    rs.getInt("escalas"),
                    rs.getString("estado")
                );

                vuelos.add(vuelo);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al buscar vuelos:");
            e.printStackTrace();
        } finally {
            // Cerrar recursos
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null && !conn.isClosed()) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return vuelos;
    }
}