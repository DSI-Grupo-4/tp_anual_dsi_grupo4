package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.List;

public class Donacion {
    private Integer cantidadAsignada;
    private Necesidad necesidadAsignada;
    private ItemDonado itemDonado;
    private LocalDateTime fechaCreacion;
    private EstadoDonacion estadoActual;
    private List<TimeStamp> historialEstados;
    private EntidadBeneficiaria entidadBeneficiaria;

    public Donacion(ItemDonado itemDonado, Integer cantidadAsignada, Necesidad necesidadAsignada, EntidadBeneficiaria entidadBeneficiaria) {
        this.itemDonado = itemDonado;
        this.cantidadAsignada = cantidadAsignada;
        this.necesidadAsignada = necesidadAsignada;
        this.entidadBeneficiaria = entidadBeneficiaria;
        this.fechaCreacion = LocalDateTime.now();
    }

    public void asignarA(Necesidad necesidad) {
        this.necesidadAsignada = necesidad;
        estadoActual = EstadoDonacion.ASIGNADA;
    }

    public boolean estaAsignada() {
        return necesidadAsignada != null;
    }

    private void cambiarEstado(EstadoDonacion nuevoEstado, String justificacion) {
        this.estadoActual = nuevoEstado;

        historialEstados.add(
                new TimeStamp(nuevoEstado, justificacion)
        );
    }

    public void entregar() {
        cambiarEstado(EstadoDonacion.ENTREGADA, "Entrega confirmada");
    }

}
