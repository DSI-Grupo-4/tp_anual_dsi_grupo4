package ar.edu.utn.frba.dds.donaciones.domain.personas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GestorDonantesTest {

    private GestorDonantes gestor;

    @BeforeEach
    void setUp() {
        gestor = new GestorDonantes();
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
