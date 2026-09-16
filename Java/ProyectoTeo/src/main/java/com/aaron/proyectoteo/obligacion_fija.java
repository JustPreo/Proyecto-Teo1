/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aaron.proyectoteo;

import java.time.LocalDate;

/**
 *
 * @author aaron
 */
public class obligacion_fija {

    public int id_obligacion;
    public int id_usuario;
    public int id_subcategoria;
    public String nombre;
    public String descripcion;
    public double monto_mensual;
    public int dia_vencimiento;
    public boolean estado;
    public LocalDate fecha_inicio;
    public LocalDate fecha_fin;

    public obligacion_fija() {}

    public obligacion_fija(int id_obligacion,
                           int id_usuario,
                           int id_subcategoria,
                           String nombre,
                           String descripcion,
                           double monto_mensual,
                           int dia_vencimiento,
                           boolean estado,
                           LocalDate fecha_inicio,
                           LocalDate fecha_fin) {

        this.id_obligacion = id_obligacion;
        this.id_usuario = id_usuario;
        this.id_subcategoria = id_subcategoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.monto_mensual = monto_mensual;
        this.dia_vencimiento = dia_vencimiento;
        this.estado = estado;
        this.fecha_inicio = fecha_inicio;
        this.fecha_fin = fecha_fin;
    }
    
}
