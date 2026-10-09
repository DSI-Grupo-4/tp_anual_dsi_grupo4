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

    private final ar.edu.utn.frba.dds.donaciones.config.BandejaNotificaciones bandeja;
    @org.springframework.beans.factory.annotation.Value("${notificaciones.administradores.emails:}")
    private String administradores = "";

    public RabbitPublicadorEventos(ar.edu.utn.frba.dds.donaciones.config.BandejaNotificaciones bandeja) {
        this.bandeja = bandeja;
    }

    @Override
    public void publicar(String tipoEvento, Object payload) { publicar(tipoEvento, payload, null); }

    @Override
    public void publicar(String tipoEvento, Object payload, ar.edu.utn.frba.dds.donaciones.dto.CambioEstadoDTO contexto) {
        if (payload instanceof Donacion donacion) {
            boolean notificoAlguna = false;
            notificoAlguna |= notificarEntidad(tipoEvento, donacion, contexto);
            notificoAlguna |= notificarDonante(tipoEvento, donacion, contexto);
            if (java.util.Set.of("ENTREGA_FALLIDA", "ENTREGA_NO_RECIBIDA").contains(tipoEvento)
                    || donacion.getEstadoActual() == ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack.VENCIDA) {
                for (String email : administradores.split(",")) {
                    if (!email.isBlank()) publicarMensaje(tipoEvento, donacion, new MedioContacto(TipoContacto.EMAIL, email.trim(), true), contexto);
                }
            }
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
        Optional<MedioContacto> medio = "BIENVENIDA_DONANTE".equals(tipoEvento)
                ? donante.getPersona().getMediosContacto().stream().filter(c -> c.getTipo() == TipoContacto.EMAIL).findFirst()
                : donante.getPersona().medioPreferido();
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
                "donante-" + donante.getId() + "-" + tipoEvento + "-" + donante.getUltimaActividad());

        bandeja.guardar(mensaje);
    }

    private String mensajeParaEventoDeDonante(String tipoEvento) {
        return switch (tipoEvento) {
            case "BIENVENIDA_DONANTE" -> "¡Bienvenido/a a DonaTrack! Ya podés ingresar a la plataforma con tu correo registrado.";
            case "INACTIVIDAD_20_DIAS" ->
                    "¡Te extrañamos! Hace más de 20 días que no registrás actividad en DonaTrack. ¿Tenés algo para donar?";
            default -> "Actualización de tu cuenta en DonaTrack (%s)".formatted(tipoEvento);
        };
    }

    private boolean notificarEntidad(String tipoEvento, Donacion donacion, ar.edu.utn.frba.dds.donaciones.dto.CambioEstadoDTO contexto) {
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
        publicarMensaje(tipoEvento, donacion, medio.get(), contexto);
        return true;
    }

    private boolean notificarDonante(String tipoEvento, Donacion donacion, ar.edu.utn.frba.dds.donaciones.dto.CambioEstadoDTO contexto) {
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
        publicarMensaje(tipoEvento, donacion, medio.get(), contexto);
        return true;
    }

    private void publicarMensaje(String tipoEvento, Donacion donacion, MedioContacto medio, ar.edu.utn.frba.dds.donaciones.dto.CambioEstadoDTO contexto) {
        NotificacionRequest mensaje = new NotificacionRequest(
                mensajeParaEvento(tipoEvento, donacion, contexto),
                mapearMedio(medio.getTipo()),
                medio.getValor(),
                "donaciones",
                tipoEvento,
                (contexto != null && contexto.getEventoId() != null ? contexto.getEventoId() :
                        "donacion-" + donacion.getId() + "-" + donacion.getHistorialEstados().size())
                        + "-" + tipoEvento + "-" + medio.getTipo() + "-" + medio.getValor());

        bandeja.guardar(mensaje);
    }

    private String mensajeParaEvento(String tipoEvento, Donacion donacion, ar.edu.utn.frba.dds.donaciones.dto.CambioEstadoDTO contexto) {
        return switch (tipoEvento) {
            case "CAMBIO_ESTADO_DONACION" ->
                    "La donación #%d cambió de estado: %s".formatted(donacion.getId(), donacion.getEstadoActual());
            case "DONACION_ASIGNADA" -> "La donación #%d fue asignada a %s.".formatted(donacion.getId(), donacion.getEntidadBeneficiaria().getEntidad().getRazonSocial());
            case "RUTA_PLANIFICADA" ->
                    "La donación #%d ya tiene una ruta de entrega planificada.".formatted(donacion.getId());
            case "RUTA_INICIADA" ->
                    "El camión inició el traslado de la donación #%d. Seguimiento: %s".formatted(donacion.getId(), contexto != null ? contexto.getSeguimientoUrl() : "Consultar Logística");
            case "ENTREGA_CONFIRMADA" ->
                    "Comprobante: donación #%d entregada el %s por el camión %s.".formatted(donacion.getId(), contexto != null ? contexto.getFechaHoraEntrega() : "Consultar Logística", contexto != null ? contexto.getPatenteCamion() : "Consultar Logística");
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
