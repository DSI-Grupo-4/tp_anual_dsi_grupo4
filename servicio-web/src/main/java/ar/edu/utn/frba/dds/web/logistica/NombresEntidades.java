package ar.edu.utn.frba.dds.web.logistica;

import ar.edu.utn.frba.dds.web.client.donaciones.DonacionesClient;
import ar.edu.utn.frba.dds.web.client.donaciones.EntidadApi;
import ar.edu.utn.frba.dds.web.error.BackendException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Nombres de las entidades por id, para mostrar en el seguimiento. Logística guarda solo el id de la entidad.
 * Si Donaciones no responde devuelve un mapa vacío: el seguimiento se ve igual, con «Entidad N».
 */
@Component
public class NombresEntidades {

    private static final Logger log = LoggerFactory.getLogger(NombresEntidades.class);

    private final DonacionesClient donaciones;

    public NombresEntidades(DonacionesClient donaciones) {
        this.donaciones = donaciones;
    }

    public Map<Integer, String> porId() {
        Map<Integer, String> nombres = new HashMap<>();
        try {
            for (EntidadApi entidad : donaciones.entidades()) {
                if (entidad.id() != null) {
                    nombres.put(entidad.id().intValue(), entidad.nombre());
                }
            }
        } catch (BackendException e) {
            log.warn("No se pudieron obtener los nombres de las entidades: {}", e.getMessage());
        }
        return nombres;
    }
}
