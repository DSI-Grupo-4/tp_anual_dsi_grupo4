package ar.edu.utn.frba.dds.incentivos.domain.misiones;

import ar.edu.utn.frba.dds.incentivos.domain.personas.Donante;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class Completitud extends Mision {
    private Integer cantidadCategorias;

    @Override
    public boolean completoMision(Donante donante) {
        long categoriasDistintas = donante.getDonaciones().stream()
                .map(d -> d.getCategoria())
                .distinct()
                .count();

        return categoriasDistintas >= this.cantidadCategorias;
    }

    @Override
    public float progresoMision(Donante donante) {
        long categoriasDistintas = donante.getDonaciones().stream()
                .map(d -> d.getCategoria())
                .distinct()
                .count();

        return (float) categoriasDistintas / this.cantidadCategorias;
    }
}
