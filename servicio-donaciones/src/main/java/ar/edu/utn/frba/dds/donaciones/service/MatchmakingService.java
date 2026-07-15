package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.GestorDonaciones;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.ResultadoMatchmaking;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class MatchmakingService {

    private final EntidadBeneficiariaService entidadBeneficiariaService;
    private final GestorDonaciones gestorDonaciones = new GestorDonaciones();

    public MatchmakingService(EntidadBeneficiariaService entidadBeneficiariaService) {
        this.entidadBeneficiariaService = entidadBeneficiariaService;
    }

    public List<EntidadBeneficiaria> ejecutarMatchmaking(Donacion donacion) {
        ResultadoMatchmaking resultado = gestorDonaciones.ejecutarMatchmaking(
                donacion,
                entidadBeneficiariaService.obtenerEntidadesDominio()
        );

        if (!resultado.getInterseccion().isEmpty()) {
            return resultado.getInterseccion();
        }

        return Stream.concat(
                        resultado.getPorCompatibilidad().stream(),
                        resultado.getPorSubatencion().stream())
                .distinct()
                .limit(10)
                .toList();
    }

    public void confirmarAsignacion(Donacion donacion, EntidadBeneficiaria entidad) {
        donacion.cambiarEstado(EstadoTrack.ASIGNACION_REALIZADA, null);
        donacion.setEntidadBeneficiaria(entidad);
    }
}
