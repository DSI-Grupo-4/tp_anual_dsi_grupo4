package ar.edu.utn.frba.dds.incentivos.progreso;

import ar.edu.utn.frba.dds.incentivos.donacion.DatosDonacion;
import ar.edu.utn.frba.dds.incentivos.misiones.Mision;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
@Entity
@Table(name = "progreso_mision")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ProgresoMision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_progreso_mision")
    private Integer idProgresoMision;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_progreso_categoria", nullable = false)
    private ProgresoCategoria progresoCategoriaAsociado;

    // Posición en ProgresoCategoria.misiones -- asignada por
    // asignarProgresoCategoria(), llamado desde el constructor de
    // ProgresoCategoria.
    @Column(name = "orden", nullable = false)
    private Integer orden;

    @ManyToOne(optional = false, cascade = CascadeType.MERGE)
    @JoinColumn(name = "id_mision", nullable = false)
    private Mision misionAsociada;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id_progreso_insignia", unique = true)
    private ProgresoInsignia insigniaObtenida;

    protected ProgresoMision(Mision misionAsociada) {
        this.misionAsociada = misionAsociada;
    }

    void asignarProgresoCategoria(ProgresoCategoria progresoCategoria, int orden) {
        this.progresoCategoriaAsociado = progresoCategoria;
        this.orden = orden;
    }

    public ProgresoInsignia completarMision() {
        if (insigniaObtenida != null) {
            return null;
        }
        if (misionAsociada.validarCumplimiento(this)) {
            asignarInsignia();
            return insigniaObtenida;
        }
        return null;
    }

    public void asignarInsignia() {
        this.insigniaObtenida = new ProgresoInsignia(misionAsociada.getInsigniaAsociada());
    }

    public abstract void actualizarProgresoMision(DatosDonacion datosDonacion);

    public void evaluarVigencia(LocalDate fechaReferencia) {
        // Por defecto las misiones no tienen vencimiento; sólo la Racha lo redefine.
    }

    public abstract BigDecimal obtenerProgresoActual();

    public abstract BigDecimal distanciaRestante();

    @Getter
    @jakarta.persistence.Entity
    @jakarta.persistence.Table(name = "progreso_racha")
    @NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
    public static class ProgresoRacha extends ProgresoMision {
        @Column(name = "meses_consecutivos_actuales", nullable = false)
        private int mesesConsecutivosActuales;

        @Column(name = "ultima_donacion_registrada")
        private LocalDate ultimaDonacionRegistrada;

        public ProgresoRacha(Mision misionAsociada) {
            super(misionAsociada);
            this.mesesConsecutivosActuales = 0;
        }

        @Override
        public void actualizarProgresoMision(DatosDonacion datosDonacion) {
            LocalDate fecha = datosDonacion.getFecha();
            if (ultimaDonacionRegistrada == null) {
                mesesConsecutivosActuales = 1;
            } else {
                YearMonth mesAnterior = YearMonth.from(ultimaDonacionRegistrada);
                YearMonth mesNuevo = YearMonth.from(fecha);
                if (mesNuevo.equals(mesAnterior)) {
                    // misma donación del mes en curso, no se repite el conteo
                } else if (mesNuevo.equals(mesAnterior.plusMonths(1))) {
                    mesesConsecutivosActuales++;
                } else {
                    mesesConsecutivosActuales = 1;
                }
            }
            ultimaDonacionRegistrada = fecha;
        }

        @Override
        public void evaluarVigencia(LocalDate fechaReferencia) {
            if (ultimaDonacionRegistrada == null) {
                return;
            }
            YearMonth mesUltimaDonacion = YearMonth.from(ultimaDonacionRegistrada);
            YearMonth mesReferencia = YearMonth.from(fechaReferencia);
            if (mesReferencia.minusMonths(1).isAfter(mesUltimaDonacion)) {
                mesesConsecutivosActuales = 0;
            }
        }

        @Override
        public BigDecimal obtenerProgresoActual() {
            return BigDecimal.valueOf(mesesConsecutivosActuales);
        }

        @Override
        public BigDecimal distanciaRestante() {
            Mision.Racha racha = (Mision.Racha) getMisionAsociada();
            return BigDecimal.valueOf(Math.max(0, racha.getMesesRequeridos() - mesesConsecutivosActuales));
        }
    }

    @Getter
    @jakarta.persistence.Entity
    @jakarta.persistence.Table(name = "progreso_completitud")
    @NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
    public static class ProgresoCompletitud extends ProgresoMision {
        @ElementCollection(fetch = jakarta.persistence.FetchType.EAGER)
        @CollectionTable(name = "progreso_completitud_categoria_cubierta",
                joinColumns = @JoinColumn(name = "id_progreso_mision"))
        @Column(name = "categoria_bien", nullable = false, length = 50)
        private Set<String> categoriasCubiertas = new HashSet<>();

        public ProgresoCompletitud(Mision misionAsociada) {
            super(misionAsociada);
        }

        @Override
        public void actualizarProgresoMision(DatosDonacion datosDonacion) {
            if (datosDonacion.getCategoriaBien() != null) {
                categoriasCubiertas.add(datosDonacion.getCategoriaBien());
            }
        }

        @Override
        public BigDecimal obtenerProgresoActual() {
            return BigDecimal.valueOf(categoriasCubiertas.size());
        }

        @Override
        public BigDecimal distanciaRestante() {
            Mision.Completitud completitud = (Mision.Completitud) getMisionAsociada();
            return BigDecimal.valueOf(Math.max(0, completitud.getCategoriasRequeridas() - categoriasCubiertas.size()));
        }
    }

    @Getter
    @jakarta.persistence.Entity
    @jakarta.persistence.Table(name = "progreso_habil_donador")
    @NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
    public static class ProgresoHabilDonador extends ProgresoMision {
        @Column(name = "mejor_donacion_registrada", nullable = false, precision = 12, scale = 3)
        private BigDecimal mejorDonacionRegistrada;

        public ProgresoHabilDonador(Mision misionAsociada) {
            super(misionAsociada);
            this.mejorDonacionRegistrada = BigDecimal.ZERO;
        }

        @Override
        public void actualizarProgresoMision(DatosDonacion datosDonacion) {
            mejorDonacionRegistrada = mejorDonacionRegistrada.max(datosDonacion.getCantidadBienes());
        }

        @Override
        public BigDecimal obtenerProgresoActual() {
            return mejorDonacionRegistrada;
        }

        @Override
        public BigDecimal distanciaRestante() {
            Mision.HabilDonador habilDonador = (Mision.HabilDonador) getMisionAsociada();
            return BigDecimal.valueOf(habilDonador.getCantidadBienesRequerida()).subtract(mejorDonacionRegistrada).max(BigDecimal.ZERO);
        }
    }

    @Getter
    @jakarta.persistence.Entity
    @jakarta.persistence.Table(name = "progreso_donaciones_exitosas")
    @NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
    public static class ProgresoDonacionesExitosas extends ProgresoMision {
        @Column(name = "donaciones_exitosas_actuales", nullable = false)
        private int donacionesExitosasActuales;

        public ProgresoDonacionesExitosas(Mision misionAsociada) {
            super(misionAsociada);
            this.donacionesExitosasActuales = 0;
        }

        @Override
        public void actualizarProgresoMision(DatosDonacion datosDonacion) {
            if (datosDonacion.isDonacionExitosa()) {
                donacionesExitosasActuales++;
            }
        }

        @Override
        public BigDecimal obtenerProgresoActual() {
            return BigDecimal.valueOf(donacionesExitosasActuales);
        }

        @Override
        public BigDecimal distanciaRestante() {
            Mision.DonacionesExitosas donacionesExitosas = (Mision.DonacionesExitosas) getMisionAsociada();
            return BigDecimal.valueOf(Math.max(0, donacionesExitosas.getDonacionesRequeridas() - donacionesExitosasActuales));
        }
    }
}
