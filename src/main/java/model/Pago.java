package model;

/**
 * Modelo que representa la transacción de un pago de mantenimiento.
 * @author carlo
 */
public class Pago {
    private int id;
    private int numeroCasa;
    private int idPropietario; 
    private String mes;
    private int year;
    private double monto;
    
    // Constructor vacío (Muy útil para cuando traes datos de la BD paso a paso)
    public Pago(){
        
    }
    
    // Constructor actualizado con el idPropietario
    public Pago(int id, int numeroCasa, int idPropietario, String mes, int year, double monto){
        this.id = id;
        this.numeroCasa = numeroCasa;
        this.idPropietario = idPropietario;
        this.mes = mes;
        this.year = year;
        this.monto = monto;
    }

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

    // --- NUEVOS GETTER Y SETTER ---
    public int getIdPropietario() {
        return idPropietario;
    }

    public void setIdPropietario(int idPropietario) {
        this.idPropietario = idPropietario;
    }

    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }
}