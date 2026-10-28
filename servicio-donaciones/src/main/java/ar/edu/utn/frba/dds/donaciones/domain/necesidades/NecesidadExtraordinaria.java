package ar.edu.utn.frba.dds.donaciones.domain.necesidades;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import java.math.BigDecimal;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.UnidadMedida;
import lombok.Setter;

@Getter
@Setter
@jakarta.persistence.Entity
@DiscriminatorValue("EXTRAORDINARIA")
public class NecesidadExtraordinaria extends Necesidad {
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_extraordinario")
    private TipoExtraordinario tipoExtraordinario;

    protected NecesidadExtraordinaria() {
        // requerido por JPA
    }

    public NecesidadExtraordinaria(
            Long id,
            String descripcion,
            Subcategoria subcategoria,
            UnidadMedida unidadMedida,
            BigDecimal cantidadRequerida,
            TipoExtraordinario tipoExtraordinario) {

        super(id, descripcion, subcategoria, unidadMedida, cantidadRequerida);
        setTipoExtraordinario(tipoExtraordinario);
    }
    public void setTipoExtraordinario(TipoExtraordinario tipo) {
        if (tipo == null) throw new IllegalArgumentException("tipoExtraordinario es obligatorio");
        this.tipoExtraordinario = tipo;
    }
}
