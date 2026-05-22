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
import model.Cuota;
/**
 *
 * @author rache
 */
public class CuotaDAO {
   public Cuota obtenerCuota() {

        Cuota cuota = null;

        String sql = "SELECT * FROM cuotas LIMIT 1";

        try (Connection conn = Conexion.conectar();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {

                cuota = new Cuota();

                cuota.setId(rs.getInt("id"));
                cuota.setMontoActual(rs.getInt("monto_actual"));
            }

        } catch (SQLException e) {

            System.out.println("Error al obtener cuota: " + e.getMessage());
        }

        return cuota;
    }

    // ACTUALIZAR CUOTA
    public boolean actualizarCuota(int nuevaCuota) {

        String sql = "UPDATE cuotas SET monto_actual = ? WHERE id = 1";

        try (Connection conn = Conexion.conectar();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setDouble(1, nuevaCuota);

            int filas = pst.executeUpdate();

            return filas > 0;

        } catch (SQLException e) {

            System.out.println("Error al actualizar cuota: " + e.getMessage());

            return false;
        }
    } 
}
