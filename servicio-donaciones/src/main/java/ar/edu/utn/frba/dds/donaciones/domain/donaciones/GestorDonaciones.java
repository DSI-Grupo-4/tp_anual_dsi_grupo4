package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.algoritmos.AlgoritmoAsignacion;
import ar.edu.utn.frba.dds.donaciones.domain.algoritmos.CompatibilidadSemantica;
import ar.edu.utn.frba.dds.donaciones.domain.algoritmos.PrioridadSubatendidos;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class GestorDonaciones {

    private Deposito deposito;
    private List<Donacion> donaciones;
    private List<EntidadBeneficiaria> entidades;
    private List<Necesidad> necesidades;
    private List<AlgoritmoAsignacion> algoritmos;

    public GestorDonaciones() {

        this.deposito = new Deposito();
        this.donaciones = new ArrayList<>();
        this.necesidades = new ArrayList<>();
        this.entidades = new ArrayList<>();
        this.algoritmos = new ArrayList<>();

        this.algoritmos.add(
                new CompatibilidadSemantica()
        );

        this.algoritmos.add(
                new PrioridadSubatendidos()
        );
    }

    public void registrarSolicitud(SolicitudDonacion solicitud) {

        solicitud.getItems()
                .forEach(deposito::cargarItem);
    }

    public void registrarNecesidad(Necesidad necesidad, EntidadBeneficiaria entidad) {
        entidad.agregarNecesidad(necesidad);
        this.necesidades.add(necesidad);
    }

    public Donacion asignarDonacion(
            Long id,
            ItemDonado item,
            Necesidad necesidad,
            Integer cantidad) {

        item.descontar(cantidad);

        necesidad.recibir(cantidad);

        Donacion donacion =
                new Donacion(
                        id,
                        item,
                        cantidad,
                        necesidad,
                        necesidad.getEntidadBeneficiaria()
                );

        donaciones.add(donacion);

        necesidad.getEntidadBeneficiaria()
                .registrarAyuda(donacion);

        deposito.eliminarSinStock();

        return donacion;
    }

    public List<EntidadBeneficiaria> ejecutarAsignacion(
            ItemDonado item) {

        List<EntidadBeneficiaria> resultado =
                new ArrayList<>();

        for(AlgoritmoAsignacion algoritmo : algoritmos) {

            resultado.addAll(
                    algoritmo.generarRanking(
                            item,
                            entidades
                    )
            );
        }

        return resultado;
    }

    public void registrarEntidad(EntidadBeneficiaria entidad) {
        this.entidades.add(entidad);
    }

    public void agregarAlgoritmo(AlgoritmoAsignacion algoritmo) {
        this.algoritmos.add(algoritmo);
    }
}