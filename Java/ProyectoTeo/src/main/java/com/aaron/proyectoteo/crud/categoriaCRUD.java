/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aaron.proyectoteo.crud;
import com.aaron.proyectoteo.Conexion;
import com.aaron.proyectoteo.categoria;
import java.sql.*;
import java.util.ArrayList;

/**
 *
 * @author aaron
 */



public class categoriaCRUD {

    public void sp_insertar_categoria(String nombre_categoria,String descripcion,short tipo_categoria,short order_presentacion,String creado_por)throws SQLException{
        Connection con=Conexion.obtenerConexion();
        CallableStatement state=con.prepareCall("{CALL dbo.sp_insertar_categoria(?,?,?,?,?)}");
        state.setString(1,nombre_categoria);
        state.setString(2,descripcion);
        state.setShort(3,tipo_categoria);
        state.setShort(4,order_presentacion);
        state.setString(5,creado_por);
        state.execute();
        state.close();
    }

    public void sp_actualizar_categoria(int id_categoria,String nombre_categoria,String descripcion,short tipo_categoria,short order_presentacion,String modificado_por)throws SQLException{
        Connection con=Conexion.obtenerConexion();
        CallableStatement state=con.prepareCall("{CALL dbo.sp_actualizar_categoria(?,?,?,?,?,?)}");
        state.setInt(1,id_categoria);
        state.setString(2,nombre_categoria);
        state.setString(3,descripcion);
        state.setShort(4,tipo_categoria);
        state.setShort(5,order_presentacion);
        state.setString(6,modificado_por);
        state.execute();
        state.close();
    }

    public void sp_eliminar_categoria(int id_categoria,String modificado_por)throws SQLException{
        Connection con= Conexion.obtenerConexion();
        CallableStatement state=con.prepareCall("{CALL dbo.sp_eliminar_categoria(?,?)}");
        state.setInt(1,id_categoria);
        state.setString(2,modificado_por);
        state.execute();
        state.close();
    }

    public categoria sp_consultar_categoria(int id_categoria)throws SQLException{
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_consultar_categoria(?)}");
        state.setInt(1,id_categoria);
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

    public ArrayList<categoria> listar(Short tipo_categoria)throws SQLException{
        Connection con=Conexion.obtenerConexion();
        CallableStatement state =con.prepareCall("{CALL dbo.sp_listar_categorias(?)}");

        if(tipo_categoria==null){
            state.setNull(1,Types.SMALLINT);
        }else{
            state.setShort(1,tipo_categoria);
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
        return categorias;
    }
}