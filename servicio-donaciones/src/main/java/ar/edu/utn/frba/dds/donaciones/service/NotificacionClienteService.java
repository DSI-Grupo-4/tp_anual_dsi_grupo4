package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.personas.MedioContacto;
import ar.edu.utn.frba.dds.donaciones.domain.personas.Persona;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoContacto;
import ar.edu.utn.frba.dds.donaciones.dto.NotificacionRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * Cliente HTTP hacia el Servicio de Notificaciones (POST /api/notificaciones).
 *
 * Este es el componente que faltaba: el Servicio de Donaciones nunca invocaba
 * al Servicio de Notificaciones, a pesar de que el enunciado (Entrega 2 -
 * "Eventos e integración con medios de notificación") pide notificar en 3
 * casos que le corresponden a este servicio:
 *   1. Donante inactivo hace más de 20 días.
 *   2. Entidad beneficiaria a la que se le asigna una donación.
 *   3. Donante cuya donación acaba de ser asignada a una entidad.
 *
 * Sigue el mismo patrón que ya usa el Servicio de Incentivos en su clase
 * Consultor: URL configurable por properties, RestTemplate simple y, si el
 * envío falla o no hay datos de contacto, se loguea un warning y se continúa
 * (no se aborta la operación de negocio por un problema de notificaciones).
 */
@Service
public class NotificacionClienteService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionClienteService.class);

    private static final String SERVICIO_ORIGEN = "donaciones";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${servicios.notificaciones.url:http://notificaciones-service/api/notificaciones}")
    private String notificacionesUrl;

    /**
     * Notifica a una Persona (donante humano/jurídico o entidad beneficiaria)
     * usando su medio de contacto preferido. Si no tiene ninguno marcado como
     * preferido, usa el primero que tenga cargado. Si no tiene ningún medio
     * de contacto, no se puede notificar: se loguea y no se hace nada (no se
     * rompe el flujo principal, p.ej. la asignación de la donación).
     */
    public void notificarPersona(Persona persona, String mensaje) {
        if (persona == null) {
            log.warn("No se pudo notificar: la persona es null. Mensaje: {}", mensaje);
            return;
        }

        Optional<MedioContacto> medio = persona.medioPreferido();
        if (medio.isEmpty() && persona.getMediosContacto() != null && !persona.getMediosContacto().isEmpty()) {
            medio = Optional.of(persona.getMediosContacto().get(0));
        }

        if (medio.isEmpty()) {
            log.warn("No se pudo notificar: la persona no tiene ningún medio de contacto cargado. Mensaje: {}", mensaje);
            return;
        }

        enviar(mensaje, mapearMedio(medio.get().getTipo()), medio.get().getValor());
    }

    private void enviar(String mensaje, String medio, String contacto) {
        NotificacionRequestDTO body = new NotificacionRequestDTO(mensaje, medio, contacto, SERVICIO_ORIGEN);
        try {
            restTemplate.postForEntity(notificacionesUrl, body, Void.class);
        } catch (RuntimeException ex) {
            log.warn("No se pudo enviar la notificación a {} ({}): {}", contacto, medio, ex.getMessage());
        }
    }

    private String mapearMedio(TipoContacto tipo) {
        // TipoContacto (Donaciones) -> MedioComunicacion (Notificaciones).
        // TELEFONO no existe como medio de envío en Notificaciones, así que
        // se mapea a SMS, que es el equivalente por número de teléfono.
        return switch (tipo) {
            case EMAIL -> "EMAIL";
            case WHATSAPP -> "WHATSAPP";
            case TELEFONO -> "SMS";
        };
    }
}
