package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.dto.DonacionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PaginaDTO;
import ar.edu.utn.frba.dds.donaciones.service.ConsultaAdministracionService;
import ar.edu.utn.frba.dds.donaciones.service.EntidadBeneficiariaService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Donaciones asignadas a una entidad, para su portal. Es el equivalente de /api/donantes/{id}/donaciones:
 * la entidad es parte de la ruta y SeguridadApiConfig solo deja pasar a un ADMIN o a quien representa a esa entidad.
 * Devuelve lo mismo que ya ve la entidad en GET /api/donaciones/{id} de cada una de sus donaciones.
 */
@RestController
@RequestMapping("/api/entidades/{id}/donaciones")
public class DonacionesDeLaEntidadController {

    private static final int TAMANIO_PAGINA = 100;

    private final ConsultaAdministracionService consultas;
    private final EntidadBeneficiariaService entidades;

    public DonacionesDeLaEntidadController(ConsultaAdministracionService consultas, EntidadBeneficiariaService entidades) {
        this.consultas = consultas;
        this.entidades = entidades;
    }

    @Operation(summary = "Donaciones asignadas a la entidad",
            description = "De la más reciente a la más antigua. 404 si la entidad no existe.")
    @GetMapping
    public List<DonacionDTO> listar(@PathVariable Long id) {
        entidades.buscarEntidad(id);
        List<DonacionDTO> todas = new ArrayList<>();
        int pagina = 0;
        PaginaDTO<DonacionDTO> actual;
        do {
            actual = consultas.donaciones("", null, null, null, null, id, pagina, TAMANIO_PAGINA);
            todas.addAll(actual.content());
            pagina++;
        } while (pagina < actual.totalPages());
        return todas;
    }
}
