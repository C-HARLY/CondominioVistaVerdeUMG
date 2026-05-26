package logic; // O el paquete donde lo tengas

import db.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Propietario; // 🌟 VITAL: Importar tu modelo

/**
 *
 * @author Eluzai
 */
public class CasaDAO {
    
    // Metodo para traer casas disponibles
    public List<Integer> obtenerCasasDisponibles() {
        List<Integer> casasDisponibles = new ArrayList<>();
        // Query: Trae el número de las casas cuyo estado sea 'Disponible'
        String sql = "SELECT numero_casa FROM casas WHERE estado = 'Disponible' ORDER BY numero_casa ASC";

        try (Connection conn = Conexion.conectar();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            // Recorremos los resultados y los agregamos a la lista
            while (rs.next()) {
                casasDisponibles.add(rs.getInt("numero_casa"));
            }

        } catch (SQLException e) {
            System.out.println("Error al cargar las casas disponibles: " + e.getMessage());
        }

        return casasDisponibles;
    } 
   
    // Metodo para la vista de PAGOS (casas ocupadas)
    public List<Integer> obtenerCasasOcupadas() {
        List<Integer> casasOcupadas = new ArrayList<>();
        // Query: Solo trae las casas que SÍ tienen dueño
        String sql = "SELECT numero_casa FROM casas WHERE estado = 'Ocupada' ORDER BY numero_casa ASC";

        try (Connection conn = Conexion.conectar();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                casasOcupadas.add(rs.getInt("numero_casa"));
            }

        } catch (SQLException e) {
            System.out.println("Error al cargar casas ocupadas: " + e.getMessage());
        }

        return casasOcupadas;
    }
    
    // Trae toda la información del dueño de la casa 
    public Propietario obtenerPropietarioPorCasa(int numeroCasa) {
        Propietario prop = null; 
        
        String sql = "SELECT p.id, p.nombre, p.telefono, p.correo, p.fecha_registro FROM propietarios p " +
                     "INNER JOIN casas c ON p.id_casa = c.id " +
                     "WHERE c.numero_casa = ?";

        try (Connection conn = Conexion.conectar();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, numeroCasa);
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    prop = new Propietario();
                    prop.setId(rs.getInt("id"));
                    prop.setNombre(rs.getString("nombre"));
                    prop.setNumeroCasa(numeroCasa); 
                    
                    String tel = rs.getString("telefono");
                    prop.setTelefono((tel != null && !tel.trim().isEmpty()) ? tel : "Sin teléfono");
                    
                    String correo = rs.getString("correo");
                    prop.setCorreo((correo != null && !correo.trim().isEmpty()) ? correo : "Sin correo");

                    // 🌟 CAPTURAMOS LA FECHA Y LA GUARDAMOS EN EL OBJETO
                    java.sql.Date fechaSQL = rs.getDate("fecha_registro");
                    if (fechaSQL != null) {
                        prop.setFechaRegistro(fechaSQL.toLocalDate());
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar el propietario: " + e.getMessage());
        }
        
        return prop; 
    }
}