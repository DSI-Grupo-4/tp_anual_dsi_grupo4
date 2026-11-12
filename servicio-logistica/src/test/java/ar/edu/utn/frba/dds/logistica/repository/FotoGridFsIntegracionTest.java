package ar.edu.utn.frba.dds.logistica.repository;

import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.utn.frba.dds.logistica.dto.FotoRecuperada;
import ar.edu.utn.frba.dds.logistica.dto.FotoSubidaDTO;
import ar.edu.utn.frba.dds.logistica.service.FotoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Guarda y lee una foto contra un MongoDB real (GridFS). Se omite si no hay Docker, como RutaPersistenciaTest. */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class FotoGridFsIntegracionTest {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
    }

    @Autowired
    private FotoService fotos;

    @Test
    void laFotoGuardadaSeLeeIgualConSuTipo() throws Exception {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 13, 'I', 'H', 'D', 'R'};

        FotoSubidaDTO subida = fotos.guardar(new MockMultipartFile("file", "entrega.png", "image/png", png));
        FotoRecuperada leida = fotos.obtener(subida.getId());

        assertThat(subida.getUrl()).endsWith("/api/fotos/" + subida.getId());
        assertThat(leida.contentType()).isEqualTo("image/png");
        assertThat(leida.longitud()).isEqualTo(png.length);
        assertThat(leida.contenido().readAllBytes()).isEqualTo(png);
    }
}
