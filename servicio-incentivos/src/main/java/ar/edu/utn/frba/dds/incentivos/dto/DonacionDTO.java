package ar.edu.utn.frba.dds.incentivos.dto;
import java.time.LocalDate;
import java.util.List;

import ar.edu.utn.frba.dds.incentivos.domain.personas.Donante;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DonacionDTO {
    private List<Donante> donantesAsociados;
    private LocalDate fecha;
    private String beneficiario; //razon social
}
