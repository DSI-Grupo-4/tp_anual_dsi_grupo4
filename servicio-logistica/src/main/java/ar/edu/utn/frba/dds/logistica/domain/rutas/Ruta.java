package ar.edu.utn.frba.dds.logistica.domain.rutas;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // lo usa el ODM para instanciar al leer
@Document(collection = "rutas")
public class Ruta {

    // Id numérico: lo asigna GeneradorIds al guardar si es null.
    @Id
    private Integer idRuta;

    // Referencia a "camiones" (el estado del camión se actualiza en su propio documento).
    @DBRef
    private Camion camionAsociado;

    // Opcional: una ruta puede quedar sin chofer asignado.
    @DBRef
    private Chofer chofer;

    @Indexed
    private LocalDate fecha;

    // Paradas embebidas, en orden de visita: la ruta se lee con una sola consulta.
    private List<Parada> paradas = new ArrayList<>();

    @Indexed
    private EstadoRuta estadoRuta;

    public Ruta(Integer idRuta, Camion camionAsociado, Chofer chofer,
                LocalDate fecha, List<Parada> paradas) {
        this.idRuta = idRuta;
        this.camionAsociado = camionAsociado;
        this.chofer = chofer;
        this.fecha = fecha;
        this.paradas = new ArrayList<>(paradas);
        this.paradas.forEach(p -> p.setRuta(this));
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
