/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aaron.proyectoteo.crud;

import com.aaron.proyectoteo.Conexion;
import com.aaron.proyectoteo.categoria;
import com.aaron.proyectoteo.subcategoria;
import java.sql.*;
import java.util.ArrayList;

/**
 *
 * @author aaron
 */
public class subcategoriaCRUD {

    public void sp_insertar_subcategoria(int id_categoria, String nombre, String descripcion, String creado_por) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_insertar_subcategoria(?,?,?,?)}");
        state.setInt(1, id_categoria);
        state.setString(2, nombre);
        state.setString(3, descripcion);
        state.setString(4, creado_por);
        state.execute();
        state.close();
    }

    public void sp_actualizar_subcategoria(int id_subcategoria, String nombre, String descripcion, boolean estado, String modificado_por) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_actualizar_subcategoria(?,?,?,?,?)}");
        state.setInt(1, id_subcategoria);
        state.setString(2, nombre);
        state.setString(3, descripcion);
        state.setBoolean(4, estado);
        state.setString(5, modificado_por);
        state.execute();
        state.close();
    }

    public void sp_eliminar_subcategoria(int id_subcategoria) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_eliminar_subcategoria(?)}");
        state.setInt(1, id_subcategoria);
        state.execute();
        state.close();
    }

    public subcategoria sp_consultar_subcategoria(int id_subcategoria) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_consultar_subcategoria(?)}");
        state.setInt(1, id_subcategoria);
        ResultSet res = state.executeQuery();

        if (res.next()) {
            subcategoria s = new subcategoria();
            s.id_subcategoria = res.getInt("id_subcategoria");
            s.id_categoria = res.getInt("id_categoria");
            s.nombre = res.getString("nombre_subcategoria");
            s.descripcion = res.getString("descripcion");
            s.estado = res.getBoolean("estado");
            s.es_default = res.getBoolean("es_default");
            res.close();
            state.close();
            return s;
        }

        res.close();
        state.close();
        return null;
    }

    public ArrayList<subcategoria> listarPorCategoria(int id_categoria) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_listar_subcategorias_por_categoria(?)}");
        state.setInt(1, id_categoria);
        ResultSet res = state.executeQuery();
        ArrayList<subcategoria> lista = new ArrayList<>();

        while (res.next()) {
            subcategoria s = new subcategoria();
            s.id_subcategoria = res.getInt("id_subcategoria");
            s.id_categoria = res.getInt("id_categoria");
            s.nombre = res.getString("nombre");
            s.descripcion = res.getString("descripcion");
            s.estado = res.getBoolean("estado");
            s.es_default = res.getBoolean("es_default");
            lista.add(s);
        }

        res.close();
        state.close();
        return lista;
    }

    public ArrayList<subcategoria> listarTodas() throws SQLException {
        categoriaCRUD cCrud = new categoriaCRUD();
        ArrayList<categoria> categorias = cCrud.listar(null);
        ArrayList<subcategoria> lista = new ArrayList<>();
        for (categoria c : categorias) {
            ArrayList<subcategoria> subs = listarPorCategoria(c.id_categoria);
            lista.addAll(subs);
        }
        return lista;
    }
}
