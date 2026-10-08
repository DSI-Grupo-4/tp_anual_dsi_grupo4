package ar.edu.utn.frba.dds.donaciones.integracion;

import ar.edu.utn.frba.dds.donaciones.config.RabbitNotificacionesConfig;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.personas.Donante;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.domain.personas.MedioContacto;
import ar.edu.utn.frba.dds.donaciones.domain.personas.Persona;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoContacto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementación real de PublicadorEventosPort: publica a la cola de
 * Notificaciones vía RabbitMQ. Reemplaza a NoOpPublicadorEventos.
 *
 * Ahora que Donacion referencia al Donante, se notifica a ambas partes
 * (entidad beneficiaria y donante) cuando cada una tiene un medio de
 * contacto resolvible — son mensajes independientes, cada uno con su propio
 * eventoId, no la misma notificación duplicada.
 */
@Component
public class RabbitPublicadorEventos implements PublicadorEventosPort {

    private static final Logger logger = LoggerFactory.getLogger(RabbitPublicadorEventos.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitPublicadorEventos(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicar(String tipoEvento, Object payload) {
        if (payload instanceof Donacion donacion) {
            boolean notificoAlguna = false;
            notificoAlguna |= notificarEntidad(tipoEvento, donacion);
            notificoAlguna |= notificarDonante(tipoEvento, donacion);
            if (!notificoAlguna) {
                logger.info("Evento '{}' de la donación {} sin ningún destinatario con contacto resolvible.",
                        tipoEvento, donacion.getId());
            }
            return;
        }

        if (payload instanceof Donante donante) {
            notificarDonanteDirecto(tipoEvento, donante);
            return;
        }

        logger.warn("No se pudo publicar el evento '{}': el payload no es una Donacion ni un Donante ({}).",
                tipoEvento, payload == null ? "null" : payload.getClass());
    }

    private void notificarDonanteDirecto(String tipoEvento, Donante donante) {
        if (donante.getPersona() == null) {
            return;
        }
        Optional<MedioContacto> medio = donante.getPersona().medioPreferido();
        if (medio.isEmpty()) {
            logger.warn("El donante {} no tiene medio de contacto preferido"
                    + " — no se pudo notificar el evento '{}'.", donante.getId(), tipoEvento);
            return;
        }
        NotificacionRequest mensaje = new NotificacionRequest(
                mensajeParaEventoDeDonante(tipoEvento),
                mapearMedio(medio.get().getTipo()),
                medio.get().getValor(),
                "donaciones",
                tipoEvento,
                UUID.randomUUID().toString());

        rabbitTemplate.convertAndSend(
                RabbitNotificacionesConfig.EXCHANGE,
                RabbitNotificacionesConfig.ROUTING_KEY,
                mensaje);
    }

    private String mensajeParaEventoDeDonante(String tipoEvento) {
        return switch (tipoEvento) {
            case "INACTIVIDAD_20_DIAS" ->
                    "¡Te extrañamos! Hace más de 20 días que no registrás actividad en DonaTrack. ¿Tenés algo para donar?";
            default -> "Actualización de tu cuenta en DonaTrack (%s)".formatted(tipoEvento);
        };
    }

    private boolean notificarEntidad(String tipoEvento, Donacion donacion) {
        EntidadBeneficiaria entidad = donacion.getEntidadBeneficiaria();
        if (entidad == null) {
            return false;
        }
        Optional<MedioContacto> medio = entidad.getEntidad().medioPreferido();
        if (medio.isEmpty()) {
            logger.warn("La entidad beneficiaria {} no tiene medio de contacto preferido"
                    + " — no se pudo notificar el evento '{}'.", entidad.getId(), tipoEvento);
            return false;
        }
        publicarMensaje(tipoEvento, donacion, medio.get());
        return true;
    }

    private boolean notificarDonante(String tipoEvento, Donacion donacion) {
        Donante donante = donacion.getDonante();
        if (donante == null || donante.getPersona() == null) {
            return false;
        }
        Persona persona = donante.getPersona();
        Optional<MedioContacto> medio = persona.medioPreferido();
        if (medio.isEmpty()) {
            logger.warn("El donante {} no tiene medio de contacto preferido"
                    + " — no se pudo notificar el evento '{}'.", donante.getId(), tipoEvento);
            return false;
        }
        publicarMensaje(tipoEvento, donacion, medio.get());
        return true;
    }

    private void publicarMensaje(String tipoEvento, Donacion donacion, MedioContacto medio) {
        NotificacionRequest mensaje = new NotificacionRequest(
                mensajeParaEvento(tipoEvento, donacion),
                mapearMedio(medio.getTipo()),
                medio.getValor(),
                "donaciones",
                tipoEvento,
                UUID.randomUUID().toString());

        rabbitTemplate.convertAndSend(
                RabbitNotificacionesConfig.EXCHANGE,
                RabbitNotificacionesConfig.ROUTING_KEY,
                mensaje);
    }

    // Casos de notificación con texto distinto por tipo de evento real de
    // Logística — el resto (enlace al mapa en vivo, comprobante con
    // camión/fecha/hora, aviso a personas administradoras) queda pendiente,
    // no hay modelo de "persona administradora" contactable en el dominio hoy.
    private String mensajeParaEvento(String tipoEvento, Donacion donacion) {
        return switch (tipoEvento) {
            case "CAMBIO_ESTADO_DONACION" ->
                    "La donación #%d cambió de estado: %s".formatted(donacion.getId(), donacion.getEstadoActual());
            case "RUTA_PLANIFICADA" ->
                    "La donación #%d ya tiene una ruta de entrega planificada.".formatted(donacion.getId());
            case "RUTA_INICIADA" ->
                    "El camión inició el traslado de la donación #%d.".formatted(donacion.getId());
            case "ENTREGA_CONFIRMADA" ->
                    "La donación #%d fue entregada con éxito.".formatted(donacion.getId());
            case "ENTREGA_NO_RECIBIDA", "ENTREGA_FALLIDA" ->
                    "La donación #%d no pudo entregarse: %s"
                            .formatted(donacion.getId(), justificacionDeLaUltimaEntregaFallida(donacion));
            default -> "Actualización de la donación #%d (%s)".formatted(donacion.getId(), tipoEvento);
        };
    }

    private String justificacionDeLaUltimaEntregaFallida(Donacion donacion) {
        return donacion.getHistorialEstados().stream()
                .filter(c -> c.getEstadoNuevo() == donacion.getEstadoActual())
                .reduce((primero, ultimo) -> ultimo)
                .map(c -> c.getJustificacion() != null ? c.getJustificacion() : "sin justificación informada")
                .orElse("sin justificación informada");
    }

    private String mapearMedio(TipoContacto tipo) {
        return switch (tipo) {
            case EMAIL -> "EMAIL";
            case TELEFONO -> "SMS";
            case WHATSAPP -> "WHATSAPP";
        };
    }

    private record NotificacionRequest(String mensaje, String medio, String contacto, String servicioOrigen,
                                        String tipoEvento, String eventoId) {
    }
}
