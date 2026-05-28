package logic;

import db.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.Pago;

/**
 * Data Access Object (DAO) transaccional encargado del procesamiento de cobros.
 * 
 * Gestiona el registro y la validación de los pagos de cuotas de mantenimiento,
 * asegurando la integridad financiera del condominio y previniendo anomalías 
 * comunes como un pago duplicado en un mismo período.
 
 */
public class PagoDAO {
    /**
     * Procesa y registra formalmente una transacción de pago en la base de datos.
     * El método emplea una subconsulta SQL dinámica 
     * {@code (SELECT id FROM casas...)} para resolver la llave foránea (FK). 
     * Esto permite que la capa de la Vista (JavaFX) trabaje exclusivamente con 
     * el identificador de negocio (número de casa), mientras que el DAO se encarga 
     * de la resolución estructural hacia la base de datos.
     * 
     * @param nuevoPago Objeto que encapsula todos los detalles de la transacción (monto, período, etc.).
     * @return true si el pago se registró con éxito en el libro contable; false si ocurrió un error.
     */
    public boolean registrarPago(Pago nuevoPago) {
        
        String sql = "INSERT INTO pagos (id_casa, mes, anio, monto) "
                   + "VALUES ((SELECT id FROM casas WHERE numero_casa = ?), ?, ?, ?)";

        try (Connection conn = Conexion.conectar();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, nuevoPago.getNumeroCasa());
            pst.setString(2, nuevoPago.getMes());
            pst.setInt(3, nuevoPago.getYear());
            pst.setDouble(4, nuevoPago.getMonto());

            int filasAfectadas = pst.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println(" Error de transacción al registrar pago en PagoDAO: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Motor de validación preventiva contra cobros duplicados.
     * 
     * Antes de autorizar una nueva transacción, este método consulta el historial 
     * para garantizar que no exista un recibo emitido para la misma casa en el 
     * mismo mes y año. Hace sinergia directa con la restricción {@code UNIQUE} 
     * configurada en el diseño relacional de la tabla 'pagos'.
     * 
     * 
     * Se utiliza {@code SELECT 1} en lugar de {@code SELECT *} 
     * para minimizar el consumo de ancho de banda y memoria de la JVM.
     * 
     * @param idCasa El identificador único estructural de la vivienda.
     * @param mes El mes del período de facturación a validar.
     * @param anio El año del período de facturación a validar.
     * @return true si el sistema detecta que el período ya fue cancelado previamente; false si está libre de pago.
     */
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
                    existe = true; 
                }
            }
        } catch (Exception e) {
            System.err.println(" Error de validación al verificar historial de pagos en PagoDAO: " + e.getMessage());
        }
        return existe;
    }
}