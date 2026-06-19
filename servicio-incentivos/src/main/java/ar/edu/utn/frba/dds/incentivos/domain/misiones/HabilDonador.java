package ar.edu.utn.frba.dds.incentivos.domain.misiones;

import ar.edu.utn.frba.dds.incentivos.domain.personas.Donante;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class HabilDonador extends Mision {
    private Integer cantidadBienes;

    @Override
    public boolean completoMision(Donante donante) {
        return donante.getDonaciones().stream()
                .anyMatch(d -> d.getCantidadBienes() > this.cantidadBienes);
    }

    @Override
    public float progresoMision(Donante donante) {
        if (this.completoMision(donante)) {
            return 1f;
        }
        return 0f;
    }
}
