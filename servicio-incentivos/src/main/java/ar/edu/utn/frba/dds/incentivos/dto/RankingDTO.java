package ar.edu.utn.frba.dds.incentivos.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class RankingDTO {
    private LocalDate fechaEmision;
    private List<ActividadMensualDonanteDTO> topDonantes;
}
