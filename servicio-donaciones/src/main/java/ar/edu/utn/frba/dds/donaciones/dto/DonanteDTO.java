package ar.edu.utn.frba.dds.donaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ar.edu.utn.frba.dds.donaciones.domain.personas.Genero;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoOrganizacion;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(example = """
{
  "tipo": "HUMANA",
  "nombre": "Ana",
  "apellido": "Perez",
  "edad": 30,
  "documento": "30123456",
  "genero": "FEMENINO",
  "mediosContacto": [
    {"tipo": "EMAIL", "valor": "ana@example.org", "esPreferido": true},
    {"tipo": "WHATSAPP", "valor": "+5493515551234", "esPreferido": false}
  ]
}
""")
public class DonanteDTO {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
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
    @jakarta.validation.Valid
    private java.util.List<@jakarta.validation.constraints.NotNull MedioContactoDTO> mediosContacto;
}
