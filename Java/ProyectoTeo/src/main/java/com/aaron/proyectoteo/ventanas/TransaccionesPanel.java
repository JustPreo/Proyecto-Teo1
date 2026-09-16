package com.aaron.proyectoteo.ventanas;

import com.aaron.proyectoteo.crud.*;
import com.aaron.proyectoteo.presupuesto;
import com.aaron.proyectoteo.subcategoria;
import com.aaron.proyectoteo.transaccion;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class TransaccionesPanel extends JPanel {

    private JComboBox<presupuesto> cmbPresupuestos;
    private JComboBox<String> cmbTipoFiltro;
    private JTable tblTransacciones;
    private DefaultTableModel modelTransacciones;

    private transaccionCRUD tCrud = new transaccionCRUD();
    private presupuestoCRUD pCrud = new presupuestoCRUD();
    private subcategoriaCRUD subCrud = new subcategoriaCRUD();

    public TransaccionesPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.CONTENT_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        initComponents();
        cargarFiltros();
    }

    private void initComponents() {
        // Top Action Bar
        JPanel topPanel = new JPanel(new BorderLayout(15, 0));
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Registro de Transacciones");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel filterContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterContainer.setOpaque(false);

        cmbPresupuestos = new JComboBox<>();
        cmbPresupuestos.setPreferredSize(new Dimension(240, 36));
        cmbPresupuestos.addActionListener(e -> filtrarTransacciones());

        cmbTipoFiltro = new JComboBox<>(new String[]{"Todos los tipos", "1 - Ingreso", "2 - Gasto", "3 - Ahorro"});
        cmbTipoFiltro.setPreferredSize(new Dimension(160, 36));
        cmbTipoFiltro.addActionListener(e -> filtrarTransacciones());

        filterContainer.add(lblTitle);
        filterContainer.add(Box.createHorizontalStrut(15));
        filterContainer.add(new JLabel("Presupuesto:"));
        filterContainer.add(cmbPresupuestos);
        filterContainer.add(new JLabel("Tipo:"));
        filterContainer.add(cmbTipoFiltro);

        JButton btnNueva = UITheme.createPrimaryButton("+ Nueva Transacción");
        btnNueva.addActionListener(e -> abrirModalNuevaTransaccion());

        JButton btnEliminar = UITheme.createSecondaryButton("🗑️ Eliminar Seleccionada");
        btnEliminar.addActionListener(e -> eliminarTransaccionSeleccionada());

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBtns.setOpaque(false);
        rightBtns.add(btnEliminar);
        rightBtns.add(btnNueva);

        topPanel.add(filterContainer, BorderLayout.WEST);
        topPanel.add(rightBtns, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Content - Table
        JPanel tableContainer = new JPanel(new BorderLayout(0, 10));
        tableContainer.setBackground(UITheme.CARD_BG);
        tableContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));

        String[] cols = {"ID", "Fecha", "Año/Mes", "Tipo", "Subcategoría", "Descripción", "Monto (L)", "Método de Pago", "Factura"};
        modelTransacciones = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblTransacciones = new JTable(modelTransacciones);
        UITheme.styleTable(tblTransacciones);
        JScrollPane scroll = new JScrollPane(tblTransacciones);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        tableContainer.add(scroll, BorderLayout.CENTER);

        add(tableContainer, BorderLayout.CENTER);
    }

    public void cargarFiltros() {
        cmbPresupuestos.removeAllItems();
        try {
            ArrayList<presupuesto> lista = pCrud.listarTodos();
            for (presupuesto p : lista) {
                cmbPresupuestos.addItem(p);
            }
            if (cmbPresupuestos.getItemCount() > 0) {
                cmbPresupuestos.setSelectedIndex(0);
                filtrarTransacciones();
            }
        } catch (Exception e) {}
    }

    private void filtrarTransacciones() {
        presupuesto p = (presupuesto) cmbPresupuestos.getSelectedItem();
        if (p == null) return;

        Short tipo = null;
        int idx = cmbTipoFiltro.getSelectedIndex();
        if (idx == 1) tipo = 1;
        else if (idx == 2) tipo = 2;
        else if (idx == 3) tipo = 3;

        modelTransacciones.setRowCount(0);
        try {
            Connection con = com.aaron.proyectoteo.Conexion.obtenerConexion();
            String sql = "SELECT t.id_transaccion, t.fecha, t.ano, t.mes, t.tipo, s.nombre AS sub_nombre, t.descripcion, t.monto, t.metodo_pago, t.numero_factura " +
                         "FROM transaccion t " +
                         "INNER JOIN subcategoria s ON t.id_subcategoria = s.id_subcategoria " +
                         "WHERE t.id_presupuesto = ? " +
                         (tipo != null ? "AND t.tipo = ? " : "") +
                         "ORDER BY t.fecha DESC, t.id_transaccion DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, p.id_presupuesto);
            if (tipo != null) ps.setShort(2, tipo);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                short tp = rs.getShort("tipo");
                String tpStr = tp == 1 ? "Ingreso" : (tp == 2 ? "Gasto" : "Ahorro");

                modelTransacciones.addRow(new Object[]{
                    rs.getInt("id_transaccion"),
                    rs.getDate("fecha"),
                    rs.getInt("mes") + "/" + rs.getInt("ano"),
                    tpStr,
                    rs.getString("sub_nombre"),
                    rs.getString("descripcion"),
                    String.format("L %.2f", rs.getDouble("monto")),
                    rs.getString("metodo_pago"),
                    rs.getString("numero_factura") != null ? rs.getString("numero_factura") : "-"
                });
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            // Ignorar
        }
    }

    private void abrirModalNuevaTransaccion() {
        presupuesto p = (presupuesto) cmbPresupuestos.getSelectedItem();
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Selecciona o crea un presupuesto primero.");
            return;
        }

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Registrar Transacción", true);
        dlg.setSize(440, 520);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(9, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JComboBox<subcategoria> cmbSub = new JComboBox<>();
        try {
            ArrayList<subcategoria> subs = subCrud.listarTodas();
            for (subcategoria s : subs) cmbSub.addItem(s);
        } catch (Exception e) {}

        JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"1 - Ingreso", "2 - Gasto", "3 - Ahorro"});
        JTextField txtDesc = new JTextField();
        JTextField txtMonto = new JTextField("0.00");
        LocalDate now = LocalDate.now();
        JTextField txtFecha = new JTextField(now.toString()); // YYYY-MM-DD
        JComboBox<String> cmbMetodo = new JComboBox<>(new String[]{"efectivo", "tarjeta_debito", "tarjeta_credito", "transferencia"});
        JTextField txtFactura = new JTextField();
        JTextField txtObs = new JTextField();

        form.add(new JLabel("Subcategoría:"));
        form.add(cmbSub);
        form.add(new JLabel("Tipo:"));
        form.add(cmbTipo);
        form.add(new JLabel("Descripción:"));
        form.add(txtDesc);
        form.add(new JLabel("Monto (L):"));
        form.add(txtMonto);
        form.add(new JLabel("Fecha (AAAA-MM-DD):"));
        form.add(txtFecha);
        form.add(new JLabel("Método de Pago:"));
        form.add(cmbMetodo);
        form.add(new JLabel("No. Factura (opcional):"));
        form.add(txtFactura);
        form.add(new JLabel("Observaciones:"));
        form.add(txtObs);

        JButton btnGuardar = UITheme.createPrimaryButton("Registrar Transacción");
        btnGuardar.addActionListener(e -> {
            subcategoria sub = (subcategoria) cmbSub.getSelectedItem();
            if (sub == null) return;
            try {
                LocalDate f = LocalDate.parse(txtFecha.getText().trim());
                short tipo = (short) (cmbTipo.getSelectedIndex() + 1);

                tCrud.sp_insertar_transaccion(
                    1, // usuario admin
                    p.id_presupuesto,
                    sub.id_subcategoria,
                    null, // sin obligacion por defecto
                    f.getYear(),
                    (short) f.getMonthValue(),
                    tipo,
                    txtDesc.getText().trim(),
                    Double.parseDouble(txtMonto.getText().trim()),
                    f,
                    (String) cmbMetodo.getSelectedItem(),
                    txtFactura.getText().trim().isEmpty() ? null : txtFactura.getText().trim(),
                    txtObs.getText().trim().isEmpty() ? null : txtObs.getText().trim(),
                    "Admin"
                );
                JOptionPane.showMessageDialog(dlg, "Transacción registrada exitosamente.");
                dlg.dispose();
                filtrarTransacciones();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error al registrar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnGuardar, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void eliminarTransaccionSeleccionada() {
        int row = tblTransacciones.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una transacción en la tabla.");
            return;
        }

        int idTrans = (int) modelTransacciones.getValueAt(row, 0);
        int opt = JOptionPane.showConfirmDialog(this, "¿Eliminar transacción ID #" + idTrans + "?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                tCrud.sp_eliminar_transaccion(idTrans);
                JOptionPane.showMessageDialog(this, "Transacción eliminada");
                filtrarTransacciones();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
