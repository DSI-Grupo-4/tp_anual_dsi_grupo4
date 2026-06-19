package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DirectorioTimestamp extends Donacion {
    private Necesidad necesidad;
    private List<CambioEstado> timestamp = new ArrayList<>();
    private int cupal;
    private List<Item> itemsAsignados = new ArrayList<>();

    public void asignarAccesorio() {
    }
}
