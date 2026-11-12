package ar.edu.utn.frba.dds.logistica.domain.rutas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class CamionUbicacionTest {

    private static Camion camion() {
        return new Camion(1, "ABC123", 20, 3, 1000, EstadoCamion.DISPONIBLE);
    }

    @Test
    void unCamionNuevoNoTieneUbicacion() {
        assertThat(camion().tieneUbicacion()).isFalse();
    }

    @Test
    void reportarUbicacionGuardaLasCoordenadasLaFechaYSiEsSimulada() {
        Camion camion = camion();
        LocalDateTime ahora = LocalDateTime.of(2026, 11, 11, 17, 30);

        camion.reportarUbicacion(-34.6, -58.4, ahora, true);

        assertThat(camion.tieneUbicacion()).isTrue();
        assertThat(camion.getLatitud()).isEqualTo(-34.6);
        assertThat(camion.getLongitud()).isEqualTo(-58.4);
        assertThat(camion.getUbicacionActualizadaEn()).isEqualTo(ahora);
        assertThat(camion.getUbicacionSimulada()).isTrue();
    }

    @Test
    void unaNuevaUbicacionReemplazaALaAnterior() {
        Camion camion = camion();
        camion.reportarUbicacion(-34.6, -58.4, LocalDateTime.of(2026, 11, 11, 17, 0), true);

        camion.reportarUbicacion(-34.7, -58.5, LocalDateTime.of(2026, 11, 11, 17, 5), false);

        assertThat(camion.getLatitud()).isEqualTo(-34.7);
        assertThat(camion.getUbicacionSimulada()).isFalse();
    }

    @Test
    void lasCoordenadasQueNoExistenSeRechazanYNoSeGuardan() {
        Camion camion = camion();

        assertThatThrownBy(() -> camion.reportarUbicacion(91, 0, LocalDateTime.now(), false))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("latitud");
        assertThatThrownBy(() -> camion.reportarUbicacion(0, 181, LocalDateTime.now(), false))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("longitud");
        assertThatThrownBy(() -> camion.reportarUbicacion(Double.NaN, 0, LocalDateTime.now(), false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(camion.tieneUbicacion()).isFalse();
    }

    @Test
    void unaDireccionSabeSiTieneCoordenadas() {
        Direccion direccion = new Direccion("Calle", "1", null);
        assertThat(direccion.tieneCoordenadas()).isFalse();

        direccion.setLatitud(-34.6);
        assertThat(direccion.tieneCoordenadas()).isFalse();

        direccion.setLongitud(-58.4);
        assertThat(direccion.tieneCoordenadas()).isTrue();
    }
}
