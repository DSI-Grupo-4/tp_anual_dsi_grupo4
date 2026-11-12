package ar.edu.utn.frba.dds.web.config;

import ar.edu.utn.frba.dds.web.client.ManejoErroresApi;
import ar.edu.utn.frba.dds.web.client.PropagadorCredenciales;
import ar.edu.utn.frba.dds.web.error.ServicioBackend;
import java.net.http.HttpClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Un RestClient por backend, con URL base, timeouts, credenciales y manejo de errores.
 * Los clientes de cada servicio (DonacionesClient, LogisticaClient, IncentivosClient)
 * los reciben por @Qualifier. Para llamar a un endpoint nuevo, agregar un método
 * al cliente del servicio correspondiente, no usar HTTP directo desde un controller.
 */
@Configuration
public class ClientesApiConfig {

    /** La importación síncrona de 20.000 filas no comparte el timeout de las lecturas. */
    @Bean
    @Qualifier("importacionDonantes")
    RestClient restClientImportacion(RestClient.Builder builder, ApiProperties props,
            ObjectProvider<PropagadorCredenciales> credenciales,
            @org.springframework.beans.factory.annotation.Value("${api.timeout-importacion:10m}") java.time.Duration timeout) {
        var fabrica = new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(props.timeoutConexion()).build());
        fabrica.setReadTimeout(timeout);
        return ManejoErroresApi.configurar(builder.clone().baseUrl(props.donaciones().url()).requestFactory(fabrica),
                ServicioBackend.DONACIONES, credenciales.getIfAvailable(() -> PropagadorCredenciales.NINGUNO)).build();
    }

    @Bean
    @Qualifier("donaciones")
    RestClient restClientDonaciones(RestClient.Builder builder, ApiProperties props, ObjectProvider<PropagadorCredenciales> credenciales) {
        return crear(builder, props, props.donaciones().url(), ServicioBackend.DONACIONES, credenciales);
    }

    @Bean
    @Qualifier("incentivos")
    RestClient restClientIncentivos(RestClient.Builder builder, ApiProperties props, ObjectProvider<PropagadorCredenciales> credenciales) {
        return crear(builder, props, props.incentivos().url(), ServicioBackend.INCENTIVOS, credenciales);
    }

    @Bean
    @Qualifier("logistica")
    RestClient restClientLogistica(RestClient.Builder builder, ApiProperties props, ObjectProvider<PropagadorCredenciales> credenciales) {
        return crear(builder, props, props.logistica().url(), ServicioBackend.LOGISTICA, credenciales);
    }

    private static RestClient crear(RestClient.Builder builder, ApiProperties props, String url, ServicioBackend servicio,
                                    ObjectProvider<PropagadorCredenciales> credenciales) {
        HttpClient http = HttpClient.newBuilder().connectTimeout(props.timeoutConexion()).build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(http);
        fabrica.setReadTimeout(props.timeoutLectura());
        RestClient.Builder configurado = builder.clone().baseUrl(url).requestFactory(fabrica);
        return ManejoErroresApi.configurar(configurado, servicio,
                credenciales.getIfAvailable(() -> PropagadorCredenciales.NINGUNO)).build();
    }
}
