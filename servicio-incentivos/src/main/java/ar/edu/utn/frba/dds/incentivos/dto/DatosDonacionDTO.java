package ar.edu.utn.frba.dds.incentivos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import java.math.BigDecimal;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Schema(example = """
{
  "donacionId": 1,
  "fecha": "2026-10-08",
  "categoriaNombre": "ALIMENTOS",
  "cantidadBienes": 2.5,
  "donacionExitosa": true,
  "beneficiarioId": 1,
  "beneficiarioNombre": "Comedor Sonrisas",
  "donanteNombre": "Ana Perez",
  "donanteMedioContacto": "EMAIL",
  "donanteContacto": "ana.perez@example.com"
}
""")
public class DatosDonacionDTO {
    @Schema(description = "ID de la donación en Donaciones. Reenviar el mismo ID al confirmar la entrega; usar otro ID para una donación nueva.")
    @Positive
    private Long donacionId;
    @NotNull(message = "fecha es obligatoria")
    private LocalDate fecha;
    // Categoría del bien donado (Alimentos/Mobiliario/Vestimenta, tal como la
    // modela el Servicio de Donaciones) -- usada solo para la misión
    // "Completitud" (variedad de categorías donadas). No es el rango del
    // donante (Colaborador/Sostenedor/Transformador), eso lo calcula Incentivos.
    private String categoriaNombre;
    @NotNull(message = "cantidadBienes es obligatoria")
    @Positive(message = "cantidadBienes debe ser mayor a 0")
    private BigDecimal cantidadBienes;
    private boolean donacionExitosa;
    private Long beneficiarioId;
    private String beneficiarioNombre;
    private String donanteNombre;
    // Donaciones los manda siempre que el donante tenga un medio de contacto
    // preferido configurado (ver IncentivosClient.construirRequest) -- se
    // guardan en el Donante para que NotificacionesClient pueda usarlos
    // después. Quedan null solo si el donante no tiene ningún contacto.
    private String donanteMedioContacto;
    private String donanteContacto;
}
