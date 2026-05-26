package logic;

import db.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.CasaMorosa;

public class CasasMorosasDAO {

    // Método que obtiene las casas ocupadas que no han realizado pago
    // en el mes y año seleccionados
    public List<CasaMorosa> obtenerCasasMorosas(String mes, int anio) {

        // Lista donde se almacenarán las casas morosas encontradas
        List<CasaMorosa> lista = new ArrayList<>();

        // Consulta SQL para buscar casas sin pago registrado
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

        try (
            // Conexión a la base de datos
            Connection conn = Conexion.conectar();

            // Preparación de la consulta SQL
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            // Se asignan los parámetros mes y año
            ps.setString(1, mes);
            ps.setInt(2, anio);

            // Ejecución de la consulta
            ResultSet rs = ps.executeQuery();

            // Recorre los resultados obtenidos
            while (rs.next()) {

                CasaMorosa casa = new CasaMorosa();

                // Obtiene el número de casa
                casa.setNumeroCasa(rs.getString("numero_casa"));

                // Obtiene el nombre del propietario
                String nombre = rs.getString("nombre");

                if (nombre == null) {
                    nombre = "";
                }

                casa.setNombre(nombre);

                // Obtiene el teléfono del propietario
                casa.setTelefono(rs.getString("telefono"));

                // Obtiene el correo del propietario
                casa.setCorreo(rs.getString("correo"));

                // Agrega el objeto a la lista final
                lista.add(casa);
            }

        } catch (Exception e) {

            // Muestra errores en consola para facilitar depuración
            System.out.println("Error en CasasMorosas DAO: " + e.getMessage());
        }

        // Retorna la lista de casas morosas
        return lista;
    }
}