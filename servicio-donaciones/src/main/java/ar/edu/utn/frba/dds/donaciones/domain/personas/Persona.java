package ar.edu.utn.frba.dds.donaciones.domain.personas;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class Persona {
    private List<MedioContacto> medios = new ArrayList<>();
    private LocalDateTime ultimaActividadDonacion;

    public void setPreferredMedio(TipoMedio tipo, String nr) {
        medios.stream()
                .filter(m -> m.getType() == tipo && nr.equals(m.getValor()))
                .findFirst()
                .ifPresent(m -> {
                });
    }

    public void getPreferredMedio(TipoMedio tipo) {
    }

    public void addMedioContacto(TipoMedio tipo, String nr) {
        medios.add(new MedioContacto(tipo, nr));
    }

    public List<MedioContacto> getMediosTipo(TipoMedio tipo) {
        return medios.stream()
                .filter(m -> m.getType() == tipo)
                .collect(Collectors.toList());
    }

    public void sacarMedio(TipoMedio tipo, String nr) {
        medios.removeIf(m -> m.getType() == tipo && nr.equals(m.getValor()));
    }
}
