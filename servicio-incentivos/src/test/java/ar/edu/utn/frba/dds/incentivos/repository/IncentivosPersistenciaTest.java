package ar.edu.utn.frba.dds.incentivos.repository;

import ar.edu.utn.frba.dds.incentivos.consultor.Beneficiario;
import ar.edu.utn.frba.dds.incentivos.donacion.DatosDonacion;
import ar.edu.utn.frba.dds.incentivos.donante.Donante;
import ar.edu.utn.frba.dds.incentivos.misiones.Categoria;
import ar.edu.utn.frba.dds.incentivos.misiones.GestorMisiones;
import ar.edu.utn.frba.dds.incentivos.progreso.ProgresoMision;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Verifica el mapeo objeto-relacional real (herencia JOINED en Mision y
// ProgresoMision, la referencia circular ProgresoAsociado<->ProgresoMision,
// las colecciones en cascada) contra H2 en memoria -- mismo patrón que
// RutaPersistenciaTest en Logística.
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class IncentivosPersistenciaTest {

    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private DonanteRepository donanteRepository;
    @Autowired
    private TestEntityManager em;

    private void persistirCatalogoReal() {
        GestorMisiones gestor = GestorMisiones.getInstance();
        for (int i = 0; i < gestor.cantCategorias(); i++) {
            Categoria categoria = gestor.siguienteCategoria(i);
            if (categoria.getIdCategoria() == null) {
                categoriaRepository.save(categoria);
            }
        }
    }

    @Test
    void persisteUnDonanteConSuProgresoYLoRecuperaIgual() {
        persistirCatalogoReal();

        Donante donante = new Donante(123L);
        donante.actualizarNombreSiFalta("Ana Perez");
        Beneficiario beneficiario = new Beneficiario(9L, "Comedor Sonrisas");
        DatosDonacion donacion = new DatosDonacion(LocalDate.now(), "ALIMENTOS",
                BigDecimal.ONE, true, beneficiario);
        donante.registrarActividadDonacion(donacion);

        donanteRepository.save(donante);
        em.flush();
        em.clear();

        Donante recuperado = donanteRepository.findById(123L).orElseThrow();
        assertThat(recuperado.getNombre()).isEqualTo("Ana Perez");
        assertThat(recuperado.getSolicitudesDonacionHechas()).isEqualTo(1);
        assertThat(recuperado.getHistorialDonaciones()).hasSize(1);
        assertThat(recuperado.getBeneficiariosAyudados()).extracting(Beneficiario::getId).containsExactly(9L);

        // La primera misión (Racha Colaborador, 1 mes) se completa con esta única donación.
        List<ProgresoMision> misionesColaborador = recuperado.getProgresoAsociado()
                .getCategoriasObtenidas().get(0).getMisiones();
        assertThat(misionesColaborador.get(0).getInsigniaObtenida()).isNotNull();
        assertThat(misionesColaborador.get(0).getInsigniaObtenida().getInsigniaAsociada().getNombre())
                .isEqualTo("Primer Paso");

        // El misionActual (referencia circular) avanzó a la 2da misión y se persistió.
        assertThat(recuperado.getProgresoAsociado().getMisionActual()).isNotNull();
        assertThat(recuperado.getProgresoAsociado().getMisionActual().getOrden()).isEqualTo(1);

        // Dos donantes nuevos no comparten progreso (cada uno con su propio
        // ProgresoAsociado, aunque apunten al mismo catálogo de misiones).
        Donante donante2 = new Donante(201L);
        Donante donante3 = new Donante(202L);
        donanteRepository.save(donante2);
        donanteRepository.save(donante3);
        em.flush();
        em.clear();

        Donante recuperado2 = donanteRepository.findById(201L).orElseThrow();
        Donante recuperado3 = donanteRepository.findById(202L).orElseThrow();
        assertThat(recuperado2.getProgresoAsociado().getIdProgresoAsociado())
                .isNotEqualTo(recuperado3.getProgresoAsociado().getIdProgresoAsociado());
    }
}
