package ar.edu.utn.frba.dds.incentivos.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DatosDonacionDTO {
    private LocalDate fecha;
    // Categoría del bien donado (Alimentos/Mobiliario/Vestimenta, tal como la
    // modela el Servicio de Donaciones) -- usada solo para la misión
    // "Completitud" (variedad de categorías donadas). No es el rango del
    // donante (Colaborador/Sostenedor/Transformador), eso lo calcula Incentivos.
    private String categoriaNombre;
    private int cantidadBienes;
    private boolean donacionExitosa;
    private Long beneficiarioId;
    private String beneficiarioNombre;
    private String donanteNombre;
    // Opcionales -- si quien llama (hoy nadie) los manda, se guardan en el
    // Donante para que NotificacionesClient pueda usarlos después.
    private String donanteMedioContacto;
    private String donanteContacto;
}
