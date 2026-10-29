package ar.edu.utn.frba.dds.logistica.domain.rutas;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

// Documento embebido dentro de Entrega (lista "fotos"): no tiene colección ni id propios.
@Getter
@Setter
@Schema(example = """
        {
          "url": "https://example.org/entrega.jpg",
          "fecha": "2026-10-08"
        }
        """)
public class FotoEntrega {

    @NotBlank(message = "url es obligatoria")
    private String url;

    private LocalDate fecha;

    public FotoEntrega() {}

    public FotoEntrega(String url, LocalDate fecha) {
        this.url = url;
        this.fecha = fecha;
    }
}
