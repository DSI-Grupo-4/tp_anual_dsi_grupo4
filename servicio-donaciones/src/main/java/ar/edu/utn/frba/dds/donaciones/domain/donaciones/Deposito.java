package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Categoria;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class Deposito {

    private List<ItemDonado> items;
    // Categorías/subcategorías conocidas por el depósito: permite dar de alta
    // categorías nuevas (con sus atributos dinámicos) sin recompilar el
    // sistema, según la nota de diseño original del DDC (D-002).
    private List<Categoria> categorias;

    public Deposito() {
        this.items = new ArrayList<>();
        this.categorias = new ArrayList<>();
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

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public void agregarCategoria(Categoria categoria) {
        categorias.add(categoria);
    }

    public Optional<Subcategoria> buscarSubcategoria(String nombre) {
        return categorias.stream()
                .flatMap(categoria -> categoria.getSubcategorias().stream())
                .filter(subcategoria -> subcategoria.getNombre().equalsIgnoreCase(nombre))
                .findFirst();
    }
}