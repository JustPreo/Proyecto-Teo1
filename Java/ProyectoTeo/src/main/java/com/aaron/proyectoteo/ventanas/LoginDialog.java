package com.aaron.proyectoteo.ventanas;

import com.aaron.proyectoteo.crud.usuarioCRUD;
import com.aaron.proyectoteo.usuario;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginDialog extends JDialog {

    public interface LoginCallback {
        void onUserSelected(usuario user);
    }

    private JComboBox<usuario> cmbUsuarios;
    private LoginCallback callback;
    private usuarioCRUD uCrud = new usuarioCRUD();

    public LoginDialog(Frame parent, LoginCallback callback) {
        super(parent, "Iniciar Sesión - FinTrack", true);
        this.callback = callback;

        setSize(420, 270);
        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initComponents();
        cargarUsuarios();
    }

    private void initComponents() {
        JPanel content = new JPanel(new BorderLayout(16, 16));
        content.setBackground(UITheme.CONTENT_BG);
        content.setBorder(new EmptyBorder(22, 28, 22, 28));

        // Header Panel
        JPanel pnlHeader = new JPanel(new GridLayout(2, 1, 0, 6));
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Bienvenido a FinTrack", JLabel.CENTER);
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JLabel lblSub = new JLabel("Selecciona tu perfil de usuario para ingresar", JLabel.CENTER);
        lblSub.setFont(UITheme.FONT_REGULAR);
        lblSub.setForeground(UITheme.TEXT_SECONDARY);

        pnlHeader.add(lblTitle);
        pnlHeader.add(lblSub);
        content.add(pnlHeader, BorderLayout.NORTH);

        // Center Panel (Card with ComboBox)
        JPanel pnlCenter = new JPanel(new BorderLayout(0, 10));
        pnlCenter.setBackground(UITheme.CARD_BG);
        pnlCenter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblSelect = new JLabel("Usuario:");
        lblSelect.setFont(UITheme.FONT_BOLD);
        lblSelect.setForeground(UITheme.TEXT_PRIMARY);

        cmbUsuarios = new JComboBox<>();
        cmbUsuarios.setPreferredSize(new Dimension(300, 26));
        UITheme.styleComboBox(cmbUsuarios);

        pnlCenter.add(lblSelect, BorderLayout.NORTH);
        pnlCenter.add(cmbUsuarios, BorderLayout.CENTER);

        content.add(pnlCenter, BorderLayout.CENTER);

        // Buttons Panel
        JPanel pnlButtons = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlButtons.setOpaque(false);

        JButton btnNuevo = UITheme.createSecondaryButton("+ Crear Usuario");
        btnNuevo.addActionListener(e -> abrirModalNuevoUsuario());

        JButton btnIngresar = UITheme.createPrimaryButton("Ingresar ➜");
        btnIngresar.addActionListener(e -> {
            usuario selected = (usuario) cmbUsuarios.getSelectedItem();
            if (selected != null) {
                dispose();
                if (callback != null) callback.onUserSelected(selected);
            } else {
                JOptionPane.showMessageDialog(this, "Por favor crea o selecciona un usuario.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });

        pnlButtons.add(btnNuevo);
        pnlButtons.add(btnIngresar);
        content.add(pnlButtons, BorderLayout.SOUTH);

        setContentPane(content);
    }

    private void cargarUsuarios() {
        cmbUsuarios.removeAllItems();
        try {
            ArrayList<usuario> lista = uCrud.listar();
            for (usuario u : lista) {
                if (u.estado) {
                    cmbUsuarios.addItem(u);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al conectar con la base de datos: " + e.getMessage(), "Error DB", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirModalNuevoUsuario() {
        JDialog dlg = new JDialog(this, "Registrar Nuevo Usuario", true);
        dlg.setSize(380, 360);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 12));
        form.setBackground(UITheme.CARD_BG);
        form.setBorder(new EmptyBorder(24, 24, 24, 24));

        JTextField txtNombre = new JTextField();
        JTextField txtApellido = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtSalario = new JTextField("0.00");

        JLabel l1 = new JLabel("Nombre:"); l1.setForeground(UITheme.TEXT_PRIMARY);
        JLabel l2 = new JLabel("Apellido:"); l2.setForeground(UITheme.TEXT_PRIMARY);
        JLabel l3 = new JLabel("Correo:"); l3.setForeground(UITheme.TEXT_PRIMARY);
        JLabel l4 = new JLabel("Salario Base (L):"); l4.setForeground(UITheme.TEXT_PRIMARY);

        form.add(l1); form.add(txtNombre);
        form.add(l2); form.add(txtApellido);
        form.add(l3); form.add(txtEmail);
        form.add(l4); form.add(txtSalario);

        JButton btnGuardar = UITheme.createPrimaryButton("Guardar Usuario");
        btnGuardar.addActionListener(e -> {
            try {
                uCrud.sp_insertar_usuario(
                    txtNombre.getText().trim(),
                    txtApellido.getText().trim(),
                    txtEmail.getText().trim(),
                    Double.parseDouble(txtSalario.getText().trim()),
                    "System"
                );
                JOptionPane.showMessageDialog(dlg, "Usuario registrado exitosamente");
                dlg.dispose();
                cargarUsuarios();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error al registrar usuario: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnGuardar, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
