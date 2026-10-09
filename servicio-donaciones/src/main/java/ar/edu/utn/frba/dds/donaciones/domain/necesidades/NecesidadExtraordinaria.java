package ar.edu.utn.frba.dds.donaciones.domain.necesidades;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import lombok.Getter;
import java.math.BigDecimal;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.UnidadMedida;
import lombok.Setter;

@Getter
@Setter
public class NecesidadExtraordinaria extends Necesidad {
    private TipoExtraordinario tipoExtraordinario;

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
