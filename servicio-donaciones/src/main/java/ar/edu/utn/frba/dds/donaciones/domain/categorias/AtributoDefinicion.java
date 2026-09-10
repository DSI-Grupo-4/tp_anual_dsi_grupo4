package ar.edu.utn.frba.dds.donaciones.domain.categorias;

import lombok.Getter;
import lombok.Setter;

/**
 * Define un atributo propio de una Subcategoria (ej. "fechaVencimiento" para
 * perecederos, "estadoUso" para mobiliario/vestimenta) sin necesidad de
 * crear una subclase de Java por cada categoria nueva. Diseño original
 * documentado en la nota *4 del DDC de Donaciones (D-002).
 */
@Getter
@Setter
public class AtributoDefinicion {
    private String nombre;
    private TipoDato tipo;
    private boolean obligatorio;

    public AtributoDefinicion(String nombre, TipoDato tipo, boolean obligatorio) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.obligatorio = obligatorio;
    }
}
