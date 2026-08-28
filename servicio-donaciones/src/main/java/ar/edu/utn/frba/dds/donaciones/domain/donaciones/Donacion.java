package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
public class Donacion {

    private static final Map<EstadoTrack, Set<EstadoTrack>> TRANSICIONES_VALIDAS;

    static {
        Map<EstadoTrack, Set<EstadoTrack>> map = new HashMap<>();
        map.put(EstadoTrack.EN_DEPOSITO,
                Set.of(EstadoTrack.ASIGNACION_REALIZADA, EstadoTrack.VENCIDA));
        map.put(EstadoTrack.ASIGNACION_REALIZADA,
                Set.of(EstadoTrack.LISTA_PARA_ENTREGAR));
        map.put(EstadoTrack.LISTA_PARA_ENTREGAR,
                Set.of(EstadoTrack.EN_TRASLADO));
        map.put(EstadoTrack.EN_TRASLADO,
                Set.of(EstadoTrack.ENTREGADA, EstadoTrack.ENTREGA_FALLIDA));
        map.put(EstadoTrack.ENTREGA_FALLIDA,
                Set.of(EstadoTrack.EN_DEPOSITO));
        TRANSICIONES_VALIDAS = Collections.unmodifiableMap(map);
    }

    private Long id;
    private Integer cantidadAsignada;
    private Necesidad necesidadAsignada;
    private ItemDonado itemDonado;
    private LocalDateTime fechaCreacion;
    private EstadoTrack estadoActual;
    private List<CambioEstado> historialEstados;
    private EntidadBeneficiaria entidadBeneficiaria;
    private List<EntidadBeneficiaria> candidatas;

    public Donacion(Long id, ItemDonado itemDonado, Integer cantidadAsignada) {
        this.id = id;
        this.itemDonado = itemDonado;
        this.cantidadAsignada = cantidadAsignada;
        this.fechaCreacion = LocalDateTime.now();
        this.estadoActual = EstadoTrack.EN_DEPOSITO;
        this.historialEstados = new ArrayList<>();
        this.candidatas = new ArrayList<>();
        this.historialEstados.add(new CambioEstado(EstadoTrack.EN_DEPOSITO, "Donación recibida en depósito"));
    }

    public Donacion(Long id, ItemDonado itemDonado, Integer cantidadAsignada,
                    Necesidad necesidadAsignada, EntidadBeneficiaria entidadBeneficiaria) {
        this(id, itemDonado, cantidadAsignada);
        this.necesidadAsignada = necesidadAsignada;
        this.entidadBeneficiaria = entidadBeneficiaria;
    }

    public void cambiarEstado(EstadoTrack nuevoEstado, String justificacion) {
        if (nuevoEstado == EstadoTrack.ENTREGA_FALLIDA
                && (justificacion == null || justificacion.isBlank())) {
            throw new IllegalArgumentException("La justificación es obligatoria para ENTREGA_FALLIDA");
        }

        Set<EstadoTrack> permitidas = TRANSICIONES_VALIDAS.get(estadoActual);
        if (permitidas == null || !permitidas.contains(nuevoEstado)) {
            throw new EstadoInvalidoException(estadoActual, nuevoEstado);
        }

        this.estadoActual = nuevoEstado;
        this.historialEstados.add(new CambioEstado(nuevoEstado, justificacion));
    }

    public boolean estaAsignada() {
        return necesidadAsignada != null;
    }
}
