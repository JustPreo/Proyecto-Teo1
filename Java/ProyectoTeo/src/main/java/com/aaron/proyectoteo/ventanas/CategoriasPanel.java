package com.aaron.proyectoteo.ventanas;

import com.aaron.proyectoteo.categoria;
import com.aaron.proyectoteo.crud.categoriaCRUD;
import com.aaron.proyectoteo.crud.subcategoriaCRUD;
import com.aaron.proyectoteo.subcategoria;
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

    public CategoriasPanel() {
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

        JButton btnNuevaSub = UITheme.createSecondaryButton("+ Nueva Subcategoría");
        btnNuevaSub.addActionListener(e -> abrirModalNuevaSubcategoria());

        rightBtns.add(btnNuevaSub);
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

        centerPanel.add(pnlCat);
        centerPanel.add(pnlSub);

        add(centerPanel, BorderLayout.CENTER);
    }

    public void cargarCategorias() {
        modelCategorias.setRowCount(0);
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

    private void abrirModalNuevaCategoria() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nueva Categoría", true);
        dlg.setSize(380, 320);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtNombre = new JTextField();
        JTextField txtDesc = new JTextField();
        JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"1 - Ingreso", "2 - Gasto", "3 - Ahorro"});
        JTextField txtOrden = new JTextField("1");

        form.add(new JLabel("Nombre:"));
        form.add(txtNombre);
        form.add(new JLabel("Descripción:"));
        form.add(txtDesc);
        form.add(new JLabel("Tipo:"));
        form.add(cmbTipo);
        form.add(new JLabel("Orden Presentación:"));
        form.add(txtOrden);

        JButton btnGuardar = UITheme.createPrimaryButton("Crear Categoría");
        btnGuardar.addActionListener(e -> {
            try {
                short tipo = (short) (cmbTipo.getSelectedIndex() + 1);
                catCrud.sp_insertar_categoria(
                    txtNombre.getText().trim(),
                    txtDesc.getText().trim(),
                    tipo,
                    Short.parseShort(txtOrden.getText().trim()),
                    "Admin"
                );
                JOptionPane.showMessageDialog(dlg, "Categoría creada con éxito");
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

    private void abrirModalNuevaSubcategoria() {
        int row = tblCategorias.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una categoría primero.");
            return;
        }
        int idCat = (int) modelCategorias.getValueAt(row, 0);

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nueva Subcategoría", true);
        dlg.setSize(380, 260);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtNombre = new JTextField();
        JTextField txtDesc = new JTextField();

        form.add(new JLabel("Nombre Subcategoría:"));
        form.add(txtNombre);
        form.add(new JLabel("Descripción:"));
        form.add(txtDesc);

        JButton btnGuardar = UITheme.createPrimaryButton("Crear Subcategoría");
        btnGuardar.addActionListener(e -> {
            try {
                subCrud.sp_insertar_subcategoria(
                    idCat,
                    txtNombre.getText().trim(),
                    txtDesc.getText().trim(),
                    "Admin"
                );
                JOptionPane.showMessageDialog(dlg, "Subcategoría creada con éxito");
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
}
