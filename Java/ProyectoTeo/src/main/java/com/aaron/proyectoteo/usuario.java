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
public class usuario {
    public int id_usuario;
    public String nombre;
    public String apellido;
    public String correo_electronico;
    public double salario_base;
    public LocalDateTime fecha_registro;
    public boolean estado;

    public usuario() {}

    public usuario(int id_usuario, String nombre, String apellido, String correo_electronico, double salario_base, LocalDateTime fecha_registro, boolean estado) {
        this.id_usuario = id_usuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo_electronico = correo_electronico;
        this.salario_base = salario_base;
        this.fecha_registro = fecha_registro;
        this.estado = estado;
    }

    @Override
    public String toString() {
        return nombre + " " + apellido + " (" + correo_electronico + ")";
    }
}