package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Categoria;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Deposito {
    private static final Deposito INSTANCE = new Deposito();

    private List<Item> items = new ArrayList<>();
    private List<Categoria> categorias = new ArrayList<>();

    private Deposito() {
    }

    public static Deposito getInstance() {
        return INSTANCE;
    }

    public void cargarItem(Item item) {
        items.add(item);
    }

    public void actualizarItem(Item item) {
        int idx = items.indexOf(item);
        if (idx >= 0) {
            items.set(idx, item);
        }
    }
}
