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
import java.util.stream.Collectors;

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
        this.algoritmos.add(new CompatibilidadSemantica());
        this.algoritmos.add(new PrioridadSubatendidos());
    }

    public void registrarSolicitud(SolicitudDonacion solicitud) {
        solicitud.getItems().forEach(deposito::cargarItem);
    }

    public void registrarNecesidad(Necesidad necesidad, EntidadBeneficiaria entidad) {
        entidad.agregarNecesidad(necesidad);
        this.necesidades.add(necesidad);
    }

    public void registrarEntidad(EntidadBeneficiaria entidad) {
        this.entidades.add(entidad);
    }

    public void agregarAlgoritmo(AlgoritmoAsignacion algoritmo) {
        this.algoritmos.add(algoritmo);
    }

    public Donacion asignarDonacion(
            Long id,
            ItemDonado item,
            Necesidad necesidad,
            Integer cantidad) {

        item.descontar(cantidad);
        necesidad.recibir(cantidad);

        Donacion donacion = new Donacion(id, item, cantidad, necesidad,
                necesidad.getEntidadBeneficiaria());
        donaciones.add(donacion);
        necesidad.getEntidadBeneficiaria().registrarAyuda(donacion);
        deposito.eliminarSinStock();

        return donacion;
    }

    public List<EntidadBeneficiaria> ejecutarMatchmaking(
            Donacion donacion,
            List<EntidadBeneficiaria> todasLasEntidades) {

        List<List<EntidadBeneficiaria>> resultadosPorAlgoritmo = algoritmos.stream()
                .map(alg -> alg.ejecutarAlgoritmo(donacion, todasLasEntidades))
                .toList();

        List<EntidadBeneficiaria> interseccion = resultadosPorAlgoritmo.stream()
                .reduce((a, b) -> a.stream()
                        .filter(b::contains)
                        .collect(Collectors.toList()))
                .orElse(new ArrayList<>());

        if (!interseccion.isEmpty()) {
            return interseccion.stream().limit(10).toList();
        }

        return resultadosPorAlgoritmo.stream()
                .flatMap(List::stream)
                .distinct()
                .limit(10)
                .toList();
    }

    public void confirmarAsignacion(Donacion donacion, EntidadBeneficiaria entidad) {
        donacion.cambiarEstado(EstadoTrack.ASIGNACION_REALIZADA, null);
        donacion.setEntidadBeneficiaria(entidad);
    }
}
