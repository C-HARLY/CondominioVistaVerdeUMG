/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package db;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 *
 * @author carlo
 */
public class Conexion {
    //URL BASE DE DATOS EN neon.tech 
    private static final String URL = "jdbc:postgresql://ep-young-math-aqseeseq.c-8.us-east-1.aws.neon.tech/neondb?sslmode=require";    //USUARIO
    private static final String USER = "neondb_owner";
    // PASSWORD
    private static final String PASS = "npg_Bz2ydLU7vowY";
    
    // Método estático corregido
    public static Connection conectar() {
        Connection conn = null; 
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("  Conectado a la base de datos Exitosamente");
        } catch (SQLException e) {
            System.err.println(" ERROR  DE CONEXIÓN:");
            System.err.println(e.getMessage());
        }
        return conn;
    }
}