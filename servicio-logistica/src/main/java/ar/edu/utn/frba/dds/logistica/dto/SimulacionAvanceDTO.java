package ar.edu.utn.frba.dds.logistica.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Resultado de un paso del simulador: dónde quedó el camión y cuánto le falta hasta la próxima parada.
 *
 * @param numeroParadaDestino  parada hacia la que avanza el camión
 * @param distanciaRestanteKm  kilómetros que faltan hasta esa parada (0 si llegó)
 * @param llegoAlDestino       true si con este paso el camión llegó a la parada
 * @param destinoDeEjemplo     true si la parada no tenía coordenadas y se usó un punto de ejemplo
 */
@Getter
@AllArgsConstructor
public class SimulacionAvanceDTO {

    private UbicacionCamionDTO ubicacion;
    private Integer numeroParadaDestino;
    private double distanciaRestanteKm;
    private boolean llegoAlDestino;
    private boolean destinoDeEjemplo;
}
