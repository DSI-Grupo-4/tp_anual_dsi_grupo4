package ar.edu.utn.frba.dds.donaciones.domain.personas;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PersonaJuridica extends Persona {
    private String razonSocial;
    private TipoOrg tipo;
    private String rubro;
    private List<PersonaHumana> representante = new ArrayList<>();

    public PersonaJuridica(String razonSocial, TipoOrg tipo, String rubro) {
        super();
        this.razonSocial = razonSocial;
        this.tipo = tipo;
        this.rubro = rubro;
    }
}
