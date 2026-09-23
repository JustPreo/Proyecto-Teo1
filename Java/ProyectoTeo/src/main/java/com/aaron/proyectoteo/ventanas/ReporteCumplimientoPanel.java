package com.aaron.proyectoteo.ventanas;

import com.aaron.proyectoteo.categoria;
import com.aaron.proyectoteo.presupuesto;
import com.aaron.proyectoteo.presupuesto_detalle;
import com.aaron.proyectoteo.subcategoria;
import com.aaron.proyectoteo.transaccion;
import com.aaron.proyectoteo.usuario;
import com.aaron.proyectoteo.crud.categoriaCRUD;
import com.aaron.proyectoteo.crud.presupuestoCRUD;
import com.aaron.proyectoteo.crud.presupuesto_detalleCRUD;
import com.aaron.proyectoteo.crud.subcategoriaCRUD;
import com.aaron.proyectoteo.crud.transaccionCRUD;
import com.aaron.proyectoteo.crud.logicaNegocioCRUD;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

/** Reporte 3: cumplimiento del presupuesto por categoría y subcategoría. */
public class ReporteCumplimientoPanel extends JPanel {

    private final usuario usuarioActual;
    private final presupuestoCRUD presupuestoCrud = new presupuestoCRUD();
    private final presupuesto_detalleCRUD detalleCrud = new presupuesto_detalleCRUD();
    private final transaccionCRUD transaccionCrud = new transaccionCRUD();
    private final subcategoriaCRUD subcategoriaCrud = new subcategoriaCRUD();
    private final categoriaCRUD categoriaCrud = new categoriaCRUD();
    private final logicaNegocioCRUD logicaCrud = new logicaNegocioCRUD();
    private final JComboBox<String> selectorMes = new JComboBox<>(new String[]{
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    });
    private final JSpinner selectorAnio = new JSpinner();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Categoría", "Subcategoría", "Presupuestado", "Ejecutado", "Diferencia", "%", "Estado"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final GraficoCumplimiento grafico = new GraficoCumplimiento();
    private List<Resumen> datos = new ArrayList<>();

    public ReporteCumplimientoPanel(usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        int anioActual = LocalDate.now().getYear();
        selectorAnio.setModel(new SpinnerNumberModel(anioActual, 2000, 2100, 1));
        selectorAnio.setPreferredSize(new Dimension(85, 34));
        selectorMes.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
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
        JLabel lblTitulo = new JLabel("Cumplimiento del presupuesto");
        lblTitulo.setFont(UITheme.FONT_TITLE);
        lblTitulo.setForeground(UITheme.TEXT_PRIMARY);
        JLabel lblDescripcion = new JLabel("Presupuestado vs. ejecutado por categoría y subcategoría");
        lblDescripcion.setFont(UITheme.FONT_REGULAR);
        lblDescripcion.setForeground(UITheme.TEXT_SECONDARY);
        titulo.add(lblTitulo);
        titulo.add(lblDescripcion);

        JPanel filtros = new JPanel();
        filtros.setOpaque(false);
        JLabel lblMes = new JLabel("Mes:");
        lblMes.setForeground(UITheme.TEXT_LIGHT);
        filtros.add(lblMes);
        filtros.add(selectorMes);
        JLabel lblAnio = new JLabel("Año:");
        lblAnio.setForeground(UITheme.TEXT_LIGHT);
        filtros.add(lblAnio);
        filtros.add(selectorAnio);
        JButton btnConsultar = UITheme.createPrimaryButton("Consultar");
        btnConsultar.addActionListener(e -> cargarReporte());
        JButton btnPdf = UITheme.createSecondaryButton("Generar PDF");
        btnPdf.addActionListener(e -> generarPdf());
        filtros.add(btnConsultar);
        filtros.add(btnPdf);

        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(filtros, BorderLayout.EAST);
        add(encabezado, BorderLayout.NORTH);

        tabla.setRowHeight(28);
        tabla.getTableHeader().setReorderingAllowed(false);
        UITheme.styleTable(tabla);
        tabla.getColumnModel().getColumn(6).setCellRenderer(new EstadoRenderer());
        JPanel tablaPanel = new JPanel(new BorderLayout());
        tablaPanel.setBackground(UITheme.CARD_BG);
        tablaPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(12, 12, 12, 12)));
        tablaPanel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel graficoPanel = new JPanel(new BorderLayout());
        graficoPanel.setBackground(UITheme.CARD_BG);
        graficoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(12, 12, 12, 12)));
        graficoPanel.add(grafico, BorderLayout.CENTER);

        JPanel contenido = new JPanel(new GridLayout(1, 2, 16, 0));
        contenido.setOpaque(false);
        contenido.add(tablaPanel);
        contenido.add(graficoPanel);
        add(contenido, BorderLayout.CENTER);
    }

    public final void cargarReporte() {
        try {
            datos = consultarDatos(obtenerPeriodo());
            modelo.setRowCount(0);
            for (Resumen resumen : datos) {
                modelo.addRow(new Object[]{resumen.categoria, resumen.subcategoria,
                    dinero(resumen.presupuestado), dinero(resumen.ejecutado),
                    dinero(resumen.diferencia()), String.format("%.2f%%", resumen.porcentaje()),
                    resumen.estado()});
            }
            grafico.setDatos(datos);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar el reporte: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private YearMonth obtenerPeriodo() {
        return YearMonth.of((Integer) selectorAnio.getValue(), selectorMes.getSelectedIndex() + 1);
    }

    private List<Resumen> consultarDatos(YearMonth periodo) throws Exception {
        Map<Integer, categoria> categorias = new HashMap<>();
        for (categoria c : categoriaCrud.listar(null)) {
            categorias.put(c.id_categoria, c);
        }
        Map<Integer, subcategoria> subcategorias = new HashMap<>();
        for (subcategoria s : subcategoriaCrud.listarTodas()) {
            subcategorias.put(s.id_subcategoria, s);
        }

        List<Resumen> resultado = new ArrayList<>();
        for (presupuesto presupuesto : presupuestoCrud.listarPorUsuario(usuarioActual.id_usuario, null)) {
            YearMonth inicio = YearMonth.of(presupuesto.ano_inicio, presupuesto.mes_inicio);
            YearMonth fin = YearMonth.of(presupuesto.ano_fin, presupuesto.mes_fin);
            if (periodo.isBefore(inicio) || periodo.isAfter(fin)) {
                continue;
            }

            for (presupuesto_detalle detalle : detalleCrud.listarPorPresupuesto(presupuesto.id_presupuesto)) {
                subcategoria sub = subcategorias.get(detalle.id_subcategoria);
                if (sub == null) {
                    continue;
                }
                categoria cat = categorias.get(sub.id_categoria);
                if (cat == null) {
                    continue;
                }
                double ejecutado = logicaCrud.calcularMontoEjecutadoMes(
                        detalle.id_subcategoria, presupuesto.id_presupuesto,
                        periodo.getYear(), periodo.getMonthValue());
                double porcentaje = logicaCrud.calcularPorcentajeEjecucionMes(
                        detalle.id_subcategoria, presupuesto.id_presupuesto,
                        periodo.getYear(), periodo.getMonthValue());
                resultado.add(new Resumen(cat.nombre_categoria, sub.nombre,
                        detalle.monto_mensual, ejecutado, porcentaje));
            }
        }
        return resultado;
    }

    private String dinero(double valor) {
        return String.format("L %.2f", valor);
    }

    private void generarPdf() {
        if (datos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay detalles para generar el PDF.",
                    "Reporte vacío", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser selectorArchivo = new JFileChooser();
        selectorArchivo.setSelectedFile(new File("reporte_cumplimiento_presupuesto.pdf"));
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
            documento.add(new org.openpdf.text.Paragraph("Cumplimiento del presupuesto"));
            documento.add(new org.openpdf.text.Paragraph("Usuario: " + usuarioActual.nombre + " " + usuarioActual.apellido));
            documento.add(new org.openpdf.text.Paragraph("Mes: " + obtenerPeriodo()));
            documento.add(new org.openpdf.text.Paragraph(" "));
            PdfPTable tablaPdf = new PdfPTable(7);
            tablaPdf.setWidthPercentage(100);
            String[] cabeceras = {"Categoría", "Subcategoría", "Presupuestado", "Ejecutado", "Diferencia", "%", "Estado"};
            for (String cabecera : cabeceras) {
                PdfPCell celda = new PdfPCell(new Phrase(cabecera));
                celda.setHorizontalAlignment(Element.ALIGN_CENTER);
                tablaPdf.addCell(celda);
            }
            for (Resumen resumen : datos) {
                tablaPdf.addCell(new Phrase(resumen.categoria));
                tablaPdf.addCell(new Phrase(resumen.subcategoria));
                tablaPdf.addCell(new Phrase(dinero(resumen.presupuestado)));
                tablaPdf.addCell(new Phrase(dinero(resumen.ejecutado)));
                tablaPdf.addCell(new Phrase(dinero(resumen.diferencia())));
                tablaPdf.addCell(new Phrase(String.format("%.2f%%", resumen.porcentaje())));
                tablaPdf.addCell(new Phrase(resumen.estado()));
            }
            documento.add(tablaPdf);
            documento.add(new org.openpdf.text.Paragraph(" "));
            BufferedImage imagen = new BufferedImage(Math.max(800, grafico.getWidth()),
                    Math.max(480, grafico.getHeight()), BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = imagen.createGraphics();
            grafico.setSize(imagen.getWidth(), imagen.getHeight());
            grafico.printAll(graphics);
            graphics.dispose();
            org.openpdf.text.Image pdfGrafico = org.openpdf.text.Image.getInstance(imagen, null);
            pdfGrafico.scaleToFit(520, 340);
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

    private static class Resumen {
        private final String categoria;
        private final String subcategoria;
        private final double presupuestado;
        private final double ejecutado;
        private final double porcentajeCalculado;

        private Resumen(String categoria, String subcategoria, double presupuestado,
                double ejecutado, double porcentajeCalculado) {
            this.categoria = categoria;
            this.subcategoria = subcategoria;
            this.presupuestado = presupuestado;
            this.ejecutado = ejecutado;
            this.porcentajeCalculado = porcentajeCalculado;
        }

        private double diferencia() {
            return presupuestado - ejecutado;
        }

        private double porcentaje() {
            return porcentajeCalculado;
        }

        private String estado() {
            if (porcentaje() > 100) return "Rojo";
            if (porcentaje() >= 80) return "Amarillo";
            return "Verde";
        }
    }

    private static class EstadoRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            java.awt.Component componente = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(CENTER);
            if (!isSelected) {
                String estado = String.valueOf(value);
                componente.setForeground("Rojo".equals(estado) ? UITheme.DANGER
                        : "Amarillo".equals(estado) ? UITheme.WARNING : UITheme.SUCCESS);
            }
            return componente;
        }
    }

    private static class GraficoCumplimiento extends JPanel {
        private List<Resumen> datos = new ArrayList<>();

        private GraficoCumplimiento() {
            setPreferredSize(new Dimension(480, 360));
            setBackground(UITheme.CARD_BG);
        }

        private void setDatos(List<Resumen> datos) {
            this.datos = datos;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(UITheme.TEXT_PRIMARY);
            g.setFont(UITheme.FONT_SUBTITLE);
            g.drawString("Presupuestado vs. ejecutado", 20, 25);
            if (datos.isEmpty()) {
                g.setColor(UITheme.TEXT_SECONDARY);
                g.drawString("Sin datos para el mes seleccionado", 20, getHeight() / 2);
                g.dispose();
                return;
            }
            double maximo = datos.stream().mapToDouble(r -> Math.max(r.presupuestado, r.ejecutado))
                    .max().orElse(1);
            int left = 35;
            int top = 52;
            int ancho = Math.max(1, getWidth() - 55);
            int alto = Math.max(1, getHeight() - 95);
            int grupo = Math.max(1, ancho / datos.size());
            int barra = Math.max(5, Math.min(22, grupo / 3));
            for (int i = 0; i < datos.size(); i++) {
                Resumen r = datos.get(i);
                int x = left + i * grupo + 4;
                int hp = (int) Math.round(r.presupuestado / maximo * alto);
                int he = (int) Math.round(r.ejecutado / maximo * alto);
                g.setColor(UITheme.PRIMARY);
                g.fillRect(x, top + alto - hp, barra, hp);
                g.setColor(UITheme.DANGER);
                g.fillRect(x + barra + 3, top + alto - he, barra, he);
                g.setColor(UITheme.TEXT_SECONDARY);
                g.setFont(UITheme.FONT_SMALL);
                String texto = r.subcategoria.length() > 10 ? r.subcategoria.substring(0, 10) + "..." : r.subcategoria;
                g.drawString(texto, x, top + alto + 18);
            }
            g.setColor(UITheme.BORDER_COLOR);
            g.drawLine(left, top, left, top + alto);
            g.drawLine(left, top + alto, left + ancho, top + alto);
            g.dispose();
        }
    }
}
