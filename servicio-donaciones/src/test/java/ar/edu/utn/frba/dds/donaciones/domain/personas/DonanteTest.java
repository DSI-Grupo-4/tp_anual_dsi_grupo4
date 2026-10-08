package ar.edu.utn.frba.dds.donaciones.domain.personas;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DonanteTest {

    @Test
    void unDonanteRecienCreadoNoDebeNotificarsePorInactividad() {
        Donante donante = new Donante(1L, new PersonaHumana("Ana", "Perez", 30, "1", null));

        assertThat(donante.debeNotificarsePorInactividad(20)).isFalse();
    }

    @Test
    void unDonanteConMasDe20DiasSinActividadDebeNotificarse() {
        Donante donante = new Donante(1L, new PersonaHumana("Ana", "Perez", 30, "1", null));
        donante.setUltimaActividad(LocalDate.now().minusDays(21));

        assertThat(donante.debeNotificarsePorInactividad(20)).isTrue();
    }

    @Test
    void unaVezNotificadoNoVuelveANotificarseHastaQueHayaActividadNueva() {
        Donante donante = new Donante(1L, new PersonaHumana("Ana", "Perez", 30, "1", null));
        donante.setUltimaActividad(LocalDate.now().minusDays(21));

        donante.marcarNotificadoPorInactividad();
        assertThat(donante.debeNotificarsePorInactividad(20)).isFalse();

        donante.registrarActividad();
        donante.setUltimaActividad(LocalDate.now().minusDays(21));
        assertThat(donante.debeNotificarsePorInactividad(20)).isTrue();
    }

    @Test
    void unDonanteJuridicoAdmiteUnUnicoRepresentante() {
        PersonaJuridica persona = new PersonaJuridica("Arcos Plateados SA", TipoOrganizacion.EMPRESA, null, null);
        Donante donante = new Donante(1L, persona);
        PersonaHumana representante = new PersonaHumana("Juan", "Perez", 40, "12345678", null);

        donante.agregarRepresentante(representante);

        assertThat(persona.getRepresentantes()).containsExactly(representante);
    }

    @Test
    void unDonanteJuridicoRechazaUnSegundoRepresentante() {
        // Cardinalidad 1 para donante (a diferencia de EntidadBeneficiaria).
        PersonaJuridica persona = new PersonaJuridica("Arcos Plateados SA", TipoOrganizacion.EMPRESA, null, null);
        Donante donante = new Donante(1L, persona);
        donante.agregarRepresentante(new PersonaHumana("Juan", "Perez", 40, "12345678", null));

        assertThatThrownBy(() ->
                donante.agregarRepresentante(new PersonaHumana("Ana", "Gomez", 35, "87654321", null)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unDonantePersonaHumanaNoAdmiteRepresentantes() {
        PersonaHumana persona = new PersonaHumana("Ana", "Perez", 30, "11111111", null);
        Donante donante = new Donante(1L, persona);

        assertThatThrownBy(() ->
                donante.agregarRepresentante(new PersonaHumana("Otro", "Repr", 30, "22222222", null)))
                .isInstanceOf(IllegalStateException.class);
    }
}
