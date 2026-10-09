package ar.edu.utn.frba.dds.donaciones.domain.necesidades;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.*;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
@Getter
public abstract class Necesidad {
    private String descripcion;
    private Subcategoria subcategoria;
    private UnidadMedida unidadMedida;
    private BigDecimal cantidadRequerida;
    protected BigDecimal cantidadRecibida = BigDecimal.ZERO;
    @Setter private EntidadBeneficiaria entidadBeneficiaria;
    private final Long id;
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
