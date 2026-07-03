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

    // Medios de contacto de la persona jurídica (donante empresa/ONG o entidad
    // beneficiaria). Necesarios para poder notificar (Entrega 2).
    private String email;
    private String telefono;
}
