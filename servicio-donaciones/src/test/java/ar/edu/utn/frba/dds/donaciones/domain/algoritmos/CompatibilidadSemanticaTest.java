package ar.edu.utn.frba.dds.donaciones.domain.algoritmos;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.ItemDonado;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.NecesidadRecurrente;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Periodicidad;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaJuridica;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoOrganizacion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CompatibilidadSemanticaTest {

    private EntidadBeneficiaria entidadConNecesidad(Long id, String nombre, String subcategoria) {
        EntidadBeneficiaria entidad = new EntidadBeneficiaria(id,
                new PersonaJuridica(nombre, TipoOrganizacion.ONG, null, null), nombre);
        entidad.agregarNecesidad(new NecesidadRecurrente(id, "Necesidad de " + nombre,
                new Subcategoria(subcategoria), 100, Periodicidad.SEMANAL));
        return entidad;
    }

    @Test
    void favoreceALaEntidadCuyaNecesidadCoincideConLaSubcategoriaDonada() {
        Donacion donacionFideos = new Donacion(1L,
                new ItemDonado(1L, "Fideos", new Subcategoria("fideos secos"), 100, null), 100);

        EntidadBeneficiaria comedor = entidadConNecesidad(1L, "Comedor Sonrisas", "fideos secos");
        EntidadBeneficiaria escuela = entidadConNecesidad(2L, "Escuela Rural 10", "sillas");

        List<EntidadBeneficiaria> resultado = new CompatibilidadSemantica()
                .ejecutarAlgoritmo(donacionFideos, List.of(comedor, escuela));

        assertThat(resultado).containsExactly(comedor);
    }

    @Test
    void noProponeEntidadesSinNecesidadesPendientesDeEsaSubcategoria() {
        Donacion donacionSillas = new Donacion(1L,
                new ItemDonado(1L, "Sillas", new Subcategoria("sillas"), 6, null), 6);

        EntidadBeneficiaria escuela = entidadConNecesidad(1L, "Escuela Rural 10", "sillas");
        escuela.getNecesidades().get(0).recibir(100); // ya satisfecha

        List<EntidadBeneficiaria> resultado = new CompatibilidadSemantica()
                .ejecutarAlgoritmo(donacionSillas, List.of(escuela));

        assertThat(resultado).isEmpty();
    }

    @Test
    void esInsensibleAMayusculasAlComparearSubcategorias() {
        Donacion donacion = new Donacion(1L,
                new ItemDonado(1L, "Sillas", new Subcategoria("SILLAS"), 6, null), 6);

        EntidadBeneficiaria escuela = entidadConNecesidad(1L, "Escuela Rural 10", "sillas");

        List<EntidadBeneficiaria> resultado = new CompatibilidadSemantica()
                .ejecutarAlgoritmo(donacion, List.of(escuela));

        assertThat(resultado).containsExactly(escuela);
    }

    @Test
    void noRompeSiLaDonacionTieneSubcategoriaSinNombre() {
        // Regresión del hallazgo alto: antes esto tiraba NPE en puntaje()
        // en vez de simplemente no matchear con ninguna necesidad.
        Donacion donacionSinSubcategoria = new Donacion(1L,
                new ItemDonado(1L, "Item", new Subcategoria(null), 1, null), 1);

        EntidadBeneficiaria escuela = entidadConNecesidad(1L, "Escuela Rural 10", "sillas");

        List<EntidadBeneficiaria> resultado = new CompatibilidadSemantica()
                .ejecutarAlgoritmo(donacionSinSubcategoria, List.of(escuela));

        assertThat(resultado).isEmpty();
    }
}
