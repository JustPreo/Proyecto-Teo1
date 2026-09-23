package com.aaron.proyectoteo.ventanas;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class UITheme {

    // Colors (Dark Mode Theme)
    public static final Color SIDEBAR_BG = new Color(15, 23, 42);          // Deep dark slate #0F172A
    public static final Color SIDEBAR_HOVER = new Color(30, 41, 59);       // Slate 800 #1E293B
    public static final Color SIDEBAR_ACTIVE = new Color(59, 130, 246);     // Bright blue #3B82F6
    public static final Color CONTENT_BG = new Color(2, 6, 23);            // Ultra dark #020617
    public static final Color CARD_BG = new Color(15, 23, 42);             // Card dark #0F172A
    public static final Color CARD_BG_ALT = new Color(20, 29, 50);         // Slightly lighter card for stripes
    public static final Color BORDER_COLOR = new Color(51, 65, 85);        // Slate 700 #334155
    public static final Color TEXT_PRIMARY = new Color(248, 250, 252);     // Almost white #F8FAFC
    public static final Color TEXT_SECONDARY = new Color(148, 163, 184);   // Slate 400 #94A3B8
    public static final Color TEXT_LIGHT = new Color(226, 232, 240);       // Slate 200 #E2E8F0

    // Accents
    public static final Color SUCCESS = new Color(52, 211, 153);           // Emerald 400 #34D399
    public static final Color DANGER = new Color(248, 113, 113);           // Red 400 #F87171
    public static final Color PRIMARY = new Color(59, 130, 246);           // Blue 500 #3B82F6
    public static final Color WARNING = new Color(251, 191, 36);           // Amber 400 #FBBF24
    public static final Color PURPLE = new Color(167, 139, 250);           // Violet 400 #A78BFA

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_CARD_VAL = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    private static final Color SLATE_800 = new Color(30, 41, 59);
    private static final Color SLATE_700 = new Color(51, 65, 85);
    private static final Color SELECTION_BLUE = new Color(30, 58, 138);

    /**
     * Aplica valores por defecto del tema oscuro a toda la UI (labels, text fields,
     * checkboxes, tablas, paneles, JOptionPane, etc). Debe llamarse una sola vez,
     * justo después de fijar el Look & Feel.
     */
    public static void installDarkDefaults() {
        Color panelBg = CONTENT_BG;
        Color fieldBg = CARD_BG;

        UIManager.put("Panel.background", panelBg);
        UIManager.put("Label.foreground", TEXT_PRIMARY);
        UIManager.put("Label.background", panelBg);

        UIManager.put("OptionPane.background", panelBg);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("OptionPane.buttonFont", FONT_BOLD);

        UIManager.put("Button.background", SLATE_800);
        UIManager.put("Button.foreground", TEXT_PRIMARY);
        UIManager.put("Button.select", SLATE_700);
        UIManager.put("Button.font", FONT_BOLD);
        UIManager.put("Button.border", new EmptyBorder(8, 16, 8, 16));

        UIManager.put("TextField.background", fieldBg);
        UIManager.put("TextField.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", TEXT_PRIMARY);
        UIManager.put("TextField.selectionBackground", SELECTION_BLUE);
        UIManager.put("TextField.selectionForeground", Color.WHITE);
        UIManager.put("TextField.font", FONT_REGULAR);
        UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));

        UIManager.put("TextArea.background", fieldBg);
        UIManager.put("TextArea.foreground", TEXT_PRIMARY);
        UIManager.put("TextArea.caretForeground", TEXT_PRIMARY);

        UIManager.put("ComboBox.background", fieldBg);
        UIManager.put("ComboBox.foreground", TEXT_PRIMARY);
        UIManager.put("ComboBox.selectionBackground", SELECTION_BLUE);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("ComboBox.font", FONT_REGULAR);

        UIManager.put("CheckBox.background", panelBg);
        UIManager.put("CheckBox.foreground", TEXT_PRIMARY);
        UIManager.put("CheckBox.font", FONT_REGULAR);

        UIManager.put("Table.background", CARD_BG);
        UIManager.put("Table.foreground", TEXT_PRIMARY);
        UIManager.put("Table.selectionBackground", SELECTION_BLUE);
        UIManager.put("Table.selectionForeground", Color.WHITE);
        UIManager.put("Table.font", FONT_REGULAR);

        UIManager.put("TableHeader.background", SLATE_800);
        UIManager.put("TableHeader.foreground", TEXT_LIGHT);
        UIManager.put("TableHeader.font", FONT_BOLD);

        UIManager.put("ScrollPane.background", panelBg);
        UIManager.put("Viewport.background", fieldBg);

        UIManager.put("ToolTip.background", SLATE_800);
        UIManager.put("ToolTip.foreground", TEXT_PRIMARY);
        UIManager.put("ToolTip.font", FONT_REGULAR);
    }

    public static JPanel createCard(String title, String value, String subtitle, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(10, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                // Top accent line
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth() - 1, 4, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(FONT_SMALL);
        lblTitle.setForeground(TEXT_SECONDARY);

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(FONT_CARD_VAL);
        lblVal.setForeground(TEXT_PRIMARY);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(FONT_SMALL);
        lblSub.setForeground(TEXT_SECONDARY);

        JPanel pnlCenter = new JPanel(new GridLayout(2, 1, 0, 4));
        pnlCenter.setOpaque(false);
        pnlCenter.add(lblVal);
        pnlCenter.add(lblSub);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(pnlCenter, BorderLayout.CENTER);

        return card;
    }

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setBorder(new RoundedBorder(10, null));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(hover(btn, PRIMARY, PRIMARY.darker()));
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(SLATE_800);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setBorder(new RoundedBorder(10, BORDER_COLOR));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(hover(btn, SLATE_800, SLATE_700));
        return btn;
    }

    private static MouseAdapter hover(JButton btn, Color base, Color over) {
        return new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(over);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(base);
            }
        };
    }

    /**
     * Estiliza un JComboBox con esquinas redondeadas, flecha personalizada
     * (chevron) y un dropdown acorde al tema oscuro.
     */
    public static void styleComboBox(JComboBox<?> combo) {
        combo.setUI(new ModernComboBoxUI());
        combo.setRenderer(new ComboRenderer());
        combo.setFont(FONT_REGULAR);
        combo.setForeground(TEXT_PRIMARY);
        combo.setBackground(CARD_BG);
        combo.setOpaque(true);
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(0, 8, 0, 2)
        ));
        combo.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(34);
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(CARD_BG);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(SELECTION_BLUE);
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(SLATE_800);
        header.setForeground(TEXT_LIGHT);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? CARD_BG : CARD_BG_ALT);
                    c.setForeground(TEXT_PRIMARY);
                }
                setBorder(new EmptyBorder(5, 12, 5, 12));
                return c;
            }
        };
        table.setDefaultRenderer(Object.class, renderer);
    }

    // ---------- Custom rendering internals ----------

    private static class ModernComboBoxUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton btn = new JButton(createChevronIcon());
            btn.setBorder(BorderFactory.createEmptyBorder());
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setOpaque(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(20, 20));
            return btn;
        }

        @Override
        public void installUI(JComponent c) {
            super.installUI(c);
            c.setOpaque(true);
            c.setBackground(CARD_BG);
            c.setForeground(TEXT_PRIMARY);
        }

        @Override
        protected ComboPopup createPopup() {
            return new BasicComboPopup(comboBox) {
                @Override
                protected JScrollPane createScroller() {
                    JScrollPane sp = super.createScroller();
                    sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
                    sp.getViewport().setBackground(CARD_BG);
                    return sp;
                }
            };
        }
    }

    private static Icon createChevronIcon() {
        return new Icon() {
            @Override
            public int getIconWidth() { return 12; }
            @Override
            public int getIconHeight() { return 12; }
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new BasicStroke(1.8f));
                g2.setColor(TEXT_SECONDARY);
                g2.drawLine(x + 2, y + 4, x + 6, y + 8);
                g2.drawLine(x + 6, y + 8, x + 10, y + 4);
                g2.dispose();
            }
        };
    }

    private static class ComboRenderer extends JLabel implements ListCellRenderer<Object> {
        ComboRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setText(value == null ? "" : value.toString());
            setFont(FONT_REGULAR);
            setBorder(new EmptyBorder(3, 10, 3, 10));

            if (index == -1) {
                setBackground(CARD_BG);
                setForeground(TEXT_PRIMARY);
            } else if (isSelected) {
                setBackground(SELECTION_BLUE);
                setForeground(Color.WHITE);
            } else {
                setBackground(CARD_BG);
                setForeground(TEXT_PRIMARY);
            }
            return this;
        }
    }

    private static class RoundedBorder implements Border {
        private final int radius;
        private final Color outline;

        RoundedBorder(int radius, Color outline) {
            this.radius = radius;
            this.outline = outline;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(8, 16, 8, 16);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getBackground());
            g2.fillRoundRect(x, y, width - 1, height - 1, radius, radius);
            if (outline != null) {
                g2.setColor(outline);
                g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            }
            g2.dispose();
        }
    }
}
