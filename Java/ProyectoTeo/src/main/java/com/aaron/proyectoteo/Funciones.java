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
public final class Funciones {
    

    private Funciones(){
    }
    
    //fn_obtener_categoria_por_subcategoria(id_subcategoria)
    public static int fn_obtener_categoria_por_subcategoria(int id_subcategoria)throws SQLException{
        Connection con = Conexion.obtenerConexion();
            
        PreparedStatement state = con.prepareStatement("SELECT dbo.fn_obtener_categoria_por_subcategoria" +
                    "(?) AS cat");
            state.setInt(1, id_subcategoria);
            
          try (ResultSet res = state.executeQuery()) {
            if (res.next()) {
                int categoria = res.getInt("cat");

                if (res.wasNull()) {
                    return -1;
                }

                return categoria;
            }

            return -1;
        }
    }
    
    
    //fn_calcular_monto_ejecutado(id_subcategoria, anio, mes)
    public static double fn_calcular_monto_ejecutado(int id_subcategoria, int anio, int mes)throws SQLException{
        Connection con = Conexion.obtenerConexion();
            
        PreparedStatement state = con.prepareStatement("SELECT dbo.fn_calcular_monto_ejecutado" +
                    "(?,?,?) AS monto");
            state.setInt(1, id_subcategoria);
            state.setInt(2, anio);
            state.setInt(3, mes);
            
          try (ResultSet res = state.executeQuery()) {
                if (res.next()) {
                    return res.getDouble("monto");
                }
                return 0.00;
            }
        }
    }
   
