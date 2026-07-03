package ar.edu.utn.frba.dds.incentivos.controller;

import ar.edu.utn.frba.dds.incentivos.consultor.Consultor;
import ar.edu.utn.frba.dds.incentivos.dto.ActividadMensualDonanteDTO;
import ar.edu.utn.frba.dds.incentivos.dto.RankingDTO;
import ar.edu.utn.frba.dds.incentivos.ranking.ActividadMensualDonante;
import ar.edu.utn.frba.dds.incentivos.ranking.Ranking;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/ranking")
public class RankingController {

    private final Consultor consultor = Consultor.getInstance();

    @GetMapping("/historial")
    public List<RankingDTO> obtenerHistorial() {
        return consultor.obtenerHistorialRanking().stream()
                .map(this::convertirADTO)
                .toList();
    }

    @GetMapping("/mes")
    public RankingDTO obtenerRankingDeMes(@RequestParam("fecha") LocalDate fecha) {
        return convertirADTO(consultor.obtenerRankingDeMes(fecha));
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
        dto.setCantidad(actividad.getCantidad());
        dto.setPuesto(puesto);
        return dto;
    }
}
