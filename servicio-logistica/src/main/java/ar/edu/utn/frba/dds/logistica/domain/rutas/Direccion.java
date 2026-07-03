package ar.edu.utn.frba.dds.logistica.domain.rutas;

public class Direccion {
    private String calle;
    private String numero;
    private Ciudad ciudad;

    public Direccion(
            String calle,
            String numero,
            Ciudad ciudad) {

        this.calle = calle;
        this.numero = numero;
        this.ciudad = ciudad;
    }
}
