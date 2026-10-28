package ar.edu.utn.frba.dds.donaciones.domain.algoritmos;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.UnidadMedida;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.ItemDonado;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.NecesidadExtraordinaria;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.TipoExtraordinario;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaJuridica;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoOrganizacion;
import org.junit.jupiter.api.Test;
import ar.edu.utn.frba.dds.donaciones.DatosPrueba;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrioridadSubatendidosTest {

    // Las donaciones de este test son siempre de Subcategoria.SILLA/UnidadMedida.UNIDAD
    // (ver DatosPrueba.item); desde que PrioridadSubatendidos exige una necesidad
    // pendiente compatible, las entidades necesitan una para no quedar filtradas.
    private EntidadBeneficiaria entidad(Long id, String nombre) {
        EntidadBeneficiaria entidad = new EntidadBeneficiaria(id,
                new PersonaJuridica(nombre, TipoOrganizacion.ONG, null, null), nombre);
        entidad.agregarNecesidad(new NecesidadExtraordinaria(null, "Necesita sillas",
                Subcategoria.SILLA, UnidadMedida.UNIDAD, BigDecimal.TEN, TipoExtraordinario.INUNDACION));
        return entidad;
    }

    private void registrarEntregaReciente(EntidadBeneficiaria entidad) {
        ItemDonado item = DatosPrueba.item(null, "Item", DatosPrueba.subcategoria("cat"), 1, null);
        Donacion donacion = DatosPrueba.donacion(null, item, 1);
        donacion.cambiarEstado(EstadoTrack.ASIGNACION_REALIZADA, null);
        donacion.cambiarEstado(EstadoTrack.LISTA_PARA_ENTREGAR, null);
        donacion.cambiarEstado(EstadoTrack.EN_TRASLADO, null);
        donacion.cambiarEstado(EstadoTrack.ENTREGADA, null);
        entidad.registrarAyuda(donacion);
    }

    @Test
    void priorizaALaEntidadConMenosDonacionesRecibidasEnElUltimoTrimestre() {
        EntidadBeneficiaria muyAtendida = entidad(1L, "Comedor Muy Atendido");
        registrarEntregaReciente(muyAtendida);
        registrarEntregaReciente(muyAtendida);
        registrarEntregaReciente(muyAtendida);

        EntidadBeneficiaria subatendida = entidad(2L, "Escuela Rural 10");
        // sin donaciones recibidas todavía

        List<EntidadBeneficiaria> resultado = new PrioridadSubatendidos()
                .ejecutarAlgoritmo(DatosPrueba.donacion(null, DatosPrueba.item(null,"Silla",Subcategoria.SILLA,1,null),1), List.of(muyAtendida, subatendida));

        assertThat(resultado.get(0)).isEqualTo(subatendida);
    }

    @Test
    void noConsideraDonacionesQueNoLlegaronAEntregarse() {
        EntidadBeneficiaria conAsignacionSinEntregar = entidad(1L, "Comedor A");
        Donacion sinEntregar = DatosPrueba.donacion(null,
                DatosPrueba.item(null, "Item", DatosPrueba.subcategoria("cat"), 1, null), 1);
        sinEntregar.cambiarEstado(EstadoTrack.ASIGNACION_REALIZADA, null);
        conAsignacionSinEntregar.registrarAyuda(sinEntregar);

        EntidadBeneficiaria sinNada = entidad(2L, "Comedor B");

        // Ninguna tiene entregas efectivas -> deberían quedar empatadas, en
        // el orden original (ambas con 0 donaciones "ENTREGADA" recientes).
        List<EntidadBeneficiaria> resultado = new PrioridadSubatendidos()
                .ejecutarAlgoritmo(DatosPrueba.donacion(null, DatosPrueba.item(null,"Silla",Subcategoria.SILLA,1,null),1), List.of(conAsignacionSinEntregar, sinNada));

        assertThat(resultado).containsExactly(conAsignacionSinEntregar, sinNada);
    }
}
