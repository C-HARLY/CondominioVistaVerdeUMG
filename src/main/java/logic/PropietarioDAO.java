/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logic;

import db.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import model.Propietario;

/**
 *
 * @author Eluzai
 */
public class PropietarioDAO {
    public boolean registrar(Propietario prop) {
        // SQL para insertar el propietario buscando el ID de la casa por su número
        String sqlInsert = "INSERT INTO propietarios (nombre, telefono, correo, id_casa) "
                         + "VALUES (?, ?, ?, (SELECT id FROM casas WHERE numero_casa = ?))";
        
        // SQL para marcar la casa como ocupada
        String sqlUpdateCasa = "UPDATE casas SET estado = 'Ocupada' WHERE numero_casa = ?";

        try (Connection conn = Conexion.conectar()) {
            // Desactivamos el auto-commit para manejar la transacción nosotros
            conn.setAutoCommit(false);

            try (PreparedStatement psProp = conn.prepareStatement(sqlInsert);
                 PreparedStatement psCasa = conn.prepareStatement(sqlUpdateCasa)) {

                // Datos del propietario
                psProp.setString(1, prop.getNombre());
                psProp.setString(2, prop.getTelefono());
                psProp.setString(3, prop.getCorreo());
                psProp.setInt(4, prop.getNumeroCasa());
                psProp.executeUpdate();

                // Actualizar estado de la casa
                psCasa.setInt(1, prop.getNumeroCasa());
                psCasa.executeUpdate();

                // Si llegamos aquí sin errores, confirmamos todo en la nube (Neon)
                conn.commit();
                return true;

            } catch (SQLException e) {
                // Si algo falla, deshacemos lo que se haya hecho para no dejar basura
                conn.rollback();
                System.out.println("Error al registrar: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error de conexión: " + e.getMessage());
            return false;
        }
    }
}
