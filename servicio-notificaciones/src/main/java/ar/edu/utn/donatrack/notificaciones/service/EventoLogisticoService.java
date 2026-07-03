package ar.edu.utn.donatrack.notificaciones.service;

import ar.edu.utn.donatrack.notificaciones.dto.NotificacionEntregaExitosaDTO;
import ar.edu.utn.donatrack.notificaciones.dto.NotificacionEntregaFallidaDTO;
import ar.edu.utn.donatrack.notificaciones.dto.NotificacionInicioRutaDTO;
import ar.edu.utn.donatrack.notificaciones.dto.NotificacionRequestDTO;
import ar.edu.utn.donatrack.notificaciones.model.Notificacion;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Procesa los eventos logísticos recibidos desde el Servicio de Donaciones
 * (que a su vez los consume de Logística por polling) y genera las notificaciones
 * correspondientes a cada destinatario.
 *
 * Decisión de diseño: este servicio no llama directamente a Logística.
 * El flujo es: Logística → EventoLogistico disponible → Donaciones hace polling
 * → llama a este endpoint con los datos ya resueltos (contactos, medios, etc.).
 *
 * ARCHIVO NUEVO - Entrega 3.
 */
@Service
public class EventoLogisticoService {

    private final NotificacionService notificacionService;

    public EventoLogisticoService(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    /**
     * Notifica inicio de ruta a todos los donantes y entidades beneficiarias
     * cuyas entregas forman parte de la ruta.
     * El mensaje incluye el enlace al mapa interactivo.
     */
    public List<Notificacion> notificarInicioRuta(NotificacionInicioRutaDTO dto) {
        List<Notificacion> enviadas = new ArrayList<>();

        for (NotificacionInicioRutaDTO.DestinatarioDTO destinatario : dto.getDestinatarios()) {
            String mensaje = String.format(
                    "Tu entrega está en camino. Podés seguirla en tiempo real: %s",
                    dto.getEnlaceMapa()
            );

            NotificacionRequestDTO request = new NotificacionRequestDTO(
                    mensaje,
                    destinatario.getMedio(),
                    destinatario.getContacto(),
                    "SERVICIO_LOGISTICA"
            );
            enviadas.add(notificacionService.enviarNotificacion(request));
        }

        return enviadas;
    }

    /**
     * Notifica entrega exitosa al donante y a la entidad beneficiaria.
     * El mensaje incluye el comprobante: fecha, hora y patente del camión.
     */
    public List<Notificacion> notificarEntregaExitosa(NotificacionEntregaExitosaDTO dto) {
        List<Notificacion> enviadas = new ArrayList<>();

        String comprobante = String.format(
                "La donación fue entregada el %s por el camión %s.%s",
                dto.getFechaHoraEntrega(),
                dto.getPatenteCamion(),
                dto.getObservacion() != null ? " Observación: " + dto.getObservacion() : ""
        );

        // Notificar al donante
        NotificacionRequestDTO requestDonante = new NotificacionRequestDTO(
                "¡Tu donación llegó a destino! " + comprobante,
                dto.getMedioDonante(),
                dto.getContactoDonante(),
                "SERVICIO_LOGISTICA"
        );
        enviadas.add(notificacionService.enviarNotificacion(requestDonante));

        // Notificar a la entidad beneficiaria
        NotificacionRequestDTO requestEntidad = new NotificacionRequestDTO(
                "Recepción registrada exitosamente. " + comprobante,
                dto.getMedioEntidadBeneficiaria(),
                dto.getContactoEntidadBeneficiaria(),
                "SERVICIO_LOGISTICA"
        );
        enviadas.add(notificacionService.enviarNotificacion(requestEntidad));

        return enviadas;
    }

    /**
     * Notifica entrega no satisfactoria al donante, a la entidad beneficiaria
     * y a todas las personas administradoras.
     */
    public List<Notificacion> notificarEntregaFallida(NotificacionEntregaFallidaDTO dto) {
        List<Notificacion> enviadas = new ArrayList<>();

        String mensajeBase = String.format(
                "La entrega de la donación #%d no pudo concretarse. Motivo: %s.%s",
                dto.getIdDonacion(),
                dto.getMotivo(),
                dto.isReplanificable() ? " Se generará una nueva planificación." : ""
        );

        // Notificar al donante
        enviadas.add(notificacionService.enviarNotificacion(new NotificacionRequestDTO(
                mensajeBase, dto.getMedioDonante(), dto.getContactoDonante(), "SERVICIO_LOGISTICA"
        )));

        // Notificar a la entidad beneficiaria
        enviadas.add(notificacionService.enviarNotificacion(new NotificacionRequestDTO(
                mensajeBase, dto.getMedioEntidadBeneficiaria(),
                dto.getContactoEntidadBeneficiaria(), "SERVICIO_LOGISTICA"
        )));

        // Notificar a cada administradora
        if (dto.getAdministradoras() != null) {
            for (NotificacionEntregaFallidaDTO.AdministradoraDTO admin : dto.getAdministradoras()) {
                enviadas.add(notificacionService.enviarNotificacion(new NotificacionRequestDTO(
                        "[ADMIN] " + mensajeBase, admin.getMedio(),
                        admin.getContacto(), "SERVICIO_LOGISTICA"
                )));
            }
        }

        return enviadas;
    }
}
