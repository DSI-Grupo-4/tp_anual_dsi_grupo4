package ar.edu.utn.frba.dds.donaciones.domain.categorias;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Getter
@NoArgsConstructor(force = true)
@Embeddable
public final class ConEstado {
    @Enumerated(EnumType.STRING)
    @Column(name = "condicion")
    private final Condicion condicion;
    public ConEstado(Condicion condicion) {
        if (condicion == null) throw new IllegalArgumentException("condicion es obligatoria");
        this.condicion = condicion;
    }
}
