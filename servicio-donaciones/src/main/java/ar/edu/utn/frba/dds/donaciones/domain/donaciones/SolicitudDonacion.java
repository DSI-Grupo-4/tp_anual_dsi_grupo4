package ar.edu.utn.frba.dds.donaciones.domain.donaciones;
import ar.edu.utn.frba.dds.donaciones.domain.personas.Donante;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class SolicitudDonacion {
    @Setter private Long id;
    private final String descripcion;
    private final LocalDateTime fechaRegistro = LocalDateTime.now();
    private final List<ItemDonado> items = new ArrayList<>();
    @Setter private Donante donante;
    public SolicitudDonacion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) throw new IllegalArgumentException("descripcion es obligatoria");
        this.descripcion = descripcion;
    }
    public List<ItemDonado> getItems() { return List.copyOf(items); }
    public void agregarItem(ItemDonado item) {
        if (item == null) throw new IllegalArgumentException("El item es obligatorio");
        items.add(item);
    }
    public List<Donacion> segmentar() {
        List<List<ItemDonado>> grupos = new ArrayList<>();
        for (ItemDonado item : items) {
            List<ItemDonado> grupo = grupos.stream().filter(g -> g.get(0).mismaSegmentacion(item))
                    .findFirst().orElseGet(() -> { List<ItemDonado> nuevo = new ArrayList<>(); grupos.add(nuevo); return nuevo; });
            grupo.add(item);
        }
        return grupos.stream().map(grupo -> {
            Donacion donacion = new Donacion(null, grupo, this);
            donacion.setDonante(donante);
            return donacion;
        }).toList();
    }
}
