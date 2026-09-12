/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aaron.proyectoteo;

import java.time.LocalDateTime;

/**
 *
 * @author aaron
 */
public class usuario {//Crearlo por ahora , despues miro su utilidad
    int id_usuario;
            String nombre; 
            String apellido;
            String correo_electronico;
            double salario_base;
            private LocalDateTime fecha_registro;
            
    public void usuario(int id_usuario,String nombre , String apellido , String correo_electronico , double salario_base , LocalDateTime fecha_registro){
    this.nombre = nombre;
    this.apellido = apellido;
    this.correo_electronico = correo_electronico;
    this.salario_base = salario_base;
    this.fecha_registro = fecha_registro;
    }//Creacion del user (maybe al seleccionar(?
    
    
    
    
}