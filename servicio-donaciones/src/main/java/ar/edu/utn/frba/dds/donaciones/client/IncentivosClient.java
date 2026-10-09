package ar.edu.utn.frba.dds.donaciones.client;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Categoria;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import ar.edu.utn.frba.dds.donaciones.domain.personas.Donante;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.domain.personas.MedioContacto;
import ar.edu.utn.frba.dds.donaciones.domain.personas.Persona;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaHumana;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaJuridica;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoContacto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Integración síncrona Donaciones -> Incentivos: avisa cuando una donación
 * llegó a ENTREGADA para que impacte en el cálculo de progreso de misiones
 * del donante. No es parte de la cola de mensajes hacia Notificaciones (esa
 * asincronía es un requerimiento específico para ese servicio) -- es una
 * llamada REST directa, igual que Donaciones -> Logística.
 *
 * Si Incentivos no responde, se loguea y se continúa: esto nunca debe
 * bloquear el cambio de estado de la donación en Donaciones.
 */
@Component
public class IncentivosClient {

    private static final Logger logger = LoggerFactory.getLogger(IncentivosClient.class);

    private final RestClient restClient;

    public IncentivosClient(@Value("${incentivos.base-url:http://localhost:8081}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public void registrarActividadDonacion(Donacion donacion) {
        Donante donante = donacion.getDonante();
        if (donante == null) {
            logger.warn("Donación {} sin donante asociado -- no se notifica actividad a Incentivos.",
                    donacion.getId());
            return;
        }
        try {
            restClient.post()
                    .uri("/donantes/{id}/actividad-donacion", donante.getId())
                    .body(construirRequest(donacion, donante))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException error) {
            logger.warn("No se pudo registrar la actividad de donación del donante {} en Incentivos: {}",
                    donante.getId(), error.getMessage());
        }
    }

    private ActividadDonacionRequest construirRequest(Donacion donacion, Donante donante) {
        Persona persona = donante.getPersona();
        Optional<MedioContacto> medio = persona != null ? persona.medioPreferido() : Optional.empty();
        EntidadBeneficiaria entidad = donacion.getEntidadBeneficiaria();

        return new ActividadDonacionRequest(
                LocalDate.now(),
                categoriaBienDe(donacion),
                donacion.getCantidadAsignada(),
                donacion.getEstadoActual() == EstadoTrack.ENTREGADA,
                entidad != null ? entidad.getId() : null,
                entidad != null ? entidad.getEntidad().getRazonSocial() : null,
                nombreDe(persona),
                medio.map(m -> mapearMedio(m.getTipo())).orElse(null),
                medio.map(MedioContacto::getValor).orElse(null));
    }

    private String categoriaBienDe(Donacion donacion) {
        return donacion.getCategoria().name();
    }

    private String nombreDe(Persona persona) {
        if (persona instanceof PersonaHumana humana) {
            return humana.nombreCompleto();
        }
        if (persona instanceof PersonaJuridica juridica) {
            return juridica.getRazonSocial();
        }
        return null;
    }

    private String mapearMedio(TipoContacto tipo) {
        return switch (tipo) {
            case EMAIL -> "EMAIL";
            case TELEFONO -> "SMS";
            case WHATSAPP -> "WHATSAPP";
        };
    }

    private record ActividadDonacionRequest(
            LocalDate fecha,
            String categoriaNombre,
            java.math.BigDecimal cantidadBienes,
            boolean donacionExitosa,
            Long beneficiarioId,
            String beneficiarioNombre,
            String donanteNombre,
            String donanteMedioContacto,
            String donanteContacto) {
    }
}
