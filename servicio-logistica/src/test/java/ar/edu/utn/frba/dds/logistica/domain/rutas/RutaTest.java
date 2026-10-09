package ar.edu.utn.frba.dds.logistica.domain.rutas;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RutaTest {

    private Direccion direccion() {
        return new Direccion("Falsa", "123", new Ciudad("CABA", new Provincia("Buenos Aires")));
    }

    private Camion camion() {
        return new Camion(1, "AA123BB", 500, 3, 1000, EstadoCamion.DISPONIBLE);
    }

    private Entrega entrega() {
        return new Entrega(1, 100, 10, direccion(), LocalDate.now(), 10, 1, 1);
    }

    @Test
    void planificarUnaRutaComprometeElCamionAunqueElChoferNoLaHayaIniciado() {
        // Regresión: antes el camión quedaba DISPONIBLE hasta iniciarRuta(),
        // pudiendo recibir una segunda ruta planificada en paralelo.
        Camion camion = camion();
        Entrega entrega = entrega();
        Parada parada = new Parada(1, 10, direccion(), List.of(entrega));

        new Ruta(1, camion, null, LocalDate.now().plusDays(1), List.of(parada));

        assertThat(camion.getEstadoCamion()).isEqualTo(EstadoCamion.ASIGNADO);
    }

    @Test
    void iniciarRutaPasaElCamionAEnRutaYLasEntregasATraslado() {
        Camion camion = camion();
        Entrega entrega = entrega();
        Parada parada = new Parada(1, 10, direccion(), List.of(entrega));
        Ruta ruta = new Ruta(1, camion, null, LocalDate.now().plusDays(1), List.of(parada));

        ruta.iniciarRuta();

        assertThat(camion.getEstadoCamion()).isEqualTo(EstadoCamion.EN_RUTA);
        assertThat(entrega.getEstadoEntrega()).isEqualTo(EstadoEntrega.EN_TRASLADO);
    }

    @Test
    void finalizarRutaLiberaElCamion() {
        // Regresión del hallazgo más grave: finalizarRuta() nunca se llamaba
        // desde ningún lado -- un camión que ya hizo una ruta quedaba
        // EN_RUTA para siempre y dejaba de considerarse para planificar.
        Camion camion = camion();
        Entrega entrega = entrega();
        Parada parada = new Parada(1, 10, direccion(), List.of(entrega));
        Ruta ruta = new Ruta(1, camion, null, LocalDate.now().plusDays(1), List.of(parada));
        ruta.iniciarRuta();

        ruta.finalizarRuta();

        assertThat(camion.getEstadoCamion()).isEqualTo(EstadoCamion.DISPONIBLE);
        assertThat(ruta.getEstadoRuta()).isEqualTo(EstadoRuta.FINALIZADA);
    }

    @Test
    void completoTodasLasEntregasEsTrueConUnaMezclaDeEntregadaYFallida() {
        Camion camion = camion();
        Entrega entregada = entrega();
        Entrega fallida = new Entrega(2, 101, 20, direccion(), LocalDate.now(), 10, 1, 1);
        Parada paradaA = new Parada(1, 10, direccion(), List.of(entregada));
        Parada paradaB = new Parada(2, 20, direccion(), List.of(fallida));
        Ruta ruta = new Ruta(1, camion, null, LocalDate.now().plusDays(1), List.of(paradaA, paradaB));

        paradaA.confirmarRecepcion(new FotoEntrega());
        paradaB.marcarFallida("Incidente logístico");

        assertThat(ruta.completoTodasLasEntregas()).isTrue();
    }

    @Test
    void completoTodasLasEntregasEsFalseMientrasUnaSigaEnTraslado() {
        Camion camion = camion();
        Entrega entrega = entrega();
        Parada parada = new Parada(1, 10, direccion(), List.of(entrega));
        Ruta ruta = new Ruta(1, camion, null, LocalDate.now().plusDays(1), List.of(parada));

        ruta.iniciarRuta();

        assertThat(ruta.completoTodasLasEntregas()).isFalse();
    }
}
