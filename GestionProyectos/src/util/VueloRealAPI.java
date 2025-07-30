// util/VueloRealAPI.java
package util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import modelo.Vuelo;
import modelo.Aerolinea;

/**
 * Cliente API para obtener vuelos en tiempo real desde OpenSky Network.
 * 
 * Obtiene datos de vuelos comerciales en vuelo, procesa el JSON manualmente
 * y convierte la información en objetos Vuelo con datos estimados.
 * 
 * Documentación: https://opensky-network.org/apidoc/rest.html
 * 
 * @author [Tu nombre]
 */
public class VueloRealAPI {

    // Endpoint público que no requiere autenticación
    private static final String API_URL = "https://opensky-network.org/api/states/all";

    /**
     * Obtiene una lista de hasta 50 vuelos en tiempo real.
     * 
     * @return Lista de objetos Vuelo con datos estimados (origen, destino, precio, etc.)
     */
    public List<Vuelo> obtenerVuelosReales() {
        List<Vuelo> vuelos = new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(java.time.Duration.ofSeconds(15)) // Aumentado para estabilidad
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();

                // Extraer el array de estados
                List<String[]> datosVuelos = extraerDatosVuelos(responseBody);

                int contador = 0;
                for (String[] campos : datosVuelos) {
                    if (contador >= 50) break; // Limitar a 50 vuelos para no saturar la interfaz

                    try {
                        String codigoVuelo = limpiarTexto(campos.length > 1 ? campos[1] : null);
                        String paisOrigen = limpiarTexto(campos.length > 2 ? campos[2] : null);
                        long ultimaActualizacion = parseLongSeguro(campos.length > 3 ? campos[3] : "0");
                        double latitud = parseDoubleSeguro(campos.length > 6 ? campos[6] : "0");
                        double longitud = parseDoubleSeguro(campos.length > 7 ? campos[7] : "0");
                        String paisDestino = obtenerPaisDestino(latitud, longitud);

                        // Validar que tenga un código de vuelo válido
                        if (esCadenaValida(codigoVuelo)) {
                            Aerolinea aerolinea = crearAerolinea(codigoVuelo);
                            Vuelo vuelo = crearVuelo(aerolinea, codigoVuelo, paisOrigen, paisDestino,
                                    ultimaActualizacion, latitud, longitud);
                            vuelos.add(vuelo);
                            contador++;
                        }
                    } catch (Exception e) {
                        // Ignorar vuelos con datos corruptos
                        continue;
                    }
                }

                System.out.println("✅ API: " + vuelos.size() + " vuelos en tiempo real procesados.");
            } else {
                System.err.println("❌ API responded with status: " + response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Error de conexión con OpenSky API:");
            e.printStackTrace();
        }

        return vuelos;
    }

    // === Métodos auxiliares ===

    /**
     * Extrae los datos de vuelo del JSON y devuelve una lista de arrays de campos.
     */
    private List<String[]> extraerDatosVuelos(String json) {
        List<String[]> datos = new ArrayList<>();
        String statesKey = "\"states\":";

        int inicioArray = json.indexOf(statesKey);
        if (inicioArray == -1) return datos;

        int bracketOpen = json.indexOf('[', inicioArray);
        int bracketClose = json.lastIndexOf(']');

        if (bracketOpen == -1 || bracketClose == -1) return datos;

        String arrayContent = json.substring(bracketOpen + 1, bracketClose).trim();

        // Dividir por filas (cada fila es un vuelo)
        String[] filas = arrayContent.split("\\],\\[");
        for (String fila : filas) {
            // Limpiar comillas y espacios
            fila = fila.replaceAll("\"", "").trim();
            String[] campos = fila.split(",", -1); // -1 para mantener campos vacíos
            datos.add(campos);
        }

        return datos;
    }

    /**
     * Crea un objeto Aerolinea basado en el código del vuelo.
     */
    private Aerolinea crearAerolinea(String codigoVuelo) {
        Aerolinea aerolinea = new Aerolinea();
        aerolinea.setNombre(extraerNombreAerolinea(codigoVuelo));
        aerolinea.setCodigo(extraerCodigoAerolinea(codigoVuelo));
        return aerolinea;
    }

    /**
     * Crea un objeto Vuelo con los datos estimados.
     */
    private Vuelo crearVuelo(Aerolinea aerolinea, String codigoVuelo, String origen, String destino,
                            long ultimaActualizacion, double latitud, double longitud) {
        Vuelo vuelo = new Vuelo();
        vuelo.setAerolinea(aerolinea);
        vuelo.setNumeroVuelo(codigoVuelo);
        vuelo.setOrigen(origen);
        vuelo.setDestino(destino);

        if (ultimaActualizacion > 0) {
            vuelo.setFechaSalida(Instant.ofEpochSecond(ultimaActualizacion)
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        } else {
            vuelo.setFechaSalida(java.time.LocalDateTime.now());
        }

        vuelo.setPrecio(100 + Math.random() * 900); // $100 - $1000
        vuelo.setAsientosDisponibles(30 + (int) (Math.random() * 50)); // 30-80
        vuelo.setEscalas(0);
        vuelo.setEstado("En vuelo");
        vuelo.setIdVuelo(0); // Temporal: no está en la BD

        return vuelo;
    }

    /**
     * Estima el país de destino según la posición geográfica.
     */
    private String obtenerPaisDestino(double lat, double lon) {
        if (lat == 0 && lon == 0) return "Desconocido";

        if (lat > 35 && lat < 45 && lon > -10 && lon < 5) return "España";
        if (lat > 24 && lat < 50 && lon > -125 && lon < -65) return "Estados Unidos";
        if (lat > 4 && lat < 14 && lon > -75 && lon < -66) return "Colombia";
        if (lat > -35 && lat < 5 && lon > -80 && lon < -65) return "Perú";
        if (lat > 50 && lon > 5) return "Alemania";
        if (lat > 55 && lon < 40) return "Rusia";
        if (lat < -10 && lon > 100) return "Australia";
        if (lat > 10 && lon > 70) return "India";

        return "Internacional";
    }

    // === Métodos de utilidad ===

    private String limpiarTexto(String s) {
        if (s == null || s.trim().isEmpty() || "null".equalsIgnoreCase(s)) {
            return "Desconocido";
        }
        return s.trim();
    }

    private boolean esCadenaValida(String s) {
        return s != null && !s.isEmpty() && !"null".equalsIgnoreCase(s) && s.length() >= 3;
    }

    private long parseLongSeguro(String s) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseDoubleSeguro(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String extraerNombreAerolinea(String codigo) {
        if (codigo.startsWith("AV")) return "Avianca";
        if (codigo.startsWith("AA")) return "American Airlines";
        if (codigo.startsWith("DL")) return "Delta Airlines";
        if (codigo.startsWith("UA")) return "United Airlines";
        if (codigo.startsWith("IB")) return "Iberia";
        if (codigo.startsWith("AF")) return "Air France";
        if (codigo.startsWith("BA")) return "British Airways";
        if (codigo.startsWith("LH")) return "Lufthansa";
        if (codigo.startsWith("EK")) return "Emirates";
        if (codigo.length() >= 2) {
            return codigo.substring(0, 2).toUpperCase() + " Airways";
        }
        return "Aerolínea Desconocida";
    }

    private String extraerCodigoAerolinea(String codigo) {
        if (codigo.length() >= 2) {
            return codigo.substring(0, 2).toUpperCase();
        }
        return "XX";
    }
}