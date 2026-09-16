package com.aaron.proyectoteo.ventanas;

import com.aaron.proyectoteo.Funciones;
import com.aaron.proyectoteo.crud.*;
import com.aaron.proyectoteo.obligacion_fija;
import com.aaron.proyectoteo.presupuesto;
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

public class DashboardPanel extends JPanel {

    private JPanel cardsPanel;
    private JTable tblTransacciones;
    private DefaultTableModel modelTransacciones;
    private JTable tblObligaciones;
    private DefaultTableModel modelObligaciones;

    private int currentUserId = 1;
    private int currentPresupuestoId = 1;

    public DashboardPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.CONTENT_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        initComponents();
        recargarDatos();
    }

    private void initComponents() {
        // Top Header Section
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Resumen Financiero");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        LocalDate now = LocalDate.now();
        JLabel lblDate = new JLabel("Fecha: " + now.getDayOfMonth() + "/" + now.getMonthValue() + "/" + now.getYear());
        lblDate.setFont(UITheme.FONT_REGULAR);
        lblDate.setForeground(UITheme.TEXT_SECONDARY);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(lblDate, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Center Container: Cards + Tables
        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));
        centerContainer.setOpaque(false);

        // 4 KPI Cards
        cardsPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setPreferredSize(new Dimension(800, 110));
        centerContainer.add(cardsPanel);
        centerContainer.add(Box.createVerticalStrut(24));

        // Two split panels for Tables
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        tablesPanel.setOpaque(false);

        // Left Table: Últimas transacciones
        JPanel pnlTrans = new JPanel(new BorderLayout(0, 10));
        pnlTrans.setBackground(UITheme.CARD_BG);
        pnlTrans.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel lblTransTitle = new JLabel("Últimos Movimientos");
        lblTransTitle.setFont(UITheme.FONT_SUBTITLE);
        lblTransTitle.setForeground(UITheme.TEXT_PRIMARY);
        pnlTrans.add(lblTransTitle, BorderLayout.NORTH);

        String[] colTrans = {"Fecha", "Tipo", "Descripción", "Monto", "Método"};
        modelTransacciones = new DefaultTableModel(colTrans, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblTransacciones = new JTable(modelTransacciones);
        UITheme.styleTable(tblTransacciones);
        JScrollPane scrollTrans = new JScrollPane(tblTransacciones);
        scrollTrans.setBorder(BorderFactory.createEmptyBorder());
        scrollTrans.getViewport().setBackground(UITheme.CARD_BG);
        pnlTrans.add(scrollTrans, BorderLayout.CENTER);

        // Right Table: Próximas obligaciones a vencer
        JPanel pnlOblig = new JPanel(new BorderLayout(0, 10));
        pnlOblig.setBackground(UITheme.CARD_BG);
        pnlOblig.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel lblObligTitle = new JLabel("Obligaciones por Vencer (Mes Actual)");
        lblObligTitle.setFont(UITheme.FONT_SUBTITLE);
        lblObligTitle.setForeground(UITheme.TEXT_PRIMARY);
        pnlOblig.add(lblObligTitle, BorderLayout.NORTH);

        String[] colOblig = {"Obligación", "Monto", "Día Venc.", "Días Restantes"};
        modelObligaciones = new DefaultTableModel(colOblig, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblObligaciones = new JTable(modelObligaciones);
        UITheme.styleTable(tblObligaciones);
        JScrollPane scrollOblig = new JScrollPane(tblObligaciones);
        scrollOblig.setBorder(BorderFactory.createEmptyBorder());
        scrollOblig.getViewport().setBackground(UITheme.CARD_BG);
        pnlOblig.add(scrollOblig, BorderLayout.CENTER);

        tablesPanel.add(pnlTrans);
        tablesPanel.add(pnlOblig);

        centerContainer.add(tablesPanel);

        add(centerContainer, BorderLayout.CENTER);
    }

    public void recargarDatos() {
        LocalDate now = LocalDate.now();
        int anio = now.getYear();
        int mes = now.getMonthValue();

        double totalIngresos = 0;
        double totalGastos = 0;
        double totalAhorros = 0;
        double balanceFinal = 0;

        try {
            // Calcular balance mensual mediante stored procedure
            Connection con = com.aaron.proyectoteo.Conexion.obtenerConexion();
            java.sql.CallableStatement cs = con.prepareCall("{CALL dbo.sp_calcular_balance_mensual(?,?,?,?,?,?,?,?)}");
            cs.setInt(1, currentUserId);
            cs.setInt(2, currentPresupuestoId);
            cs.setInt(3, anio);
            cs.setInt(4, mes);
            cs.registerOutParameter(5, java.sql.Types.DECIMAL);
            cs.registerOutParameter(6, java.sql.Types.DECIMAL);
            cs.registerOutParameter(7, java.sql.Types.DECIMAL);
            cs.registerOutParameter(8, java.sql.Types.DECIMAL);
            cs.execute();

            totalIngresos = cs.getDouble(5);
            totalGastos = cs.getDouble(6);
            totalAhorros = cs.getDouble(7);
            balanceFinal = cs.getDouble(8);
            cs.close();
        } catch (Exception e) {
            // Defaults 0
        }

        // Reconstruir Cards
        cardsPanel.removeAll();
        cardsPanel.add(UITheme.createCard("Total Ingresos", String.format("L %.2f", totalIngresos), "Mes " + mes + "/" + anio, UITheme.SUCCESS));
        cardsPanel.add(UITheme.createCard("Total Gastos", String.format("L %.2f", totalGastos), "Mes " + mes + "/" + anio, UITheme.DANGER));
        cardsPanel.add(UITheme.createCard("Total Ahorro", String.format("L %.2f", totalAhorros), "Mes " + mes + "/" + anio, UITheme.PURPLE));
        cardsPanel.add(UITheme.createCard("Balance Neto", String.format("L %.2f", balanceFinal), balanceFinal >= 0 ? "Superávit" : "Déficit", balanceFinal >= 0 ? UITheme.PRIMARY : UITheme.DANGER));
        cardsPanel.revalidate();
        cardsPanel.repaint();

        // Cargar Últimas Transacciones mediante stored procedure
        modelTransacciones.setRowCount(0);
        try {
            transaccionCRUD tCrud = new transaccionCRUD();
            ArrayList<transaccion> lista = tCrud.listarPorPresupuesto(currentPresupuestoId, null, null, null, null);
            int count = 0;
            for (transaccion t : lista) {
                if (count++ >= 10) break;
                String tipoStr = t.tipo == 1 ? "Ingreso" : (t.tipo == 2 ? "Gasto" : "Ahorro");
                modelTransacciones.addRow(new Object[]{
                    t.fecha,
                    tipoStr,
                    t.descripcion,
                    String.format("L %.2f", t.monto),
                    t.metodo_pago
                });
            }
        } catch (Exception e) {
            // Ignorar si aún no hay datos
        }

        // Cargar Obligaciones por Vencer
        modelObligaciones.setRowCount(0);
        try {
            obligacion_fijaCRUD oCrud = new obligacion_fijaCRUD();
            ArrayList<obligacion_fija> lista = oCrud.listarPorUsuario(currentUserId, true);
            for (obligacion_fija o : lista) {
                int dias = Funciones.fn_dias_hasta_vencimiento(o.id_obligacion);
                modelObligaciones.addRow(new Object[]{
                    o.nombre,
                    String.format("L %.2f", o.monto_mensual),
                    "Día " + o.dia_vencimiento,
                    dias >= 0 ? dias + " días" : "Vencido"
                });
            }
        } catch (Exception e) {
            // Ignorar
        }
    }
}
