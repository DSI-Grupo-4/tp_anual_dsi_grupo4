package ar.edu.utn.frba.dds.donaciones.domain.necesidades;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Necesidad {
    private String descripcion;
    private Subcategoria subcategoria;
    private Integer cantidadRequerida;
    private Integer cantidadRecibida;
    private EntidadBeneficiaria entidadBeneficiaria;
    private Long id;

    public Necesidad(
        Long id,
        String descripcion,
        Subcategoria subcategoria,
        Integer cantidadRequerida) {

    this.id = id;
    this.descripcion = descripcion;
    this.subcategoria = subcategoria;
    this.cantidadRequerida = cantidadRequerida;
    this.cantidadRecibida = 0;
}

    public boolean satisfecha() {
        return cantidadRecibida >= cantidadRequerida;
    }

    public void recibir(Integer cantidad) {
        cantidadRecibida += cantidad;
    }
}
