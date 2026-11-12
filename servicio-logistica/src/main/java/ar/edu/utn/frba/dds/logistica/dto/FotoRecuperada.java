package ar.edu.utn.frba.dds.logistica.dto;

import java.io.InputStream;

/** Una foto leída de la base lista para servirse. El flujo lo cierra quien la sirve. */
public record FotoRecuperada(String contentType, long longitud, InputStream contenido) {
}
