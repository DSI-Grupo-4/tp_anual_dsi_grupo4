package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

public class EstadoInvalidoException extends RuntimeException {
    public EstadoInvalidoException(EstadoTrack desde, EstadoTrack hacia) {
        super("Transición inválida: " + desde + " → " + hacia);
    }
}
