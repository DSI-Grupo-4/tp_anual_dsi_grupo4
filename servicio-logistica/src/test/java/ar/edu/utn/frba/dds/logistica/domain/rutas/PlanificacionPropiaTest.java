package ar.edu.utn.frba.dds.logistica.domain.rutas;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlanificacionPropiaTest {

    private final PlanificacionPropia estrategia = new PlanificacionPropia();

    private Direccion direccion() {
        return new Direccion("Falsa", "123", new Ciudad("CABA", new Provincia("Buenos Aires")));
    }

    private Camion camion(int id, int capacidadCargaKg) {
        return new Camion(id, "AA%03dBB".formatted(id), 500, 3, capacidadCargaKg, EstadoCamion.DISPONIBLE);
    }

    private Entrega entrega(int id, int idEntidad, int pesoKg) {
        return new Entrega(id, 100 + id, idEntidad, direccion(), LocalDate.now(), pesoKg, 1, 1);
    }

    @Test
    void dosParadasQueIndividualmenteEntranPeroJuntasNoSeReparten() {
        // Regresión Tier 3 #9b: antes cada parada se validaba contra la capacidad
        // TOTAL del camión, no la restante -- un camión de 1000kg aceptaba dos
        // paradas de 600kg cada una (600 <= 1000) y terminaba con 1200kg reales.
        Camion unicoCamion = camion(1, 1000);
        Entrega entregaA = entrega(1, 10, 600);
        Entrega entregaB = entrega(2, 20, 600);

        List<Ruta> rutas = estrategia.planificar(
                List.of(entregaA, entregaB), List.of(unicoCamion), List.of());

        // Con un solo camión disponible y sin que entren juntas, una de las dos
        // queda sin planificar en vez de sobrecargar el camión.
        int entregasPlanificadas = rutas.stream()
                .flatMap(r -> r.getParadas().stream())
                .mapToInt(p -> p.getEntregas().size())
                .sum();
        assertThat(entregasPlanificadas).isEqualTo(1);

        Ruta ruta = rutas.get(0);
        int pesoTotalEnRuta = ruta.getParadas().stream()
                .flatMap(p -> p.getEntregas().stream())
                .mapToInt(Entrega::getPesoKG)
                .sum();
        assertThat(pesoTotalEnRuta).isLessThanOrEqualTo(1000);
    }

    @Test
    void dosParadasQueJuntasNoEntranSeRepartenEntreDosCamiones() {
        Camion camion1 = camion(1, 1000);
        Camion camion2 = camion(2, 1000);
        Entrega entregaA = entrega(1, 10, 600);
        Entrega entregaB = entrega(2, 20, 600);

        List<Ruta> rutas = estrategia.planificar(
                List.of(entregaA, entregaB), List.of(camion1, camion2), List.of());

        int entregasPlanificadas = rutas.stream()
                .flatMap(r -> r.getParadas().stream())
                .mapToInt(p -> p.getEntregas().size())
                .sum();
        assertThat(entregasPlanificadas).isEqualTo(2);
        assertThat(rutas).hasSize(2);
    }

    @Test
    void variasParadasQueEntranJuntasQuedanEnLaMismaRuta() {
        Camion camion = camion(1, 1000);
        Entrega entregaA = entrega(1, 10, 300);
        Entrega entregaB = entrega(2, 20, 300);

        List<Ruta> rutas = estrategia.planificar(
                List.of(entregaA, entregaB), List.of(camion), List.of());

        assertThat(rutas).hasSize(1);
        assertThat(rutas.get(0).getParadas()).hasSize(2);
    }
}
