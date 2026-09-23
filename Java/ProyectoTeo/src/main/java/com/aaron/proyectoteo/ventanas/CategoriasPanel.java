package com.aaron.proyectoteo.ventanas;

import com.aaron.proyectoteo.categoria;
import com.aaron.proyectoteo.crud.categoriaCRUD;
import com.aaron.proyectoteo.crud.subcategoriaCRUD;
import com.aaron.proyectoteo.subcategoria;
import com.aaron.proyectoteo.usuario;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class CategoriasPanel extends JPanel {

    private JTable tblCategorias;
    private DefaultTableModel modelCategorias;
    private JTable tblSubcategorias;
    private DefaultTableModel modelSubcategorias;

    private categoriaCRUD catCrud = new categoriaCRUD();
    private subcategoriaCRUD subCrud = new subcategoriaCRUD();

    private final String nombreAuditor;

    public CategoriasPanel(usuario usuarioActual) {
        this.nombreAuditor = usuarioActual.nombre + " " + usuarioActual.apellido;
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.CONTENT_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        initComponents();
        cargarCategorias();
    }

    private void initComponents() {
        // Top Action Bar
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Categorías y Subcategorías");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBtns.setOpaque(false);

        JButton btnNuevaCat = UITheme.createPrimaryButton("+ Nueva Categoría");
        btnNuevaCat.addActionListener(e -> abrirModalNuevaCategoria());

        JButton btnEditarCat = UITheme.createSecondaryButton("✎ Editar Categoría");
        btnEditarCat.addActionListener(e -> editarCategoriaSeleccionada());

        JButton btnEliminarCat = UITheme.createSecondaryButton("🗑️ Eliminar Categoría");
        btnEliminarCat.addActionListener(e -> eliminarCategoriaSeleccionada());

        rightBtns.add(btnEliminarCat);
        rightBtns.add(btnEditarCat);
        rightBtns.add(btnNuevaCat);

        topPanel.add(lblTitle, BorderLayout.WEST);
        topPanel.add(rightBtns, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Split (Categorias & Subcategorias)
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setOpaque(false);

        // Left Table: Categorías
        JPanel pnlCat = new JPanel(new BorderLayout(0, 10));
        pnlCat.setBackground(UITheme.CARD_BG);
        pnlCat.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel lblCatTitle = new JLabel("Categorías Principales");
        lblCatTitle.setFont(UITheme.FONT_SUBTITLE);
        lblCatTitle.setForeground(UITheme.TEXT_PRIMARY);
        pnlCat.add(lblCatTitle, BorderLayout.NORTH);

        String[] colsCat = {"ID", "Nombre Categoría", "Tipo", "Orden"};
        modelCategorias = new DefaultTableModel(colsCat, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblCategorias = new JTable(modelCategorias);
        UITheme.styleTable(tblCategorias);
        tblCategorias.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSubcategoriasSeleccionada();
        });
        JScrollPane scrollCat = new JScrollPane(tblCategorias);
        scrollCat.setBorder(BorderFactory.createEmptyBorder());
        scrollCat.getViewport().setBackground(UITheme.CARD_BG);
        pnlCat.add(scrollCat, BorderLayout.CENTER);

        // Right Table: Subcategorías
        JPanel pnlSub = new JPanel(new BorderLayout(0, 10));
        pnlSub.setBackground(UITheme.CARD_BG);
        pnlSub.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel lblSubTitle = new JLabel("Subcategorías Asociadas");
        lblSubTitle.setFont(UITheme.FONT_SUBTITLE);
        lblSubTitle.setForeground(UITheme.TEXT_PRIMARY);
        pnlSub.add(lblSubTitle, BorderLayout.NORTH);

        String[] colsSub = {"ID", "Nombre Subcategoría", "Descripción", "Es Default", "Estado"};
        modelSubcategorias = new DefaultTableModel(colsSub, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblSubcategorias = new JTable(modelSubcategorias);
        UITheme.styleTable(tblSubcategorias);
        JScrollPane scrollSub = new JScrollPane(tblSubcategorias);
        scrollSub.setBorder(BorderFactory.createEmptyBorder());
        scrollSub.getViewport().setBackground(UITheme.CARD_BG);
        pnlSub.add(scrollSub, BorderLayout.CENTER);

        JPanel subBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        subBtns.setOpaque(false);

        JButton btnNuevaSub = UITheme.createPrimaryButton("+ Nueva Subcategoría");
        btnNuevaSub.addActionListener(e -> abrirModalNuevaSubcategoria());

        JButton btnEditarSub = UITheme.createSecondaryButton("✎ Editar");
        btnEditarSub.addActionListener(e -> editarSubcategoriaSeleccionada());

        JButton btnEliminarSub = UITheme.createSecondaryButton("🗑️ Eliminar");
        btnEliminarSub.addActionListener(e -> eliminarSubcategoriaSeleccionada());

        subBtns.add(btnEliminarSub);
        subBtns.add(btnEditarSub);
        subBtns.add(btnNuevaSub);

        pnlSub.add(subBtns, BorderLayout.SOUTH);

        centerPanel.add(pnlCat);
        centerPanel.add(pnlSub);

        add(centerPanel, BorderLayout.CENTER);
    }

    public void cargarCategorias() {
        modelCategorias.setRowCount(0);
        modelSubcategorias.setRowCount(0);
        try {
            ArrayList<categoria> lista = catCrud.listar(null);
            for (categoria c : lista) {
                String tpStr = c.tipo_categoria == 1 ? "1 - Ingreso" : (c.tipo_categoria == 2 ? "2 - Gasto" : "3 - Ahorro");
                modelCategorias.addRow(new Object[]{
                    c.id_categoria,
                    c.nombre_categoria,
                    tpStr,
                    c.order_presentacion
                });
            }
            if (tblCategorias.getRowCount() > 0) {
                tblCategorias.setRowSelectionInterval(0, 0);
            }
        } catch (Exception e) {}
    }

    private void cargarSubcategoriasSeleccionada() {
        int row = tblCategorias.getSelectedRow();
        modelSubcategorias.setRowCount(0);
        if (row < 0) return;

        int idCat = (int) modelCategorias.getValueAt(row, 0);
        try {
            ArrayList<subcategoria> subs = subCrud.listarPorCategoria(idCat);
            for (subcategoria s : subs) {
                modelSubcategorias.addRow(new Object[]{
                    s.id_subcategoria,
                    s.nombre,
                    s.descripcion != null ? s.descripcion : "",
                    s.es_default ? "Sí" : "No",
                    s.estado ? "Activo" : "Inactivo"
                });
            }
        } catch (Exception e) {}
    }

    private int getIdCategoriaSeleccionada() {
        int row = tblCategorias.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una categoría primero.");
            return -1;
        }
        return (int) modelCategorias.getValueAt(row, 0);
    }

    private int getIdSubcategoriaSeleccionada() {
        int row = tblSubcategorias.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una subcategoría primero.");
            return -1;
        }
        return (int) modelSubcategorias.getValueAt(row, 0);
    }

    private void abrirModalNuevaCategoria() {
        abrirModalCategoria(null);
    }

    private void editarCategoriaSeleccionada() {
        int id = getIdCategoriaSeleccionada();
        if (id < 0) return;
        try {
            categoria c = catCrud.sp_consultar_categoria(id);
            abrirModalCategoria(c);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar categoría: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirModalCategoria(categoria c) {
        boolean esEdicion = c != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), esEdicion ? "Editar Categoría" : "Nueva Categoría", true);
        dlg.setSize(380, 320);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtNombre = new JTextField(esEdicion ? c.nombre_categoria : "");
        JTextField txtDesc = new JTextField(esEdicion ? (c.descripcion != null ? c.descripcion : "") : "");
        JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"1 - Ingreso", "2 - Gasto", "3 - Ahorro"});
        UITheme.styleComboBox(cmbTipo);
        if (esEdicion) cmbTipo.setSelectedIndex(c.tipo_categoria - 1);
        JTextField txtOrden = new JTextField(esEdicion ? String.valueOf(c.order_presentacion) : "1");

        form.add(new JLabel("Nombre:"));
        form.add(txtNombre);
        form.add(new JLabel("Descripción:"));
        form.add(txtDesc);
        form.add(new JLabel("Tipo:"));
        form.add(cmbTipo);
        form.add(new JLabel("Orden Presentación:"));
        form.add(txtOrden);

        JButton btnGuardar = UITheme.createPrimaryButton(esEdicion ? "Guardar Cambios" : "Crear Categoría");
        btnGuardar.addActionListener(e -> {
            try {
                short tipo = (short) (cmbTipo.getSelectedIndex() + 1);
                if (esEdicion) {
                    catCrud.sp_actualizar_categoria(
                        c.id_categoria,
                        txtNombre.getText().trim(),
                        txtDesc.getText().trim(),
                        tipo,
                        Short.parseShort(txtOrden.getText().trim()),
                        nombreAuditor
                    );
                } else {
                    catCrud.sp_insertar_categoria(
                        txtNombre.getText().trim(),
                        txtDesc.getText().trim(),
                        tipo,
                        Short.parseShort(txtOrden.getText().trim()),
                        nombreAuditor
                    );
                }
                JOptionPane.showMessageDialog(dlg, esEdicion ? "Categoría actualizada con éxito" : "Categoría creada con éxito");
                dlg.dispose();
                cargarCategorias();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnGuardar, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void eliminarCategoriaSeleccionada() {
        int id = getIdCategoriaSeleccionada();
        if (id < 0) return;
        int opt = JOptionPane.showConfirmDialog(this, "¿Eliminar la categoría seleccionada?\nSe eliminarán sus subcategorías no usadas.", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                catCrud.sp_eliminar_categoria(id, nombreAuditor);
                JOptionPane.showMessageDialog(this, "Categoría eliminada con éxito");
                cargarCategorias();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void abrirModalNuevaSubcategoria() {
        int idCat = getIdCategoriaSeleccionada();
        if (idCat < 0) return;
        abrirModalSubcategoria(idCat, null);
    }

    private void editarSubcategoriaSeleccionada() {
        int id = getIdSubcategoriaSeleccionada();
        if (id < 0) return;
        try {
            subcategoria s = subCrud.sp_consultar_subcategoria(id);
            abrirModalSubcategoria(s.id_categoria, s);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar subcategoría: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirModalSubcategoria(int idCat, subcategoria s) {
        boolean esEdicion = s != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), esEdicion ? "Editar Subcategoría" : "Nueva Subcategoría", true);
        dlg.setSize(380, 300);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtNombre = new JTextField(esEdicion ? s.nombre : "");
        JTextField txtDesc = new JTextField(esEdicion ? (s.descripcion != null ? s.descripcion : "") : "");
        JCheckBox chkEstado = new JCheckBox("Activo", esEdicion ? s.estado : true);
        chkEstado.setOpaque(false);
        chkEstado.setForeground(UITheme.TEXT_PRIMARY);

        form.add(new JLabel("Nombre Subcategoría:"));
        form.add(txtNombre);
        form.add(new JLabel("Descripción:"));
        form.add(txtDesc);
        form.add(new JLabel("Estado:"));
        form.add(chkEstado);

        JButton btnGuardar = UITheme.createPrimaryButton(esEdicion ? "Guardar Cambios" : "Crear Subcategoría");
        btnGuardar.addActionListener(e -> {
            try {
                if (esEdicion) {
                    subCrud.sp_actualizar_subcategoria(
                        s.id_subcategoria,
                        txtNombre.getText().trim(),
                        txtDesc.getText().trim(),
                        chkEstado.isSelected(),
                        nombreAuditor
                    );
                } else {
                    subCrud.sp_insertar_subcategoria(
                        idCat,
                        txtNombre.getText().trim(),
                        txtDesc.getText().trim(),
                        nombreAuditor
                    );
                }
                JOptionPane.showMessageDialog(dlg, esEdicion ? "Subcategoría actualizada con éxito" : "Subcategoría creada con éxito");
                dlg.dispose();
                cargarSubcategoriasSeleccionada();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btnGuardar, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void eliminarSubcategoriaSeleccionada() {
        int id = getIdSubcategoriaSeleccionada();
        if (id < 0) return;
        int opt = JOptionPane.showConfirmDialog(this, "¿Eliminar la subcategoría seleccionada?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            try {
                subCrud.sp_eliminar_subcategoria(id);
                JOptionPane.showMessageDialog(this, "Subcategoría eliminada con éxito");
                cargarSubcategoriasSeleccionada();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
