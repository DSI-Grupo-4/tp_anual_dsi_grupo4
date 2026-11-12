package ar.edu.utn.frba.dds.logistica.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Camion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoCamion;
import ar.edu.utn.frba.dds.logistica.dto.UbicacionCamionDTO;
import ar.edu.utn.frba.dds.logistica.dto.UbicacionRequestDTO;
import ar.edu.utn.frba.dds.logistica.repository.CamionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UbicacionCamionServiceTest {

    private final CamionRepository repositorio = mock(CamionRepository.class);
    private final UbicacionCamionService servicio = new UbicacionCamionService(repositorio);

    @BeforeEach
    void guardarDevuelveLoMismo() {
        when(repositorio.save(any(Camion.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    private static Camion camion(int id, String patente) {
        return new Camion(id, patente, 20, 3, 1000, EstadoCamion.EN_RUTA);
    }

    @Test
    void reportarGuardaLaUbicacionConLaHoraDelServidorYSinMarcarlaComoSimulada() {
        Camion camion = camion(1, "AA123BB");
        when(repositorio.findById(1)).thenReturn(Optional.of(camion));

        UbicacionCamionDTO dto = servicio.reportar(1, new UbicacionRequestDTO(-34.6, -58.4));

        assertThat(dto.getLatitud()).isEqualTo(-34.6);
        assertThat(dto.isSimulada()).isFalse();
        assertThat(dto.getActualizadaEn()).isBefore(LocalDateTime.now().plusDays(1)).isAfter(LocalDateTime.now().minusDays(1));
        verify(repositorio).save(camion);
    }

    @Test
    void reportarParaUnCamionInexistenteEsUn404() {
        when(repositorio.findById(9)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.reportar(9, new UbicacionRequestDTO(-34.6, -58.4)))
                .isInstanceOf(NoSuchElementException.class).hasMessageContaining("No existe el camión 9");
        verify(repositorio, never()).save(any());
    }

    @Test
    void consultarUnCamionQueNuncaInformoNoDevuelveCoordenadasInventadas() {
        when(repositorio.findById(1)).thenReturn(Optional.of(camion(1, "AA123BB")));

        assertThatThrownBy(() -> servicio.ubicacion(1))
                .isInstanceOf(NoSuchElementException.class).hasMessageContaining("todavía no informó");
    }

    @Test
    void lasUbicacionesSoloIncluyenALosCamionesQueInformaronYVanOrdenadas() {
        Camion a = camion(2, "BB222CC");
        a.reportarUbicacion(-34.6, -58.4, LocalDateTime.now(), false);
        Camion b = camion(1, "AA111BB");
        b.reportarUbicacion(-34.7, -58.5, LocalDateTime.now(), true);
        Camion sinUbicacion = camion(3, "CC333DD");
        when(repositorio.findAll()).thenReturn(List.of(a, b, sinUbicacion));

        List<UbicacionCamionDTO> ubicaciones = servicio.ubicaciones();

        assertThat(ubicaciones).extracting(UbicacionCamionDTO::getIdCamion).containsExactly(1, 2);
    }
}
