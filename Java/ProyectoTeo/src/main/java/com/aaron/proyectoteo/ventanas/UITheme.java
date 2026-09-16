package com.aaron.proyectoteo.ventanas;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

public class UITheme {

    // Colors (Dark Mode Theme)
    public static final Color SIDEBAR_BG = new Color(15, 23, 42);          // Deep dark slate #0F172A
    public static final Color SIDEBAR_HOVER = new Color(30, 41, 59);       // Slate 800 #1E293B
    public static final Color SIDEBAR_ACTIVE = new Color(59, 130, 246);     // Bright blue #3B82F6
    public static final Color CONTENT_BG = new Color(2, 6, 23);            // Ultra dark #020617
    public static final Color CARD_BG = new Color(15, 23, 42);             // Card dark #0F172A
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
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(PRIMARY.darker());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(PRIMARY);
            }
        });
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(new Color(30, 41, 59)); // Dark slate 800
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(7, 15, 7, 15)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(51, 65, 85)); // Slate 700
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(30, 41, 59));
            }
        });
        return btn;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(32);
        table.setBackground(CARD_BG);
        table.setForeground(TEXT_PRIMARY);
        table.setShowGrid(true);
        table.setGridColor(new Color(30, 41, 59));
        table.setSelectionBackground(new Color(30, 58, 138)); // Deep blue selection
        table.setSelectionForeground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(30, 41, 59));
        header.setForeground(TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(CARD_BG);
        centerRenderer.setForeground(TEXT_PRIMARY);
    }
}
