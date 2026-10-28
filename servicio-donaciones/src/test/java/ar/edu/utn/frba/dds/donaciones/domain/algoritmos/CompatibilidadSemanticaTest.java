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
import ar.edu.utn.frba.dds.donaciones.DatosPrueba;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.UnidadMedida;
import java.math.BigDecimal;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CompatibilidadSemanticaTest {

    private EntidadBeneficiaria entidadConNecesidad(Long id, String nombre, String subcategoria) {
        EntidadBeneficiaria entidad = new EntidadBeneficiaria(id,
                new PersonaJuridica(nombre, TipoOrganizacion.ONG, null, null), nombre);
        entidad.agregarNecesidad(new NecesidadRecurrente(id, "Necesidad de " + nombre,
                DatosPrueba.subcategoria(subcategoria), UnidadMedida.UNIDAD, BigDecimal.valueOf(100), Periodicidad.SEMANAL));
        return entidad;
    }

    @Test
    void favoreceALaEntidadCuyaNecesidadCoincideConLaSubcategoriaDonada() {
        Donacion donacionFideos = DatosPrueba.donacion(1L,
                DatosPrueba.item(1L, "Fideos", DatosPrueba.subcategoria("fideos secos"), 100, null), 100);

        EntidadBeneficiaria comedor = entidadConNecesidad(1L, "Comedor Sonrisas", "fideos secos");
        EntidadBeneficiaria escuela = entidadConNecesidad(2L, "Escuela Rural 10", "sillas");

        List<EntidadBeneficiaria> resultado = new CompatibilidadSemantica()
                .ejecutarAlgoritmo(donacionFideos, List.of(comedor, escuela));

        assertThat(resultado).containsExactly(comedor);
    }

    @Test
    void noProponeEntidadesSinNecesidadesPendientesDeEsaSubcategoria() {
        Donacion donacionSillas = DatosPrueba.donacion(1L,
                DatosPrueba.item(1L, "Sillas", DatosPrueba.subcategoria("sillas"), 6, null), 6);

        EntidadBeneficiaria escuela = entidadConNecesidad(1L, "Escuela Rural 10", "sillas");
        escuela.getNecesidades().get(0).recibir(BigDecimal.valueOf(100)); // ya satisfecha

        List<EntidadBeneficiaria> resultado = new CompatibilidadSemantica()
                .ejecutarAlgoritmo(donacionSillas, List.of(escuela));

        assertThat(resultado).isEmpty();
    }

    @Test
    void coincidenLasMismasConstantesDeSubcategoria() {
        Donacion donacion = DatosPrueba.donacion(1L,
                DatosPrueba.item(1L, "Sillas", DatosPrueba.subcategoria("SILLAS"), 6, null), 6);

        EntidadBeneficiaria escuela = entidadConNecesidad(1L, "Escuela Rural 10", "sillas");

        List<EntidadBeneficiaria> resultado = new CompatibilidadSemantica()
                .ejecutarAlgoritmo(donacion, List.of(escuela));

        assertThat(resultado).containsExactly(escuela);
    }

    @Test
    void noCoincideLaMismaSubcategoriaEnDistintaUnidad() {
        Donacion donacion = DatosPrueba.donacion(1L, DatosPrueba.item(1L, "Fideos", Subcategoria.FIDEOS_SECOS, 10, null), 10);
        EntidadBeneficiaria entidad = entidadConNecesidad(1L, "Comedor", "fideos secos");
        entidad.getNecesidades().get(0).actualizar("Fideos por kg", Subcategoria.FIDEOS_SECOS, UnidadMedida.KILOGRAMO, BigDecimal.TEN);
        assertThat(new CompatibilidadSemantica().ejecutarAlgoritmo(donacion, List.of(entidad))).isEmpty();
    }
}
