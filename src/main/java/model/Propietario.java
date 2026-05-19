/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author carlo
 */
public class Propietario {
    private int id;
    private String nombre;
    private int numeroCasa;
    private String telefono;
    private String correo;
    
    public Propietario(){ //constructor vacio 
        
    }
    
    //constructor completo
    
    public Propietario(int id, String nombre, int numeroCasa, String telefono, String correo ){
        this.id = id;
        this.nombre = nombre;
        this.numeroCasa = numeroCasa;
        this.telefono = telefono;
        this.correo = correo;
        
    }
    // Construcctor para Registrar// 
    public Propietario(String nombre, int numeroCasa, String telefono, String correo) {
        this.nombre = nombre;
        this.numeroCasa = numeroCasa;
        this.telefono = telefono;
        this.correo = correo;
    }
     
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getNumeroCasa() {
        return numeroCasa;
    }

    public void setNumeroCasa(int numeroCasa) {
        this.numeroCasa = numeroCasa;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
    
    
    
            
}
