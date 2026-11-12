package ar.edu.utn.frba.dds.logistica.dto;

import ar.edu.utn.frba.dds.logistica.domain.rutas.FotoEntrega;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(example = """
        {
          "fotos": [
            { "url": "http://localhost:8083/api/fotos/6730a1f2c3d4e5f607182930" }
          ]
        }
        """)
public class ConfirmacionRecepcionDTO {

    @NotEmpty(message = "fotos es obligatoria: hace falta al menos una")
    @Size(max = 5, message = "se aceptan hasta 5 fotos")
    private List<@Valid FotoEntrega> fotos;
}
