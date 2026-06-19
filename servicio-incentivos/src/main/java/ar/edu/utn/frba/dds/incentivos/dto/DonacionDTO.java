package ar.edu.utn.frba.dds.incentivos.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class DonacionDTO {
    private List<Integer> donanteIds;
    private LocalDate fecha;
    private String beneficiario;
}
