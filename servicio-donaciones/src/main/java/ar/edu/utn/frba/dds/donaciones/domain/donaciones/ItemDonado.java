package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.AtributoValor;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ItemDonado{
    private Long id;
    private String descripcion;
    private Subcategoria subcategoria;
    private Integer cantidad;
    private String foto;
    private Integer pesoKg;
    private Integer volumenM3;
    private Integer alturaM;
    // Valores concretos de los atributos dinámicos definidos por la
    // Subcategoria (ej. fechaVencimiento, estadoUso). Ver D-002.
    private List<AtributoValor> valoresAtributos;

    public ItemDonado(
            Long id,
            String descripcion,
            Subcategoria subcategoria,
            Integer cantidad,
            String foto) {

        this.id = id;
        this.descripcion = descripcion;
        this.subcategoria = subcategoria;
        this.cantidad = cantidad;
        this.foto = foto;
        this.valoresAtributos = new ArrayList<>();
    }

    public void agregarValorAtributo(AtributoValor valor) {
        valoresAtributos.add(valor);
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
