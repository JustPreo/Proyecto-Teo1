/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aaron.proyectoteo;

/**
 *
 * @author aaron
 */
public class subcategoria {
    int id_subcategoria;
    int id_categoria;
    String nombre;
    String descripcion;
    boolean estado;
    boolean es_default;
    


    public void subcategoria(int id_subcategoria,int id_categoria,String nombre,String descripcion,boolean estado,boolean es_default){
    this.id_subcategoria = id_subcategoria;
    this.id_categoria = id_categoria;
    this.nombre = nombre;
    this.descripcion = descripcion;
    this.estado = estado;
    this.es_default = es_default;
    }
}
