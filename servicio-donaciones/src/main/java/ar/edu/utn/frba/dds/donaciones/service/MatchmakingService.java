package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.algoritmos.AlgoritmoAsignacion;
import ar.edu.utn.frba.dds.donaciones.domain.algoritmos.CompatibilidadSemantica;
import ar.edu.utn.frba.dds.donaciones.domain.algoritmos.PrioridadSubatendidos;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchmakingService {

    private final EntidadBeneficiariaService entidadBeneficiariaService;
    private final List<AlgoritmoAsignacion> algoritmos;

    public MatchmakingService(EntidadBeneficiariaService entidadBeneficiariaService) {
        this.entidadBeneficiariaService = entidadBeneficiariaService;
        this.algoritmos = List.of(
                new CompatibilidadSemantica(),
                new PrioridadSubatendidos()
        );
    }

    public List<EntidadBeneficiaria> ejecutarMatchmaking(Donacion donacion) {
        List<EntidadBeneficiaria> entidades =
                entidadBeneficiariaService.obtenerEntidadesDominio();

        List<List<EntidadBeneficiaria>> resultados = algoritmos.stream()
                .map(alg -> alg.ejecutarAlgoritmo(donacion, entidades))
                .toList();

        List<EntidadBeneficiaria> interseccion = resultados.stream()
                .reduce((a, b) -> a.stream()
                        .filter(b::contains)
                        .collect(Collectors.toList()))
                .orElse(List.of());

        if (!interseccion.isEmpty()) {
            return interseccion.stream().limit(10).toList();
        }

        return resultados.stream()
                .flatMap(List::stream)
                .distinct()
                .limit(10)
                .toList();
    }

    public void confirmarAsignacion(Donacion donacion, EntidadBeneficiaria entidad) {
        donacion.cambiarEstado(EstadoTrack.ASIGNACION_REALIZADA, null);
        donacion.setEntidadBeneficiaria(entidad);
    }
}
