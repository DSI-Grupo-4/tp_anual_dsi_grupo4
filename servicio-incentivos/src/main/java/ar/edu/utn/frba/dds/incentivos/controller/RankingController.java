package ar.edu.utn.frba.dds.incentivos.controller;

import ar.edu.utn.frba.dds.incentivos.consultor.Consultor;
import ar.edu.utn.frba.dds.incentivos.dto.ActividadMensualDonanteDTO;
import ar.edu.utn.frba.dds.incentivos.dto.RankingDTO;
import ar.edu.utn.frba.dds.incentivos.dto.RankingPosicionDTO;
import ar.edu.utn.frba.dds.incentivos.ranking.ActividadMensualDonante;
import ar.edu.utn.frba.dds.incentivos.ranking.Ranking;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    private final Consultor consultor = Consultor.getInstance();

    @io.swagger.v3.oas.annotations.Operation(summary = "Obtener historial", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @GetMapping("/historial")
    public List<RankingDTO> obtenerHistorial() {
        return consultor.obtenerHistorialRanking().stream()
                .map(this::convertirADTO)
                .toList();
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Obtener ranking de mes", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @GetMapping("/mes")
    public RankingDTO obtenerRankingDeMes(@io.swagger.v3.oas.annotations.Parameter(description = "Fecha de un mes cuyo ranking esté generado.", example = "2026-09-30") @RequestParam("fecha") LocalDate fecha) {
        return convertirADTO(consultor.obtenerRankingDeMes(fecha));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Ranking del mes en curso", description = "Todavía sin cerrar: todos los donantes con misiones completadas este mes, de más a menos. Lista vacía si nadie completó misiones.")
    @GetMapping("/actual")
    public List<RankingPosicionDTO> obtenerRankingEnCurso() {
        List<ActividadMensualDonante> actividad = consultor.obtenerRankingEnCurso();
        String mes = YearMonth.now().toString();
        return IntStream.range(0, actividad.size())
                .mapToObj(i -> posicion(actividad.get(i), i + 1, mes))
                .toList();
    }

    private static RankingPosicionDTO posicion(ActividadMensualDonante actividad, int puesto, String mes) {
        RankingPosicionDTO dto = new RankingPosicionDTO();
        dto.setPuesto(puesto);
        dto.setDonanteId(actividad.getDonanteAsociado().getId());
        dto.setDonanteNombre(actividad.getDonanteAsociado().getNombre());
        dto.setCantidadMisiones(actividad.getCantidad());
        dto.setMes(mes);
        return dto;
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Obtener ultimo", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @GetMapping("/ultimo")
    public List<RankingPosicionDTO> obtenerUltimo() {
        Ranking ranking = consultor.obtenerUltimoRanking();
        String mes = YearMonth.from(ranking.getFechaEmision()).toString();
        List<ActividadMensualDonante> topDonantes = ranking.getTopDonantes();

        return IntStream.range(0, topDonantes.size())
                .mapToObj(i -> posicion(topDonantes.get(i), i + 1, mes))
                .toList();
    }

    private RankingDTO convertirADTO(Ranking ranking) {
        RankingDTO dto = new RankingDTO();
        dto.setFechaEmision(ranking.getFechaEmision());

        List<ActividadMensualDonante> topDonantes = ranking.getTopDonantes();
        dto.setTopDonantes(IntStream.range(0, topDonantes.size())
                .mapToObj(i -> convertirADTO(topDonantes.get(i), i + 1))
                .toList());
        return dto;
    }

    private ActividadMensualDonanteDTO convertirADTO(ActividadMensualDonante actividad, int puesto) {
        ActividadMensualDonanteDTO dto = new ActividadMensualDonanteDTO();
        dto.setDonanteId(actividad.getDonanteAsociado().getId());
        dto.setDonanteNombre(actividad.getDonanteAsociado().getNombre());
        dto.setCantidad(actividad.getCantidad());
        dto.setPuesto(puesto);
        return dto;
    }
}
