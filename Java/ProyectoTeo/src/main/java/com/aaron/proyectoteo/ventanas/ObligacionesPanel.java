package com.aaron.proyectoteo.ventanas;

import com.aaron.proyectoteo.Funciones;
import com.aaron.proyectoteo.crud.*;
import com.aaron.proyectoteo.obligacion_fija;
import com.aaron.proyectoteo.presupuesto;
import com.aaron.proyectoteo.subcategoria;
import com.aaron.proyectoteo.transaccion;
import com.aaron.proyectoteo.usuario;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import org.jdatepicker.JDatePicker;

public class ObligacionesPanel extends JPanel {

    private JTable tblObligaciones;
    private DefaultTableModel modelObligaciones;
    private JCheckBox chkVigentes;

    private obligacion_fijaCRUD oCrud = new obligacion_fijaCRUD();
    private subcategoriaCRUD subCrud = new subcategoriaCRUD();
    private presupuestoCRUD pCrud = new presupuestoCRUD();
    private transaccionCRUD tCrud = new transaccionCRUD();

    private final usuario usuarioActual;
    private final String nombreAuditor;

    public ObligacionesPanel(usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        this.nombreAuditor = usuarioActual.nombre + " " + usuarioActual.apellido;
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
        btnNueva.addActionListener(e -> abrirModalObligacion(null));

        JButton btnEditar = UITheme.createSecondaryButton("✎ Editar Seleccionada");
        btnEditar.addActionListener(e -> editarObligacionSeleccionada());

        JButton btnPagar = UITheme.createSecondaryButton("Pagar Seleccionada");
        btnPagar.addActionListener(e -> pagarSeleccionada());

        JButton btnDesactivar = UITheme.createSecondaryButton("⏹ Desactivar Seleccionada");
        btnDesactivar.addActionListener(e -> desactivarSeleccionada());

        rightBtns.add(btnDesactivar);
        rightBtns.add(btnPagar);
        rightBtns.add(btnEditar);
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
        scroll.getViewport().setBackground(UITheme.CARD_BG);
        tableContainer.add(scroll, BorderLayout.CENTER);

        add(tableContainer, BorderLayout.CENTER);
    }

    public void cargarObligaciones() {
        modelObligaciones.setRowCount(0);
        try {
            Boolean vigente = chkVigentes.isSelected() ? true : null;
            ArrayList<obligacion_fija> lista = oCrud.listarPorUsuario(usuarioActual.id_usuario, vigente);
            for (obligacion_fija o : lista) {
                Integer dias = Funciones.fn_dias_hasta_vencimiento(o.id_obligacion);
                modelObligaciones.addRow(new Object[]{
                    o.id_obligacion,
                    o.nombre,
                    String.format("L %.2f", o.monto_mensual),
                    "Día " + o.dia_vencimiento,
                    dias == null ? "No iniciada" : (dias >= 0 ? dias + " días" : "Vencido/Inactivo"),
                    o.fecha_inicio != null ? o.fecha_inicio : "-",
                    o.fecha_fin != null ? o.fecha_fin : "Indefinido",
                    o.estado ? "Vigente" : "Inactivo"
                });
            }
        } catch (Exception e) {}
    }

    private void editarObligacionSeleccionada() {
        int row = tblObligaciones.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una obligación en la tabla.");
            return;
        }
        int id = (int) modelObligaciones.getValueAt(row, 0);
        try {
            obligacion_fija o = oCrud.sp_consultar_obligacion(id);
            if (o == null) {
                JOptionPane.showMessageDialog(this, "No se pudo cargar la obligación.");
                return;
            }
            abrirModalObligacion(o);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar obligación: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirModalObligacion(obligacion_fija o) {
        boolean esEdicion = o != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), esEdicion ? "Editar Obligación Fija" : "Registrar Obligación Fija", true);
        dlg.setSize(420, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(8, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JComboBox<subcategoria> cmbSub = new JComboBox<>();
        UITheme.styleComboBox(cmbSub);
        try {
            ArrayList<subcategoria> subs = subCrud.listarTodas(usuarioActual.id_usuario);
            for (subcategoria s : subs) cmbSub.addItem(s);
            if (esEdicion) {
                for (int i = 0; i < cmbSub.getItemCount(); i++) {
                    if (cmbSub.getItemAt(i).id_subcategoria == o.id_subcategoria) {
                        cmbSub.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (Exception e) {}

        JTextField txtNombre = new JTextField(esEdicion ? o.nombre : "");
        JTextField txtDesc = new JTextField(esEdicion && o.descripcion != null ? o.descripcion : "");
        JTextField txtMonto = new JTextField(esEdicion ? String.valueOf(o.monto_mensual) : "0.00");
        JTextField txtDia = new JTextField(esEdicion ? String.valueOf(o.dia_vencimiento) : "1");
        JDatePicker selectorFechaInicio = DatePickerUtils.crear(
                esEdicion && o.fecha_inicio != null ? o.fecha_inicio : LocalDate.now());
        JDatePicker selectorFechaFin = DatePickerUtils.crear(esEdicion ? o.fecha_fin : null);
        JCheckBox chkSinFechaFin = new JCheckBox("Sin fecha fin", !esEdicion || o.fecha_fin == null);
        chkSinFechaFin.setOpaque(false);
        chkSinFechaFin.setForeground(UITheme.TEXT_PRIMARY);
        selectorFechaFin.setEnabled(!chkSinFechaFin.isSelected());
        chkSinFechaFin.addActionListener(e -> selectorFechaFin.setEnabled(!chkSinFechaFin.isSelected()));

        JPanel panelFechaFin = new JPanel(new BorderLayout(6, 0));
        panelFechaFin.setOpaque(false);
        panelFechaFin.add(selectorFechaFin, BorderLayout.CENTER);
        panelFechaFin.add(chkSinFechaFin, BorderLayout.EAST);
        JCheckBox chkVigente = new JCheckBox("Vigente", esEdicion ? o.estado : true);
        chkVigente.setOpaque(false);
        chkVigente.setForeground(UITheme.TEXT_PRIMARY);

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
        form.add(new JLabel("Fecha Inicio:"));
        form.add(selectorFechaInicio);
        form.add(new JLabel("Fecha Fin (opcional):"));
        form.add(panelFechaFin);
        form.add(new JLabel("Estado:"));
        form.add(chkVigente);

        JButton btnGuardar = UITheme.createPrimaryButton(esEdicion ? "Guardar Cambios" : "Guardar Obligación");
        btnGuardar.addActionListener(e -> {
            subcategoria sub = (subcategoria) cmbSub.getSelectedItem();
            if (sub == null) return;
            try {
                LocalDate fi = DatePickerUtils.obtener(selectorFechaInicio);
                LocalDate ff = chkSinFechaFin.isSelected() ? null : DatePickerUtils.obtener(selectorFechaFin);
                if (fi == null || (!chkSinFechaFin.isSelected() && ff == null)) {
                    throw new IllegalArgumentException("Selecciona las fechas de la obligación.");
                }

                if (esEdicion) {
                    oCrud.sp_actualizar_obligacion(
                        o.id_obligacion,
                        sub.id_subcategoria,
                        txtNombre.getText().trim(),
                        txtDesc.getText().trim(),
                        Double.parseDouble(txtMonto.getText().trim()),
                        Integer.parseInt(txtDia.getText().trim()),
                        fi,
                        ff,
                        chkVigente.isSelected(),
                        nombreAuditor
                    );
                } else {
                    oCrud.sp_insertar_obligacion(
                        usuarioActual.id_usuario,
                        sub.id_subcategoria,
                        txtNombre.getText().trim(),
                        txtDesc.getText().trim(),
                        Double.parseDouble(txtMonto.getText().trim()),
                        Integer.parseInt(txtDia.getText().trim()),
                        fi,
                        ff,
                        nombreAuditor
                    );
                }
                JOptionPane.showMessageDialog(dlg, esEdicion ? "Obligación actualizada con éxito" : "Obligación registrada con éxito");
                dlg.dispose();
                cargarObligaciones();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

    private void pagarSeleccionada() {
        int row = tblObligaciones.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una obligación para pagar.");
            return;
        }

        int idObligacion = (int) modelObligaciones.getValueAt(row, 0);
        try {
            obligacion_fija obligacion = oCrud.sp_consultar_obligacion(idObligacion);
            if (obligacion == null || !obligacion.estado) {
                JOptionPane.showMessageDialog(this, "La obligación no existe o está inactiva.");
                return;
            }

            ArrayList<presupuesto> presupuestos = pCrud.listarPorUsuario(usuarioActual.id_usuario, (short) 1);
            if (presupuestos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay un presupuesto activo para registrar el pago.");
                return;
            }

            JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Pagar Obligación", true);
            dlg.setSize(460, 390);
            dlg.setLocationRelativeTo(this);
            dlg.setLayout(new BorderLayout());

            JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
            form.setBorder(new EmptyBorder(20, 20, 20, 20));

            JComboBox<presupuesto> cmbPresupuesto = new JComboBox<>();
            UITheme.styleComboBox(cmbPresupuesto);
            for (presupuesto p : presupuestos) cmbPresupuesto.addItem(p);

            JLabel lblMonto = new JLabel(String.format("L %.2f", obligacion.monto_mensual));
            JDatePicker selectorFecha = DatePickerUtils.crear(LocalDate.now());
            JComboBox<String> cmbMetodo = new JComboBox<>(new String[]{
                "efectivo", "tarjeta_debito", "tarjeta_credito", "transferencia"
            });
            UITheme.styleComboBox(cmbMetodo);
            JTextField txtFactura = new JTextField();
            JTextField txtObs = new JTextField();

            form.add(new JLabel("Presupuesto:"));
            form.add(cmbPresupuesto);
            form.add(new JLabel("Monto:"));
            form.add(lblMonto);
            form.add(new JLabel("Fecha:"));
            form.add(selectorFecha);
            form.add(new JLabel("Método de Pago:"));
            form.add(cmbMetodo);
            form.add(new JLabel("No. Factura (opcional):"));
            form.add(txtFactura);
            form.add(new JLabel("Observaciones:"));
            form.add(txtObs);

            JButton btnPagar = UITheme.createPrimaryButton("Registrar Pago");
            btnPagar.addActionListener(e -> {
                try {
                    presupuesto presupuestoSeleccionado = (presupuesto) cmbPresupuesto.getSelectedItem();
                    if (presupuestoSeleccionado == null) {
                        throw new IllegalArgumentException("Selecciona un presupuesto.");
                    }

                    LocalDate fecha = DatePickerUtils.obtener(selectorFecha);
                    if (fecha == null) {
                        throw new IllegalArgumentException("Selecciona una fecha.");
                    }
                    short mes = (short) fecha.getMonthValue();

                    for (transaccion t : tCrud.listarPorPresupuesto(
                            presupuestoSeleccionado.id_presupuesto, null, null,
                            fecha.getYear(), mes)) {
                        if (t.id_obligacion != null
                                && t.id_obligacion == obligacion.id_obligacion) {
                            throw new IllegalStateException("La obligación ya tiene un pago registrado para ese mes.");
                        }
                    }

                    tCrud.sp_registrar_transaccion_completa(
                        usuarioActual.id_usuario,
                        presupuestoSeleccionado.id_presupuesto,
                        fecha.getYear(),
                        mes,
                        obligacion.id_subcategoria,
                        (short) 2,
                        "Pago - " + obligacion.nombre,
                        obligacion.monto_mensual,
                        fecha,
                        (String) cmbMetodo.getSelectedItem(),
                        txtFactura.getText().trim().isEmpty() ? null : txtFactura.getText().trim(),
                        txtObs.getText().trim().isEmpty() ? null : txtObs.getText().trim(),
                        obligacion.id_obligacion,
                        nombreAuditor
                    );

                    JOptionPane.showMessageDialog(dlg, "Pago registrado correctamente.");
                    dlg.dispose();
                    cargarObligaciones();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dlg, "Error al registrar el pago: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            JButton btnCancelar = UITheme.createSecondaryButton("Cancelar");
            btnCancelar.addActionListener(e -> dlg.dispose());
            acciones.add(btnCancelar);
            acciones.add(btnPagar);

            dlg.add(form, BorderLayout.CENTER);
            dlg.add(acciones, BorderLayout.SOUTH);
            dlg.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al preparar el pago: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
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
                oCrud.sp_eliminar_obligacion(id, nombreAuditor);
                JOptionPane.showMessageDialog(this, "Obligación desactivada.");
                cargarObligaciones();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
