package ar.edu.utn.frba.dds.web.client.incentivos;

import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.Insignia;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.InsigniaObtenida;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.Metricas;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.MetricasSistema;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.Mision;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.PosicionRanking;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.RankingMensual;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Cliente del servicio de Incentivos (Persona 4): métricas, misiones e insignias del donante. */
@Component
public class IncentivosClient {

    private final RestClient http;

    public IncentivosClient(@Qualifier("incentivos") RestClient http) {
        this.http = http;
    }

    protected RestClient http() {
        return http;
    }

    /** GET /api/donantes/{id}/metricas?periodo= (HISTORICO, MENSUAL, TRIMESTRAL, SEMESTRAL o ANUAL) */
    public Metricas metricas(long donanteId, String periodo) {
        return http.get().uri("/api/donantes/{id}/metricas?periodo={periodo}", donanteId, periodo)
                .retrieve().body(Metricas.class);
    }

    /** GET /api/donantes/{id}/misiones: misiones de la categoría actual, con su progreso. */
    public List<Mision> misiones(long donanteId) {
        return http.get().uri("/api/donantes/{id}/misiones", donanteId)
                .retrieve().body(new ParameterizedTypeReference<>() { });
    }

    /** GET /api/donantes/{id}/insignias: solo las visibles. */
    public List<Insignia> insignias(long donanteId) {
        return http.get().uri("/api/donantes/{id}/insignias", donanteId)
                .retrieve().body(new ParameterizedTypeReference<>() { });
    }

    /** GET /api/donantes/{id}/insignias/obtenidas: todas, visibles u ocultas. */
    public List<InsigniaObtenida> insigniasObtenidas(long donanteId) {
        return http.get().uri("/api/donantes/{id}/insignias/obtenidas", donanteId)
                .retrieve().body(new ParameterizedTypeReference<>() { });
    }

    /** PATCH /api/donantes/{id}/insignias/{nombre}/visibilidad */
    public void cambiarVisibilidad(long donanteId, String insignia, boolean visible) {
        http.patch().uri("/api/donantes/{id}/insignias/{nombre}/visibilidad", donanteId, insignia)
                .body(Map.of("visible", visible)).retrieve().toBodilessEntity();
    }

    /** GET /api/admin/metricas: totales del sistema y evolución mensual. */
    public MetricasSistema metricasSistema() {
        return http.get().uri("/api/admin/metricas").retrieve().body(MetricasSistema.class);
    }

    /** GET /api/ranking/actual: ranking del mes en curso, todavía sin cerrar. */
    public List<PosicionRanking> rankingEnCurso() {
        return http.get().uri("/api/ranking/actual").retrieve().body(new ParameterizedTypeReference<>() { });
    }

    /** GET /api/ranking/historial: rankings mensuales ya cerrados, del más viejo al más nuevo. */
    public List<RankingMensual> historialRankings() {
        return http.get().uri("/api/ranking/historial").retrieve().body(new ParameterizedTypeReference<>() { });
    }
}
