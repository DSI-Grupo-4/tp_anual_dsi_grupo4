package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.dto.RankingEntidadDTO;
import ar.edu.utn.frba.dds.donaciones.dto.SolicitudRankingDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AsignacionService {

    private final EntidadBeneficiariaService entidadBeneficiariaService;

    public AsignacionService(EntidadBeneficiariaService entidadBeneficiariaService) {
        this.entidadBeneficiariaService = entidadBeneficiariaService;
    }

    public List<RankingEntidadDTO> generarRanking(SolicitudRankingDTO dto) {
        return List.of();
    }
}
