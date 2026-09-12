/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aaron.proyectoteo;

/**
 *
 * @author aaron
 */
public class categoria {
    int id_categoria;
    String nombre_categoria;
    String descripcion;
    short tipo_categoria;
    short order_presentacion;
    
    public void categoria (int id_categoria,String nombre_categoria,String descripcion,short tipo_categoria,short order_presentacion){
        this.id_categoria = id_categoria;
        this.nombre_categoria = nombre_categoria;
        this.descripcion = descripcion;
        this.tipo_categoria = tipo_categoria;
        this.order_presentacion = order_presentacion;
    }
    
}
