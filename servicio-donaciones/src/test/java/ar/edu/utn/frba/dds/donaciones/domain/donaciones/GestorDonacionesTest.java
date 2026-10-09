package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.NecesidadRecurrente;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Periodicidad;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaJuridica;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoOrganizacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ar.edu.utn.frba.dds.donaciones.DatosPrueba;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.UnidadMedida;
import java.math.BigDecimal;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GestorDonacionesTest {

    private GestorDonaciones gestor;

    @BeforeEach
    void setUp() {
        gestor = new GestorDonaciones();
    }

    private Donacion nuevaDonacion(Long id, String subcategoria) {
        return DatosPrueba.donacion(id, DatosPrueba.item(null, subcategoria, DatosPrueba.subcategoria(subcategoria), 1, null), 1);
    }

    @Test
    void registrarDonacionAsignaIdCuandoNoTiene() {
        Donacion registrada = gestor.registrarDonacion(nuevaDonacion(null, "frazadas"));

        assertThat(registrada.getId()).isNotNull();
        assertThat(gestor.getDonaciones()).containsExactly(registrada);
    }

    @Test
    void registrarDonacionNoPisaUnIdYaAsignado() {
        Donacion registrada = gestor.registrarDonacion(nuevaDonacion(99L, "frazadas"));

        assertThat(registrada.getId()).isEqualTo(99L);
    }

    @Test
    void idsSecuencialesNoColisionanEntreVariasDonaciones() {
        Donacion d1 = gestor.registrarDonacion(nuevaDonacion(null, "a"));
        Donacion d2 = gestor.registrarDonacion(nuevaDonacion(null, "b"));

        assertThat(d1.getId()).isNotEqualTo(d2.getId());
    }

    @Test
    void buscarPorIdLanzaExcepcionSiNoExiste() {
        assertThatThrownBy(() -> gestor.buscarPorId(404L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void eliminarSacaLaDonacionDeLaLista() {
        Donacion donacion = gestor.registrarDonacion(nuevaDonacion(null, "frazadas"));

        gestor.eliminar(donacion.getId());

        assertThat(gestor.getDonaciones()).isEmpty();
    }

    @Test
    void ejecutarMatchmakingIntersectaAmbosAlgoritmos() {
        Donacion donacion = DatosPrueba.donacion(1L,
                DatosPrueba.item(1L, "Fideos", DatosPrueba.subcategoria("fideos secos"), 100, null), 100);

        EntidadBeneficiaria conNecesidad = new EntidadBeneficiaria(1L,
                new PersonaJuridica("Comedor Sonrisas", TipoOrganizacion.ONG, null, null),
                "Comedor Sonrisas");
        conNecesidad.agregarNecesidad(new NecesidadRecurrente(1L, "Fideos semanales",
                DatosPrueba.subcategoria("fideos secos"), UnidadMedida.UNIDAD, BigDecimal.valueOf(100), Periodicidad.SEMANAL));

        EntidadBeneficiaria sinNecesidad = new EntidadBeneficiaria(2L,
                new PersonaJuridica("Escuela Rural 10", TipoOrganizacion.GUBERNAMENTAL, null, null),
                "Escuela Rural 10");

        ResultadoMatchmaking resultado = gestor.ejecutarMatchmaking(
                donacion, List.of(conNecesidad, sinNecesidad));

        // PrioridadSubatendidos no filtra (ordena todas por menos atendidas),
        // así que la intersección queda determinada por CompatibilidadSemantica.
        assertThat(resultado.getPorCompatibilidad()).containsExactly(conNecesidad);
        assertThat(resultado.getInterseccion()).containsExactly(conNecesidad);
    }
}
