package ar.edu.utn.frba.dds.logistica.domain.rutas;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Value object: se guarda embebido dentro del documento de Entrega y de Parada (sin colección propia).
@Getter
@NoArgsConstructor
@Setter
public class Direccion {

    private String calle;
    private String numero;
    private Ciudad ciudad;

    // Coordenadas de la dirección (opcionales): las manda Donaciones cuando la entidad tiene la sede ubicada en el mapa.
    private Double latitud;
    private Double longitud;

    public boolean tieneCoordenadas() {
        return latitud != null && longitud != null;
    }

    public Direccion(
            String calle,
            String numero,
            Ciudad ciudad) {
        this.calle = calle;
        this.numero = numero;
        this.ciudad = ciudad;
    }
}
