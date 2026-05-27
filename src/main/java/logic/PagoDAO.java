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
import model.Pago;

/**
 *
 * @author carlo
 */
public class PagoDAO {
    // Ahora recibimos el objeto Pago completo
    public boolean registrarPago(Pago nuevoPago) {
        
        //  busca el ID real de la casa basado en el numero de casa que seleccionó el usuario
        String sql = "INSERT INTO pagos (id_casa, mes, anio, monto) "
                   + "VALUES ((SELECT id FROM casas WHERE numero_casa = ?), ?, ?, ?)";

        try (Connection conn = Conexion.conectar();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            // Usamos los getters del modelo
            pst.setInt(1, nuevoPago.getNumeroCasa());
            pst.setString(2, nuevoPago.getMes());
            pst.setInt(3, nuevoPago.getYear());
            pst.setDouble(4, nuevoPago.getMonto());

            int filasAfectadas = pst.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.out.println("Error al registrar el pago: " + e.getMessage());
            return false;
        }
    }
    
    //METODO PARA VALIDAR SI YA SE PAGO EL MES ANTERIOR ANTES DE PAGAR EL SIGUIENTE
    public boolean verificarPagoExiste(int idCasa, String mes, int anio) {
    boolean existe = false;
    String sql = "SELECT 1 FROM pagos WHERE id_casa = ? AND mes = ? AND anio = ?";

    try (Connection conn = db.Conexion.conectar();
         PreparedStatement ps = conn.prepareStatement(sql)) {
         
        ps.setInt(1, idCasa);
        ps.setString(2, mes);
        ps.setInt(3, anio);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                existe = true; // Si encuentra al menos una fila, ya está pagado
            }
        }
    } catch (Exception e) {
        System.err.println("Error al verificar pago anterior: " + e.getMessage());
    }
    return existe;
}
    
}
