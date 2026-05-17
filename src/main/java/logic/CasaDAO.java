/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logic;

import db.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eluzai
 */
public class CasaDAO {
    
    //Metodo para traer casas disponibles
   public List<Integer> obtenerCasasDisponibles() {
        List<Integer> casasDisponibles = new ArrayList<>();
        // Query: Trae el número de las casas cuyo estado sea 'Disponible'
        String sql = "SELECT numero_casa FROM casas WHERE estado = 'Disponible' ORDER BY numero_casa ASC";

        // Usamos try-with-resources con el método Conexion.conectar()
        try (Connection conn = Conexion.conectar();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            // Recorremos los resultados y los agregamos a la lista
            while (rs.next()) {
                casasDisponibles.add(rs.getInt("numero_casa"));
            }

        } catch (SQLException e) {
            System.out.println("Error al cargar las casas: " + e.getMessage());
        }

        return casasDisponibles;
    } 
   
   
   
   // Nuevo método para la vista de PAGOS(casas ocupadas)
    public List<Integer> obtenerCasasOcupadas() {
        List<Integer> casasOcupadas = new ArrayList<>();
        // Query: Solo trae las casas que SÍ tienen dueño
        String sql = "SELECT numero_casa FROM casas WHERE estado = 'Ocupada' ORDER BY numero_casa ASC";

        try (Connection conn = db.Conexion.conectar();
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
    
    // Metodo para obtener la fecha en que se registro el propietario   
    public java.time.LocalDate obtenerFechaRegistroPropietario(int numeroCasa) {
        String sql = "SELECT p.fecha_registro FROM propietarios p " +
                     "INNER JOIN casas c ON p.id_casa = c.id " +
                     "WHERE c.numero_casa = ?";
        
        try (java.sql.Connection conn = db.Conexion.conectar();
             java.sql.PreparedStatement pst = conn.prepareStatement(sql)) {
            
            pst.setInt(1, numeroCasa);
            try (java.sql.ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    java.sql.Date fechaSQL = rs.getDate("fecha_registro");
                    if (fechaSQL != null) {
                        return fechaSQL.toLocalDate(); // Lo convertimos a LocalDate de Java
                    }
                }
            }
        } catch (java.sql.SQLException e) {
            System.out.println("Error al buscar fecha de registro: " + e.getMessage());
        }
        return null; // Si hay error o no tiene fecha
    }
}


