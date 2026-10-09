package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.dto.DonanteDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaHumanaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaJuridicaDTO;
import ar.edu.utn.frba.dds.donaciones.service.DonanteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Donantes", description = "Alta, edición e importación masiva por CSV de personas donantes (humanas o jurídicas)")
@RestController
@RequestMapping("/api/donantes")
public class DonanteController {

    private final DonanteService donanteService;

    public DonanteController(DonanteService donanteService) {
        this.donanteService = donanteService;
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Obtener todos", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @GetMapping
    public List<DonanteDTO> obtenerTodos() {
        return donanteService.obtenerTodos();
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Obtener por id", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @GetMapping("/{id}")
    public DonanteDTO obtenerPorId(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Long id) {
        return donanteService.buscarPorId(id);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Crear humano", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @PostMapping("/humanos")
    @ResponseStatus(HttpStatus.CREATED)
    public DonanteDTO crearHumano(@Valid @RequestBody PersonaHumanaDTO dto) {
        return donanteService.crearDonanteHumano(dto);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Crear juridico", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @PostMapping("/juridicos")
    @ResponseStatus(HttpStatus.CREATED)
    public DonanteDTO crearJuridico(@Valid @RequestBody PersonaJuridicaDTO dto) {
        return donanteService.crearDonanteJuridico(dto);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Actualizar", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @PutMapping("/{id}")
    public DonanteDTO actualizar(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Long id, @RequestBody DonanteDTO dto) {
        // PUT reemplaza el estado completo: el cliente debe mandar todos los
        // campos que quiere conservar (semántica estándar de PUT). El tipo
        // (humano/jurídico) NO lo decide el body — es inmutable y se
        // determina en DonanteService a partir del donante real ya guardado;
        // antes se confiaba en dto.getTipo() y un mismatch tiraba
        // ClassCastException (500 sin manejar, ver scan de calidad).
        return donanteService.actualizar(id, dto);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Eliminar", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Long id) {
        donanteService.eliminar(id);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Importar", description = "Seleccionar el archivo CSV de ejemplo en src/data. La importación puede emitir notificaciones y requiere RabbitMQ.")
    @PostMapping(value = "/importar", consumes = "multipart/form-data")
    public List<DonanteDTO> importar(
            @io.swagger.v3.oas.annotations.Parameter(description = "Seleccionar CSV UTF-8; ejemplo incluido en servicio-donaciones/src/data.") @RequestParam MultipartFile archivo) {

        return donanteService.importarCSV(archivo);
    }
}
