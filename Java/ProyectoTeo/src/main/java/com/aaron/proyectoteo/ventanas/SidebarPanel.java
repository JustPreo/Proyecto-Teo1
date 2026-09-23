package com.aaron.proyectoteo.ventanas;

import com.aaron.proyectoteo.usuario;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class SidebarPanel extends JPanel {

    public interface NavigationListener {
        void onNavigate(String moduleName);
    }

    private NavigationListener listener;
    private List<JButton> navButtons = new ArrayList<>();
    private String activeModule = "Dashboard";

    private JLabel lblUser;
    private Runnable onLogout;

    public void setOnLogout(Runnable onLogout) {
        this.onLogout = onLogout;
    }

    public void setUsuarioActual(usuario u) {
        if (lblUser != null && u != null) {
            lblUser.setText("👤 " + u.nombre + " " + u.apellido);
        }
    }

    public SidebarPanel(NavigationListener listener) {
        this.listener = listener;
        setLayout(new BorderLayout());
        setBackground(UITheme.SIDEBAR_BG);
        setPreferredSize(new Dimension(240, 720));

        initComponents();
    }

    private void initComponents() {
        // App Brand / Logo Header
        JPanel brandPanel = new JPanel(new BorderLayout(10, 0));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new EmptyBorder(24, 20, 24, 20));

        JLabel lblLogo = new JLabel("💳 FinTrack");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblLogo.setForeground(Color.WHITE);

        JLabel lblSubLogo = new JLabel("Sistema Presupuesto");
        lblSubLogo.setFont(UITheme.FONT_SMALL);
        lblSubLogo.setForeground(UITheme.TEXT_LIGHT);

        JPanel pnlTexts = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlTexts.setOpaque(false);
        pnlTexts.add(lblLogo);
        pnlTexts.add(lblSubLogo);

        brandPanel.add(pnlTexts, BorderLayout.CENTER);
        add(brandPanel, BorderLayout.NORTH);

        // Navigation Items List
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(10, 12, 10, 12));

        addNavItem(menuPanel, "📊 Dashboard", "Dashboard");
        addNavItem(menuPanel, "💰 Presupuestos", "Presupuestos");
        addNavItem(menuPanel, "💳 Transacciones", "Transacciones");
        addNavItem(menuPanel, "⏰ Obligaciones Fijas", "Obligaciones");
        addNavItem(menuPanel, "📁 Categorías", "Categorias");
        addNavItem(menuPanel, "📈 Reportes", "Reportes");

        add(menuPanel, BorderLayout.CENTER);

        // Footer Profile Info
        JPanel footerPanel = new JPanel(new BorderLayout(0, 10));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(16, 20, 20, 20));

        lblUser = new JLabel("👤 Usuario");
        lblUser.setFont(UITheme.FONT_BOLD);
        lblUser.setForeground(Color.WHITE);

        JLabel lblDb = new JLabel("● SQL Server Online");
        lblDb.setFont(UITheme.FONT_SMALL);
        lblDb.setForeground(UITheme.SUCCESS);

        JPanel pnlUser = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlUser.setOpaque(false);
        pnlUser.add(lblUser);
        pnlUser.add(lblDb);

        JButton btnLogout = UITheme.createSecondaryButton("Cerrar Sesión");
        btnLogout.setMaximumSize(new Dimension(200, 36));
        btnLogout.addActionListener(e -> {
            if (onLogout != null) onLogout.run();
        });

        footerPanel.add(pnlUser, BorderLayout.CENTER);
        footerPanel.add(btnLogout, BorderLayout.SOUTH);
        add(footerPanel, BorderLayout.SOUTH);

        updateActiveStyles();
    }

    private void addNavItem(JPanel container, String text, String moduleKey) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BOLD);
        btn.setForeground(UITheme.TEXT_LIGHT);
        btn.setBackground(UITheme.SIDEBAR_BG);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setBorder(new EmptyBorder(12, 16, 12, 16));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(220, 44));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("moduleKey", moduleKey);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!moduleKey.equals(activeModule)) {
                    btn.setBackground(UITheme.SIDEBAR_HOVER);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (!moduleKey.equals(activeModule)) {
                    btn.setBackground(UITheme.SIDEBAR_BG);
                }
            }
        });

        btn.addActionListener(e -> {
            activeModule = moduleKey;
            updateActiveStyles();
            if (listener != null) listener.onNavigate(moduleKey);
        });

        navButtons.add(btn);
        container.add(btn);
        container.add(Box.createVerticalStrut(6));
    }

    private void updateActiveStyles() {
        for (JButton btn : navButtons) {
            String key = (String) btn.getClientProperty("moduleKey");
            if (key.equals(activeModule)) {
                btn.setBackground(UITheme.SIDEBAR_ACTIVE);
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(UITheme.SIDEBAR_BG);
                btn.setForeground(UITheme.TEXT_LIGHT);
            }
        }
    }
}
