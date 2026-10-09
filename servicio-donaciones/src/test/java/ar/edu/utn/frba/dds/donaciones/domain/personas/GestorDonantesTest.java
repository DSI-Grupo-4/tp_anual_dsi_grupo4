package ar.edu.utn.frba.dds.donaciones.domain.personas;

import ar.edu.utn.frba.dds.donaciones.repository.DonanteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// DonanteRepository se simula con un mock respaldado por una lista en
// memoria: estos tests verifican el comportamiento de GestorDonantes en sí
// (ids secuenciales sin colisión entre alta manual e importación CSV,
// dedup por email), no el mapeo JPA.
class GestorDonantesTest {

    private GestorDonantes gestor;
    private final List<Donante> donantesGuardados = new ArrayList<>();
    private final AtomicLong siguienteId = new AtomicLong(1L);

    @BeforeEach
    void setUp() {
        DonanteRepository donanteRepository = mock(DonanteRepository.class);

        when(donanteRepository.save(any())).thenAnswer(invocation -> {
            Donante donante = invocation.getArgument(0);
            if (donante.getId() == null) {
                donante.setId(siguienteId.getAndIncrement());
            }
            donantesGuardados.removeIf(d -> d.getId().equals(donante.getId()));
            donantesGuardados.add(donante);
            return donante;
        });
        when(donanteRepository.findAll()).thenAnswer(invocation -> List.copyOf(donantesGuardados));
        when(donanteRepository.findById(any())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return donantesGuardados.stream().filter(d -> d.getId().equals(id)).findFirst();
        });
        doAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            donantesGuardados.removeIf(d -> d.getId().equals(id));
            return null;
        }).when(donanteRepository).deleteById(any());

        gestor = new GestorDonantes(donanteRepository);
    }

    @Test
    void registrarDonanteAsignaIdsSecuenciales() {
        Donante d1 = gestor.registrarDonante(new PersonaHumana("Ana", "Perez", 30, "1", null));
        Donante d2 = gestor.registrarDonante(new PersonaHumana("Luis", "Gomez", 40, "2", null));

        assertThat(d1.getId()).isEqualTo(1L);
        assertThat(d2.getId()).isEqualTo(2L);
    }

    @Test
    void laImportacionMasivaContinuaLaMismaSecuenciaDeIdsQueElAltaManual() {
        // Regresión: antes, ImportadorCSV numeraba sus Donante desde 1L por
        // su cuenta, colisionando con los ids ya asignados manualmente.
        gestor.registrarDonante(new PersonaHumana("Ana", "Perez", 30, "1", null)); // id=1

        String csv = "TipoPersona,TipoDoc,Documento,Nombre,Email,Telefono\n"
                + "HUMANA,DNI,87654321,Luis Gomez,luis@mail.com,123456\n";
        ImportadorCSV importador = new ImportadorCSV("csv-test");
        importador.importar(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
        gestor.agregarImportador(importador);

        List<Donante> importados = gestor.importarDonantes("csv-test");

        assertThat(importados).hasSize(1);
        assertThat(importados.get(0).getId()).isEqualTo(2L); // no colisiona con el id=1 ya usado
        assertThat(gestor.getDonantesRegistrados()).hasSize(2);
    }

    @Test
    void laImportacionMasivaActualizaAlDonanteExistenteSiElEmailYaEstaRegistrado() {
        String csv = "TipoPersona,TipoDoc,Documento,Nombre,Email,Telefono\n"
                + "HUMANA,DNI,11111111,Ana Viejo,ana@mail.com,000\n";
        ImportadorCSV importador = new ImportadorCSV("csv-test");
        importador.importar(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
        gestor.agregarImportador(importador);
        Donante primeraImportacion = gestor.importarDonantes("csv-test").get(0);

        // Reimportar el mismo email, con datos actualizados -- no debe duplicar.
        String csvActualizado = "TipoPersona,TipoDoc,Documento,Nombre,Email,Telefono\n"
                + "HUMANA,DNI,11111111,Ana Nueva,ana@mail.com,999\n";
        ImportadorCSV importador2 = new ImportadorCSV("csv-test-2");
        importador2.importar(new ByteArrayInputStream(csvActualizado.getBytes(StandardCharsets.UTF_8)));
        gestor.agregarImportador(importador2);
        Donante segundaImportacion = gestor.importarDonantes("csv-test-2").get(0);

        assertThat(gestor.getDonantesRegistrados()).hasSize(1);
        assertThat(segundaImportacion.getId()).isEqualTo(primeraImportacion.getId());
        PersonaHumana actualizada = (PersonaHumana) segundaImportacion.getPersona();
        assertThat(actualizada.getNombre()).isEqualTo("Ana");
    }

    @Test
    void buscarPorIdLanzaExcepcionSiNoExiste() {
        assertThatThrownBy(() -> gestor.buscarPorId(404L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void eliminarSacaAlDonanteDeLaLista() {
        Donante donante = gestor.registrarDonante(new PersonaHumana("Ana", "Perez", 30, "1", null));

        gestor.eliminar(donante.getId());

        assertThat(gestor.getDonantesRegistrados()).isEmpty();
    }
}
