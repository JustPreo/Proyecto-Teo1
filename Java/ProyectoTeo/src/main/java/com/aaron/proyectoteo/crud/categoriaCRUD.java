/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aaron.proyectoteo.crud;
import com.aaron.proyectoteo.Conexion;
import com.aaron.proyectoteo.categoria;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;

/**
 *
 * @author aaron
 */



public class categoriaCRUD {

    public void sp_insertar_categoria(int id_usuario, String nombre_categoria,String descripcion,short tipo_categoria,short order_presentacion,String creado_por)throws SQLException{
        Connection con=Conexion.obtenerConexion();
        CallableStatement state=con.prepareCall("{CALL dbo.sp_insertar_categoria(?,?,?,?,?,?)}");
        state.setInt(1,id_usuario);
        state.setString(2,nombre_categoria);
        state.setString(3,descripcion);
        state.setShort(4,tipo_categoria);
        state.setShort(5,order_presentacion);
        state.setString(6,creado_por);
        state.execute();
        state.close();
    }

    public void sp_actualizar_categoria(int id_usuario, int id_categoria,String nombre_categoria,String descripcion,short tipo_categoria,short order_presentacion,String modificado_por)throws SQLException{
        Connection con=Conexion.obtenerConexion();
        CallableStatement state=con.prepareCall("{CALL dbo.sp_actualizar_categoria(?,?,?,?,?,?,?)}");
        state.setInt(1,id_usuario);
        state.setInt(2,id_categoria);
        state.setString(3,nombre_categoria);
        state.setString(4,descripcion);
        state.setShort(5,tipo_categoria);
        state.setShort(6,order_presentacion);
        state.setString(7,modificado_por);
        state.execute();
        state.close();
    }

    public void sp_eliminar_categoria(int id_usuario, int id_categoria,String modificado_por)throws SQLException{
        Connection con= Conexion.obtenerConexion();
        CallableStatement state=con.prepareCall("{CALL dbo.sp_eliminar_categoria(?,?,?)}");
        state.setInt(1,id_usuario);
        state.setInt(2,id_categoria);
        state.setString(3,modificado_por);
        state.execute();
        state.close();
    }

    public categoria sp_consultar_categoria(int id_usuario, int id_categoria)throws SQLException{
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_consultar_categoria(?,?)}");
        state.setInt(1,id_usuario);
        state.setInt(2,id_categoria);
        ResultSet res=state.executeQuery();

        if(res.next()){
            categoria c=new categoria();
            c.id_categoria=res.getInt("id_categoria");
            c.nombre_categoria=res.getString("nombre_categoria");
            c.descripcion=res.getString("descripcion");
            c.tipo_categoria=res.getShort("tipo_categoria");
            c.order_presentacion=res.getShort("order_presentacion");
            res.close();
            state.close();
            return c;
        }

        res.close();
        state.close();
        return null;
    }

    public ArrayList<categoria> listar(int id_usuario, Short tipo_categoria)throws SQLException{
        Connection con=Conexion.obtenerConexion();
        CallableStatement state =con.prepareCall("{CALL dbo.sp_listar_categorias(?,?)}");
        state.setInt(1, id_usuario);

        if(tipo_categoria==null){
            state.setNull(2,Types.SMALLINT);
        }else{
            state.setShort(2,tipo_categoria);
        }

        ResultSet res=state.executeQuery();
        ArrayList<categoria> categorias=new ArrayList<>();

        while(res.next()){
            categoria c=new categoria();
            c.id_categoria=res.getInt("id_categoria");
            c.nombre_categoria=res.getString("nombre_categoria");
            c.descripcion=res.getString("descripcion");
            c.tipo_categoria=res.getShort("tipo_categoria");
            c.order_presentacion=res.getShort("order_presentacion");
            categorias.add(c);
        }

        res.close();
        state.close();

        categorias.sort(Comparator
            .comparingInt((categoria c) -> c.tipo_categoria)
            .thenComparingInt(c -> c.order_presentacion)
            .thenComparing(c -> c.nombre_categoria, String.CASE_INSENSITIVE_ORDER));

        return categorias;
    }
}
