/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aaron.proyectoteo;

/**
 *
 * @author aaron
 */
public class presupuesto_detalle {

    public int id_presupuesto_detalle;
    public int id_presupuesto;
    public int id_subcategoria;
    public double monto_mensual;
    public String observaciones;

    public presupuesto_detalle() {}

    public presupuesto_detalle(int id_presupuesto_detalle,
                               int id_presupuesto,
                               int id_subcategoria,
                               double monto_mensual,
                               String observaciones) {

        this.id_presupuesto_detalle = id_presupuesto_detalle;
        this.id_presupuesto = id_presupuesto;
        this.id_subcategoria = id_subcategoria;
        this.monto_mensual = monto_mensual;
        this.observaciones = observaciones;
    }
}
    
