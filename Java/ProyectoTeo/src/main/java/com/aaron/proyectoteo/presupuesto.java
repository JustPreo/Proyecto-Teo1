/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aaron.proyectoteo;

/**
 *
 * @author aaron
 */
public class presupuesto {
    public int id_presupuesto;
    public int id_usuario;
    public String nombre_descriptivo;
    public int ano_inicio;
    public int mes_inicio;
    public int ano_fin;
    public int mes_fin;
    public double total_ingresos;
    public double total_gastos;
    public double total_ahorro;
    public short estado_presupuesto = 1;
    
    public presupuesto() {}

    public presupuesto(int id_presupuesto, int id_usuario, String nombre_descriptivo, int ano_inicio, int mes_inicio, int ano_fin, int mes_fin,
                             double total_ingresos, double total_gastos, double total_ahorro, short estado_presupuesto){
        this.id_presupuesto = id_presupuesto;
        this.id_usuario = id_usuario;
        this.nombre_descriptivo = nombre_descriptivo;
        this.ano_inicio = ano_inicio;
        this.mes_inicio = mes_inicio;
        this.ano_fin = ano_fin;
        this.mes_fin = mes_fin;
        this.total_ingresos = total_ingresos;
        this.total_gastos = total_gastos;
        this.total_ahorro = total_ahorro;
        this.estado_presupuesto = estado_presupuesto;
    }

    public presupuesto(int id_presupuesto, int id_usuario, String nombre_descriptivo, int ano_inicio, int mes_inicio, int ano_fin, int mes_fin,
                             double total_ingresos, double total_gastos, double total_ahorro){
        this(id_presupuesto, id_usuario, nombre_descriptivo, ano_inicio, mes_inicio, ano_fin, mes_fin, total_ingresos, total_gastos, total_ahorro, (short) 1);
    }

    @Override
    public String toString() {
        return nombre_descriptivo + " (" + mes_inicio + "/" + ano_inicio + " - " + mes_fin + "/" + ano_fin + ")";
    }
}
