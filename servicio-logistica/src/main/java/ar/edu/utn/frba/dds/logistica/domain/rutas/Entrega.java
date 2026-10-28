package ar.edu.utn.frba.dds.logistica.domain.rutas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "entrega", indexes = {
        @Index(name = "idx_entrega_estado_entrega", columnList = "estado_entrega"),
        @Index(name = "idx_entrega_fecha", columnList = "fecha")
})
public class Entrega {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entrega")
    private Integer idEntrega;

    // Referencia lógica al servicio de Donaciones: solo el id, sin FK (Logística no invoca a Donaciones).
    @Column(name = "id_donacion_asociada", nullable = false, unique = true)
    private Integer idDonacionAsociada;

    // Referencia lógica al servicio de Donaciones: solo el id, sin FK.
    @Column(name = "id_entidad_beneficiaria_asociada", nullable = false)
    private Integer idEntidadBeneficiariaAsociada;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "calle", column = @Column(name = "direccion_destino_calle", nullable = false, length = 120)),
            @AttributeOverride(name = "numero", column = @Column(name = "direccion_destino_numero", nullable = false, length = 10)),
            @AttributeOverride(name = "ciudad.nombre", column = @Column(name = "direccion_destino_ciudad", nullable = false, length = 80)),
            @AttributeOverride(name = "ciudad.provincia.nombre", column = @Column(name = "direccion_destino_provincia", nullable = false, length = 80))
    })
    private Direccion direccionDestino;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_entrega", nullable = false)
    private EstadoEntrega estadoEntrega;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "fecha_hora_entrega")
    private java.time.LocalDateTime fechaHoraEntrega;

    @Column(name = "seguimiento_url", length = 500)
    private String seguimientoUrl;

    @OneToMany(mappedBy = "entrega", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FotoEntrega> fotos = new ArrayList<>();

    // Camión que realizó/realiza la entrega. Denormalizado a propósito respecto de Ruta.camionAsociado.
    @ManyToOne
    @JoinColumn(name = "id_camion")
    private Camion camionEntrega;

    // Lado dueño de la relación Parada 1..N Entrega. Null hasta que se asigna a una ruta.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_parada")
    private Parada parada;

    @Column(name = "peso_kg", nullable = false)
    private Integer pesoKG;

    @Column(name = "volumen_m3", nullable = false)
    private Integer volumenM3;

    @Column(name = "altura_m", nullable = false)
    private Integer alturaM;

    @Column(name = "justificacion_fallo", length = 500)
    private String justificacionFallo; // pedido por el enunciado ("Tocamos timbre pero nadie respondió")

    public Entrega(Integer idEntrega, Integer idDonacionAsociada, Integer idEntidadBeneficiariaAsociada,
                   Direccion direccionDestino, LocalDate fecha,
                   Integer pesoKG, Integer volumenM3, Integer alturaM) {
        this.idEntrega = idEntrega;
        this.idDonacionAsociada = idDonacionAsociada;
        this.idEntidadBeneficiariaAsociada = idEntidadBeneficiariaAsociada;
        this.direccionDestino = direccionDestino;
        this.fecha = fecha;
        this.pesoKG = pesoKG;
        this.volumenM3 = volumenM3;
        this.alturaM = alturaM;
        this.estadoEntrega = EstadoEntrega.PENDIENTE;
    }

    public void cambiarEstado(EstadoEntrega estadoNuevo) {
        this.estadoEntrega = estadoNuevo;
    }

    public void asignarARuta(Camion camion) {
        this.camionEntrega = camion;
        cambiarEstado(EstadoEntrega.ASIGNADA_A_RUTA);
    }

    public void iniciarTraslado() {
        cambiarEstado(EstadoEntrega.EN_TRASLADO);
    }

    /**
     * Una parada confirma varias entregas con la misma foto del request: cada entrega
     * guarda su propia copia (una fila de foto_entrega pertenece a una sola entrega).
     */
    public void confirmarEntrega(FotoEntrega foto) {
        if (foto != null) {
            FotoEntrega copia = new FotoEntrega(foto.getUrl(), foto.getFecha() != null ? foto.getFecha() : LocalDate.now());
            copia.setEntrega(this);
            this.fotos.add(copia);
        }
        this.fechaHoraEntrega = java.time.LocalDateTime.now();
        cambiarEstado(EstadoEntrega.ENTREGADA);
    }

    public void marcarNoRecibida(String justificacion) {
        this.justificacionFallo = justificacion;
        cambiarEstado(EstadoEntrega.NO_RECIBIDA);
    }

    /**
     * El chofer o una persona administradora reporta que la entrega no se
     * pudo concretar por un motivo distinto a que la entidad no la haya
     * recibido -- vencimiento de los bienes antes de llegar, incidente
     * logístico en el camino, etc. (ejemplos textuales del enunciado).
     */
    public void marcarFallida(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo de la entrega fallida es obligatorio");
        }
        this.justificacionFallo = motivo;
        cambiarEstado(EstadoEntrega.FALLIDA);
    }

    /**
     * La persona administradora revisó una entrega NO_RECIBIDA/FALLIDA y
     * determinó que se puede reintentar -- "se dejará constancia del estado
     * correspondiente" (texto del enunciado): queda en REPLANIFICABLE, que
     * EntregaRepository.obtenerPendientes() trata igual que PENDIENTE para
     * la próxima corrida de PlanificadorService.planificarRutasDelDia().
     */
    public void reingresarADeposito() {
        if (estadoEntrega != EstadoEntrega.NO_RECIBIDA && estadoEntrega != EstadoEntrega.FALLIDA) {
            throw new IllegalStateException(
                    "La entrega " + idEntrega + " no puede reingresar al depósito estando en estado " + estadoEntrega);
        }
        cambiarEstado(EstadoEntrega.REPLANIFICABLE);
    }
}
