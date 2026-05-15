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
}
