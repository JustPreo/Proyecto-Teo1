/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aaron.proyectoteo;

import java.sql.*;

/**
 *
 * @author aaron
 */
public final class Conexion {
    private static final String URL = "jdbc:sqlserver://localhost:1433;"
                + "databaseName=Proyecto;"+"trustServerCertificate=true";
    private static final String user = "sa";
    private static final String contra = "Clave2026q3";

    private Conexion(){}

    public static Connection obtenerConexion()throws SQLException
    {
    return DriverManager.getConnection(URL,user,contra);
    }

    public static void verificarConexion()throws SQLException{
        try (Connection con = obtenerConexion()){
            if(!con.isValid(3)){
                throw new SQLException("WOMP WOMP");

            }
        }
    }


}