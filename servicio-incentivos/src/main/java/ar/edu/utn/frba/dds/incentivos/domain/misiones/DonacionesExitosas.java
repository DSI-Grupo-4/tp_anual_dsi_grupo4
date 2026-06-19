package ar.edu.utn.frba.dds.incentivos.domain.misiones;

import ar.edu.utn.frba.dds.incentivos.domain.personas.Donante;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class DonacionesExitosas extends Mision {
    private Integer donacionesRealizadas;
    private Integer cantidadDonaciones;

    @Override
    public boolean completoMision(Donante donante) {
        long donacionesExitosas = donante.getDonaciones().stream()
                .filter(d -> d.getEstado() == EstadoDonacion.ENTREGADA)
                .count();

        return donacionesExitosas >= this.cantidadDonaciones;
    }

    @Override
    public float progresoMision(Donante donante) {
        long donacionesExitosas = donante.getDonaciones().stream()
                .filter(d -> d.getEstado() == EstadoDonacion.ENTREGADA)
                .count();

        return (float) donacionesExitosas / this.cantidadDonaciones;
    }
}
