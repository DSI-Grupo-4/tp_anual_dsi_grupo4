package ar.edu.utn.frba.dds.donaciones.domain.categorias;

import lombok.Getter;
import lombok.Setter;

/**
 * Valor concreto que un ItemDonado asigna a un AtributoDefinicion de su
 * Subcategoria (ej. definicion="fechaVencimiento", valor="2027-01-01").
 * El valor se guarda como texto y se interpreta según el TipoDato de la
 * definición asociada.
 */
@Getter
@Setter
public class AtributoValor {
    private AtributoDefinicion definicion;
    private String valor;

    public AtributoValor(AtributoDefinicion definicion, String valor) {
        this.definicion = definicion;
        this.valor = valor;
    }
}
