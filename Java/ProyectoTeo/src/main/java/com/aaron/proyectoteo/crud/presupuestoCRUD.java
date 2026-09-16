/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aaron.proyectoteo.crud;

import com.aaron.proyectoteo.Conexion;
import com.aaron.proyectoteo.presupuesto;
import java.sql.*;
import java.util.ArrayList;

/**
 *
 * @author aaron
 */
public class presupuestoCRUD {

    public void sp_insertar_presupuesto(int id_usuario, String nombre, int ano_inicio, short mes_inicio, int ano_fin, short mes_fin, double total_ingresos, double total_gastos, double total_ahorro, String creado_por) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_insertar_presupuesto(?,?,?,?,?,?,?,?,?,?)}");
        state.setInt(1, id_usuario);
        state.setString(2, nombre);
        state.setInt(3, ano_inicio);
        state.setShort(4, mes_inicio);
        state.setInt(5, ano_fin);
        state.setShort(6, mes_fin);
        state.setDouble(7, total_ingresos);
        state.setDouble(8, total_gastos);
        state.setDouble(9, total_ahorro);
        state.setString(10, creado_por);
        state.execute();
        state.close();
    }

    public void sp_actualizar_presupuesto(int id_presupuesto, String nombre, int ano_inicio, short mes_inicio, int ano_fin, short mes_fin, double total_ingresos, double total_gastos, double total_ahorro, short estado_presupuesto, String modificado_por) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_actualizar_presupuesto(?,?,?,?,?,?,?,?,?,?,?)}");
        state.setInt(1, id_presupuesto);
        state.setString(2, nombre);
        state.setInt(3, ano_inicio);
        state.setShort(4, mes_inicio);
        state.setInt(5, ano_fin);
        state.setShort(6, mes_fin);
        state.setDouble(7, total_ingresos);
        state.setDouble(8, total_gastos);
        state.setDouble(9, total_ahorro);
        state.setShort(10, estado_presupuesto);
        state.setString(11, modificado_por);
        state.execute();
        state.close();
    }

    public void sp_eliminar_presupuesto(int id_presupuesto) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_eliminar_presupuesto(?)}");
        state.setInt(1, id_presupuesto);
        state.execute();
        state.close();
    }

    public presupuesto sp_consultar_presupuesto(int id_presupuesto) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_consultar_presupuesto(?)}");
        state.setInt(1, id_presupuesto);
        ResultSet res = state.executeQuery();

        if (res.next()) {
            presupuesto p = new presupuesto();
            p.id_presupuesto = res.getInt("id_presupuesto");
            p.id_usuario = res.getInt("id_usuario");
            p.nombre_descriptivo = res.getString("nombre_descriptivo");
            p.ano_inicio = res.getInt("ano_inicio");
            p.mes_inicio = res.getInt("mes_inicio");
            p.ano_fin = res.getInt("ano_fin");
            p.mes_fin = res.getInt("mes_fin");
            p.total_ingresos = res.getDouble("total_ingresos");
            p.total_gastos = res.getDouble("total_gastos");
            p.total_ahorro = res.getDouble("total_ahorro");
            p.estado_presupuesto = res.getShort("estado_presupuesto");
            res.close();
            state.close();
            return p;
        }

        res.close();
        state.close();
        return null;
    }

    public ArrayList<presupuesto> listarPorUsuario(int id_usuario, Short estado) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_listar_presupuestos_usuario(?,?)}");
        state.setInt(1, id_usuario);
        if (estado == null) {
            state.setNull(2, Types.SMALLINT);
        } else {
            state.setShort(2, estado);
        }

        ResultSet res = state.executeQuery();
        ArrayList<presupuesto> lista = new ArrayList<>();

        while (res.next()) {
            presupuesto p = new presupuesto();
            p.id_presupuesto = res.getInt("id_presupuesto");
            p.id_usuario = id_usuario;
            p.nombre_descriptivo = res.getString("nombre_descriptivo");
            p.total_ingresos = res.getDouble("total_ingresos");
            p.total_gastos = res.getDouble("total_gastos");
            p.total_ahorro = res.getDouble("total_ahorro");
            p.estado_presupuesto = res.getShort("estado_presupuesto");
            lista.add(p);
        }

        res.close();
        state.close();
        return lista;
    }

    public ArrayList<presupuesto> listarTodos() throws SQLException {
        // Obtenemos los presupuestos llamando al procedure oficial del usuario (admin id=1)
        return listarPorUsuario(1, null);
    }
}
