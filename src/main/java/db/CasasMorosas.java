package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.CasaMorosa;

public class CasasMorosas {

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

        try (
            Connection conn = Conexion.conectar();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, mes);
            ps.setInt(2, anio);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                CasaMorosa casa = new CasaMorosa();

                casa.setNumeroCasa(rs.getString("numero_casa"));

                
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");

                if (nombre == null) nombre = "";
                if (apellido == null) apellido = "";

                casa.setNombre(nombre + " " + apellido);

                casa.setTelefono(rs.getString("telefono"));

                lista.add(casa);
            }

        } catch (Exception e) {
            System.out.println("Error en CasasMorosas DAO: " + e.getMessage());
        }

        return lista;
    }
}