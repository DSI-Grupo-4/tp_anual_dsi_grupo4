package ar.edu.utn.frba.dds.logistica.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Una parada de la entidad que consulta. No incluye las otras paradas, su dirección ni el chofer. */
public record EnvioPropioDTO(
        Integer idRuta,
        Integer idParada,
        Integer numeroParada,
        int totalParadas,
        LocalDate fecha,
        String estadoRuta,
        String patente,
        Double latitud,
        Double longitud,
        LocalDateTime ubicacionActualizadaEn,
        boolean ubicacionSimulada,
        List<EntregaVista> entregas) {

    public record EntregaVista(
            Integer idEntrega,
            Integer idDonacionAsociada,
            String estadoEntrega,
            LocalDateTime fechaHoraEntrega,
            String justificacionFallo,
            List<FotoVista> fotos) {
    }

    public record FotoVista(String url, java.time.LocalDate fecha) {
    }
}
