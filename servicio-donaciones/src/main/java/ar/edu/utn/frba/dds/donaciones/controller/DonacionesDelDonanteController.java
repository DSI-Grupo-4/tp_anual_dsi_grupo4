package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Categoria;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import ar.edu.utn.frba.dds.donaciones.dto.DonacionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PaginaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.TimeStampDTO;
import ar.edu.utn.frba.dds.donaciones.repository.DonacionRepository;
import ar.edu.utn.frba.dds.donaciones.service.ConsultaAdministracionService;
import ar.edu.utn.frba.dds.donaciones.service.DonacionService;
import ar.edu.utn.frba.dds.donaciones.service.DonanteService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
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
    private final DonacionService donacionService;

    public DonacionesDelDonanteController(ConsultaAdministracionService consultas, DonanteService donantes,
                                          DonacionRepository donaciones, DonacionService donacionService) {
        this.consultas = consultas;
        this.donantes = donantes;
        this.donaciones = donaciones;
        this.donacionService = donacionService;
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Donaciones del donante",
            description = "Paginadas, de la más reciente a la más antigua. Filtros opcionales: texto en la descripción de los bienes (q), estado, categoría, subcategoría y entidad destino (entidadId). 404 si el donante no existe.")
    @GetMapping
    public PaginaDTO<DonacionDTO> listar(@PathVariable Long id,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) EstadoTrack estado,
            @RequestParam(required = false) Categoria categoria,
            @RequestParam(required = false) Subcategoria subcategoria,
            @RequestParam(required = false) Long entidadId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        donantes.buscarPorId(id);
        int tamanio = Math.max(1, Math.min(size, TAMANIO_MAXIMO));
        return consultas.donaciones(q.strip(), estado, categoria, subcategoria, id, entidadId, Math.max(0, page), tamanio);
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

    @io.swagger.v3.oas.annotations.Operation(summary = "Una donación del donante",
            description = "404 si el donante no existe o si la donación es de otro donante.")
    @GetMapping("/{donacionId}")
    public DonacionDTO detalle(@PathVariable Long id, @PathVariable Long donacionId) {
        return delDonante(id, donacionId);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Historial de estados de una donación del donante",
            description = "404 si el donante no existe o si la donación es de otro donante.")
    @GetMapping("/{donacionId}/historial")
    public List<TimeStampDTO> historial(@PathVariable Long id, @PathVariable Long donacionId) {
        delDonante(id, donacionId);
        return donacionService.obtenerHistorial(donacionId);
    }

    /** La donación, solo si pertenece al donante. Si es de otro, responde igual que si no existiera. */
    private DonacionDTO delDonante(Long id, Long donacionId) {
        donantes.buscarPorId(id);
        DonacionDTO donacion = donacionService.obtenerPorId(donacionId);
        if (donacion == null || !id.equals(donacion.getDonanteId())) {
            throw new NoSuchElementException("No existe la donación " + donacionId + " del donante " + id);
        }
        return donacion;
    }
}
