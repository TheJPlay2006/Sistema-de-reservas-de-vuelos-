// Main.java
import javax.swing.SwingUtilities;
import vista.LoginDialog;
import vista.VuelosGUI;

/**
 * Clase principal que inicia la aplicación.
 * Primero muestra el LoginDialog y luego la interfaz principal.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginDialog login = new LoginDialog(null); 
            login.setVisible(true);

            if (login.isLoginExitoso()) {
                int idUsuario = login.getUsuarioLogueado().getIdUsuario();
                String nombreUsuario = login.getUsuarioLogueado().getNombre();

                new VuelosGUI(idUsuario, nombreUsuario).setVisible(true);
                login.dispose();
            } else {
                System.exit(0);
            }
        });
    }
}