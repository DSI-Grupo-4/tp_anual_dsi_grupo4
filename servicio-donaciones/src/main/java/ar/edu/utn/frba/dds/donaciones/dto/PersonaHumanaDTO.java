package ar.edu.utn.frba.dds.donaciones.dto;

import ar.edu.utn.frba.dds.donaciones.domain.personas.Genero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonaHumanaDTO {
    private String nombre;
    private String apellido;
    private Integer edad;
    private String documento;
    private Genero genero;

    // Medios de contacto: el enunciado exige al menos email de forma obligatoria
    // (Entrega 1 - Servicio de Donaciones - Donantes) y son necesarios para poder
    // resolver a quién y por dónde notificar (Entrega 2).
    private String email;
    private String telefono;
    private String whatsapp;
}