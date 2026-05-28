package logic;

import db.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import model.Cuota;

/**
 * Data Access Object (DAO) para la gestión paramétrica de las cuotas de mantenimiento.
 * 
 *  Esta clase implementa un patrón de registro de solo 
 * adición (Append-Only Log) o Bitácora Histórica. En lugar de sobrescribir el 
 * valor de la cuota en una única fila, cada cambio inserta un nuevo registro. 
 * Esto garantiza la inmutabilidad de los datos, permitiendo registro financiero 
 * y asegurando que exista un rastro exacto de las variaciones de precios en el tiempo.
 *
 
 */
public class CuotaDAO {
    
    /**
     * Recupera el monto de mantenimiento vigente aplicable a los nuevos cobros.
     * 
     * Utiliza una consulta optimizada que ordena el historial de cuotas de forma 
     * descendente por la fecha de modificación y extrae únicamente el registro 
     * más reciente (Top 1).
     * 
     *
     * @return Una instancia de {@link Cuota} con el monto actual vigente, 
     * o null si no existe ninguna configuración inicial en la base de datos.
     */
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
            System.err.println(" Error crítico al consultar la cuota vigente en CuotaDAO: " + e.getMessage());
        }

        return cuota;
    }

    /**
     * Establece y registra un nuevo monto oficial para la cuota de mantenimiento del condominio.
     * 
     * Para preservar la integridad de los reportes financieros pasados, este método 
     * ejecuta un { INSERT} en el historial de la base de datos en lugar de 
     * un { UPDATE}. El nuevo valor entrará en vigencia inmediatamente para 
     * cualquier transacción que ocurra posterior a esta ejecución.
     * 
     * @param nuevoMonto El nuevo valor monetario que se cobrará como cuota de mantenimiento.
     * @return true si el nuevo monto se registró exitosamente en la bitácora, false en caso de error SQL.
     */
    public boolean actualizarMontoMantenimiento(double nuevoMonto) {
        
        String sql = "INSERT INTO historial_cuotas (monto_cuota) VALUES (?)";

        try (Connection conn = Conexion.conectar();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setDouble(1, nuevoMonto);
            int filas = pst.executeUpdate();

            return filas > 0;

        } catch (SQLException e) {
            System.err.println(" Error de transacción al actualizar historial de cuotas en CuotaDAO: " + e.getMessage());
            return false;
        }
    } 
}