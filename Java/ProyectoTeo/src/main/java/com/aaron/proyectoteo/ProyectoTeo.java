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
        try{
            Conexion.verificarConexion();
            
            
            int cat = Funciones.fn_obtener_categoria_por_subcategoria(1);
            System.out.println(cat);
            
            
            double monto = Funciones.fn_calcular_monto_ejecutado(1, 1, 1);
            System.out.println(monto);
            

            boolean valido = Funciones.fn_validar_vigencia_presupuesto(LocalDate.of(2026, 1, 1),1);
            System.out.println(valido);
            
            double total2 = Funciones.fn_obtener_total_ejecutado_categoria_mes(1, 1, 1);
            System.out.println(total2);            
            
            double total1 = Funciones.fn_obtener_total_categoria_mes(1, 1, 1, 1);
            System.out.println(total1);
            
            double total3 = Funciones.fn_obtener_categoria_por_subcategoria(1);
            System.out.println(total3);
            
            double total4 = Funciones.fn_obtener_balance_subcategoria(1, 1, 1, 1);
            System.out.println(total4);

                    
        }catch(Exception e){
            System.out.println(e.getMessage());}
        
    }
    
    
    
             
}

