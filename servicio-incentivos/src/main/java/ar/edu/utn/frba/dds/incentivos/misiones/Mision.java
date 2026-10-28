package ar.edu.utn.frba.dds.incentivos.misiones;

import ar.edu.utn.frba.dds.incentivos.progreso.ProgresoMision;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
@Entity
@Table(name = "mision")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Mision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mision")
    private Integer idMision;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoriaAsociada;

    // Posición en Categoria.misiones -- la asigna Categoria.agregarMision().
    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "nombre_mision", nullable = false, unique = true, length = 100)
    private String nombreMision;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "id_insignia", nullable = false, unique = true)
    private Insignia insigniaAsociada;

    protected Mision(String nombreMision, Insignia insigniaAsociada) {
        this.nombreMision = nombreMision;
        this.insigniaAsociada = insigniaAsociada;
    }

    void asignarCategoria(Categoria categoria, int orden) {
        this.categoriaAsociada = categoria;
        this.orden = orden;
    }

    public abstract boolean validarCumplimiento(ProgresoMision progresoMision);

    public abstract ProgresoMision crearProgreso();

    @jakarta.persistence.Entity
    @jakarta.persistence.Table(name = "mision_racha")
    @NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
    public static class Racha extends Mision {
        @Column(name = "meses_requeridos", nullable = false)
        private int mesesRequeridos;

        public Racha(String nombreMision, Insignia insigniaAsociada, int mesesRequeridos) {
            super(nombreMision, insigniaAsociada);
            this.mesesRequeridos = mesesRequeridos;
        }

        public int getMesesRequeridos() {
            return mesesRequeridos;
        }

        @Override
        public boolean validarCumplimiento(ProgresoMision progresoMision) {
            ProgresoMision.ProgresoRacha progreso = (ProgresoMision.ProgresoRacha) progresoMision;
            return progreso.getMesesConsecutivosActuales() >= mesesRequeridos;
        }

        @Override
        public ProgresoMision crearProgreso() {
            return new ProgresoMision.ProgresoRacha(this);
        }
    }

    @jakarta.persistence.Entity
    @jakarta.persistence.Table(name = "mision_completitud")
    @NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
    public static class Completitud extends Mision {
        @Column(name = "categorias_requeridas", nullable = false)
        private int categoriasRequeridas;

        public Completitud(String nombreMision, Insignia insigniaAsociada, int categoriasRequeridas) {
            super(nombreMision, insigniaAsociada);
            this.categoriasRequeridas = categoriasRequeridas;
        }

        public int getCategoriasRequeridas() {
            return categoriasRequeridas;
        }

        @Override
        public boolean validarCumplimiento(ProgresoMision progresoMision) {
            ProgresoMision.ProgresoCompletitud progreso = (ProgresoMision.ProgresoCompletitud) progresoMision;
            return progreso.getCategoriasCubiertas().size() >= categoriasRequeridas;
        }

        @Override
        public ProgresoMision crearProgreso() {
            return new ProgresoMision.ProgresoCompletitud(this);
        }
    }

    @jakarta.persistence.Entity
    @jakarta.persistence.Table(name = "mision_habil_donador")
    @NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
    public static class HabilDonador extends Mision {
        @Column(name = "cantidad_bienes_requerida", nullable = false)
        private int cantidadBienesRequerida;

        public HabilDonador(String nombreMision, Insignia insigniaAsociada, int cantidadBienesRequerida) {
            super(nombreMision, insigniaAsociada);
            this.cantidadBienesRequerida = cantidadBienesRequerida;
        }

        public int getCantidadBienesRequerida() {
            return cantidadBienesRequerida;
        }

        @Override
        public boolean validarCumplimiento(ProgresoMision progresoMision) {
            ProgresoMision.ProgresoHabilDonador progreso = (ProgresoMision.ProgresoHabilDonador) progresoMision;
            return progreso.getMejorDonacionRegistrada().compareTo(java.math.BigDecimal.valueOf(cantidadBienesRequerida)) >= 0;
        }

        @Override
        public ProgresoMision crearProgreso() {
            return new ProgresoMision.ProgresoHabilDonador(this);
        }
    }

    @jakarta.persistence.Entity
    @jakarta.persistence.Table(name = "mision_donaciones_exitosas")
    @NoArgsConstructor(access = AccessLevel.PROTECTED) // requerido por JPA
    public static class DonacionesExitosas extends Mision {
        @Column(name = "donaciones_requeridas", nullable = false)
        private int donacionesRequeridas;

        public DonacionesExitosas(String nombreMision, Insignia insigniaAsociada, int donacionesRequeridas) {
            super(nombreMision, insigniaAsociada);
            this.donacionesRequeridas = donacionesRequeridas;
        }

        public int getDonacionesRequeridas() {
            return donacionesRequeridas;
        }

        @Override
        public boolean validarCumplimiento(ProgresoMision progresoMision) {
            ProgresoMision.ProgresoDonacionesExitosas progreso = (ProgresoMision.ProgresoDonacionesExitosas) progresoMision;
            return progreso.getDonacionesExitosasActuales() >= donacionesRequeridas;
        }

        @Override
        public ProgresoMision crearProgreso() {
            return new ProgresoMision.ProgresoDonacionesExitosas(this);
        }
    }
}
