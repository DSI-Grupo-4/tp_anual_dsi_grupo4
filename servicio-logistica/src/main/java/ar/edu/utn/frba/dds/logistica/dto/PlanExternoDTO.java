package ar.edu.utn.frba.dds.logistica.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Lo que un componente externo de planificación de rutas manda a
 * POST /api/planificador/callback: a qué camión le tocó qué conjunto de
 * entregas, agrupadas en paradas. Los ids de entrega y de camión/chofer
 * deben existir ya en este servicio -- el callback no crea entidades
 * nuevas, solo registra la asignación.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(example = """
{
  "rutas": [
    {
      "idCamion": 1,
      "idChofer": 1,
      "paradas": [
        { "entregasIds": [1, 2] }
      ]
    }
  ]
}
""")
public class PlanExternoDTO {
    @NotEmpty(message = "rutas no puede estar vacío")
    private List<@Valid RutaPlanificadaDTO> rutas;
}
