package ar.edu.utn.frba.dds.incentivos.donante;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * GestorDonante es un singleton manual compartido por todo el proceso de
 * test -- se usan ids negativos/altos exclusivos de esta clase para no
 * pisar donantes que otros tests puedan registrar en el mismo id.
 */
class GestorDonanteTest {

    private final GestorDonante gestor = GestorDonante.getInstance();

    @Test
    void buscarDonanteTiraExcepcionSiNuncaTuvoActividad() {
        // Regresión: GET /{id}/metricas, /{id}/misiones, /{id}/insignias y
        // PATCH .../visibilidad usaban obtenerDonante() (alta perezosa) en
        // vez de esto -- cualquier id devolvía 200 con progreso vacío en
        // lugar de 404.
        assertThatThrownBy(() -> gestor.buscarDonante(-9001L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void buscarDonanteEncuentraUnoQueYaTuvoActividad() {
        Donante registrado = gestor.obtenerDonante(-9002L, "Ana Perez");

        Donante encontrado = gestor.buscarDonante(-9002L);

        assertThat(encontrado).isSameAs(registrado);
    }

    @Test
    void obtenerDonanteSigueCreandoDeFormaPerezosaParaElAltaDesdeActividadDonacion() {
        Donante creado = gestor.obtenerDonante(-9003L);

        assertThat(creado.getId()).isEqualTo(-9003L);
        assertThat(gestor.buscarDonante(-9003L)).isSameAs(creado);
    }
}
