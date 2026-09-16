/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.aaron.proyectoteo.crud;

import com.aaron.proyectoteo.Conexion;
import com.aaron.proyectoteo.transaccion;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 *
 * @author aaron
 */
public class transaccionCRUD {

    public void sp_insertar_transaccion(int id_usuario, int id_presupuesto, int id_subcategoria, Integer id_obligacion, int ano, short mes, short tipo, String descripcion, double monto, LocalDate fecha, String metodo_pago, String numero_factura, String observaciones, String creado_por) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_insertar_transaccion(?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
        state.setInt(1, id_usuario);
        state.setInt(2, id_presupuesto);
        state.setInt(3, id_subcategoria);
        if (id_obligacion != null) {
            state.setInt(4, id_obligacion);
        } else {
            state.setNull(4, Types.INTEGER);
        }
        state.setInt(5, ano);
        state.setShort(6, mes);
        state.setShort(7, tipo);
        state.setString(8, descripcion);
        state.setDouble(9, monto);
        state.setDate(10, Date.valueOf(fecha));
        state.setString(11, metodo_pago);
        state.setString(12, numero_factura);
        state.setString(13, observaciones);
        state.setString(14, creado_por);
        state.execute();
        state.close();
    }

    public void sp_actualizar_transaccion(int id_transaccion, int id_subcategoria, Integer id_obligacion, int ano, short mes, short tipo, String descripcion, double monto, LocalDate fecha, String metodo_pago, String numero_factura, String observaciones, String modificado_por) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_actualizar_transaccion(?,?,?,?,?,?,?,?,?,?,?,?,?)}");
        state.setInt(1, id_transaccion);
        state.setInt(2, id_subcategoria);
        if (id_obligacion != null) {
            state.setInt(3, id_obligacion);
        } else {
            state.setNull(3, Types.INTEGER);
        }
        state.setInt(4, ano);
        state.setShort(5, mes);
        state.setShort(6, tipo);
        state.setString(7, descripcion);
        state.setDouble(8, monto);
        state.setDate(9, Date.valueOf(fecha));
        state.setString(10, metodo_pago);
        state.setString(11, numero_factura);
        state.setString(12, observaciones);
        state.setString(13, modificado_por);
        state.execute();
        state.close();
    }

    public void sp_eliminar_transaccion(int id_transaccion) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_eliminar_transaccion(?)}");
        state.setInt(1, id_transaccion);
        state.execute();
        state.close();
    }

    public ArrayList<transaccion> listarPorPresupuesto(int id_presupuesto, Short tipo, Integer id_subcategoria, Integer ano, Short mes) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall("{CALL dbo.sp_listar_transacciones_presupuesto(?,?,?,?,?)}");
        state.setInt(1, id_presupuesto);
        if (tipo != null) state.setShort(2, tipo); else state.setNull(2, Types.SMALLINT);
        if (id_subcategoria != null) state.setInt(3, id_subcategoria); else state.setNull(3, Types.INTEGER);
        if (ano != null) state.setInt(4, ano); else state.setNull(4, Types.INTEGER);
        if (mes != null) state.setShort(5, mes); else state.setNull(5, Types.SMALLINT);

        ResultSet res = state.executeQuery();
        ArrayList<transaccion> lista = new ArrayList<>();

        while (res.next()) {
            transaccion t = new transaccion();
            t.id_transaccion = res.getInt("id_transaccion");
            t.id_presupuesto = id_presupuesto;
            Date d = res.getDate("fecha");
            if (d != null) t.fecha = d.toLocalDate();
            t.anio = res.getInt("ano");
            t.mes = res.getInt("mes");
            t.tipo = res.getShort("tipo");
            t.descripcion = res.getString("descripcion");
            t.monto = res.getDouble("monto");
            t.metodo_pago = res.getString("metodo_pago");
            t.numero_factura = res.getString("numero_factura");
            t.id_subcategoria = res.getInt("id_subcategoria");
            lista.add(t);
        }

        res.close();
        state.close();
        return lista;
    }
}
