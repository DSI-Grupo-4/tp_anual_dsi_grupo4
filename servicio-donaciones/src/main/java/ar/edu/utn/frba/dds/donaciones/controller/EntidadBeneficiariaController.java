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

    @io.swagger.v3.oas.annotations.Operation(summary = "Obtener todas", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @GetMapping
    public List<EntidadBeneficiariaDTO> obtenerTodas() {
        return entidadService.obtenerTodas();
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Obtener por id", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @GetMapping("/{id}")
    public EntidadBeneficiariaDTO obtenerPorId(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Long id) {
        return entidadService.obtenerPorId(id);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Crear", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntidadBeneficiariaDTO crear(@Valid @RequestBody EntidadBeneficiariaDTO dto) {
        return entidadService.crear(dto);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Actualizar", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @PutMapping("/{id}")
    public EntidadBeneficiariaDTO actualizar(
            @io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Long id,
            @Valid @RequestBody EntidadBeneficiariaDTO dto) {
        return entidadService.actualizar(id, dto);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Eliminar", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Long id) {
        entidadService.eliminar(id);
    }
}
