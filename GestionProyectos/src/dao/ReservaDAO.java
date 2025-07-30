// dao/ReservaDAO.java
package dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import modelo.*;
import util.ConexionBD;

/**
 * Clase DAO para gestionar operaciones relacionadas con Reservas en la base de datos.
 * Permite crear, listar y cancelar reservas con validaciones completas.
 * 
 * @author [Tu nombre]
 */
public class ReservaDAO {

    /**
     * Registra una nueva reserva si hay asientos disponibles y no existe duplicado.
     * Usa transacción para mantener consistencia: reserva + actualización de asientos.
     * 
     * @param reserva Objeto Reserva con usuario, vuelo y cantidad de asientos
     * @return true si la reserva fue exitosa, false si falló por validación o error
     */
    public boolean crearReserva(Reserva reserva) {
        Connection conn = null;
        CallableStatement cstmt = null;
        boolean exito = false;

        // Validación básica
        if (reserva == null || reserva.getUsuario() == null || reserva.getVuelo() == null) {
            System.err.println("❌ Datos de reserva incompletos.");
            return false;
        }

        int idUsuario = reserva.getUsuario().getIdUsuario();
        int idVuelo = reserva.getVuelo().getIdVuelo();
        int cantidadAsientos = reserva.getCantidadAsientos();

        if (cantidadAsientos <= 0) {
            System.err.println("❌ Cantidad de asientos inválida: " + cantidadAsientos);
            return false;
        }

        try {
            conn = ConexionBD.getConnection();
            conn.setAutoCommit(false); // Iniciar transacción

            // Verificar si ya existe una reserva para este usuario y vuelo
            if (existeReserva(conn, idUsuario, idVuelo)) {
                System.err.println("❌ Ya tienes una reserva para este vuelo.");
                conn.rollback();
                return false;
            }

            // Verificar disponibilidad de asientos
            int asientosDisponibles = obtenerAsientosDisponibles(conn, idVuelo);
            if (asientosDisponibles < cantidadAsientos) {
                System.err.println("❌ No hay suficientes asientos disponibles. Disponibles: " + asientosDisponibles);
                conn.rollback();
                return false;
            }

            // Insertar la reserva mediante procedimiento almacenado
            String sqlInsert = "{CALL sp_insertar_reserva(?, ?, ?, ?)}";
            cstmt = conn.prepareCall(sqlInsert);
            cstmt.setInt(1, idUsuario);
            cstmt.setInt(2, idVuelo);
            cstmt.setInt(3, cantidadAsientos);
            cstmt.registerOutParameter(4, Types.INTEGER); // ID generado

            cstmt.execute();
            int idGenerado = cstmt.getInt(4);

            // Actualizar asientos disponibles del vuelo
            if (!actualizarAsientosDisponibles(conn, idVuelo, -cantidadAsientos)) {
                conn.rollback();
                return false;
            }

            // Confirmar transacción
            conn.commit();
            reserva.setIdReserva(idGenerado);
            reserva.setFechaReserva(LocalDateTime.now());
            reserva.setEstado("Confirmada");
            exito = true;

            System.out.println("✅ Reserva creada con éxito. ID: " + idGenerado);

        } catch (SQLException e) {
            System.err.println("❌ Error en la base de datos durante la reserva:");
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        } finally {
            // Cerrar recursos
            try {
                if (cstmt != null) cstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return exito;
    }

    /**
     * Obtiene todas las reservas confirmadas de un usuario.
     * 
     * @param idUsuario ID del usuario
     * @return Lista de reservas (vacía si no hay ninguna)
     */
    public List<Reserva> obtenerReservasPorUsuario(int idUsuario) {
        List<Reserva> reservas = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        String sql = """
            SELECT 
                r.id_reserva, r.fecha_reserva, r.estado, r.cantidad_asientos,
                v.id_vuelo, v.numero_vuelo, v.origen, v.destino, v.fecha_salida, v.precio,
                a.id_aerolinea, a.nombre AS nombre_aerolinea, a.codigo,
                u.id_usuario, u.nombre AS nombre_usuario, u.email
            FROM Reserva r
            INNER JOIN Vuelo v ON r.id_vuelo = v.id_vuelo
            INNER JOIN Aerolinea a ON v.id_aerolinea = a.id_aerolinea
            INNER JOIN Usuario u ON r.id_usuario = u.id_usuario
            WHERE r.id_usuario = ? AND r.estado = 'Confirmada'
            ORDER BY r.fecha_reserva DESC
            """;

        try {
            conn = ConexionBD.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idUsuario);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Aerolinea aerolinea = new Aerolinea(
                    rs.getInt("id_aerolinea"),
                    rs.getString("nombre_aerolinea"),
                    rs.getString("codigo")
                );

                Vuelo vuelo = new Vuelo();
                vuelo.setIdVuelo(rs.getInt("id_vuelo"));
                vuelo.setNumeroVuelo(rs.getString("numero_vuelo"));
                vuelo.setOrigen(rs.getString("origen"));
                vuelo.setDestino(rs.getString("destino"));
                vuelo.setFechaSalida(rs.getTimestamp("fecha_salida").toLocalDateTime());
                vuelo.setPrecio(rs.getDouble("precio"));
                vuelo.setAerolinea(aerolinea);

                Usuario usuario = new Usuario();
                usuario.setIdUsuario(rs.getInt("id_usuario"));
                usuario.setNombre(rs.getString("nombre_usuario"));

                Reserva reserva = new Reserva();
                reserva.setIdReserva(rs.getInt("id_reserva"));
                reserva.setUsuario(usuario);
                reserva.setVuelo(vuelo);
                reserva.setFechaReserva(rs.getTimestamp("fecha_reserva").toLocalDateTime());
                reserva.setEstado(rs.getString("estado"));
                reserva.setCantidadAsientos(rs.getInt("cantidad_asientos"));

                reservas.add(reserva);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al obtener reservas del usuario " + idUsuario);
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null && !conn.isClosed()) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return reservas;
    }

    /**
     * Cancela una reserva confirmada y devuelve los asientos al vuelo.
     * 
     * @param idReserva ID de la reserva a cancelar
     * @return true si se canceló con éxito, false si falló
     */
    public boolean cancelarReserva(int idReserva) {
        Connection conn = null;
        PreparedStatement stmtReserva = null;
        PreparedStatement stmtVuelo = null;
        ResultSet rs = null;
        boolean exito = false;

        try {
            conn = ConexionBD.getConnection();
            conn.setAutoCommit(false); // Iniciar transacción

            // Verificar que la reserva exista y esté confirmada
            String sqlSelect = """
                SELECT r.cantidad_asientos, r.estado, v.id_vuelo 
                FROM Reserva r
                INNER JOIN Vuelo v ON r.id_vuelo = v.id_vuelo
                WHERE r.id_reserva = ?
                """;

            stmtReserva = conn.prepareStatement(sqlSelect);
            stmtReserva.setInt(1, idReserva);
            rs = stmtReserva.executeQuery();

            if (!rs.next()) {
                System.err.println("❌ Reserva no encontrada: " + idReserva);
                conn.rollback();
                return false;
            }

            if (!"Confirmada".equals(rs.getString("estado"))) {
                System.err.println("❌ La reserva ya está cancelada.");
                conn.rollback();
                return false;
            }

            int cantidadAsientos = rs.getInt("cantidad_asientos");
            int idVuelo = rs.getInt("id_vuelo");
            rs.close();

            // Actualizar estado de la reserva
            String sqlUpdateReserva = "UPDATE Reserva SET estado = 'Cancelada' WHERE id_reserva = ?";
            stmtReserva = conn.prepareStatement(sqlUpdateReserva);
            stmtReserva.setInt(1, idReserva);
            int filasReserva = stmtReserva.executeUpdate();

            if (filasReserva == 0) {
                throw new SQLException("No se pudo actualizar la reserva.");
            }

            // Devolver asientos al vuelo
            String sqlUpdateVuelo = "UPDATE Vuelo SET asientos_disponibles = asientos_disponibles + ? WHERE id_vuelo = ?";
            stmtVuelo = conn.prepareStatement(sqlUpdateVuelo);
            stmtVuelo.setInt(1, cantidadAsientos);
            stmtVuelo.setInt(2, idVuelo);
            int filasVuelo = stmtVuelo.executeUpdate();

            if (filasVuelo == 0) {
                throw new SQLException("No se pudo actualizar el vuelo.");
            }

            // Confirmar transacción
            conn.commit();
            System.out.println("✅ Reserva " + idReserva + " cancelada. Se devolvieron " + cantidadAsientos + " asientos.");
            exito = true;

        } catch (SQLException e) {
            System.err.println("❌ Error al cancelar la reserva:");
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmtReserva != null) stmtReserva.close();
                if (stmtVuelo != null) stmtVuelo.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return exito;
    }

    // --- Métodos privados auxiliares ---

    /**
     * Verifica si ya existe una reserva para este usuario y vuelo.
     */
    private boolean existeReserva(Connection conn, int idUsuario, int idVuelo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Reserva WHERE id_usuario = ? AND id_vuelo = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idVuelo);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    /**
     * Obtiene los asientos disponibles del vuelo.
     */
    private int obtenerAsientosDisponibles(Connection conn, int idVuelo) throws SQLException {
        String sql = "SELECT asientos_disponibles FROM Vuelo WHERE id_vuelo = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idVuelo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("asientos_disponibles");
            } else {
                throw new SQLException("❌ Vuelo no encontrado: " + idVuelo);
            }
        }
    }

    /**
     * Actualiza los asientos disponibles (positivo: sumar, negativo: restar).
     */
    private boolean actualizarAsientosDisponibles(Connection conn, int idVuelo, int cambio) throws SQLException {
        String sql = "UPDATE Vuelo SET asientos_disponibles = asientos_disponibles + ? WHERE id_vuelo = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cambio);
            stmt.setInt(2, idVuelo);
            return stmt.executeUpdate() > 0;
        }
    }
}