package domain.donaciones;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SolicitudDonacion {
    private String descripcionGeneral;
    private LocalDateTime fechaRegistro;
    private List<ItemDonado> items;

    public SolicitudDonacion(String descripcionGeneral) {
        this.descripcionGeneral = descripcionGeneral;
        this.fechaRegistro = LocalDateTime.now();
        this.items = new ArrayList<>();
    }

    public void agregarItem(ItemDonado item) {
        items.add(item);
    }

    public List<Donacion> segmentar() {
        return items.stream().map(Donacion::new).toList();
    }
}
