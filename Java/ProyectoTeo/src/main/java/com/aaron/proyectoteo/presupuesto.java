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
    int id_presupuesto;
    int id_usuario;
    String nombre_descriptivo;
    int ano_inicio;
    int mes_inicio;
    int ano_fin;
    int mes_fin;
    double total_ingresos;
    double total_gastos;
    double total_ahorro;
    
    
    public void presupuesto(int id_presupuesto,int id_usuario,String nombre_descriptivo,int ano_inicio,int mes_inicio,int ano_fin,int mes_fin,
                            double total_ingresos,double total_gastos,double total_ahorro){
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
    }
}
