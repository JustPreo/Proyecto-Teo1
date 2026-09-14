/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aaron.proyectoteo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author aaron
 */
public class transaccion {

    int id_transaccion;
    int id_usuario;
    int id_presupuesto;
    int anio;
    int mes;
    int id_subcategoria;
    int id_obligacion;
    short tipo;
    String descripcion;
    double monto;
    LocalDate fecha;
    String metodo_pago;
    String numero_factura;
    String observaciones;
    LocalDateTime fecha_registro;

    public transaccion(int id_transaccion,
                       int id_usuario,
                       int id_presupuesto,
                       int anio,
                       int mes,
                       int id_subcategoria,
                       int id_obligacion,
                       short tipo,
                       String descripcion,
                       double monto,
                       LocalDate fecha,
                       String metodo_pago,
                       String numero_factura,
                       String observaciones,
                       LocalDateTime fecha_registro) {

        this.id_transaccion = id_transaccion;
        this.id_usuario = id_usuario;
        this.id_presupuesto = id_presupuesto;
        this.anio = anio;
        this.mes = mes;
        this.id_subcategoria = id_subcategoria;
        this.id_obligacion = id_obligacion;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.monto = monto;
        this.fecha = fecha;
        this.metodo_pago = metodo_pago;
        this.numero_factura = numero_factura;
        this.observaciones = observaciones;
        this.fecha_registro = fecha_registro;
    }
    
}
