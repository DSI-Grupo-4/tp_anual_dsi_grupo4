package ar.edu.utn.frba.dds.donaciones.scheduler;

import ar.edu.utn.frba.dds.donaciones.client.LogisticaClient;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import ar.edu.utn.frba.dds.donaciones.dto.CambioEstadoDTO;
import ar.edu.utn.frba.dds.donaciones.dto.EventoLogisticoDTO;
import ar.edu.utn.frba.dds.donaciones.service.DonacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Donaciones consulta los eventos de servicio-logistica (pull), Logística
// nunca la invoca a ella. Cada evento se traduce a la transición de estado
// correspondiente en la Donacion asociada, siguiendo exactamente las
// transiciones válidas de Donacion.cambiarEstado.
@Component
public class EventosLogisticaScheduler {

    private static final Logger logger = LoggerFactory.getLogger(EventosLogisticaScheduler.class);

    private final LogisticaClient logisticaClient;
    private final DonacionService donacionService;

    public EventosLogisticaScheduler(LogisticaClient logisticaClient, DonacionService donacionService) {
        this.logisticaClient = logisticaClient;
        this.donacionService = donacionService;
    }

    @Scheduled(fixedDelayString = "${logistica.eventos.poll-delay-ms:60000}")
    public void consumirEventos() {
        for (EventoLogisticoDTO evento : logisticaClient.obtenerEventosNoPublicados()) {
            try {
                aplicarEvento(evento);
            } catch (Exception e) {
                logger.warn("No se pudo aplicar el evento {} ({}) a la donación asociada: {}",
                        evento.getIdEvento(), evento.getTipoEvento(), e.getMessage());
            }
            logisticaClient.marcarPublicado(evento.getIdEvento());
        }
    }

    private void aplicarEvento(EventoLogisticoDTO evento) {
        if (evento.getEntregaAsociada() == null || evento.getEntregaAsociada().getIdDonacionAsociada() == null) {
            return;
        }

        CambioEstadoDTO cambio = mapearACambioEstado(evento);
        if (cambio == null) {
            return; // evento sin transición asociada del lado de Donaciones (ej. RUTA_INICIADA a nivel ruta sin cambio de estado propio)
        }

        Long idDonacion = evento.getEntregaAsociada().getIdDonacionAsociada().longValue();
        donacionService.cambiarEstado(idDonacion, cambio);
    }

    // Corresponde 1 a 1 con TRANSICIONES_VALIDAS en Donacion.java
    private CambioEstadoDTO mapearACambioEstado(EventoLogisticoDTO evento) {
        EstadoTrack nuevoEstado = switch (evento.getTipoEvento()) {
            case "RUTA_PLANIFICADA" -> EstadoTrack.LISTA_PARA_ENTREGAR;
            case "RUTA_INICIADA" -> EstadoTrack.EN_TRASLADO;
            case "ENTREGA_CONFIRMADA" -> EstadoTrack.ENTREGADA;
            case "ENTREGA_NO_RECIBIDA", "ENTREGA_FALLIDA" -> EstadoTrack.ENTREGA_FALLIDA;
            default -> null;
        };

        if (nuevoEstado == null) {
            return null;
        }

        CambioEstadoDTO dto = new CambioEstadoDTO();
        dto.setNuevoEstado(nuevoEstado);
        if (nuevoEstado == EstadoTrack.ENTREGA_FALLIDA) {
            String justificacion = evento.getEntregaAsociada().getJustificacionFallo();
            // cambiarEstado exige justificación no vacía para ENTREGA_FALLIDA
            dto.setJustificacion(justificacion != null && !justificacion.isBlank()
                    ? justificacion
                    : "Entrega no recibida (informado por logística)");
        }
        return dto;
    }
}
