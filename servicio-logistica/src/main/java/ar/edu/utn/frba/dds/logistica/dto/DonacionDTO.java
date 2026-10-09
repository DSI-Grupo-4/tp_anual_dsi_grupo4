package ar.edu.utn.frba.dds.logistica.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Direccion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(example = """
{
  "idDonacion": 1,
  "entidadBeneficiariaAsociadaID": 1,
  "direccionDestino": {
    "calle": "Av. San Martin",
    "numero": "1250",
    "ciudad": {
      "nombre": "Cordoba",
      "provincia": {
        "nombre": "Cordoba"
      }
    }
  },
  "pesoKG": 15,
  "volumenM3": 2,
  "alturaM": 1
}
""")
public class DonacionDTO {
    private Integer idDonacion;
    private Integer entidadBeneficiariaAsociadaID;
    private Direccion direccionDestino;
    private Integer pesoKG;
    private Integer volumenM3;
    private Integer alturaM;
}
