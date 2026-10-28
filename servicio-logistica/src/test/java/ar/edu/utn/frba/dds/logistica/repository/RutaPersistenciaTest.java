package ar.edu.utn.frba.dds.logistica.repository;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Camion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Ciudad;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Direccion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoCamion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoEntrega;
import ar.edu.utn.frba.dds.logistica.domain.rutas.FotoEntrega;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Parada;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Provincia;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Ruta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Verifica el mapeo objeto-relacional (embebidos, cascadas, relaciones) contra H2 en memoria.
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class RutaPersistenciaTest {

    @Autowired
    private RutaRepository rutaRepository;
    @Autowired
    private EntregaRepository entregaRepository;
    @Autowired
    private CamionRepository camionRepository;
    @Autowired
    private TestEntityManager em;

    private Direccion direccion() {
        return new Direccion("Falsa", "123", new Ciudad("CABA", new Provincia("Buenos Aires")));
    }

    private Ruta rutaConDosEntregasEnUnaParada() {
        Camion camion = camionRepository.save(new Camion(null, "AA123BB", 500, 3, 1000, EstadoCamion.DISPONIBLE));
        Entrega e1 = entregaRepository.save(new Entrega(null, 100, 10, direccion(), LocalDate.now(), 10, 1, 1));
        Entrega e2 = entregaRepository.save(new Entrega(null, 101, 10, direccion(), LocalDate.now(), 10, 1, 1));
        Parada parada = new Parada(1, 10, direccion(), List.of(e1, e2));
        e1.asignarARuta(camion);
        e2.asignarARuta(camion);
        return rutaRepository.save(new Ruta(null, camion, null, LocalDate.now().plusDays(1), List.of(parada)));
    }

    @Test
    void unaRutaSePersisteConSusParadasYEntregasYSeRecuperaIgual() {
        Ruta ruta = rutaConDosEntregasEnUnaParada();
        em.flush();
        em.clear();

        Ruta recuperada = rutaRepository.findById(ruta.getIdRuta()).orElseThrow();

        assertThat(recuperada.getParadas()).hasSize(1);
        Parada parada = recuperada.getParadas().get(0);
        assertThat(parada.getNumeroParada()).isEqualTo(1);
        assertThat(parada.getDireccion().getCiudad().getProvincia().getNombre()).isEqualTo("Buenos Aires");
        assertThat(parada.getEntregas()).hasSize(2);
        assertThat(recuperada.getCamionAsociado().getEstadoCamion()).isEqualTo(EstadoCamion.ASIGNADO);
    }

    @Test
    void confirmarRecepcionGuardaUnaFotoPorEntregaYLasMarcaEntregadas() {
        Ruta ruta = rutaConDosEntregasEnUnaParada();
        em.flush();
        em.clear();

        Ruta recuperada = rutaRepository.findById(ruta.getIdRuta()).orElseThrow();
        recuperada.getParadas().get(0).confirmarRecepcion(new FotoEntrega("https://example.org/f.jpg", null));
        em.flush();
        em.clear();

        List<Entrega> entregas = entregaRepository.obtenerTodas();
        assertThat(entregas).hasSize(2);
        assertThat(entregas).allSatisfy(e -> {
            assertThat(e.getEstadoEntrega()).isEqualTo(EstadoEntrega.ENTREGADA);
            assertThat(e.getFotos()).hasSize(1);
        });
    }
}
