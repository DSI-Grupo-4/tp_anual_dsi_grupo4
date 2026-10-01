package ar.edu.utn.frba.dds.donaciones.integration;

import ar.edu.utn.frba.dds.donaciones.client.LogisticaClient;
import ar.edu.utn.frba.dds.donaciones.dto.DonacionPendienteDTO;
import ar.edu.utn.frba.dds.donaciones.dto.EventoLogisticoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LogisticaBroker {

    private static final Logger logger = LoggerFactory.getLogger(LogisticaBroker.class);

    private final List<LogisticaClient> proveedores;
    private final EstrategiaSeleccion estrategia;
    private final AtomicInteger siguiente = new AtomicInteger();
    private final ConcurrentHashMap<UUID, LogisticaClient> origenPorEvento = new ConcurrentHashMap<>();
    private volatile LogisticaClient proveedorActivo;

    public LogisticaBroker(
            @Value("${logistica.proveedores.propia-url:http://localhost:8083}") String propiaUrl,
            @Value("${logistica.proveedores.alternativa-url:}") String alternativaUrl,
            @Value("${logistica.broker.estrategia:ROUND_ROBIN}") String estrategia) {
        this.proveedores = new ArrayList<>();
        this.proveedores.add(new LogisticaClient("PROPIA", propiaUrl));
        if (alternativaUrl != null && !alternativaUrl.isBlank()) {
            this.proveedores.add(new LogisticaClient("ALTERNATIVA", alternativaUrl));
        }
        this.estrategia = EstrategiaSeleccion.valueOf(estrategia.toUpperCase(Locale.ROOT));
        this.proveedorActivo = this.proveedores.get(0);
    }

    public void enviarLote(List<DonacionPendienteDTO> lote) {
        RuntimeException ultimoError = null;
        for (LogisticaClient proveedor : candidatosParaEnvio()) {
            try {
                proveedor.enviarLote(lote);
                proveedorActivo = proveedor;
                logger.info("Lote enviado mediante el proveedor logistico {}", proveedor.getNombre());
                return;
            } catch (RuntimeException error) {
                ultimoError = error;
                logger.warn("Fallo el proveedor logistico {}: {}", proveedor.getNombre(), error.getMessage());
            }
        }
        throw new IllegalStateException("Ningun proveedor logistico pudo recibir el lote", ultimoError);
    }

    public List<EventoLogisticoDTO> obtenerEventosNoPublicados() {
        List<EventoLogisticoDTO> eventos = new ArrayList<>();
        for (LogisticaClient proveedor : proveedores) {
            try {
                List<EventoLogisticoDTO> publicados = proveedor.obtenerEventosNoPublicados();
                publicados.forEach(evento -> origenPorEvento.put(evento.getIdEvento(), proveedor));
                eventos.addAll(publicados);
            } catch (RuntimeException error) {
                logger.warn("No se pudieron consultar eventos en {}: {}", proveedor.getNombre(), error.getMessage());
            }
        }
        return eventos;
    }

    public void marcarPublicado(UUID idEvento) {
        LogisticaClient origen = origenPorEvento.remove(idEvento);
        if (origen == null) {
            origen = proveedorActivo;
        }
        try {
            origen.marcarPublicado(idEvento);
        } catch (RuntimeException error) {
            logger.warn("No se pudo confirmar el evento {} en {}: {}",
                    idEvento, origen.getNombre(), error.getMessage());
        }
    }

    private List<LogisticaClient> candidatosParaEnvio() {
        if (estrategia == EstrategiaSeleccion.ROUND_ROBIN && proveedores.size() > 1) {
            int inicio = Math.floorMod(siguiente.getAndIncrement(), proveedores.size());
            List<LogisticaClient> ordenados = new ArrayList<>(proveedores.size());
            for (int i = 0; i < proveedores.size(); i++) {
                ordenados.add(proveedores.get((inicio + i) % proveedores.size()));
            }
            return ordenados;
        }
        return List.copyOf(proveedores);
    }

    enum EstrategiaSeleccion {
        FAILOVER,
        ROUND_ROBIN
    }
}
