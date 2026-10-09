package ar.edu.utn.frba.dds.logistica.domain.rutas;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class Ruta {
    private Integer idRuta;
    private Camion camionAsociado;
    private Chofer chofer;
    private LocalDate fecha;
    private List<Parada> paradas;
    private EstadoRuta estadoRuta;

    public Ruta(Integer idRuta, Camion camionAsociado, Chofer chofer,
                LocalDate fecha, List<Parada> paradas) {
        this.idRuta = idRuta;
        this.camionAsociado = camionAsociado;
        this.chofer = chofer;
        this.fecha = fecha;
        this.paradas = paradas;
        this.estadoRuta = EstadoRuta.PLANIFICADA;
        // Único punto de creación de una Ruta (interno vía PlanificacionPropia
        // o externo vía el callback) -- el camión queda comprometido con esta
        // ruta apenas se planifica, no recién cuando el chofer la inicia.
        // Antes quedaba DISPONIBLE hasta iniciarRuta(), lo que permitía
        // planificarlo dos veces en paralelo si se corría la planificación
        // más de una vez antes de que saliera a la calle.
        this.camionAsociado.cambiarEstado(EstadoCamion.ASIGNADO);
    }

    public void iniciarRuta() {
        if (this.estadoRuta != EstadoRuta.PLANIFICADA) {
            throw new IllegalStateException(
                    "La ruta " + idRuta + " no puede iniciarse estando en estado " + estadoRuta);
        }
        this.estadoRuta = EstadoRuta.INICIADA;
        this.camionAsociado.cambiarEstado(EstadoCamion.EN_RUTA);
        paradas.stream()
                .flatMap(p -> p.getEntregas().stream())
                .forEach(Entrega::iniciarTraslado);
    }

    public void finalizarRuta() {
        this.estadoRuta = EstadoRuta.FINALIZADA;
        this.camionAsociado.cambiarEstado(EstadoCamion.DISPONIBLE);
    }

    /**
     * true cuando el chofer ya pasó por todas las paradas y cada entrega
     * llegó a una resolución -- entregada, o fallida por cualquier motivo
     * (no es "se entregaron todas", es "no queda nada pendiente de
     * resolver"). El camión queda libre para la próxima planificación
     * independientemente de si hubo entregas fallidas.
     */
    public Boolean completoTodasLasEntregas() {
        return paradas.stream()
                .flatMap(p -> p.getEntregas().stream())
                .allMatch(e -> e.getEstadoEntrega() == EstadoEntrega.ENTREGADA
                        || e.getEstadoEntrega() == EstadoEntrega.NO_RECIBIDA
                        || e.getEstadoEntrega() == EstadoEntrega.FALLIDA);
    }
}
