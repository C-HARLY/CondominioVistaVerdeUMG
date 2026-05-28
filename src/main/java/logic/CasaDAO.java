package logic;

import db.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Propietario;

/**
 * Data Access Object (DAO) para la gestión de la entidad Casa.
 * 
 * Centraliza todas las transacciones de lectura y escritura hacia la base de datos 
 * relacionadas con la disponibilidad de las viviendas y la asignación de sus 
 * respectivos propietarios en el Condominio Vista Verde.
 */
public class CasaDAO {
    
    /**
     * Consulta el listado de casas que actualmente no tienen un propietario asignado.
     * Este método es vital para poblar los componentes de la interfaz de usuario (ej. ComboBox)
     * al momento de registrar un nuevo residente en el sistema.
     *
     * @return Una lista de enteros conteniendo los números de las casas con estado 'Disponible'.
     * Retorna una lista vacía si todas las casas están ocupadas o si ocurre un error SQL.
     */
    public List<Integer> obtenerCasasDisponibles() {
        List<Integer> casasDisponibles = new ArrayList<>();
        String sql = "SELECT numero_casa FROM casas WHERE estado = 'Disponible' ORDER BY numero_casa ASC";

        // El uso de try-with-resources garantiza el cierre automático de la conexión al pool
        try (Connection conn = Conexion.conectar();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                casasDisponibles.add(rs.getInt("numero_casa"));
            }

        } catch (SQLException e) {
            System.err.println("Error crítico al cargar casas disponibles en CasaDAO: " + e.getMessage());
        }

        return casasDisponibles;
    } 
   
    /**
     * Consulta el catálogo de casas que actualmente cuentan con un propietario activo.
     * Se utiliza principalmente en el módulo de pagos y reportes financieros para evitar
     * que se intente cobrar una cuota a una vivienda deshabitada.
     *
     * @return Una lista de enteros con los números de las casas con estado 'Ocupada'.
     */
    public List<Integer> obtenerCasasOcupadas() {
        List<Integer> casasOcupadas = new ArrayList<>();
        String sql = "SELECT numero_casa FROM casas WHERE estado = 'Ocupada' ORDER BY numero_casa ASC";

        try (Connection conn = Conexion.conectar();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                casasOcupadas.add(rs.getInt("numero_casa"));
            }

        } catch (SQLException e) {
            System.err.println("Error  al cargar casas ocupadas en CasaDAO: " + e.getMessage());
        }

        return casasOcupadas;
    }
    
    /**
     * Recupera el perfil completo del propietario asociado a una vivienda específica,
     * cruzando la información relacional entre la tabla 'propietarios' y 'casas'.
     * 
     *  El método implementa validaciones de seguridad para 
     * evitar excepciones de tipo NullPointer en la vista, asignando valores por defecto 
     * ("Sin teléfono", "Sin correo") si los campos opcionales vienen vacíos desde la BD.
     * 
     *
     * @param numeroCasa El identificador lógico de la vivienda a consultar.
     * @return Una instancia del modelo {@link Propietario} con los datos mapeados, 
     * o null si la casa no existe o no tiene un dueño asignado.
     */
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
                    
                    // Saneamiento de datos nulos o en blanco
                    String tel = rs.getString("telefono");
                    prop.setTelefono((tel != null && !tel.trim().isEmpty()) ? tel : "Sin teléfono");
                    
                    String correo = rs.getString("correo");
                    prop.setCorreo((correo != null && !correo.trim().isEmpty()) ? correo : "Sin correo");

                    // Conversión segura de java.sql.Date a la API de Fechas moderna de Java 8+ (LocalDate)
                    java.sql.Date fechaSQL = rs.getDate("fecha_registro");
                    if (fechaSQL != null) {
                        prop.setFechaRegistro(fechaSQL.toLocalDate());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error relacional al buscar propietario en CasaDAO: " + e.getMessage());
        }
        
        return prop; 
    }
}