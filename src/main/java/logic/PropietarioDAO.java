package logic;

import db.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Propietario;

/**
 * Data Access Object (DAO) principal para la gestión del ciclo de vida de los residentes.
 * Esta clase orquesta operaciones que afectan 
 * simultáneamente a múltiples tablas ({@code propietarios} y {@code casas}). 
 * Para garantizar la integridad referencial y cumplir con las propiedades ACID, 
 * los métodos de escritura implementan control manual de transacciones (Commit/Rollback).
 * 
 */
public class PropietarioDAO {

    /**
     * Registra a un nuevo residente y actualiza el estado de la vivienda asignada.
     * Se ejecuta bajo una transacción atómica: valida que la casa esté disponible,
     * inserta el perfil del propietario y bloquea la casa cambiándola a 'Ocupada'.
     * Si cualquiera de estos pasos falla, se aplica un Rollback total.
     *
     * @param prop Instancia del modelo con los datos del nuevo propietario.
     * @return true si la transacción completa (Insert + Update) se consolida con éxito; false si ocurre una falla o la casa ya estaba ocupada.
     */
    public boolean registrar(Propietario prop) {

        String sqlValidacion = """
            SELECT estado
            FROM casas
            WHERE numero_casa = ?
            """;

        String sqlInsert = """
            INSERT INTO propietarios (nombre, telefono, correo, id_casa)
            VALUES (?, ?, ?, (SELECT id FROM casas WHERE numero_casa = ?))
            """;

        String sqlUpdateCasa = """
            UPDATE casas
            SET estado = 'Ocupada'
            WHERE numero_casa = ?
            """;

        try (Connection conn = Conexion.conectar()) {

            // Se deshabilita el auto-commit para agrupar las sentencias en una única transacción
            conn.setAutoCommit(false);

            // 1. Validar la disponibilidad  de la casa
            try (PreparedStatement psValidar = conn.prepareStatement(sqlValidacion)) {
                psValidar.setInt(1, prop.getNumeroCasa());
                ResultSet rs = psValidar.executeQuery();

                if (rs.next()) {
                    String estadoCasa = rs.getString("estado");
                    if ("Ocupada".equalsIgnoreCase(estadoCasa)) {
                        System.err.println(" Validación (PropietarioDAO): La casa ya tiene un propietario activo.");
                        conn.rollback();
                        return false;
                    }
                }
            }

            // 2. Ejecutar inserción y actualización en bloque
            try (PreparedStatement psProp = conn.prepareStatement(sqlInsert);
                 PreparedStatement psCasa = conn.prepareStatement(sqlUpdateCasa)) {

                psProp.setString(1, prop.getNombre());
                psProp.setString(2, prop.getTelefono());
                psProp.setString(3, prop.getCorreo());
                psProp.setInt(4, prop.getNumeroCasa());
                psProp.executeUpdate();

                psCasa.setInt(1, prop.getNumeroCasa());
                psCasa.executeUpdate();

                // Consolidar los cambios físicamente en el disco de la base de datos
                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback(); // Previene la corrupción de datos si el proceso falla a medias
                System.err.println(" Error Transaccional en registro (PropietarioDAO): " + e.getMessage());
                return false;
            }

        } catch (SQLException e) {
            System.err.println(" Error de red/conexión en PropietarioDAO: " + e.getMessage());
            return false;
        }
    }

    /**
     * Recupera el directorio completo de los residentes que actualmente habitan el condominio.
     * Se realiza un filtrado excluyendo cualquier casa con estado 'Disponible'.
     *
     */
    public List<Propietario> obtenerTodos() {
        List<Propietario> lista = new ArrayList<>();

        String sql = """
            SELECT 
                c.numero_casa,
                p.nombre,
                p.telefono,
                p.correo
            FROM casas c
            JOIN propietarios p
                ON c.id = p.id_casa
            WHERE c.estado = 'Ocupada'
            ORDER BY c.numero_casa ASC
            """;

        try (Connection conn = Conexion.conectar()) {
            if (conn != null) {
                try (PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        String nombre = rs.getString("nombre");
                        if (nombre == null) nombre = "";

                        int numCasa = rs.getInt("numero_casa");
                        String telefono = rs.getString("telefono");
                        String correo = rs.getString("correo");

                        Propietario prop = new Propietario(nombre.trim(), numCasa, telefono, correo);
                        lista.add(prop);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println(" Error al cargar el directorio de propietarios: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Actualiza los datos de contacto de un residente existente.
     * Resuelve de forma dinámica la llave foránea cruzando el identificador lógico 
     * (número de casa) hacia la entidad física de los propietarios.
     *
     * @param numCasa El número de la vivienda del propietario a modificar.
     * @param nuevoTelefono El número telefónico actualizado.
     * @param nuevoCorreo La dirección de correo electrónico actualizada.
     * @return true si la actualización fue exitosa, false en caso de error.
     */
    public boolean actualizarContacto(int numCasa, String nuevoTelefono, String nuevoCorreo) {
        String sql = """
            UPDATE propietarios 
            SET telefono = ?, correo = ? 
            WHERE id_casa = (SELECT id FROM casas WHERE numero_casa = ?)
            """;

        try (Connection conn = Conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nuevoTelefono);
            ps.setString(2, nuevoCorreo);
            ps.setInt(3, numCasa);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println(" Error al actualizar contacto (PropietarioDAO): " + e.getMessage());
            return false;
        }
    }

    /**
     * Revoca los derechos de un residente y libera la propiedad asociada.
     * * [ACTUALIZACIÓN ]: En lugar de hacer un DELETE (que rompería la  
     *    los pagos históricos), se hace una desvinculación (UPDATE id_casa = NULL).
     * Así la casa queda libre, pero el dueño y sus recibos permanecen en contabilidad.
     *
     * @param numCasa El identificador lógico de la vivienda que quedará vacante.
     * @return true si la desvinculación y actualización en cascada fueron exitosas; false si hubo un fallo.
     */
    public boolean removerPropietario(int numCasa) {
        
        //  En lugar de DELETE, usamos UPDATE para poner la casa en NULL.
        String sqlDesvincularPropietario = """
            UPDATE propietarios
            SET id_casa = NULL
            WHERE id_casa = (SELECT id FROM casas WHERE numero_casa = ?)
            """;

        String sqlActualizarCasa = """
            UPDATE casas
            SET estado = 'Disponible'
            WHERE numero_casa = ?
            """;

        try (Connection conn = Conexion.conectar()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psDesvincular = conn.prepareStatement(sqlDesvincularPropietario);
                 PreparedStatement psCasa = conn.prepareStatement(sqlActualizarCasa)) {

                // 1. Le quitamos la casa al dueño (pero su nombre e historial quedan a salvo)
                psDesvincular.setInt(1, numCasa);
                psDesvincular.executeUpdate();

                // 2. cambiamos estado a "Disponible" a la casa
                psCasa.setInt(1, numCasa);
                psCasa.executeUpdate();

                // Consolidamos ambas transacciones
                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.err.println(" Error Transaccional al remover propietario: " + e.getMessage());
                return false;
            }

        } catch (SQLException e) {
            System.err.println(" Error de conexión en removerPropietario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Consulta el listado de casas disponibles.
     * @return Lista de números de las casas desocupadas.
     */
    public List<Integer> obtenerCasasDisponibles() {
        List<Integer> lista = new ArrayList<>();
        String sql = """
            SELECT numero_casa
            FROM casas
            WHERE estado = 'Disponible'
            ORDER BY numero_casa ASC
            """;

        try (Connection conn = Conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(rs.getInt("numero_casa"));
            }

        } catch (SQLException e) {
            System.err.println(" Error al obtener casas disponibles en PropietarioDAO: " + e.getMessage());
        }

        return lista;
    }
}