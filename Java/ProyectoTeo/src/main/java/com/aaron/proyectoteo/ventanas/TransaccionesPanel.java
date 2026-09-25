package com.aaron.proyectoteo.ventanas;

import com.aaron.proyectoteo.crud.*;
import com.aaron.proyectoteo.presupuesto;
import com.aaron.proyectoteo.subcategoria;
import com.aaron.proyectoteo.transaccion;
import com.aaron.proyectoteo.usuario;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import org.jdatepicker.JDatePicker;

public class TransaccionesPanel extends JPanel {

    private JComboBox<presupuesto> cmbPresupuestos;
    private JComboBox<String> cmbTipoFiltro;
    private JTable tblTransacciones;
    private DefaultTableModel modelTransacciones;

    private transaccionCRUD tCrud = new transaccionCRUD();
    private presupuestoCRUD pCrud = new presupuestoCRUD();
    private subcategoriaCRUD subCrud = new subcategoriaCRUD();

    private final usuario usuarioActual;
    private final String nombreAuditor;

    public TransaccionesPanel(usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        this.nombreAuditor = usuarioActual.nombre + " " + usuarioActual.apellido;
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
        cmbPresupuestos.setPreferredSize(new Dimension(240, 30));
        UITheme.styleComboBox(cmbPresupuestos);
        cmbPresupuestos.addActionListener(e -> filtrarTransacciones());

        cmbTipoFiltro = new JComboBox<>(new String[]{"Todos los tipos", "1 - Ingreso", "2 - Gasto", "3 - Ahorro"});
        cmbTipoFiltro.setPreferredSize(new Dimension(160, 30));
        UITheme.styleComboBox(cmbTipoFiltro);
        cmbTipoFiltro.addActionListener(e -> filtrarTransacciones());

        filterContainer.add(lblTitle);
        filterContainer.add(Box.createHorizontalStrut(15));
        filterContainer.add(new JLabel("Presupuesto:"));
        filterContainer.add(cmbPresupuestos);
        filterContainer.add(new JLabel("Tipo:"));
        filterContainer.add(cmbTipoFiltro);

        JButton btnNueva = UITheme.createPrimaryButton("+ Nueva Transacción");
        btnNueva.addActionListener(e -> abrirModalTransaccion(null));

        JButton btnEditar = UITheme.createSecondaryButton("✎ Editar Seleccionada");
        btnEditar.addActionListener(e -> editarTransaccionSeleccionada());

        JButton btnEliminar = UITheme.createSecondaryButton("🗑️ Eliminar Seleccionada");
        btnEliminar.addActionListener(e -> eliminarTransaccionSeleccionada());

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBtns.setOpaque(false);
        rightBtns.add(btnEliminar);
        rightBtns.add(btnEditar);
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
        scroll.getViewport().setBackground(UITheme.CARD_BG);
        tableContainer.add(scroll, BorderLayout.CENTER);

        add(tableContainer, BorderLayout.CENTER);
    }

    public void cargarFiltros() {
        cmbPresupuestos.removeAllItems();
        try {
            ArrayList<presupuesto> lista = pCrud.listarPorUsuario(usuarioActual.id_usuario, null);
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
            // Llamada exclusiva a sp_listar_transacciones_presupuesto vía DAO
            ArrayList<transaccion> lista = tCrud.listarPorPresupuesto(p.id_presupuesto, tipo, null, null, null);
            for (transaccion t : lista) {
                String tpStr = t.tipo == 1 ? "Ingreso" : (t.tipo == 2 ? "Gasto" : "Ahorro");

                modelTransacciones.addRow(new Object[]{
                    t.id_transaccion,
                    t.fecha,
                    t.mes + "/" + t.anio,
                    tpStr,
                    "Sub #" + t.id_subcategoria,
                    t.descripcion,
                    String.format("L %.2f", t.monto),
                    t.metodo_pago,
                    t.numero_factura != null ? t.numero_factura : "-"
                });
            }
        } catch (Exception e) {
            // Ignorar
        }
    }

    private void editarTransaccionSeleccionada() {
        int row = tblTransacciones.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una transacción en la tabla.");
            return;
        }
        int idTrans = (int) modelTransacciones.getValueAt(row, 0);
        try {
            transaccion t = tCrud.sp_consultar_transaccion(idTrans);
            if (t == null) {
                JOptionPane.showMessageDialog(this, "No se pudo cargar la transacción.");
                return;
            }
            abrirModalTransaccion(t);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar transacción: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirModalTransaccion(transaccion t) {
        presupuesto p = (presupuesto) cmbPresupuestos.getSelectedItem();
        if (p == null && t == null) {
            JOptionPane.showMessageDialog(this, "Selecciona o crea un presupuesto primero.");
            return;
        }

        boolean esEdicion = t != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), esEdicion ? "Editar Transacción" : "Registrar Transacción", true);
        dlg.setSize(440, 520);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(9, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JComboBox<subcategoria> cmbSub = new JComboBox<>();
        UITheme.styleComboBox(cmbSub);
        try {
            ArrayList<subcategoria> subs = subCrud.listarTodas(usuarioActual.id_usuario);
            for (subcategoria s : subs) cmbSub.addItem(s);
            if (esEdicion) {
                for (int i = 0; i < cmbSub.getItemCount(); i++) {
                    if (cmbSub.getItemAt(i).id_subcategoria == t.id_subcategoria) {
                        cmbSub.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (Exception e) {}

        JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"1 - Ingreso", "2 - Gasto", "3 - Ahorro"});
        UITheme.styleComboBox(cmbTipo);
        if (esEdicion) cmbTipo.setSelectedIndex(t.tipo - 1);

        JTextField txtDesc = new JTextField(esEdicion ? t.descripcion : "");
        JTextField txtMonto = new JTextField(esEdicion ? String.valueOf(t.monto) : "0.00");
        JDatePicker selectorFecha = DatePickerUtils.crear(esEdicion ? t.fecha : LocalDate.now());
        JComboBox<String> cmbMetodo = new JComboBox<>(new String[]{"efectivo", "tarjeta_debito", "tarjeta_credito", "transferencia"});
        UITheme.styleComboBox(cmbMetodo);
        if (esEdicion && t.metodo_pago != null) cmbMetodo.setSelectedItem(t.metodo_pago);
        JTextField txtFactura = new JTextField(esEdicion && t.numero_factura != null ? t.numero_factura : "");
        JTextField txtObs = new JTextField(esEdicion && t.observaciones != null ? t.observaciones : "");

        form.add(new JLabel("Subcategoría:"));
        form.add(cmbSub);
        form.add(new JLabel("Tipo:"));
        form.add(cmbTipo);
        form.add(new JLabel("Descripción:"));
        form.add(txtDesc);
        form.add(new JLabel("Monto (L):"));
        form.add(txtMonto);
        form.add(new JLabel("Fecha:"));
        form.add(selectorFecha);
        form.add(new JLabel("Método de Pago:"));
        form.add(cmbMetodo);
        form.add(new JLabel("No. Factura (opcional):"));
        form.add(txtFactura);
        form.add(new JLabel("Observaciones:"));
        form.add(txtObs);

        JButton btnGuardar = UITheme.createPrimaryButton(esEdicion ? "Guardar Cambios" : "Registrar Transacción");
        btnGuardar.addActionListener(e -> {
            subcategoria sub = (subcategoria) cmbSub.getSelectedItem();
            if (sub == null) return;
            try {
                LocalDate f = DatePickerUtils.obtener(selectorFecha);
                if (f == null) {
                    throw new IllegalArgumentException("Selecciona una fecha.");
                }
                short tipo = (short) (cmbTipo.getSelectedIndex() + 1);

                if (esEdicion) {
                    tCrud.sp_actualizar_transaccion(
                        t.id_transaccion,
                        sub.id_subcategoria,
                        t.id_obligacion,
                        f.getYear(),
                        (short) f.getMonthValue(),
                        tipo,
                        txtDesc.getText().trim(),
                        Double.parseDouble(txtMonto.getText().trim()),
                        f,
                        (String) cmbMetodo.getSelectedItem(),
                        txtFactura.getText().trim().isEmpty() ? null : txtFactura.getText().trim(),
                        txtObs.getText().trim().isEmpty() ? null : txtObs.getText().trim(),
                        nombreAuditor
                    );
                } else {
                    // Las transacciones sin obligación usan el CRUD básico; las cargas
                    // masivas y los casos completos siguen usando sp_registrar_transaccion_completa.
                    tCrud.sp_insertar_transaccion(
                        usuarioActual.id_usuario,
                        p.id_presupuesto,
                        sub.id_subcategoria,
                        null,
                        f.getYear(),
                        (short) f.getMonthValue(),
                        tipo,
                        txtDesc.getText().trim(),
                        Double.parseDouble(txtMonto.getText().trim()),
                        f,
                        (String) cmbMetodo.getSelectedItem(),
                        txtFactura.getText().trim().isEmpty() ? null : txtFactura.getText().trim(),
                        txtObs.getText().trim().isEmpty() ? null : txtObs.getText().trim(),
                        nombreAuditor
                    );
                }
                JOptionPane.showMessageDialog(dlg, esEdicion ? "Transacción actualizada exitosamente." : "Transacción registrada exitosamente.");
                dlg.dispose();
                filtrarTransacciones();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.add(form, BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnCancelar = UITheme.createSecondaryButton("Cancelar");
        btnCancelar.addActionListener(e -> dlg.dispose());
        acciones.add(btnCancelar);
        acciones.add(btnGuardar);
        dlg.add(acciones, BorderLayout.SOUTH);
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
