package ar.edu.utn.frba.dds.donaciones.domain.donaciones;
import ar.edu.utn.frba.dds.donaciones.DatosPrueba;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
class SolicitudDonacionTest {
    @Test void agrupaSinPerderFotosDescripcionesNiOrigen() {
        SolicitudDonacion s = new SolicitudDonacion("Mudanza");
        ItemDonado a = DatosPrueba.item(null,"Sillas rojas",Subcategoria.SILLA,2,"rojas.jpg");
        ItemDonado b = DatosPrueba.item(null,"Sillas azules",Subcategoria.SILLA,3,"azules.jpg");
        s.agregarItem(a); s.agregarItem(b); s.agregarItem(DatosPrueba.item(null,"Mesa",Subcategoria.MESA,1,null));
        List<Donacion> ds = s.segmentar();
        assertThat(ds).hasSize(2);
        assertThat(ds.get(0).getItems()).containsExactly(a,b);
        assertThat(ds.get(0).getCantidadAsignada()).isEqualByComparingTo("5");
        assertThat(ds.get(0).getSolicitudOrigen()).isSameAs(s);
        assertThat(ds.get(0).getPesoKg()).isEqualTo(20);
        assertThat(ds.get(0).getAlturaM()).isEqualTo(1);
    }
    private ItemDonado arroz(UnidadMedida unidad, String cantidad, LocalDate fecha) {
        return new ItemDonado(null,"Arroz",Categoria.ALIMENTOS,Subcategoria.ARROZ,unidad,new BigDecimal(cantidad),null,new Perecedero(fecha),null,2,1,1);
    }
    @Test void separaVencimientosUnidadesYConservaDecimales() {
        SolicitudDonacion s = new SolicitudDonacion("Alimentos");
        LocalDate fecha = LocalDate.now().plusMonths(6);
        s.agregarItem(arroz(UnidadMedida.KILOGRAMO,"2.5",fecha));
        s.agregarItem(arroz(UnidadMedida.KILOGRAMO,"0.75",fecha));
        s.agregarItem(arroz(UnidadMedida.KILOGRAMO,"1",fecha.plusDays(1)));
        s.agregarItem(arroz(UnidadMedida.PAQUETE,"2",fecha));
        List<Donacion> ds = s.segmentar();
        assertThat(ds).hasSize(3);
        assertThat(ds.get(0).getCantidadAsignada()).isEqualByComparingTo("3.25");
    }
    @Test void separaNuevosYUsados() {
        SolicitudDonacion s = new SolicitudDonacion("Muebles");
        s.agregarItem(DatosPrueba.item(null,"Usada",Subcategoria.SILLA,1,null));
        s.agregarItem(new ItemDonado(null,"Nueva",Categoria.MOBILIARIO,Subcategoria.SILLA,UnidadMedida.UNIDAD,BigDecimal.ONE,null,null,new ConEstado(Condicion.NUEVO),1,1,1));
        assertThat(s.segmentar()).hasSize(2);
    }
    @Test void rechazaGrupoHeterogeneoYNoMutaLaDonacion() {
        ItemDonado silla = DatosPrueba.item(null,"Silla",Subcategoria.SILLA,1,null);
        Donacion d = new Donacion(1L,List.of(silla),null);
        assertThatThrownBy(() -> d.reemplazarItems(List.of(silla,DatosPrueba.item(null,"Mesa",Subcategoria.MESA,1,null))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(d.getItems()).containsExactly(silla);
        assertThatThrownBy(() -> d.getItems().clear()).isInstanceOf(UnsupportedOperationException.class);
    }
    @Test void vencidoNoPuedeAsignarse() {
        Donacion d = new Donacion(1L,List.of(arroz(UnidadMedida.KILOGRAMO,"2",LocalDate.now().minusDays(1))),null);
        assertThatThrownBy(() -> d.cambiarEstado(EstadoTrack.ASIGNACION_REALIZADA,null)).isInstanceOf(IllegalStateException.class);
        d.cambiarEstado(EstadoTrack.VENCIDA,null);
    }
}
