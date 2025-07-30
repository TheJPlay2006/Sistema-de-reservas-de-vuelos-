// vista/SistemaReservasGUI.java
package vista;

import dao.ReservaDAO;
import dao.VueloDAO;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import modelo.Reserva;
import modelo.Vuelo;
import modelo.Usuario;
import util.VueloRealAPI; // Asegúrate de tener esta clase

/**
 * Interfaz gráfica principal del sistema de reservas de vuelo.
 * Incluye login, búsqueda local, y vuelos en tiempo real desde API.
 * 
 * @author [Tu nombre]
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

        // Pestaña 1: Búsqueda de vuelos
        tabbedPane.addTab("🔍 Buscar Vuelos", crearPanelBusqueda());

        // Pestaña 2: Itinerario
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
        JButton btnCargarReales = new JButton("🌐 Vuelos en Tiempo Real"); // Nuevo botón

        panelFiltros.add(new JLabel("Origen:"));
        panelFiltros.add(txtOrigen);
        panelFiltros.add(new JLabel("Destino:"));
        panelFiltros.add(txtDestino);
        panelFiltros.add(new JLabel("Fecha:"));
        panelFiltros.add(spinnerFecha);
        panelFiltros.add(btnBuscar);
        panelFiltros.add(btnLimpiar);
        panelFiltros.add(btnCargarReales); // Añadir a filtros

        // Tabla de resultados
        String[] columnas = {"ID", "Aerolínea", "Núm. Vuelo", "Origen", "Destino", "Hora", "Asientos", "Precio"};
        modeloVuelos = new DefaultTableModel(columnas, 0);
        tablaVuelos = new JTable(modeloVuelos);
        tablaVuelos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollVuelos = new JScrollPane(tablaVuelos);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnReservar = new JButton("✅ Hacer Reserva");
        btnReservar.setBackground(new Color(46, 139, 87));
        btnReservar.setForeground(Color.WHITE);
        panelBotones.add(btnReservar);

        // Añadir al panel principal
        panel.add(panelFiltros, BorderLayout.NORTH);
        panel.add(scrollVuelos, BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);

        // === LISTENERS ===

        // Buscar vuelos en la base de datos
        btnBuscar.addActionListener(e -> cargarVuelos());

        // Limpiar filtros
        btnLimpiar.addActionListener(e -> {
            txtOrigen.setText("");
            txtDestino.setText("");
            spinnerFecha.setValue(new java.util.Date());
            cargarVuelos();
        });

        // Cargar vuelos reales desde API
        btnCargarReales.addActionListener(e -> {
            modeloVuelos.setRowCount(0); // Limpiar tabla

            VueloRealAPI api = new VueloRealAPI();
            List<Vuelo> vuelosReales = api.obtenerVuelosReales();

            if (vuelosReales.isEmpty()) {
                JOptionPane.showMessageDialog(this, "❌ No se pudieron obtener vuelos en tiempo real.\nVerifique conexión a internet.");
                return;
            }

            for (Vuelo v : vuelosReales) {
                modeloVuelos.addRow(new Object[]{
                    0, // ID temporal (no persistido)
                    v.getAerolinea().getNombre(),
                    v.getNumeroVuelo(),
                    v.getOrigen(),
                    v.getDestino(),
                    v.getFechaSalida().format(formatter),
                    v.getAsientosDisponibles(),
                    String.format("%.2f", v.getPrecio())
                });
            }

            JOptionPane.showMessageDialog(this, "✅ " + vuelosReales.size() + " vuelos en tiempo real cargados.");
        });

        // Hacer reserva
        btnReservar.addActionListener(e -> hacerReserva());

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

        // Botón de cancelación
        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnCancelar = new JButton("🗑️ Cancelar Reserva");
        btnCancelar.setBackground(Color.RED);
        btnCancelar.setForeground(Color.WHITE);
        panelBotones.add(btnCancelar);

        panel.add(scrollReservas, BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);

        btnCancelar.addActionListener(e -> cancelarReserva());

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

        JOptionPane.showMessageDialog(this, "✈️ " + vuelos.size() + " vuelos encontrados en tu base de datos.");
    }

    private void hacerReserva() {
    int fila = tablaVuelos.getSelectedRow();
    if (fila == -1) {
        JOptionPane.showMessageDialog(this, "⚠️ Seleccione un vuelo para reservar.");
        return;
    }

    // Obtener ID del vuelo
    Object idObj = modeloVuelos.getValueAt(fila, 0);
    
    // Validar que sea un número y mayor que 0
    if (!(idObj instanceof Integer)) {
        JOptionPane.showMessageDialog(this, "❌ No se puede reservar este vuelo.\nSolo los vuelos de la base de datos son reservables.");
        return;
    }

    int idVuelo = (Integer) idObj;
    if (idVuelo <= 0) {
        JOptionPane.showMessageDialog(this, "🚫 Este vuelo es solo informativo.\nNo se puede reservar un vuelo en tiempo real.");
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
    vuelo.setIdVuelo(idVuelo); // Ahora sabemos que es válido
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