package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.personas.Donante;
import ar.edu.utn.frba.dds.donaciones.domain.personas.Genero;
import ar.edu.utn.frba.dds.donaciones.domain.personas.GestorDonantes;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaHumana;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaJuridica;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoOrganizacion;
import ar.edu.utn.frba.dds.donaciones.dto.DonanteDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaHumanaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DonanteServiceTest {

    @Mock
    private GestorDonantes gestorDonantes;

    private DonanteService donanteService;

    @BeforeEach
    void setUp() {
        donanteService = new DonanteService(gestorDonantes);
    }

    @Test
    void crearDonanteHumanoDelegaElAltaEnGestorDonantes() {
        PersonaHumanaDTO dto = new PersonaHumanaDTO();
        dto.setNombre("Ana");
        dto.setApellido("Perez");
        dto.setDocumento("12345678");

        Donante donanteRegistrado = new Donante(1L,
                new PersonaHumana("Ana", "Perez", null, "12345678", null));
        when(gestorDonantes.registrarDonante(any())).thenReturn(donanteRegistrado);

        DonanteDTO resultado = donanteService.crearDonanteHumano(dto);

        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("Ana");
        assertThat(resultado.getTipo()).isEqualTo("HUMANA");
    }

    @Test
    void eliminarDelegaEnGestorDonantes() {
        donanteService.eliminar(3L);

        verify(gestorDonantes).eliminar(eq(3L));
    }

    @Test
    void convertirADtoExponeEdadYGeneroDeUnaPersonaHumana() {
        // Regresión del hallazgo medio: DonanteDTO no exponía edad/género,
        // lo que hacía invisible (y en el PUT, destructivo) este dato.
        Donante donante = new Donante(1L,
                new PersonaHumana("Ana", "Perez", 30, "12345678", Genero.FEMENINO));

        DonanteDTO dto = donanteService.convertirADTO(donante);

        assertThat(dto.getEdad()).isEqualTo(30);
        assertThat(dto.getGenero()).isEqualTo(Genero.FEMENINO);
    }

    @Test
    void actualizarHumanoConservaEdadYGeneroCuandoElClienteLosReenvia() {
        Donante donante = new Donante(1L,
                new PersonaHumana("Ana", "Perez", 30, "12345678", Genero.FEMENINO));
        when(gestorDonantes.buscarPorId(1L)).thenReturn(donante);

        PersonaHumanaDTO cambios = new PersonaHumanaDTO();
        cambios.setNombre("Ana");
        cambios.setApellido("Perez");
        cambios.setEdad(31); // cumplió años
        cambios.setGenero(Genero.FEMENINO);
        cambios.setDocumento("12345678");

        DonanteDTO resultado = donanteService.actualizarHumano(1L, cambios);

        assertThat(resultado.getEdad()).isEqualTo(31);
        assertThat(resultado.getGenero()).isEqualTo(Genero.FEMENINO);
    }

    @Test
    void actualizarIgnoraElTipoDeclaradoPorElClienteYUsaElRealDeUnaPersonaJuridica() {
        // Regresión: antes DonanteController decidía la rama según
        // dto.getTipo() (lo que manda el cliente); acá el donante real es
        // JURIDICA pero el DTO dice "HUMANA" — antes esto tiraba
        // ClassCastException (500 sin manejar), confirmado en vivo.
        Donante donante = new Donante(1L,
                new PersonaJuridica("Arcos Plateados SA", TipoOrganizacion.EMPRESA, null, null));
        when(gestorDonantes.buscarPorId(1L)).thenReturn(donante);

        DonanteDTO cambios = new DonanteDTO();
        cambios.setTipo("HUMANA"); // mentira: el donante real es jurídico
        cambios.setRazonSocial("Arcos Plateados SA (renombrada)");
        cambios.setTipoOrganizacion(TipoOrganizacion.EMPRESA);

        // Si el bug siguiera presente, esta línea tiraría ClassCastException.
        DonanteDTO resultado = donanteService.actualizar(1L, cambios);

        assertThat(resultado.getTipo()).isEqualTo("JURIDICA");
        assertThat(resultado.getRazonSocial()).isEqualTo("Arcos Plateados SA (renombrada)");
    }

    @Test
    void actualizarIgnoraElTipoDeclaradoPorElClienteYUsaElRealDeUnaPersonaHumana() {
        Donante donante = new Donante(1L,
                new PersonaHumana("Ana", "Perez", 30, "12345678", Genero.FEMENINO));
        when(gestorDonantes.buscarPorId(1L)).thenReturn(donante);

        DonanteDTO cambios = new DonanteDTO();
        cambios.setTipo("JURIDICA"); // mentira: el donante real es humano
        cambios.setNombre("Ana Actualizada");
        cambios.setApellido("Perez");
        cambios.setEdad(31);
        cambios.setGenero(Genero.FEMENINO);
        cambios.setDocumento("12345678");

        DonanteDTO resultado = donanteService.actualizar(1L, cambios);

        assertThat(resultado.getTipo()).isEqualTo("HUMANA");
        assertThat(resultado.getNombre()).isEqualTo("Ana Actualizada");
    }
}
