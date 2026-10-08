package ar.edu.utn.frba.dds.donaciones.integracion;

import ar.edu.utn.frba.dds.donaciones.config.RabbitNotificacionesConfig;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.domain.personas.MedioContacto;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoContacto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementación real de PublicadorEventosPort (RF-3): publica a la cola de
 * Notificaciones vía RabbitMQ. Reemplaza a NoOpPublicadorEventos.
 *
 * Limitación conocida (ver decisiones.md): el dominio actual no tiene una
 * referencia Donacion -> Donante, así que solo se puede resolver
 * destinatario/contacto cuando el payload trae una EntidadBeneficiaria
 * asociada. Los eventos dirigidos a la persona donante (ej. "tu donación
 * fue asignada") no se pueden publicar todavía por este medio — quedan
 * logueados como advertencia en vez de fallar silenciosamente.
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
        if (!(payload instanceof Donacion donacion)) {
            logger.warn("No se pudo publicar el evento '{}': el payload no es una Donacion ({}).",
                    tipoEvento, payload == null ? "null" : payload.getClass());
            return;
        }

        EntidadBeneficiaria entidad = donacion.getEntidadBeneficiaria();
        if (entidad == null) {
            logger.info("Evento '{}' de la donación {} sin entidad beneficiaria asociada todavía"
                    + " — no hay a quién notificar por este camino (falta modelar Donacion -> Donante).",
                    tipoEvento, donacion.getId());
            return;
        }

        Optional<MedioContacto> medio = entidad.getEntidad().medioPreferido();
        if (medio.isEmpty()) {
            logger.warn("La entidad beneficiaria {} no tiene medio de contacto preferido"
                    + " — no se pudo notificar el evento '{}'.", entidad.getId(), tipoEvento);
            return;
        }

        NotificacionRequest mensaje = new NotificacionRequest(
                mensajeParaEvento(tipoEvento, donacion),
                mapearMedio(medio.get().getTipo()),
                medio.get().getValor(),
                "donaciones");

        rabbitTemplate.convertAndSend(
                RabbitNotificacionesConfig.EXCHANGE,
                RabbitNotificacionesConfig.ROUTING_KEY,
                mensaje);
    }

    private String mensajeParaEvento(String tipoEvento, Donacion donacion) {
        if ("CAMBIO_ESTADO_DONACION".equals(tipoEvento)) {
            return "La donación #%d cambió de estado: %s".formatted(donacion.getId(), donacion.getEstadoActual());
        }
        return "Actualización de la donación #%d (%s)".formatted(donacion.getId(), tipoEvento);
    }

    private String mapearMedio(TipoContacto tipo) {
        return switch (tipo) {
            case EMAIL -> "EMAIL";
            case TELEFONO -> "SMS";
            case WHATSAPP -> "WHATSAPP";
        };
    }

    private record NotificacionRequest(String mensaje, String medio, String contacto, String servicioOrigen) {
    }
}
