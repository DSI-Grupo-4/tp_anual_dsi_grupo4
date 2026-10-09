package ar.edu.utn.frba.dds.logistica.domain.rutas;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Schema(example = """
{
  "url": "https://example.org/entrega.jpg",
  "fecha": "2026-10-08"
}
""")
public class FotoEntrega {
    private String url;
    private LocalDate fecha;
}
