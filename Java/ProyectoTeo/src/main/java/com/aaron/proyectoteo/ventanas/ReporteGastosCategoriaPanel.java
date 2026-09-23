package com.aaron.proyectoteo.ventanas;

import com.aaron.proyectoteo.categoria;
import com.aaron.proyectoteo.presupuesto;
import com.aaron.proyectoteo.subcategoria;
import com.aaron.proyectoteo.transaccion;
import com.aaron.proyectoteo.usuario;
import com.aaron.proyectoteo.crud.categoriaCRUD;
import com.aaron.proyectoteo.crud.presupuestoCRUD;
import com.aaron.proyectoteo.crud.subcategoriaCRUD;
import com.aaron.proyectoteo.crud.transaccionCRUD;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

/** Reporte 2: distribución de gastos por categoría. */
public class ReporteGastosCategoriaPanel extends JPanel {

    private final usuario usuarioActual;
    private final presupuestoCRUD presupuestoCrud = new presupuestoCRUD();
    private final transaccionCRUD transaccionCrud = new transaccionCRUD();
    private final subcategoriaCRUD subcategoriaCrud = new subcategoriaCRUD();
    private final categoriaCRUD categoriaCrud = new categoriaCRUD();
    private final JComboBox<String> selectorMes = new JComboBox<>(new String[]{
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    });
    private final JSpinner selectorAnio = new JSpinner();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Categoría", "Monto gastado", "% del total", "Transacciones"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final GraficoCategorias grafico = new GraficoCategorias();
    private List<ResumenCategoria> datos = new ArrayList<>();

    public ReporteGastosCategoriaPanel(usuario usuarioActual) {
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
        JLabel lblTitulo = new JLabel("Distribución de gastos por categoría");
        lblTitulo.setFont(UITheme.FONT_TITLE);
        lblTitulo.setForeground(UITheme.TEXT_PRIMARY);
        JLabel lblDescripcion = new JLabel("Usuario: " + usuarioActual.nombre + " " + usuarioActual.apellido);
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

        tabla.setRowHeight(30);
        tabla.getTableHeader().setReorderingAllowed(false);
        UITheme.styleTable(tabla);
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
            YearMonth periodo = obtenerPeriodo();
            datos = consultarDatos(periodo);
            modelo.setRowCount(0);
            for (ResumenCategoria resumen : datos) {
                modelo.addRow(new Object[]{
                        resumen.nombre,
                        dinero(resumen.monto),
                        String.format("%.2f%%", resumen.porcentaje),
                        resumen.cantidadTransacciones
                });
            }
            grafico.setDatos(datos);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar el reporte: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private YearMonth obtenerPeriodo() {
        Integer anio = (Integer) selectorAnio.getValue();
        int mes = selectorMes.getSelectedIndex() + 1;
        if (anio == null || mes < 1) {
            throw new IllegalArgumentException("Debes seleccionar el mes y el año.");
        }
        return YearMonth.of(anio, mes);
    }

    private List<ResumenCategoria> consultarDatos(YearMonth periodo) throws Exception {
        Map<Integer, String> nombresCategorias = new HashMap<>();
        for (categoria categoria : categoriaCrud.listar(null)) {
            nombresCategorias.put(categoria.id_categoria, categoria.nombre_categoria);
        }

        Map<Integer, Integer> categoriaPorSubcategoria = new HashMap<>();
        for (subcategoria subcategoria : subcategoriaCrud.listarTodas()) {
            categoriaPorSubcategoria.put(subcategoria.id_subcategoria, subcategoria.id_categoria);
        }

        Map<Integer, ResumenCategoria> acumulados = new LinkedHashMap<>();
        for (presupuesto presupuesto : presupuestoCrud.listarPorUsuario(usuarioActual.id_usuario, null)) {
            for (transaccion transaccion : transaccionCrud.listarPorPresupuesto(
                    presupuesto.id_presupuesto, (short) 2, null, periodo.getYear(), (short) periodo.getMonthValue())) {
                Integer idCategoria = categoriaPorSubcategoria.get(transaccion.id_subcategoria);
                if (idCategoria == null) {
                    continue;
                }
                String nombre = nombresCategorias.getOrDefault(idCategoria, "Categoría #" + idCategoria);
                ResumenCategoria resumen = acumulados.computeIfAbsent(
                        idCategoria, id -> new ResumenCategoria(id, nombre));
                resumen.monto += transaccion.monto;
                resumen.cantidadTransacciones++;
            }
        }

        double total = acumulados.values().stream().mapToDouble(r -> r.monto).sum();
        for (ResumenCategoria resumen : acumulados.values()) {
            resumen.porcentaje = total == 0 ? 0 : resumen.monto * 100 / total;
        }
        List<ResumenCategoria> resultado = new ArrayList<>(acumulados.values());
        resultado.sort((a, b) -> Double.compare(b.monto, a.monto));
        return resultado;
    }

    private String dinero(double valor) {
        return String.format("L %.2f", valor);
    }

    private void generarPdf() {
        if (datos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay gastos para generar el PDF.",
                    "Reporte vacío", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser selectorArchivo = new JFileChooser();
        selectorArchivo.setSelectedFile(new File("reporte_gastos_por_categoria.pdf"));
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
            documento.add(new org.openpdf.text.Paragraph("Distribución de gastos por categoría"));
            documento.add(new org.openpdf.text.Paragraph("Usuario: " + usuarioActual.nombre + " " + usuarioActual.apellido));
            documento.add(new org.openpdf.text.Paragraph("Mes: " + obtenerPeriodo()));
            documento.add(new org.openpdf.text.Paragraph(" "));

            PdfPTable tablaPdf = new PdfPTable(4);
            tablaPdf.setWidthPercentage(100);
            agregarCeldaCabecera(tablaPdf, "Categoría");
            agregarCeldaCabecera(tablaPdf, "Monto gastado");
            agregarCeldaCabecera(tablaPdf, "% del total");
            agregarCeldaCabecera(tablaPdf, "Transacciones");
            for (ResumenCategoria resumen : datos) {
                tablaPdf.addCell(new Phrase(resumen.nombre));
                tablaPdf.addCell(new Phrase(dinero(resumen.monto)));
                tablaPdf.addCell(new Phrase(String.format("%.2f%%", resumen.porcentaje)));
                tablaPdf.addCell(new Phrase(String.valueOf(resumen.cantidadTransacciones)));
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

    private void agregarCeldaCabecera(PdfPTable tablaPdf, String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto));
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        tablaPdf.addCell(celda);
    }

    private static class ResumenCategoria {
        private final int idCategoria;
        private final String nombre;
        private double monto;
        private double porcentaje;
        private int cantidadTransacciones;

        private ResumenCategoria(int idCategoria, String nombre) {
            this.idCategoria = idCategoria;
            this.nombre = nombre;
        }
    }

    private static class GraficoCategorias extends JPanel {
        private static final Color[] COLORES = {
            new Color(59, 130, 246), new Color(52, 211, 153), new Color(251, 191, 36),
            new Color(248, 113, 113), new Color(167, 139, 250), new Color(45, 212, 191),
            new Color(251, 146, 60), new Color(244, 114, 182)
        };
        private List<ResumenCategoria> datos = new ArrayList<>();

        private GraficoCategorias() {
            setPreferredSize(new Dimension(480, 360));
            setBackground(UITheme.CARD_BG);
        }

        private void setDatos(List<ResumenCategoria> datos) {
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
            g.drawString("Distribución del gasto", 20, 25);
            if (datos.isEmpty()) {
                g.setColor(UITheme.TEXT_SECONDARY);
                g.drawString("Sin gastos para el mes seleccionado", 20, getHeight() / 2);
                g.dispose();
                return;
            }

            int diametro = Math.min(230, Math.min(getWidth() / 2 - 25, getHeight() - 80));
            int x = 20;
            int y = 48;
            int inicio = 0;
            for (int i = 0; i < datos.size(); i++) {
                ResumenCategoria dato = datos.get(i);
                int angulo = i == datos.size() - 1 ? 360 - inicio : (int) Math.round(dato.porcentaje * 3.6);
                g.setColor(COLORES[i % COLORES.length]);
                g.fillArc(x, y, diametro, diametro, inicio, angulo);
                inicio += angulo;
            }
            g.setColor(UITheme.CARD_BG);
            int hueco = diametro / 3;
            g.fillOval(x + (diametro - hueco) / 2, y + (diametro - hueco) / 2, hueco, hueco);

            int leyendaX = x + diametro + 24;
            int leyendaY = 65;
            g.setFont(UITheme.FONT_SMALL);
            for (int i = 0; i < datos.size(); i++) {
                ResumenCategoria dato = datos.get(i);
                g.setColor(COLORES[i % COLORES.length]);
                g.fillRect(leyendaX, leyendaY + i * 28, 12, 12);
                g.setColor(UITheme.TEXT_LIGHT);
                String nombre = dato.nombre.length() > 18 ? dato.nombre.substring(0, 18) + "..." : dato.nombre;
                g.drawString(nombre + " (" + String.format("%.1f%%", dato.porcentaje) + ")",
                        leyendaX + 18, leyendaY + 10 + i * 28);
            }
            g.dispose();
        }
    }
}
