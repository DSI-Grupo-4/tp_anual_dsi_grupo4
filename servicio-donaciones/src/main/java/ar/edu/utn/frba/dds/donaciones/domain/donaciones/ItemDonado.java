package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemDonado{
    private Long id;
    private String descripcion;
    private Subcategoria subcategoria;
    private Integer cantidad;
    private UnidadMedida unidadMedida;
    private String foto;

    public ItemDonado(
            Long id,
            String descripcion,
            Subcategoria subcategoria,
            Integer cantidad,
            UnidadMedida unidadMedida,
            String foto) {

        this.id = id;
        this.descripcion = descripcion;
        this.subcategoria = subcategoria;
        this.cantidad = cantidad;
        this.unidadMedida = unidadMedida;
        this.foto = foto;
    }

    public void descontar(Integer cantidadADescontar) {
        if(cantidadADescontar > cantidad) {
            throw new RuntimeException("Stock insuficiente");
        }

        cantidad -= cantidadADescontar;
    }

    public Boolean sinStock() {
        return cantidad == 0;
    }

    public Boolean tieneStock(Integer cantidadSolicitada) {
        return cantidad >= cantidadSolicitada;
    }
}
