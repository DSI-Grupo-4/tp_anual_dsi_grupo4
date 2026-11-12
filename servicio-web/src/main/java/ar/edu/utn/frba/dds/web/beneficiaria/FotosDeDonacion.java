package ar.edu.utn.frba.dds.web.beneficiaria;

import ar.edu.utn.frba.dds.web.client.logistica.EntregaApi;
import ar.edu.utn.frba.dds.web.client.logistica.LogisticaClient;
import ar.edu.utn.frba.dds.web.error.BackendException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Las fotos que se cargaron al confirmar la recepción de una donación. Viven en Logística (en la entrega).
 * Si Logística no responde, devuelve una lista vacía: el detalle de la donación se ve igual, sin fotos.
 */
@Component
public class FotosDeDonacion {

    private static final Logger log = LoggerFactory.getLogger(FotosDeDonacion.class);

    private final LogisticaClient logistica;

    public FotosDeDonacion(LogisticaClient logistica) {
        this.logistica = logistica;
    }

    public List<EntregaApi.FotoApi> de(Long idDonacion) {
        try {
            return logistica.entregas().stream()
                    .filter(e -> e.idDonacionAsociada() != null && idDonacion.equals(e.idDonacionAsociada().longValue()))
                    .flatMap(e -> e.fotos().stream())
                    .toList();
        } catch (BackendException e) {
            log.warn("No se pudieron obtener las fotos de la donación {}: {}", idDonacion, e.getMessage());
            return List.of();
        }
    }
}
