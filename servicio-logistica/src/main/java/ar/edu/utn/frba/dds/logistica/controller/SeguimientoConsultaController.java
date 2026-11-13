package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.domain.rutas.GestorRutas;
import ar.edu.utn.frba.dds.logistica.dto.CamionEnCursoDTO;
import ar.edu.utn.frba.dds.logistica.dto.EnvioPropioDTO;
import ar.edu.utn.frba.dds.logistica.seguridad.UsuarioToken;
import ar.edu.utn.frba.dds.logistica.service.ConsultaSeguimiento;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Consultas de seguimiento que no entregan la ruta completa.
 * GET /api/rutas sigue siendo de administración.
 */
@RestController
public class SeguimientoConsultaController {

    private final GestorRutas rutas;

    public SeguimientoConsultaController(GestorRutas rutas) {
        this.rutas = rutas;
    }

    @Operation(summary = "Paradas de una entidad, sin las demás paradas de la ruta")
    @GetMapping("/api/mis-envios")
    public List<EnvioPropioDTO> envios(@RequestParam long entidadId, Authentication auth) {
        boolean admin = UsuarioToken.tieneRol(auth, UsuarioToken.ADMIN);
        if (!admin && !UsuarioToken.entidadIds(auth).contains(entidadId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return ConsultaSeguimiento.envios(rutas.getRutas(), entidadId);
    }

    @Operation(summary = "Camiones que llevan las donaciones indicadas, sin el resto de la ruta")
    @PostMapping("/api/seguimiento/camiones")
    public List<CamionEnCursoDTO> camiones(@RequestBody PedidoCamiones pedido) {
        List<Integer> ids = pedido.idsDonacion() == null ? List.of() : pedido.idsDonacion();
        return ConsultaSeguimiento.camiones(rutas.getRutas(), Set.copyOf(ids));
    }

    public record PedidoCamiones(@NotNull List<Integer> idsDonacion) {
    }
}
