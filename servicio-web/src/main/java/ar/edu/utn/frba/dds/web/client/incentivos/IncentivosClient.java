package ar.edu.utn.frba.dds.web.client.incentivos;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente del servicio de Incentivos. Todavía sin métodos: los agrega la Persona 4
 * (métricas, misiones, insignias, ranking), siguiendo el mismo patrón que LogisticaClient.
 */
@Component
public class IncentivosClient {

    private final RestClient http;

    public IncentivosClient(@Qualifier("incentivos") RestClient http) {
        this.http = http;
    }

    protected RestClient http() {
        return http;
    }
}
