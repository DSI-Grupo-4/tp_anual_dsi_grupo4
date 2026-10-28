package ar.edu.utn.frba.dds.donaciones.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
@Getter @Setter
@Schema(example = """
{
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
    }
  ]
}
""")
public class ActualizarDonacionDTO {
    @NotEmpty private List<@NotNull @Valid ItemDonadoDTO> items;
}
