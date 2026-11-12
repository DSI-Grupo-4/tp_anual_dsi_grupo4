package ar.edu.utn.frba.dds.web.client.logistica;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entrega vista por la administración (GET /api/entregas). A diferencia de EntregaApi (la de las pantallas públicas),
 * incluye el motivo de una entrega que no se pudo concretar. Nunca llega a las pantallas de entidades o donantes.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EntregaAdminApi(
        Integer idEntrega,
        Integer idDonacionAsociada,
        Integer idEntidadBeneficiariaAsociada,
        String estadoEntrega,
        LocalDate fecha,
        LocalDateTime fechaHoraEntrega,
        String justificacionFallo) {

    /** Una entrega que no se pudo concretar y hay que devolver al depósito para planificarla de nuevo. */
    public boolean puedeReingresar() {
        return "NO_RECIBIDA".equals(estadoEntrega) || "FALLIDA".equals(estadoEntrega);
    }
}
