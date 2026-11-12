package ar.edu.utn.frba.dds.web.logistica;

import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.EntregaDeRutaApi;
import java.time.LocalDate;
import java.util.List;

/**
 * La parada de una ruta que le corresponde a una entidad, con sus entregas. Es lo único de la ruta que ve la entidad:
 * no incluye las otras paradas, ni sus direcciones, ni el chofer.
 *
 * @param numeroParada posición de la parada en la ruta
 * @param totalParadas cantidad de paradas de la ruta
 */
public record EnvioEntidad(
        Integer idRuta,
        Integer idParada,
        Integer numeroParada,
        int totalParadas,
        LocalDate fecha,
        String estadoRuta,
        String patente,
        List<EntregaDeRutaApi> entregas) {
}
