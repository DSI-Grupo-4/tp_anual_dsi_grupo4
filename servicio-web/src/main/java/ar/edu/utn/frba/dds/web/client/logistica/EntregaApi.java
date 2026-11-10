package ar.edu.utn.frba.dds.web.client.logistica;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entrega tal como la devuelve GET /api/entregas de Logística.
 * Solo se mapean los campos que usa el cliente web: la dirección de destino
 * y el camión NO se leen a propósito, para que nunca lleguen a una vista pública.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EntregaApi(
        Integer idEntrega,
        Integer idDonacionAsociada,
        Integer idEntidadBeneficiariaAsociada,
        String estadoEntrega,
        LocalDate fecha,
        LocalDateTime fechaHoraEntrega,
        List<FotoApi> fotos) {

    public List<FotoApi> fotos() {
        return fotos == null ? List.of() : fotos;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FotoApi(String url, LocalDate fecha) {
    }
}
