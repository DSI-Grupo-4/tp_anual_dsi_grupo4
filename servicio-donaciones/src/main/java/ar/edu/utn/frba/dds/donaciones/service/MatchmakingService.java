package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchmakingService {

    private final EntidadBeneficiariaService entidadBeneficiariaService;

    public MatchmakingService(EntidadBeneficiariaService entidadBeneficiariaService) {
        this.entidadBeneficiariaService = entidadBeneficiariaService;
    }

    public List<EntidadBeneficiaria> ejecutarMatchmaking(Donacion donacion) {
        return List.of();
    }

    public void confirmarAsignacion(Donacion donacion, EntidadBeneficiaria entidad) {
        // no-op: Donacion domain model doesn't support assignment state
    }
}
