package logic;

import db.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.CasaMorosa;

public class CasasMorosasDAO {

    public List<CasaMorosa> obtenerCasasMorosas(String mes, int anio) {
        List<CasaMorosa> lista = new ArrayList<>();

        // SQL de la rama feature (Sin apellido, con correo)
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

        // Estructura de conexión segura de la rama main adaptada a los datos correctos
        try (Connection conn = Conexion.conectar()) {
            
            if (conn != null) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, mes);
                    ps.setInt(2, anio);
                    
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            CasaMorosa casa = new CasaMorosa();
                            casa.setNumeroCasa(rs.getString("numero_casa"));
                            
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
                System.out.println(" CasasMorosasDAO: No se pudo obtener conexión del pool (Timeout).");
            }

        } catch (Exception e) {
            System.out.println(" Error en CasasMorosas DAO: " + e.getMessage());
        }

        return lista;
    }
}