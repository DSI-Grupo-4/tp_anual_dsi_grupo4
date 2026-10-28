package ar.edu.utn.frba.dds.donaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Representa la "única carga": una persona donante (o la persona
 * administradora en su nombre) registra en un solo POST la descripción
 * general y todos los bienes que trae. El servicio se encarga de
 * segmentarla en N Donacion, una por subcategoría
 * (ver SolicitudDonacion.segmentar()).
 */
@Getter
@Setter
@Schema(example = """
{
  "donanteId": 1,
  "descripcion": "Muebles y alimentos para donar",
  "items": [
    {
      "descripcion": "Sillas de oficina usadas",
      "categoria": "MOBILIARIO",
      "subcategoria": "SILLA",
      "unidadMedida": "UNIDAD",
      "cantidad": 3,
      "condicion": "USADO",
      "foto": "https://example.org/sillas.jpg",
      "pesoKg": 15,
      "volumenM3": 2,
      "alturaM": 1
    },
    {
      "descripcion": "Arroz a granel",
      "categoria": "ALIMENTOS",
      "subcategoria": "ARROZ",
      "unidadMedida": "KILOGRAMO",
      "cantidad": 2.5,
      "fechaVencimiento": "2030-12-31",
      "pesoKg": 3,
      "volumenM3": 1,
      "alturaM": 1
    }
  ]
}
""")
public class CargaDonacionDTO {
    @NotNull(message = "donanteId es obligatorio")
    private Long donanteId;
    @NotBlank(message = "descripcion es obligatoria")
    private String descripcion;
    @NotEmpty(message = "la carga debe tener al menos un ítem")
    private List<@NotNull @Valid ItemDonadoDTO> items;
}
