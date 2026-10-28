package ar.edu.utn.frba.dds.incentivos.scheduler;

import ar.edu.utn.frba.dds.incentivos.donante.GestorDonante;
import ar.edu.utn.frba.dds.incentivos.misiones.GestorMisiones;
import ar.edu.utn.frba.dds.incentivos.ranking.HistorialRanking;
import ar.edu.utn.frba.dds.incentivos.repository.ActividadMensualDonanteRepository;
import ar.edu.utn.frba.dds.incentivos.repository.CategoriaRepository;
import ar.edu.utn.frba.dds.incentivos.repository.DonanteRepository;
import ar.edu.utn.frba.dds.incentivos.repository.RankingRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

// GestorMisiones/GestorDonante/HistorialRanking son singletons manuales, no
// beans de Spring -- mismo patrón que WebhookN8nConfigurer/
// NotificacionesConfigurer: les inyectamos los repositorios al boot en vez
// de que ellos dependan de Spring. @Order(0) porque GestorDonante y
// HistorialRanking reconstruyen sus Donante/ActividadMensualDonante desde
// filas que ya referencian el catálogo -- el catálogo tiene que existir
// primero.
@Component
@Order(0)
public class PersistenciaConfigurer implements ApplicationRunner {

    private final CategoriaRepository categoriaRepository;
    private final DonanteRepository donanteRepository;
    private final RankingRepository rankingRepository;
    private final ActividadMensualDonanteRepository actividadMensualDonanteRepository;

    public PersistenciaConfigurer(CategoriaRepository categoriaRepository,
                                   DonanteRepository donanteRepository,
                                   RankingRepository rankingRepository,
                                   ActividadMensualDonanteRepository actividadMensualDonanteRepository) {
        this.categoriaRepository = categoriaRepository;
        this.donanteRepository = donanteRepository;
        this.rankingRepository = rankingRepository;
        this.actividadMensualDonanteRepository = actividadMensualDonanteRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        GestorMisiones.getInstance().configurarPersistencia(categoriaRepository);
        GestorDonante.getInstance().configurarPersistencia(donanteRepository);
        HistorialRanking.getInstance().configurarPersistencia(rankingRepository, actividadMensualDonanteRepository);
    }
}
