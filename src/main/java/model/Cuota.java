/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author carlo
 */
public class Cuota {
    private int id;
  private int montoActual;

    public Cuota() {
    }

   public Cuota(int id, int montoActual) {
        this.id = id;
        this.montoActual = montoActual;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMontoActual() {
        return montoActual;
    }

    public void setMontoActual(int montoActual) {
        this.montoActual = montoActual;
    }
    
    
}
