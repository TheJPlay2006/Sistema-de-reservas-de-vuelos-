// vista/LoginDialog.java
package vista;

import dao.UsuarioDAO;
import modelo.Usuario;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Diálogo de inicio de sesión y registro de usuarios.
 * Al iniciar sesión, abre la interfaz VuelosGUI.
 */
public class LoginDialog extends JDialog {
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private Usuario usuarioLogueado;
    private boolean loginExitoso = false;

    public LoginDialog(JFrame parent) {
        super(parent, "🔐 Iniciar Sesión", true);
        setSize(350, 200);
        setLocationRelativeTo(parent);
        setLayout(null);
        setResizable(false); // Mejor experiencia de usuario

        // Etiqueta y campo para Email
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setBounds(30, 30, 80, 25);
        add(lblEmail);

        txtEmail = new JTextField();
        txtEmail.setBounds(120, 30, 180, 25);
        add(txtEmail);

        // Etiqueta y campo para Contraseña
        JLabel lblPassword = new JLabel("Contraseña:");
        lblPassword.setBounds(30, 70, 80, 25);
        add(lblPassword);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(120, 70, 180, 25);
        add(txtPassword);

        // Botones
        JButton btnLogin = new JButton("Iniciar Sesión");
        btnLogin.setBounds(60, 110, 120, 30);
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iniciarSesion();
            }
        });

        JButton btnRegister = new JButton("Registrar");
        btnRegister.setBounds(190, 110, 120, 30);
        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registrar();
            }
        });

        add(btnLogin);
        add(btnRegister);

        // Acción al cerrar
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Permitir presionar Enter en los campos
        txtPassword.addActionListener(e -> iniciarSesion());
    }

    /**
     * Maneja el proceso de inicio de sesión.
     */
    private void iniciarSesion() {
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        // Validación de campos
        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Complete todos los campos.", "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validación básica de formato de email
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "❌ Formato de email inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        UsuarioDAO dao = new UsuarioDAO();
        Usuario user = dao.login(email, password);

        if (user != null) {
            this.usuarioLogueado = user;
            this.loginExitoso = true;
            JOptionPane.showMessageDialog(this, "✅ Bienvenido, " + user.getNombre() + "!", "Inicio de sesión exitoso", JOptionPane.INFORMATION_MESSAGE);

            // === ABRIR VuelosGUI Y CERRAR LOGIN ===
            new VuelosGUI(user.getIdUsuario(), user.getNombre()).setVisible(true);
            dispose(); // Cierra el diálogo de login
        } else {
            JOptionPane.showMessageDialog(this, "❌ Email o contraseña incorrectos.\nVerifique sus credenciales.", "Error de inicio de sesión", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Maneja el proceso de registro de un nuevo usuario.
     */
    private void registrar() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre:", "Registro", JOptionPane.QUESTION_MESSAGE);
        if (nombre == null || nombre.trim().isEmpty()) return;

        String email = JOptionPane.showInputDialog(this, "Email:", "Registro", JOptionPane.QUESTION_MESSAGE);
        if (email == null || email.trim().isEmpty()) return;

        // Validación de email
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "❌ Formato de email inválido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String telefono = JOptionPane.showInputDialog(this, "Teléfono:", "Registro", JOptionPane.QUESTION_MESSAGE);
        String password = JOptionPane.showInputDialog(this, "Contraseña:", "Registro", JOptionPane.QUESTION_MESSAGE);

        if (password == null || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La contraseña es obligatoria.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Crear objeto Usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre.trim());
        usuario.setEmail(email.trim());
        usuario.setTelefono(telefono != null ? telefono.trim() : null);
        usuario.setPassword(password); // En producción: encriptar con BCrypt

        UsuarioDAO dao = new UsuarioDAO();
        if (dao.registrarUsuario(usuario)) {
            JOptionPane.showMessageDialog(this, "✅ Registro exitoso. Ahora puede iniciar sesión.", "Registro completado", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "❌ Error al registrar. ¿El email ya existe?", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Devuelve el usuario que inició sesión.
     * @return Usuario logueado
     */
    public Usuario getUsuarioLogueado() {
        return usuarioLogueado;
    }

    /**
     * Verifica si el inicio de sesión fue exitoso.
     * @return true si el login fue exitoso, false en caso contrario
     */
    public boolean isLoginExitoso() {
        return loginExitoso;
    }
}