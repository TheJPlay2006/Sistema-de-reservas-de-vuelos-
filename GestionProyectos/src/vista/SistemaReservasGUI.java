// vista/SistemaReservasGUI.java
package vista;

import dao.ReservaDAO;
import dao.VueloDAO;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JOptionPane;
import modelo.Reserva;
import modelo.Vuelo;
import modelo.Aerolinea;
import modelo.Usuario;
import util.VueloRealAPI;
import util.ConexionBD;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Chunk;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Clase de controlador para el sistema de reservas.
 * Contiene toda la lógica de negocio, pero NO interfaz gráfica.
 */
public class SistemaReservasGUI {

    private int idUsuario;
    private String nombreUsuario;
    private ReservaDAO reservaDAO;
    private VueloDAO vueloDAO;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Constructor que recibe el usuario logueado
    public SistemaReservasGUI(int idUsuario, String nombreUsuario) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.reservaDAO = new ReservaDAO();
        this.vueloDAO = new VueloDAO();
    }

    // === MÉTODOS DE NEGOCIO ===

    /**
     * Carga los vuelos desde la base de datos según filtros.
     * @param origen Origen del vuelo (puede ser null)
     * @param destino Destino del vuelo (puede ser null)
     * @param fecha Fecha de salida
     * @return Lista de vuelos que coinciden
     */
    public List<Vuelo> buscarVuelos(String origen, String destino, LocalDate fecha) {
        return vueloDAO.buscarVuelos(origen, destino, fecha);
    }

    /**
     * Realiza una reserva de un vuelo.
     * @param idVuelo ID del vuelo a reservar
     * @param cantidadAsientos Cantidad de asientos a reservar
     * @return true si la reserva fue exitosa
     */
    public boolean hacerReserva(int idVuelo, int cantidadAsientos) {
        modelo.Usuario usuario = new modelo.Usuario();
        usuario.setIdUsuario(idUsuario);

        modelo.Vuelo vuelo = new modelo.Vuelo();
        vuelo.setIdVuelo(idVuelo);

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setVuelo(vuelo);
        reserva.setCantidadAsientos(cantidadAsientos);

        boolean exito = reservaDAO.crearReserva(reserva);
        if (exito) {
            JOptionPane.showMessageDialog(null, "🎉 ¡Reserva exitosa!");
        } else {
            JOptionPane.showMessageDialog(null, "❌ No se pudo completar la reserva.");
        }
        return exito;
    }

    /**
     * Cancela una reserva existente.
     * @param idReserva ID de la reserva a cancelar
     * @return true si la cancelación fue exitosa
     */
    public boolean cancelarReserva(int idReserva) {
        boolean exito = reservaDAO.cancelarReserva(idReserva);
        if (exito) {
            JOptionPane.showMessageDialog(null, "✅ Reserva cancelada con éxito.");
        } else {
            JOptionPane.showMessageDialog(null, "❌ No se pudo cancelar la reserva.");
        }
        return exito;
    }

    /**
     * Obtiene las reservas del usuario actual.
     * @return Lista de reservas del usuario
     */
    public List<Reserva> obtenerReservasUsuario() {
        return reservaDAO.obtenerReservasPorUsuario(idUsuario);
    }

    /**
     * Obtiene vuelos en el aire desde la API.
     * @return Lista de vuelos en tiempo real
     */
    public List<Vuelo> obtenerVuelosEnElAire() {
        VueloRealAPI api = new VueloRealAPI();
        return api.obtenerVuelosReales();
    }

    /**
     * Agrega un vuelo de la API a la base de datos del sistema.
     * @param vuelo Vuelo a agregar
     * @return true si se agregó correctamente
     */
    public boolean agregarVueloDesdeAPI(Vuelo vuelo) {
        int idAerolinea = obtenerOCrearAerolinea(vuelo.getAerolinea().getNombre());
        if (idAerolinea == -1) {
            JOptionPane.showMessageDialog(null, "❌ No se pudo registrar la aerolínea.");
            return false;
        }

        return insertarVueloEnBD(
            idAerolinea,
            vuelo.getNumeroVuelo(),
            vuelo.getOrigen(),
            vuelo.getDestino(),
            vuelo.getFechaSalida(),
            vuelo.getAsientosDisponibles(),
            vuelo.getPrecio()
        );
    }

    /**
     * Exporta el itinerario del usuario a un archivo PDF.
     */
    public void exportarItinerarioAPDF() {
        List<Reserva> reservas = reservaDAO.obtenerReservasPorUsuario(idUsuario);
        if (reservas.isEmpty()) {
            JOptionPane.showMessageDialog(null, "❌ No tienes reservas para exportar.");
            return;
        }

        // Seleccionar ubicación del archivo
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setDialogTitle("Guardar itinerario como PDF");
        chooser.setSelectedFile(new File("Itinerario_" + idUsuario + ".pdf"));
        int result = chooser.showSaveDialog(null);

        if (result != javax.swing.JFileChooser.APPROVE_OPTION) {
            return; // Cancelado
        }

        File archivo = chooser.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".pdf")) {
            archivo = new File(archivo.getPath() + ".pdf");
        }

        try {
            // Crear documento PDF con OpenPDF
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(archivo));
            document.open();

            // Definir fuentes
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
            Font smallFont = new Font(Font.HELVETICA, 9, Font.NORMAL);

            // Título principal
            Paragraph title = new Paragraph("ITINERARIO DE VUELOS", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Información del usuario
            Paragraph userIdPara = new Paragraph();
            userIdPara.add(new Chunk("Usuario ID: ", headerFont));
            userIdPara.add(new Chunk(String.valueOf(idUsuario), normalFont));
            document.add(userIdPara);

            Paragraph datePara = new Paragraph();
            datePara.add(new Chunk("Fecha de generación: ", headerFont));
            datePara.add(new Chunk(java.time.LocalDateTime.now().format(formatter), normalFont));
            datePara.setSpacingAfter(20);
            document.add(datePara);

            // Crear tabla con 6 columnas
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            float[] columnWidths = {15f, 25f, 20f, 10f, 15f, 15f};
            table.setWidths(columnWidths);

            // Color de fondo para encabezados
            Color headerColor = new Color(230, 230, 230);

            // Agregar encabezados de tabla
            String[] headers = {"Vuelo", "Ruta", "Salida", "Asientos", "Precio Total", "Estado"};
            for (String header : headers) {
                PdfPCell headerCell = new PdfPCell(new Phrase(header, headerFont));
                headerCell.setBackgroundColor(headerColor);
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                headerCell.setPadding(8);
                headerCell.setBorderWidth(1);
                table.addCell(headerCell);
            }

            // Agregar datos de las reservas
            double totalGeneral = 0;
            for (Reserva reserva : reservas) {
                double precioTotal = reserva.getVuelo().getPrecio() * reserva.getCantidadAsientos();
                totalGeneral += precioTotal;
                String ruta = reserva.getVuelo().getOrigen() + " → " + reserva.getVuelo().getDestino();
                String salida = reserva.getVuelo().getFechaSalida().format(formatter);

                // Celda Vuelo
                PdfPCell cellVuelo = new PdfPCell(new Phrase(reserva.getVuelo().getNumeroVuelo(), normalFont));
                cellVuelo.setPadding(5);
                table.addCell(cellVuelo);

                // Celda Ruta
                PdfPCell cellRuta = new PdfPCell(new Phrase(ruta, smallFont));
                cellRuta.setPadding(5);
                table.addCell(cellRuta);

                // Celda Salida
                PdfPCell cellSalida = new PdfPCell(new Phrase(salida, smallFont));
                cellSalida.setPadding(5);
                table.addCell(cellSalida);

                // Celda Asientos (centrado)
                PdfPCell cellAsientos = new PdfPCell(new Phrase(String.valueOf(reserva.getCantidadAsientos()), normalFont));
                cellAsientos.setHorizontalAlignment(Element.ALIGN_CENTER);
                cellAsientos.setPadding(5);
                table.addCell(cellAsientos);

                // Celda Precio (alineado a la derecha)
                PdfPCell cellPrecio = new PdfPCell(new Phrase("$" + String.format("%.2f", precioTotal), normalFont));
                cellPrecio.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellPrecio.setPadding(5);
                table.addCell(cellPrecio);

                // Celda Estado (centrado)
                PdfPCell cellEstado = new PdfPCell(new Phrase(reserva.getEstado(), normalFont));
                cellEstado.setHorizontalAlignment(Element.ALIGN_CENTER);
                cellEstado.setPadding(5);
                table.addCell(cellEstado);
            }

            // Agregar tabla al documento
            document.add(table);

            // Agregar resumen
            Paragraph espaciado = new Paragraph(" ");
            espaciado.setSpacingBefore(20);
            document.add(espaciado);

            Paragraph tituloResumen = new Paragraph("RESUMEN", headerFont);
            tituloResumen.setAlignment(Element.ALIGN_RIGHT);
            document.add(tituloResumen);

            Paragraph totalReservas = new Paragraph();
            totalReservas.add(new Chunk("Total de reservas: ", normalFont));
            totalReservas.add(new Chunk(String.valueOf(reservas.size()), headerFont));
            totalReservas.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalReservas);

            Paragraph montoTotal = new Paragraph();
            montoTotal.add(new Chunk("Monto total: ", normalFont));
            montoTotal.add(new Chunk("$" + String.format("%.2f", totalGeneral), headerFont));
            montoTotal.setAlignment(Element.ALIGN_RIGHT);
            document.add(montoTotal);

            // Cerrar documento
            document.close();

            JOptionPane.showMessageDialog(null, "✅ Itinerario exportado exitosamente como PDF:\n" + archivo.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "❌ Error al generar el PDF:\n" + e.getMessage());
        }
    }

    // === MÉTODOS AUXILIARES PARA BASE DE DATOS ===

    private int obtenerOCrearAerolinea(String nombre) {
        String sqlSelect = "SELECT id_aerolinea FROM Aerolinea WHERE nombre = ?";
        String sqlInsert = "INSERT INTO Aerolinea (nombre, codigo) VALUES (?, ?); SELECT SCOPE_IDENTITY();";

        try (Connection conn = ConexionBD.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(sqlSelect);
            stmt.setString(1, nombre);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_aerolinea");
            }

            // Crear nueva aerolínea
            String codigo = nombre.length() >= 2 ? nombre.substring(0, 2).toUpperCase() : "XX";
            stmt = conn.prepareStatement(sqlInsert);
            stmt.setString(1, nombre);
            stmt.setString(2, codigo);
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return (int) result.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private boolean insertarVueloEnBD(int idAerolinea, String numeroVuelo, String origen, String destino,
                                      java.time.LocalDateTime fechaSalida, int asientos, double precio) {
        String sql = "INSERT INTO Vuelo (id_aerolinea, numero_vuelo, origen, destino, " +
                     "fecha_salida, fecha_llegada, asientos_totales, asientos_disponibles, " +
                     "precio, escalas, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idAerolinea);
            stmt.setString(2, numeroVuelo);
            stmt.setString(3, origen);
            stmt.setString(4, destino);
            stmt.setObject(5, fechaSalida);
            stmt.setObject(6, fechaSalida.plusHours(2));
            stmt.setInt(7, asientos);
            stmt.setInt(8, asientos);
            stmt.setDouble(9, precio);
            stmt.setInt(10, 0);
            stmt.setString(11, "Activo");

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // === Getters ===
    public int getIdUsuario() {
        return idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }
}