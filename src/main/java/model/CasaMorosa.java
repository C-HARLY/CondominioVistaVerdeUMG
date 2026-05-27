package model;

public class CasaMorosa {

    // Almacena el número de la casa morosa
    private String numeroCasa;

    // Almacena el nombre del propietario
    private String nombre;

    // Almacena el teléfono del propietario
    private String telefono;

    // Almacena el correo del propietario
    private String correo;

    public CasaMorosa() {
    }

    public String getNumeroCasa() {
        return numeroCasa;
    }

    public void setNumeroCasa(String numeroCasa) {
        this.numeroCasa = numeroCasa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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