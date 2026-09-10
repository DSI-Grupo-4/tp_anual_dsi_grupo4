package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.AtributoDefinicion;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.AtributoValor;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.TipoDato;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SolicitudDonacionTest {

    @Test
    void segmentarGeneraUnaDonacionPorCadaItemCargado() {
        SolicitudDonacion solicitud = new SolicitudDonacion("Mudanza oficina Arcos Plateados");
        ItemDonado sillas = new ItemDonado(null, "Sillas de oficina", new Subcategoria("sillas"), 6, null);
        ItemDonado mesa = new ItemDonado(null, "Mesa rectangular", new Subcategoria("mesas"), 1, null);
        solicitud.agregarItem(sillas);
        solicitud.agregarItem(mesa);

        List<Donacion> generadas = solicitud.segmentar();

        assertThat(generadas).hasSize(2);
        assertThat(generadas).extracting(Donacion::getItemDonado).containsExactly(sillas, mesa);
    }

    @Test
    void cadaDonacionSegmentadaQuedaAsociadaAUnaUnicaSubcategoria() {
        SolicitudDonacion solicitud = new SolicitudDonacion("Donacion de alimentos");
        Subcategoria fideos = new Subcategoria("fideos secos");
        solicitud.agregarItem(new ItemDonado(null, "Fideos secos", fideos, 100, null));

        List<Donacion> generadas = solicitud.segmentar();

        assertThat(generadas).singleElement()
                .extracting(d -> d.getItemDonado().getSubcategoria().getNombre())
                .isEqualTo("fideos secos");
    }

    @Test
    void perecederosConVencimientosDistintosQuedanEnDonacionesSeparadas() {
        // Consigna (Entrega 1): "el sistema podrá generar donaciones
        // separadas cuando existan diferencias en la fecha de vencimiento".
        SolicitudDonacion solicitud = new SolicitudDonacion("Donacion planta industrial");
        Subcategoria fideos = new Subcategoria("fideos secos");
        AtributoDefinicion fechaVencimiento = new AtributoDefinicion("fechaVencimiento", TipoDato.FECHA, true);

        ItemDonado lote1 = new ItemDonado(null, "Fideos lote 1", fideos, 50, null);
        lote1.agregarValorAtributo(new AtributoValor(fechaVencimiento, "2027-01-01"));
        ItemDonado lote2 = new ItemDonado(null, "Fideos lote 2", fideos, 50, null);
        lote2.agregarValorAtributo(new AtributoValor(fechaVencimiento, "2027-06-01"));

        solicitud.agregarItem(lote1);
        solicitud.agregarItem(lote2);

        List<Donacion> generadas = solicitud.segmentar();

        assertThat(generadas).hasSize(2);
        assertThat(generadas).extracting(Donacion::getItemDonado).containsExactly(lote1, lote2);
    }

    @Test
    void segmentarSinItemsDevuelveListaVacia() {
        SolicitudDonacion solicitud = new SolicitudDonacion("Carga vacia");

        assertThat(solicitud.segmentar()).isEmpty();
    }

    @Test
    void lasDonacionesGeneradasNoTienenIdAsignadoTodavia() {
        // El id lo asigna GestorDonaciones.registrarDonacion() al agregarlas
        // a la lista real, no la segmentación en sí (ver GestorDonacionesTest).
        SolicitudDonacion solicitud = new SolicitudDonacion("Donacion simple");
        solicitud.agregarItem(new ItemDonado(null, "Frazadas", new Subcategoria("frazadas"), 10, null));

        List<Donacion> generadas = solicitud.segmentar();

        assertThat(generadas).singleElement().extracting(Donacion::getId).isNull();
    }
}
