package ar.edu.utn.frba.dds.incentivos.controller;

import ar.edu.utn.frba.dds.incentivos.consultor.Consultor;
import ar.edu.utn.frba.dds.incentivos.dto.EvolucionMensualDTO;
import ar.edu.utn.frba.dds.incentivos.dto.MetricasSistemaDTO;
import ar.edu.utn.frba.dds.incentivos.metricas.EvolucionMensual;
import ar.edu.utn.frba.dds.incentivos.metricas.MetricasSistema;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final Consultor consultor = Consultor.getInstance();

    @Operation(
            summary = "Obtener métricas del sistema",
            description = "Obtiene las métricas generales del sistema, incluyendo donantes activos, solicitudes de donación, misiones completadas, insignias otorgadas y su evolución mensual."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Métricas obtenidas correctamente"
            )
    })
    @GetMapping("/metricas")
    public MetricasSistemaDTO obtenerMetricasSistema() {
        return convertirADTO(consultor.obtenerMetricasSistema());
    }

    private MetricasSistemaDTO convertirADTO(MetricasSistema metricas) {
        MetricasSistemaDTO dto = new MetricasSistemaDTO();
        dto.setDonantesActivos(metricas.getDonantesActivos());
        dto.setSolicitudesDonacionTotales(metricas.getSolicitudesDonacionTotales());
        dto.setMisionesCompletadasTotales(metricas.getMisionesCompletadasTotales());
        dto.setInsigniasOtorgadasTotales(metricas.getInsigniasOtorgadasTotales());
        dto.setEvolucionMensual(metricas.getEvolucionMensual().stream()
                .map(this::convertirADTO)
                .toList());
        return dto;
    }

    private EvolucionMensualDTO convertirADTO(EvolucionMensual evolucionMensual) {
        EvolucionMensualDTO dto = new EvolucionMensualDTO();
        dto.setMes(evolucionMensual.getMes().toString());
        dto.setSolicitudes(evolucionMensual.getSolicitudes());
        dto.setImpacto(evolucionMensual.getImpacto());
        return dto;
    }
}
