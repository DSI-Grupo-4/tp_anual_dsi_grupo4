package ar.edu.utn.frba.dds.incentivos.ranking;

import ar.edu.utn.frba.dds.incentivos.donante.Donante;
import ar.edu.utn.frba.dds.incentivos.misiones.Categoria;
import ar.edu.utn.frba.dds.incentivos.misiones.GestorMisiones;
import ar.edu.utn.frba.dds.incentivos.repository.ActividadMensualDonanteRepository;
import ar.edu.utn.frba.dds.incentivos.repository.DonanteRepository;
import ar.edu.utn.frba.dds.incentivos.repository.RankingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

// Reproduce el bug real: al cerrar el mes, quienes no entran al top 3 quedaban con ranking=null en
// la base para siempre, y el próximo boot los recargaba mezclados con la actividad del mes nuevo.
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class HistorialRankingPersistenciaTest {

    @Autowired
    private RankingRepository rankingRepository;
    @Autowired
    private ActividadMensualDonanteRepository actividadMensualDonanteRepository;
    @Autowired
    private DonanteRepository donanteRepository;
    @Autowired
    private TestEntityManager em;

    private void persistirCatalogoReal() {
        GestorMisiones gestor = GestorMisiones.getInstance();
        for (int i = 0; i < gestor.cantCategorias(); i++) {
            Categoria categoria = gestor.siguienteCategoria(i);
            if (categoria.getIdCategoria() == null) {
                em.persist(categoria);
            }
        }
    }

    private Donante nuevoDonante(long id) {
        Donante donante = new Donante(id);
        donanteRepository.save(donante);
        return donante;
    }

    @Test
    void alCerrarElMesSoloSobreviveLaActividadDeQuienesEntranAlTopTres() {
        // GestorMisiones es un singleton manual de todo el proceso (no un bean de Spring): si otra
        // clase @DataJpaTest ya corrió antes en esta misma JVM, su catálogo quedó con ids de una
        // base H2 distinta (ya cerrada). Se resetea para que sembrarCatalogo() arranque limpio y
        // persistirCatalogoReal() inserte contra la base de este test, no contra una vieja.
        ReflectionTestUtils.setField(GestorMisiones.class, "instancia", null);
        ReflectionTestUtils.setField(HistorialRanking.class, "instancia", null);
        persistirCatalogoReal();

        HistorialRanking historial = HistorialRanking.getInstance();
        try {
            historial.configurarPersistencia(rankingRepository, actividadMensualDonanteRepository);

            Donante ganador1 = nuevoDonante(-8001L);
            Donante ganador2 = nuevoDonante(-8002L);
            Donante ganador3 = nuevoDonante(-8003L);
            Donante perdedor1 = nuevoDonante(-8004L);
            Donante perdedor2 = nuevoDonante(-8005L);

            registrarVeces(historial, ganador1, 5);
            registrarVeces(historial, ganador2, 4);
            registrarVeces(historial, ganador3, 3);
            registrarVeces(historial, perdedor1, 2);
            registrarVeces(historial, perdedor2, 1);

            historial.generarRanking();
            em.flush();
            em.clear();

            // En la base solo debe quedar la actividad del mes en curso -- nada de los 2 perdedores.
            assertThat(actividadMensualDonanteRepository.findByRankingIsNull()).isEmpty();

            // El ranking cerrado sí conserva a los 3 ganadores.
            Ranking ranking = rankingRepository.findAllByOrderByFechaEmisionAsc().get(0);
            assertThat(ranking.getTopDonantes()).extracting(a -> a.getDonanteAsociado().getId())
                    .containsExactly(-8001L, -8002L, -8003L);

            // Simula un reinicio del proceso: singleton nuevo, recarga desde la base.
            ReflectionTestUtils.setField(HistorialRanking.class, "instancia", null);
            HistorialRanking historialReiniciado = HistorialRanking.getInstance();
            historialReiniciado.configurarPersistencia(rankingRepository, actividadMensualDonanteRepository);

            // Antes del fix: los 2 perdedores reaparecían acá, arrastrados del mes anterior.
            assertThat(historialReiniciado.obtenerPosicionActual(perdedor1)).isEmpty();
            assertThat(historialReiniciado.obtenerPosicionActual(perdedor2)).isEmpty();
        } finally {
            // Deja ambos singletons limpios para el próximo test de la misma JVM (ida y vuelta,
            // no solo al entrar): si no, el próximo @DataJpaTest hereda ids de esta base, ya cerrada.
            ReflectionTestUtils.setField(HistorialRanking.class, "instancia", null);
            ReflectionTestUtils.setField(GestorMisiones.class, "instancia", null);
        }
    }

    private void registrarVeces(HistorialRanking historial, Donante donante, int veces) {
        for (int i = 0; i < veces; i++) {
            historial.registrarMisionCompletada(donante);
        }
    }
}
