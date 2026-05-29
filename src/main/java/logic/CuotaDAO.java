package logic;

import db.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Cuota;

public class CuotaDAO {

    public Cuota obtenerCuota() {
        Cuota cuota = null;
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

    public List<Cuota> obtenerHistorial() {
        List<Cuota> historial = new ArrayList<>();
        String sql = "SELECT id_cambio, monto_cuota, " +
                     "fecha_cambio AT TIME ZONE 'UTC' AT TIME ZONE 'America/Guatemala' AS fecha_cambio " +
                     "FROM historial_cuotas ORDER BY fecha_cambio DESC";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Cuota c = new Cuota();
                c.setId(rs.getInt("id_cambio"));
                c.setMontoActual(rs.getDouble("monto_cuota"));
                c.setFechaCambio(rs.getTimestamp("fecha_cambio").toLocalDateTime());
                historial.add(c);
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener historial: " + e.getMessage());
        }
        return historial;
    }

    public boolean actualizarMontoMantenimiento(double nuevoMonto) {
        long montoRedondeado = Math.round(nuevoMonto);
        String sql = "INSERT INTO historial_cuotas (monto_cuota) VALUES (?)";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setLong(1, montoRedondeado);
            int filas = pst.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            System.out.println("Error al actualizar cuota: " + e.getMessage());
            return false;
        }
    }

    public int obtenerUltimoId() {
        String sql = "SELECT id_cambio FROM historial_cuotas ORDER BY fecha_cambio DESC LIMIT 1";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("id_cambio");
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener ultimo id: " + e.getMessage());
        }
        return -1;
    }

    public boolean eliminarCuota(int id) {
        String sql = "DELETE FROM historial_cuotas WHERE id_cambio = ?";
        try (Connection conn = Conexion.conectar();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, id);
            int filas = pst.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            System.out.println("Error al eliminar cuota: " + e.getMessage());
            return false;
        }
    }
}