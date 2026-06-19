package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.SubCategoria;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Donacion {
    private SubCategoria subcategoria;
    private int cantidadAportada;
    private Fecha fechaTimestamp;
    private List<Item> itemsAsignados = new ArrayList<>();
}
