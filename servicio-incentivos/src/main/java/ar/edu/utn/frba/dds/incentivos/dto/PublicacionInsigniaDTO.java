package ar.edu.utn.frba.dds.incentivos.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PublicacionInsigniaDTO {
    private Long donanteId;
    private String donanteNombre;
    private String insigniaNombre;
    private String imagenUrl;
    // Consultor usa un RestTemplate crudo (new RestTemplate()), sin el
    // ObjectMapper de Spring Boot que registra JavaTimeModule con fechas en
    // ISO-8601: sin este formato explícito, Jackson serializa LocalDate como
    // array [año, mes, día], que el nodo de validación de n8n rechaza (400).
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaObtencion;
}
