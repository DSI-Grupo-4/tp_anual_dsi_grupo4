package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.*;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/** Un renglón homogéneo: la cantidad y las dimensiones corresponden al lote completo. */
@Getter
public class ItemDonado {
    private final Long id;
    private final String descripcion;
    private final String foto;
    private final Categoria categoria;
    private final Subcategoria subcategoria;
    private final UnidadMedida unidadMedida;
    private final BigDecimal cantidad;
    private final Perecedero perecedero;
    private final ConEstado conEstado;
    private final Integer pesoKg;
    private final Integer volumenM3;
    private final Integer alturaM;

    public ItemDonado(Long id, String descripcion, Categoria categoria, Subcategoria subcategoria,
                      UnidadMedida unidadMedida, BigDecimal cantidad, String foto,
                      Perecedero perecedero, ConEstado conEstado,
                      Integer pesoKg, Integer volumenM3, Integer alturaM) {
        validar(descripcion, categoria, subcategoria, unidadMedida, cantidad, perecedero, conEstado);
        validarDimension(pesoKg, "pesoKg");
        validarDimension(volumenM3, "volumenM3");
        validarDimension(alturaM, "alturaM");
        this.id = id;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.subcategoria = subcategoria;
        this.unidadMedida = unidadMedida;
        this.cantidad = cantidad;
        this.foto = foto;
        this.perecedero = perecedero;
        this.conEstado = conEstado;
        this.pesoKg = pesoKg;
        this.volumenM3 = volumenM3;
        this.alturaM = alturaM;
    }

    private static void validar(String descripcion, Categoria categoria, Subcategoria subcategoria,
                                UnidadMedida unidad, BigDecimal cantidad, Perecedero perecedero,
                                ConEstado conEstado) {
        if (descripcion == null || descripcion.isBlank()) throw new IllegalArgumentException("descripcion es obligatoria");
        if (categoria == null || subcategoria == null || unidad == null)
            throw new IllegalArgumentException("categoria, subcategoria y unidadMedida son obligatorias");
        Categoria esperada = switch (subcategoria) {
            case SILLA, MESA, BANCO, COLCHON -> Categoria.MOBILIARIO;
            case FIDEOS_SECOS, ARROZ, LEGUMBRES_SECAS, ACEITE_VEGETAL, TOMATE, FRUTA -> Categoria.ALIMENTOS;
            case CAMPERA, REMERA, PANTALON, ROPA_INFANTIL, FRAZADA -> Categoria.VESTIMENTA;
        };
        if (categoria != esperada) throw new IllegalArgumentException(subcategoria + " pertenece a " + esperada);
        if (cantidad == null || cantidad.signum() <= 0) throw new IllegalArgumentException("cantidad debe ser mayor a 0");
        if (unidad != UnidadMedida.KILOGRAMO && unidad != UnidadMedida.LITRO && cantidad.stripTrailingZeros().scale() > 0)
            throw new IllegalArgumentException("La cantidad debe ser entera para " + unidad);
        boolean unidadValida = switch (subcategoria) {
            case SILLA, MESA, BANCO, COLCHON, CAMPERA, REMERA, PANTALON, ROPA_INFANTIL, FRAZADA -> unidad == UnidadMedida.UNIDAD;
            case ACEITE_VEGETAL -> unidad == UnidadMedida.LITRO || unidad == UnidadMedida.UNIDAD || unidad == UnidadMedida.CAJA;
            default -> unidad != UnidadMedida.LITRO;
        };
        if (!unidadValida) throw new IllegalArgumentException("Unidad incompatible con " + subcategoria);
        if (categoria == Categoria.ALIMENTOS) {
            // Catálogo inicial: todos los alimentos admitidos se registran con vencimiento.
            if (perecedero == null) throw new IllegalArgumentException("Los alimentos requieren fechaVencimiento");
            if (conEstado != null) throw new IllegalArgumentException("Los alimentos no admiten condicion de uso");
        } else {
            if (conEstado == null) throw new IllegalArgumentException("Mobiliario y vestimenta requieren condicion");
            if (perecedero != null) throw new IllegalArgumentException("El vencimiento solo corresponde a alimentos del catálogo");
        }
    }

    private static void validarDimension(Integer valor, String nombre) {
        if (valor != null && valor <= 0) throw new IllegalArgumentException(nombre + " debe ser mayor a 0");
    }

    public boolean mismaSegmentacion(ItemDonado otro) {
        return categoria == otro.categoria && subcategoria == otro.subcategoria
                && unidadMedida == otro.unidadMedida
                && Objects.equals(condicion(), otro.condicion())
                && Objects.equals(vencimiento(), otro.vencimiento());
    }
    public Condicion condicion() { return conEstado == null ? null : conEstado.getCondicion(); }
    public LocalDate vencimiento() { return perecedero == null ? null : perecedero.getFechaVencimiento(); }
    public boolean estaVencido() { return perecedero != null && perecedero.estaVencido(); }
}
