package com.aaron.proyectoteo.ventanas;

import com.aaron.proyectoteo.categoria;
import com.aaron.proyectoteo.obligacion_fija;
import com.aaron.proyectoteo.presupuesto;
import com.aaron.proyectoteo.transaccion;
import com.aaron.proyectoteo.usuario;
import com.aaron.proyectoteo.crud.categoriaCRUD;
import com.aaron.proyectoteo.crud.obligacion_fijaCRUD;
import com.aaron.proyectoteo.crud.presupuestoCRUD;
import com.aaron.proyectoteo.crud.transaccionCRUD;
import com.aaron.proyectoteo.crud.subcategoriaCRUD;
import com.aaron.proyectoteo.subcategoria;
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
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
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

/** Reporte 4: estado de obligaciones fijas y cumplimiento de pagos. */
public class ReporteObligacionesPanel extends JPanel {

    private final usuario usuarioActual;
    private final obligacion_fijaCRUD obligacionCrud = new obligacion_fijaCRUD();
    private final presupuestoCRUD presupuestoCrud = new presupuestoCRUD();
    private final transaccionCRUD transaccionCrud = new transaccionCRUD();
    private final subcategoriaCRUD subcategoriaCrud = new subcategoriaCRUD();
    private final categoriaCRUD categoriaCrud = new categoriaCRUD();
    private final JComboBox<String> selectorMes = new JComboBox<>(new String[]{
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    });
    private final JSpinner selectorAnio = new JSpinner();
    private final JComboBox<String> selectorEstado = new JComboBox<>(new String[]{
        "Todos", "Pagada", "Pendiente", "Vencida"
    });
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Obligación", "Categoría", "Monto", "Vencimiento", "Estado", "Días", "Último pago"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modelo);
    private final GraficoEstados grafico = new GraficoEstados();
    private List<ResumenObligacion> datos = new ArrayList<>();

    public ReporteObligacionesPanel(usuario usuarioActual) {
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
        JLabel lblTitulo = new JLabel("Estado de obligaciones fijas");
        lblTitulo.setFont(UITheme.FONT_TITLE);
        lblTitulo.setForeground(UITheme.TEXT_PRIMARY);
        JLabel lblDescripcion = new JLabel("Pagos, vencimientos y días de atraso");
        lblDescripcion.setFont(UITheme.FONT_REGULAR);
        lblDescripcion.setForeground(UITheme.TEXT_SECONDARY);
        titulo.add(lblTitulo);
        titulo.add(lblDescripcion);

        JPanel filtros = new JPanel();
        filtros.setOpaque(false);
        filtros.add(etiqueta("Mes:"));
        filtros.add(selectorMes);
        filtros.add(etiqueta("Año:"));
        filtros.add(selectorAnio);
        filtros.add(etiqueta("Estado:"));
        filtros.add(selectorEstado);
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
        tabla.getColumnModel().getColumn(4).setCellRenderer(new EstadoRenderer());
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

    private JLabel etiqueta(String texto) {
        JLabel label = new JLabel(texto);
        label.setForeground(UITheme.TEXT_LIGHT);
        return label;
    }

    public final void cargarReporte() {
        try {
            YearMonth periodo = obtenerPeriodo();
            List<ResumenObligacion> todos = consultarDatos(periodo);
            datos = filtrarEstado(todos);
            modelo.setRowCount(0);
            for (ResumenObligacion r : datos) {
                modelo.addRow(new Object[]{r.nombre, r.categoria, dinero(r.monto),
                    r.fechaVencimiento, r.estado, r.diasTexto(), r.ultimoPagoTexto()});
            }
            grafico.setDatos(todos);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar el reporte: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private YearMonth obtenerPeriodo() {
        return YearMonth.of((Integer) selectorAnio.getValue(), selectorMes.getSelectedIndex() + 1);
    }

    private List<ResumenObligacion> filtrarEstado(List<ResumenObligacion> todos) {
        String estado = (String) selectorEstado.getSelectedItem();
        if ("Todos".equals(estado)) return todos;
        return todos.stream().filter(r -> r.estado.equals(estado)).toList();
    }

    private List<ResumenObligacion> consultarDatos(YearMonth periodo) throws Exception {
        Map<Integer, String> categoriaPorSubcategoria = new HashMap<>();
        Map<Integer, Integer> idCategoriaPorSubcategoria = new HashMap<>();
        Map<Integer, categoria> categorias = new HashMap<>();
        for (categoria c : categoriaCrud.listar(null)) categorias.put(c.id_categoria, c);
        for (subcategoria s : subcategoriaCrud.listarTodas()) {
            idCategoriaPorSubcategoria.put(s.id_subcategoria, s.id_categoria);
            categoria c = categorias.get(s.id_categoria);
            if (c != null) categoriaPorSubcategoria.put(s.id_subcategoria, c.nombre_categoria);
        }

        presupuesto presupuestoPeriodo = null;
        for (presupuesto p : presupuestoCrud.listarPorUsuario(usuarioActual.id_usuario, null)) {
            YearMonth inicio = YearMonth.of(p.ano_inicio, p.mes_inicio);
            YearMonth fin = YearMonth.of(p.ano_fin, p.mes_fin);
            if (!periodo.isBefore(inicio) && !periodo.isAfter(fin)) {
                presupuestoPeriodo = p;
                break;
            }
        }

        Map<Integer, transaccion> pagos = new HashMap<>();
        if (presupuestoPeriodo != null) {
            for (transaccion t : transaccionCrud.listarPorPresupuesto(
                    presupuestoPeriodo.id_presupuesto, null, null, periodo.getYear(), (short) periodo.getMonthValue())) {
                if (t.id_obligacion != null && (!pagos.containsKey(t.id_obligacion)
                        || (t.fecha != null && pagos.get(t.id_obligacion).fecha != null
                        && t.fecha.isAfter(pagos.get(t.id_obligacion).fecha)))) {
                    pagos.put(t.id_obligacion, t);
                }
            }
        }

        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = periodo.atDay(1);
        LocalDate finMes = periodo.atEndOfMonth();
        List<ResumenObligacion> resultado = new ArrayList<>();
        if (presupuestoPeriodo == null) return resultado;
        for (obligacion_fija o : obligacionCrud.procesarObligacionesMes(
                usuarioActual.id_usuario, periodo.getYear(), periodo.getMonthValue(),
                presupuestoPeriodo.id_presupuesto)) {
            if (o.fecha_inicio != null && o.fecha_inicio.isAfter(finMes)) continue;
            if (o.fecha_fin != null && o.fecha_fin.isBefore(inicioMes)) continue;
            int dia = Math.min(o.dia_vencimiento, periodo.lengthOfMonth());
            LocalDate vencimiento = periodo.atDay(dia);
            transaccion pago = pagos.get(o.id_obligacion);
            String estado;
            if (pago != null) {
                estado = "Pagada";
            } else if (hoy.isAfter(vencimiento)) {
                estado = "Vencida";
            } else {
                estado = "Pendiente";
            }
            resultado.add(new ResumenObligacion(o.id_obligacion, o.nombre,
                    categoriaPorSubcategoria.getOrDefault(o.id_subcategoria, "Sin categoría"),
                    o.monto_mensual, vencimiento, estado, pago == null ? null : pago.fecha));
        }
        return resultado;
    }

    private String dinero(double valor) {
        return String.format("L %.2f", valor);
    }

    private void generarPdf() {
        if (datos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay obligaciones para generar el PDF.",
                    "Reporte vacío", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser selectorArchivo = new JFileChooser();
        selectorArchivo.setSelectedFile(new File("reporte_obligaciones_fijas.pdf"));
        if (selectorArchivo.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File archivo = selectorArchivo.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".pdf")) archivo = new File(archivo + ".pdf");

        Document documento = new Document();
        try (FileOutputStream salida = new FileOutputStream(archivo)) {
            PdfWriter.getInstance(documento, salida);
            documento.open();
            documento.add(new org.openpdf.text.Paragraph("Estado de obligaciones fijas"));
            documento.add(new org.openpdf.text.Paragraph("Usuario: " + usuarioActual.nombre + " " + usuarioActual.apellido));
            documento.add(new org.openpdf.text.Paragraph("Mes: " + obtenerPeriodo()));
            documento.add(new org.openpdf.text.Paragraph(" "));
            PdfPTable tablaPdf = new PdfPTable(7);
            tablaPdf.setWidthPercentage(100);
            String[] cabeceras = {"Obligación", "Categoría", "Monto", "Vencimiento", "Estado", "Días", "Último pago"};
            for (String cabecera : cabeceras) {
                PdfPCell celda = new PdfPCell(new Phrase(cabecera));
                celda.setHorizontalAlignment(Element.ALIGN_CENTER);
                tablaPdf.addCell(celda);
            }
            for (ResumenObligacion r : datos) {
                tablaPdf.addCell(new Phrase(r.nombre));
                tablaPdf.addCell(new Phrase(r.categoria));
                tablaPdf.addCell(new Phrase(dinero(r.monto)));
                tablaPdf.addCell(new Phrase(r.fechaVencimiento.toString()));
                tablaPdf.addCell(new Phrase(r.estado));
                tablaPdf.addCell(new Phrase(r.diasTexto()));
                tablaPdf.addCell(new Phrase(r.ultimoPagoTexto()));
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
            if (documento.isOpen()) documento.close();
            JOptionPane.showMessageDialog(this, "No se pudo generar el PDF: " + ex.getMessage(),
                    "Error de PDF", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class ResumenObligacion {
        private final int id;
        private final String nombre;
        private final String categoria;
        private final double monto;
        private final LocalDate fechaVencimiento;
        private final String estado;
        private final LocalDate ultimoPago;

        private ResumenObligacion(int id, String nombre, String categoria, double monto,
                LocalDate fechaVencimiento, String estado, LocalDate ultimoPago) {
            this.id = id;
            this.nombre = nombre;
            this.categoria = categoria;
            this.monto = monto;
            this.fechaVencimiento = fechaVencimiento;
            this.estado = estado;
            this.ultimoPago = ultimoPago;
        }

        private String diasTexto() {
            long dias = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
            if ("Vencida".equals(estado)) return Math.abs(dias) + " días atraso";
            if ("Pagada".equals(estado)) return "Pagada";
            return dias + " días";
        }

        private String ultimoPagoTexto() {
            return ultimoPago == null ? "Pendiente" : ultimoPago.toString();
        }
    }

    private static class EstadoRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            java.awt.Component componente = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
            setHorizontalAlignment(CENTER);
            if (!isSelected) {
                String estado = String.valueOf(value);
                componente.setForeground("Pagada".equals(estado) ? UITheme.SUCCESS
                        : "Vencida".equals(estado) ? UITheme.DANGER : UITheme.WARNING);
            }
            return componente;
        }
    }

    private static class GraficoEstados extends JPanel {
        private List<ResumenObligacion> datos = new ArrayList<>();

        private GraficoEstados() {
            setPreferredSize(new Dimension(480, 360));
            setBackground(UITheme.CARD_BG);
        }

        private void setDatos(List<ResumenObligacion> datos) {
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
            g.drawString("Resumen de obligaciones", 20, 25);
            int pagadas = (int) datos.stream().filter(r -> "Pagada".equals(r.estado)).count();
            int pendientes = (int) datos.stream().filter(r -> "Pendiente".equals(r.estado)).count();
            int vencidas = (int) datos.stream().filter(r -> "Vencida".equals(r.estado)).count();
            int total = pagadas + pendientes + vencidas;
            if (total == 0) {
                g.setColor(UITheme.TEXT_SECONDARY);
                g.drawString("Sin obligaciones para el mes seleccionado", 20, getHeight() / 2);
                g.dispose();
                return;
            }
            int diametro = Math.min(230, Math.min(getWidth() / 2 - 25, getHeight() - 80));
            int x = 20;
            int y = 55;
            int inicio = 0;
            int[] cantidades = {pagadas, pendientes, vencidas};
            Color[] colores = {UITheme.SUCCESS, UITheme.WARNING, UITheme.DANGER};
            String[] nombres = {"Pagadas", "Pendientes", "Vencidas"};
            for (int i = 0; i < cantidades.length; i++) {
                int angulo = i == cantidades.length - 1 ? 360 - inicio : cantidades[i] * 360 / total;
                g.setColor(colores[i]);
                g.fillArc(x, y, diametro, diametro, inicio, angulo);
                inicio += angulo;
            }
            g.setColor(UITheme.CARD_BG);
            int hueco = diametro / 3;
            g.fillOval(x + (diametro - hueco) / 2, y + (diametro - hueco) / 2, hueco, hueco);
            g.setFont(UITheme.FONT_SMALL);
            int leyendaX = x + diametro + 24;
            for (int i = 0; i < nombres.length; i++) {
                g.setColor(colores[i]);
                g.fillRect(leyendaX, 70 + i * 30, 12, 12);
                g.setColor(UITheme.TEXT_LIGHT);
                g.drawString(nombres[i] + ": " + cantidades[i], leyendaX + 18, 80 + i * 30);
            }
            g.dispose();
        }
    }
}
