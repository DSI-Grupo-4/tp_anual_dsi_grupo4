package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import ar.edu.utn.frba.dds.donaciones.dto.CamionEnCursoDTO;
import ar.edu.utn.frba.dds.donaciones.integracion.LogisticaBroker;
import ar.edu.utn.frba.dds.donaciones.repository.DonacionRepository;
import ar.edu.utn.frba.dds.donaciones.service.DonanteService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Camiones que llevan donaciones en traslado de este donante. Logística no
 * conoce al donante: la lista de ids sale de acá, donde el dueño está verificado.
 */
@RestController
@RequestMapping("/api/donantes/{id}/camiones")
public class CamionesDelDonanteController {

    private final DonanteService donantes;
    private final DonacionRepository donaciones;
    private final LogisticaBroker logistica;

    public CamionesDelDonanteController(DonanteService donantes, DonacionRepository donaciones, LogisticaBroker logistica) {
        this.donantes = donantes;
        this.donaciones = donaciones;
        this.logistica = logistica;
    }

    @Operation(summary = "Camiones que llevan donaciones en traslado de este donante")
    @GetMapping
    public List<CamionEnCursoDTO> camiones(@PathVariable Long id) {
        donantes.buscarPorId(id);
        Specification<Donacion> enTraslado = (raiz, consulta, cb) -> cb.and(
                cb.equal(raiz.get("donante").get("id"), id),
                cb.equal(raiz.get("estadoActual"), EstadoTrack.EN_TRASLADO));
        List<Integer> ids = donaciones.findAll(enTraslado).stream()
                .map(donacion -> Math.toIntExact(donacion.getId()))
                .toList();
        return logistica.camionesDe(ids);
    }
}
