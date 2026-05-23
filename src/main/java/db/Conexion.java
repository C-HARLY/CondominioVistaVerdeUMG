/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package db;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 *
 * @author carlo
 */
public class Conexion {
    // URL BASE DE DATOS EN neon.tech 
    private static final String URL = "jdbc:postgresql://ep-young-math-aqseeseq.c-8.us-east-1.aws.neon.tech/neondb?sslmode=require";
    // USUARIO
    private static final String USER = "neondb_owner";
    // PASSWORD
    private static final String PASS = "npg_Bz2ydLU7vowY";
    
    // El "estanque" de conexiones
    private static HikariDataSource dataSource;

    // Bloque estático: Se ejecuta automáticamente UNA SOLA VEZ cuando el programa arranca
    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(URL);
            config.setUsername(USER);
            config.setPassword(PASS);
            
            // Configuraciones de optimización para la nube
            config.setMaximumPoolSize(5); // Solo necesitamos 5 conexiones abiertas simultáneas
            config.setMinimumIdle(1);     // Siempre habrá al menos 1 conexión lista y esperando
            config.setConnectionTimeout(10000); // Si la red falla, se rinde a los 10 segundos
            
            dataSource = new HikariDataSource(config);
            System.out.println(" Pool de conexiones HikariCP inicializado correctamente.");
        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO AL INICIAR EL POOL DE CONEXIONES:");
            e.printStackTrace();
        }
    }

    // Método estático optimizado
    public static Connection conectar() {
        Connection conn = null; 
        try {
            conn = dataSource.getConnection();
        } catch (SQLException e) {
            System.err.println(" ERROR AL OBTENER CONEXIÓN :");
            System.err.println(e.getMessage());
        }
        return conn;
    }
}