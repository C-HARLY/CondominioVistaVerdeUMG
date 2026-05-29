package logic;

import db.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.ReporteCasaDTO;

/**
 * Data Access Object (DAO) enfocado en la generación de métricas y reportes financieros.
 * Extrae y consolida información analítica cruzando múltiples entidades del sistema 
 * (Casas, Propietarios, Pagos). Para optimizar el rendimiento y no saturar la memoria, 
 * proyecta los resultados directamente sobre el objeto de transferencia {@link ReporteCasaDTO}.
 */
public class ReporteDAO {

    /**
     * Construye el reporte general financiero del condominio para un período específico.
     * * Esta consulta utiliza técnicas avanzadas de agregación:
     * * {@code LEFT JOIN} y {@code COALESCE} para incluir viviendas desocupadas sin generar valores nulos en la interfaz.
     * Subconsultas correlacionadas ({@code CASE WHEN EXISTS}) para determinar el estado de solvencia sin duplicar registros.
     * Agregación dinámica para calcular el acumulado anual por vivienda.
     * [ACTUALIZACIÓN]: Ahora aísla el historial vinculando estrictamente el id_propietario.
     *
     * @param mesActual El mes de la facturación en curso a evaluar (ej. "Mayo").
     * @param anioActual El año fiscal en curso.
     * @return Una lista de objetos {@link ReporteCasaDTO} con los indicadores financieros calculados por cada vivienda.
     */
    public List<ReporteCasaDTO> obtenerReporteGeneral(String mesActual, int anioActual) {

        List<ReporteCasaDTO> listaReporte = new ArrayList<>();

        /*
         * Refactorización a Text Blocks. 
         * Se agregó la condición "id_propietario = pr.id" a todas las subconsultas
         * para ignorar los pagos de dueños anteriores.
         */
        String sql = """
            SELECT 
                c.numero_casa, 
                COALESCE(pr.nombre, 'Sin Asignar') as nombre_propietario, 
                CASE 
                    WHEN pr.id IS NOT NULL AND EXISTS (
                        SELECT 1 FROM pagos p 
                        WHERE p.id_casa = c.id 
                          AND p.id_propietario = pr.id 
                          AND p.mes = ? 
                          AND p.anio = ?
                    ) THEN 'Pagado' 
                    ELSE 'Pendiente' 
                END as estado_mes_actual, 
                
                COALESCE((
                    SELECT SUM(p3.monto) FROM pagos p3 
                    WHERE p3.id_casa = c.id 
                      AND p3.id_propietario = pr.id 
                      AND p3.mes = ? 
                      AND p3.anio = ?
                ), 0.00) as monto_pagado_mes, 
                
                COALESCE((
                    SELECT SUM(p2.monto) FROM pagos p2 
                    WHERE p2.id_casa = c.id 
                      AND p2.id_propietario = pr.id 
                      AND p2.anio = ?
                ), 0.00) as total_pagado_anio 
                
            FROM casas c 
            LEFT JOIN propietarios pr ON pr.id_casa = c.id 
            ORDER BY c.numero_casa ASC
            """;

        // Pool de conexiones con cierre automático (try-with-resources)
        try (Connection conn = Conexion.conectar()) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    
                    // Mapeo de parámetros para las subconsultas correlacionadas (Se mantienen iguales)
                    pstmt.setString(1, mesActual);
                    pstmt.setInt(2, anioActual);
                    pstmt.setString(3, mesActual);
                    pstmt.setInt(4, anioActual);
                    pstmt.setInt(5, anioActual);

                    try (ResultSet rs = pstmt.executeQuery()) {
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
                    }
                }
            } else {
                 System.err.println(" ERROR (ReporteDAO): No se pudo obtener conexión del pool (Timeout).");
            }
        } catch (Exception e) {
            System.err.println(" Error analítico en ReporteDAO.obtenerReporteGeneral: " + e.getMessage());
        }

        return listaReporte;
    }
}