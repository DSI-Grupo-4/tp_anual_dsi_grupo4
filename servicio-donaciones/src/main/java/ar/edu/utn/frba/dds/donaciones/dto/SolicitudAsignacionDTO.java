package ar.edu.utn.frba.dds.donaciones.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
/** Usa el mismo contrato y validaciones que la carga de un item. */
@Getter @Setter
@Schema(example = """
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
""")
public class SolicitudAsignacionDTO extends ItemDonadoDTO { }
