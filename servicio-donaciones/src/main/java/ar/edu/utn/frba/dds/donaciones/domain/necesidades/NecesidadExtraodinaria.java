package ar.edu.utn.frba.dds.donaciones.domain.necesidades;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;

public class NecesidadExtraodinaria extends Necesidad {
    private TipoExtraordinario tipoExtraordinario;

    public NecesidadExtraodinaria(String descripcion, Subcategoria subcategoria, Integer cantidadRequerida) {
        super(descripcion, subcategoria, cantidadRequerida);

        this.tipoExtraordinario = tipoExtraordinario;
    }
}
