package ar.edu.utn.frba.dds.donaciones;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.*;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.*;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.dto.ItemDonadoDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
public final class DatosPrueba {
    public static Subcategoria subcategoria(String nombre) {
        return switch(nombre.toLowerCase()) {
            case "fideos secos", "fideos" -> Subcategoria.FIDEOS_SECOS;
            case "arroz" -> Subcategoria.ARROZ;
            case "sillas", "silla", "a", "cat" -> Subcategoria.SILLA;
            case "mesas", "mesa", "b" -> Subcategoria.MESA;
            case "frazadas" -> Subcategoria.FRAZADA;
            case "camperas de abrigo", "ropa" -> Subcategoria.CAMPERA;
            default -> Subcategoria.valueOf(nombre);
        };
    }
    public static Categoria categoria(Subcategoria sub) {
        return switch(sub) {
            case FIDEOS_SECOS, ARROZ, LEGUMBRES_SECAS, ACEITE_VEGETAL, TOMATE, FRUTA -> Categoria.ALIMENTOS;
            case SILLA, MESA, BANCO, COLCHON -> Categoria.MOBILIARIO;
            default -> Categoria.VESTIMENTA;
        };
    }
    public static ItemDonado item(Long id, String descripcion, Subcategoria sub, int cantidad, String foto) {
        Categoria categoria = categoria(sub);
        return new ItemDonado(id, descripcion, categoria, sub, UnidadMedida.UNIDAD, BigDecimal.valueOf(cantidad), foto,
                categoria == Categoria.ALIMENTOS ? new Perecedero(LocalDate.now().plusYears(1)) : null,
                categoria == Categoria.ALIMENTOS ? null : new ConEstado(Condicion.USADO), 10, 1, 1);
    }
    public static Donacion donacion(Long id, ItemDonado item, int cantidad) {
        return new Donacion(id, List.of(item), null);
    }
    public static Donacion donacion(Long id, ItemDonado item, int cantidad, Necesidad necesidad, EntidadBeneficiaria entidad) {
        Donacion d = donacion(id,item,cantidad); d.setNecesidadAsignada(necesidad); d.setEntidadBeneficiaria(entidad); return d;
    }
    public static void completar(ItemDonadoDTO dto) {
        dto.setCategoria(categoria(dto.getSubcategoria())); dto.setUnidadMedida(UnidadMedida.UNIDAD);
        if (dto.getCategoria() == Categoria.ALIMENTOS) dto.setFechaVencimiento(LocalDate.now().plusYears(1));
        else dto.setCondicion(Condicion.USADO);
        dto.setPesoKg(10); dto.setVolumenM3(1); dto.setAlturaM(1);
    }
    public static ItemDonadoDTO dto(String descripcion, Subcategoria sub, int cantidad) {
        ItemDonadoDTO d = new ItemDonadoDTO(); d.setDescripcion(descripcion); d.setSubcategoria(sub);
        d.setCantidad(BigDecimal.valueOf(cantidad)); completar(d); return d;
    }
}
