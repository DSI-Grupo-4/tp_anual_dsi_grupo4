package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.algoritmos.AlgoritmoAsignacion;
import ar.edu.utn.frba.dds.donaciones.domain.algoritmos.CompatibilidadSemantica;
import ar.edu.utn.frba.dds.donaciones.domain.algoritmos.PrioridadSubatendidos;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Component
@Getter
@Setter
public class GestorDonaciones {

    private Deposito deposito;
    private List<Donacion> donaciones;
    private List<AlgoritmoAsignacion> algoritmos;
    private Long siguienteId = 1L;

    // Constructor de conveniencia (tests, uso manual sin contexto de Spring).
    public GestorDonaciones() {
        this(new Deposito());
    }

    @Autowired
    public GestorDonaciones(Deposito deposito) {
        this.deposito = deposito;
        this.donaciones = new ArrayList<>();
        this.algoritmos = new ArrayList<>();
        this.algoritmos.add(new CompatibilidadSemantica());
        this.algoritmos.add(new PrioridadSubatendidos());
    }

    /**
     * Registra una donación ya construida (por ej. resultado de
     * SolicitudDonacion.segmentar(), que llega con id=null) asignándole id
     * si no tiene, y la agrega a la única lista real de donaciones.
     */
    public Donacion registrarDonacion(Donacion donacion) {
        if (donacion.getId() == null) {
            donacion.setId(siguienteId++);
        }
        donaciones.add(donacion);
        return donacion;
    }

    public Donacion buscarPorId(Long id) {
        return donaciones.stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "No existe la donación " + id));
    }

    public void eliminar(Long id) {
        donaciones.removeIf(d -> d.getId().equals(id));
    }

    public void registrarSolicitud(SolicitudDonacion solicitud) {
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
