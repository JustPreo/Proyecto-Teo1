/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aaron.proyectoteo.crud;

import com.aaron.proyectoteo.Conexion;
import com.aaron.proyectoteo.usuario;
import java.sql.*;
import java.util.ArrayList;

/**
 *
 * @author aaron
 */
public class usuarioCRUD {

    public void sp_insertar_usuario(String nombre, String apellido, String correo_electronico, double salario_base, String creado_por) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_insertar_usuario(?,?,?,?,?)}");
        state.setString(1, nombre);
        state.setString(2, apellido);
        state.setString(3, correo_electronico);
        state.setDouble(4, salario_base);
        state.setString(5, creado_por);
        state.execute();
        state.close();
    }

    public void sp_actualizar_usuario(int id_usuario, String nombre, String apellido, String correo_electronico, double salario_base, String modificado_por) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_actualizar_usuario(?,?,?,?,?,?)}");
        state.setInt(1, id_usuario);
        state.setString(2, nombre);
        state.setString(3, apellido);
        state.setString(4, correo_electronico);
        state.setDouble(5, salario_base);
        state.setString(6, modificado_por);
        state.execute();
        state.close();
    }

    public void sp_eliminar_usuario(int id_usuario, String modificado_por) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_eliminar_usuario(?,?)}");
        state.setInt(1, id_usuario);
        state.setString(2, modificado_por);
        state.execute();
        state.close();
    }

    public usuario sp_consultar_usuario(int id_usuario) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_consultar_usuario(?)}");
        state.setInt(1, id_usuario);
        ResultSet res = state.executeQuery();

        if (res.next()) {
            usuario u = new usuario();
            u.id_usuario = res.getInt("id_usuario");
            u.nombre = res.getString("nombre");
            u.apellido = res.getString("apellido");
            u.correo_electronico = res.getString("correo_electronico");
            u.salario_base = res.getDouble("salario_base");
            Timestamp ts = res.getTimestamp("fecha_registro");
            if (ts != null) u.fecha_registro = ts.toLocalDateTime();
            u.estado = res.getBoolean("estado");
            res.close();
            state.close();
            return u;
        }

        res.close();
        state.close();
        return null;
    }

    public ArrayList<usuario> listar() throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_listar_usuarios}");
        ResultSet res = state.executeQuery();
        ArrayList<usuario> lista = new ArrayList<>();

        while (res.next()) {
            usuario u = new usuario();
            u.id_usuario = res.getInt("id_usuario");
            u.nombre = res.getString("nombre");
            u.apellido = res.getString("apellido");
            u.correo_electronico = res.getString("correo_electronico");
            u.salario_base = res.getDouble("salario_base");
            Timestamp ts = res.getTimestamp("fecha_registro");
            if (ts != null) u.fecha_registro = ts.toLocalDateTime();
            u.estado = res.getBoolean("estado");
            lista.add(u);
        }

        res.close();
        state.close();
        return lista;
    }
}
