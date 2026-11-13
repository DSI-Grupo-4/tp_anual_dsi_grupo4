package ar.edu.utn.frba.dds.web.beneficiaria;

import ar.edu.utn.frba.dds.web.client.logistica.EntregaApi;
import ar.edu.utn.frba.dds.web.error.BackendException;
import ar.edu.utn.frba.dds.web.logistica.SeguimientoService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Las fotos que se cargaron al confirmar la recepción de una donación. Viven en Logística (en la entrega).
 * Se leen de los envíos de la propia entidad (GET /api/mis-envios), nunca del listado general de entregas.
 * Si Logística no responde, devuelve una lista vacía: el detalle de la donación se ve igual, sin fotos.
 */
@Component
public class FotosDeDonacion {

    private static final Logger log = LoggerFactory.getLogger(FotosDeDonacion.class);

    private final SeguimientoService seguimiento;

    public FotosDeDonacion(SeguimientoService seguimiento) {
        this.seguimiento = seguimiento;
    }

    public List<EntregaApi.FotoApi> de(Long entidadId, Long idDonacion) {
        try {
            return seguimiento.enviosDe(entidadId).stream()
                    .flatMap(envio -> envio.entregas().stream())
                    .filter(e -> e.idDonacionAsociada() != null && idDonacion.equals(e.idDonacionAsociada().longValue()))
                    .filter(e -> "ENTREGADA".equals(e.estadoEntrega()))
                    .flatMap(e -> e.fotos().stream())
                    .toList();
        } catch (BackendException e) {
            log.warn("No se pudieron obtener las fotos de la donación {}: {}", idDonacion, e.getMessage());
            return List.of();
        }
    }
}
