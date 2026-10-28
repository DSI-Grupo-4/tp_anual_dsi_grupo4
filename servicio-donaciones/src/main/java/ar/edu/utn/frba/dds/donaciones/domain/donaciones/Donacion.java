package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;
import ar.edu.utn.frba.dds.donaciones.domain.personas.Donante;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.*;
import java.util.*;
import java.util.stream.Stream;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
@Entity
@Table(name = "donacion", indexes = {
        @Index(name = "idx_donacion_estado_actual", columnList = "estado_actual"),
        @Index(name = "idx_donacion_entidad_beneficiaria", columnList = "entidad_beneficiaria_id")
})
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "necesidad_asignada_id")
    private Necesidad necesidadAsignada;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "donacion", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ItemDonado> items;

    // La colección de Hibernate tiene que ser mutable para que JPA la
    // administre -- el getter público sigue siendo una vista inmutable,
    // como antes (ver SolicitudDonacionTest.rechazaGrupoHeterogeneoYNoMutaLaDonacion).
    public List<ItemDonado> getItems() {
        return List.copyOf(items);
    }

    @Setter(AccessLevel.NONE)
    @ManyToOne(optional = false)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private SolicitudDonacion solicitudOrigen;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_actual", nullable = false)
    private EstadoTrack estadoActual;

    @OneToMany(mappedBy = "donacion", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    private List<CambioEstado> historialEstados;

    @ManyToOne
    @JoinColumn(name = "entidad_beneficiaria_id")
    private EntidadBeneficiaria entidadBeneficiaria;

    // Resultado de la última corrida de matchmaking (cron nocturno o GET
    // /{id}/candidatas a demanda) -- null significa "todavía no se corrió".
    // Las 3 listas se conservan tal cual, sin colapsar, porque la elección
    // de la entidad final debe poder hacerse "a partir del resultado de
    // ejecución de los algoritmos" (ambas corridas si no hubo intersección).
    // Se persiste como filas sueltas (CandidataMatchmaking, una por lista +
    // posición) y se reconstruye a un ResultadoMatchmaking en memoria.
    @OneToMany(mappedBy = "donacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CandidataMatchmaking> candidatasMatchmaking = new ArrayList<>();

    @Column(name = "matchmaking_ejecutado_en")
    private LocalDateTime matchmakingEjecutadoEn;

    // Quién hizo la donación — antes no se registraba en ningún lado.
    @ManyToOne
    @JoinColumn(name = "donante_id", nullable = false)
    private Donante donante;

    public Donacion(Long id, List<ItemDonado> items, SolicitudDonacion solicitudOrigen) {
        validarItems(items);
        this.id = id;
        this.items = new ArrayList<>();
        items.forEach(this::agregarItemInterno);
        this.solicitudOrigen = solicitudOrigen;
        this.fechaCreacion = LocalDateTime.now();
        this.estadoActual = EstadoTrack.EN_DEPOSITO;
        this.historialEstados = new ArrayList<>();
        agregarCambioEstado(EstadoTrack.EN_DEPOSITO, "Donación recibida en depósito");
    }

    private void agregarItemInterno(ItemDonado item) {
        item.asignarDonacion(this);
        this.items.add(item);
    }

    private void agregarCambioEstado(EstadoTrack estado, String justificacion) {
        CambioEstado cambio = new CambioEstado(estado, justificacion);
        cambio.asignarDonacion(this);
        this.historialEstados.add(cambio);
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
        // Los items viejos quedan huérfanos (donacion_id = null) en vez de
        // borrarse: siguen existiendo como registro de lo que trajo la
        // SolicitudDonacion original.
        this.items.forEach(item -> item.asignarDonacion(null));
        this.items = new ArrayList<>();
        nuevos.forEach(this::agregarItemInterno);
    }
    public BigDecimal getCantidadAsignada() {
        return items.stream().map(ItemDonado::getCantidad).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    @Transient
    public Categoria getCategoria() { return items.get(0).getCategoria(); }
    @Transient
    public Subcategoria getSubcategoria() { return items.get(0).getSubcategoria(); }
    @Transient
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
        agregarCambioEstado(nuevoEstado, justificacion);
    }

    public boolean estaAsignada() {
        return necesidadAsignada != null;
    }

    public void setResultadoMatchmaking(ResultadoMatchmaking resultado) {
        this.candidatasMatchmaking.clear();
        agregarLista(resultado.getPorCompatibilidad(), ListaMatchmaking.COMPATIBILIDAD);
        agregarLista(resultado.getPorSubatencion(), ListaMatchmaking.SUBATENCION);
        agregarLista(resultado.getInterseccion(), ListaMatchmaking.INTERSECCION);
        this.matchmakingEjecutadoEn = LocalDateTime.now();
    }

    private void agregarLista(List<EntidadBeneficiaria> entidades, ListaMatchmaking lista) {
        for (int i = 0; i < entidades.size(); i++) {
            CandidataMatchmaking candidata = new CandidataMatchmaking(this, lista, i, entidades.get(i));
            this.candidatasMatchmaking.add(candidata);
        }
    }

    @Transient
    public ResultadoMatchmaking getResultadoMatchmaking() {
        if (candidatasMatchmaking.isEmpty()) return null;
        return new ResultadoMatchmaking(
                candidatasDeLista(ListaMatchmaking.COMPATIBILIDAD),
                candidatasDeLista(ListaMatchmaking.SUBATENCION),
                candidatasDeLista(ListaMatchmaking.INTERSECCION));
    }

    private List<EntidadBeneficiaria> candidatasDeLista(ListaMatchmaking lista) {
        return candidatasMatchmaking.stream()
                .filter(c -> c.getLista() == lista)
                .sorted(Comparator.comparingInt(CandidataMatchmaking::getPosicion))
                .map(CandidataMatchmaking::getEntidadBeneficiaria)
                .toList();
    }

    /** Unión sin duplicados de las 3 listas del último matchmaking corrido; vacía si todavía no se corrió. */
    public List<EntidadBeneficiaria> candidatasPropuestas() {
        ResultadoMatchmaking resultado = getResultadoMatchmaking();
        if (resultado == null) return List.of();
        return Stream.of(resultado.getPorCompatibilidad(),
                        resultado.getPorSubatencion(),
                        resultado.getInterseccion())
                .flatMap(List::stream)
                .distinct()
                .toList();
    }
}
