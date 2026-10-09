package ar.edu.utn.frba.dds.incentivos.misiones;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GestorMisiones es un singleton manual (así se concibió el servicio, no se
 * migra a bean de Spring acá) -- estos tests leen el catálogo ya sembrado,
 * no mutan nada, así que no hay problema de estado compartido entre tests.
 */
class GestorMisionesTest {

    private final GestorMisiones gestor = GestorMisiones.getInstance();

    @Test
    void hayExactamenteTresCategoriasEnElOrdenEsperado() {
        assertThat(gestor.cantCategorias()).isEqualTo(3);
        assertThat(gestor.siguienteCategoria(0).getNombre()).isEqualTo("Colaborador");
        assertThat(gestor.siguienteCategoria(1).getNombre()).isEqualTo("Sostenedor");
        assertThat(gestor.siguienteCategoria(2).getNombre()).isEqualTo("Transformador");
    }

    @Test
    void cadaCategoriaTieneExactamenteCincoMisiones() {
        for (int i = 0; i < gestor.cantCategorias(); i++) {
            Categoria categoria = gestor.siguienteCategoria(i);
            assertThat(categoria.cantMisiones())
                    .as("cantidad de misiones de %s", categoria.getNombre())
                    .isEqualTo(5);
        }
    }

    @Test
    void cadaCategoriaUsaSoloLosCuatroTiposDeMisionDelEnunciado() {
        for (int i = 0; i < gestor.cantCategorias(); i++) {
            Categoria categoria = gestor.siguienteCategoria(i);
            for (int j = 0; j < categoria.cantMisiones(); j++) {
                Mision mision = categoria.indexMision(j);
                assertThat(mision).isInstanceOfAny(
                        Mision.Racha.class, Mision.Completitud.class,
                        Mision.HabilDonador.class, Mision.DonacionesExitosas.class);
            }
        }
    }

    @Test
    void laDificultadEscalaEntreCategorias() {
        Mision.Racha rachaColaborador = (Mision.Racha) buscarPorTipo(0, Mision.Racha.class);
        Mision.Racha rachaSostenedor = (Mision.Racha) buscarPorTipo(1, Mision.Racha.class);
        Mision.Racha rachaTransformador = (Mision.Racha) buscarPorTipo(2, Mision.Racha.class);

        assertThat(rachaColaborador.getMesesRequeridos())
                .isLessThan(rachaSostenedor.getMesesRequeridos());
        assertThat(rachaSostenedor.getMesesRequeridos())
                .isLessThan(rachaTransformador.getMesesRequeridos());
    }

    @Test
    void completitudNuncaPideMasDeTresPorqueDonacionesSoloTieneTresCategorias() {
        for (int i = 0; i < gestor.cantCategorias(); i++) {
            Mision.Completitud completitud = (Mision.Completitud) buscarPorTipo(i, Mision.Completitud.class);
            assertThat(completitud.getCategoriasRequeridas()).isLessThanOrEqualTo(3);
        }
    }

    @Test
    void todasLasInsigniasDelCatalogoSonUnicas() {
        Set<String> nombres = new HashSet<>();
        for (int i = 0; i < gestor.cantCategorias(); i++) {
            Categoria categoria = gestor.siguienteCategoria(i);
            for (int j = 0; j < categoria.cantMisiones(); j++) {
                String nombreInsignia = categoria.indexMision(j).getInsigniaAsociada().getNombre();
                assertThat(nombres.add(nombreInsignia))
                        .as("insignia duplicada: %s", nombreInsignia)
                        .isTrue();
            }
        }
    }

    private Mision buscarPorTipo(int indiceCategoria, Class<? extends Mision> tipo) {
        Categoria categoria = gestor.siguienteCategoria(indiceCategoria);
        for (int j = 0; j < categoria.cantMisiones(); j++) {
            Mision mision = categoria.indexMision(j);
            if (tipo.isInstance(mision)) {
                return mision;
            }
        }
        throw new AssertionError("No se encontró ninguna misión de tipo " + tipo.getSimpleName()
                + " en " + categoria.getNombre());
    }
}
