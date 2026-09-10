package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.dto.EntidadBeneficiariaDTO;
import ar.edu.utn.frba.dds.donaciones.service.EntidadBeneficiariaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Entidades beneficiarias", description = "Gestión de entidades beneficiarias registradas")
@RestController
@RequestMapping("/api/entidades")
public class EntidadBeneficiariaController {

    private final EntidadBeneficiariaService entidadService;

    public EntidadBeneficiariaController(EntidadBeneficiariaService entidadService) {
        this.entidadService = entidadService;
    }

    @GetMapping
    public List<EntidadBeneficiariaDTO> obtenerTodas() {
        return entidadService.obtenerTodas();
    }

    @GetMapping("/{id}")
    public EntidadBeneficiariaDTO obtenerPorId(@PathVariable Long id) {
        return entidadService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntidadBeneficiariaDTO crear(@Valid @RequestBody EntidadBeneficiariaDTO dto) {
        return entidadService.crear(dto);
    }

    @PutMapping("/{id}")
    public EntidadBeneficiariaDTO actualizar(
            @PathVariable Long id,
            @RequestBody EntidadBeneficiariaDTO dto) {
        return entidadService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        entidadService.eliminar(id);
    }
}
