package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.algoritmos.AlgoritmoAsignacion;
import ar.edu.utn.frba.dds.donaciones.domain.algoritmos.CompatibilidadSemantica;
import ar.edu.utn.frba.dds.donaciones.domain.algoritmos.PrioridadSubatendidos;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.repository.DonacionRepository;
import ar.edu.utn.frba.dds.donaciones.repository.SolicitudDonacionRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.NoSuchElementException;

@Component
@Getter
@Setter
public class GestorDonaciones {

    private Deposito deposito;
    private List<AlgoritmoAsignacion> algoritmos;
    private final DonacionRepository donacionRepository;
    private final SolicitudDonacionRepository solicitudDonacionRepository;

    public GestorDonaciones(Deposito deposito, DonacionRepository donacionRepository,
                             SolicitudDonacionRepository solicitudDonacionRepository) {
        this.deposito = deposito;
        this.donacionRepository = donacionRepository;
        this.solicitudDonacionRepository = solicitudDonacionRepository;
        this.algoritmos = new java.util.ArrayList<>();
        this.algoritmos.add(new CompatibilidadSemantica());
        this.algoritmos.add(new PrioridadSubatendidos());
    }

    public List<Donacion> getDonaciones() {
        return donacionRepository.findAll();
    }

    /**
     * Registra una donación ya construida (por ej. resultado de
     * SolicitudDonacion.segmentar(), que llega con id=null) persistiéndola
     * -- el id lo asigna la base al guardar.
     */
    public Donacion registrarDonacion(Donacion donacion) {
        donacion = donacionRepository.save(donacion);
        if (donacion.getDonante() != null) donacion.getDonante().agregarDonacion(donacion);
        return donacion;
    }

    /** Persiste mutaciones hechas sobre una Donacion ya existente (cambiarEstado, confirmarAsignacion, etc). */
    public Donacion guardar(Donacion donacion) {
        return donacionRepository.save(donacion);
    }

    public Donacion buscarPorId(Long id) {
        return donacionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "No existe la donación " + id));
    }

    public void eliminar(Long id) {
        Donacion donacion = buscarPorId(id);
        if (donacion.getEstadoActual() != EstadoTrack.EN_DEPOSITO)
            throw new IllegalStateException("Solo puede eliminarse una donación en depósito");
        deposito.reemplazarItems(donacion.getItems(), List.of());
        donacionRepository.deleteById(id);
        if (donacion.getDonante() != null) donacion.getDonante().getDonaciones().remove(donacion);
    }

    public void registrarSolicitud(SolicitudDonacion solicitud) {
        solicitud = solicitudDonacionRepository.save(solicitud);
        solicitud.getItems().forEach(deposito::cargarItem);
    }

    // NOTA (hallazgo "bajo" del scan de calidad): agregarAlgoritmo()/el campo
    // algoritmos quedan sin uso real en ejecutarMatchmaking() a propósito —
    // no se "activan" acá porque ResultadoMatchmaking tiene una forma fija
    // pensada para exactamente 2 algoritmos (porCompatibilidad/porSubatencion/
    // interseccion), no N. Generalizar eso es un rediseño, no una limpieza;
    // si se necesita un 3er algoritmo, es su propio ciclo síntoma→propuesta.
    public void agregarAlgoritmo(AlgoritmoAsignacion algoritmo) {
        this.algoritmos.add(algoritmo);
    }

    public ResultadoMatchmaking ejecutarMatchmaking(
            Donacion donacion,
            List<EntidadBeneficiaria> todasLasEntidades) {

        List<EntidadBeneficiaria> porCompatibilidad = new CompatibilidadSemantica()
                .ejecutarAlgoritmo(donacion, todasLasEntidades);
        List<EntidadBeneficiaria> porSubatencion = new PrioridadSubatendidos()
                .ejecutarAlgoritmo(donacion, todasLasEntidades);

        List<EntidadBeneficiaria> interseccion = porCompatibilidad.stream()
                .filter(porSubatencion::contains)
                .limit(10)
                .toList();

        return new ResultadoMatchmaking(porCompatibilidad, porSubatencion, interseccion);
    }
}
