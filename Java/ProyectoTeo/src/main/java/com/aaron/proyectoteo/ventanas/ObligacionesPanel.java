package com.aaron.proyectoteo.ventanas;

import com.aaron.proyectoteo.Funciones;
import com.aaron.proyectoteo.crud.*;
import com.aaron.proyectoteo.obligacion_fija;
import com.aaron.proyectoteo.subcategoria;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class ObligacionesPanel extends JPanel {

    private JTable tblObligaciones;
    private DefaultTableModel modelObligaciones;
    private JCheckBox chkVigentes;

    private obligacion_fijaCRUD oCrud = new obligacion_fijaCRUD();
    private subcategoriaCRUD subCrud = new subcategoriaCRUD();

    public ObligacionesPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.CONTENT_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        initComponents();
        cargarObligaciones();
    }

    private void initComponents() {
        // Top Action Bar
        JPanel topPanel = new JPanel(new BorderLayout(15, 0));
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Obligaciones y Pagos Fijos");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel leftContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftContainer.setOpaque(false);
        leftContainer.add(lblTitle);

        chkVigentes = new JCheckBox("Solo Vigentes", true);
        chkVigentes.setOpaque(false);
        chkVigentes.setFont(UITheme.FONT_REGULAR);
        chkVigentes.addActionListener(e -> cargarObligaciones());
        leftContainer.add(chkVigentes);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBtns.setOpaque(false);

        JButton btnNueva = UITheme.createPrimaryButton("+ Nueva Obligación");
        btnNueva.addActionListener(e -> abrirModalNuevaObligacion());

        JButton btnDesactivar = UITheme.createSecondaryButton("⏹ Desactivar Seleccionada");
        btnDesactivar.addActionListener(e -> desactivarSeleccionada());

        rightBtns.add(btnDesactivar);
        rightBtns.add(btnNueva);

        topPanel.add(leftContainer, BorderLayout.WEST);
        topPanel.add(rightBtns, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Content - Table
        JPanel tableContainer = new JPanel(new BorderLayout(0, 10));
        tableContainer.setBackground(UITheme.CARD_BG);
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));

        String[] cols = {"ID", "Obligación", "Monto Mensual", "Día Vencimiento", "Días Restantes", "Fecha Inicio", "Fecha Fin", "Estado"};
        modelObligaciones = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblObligaciones = new JTable(modelObligaciones);
        UITheme.styleTable(tblObligaciones);
        JScrollPane scroll = new JScrollPane(tblObligaciones);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        tableContainer.add(scroll, BorderLayout.CENTER);

        add(tableContainer, BorderLayout.CENTER);
    }

    public void cargarObligaciones() {
        modelObligaciones.setRowCount(0);
        try {
            Boolean vigente = chkVigentes.isSelected() ? true : null;
            ArrayList<obligacion_fija> lista = oCrud.listarPorUsuario(1, vigente);
            for (obligacion_fija o : lista) {
                int dias = Funciones.fn_dias_hasta_vencimiento(o.id_obligacion);
                modelObligaciones.addRow(new Object[]{
                    o.id_obligacion,
                    o.nombre,
                    String.format("L %.2f", o.monto_mensual),
                    "Día " + o.dia_vencimiento,
                    dias >= 0 ? dias + " días" : "Vencido/Inactivo",
                    o.fecha_inicio != null ? o.fecha_inicio : "-",
                    o.fecha_fin != null ? o.fecha_fin : "Indefinido",
                    o.estado ? "Vigente" : "Inactivo"
                });
            }
        } catch (Exception e) {}
    }

    private void abrirModalNuevaObligacion() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Registrar Obligación Fija", true);
        dlg.setSize(420, 480);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JComboBox<subcategoria> cmbSub = new JComboBox<>();
        try {
            ArrayList<subcategoria> subs = subCrud.listarTodas();
            for (subcategoria s : subs) cmbSub.addItem(s);
        } catch (Exception e) {}

        JTextField txtNombre = new JTextField();
        JTextField txtDesc = new JTextField();
        JTextField txtMonto = new JTextField("0.00");
        JTextField txtDia = new JTextField("1");
        JTextField txtFechaInicio = new JTextField(LocalDate.now().toString());
        JTextField txtFechaFin = new JTextField();

        form.add(new JLabel("Subcategoría:"));
        form.add(cmbSub);
        form.add(new JLabel("Nombre Obligación:"));
        form.add(txtNombre);
        form.add(new JLabel("Descripción:"));
        form.add(txtDesc);
        form.add(new JLabel("Monto Mensual (L):"));
        form.add(txtMonto);
        form.add(new JLabel("Día de Vencimiento (1-31):"));
        form.add(txtDia);
        form.add(new JLabel("Fecha Inicio (AAAA-MM-DD):"));
        form.add(txtFechaInicio);
        form.add(new JLabel("Fecha Fin (opcional):"));
        form.add(txtFechaFin);

        JButton btnGuardar = UITheme.createPrimaryButton("Guardar Obligación");
        btnGuardar.addActionListener(e -> {
            subcategoria sub = (subcategoria) cmbSub.getSelectedItem();
            if (sub == null) return;
            try {
                LocalDate fi = LocalDate.parse(txtFechaInicio.getText().trim());
                LocalDate ff = txtFechaFin.getText().trim().isEmpty() ? null : LocalDate.parse(txtFechaFin.getText().trim());

                oCrud.sp_insertar_obligacion(
                    1,
                    sub.id_subcategoria,
                    txtNombre.getText().trim(),
                    txtDesc.getText().trim(),
                    Double.parseDouble(txtMonto.getText().trim()),
                    Integer.parseInt(txtDia.getText().trim()),
                    fi,
                    ff,
                    "Admin"
                );
                JOptionPane.showMessageDialog(dlg, "Obligación registrada con éxito");
                dlg.dispose();
                cargarObligaciones();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnGuardar, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void desactivarSeleccionada() {
        int row = tblObligaciones.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una obligación en la tabla.");
            return;
        }

        int id = (int) modelObligaciones.getValueAt(row, 0);
        int opt = JOptionPane.showConfirmDialog(this, "¿Desactivar obligación #" + id + "?", "Confirmar Desactivación", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                oCrud.sp_eliminar_obligacion(id, "Admin");
                JOptionPane.showMessageDialog(this, "Obligación desactivada.");
                cargarObligaciones();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
