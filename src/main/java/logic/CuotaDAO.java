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
        
        // CAMBIO 1: Ahora busca en el historial y trae el más reciente
        String sql = "SELECT * FROM historial_cuotas ORDER BY fecha_cambio DESC LIMIT 1";

        try (Connection conn = Conexion.conectar();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            if (rs.next()) {
                cuota = new Cuota();
                
                cuota.setId(rs.getInt("id_cambio"));
                cuota.setMontoActual(rs.getDouble("monto_cuota"));
            }

        } catch (SQLException e) {
            System.out.println("Error al obtener cuota: " + e.getMessage());
        }

        return cuota;
    }

    // ACTUALIZAR CUOTA
    public boolean actualizarMontoMantenimiento(double nuevoMonto) {
        
        //  CAMBIO : El UPDATE se convirtió en INSERT para llevar la bitácora
        String sql = "INSERT INTO historial_cuotas (monto_cuota) VALUES (?)";

        try (Connection conn = Conexion.conectar();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setDouble(1, nuevoMonto);
            int filas = pst.executeUpdate();

            return filas > 0;

        } catch (SQLException e) {
            System.out.println("Error al actualizar cuota: " + e.getMessage());
            return false;
        }
    } 
}