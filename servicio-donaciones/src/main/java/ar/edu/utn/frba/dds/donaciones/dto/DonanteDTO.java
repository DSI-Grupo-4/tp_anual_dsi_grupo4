package ar.edu.utn.frba.dds.donaciones.dto;

import ar.edu.utn.frba.dds.donaciones.domain.personas.Genero;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoOrganizacion;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DonanteDTO {
    private Long id;
    private String tipo; // discriminador: "HUMANA" | "JURIDICA"
    // Campos de PersonaHumana
    private String nombre;
    private String apellido;
    private Integer edad;
    private Genero genero;
    private String documento;
    // Campos de PersonaJuridica
    private String razonSocial;
    private TipoOrganizacion tipoOrganizacion;
    private String rubro;
}
