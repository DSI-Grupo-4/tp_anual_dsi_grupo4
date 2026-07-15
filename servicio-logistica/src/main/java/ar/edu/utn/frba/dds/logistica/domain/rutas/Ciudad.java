package ar.edu.utn.frba.dds.logistica.domain.rutas;

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
