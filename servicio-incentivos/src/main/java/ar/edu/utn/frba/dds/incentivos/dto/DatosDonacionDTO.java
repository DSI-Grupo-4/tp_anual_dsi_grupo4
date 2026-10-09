package ar.edu.utn.frba.dds.incentivos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import java.math.BigDecimal;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Schema(example = """
{
  "fecha": "2026-10-08",
  "categoriaNombre": "ALIMENTOS",
  "cantidadBienes": 2.5,
  "donacionExitosa": true,
  "beneficiarioId": 1,
  "beneficiarioNombre": "Comedor Sonrisas",
  "donanteNombre": "Ana Perez"
}
""")
public class DatosDonacionDTO {
    private LocalDate fecha;
    // Categoría del bien donado (Alimentos/Mobiliario/Vestimenta, tal como la
    // modela el Servicio de Donaciones) -- usada solo para la misión
    // "Completitud" (variedad de categorías donadas). No es el rango del
    // donante (Colaborador/Sostenedor/Transformador), eso lo calcula Incentivos.
    private String categoriaNombre;
    private BigDecimal cantidadBienes;
    private boolean donacionExitosa;
    private Long beneficiarioId;
    private String beneficiarioNombre;
    private String donanteNombre;
    // Opcionales -- si quien llama (hoy nadie) los manda, se guardan en el
    // Donante para que NotificacionesClient pueda usarlos después.
    private String donanteMedioContacto;
    private String donanteContacto;
}
