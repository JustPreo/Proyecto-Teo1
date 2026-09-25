/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aaron.proyectoteo;

import java.sql.*;
import java.time.LocalDate;

/**
 *
 * @author aaron
 */
public final class Funciones {
    

    private Funciones(){
    }
    
    //1 fn_obtener_categoria_por_subcategoria(id_subcategoria)
    public static int fn_obtener_categoria_por_subcategoria(int id_subcategoria)throws SQLException{
        Connection con = Conexion.obtenerConexion();
            
        PreparedStatement state = con.prepareStatement("SELECT dbo.fn_obtener_categoria_por_subcategoria" +
                    "(?) AS cat");
            state.setInt(1, id_subcategoria);
            
          try (ResultSet res = state.executeQuery()) {
            if (res.next()) {
                int categoria = res.getInt("cat");

                if (res.wasNull()) {
                    return -1;
                }

                return categoria;
            }

            return -1;
        }
    }
    
    
    //2 fn_calcular_monto_ejecutado(id_subcategoria, anio, mes)
    public static double fn_calcular_monto_ejecutado(int anio, int mes,int id_subcategoria)throws SQLException{
        Connection con = Conexion.obtenerConexion();
            
        PreparedStatement state = con.prepareStatement("SELECT dbo.fn_calcular_monto_ejecutado" +
                    "(?,?,?) AS monto");
            state.setInt(1, anio);
            state.setInt(2, mes);
            state.setInt(3, id_subcategoria);
            
          try (ResultSet res = state.executeQuery()) {
                    if (res.next()){
                        return res.getDouble("monto");
                    }
            }
          return 0;
        }
    
    
    //3 fn_validar_vigencia_presupuesto
    public static boolean fn_validar_vigencia_presupuesto(LocalDate fecha,int idPresupuesto)throws SQLException{
        Connection con = Conexion.obtenerConexion();
            
        PreparedStatement state = con.prepareStatement("SELECT dbo.fn_validar_vigencia_presupuesto" +
                    "(?,?) AS vigencia");
            state.setDate(1, Date.valueOf(fecha));
            state.setInt(2, idPresupuesto);
            
          try (ResultSet res = state.executeQuery()) {
              String vig = "N";      
              if (res.next()){
                        vig = res.getString("vigencia");
                    }
              return "s".equals(vig.toLowerCase());
            }
        }
    
    //4 fn_obtener_total_ejecutado_categoria_mes
    public static double fn_obtener_total_ejecutado_categoria_mes(int id_categoria, int anio, int mes)throws SQLException{//gastado
        
        Connection con = Conexion.obtenerConexion();
            
        PreparedStatement state = con.prepareStatement("SELECT dbo.fn_obtener_total_ejecutado_categoria_mes" +
                    "(?,?,?) AS total");
            state.setInt(1, id_categoria);
            state.setInt(2, anio);
            state.setInt(3, mes);
            
          try (ResultSet res = state.executeQuery()) {
              if (res.next()){

                    return res.getDouble("total");
              }
        }
          return 0;
    }
    //5 fn_obtener_total_categoria_mes
    public static double fn_obtener_total_categoria_mes(int id_categoria ,int id_presupuesto , int anio, int mes ) throws SQLException{//presupuestado
        
        Connection con = Conexion.obtenerConexion();
            
        PreparedStatement state = con.prepareStatement("SELECT dbo.fn_obtener_total_categoria_mes" +
                    "(?,?,?,?) AS total");
            state.setInt(1, id_categoria);
            state.setInt(2, id_presupuesto);
            state.setInt(3, anio);
            state.setInt(4, mes);
            
          try (ResultSet res = state.executeQuery()) {
              if (res.next()){
                    return res.getDouble("total");
              }
        }
          return 0;
    }
    
    //6 fn_obtener_balance_subcategoria(id_presupuesto, id_subcategoria, anio, mes)
    
    public static double fn_obtener_balance_subcategoria(int id_presupuesto ,int id_subcategoria , int anio, int mes ) throws SQLException{
        
        Connection con = Conexion.obtenerConexion();
            
        PreparedStatement state = con.prepareStatement("SELECT dbo.fn_obtener_balance_subcategoria" +
                    "(?,?,?,?) AS balance");
            state.setInt(1, id_presupuesto);
            state.setInt(2, id_subcategoria);
            state.setInt(3, anio);
            state.setInt(4, mes);
            
          try (ResultSet res = state.executeQuery()) {
              if (res.next()){
                    return res.getDouble("balance");
              }
        }
          return 0;
    }
    
    
    //7 fn_calcular_porcentaje_ejecutado(id_subcategoria, id_presupuesto, anio, mes)
    public static double fn_calcular_porcentaje_ejecutado(int id_subcategoria, int id_presupuesto, int anio, int mes) throws SQLException{
        
        Connection con = Conexion.obtenerConexion();
            
        PreparedStatement state = con.prepareStatement("SELECT dbo.fn_calcular_porcentaje_ejecutado" +
                    "(?,?,?,?) AS porcentaje");
            state.setInt(1, id_subcategoria);
            state.setInt(2, id_presupuesto);
            state.setInt(3, anio);
            state.setInt(4, mes);
            
          try (ResultSet res = state.executeQuery()) {
              if (res.next()){
                    return res.getDouble("porcentaje");
              }
        }
          return 0;
    }
    
    //8 fn_dias_hasta_vencimiento(id_obligacion) int
    
    public static Integer fn_dias_hasta_vencimiento(int id_obligacion) throws SQLException{
        
        Connection con = Conexion.obtenerConexion();
            
        PreparedStatement state = con.prepareStatement("SELECT dbo.fn_dias_hasta_vencimiento" +
                    "(?) AS dias");
            state.setInt(1, id_obligacion);
            
          try (ResultSet res = state.executeQuery()) {
              if (res.next()){
                    int dias = res.getInt("dias");
                    return res.wasNull() ? null : dias;
              }
        }
          return null;
    }
    
    //9 fn_obtener_promedio_gasto_subcategoria(id_usuario, id_subcategoria, cantidad_meses)decimal
    public static double fn_obtener_promedio_gasto_subcategoria(int id_usuario ,int id_subcategoria , int cantidad_meses) throws SQLException{
        
        Connection con = Conexion.obtenerConexion();
            
        PreparedStatement state = con.prepareStatement("SELECT dbo.fn_obtener_promedio_gasto_subcategoria" +
                    "(?,?,?) AS promedio");
            state.setInt(1, id_usuario);
            state.setInt(2, id_subcategoria);
            state.setInt(3, cantidad_meses);
            
          try (ResultSet res = state.executeQuery()) {
              if (res.next()){
                    return res.getDouble("promedio");
              }
        }
          return 0;
    }
    
    
    //10 fn_calcular_proyeccion_gasto_mensual(id_subcategoria, anio, mes)decimal
    
    public static double fn_calcular_proyeccion_gasto_mensual(int id_subcategoria ,int anio , int mes) throws SQLException{
        
        Connection con = Conexion.obtenerConexion();
            
        PreparedStatement state = con.prepareStatement("SELECT dbo.fn_calcular_proyeccion_gasto_mensual" +
                    "(?,?,?) AS proyeccion");
            state.setInt(1, id_subcategoria);
            state.setInt(2, anio);
            state.setInt(3, mes);
            
          try (ResultSet res = state.executeQuery()) {
              if (res.next()){
                    return res.getDouble("proyeccion");
              }
        }
          return 0;
    }
    
    
    
}

    
   
