package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;
import ar.edu.utn.frba.dds.donaciones.domain.personas.Donante;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.*;
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

    private Necesidad necesidadAsignada;
    @Setter(lombok.AccessLevel.NONE)
    private List<ItemDonado> items;
    @Setter(lombok.AccessLevel.NONE)
    private SolicitudDonacion solicitudOrigen;
    private LocalDateTime fechaCreacion;
    private EstadoTrack estadoActual;
    private List<CambioEstado> historialEstados;
    private EntidadBeneficiaria entidadBeneficiaria;
    private List<EntidadBeneficiaria> candidatas;
    // Quién hizo la donación — antes no se registraba en ningún lado.
    private Donante donante;

    public Donacion(Long id, List<ItemDonado> items, SolicitudDonacion solicitudOrigen) {
        validarItems(items);
        this.id = id;
        this.items = List.copyOf(items);
        this.solicitudOrigen = solicitudOrigen;
        this.fechaCreacion = LocalDateTime.now();
        this.estadoActual = EstadoTrack.EN_DEPOSITO;
        this.historialEstados = new ArrayList<>();
        this.candidatas = new ArrayList<>();
        this.historialEstados.add(new CambioEstado(EstadoTrack.EN_DEPOSITO, "Donación recibida en depósito"));
    }
    private static void validarItems(List<ItemDonado> items) {
        if (items == null || items.isEmpty() || items.stream().anyMatch(Objects::isNull))
            throw new IllegalArgumentException("La donación debe contener al menos un item");
        if (items.stream().anyMatch(i -> !items.get(0).mismaSegmentacion(i)))
            throw new IllegalArgumentException("Los items deben tener la misma subcategoria, unidad, condicion y vencimiento");
    }
    public void reemplazarItems(List<ItemDonado> nuevos) {
        if (estadoActual != EstadoTrack.EN_DEPOSITO)
            throw new IllegalStateException("Solo pueden editarse los items de una donación en depósito");
        validarItems(nuevos);
        this.items = List.copyOf(nuevos);
    }
    public BigDecimal getCantidadAsignada() {
        return items.stream().map(ItemDonado::getCantidad).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public Categoria getCategoria() { return items.get(0).getCategoria(); }
    public Subcategoria getSubcategoria() { return items.get(0).getSubcategoria(); }
    public UnidadMedida getUnidadMedida() { return items.get(0).getUnidadMedida(); }
    public boolean estaVencida() { return items.stream().anyMatch(ItemDonado::estaVencido); }
    // Peso y volumen totales del grupo; altura máxima. No se multiplica por cantidad.
    public Integer getPesoKg() { return sumarDimension(true); }
    public Integer getVolumenM3() { return sumarDimension(false); }
    private Integer sumarDimension(boolean peso) {
        if (items.stream().anyMatch(i -> (peso ? i.getPesoKg() : i.getVolumenM3()) == null)) return null;
        return items.stream().map(i -> peso ? i.getPesoKg() : i.getVolumenM3()).reduce(0, Math::addExact);
    }
    public Integer getAlturaM() {
        if (items.stream().anyMatch(i -> i.getAlturaM() == null)) return null;
        return items.stream().mapToInt(ItemDonado::getAlturaM).max().orElse(0);
    }

    public void cambiarEstado(EstadoTrack nuevoEstado, String justificacion) {
        if (nuevoEstado == null) throw new IllegalArgumentException("nuevoEstado es obligatorio");
        if (estaVencida() && nuevoEstado != EstadoTrack.VENCIDA && nuevoEstado != EstadoTrack.ENTREGA_FALLIDA)
            throw new IllegalStateException("La donación contiene bienes vencidos");
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
