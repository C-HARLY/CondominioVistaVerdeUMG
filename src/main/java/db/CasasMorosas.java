package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.CasaMorosa;

public class CasasMorosas {

    public List<CasaMorosa> obtenerCasasMorosas(int mes, int anio) {

        List<CasaMorosa> lista = new ArrayList<>();

        String sql = """
            SELECT c.id_casa,
                   c.numero_casa,
                   u.nombre,
                   u.apellido,
                   u.telefono
            FROM casas c
            JOIN usuarios u
                ON c.id_usuario = u.id_usuario
            WHERE NOT EXISTS (
                SELECT 1
                FROM pagos p
                WHERE p.id_casa = c.id_casa
                  AND p.mes = ?
                  AND p.anio = ?
            )
        """;

        try (
            Connection conn = Conexion.conectar();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, mes);
            ps.setInt(2, anio);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                CasaMorosa casa = new CasaMorosa();

                casa.setNumeroCasa(rs.getString("numero_casa"));

                casa.setNombre(
                        rs.getString("nombre") + " " +
                        rs.getString("apellido")
                );

                casa.setTelefono(rs.getString("telefono"));

                lista.add(casa);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return lista;
    }
}