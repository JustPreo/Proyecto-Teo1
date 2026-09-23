/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aaron.proyectoteo.crud;

import com.aaron.proyectoteo.Conexion;
import com.aaron.proyectoteo.obligacion_fija;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 *
 * @author aaron
 */
public class obligacion_fijaCRUD {

    public void sp_insertar_obligacion(int id_usuario, int id_subcategoria, String nombre, String descripcion, double monto_fijo_mensual, int dia_vencimiento, LocalDate fecha_inicio, LocalDate fecha_fin, String creado_por) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_insertar_obligacion(?,?,?,?,?,?,?,?,?)}");
        state.setInt(1, id_usuario);
        state.setInt(2, id_subcategoria);
        state.setString(3, nombre);
        state.setString(4, descripcion);
        state.setDouble(5, monto_fijo_mensual);
        state.setInt(6, dia_vencimiento);
        state.setDate(7, Date.valueOf(fecha_inicio));
        if (fecha_fin != null) {
            state.setDate(8, Date.valueOf(fecha_fin));
        } else {
            state.setNull(8, Types.DATE);
        }
        state.setString(9, creado_por);
        state.execute();
        state.close();
    }

    public void sp_actualizar_obligacion(int id_obligacion, int id_subcategoria, String nombre, String descripcion, double monto_fijo_mensual, int dia_vencimiento, LocalDate fecha_inicio, LocalDate fecha_fin, boolean vigente, String modificado_por) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_actualizar_obligacion(?,?,?,?,?,?,?,?,?,?)}");
        state.setInt(1, id_obligacion);
        state.setInt(2, id_subcategoria);
        state.setString(3, nombre);
        state.setString(4, descripcion);
        state.setDouble(5, monto_fijo_mensual);
        state.setInt(6, dia_vencimiento);
        state.setDate(7, Date.valueOf(fecha_inicio));
        if (fecha_fin != null) {
            state.setDate(8, Date.valueOf(fecha_fin));
        } else {
            state.setNull(8, Types.DATE);
        }
        state.setBoolean(9, vigente);
        state.setString(10, modificado_por);
        state.execute();
        state.close();
    }

    public void sp_eliminar_obligacion(int id_obligacion, String modificado_por) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_eliminar_obligacion(?,?)}");
        state.setInt(1, id_obligacion);
        state.setString(2, modificado_por);
        state.execute();
        state.close();
    }

    public obligacion_fija sp_consultar_obligacion(int id_obligacion) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_consultar_obligacion(?)}");
        state.setInt(1, id_obligacion);
        ResultSet res = state.executeQuery();

        if (res.next()) {
            obligacion_fija o = new obligacion_fija();
            o.id_obligacion = res.getInt("id_obligacion");
            o.id_usuario = res.getInt("id_usuario");
            o.id_subcategoria = res.getInt("id_subcategoria");
            o.nombre = res.getString("nombre");
            o.descripcion = res.getString("descripcion");
            o.monto_mensual = res.getDouble("monto_fijo_mensual");
            o.dia_vencimiento = res.getInt("dia_vencimiento");
            o.estado = res.getBoolean("vigente");
            Date fi = res.getDate("fecha_inicio");
            if (fi != null) o.fecha_inicio = fi.toLocalDate();
            Date ff = res.getDate("fecha_fin");
            if (ff != null) o.fecha_fin = ff.toLocalDate();
            res.close();
            state.close();
            return o;
        }

        res.close();
        state.close();
        return null;
    }

    public ArrayList<obligacion_fija> listarPorUsuario(int id_usuario, Boolean vigente) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_listar_obligaciones_usuario(?,?)}");
        state.setInt(1, id_usuario);
        if (vigente == null) {
            state.setNull(2, Types.BIT);
        } else {
            state.setBoolean(2, vigente);
        }

        ResultSet res = state.executeQuery();
        ArrayList<obligacion_fija> lista = new ArrayList<>();

        while (res.next()) {
            obligacion_fija o = new obligacion_fija();
            o.id_obligacion = res.getInt("id_obligacion");
            o.id_usuario = id_usuario;
            o.id_subcategoria = res.getInt("id_subcategoria");
            o.nombre = res.getString("nombre");
            o.descripcion = res.getString("descripcion");
            o.monto_mensual = res.getDouble("monto_fijo_mensual");
            o.dia_vencimiento = res.getInt("dia_vencimiento");
            o.estado = res.getBoolean("vigente");
            Date fi = res.getDate("fecha_inicio");
            if (fi != null) o.fecha_inicio = fi.toLocalDate();
            Date ff = res.getDate("fecha_fin");
            if (ff != null) o.fecha_fin = ff.toLocalDate();
            lista.add(o);
        }

        res.close();
        state.close();
        return lista;
    }
}
