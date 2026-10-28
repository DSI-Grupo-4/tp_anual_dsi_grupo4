package ar.edu.utn.frba.dds.donaciones.domain.necesidades;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.*;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
@Getter
@Entity
@Table(name = "necesidad")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_necesidad", discriminatorType = DiscriminatorType.STRING)
public abstract class Necesidad {
    @Column(name = "descripcion", nullable = false, length = 255)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "subcategoria", nullable = false)
    private Subcategoria subcategoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false)
    private UnidadMedida unidadMedida;

    @Column(name = "cantidad_requerida", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidadRequerida;

    @Column(name = "cantidad_recibida", nullable = false, precision = 12, scale = 3)
    protected BigDecimal cantidadRecibida = BigDecimal.ZERO;

    @Setter
    @ManyToOne
    @JoinColumn(name = "entidad_beneficiaria_id")
    private EntidadBeneficiaria entidadBeneficiaria;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    protected Necesidad() {
        // requerido por JPA
    }

    public Necesidad(Long id, String descripcion, Subcategoria subcategoria, UnidadMedida unidad, BigDecimal cantidad) {
        this.id = id;
        actualizar(descripcion, subcategoria, unidad, cantidad);
    }
    public void actualizar(String descripcion, Subcategoria subcategoria, UnidadMedida unidad, BigDecimal cantidad) {
        if (descripcion == null || descripcion.isBlank() || subcategoria == null || unidad == null)
            throw new IllegalArgumentException("descripcion, subcategoria y unidadMedida son obligatorias");
        if (cantidad == null || cantidad.signum() <= 0) throw new IllegalArgumentException("cantidadRequerida debe ser mayor a 0");
        if (unidad != UnidadMedida.KILOGRAMO && unidad != UnidadMedida.LITRO && cantidad.stripTrailingZeros().scale() > 0)
            throw new IllegalArgumentException("La cantidad debe ser entera para " + unidad);
        boolean valida = switch (subcategoria) {
            case SILLA, MESA, BANCO, COLCHON, CAMPERA, REMERA, PANTALON, ROPA_INFANTIL, FRAZADA -> unidad == UnidadMedida.UNIDAD;
            case ACEITE_VEGETAL -> unidad == UnidadMedida.LITRO || unidad == UnidadMedida.UNIDAD || unidad == UnidadMedida.CAJA;
            default -> unidad != UnidadMedida.LITRO;
        };
        if (!valida) throw new IllegalArgumentException("Unidad incompatible con " + subcategoria);
        if (cantidadRecibida.signum() > 0 && (this.subcategoria != subcategoria || this.unidadMedida != unidad))
            throw new IllegalStateException("No se puede reclasificar una necesidad con cantidades recibidas");
        this.descripcion = descripcion;
        this.subcategoria = subcategoria;
        this.unidadMedida = unidad;
        this.cantidadRequerida = cantidad;
    }
    public boolean satisfecha() { return cantidadRecibida.compareTo(cantidadRequerida) >= 0; }
    public void recibir(BigDecimal cantidad) {
        if (cantidad == null || cantidad.signum() <= 0) throw new IllegalArgumentException("La cantidad recibida debe ser positiva");
        cantidadRecibida = cantidadRecibida.add(cantidad);
    }
}
