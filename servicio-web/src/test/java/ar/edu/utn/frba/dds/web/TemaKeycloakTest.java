package ar.edu.utn.frba.dds.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * El tema de login de Keycloak (keycloak/themes/donatrack) lleva una copia de los tokens
 * de diseño porque Keycloak no puede pedírselos a servicio-web. Este test falla si la copia
 * quedó desactualizada: al cambiar tokens.css, copiarlo también al tema.
 */
class TemaKeycloakTest {

    private static final Path ORIGINAL = Path.of("src/main/resources/static/css/tokens.css");
    private static final Path COPIA = Path.of("../keycloak/themes/donatrack/login/resources/css/tokens.css");

    @Test
    void elTemaDeKeycloakUsaLosMismosTokensQueServicioWeb() throws IOException {
        assertThat(COPIA).exists();
        assertThat(Files.readString(COPIA))
                .as("Copiá %s a %s", ORIGINAL, COPIA)
                .isEqualTo(Files.readString(ORIGINAL));
    }
}
