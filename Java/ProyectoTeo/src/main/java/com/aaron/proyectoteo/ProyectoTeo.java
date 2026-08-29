/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.aaron.proyectoteo;

import java.sql.*;

/**
 *
 * @author aaron
 */
public class ProyectoTeo {

    public static void main(String[] args) {
        try{
        Conexion.verificarConexion();
            System.out.println("Se pudo");
            PreparedStatement state = Conexion.obenterConexion().prepareStatement("SELECT dbo.fn_calcular_monto_ejecutado" +
                    "(?, ?, ?) AS monto");
            state.setInt(1, 1);
            state.setInt(2, 2);
            state.setInt(3, 1);
            
            try (ResultSet resultado = state.executeQuery()) {

                System.out.println(resultado.next() ? resultado.getBigDecimal("monto"):"a"); 
            }
                    
        }catch(Exception e){
            System.out.println(e.getMessage());}
        
    }
    
    
    
             
}

