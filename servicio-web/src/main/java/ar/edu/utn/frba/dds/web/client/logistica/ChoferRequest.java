package ar.edu.utn.frba.dds.web.client.logistica;

/** Cuerpo para crear o actualizar un chofer. */
public record ChoferRequest(String nombre, Integer dni, Boolean habilitado) {
}
