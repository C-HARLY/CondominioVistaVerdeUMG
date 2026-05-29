package logic;

import db.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Pago;

/**
 * Data Access Object (DAO) transaccional encargado del procesamiento de cobros.
 * Actualizado para amarrar los pagos al Propietario (Inquilino) y no solo a la Casa.
 */
public class PagoDAO {

    /**
     * Procesa y registra formalmente una transacción de pago en la base de datos.
     * AHORA INCLUYE EL ID DEL PROPIETARIO PARA SEPARAR HISTORIALES.
     */
    public boolean registrarPago(Pago nuevoPago) {
        
        // El SQL ahora inserta también el id_propietario
        String sql = "INSERT INTO pagos (id_casa, id_propietario, mes, anio, monto) "
                   + "VALUES ((SELECT id FROM casas WHERE numero_casa = ?), ?, ?, ?, ?)";

        try (Connection conn = Conexion.conectar();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, nuevoPago.getNumeroCasa());
            pst.setInt(2, nuevoPago.getIdPropietario()); // Aquí viaja el ID del dueño actual
            pst.setString(3, nuevoPago.getMes());
            pst.setInt(4, nuevoPago.getYear());
            pst.setDouble(5, nuevoPago.getMonto());

            int filasAfectadas = pst.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println(" Error de transacción al registrar pago en PagoDAO: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * NUEVO MÉTODO DEFINITIVO: Trae solo los pagos de una casa que le pertenecen
     * EXCLUSIVAMENTE al dueño actual. Ignora por completo los pagos de dueños anteriores.
     * * @param numeroCasa El número de la casa.
     * @param idPropietario El ID de la persona que vive ahí actualmente.
     */
    public List<Pago> obtenerPagosValidos(int numeroCasa, int idPropietario) {
        List<Pago> listaPagosValidos = new ArrayList<>();
        
        // Consulta SQL limpia: Dame los pagos de esta casa, pero SOLO los de este dueño
        String sql = "SELECT p.* FROM pagos p " +
                     "JOIN casas c ON p.id_casa = c.id " +
                     "WHERE c.numero_casa = ? AND p.id_propietario = ?";

        try (Connection conn = Conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, numeroCasa);
            ps.setInt(2, idPropietario); // Filtro invencible
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Reconstruimos el objeto Pago usando el nuevo constructor de 6 parámetros
                    Pago p = new Pago(
                        rs.getInt("id"),
                        numeroCasa, 
                        rs.getInt("id_propietario"),
                        rs.getString("mes"),
                        rs.getInt("anio"),
                        rs.getDouble("monto")
                    );
                    
                    listaPagosValidos.add(p);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener el historial de pagos del propietario: " + e.getMessage());
        }
        
        return listaPagosValidos;
    }
    
    // (Opcional) Puedes borrar verificarPagoExiste si ya no lo usas en ningún lado,
    // ya que ahora todo lo validamos en el controlador usando obtenerPagosValidos.
}