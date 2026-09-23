package com.aaron.proyectoteo.ventanas;

import com.aaron.proyectoteo.usuario;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** Contenedor de los reportes disponibles. */
public class ReportesPanel extends JPanel {

    private static final String REPORTE_INGRESOS_GASTOS = "ingresosGastos";
    private static final String REPORTE_GASTOS_CATEGORIA = "gastosCategoria";
    private static final String REPORTE_CUMPLIMIENTO = "cumplimiento";
    private static final String REPORTE_OBLIGACIONES = "obligaciones";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contenido = new JPanel(cardLayout);
    private final ReporteIngresosGastosPanel reporteIngresosGastos;
    private final ReporteGastosCategoriaPanel reporteGastosCategoria;
    private final ReporteCumplimientoPanel reporteCumplimiento;
    private final ReporteObligacionesPanel reporteObligaciones;

    public ReportesPanel(usuario usuarioActual) {
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.CONTENT_BG);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel selectorPanel = new JPanel(new BorderLayout(12, 0));
        selectorPanel.setOpaque(false);
        JLabel etiqueta = new JLabel("Tipo de reporte:");
        etiqueta.setForeground(UITheme.TEXT_LIGHT);
        etiqueta.setFont(UITheme.FONT_BOLD);

        JComboBox<String> selector = new JComboBox<>(new String[]{
            "Reporte 1 - Ingresos vs. gastos",
            "Reporte 2 - Gastos por categoría",
            "Reporte 3 - Cumplimiento del presupuesto",
            "Reporte 4 - Obligaciones fijas"
        });
        selector.setPreferredSize(new Dimension(300, 34));
        UITheme.styleComboBox(selector);
        selector.addActionListener(e -> {
            if (selector.getSelectedIndex() == 0) {
                cardLayout.show(contenido, REPORTE_INGRESOS_GASTOS);
            } else if (selector.getSelectedIndex() == 1) {
                cardLayout.show(contenido, REPORTE_GASTOS_CATEGORIA);
            } else if (selector.getSelectedIndex() == 2) {
                cardLayout.show(contenido, REPORTE_CUMPLIMIENTO);
            } else {
                cardLayout.show(contenido, REPORTE_OBLIGACIONES);
            }
        });

        selectorPanel.add(etiqueta, BorderLayout.WEST);
        selectorPanel.add(selector, BorderLayout.CENTER);
        add(selectorPanel, BorderLayout.NORTH);

        reporteIngresosGastos = new ReporteIngresosGastosPanel(usuarioActual);
        reporteGastosCategoria = new ReporteGastosCategoriaPanel(usuarioActual);
        reporteCumplimiento = new ReporteCumplimientoPanel(usuarioActual);
        reporteObligaciones = new ReporteObligacionesPanel(usuarioActual);
        contenido.setOpaque(false);
        contenido.add(reporteIngresosGastos, REPORTE_INGRESOS_GASTOS);
        contenido.add(reporteGastosCategoria, REPORTE_GASTOS_CATEGORIA);
        contenido.add(reporteCumplimiento, REPORTE_CUMPLIMIENTO);
        contenido.add(reporteObligaciones, REPORTE_OBLIGACIONES);
        add(contenido, BorderLayout.CENTER);
    }

    public void cargarReporteSeleccionado() {
        // Ambos reportes se actualizan al entrar para reflejar cambios recientes.
        reporteIngresosGastos.cargarReporte();
        reporteGastosCategoria.cargarReporte();
        reporteCumplimiento.cargarReporte();
        reporteObligaciones.cargarReporte();
    }
}
