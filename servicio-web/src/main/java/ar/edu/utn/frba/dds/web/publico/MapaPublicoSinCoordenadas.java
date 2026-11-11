package ar.edu.utn.frba.dds.web.publico;

import ar.edu.utn.frba.dds.web.mapa.MarcadoresMapa;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Implementación por defecto mientras Logística no exponga coordenadas (D-042):
 * el mapa se muestra vacío con un aviso. Reemplazarla por una que use LogisticaClient
 * cuando exista el endpoint, con el mismo contrato de {@link ar.edu.utn.frba.dds.web.mapa.Marcador}.
 */
@Service
public class MapaPublicoSinCoordenadas implements MapaPublico {

    @Override
    public MarcadoresMapa entregasRealizadas() {
        return new MarcadoresMapa(List.of(), MarcadoresMapa.Origen.SIN_COORDENADAS);
    }
}
