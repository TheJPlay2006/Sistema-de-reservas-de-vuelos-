// vista/SistemaReservasGUI.java
package vista;

import dao.ReservaDAO;
import dao.VueloDAO;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import modelo.Reserva;
import modelo.Vuelo;
import modelo.Aerolinea;
import modelo.Usuario;
import util.VueloRealAPI;
import util.ConexionBD;

/**
 * Interfaz gráfica principal del sistema de reservas.
 * Incluye login, búsqueda local, vuelos en el aire y posibilidad de agregarlos al sistema.
 */
public class SistemaReservasGUI extends JFrame {

    private JTable tablaVuelos;
    private DefaultTableModel modeloVuelos;
    private JTable tablaReservas;
    private DefaultTableModel modeloReservas;
    private JTextField txtOrigen, txtDestino;
    private JSpinner spinnerFecha;
    private ReservaDAO reservaDAO;
    private VueloDAO vueloDAO;
    private int idUsuario;
    private String nombreUsuario;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public SistemaReservasGUI() {
        // === Mostrar login primero ===
        LoginDialog login = new LoginDialog(this);
        login.setVisible(true);

        if (!login.isLoginExitoso()) {
            System.exit(0);
        }

        Usuario usuario = login.getUsuarioLogueado();
        this.idUsuario = usuario.getIdUsuario();
        this.nombreUsuario = usuario.getNombre();

        // Inicializar DAOs
        this.reservaDAO = new ReservaDAO();
        this.vueloDAO = new VueloDAO();

        // Configuración de la ventana
        setTitle("✈️ Sistema de Reservas - Bienvenido, " + nombreUsuario);
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Crear pestañas
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("🔍 Buscar Vuelos", crearPanelBusqueda());
        tabbedPane.addTab("🧳 Mi Itinerario", crearPanelItinerario());

        // Barra de estado
        JLabel statusBar = new JLabel("Usuario: " + nombreUsuario + " | ID: " + idUsuario);
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        add(statusBar, BorderLayout.SOUTH);

        add(tabbedPane, BorderLayout.CENTER);

        // Cargar datos iniciales
        cargarVuelos();
        cargarReservas();
    }

    // === PANEL DE BÚSQUEDA DE VUELOS ===
    private JPanel crearPanelBusqueda() {
        JPanel panel = new JPanel(new BorderLayout());

        // Panel de filtros
        JPanel panelFiltros = new JPanel(new FlowLayout());
        panelFiltros.setBorder(BorderFactory.createTitledBorder("Filtros de Búsqueda"));

        txtOrigen = new JTextField(10);
        txtDestino = new JTextField(10);

        LocalDate hoy = LocalDate.now();
        SpinnerDateModel model = new SpinnerDateModel(
            java.util.Date.from(hoy.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()),
            null, null, java.util.Calendar.DAY_OF_MONTH);
        spinnerFecha = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinnerFecha, "yyyy-MM-dd");
        spinnerFecha.setEditor(editor);

        JButton btnBuscar = new JButton("Buscar Vuelos");
        JButton btnLimpiar = new JButton("Limpiar");
        JButton btnCargarReales = new JButton("☁️ Vuelos en el aire");

        panelFiltros.add(new JLabel("Origen:"));
        panelFiltros.add(txtOrigen);
        panelFiltros.add(new JLabel("Destino:"));
        panelFiltros.add(txtDestino);
        panelFiltros.add(new JLabel("Fecha:"));
        panelFiltros.add(spinnerFecha);
        panelFiltros.add(btnBuscar);
        panelFiltros.add(btnLimpiar);
        panelFiltros.add(btnCargarReales);

        // Tabla de resultados
        String[] columnas = {"ID", "Aerolínea", "Núm. Vuelo", "Origen", "Destino", "Salida", "Asientos", "Precio"};
        modeloVuelos = new DefaultTableModel(columnas, 0);
        tablaVuelos = new JTable(modeloVuelos);
        tablaVuelos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollVuelos = new JScrollPane(tablaVuelos);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnReservar = new JButton("✅ Hacer Reserva");
        btnReservar.setBackground(new Color(46, 139, 87));
        btnReservar.setForeground(Color.WHITE);

        JButton btnAgregarVuelo = new JButton("➕ Agregar a mi sistema");
        btnAgregarVuelo.setBackground(new Color(0, 102, 204));
        btnAgregarVuelo.setForeground(Color.WHITE);

        panelBotones.add(btnReservar);
        panelBotones.add(btnAgregarVuelo);

        // Añadir al panel principal
        panel.add(panelFiltros, BorderLayout.NORTH);
        panel.add(scrollVuelos, BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);

        // === LISTENERS ===
        btnBuscar.addActionListener(e -> cargarVuelos());

        btnLimpiar.addActionListener(e -> {
            txtOrigen.setText("");
            txtDestino.setText("");
            spinnerFecha.setValue(new java.util.Date());
            cargarVuelos();
        });

        btnCargarReales.addActionListener(e -> {
            modeloVuelos.setRowCount(0);
            VueloRealAPI api = new VueloRealAPI();
            List<Vuelo> vuelosReales = api.obtenerVuelosReales();

            if (vuelosReales.isEmpty()) {
                JOptionPane.showMessageDialog(this, "❌ No se pudieron obtener vuelos en el aire.\nVerifique conexión a internet.");
                return;
            }

            for (Vuelo v : vuelosReales) {
                modeloVuelos.addRow(new Object[]{
                    0,
                    v.getAerolinea().getNombre(),
                    v.getNumeroVuelo(),
                    v.getOrigen(),
                    v.getDestino(),
                    v.getFechaSalida().format(formatter),
                    v.getAsientosDisponibles(),
                    String.format("%.2f", v.getPrecio())
                });
            }

            JOptionPane.showMessageDialog(this, "✅ " + vuelosReales.size() + " vuelos en el aire cargados.");
        });

        btnReservar.addActionListener(e -> hacerReserva());

        btnAgregarVuelo.addActionListener(e -> {
            int fila = tablaVuelos.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "⚠️ Seleccione un vuelo para agregar.");
                return;
            }

            Object idObj = modeloVuelos.getValueAt(fila, 0);
            if (idObj instanceof Integer && (Integer) idObj > 0) {
                JOptionPane.showMessageDialog(this, "✅ Este vuelo ya está en tu sistema.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                this,
                "¿Desea agregar este vuelo a su sistema?\nPodrá reservar asientos como cualquier otro vuelo.",
                "Agregar Vuelo",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                agregarVueloDesdeAPI(fila);
            }
        });

        return panel;
    }

    // === PANEL DE ITINERARIO ===
    private JPanel crearPanelItinerario() {
        JPanel panel = new JPanel(new BorderLayout());

        // Tabla de reservas
        String[] columnas = {"ID", "Vuelo", "Ruta", "Salida", "Asientos", "Precio Total", "Fecha Reserva"};
        modeloReservas = new DefaultTableModel(columnas, 0);
        tablaReservas = new JTable(modeloReservas);
        tablaReservas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollReservas = new JScrollPane(tablaReservas);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout());

        JButton btnCancelar = new JButton("🗑️ Cancelar Reserva");
        btnCancelar.setBackground(Color.RED);
        btnCancelar.setForeground(Color.WHITE);

        JButton btnExportarPDF = new JButton("📄 Exportar a PDF");
        btnExportarPDF.setBackground(new Color(255, 165, 0));
        btnExportarPDF.setForeground(Color.WHITE);

        panelBotones.add(btnCancelar);
        panelBotones.add(btnExportarPDF);

        panel.add(scrollReservas, BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);

        // Listeners
        btnCancelar.addActionListener(e -> cancelarReserva());
        btnExportarPDF.addActionListener(e -> exportarItinerarioACSV());

        return panel;
    }

    // === MÉTODOS DE NEGOCIO ===

    private void cargarVuelos() {
        modeloVuelos.setRowCount(0);
        String origen = txtOrigen.getText().trim();
        String destino = txtDestino.getText().trim();
        LocalDate fecha = LocalDate.parse(
            new java.text.SimpleDateFormat("yyyy-MM-dd").format(spinnerFecha.getValue()),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
        );

        List<Vuelo> vuelos = vueloDAO.buscarVuelos(
            origen.isEmpty() ? null : origen,
            destino.isEmpty() ? null : destino,
            fecha
        );

        for (Vuelo v : vuelos) {
            modeloVuelos.addRow(new Object[]{
                v.getIdVuelo(),
                v.getAerolinea().getNombre(),
                v.getNumeroVuelo(),
                v.getOrigen(),
                v.getDestino(),
                v.getFechaSalida().format(formatter),
                v.getAsientosDisponibles(),
                String.format("%.2f", v.getPrecio())
            });
        }

        JOptionPane.showMessageDialog(this, "✈️ " + vuelos.size() + " vuelos encontrados en tu sistema.");
    }

    private void hacerReserva() {
        int fila = tablaVuelos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "⚠️ Seleccione un vuelo para reservar.");
            return;
        }

        Object idObj = modeloVuelos.getValueAt(fila, 0);
        if (!(idObj instanceof Integer)) {
            JOptionPane.showMessageDialog(this, "❌ No se puede reservar este vuelo.\nSolo los vuelos de tu sistema son reservables.");
            return;
        }

        int idVuelo = (Integer) idObj;
        if (idVuelo <= 0) {
            JOptionPane.showMessageDialog(this, "🚫 Este vuelo es solo informativo.\nAgrégalo a tu sistema primero.");
            return;
        }

        String numeroVuelo = (String) modeloVuelos.getValueAt(fila, 2);
        int disponibles = (int) modeloVuelos.getValueAt(fila, 6);

        String input = JOptionPane.showInputDialog(this, "¿Cuántos asientos desea reservar?\nDisponibles: " + disponibles, "Cantidad", JOptionPane.QUESTION_MESSAGE);
        if (input == null) return;

        int cantidad;
        try {
            cantidad = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "❌ Ingrese un número válido.");
            return;
        }

        if (cantidad <= 0 || cantidad > disponibles) {
            JOptionPane.showMessageDialog(this, "❌ Cantidad inválida.");
            return;
        }

        modelo.Usuario usuario = new modelo.Usuario();
        usuario.setIdUsuario(idUsuario);

        modelo.Vuelo vuelo = new modelo.Vuelo();
        vuelo.setIdVuelo(idVuelo);
        vuelo.setNumeroVuelo(numeroVuelo);
        vuelo.setAsientosDisponibles(disponibles);
        vuelo.setPrecio(Double.parseDouble(modeloVuelos.getValueAt(fila, 7).toString()));

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setVuelo(vuelo);
        reserva.setCantidadAsientos(cantidad);

        if (reservaDAO.crearReserva(reserva)) {
            JOptionPane.showMessageDialog(this, "🎉 ¡Reserva exitosa!\nVuelo: " + numeroVuelo + "\nAsientos: " + cantidad);
            cargarVuelos();
            cargarReservas();
        } else {
            JOptionPane.showMessageDialog(this, "❌ No se pudo completar la reserva.");
        }
    }

    private void agregarVueloDesdeAPI(int fila) {
        String numeroVuelo = (String) modeloVuelos.getValueAt(fila, 2);
        String origen = (String) modeloVuelos.getValueAt(fila, 3);
        String destino = (String) modeloVuelos.getValueAt(fila, 4);
        String horaStr = (String) modeloVuelos.getValueAt(fila, 5);
        int asientos = (int) modeloVuelos.getValueAt(fila, 6);
        double precio = Double.parseDouble(modeloVuelos.getValueAt(fila, 7).toString());
        String aerolineaNombre = (String) modeloVuelos.getValueAt(fila, 1);

        java.time.LocalDateTime fechaSalida;
        try {
            fechaSalida = java.time.LocalDateTime.parse(horaStr, formatter);
        } catch (Exception ex) {
            fechaSalida = java.time.LocalDateTime.now().plusHours(1);
        }

        int idAerolinea = obtenerOCrearAerolinea(aerolineaNombre);
        if (idAerolinea == -1) {
            JOptionPane.showMessageDialog(this, "❌ No se pudo registrar la aerolínea.");
            return;
        }

        if (insertarVueloEnBD(idAerolinea, numeroVuelo, origen, destino, fechaSalida, asientos, precio)) {
            JOptionPane.showMessageDialog(this, "✅ Vuelo agregado a tu sistema.\nYa puedes reservar asientos.");
            cargarVuelos();
        } else {
            JOptionPane.showMessageDialog(this, "❌ No se pudo agregar el vuelo.");
        }
    }

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
                return (int) result.getDouble(1); // SCOPE_IDENTITY() devuelve NUMERIC
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
            stmt.setObject(6, fechaSalida.plusHours(2)); // Llegada estimada
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

    private void cargarReservas() {
        modeloReservas.setRowCount(0);
        List<Reserva> reservas = reservaDAO.obtenerReservasPorUsuario(idUsuario);

        for (Reserva r : reservas) {
            double precioTotal = r.getVuelo().getPrecio() * r.getCantidadAsientos();
            modeloReservas.addRow(new Object[]{
                r.getIdReserva(),
                r.getVuelo().getNumeroVuelo(),
                r.getVuelo().getOrigen() + " → " + r.getVuelo().getDestino(),
                r.getVuelo().getFechaSalida().format(formatter),
                r.getCantidadAsientos(),
                String.format("%.2f", precioTotal),
                r.getFechaReserva().format(formatter)
            });
        }
    }

    private void cancelarReserva() {
        int fila = tablaReservas.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "⚠️ Seleccione una reserva para cancelar.");
            return;
        }

        int idReserva = (int) modeloReservas.getValueAt(fila, 0);
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro de cancelar la reserva #" + idReserva + "?",
            "Confirmar Cancelación",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (reservaDAO.cancelarReserva(idReserva)) {
                JOptionPane.showMessageDialog(this, "✅ Reserva cancelada con éxito.");
                cargarReservas();
                cargarVuelos();
            } else {
                JOptionPane.showMessageDialog(this, "❌ No se pudo cancelar la reserva.");
            }
        }
    }

    // === EXPORTAR A PDF ===
 private void exportarItinerarioACSV() {
    // Obtener reservas del usuario
    List<Reserva> reservas = reservaDAO.obtenerReservasPorUsuario(idUsuario);
    if (reservas.isEmpty()) {
        JOptionPane.showMessageDialog(this, "❌ No tienes reservas para exportar.");
        return;
    }

    // Seleccionar ubicación del archivo
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Guardar itinerario como CSV");
    chooser.setSelectedFile(new java.io.File("Itinerario_" + idUsuario + ".csv"));
    int result = chooser.showSaveDialog(this);

    if (result != JFileChooser.APPROVE_OPTION) {
        return; // Cancelado
    }

    java.io.File archivo = chooser.getSelectedFile();
    if (!archivo.getName().toLowerCase().endsWith(".csv")) {
        archivo = new java.io.File(archivo.getPath() + ".csv");
    }

    try (java.io.PrintWriter writer = new java.io.PrintWriter(archivo, "UTF-8")) {
        // Escribir encabezados
        writer.println("Vuelo,Ruta,Salida,Asientos,Precio Total,Estado,Fecha Reserva");

        // Escribir filas
        for (Reserva r : reservas) {
            double precioTotal = r.getVuelo().getPrecio() * r.getCantidadAsientos();
            String ruta = r.getVuelo().getOrigen() + " → " + r.getVuelo().getDestino();
            String salida = r.getVuelo().getFechaSalida().format(formatter);
            String fechaReserva = r.getFechaReserva().format(formatter);

            // Escapar comillas y campos con comas
            String linea = String.format("%s,\"%s\",\"%s\",%d,%.2f,%s,\"%s\"",
                r.getVuelo().getNumeroVuelo(),
                ruta,
                salida,
                r.getCantidadAsientos(),
                precioTotal,
                r.getEstado(),
                fechaReserva
            );

            writer.println(linea);
        }

        JOptionPane.showMessageDialog(this, "✅ Itinerario exportado como CSV:\n" + archivo.getAbsolutePath());

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "❌ Error al guardar el archivo:\n" + e.getMessage());
    }
}
    // === Main ===
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
            } catch (Exception e) {
                e.printStackTrace();
            }
            new SistemaReservasGUI().setVisible(true);
        });
    }
}