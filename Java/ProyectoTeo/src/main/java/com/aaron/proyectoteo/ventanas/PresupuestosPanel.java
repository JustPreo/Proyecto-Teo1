package com.aaron.proyectoteo.ventanas;

import com.aaron.proyectoteo.Funciones;
import com.aaron.proyectoteo.crud.*;
import com.aaron.proyectoteo.presupuesto;
import com.aaron.proyectoteo.presupuesto_detalle;
import com.aaron.proyectoteo.subcategoria;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class PresupuestosPanel extends JPanel {

    private JComboBox<presupuesto> cmbPresupuestos;
    private JTable tblDetalles;
    private DefaultTableModel modelDetalles;
    private JLabel lblTotalIngresos, lblTotalGastos, lblTotalAhorro, lblEstado;

    private presupuestoCRUD pCrud = new presupuestoCRUD();
    private presupuesto_detalleCRUD pdCrud = new presupuesto_detalleCRUD();
    private subcategoriaCRUD subCrud = new subcategoriaCRUD();

    public PresupuestosPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.CONTENT_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        initComponents();
        cargarPresupuestos();
    }

    private void initComponents() {
        // Top Action Bar
        JPanel topPanel = new JPanel(new BorderLayout(15, 0));
        topPanel.setOpaque(false);

        JPanel titleContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleContainer.setOpaque(false);
        JLabel lblTitle = new JLabel("Gestión de Presupuestos");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        cmbPresupuestos = new JComboBox<>();
        cmbPresupuestos.setPreferredSize(new Dimension(320, 36));
        cmbPresupuestos.setFont(UITheme.FONT_REGULAR);
        cmbPresupuestos.addActionListener(e -> seleccionarPresupuesto());

        titleContainer.add(lblTitle);
        titleContainer.add(cmbPresupuestos);

        JPanel btnContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnContainer.setOpaque(false);

        JButton btnNuevo = UITheme.createPrimaryButton("+ Nuevo Presupuesto");
        btnNuevo.addActionListener(e -> abrirModalNuevoPresupuesto());

        JButton btnAgregarDetalle = UITheme.createSecondaryButton("+ Asignar Subcategoría");
        btnAgregarDetalle.addActionListener(e -> abrirModalNuevoDetalle());

        JButton btnCerrar = UITheme.createSecondaryButton("🔒 Cerrar Presupuesto");
        btnCerrar.addActionListener(e -> cerrarPresupuestoActual());

        btnContainer.add(btnAgregarDetalle);
        btnContainer.add(btnNuevo);
        btnContainer.add(btnCerrar);

        topPanel.add(titleContainer, BorderLayout.WEST);
        topPanel.add(btnContainer, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Content
        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setOpaque(false);

        // Summary Badges
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        summaryPanel.setOpaque(false);
        summaryPanel.setPreferredSize(new Dimension(800, 80));

        lblTotalIngresos = new JLabel("Ingresos: L 0.00", JLabel.CENTER);
        lblTotalIngresos.setFont(UITheme.FONT_BOLD);
        lblTotalIngresos.setForeground(UITheme.SUCCESS);
        lblTotalIngresos.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        lblTotalIngresos.setOpaque(true);
        lblTotalIngresos.setBackground(UITheme.CARD_BG);

        lblTotalGastos = new JLabel("Gastos: L 0.00", JLabel.CENTER);
        lblTotalGastos.setFont(UITheme.FONT_BOLD);
        lblTotalGastos.setForeground(UITheme.DANGER);
        lblTotalGastos.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        lblTotalGastos.setOpaque(true);
        lblTotalGastos.setBackground(UITheme.CARD_BG);

        lblTotalAhorro = new JLabel("Ahorro: L 0.00", JLabel.CENTER);
        lblTotalAhorro.setFont(UITheme.FONT_BOLD);
        lblTotalAhorro.setForeground(UITheme.PURPLE);
        lblTotalAhorro.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        lblTotalAhorro.setOpaque(true);
        lblTotalAhorro.setBackground(UITheme.CARD_BG);

        lblEstado = new JLabel("Estado: -", JLabel.CENTER);
        lblEstado.setFont(UITheme.FONT_BOLD);
        lblEstado.setForeground(UITheme.PRIMARY);
        lblEstado.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        lblEstado.setOpaque(true);
        lblEstado.setBackground(UITheme.CARD_BG);

        summaryPanel.add(lblTotalIngresos);
        summaryPanel.add(lblTotalGastos);
        summaryPanel.add(lblTotalAhorro);
        summaryPanel.add(lblEstado);

        centerPanel.add(summaryPanel, BorderLayout.NORTH);

        // Table
        JPanel tableContainer = new JPanel(new BorderLayout(0, 10));
        tableContainer.setBackground(UITheme.CARD_BG);
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel lblSubTitle = new JLabel("Desglose y Ejecución por Subcategoría");
        lblSubTitle.setFont(UITheme.FONT_SUBTITLE);
        lblSubTitle.setForeground(UITheme.TEXT_PRIMARY);
        tableContainer.add(lblSubTitle, BorderLayout.NORTH);

        String[] cols = {"ID Detalle", "Subcategoría", "Monto Presupuestado", "Monto Ejecutado", "Balance Restante", "% Ejecutado", "Justificación"};
        modelDetalles = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblDetalles = new JTable(modelDetalles);
        UITheme.styleTable(tblDetalles);
        JScrollPane scroll = new JScrollPane(tblDetalles);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.CARD_BG);
        tableContainer.add(scroll, BorderLayout.CENTER);

        centerPanel.add(tableContainer, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    public void cargarPresupuestos() {
        cmbPresupuestos.removeAllItems();
        try {
            ArrayList<presupuesto> lista = pCrud.listarTodos();
            for (presupuesto p : lista) {
                cmbPresupuestos.addItem(p);
            }
            if (cmbPresupuestos.getItemCount() > 0) {
                cmbPresupuestos.setSelectedIndex(0);
                seleccionarPresupuesto();
            }
        } catch (Exception e) {
            // Error handling
        }
    }

    private void seleccionarPresupuesto() {
        presupuesto p = (presupuesto) cmbPresupuestos.getSelectedItem();
        if (p == null) return;

        lblTotalIngresos.setText(String.format("Ingresos: L %.2f", p.total_ingresos));
        lblTotalGastos.setText(String.format("Gastos: L %.2f", p.total_gastos));
        lblTotalAhorro.setText(String.format("Ahorro: L %.2f", p.total_ahorro));
        
        String st = p.estado_presupuesto == 1 ? "Activo" : (p.estado_presupuesto == 2 ? "Cerrado" : "Borrador");
        lblEstado.setText("Estado: " + st);

        // Cargar detalles usando el stored procedure
        modelDetalles.setRowCount(0);
        try {
            ArrayList<presupuesto_detalle> lista = pdCrud.listarPorPresupuesto(p.id_presupuesto);
            for (presupuesto_detalle pd : lista) {
                int idDetalle = pd.id_presupuesto_detalle;
                int idSub = pd.id_subcategoria;
                String subNombre = pd.nombre_subcategoria != null ? pd.nombre_subcategoria : ("Sub #" + idSub);
                double montoPres = pd.monto_mensual;
                String just = pd.observaciones;

                double montoEjec = Funciones.fn_calcular_monto_ejecutado(p.ano_inicio, p.mes_inicio, idSub);
                double balance = Funciones.fn_obtener_balance_subcategoria(p.id_presupuesto, idSub, p.ano_inicio, p.mes_inicio);
                double pct = Funciones.fn_calcular_porcentaje_ejecutado(p.id_presupuesto, idSub, p.ano_inicio, p.mes_inicio);

                modelDetalles.addRow(new Object[]{
                    idDetalle,
                    subNombre,
                    String.format("L %.2f", montoPres),
                    String.format("L %.2f", montoEjec),
                    String.format("L %.2f", balance),
                    String.format("%.1f %%", pct),
                    just != null ? just : ""
                });
            }
        } catch (Exception e) {
            // Ignorar
        }
    }

    private void abrirModalNuevoPresupuesto() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Crear Nuevo Presupuesto", true);
        dlg.setSize(420, 520);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(9, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtNombre = new JTextField();
        JTextField txtAnoInicio = new JTextField(String.valueOf(LocalDate.now().getYear()));
        JTextField txtMesInicio = new JTextField(String.valueOf(LocalDate.now().getMonthValue()));
        JTextField txtAnoFin = new JTextField(String.valueOf(LocalDate.now().getYear()));
        JTextField txtMesFin = new JTextField(String.valueOf(LocalDate.now().getMonthValue()));
        JTextField txtIngresos = new JTextField("0.00");
        JTextField txtGastos = new JTextField("0.00");
        JTextField txtAhorro = new JTextField("0.00");

        form.add(new JLabel("Nombre Descriptivo:"));
        form.add(txtNombre);
        form.add(new JLabel("Año Inicio:"));
        form.add(txtAnoInicio);
        form.add(new JLabel("Mes Inicio (1-12):"));
        form.add(txtMesInicio);
        form.add(new JLabel("Año Fin:"));
        form.add(txtAnoFin);
        form.add(new JLabel("Mes Fin (1-12):"));
        form.add(txtMesFin);
        form.add(new JLabel("Total Ingresos (L):"));
        form.add(txtIngresos);
        form.add(new JLabel("Total Gastos (L):"));
        form.add(txtGastos);
        form.add(new JLabel("Total Ahorro (L):"));
        form.add(txtAhorro);

        JButton btnGuardar = UITheme.createPrimaryButton("Guardar");
        btnGuardar.addActionListener(e -> {
            try {
                pCrud.sp_insertar_presupuesto(
                    1,
                    txtNombre.getText().trim(),
                    Integer.parseInt(txtAnoInicio.getText().trim()),
                    Short.parseShort(txtMesInicio.getText().trim()),
                    Integer.parseInt(txtAnoFin.getText().trim()),
                    Short.parseShort(txtMesFin.getText().trim()),
                    Double.parseDouble(txtIngresos.getText().trim()),
                    Double.parseDouble(txtGastos.getText().trim()),
                    Double.parseDouble(txtAhorro.getText().trim()),
                    "Admin"
                );
                JOptionPane.showMessageDialog(dlg, "Presupuesto creado con éxito");
                dlg.dispose();
                cargarPresupuestos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error al crear presupuesto: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnGuardar, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void abrirModalNuevoDetalle() {
        presupuesto p = (presupuesto) cmbPresupuestos.getSelectedItem();
        if (p == null) return;

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Asignar Subcategoría a Presupuesto", true);
        dlg.setSize(380, 280);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JComboBox<subcategoria> cmbSub = new JComboBox<>();
        try {
            ArrayList<subcategoria> subs = subCrud.listarTodas();
            for (subcategoria s : subs) cmbSub.addItem(s);
        } catch (Exception e) {}

        JTextField txtMonto = new JTextField("0.00");
        JTextField txtJust = new JTextField();

        form.add(new JLabel("Subcategoría:"));
        form.add(cmbSub);
        form.add(new JLabel("Monto Mensual (L):"));
        form.add(txtMonto);
        form.add(new JLabel("Justificación:"));
        form.add(txtJust);

        JButton btnGuardar = UITheme.createPrimaryButton("Guardar Asignación");
        btnGuardar.addActionListener(e -> {
            subcategoria sub = (subcategoria) cmbSub.getSelectedItem();
            if (sub == null) return;
            try {
                pdCrud.sp_insertar_presupuesto_detalle(
                    p.id_presupuesto,
                    sub.id_subcategoria,
                    Double.parseDouble(txtMonto.getText().trim()),
                    txtJust.getText().trim(),
                    "Admin"
                );
                JOptionPane.showMessageDialog(dlg, "Subcategoría asignada correctamente");
                dlg.dispose();
                seleccionarPresupuesto();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnGuardar, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void cerrarPresupuestoActual() {
        presupuesto p = (presupuesto) cmbPresupuestos.getSelectedItem();
        if (p == null) return;

        int opt = JOptionPane.showConfirmDialog(this, "¿Estás seguro de cerrar el presupuesto '" + p.nombre_descriptivo + "'?", "Confirmar Cierre", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                // Cerramos usando el procedure oficial de actualizar presupuesto con estado 2
                pCrud.sp_actualizar_presupuesto(
                    p.id_presupuesto,
                    p.nombre_descriptivo,
                    p.ano_inicio,
                    (short) p.mes_inicio,
                    p.ano_fin,
                    (short) p.mes_fin,
                    p.total_ingresos,
                    p.total_gastos,
                    p.total_ahorro,
                    (short) 2, // Estado cerrado
                    "Admin"
                );
                JOptionPane.showMessageDialog(this, "Presupuesto cerrado con éxito");
                cargarPresupuestos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
