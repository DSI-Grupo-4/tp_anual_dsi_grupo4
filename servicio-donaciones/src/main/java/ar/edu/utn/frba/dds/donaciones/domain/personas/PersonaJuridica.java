package ar.edu.utn.frba.dds.donaciones.domain.personas;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PersonaJuridica extends Persona {
    private String razonSocial;
    private TipoOrganizacion tipo;
    private String rubro;

    // Lista siempre (nunca un único representante fijo): la cardinalidad
    // real depende del rol que cumpla esta persona jurídica (1 si actúa
    // como donante, N si actúa como entidad beneficiaria) y se valida en
    // Donante/EntidadBeneficiaria, que son quienes conocen ese rol.
    private List<PersonaHumana> representantes;

    public PersonaJuridica(
            String razonSocial,
            TipoOrganizacion tipo,
            String rubro,
            List<PersonaHumana> representantes) {

        super();

        this.razonSocial = razonSocial;
        this.tipo = tipo;
        this.rubro = rubro;
        this.representantes = representantes != null ? representantes : new ArrayList<>();
    }

    public void agregarRepresentante(PersonaHumana representante) {
        representantes.add(representante);
    }

    public String nombreRepresentante() {
        return representantes.isEmpty() ? null : representantes.get(0).nombreCompleto();
    }
}
