package ar.edu.utn.frba.dds.donaciones.controller;
import ar.edu.utn.frba.dds.donaciones.dto.*;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.*;
import ar.edu.utn.frba.dds.donaciones.service.ConsultaAdministracionService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
/** Lecturas administrativas paginadas. Integración de permisos ADMIN a cargo de Persona 2. */
@RestController
@RequestMapping("/api/administracion")
public class AdministracionController {
    private final ConsultaAdministracionService consultas;
    public AdministracionController(ConsultaAdministracionService consultas) { this.consultas=consultas; }
    @GetMapping("/resumen") public Map<String,Long> resumen() { return consultas.resumen(); }
    @GetMapping("/donantes") public PaginaDTO<DonanteDTO> donantes(@RequestParam(defaultValue="") String q,
        @RequestParam(required=false) Boolean activo, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        return consultas.donantes(q,activo,page,size);
    }
    @GetMapping("/entidades") public PaginaDTO<EntidadBeneficiariaDTO> entidades(@RequestParam(defaultValue="") String q,
        @RequestParam(required=false) Boolean activa, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        return consultas.entidades(q,activa,page,size);
    }
    @GetMapping("/donaciones") public PaginaDTO<DonacionDTO> donaciones(@RequestParam(defaultValue="") String q,
        @RequestParam(required=false) EstadoTrack estado, @RequestParam(required=false) Categoria categoria,
        @RequestParam(required=false) Subcategoria subcategoria, @RequestParam(required=false) Long donanteId,
        @RequestParam(required=false) Long entidadId,
        @RequestParam(required=false) @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate desde,
        @RequestParam(required=false) @org.springframework.format.annotation.DateTimeFormat(iso=org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate hasta,
        @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        return consultas.donaciones(q,estado,categoria,subcategoria,donanteId,entidadId,desde,hasta,page,size);
    }
}
