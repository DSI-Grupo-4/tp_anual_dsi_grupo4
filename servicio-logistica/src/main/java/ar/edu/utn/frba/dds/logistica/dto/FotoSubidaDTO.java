package ar.edu.utn.frba.dds.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Foto recién guardada: su id y la URL con la que se arma la FotoEntrega de una confirmación de recepción. */
@Getter
@AllArgsConstructor
public class FotoSubidaDTO {

    private String id;
    private String url;
}
