package model;

import java.time.LocalDateTime;

public class Cuota {
    private int id;
    private double montoActual;
    private LocalDateTime fechaCambio;

    public Cuota() {}

    public Cuota(int id, double montoActual) {
        this.id = id;
        this.montoActual = montoActual;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getMontoActual() { return montoActual; }
    public void setMontoActual(double montoActual) { this.montoActual = montoActual; }

    public LocalDateTime getFechaCambio() { return fechaCambio; }
    public void setFechaCambio(LocalDateTime fechaCambio) { this.fechaCambio = fechaCambio; }
}