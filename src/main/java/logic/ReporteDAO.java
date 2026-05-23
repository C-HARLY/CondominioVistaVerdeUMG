package logic;

import db.Conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.List;

import model.ReporteCasaDTO;

public class ReporteDAO {

    // CONEXIÓN GLOBAL REUTILIZABLE
    private final Connection conn;

    // CONSTRUCTOR
    public ReporteDAO() {

        this.conn = Conexion.conectar();
    }

    /* =========================================================
       REPORTE GENERAL
    ========================================================= */
    public List<ReporteCasaDTO> obtenerReporteGeneral(String mesActual, int anioActual) {

        List<ReporteCasaDTO> listaReporte = new ArrayList<>();

        String sql =
            "SELECT " +
            "    c.numero_casa, " +
            "    COALESCE(pr.nombre, 'Sin Asignar') as nombre_propietario, " +
            "    CASE " +
            "        WHEN EXISTS (SELECT 1 FROM pagos p WHERE p.id_casa = c.id AND p.mes = ? AND p.anio = ?) " +
            "        THEN 'Pagado' ELSE 'Pendiente' " +
            "    END as estado_mes_actual, " +
            "    COALESCE((SELECT SUM(p3.monto) FROM pagos p3 WHERE p3.id_casa = c.id AND p3.mes = ? AND p3.anio = ?), 0.00) as monto_pagado_mes, " +
            "    COALESCE((SELECT SUM(p2.monto) FROM pagos p2 WHERE p2.id_casa = c.id AND p2.anio = ?), 0.00) as total_pagado_anio " +
            "FROM casas c " +
            "LEFT JOIN propietarios pr ON pr.id_casa = c.id " +
            "ORDER BY c.numero_casa ASC";

        try {

            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, mesActual);
            pstmt.setInt(2, anioActual);

            pstmt.setString(3, mesActual);
            pstmt.setInt(4, anioActual);

            pstmt.setInt(5, anioActual);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {

                ReporteCasaDTO fila = new ReporteCasaDTO(

                    rs.getInt("numero_casa"),

                    rs.getString("nombre_propietario"),

                    rs.getString("estado_mes_actual"),

                    rs.getDouble("monto_pagado_mes"),

                    rs.getDouble("total_pagado_anio")
                );

                listaReporte.add(fila);
            }

            rs.close();
            pstmt.close();

        } catch (Exception e) {

            System.err.println(
                "Error crítico en ReporteDAO.obtenerReporteGeneral: "
                + e.getMessage()
            );

            e.printStackTrace();
        }

        return listaReporte;
    }

    /* =========================================================
       OBTENER CUOTA ACTUAL
    ========================================================= */
    public double obtenerCuotaActual() {

        double cuota = 1500.00;

        String sql =
            "SELECT monto_actual FROM cuotas LIMIT 1";

        try {

            PreparedStatement pstmt =
                conn.prepareStatement(sql);

            ResultSet rs =
                pstmt.executeQuery();

            if (rs.next()) {

                cuota =
                    rs.getDouble("monto_actual");
            }

            rs.close();
            pstmt.close();

        } catch (Exception e) {

            System.err.println(
                "Error al obtener la cuota de la DB: "
                + e.getMessage()
            );
        }

        return cuota;
    }
}