package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SolicitudDonacion {
    private LocalDateTime fechaAltas;
    private LocalDateTime fechaLogro;
    private List<Item> items = new ArrayList<>();

    public SolicitudDonacion() {
        this.fechaAltas = LocalDateTime.now();
    }

    public void agregarItem(Item item) {
        items.add(item);
    }

    public List<Donacion> segmentar() {
        return List.of();
    }
}
