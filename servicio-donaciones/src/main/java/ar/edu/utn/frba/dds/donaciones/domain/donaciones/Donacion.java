package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;

public class Donacion {
    private Integer cantidadAsignada;
    private Necesidad necesidadAsignada;
    private ItemDonado itemDonado;
    private EstadoDonacion estadoDonacion;

    public Donacion(ItemDonado itemDonado, Integer cantidadAsignada, Necesidad necesidadAsignada) {
        this.itemDonado = itemDonado;
        this.cantidadAsignada = cantidadAsignada;
        this.necesidadAsignada = necesidadAsignada;
        this.estadoDonacion = EstadoDonacion.EN_DEPOSITO;
    }

    public void asignarA(Necesidad necesidad){
        this.necesidadAsignada = necesidad;
        estadoDonacion = EstadoDonacion.ASIGNADA;
    }

    public boolean estaAsignada() {
        return necesidadAsignada != null;
    }

}
