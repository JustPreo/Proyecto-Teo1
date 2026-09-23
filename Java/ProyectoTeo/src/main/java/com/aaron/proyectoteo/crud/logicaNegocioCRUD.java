package com.aaron.proyectoteo.crud;

import com.aaron.proyectoteo.Conexion;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/** Acceso Java a los procedimientos de lógica de negocio de la base de datos. */
public class logicaNegocioCRUD {

    public double calcularMontoEjecutadoMes(int idSubcategoria, int idPresupuesto,
            int anio, int mes) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall(
                "{CALL dbo.sp_calcular_monto_ejecutado_mes(?,?,?,?,?)}");
        state.setInt(1, idSubcategoria);
        state.setInt(2, idPresupuesto);
        state.setInt(3, anio);
        state.setInt(4, mes);
        state.registerOutParameter(5, Types.DECIMAL);
        state.execute();
        double resultado = state.getDouble(5);
        state.close();
        return resultado;
    }

    public double calcularPorcentajeEjecucionMes(int idSubcategoria, int idPresupuesto,
            int anio, int mes) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall(
                "{CALL dbo.sp_calcular_porcentaje_ejecucion_mes(?,?,?,?,?)}");
        state.setInt(1, idSubcategoria);
        state.setInt(2, idPresupuesto);
        state.setInt(3, anio);
        state.setInt(4, mes);
        state.registerOutParameter(5, Types.DECIMAL);
        state.execute();
        double resultado = state.getDouble(5);
        state.close();
        return resultado;
    }

    public ResumenCategoria obtenerResumenCategoriaMes(int idCategoria, int idPresupuesto,
            int anio, int mes) throws SQLException {
        Connection con = Conexion.obtenerConexion();
        CallableStatement state = con.prepareCall(
                "{CALL dbo.sp_obtener_resumen_categoria_mes(?,?,?,?,?,?,?)}");
        state.setInt(1, idCategoria);
        state.setInt(2, idPresupuesto);
        state.setInt(3, anio);
        state.setInt(4, mes);
        state.registerOutParameter(5, Types.DECIMAL);
        state.registerOutParameter(6, Types.DECIMAL);
        state.registerOutParameter(7, Types.DECIMAL);
        state.execute();
        ResumenCategoria resultado = new ResumenCategoria(
                state.getDouble(5), state.getDouble(6), state.getDouble(7));
        state.close();
        return resultado;
    }

    public static class ResumenCategoria {
        public final double montoPresupuestado;
        public final double montoEjecutado;
        public final double porcentaje;

        public ResumenCategoria(double montoPresupuestado, double montoEjecutado, double porcentaje) {
            this.montoPresupuestado = montoPresupuestado;
            this.montoEjecutado = montoEjecutado;
            this.porcentaje = porcentaje;
        }
    }
}
