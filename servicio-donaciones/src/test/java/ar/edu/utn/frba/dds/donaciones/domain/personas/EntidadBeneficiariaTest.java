package ar.edu.utn.frba.dds.donaciones.domain.personas;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EntidadBeneficiariaTest {

    @Test
    void unaEntidadBeneficiariaAdmiteMasDeUnRepresentanteSinLimite() {
        // A diferencia de Donante (máx. 1), EntidadBeneficiaria no tiene
        // tope de representantes.
        PersonaJuridica persona = new PersonaJuridica("Comedor Sonrisas", TipoOrganizacion.ONG, null, null);
        EntidadBeneficiaria entidad = new EntidadBeneficiaria(1L, persona, "Comedor infantil");

        entidad.agregarRepresentante(new PersonaHumana("Juan", "Perez", 40, "1", null));
        entidad.agregarRepresentante(new PersonaHumana("Ana", "Gomez", 35, "2", null));
        entidad.agregarRepresentante(new PersonaHumana("Luis", "Diaz", 50, "3", null));

        assertThat(persona.getRepresentantes()).hasSize(3);
    }
}
