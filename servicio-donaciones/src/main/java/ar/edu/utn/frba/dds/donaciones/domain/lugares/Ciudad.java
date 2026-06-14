package ar.edu.utn.frba.dds.donaciones.domain.lugares;
public class Ciudad {
    private String nombre;
    private Provincia provincia;

    public Ciudad(
            String nombre,
            Provincia provincia) {
        this.nombre = nombre;
        this.provincia = provincia;
    }
}