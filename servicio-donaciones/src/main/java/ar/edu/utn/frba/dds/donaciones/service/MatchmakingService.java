package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.GestorDonaciones;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.ResultadoMatchmaking;
import org.springframework.stereotype.Service;

@Service
public class MatchmakingService {

    private final EntidadBeneficiariaService entidadBeneficiariaService;
    private final GestorDonaciones gestorDonaciones;

    public MatchmakingService(
            EntidadBeneficiariaService entidadBeneficiariaService,
            GestorDonaciones gestorDonaciones) {
        this.entidadBeneficiariaService = entidadBeneficiariaService;
        this.gestorDonaciones = gestorDonaciones;
    }

    public ResultadoMatchmaking ejecutarMatchmaking(Donacion donacion) {
        return gestorDonaciones.ejecutarMatchmaking(
                donacion,
                entidadBeneficiariaService.obtenerEntidadesDominio()
        );
    }
}
