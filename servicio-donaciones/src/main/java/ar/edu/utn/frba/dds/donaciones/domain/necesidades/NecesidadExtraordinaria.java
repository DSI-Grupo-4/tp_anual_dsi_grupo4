package ar.edu.utn.frba.dds.donaciones.domain.necesidades;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NecesidadExtraordinaria extends Necesidad {
    private TipoExtraordinario tipoExtraordinario;

    public NecesidadExtraordinaria(
            Long id,
            String descripcion,
            Subcategoria subcategoria,
            Integer cantidadRequerida,
            TipoExtraordinario tipoExtraordinario) {

        super(id, descripcion, subcategoria, cantidadRequerida);
        this.tipoExtraordinario = tipoExtraordinario;
    }
}