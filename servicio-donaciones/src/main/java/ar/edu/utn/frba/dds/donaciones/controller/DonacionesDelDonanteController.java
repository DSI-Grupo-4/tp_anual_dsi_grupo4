package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Categoria;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import ar.edu.utn.frba.dds.donaciones.dto.DonacionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PaginaDTO;
import ar.edu.utn.frba.dds.donaciones.repository.DonacionRepository;
import ar.edu.utn.frba.dds.donaciones.service.ConsultaAdministracionService;
import ar.edu.utn.frba.dds.donaciones.service.DonanteService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Donaciones de un donante, para su portal (Persona 4). A diferencia de
 * /api/administracion/donaciones, el donante es parte de la ruta: cuando exista
 * autenticación (Persona 2), alcanza con verificar que {id} sea el del usuario.
 */
@RestController
@RequestMapping("/api/donantes/{id}/donaciones")
public class DonacionesDelDonanteController {

    private static final int TAMANIO_MAXIMO = 100;

    private final ConsultaAdministracionService consultas;
    private final DonanteService donantes;
    private final DonacionRepository donaciones;

    public DonacionesDelDonanteController(ConsultaAdministracionService consultas, DonanteService donantes,
                                          DonacionRepository donaciones) {
        this.consultas = consultas;
        this.donantes = donantes;
        this.donaciones = donaciones;
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Donaciones del donante",
            description = "Paginadas, de la más reciente a la más antigua. Filtros opcionales por estado, categoría y subcategoría. 404 si el donante no existe.")
    @GetMapping
    public PaginaDTO<DonacionDTO> listar(@PathVariable Long id,
            @RequestParam(required = false) EstadoTrack estado,
            @RequestParam(required = false) Categoria categoria,
            @RequestParam(required = false) Subcategoria subcategoria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        donantes.buscarPorId(id);
        int tamanio = Math.max(1, Math.min(size, TAMANIO_MAXIMO));
        return consultas.donaciones("", estado, categoria, subcategoria, id, null, Math.max(0, page), tamanio);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Cantidad de donaciones del donante por estado",
            description = "Incluye todos los estados, aunque tengan 0. 404 si el donante no existe.")
    @GetMapping("/resumen")
    public Map<EstadoTrack, Long> resumen(@PathVariable Long id) {
        donantes.buscarPorId(id);
        Map<EstadoTrack, Long> cantidades = new LinkedHashMap<>();
        for (EstadoTrack estado : EstadoTrack.values()) {
            Specification<Donacion> delDonanteEnEstado = (r, q, cb) -> cb.and(
                    cb.equal(r.get("donante").get("id"), id),
                    cb.equal(r.get("estadoActual"), estado));
            cantidades.put(estado, donaciones.count(delDonanteEnEstado));
        }
        return cantidades;
    }
}
