package ar.edu.utn.frba.dds.donaciones.domain.personas;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class EntidadBeneficiaria {
    private Long id;
    private PersonaJuridica entidad;
    private String descripcion;
    private List<Necesidad> necesidades;
    private List<Donacion> donacionesRecibidas;

    public EntidadBeneficiaria(Long id, PersonaJuridica entidad, String descripcion) {
        this.id = id;
        this.entidad = entidad;
        this.descripcion = descripcion;
        this.necesidades = new ArrayList<>();
        this.donacionesRecibidas = new ArrayList<>();
    }

    public void agregarNecesidad(Necesidad necesidad) {
        necesidad.setEntidadBeneficiaria(this);
        this.necesidades.add(necesidad);
    }

    public void registrarAyuda(Donacion donacion) {
        donacionesRecibidas.add(donacion);
    }

    public Integer cantidadAyudasRecibidas() {
        return donacionesRecibidas.size();
    }

    public List<Necesidad> necesidadesPendientes() {
        return necesidades.stream()
                .filter(n -> !n.satisfecha())
                .toList();
    }
}
