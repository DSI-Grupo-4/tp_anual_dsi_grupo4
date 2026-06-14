package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemDonado{
    private String descripcion;
    private Subcategoria subcategoria;
    private Integer cantidad;
    private UnidadMedida unidadMedida;
    private String foto;

    public ItemDonado(
            String descripcion,
            Subcategoria subcategoria,
            Integer cantidad,
            UnidadMedida unidadMedida,
            String foto) {

        this.descripcion = descripcion;
        this.subcategoria = subcategoria;
        this.cantidad = cantidad;
        this.unidadMedida = unidadMedida;
        this.foto = foto;
    }
}
