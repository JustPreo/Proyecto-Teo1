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
            double monto = Funciones.fn_calcular_monto_ejecutado(1, 1, 1);
            System.out.println(monto);
            
            int cat = Funciones.fn_obtener_categoria_por_subcategoria(0);
            System.out.println(cat);
                    
        }catch(Exception e){
            System.out.println(e.getMessage());}
        
    }
    
    
    
             
}

