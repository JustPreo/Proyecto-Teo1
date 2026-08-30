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
            
            
            int cat = Funciones.fn_obtener_categoria_por_subcategoria(1);
            System.out.println(cat);
            
            
            double monto = Funciones.fn_calcular_monto_ejecutado(1, 1, 1);
            System.out.println(monto);
            

            
            double total1 = Funciones.fn_obtener_total_categoria_mes(1, 1, 1, 1);
            System.out.println(total1);
            
            double total2 = Funciones.fn_obtener_total_ejecutado_categoria_mes(1, 1, 1);
            System.out.println(total2);
                    
        }catch(Exception e){
            System.out.println(e.getMessage());}
        
    }
    
    
    
             
}

