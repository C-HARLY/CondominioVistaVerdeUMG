/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author carlo
 */
public class ReporteCasaDTO {
    private int numeroCasa;
    private String propietario;
    private String estadoMes;
    private double montoMes;
    private double totalAnual;

    public ReporteCasaDTO(int numeroCasa, String propietario, String estadoMes, double montoMes, double totalAnual) {
        this.numeroCasa = numeroCasa;
        this.propietario = propietario;
        this.estadoMes = estadoMes;
        this.montoMes = montoMes; 
        this.totalAnual = totalAnual;
    }

    // Getters
    public int getNumeroCasa() { return numeroCasa; }
    public String getPropietario() { return propietario; }
    public String getEstadoMes() { return estadoMes; }
    public double getMontoMes() { return montoMes; }
    public double getTotalAnual() { return totalAnual; }

    // Setters
    public void setNumeroCasa(int numeroCasa) { this.numeroCasa = numeroCasa; }
    public void setPropietario(String propietario) { this.propietario = propietario; }
    public void setEstadoMes(String estadoMes) { this.estadoMes = estadoMes; }
    public void setMontoMes(double montoMes) { this.montoMes = montoMes; }
    public void setTotalAnual(double totalAnual) { this.totalAnual = totalAnual; }
}