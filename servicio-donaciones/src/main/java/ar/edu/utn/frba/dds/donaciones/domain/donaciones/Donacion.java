package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;

public class Donacion {
    private Subcategoria subcategoria;
    private Integer cantidad;
    private UnidadMedida unidadMedida;
    private String foto;
    private Necesidad necesidadAsignada;

    public Donacion(ItemDonado item) {

        this.subcategoria = item.getSubcategoria();
        this.cantidad = item.getCantidad();
        this.unidadMedida = item.getUnidadMedida();
        this.foto = item.getFoto();
    }

    public void asignarA(Necesidad necesidad){
        this.necesidadAsignada = necesidad;
    }

    public boolean estaAsignada() {
        return necesidadAsignada != null;
    }
}
