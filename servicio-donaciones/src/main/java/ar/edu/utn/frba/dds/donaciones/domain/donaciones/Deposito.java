package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import java.util.ArrayList;
import java.util.List;

public class Deposito {

    private List<ItemDonado> items;

    public Deposito() {
        this.items = new ArrayList<>();
    }

    public void cargarItem(ItemDonado item) {
        items.add(item);
    }

    public List<ItemDonado> itemsDisponibles() {
        return items;
    }

    public void eliminarSinStock() {
        items.removeIf(ItemDonado::sinStock);
    }
}