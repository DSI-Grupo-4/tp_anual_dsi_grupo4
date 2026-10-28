package ar.edu.utn.frba.dds.logistica.domain.rutas;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
@Entity
@Table(name = "ruta", indexes = {
        @Index(name = "idx_ruta_fecha_estado_ruta", columnList = "fecha, estado_ruta")
})
public class Ruta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ruta")
    private Integer idRuta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_camion", nullable = false)
    private Camion camionAsociado;

    @ManyToOne
    @JoinColumn(name = "id_chofer") // opcional: una ruta puede quedar sin chofer asignado
    private Chofer chofer;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numeroParada ASC")
    private List<Parada> paradas = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_ruta", nullable = false)
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
