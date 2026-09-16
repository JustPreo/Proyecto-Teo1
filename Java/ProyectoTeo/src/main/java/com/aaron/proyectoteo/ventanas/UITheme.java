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

    // Colors
    public static final Color SIDEBAR_BG = new Color(24, 32, 47);          // Dark slate #18202F
    public static final Color SIDEBAR_HOVER = new Color(38, 49, 70);       // #263146
    public static final Color SIDEBAR_ACTIVE = new Color(37, 99, 235);     // Blue #2563EB
    public static final Color CONTENT_BG = new Color(243, 244, 246);       // Light gray #F3F4F6
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(229, 231, 235);     // #E5E7EB
    public static final Color TEXT_PRIMARY = new Color(17, 24, 39);        // Dark #111827
    public static final Color TEXT_SECONDARY = new Color(107, 114, 128);   // Gray #6B7280
    public static final Color TEXT_LIGHT = new Color(209, 213, 219);       // Light gray #D1D5DB
    
    // Accents
    public static final Color SUCCESS = new Color(16, 185, 129);           // Green #10B981
    public static final Color DANGER = new Color(239, 68, 68);             // Red #EF4444
    public static final Color PRIMARY = new Color(37, 99, 235);            // Blue #2563EB
    public static final Color WARNING = new Color(245, 158, 11);           // Amber #F59E0B
    public static final Color PURPLE = new Color(139, 92, 246);            // Purple #8B5CF6

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
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(7, 15, 7, 15)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(243, 244, 246));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
        });
        return btn;
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(32);
        table.setShowGrid(true);
        table.setGridColor(new Color(243, 244, 246));
        table.setSelectionBackground(new Color(224, 231, 255));
        table.setSelectionForeground(TEXT_PRIMARY);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(249, 250, 251));
        header.setForeground(TEXT_SECONDARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
    }
}
