
import javax.swing.SwingUtilities;
import vista.SistemaReservasGUI;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author jh599
 */
public class Main {
    // === Main ===
public static void main(String[] args) {
    try {
    Class.forName("com.itextpdf.kernel.pdf.PdfWriter");
    Class.forName("org.slf4j.LoggerFactory");
    System.out.println("✅ iText y SLF4J están disponibles");
} catch (ClassNotFoundException e) {
    System.err.println("❌ Falta una librería: " + e.getMessage());
}
}
}
