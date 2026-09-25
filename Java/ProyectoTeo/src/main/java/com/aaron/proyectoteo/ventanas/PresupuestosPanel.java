package com.aaron.proyectoteo.ventanas;

import com.aaron.proyectoteo.Funciones;
import com.aaron.proyectoteo.crud.*;
import com.aaron.proyectoteo.presupuesto;
import com.aaron.proyectoteo.presupuesto_detalle;
import com.aaron.proyectoteo.subcategoria;
import com.aaron.proyectoteo.usuario;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import org.jdatepicker.JDatePicker;

public class PresupuestosPanel extends JPanel {

    private JComboBox<presupuesto> cmbPresupuestos;
    private JTable tblDetalles;
    private DefaultTableModel modelDetalles;
    private JLabel lblTotalIngresos, lblTotalGastos, lblTotalAhorro, lblEstado;

    private presupuestoCRUD pCrud = new presupuestoCRUD();
    private presupuesto_detalleCRUD pdCrud = new presupuesto_detalleCRUD();
    private subcategoriaCRUD subCrud = new subcategoriaCRUD();

    private final usuario usuarioActual;
    private final String nombreAuditor;

    private ArrayList<presupuesto_detalle> detallesActuales = new ArrayList<>();

    public PresupuestosPanel(usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        this.nombreAuditor = usuarioActual.nombre + " " + usuarioActual.apellido;
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
        cmbPresupuestos.setPreferredSize(new Dimension(320, 30));
        UITheme.styleComboBox(cmbPresupuestos);
        cmbPresupuestos.addActionListener(e -> seleccionarPresupuesto());

        titleContainer.add(lblTitle);
        titleContainer.add(cmbPresupuestos);

        JPanel btnContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnContainer.setOpaque(false);

        JButton btnNuevo = UITheme.createPrimaryButton("+ Nuevo Presupuesto");
        btnNuevo.addActionListener(e -> abrirModalPresupuesto(null));

        JButton btnEditar = UITheme.createSecondaryButton("✎ Editar");
        btnEditar.addActionListener(e -> editarPresupuestoSeleccionado());

        JButton btnEliminar = UITheme.createSecondaryButton("🗑️ Eliminar");
        btnEliminar.addActionListener(e -> eliminarPresupuestoSeleccionado());

        JButton btnCerrar = UITheme.createSecondaryButton("🔒 Cerrar Presupuesto");
        btnCerrar.addActionListener(e -> cerrarPresupuestoActual());

        btnContainer.add(btnEliminar);
        btnContainer.add(btnEditar);
        btnContainer.add(btnCerrar);
        btnContainer.add(btnNuevo);

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

        String[] cols = {"ID Detalle", "Subcategoría", "Monto Presupuestado", "Monto Ejecutado", "Balance Restante", "% Ejecutado", "Promedio 3 meses", "Proyección mensual", "Justificación"};
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

        JPanel detalleBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        detalleBtns.setOpaque(false);

        JButton btnAgregarDetalle = UITheme.createPrimaryButton("+ Asignar Subcategoría");
        btnAgregarDetalle.addActionListener(e -> abrirModalDetalle(null));

        JButton btnEditarDetalle = UITheme.createSecondaryButton("✎ Editar Detalle");
        btnEditarDetalle.addActionListener(e -> editarDetalleSeleccionado());

        JButton btnEliminarDetalle = UITheme.createSecondaryButton("🗑️ Eliminar Detalle");
        btnEliminarDetalle.addActionListener(e -> eliminarDetalleSeleccionado());

        detalleBtns.add(btnEliminarDetalle);
        detalleBtns.add(btnEditarDetalle);
        detalleBtns.add(btnAgregarDetalle);

        tableContainer.add(detalleBtns, BorderLayout.SOUTH);

        centerPanel.add(tableContainer, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    public void cargarPresupuestos() {
        cmbPresupuestos.removeAllItems();
        try {
            ArrayList<presupuesto> lista = pCrud.listarPorUsuario(usuarioActual.id_usuario, null);
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
        detallesActuales.clear();
        try {
            ArrayList<presupuesto_detalle> lista = pdCrud.listarPorPresupuesto(p.id_presupuesto);
            detallesActuales = lista;
            for (presupuesto_detalle pd : lista) {
                int idDetalle = pd.id_presupuesto_detalle;
                int idSub = pd.id_subcategoria;
                String subNombre = pd.nombre_subcategoria != null ? pd.nombre_subcategoria : ("Sub #" + idSub);
                double montoPres = pd.monto_mensual;
                String just = pd.observaciones;

                double montoEjec = Funciones.fn_calcular_monto_ejecutado(p.ano_inicio, p.mes_inicio, idSub);
                double balance = Funciones.fn_obtener_balance_subcategoria(p.id_presupuesto, idSub, p.ano_inicio, p.mes_inicio);
                double pct = Funciones.fn_calcular_porcentaje_ejecutado(idSub, p.id_presupuesto, p.ano_inicio, p.mes_inicio);
                double promedio = Funciones.fn_obtener_promedio_gasto_subcategoria(
                        usuarioActual.id_usuario, idSub, 3);
                double proyeccion = Funciones.fn_calcular_proyeccion_gasto_mensual(
                        idSub, p.ano_inicio, p.mes_inicio);

                modelDetalles.addRow(new Object[]{
                    idDetalle,
                    subNombre,
                    String.format("L %.2f", montoPres),
                    String.format("L %.2f", montoEjec),
                    String.format("L %.2f", balance),
                    String.format("%.1f %%", pct),
                    String.format("L %.2f", promedio),
                    String.format("L %.2f", proyeccion),
                    just != null ? just : ""
                });
            }
        } catch (Exception e) {
            // Ignorar
        }
    }

    private presupuesto getPresupuestoSeleccionado() {
        presupuesto p = (presupuesto) cmbPresupuestos.getSelectedItem();
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Selecciona o crea un presupuesto primero.");
            return null;
        }
        return p;
    }

    private void editarPresupuestoSeleccionado() {
        presupuesto p = getPresupuestoSeleccionado();
        if (p == null) return;
        try {
            presupuesto completo = pCrud.sp_consultar_presupuesto(p.id_presupuesto);
            abrirModalPresupuesto(completo != null ? completo : p);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar presupuesto: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirModalPresupuesto(presupuesto p) {
        boolean esEdicion = p != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), esEdicion ? "Editar Presupuesto" : "Crear Nuevo Presupuesto", true);
        dlg.setSize(420, 560);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtNombre = new JTextField(esEdicion ? p.nombre_descriptivo : "");
        JDatePicker selectorInicio = DatePickerUtils.crear(esEdicion
                ? LocalDate.of(p.ano_inicio, p.mes_inicio, 1)
                : LocalDate.now());
        JDatePicker selectorFin = DatePickerUtils.crear(esEdicion
                ? LocalDate.of(p.ano_fin, p.mes_fin, 1)
                : LocalDate.now());
        JTextField txtIngresos = new JTextField(esEdicion ? String.valueOf(p.total_ingresos) : "0.00");
        JTextField txtGastos = new JTextField(esEdicion ? String.valueOf(p.total_gastos) : "0.00");
        JTextField txtAhorro = new JTextField(esEdicion ? String.valueOf(p.total_ahorro) : "0.00");
        JComboBox<String> cmbEstado = new JComboBox<>(new String[]{"Activo", "Cerrado", "Borrador"});
        UITheme.styleComboBox(cmbEstado);
        if (esEdicion) cmbEstado.setSelectedIndex(p.estado_presupuesto - 1);

        form.add(new JLabel("Nombre Descriptivo:"));
        form.add(txtNombre);
        form.add(new JLabel("Fecha Inicio:"));
        form.add(selectorInicio);
        form.add(new JLabel("Fecha Fin:"));
        form.add(selectorFin);
        form.add(new JLabel("Total Ingresos (L):"));
        form.add(txtIngresos);
        form.add(new JLabel("Total Gastos (L):"));
        form.add(txtGastos);
        form.add(new JLabel("Total Ahorro (L):"));
        form.add(txtAhorro);
        form.add(new JLabel("Estado:"));
        form.add(cmbEstado);

        JButton btnGuardar = UITheme.createPrimaryButton(esEdicion ? "Guardar Cambios" : "Crear Presupuesto");
        btnGuardar.addActionListener(e -> {
            try {
                short estado = (short) (cmbEstado.getSelectedIndex() + 1);
                LocalDate inicio = DatePickerUtils.obtener(selectorInicio);
                LocalDate fin = DatePickerUtils.obtener(selectorFin);
                if (inicio == null || fin == null) {
                    throw new IllegalArgumentException("Selecciona las fechas de inicio y fin.");
                }
                if (esEdicion) {
                    pCrud.sp_actualizar_presupuesto(
                        p.id_presupuesto,
                        txtNombre.getText().trim(),
                        inicio.getYear(),
                        (short) inicio.getMonthValue(),
                        fin.getYear(),
                        (short) fin.getMonthValue(),
                        Double.parseDouble(txtIngresos.getText().trim()),
                        Double.parseDouble(txtGastos.getText().trim()),
                        Double.parseDouble(txtAhorro.getText().trim()),
                        estado,
                        nombreAuditor
                    );
                } else {
                    int idNuevo = pCrud.sp_crear_presupuesto_completo(
                            usuarioActual.id_usuario,
                            txtNombre.getText().trim(),
                            "Presupuesto creado desde la aplicación",
                            inicio,
                            fin,
                            "[]",
                            nombreAuditor);
                    if (idNuevo <= 0) {
                        throw new IllegalStateException("No se pudo obtener el presupuesto creado.");
                    }
                    // El procedimiento completo crea el presupuesto y sus detalles.
                    // Como este formulario captura los totales globales, se actualizan aquí.
                    pCrud.sp_actualizar_presupuesto(
                            idNuevo,
                            txtNombre.getText().trim(),
                            inicio.getYear(), (short) inicio.getMonthValue(),
                            fin.getYear(), (short) fin.getMonthValue(),
                            Double.parseDouble(txtIngresos.getText().trim()),
                            Double.parseDouble(txtGastos.getText().trim()),
                            Double.parseDouble(txtAhorro.getText().trim()),
                            estado,
                            nombreAuditor);
                }
                JOptionPane.showMessageDialog(dlg, esEdicion ? "Presupuesto actualizado con éxito" : "Presupuesto creado con éxito");
                dlg.dispose();
                cargarPresupuestos();
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

    private void eliminarPresupuestoSeleccionado() {
        presupuesto p = getPresupuestoSeleccionado();
        if (p == null) return;
        int opt = JOptionPane.showConfirmDialog(this, "¿Eliminar el presupuesto '" + p.nombre_descriptivo + "'?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                pCrud.sp_eliminar_presupuesto(p.id_presupuesto);
                JOptionPane.showMessageDialog(this, "Presupuesto eliminado con éxito");
                cargarPresupuestos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private presupuesto_detalle getDetalleSeleccionado() {
        int row = tblDetalles.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un detalle en la tabla.");
            return null;
        }
        int idDetalle = (int) modelDetalles.getValueAt(row, 0);
        for (presupuesto_detalle pd : detallesActuales) {
            if (pd.id_presupuesto_detalle == idDetalle) return pd;
        }
        return null;
    }

    private void editarDetalleSeleccionado() {
        presupuesto_detalle pd = getDetalleSeleccionado();
        if (pd == null) return;
        try {
            presupuesto_detalle completo = pdCrud.sp_consultar_presupuesto_detalle(pd.id_presupuesto_detalle);
            abrirModalDetalle(completo != null ? completo : pd);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar el detalle: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirModalDetalle(presupuesto_detalle pd) {
        presupuesto p = getPresupuestoSeleccionado();
        if (p == null) return;

        boolean esEdicion = pd != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), esEdicion ? "Editar Detalle" : "Asignar Subcategoría a Presupuesto", true);
        dlg.setSize(380, esEdicion ? 260 : 280);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtMonto = new JTextField(esEdicion ? String.valueOf(pd.monto_mensual) : "0.00");
        JTextField txtJust = new JTextField(esEdicion ? (pd.observaciones != null ? pd.observaciones : "") : "");

        form.add(new JLabel("Subcategoría:"));
        if (esEdicion) {
            JLabel lblSub = new JLabel(pd.nombre_subcategoria != null ? pd.nombre_subcategoria : ("Sub #" + pd.id_subcategoria));
            lblSub.setForeground(UITheme.TEXT_PRIMARY);
            form.add(lblSub);
        } else {
            JComboBox<subcategoria> cmbSub = new JComboBox<>();
            UITheme.styleComboBox(cmbSub);
            try {
                ArrayList<subcategoria> subs = subCrud.listarTodas(usuarioActual.id_usuario);
                for (subcategoria s : subs) cmbSub.addItem(s);
            } catch (Exception e) {}
            form.add(cmbSub);
        }
        form.add(new JLabel("Monto Mensual (L):"));
        form.add(txtMonto);
        form.add(new JLabel("Justificación:"));
        form.add(txtJust);

        JButton btnGuardar = UITheme.createPrimaryButton(esEdicion ? "Guardar Cambios" : "Guardar Asignación");
        btnGuardar.addActionListener(e -> {
            try {
                if (esEdicion) {
                    pdCrud.sp_actualizar_presupuesto_detalle(
                        pd.id_presupuesto_detalle,
                        Double.parseDouble(txtMonto.getText().trim()),
                        txtJust.getText().trim(),
                        nombreAuditor
                    );
                } else {
                    JComboBox<subcategoria> cmbSub = (JComboBox<subcategoria>) form.getComponent(1);
                    subcategoria sub = (subcategoria) cmbSub.getSelectedItem();
                    if (sub == null) return;
                    pdCrud.sp_insertar_presupuesto_detalle(
                        p.id_presupuesto,
                        sub.id_subcategoria,
                        Double.parseDouble(txtMonto.getText().trim()),
                        txtJust.getText().trim(),
                        nombreAuditor
                    );
                }
                JOptionPane.showMessageDialog(dlg, esEdicion ? "Detalle actualizado con éxito" : "Subcategoría asignada correctamente");
                dlg.dispose();
                seleccionarPresupuesto();
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

    private void eliminarDetalleSeleccionado() {
        presupuesto_detalle pd = getDetalleSeleccionado();
        if (pd == null) return;
        int opt = JOptionPane.showConfirmDialog(this, "¿Eliminar el detalle seleccionado?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                pdCrud.sp_eliminar_presupuesto_detalle(pd.id_presupuesto_detalle);
                JOptionPane.showMessageDialog(this, "Detalle eliminado con éxito");
                seleccionarPresupuesto();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cerrarPresupuestoActual() {
        presupuesto p = getPresupuestoSeleccionado();
        if (p == null) return;

        int opt = JOptionPane.showConfirmDialog(this, "¿Estás seguro de cerrar el presupuesto '" + p.nombre_descriptivo + "'?", "Confirmar Cierre", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                pCrud.sp_cerrar_presupuesto(p.id_presupuesto, nombreAuditor);
                JOptionPane.showMessageDialog(this, "Presupuesto cerrado con éxito");
                cargarPresupuestos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
