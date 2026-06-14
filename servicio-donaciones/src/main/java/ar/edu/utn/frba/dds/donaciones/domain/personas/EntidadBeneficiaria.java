package ar.edu.utn.frba.dds.donaciones.domain.personas;

import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EntidadBeneficiaria {
    private PersonaJuridica entidad;
    private String descripcion;
    private List<Necesidad> necesidades;
    private Integer ayudasRecibidas;

    public EntidadBeneficiaria(PersonaJuridica entidad, String descripcion) {
        this.entidad = entidad;
        this.descripcion = descripcion;
        this.necesidades = new ArrayList<>();
    }

    public void agregarNecesidad(Necesidad necesidad) {
        this.necesidades.add(necesidad);
    }

    public void registrarAyuda() {
        ayudasRecibidas++;
    }
}
