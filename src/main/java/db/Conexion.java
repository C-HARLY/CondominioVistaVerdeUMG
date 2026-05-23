package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    // URL BASE DE DATOS NEON
    private static final String URL =
        "jdbc:postgresql://ep-young-math-aqseeseq.c-8.us-east-1.aws.neon.tech/neondb?sslmode=require";

    // USUARIO
    private static final String USER = "neondb_owner";

    // PASSWORD
    private static final String PASS = "npg_Bz2ydLU7vowY";

    // CONEXIÓN GLOBAL REUTILIZABLE
    private static Connection conn;

    // MÉTODO DE CONEXIÓN
    public static Connection conectar() {

        try {

            // SOLO CREA UNA NUEVA SI NO EXISTE
            if (conn == null || conn.isClosed()) {

                conn = DriverManager.getConnection(URL, USER, PASS);

                System.out.println("Conectado a la base de datos Exitosamente");
            }

        } catch (SQLException e) {

            System.err.println("ERROR DE CONEXIÓN:");

            System.err.println(e.getMessage());
        }

        return conn;
    }

    // CERRAR CONEXIÓN MANUALMENTE (OPCIONAL)
    public static void cerrarConexion() {

        try {

            if (conn != null && !conn.isClosed()) {

                conn.close();

                System.out.println("Conexión cerrada correctamente");
            }

        } catch (SQLException e) {

            e.printStackTrace();
        }
    }
}