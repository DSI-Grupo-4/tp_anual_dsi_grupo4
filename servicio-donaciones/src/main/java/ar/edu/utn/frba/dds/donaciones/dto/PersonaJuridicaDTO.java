package ar.edu.utn.frba.dds.donaciones.dto;

import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoOrganizacion;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonaJuridicaDTO {
    private String razonSocial;
    private TipoOrganizacion tipo;
    private String rubro;
}
