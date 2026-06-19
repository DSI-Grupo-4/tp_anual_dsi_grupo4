package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.SubCategoria;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Item {
    private String nombre;
    private String descripcion;
    private String foto;
    private SubCategoria demoninadoAsignada;
    private SubCategoria subCategoriaAñadida;
    private int cantidad;
    private String esPerecedero;

    public void agruparItems() {
    }

    public void añadirItem() {
    }

    public void esVencido() {
    }

    public void calcularDescripcion() {
    }
}
