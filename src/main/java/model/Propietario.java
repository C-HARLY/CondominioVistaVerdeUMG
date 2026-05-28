package model;
import java.time.LocalDate; 

public class Propietario extends Persona {
    
    // ¡Mira qué limpio queda! Solo atributos exclusivos del Propietario
    private int id;
    private int numeroCasa;
    private LocalDate fechaRegistro; 
    
    public Propietario(){ 
        super(); // Llama al constructor vacío de Persona
    }
    
    // Constructor completo original adaptado con POO
    public Propietario(int id, String nombre, int numeroCasa, String telefono, String correo ){
        super(nombre, telefono, correo); // Le pasa los datos a la clase Padre
        this.id = id;
        this.numeroCasa = numeroCasa;
    }
    
    // Constructor para Registrar original adaptado con POO
    public Propietario(String nombre, int numeroCasa, String telefono, String correo) {
        super(nombre, telefono, correo); // Le pasa los datos a la clase Padre
        this.numeroCasa = numeroCasa;
    }
     
    // GETTERS Y SETTERS EXCLUSIVOS DE PROPIETARIO
    // (Ya no necesitas poner los getNombre o getCorreo aquí, ¡los hereda automáticamente!)
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumeroCasa() {
        return numeroCasa;
    }

    public void setNumeroCasa(int numeroCasa) {
        this.numeroCasa = numeroCasa;
    }
    
    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}