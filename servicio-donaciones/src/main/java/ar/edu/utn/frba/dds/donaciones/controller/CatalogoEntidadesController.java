package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.dto.FichaPublicaEntidadDTO;
import ar.edu.utn.frba.dds.donaciones.dto.NecesidadDTO;
import ar.edu.utn.frba.dds.donaciones.service.EntidadBeneficiariaService;
import ar.edu.utn.frba.dds.donaciones.service.NecesidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consulta del portal del donante. No reemplaza el ABM de /api/entidades:
 * esa sigue siendo de administración y de la propia entidad.
 */
@Tag(name = "Catálogo de entidades", description = "Ficha pública para quien dona")
@RestController
@RequestMapping("/api/catalogo/entidades")
public class CatalogoEntidadesController {

    private final EntidadBeneficiariaService entidades;
    private final NecesidadService necesidades;

    public CatalogoEntidadesController(EntidadBeneficiariaService entidades, NecesidadService necesidades) {
        this.entidades = entidades;
        this.necesidades = necesidades;
    }

    @Operation(summary = "Entidades activas, sin datos de contacto")
    @GetMapping
    public List<FichaPublicaEntidadDTO> activas() {
        return entidades.fichasActivas();
    }

    @Operation(summary = "Ficha de una entidad, sin calle ni representantes")
    @GetMapping("/{id}")
    public FichaPublicaEntidadDTO ficha(@PathVariable Long id) {
        return entidades.fichaPublica(id);
    }

    @Operation(summary = "Necesidades abiertas de una entidad activa")
    @GetMapping("/{id}/necesidades")
    public List<NecesidadDTO> necesidadesAbiertas(@PathVariable Long id) {
        return necesidades.obtenerAbiertas(id);
    }
}
