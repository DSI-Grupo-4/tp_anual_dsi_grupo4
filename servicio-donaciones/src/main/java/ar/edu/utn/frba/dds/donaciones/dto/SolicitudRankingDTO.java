package ar.edu.utn.frba.dds.donaciones.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SolicitudRankingDTO {
    private String descripcionItem;
    private String subcategoria;
    private Integer cantidad;
    private String algoritmo;
}