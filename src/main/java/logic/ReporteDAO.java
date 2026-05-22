/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logic;

import db.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.ReporteCasaDTO;

/**
 *
 * @author carlo
 */
public class ReporteDAO {
    
  
    public List<ReporteCasaDTO> obtenerReporteGeneral(String mesActual, int anioActual) {
        List<ReporteCasaDTO> listaReporte = new ArrayList<>();
        
        // Copiamos exactamente la query que ya probaste y te funcionó en Neon
       String sql = "SELECT " +
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

        // NOTA: Reemplaza 'ConexionDB.conectar()' por el método real con el que abres tu conexión a Neon
        try (Connection conn = Conexion.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Inyectamos las 5 variables en orden:
            pstmt.setString(1, mesActual); // Para el estado (mes)
            pstmt.setInt(2, anioActual);   // Para el estado (año)
            pstmt.setString(3, mesActual); // Para el monto del mes (mes)
            pstmt.setInt(4, anioActual);   // Para el monto del mes (año)
            pstmt.setInt(5, anioActual);   // Para el monto anual total (año)

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Instanciamos el objeto DTO con los datos de la fila actual
                    ReporteCasaDTO fila = new ReporteCasaDTO(
                        rs.getInt("numero_casa"),
                        rs.getString("nombre_propietario"),
                        rs.getString("estado_mes_actual"),
                        rs.getDouble("monto_pagado_mes"),
                        rs.getDouble("total_pagado_anio")
                    );
                    // Lo agregamos a la lista
                    listaReporte.add(fila);
                }
            }
        } catch (Exception e) {
            // Un buen log en consola por si algo truena a nivel de driver o red
            System.err.println("Error crítico en ReporteDAO.obtenerReporteGeneral: " + e.getMessage());
            e.printStackTrace();
        }
        
        return listaReporte;
    }
    
    public double obtenerCuotaActual() {
    double cuota = 1500.00; // Valor por defecto por si pasa algo
    String sql = "SELECT monto_actual FROM cuotas LIMIT 1"; // Trae el único registro que hay
    
    try (Connection conn = Conexion.conectar();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {
        
        if (rs.next()) {
            cuota = rs.getDouble("monto_actual");
        }
    } catch (Exception e) {
        System.err.println("Error al obtener la cuota de la DB: " + e.getMessage());
    }
    return cuota;
}
}
