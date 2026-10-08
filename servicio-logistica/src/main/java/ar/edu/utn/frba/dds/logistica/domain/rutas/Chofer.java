package ar.edu.utn.frba.dds.logistica.domain.rutas;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Chofer {
    private Integer idChofer;
    private String nombre;
    private Integer dni;
    private Boolean habilitado;

    public boolean estaHabilitado() {
        return Boolean.TRUE.equals(habilitado);
    }
}
