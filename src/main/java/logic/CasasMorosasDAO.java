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

        String sql = """
                        SELECT 
                            c.numero_casa,
                            p.nombre,
                            p.apellido,
                            p.telefono
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

        // 1. Abrimos SOLO la conexión en el primer try
        try (Connection conn = Conexion.conectar()) {
            
            // 2. Validamos que Hikari sí nos haya dado una conexión 
            if (conn != null) {
                
                // 3. Abrimos el Statement de forma segura
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, mes);
                    ps.setInt(2, anio);
                    
                    // 4. Abrimos el ResultSet 
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            CasaMorosa casa = new CasaMorosa();
                            casa.setNumeroCasa(rs.getString("numero_casa"));
                            
                            String nombre = rs.getString("nombre");
                            String apellido = rs.getString("apellido");

                            if (nombre == null) nombre = "";
                            if (apellido == null) apellido = "";

                            casa.setNombre((nombre + " " + apellido).trim());
                            casa.setTelefono(rs.getString("telefono"));

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