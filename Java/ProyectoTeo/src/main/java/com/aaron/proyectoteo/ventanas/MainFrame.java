package com.aaron.proyectoteo.ventanas;

import com.aaron.proyectoteo.ProyectoTeo;
import com.aaron.proyectoteo.usuario;
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
    private ReporteIngresosGastosPanel reporteIngresosGastosPanel;

    public MainFrame(usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
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
        SidebarPanel sidebar = new SidebarPanel(this::mostrarModulo);
        sidebar.setUsuarioActual(usuarioActual);
        sidebar.setOnLogout(() -> {
            dispose();
            ProyectoTeo.mostrarLogin();
        });
        add(sidebar, BorderLayout.WEST);

        // Center Area with CardLayout
        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(UITheme.CONTENT_BG);

        dashboardPanel = new DashboardPanel(usuarioActual);
        presupuestosPanel = new PresupuestosPanel(usuarioActual);
        transaccionesPanel = new TransaccionesPanel(usuarioActual);
        obligacionesPanel = new ObligacionesPanel(usuarioActual);
        categoriasPanel = new CategoriasPanel(usuarioActual);
        reporteIngresosGastosPanel = new ReporteIngresosGastosPanel(usuarioActual);

        contentContainer.add(dashboardPanel, "Dashboard");
        contentContainer.add(presupuestosPanel, "Presupuestos");
        contentContainer.add(transaccionesPanel, "Transacciones");
        contentContainer.add(obligacionesPanel, "Obligaciones");
        contentContainer.add(categoriasPanel, "Categorias");
        contentContainer.add(reporteIngresosGastosPanel, "Reportes");

        add(contentContainer, BorderLayout.CENTER);
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
            reporteIngresosGastosPanel.cargarReporte();
        }
    }
}
