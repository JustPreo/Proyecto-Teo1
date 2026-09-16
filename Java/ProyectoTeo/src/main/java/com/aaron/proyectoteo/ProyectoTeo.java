/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.aaron.proyectoteo;

import java.sql.*;
import java.time.LocalDate;

/**
 *
 * @author aaron
 */
public class ProyectoTeo {

    public static void main(String[] args) {
        try {
            // Intentar verificar conexion en consola
            Conexion.verificarConexion();
            System.out.println("Conexión a SQL Server exitosa.");
        } catch (Exception e) {
            System.err.println("Advertencia DB: " + e.getMessage());
        }

        // Iniciar interfaz gráfica Swing
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        javax.swing.SwingUtilities.invokeLater(() -> {
            new com.aaron.proyectoteo.ventanas.MainFrame().setVisible(true);
        });
    }
    
    
    
             
}

