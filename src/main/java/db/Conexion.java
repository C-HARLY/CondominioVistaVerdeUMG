package db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Gestiona la conexión a la base de datos PostgreSQL en la nube (Neon.tech) 
 * utilizando el patrón de diseño Pool de Conexiones a través de HikariCP.
 * 
 * Se utiliza un pool en lugar de conexiones tradicionales JDBC para optimizar el 
 * rendimiento del sistema, reutilizando conexiones activas y evitando la latencia 
 * de abrir y cerrar túneles de red en cada consulta de la aplicación.
 *
 */
public class Conexion {

    // Credenciales de acceso a la base de datos en la nube
    private static final String URL = "jdbc:postgresql://ep-young-math-aqseeseq.c-8.us-east-1.aws.neon.tech/neondb?sslmode=require";
    private static final String USER = "neondb_owner";
    private static final String PASS = "npg_Bz2ydLU7vowY";
    
    /**
     * Instancia única y global del pool de conexiones.
     */
    private static HikariDataSource dataSource;

    /* * Bloque de inicialización estática.
     * Se ejecuta de manera segura por la Máquina Virtual de Java (JVM) una única 
     * vez al cargar la clase en memoria, garantizando que el pool esté configurado 
     * y listo antes de la primera petición del usuario.
     */
    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(URL);
            config.setUsername(USER);
            config.setPassword(PASS);
            
            // Optimizaciones de red para mitigar latencia 
            config.setMaximumPoolSize(5); 
            config.setMinimumIdle(1);      
            config.setConnectionTimeout(10000); 
            
            dataSource = new HikariDataSource(config);
            System.out.println(" HikariCP: Pool de conexiones inicializado correctamente.");
        } catch (Exception e) {
            System.err.println(" Fallo al inicializar el pool de conexiones.");
            e.printStackTrace();
        }
    }

    /**
     * Obtiene una conexión activa del pool lista para ejecutar transacciones SQL.
     * 
     * IMPORTANTE: La capa DAO que invoque este método asume la responsabilidad 
     * estricta de cerrar la conexión (ej. usando try-with-resources) para 
     * devolverla al pool. Si no se libera, el sistema sufrirá un "Connection Leak".
     * 
     *
     * @return Un objeto {@link Connection} establecido, o null si se agota el tiempo de espera.
     */
    public static Connection conectar() {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
        } catch (SQLException e) {
            System.err.println(" ERROR SQL: No se pudo obtener conexión del pool.");
            System.err.println(e.getMessage());
        }
        return conn;
    }
}