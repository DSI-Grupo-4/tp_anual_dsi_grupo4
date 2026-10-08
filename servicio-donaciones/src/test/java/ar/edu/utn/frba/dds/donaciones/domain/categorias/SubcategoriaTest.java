package ar.edu.utn.frba.dds.donaciones.domain.categorias;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.ItemDonado;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SubcategoriaTest {

    @Test
    void unaSubcategoriaNuevaNoTieneAtributosDefinidos() {
        Subcategoria fideos = new Subcategoria("fideos secos");

        assertThat(fideos.getAtributos()).isEmpty();
    }

    @Test
    void agregarAtributoLoSumaALaListaDeAtributosDeLaSubcategoria() {
        // La extensibilidad de categorías se resuelve con atributos
        // dinámicos gestionados por Deposito, no con subclases por tipo.
        Subcategoria fideos = new Subcategoria("fideos secos");
        AtributoDefinicion fechaVencimiento = new AtributoDefinicion("fechaVencimiento", TipoDato.FECHA, true);

        fideos.agregarAtributo(fechaVencimiento);

        assertThat(fideos.getAtributos()).containsExactly(fechaVencimiento);
    }

    @Test
    void unItemDonadoPuedeCargarValoresParaLosAtributosDeSuSubcategoria() {
        Subcategoria vestimenta = new Subcategoria("camperas de abrigo");
        AtributoDefinicion estadoUso = new AtributoDefinicion("estadoUso", TipoDato.TEXTO, true);
        vestimenta.agregarAtributo(estadoUso);

        ItemDonado item = new ItemDonado(null, "Campera de abrigo", vestimenta, 3, null);
        item.agregarValorAtributo(new AtributoValor(estadoUso, "USADO"));

        assertThat(item.getValoresAtributos()).hasSize(1);
        assertThat(item.getValoresAtributos().get(0).getValor()).isEqualTo("USADO");
        assertThat(item.getValoresAtributos().get(0).getDefinicion()).isEqualTo(estadoUso);
    }

    @Test
    void laUnidadDeMedidaEsUnaPropiedadDeLaSubcategoriaNoDelItem() {
        // Antes vivía en ItemDonado; se movió acá porque es una propiedad
        // del tipo de bien, no del ítem individual.
        Subcategoria sillas = new Subcategoria("sillas", new Categoria("mobiliario", null), UnidadMedida.UNIDAD);

        assertThat(sillas.getUnidadMedida()).isEqualTo(UnidadMedida.UNIDAD);
        assertThat(sillas.getCategoria().getNombre()).isEqualTo("mobiliario");
    }
}
