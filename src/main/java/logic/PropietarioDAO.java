package logic;

import db.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Propietario;

public class PropietarioDAO {

    // ==========================================
    // 1. REGISTRAR NUEVO PROPIETARIO
    // ==========================================
    public boolean registrar(Propietario prop) {

        // VALIDAR ESTADO DE LA CASA
        String sqlValidacion = """
            SELECT estado
            FROM casas
            WHERE numero_casa = ?
            """;

        // INSERTAR PROPIETARIO
        String sqlInsert = """
            INSERT INTO propietarios
            (nombre, telefono, correo, id_casa)
            VALUES (?, ?, ?, 
            (SELECT id FROM casas WHERE numero_casa = ?))
            """;

        // ACTUALIZAR ESTADO CASA
        String sqlUpdateCasa = """
            UPDATE casas
            SET estado = 'Ocupada'
            WHERE numero_casa = ?
            """;

        try (Connection conn = Conexion.conectar()) {

            conn.setAutoCommit(false);

            // ======================================
            // VALIDAR CASA
            // ======================================

            try (PreparedStatement psValidar =
                         conn.prepareStatement(sqlValidacion)) {

                psValidar.setInt(1, prop.getNumeroCasa());

                ResultSet rs = psValidar.executeQuery();

                if (rs.next()) {

                    String estadoCasa =
                            rs.getString("estado");

                    if ("Ocupada".equalsIgnoreCase(estadoCasa)) {

                        System.out.println(
                            "La casa ya tiene un propietario activo."
                        );

                        conn.rollback();

                        return false;
                    }
                }
            }

            // ======================================
            // INSERTAR + ACTUALIZAR CASA
            // ======================================

            try (PreparedStatement psProp =
                         conn.prepareStatement(sqlInsert);

                 PreparedStatement psCasa =
                         conn.prepareStatement(sqlUpdateCasa)) {

                // DATOS PROPIETARIO

                psProp.setString(1, prop.getNombre());
                psProp.setString(2, prop.getTelefono());
                psProp.setString(3, prop.getCorreo());
                psProp.setInt(4, prop.getNumeroCasa());

                psProp.executeUpdate();

                // ACTUALIZAR CASA

                psCasa.setInt(1, prop.getNumeroCasa());

                psCasa.executeUpdate();

                conn.commit();

                return true;

            } catch (SQLException e) {

                conn.rollback();

                System.out.println(
                    "Error al registrar: "
                    + e.getMessage()
                );

                return false;
            }

        } catch (SQLException e) {

            System.out.println(
                "Error de conexión: "
                + e.getMessage()
            );

            return false;
        }
    }

    // ==========================================
    // 2. OBTENER TODOS LOS PROPIETARIOS ACTIVOS
    // ==========================================
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

                try (PreparedStatement ps =
                             conn.prepareStatement(sql);

                     ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {

                        String nombre =
                                rs.getString("nombre");

                        if (nombre == null) {
                            nombre = "";
                        }

                        int numCasa =
                                rs.getInt("numero_casa");

                        String telefono =
                                rs.getString("telefono");

                        String correo =
                                rs.getString("correo");

                        Propietario prop =
                                new Propietario(
                                        nombre.trim(),
                                        numCasa,
                                        telefono,
                                        correo
                                );

                        lista.add(prop);
                    }
                }
            }

        } catch (SQLException e) {

            System.out.println(
                "Error al obtener propietarios: "
                + e.getMessage()
            );
        }

        return lista;
    }

    // ==========================================
    // 3. ACTUALIZAR DATOS DE CONTACTO
    // ==========================================
    public boolean actualizarContacto(
            int numCasa,
            String nuevoTelefono,
            String nuevoCorreo
    ) {

        String sql = """
            UPDATE propietarios 
            SET telefono = ?, correo = ? 
            WHERE id_casa = (
                SELECT id 
                FROM casas 
                WHERE numero_casa = ?
            )
            """;

        try (Connection conn = Conexion.conectar();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setString(1, nuevoTelefono);
            ps.setString(2, nuevoCorreo);
            ps.setInt(3, numCasa);

            int filasAfectadas =
                    ps.executeUpdate();

            return filasAfectadas > 0;

        } catch (SQLException e) {

            System.out.println(
                "Error al actualizar propietario: "
                + e.getMessage()
            );

            return false;
        }
    }

    // ==========================================
    // 4. REMOVER PROPIETARIO
    // ==========================================
    public boolean removerPropietario(int numCasa) {

    String sqlEliminarPropietarios = """
        DELETE FROM propietarios
        WHERE id_casa = (
            SELECT id
            FROM casas
            WHERE numero_casa = ?
        )
        """;

    String sqlActualizarCasa = """
        UPDATE casas
        SET estado = 'Disponible'
        WHERE numero_casa = ?
        """;

    try (Connection conn = Conexion.conectar()) {

        conn.setAutoCommit(false);

        try (
            PreparedStatement psEliminar =
                    conn.prepareStatement(sqlEliminarPropietarios);

            PreparedStatement psCasa =
                    conn.prepareStatement(sqlActualizarCasa)
        ) {

            // =========================
            // ELIMINAR PROPIETARIOS
            // =========================

            psEliminar.setInt(1, numCasa);

            psEliminar.executeUpdate();

            // =========================
            // ACTUALIZAR CASA
            // =========================

            psCasa.setInt(1, numCasa);

            psCasa.executeUpdate();

            conn.commit();

            return true;

        } catch (SQLException e) {

            conn.rollback();

            System.out.println(
                    "Error al remover propietario: "
                    + e.getMessage()
            );

            return false;
        }

    } catch (SQLException e) {

        System.out.println(
                "Error de conexión: "
                + e.getMessage()
        );

        return false;
    }
}

    // ==========================================
    // 5. OBTENER CASAS DISPONIBLES
    // ==========================================
    public List<Integer> obtenerCasasDisponibles() {

        List<Integer> lista = new ArrayList<>();

        String sql = """
            SELECT numero_casa
            FROM casas
            WHERE estado = 'Disponible'
            ORDER BY numero_casa ASC
            """;

        try (Connection conn = Conexion.conectar();
             PreparedStatement ps =
                     conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                lista.add(
                    rs.getInt("numero_casa")
                );
            }

        } catch (SQLException e) {

            System.out.println(
                "Error al obtener casas disponibles: "
                + e.getMessage()
            );
        }

        return lista;
    }
}