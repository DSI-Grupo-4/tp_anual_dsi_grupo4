package ar.edu.utn.frba.dds.donaciones.domain.donaciones;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
@Component
public class Deposito {
    private final List<ItemDonado> items = new ArrayList<>();
    public void cargarItem(ItemDonado item) { items.add(item); }
    public List<ItemDonado> itemsDisponibles() { return List.copyOf(items); }
    public void reemplazarItems(List<ItemDonado> anteriores, List<ItemDonado> nuevos) {
        items.removeAll(anteriores);
        items.addAll(nuevos);
    }
}
