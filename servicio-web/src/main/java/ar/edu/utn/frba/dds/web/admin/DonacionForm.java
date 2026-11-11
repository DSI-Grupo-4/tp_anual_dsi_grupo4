package ar.edu.utn.frba.dds.web.admin;
import ar.edu.utn.frba.dds.web.client.donaciones.AdminApi;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;
import lombok.Data;
@Data
public class DonacionForm {
    private Long donanteId;
    private String descripcion="";
    @NotEmpty(message="Agregá al menos un ítem.") @Size(max=50,message="Podés cargar hasta 50 ítems por vez.") @Valid
    private List<ItemForm> items=new ArrayList<>(List.of(new ItemForm()));
    @Data public static class ItemForm {
        @NotBlank(message="Describí el ítem.") @Size(max=255) private String descripcion="";
        @NotBlank private String categoria="", subcategoria="", unidadMedida="";
        @NotNull(message="Ingresá la cantidad.") @Positive(message="La cantidad debe ser mayor a cero.") private BigDecimal cantidad;
        @Size(max=500) private String foto="";
        private String condicion="", fechaVencimiento="";
        @Positive private Integer pesoKg, volumenM3, alturaM;
        public AdminApi.Item datos() { return new AdminApi.Item(descripcion,categoria,subcategoria,unidadMedida,cantidad,
            vacio(foto),vacio(condicion),vacio(fechaVencimiento),pesoKg,volumenM3,alturaM); }
        private String vacio(String s) { return s==null||s.isBlank()?null:s; }
    }
    public Object datos(boolean edicion) {
        var datos=new LinkedHashMap<String,Object>(); datos.put("items",items.stream().map(ItemForm::datos).toList());
        if(!edicion) { datos.put("donanteId",donanteId); datos.put("descripcion",descripcion); }
        return datos;
    }
    public static DonacionForm desde(AdminApi.Donacion d) {
        var f=new DonacionForm(); f.donanteId=d.donanteId(); f.descripcion=d.descripcionItem(); f.items.clear();
        for(var i:d.items()) {
            var r=new ItemForm(); r.descripcion=i.descripcion(); r.categoria=i.categoria(); r.subcategoria=i.subcategoria();
            r.unidadMedida=i.unidadMedida(); r.cantidad=i.cantidad(); r.foto=i.foto(); r.condicion=i.condicion();
            r.fechaVencimiento=i.fechaVencimiento(); r.pesoKg=i.pesoKg(); r.volumenM3=i.volumenM3(); r.alturaM=i.alturaM(); f.items.add(r);
        }
        return f;
    }
}
