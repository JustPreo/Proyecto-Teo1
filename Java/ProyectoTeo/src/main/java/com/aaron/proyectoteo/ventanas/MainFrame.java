package com.aaron.proyectoteo.ventanas;

import com.aaron.proyectoteo.ProyectoTeo;
import com.aaron.proyectoteo.usuario;
import com.aaron.proyectoteo.crud.usuarioCRUD;
import java.awt.*;
import javax.swing.*;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentContainer;
    private final usuario usuarioActual;

    private DashboardPanel dashboardPanel;
    private PresupuestosPanel presupuestosPanel;
    private TransaccionesPanel transaccionesPanel;
    private ObligacionesPanel obligacionesPanel;
    private CategoriasPanel categoriasPanel;
    private ReportesPanel reportesPanel;
    private SidebarPanel sidebarPanel;

    public MainFrame(usuario usuarioActual) {
        usuario perfilActual = usuarioActual;
        try {
            usuario consultado = new usuarioCRUD().sp_consultar_usuario(usuarioActual.id_usuario);
            if (consultado != null) perfilActual = consultado;
        } catch (Exception ignored) {
            // Se conserva el perfil seleccionado si la consulta detallada falla.
        }
        this.usuarioActual = perfilActual;
        setTitle("Sistema de Presupuesto Personal - FinTrack");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 780);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Sidebar Navigation
        sidebarPanel = new SidebarPanel(this::mostrarModulo);
        sidebarPanel.setUsuarioActual(usuarioActual);
        sidebarPanel.setOnEditProfile(this::editarPerfil);
        sidebarPanel.setOnDeactivateProfile(this::desactivarPerfil);
        sidebarPanel.setOnLogout(() -> {
            dispose();
            ProyectoTeo.mostrarLogin();
        });
        add(sidebarPanel, BorderLayout.WEST);

        // Center Area with CardLayout
        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(UITheme.CONTENT_BG);

        dashboardPanel = new DashboardPanel(usuarioActual);
        presupuestosPanel = new PresupuestosPanel(usuarioActual);
        transaccionesPanel = new TransaccionesPanel(usuarioActual);
        obligacionesPanel = new ObligacionesPanel(usuarioActual);
        categoriasPanel = new CategoriasPanel(usuarioActual);
        reportesPanel = new ReportesPanel(usuarioActual);

        contentContainer.add(dashboardPanel, "Dashboard");
        contentContainer.add(presupuestosPanel, "Presupuestos");
        contentContainer.add(transaccionesPanel, "Transacciones");
        contentContainer.add(obligacionesPanel, "Obligaciones");
        contentContainer.add(categoriasPanel, "Categorias");
        contentContainer.add(reportesPanel, "Reportes");

        add(contentContainer, BorderLayout.CENTER);
    }

    private void editarPerfil() {
        JDialog dialogo = new JDialog(this, "Editar perfil", true);
        dialogo.setSize(420, 320);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout());

        JPanel formulario = new JPanel(new GridLayout(5, 2, 10, 10));
        formulario.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JTextField nombre = new JTextField(usuarioActual.nombre);
        JTextField apellido = new JTextField(usuarioActual.apellido);
        JTextField correo = new JTextField(usuarioActual.correo_electronico);
        JTextField salario = new JTextField(String.valueOf(usuarioActual.salario_base));
        formulario.add(new JLabel("Nombre:"));
        formulario.add(nombre);
        formulario.add(new JLabel("Apellido:"));
        formulario.add(apellido);
        formulario.add(new JLabel("Correo:"));
        formulario.add(correo);
        formulario.add(new JLabel("Salario base:"));
        formulario.add(salario);
        formulario.add(new JLabel("Estado:"));
        formulario.add(new JLabel(usuarioActual.estado ? "Activo" : "Inactivo"));

        JButton guardar = UITheme.createPrimaryButton("Guardar cambios");
        guardar.addActionListener(e -> {
            try {
                new usuarioCRUD().sp_actualizar_usuario(usuarioActual.id_usuario,
                        nombre.getText().trim(), apellido.getText().trim(), correo.getText().trim(),
                        Double.parseDouble(salario.getText().trim()),
                        usuarioActual.nombre + " " + usuarioActual.apellido);
                usuarioActual.nombre = nombre.getText().trim();
                usuarioActual.apellido = apellido.getText().trim();
                usuarioActual.correo_electronico = correo.getText().trim();
                usuarioActual.salario_base = Double.parseDouble(salario.getText().trim());
                sidebarPanel.setUsuarioActual(usuarioActual);
                dialogo.dispose();
                JOptionPane.showMessageDialog(this, "Perfil actualizado correctamente.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialogo, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialogo.add(formulario, BorderLayout.CENTER);
        dialogo.add(guardar, BorderLayout.SOUTH);
        dialogo.setVisible(true);
    }

    private void desactivarPerfil() {
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Deseas desactivar tu usuario? La operación conservará el historial.",
                "Confirmar desactivación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opcion != JOptionPane.YES_OPTION) return;
        try {
            new usuarioCRUD().sp_eliminar_usuario(usuarioActual.id_usuario,
                    usuarioActual.nombre + " " + usuarioActual.apellido);
            dispose();
            ProyectoTeo.mostrarLogin();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void mostrarModulo(String moduleName) {
        cardLayout.show(contentContainer, moduleName);

        // Actualizaciones automáticas de datos al cambiar de vista
        if ("Dashboard".equals(moduleName)) {
            dashboardPanel.recargarDatos();
        } else if ("Presupuestos".equals(moduleName)) {
            presupuestosPanel.cargarPresupuestos();
        } else if ("Transacciones".equals(moduleName)) {
            transaccionesPanel.cargarFiltros();
        } else if ("Obligaciones".equals(moduleName)) {
            obligacionesPanel.cargarObligaciones();
        } else if ("Categorias".equals(moduleName)) {
            categoriasPanel.cargarCategorias();
        } else if ("Reportes".equals(moduleName)) {
            reportesPanel.cargarReporteSeleccionado();
        }
    }
}
