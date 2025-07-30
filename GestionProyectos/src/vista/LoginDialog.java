// vista/LoginDialog.java
package vista;

import dao.UsuarioDAO;
import modelo.Usuario;
import javax.swing.*;

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

        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setBounds(30, 30, 80, 25);
        txtEmail = new JTextField();
        txtEmail.setBounds(120, 30, 180, 25);

        JLabel lblPassword = new JLabel("Contraseña:");
        lblPassword.setBounds(30, 70, 80, 25);
        txtPassword = new JPasswordField();
        txtPassword.setBounds(120, 70, 180, 25);

        JButton btnLogin = new JButton("Iniciar Sesión");
        btnLogin.setBounds(60, 110, 120, 30);
        JButton btnRegister = new JButton("Registrar");
        btnRegister.setBounds(190, 110, 120, 30);

        add(lblEmail);
        add(txtEmail);
        add(lblPassword);
        add(txtPassword);
        add(btnLogin);
        add(btnRegister);

        btnLogin.addActionListener(e -> iniciarSesion());
        btnRegister.addActionListener(e -> registrar());

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void iniciarSesion() {
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Complete todos los campos.");
            return;
        }

        UsuarioDAO dao = new UsuarioDAO();
        Usuario user = dao.login(email, password);

        if (user != null) {
            this.usuarioLogueado = user;
            this.loginExitoso = true;
            JOptionPane.showMessageDialog(this, "✅ Bienvenido, " + user.getNombre() + "!");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "❌ Email o contraseña incorrectos.");
        }
    }

    private void registrar() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre:");
        if (nombre == null || nombre.trim().isEmpty()) return;

        String email = JOptionPane.showInputDialog(this, "Email:");
        if (email == null || email.trim().isEmpty()) return;

        String telefono = JOptionPane.showInputDialog(this, "Teléfono:");
        String password = JOptionPane.showInputDialog(this, "Contraseña:");

        if (password == null || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "La contraseña es obligatoria.");
            return;
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setTelefono(telefono);
        usuario.setPassword(password);  

        UsuarioDAO dao = new UsuarioDAO();
        if (dao.registrarUsuario(usuario)) {
            JOptionPane.showMessageDialog(this, "✅ Registro exitoso. Ahora puede iniciar sesión.");
        } else {
            JOptionPane.showMessageDialog(this, "❌ Error al registrar. ¿El email ya existe?");
        }
    }

    public Usuario getUsuarioLogueado() {
        return usuarioLogueado;
    }

    public boolean isLoginExitoso() {
        return loginExitoso;
    }
}