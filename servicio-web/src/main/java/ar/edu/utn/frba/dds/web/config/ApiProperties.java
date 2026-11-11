package ar.edu.utn.frba.dds.web.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** URLs y tiempos máximos de las APIs que consume el cliente web (prefijo donatrack.api). */
@ConfigurationProperties(prefix = "donatrack.api")
public record ApiProperties(
        Servicio donaciones,
        Servicio incentivos,
        Servicio logistica,
        Duration timeoutConexion,
        Duration timeoutLectura) {

    public record Servicio(String url) {
    }
}
