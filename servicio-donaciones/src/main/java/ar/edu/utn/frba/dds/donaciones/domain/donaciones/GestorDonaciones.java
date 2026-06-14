package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;

import java.util.ArrayList;
import java.util.List;

public class GestorDonaciones {

    private Deposito deposito;
    private List<Donacion> donaciones;
    private List<EntidadBeneficiaria> entidades;

    public GestorDonaciones() {

        this.deposito = new Deposito();
        this.donaciones = new ArrayList<>();
    }

    public void registrarSolicitud(SolicitudDonacion solicitud) {

        solicitud.getItems()
                .forEach(deposito::cargarItem);
    }

    public void registrarNecesidad(Necesidad necesidad, EntidadBeneficiaria entidad) {
        entidad.agregarNecesidad(necesidad);
    }

    public Donacion asignarDonacion(
            ItemDonado item,
            Necesidad necesidad,
            Integer cantidad) {

        item.descontar(cantidad);

        necesidad.recibir(cantidad);

        Donacion donacion =
                new Donacion(
                        item,
                        cantidad,
                        necesidad
                );

        donaciones.add(donacion);

        necesidad.getEntidadBeneficiaria()
                .registrarAyuda();

        deposito.eliminarSinStock();

        return donacion;
    }
}