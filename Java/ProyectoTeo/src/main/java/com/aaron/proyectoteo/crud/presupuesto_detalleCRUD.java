/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aaron.proyectoteo.crud;

import com.aaron.proyectoteo.Conexion;
import com.aaron.proyectoteo.presupuesto_detalle;
import java.sql.*;
import java.util.ArrayList;

/**
 *
 * @author aaron
 */
public class presupuesto_detalleCRUD {

    public void sp_insertar_presupuesto_detalle(int id_presupuesto, int id_subcategoria, double monto_mensual, String justificacion_monto, String creado_por) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_insertar_presupuesto_detalle(?,?,?,?,?)}");
        state.setInt(1, id_presupuesto);
        state.setInt(2, id_subcategoria);
        state.setDouble(3, monto_mensual);
        state.setString(4, justificacion_monto);
        state.setString(5, creado_por);
        state.execute();
        state.close();
    }

    public void sp_actualizar_presupuesto_detalle(int id_detalle, double monto_mensual, String justificacion_monto, String modificado_por) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_actualizar_presupuesto_detalle(?,?,?,?)}");
        state.setInt(1, id_detalle);
        state.setDouble(2, monto_mensual);
        state.setString(3, justificacion_monto);
        state.setString(4, modificado_por);
        state.execute();
        state.close();
    }

    public void sp_eliminar_presupuesto_detalle(int id_detalle) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_eliminar_presupuesto_detalle(?)}");
        state.setInt(1, id_detalle);
        state.execute();
        state.close();
    }

    public ArrayList<presupuesto_detalle> listarPorPresupuesto(int id_presupuesto) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_listar_detalles_presupuesto(?)}");
        state.setInt(1, id_presupuesto);
        ResultSet res = state.executeQuery();
        ArrayList<presupuesto_detalle> lista = new ArrayList<>();

        while (res.next()) {
            presupuesto_detalle pd = new presupuesto_detalle();
            pd.id_presupuesto_detalle = res.getInt("id_detalle");
            pd.id_presupuesto = res.getInt("id_presupuesto");
            pd.id_subcategoria = res.getInt("id_subcategoria");
            pd.monto_mensual = res.getDouble("monto_mensual");
            pd.observaciones = res.getString("justificacion_monto");
            lista.add(pd);
        }

        res.close();
        state.close();
        return lista;
    }
}
