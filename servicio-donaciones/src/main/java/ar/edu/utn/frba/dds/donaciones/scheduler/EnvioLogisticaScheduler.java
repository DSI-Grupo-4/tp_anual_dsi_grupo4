package ar.edu.utn.frba.dds.donaciones.scheduler;

import ar.edu.utn.frba.dds.donaciones.dto.DonacionPendienteDTO;
import ar.edu.utn.frba.dds.donaciones.integracion.LogisticaBroker;
import ar.edu.utn.frba.dds.donaciones.service.DonacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Broker de Logística: en horario de baja carga (después de
 * MatchmakingScheduler, antes del scheduler de planificación de rutas de
 * Logística), empuja en lotes de a lo sumo 100 las donaciones en estado
 * "Asignación realizada" hacia el proveedor logístico activo, vía el broker.
 *
 * A diferencia de una integración que enviara una donación a la vez al
 * momento de confirmarse la asignación, este scheduler reutiliza la
 * paginación que ya expone DonacionService.obtenerPendientes(page, size)
 * para respetar el límite de lote real, en vez de depender de que cada
 * envío individual sea casualmente ≤ 100.
 */
@Component
public class EnvioLogisticaScheduler {

    private static final Logger logger = LoggerFactory.getLogger(EnvioLogisticaScheduler.class);
    private static final int TAMANIO_LOTE = 100;

    private final DonacionService donacionService;
    private final LogisticaBroker logisticaBroker;

    public EnvioLogisticaScheduler(DonacionService donacionService, LogisticaBroker logisticaBroker) {
        this.donacionService = donacionService;
        this.logisticaBroker = logisticaBroker;
    }

    @Scheduled(cron = "${logistica.envio.cron:0 30 2 * * *}")
    public void enviarPendientesALogistica() {
        int page = 0;
        List<DonacionPendienteDTO> lote;

        while (!(lote = donacionService.obtenerPendientes(page, TAMANIO_LOTE)).isEmpty()) {
            try {
                logisticaBroker.enviarLote(lote);
                logger.info("Lote de {} donación(es) enviado a Logística (página {}).", lote.size(), page);
            } catch (RuntimeException error) {
                logger.warn("No se pudo enviar el lote (página {}) a ningún proveedor logístico: {}",
                        page, error.getMessage());
                return; // no seguimos pidiendo más páginas si el proveedor está caído
            }

            if (lote.size() < TAMANIO_LOTE) {
                return; // última página
            }
            page++;
        }
    }
}
