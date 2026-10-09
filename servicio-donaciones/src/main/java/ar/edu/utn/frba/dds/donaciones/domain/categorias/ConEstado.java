package ar.edu.utn.frba.dds.donaciones.domain.categorias;
import lombok.Getter;
import java.time.LocalDate;
@Getter
public final class ConEstado {
    private final Condicion condicion;
    public ConEstado(Condicion condicion) {
        if (condicion == null) throw new IllegalArgumentException("condicion es obligatoria");
        this.condicion = condicion;
    }
}
