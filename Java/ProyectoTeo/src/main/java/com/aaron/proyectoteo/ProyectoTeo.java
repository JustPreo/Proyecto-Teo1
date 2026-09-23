/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.aaron.proyectoteo;

import com.aaron.proyectoteo.ventanas.LoginDialog;
import com.aaron.proyectoteo.ventanas.MainFrame;
import com.aaron.proyectoteo.ventanas.UITheme;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author aaron
 */
public class ProyectoTeo {

    public static void main(String[] args) {
        try {
            Conexion.verificarConexion();
            System.out.println("Conexión a SQL Server exitosa.");
        } catch (Exception e) {
            System.err.println("Advertencia DB: " + e.getMessage());
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        UITheme.installDarkDefaults();

        SwingUtilities.invokeLater(() -> mostrarLogin());
    }

    public static void mostrarLogin() {
        JFrame parent = new JFrame();
        parent.setUndecorated(true);
        parent.setLocationRelativeTo(null);

        LoginDialog login = new LoginDialog(parent, user -> {
            parent.dispose();
            new MainFrame(user).setVisible(true);
        });
        login.setVisible(true);
    }
}
