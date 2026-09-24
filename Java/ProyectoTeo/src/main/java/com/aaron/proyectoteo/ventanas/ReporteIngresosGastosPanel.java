package com.aaron.proyectoteo.ventanas;

import com.aaron.proyectoteo.Funciones;
import com.aaron.proyectoteo.presupuesto;
import com.aaron.proyectoteo.transaccion;
import com.aaron.proyectoteo.usuario;
import com.aaron.proyectoteo.crud.presupuestoCRUD;
import com.aaron.proyectoteo.crud.transaccionCRUD;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.print.PageFormat;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import org.jdatepicker.JDatePicker;
import org.jdatepicker.LocalDateModel;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

/** Reporte 1: resumen mensual de ingresos, gastos y balance final. */
public class ReporteIngresosGastosPanel extends JPanel {

    private static final DateTimeFormatter PERIODO_FORMATO = DateTimeFormatter.ofPattern("yyyy-MM");

    private final usuario usuarioActual;
    private final presupuestoCRUD presupuestoCrud = new presupuestoCRUD();
    private final transaccionCRUD transaccionCrud = new transaccionCRUD();
    private final JDatePicker pickerDesde;
    private final JDatePicker pickerHasta;
    private final LocalDateModel modeloDesde;
    private final LocalDateModel modeloHasta;
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Mes", "Ingresos", "Gastos", "Balance final"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final GraficoMensual grafico = new GraficoMensual();
    private List<ResumenMensual> datos = new ArrayList<>();

    public ReporteIngresosGastosPanel(usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        modeloDesde = new LocalDateModel();
        modeloDesde.setValue(LocalDate.now().minusMonths(5).withDayOfMonth(1));
        modeloDesde.setSelected(true);
        modeloHasta = new LocalDateModel();
        modeloHasta.setValue(LocalDate.now());
        modeloHasta.setSelected(true);
        pickerDesde = new JDatePicker(modeloDesde);
        pickerHasta = new JDatePicker(modeloHasta);
        setLayout(new BorderLayout(16, 16));
        setBackground(UITheme.CONTENT_BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        construirInterfaz();
        cargarReporte();
    }

    private void construirInterfaz() {
        JPanel encabezado = new JPanel(new BorderLayout(12, 8));
        encabezado.setOpaque(false);

        JPanel titulo = new JPanel(new GridLayout(2, 1, 0, 4));
        titulo.setOpaque(false);
        JLabel lblTitulo = new JLabel("Resumen mensual de ingresos vs. gastos");
        lblTitulo.setFont(UITheme.FONT_TITLE);
        lblTitulo.setForeground(UITheme.TEXT_PRIMARY);
        JLabel lblDescripcion = new JLabel("Usuario: " + usuarioActual.nombre + " " + usuarioActual.apellido);
        lblDescripcion.setFont(UITheme.FONT_REGULAR);
        lblDescripcion.setForeground(UITheme.TEXT_SECONDARY);
        titulo.add(lblTitulo);
        titulo.add(lblDescripcion);

        JPanel filtros = new JPanel();
        filtros.setOpaque(false);
        filtros.add(etiqueta("Desde:"));
        filtros.add(pickerDesde);
        filtros.add(etiqueta("Hasta:"));
        filtros.add(pickerHasta);

        JButton btnConsultar = UITheme.createPrimaryButton("Consultar");
        btnConsultar.addActionListener(e -> cargarReporte());
        JButton btnPdf = UITheme.createSecondaryButton("Generar PDF");
        btnPdf.addActionListener(e -> generarPdf());
        filtros.add(btnConsultar);
        filtros.add(btnPdf);

        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(filtros, BorderLayout.EAST);
        add(encabezado, BorderLayout.NORTH);

        tabla.setRowHeight(30);
        tabla.getTableHeader().setReorderingAllowed(false);
        UITheme.styleTable(tabla);
        JPanel tablaPanel = new JPanel(new BorderLayout());
        tablaPanel.setBackground(UITheme.CARD_BG);
        tablaPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(12, 12, 12, 12)));
        tablaPanel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel contenido = new JPanel(new GridLayout(1, 2, 16, 0));
        contenido.setOpaque(false);
        contenido.add(tablaPanel);
        JPanel graficoPanel = new JPanel(new BorderLayout());
        graficoPanel.setBackground(UITheme.CARD_BG);
        graficoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(12, 12, 12, 12)));
        graficoPanel.add(grafico, BorderLayout.CENTER);
        contenido.add(graficoPanel);
        add(contenido, BorderLayout.CENTER);
    }

    private JLabel etiqueta(String texto) {
        JLabel label = new JLabel(texto);
        label.setForeground(UITheme.TEXT_LIGHT);
        return label;
    }

    public final void cargarReporte() {
        try {
            LocalDate fechaDesde = obtenerFecha(modeloDesde);
            LocalDate fechaHasta = obtenerFecha(modeloHasta);
            YearMonth desde = YearMonth.from(fechaDesde);
            YearMonth hasta = YearMonth.from(fechaHasta);
            if (hasta.isBefore(desde)) {
                throw new IllegalArgumentException("El período final no puede ser anterior al inicial.");
            }

            datos = consultarDatos(desde, hasta);
            modelo.setRowCount(0);
            for (ResumenMensual resumen : datos) {
                modelo.addRow(new Object[]{
                    resumen.periodo.format(PERIODO_FORMATO),
                    dinero(resumen.ingresos),
                    dinero(resumen.gastos),
                    dinero(resumen.balance())
                });
            }
            grafico.setDatos(datos);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Período inválido", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar el reporte: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDate obtenerFecha(LocalDateModel modelo) {
        LocalDate fecha = modelo.getValue();
        if (fecha == null) {
            throw new IllegalArgumentException("Debes seleccionar ambas fechas.");
        }
        return fecha;
    }

    private List<ResumenMensual> consultarDatos(YearMonth desde, YearMonth hasta) throws Exception {
        Map<YearMonth, ResumenMensual> acumulados = new LinkedHashMap<>();
        YearMonth periodo = desde;
        while (!periodo.isAfter(hasta)) {
            acumulados.put(periodo, new ResumenMensual(periodo));
            periodo = periodo.plusMonths(1);
        }

        ArrayList<presupuesto> presupuestos = presupuestoCrud.listarPorUsuario(usuarioActual.id_usuario, null);
        for (presupuesto presupuesto : presupuestos) {
            ArrayList<transaccion> transacciones = transaccionCrud.listarPorPresupuesto(
                    presupuesto.id_presupuesto, null, null, null, null);
            for (transaccion transaccion : transacciones) {
                YearMonth mes = YearMonth.of(transaccion.anio, transaccion.mes);
                ResumenMensual resumen = acumulados.get(mes);
                if (resumen == null) {
                    continue;
                }
                if (transaccion.fecha == null || !Funciones.fn_validar_vigencia_presupuesto(
                        transaccion.fecha, presupuesto.id_presupuesto)) {
                    continue;
                }
                if (transaccion.tipo == 1) {
                    resumen.ingresos += transaccion.monto;
                } else if (transaccion.tipo == 2) {
                    resumen.gastos += transaccion.monto;
                }
            }
        }
        return new ArrayList<>(acumulados.values());
    }

    private String dinero(double valor) {
        return String.format("L %.2f", valor);
    }

    private void generarPdf() {
        if (datos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay datos para generar el PDF.",
                    "Reporte vacío", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser selectorArchivo = new JFileChooser();
        selectorArchivo.setSelectedFile(new File("reporte_ingresos_gastos.pdf"));
        if (selectorArchivo.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivo = selectorArchivo.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".pdf")) {
            archivo = new File(archivo.getAbsolutePath() + ".pdf");
        }

        Document documento = new Document();
        try (FileOutputStream salida = new FileOutputStream(archivo)) {
            PdfWriter.getInstance(documento, salida);
            documento.open();
            documento.add(new Paragraph("Resumen mensual de ingresos vs. gastos"));
            documento.add(new Paragraph("Usuario: " + usuarioActual.nombre + " " + usuarioActual.apellido));
            documento.add(new Paragraph(" "));

            PdfPTable tablaPdf = new PdfPTable(4);
            tablaPdf.setWidthPercentage(100);
            agregarCeldaCabecera(tablaPdf, "Mes");
            agregarCeldaCabecera(tablaPdf, "Ingresos");
            agregarCeldaCabecera(tablaPdf, "Gastos");
            agregarCeldaCabecera(tablaPdf, "Balance final");
            for (ResumenMensual resumen : datos) {
                tablaPdf.addCell(new Phrase(resumen.periodo.format(PERIODO_FORMATO)));
                tablaPdf.addCell(new Phrase(dinero(resumen.ingresos)));
                tablaPdf.addCell(new Phrase(dinero(resumen.gastos)));
                tablaPdf.addCell(new Phrase(dinero(resumen.balance())));
            }
            documento.add(tablaPdf);
            documento.add(new Paragraph(" "));

            BufferedImage imagenGrafico = new BufferedImage(
                    Math.max(800, grafico.getWidth()), Math.max(420, grafico.getHeight()), BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = imagenGrafico.createGraphics();
            grafico.setSize(imagenGrafico.getWidth(), imagenGrafico.getHeight());
            grafico.printAll(graphics);
            graphics.dispose();
            org.openpdf.text.Image pdfGrafico = org.openpdf.text.Image.getInstance(imagenGrafico, null);
            pdfGrafico.scaleToFit(520, 300);
            pdfGrafico.setAlignment(Element.ALIGN_CENTER);
            documento.add(pdfGrafico);
            documento.close();
            JOptionPane.showMessageDialog(this, "PDF generado en:\n" + archivo.getAbsolutePath(),
                    "Reporte generado", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            if (documento.isOpen()) {
                documento.close();
            }
            JOptionPane.showMessageDialog(this, "No se pudo generar el PDF: " + ex.getMessage(),
                    "Error de PDF", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void agregarCeldaCabecera(PdfPTable tablaPdf, String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto));
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        tablaPdf.addCell(celda);
    }

    private static class ResumenMensual {
        private final YearMonth periodo;
        private double ingresos;
        private double gastos;

        private ResumenMensual(YearMonth periodo) {
            this.periodo = periodo;
        }

        private double balance() {
            return ingresos - gastos;
        }
    }

    private class GraficoMensual extends JPanel {
        private List<ResumenMensual> datos = new ArrayList<>();

        private GraficoMensual() {
            setPreferredSize(new Dimension(480, 360));
            setBackground(UITheme.CARD_BG);
        }

        private void setDatos(List<ResumenMensual> datos) {
            this.datos = datos;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int left = 55;
            int right = 20;
            int top = 42;
            int bottom = 55;
            int chartWidth = Math.max(1, getWidth() - left - right);
            int chartHeight = Math.max(1, getHeight() - top - bottom);

            g.setColor(UITheme.TEXT_PRIMARY);
            g.setFont(UITheme.FONT_SUBTITLE);
            g.drawString("Ingresos vs. gastos", left, 22);
            g.setFont(UITheme.FONT_SMALL);
            g.setColor(UITheme.SUCCESS);
            g.fillRect(left, 30, 12, 12);
            g.setColor(UITheme.TEXT_LIGHT);
            g.drawString("Ingresos", left + 18, 40);
            g.setColor(UITheme.DANGER);
            g.fillRect(left + 85, 30, 12, 12);
            g.setColor(UITheme.TEXT_LIGHT);
            g.drawString("Gastos", left + 103, 40);

            double maximo = datos.stream()
                    .mapToDouble(d -> Math.max(d.ingresos, d.gastos))
                    .max().orElse(1);
            maximo = Math.max(maximo, 1);
            g.setColor(UITheme.BORDER_COLOR);
            g.drawLine(left, top, left, top + chartHeight);
            g.drawLine(left, top + chartHeight, left + chartWidth, top + chartHeight);

            if (datos.isEmpty()) {
                g.setColor(UITheme.TEXT_SECONDARY);
                g.drawString("Sin datos para el período seleccionado", left + 20, top + chartHeight / 2);
                g.dispose();
                return;
            }

            int grupo = Math.max(1, chartWidth / datos.size());
            int anchoBarra = Math.max(5, Math.min(28, grupo / 3));
            for (int i = 0; i < datos.size(); i++) {
                ResumenMensual dato = datos.get(i);
                int x = left + i * grupo + Math.max(3, (grupo - (anchoBarra * 2 + 4)) / 2);
                int altoIngresos = (int) Math.round(dato.ingresos / maximo * chartHeight);
                int altoGastos = (int) Math.round(dato.gastos / maximo * chartHeight);
                g.setColor(UITheme.SUCCESS);
                g.fillRect(x, top + chartHeight - altoIngresos, anchoBarra, altoIngresos);
                g.setColor(UITheme.DANGER);
                g.fillRect(x + anchoBarra + 4, top + chartHeight - altoGastos, anchoBarra, altoGastos);
                g.setColor(UITheme.TEXT_SECONDARY);
                g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g.drawString(dato.periodo.toString(), x - 2, top + chartHeight + 17);
            }
            g.dispose();
        }
    }

}
