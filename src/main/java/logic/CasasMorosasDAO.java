package logic;

import db.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.CasaMorosa;

/**
 * Data Access Object (DAO) especializado en consultas analíticas y reportes de morosidad.
 * 
 * A diferencia de los DAOs transaccionales, esta clase no gestiona una entidad 
 * de dominio pura, sino que orquesta consultas complejas para poblar el DTO 
 * transitorio {@link CasaMorosa}, cruzando datos entre casas, propietarios y pagos.

 */
public class CasasMorosasDAO {

    /**
     * Identifica y recupera el listado de residentes que presentan un estado de 
     * morosidad para un período de facturación específico.
     * 
     *  Se utiliza una subconsulta con {@code NOT EXISTS} 
     * (Anti-Join) en lugar de un {@code LEFT JOIN} tradicional. Esto optimiza el motor 
     * de PostgreSQL, ya que detiene la búsqueda tan pronto como encuentra el primer 
     * registro de pago válido, mejorando el rendimiento a medida que crece el historial.
     * 
     *
     * @param mes  El mes del período de facturación a evaluar (ej. "Enero").
     * @param anio El año del período de facturación a evaluar (ej. 2026).
     * @return Una lista de objetos {@link CasaMorosa} con los datos de contacto 
     * de los propietarios deudores. Retorna una lista vacía si no hay morosos.
     */
    public List<CasaMorosa> obtenerCasasMorosas(String mes, int anio) {
        List<CasaMorosa> lista = new ArrayList<>();

        /* * Se utilizan Text Blocks para mantener la legibilidad de la consulta SQL 
         * y facilitar su mantenimiento sin concatenaciones riesgosas.
         */
        String sql = """
            SELECT 
                c.numero_casa,
                p.nombre,
                p.telefono,
                p.correo
            FROM casas c
            JOIN propietarios p
                ON c.id = p.id_casa
            WHERE c.estado = 'Ocupada'
            AND NOT EXISTS (
                SELECT 1
                FROM pagos pa
                WHERE pa.id_casa = c.id
                  AND pa.mes = ?
                  AND pa.anio = ?
            )
            ORDER BY c.numero_casa ASC
            """;

        // Se mantiene el patrón seguro de cierre de recursos anidados con el pool
        try (Connection conn = Conexion.conectar()) {
            
            if (conn != null) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, mes);
                    ps.setInt(2, anio);
                    
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            CasaMorosa casa = new CasaMorosa();
                            casa.setNumeroCasa(rs.getString("numero_casa"));
                            
                            // Saneamiento y limpieza de datos provenientes de la BD
                            String nombre = rs.getString("nombre");
                            if (nombre == null) nombre = "";
                            
                            casa.setNombre(nombre.trim());
                            casa.setTelefono(rs.getString("telefono"));
                            casa.setCorreo(rs.getString("correo")); 

                            lista.add(casa);
                        }
                    }
                }
            } else {
                System.err.println(" ERROR  (CasasMorosasDAO): No se pudo obtener conexión del pool (Timeout).");
            }

        } catch (Exception e) {
            System.err.println(" Excepción SQL en CasasMorosasDAO al consultar deudores: " + e.getMessage());
        }

        return lista;
    }
}